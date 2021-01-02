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

import java.util.*;
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

    private boolean isNodeBeakerTopologyKind() {
        return voltageLevelResource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER;
    }

    private void checkNodeBreakerTopology() {
        if (isNodeBeakerTopologyKind()) {
            throw new PowsyblException("Not supported in a node breaker topology");
        }
    }

    private <T> AbstractTopology<T> getTopologyInstance() {
        return isNodeBeakerTopologyKind() ?
                (AbstractTopology<T>) NodeBreakerTopology.INSTANCE : (AbstractTopology<T>) BusBreakerTopology.INSTANCE;
    }

    private Map<String, Bus> calculateBuses() {
        return getTopologyInstance().calculateBuses(index, voltageLevelResource);
    }

    @Override
    public List<Bus> getBuses() {
        if (isNodeBeakerTopologyKind()) {
            // calculated buses
            return calculateBuses().values().stream().collect(Collectors.toList());
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
        if (isNodeBeakerTopologyKind()) {
            // calculated bus
            return calculateBuses().get(busId);
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
        if (removedBus.getConnectedTerminalCount() > 0) {
            throw new PowsyblException("Cannot remove bus '" + removedBus.getId() + "' because of connectable equipments");
        }
        if (!getSwitches(removedBus.getId()).isEmpty()) {
            throw new PowsyblException("Cannot remove bus '" + removedBus.getId() + "' because switch(es) is connected to it");
        }
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
        if (isNodeBeakerTopologyKind()) {
            return index.getSwitches(voltageLevelResource.getId()).stream().filter(Switch::isRetained).collect(Collectors.toList());
        } else {
            return index.getSwitches(voltageLevelResource.getId());
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
        getSwitches().forEach(s -> removeSwitch(s.getId()));
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

    private Optional<SwitchImpl> getOptionalSwitch(String switchId) {
        return index.getSwitch(switchId).filter(aSwitch -> topologyKind == TopologyKind.BUS_BREAKER || aSwitch.isRetained());
    }

    private SwitchImpl getSwitchOrThrowException(String switchId) {
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
        checkNodeBreakerTopology();
        Objects.requireNonNull(traverser);

        traverse(terminal.getBusBreakerView().getBus(), traverser, traversedTerminals, new HashSet<>());
    }

    private void traverse(Bus bus, VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals, Set<Bus> traversedBuses) {
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
