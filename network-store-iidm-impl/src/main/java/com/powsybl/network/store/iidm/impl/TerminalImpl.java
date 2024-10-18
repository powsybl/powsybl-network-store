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
import com.powsybl.network.store.iidm.impl.util.JGraphTGraph;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TerminalImpl<U extends IdentifiableAttributes> implements Terminal, Validable {

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

    protected boolean isNodeBeakerTopologyKind() {
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

    private double computePathWeight(List<Edge> path, Predicate<Switch> openOperableSwitch) {
        return path.stream().mapToDouble(edge -> computeEdgeWeight(edge, openOperableSwitch)).sum();
    }

    private double computeEdgeWeight(Edge edge, Predicate<Switch> openOperableSwitch) {
        return testSwitchFromEdge(edge, openOperableSwitch) ? 1d : 0d;
    }

    /**
     * Check that the edge corresponds to a switch and test the predicate on the switch
     */
    private boolean testSwitchFromEdge(Edge edge, Predicate<Switch> predicate) {
        if (edge.getBiConnectable() instanceof SwitchAttributes switchAttributes) {
            // Get the switch behind the switchAttributes
            Optional<SwitchImpl> sw = index.getSwitch(switchAttributes.getResource().getId());

            // Test the switch
            return sw.isPresent() && predicate.test(sw.get());
        }
        return false;
    }

    /**
     * Check if a switch from the edge is open but cannot be operated (according to the given predicate)
     * @param edge the edge to test
     * @param isSwitchOperable the predicate defining if a switch can be operated
     * @return <code>true</code> if the switch is open and cannot be operated
     */
    private boolean checkNonClosableSwitch(Edge edge, Predicate<Switch> isSwitchOperable) {
        return testSwitchFromEdge(edge, SwitchPredicates.IS_OPEN.and(isSwitchOperable.negate()));
    }

    /**
     * <p>This method is an adaptation of the same method from NodeBreakerVoltageLevel in powsybl-core, in order to keep
     * the same logic and the same results on both sides.</p>
     */
    boolean getConnectingSwitches(Predicate<Switch> isSwitchOperable, Set<SwitchImpl> switchForConnection) {
        boolean done = false;

        // Voltage level
        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();

        // Predicates useful
        Predicate<Switch> isOpenOperableSwitch = SwitchPredicates.IS_OPEN.and(isSwitchOperable);

        // Full graph of the network
        JGraphTGraph graph = new JGraphTGraph(NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, true, true));

        // Node of the present terminal (start of the paths)
        int node = getAttributes().getNode();

        // Nodes of the busbar sections (end of the paths)
        Set<Integer> busbarSectionNodes = getBusbarSectionNodes(voltageLevelResource);

        // find all paths starting from the current terminal to a busbar section that does not contain an open switch
        // that is not of the type of switch the user wants to operate
        // Paths are already sorted by the number of open switches and by the size of the paths
        List<List<Edge>> paths = graph.findAllPaths(node,
            busbarSectionNodes::contains,
            edge -> checkNonClosableSwitch(edge, isSwitchOperable),
            Comparator.comparing((List<Edge> list) -> computePathWeight(list, isOpenOperableSwitch))
                .thenComparing(List::size));

        // Close the switches on the shortest path if at least a path is found
        if (!paths.isEmpty()) {
            // the shortest path is the best
            List<Edge> shortestPath = paths.get(0);

            // close all open operable switches on the path
            shortestPath.stream()
                .filter(edge -> testSwitchFromEdge(edge, isOpenOperableSwitch))
                .forEach(edge -> {
                    if (edge.getBiConnectable() instanceof SwitchAttributes switchAttributes) {
                        // Get the switch behind the switchAttributes
                        Optional<SwitchImpl> sw = index.getSwitch(switchAttributes.getResource().getId());

                        // Add the switch to the list of switches to open
                        sw.ifPresent(switchForConnection::add);
                    }
                });
            done = true;
        }

        return done;
    }

    private boolean connectNodeBreaker(Predicate<Switch> isTypeSwitchToOperate) {

        // Set of switches that are to be closed
        Set<SwitchImpl> switchesToClose = new HashSet<>();

        if (isConnected()) {
            return false;
        }

        // Get the list of switches to open
        if (getConnectingSwitches(isTypeSwitchToOperate, switchesToClose)) {
            // Open the switches
            switchesToClose.forEach(sw -> sw.setOpen(false));
        } else {
            return false;
        }

        return true;
    }

    protected void connectBusBreaker() {
        getAbstractIdentifiable().updateResource(r -> {
            var a = getAttributes(r);
            a.setBus(a.getConnectableBus());

            // Notification to the listeners
            String side = Terminal.getConnectableSide(this).map(s -> Integer.toString(s.getNum())).orElse("");
            index.notifyUpdate(getConnectable(), "connected" + side, index.getNetwork().getVariantManager().getWorkingVariantId(), false, true);
        });
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
            boolean connectedBefore = isConnected();
            index.notifyUpdate(getConnectable(), "beginConnect", index.getNetwork().getVariantManager().getWorkingVariantId(), connectedBefore, null);
            if (isNodeBeakerTopologyKind()) {
                if (connectNodeBreaker(isTypeSwitchToOperate)) {
                    done = true;
                }
            } else { // TopologyKind.BUS_BREAKER
                // Check that the bus-breaker terminal has no bus defined (i.e. it is disconnected)
                if (getAttributes().getBus() == null) {
                    connectBusBreaker();
                    done = true;
                }
            }

            boolean connectedAfter = isConnected();
            index.notifyUpdate(getConnectable(), "endConnect", index.getNetwork().getVariantManager().getWorkingVariantId(), null, connectedAfter);

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
    boolean identifySwitchToOpenPath(List<Edge> path, Predicate<Switch> isSwitchOpenable, Set<SwitchImpl> switchesToOpen) {
        for (Edge edge : path) {
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

    private boolean isAnOpenSwitch(Edge edge) {
        return testSwitchFromEdge(edge, SwitchPredicates.IS_OPEN);
    }

    /**
     * <p>This method is an adaptation of the same method from NodeBreakerVoltageLevel in powsybl-core, in order to keep
     * the same logic and the same results on both sides.</p>
     */
    boolean getDisconnectingSwitches(Predicate<Switch> isSwitchOpenable, Set<SwitchImpl> switchesToOpen) {

        // Voltage level
        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();

        // Full graph of the network
        JGraphTGraph graph = new JGraphTGraph(NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, true, true));

        // Node of the present terminal (start of the paths)
        int node = getAttributes().getNode();

        // Nodes of the busbar sections (end of the paths)
        Set<Integer> busbarSectionNodes = getBusbarSectionNodes(voltageLevelResource);

        // find all paths starting from the current terminal to a busbar section that does not contain an open switch
        List<List<Edge>> paths = graph.findAllPaths(node,
            busbarSectionNodes::contains,
            this::isAnOpenSwitch,
            Comparator.comparing(List::size));
        if (paths.isEmpty()) {
            return false;
        }

        // Each path is visited and for each, the first openable switch found is added in the set of switches to open
        for (List<Edge> path : paths) {
            // Identify the first openable switch on the path
            if (!identifySwitchToOpenPath(path, isSwitchOpenable, switchesToOpen)) {
                // If no such switch was found, return false immediately
                return false;
            }
        }
        return true;
    }

    private boolean disconnectNodeBreaker(Predicate<Switch> isSwitchOpenable) {

        // Set of switches that are to be opened
        Set<SwitchImpl> switchesToOpen = new HashSet<>();

        // Get the list of switches to open
        if (getDisconnectingSwitches(isSwitchOpenable, switchesToOpen)) {
            // Open the switches
            switchesToOpen.forEach(sw -> sw.setOpen(true));
        } else {
            return false;
        }

        return true;
    }

    protected boolean disconnectBusBreaker() {
        var attributes = getAttributes();
        if (attributes.getBus() != null) {
            getAbstractIdentifiable().updateResource(resource -> {
                var a = getAttributes(resource);
                a.setBus(null);

                // Notification to the listeners
                String side = Terminal.getConnectableSide(this).map(s -> Integer.toString(s.getNum())).orElse("");
                index.notifyUpdate(getConnectable(), "connected" + side, index.getNetwork().getVariantManager().getWorkingVariantId(), true, false);
            });
            return true;
        }
        return false;
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
            boolean disconnectedBefore = !isConnected();
            index.notifyUpdate(getConnectable(), "beginDisconnect", index.getNetwork().getVariantManager().getWorkingVariantId(), disconnectedBefore, null);
            if (isNodeBeakerTopologyKind()) {
                if (disconnectNodeBreaker(isSwitchOpenable)) {
                    done = true;
                }
            } else { // TopologyKind.BUS_BREAKER
                if (disconnectBusBreaker()) {
                    done = true;
                }
            }

            boolean disconnectedAfter = !isConnected();
            index.notifyUpdate(getConnectable(), "endDisconnect", index.getNetwork().getVariantManager().getWorkingVariantId(), null, disconnectedAfter);

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

    @Override
    public ThreeSides getSide() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSide'");
    }

    public void addNewRegulatingPoint(RegulatingPoint regulatingPoint) {
        getAttributes().getRegulatingEquipments()
            .put(regulatingPoint.getRegulatingEquipmentId(), regulatingPoint.getRegulatingEquipmentType());
    }

    public void removeRegulatingPoint(RegulatingPoint regulatingPoint) {
        getAttributes().getRegulatingEquipments()
            .remove(regulatingPoint.getRegulatingEquipmentId());
    }

    public void removeAsRegulatingPoint() {
        getAttributes().getRegulatingEquipments().forEach((regulatingEquipmentId, resourceType) ->
            index.getRegulatingEquipment(regulatingEquipmentId, resourceType)
                .getRegulatingPoint().removeRegulation());
        getAttributes().getRegulatingEquipments().clear();
    }
}
