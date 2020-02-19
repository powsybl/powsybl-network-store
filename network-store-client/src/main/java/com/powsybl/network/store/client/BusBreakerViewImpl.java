/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusAdder;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class BusBreakerViewImpl implements VoltageLevel.BusBreakerView {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    public BusBreakerViewImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    static BusBreakerViewImpl create(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        return new BusBreakerViewImpl(voltageLevelResource, index);
    }

    @Override
    public List<Bus> getBuses() {
        return index.getBuses(voltageLevelResource.getId());
    }

    @Override
    public Stream<Bus> getBusStream() {
        return getBuses().stream();
    }

    @Override
    public Bus getBus(String busId) {
        return index.getBus(busId).filter(bus1 -> bus1.getVoltageLevel().getId().equals(voltageLevelResource.getId())).orElse(null);
    }

    @Override
    public BusAdder newBus() {
        return new ConfiguredBusAdderImpl(voltageLevelResource, index);
    }

    @Override
    public void removeBus(String busId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void removeAllBuses() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Switch> getSwitches() {
        return index.getSwitches(voltageLevelResource.getId());
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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void removeAllSwitches() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Bus getBus1(String switchId) {
        SwitchImpl aSwitch = index.getSwitch(switchId).orElseThrow(() -> new PowsyblException("switch " + switchId + " doesn't exist"));
        return index.getBus(aSwitch.getBus1()).orElse(null);
    }

    @Override
    public Bus getBus2(String switchId) {
        SwitchImpl aSwitch = index.getSwitch(switchId).orElseThrow(() -> new PowsyblException("switch " + switchId + " doesn't exist"));
        return index.getBus(aSwitch.getBus2()).orElse(null);
    }

    @Override
    public Switch getSwitch(String switchId) {
        return index.getSwitch(switchId).orElse(null);
    }

    @Override
    public SwitchAdder newSwitch() {
        return new SwitchAdderBusBreakerImpl(voltageLevelResource, index);
    }
}
