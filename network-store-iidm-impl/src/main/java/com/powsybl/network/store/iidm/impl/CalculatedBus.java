/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.commons.extensions.ExtensionAdderProviders;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.network.store.model.*;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
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

    private final Function<Terminal, Bus> getBusFromTerminal;

    private static final String VOLTAGE = "v";
    private static final String ANGLE = "angle";

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
        getBusFromTerminal = isBusView ? t -> t.getBusView().getBus() : t -> t.getBusBreakerView().getBus();
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
        return getAttributes().getV();
    }

    private void setVInCalculatedBus(CalculatedBusAttributes calculatedBusAttributes, double value) {
        calculatedBusAttributes.setV(value);
    }

    private void setVInConfiguredBus(ConfiguredBusAttributes configuredBusAttributes, double value) {
        configuredBusAttributes.setV(value);
    }

    private double getVInBus(Bus bus) {
        return bus.getV();
    }

    private void setAngleInCalculatedBus(CalculatedBusAttributes calculatedBusAttributes, double value) {
        calculatedBusAttributes.setAngle(value);
    }

    private void setAngleInConfiguredBus(ConfiguredBusAttributes configuredBusAttributes, double value) {
        configuredBusAttributes.setAngle(value);
    }

    private double getAngleInBus(Bus bus) {
        return bus.getAngle();
    }

    @Override
    public Bus setV(double v) {
        getAttributes().setV(v);
        index.updateVoltageLevelResource(voltageLevelResource, AttributeFilter.SV);

        if (getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            // update V in configured buses
            updateConfiguredBuses(v, getAttributes(), VOLTAGE, this::getVInBus, this::setVInConfiguredBus);
        } else {
            if (isBusView) {
                // update V for buses in BusBreakerView
                updateBusesAttributes(v, voltageLevelResource.getAttributes().getCalculatedBusesForBusBreakerView(), getAttributes(), this::setVInCalculatedBus);
            } else {
                // update V for buses in BusView
                updateBusesAttributes(v, voltageLevelResource.getAttributes().getCalculatedBusesForBusView(), getAttributes(), this::setVInCalculatedBus);
            }
        }
        return this;
    }

    private void updateBusesAttributes(double value,
                                       List<CalculatedBusAttributes> calculatedBusAttributesList,
                                       CalculatedBusAttributes sourceBusAttributes,
                                       ObjDoubleConsumer<CalculatedBusAttributes> setValue) {
        if (!CollectionUtils.isEmpty(calculatedBusAttributesList)) {
            calculatedBusAttributesList.forEach(busToUpdate -> busToUpdate.getVertices().forEach(vertex1 ->
                sourceBusAttributes.getVertices().stream().filter(v -> v.getId().equals(vertex1.getId())).findFirst().ifPresent(vertex2 -> {
                    setValue.accept(busToUpdate, value);
                    index.updateVoltageLevelResource(voltageLevelResource, AttributeFilter.SV);
                })
            ));
        }
    }

    private void updateConfiguredBuses(double newValue,
                                       CalculatedBusAttributes calculatedBusAttributes,
                                       String attributeName,
                                       ToDoubleFunction<Bus> getValue,
                                       ObjDoubleConsumer<ConfiguredBusAttributes> setValue) {
        List<String> busesIds = calculatedBusAttributes.getVertices().stream()
            .map(Vertex::getBus)
            .toList();

        List<Bus> buses = getVoltageLevel().getBusBreakerView().getBusStream()
            .filter(bus -> busesIds.contains(bus.getId()) && !Objects.equals(getValue.applyAsDouble(bus), newValue))
            .toList();

        Map<Bus, Map.Entry<Double, Double>> oldNewValues = buses.stream()
            .collect(Collectors.toMap(
                bus -> bus,
                bus -> new AbstractMap.SimpleEntry<>(getValue.applyAsDouble(bus), newValue)
            ));

        buses.forEach(bus -> {
            setValue.accept(((ConfiguredBusImpl) bus).getResource().getAttributes(), newValue);
            index.updateConfiguredBusResource(((ConfiguredBusImpl) bus).getResource(), null);
        });

        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        oldNewValues.forEach((bus, oldNewValue) ->
            index.notifyUpdate(bus, attributeName, variantId, oldNewValue.getKey(), oldNewValue.getValue())
        );
    }

    @Override
    public double getAngle() {
        return getAttributes().getAngle();
    }

    @Override
    public Bus setAngle(double angle) {
        getAttributes().setAngle(angle);
        index.updateVoltageLevelResource(voltageLevelResource, AttributeFilter.SV);

        if (getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            // update angle in configuredBus
            updateConfiguredBuses(angle, getAttributes(), ANGLE, this::getAngleInBus, this::setAngleInConfiguredBus);
        } else {
            if (isBusView) {
                // update angle for Bus in BusBreakerView
                updateBusesAttributes(angle, voltageLevelResource.getAttributes().getCalculatedBusesForBusBreakerView(), getAttributes(), this::setAngleInCalculatedBus);
            } else {
                // update angle for Bus in BusView
                updateBusesAttributes(angle, voltageLevelResource.getAttributes().getCalculatedBusesForBusView(), getAttributes(), this::setAngleInCalculatedBus);
            }
        }
        return this;
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

    @Override
    public double getFictitiousP0() {
        return TopologyKind.NODE_BREAKER == getVoltageLevel().getTopologyKind() ?
            Networks.getNodes(id, getVoltageLevel(), getBusFromTerminal)
                .mapToDouble(n -> getVoltageLevel().getNodeBreakerView().getFictitiousP0(n))
                .reduce(0.0, Double::sum) :
            getAllTerminalsStream().map(t -> t.getBusBreakerView().getBus()).distinct()
                .map(Bus::getFictitiousP0)
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousP0(double p0) {
        if (TopologyKind.NODE_BREAKER == getVoltageLevel().getTopologyKind()) {
            Networks.getNodes(id, getVoltageLevel(), getBusFromTerminal).forEach(n -> getVoltageLevel().getNodeBreakerView().setFictitiousP0(n, 0.0));
            getVoltageLevel().getNodeBreakerView().setFictitiousP0(Networks.getNodes(id, getVoltageLevel(), getBusFromTerminal)
                    .findFirst()
                    .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")),
                p0);
        } else {
            getAllTerminalsStream().map(t -> t.getBusBreakerView().getBus()).distinct().forEach(b -> b.setFictitiousP0(p0));
        }
        return this;
    }

    @Override
    public double getFictitiousQ0() {
        return TopologyKind.NODE_BREAKER == getVoltageLevel().getTopologyKind() ?
            Networks.getNodes(id, getVoltageLevel(), getBusFromTerminal)
                .mapToDouble(n -> getVoltageLevel().getNodeBreakerView().getFictitiousQ0(n))
                .reduce(0.0, Double::sum) :
            getAllTerminalsStream().map(t -> t.getBusBreakerView().getBus()).distinct()
                .map(Bus::getFictitiousQ0)
                .reduce(0.0, Double::sum);
    }

    @Override
    public Bus setFictitiousQ0(double q0) {
        if (TopologyKind.NODE_BREAKER == getVoltageLevel().getTopologyKind()) {
            Networks.getNodes(id, getVoltageLevel(), getBusFromTerminal).forEach(n -> getVoltageLevel().getNodeBreakerView().setFictitiousQ0(n, 0.0));
            getVoltageLevel().getNodeBreakerView().setFictitiousQ0(Networks.getNodes(id, getVoltageLevel(), getBusFromTerminal)
                    .findFirst()
                    .orElseThrow(() -> new PowsyblException("Bus " + id + " should contain at least one node")),
                q0);
        } else {
            getAllTerminalsStream().map(t -> t.getBusBreakerView().getBus()).distinct().forEach(b -> b.setFictitiousQ0(q0));
        }
        return this;
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

    public int getCalculatedBusNum() {
        return calculatedBusNum;
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
