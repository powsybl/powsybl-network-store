/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.BusAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusImpl extends AbstractIdentifiableImpl<Bus, BusAttributes> implements Bus {

    public BusImpl(NetworkObjectIndex index, Resource<BusAttributes> resource) {
        super(index, resource);
    }

    static BusImpl create(NetworkObjectIndex index) {
        return new BusImpl(index, new Resource<>());
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getV() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Bus setV(double v) {
        // TODO
        return this;
    }

    @Override
    public double getAngle() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Bus setAngle(double angle) {
        // TODO
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
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        throw new UnsupportedOperationException("TODO");
    }
}
