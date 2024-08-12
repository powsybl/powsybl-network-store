/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.commons.extensions.ExtensionAdderProviders;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@EqualsAndHashCode(exclude = {"connectedComponent", "synchronousComponent"})
public final class CalculatedBus implements BaseBus {

    private final NetworkObjectIndex index;

    private String voltageLevelId;

    private final String id;

    private final String name;

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final int calculatedBusNum;

    private final boolean isBusView;

    private final ComponentImpl connectedComponent;

    private final ComponentImpl synchronousComponent;

    CalculatedBus(NetworkObjectIndex index, String voltageLevelId, String id, String name, Resource<VoltageLevelAttributes> voltageLevelResource,
                  int calculatedBusNum, boolean isBusView) {
        this.index = Objects.requireNonNull(index);
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.voltageLevelResource = Objects.requireNonNull(voltageLevelResource);
        this.calculatedBusNum = calculatedBusNum;
        this.isBusView = isBusView;
        connectedComponent = new ComponentImpl(this, ComponentType.CONNECTED);
        synchronousComponent = new ComponentImpl(this, ComponentType.SYNCHRONOUS);
    }

    boolean isBusView() {
        return isBusView;
    }

    @Override
    public NetworkImpl getNetwork() {
        return index.getNetwork();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNameOrId() {
        return name != null ? name : id;
    }

    @Override
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(name);
    }

    @Override
    public boolean hasProperty() {
        return false;
    }

    @Override
    public boolean hasProperty(String key) {
        return false;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return null;
    }

    @Override
    public String setProperty(String key, String value) {
        return null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return Collections.emptySet();
    }

    @Override
    public boolean removeProperty(String key) {
        throw new UnsupportedOperationException("Removing a property on a calculated bus is not authorized");
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return index.getVoltageLevel(voltageLevelId).orElseThrow(IllegalStateException::new);
    }

    @Override
    public double getV() {
        return getBusAttribute(this::getBusViewV, this::getConfiguredBusV, "v");
    }

    @Override
    public double getAngle() {
        return getBusAttribute(this::getBusViewAngle, this::getConfiguredBusAngle, "angle");
    }

    private double getBusAttribute(Function<List<CalculatedBusAttributes>, Double> busViewFunction,
                                   Function<List<CalculatedBusAttributes>, Double> configuredBusFunction,
                                   String attributeName) {
        VoltageLevelAttributes attributes = voltageLevelResource.getAttributes();
        List<CalculatedBusAttributes> calculatedBusAttributesForBusList = attributes.getCalculatedBusesForBusView();

        // Check the Bus View
        double busViewValue = busViewFunction.apply(calculatedBusAttributesForBusList);
        if (!Double.isNaN(busViewValue)) {
            return busViewValue;
        }

        // If the topology is NODE_BREAKER, check the Bus Breaker View
        switch (attributes.getTopologyKind()) {
            case NODE_BREAKER -> {
                List<CalculatedBusAttributes> calculatedBusAttributesForBusBreakerList = attributes.getCalculatedBusesForBusBreakerView();
                double busBreakerViewValue = busViewFunction.apply(calculatedBusAttributesForBusBreakerList);
                if (!Double.isNaN(busBreakerViewValue)) {
                    return busBreakerViewValue;
                }
            }
            case BUS_BREAKER -> {
                return configuredBusFunction.apply(calculatedBusAttributesForBusList);
            }
        }
        return Double.NaN;
    }

    private double getBusViewV(List<CalculatedBusAttributes> calculatedBusAttributesList) {
        return getBusViewAttribute(calculatedBusAttributesList, CalculatedBusAttributes::getV);
    }

    private double getBusViewAngle(List<CalculatedBusAttributes> calculatedBusAttributesList) {
        return getBusViewAttribute(calculatedBusAttributesList, CalculatedBusAttributes::getAngle);
    }

