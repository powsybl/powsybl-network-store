/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.*;
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

    private static boolean isOpenedDisconnector(Edge edge) {
        if (edge instanceof SwitchAttributes) {
            SwitchAttributes switchAttributes = (SwitchAttributes) edge;
            return switchAttributes.getKind() == SwitchKind.DISCONNECTOR && switchAttributes.isOpen();
        }
        return false;
    }

    private static List<List<Edge>> getAllPathsToBusbarSection(Graph<Integer, Edge> graph, int v, Set<Integer> busbarSectionNodes) {
        List<List<Edge>> allPaths = new ArrayList<>();
        getAllPathsToBusbarSection(graph, v, allPaths, new ArrayList<>(), new HashSet<>(), busbarSectionNodes);
        return allPaths;
    }

    private static void getAllPathsToBusbarSection(Graph<Integer, Edge> graph, int v, List<List<Edge>> allPaths,
                                                   List<Edge> path, Set<Integer> encountered, Set<Integer> busbarSectionNodes) {
        encountered.add(v);

        for (int v2 : Graphs.neighborSetOf(graph, v)) {
            if (encountered.contains(v2)) {
                continue;
            }

            Edge edge = graph.getEdge(v, v2);

            if (isOpenedDisconnector(edge)) {
                continue;
            }

            List<Edge> path2 = new ArrayList<>(path);
            path2.add(edge);

            if (busbarSectionNodes.contains(v2)) {
                allPaths.add(path2);
            } else {
                getAllPathsToBusbarSection(graph, v2, allPaths, path2, encountered, busbarSectionNodes);
            }
        }
    }

    private List<List<Edge>> getAllPathsToBusbarSection(Resource<VoltageLevelAttributes> voltageLevelResource) {
        // get all path from this terminal to a busbar section that do not encounter an opened disconnector
        Graph<Integer, Edge> graph = NodeBreakerTopology.INSTANCE.buildGraph(index, voltageLevelResource, true);
        Set<Integer> busbarSectionNodes = index.getStoreClient().getVoltageLevelBusbarSections(index.getNetwork().getUuid(), voltageLevelResource.getId())
                .stream().map(resource -> resource.getAttributes().getNode())
                .collect(Collectors.toSet());
        return getAllPathsToBusbarSection(graph, attributes.getNode(), busbarSectionNodes);
    }

    @Override
    public boolean connect() {
        boolean done = false;

        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
        VoltageLevelAttributes voltageLevelAttributes = voltageLevelResource.getAttributes();
        if (voltageLevelAttributes.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            List<List<Edge>> paths = getAllPathsToBusbarSection(voltageLevelResource);

            if (!paths.isEmpty()) {
                // sort paths by length
                paths.sort(Comparator.comparingInt(List::size));

                // close all switches of shortest path
                for (Edge edge : paths.get(0)) {
                    if (edge instanceof SwitchAttributes) {
                        SwitchAttributes switchAttributes = (SwitchAttributes) edge;
                        if (switchAttributes.isOpen()) {
                            switchAttributes.setOpen(false);
                            done = true;
                        }
                    }
                }
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

    @Override
    public boolean disconnect() {
        boolean done = false;

        Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevelResource();
        VoltageLevelAttributes voltageLevelAttributes = voltageLevelResource.getAttributes();
        if (voltageLevelAttributes.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            List<List<Edge>> paths = getAllPathsToBusbarSection(voltageLevelResource);

            if (!paths.isEmpty()) {
                // open first closed switch of all paths
                for (List<Edge> path : paths) {
                    for (Edge edge : path) {
                        if (edge instanceof SwitchAttributes) {
                            SwitchAttributes switchAttributes = (SwitchAttributes) edge;
                            if (!switchAttributes.isOpen()) {
                                switchAttributes.setOpen(true);
                                done = true;
                                break;
                            }
                        }
                    }
                }
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
