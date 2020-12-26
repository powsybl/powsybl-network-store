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

    private Bus calculateBus(boolean includeOpenSwitches) {
        return isNodeBeakerTopologyKind() ?
                getTopologyInstance().calculateBus(index, getVoltageLevelResource(), attributes.getNode(), includeOpenSwitches, true) :
                getTopologyInstance().calculateBus(index, getVoltageLevelResource(), attributes.getBus(), includeOpenSwitches, true);
    }

    private List<Bus> calculateBuses(boolean includeOpenSwitches) {
        return getTopologyInstance().calculateBuses(index, getVoltageLevelResource(), includeOpenSwitches, true).values().stream().collect(Collectors.toList());
    }

    @Override
    public Bus getBus() {
        return calculateBus(false);
    }

    @Override
    public Bus getConnectableBus() {
        if (isBusBeakerTopologyKind()) { // Merged bus
            return index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(IllegalStateException::new).getBusView().getMergedBus(attributes.getConnectableBus());
        } else { // Calculated bus
            Bus bus = getBus();
            if (bus != null) {
                return bus;
            }

            // FIXME need to traverse the graph from the terminal to possibly find the associated bus
            // Find a node connected with all open switches who has a calculated bus
            List<Bus> buses = calculateBuses(false);
            getVoltageLevelResource().getAttributes().setCalculatedBusesValid(false); // Force a calculation
            Bus calculateBus = calculateBus(true);
            getVoltageLevelResource().getAttributes().setCalculatedBusesValid(false); // Force a calculation
            if (calculateBus != null) {
                return calculateBus;
            }

            return buses.isEmpty() ? null : buses.get(0);
        }
    }
}
