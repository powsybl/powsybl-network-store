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
import org.jgrapht.Graphs;

import java.util.*;
import java.util.function.Function;
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
        return index.getBusbarSection(id)
                .orElse(null);
    }

    @Override
    public void traverse(int[] nodes, VoltageLevel.NodeBreakerView.TopologyTraverser traverser) {
        Objects.requireNonNull(traverser);
        checkBusBreakerTopology();

        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        Set<Integer> done = new HashSet<>();
        for (int node : nodes) {
            if (!traverseFromNode(graph, node, traverser, done)) {
                break;
            }
        }
    }

    @Override
    public void traverse(int node, VoltageLevel.NodeBreakerView.TopologyTraverser traverser) {
        Objects.requireNonNull(traverser);
        checkBusBreakerTopology();
        traverseFromNode(node, traverser);
    }

    boolean traverseFromNode(int node, VoltageLevel.NodeBreakerView.TopologyTraverser traverser) {
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        Set<Integer> done = new HashSet<>();
        return traverseFromNode(graph, node, traverser, done);
    }

    private boolean traverseFromNode(Graph<Integer, Edge> graph, int node, VoltageLevel.NodeBreakerView.TopologyTraverser traverser,
                                     Set<Integer> done) {
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
                if (!traverseFromNode(graph, nextNode, traverser, done)) {
                    return false;
                }
            } else if (result == TraverseResult.TERMINATE_TRAVERSER) {
                return false;
            }
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
    boolean traverseFromTerminal(Terminal terminal, Terminal.TopologyTraverser traverser, Set<Terminal> traversedTerminals) {
        checkBusBreakerTopology();
        Objects.requireNonNull(traverser);

        List<Terminal> nexTerminals = new ArrayList<>();
        if (!traverseFromNode(terminal.getNodeBreakerView().getNode(), (node1, sw, node2) -> {
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
            if (!((TerminalImpl<?>) nextTerminal).traverse(traverser, traversedTerminals)) {
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
                .removeIf(internalConnectionAttributes -> internalConnectionAttributes.getNode1() == node1
                        && internalConnectionAttributes.getNode2() == node2)) {
            throw new PowsyblException("Internal connection not found between " + node1 + " and " + node2);
        }
    }

    @Override
    public boolean hasAttachedEquipment(int node) {
        // not sure
        return getTerminal(node) != null;
    }

    private void removeDanglingSwitches(int node, Graph<Integer, Edge> graph, Map<Integer, Vertex> vertices, Set<Integer> done) {
        done.add(node);
        Vertex vertex = vertices.get(node);
        for (int neighborNode : Graphs.neighborSetOf(graph, node)) {
            if (done.contains(neighborNode)) {
                continue;
            }
            Edge neighborEdge = graph.getEdge(node, neighborNode);
            if (vertex == null && Graphs.neighborSetOf(graph, node).size() <= 2 && neighborEdge.getBiConnectable() instanceof SwitchAttributes) {
                removeSwitch(((SwitchAttributes) neighborEdge.getBiConnectable()).getResource().getId());
                removeDanglingSwitches(neighborNode, graph, vertices, done);
            }
        }
    }

    public void removeDanglingSwitches(int node) {
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, getVoltageLevelResource(), true, true);
        Map<Integer, Vertex> vertices = NodeBreakerTopology.INSTANCE.buildVertices(index, getVoltageLevelResource())
                .stream()
                .collect(Collectors.toMap(Vertex::getNode, Function.identity()));

        removeDanglingSwitches(node, graph, vertices, new HashSet<>());
    }
}
