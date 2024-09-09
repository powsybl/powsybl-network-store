/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class TerminalBusBreakerViewImpl<U extends IdentifiableAttributes> implements Terminal.BusBreakerView {

    private final NetworkObjectIndex index;

    private final Connectable<?> connectable;

    private final Function<Resource<U>, InjectionAttributes> attributesGetter;

    private static final String NOT_FOUND = "not found";

    public TerminalBusBreakerViewImpl(NetworkObjectIndex index, Connectable<?> connectable, Function<Resource<U>, InjectionAttributes> attributesGetter) {
        this.index = index;
        this.connectable = connectable;
        this.attributesGetter = attributesGetter;
    }

    private AbstractIdentifiableImpl<?, U> getAbstractIdentifiable() {
        return (AbstractIdentifiableImpl<?, U>) connectable;
    }

    private InjectionAttributes getAttributes(Resource<U> resource) {
        return attributesGetter.apply(resource);
    }

    private InjectionAttributes getAttributes() {
        return getAttributes(getAbstractIdentifiable().getResource());
    }

    private Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return index.getVoltageLevel(getAttributes().getVoltageLevelId()).orElseThrow(IllegalStateException::new).getResource();
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
        return new ArrayList<>(NodeBreakerTopology.INSTANCE.calculateBuses(index, getVoltageLevelResource()).values());
    }

    private Bus calculateBus() {
        return NodeBreakerTopology.INSTANCE.calculateBus(index, getVoltageLevelResource(), getAttributes().getNode());
    }

    @Override
    public Bus getBus() {
        getAbstractIdentifiable().getResource();
        if (isNodeBeakerTopologyKind()) { // calculated bus
            return calculateBus();
        } else {  // configured bus
            String busId = getAttributes().getBus();
            return busId != null ? index.getConfiguredBus(busId).orElseThrow(() -> new AssertionError(busId + " " + NOT_FOUND)) : null;
        }
    }

    @Override
    public Bus getConnectableBus() {
        getAbstractIdentifiable().getResource();
        if (isBusBeakerTopologyKind()) { // Configured bus
            String busId = getAttributes().getConnectableBus();
            return index.getConfiguredBus(busId).orElseThrow(() -> new AssertionError(busId + " " + NOT_FOUND));
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

        if (index.getConfiguredBus(busId).isEmpty()) {
            throw new PowsyblException(busId + " " + NOT_FOUND);
        }

        var attributes = getAttributes();
        if (attributes.getConnectableBus().equals(busId)) {
            return;
        }

        getAbstractIdentifiable().updateResource(r -> {
            var a = getAttributes(r);
            a.setConnectableBus(busId);
            if (a.getBus() != null) {
                a.setBus(busId);
            }
        });

        index.getVoltageLevel(getVoltageLevelResource().getId()).orElseThrow(AssertionError::new).invalidateCalculatedBuses();
    }

    @Override
    public void moveConnectable(String busId, boolean connected) {
        Objects.requireNonNull(busId);
        if (getAbstractIdentifiable().getOptionalResource().isEmpty()) {
            throw new PowsyblException("Cannot modify removed equipment");
        }
        Bus bus = index.getNetwork().getBusBreakerView().getBus(busId);
        if (bus == null) {
            throw new PowsyblException("Bus '" + busId + "' not found");
        }
        var attributes = getAttributes();
        VoltageLevelImpl voltageLevel = (VoltageLevelImpl) bus.getVoltageLevel();
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            throw new PowsyblException("Trying to move connectable " + attributes.getResource().getId()
                    + " to bus " + busId + " of voltage level " + bus.getVoltageLevel().getId() + ", which is a node breaker voltage level");
        }
        //attributes.setConnectableBus(busId);
        //attributes.setBus(connected ? busId : null);
        //attributes.setVoltageLevelId(voltageLevel.getId());
        //index.updateResource(getAbstractIdentifiable().getResource(), null);
        getAbstractIdentifiable().updateResource(res -> {
            attributes.setConnectableBus(busId);
            attributes.setBus(connected ? busId : null);
            attributes.setNode(null);
            attributes.setVoltageLevelId(voltageLevel.getId());
        });
        voltageLevel.invalidateCalculatedBuses();
    }
}
