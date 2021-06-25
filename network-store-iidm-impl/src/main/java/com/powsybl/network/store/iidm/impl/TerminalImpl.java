/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SwitchAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.interfaces.MinimumSTCutAlgorithm;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TerminalImpl<U extends InjectionAttributes> implements Terminal, Validable {

    private static final Set<ConnectableType> CONNECTABLE_WITH_SIDES_TYPES = Set.of(ConnectableType.LINE, ConnectableType.TWO_WINDINGS_TRANSFORMER, ConnectableType.THREE_WINDINGS_TRANSFORMER);

    private final NetworkObjectIndex index;

    private final U attributes;

    private final Connectable connectable;

    private final TerminalNodeBreakerViewImpl<U> nodeBreakerView;

    private final TerminalBusBreakerViewImpl<U> busBreakerView;

    private final TerminalBusViewImpl<U> busView;

    public TerminalImpl(NetworkObjectIndex index, U attributes, Connectable connectable) {
        this.index = index;
        this.attributes = attributes;
        this.connectable = connectable;
        nodeBreakerView = new TerminalNodeBreakerViewImpl<>(attributes);
        busBreakerView = new TerminalBusBreakerViewImpl<>(index, attributes);
        busView = new TerminalBusViewImpl<>(index, attributes);
    }

    static <U extends InjectionAttributes> TerminalImpl<U> create(NetworkObjectIndex index, U attributes, Connectable connectable) {
        return new TerminalImpl<>(index, attributes, connectable);
    }

    private boolean isNodeBeakerTopologyKind() {
        return getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER;
    }

    private boolean isBusBeakerTopologyKind() {
        return getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.BUS_BREAKER;
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
    public Connectable getConnectable() {
        return connectable;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(AssertionError::new);
    }

    @Override
    public double getP() {
        return attributes.getP();
    }

    @Override
    public Terminal setP(double p) {
        if (connectable.getType() == ConnectableType.BUSBAR_SECTION) {
            throw new ValidationException(this, "cannot set active power on a busbar section");
        }
        if (!Double.isNaN(p) && connectable.getType() == ConnectableType.SHUNT_COMPENSATOR) {
            throw new ValidationException(this, "cannot set active power on a shunt compensator");
        }
        attributes.setP(p);
        index.updateResource(attributes.getResource());
        return this;
    }

    @Override
    public double getQ() {
        return attributes.getQ();
    }

    @Override
    public Terminal setQ(double q) {
        if (connectable.getType() == ConnectableType.BUSBAR_SECTION) {
            throw new ValidationException(this, "cannot set reactive power on a busbar section");
        }
        attributes.setQ(q);
        index.updateResource(attributes.getResource());
        return this;
    }

    @Override
    public double getI() {
        if (connectable.getType() == ConnectableType.BUSBAR_SECTION) {
            return 0;
        }
        return isConnected() ? Math.hypot(getP(), getQ()) / (Math.sqrt(3.) * getBusView().getBus().getV() / 1000) : 0;
    }

    private Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(IllegalStateException::new).getResource();
    }

    private Set<Integer> getBusbarSectionNodes(Resource<VoltageLevelAttributes> voltageLevelResource) {
        return index.getStoreClient().getVoltageLevelBusbarSections(index.getNetwork().getUuid(), VariantManagerImpl.INITIAL_VARIANT_NUM, voltageLevelResource.getId())
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
        BreadthFirstIterator<Integer, Edge> it = new BreadthFirstIterator<>(filteredGraph, attributes.getNode());
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

        closedSwitches.stream().forEach(switchId ->
                index.notifyUpdate(index.getSwitch(switchId).get(), "open", true, false)
        );

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
            if (attributes.getBus() == null) {
                attributes.setBus(attributes.getConnectableBus());
                index.updateResource(attributes.getResource());
                // Notification for branches (with sides) is made in the injection attributes adapters (setBus)
                if (!CONNECTABLE_WITH_SIDES_TYPES.contains(getConnectable().getType())) {
                    index.notifyUpdate(getConnectable(), "bus", null, attributes.getConnectableBus());
                }
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
                int aggregatedNode = nodeToAggregatedNode.get(attributes.getNode());
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

            openedSwitches.stream().forEach(switchId ->
                    index.notifyUpdate(index.getSwitch(switchId).get(), "open", false, true)
            );
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
            if (attributes.getBus() != null) {
                attributes.setBus(null);
                index.updateResource(attributes.getResource());
                // Notification for branches (with sides) is made in the injection attributes adapters (setBus)
                if (!CONNECTABLE_WITH_SIDES_TYPES.contains(getConnectable().getType())) {
                    index.notifyUpdate(getConnectable(), "bus", attributes.getConnectableBus(), null);
                }
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
            return (attributes.getBus() != null) && attributes.getBus().equals(attributes.getConnectableBus());
        }
    }

    @Override
    public String getMessageHeader() {
        return "Terminal of connectable : " + connectable.getId();
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser) {
        Set<Terminal> traversedTerminals = new HashSet<>();

        // One side
        traverse(traverser, traversedTerminals);

        // Other sides
        getSideTerminals().stream().forEach(ts -> ((TerminalImpl) ts).traverse(traverser, traversedTerminals));
    }

    void traverse(VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals) {
        if (traversedTerminals.contains(this)) {
            return;
        }

        traversedTerminals.add(this);
        if (!traverser.traverse(this, getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER ? isConnected() : true)) {
            return;
        }

        if (isBusBeakerTopologyKind()) {
            ((BusBreakerViewImpl) getVoltageLevel().getBusBreakerView()).traverse(this, traverser, traversedTerminals);
        } else if (isNodeBeakerTopologyKind()) {
            ((NodeBreakerViewImpl) getVoltageLevel().getNodeBreakerView()).traverse(this, traverser, traversedTerminals);
        }
    }

    Set<Terminal> getSideTerminals() {
        Set<Terminal> otherTerminals = new HashSet<>();
        if (getConnectable() instanceof Branch) {
            Branch<?> branch = (Branch) getConnectable();
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

}
