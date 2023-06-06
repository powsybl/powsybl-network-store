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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ConfiguredBusImpl extends AbstractIdentifiableImpl<Bus, ConfiguredBusAttributes> implements BaseBus {

    protected ConfiguredBusImpl(NetworkObjectIndex index, Resource<ConfiguredBusAttributes> resource) {
        super(index, resource);
    }

    static ConfiguredBusImpl create(NetworkObjectIndex index, Resource<ConfiguredBusAttributes> resource) {
        return new ConfiguredBusImpl(index, resource);
    }

    @Override
    public String getId() {
        return getResource().getId();
    }

    @Override
    public String getName() {
        return getResource().getAttributes().getName();
    }

    @Override
    public NetworkImpl getNetwork() {
        return index.getNetwork();
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return index.getVoltageLevel(getResource().getAttributes().getVoltageLevelId()).orElse(null);
    }

    @Override
    public double getV() {
        return getResource().getAttributes().getV();
    }

    @Override
    public Bus setV(double v) {
        if (v < 0) {
            throw new ValidationException(this, "voltage cannot be < 0");
        }
        double oldValue = getResource().getAttributes().getV();
        if (v != oldValue) {
            updateResource(res -> res.getAttributes().setV(v));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "v", variantId, oldValue, v);
        }
        return this;
    }

    @Override
    public double getAngle() {
        return getResource().getAttributes().getAngle();
    }

    @Override
    public Bus setAngle(double angle) {
        double oldValue = getResource().getAttributes().getAngle();
        if (angle != oldValue) {
            updateResource(res -> res.getAttributes().setAngle(angle));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "angle", variantId, oldValue, angle);
        }
        return this;
    }

    private Optional<Bus> getMergedBus() {
        return Optional.ofNullable(getVoltageLevel().getBusView().getMergedBus(getId()));
    }

    //
    // Note about "getNetwork().ensureConnectedComponentsUpToDate(true)"
    //
    // for component calculation we use bus view in 2 cases:
    //   - we are in a node/breaker topology and bus/view calculation is requested
    //   - we are in a bus/breaker topology whatever it is requested because even from bus/breaker view
    //     we can rely on bus/view (through merged bus) for component calculation

    @Override
    public Component getConnectedComponent() {
        getNetwork().ensureConnectedComponentsUpToDate(true);
        Optional<Bus> mergedBus = getMergedBus();
        return mergedBus.map(Bus::getConnectedComponent).orElse(null);
    }

    @Override
    public boolean isInMainConnectedComponent() {
        getNetwork().ensureConnectedComponentsUpToDate(true);
        Optional<Bus> mergedBus = getMergedBus();
        return mergedBus.isPresent() && mergedBus.get().isInMainConnectedComponent();
    }

    @Override
    public Component getSynchronousComponent() {
        getNetwork().ensureConnectedComponentsUpToDate(true);
        Optional<Bus> mergedBus = getMergedBus();
        return mergedBus.map(Bus::getSynchronousComponent).orElse(null);
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        getNetwork().ensureConnectedComponentsUpToDate(true);
        Optional<Bus> mergedBus = getMergedBus();
        return mergedBus.isPresent() && mergedBus.get().isInMainSynchronousComponent();
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
        Predicate<Terminal> pred =
                connected ?
                    t -> t.getBusBreakerView().getBus() != null && t.getBusBreakerView().getBus().getId().equals(getId()) :
                    t -> t.getBusBreakerView().getConnectableBus() != null && t.getBusBreakerView().getConnectableBus().getId().equals(getId());
        return getVoltageLevel().getConnectableStream()
                .flatMap(c -> (Stream<Terminal>) c.getTerminals().stream())
                .filter(t -> t.getVoltageLevel().getId().equals(getVoltageLevel().getId()) && pred.test(t))
                .collect(Collectors.toList());
    }

    @Override
    public int getConnectedTerminalCount() {
        return getConnectedTerminals().size();
    }

    private Stream<Connectable> getConnectableStream(IdentifiableType type) {
        return getConnectedTerminals().stream().map(Terminal::getConnectable).filter(c -> c.getType() == type);
    }

    @Override
    public Iterable<Line> getLines() {
        return getLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Line> getLineStream() {
        return getConnectableStream(IdentifiableType.LINE).map(Line.class::cast);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getConnectableStream(IdentifiableType.TWO_WINDINGS_TRANSFORMER).map(TwoWindingsTransformer.class::cast);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getConnectableStream(IdentifiableType.THREE_WINDINGS_TRANSFORMER).map(ThreeWindingsTransformer.class::cast);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getConnectableStream(IdentifiableType.GENERATOR).map(Generator.class::cast);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getConnectableStream(IdentifiableType.BATTERY).map(Battery.class::cast);
    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getConnectableStream(IdentifiableType.LOAD).map(Load.class::cast);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getConnectableStream(IdentifiableType.SHUNT_COMPENSATOR).map(ShuntCompensator.class::cast);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getConnectableStream(IdentifiableType.DANGLING_LINE).map(DanglingLine.class::cast);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream().filter(danglingLineFilter.getPredicate());
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getConnectableStream(IdentifiableType.STATIC_VAR_COMPENSATOR).map(StaticVarCompensator.class::cast);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getConnectableStream(IdentifiableType.HVDC_CONVERTER_STATION).map(HvdcConverterStation.class::cast)
                .filter(c -> c.getHvdcType() == HvdcConverterStation.HvdcType.LCC)
                .map(LccConverterStation.class::cast);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getVscConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getConnectableStream(IdentifiableType.HVDC_CONVERTER_STATION).map(HvdcConverterStation.class::cast)
                .filter(c -> c.getHvdcType() == HvdcConverterStation.HvdcType.VSC)
                .map(VscConverterStation.class::cast);
    }

    @Override
    public String toString() {
        return "ConfiguredBus(" +
                "id='" + getId() + '\'' +
                ')';
    }
}
