/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.Objects;

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
        VoltageLevelImpl voltageLevel = index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(IllegalStateException::new);
        Resource<VoltageLevelAttributes> voltageLevelResource = voltageLevel.getResource();

        if (voltageLevelResource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            return NodeBreakerTopology.INSTANCE.calculateBus(index, voltageLevelResource, attributes.getNode());
        } else {
            return BusBreakerTopology.INSTANCE.calculateBus(index, voltageLevelResource, attributes.getBus());
        }
    }

    @Override
    public Bus getConnectableBus() {
        throw new UnsupportedOperationException("TODO");
    }
}
