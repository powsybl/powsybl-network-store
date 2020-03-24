/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.math.graph.UndirectedGraph;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SwitchAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.powsybl.network.store.client.NodeBreakerTopologyUtil.buildNodeBreakerGraph;
import static com.powsybl.network.store.client.NodeBreakerTopologyUtil.traverseBusView;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalBusViewImpl<U extends InjectionAttributes> implements Terminal.BusView {

    private final NetworkObjectIndex index;

    private final U attributes;

    TerminalBusViewImpl(NetworkObjectIndex index, U attributes) {
        this.index = Objects.requireNonNull(index);
        this.attributes = attributes;
    }

    @Override
    public Bus getBus() {
        Resource<VoltageLevelAttributes> voltageLevelResource = index.getStoreClient().getVoltageLevel(index.getNetwork().getUuid(),
                                                                                                       attributes.getVoltageLevelId())
                .orElseThrow(IllegalStateException::new);

        // build the graph
        UndirectedGraph<Vertex, Resource<SwitchAttributes>> graph = buildNodeBreakerGraph(index, voltageLevelResource);

        // calculate bus starting from terminal node
        boolean[] encountered = new boolean[graph.getVertexCapacity()];
        Arrays.fill(encountered, false);
        Map<String, Bus> calculateBuses = new HashMap<>();
        traverseBusView(index, voltageLevelResource, graph, attributes.getNode(), encountered, calculateBuses);

        return calculateBuses.isEmpty() ? null : calculateBuses.entrySet().iterator().next().getValue();
    }

    @Override
    public Bus getConnectableBus() {
        throw new UnsupportedOperationException("TODO");
    }
}
