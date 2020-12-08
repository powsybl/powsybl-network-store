/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class BusBreakerViewImpl implements VoltageLevel.BusBreakerView {

    private final TopologyKind topologyKind;

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    public BusBreakerViewImpl(TopologyKind topologyKind, Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.topologyKind = topologyKind;
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    static BusBreakerViewImpl create(TopologyKind topologyKind, Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        return new BusBreakerViewImpl(topologyKind, voltageLevelResource, index);
    }

    private void checkNodeBreakerTopology() {
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            throw new PowsyblException("Not supported in a node breaker topology");
        }
    }

    @Override
    public List<Bus> getBuses() {
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            // calculated buses
            throw new UnsupportedOperationException("TODO");
        } else {
            // configured buses
            return index.getBuses(voltageLevelResource.getId());
        }
    }

    @Override
    public Stream<Bus> getBusStream() {
        return getBuses().stream();
    }

    @Override
    public Bus getBus(String busId) {
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            // calculated bus
            throw new UnsupportedOperationException("TODO");
        } else {
            // configured bus
            return index.getBus(busId).filter(bus1 -> bus1.getVoltageLevel().getId().equals(voltageLevelResource.getId()))
                    .orElse(null);
        }
    }

    @Override
    public BusAdder newBus() {
        checkNodeBreakerTopology(); // we can only add configured bus in a bus/breaker topo
        return new ConfiguredBusAdderImpl(voltageLevelResource, index);
    }

    @Override
    public void removeBus(String busId) {
        checkNodeBreakerTopology(); // we can only remove configured bus in a bus/breaker topo
        Bus removedBus = getBus(busId);
        index.removeBus(busId);
        index.notifyRemoval(removedBus);
    }

    @Override
    public void removeAllBuses() {
        checkNodeBreakerTopology(); // we can only remove configured buses in a bus/breaker topo
        getBuses().forEach(bus -> removeBus(bus.getId()));
    }

    @Override
    public List<Switch> getSwitches() {
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            return index.getSwitches(voltageLevelResource.getId());
        } else {
            return index.getSwitches(voltageLevelResource.getId()).stream().filter(Switch::isRetained).collect(Collectors.toList());
        }
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return getSwitches().stream();
    }

    @Override
    public int getSwitchCount() {
        return getSwitches().size();
    }

    @Override
    public void removeSwitch(String switchId) {
        SwitchImpl switchToRemove = getSwitchOrThrowException(switchId);
        index.removeSwitch(switchId);
        index.notifyRemoval(switchToRemove);
    }

    @Override
    public void removeAllSwitches() {
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            getSwitches().forEach(s -> removeSwitch(s.getId()));
        } else {
            // only remove retained switches
            throw new UnsupportedOperationException("TODO");
        }
    }

    @Override
    public Bus getBus1(String switchId) {
        SwitchImpl aSwitch = getSwitchOrThrowException(switchId);
        return index.getBus(aSwitch.getBus1()).orElse(null);
    }

    @Override
    public Bus getBus2(String switchId) {
        SwitchImpl aSwitch = getSwitchOrThrowException(switchId);
        return index.getBus(aSwitch.getBus2()).orElse(null);
    }

    public Optional<SwitchImpl> getOptionalSwitch(String switchId) {
        return index.getSwitch(switchId).filter(aSwitch -> topologyKind == TopologyKind.NODE_BREAKER || aSwitch.isRetained());
    }

    public SwitchImpl getSwitchOrThrowException(String switchId) {
        return getOptionalSwitch(switchId).orElseThrow(() -> new PowsyblException("switch " + switchId + " doesn't exist"));
    }

    @Override
    public Switch getSwitch(String switchId) {
        return getOptionalSwitch(switchId).orElse(null);
    }

    @Override
    public SwitchAdder newSwitch() {
        checkNodeBreakerTopology();
        return new SwitchAdderBusBreakerImpl(voltageLevelResource, index);
    }
}
