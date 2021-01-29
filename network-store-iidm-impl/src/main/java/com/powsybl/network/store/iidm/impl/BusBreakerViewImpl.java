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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    private List<Switch> getSwitches(String busId) {
        return getSwitchStream()
                .filter(s -> getBus1(s.getId()).getId().equals(busId) || getBus2(s.getId()).getId().equals(busId))
                .collect(Collectors.toList());
    }

    private Bus getOtherBus(String switchId, String busId) {
        if (getBus1(switchId).getId().equals(busId)) {
            return getBus2(switchId);
        } else if (getBus2(switchId).getId().equals(busId)) {
            return getBus1(switchId);
        } else {
            throw new AssertionError();
        }
    }

    void traverse(Terminal terminal, VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals) {
        traverse(terminal.getBusBreakerView().getBus(), traverser, traversedTerminals, new HashSet<>());
    }

    private void traverse(Bus bus, VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals, Set<Bus> traversedBuses) {
        checkTopologyKind();
        Objects.requireNonNull(bus);
        Objects.requireNonNull(traverser);

        if (traversedBuses.contains(bus)) {
            return;
        }

        // Terminals connected to the bus
        bus.getConnectedTerminalStream()
                .filter(t -> !traversedTerminals.contains(t))
                .filter(t -> traverser.traverse(t, t.isConnected()))
                .forEach(t -> {
                    traversedTerminals.add(t);
                    ((TerminalImpl) t).getSideTerminals().stream().forEach(ts -> ((TerminalImpl) ts).traverse(traverser, traversedTerminals));
                });

        traversedBuses.add(bus);

        // Terminals connected to the other buses connected to the bus by a traversed switch
        getSwitches(bus.getId()).stream()
                .filter(traverser::traverse)
                .map(s -> getOtherBus(s.getId(), bus.getId()))
                .forEach(b -> traverse(b, traverser, traversedTerminals, traversedBuses));
    }

}
