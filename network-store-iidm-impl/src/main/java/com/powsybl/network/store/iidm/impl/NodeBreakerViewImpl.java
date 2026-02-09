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
                .sorted()
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
        Set<Integer> encounteredVertices = new HashSet<>();
        Set<Edge> encounteredEdges = new HashSet<>();
        for (int node : nodes) {
            if (!traverseGraph(graph, node, traverser, TraversalType.DEPTH_FIRST, encounteredVertices, encounteredEdges)) {
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
        return traverseGraph(graph, node, traverser, traversalType, new HashSet<>(), new HashSet<>());
    }

    private record DirectedEdge(Edge edge, int origin) {
    }

    private TraverseResult traverseEdge(TopologyTraverser traverser, NodeBreakerBiConnectable biConnectable, int currentNode, int nextNode) {
        if (biConnectable instanceof SwitchAttributes switchAttributes) {
            SwitchImpl s = index.getSwitch(switchAttributes.getResource().getId()).orElseThrow(IllegalStateException::new);
            return traverser.traverse(currentNode, s, nextNode);
        } else if (biConnectable instanceof InternalConnectionAttributes) {
            return traverser.traverse(currentNode, null, nextNode);
        } else {
            throw new AssertionError("Unexpected connectable type: " + biConnectable.getClass());
        }
    }

    private static void traverseVertex(int vertex, Set<Integer> encounteredVertices, Deque<DirectedEdge> edgesToTraverse,
                                       Graph<Integer, Edge> graph, TraversalType traversalType) {
        if (!encounteredVertices.contains(vertex)) {
            encounteredVertices.add(vertex);
            List<Edge> edges = new ArrayList<>(graph.edgesOf(vertex));
            for (int i = 0; i < edges.size(); i++) {
                // For depth-first traversal, we're going to poll the last element added in the deque. Hence, edges have to
                // be added in reverse order, otherwise the depth-first traversal will be "on the right side" instead of
                // "on the left side" of the tree. That is, it would always go deeper taking with last neighbour of visited vertex.
                int iEdge = switch (traversalType) {
                    case DEPTH_FIRST -> edges.size() - i - 1;
                    case BREADTH_FIRST -> i;
                };
                edgesToTraverse.add(new DirectedEdge(edges.get(iEdge), vertex));
            }
        }
    }

    private boolean traverseGraph(Graph<Integer, Edge> graph, int node, TopologyTraverser traverser,
                                  TraversalType traversalType, Set<Integer> encounteredVertices, Set<Edge> encounteredEdges) {
        Deque<DirectedEdge> edgesToTraverse = new ArrayDeque<>();
        traverseVertex(node, encounteredVertices, edgesToTraverse, graph, traversalType);

        while (!edgesToTraverse.isEmpty()) {
            DirectedEdge directedEdge = switch (traversalType) {
                case DEPTH_FIRST -> edgesToTraverse.pollLast();
                case BREADTH_FIRST -> edgesToTraverse.pollFirst();
            };

            if (!encounteredEdges.contains(directedEdge.edge())) {
                encounteredEdges.add(directedEdge.edge());

                NodeBreakerBiConnectable biConnectable = directedEdge.edge().getBiConnectable();
                int nextNode = biConnectable.getNode1() == directedEdge.origin() ? biConnectable.getNode2() : biConnectable.getNode1();
                TraverseResult result = traverseEdge(traverser, biConnectable, directedEdge.origin(), nextNode);
                switch (result) {
                    case CONTINUE -> traverseVertex(nextNode, encounteredVertices, edgesToTraverse, graph, traversalType);
                    case TERMINATE_TRAVERSER -> {
                        return false;
                    }
                    case TERMINATE_PATH -> { /* path ends, continuing with next edge in the deque */ }
                }
            }
        }
        return true;
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

            return traverseTerminal(getTerminal(node2), traverser, traversedTerminals, nexTerminals);
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

    private TraverseResult traverseTerminal(Terminal terminal, Terminal.TopologyTraverser traverser, Set<Terminal> traversedTerminals, List<Terminal> nexTerminals) {
        if (terminal != null) {
            if (traversedTerminals.contains(terminal)) {
                return TraverseResult.TERMINATE_PATH;
            }
            traversedTerminals.add(terminal);
            TraverseResult result = traverser.traverse(terminal, true);
            if (result != TraverseResult.CONTINUE) {
                return result;
            }
            nexTerminals.addAll(((TerminalImpl<?>) terminal).getOtherSideTerminals());
        }

        return TraverseResult.CONTINUE;
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
        checkBusBreakerTopology();
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        return graph.edgesOf(node).stream()
                .filter(edge -> edge.getBiConnectable() instanceof SwitchAttributes)
                .map(edge -> {
                    Resource<SwitchAttributes> resource = ((SwitchAttributes) edge.getBiConnectable()).getResource();
                    return (Switch) index.getSwitch(resource.getId()).orElseThrow(IllegalStateException::new);
                })
                .distinct();
    }

    @Override
    public List<Switch> getSwitches(int node) {
        return getSwitchStream(node).toList();
    }

    @Override
    public IntStream getNodeInternalConnectedToStream(int node) {
        checkBusBreakerTopology();
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        return graph.edgesOf(node).stream()
                .filter(edge -> edge.getBiConnectable() instanceof InternalConnectionAttributes)
                .mapToInt(edge -> {
                    NodeBreakerBiConnectable biConnectable = edge.getBiConnectable();
                    return biConnectable.getNode1() == node ? biConnectable.getNode2() : biConnectable.getNode1();
                })
                .distinct();
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

    @Override
    public double getFictitiousP0(int node) {
        Map<Integer, Double> nodeToFictitiousP0 = getVoltageLevelResource().getAttributes().getNodeToFictitiousP0();
        return nodeToFictitiousP0 == null ? 0.0 : nodeToFictitiousP0.getOrDefault(node, 0.0);
    }

    @Override
    public VoltageLevel.NodeBreakerView setFictitiousP0(int node, double p0) {
        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
        Map<Integer, Double> nodeToFictitiousP0 = voltageLevelResource.getAttributes().getNodeToFictitiousP0();
        if (nodeToFictitiousP0 == null) {
            nodeToFictitiousP0 = new HashMap<>();
        }
        nodeToFictitiousP0.put(node, p0);
        voltageLevelResource.getAttributes().setNodeToFictitiousP0(nodeToFictitiousP0);
        index.updateVoltageLevelResource(voltageLevelResource);
        return this;
    }

    @Override
    public double getFictitiousQ0(int node) {
        Map<Integer, Double> nodeToFictitiousQ0 = getVoltageLevelResource().getAttributes().getNodeToFictitiousQ0();
        return nodeToFictitiousQ0 == null ? 0.0 : nodeToFictitiousQ0.getOrDefault(node, 0.0);
    }

    @Override
    public VoltageLevel.NodeBreakerView setFictitiousQ0(int node, double q0) {
        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
        Map<Integer, Double> nodeToFictitiousQ0 = voltageLevelResource.getAttributes().getNodeToFictitiousQ0();
        if (nodeToFictitiousQ0 == null) {
            nodeToFictitiousQ0 = new HashMap<>();
        }
        nodeToFictitiousQ0.put(node, q0);
        voltageLevelResource.getAttributes().setNodeToFictitiousQ0(nodeToFictitiousQ0);
        index.updateVoltageLevelResource(voltageLevelResource);
        return this;
    }
}
