package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.Optional;
import java.util.stream.Stream;

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
    public Iterable<Bus> getBuses() {
        return index.getBuses(voltageLevelResource.getId());
    }

    @Override
    public Stream<Bus> getBusStream() {
        return index.getBuses(voltageLevelResource.getId()).stream();
    }

    @Override
    public Bus getBus(String busId) {
        Optional<Bus> optBus = index.getBus(busId);
        if (optBus.isPresent() && optBus.get().getVoltageLevel().getId().equals(voltageLevelResource.getId())) {
            return optBus.get();
        }
        return null;
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
    public Iterable<Switch> getSwitches() {
        return index.getSwitches();
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return index.getSwitches().stream();
    }

    @Override
    public int getSwitchCount() {
        return index.getSwitchCount();
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