    private double getBusViewAttribute(List<CalculatedBusAttributes> calculatedBusAttributesList,
                                       ToDoubleFunction<CalculatedBusAttributes> attributeGetter) {
        if (calculatedBusAttributesList != null) {
            CalculatedBusAttributes calculatedBusAttributes = calculatedBusAttributesList.get(calculatedBusNum);
            if (calculatedBusAttributes != null && !Double.isNaN(attributeGetter.applyAsDouble(calculatedBusAttributes))) {
                return attributeGetter.applyAsDouble(calculatedBusAttributes);
            }
        }
        return Double.NaN;
    }

    private double getConfiguredBusV(List<CalculatedBusAttributes> calculatedBusAttributesList) {
        return getConfiguredBusAttribute(calculatedBusAttributesList, Bus::getV);
    }

    private double getConfiguredBusAngle(List<CalculatedBusAttributes> calculatedBusAttributesList) {
        return getConfiguredBusAttribute(calculatedBusAttributesList, Bus::getAngle);
    }

    private double getConfiguredBusAttribute(List<CalculatedBusAttributes> calculatedBusAttributesList,
                                             ToDoubleFunction<Bus> busAttributeGetter) {
        if (calculatedBusAttributesList != null) {
            CalculatedBusAttributes calculatedBusAttributes = calculatedBusAttributesList.get(calculatedBusNum);
            if (calculatedBusAttributes != null) {
                List<String> busesIds = calculatedBusAttributes.getVertices().stream()
                        .map(vertice -> vertice.getBus())
                        .collect(Collectors.toList());
                return index.getConfiguredBuses().stream()
                        .filter(bus -> busesIds.contains(bus.getId()))
                        .mapToDouble(busAttributeGetter)
                        .findFirst()
                        .orElse(Double.NaN);
            }
        }
        return Double.NaN;
    }

    @Override
    public Bus setV(double v) {
        return setBusAttribute(v, this::updateBusAttributeV, this::updateVConfiguredBuses, "v");
    }

    @Override
    public Bus setAngle(double angle) {
        return setBusAttribute(angle, this::updateBusAttributeAngle, this::updateAngleConfiguredBuses, "angle");
    }

    private <T> Bus setBusAttribute(T value,
                                    BiConsumer<T, List<CalculatedBusAttributes>> updateAttributeMethod,
                                    BiConsumer<T, CalculatedBusAttributes> updateConfiguredBusesMethod,
                                    String attributeName) {
        VoltageLevelAttributes attributes = voltageLevelResource.getAttributes();
        switch (attributes.getTopologyKind()) {
            case NODE_BREAKER -> {
                updateAttributeMethod.accept(value, attributes.getCalculatedBusesForBusView());
                updateAttributeMethod.accept(value, attributes.getCalculatedBusesForBusBreakerView());
                index.updateVoltageLevelResource(voltageLevelResource, AttributeFilter.SV);
            }
            case BUS_BREAKER -> {
                List<CalculatedBusAttributes> calculatedBusAttributesBusList = attributes.getCalculatedBusesForBusView();
                if (calculatedBusAttributesBusList != null) {
                    CalculatedBusAttributes calculatedBusAttributesBus = calculatedBusAttributesBusList.get(calculatedBusNum);
                    if (calculatedBusAttributesBus != null) {
                        setAttribute(calculatedBusAttributesBus, (double) value, attributeName);
                        index.updateVoltageLevelResource(voltageLevelResource, AttributeFilter.SV);
                        updateConfiguredBusesMethod.accept(value, calculatedBusAttributesBus);
                    }
                }
            }
        }
        return this;
    }

    private void setAttribute(CalculatedBusAttributes busAttributes, double value, String attributeName) {
        switch (attributeName) {
            case "v" -> busAttributes.setV(value);
            case "angle" -> busAttributes.setAngle(value);
        }
    }

    private void updateBusAttributeV(double v, List<CalculatedBusAttributes> calculatedBusAttributesList) {
        updateBusAttribute(calculatedBusAttributesList, v, CalculatedBusAttributes::setV);
    }

