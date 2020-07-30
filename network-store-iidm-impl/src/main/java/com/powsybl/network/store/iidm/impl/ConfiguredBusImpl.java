/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    public NetworkImpl getNetwork() {
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

        if (topologyVisitor == null) {
            throw new NullPointerException("visitor is null");
        }
        VoltageLevel busVoltageLevel = getVoltageLevel();

        for (Generator generator : busVoltageLevel.getGenerators()) {
            if (isConnectedToBus(generator)) {
                topologyVisitor.visitGenerator(generator);
            }
        }
        for (Load load : busVoltageLevel.getLoads()) {
            if (isConnectedToBus(load)) {
                topologyVisitor.visitLoad(load);
            }
        }
        for (ShuntCompensator sc : busVoltageLevel.getShuntCompensators()) {
            if (isConnectedToBus(sc)) {
                topologyVisitor.visitShuntCompensator(sc);
            }
        }
        for (StaticVarCompensator svc : busVoltageLevel.getStaticVarCompensators()) {
            if (isConnectedToBus(svc)) {
                topologyVisitor.visitStaticVarCompensator(svc);
            }
        }
        for (VscConverterStation station : busVoltageLevel.getVscConverterStations()) {
            if (isConnectedToBus(station)) {
                topologyVisitor.visitHvdcConverterStation(station);
            }
        }
        for (LccConverterStation station : busVoltageLevel.getLccConverterStations()) {
            if (isConnectedToBus(station)) {
                topologyVisitor.visitHvdcConverterStation(station);
            }
        }
        for (TwoWindingsTransformer twt : index.getTwoWindingsTransformers(busVoltageLevel.getId())) {
            if (isConnectedToBus(twt)) {
                topologyVisitor.visitTwoWindingsTransformer(twt, twt.getSide(twt.getTerminal(busVoltageLevel.getId())));
            }
        }
        for (ThreeWindingsTransformer twt : index.getThreeWindingsTransformers(busVoltageLevel.getId())) {
            if (isConnectedToBus(twt)) {
                ThreeWindingsTransformer.Side side = null;
                if (twt.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getId().equals(busVoltageLevel.getId())) {
                    side = ThreeWindingsTransformer.Side.ONE;
                } else if (twt.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getId().equals(busVoltageLevel.getId())) {
                    side = ThreeWindingsTransformer.Side.TWO;
                } else if (twt.getTerminal(ThreeWindingsTransformer.Side.THREE).getVoltageLevel().getId().equals(busVoltageLevel.getId())) {
                    side = ThreeWindingsTransformer.Side.THREE;
                }
                topologyVisitor.visitThreeWindingsTransformer(twt, side);
            }
        }
        for (Line line : index.getLines(busVoltageLevel.getId())) {
            if (isConnectedToBus(line)) {
                topologyVisitor.visitLine(line, line.getSide(line.getTerminal(busVoltageLevel.getId())));
            }
        }
        for (DanglingLine danglingLine : busVoltageLevel.getDanglingLines()) {
            if (isConnectedToBus(danglingLine)) {
                topologyVisitor.visitDanglingLine(danglingLine);
            }
        }
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor topologyVisitor) {
        throw new UnsupportedOperationException("TODO");
    }

    private <T extends Connectable> boolean isConnectedToBus(T equipment) {
        List<Terminal> terminals = equipment.getTerminals();
        Set<Terminal> busTerminals = terminals.stream().filter(t -> t.getBusBreakerView().getBus() != null ? (t.getBusBreakerView().getBus().getId() == getId()) : false).collect(Collectors.toSet());
        return  busTerminals.stream().anyMatch(t -> t.isConnected());
    }

    @Override
    protected String getTypeDescription() {
        return "ConfiguredBus";
    }
}
