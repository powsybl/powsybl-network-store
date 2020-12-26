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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ConfiguredBusImpl extends AbstractIdentifiableImpl<com.powsybl.iidm.network.Bus, ConfiguredBusAttributes> implements BaseBus {

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
        if (v < 0) {
            throw new ValidationException(this, "voltage cannot be < 0");
        }
        double oldValue = resource.getAttributes().getV();
        resource.getAttributes().setV(v);
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "v", variantId, oldValue, v);
        return this;
    }

    @Override
    public double getAngle() {
        return resource.getAttributes().getAngle();
    }

    @Override
    public Bus setAngle(double angle) {
        double oldValue = resource.getAttributes().getAngle();
        resource.getAttributes().setAngle(angle);
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "angle", variantId, oldValue, angle);
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
    public List<Terminal> getConnectedTerminals() {
        return getTerminals(true);
    }

    @Override
    public Stream<Terminal> getConnectedTerminalStream() {
        return getConnectedTerminals().stream();
    }

    @Override
    public List<Terminal> getAllTerminals() {
        return getTerminals(false);
    }

    @Override
    public Stream<Terminal> getAllTerminalsStream() {
        return getAllTerminals().stream();
    }

    private List<Terminal> getTerminals(boolean connected) {
        VoltageLevel busVoltageLevel = getVoltageLevel();
        return busVoltageLevel.getConnectableStream()
                .map(c -> c.getTerminals())
                .flatMap(List<Terminal>::stream)
                .filter(t -> t.getVoltageLevel().getId().equals(getVoltageLevel().getId())
                        && (connected ? t.getBusBreakerView().getBus() != null : t.getBusBreakerView().getConnectableBus() != null)
                        && (connected ? t.getBusBreakerView().getBus().getId().equals(getId()) : t.getBusBreakerView().getConnectableBus().getId().equals(getId())))
                .collect(Collectors.toList());
    }

    @Override
    public int getConnectedTerminalCount() {
        return getConnectedTerminals().size();
    }

    private Stream<Connectable> getConnectableStream(ConnectableType type) {
        return getConnectedTerminals().stream().map(Terminal::getConnectable).filter(c -> c.getType() == type);
    }

    @Override
    public Iterable<Line> getLines() {
        return getLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Line> getLineStream() {
        return getConnectableStream(ConnectableType.LINE)
                .map(c -> index.getLine(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getConnectableStream(ConnectableType.TWO_WINDINGS_TRANSFORMER)
                .map(c -> index.getTwoWindingsTransformer(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getConnectableStream(ConnectableType.THREE_WINDINGS_TRANSFORMER)
                .map(c -> index.getThreeWindingsTransformer(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getConnectableStream(ConnectableType.GENERATOR)
                .map(c -> index.getGenerator(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getConnectableStream(ConnectableType.BATTERY)
                .map(c -> index.getBattery(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getConnectableStream(ConnectableType.LOAD)
                .map(c -> index.getLoad(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getConnectableStream(ConnectableType.SHUNT_COMPENSATOR)
                .map(c -> index.getShuntCompensator(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getConnectableStream(ConnectableType.DANGLING_LINE)
                .map(c -> index.getDanglingLine(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getConnectableStream(ConnectableType.STATIC_VAR_COMPENSATOR)
                .map(c -> index.getStaticVarCompensator(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getConnectableStream(ConnectableType.HVDC_CONVERTER_STATION)
                .filter(c -> ((HvdcConverterStation) c).getHvdcType() == HvdcConverterStation.HvdcType.LCC)
                .map(c -> index.getLccConverterStation(c.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getVscConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getConnectableStream(ConnectableType.HVDC_CONVERTER_STATION)
                .filter(c -> ((HvdcConverterStation) c).getHvdcType() == HvdcConverterStation.HvdcType.VSC)
                .map(c -> index.getVscConverterStation(c.getId()).orElseThrow(IllegalAccessError::new));

    }

    @Override
    public String toString() {
        return "ConfiguredBus(" +
                "id='" + getId() + '\'' +
                ')';
    }

    @Override
    protected String getTypeDescription() {
        return "ConfiguredBus";
    }
}