    private void updateBusAttributeAngle(double angle, List<CalculatedBusAttributes> calculatedBusAttributesList) {
        updateBusAttribute(calculatedBusAttributesList, angle, CalculatedBusAttributes::setAngle);
    }

    private void updateBusAttribute(List<CalculatedBusAttributes> calculatedBusAttributesList, double value, BiConsumer<CalculatedBusAttributes, Double> setter) {
        if (calculatedBusAttributesList != null) {
            CalculatedBusAttributes calculatedBusAttributes = calculatedBusAttributesList.get(calculatedBusNum);
            if (calculatedBusAttributes != null) {
                setter.accept(calculatedBusAttributes, value);
            }
        }
    }

    private void updateVConfiguredBuses(double v, CalculatedBusAttributes calculatedBusAttributesBus) {
        updateConfiguredBuses(calculatedBusAttributesBus, v, Bus::getV, Bus::setV, "v");
    }

    private void updateAngleConfiguredBuses(double angle, CalculatedBusAttributes calculatedBusAttributesBus) {
        updateConfiguredBuses(calculatedBusAttributesBus, angle, Bus::getAngle, Bus::setAngle, "angle");
    }

    private void updateConfiguredBuses(CalculatedBusAttributes calculatedBusAttributesBus,
                                       double newValue,
                                       Function<Bus, Double> getter,
                                       BiConsumer<Bus, Double> setter,
                                       String attributeName) {
        List<String> busesIds = calculatedBusAttributesBus.getVertices().stream()
                .map(vertice -> vertice.getBus())
                .collect(Collectors.toList());

        List<Bus> buses = index.getConfiguredBuses().stream()
                .filter(bus -> busesIds.contains(bus.getId()) && !Objects.equals(getter.apply(bus), newValue))
                .collect(Collectors.toList());

        Map<Bus, Map.Entry<Double, Double>> oldNewValues = buses.stream()
                .collect(Collectors.toMap(
                        bus -> bus,
                        bus -> new AbstractMap.SimpleEntry<>(getter.apply(bus), newValue)
                ));

        buses.forEach(bus -> setter.accept(bus, newValue));

        if (!buses.isEmpty()) {
            index.updateConfiguredBusResource(((ConfiguredBusImpl) buses.get(0)).getResource(), null);
        }

        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        oldNewValues.forEach((bus, oldNewValue) ->
                index.notifyUpdate(bus, attributeName, variantId, oldNewValue.getKey(), oldNewValue.getValue())
        );
    }

    @Override
    public Component getConnectedComponent() {
        return connectedComponent;
    }

    private CalculatedBusAttributes getAttributes() {
        return isBusView ?
                voltageLevelResource.getAttributes().getCalculatedBusesForBusView().get(calculatedBusNum) :
                voltageLevelResource.getAttributes().getCalculatedBusesForBusBreakerView().get(calculatedBusNum);
    }

    int getConnectedComponentNum() {
        getNetwork().ensureConnectedComponentsUpToDate(isBusView);
        return getAttributes().getConnectedComponentNumber();
    }

    void setConnectedComponentNum(int num) {
        getAttributes().setConnectedComponentNumber(num);
        index.updateVoltageLevelResource(voltageLevelResource);
    }

    int getSynchronousComponentNum() {
        getNetwork().ensureSynchronousComponentsUpToDate(isBusView);
        return getAttributes().getSynchronousComponentNumber();
    }

    public void setSynchronousComponentNum(int num) {
        getAttributes().setSynchronousComponentNumber(num);
        index.updateVoltageLevelResource(voltageLevelResource);
    }

