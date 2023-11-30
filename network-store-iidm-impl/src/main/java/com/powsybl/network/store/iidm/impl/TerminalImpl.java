/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SwitchPredicates;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.network.store.model.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.AsWeightedGraph;

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
            throw new ValidationException(this, " cannot set active power on a busbar section");
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
            throw new ValidationException(this, " cannot set reactive power on a busbar section");
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
                if (edge.getBiConnectable() instanceof SwitchAttributes switchAttributes) {
                    return filter.test(switchAttributes);
                }
                return true;
            })
            .collect(Collectors.toSet()));
    }

    private double computeEdgeWeight(Edge edge, Predicate<Switch> openOperableSwitch) {
        if (edge.getBiConnectable() instanceof SwitchAttributes switchAttributes) {
            // Get the switch behind the switchAttributes
            Optional<SwitchImpl> sw = index.getSwitch(switchAttributes.getResource().getId());

            // THe weight is 1 if the switch is operable and open, else 0
            return sw.isPresent() && openOperableSwitch.test(sw.get()) ? 1d : 0d;
        } else {
            return 0d;
        }
    }

    private boolean connectNodeBreaker(Resource<VoltageLevelAttributes> voltageLevelResource, Predicate<Switch> isTypeSwitchToOperate) {
        boolean done = false;

        // Predicates useful
        Predicate<Switch> openOperableSwitch = SwitchPredicates.IS_OPEN.and(isTypeSwitchToOperate);

        // Full graph of the network
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, true, true);
        AsWeightedGraph<Integer, Edge> weightedGraph = new AsWeightedGraph<>(graph,
            edge -> computeEdgeWeight(edge, openOperableSwitch),
            true,
            false);

        // Node of the present terminal (start of the paths)
        int node = getAttributes().getNode();

        // Nodes of the busbar sections (end of the paths)
        Set<Integer> busbarSectionNodes = getBusbarSectionNodes(voltageLevelResource);

        // Path validator: an edge can be added if it is closed or if the allows to operate it
        SwitchPathValidator switchPathValidator = new SwitchPathValidator(SwitchPredicates.IS_OPEN.negate().or(isTypeSwitchToOperate), index);

        // Find all paths from the source to the targets
        AllDirectedPaths<Integer, Edge> allDirectedPaths = new AllDirectedPaths<>(weightedGraph, switchPathValidator);
        List<GraphPath<Integer, Edge>> allPaths = allDirectedPaths.getAllPaths(Set.of(node), busbarSectionNodes, true, null);

        // Sort the paths by weight then by length
        allPaths.sort(Comparator.comparingDouble((GraphPath<Integer, Edge> path) -> path.getWeight()).thenComparingInt(GraphPath::getLength));

        // Close the shortest path
        Set<String> closedSwitches = new HashSet<>();
        if (!allPaths.isEmpty()) {
            // the shortest path is the best
            GraphPath<Integer, Edge> shortestPath = allPaths.get(0);

            // close all open operable switches on the path
            shortestPath.getEdgeList().stream()
                .filter(edge -> {
                    if (edge.getBiConnectable() instanceof SwitchAttributes switchAttributes) {
                        // Get the switch behind the switchAttributes
                        Optional<SwitchImpl> sw = index.getSwitch(switchAttributes.getResource().getId());

                        // THe weight is 1 if the switch is operable and open, else 0
                        return sw.isPresent() && openOperableSwitch.test(sw.get());
                    } else {
                        return false;
                    }
                })
                .forEach(edge -> {
                    if (edge.getBiConnectable() instanceof SwitchAttributes switchAttributes) {
                        switchAttributes.setOpen(false);
                        index.updateSwitchResource(switchAttributes.getResource());
                        closedSwitches.add(switchAttributes.getResource().getId());
                    }
                });
            done = true;
        }

        // Notify update
        closedSwitches.forEach(switchId -> index.notifyUpdate(index.getSwitch(switchId).orElseThrow(), "open", true, false));

        return done;
    }

    /**
     * Try to connect the terminal.<br/>
     * Depends on the working variant.
     * @param isTypeSwitchToOperate Predicate telling if a switch is considered operable. Examples of predicates are available in the class {@link SwitchPredicates}
     * @return true if terminal has been connected, false otherwise
     * @see VariantManager
     */
    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        boolean done = false;

        try {
            Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
            VoltageLevelAttributes voltageLevelAttributes = voltageLevelResource.getAttributes();
            if (isNodeBeakerTopologyKind()) {
                if (connectNodeBreaker(voltageLevelResource, isTypeSwitchToOperate)) {
                    done = true;
                }
            } else { // TopologyKind.BUS_BREAKER
                var attributes = getAttributes();
                if (attributes.getBus() == null) {
                    getAbstractIdentifiable().updateResource(r -> {
                        var a = getAttributes(r);
                        a.setBus(a.getConnectableBus());

                        // Notification to the listeners
                        index.notifyUpdate(getConnectable(), "connected", index.getNetwork().getVariantManager().getWorkingVariantId(), false, true);

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
        } catch (PowsyblException exception) {
            if (exception.getMessage().contains("Object has been removed in current variant")) {
                throw new PowsyblException("Cannot modify removed equipment", exception);
            } else {
                throw exception;
            }
        }

        return done;
    }

    /**
     * Try to connect the terminal, using by default the {@link SwitchPredicates} IS_NONFICTIONAL_BREAKER.<br/>
     * Depends on the working variant.
     * @return true if terminal has been connected, false otherwise
     * @see VariantManager
     */
    @Override
    public boolean connect() {
        return connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER);
    }

    /**
     * Add the first openable switch in the given path to the set of switches to open
     *
     * @param path             the path to open
     * @param isSwitchOpenable predicate used to know if a switch can be opened
     * @param switchesToOpen   set of switches to be opened
     * @return true if the path has been opened, else false
     */
    boolean identifySwitchToOpenPath(GraphPath<Integer, Edge> path, Predicate<? super Switch> isSwitchOpenable, Set<SwitchImpl> switchesToOpen) {
        for (Edge edge : path.getEdgeList()) {
            if (edge.getBiConnectable() instanceof SwitchAttributes switchAttributes) {
                // Get the switch behind the switchAttributes
                Optional<SwitchImpl> sw = index.getSwitch(switchAttributes.getResource().getId());

                // Test if the switch can be opened according to the predicate
                if (sw.isPresent() && isSwitchOpenable.test(sw.get())) {
                    switchesToOpen.add(sw.get());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean disconnectNodeBreaker(Resource<VoltageLevelAttributes> voltageLevelResource, Predicate<Switch> isSwitchOpenable) {
        // Full graph of the network
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, true, true);

        // Node of the present terminal (start of the paths)
        int node = getAttributes().getNode();

        // Nodes of the busbar sections (end of the paths)
        Set<Integer> busbarSectionNodes = getBusbarSectionNodes(voltageLevelResource);

        // Find all paths from the source to the targets
        AllDirectedPaths<Integer, Edge> allDirectedPaths = new AllDirectedPaths<>(graph);
        List<GraphPath<Integer, Edge>> allPaths = allDirectedPaths.getAllPaths(Set.of(node), busbarSectionNodes, true, null);

        // Set of switches that are to be opened
        Set<SwitchImpl> switchesToOpen = new HashSet<>(allPaths.size());

        // Each path is visited and for each, the first openable switch found is added in the set of switches to open
        for (GraphPath<Integer, Edge> path : allPaths) {
            // Identify the first openable switch on the path
            if (!identifySwitchToOpenPath(path, isSwitchOpenable, switchesToOpen)) {
                // If no such switch was found, return false immediately
                return false;
            }
        }

        // The switches are now opened
        Set<String> openedSwitches = new HashSet<>();
        switchesToOpen.forEach(switchImpl -> {
            switchImpl.setOpen(true);
            index.updateSwitchResource(switchImpl.getResource());
            openedSwitches.add(switchImpl.getResource().getId());
        });

        // Notify update
        openedSwitches.forEach(switchId -> index.notifyUpdate(index.getSwitch(switchId).orElseThrow(), "open", false, true));

        return true;
    }

    /**
     * Disconnect the terminal.<br/>
     * Depends on the working variant.
     * @param isSwitchOpenable Predicate telling if a switch is considered openable. Examples of predicates are available in the class {@link SwitchPredicates}
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        boolean done = false;

        try {
            Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
            VoltageLevelAttributes voltageLevelAttributes = voltageLevelResource.getAttributes();
            if (isNodeBeakerTopologyKind()) {
                if (disconnectNodeBreaker(voltageLevelResource, isSwitchOpenable)) {
                    done = true;
                }
            } else { // TopologyKind.BUS_BREAKER
                var attributes = getAttributes();
                if (attributes.getBus() != null) {
                    getAbstractIdentifiable().updateResource(resource -> {
                        var a = getAttributes(resource);
                        a.setBus(null);

                        // Notification to the listeners
                        index.notifyUpdate(getConnectable(), "connected", index.getNetwork().getVariantManager().getWorkingVariantId(), true, false);

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
        } catch (PowsyblException exception) {
            if (exception.getMessage().contains("Object has been removed in current variant")) {
                throw new PowsyblException("Cannot modify removed equipment", exception);
            } else {
                throw exception;
            }
        }

        return done;
    }

    /**
     * Disconnect the terminal, using by default the {@link SwitchPredicates} IS_CLOSED_BREAKER.<br/>
     * Depends on the working variant.
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    @Override
    public boolean disconnect() {
        return disconnect(SwitchPredicates.IS_CLOSED_BREAKER);
    }

    @Override
    public boolean isConnected() {
        if (isNodeBeakerTopologyKind()) {
            return this.getBusView().getBus() != null;
        } else {
            var attributes = getAttributes();
            return attributes.getBus() != null && attributes.getBus().equals(attributes.getConnectableBus());
        }
    }

    @Override
    public String getMessageHeader() {
        return "Terminal of connectable : " + connectable.getId();
    }

    @Override
    public void traverse(Terminal.TopologyTraverser traverser) {
        traverse(traverser, TraversalType.DEPTH_FIRST);
    }

    @Override
    public void traverse(Terminal.TopologyTraverser traverser, TraversalType traversalType) {
        Set<Terminal> traversedTerminals = new HashSet<>();
        if (getAbstractIdentifiable().getOptionalResource().isEmpty()) {
            throw new PowsyblException("Associated equipment is removed");
        }

        // One side
        if (!traverse(traverser, traversedTerminals, traversalType)) {
            return;
        }

        // Other sides
        for (Terminal otherSideTerminal : getOtherSideTerminals()) {
            if (!((TerminalImpl<?>) otherSideTerminal).traverse(traverser, traversedTerminals, traversalType)) {
                return;
            }
        }
    }

    boolean traverse(Terminal.TopologyTraverser traverser, Set<Terminal> traversedTerminals, TraversalType traversalType) {
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
        return switch (topologyKind) {
            case NODE_BREAKER ->
                ((NodeBreakerViewImpl) voltageLevel.getNodeBreakerView()).traverseFromTerminal(this, traverser, traversedTerminals, traversalType);
            case BUS_BREAKER ->
                ((BusBreakerViewImpl) voltageLevel.getBusBreakerView()).traverseFromTerminal(this, traverser, traversedTerminals, traversalType);
            default -> throw new IllegalStateException("Unknown topology kind: " + topologyKind);
        };
    }

    Set<Terminal> getOtherSideTerminals() {
        Set<Terminal> otherTerminals = new HashSet<>();
        if (getConnectable() instanceof Branch<?> branch) {
            if (branch.getTerminal1() == this) {
                otherTerminals.add(branch.getTerminal2());
            } else if (branch.getTerminal2() == this) {
                otherTerminals.add(branch.getTerminal1());
            } else {
                throw new AssertionError();
            }
        } else if (getConnectable() instanceof ThreeWindingsTransformer ttc) {
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
