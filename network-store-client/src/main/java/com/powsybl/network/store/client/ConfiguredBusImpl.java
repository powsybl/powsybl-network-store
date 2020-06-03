/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.stream.Stream;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ConfiguredBusImpl extends AbstractIdentifiableImpl<Bus, ConfiguredBusAttributes> implements Bus {

    protected ConfiguredBusImpl(NetworkObjectIndex index, Resource<ConfiguredBusAttributes> resource) {
        super(index, resource);
    }

    static ConfiguredBusImpl create(NetworkObjectIndex index, Resource<ConfiguredBusAttributes> resource) {
        return new ConfiguredBusImpl(index, resource);
    }

    @Override
    public String getId() {
        return resource.getId();
    }

    @Override
    public String getName() {
        return resource.getAttributes().getName();
    }

    @Override
    public Network getNetwork() {
        return index.getNetwork();
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return index.getVoltageLevel(resource.getAttributes().getVoltageLevelId()).orElse(null);
    }

    @Override
    public double getV() {
        return resource.getAttributes().getV();
    }

    @Override
    public Bus setV(double v) {
        resource.getAttributes().setV(v);
        return this;
    }

    @Override
    public double getAngle() {
        return resource.getAttributes().getAngle();
    }

    @Override
    public Bus setAngle(double angle) {
        resource.getAttributes().setAngle(angle);
        return this;
    }

    @Override
    public double getP() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getQ() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Component getConnectedComponent() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isInMainConnectedComponent() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Component getSynchronousComponent() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getConnectedTerminalCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<Line> getLines() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<Line> getLineStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<Generator> getGenerators() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<Battery> getBatteries() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<Load> getLoads() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<Load> getLoadStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor topologyVisitor) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor topologyVisitor) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    protected String getTypeDescription() {
        return "ConfiguredBus";
    }
}
