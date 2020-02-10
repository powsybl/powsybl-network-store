/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TerminalNodeBreakerImpl<T extends IdentifiableAttributes, U extends InjectionAttributes> implements Terminal {

    private final NetworkObjectIndex index;

    private final Resource<T> resource;

    private final Connectable connectable;

    private final Function<T, U> attributesAdapter;

    private final TerminalNodeBreakerViewImpl nodeBreakerView;

    private final TerminalBusViewImpl busView;

    public TerminalNodeBreakerImpl(NetworkObjectIndex index, Resource<T> resource,
                            Function<T, U> attributesAdapter,
                            Connectable connectable) {
        this.index = index;
        this.resource = resource;
        this.connectable = connectable;
        this.attributesAdapter = attributesAdapter;
        nodeBreakerView = new TerminalNodeBreakerViewImpl<>(resource, attributesAdapter);
        busView = new TerminalBusViewImpl(index);
    }

    static <T extends IdentifiableAttributes, U extends InjectionAttributes> TerminalNodeBreakerImpl create(NetworkObjectIndex index, Resource<T> resource,
                                          Function<T, U> attributesAdapter,
                                          Connectable connectable) {
        return new TerminalNodeBreakerImpl<>(index, resource, attributesAdapter, connectable);
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public TerminalBusViewImpl getBusView() {
        return busView;
    }

    @Override
    public Connectable getConnectable() {
        return connectable;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return index.getVoltageLevel(attributesAdapter.apply(resource.getAttributes()).getVoltageLevelId()).orElseThrow(AssertionError::new);
    }

    @Override
    public double getP() {
        return attributesAdapter.apply(resource.getAttributes()).getP();
    }

    @Override
    public Terminal setP(double p) {
        attributesAdapter.apply(resource.getAttributes()).setP(p);
        return this;
    }

    @Override
    public double getQ() {
        return attributesAdapter.apply(resource.getAttributes()).getQ();
    }

    @Override
    public Terminal setQ(double q) {
        attributesAdapter.apply(resource.getAttributes()).setQ(q);
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
        return true;
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser) {
        throw new UnsupportedOperationException("TODO");
    }
}
