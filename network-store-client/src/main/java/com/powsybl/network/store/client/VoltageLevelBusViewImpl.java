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
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    private Map<String, Bus> calculateBus() {
        if (voltageLevelResource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            return new NodeBreakerTopology().calculateBuses(index, voltageLevelResource);
        } else {
            return new BusBreakerTopology().calculateBuses(index, voltageLevelResource);
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
