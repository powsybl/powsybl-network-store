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
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private boolean isNodeBeakerTopologyKind() {
        return getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER;
    }

    private boolean isBusBeakerTopologyKind() {
        return getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.BUS_BREAKER;
    }

    private <T> AbstractTopology<T> getTopologyInstance() {
        return isNodeBeakerTopologyKind() ?
                (AbstractTopology<T>) NodeBreakerTopology.INSTANCE : (AbstractTopology<T>) BusBreakerTopology.INSTANCE;
    }

    private Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(IllegalStateException::new).getResource();
    }

    private Bus calculateBus() {
        return isNodeBeakerTopologyKind() ?
                getTopologyInstance().calculateBus(index, getVoltageLevelResource(), attributes.getNode(), false, true) :
                getTopologyInstance().calculateBus(index, getVoltageLevelResource(), attributes.getBus(), false, true);
    }

    @Override
    public Bus getBus() {
        return calculateBus();
    }

    @Override
    public Bus getConnectableBus() {
        VoltageLevelImpl voltageLevel = index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(IllegalStateException::new);
        if (isBusBeakerTopologyKind()) { // Merged bus
            return voltageLevel.getBusView().getMergedBus(attributes.getConnectableBus());
        } else { // Calculated bus
            return findConnectableBus();
        }
    }

    private Bus findConnectableBus() {
        VoltageLevelImpl voltageLevel = index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(IllegalStateException::new);

        final Bus[] foundBus = {getBus()};

        if (foundBus[0] != null) { // connected ?
            return foundBus[0];
        }

        VoltageLevel.TopologyTraverser topologyTraverser = new VoltageLevel.TopologyTraverser() {
            @Override
            public boolean traverse(Terminal terminal, boolean connected) {
                if (foundBus[0] != null) {
                    return false;
                }
                foundBus[0] = terminal.getBusView().getBus();
                return foundBus[0] == null;
            }

            @Override
            public boolean traverse(Switch aSwitch) {
                return foundBus[0] == null;
            }
        };

        voltageLevel.getNodeBreakerView().getTerminal(attributes.getNode()).traverse(topologyTraverser);
        if (foundBus[0] != null) {
            return foundBus[0];
        }

        List<Bus> buses = voltageLevel.getBusView().getBusStream().collect(Collectors.toList());
        return buses.isEmpty() ? null : buses.get(0);
    }
}
