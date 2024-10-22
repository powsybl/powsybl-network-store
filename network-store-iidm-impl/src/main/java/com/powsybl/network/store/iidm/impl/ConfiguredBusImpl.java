/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.network.store.model.AttributeFilter;
import com.powsybl.network.store.model.CalculatedBusAttributes;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.Resource;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ObjDoubleConsumer;
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
    public double getAngle() {
        return getResource().getAttributes().getAngle();
    }

    private void setVInCalculatedBus(CalculatedBusAttributes calculatedBusAttributes, double value) {
        calculatedBusAttributes.setV(value);
    }

    private void setAngleInCalculatedBus(CalculatedBusAttributes calculatedBusAttributes, double value) {
        calculatedBusAttributes.setAngle(value);
    }

    private void updateCalculatedBusAttributes(double newValue,
                                               String voltageLevelId,
                                               ObjDoubleConsumer<CalculatedBusAttributes> setValue) {
        index.getVoltageLevel(voltageLevelId).ifPresent(voltageLevel -> {
            Map<String, Integer> calculatedBuses = voltageLevel.getResource().getAttributes().getBusToCalculatedBusForBusView();
            if (!MapUtils.isEmpty(calculatedBuses)) {
                Integer busviewnum = calculatedBuses.get(getId());
                if (busviewnum != null) {
                    CalculatedBusAttributes busviewattributes = voltageLevel.getResource().getAttributes().getCalculatedBusesForBusView().get(busviewnum);
                    setValue.accept(busviewattributes, newValue);
                    index.updateVoltageLevelResource(voltageLevel.getResource(), AttributeFilter.SV);
                }
            }
        });
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

            // update V for bus in BusView
            updateCalculatedBusAttributes(v, getResource().getAttributes().getVoltageLevelId(), this::setVInCalculatedBus);
        }
        return this;
    }

    @Override
    public Bus setAngle(double angle) {
        double oldValue = getResource().getAttributes().getAngle();
        if (angle != oldValue) {
            updateResource(res -> res.getAttributes().setAngle(angle));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "angle", variantId, oldValue, angle);

            // update angle for bus in BusView
            updateCalculatedBusAttributes(angle, getResource().getAttributes().getVoltageLevelId(), this::setAngleInCalculatedBus);
        }
        return this;
    }

    @Override
    public double getFictitiousP0() {
        return getResource().getAttributes().getFictitiousP0();
    }

    @Override
    public Bus setFictitiousP0(double p0) {
        double oldValue = getResource().getAttributes().getFictitiousP0();
        if (p0 != oldValue) {
            updateResource(res -> res.getAttributes().setFictitiousP0(p0));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "fictitiousP0", variantId, oldValue, p0);
        }
        return this;
    }

    @Override
    public double getFictitiousQ0() {
        return getResource().getAttributes().getFictitiousQ0();
    }

    @Override
    public Bus setFictitiousQ0(double q0) {
        double oldValue = getResource().getAttributes().getFictitiousQ0();
        if (q0 != oldValue) {
            updateResource(res -> res.getAttributes().setFictitiousQ0(q0));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "fictitiousQ0", variantId, oldValue, q0);
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
