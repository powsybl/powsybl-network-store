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
        return index.getBus(resource.getId()).orElse(null);
    }

    @Override
    public double getAngle() {
        return resource.getAttributes().getAngle();
    }

    @Override
    public Bus setAngle(double angle) {
        resource.getAttributes().setAngle(angle);
        return index.getBus(resource.getId()).orElse(null);
    }

    @Override
    public double getP() {
        return resource.getAttributes().getP();
    }

    @Override
    public double getQ() {
        return resource.getAttributes().getQ();
    }

    @Override
    public Component getConnectedComponent() {
        return null;
    }

    @Override
    public boolean isInMainConnectedComponent() {
        return false;
    }

    @Override
    public Component getSynchronousComponent() {
        return null;
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        return false;
    }

    @Override
    public int getConnectedTerminalCount() {
        return 0;
    }

    @Override
    public Iterable<Line> getLines() {
        return index.getLines();
    }

    @Override
    public Stream<Line> getLineStream() {
        return index.getLines().stream();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return index.getTwoWindingsTransformers();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return index.getTwoWindingsTransformers().stream();
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return index.getThreeWindingsTransformers();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return index.getThreeWindingsTransformers().stream();
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return index.getGenerators();
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return index.getGenerators().stream();
    }

    @Override
    public Iterable<Battery> getBatteries() {
        //TODO
        return null;
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        //TODO
        return null;
    }

    @Override
    public Iterable<Load> getLoads() {
        return index.getLoads();
    }

    @Override
    public Stream<Load> getLoadStream() {
        return index.getLoads().stream();
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return index.getShuntCompensators();
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return index.getShuntCompensators().stream();
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return index.getDanglingLines();
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return index.getDanglingLines().stream();
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return index.getStaticVarCompensators();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return index.getStaticVarCompensators().stream();
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return index.getLccConverterStations();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return index.getLccConverterStations().stream();

    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return index.getVscConverterStations();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return index.getVscConverterStations().stream();
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor topologyVisitor) {
        //TODO
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor topologyVisitor) {
        //TODO
    }
}
