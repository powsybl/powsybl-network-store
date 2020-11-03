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
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.interfaces.MinimumSTCutAlgorithm;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TerminalImpl<U extends InjectionAttributes> implements Terminal, Validable {

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
        return index.getStoreClient().getVoltageLevel(index.getNetwork().getUuid(), attributes.getVoltageLevelId())
                .orElseThrow(IllegalStateException::new);
    }

    private Set<Integer> getBusbarSectionNodes(Resource<VoltageLevelAttributes> voltageLevelResource) {
        Set<Integer> busbarSectionNodes = index.getStoreClient().getVoltageLevelBusbarSections(index.getNetwork().getUuid(), voltageLevelResource.getId())
                .stream().map(resource -> resource.getAttributes().getNode())
                .collect(Collectors.toSet());
        return busbarSectionNodes;
    }

    private static Graph<Integer, Edge> filterBreakers(Graph<Integer, Edge> graph, Predicate<SwitchAttributes> filter) {
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

        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, true);
        Set<Integer> busbarSectionNodes = getBusbarSectionNodes(voltageLevelResource);
        Graph<Integer, Edge> filteredGraph = filterBreakers(graph, switchAttributes -> !(switchAttributes.getKind() == SwitchKind.DISCONNECTOR && switchAttributes.isOpen()));
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
                            done = true;
                        }
                    }
                }
                // we just need to process shortest path so we can skip the others
                break;
            }
        }

        return done;
    }

    @Override
    public boolean connect() {
        boolean done = false;

        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
        VoltageLevelAttributes voltageLevelAttributes = voltageLevelResource.getAttributes();
        if (voltageLevelAttributes.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            if (connectNodeBreaker(voltageLevelResource)) {
                done = true;
            }
        } else { // TopologyKind.BUS_BREAKER
            if (attributes.getBus() == null) {
                attributes.setBus(attributes.getConnectableBus());
                done = true;
            }
        }

        if (done) {
            // to invalidate calculated buses
            voltageLevelAttributes.setCalculatedBusesValid(false);
        }

        return done;
    }

    private boolean disconnectNodeBreaker(Resource<VoltageLevelAttributes> voltageLevelResource) {
        boolean done = false;

        // false as a last
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, false);
        Set<Integer> busbarSectionNodes = getBusbarSectionNodes(voltageLevelResource);

        // find minimal cuts from terminal to each of the busbar section
        Graph<Integer, Edge> filteredGraph = filterBreakers(graph, switchAttributes -> true);
        MinimumSTCutAlgorithm<Integer, Edge> minCutAlgo = new EdmondsKarpMFImpl<>(filteredGraph);
        for (int busbarSectionNode : busbarSectionNodes) {
            minCutAlgo.calculateMinCut(attributes.getNode(), busbarSectionNode);
            for (Edge edge : minCutAlgo.getCutEdges()) {
                if (edge.getBiConnectable() instanceof SwitchAttributes) {
                    SwitchAttributes switchAttributes = (SwitchAttributes) edge.getBiConnectable();
                    if (switchAttributes.getKind() == SwitchKind.BREAKER && !switchAttributes.isOpen()) {
                        switchAttributes.setOpen(true);
                        done = true;
                    }
                }
            }
        }

        return done;
    }

    @Override
    public boolean disconnect() {
        boolean done = false;

        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
        VoltageLevelAttributes voltageLevelAttributes = voltageLevelResource.getAttributes();
        if (voltageLevelAttributes.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            if (disconnectNodeBreaker(voltageLevelResource)) {
                done = true;
            }
        } else { // TopologyKind.BUS_BREAKER
            if (attributes.getBus() != null) {
                attributes.setBus(null);
                done = true;
            }
        }

        if (done) {
            // to invalidate calculated buses
            voltageLevelAttributes.setCalculatedBusesValid(false);
        }

        return done;
    }

    @Override
    public boolean isConnected() {
        return this.getBusView().getBus() != null;
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String getMessageHeader() {
        return "Terminal of connectable : " + connectable.getId();
    }
}
