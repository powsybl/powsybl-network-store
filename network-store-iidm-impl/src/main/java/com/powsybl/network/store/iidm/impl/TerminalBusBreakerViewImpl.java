/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class TerminalBusBreakerViewImpl<U extends InjectionAttributes> implements Terminal.BusBreakerView {

    private final NetworkObjectIndex index;

    private final U attributes;

    private static final String NOT_FOUND = "not found";

    public TerminalBusBreakerViewImpl(NetworkObjectIndex index, U attributes) {
        this.index = index;
        this.attributes = attributes;
    }

    private Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(IllegalStateException::new).checkResource();
    }

    private boolean isNodeBeakerTopologyKind() {
        return getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER;
    }

    private boolean isBusBeakerTopologyKind() {
        return getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.BUS_BREAKER;
    }

    private void checkNodeBreakerTopology() {
        if (isNodeBeakerTopologyKind()) {
            throw new PowsyblException("Not supported in a node breaker topology");
        }
    }

    private List<Bus> calculateBuses() {
        return NodeBreakerTopology.INSTANCE.calculateBuses(index, getVoltageLevelResource()).values().stream().collect(Collectors.toList());
    }

    private Bus calculateBus() {
        return NodeBreakerTopology.INSTANCE.calculateBus(index, getVoltageLevelResource(), attributes.getNode());
    }

    @Override
    public Bus getBus() {
        if (isNodeBeakerTopologyKind()) { // calculated bus
            return calculateBus();
        } else {  // configured bus
            String busId = attributes.getBus();
            return busId != null ? index.getBus(busId).orElseThrow(() -> new AssertionError(busId + " " + NOT_FOUND)) : null;
        }
    }

    @Override
    public Bus getConnectableBus() {
        if (isBusBeakerTopologyKind()) { // Configured bus
            String busId = attributes.getConnectableBus();
            return index.getBus(busId).orElseThrow(() -> new AssertionError(busId + " " + NOT_FOUND));
        } else {  // Calculated bus
            Bus bus = getBus();
            if (bus != null) {
                return bus;
            } else {
                List<Bus> buses = calculateBuses();
                return buses.isEmpty() ? null : buses.get(0);
            }
        }
    }

    @Override
    public void setConnectableBus(String busId) {
        checkNodeBreakerTopology();

        if (index.getBus(busId).isEmpty()) {
            throw new PowsyblException(busId + " " + NOT_FOUND);
        }

        if (attributes.getConnectableBus().equals(busId)) {
            return;
        }

        attributes.setConnectableBus(busId);
        if (attributes.getBus() != null) {
            attributes.setBus(busId);
        }
        index.updateResource(attributes.getResource());

        index.getVoltageLevel(getVoltageLevelResource().getId()).orElseThrow(AssertionError::new).invalidateCalculatedBuses();
    }
}
