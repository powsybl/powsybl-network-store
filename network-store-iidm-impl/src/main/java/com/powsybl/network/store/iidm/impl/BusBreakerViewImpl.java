/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;
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

    private final VoltageLevelImpl voltageLevel;

    private final NetworkObjectIndex index;

    public BusBreakerViewImpl(TopologyKind topologyKind, VoltageLevelImpl voltageLevel, NetworkObjectIndex index) {
        this.topologyKind = topologyKind;
        this.voltageLevel = voltageLevel;
        this.index = index;
    }

    static BusBreakerViewImpl create(TopologyKind topologyKind, VoltageLevelImpl voltageLevel, NetworkObjectIndex index) {
        return new BusBreakerViewImpl(topologyKind, voltageLevel, index);
    }

    private Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return voltageLevel.getResource();
    }

    private boolean isNodeBeakerTopologyKind() {
        return getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER;
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
        return getTopologyInstance().calculateBuses(index, getVoltageLevelResource());
    }

    @Override
    public List<Bus> getBuses() {
        if (isNodeBeakerTopologyKind()) {
            // calculated buses
            return new ArrayList<>(calculateBuses().values());
        } else {
            // configured buses
            return index.getConfiguredBuses(getVoltageLevelResource().getId());
        }
    }

    @Override
    public Stream<Bus> getBusStream() {
        return getBuses().stream();
    }

    @Override
    public int getBusCount() {
        return (int) getBusStream().count();
    }

    @Override
    public Bus getBus(String busId) {
        if (isNodeBeakerTopologyKind()) {
            // calculated bus
            return calculateBuses().get(busId);
        } else {
            // configured bus
            return index.getConfiguredBus(busId).filter(bus1 -> bus1.getVoltageLevel().getId().equals(getVoltageLevelResource().getId()))
                    .orElse(null);
        }
    }

    @Override
    public BusAdder newBus() {
        checkNodeBreakerTopology(); // we can only add configured bus in a bus/breaker topo
        return new ConfiguredBusAdderImpl(getVoltageLevelResource(), index);
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
        index.notifyBeforeRemoval(removedBus);
        index.removeConfiguredBus(busId);
        index.notifyAfterRemoval(busId);
    }

    @Override
    public void removeAllBuses() {
        checkNodeBreakerTopology(); // we can only remove configured buses in a bus/breaker topo
        getBuses().forEach(bus -> removeBus(bus.getId()));
    }

    @Override
    public List<Switch> getSwitches() {
        if (isNodeBeakerTopologyKind()) {
            return index.getSwitches(getVoltageLevelResource().getId()).stream().filter(Switch::isRetained).collect(Collectors.toList());
        } else {
            return index.getSwitches(getVoltageLevelResource().getId());
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
        index.notifyBeforeRemoval(switchToRemove);
        index.removeSwitch(switchId);
        index.notifyAfterRemoval(switchId);
    }

    @Override
    public void removeAllSwitches() {
        getSwitches().forEach(s -> removeSwitch(s.getId()));
    }

    @Override
    public Bus getBus1(String switchId) {
        SwitchImpl aSwitch = getSwitchOrThrowException(switchId);
        if (isNodeBeakerTopologyKind()) {
            // calculated bus
            return getTopologyInstance().calculateBus(index, getVoltageLevelResource(), aSwitch.getNode1());
        } else {
            return index.getConfiguredBus(aSwitch.getBus1()).orElse(null);
        }
    }

    @Override
    public Bus getBus2(String switchId) {
        SwitchImpl aSwitch = getSwitchOrThrowException(switchId);
        if (isNodeBeakerTopologyKind()) {
            // calculated bus
            return getTopologyInstance().calculateBus(index, getVoltageLevelResource(), aSwitch.getNode2());
        } else {
            return index.getConfiguredBus(aSwitch.getBus2()).orElse(null);
        }
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
        return new SwitchAdderBusBreakerImpl(getVoltageLevelResource(), index);
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
            throw new PowsyblException("Switch '" + switchId + "' is not connected to the bus : " + busId);
        }
    }

    boolean traverseFromTerminal(Terminal terminal, Terminal.TopologyTraverser traverser, Set<Terminal> traversedTerminals, TraversalType traversalType) {
        checkNodeBreakerTopology();
        Objects.requireNonNull(traverser);

        return traverseFromBus(terminal.getBusBreakerView().getBus(), traverser, traversedTerminals, new HashSet<>(), traversalType);
    }

    private boolean traverseFromBus(Bus bus, Terminal.TopologyTraverser traverser, Set<Terminal> traversedTerminals, Set<Bus> traversedBuses, TraversalType traversalType) {
        Objects.requireNonNull(bus);
        Objects.requireNonNull(traverser);

        if (traversedBuses.contains(bus)) {
            return true;
        }

        // Terminals connected to the bus
        for (Terminal terminal : bus.getConnectedTerminals()) {
            if (traversedTerminals.contains(terminal)) {
                continue;
            }

            TraverseResult result = traverser.traverse(terminal, terminal.isConnected());
            traversedTerminals.add(terminal);
            if (result == TraverseResult.TERMINATE_TRAVERSER) {
                return false;
            } else if (result == TraverseResult.CONTINUE) {
                Set<Terminal> otherSideTerminals = ((TerminalImpl) terminal).getOtherSideTerminals();
                for (Terminal otherSideTerminal : otherSideTerminals) {
                    if (!((TerminalImpl) otherSideTerminal).traverse(traverser, traversedTerminals, traversalType)) {
                        return false;
                    }
                }
            }
        }

        traversedBuses.add(bus);

        // Terminals connected to the other buses connected to the bus by a traversed switch
        for (Switch s : getSwitches(bus.getId())) {
            TraverseResult result = traverser.traverse(s);
            if (result == TraverseResult.TERMINATE_TRAVERSER) {
                return false;
            } else if (result == TraverseResult.CONTINUE) {
                Bus otherBus = getOtherBus(s.getId(), bus.getId());
                if (!traverseFromBus(otherBus, traverser, traversedTerminals, traversedBuses, traversalType)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void traverse(Bus bus, TopologyTraverser topologyTraverser) {
        // FIXME
    }
}