    public boolean isInMainConnectedComponent() {
        getNetwork().ensureConnectedComponentsUpToDate(isBusView);
        return getAttributes().getConnectedComponentNumber() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public Component getSynchronousComponent() {
        return synchronousComponent;
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        getNetwork().ensureSynchronousComponentsUpToDate(isBusView);
        return getAttributes().getSynchronousComponentNumber() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public Stream<Terminal> getConnectedTerminalStream() {
        return getConnectedTerminals().stream();
    }

    @Override
    public int getConnectedTerminalCount() {
        return getConnectedTerminals().size();
    }

    @Override
    public Collection<Terminal> getConnectedTerminals() {
        return getAttributes().getVertices().stream()
                .map(v -> {
                    Connectable<?> c = index.getConnectable(v.getId(), v.getConnectableType());
                    return switch (c.getType()) {
                        case LINE, TWO_WINDINGS_TRANSFORMER ->
                            ((AbstractBranchImpl<?, ?>) c).getTerminal(TwoSides.valueOf(v.getSide()));
                        case THREE_WINDINGS_TRANSFORMER ->
                            ((ThreeWindingsTransformerImpl) c).getTerminal(ThreeSides.valueOf(v.getSide()));
                        default -> c.getTerminals().get(0);
                    };
                })
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Line> getLines() {
        return getLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Line> getLineStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.LINE)
                .map(v -> index.getLine(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.TWO_WINDINGS_TRANSFORMER)
                .map(v -> index.getTwoWindingsTransformer(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.THREE_WINDINGS_TRANSFORMER)
                .map(v -> index.getThreeWindingsTransformer(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.GENERATOR)
                .map(v -> index.getGenerator(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.BATTERY)
                .map(v -> index.getBattery(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.LOAD)
                .map(v -> index.getLoad(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.SHUNT_COMPENSATOR)
                .map(v -> index.getShuntCompensator(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.DANGLING_LINE)
                .map(v -> index.getDanglingLine(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.STATIC_VAR_COMPENSATOR)
                .map(v -> index.getStaticVarCompensator(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.HVDC_CONVERTER_STATION)
                .map(v -> (LccConverterStation) index.getLccConverterStation(v.getId()).orElse(null))
                .filter(Objects::nonNull);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getVscConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == IdentifiableType.HVDC_CONVERTER_STATION)
                .map(v -> (VscConverterStation) index.getVscConverterStation(v.getId()).orElse(null))
                .filter(Objects::nonNull);
    }

    @Override
    public Iterable<Terminal> getAllTerminals() {
        return getAllTerminalsStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Terminal> getAllTerminalsStream() {
        Predicate<Terminal> pred =
                isBusView ?
                    t -> t.getBusView().getConnectableBus() != null && t.getBusView().getConnectableBus().getId().equals(getId()) :
                    t -> t.getBusBreakerView().getConnectableBus() != null && t.getBusBreakerView().getConnectableBus().getId().equals(getId());
        return getVoltageLevel().getConnectableStream()
                .flatMap(c -> (Stream<Terminal>) c.getTerminals().stream())
                .filter(t -> t.getVoltageLevel().getId().equals(getVoltageLevel().getId()) && pred.test(t));
    }

    @Override
    public <E extends Extension<Bus>> void addExtension(Class<? super E> aClass, E e) {
        throw new UnsupportedOperationException("Adding an extension on calculated bus is not authorized");
    }

    @Override
    public <E extends Extension<Bus>> E getExtension(Class<? super E> aClass) {
        return null;
    }

    @Override
    public <E extends Extension<Bus>> E getExtensionByName(String s) {
        return null;
    }

    @Override
    public <E extends Extension<Bus>> boolean removeExtension(Class<E> aClass) {
        return false;
    }

    @Override
    public <E extends Extension<Bus>> Collection<E> getExtensions() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "CalculatedBus(" +
                "id='" + id + '\'' +
                ')';
    }

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    // TODO to remove when this has attributes and therefore extends
    // AbstractIdentifiable
    @Override
    public <E extends Extension<Bus>, B extends ExtensionAdder<Bus, E>> B newExtension(Class<B> type) {
        ExtensionAdderProvider provider = ExtensionAdderProviders.findCachedProvider(getImplementationName(), type);
        return (B) provider.newAdder(this);
    }
}
