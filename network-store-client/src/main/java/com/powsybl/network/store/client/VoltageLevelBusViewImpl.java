/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.math.graph.UndirectedGraph;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SwitchAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.*;
import java.util.stream.Stream;

import static com.powsybl.network.store.client.NodeBreakerTopologyUtil.buildNodeBreakerGraph;
import static com.powsybl.network.store.client.NodeBreakerTopologyUtil.traverseBusView;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelBusViewImpl implements VoltageLevel.BusView {

    private final NetworkObjectIndex index;

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    public VoltageLevelBusViewImpl(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource) {
        this.index = index;
        this.voltageLevelResource = voltageLevelResource;
    }

    private Map<String, Bus> calculateNodeBreakerBus() {
        // build the graph
        UndirectedGraph<Vertex, Resource<SwitchAttributes>> graph = buildNodeBreakerGraph(index, voltageLevelResource);

        // calculate buses
        boolean[] encountered = new boolean[graph.getVertexCapacity()];
        Arrays.fill(encountered, false);
        Map<String, Bus> calculateBuses = new HashMap<>();
        for (int e : graph.getEdges()) {
            traverseBusView(index, voltageLevelResource, graph, graph.getEdgeVertex1(e), encountered, calculateBuses);
            traverseBusView(index, voltageLevelResource, graph, graph.getEdgeVertex2(e), encountered, calculateBuses);
        }

        return calculateBuses;
    }

    private Map<String, Bus> calculateBus() {
        if (voltageLevelResource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            return calculateNodeBreakerBus();
        } else {
            throw new UnsupportedOperationException("TODO");
        }
    }

    @Override
    public List<Bus> getBuses() {
        return new ArrayList<>(calculateBus().values());
    }

    @Override
    public Stream<Bus> getBusStream() {
        return getBuses().stream();
    }

    @Override
    public Bus getBus(String id) {
        return calculateBus().get(id);
    }

    @Override
    public Bus getMergedBus(String s) {
        throw new UnsupportedOperationException("TODO");
    }
}
