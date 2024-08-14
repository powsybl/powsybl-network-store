/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.AttributeFilter;
import com.powsybl.network.store.model.CalculatedBusAttributes;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.Resource;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.*;
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
        return getBusAttribute(this::getConfiguredBusVoltage, this::getCalculatedBusVoltage);
    }

    @Override
    public double getAngle() {
        return getBusAttribute(this::getConfiguredBusAngle, this::getCalculatedBusAngle);
    }

    private double getBusAttribute(DoubleSupplier configuredBusGetter, Supplier<Optional<Double>> calculatedBusGetter) {
        // Attempt to get the attribute from configuredBus
        Double configuredValue = configuredBusGetter.getAsDouble();
        if (!Double.isNaN(configuredValue)) {
            return configuredValue;
        }

        // Attempt to get the attribute from calculatedBusesForBusView
        return calculatedBusGetter.get().orElse(Double.NaN);
    }

    private double getConfiguredBusVoltage() {
        return getConfiguredBusAttribute(ConfiguredBusAttributes::getV);
    }

    private double getConfiguredBusAngle() {
        return getConfiguredBusAttribute(ConfiguredBusAttributes::getAngle);
    }

    private double getConfiguredBusAttribute(ToDoubleFunction<ConfiguredBusAttributes> attributeGetter) {
        ConfiguredBusAttributes attributes = getResource().getAttributes();
        if (attributes != null) {
            double value = attributeGetter.applyAsDouble(attributes);
            if (!Double.isNaN(value)) {
                return value;
            }
        }
        return Double.NaN;
    }

    private Optional<Double> getCalculatedBusVoltage() {
        return getCalculatedBusAttribute(CalculatedBusAttributes::getV);
    }

    private Optional<Double> getCalculatedBusAngle() {
        return getCalculatedBusAttribute(CalculatedBusAttributes::getAngle);
    }

    private Optional<Double> getCalculatedBusAttribute(Function<CalculatedBusAttributes, Double> attributeGetter) {
        ConfiguredBusAttributes attributes = getResource().getAttributes();
        if (attributes == null) {
            return Optional.empty();
        }

        String voltageLevelId = attributes.getVoltageLevelId();
        Optional<VoltageLevelImpl> voltageLevelOpt = index.getVoltageLevel(voltageLevelId);

        return voltageLevelOpt
                .filter(voltageLevel -> voltageLevel.getResource().getVariantNum() == getResource().getVariantNum())
                .flatMap(voltageLevel -> voltageLevel.getResource().getAttributes()
                        .getCalculatedBusesForBusView().stream().findFirst())
                .map(attributeGetter);
    }

    @Override
    public Bus setV(double v) {
        validateNonNegative(v, "voltage");

        ConfiguredBusAttributes configuredBus = getResource().getAttributes();
        double oldValue = configuredBus.getV();

        if (v != oldValue) {
            updateAttribute(v, oldValue, "v", configuredBus::setV);
            updateCalculatedBusAttributesIfNeeded(v, configuredBus.getVoltageLevelId(), this::updateCalculatedBusAttributesV);
        }
        return this;
    }

    @Override
    public Bus setAngle(double angle) {

        ConfiguredBusAttributes configuredBus = getResource().getAttributes();
        double oldValue = configuredBus.getAngle();

        if (angle != oldValue) {
            updateAttribute(angle, oldValue, "angle", configuredBus::setAngle);
            updateCalculatedBusAttributesIfNeeded(angle, configuredBus.getVoltageLevelId(), this::updateCalculatedBusAttributesAngle);
        }
        return this;
    }

    private void validateNonNegative(double value, String attributeName) {
        if (value < 0) {
            throw new ValidationException(this, attributeName + " cannot be < 0");
        }
    }

    private void updateAttribute(double newValue, double oldValue, String attributeName, DoubleConsumer setter) {
        updateResource(res -> setter.accept(newValue));
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, attributeName, variantId, oldValue, newValue);
    }

    private void updateCalculatedBusAttributesIfNeeded(double newValue, String voltageLevelId,
                                                       BiConsumer<Double, List<CalculatedBusAttributes>> updater) {
        Optional<VoltageLevelImpl> voltageLevelOpt = index.getVoltageLevel(voltageLevelId);

        voltageLevelOpt.ifPresent(voltageLevel -> {
            if (isSameVariant(voltageLevel)) {
                List<CalculatedBusAttributes> calculatedBusAttributesList = voltageLevel.getResource()
                        .getAttributes()
                        .getCalculatedBusesForBusView();

                if (CollectionUtils.isNotEmpty(calculatedBusAttributesList)) {
                    updater.accept(newValue, calculatedBusAttributesList);
                    index.updateVoltageLevelResource(voltageLevel.getResource(), AttributeFilter.SV);
                }
            }
        });
    }

    private boolean isSameVariant(VoltageLevelImpl voltageLevel) {
        return voltageLevel.getResource().getVariantNum() == getResource().getVariantNum();
    }

    private void updateCalculatedBusAttributesV(double v, List<CalculatedBusAttributes> calculatedBusAttributesList) {
        calculatedBusAttributesList.forEach(calculatedBusAttributes -> calculatedBusAttributes.setV(v));
    }

    private void updateCalculatedBusAttributesAngle(double angle, List<CalculatedBusAttributes> calculatedBusAttributesList) {
        calculatedBusAttributesList.forEach(calculatedBusAttributes -> calculatedBusAttributes.setAngle(angle));
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
