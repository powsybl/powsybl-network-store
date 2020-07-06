/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.InjectionAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TerminalImpl<U extends InjectionAttributes> implements Terminal {

    private final NetworkObjectIndex index;

    private final U attributes;

    private final Connectable connectable;

    private final TerminalNodeBreakerViewImpl<U> nodeBreakerView;

    private final TerminalBusBreakerViewImpl<U> busBreakerView;

    private final TerminalBusViewImpl<U> busView;

    public TerminalImpl(NetworkObjectIndex index, U attributes, Connectable connectable) {
        this.index = index;
        this.attributes = attributes;
        this.connectable = connectable;
        nodeBreakerView = new TerminalNodeBreakerViewImpl<>(attributes);
        busBreakerView = new TerminalBusBreakerViewImpl<>(index, attributes);
        busView = new TerminalBusViewImpl<>(index, attributes);
    }

    static <U extends InjectionAttributes> TerminalImpl<U> create(NetworkObjectIndex index, U attributes, Connectable connectable) {
        return new TerminalImpl<>(index, attributes, connectable);
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public TerminalBusViewImpl<U> getBusView() {
        return busView;
    }

    @Override
    public Connectable getConnectable() {
        return connectable;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(AssertionError::new);
    }

    @Override
    public double getP() {
        return attributes.getP();
    }

    @Override
    public Terminal setP(double p) {
        attributes.setP(p);
        return this;
    }

    @Override
    public double getQ() {
        return attributes.getQ();
    }

    @Override
    public Terminal setQ(double q) {
        attributes.setQ(q);
        return this;
    }

    @Override
    public double getI() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean connect() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean disconnect() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isConnected() {
        if (this.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            return attributes.getNode() != null;
        } else {
            return attributes.getBus() != null;
        }
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser) {
        throw new UnsupportedOperationException("TODO");
    }
}
