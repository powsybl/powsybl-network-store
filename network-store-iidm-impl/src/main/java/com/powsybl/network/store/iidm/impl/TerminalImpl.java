/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.network.store.model.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.interfaces.MinimumSTCutAlgorithm;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TerminalImpl<U extends IdentifiableAttributes> implements Terminal, Validable {

    private static final Set<IdentifiableType> CONNECTABLE_WITH_SIDES_TYPES = Set.of(IdentifiableType.LINE, IdentifiableType.TWO_WINDINGS_TRANSFORMER, IdentifiableType.THREE_WINDINGS_TRANSFORMER);

    private final NetworkObjectIndex index;

    private final Connectable<?> connectable;

    private final Function<Resource<U>, InjectionAttributes> attributesGetter;

    private final TerminalNodeBreakerViewImpl<U> nodeBreakerView;

    private final TerminalBusBreakerViewImpl<U> busBreakerView;

    private final TerminalBusViewImpl<U> busView;

    public TerminalImpl(NetworkObjectIndex index, Connectable<?> connectable, Function<Resource<U>, InjectionAttributes> attributesGetter) {
        this.index = index;
        this.connectable = connectable;
        this.attributesGetter = attributesGetter;
        nodeBreakerView = new TerminalNodeBreakerViewImpl<>(index, connectable, attributesGetter);
        busBreakerView = new TerminalBusBreakerViewImpl<>(index, connectable, attributesGetter);
        busView = new TerminalBusViewImpl<>(index, connectable, attributesGetter);
    }

    private TopologyKind getTopologyKind() {
        return getVoltageLevelResource().getAttributes().getTopologyKind();
    }

    private boolean isNodeBeakerTopologyKind() {
        return getTopologyKind() == TopologyKind.NODE_BREAKER;
    }

    private boolean isBusBeakerTopologyKind() {
        return getTopologyKind() == TopologyKind.BUS_BREAKER;
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public TerminalBusViewImpl<U> getBusView() {
        return busView;
    }

    @Override
    public Connectable<?> getConnectable() {
        return connectable;
    }

    private AbstractIdentifiableImpl<?, U> getAbstractIdentifiable() {
        return (AbstractIdentifiableImpl<?, U>) connectable;
    }

    private InjectionAttributes getAttributes(Resource<U> r) {
        return attributesGetter.apply(r);
    }

    private InjectionAttributes getAttributes() {
        return getAttributes(getAbstractIdentifiable().getResource());
    }

    @Override
    public VoltageLevelImpl getVoltageLevel() {
        if (getAbstractIdentifiable().getOptionalResource().isEmpty()) {
            return null;
        }
        return index.getVoltageLevel(getAttributes().getVoltageLevelId()).orElseThrow(AssertionError::new);
    }

    @Override
    public double getP() {
        return getAttributes().getP();
    }

    @Override
    public Terminal setP(double p) {
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            throw new ValidationException(this, "cannot set active power on a busbar section");
        }
        getAbstractIdentifiable().updateResource(r -> getAttributes().setP(p), AttributeFilter.SV);
        return this;
    }

    @Override
    public double getQ() {
        return getAttributes().getQ();
    }

    @Override
    public Terminal setQ(double q) {
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            throw new ValidationException(this, "cannot set reactive power on a busbar section");
        }
        getAbstractIdentifiable().updateResource(r -> getAttributes().setQ(q), AttributeFilter.SV);
        return this;
    }

    @Override
    public double getI() {
        if (connectable.getType() == IdentifiableType.BUSBAR_SECTION) {
            return 0;
        }
        return isConnected() ? Math.hypot(getP(), getQ()) / (Math.sqrt(3.) * getBusView().getBus().getV() / 1000) : Double.NaN;
    }

    private Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return index.getVoltageLevel(getAttributes().getVoltageLevelId()).orElseThrow(IllegalStateException::new).getResource();
    }

    private Set<Integer> getBusbarSectionNodes(Resource<VoltageLevelAttributes> voltageLevelResource) {
        return index.getStoreClient().getVoltageLevelBusbarSections(index.getNetwork().getUuid(), index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream().map(resource -> resource.getAttributes().getNode())
                .collect(Collectors.toSet());
    }

    private static Graph<Integer, Edge> filterSwitches(Graph<Integer, Edge> graph, Predicate<SwitchAttributes> filter) {
        return new AsSubgraph<>(graph, null, graph.edgeSet()
                .stream()
                .filter(edge -> {
                    if (edge.getBiConnectable() instanceof SwitchAttributes) {
                        SwitchAttributes switchAttributes = (SwitchAttributes) edge.getBiConnectable();
                        return filter.test(switchAttributes);
                    }
                    return true;
                })
                .collect(Collectors.toSet()));
    }

    private boolean connectNodeBreaker(Resource<VoltageLevelAttributes> voltageLevelResource) {
        boolean done = false;

        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, true, true);
        Set<Integer> busbarSectionNodes = getBusbarSectionNodes(voltageLevelResource);

        // exclude open disconnectors and open fictitious breakers to be able to calculate a shortest path without this
        // elements that are not allowed to be closed
        Predicate<SwitchAttributes> isOpenDisconnector = switchAttributes -> switchAttributes.getKind() != SwitchKind.BREAKER && switchAttributes.isOpen();
        Predicate<SwitchAttributes> isOpenFictitiousBreaker = switchAttributes -> switchAttributes.getKind() == SwitchKind.BREAKER && switchAttributes.isOpen() && switchAttributes.isFictitious();
        Graph<Integer, Edge> filteredGraph = filterSwitches(graph, isOpenDisconnector.negate().or(isOpenFictitiousBreaker.negate()));

        Set<String> closedSwitches = new HashSet<>();
        BreadthFirstIterator<Integer, Edge> it = new BreadthFirstIterator<>(filteredGraph, getAttributes().getNode());
        while (it.hasNext()) {
            int node = it.next();
            if (busbarSectionNodes.contains(node)) {
                // close all switches along spanning tree path
                for (Integer parentNode = node; parentNode != null; parentNode = it.getParent(parentNode)) {
                    Edge edge = it.getSpanningTreeEdge(parentNode);
                    if (edge != null && edge.getBiConnectable() instanceof SwitchAttributes) {
                        SwitchAttributes switchAttributes = (SwitchAttributes) edge.getBiConnectable();
                        if (switchAttributes.getKind() == SwitchKind.BREAKER && switchAttributes.isOpen()) {
                            switchAttributes.setOpen(false);
                            index.updateSwitchResource(switchAttributes.getResource());
                            closedSwitches.add(switchAttributes.getResource().getId());
                            done = true;
                        }
                    }
                }
                // we just need to process shortest path so we can skip the others
                break;
            }
        }

        closedSwitches.forEach(switchId -> index.notifyUpdate(index.getSwitch(switchId).orElseThrow(), "open", true, false));

        return done;
    }

    @Override
    public boolean connect() {
        boolean done = false;

        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
        VoltageLevelAttributes voltageLevelAttributes = voltageLevelResource.getAttributes();
        if (isNodeBeakerTopologyKind()) {
            if (connectNodeBreaker(voltageLevelResource)) {
                done = true;
            }
        } else { // TopologyKind.BUS_BREAKER
            var attributes = getAttributes();
            if (attributes.getBus() == null) {
                getAbstractIdentifiable().updateResource(r -> {
                    var a = getAttributes(r);
                    a.setBus(a.getConnectableBus());
                    // Notification for branches (with sides) is made in the injection attributes adapters (setBus)
                    if (!CONNECTABLE_WITH_SIDES_TYPES.contains(getConnectable().getType())) {
                        index.notifyUpdate(getConnectable(), "bus", null, a.getConnectableBus());
                    }
                });
                done = true;
            }
        }

        if (done) {
            // to invalidate calculated buses
            voltageLevelAttributes.setCalculatedBusesValid(false);
            index.updateVoltageLevelResource(voltageLevelResource);
        }

        return done;
    }

    private boolean disconnectNodeBreaker(Resource<VoltageLevelAttributes> voltageLevelResource) {
        boolean done = false;

        // create a graph with only closed switches
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, false, true);
        Set<Integer> busbarSectionNodes = getBusbarSectionNodes(voltageLevelResource);

        // inspect connectivity of graph without its non fictitious breakers to check if disconnection is possible
        Predicate<SwitchAttributes> isSwitchOpenable = switchAttributes -> switchAttributes.getKind() == SwitchKind.BREAKER && !switchAttributes.isFictitious() && !switchAttributes.isOpen();
        ConnectivityInspector<Integer, Edge> connectivityInspector
                = new ConnectivityInspector<>(filterSwitches(graph, isSwitchOpenable.negate()));

        List<Set<Integer>> connectedSets = connectivityInspector.connectedSets();
        if (connectedSets.size() == 1) {
            // it means that terminal is connected to a busbar section through disconnectors only
            // so there is no way to disconnect the terminal
        } else {
            // build an aggregated graph where we only keep breakers
            int aggregatedNodeCount = 0;
            Graph<Integer, Edge> breakerOnlyGraph = new Pseudograph<>(Edge.class);
            Map<Integer, Integer> nodeToAggregatedNode = new HashMap<>();

            for (Set<Integer> connectedSet : connectedSets) {
                breakerOnlyGraph.addVertex(aggregatedNodeCount);
                for (int node : connectedSet) {
                    nodeToAggregatedNode.put(node, aggregatedNodeCount);
                }
                aggregatedNodeCount++;
            }
            for (Edge edge : graph.edgeSet()) {
                if (edge.getBiConnectable() instanceof SwitchAttributes) {
                    SwitchAttributes switchAttributes = (SwitchAttributes) edge.getBiConnectable();
                    if (isSwitchOpenable.test(switchAttributes)) {
                        int node1 = graph.getEdgeSource(edge);
                        int node2 = graph.getEdgeTarget(edge);
                        int aggregatedNode1 = nodeToAggregatedNode.get(node1);
                        int aggregatedNode2 = nodeToAggregatedNode.get(node2);
                        breakerOnlyGraph.addEdge(aggregatedNode1, aggregatedNode2, edge);
                    }
                }
            }

            Set<String> openedSwitches = new HashSet<>();
            // find minimal cuts from terminal to each of the busbar section in the aggregated breaker only graph
            // so that we can open the minimal number of breaker to disconnect the terminal
            MinimumSTCutAlgorithm<Integer, Edge> minCutAlgo = new EdmondsKarpMFImpl<>(breakerOnlyGraph);
            for (int busbarSectionNode : busbarSectionNodes) {
                int aggregatedNode = nodeToAggregatedNode.get(getAttributes().getNode());
                int busbarSectionAggregatedNode = nodeToAggregatedNode.get(busbarSectionNode);
                // if that terminal is connected to a busbar section through disconnectors only or that terminal is a busbar section
                // so there is no way to disconnect the terminal
                if (aggregatedNode == busbarSectionAggregatedNode) {
                    continue;
                }
                minCutAlgo.calculateMinCut(aggregatedNode, busbarSectionAggregatedNode);
                for (Edge edge : minCutAlgo.getCutEdges()) {
                    if (edge.getBiConnectable() instanceof SwitchAttributes) {
                        SwitchAttributes switchAttributes = (SwitchAttributes) edge.getBiConnectable();
                        switchAttributes.setOpen(true);
                        index.updateSwitchResource(switchAttributes.getResource());
                        openedSwitches.add(switchAttributes.getResource().getId());
                        done = true;
                    }
                }
            }

            openedSwitches.forEach(switchId -> index.notifyUpdate(index.getSwitch(switchId).orElseThrow(), "open", false, true));
        }

        return done;
    }

    @Override
    public boolean disconnect() {
        boolean done = false;

        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
        VoltageLevelAttributes voltageLevelAttributes = voltageLevelResource.getAttributes();
        if (isNodeBeakerTopologyKind()) {
            if (disconnectNodeBreaker(voltageLevelResource)) {
                done = true;
            }
        } else { // TopologyKind.BUS_BREAKER
            var attributes = getAttributes();
            if (attributes.getBus() != null) {
                getAbstractIdentifiable().updateResource(r -> {
                    var a = getAttributes(r);
                    a.setBus(null);
                    // Notification for branches (with sides) is made in the injection attributes adapters (setBus)
                    if (!CONNECTABLE_WITH_SIDES_TYPES.contains(getConnectable().getType())) {
                        index.notifyUpdate(getConnectable(), "bus", a.getConnectableBus(), null);
                    }
                });
                done = true;
            }
        }

        if (done) {
            // to invalidate calculated buses
            voltageLevelAttributes.setCalculatedBusesValid(false);
            index.updateVoltageLevelResource(voltageLevelResource);
        }

        return done;
    }

    @Override
    public boolean isConnected() {
        if (isNodeBeakerTopologyKind()) {
            return this.getBusView().getBus() != null;
        } else {
            var attributes = getAttributes();
            return (attributes.getBus() != null) && attributes.getBus().equals(attributes.getConnectableBus());
        }
    }

    @Override
    public String getMessageHeader() {
        return "Terminal of connectable : " + connectable.getId();
    }

    @Override
    public void traverse(Terminal.TopologyTraverser traverser) {
        Set<Terminal> traversedTerminals = new HashSet<>();
        if (getAbstractIdentifiable().getOptionalResource().isEmpty()) {
            throw new PowsyblException("Associated equipment is removed");
        }

        // One side
        if (!traverse(traverser, traversedTerminals)) {
            return;
        }

        // Other sides
        for (Terminal otherSideTerminal : getOtherSideTerminals()) {
            if (!((TerminalImpl<?>) otherSideTerminal).traverse(traverser, traversedTerminals)) {
                return;
            }
        }
    }

    boolean traverse(Terminal.TopologyTraverser traverser, Set<Terminal> traversedTerminals) {
        if (traversedTerminals.contains(this)) {
            return true;
        }

        traversedTerminals.add(this);
        VoltageLevelImpl voltageLevel = index.getVoltageLevel(getAttributes().getVoltageLevelId()).orElseThrow(IllegalStateException::new);
        boolean connected = voltageLevel.getTopologyKind() != TopologyKind.BUS_BREAKER || isConnected();
        TraverseResult result = traverser.traverse(this, connected);
        if (result != TraverseResult.CONTINUE) {
            return result == TraverseResult.TERMINATE_PATH;
        }

        TopologyKind topologyKind = getTopologyKind();
        switch (topologyKind) {
            case NODE_BREAKER:
                return ((NodeBreakerViewImpl) voltageLevel.getNodeBreakerView()).traverseFromTerminal(this, traverser, traversedTerminals);
            case BUS_BREAKER:
                return ((BusBreakerViewImpl) voltageLevel.getBusBreakerView()).traverseFromTerminal(this, traverser, traversedTerminals);
            default:
                throw new IllegalStateException("Unknown topology kind: " + topologyKind);
        }
    }

    Set<Terminal> getOtherSideTerminals() {
        Set<Terminal> otherTerminals = new HashSet<>();
        if (getConnectable() instanceof Branch) {
            Branch<?> branch = (Branch<?>) getConnectable();
            if (branch.getTerminal1() == this) {
                otherTerminals.add(branch.getTerminal2());
            } else if (branch.getTerminal2() == this) {
                otherTerminals.add(branch.getTerminal1());
            } else {
                throw new AssertionError();
            }
        } else if (getConnectable() instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer ttc = (ThreeWindingsTransformer) getConnectable();
            if (ttc.getLeg1().getTerminal() == this) {
                otherTerminals.add(ttc.getLeg2().getTerminal());
                otherTerminals.add(ttc.getLeg3().getTerminal());
            } else if (ttc.getLeg2().getTerminal() == this) {
                otherTerminals.add(ttc.getLeg1().getTerminal());
                otherTerminals.add(ttc.getLeg3().getTerminal());
            } else if (ttc.getLeg3().getTerminal() == this) {
                otherTerminals.add(ttc.getLeg1().getTerminal());
                otherTerminals.add(ttc.getLeg2().getTerminal());
            } else {
                throw new AssertionError();
            }
        }

        return otherTerminals;
    }

    public String getVoltageLevelId() {
        return getAttributes().getVoltageLevelId();
    }
}
