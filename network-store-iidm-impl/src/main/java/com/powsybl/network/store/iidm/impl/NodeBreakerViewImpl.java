/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.network.store.model.*;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerViewImpl implements VoltageLevel.NodeBreakerView {

    private final TopologyKind topologyKind;

    private final VoltageLevelImpl voltageLevel;

    private final NetworkObjectIndex index;

    public NodeBreakerViewImpl(TopologyKind topologyKind, VoltageLevelImpl voltageLevel, NetworkObjectIndex index) {
        this.topologyKind = topologyKind;
        this.voltageLevel = voltageLevel;
        this.index = index;
    }

    static NodeBreakerViewImpl create(TopologyKind topologyKind, VoltageLevelImpl voltageLevel, NetworkObjectIndex index) {
        return new NodeBreakerViewImpl(topologyKind, voltageLevel, index);
    }

    private Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return voltageLevel.getResource();
    }

    private boolean isBusBeakerTopologyKind() {
        return topologyKind == TopologyKind.BUS_BREAKER;
    }

    private void checkBusBreakerTopology() {
        if (isBusBeakerTopologyKind()) {
            throw new PowsyblException("Not supported in a bus breaker topology");
        }
    }

    @Override
    public int getMaximumNodeIndex() {
        checkBusBreakerTopology();

        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource());
        return graph.vertexSet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    @Override
    public int[] getNodes() {
        checkBusBreakerTopology();

        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource());
        return graph.vertexSet().stream()
                .mapToInt(Integer::intValue)
                .toArray();
    }

    @Override
    public BusbarSectionAdder newBusbarSection() {
        checkBusBreakerTopology();
        return new BusbarSectionAdderImpl(getVoltageLevelResource(), index);
    }

    @Override
    public List<BusbarSection> getBusbarSections() {
        checkBusBreakerTopology();
        return index.getBusbarSections(getVoltageLevelResource().getId());
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        checkBusBreakerTopology();
        return getBusbarSections().stream();
    }

    @Override
    public int getBusbarSectionCount() {
        checkBusBreakerTopology();
        return getBusbarSections().size();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        checkBusBreakerTopology();
        BusbarSection bbs = index.getBusbarSection(id).orElse(null);
        if (bbs != null && !bbs.getTerminal().getVoltageLevel().getId().equals(this.voltageLevel.getId())) {
            return null;
        }
        return bbs;
    }

    @Override
    public void traverse(int[] nodes, VoltageLevel.NodeBreakerView.TopologyTraverser traverser) {
        Objects.requireNonNull(traverser);
        checkBusBreakerTopology();

        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        Set<Integer> done = new HashSet<>();
        for (int node : nodes) {
            if (!traverseFromNode(graph, node, TraversalType.DEPTH_FIRST, traverser, done)) {
                break;
            }
        }
    }

    @Override
    public void traverse(int node, VoltageLevel.NodeBreakerView.TopologyTraverser traverser) {
        Objects.requireNonNull(traverser);
        checkBusBreakerTopology();
        traverseFromNode(node, TraversalType.DEPTH_FIRST, traverser);
    }

    boolean traverseFromNode(int node, TraversalType traversalType, VoltageLevel.NodeBreakerView.TopologyTraverser traverser) {
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        Set<Integer> done = new HashSet<>();
        return traverseFromNode(graph, node, traversalType, traverser, done);
    }

    private boolean traverseFromNode(Graph<Integer, Edge> graph, int node, TraversalType traversalType, VoltageLevel.NodeBreakerView.TopologyTraverser traverser,
                                     Set<Integer> done) {
        if (traversalType == TraversalType.DEPTH_FIRST) {   // traversal by depth first
            if (done.contains(node)) {
                return true;
            }
            done.add(node);

            for (Edge edge : graph.edgesOf(node)) {
                NodeBreakerBiConnectable biConnectable = edge.getBiConnectable();
                int nextNode = biConnectable.getNode1() == node ? biConnectable.getNode2() : biConnectable.getNode1();
                TraverseResult result;
                if (done.contains(nextNode)) {
                    continue;
                }
                if (biConnectable instanceof SwitchAttributes) {
                    result = traverseSwitch(traverser, biConnectable, node, nextNode);
                } else if (biConnectable instanceof InternalConnectionAttributes) {
                    result = traverser.traverse(node, null, nextNode);
                } else {
                    throw new AssertionError();
                }
                if (result == TraverseResult.CONTINUE) {
                    if (!traverseFromNode(graph, nextNode, traversalType, traverser, done)) {
                        return false;
                    }
                } else if (result == TraverseResult.TERMINATE_TRAVERSER) {
                    return false;
                }
            }
        } else {   // traversal by breadth first
            boolean keepGoing = true;
            Set<Edge> encounteredEdges = new HashSet<>();

            LinkedList<Integer> vertexToTraverse = new LinkedList<>();
            vertexToTraverse.offer(node);
            while (!vertexToTraverse.isEmpty()) {
                int firstV = vertexToTraverse.getFirst();
                vertexToTraverse.poll();
                if (done.contains(firstV)) {
                    continue;
                }
                done.add(firstV);

                Set<Edge> adjacentEdges = graph.edgesOf(firstV);

                for (Edge edge : adjacentEdges) {
                    if (encounteredEdges.contains(edge)) {
                        continue;
                    }
                    encounteredEdges.add(edge);

                    NodeBreakerBiConnectable biConnectable = edge.getBiConnectable();
                    int node1 = biConnectable.getNode1();
                    int node2 = biConnectable.getNode2();

                    TraverseResult traverserResult;
                    if (!done.contains(node1)) {
                        if (biConnectable instanceof SwitchAttributes) {
                            traverserResult = traverseSwitch(traverser, biConnectable, node2, node1);
                        } else if (biConnectable instanceof InternalConnectionAttributes) {
                            traverserResult = traverser.traverse(node2, null, node1);
                        } else {
                            throw new AssertionError();
                        }
                        if (traverserResult == TraverseResult.CONTINUE) {
                            vertexToTraverse.offer(node1);
                        } else if (traverserResult == TraverseResult.TERMINATE_TRAVERSER) {
                            keepGoing = false;
                        }
                    } else if (!done.contains(node2)) {
                        if (biConnectable instanceof SwitchAttributes) {
                            traverserResult = traverseSwitch(traverser, biConnectable, node1, node2);
                        } else if (biConnectable instanceof InternalConnectionAttributes) {
                            traverserResult = traverser.traverse(node1, null, node2);
                        } else {
                            throw new AssertionError();
                        }
                        if (traverserResult == TraverseResult.CONTINUE) {
                            vertexToTraverse.offer(node2);
                        } else if (traverserResult == TraverseResult.TERMINATE_TRAVERSER) {
                            keepGoing = false;
                        }
                    }
                    if (!keepGoing) {
                        break;
                    }
                }
                if (!keepGoing) {
                    break;
                }
            }
            return keepGoing;
        }

        return true;
    }

    private TraverseResult traverseSwitch(VoltageLevel.NodeBreakerView.TopologyTraverser traverser, NodeBreakerBiConnectable biConnectable, int node, int nextNode) {
        Resource<SwitchAttributes> resource = ((SwitchAttributes) biConnectable).getResource();
        SwitchImpl s = index.getSwitch(resource.getId()).orElseThrow(IllegalStateException::new);
        return traverser.traverse(node, s, nextNode);
    }

    /**
     * This is the method called when we traverse the topology stating from a terminal.
     */
    boolean traverseFromTerminal(Terminal terminal, Terminal.TopologyTraverser traverser, Set<Terminal> traversedTerminals, TraversalType traversalType) {
        checkBusBreakerTopology();
        Objects.requireNonNull(traverser);

        List<Terminal> nexTerminals = new ArrayList<>();
        if (!traverseFromNode(terminal.getNodeBreakerView().getNode(), traversalType, (node1, sw, node2) -> {
            if (sw != null) {
                TraverseResult result = traverser.traverse(sw);
                if (result != TraverseResult.CONTINUE) {
                    return result;
                }
            }

            Terminal terminalNode2 = getTerminal(node2);
            if (terminalNode2 != null && !traversedTerminals.contains(terminalNode2)) {
                traversedTerminals.add(terminalNode2);
                TraverseResult result = traverser.traverse(terminalNode2, true);
                if (result != TraverseResult.CONTINUE) {
                    return result;
                }
                nexTerminals.addAll(((TerminalImpl<?>) terminalNode2).getOtherSideTerminals());
            }

            return TraverseResult.CONTINUE;
        })) {
            return false;
        }

        for (Terminal nextTerminal : nexTerminals) {
            if (!((TerminalImpl<?>) nextTerminal).traverse(traverser, traversedTerminals, traversalType)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public SwitchAdder newSwitch() {
        checkBusBreakerTopology();
        return new SwitchAdderNodeBreakerImpl(getVoltageLevelResource(), index, null);
    }

    @Override
    public InternalConnectionAdder newInternalConnection() {
        checkBusBreakerTopology();
        return new InternalConnectionAdderNodeBreakerImpl(getVoltageLevelResource());
    }

    @Override
    public SwitchAdder newBreaker() {
        checkBusBreakerTopology();
        return new SwitchAdderNodeBreakerImpl(getVoltageLevelResource(), index, SwitchKind.BREAKER);
    }

    @Override
    public SwitchAdder newDisconnector() {
        checkBusBreakerTopology();
        return new SwitchAdderNodeBreakerImpl(getVoltageLevelResource(), index, SwitchKind.DISCONNECTOR);
    }

    @Override
    public List<Switch> getSwitches() {
        checkBusBreakerTopology();
        return index.getSwitches(getVoltageLevelResource().getId());
    }

    @Override
    public Stream<Switch> getSwitchStream(int node) {
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        return graph.edgesOf(node).stream()
                .filter(edge -> edge.getBiConnectable() instanceof SwitchAttributes)
                .map(edge -> {
                    Resource<SwitchAttributes> resource = ((SwitchAttributes) edge.getBiConnectable()).getResource();
                    return index.getSwitch(resource.getId()).orElseThrow(IllegalStateException::new);
                });
    }

    @Override
    public List<Switch> getSwitches(int node) {
        return getSwitchStream(node).collect(Collectors.toList());
    }

    @Override
    public IntStream getNodeInternalConnectedToStream(int node) {
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        return graph.edgesOf(node).stream()
                .filter(edge -> edge.getBiConnectable() instanceof InternalConnectionAttributes)
                .mapToInt(edge -> {
                    NodeBreakerBiConnectable biConnectable = edge.getBiConnectable();
                    return biConnectable.getNode1() == node ? biConnectable.getNode2() : biConnectable.getNode1();
                });
    }

    @Override
    public List<Integer> getNodesInternalConnectedTo(int node) {
        return getNodeInternalConnectedToStream(node).boxed().collect(Collectors.toList());
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        checkBusBreakerTopology();
        return getSwitches().stream();
    }

    @Override
    public int getSwitchCount() {
        checkBusBreakerTopology();
        return getSwitches().size();
    }

    @Override
    public void removeSwitch(String switchId) {
        checkBusBreakerTopology();
        Switch removedSwitch = getSwitch(switchId);
        index.notifyBeforeRemoval(removedSwitch);
        index.removeSwitch(switchId);
        index.notifyAfterRemoval(switchId);
    }

    public Switch getSwitch(String id) {
        checkBusBreakerTopology();
        return index.getSwitch(id)
                .orElse(null);
    }

    @Override
    public int getNode1(String switchId) {
        checkBusBreakerTopology();
        return ((SwitchImpl) getSwitch(switchId)).getNode1();
    }

    @Override
    public int getNode2(String switchId) {
        checkBusBreakerTopology();
        return ((SwitchImpl) getSwitch(switchId)).getNode2();
    }

    @Override
    public Terminal getTerminal(int node) {
        return getOptionalTerminal(node).orElse(null);
    }

    @Override
    public Optional<Terminal> getOptionalTerminal(int node) {
        checkBusBreakerTopology();

        // not yet optimized so this method has poor performance and will probably be optimized in the future
        // if responsible of performance issue
        Vertex vertex = NodeBreakerTopology.INSTANCE.buildVertices(index, getVoltageLevelResource())
                .stream()
                .filter(v -> v.getNode() == node)
                .findFirst()
                .orElse(null);

        if (vertex != null) {
            return Optional.of(AbstractTopology.getTerminal(index, vertex));
        }

        return Optional.empty();
    }

    @Override
    public Terminal getTerminal1(String switchId) {
        checkBusBreakerTopology();
        return getTerminal(getNode1(switchId));
    }

    @Override
    public Terminal getTerminal2(String switchId) {
        checkBusBreakerTopology();
        return getTerminal(getNode2(switchId));
    }

    @Override
    public List<InternalConnection> getInternalConnections() {
        checkBusBreakerTopology();
        return getVoltageLevelResource().getAttributes().getInternalConnections().stream().map(InternalConnectionImpl::create).collect(Collectors.toList());
    }

    @Override
    public Stream<InternalConnection> getInternalConnectionStream() {
        checkBusBreakerTopology();
        return getInternalConnections().stream();
    }

    @Override
    public int getInternalConnectionCount() {
        checkBusBreakerTopology();
        return getInternalConnections().size();
    }

    @Override
    public void removeInternalConnections(int node1, int node2) {
        if (!getVoltageLevelResource().getAttributes().getInternalConnections()
                .removeIf(attributes -> attributes.getNode1() == node1 && attributes.getNode2() == node2 ||
                                        attributes.getNode1() == node2 && attributes.getNode2() == node1)) {
            throw new PowsyblException("Internal connection not found between " + node1 + " and " + node2);
        }
    }

    @Override
    public boolean hasAttachedEquipment(int node) {
        // not sure
        return getTerminal(node) != null;
    }
}
