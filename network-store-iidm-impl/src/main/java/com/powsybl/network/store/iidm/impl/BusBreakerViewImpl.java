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

    private void checkTopologyKind() {
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            throw new PowsyblException("Not supported in a node breaker topology");
        }
    }

    @Override
    public List<Bus> getBuses() {
        checkTopologyKind();
        return index.getBuses(voltageLevelResource.getId());
    }

    @Override
    public Stream<Bus> getBusStream() {
        checkTopologyKind();
        return getBuses().stream();
    }

    @Override
    public Bus getBus(String busId) {
        checkTopologyKind();
        return index.getBus(busId).filter(bus1 -> bus1.getVoltageLevel().getId().equals(voltageLevelResource.getId()))
                .orElse(null);
    }

    @Override
    public BusAdder newBus() {
        checkTopologyKind();
        return new ConfiguredBusAdderImpl(voltageLevelResource, index);
    }

    @Override
    public void removeBus(String busId) {
        checkTopologyKind();
        Bus removedBus = getBus(busId);
        index.removeBus(busId);
        index.notifyRemoval(removedBus);
    }

    @Override
    public void removeAllBuses() {
        checkTopologyKind();
        getBuses().forEach(bus -> removeBus(bus.getId()));
    }

    @Override
    public List<Switch> getSwitches() {
        checkTopologyKind();
        return index.getSwitches(voltageLevelResource.getId());
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        checkTopologyKind();
        return getSwitches().stream();
    }

    @Override
    public int getSwitchCount() {
        checkTopologyKind();
        return getSwitches().size();
    }

    @Override
    public void removeSwitch(String switchId) {
        checkTopologyKind();
        Switch removedSwitch = getSwitch(switchId);
        index.removeSwitch(switchId);
        index.notifyRemoval(removedSwitch);
    }

    @Override
    public void removeAllSwitches() {
        checkTopologyKind();
        getSwitches().forEach(s -> removeSwitch(s.getId()));
    }

    @Override
    public Bus getBus1(String switchId) {
        checkTopologyKind();
        SwitchImpl aSwitch = index.getSwitch(switchId).orElseThrow(() -> new PowsyblException("switch " + switchId + " doesn't exist"));
        return index.getBus(aSwitch.getBus1()).orElse(null);
    }

    @Override
    public Bus getBus2(String switchId) {
        checkTopologyKind();
        SwitchImpl aSwitch = index.getSwitch(switchId).orElseThrow(() -> new PowsyblException("switch " + switchId + " doesn't exist"));
        return index.getBus(aSwitch.getBus2()).orElse(null);
    }

    @Override
    public Switch getSwitch(String switchId) {
        checkTopologyKind();
        return index.getSwitch(switchId).orElse(null);
    }

    @Override
    public SwitchAdder newSwitch() {
        checkTopologyKind();
        return new SwitchAdderBusBreakerImpl(voltageLevelResource, index);
    }
}
