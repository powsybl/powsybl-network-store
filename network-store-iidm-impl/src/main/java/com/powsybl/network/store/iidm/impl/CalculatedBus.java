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
import org.apache.commons.collections4.MapUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Predicate;
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

    private void setVInConfiguredBus(ConfiguredBusImpl configuredBus, double value) {
        configuredBus.setConfiguredBusV(value);
    }

    private void setAngleInCalculatedBus(CalculatedBusAttributes calculatedBusAttributes, double value) {
        calculatedBusAttributes.setAngle(value);
    }

    private void setAngleInConfiguredBus(ConfiguredBusImpl configuredBus, double value) {
        configuredBus.setConfiguredBusAngle(value);
    }

    @Override
    public Bus setV(double v) {
        getAttributes().setV(v);
        index.updateVoltageLevelResource(voltageLevelResource, AttributeFilter.SV);

        if (getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            // update V in configured buses
            // this triggers network notifications for 'v' for each configured bus
            updateConfiguredBuses(v, this::setVInConfiguredBus);
        } else {
            // update V for buses in the other view (busView/busBreakerView)
            updateBusesAttributes(v, this::setVInCalculatedBus);
        }
        return this;
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
            // this triggers network notifications for 'angle' for each configured bus
            updateConfiguredBuses(angle, this::setAngleInConfiguredBus);
        } else {
            // update angle for buses in the other view (busView/busBreakerView)
            updateBusesAttributes(angle, this::setAngleInCalculatedBus);
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

    public int getCalculatedBusNum() {
        return calculatedBusNum;
    }

    private void updateBusesAttributes(double value, ObjDoubleConsumer<CalculatedBusAttributes> setValue) {
        // busnum of this bus -> nodes in this bus -> busnums in the other view -> buses of the other view
        VoltageLevelAttributes vlAttributes = ((VoltageLevelImpl) getVoltageLevel()).getResource().getAttributes();
        Map<Integer, Integer> nodesToCalculatedBuses = isBusView
            ? vlAttributes.getNodeToCalculatedBusForBusView()
            : vlAttributes.getNodeToCalculatedBusForBusBreakerView();
        Map<Integer, Integer> nodesToCalculatedBusesInOtherView = isBusView
            ? vlAttributes.getNodeToCalculatedBusForBusBreakerView()
            : vlAttributes.getNodeToCalculatedBusForBusView();
        if (!MapUtils.isEmpty(nodesToCalculatedBuses) && !MapUtils.isEmpty(nodesToCalculatedBusesInOtherView)) {
            for (Entry<Integer, Integer> entry : nodesToCalculatedBuses.entrySet()) {
                if (getCalculatedBusNum() == entry.getValue()) {
                    int node = entry.getKey();
                    if (nodesToCalculatedBusesInOtherView.containsKey(node)) {
                        int busNumInOtherView = nodesToCalculatedBusesInOtherView.get(node);
                        List<CalculatedBusAttributes> calculatedBusAttributes = isBusView
                            ? vlAttributes.getCalculatedBusesForBusBreakerView()
                            : vlAttributes.getCalculatedBusesForBusView();
                        if (!CollectionUtils.isEmpty(calculatedBusAttributes)) {
                            setValue.accept(calculatedBusAttributes.get(busNumInOtherView), value);
                            index.updateVoltageLevelResource(voltageLevelResource, AttributeFilter.SV);
                        }
                    }
                }
            }
        }
    }

    private void updateConfiguredBuses(double newValue,
                                       ObjDoubleConsumer<ConfiguredBusImpl> setValue) {
        VoltageLevelAttributes vlAttributes = ((VoltageLevelImpl) getVoltageLevel()).getResource().getAttributes();
        for (Entry<String, Integer> entry : vlAttributes.getBusToCalculatedBusForBusView().entrySet()) {
            if (getCalculatedBusNum() == entry.getValue()) {
                Bus bus = getVoltageLevel().getBusBreakerView().getBus(entry.getKey());
                setValue.accept((ConfiguredBusImpl) bus, newValue);
            }
        }
    }
}
