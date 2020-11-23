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
import com.powsybl.network.store.model.CalculatedBusAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.Vertex;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CalculatedBus implements Bus {

    private final NetworkObjectIndex index;

    private String voltageLevelId;

    private final String id;

    private final String name;

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final int calculatedBusNum;

    private final ComponentImpl connectedComponent;

    private final ComponentImpl synchronousComponent;

    CalculatedBus(NetworkObjectIndex index, String voltageLevelId, String id, String name, Resource<VoltageLevelAttributes> voltageLevelResource,
                  int calculatedBusNum) {
        this.index = Objects.requireNonNull(index);
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.voltageLevelResource = Objects.requireNonNull(voltageLevelResource);
        this.calculatedBusNum = calculatedBusNum;
        connectedComponent = new ComponentImpl(this, ComponentType.CONNECTED);
        synchronousComponent = new ComponentImpl(this, ComponentType.SYNCHRONOUS);
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
    public String getName() {
        return getNameOrId();
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
    public boolean hasProperty(String s) {
        return false;
    }

    @Override
    public String getProperty(String s) {
        return null;
    }

    @Override
    public String getProperty(String s, String s1) {
        return null;
    }

    @Override
    public String setProperty(String s, String s1) {
        throw new UnsupportedOperationException("Setting a property on a calculated bus is not authorized");
    }

    @Override
    public Set<String> getPropertyNames() {
        return Collections.emptySet();
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return index.getVoltageLevel(voltageLevelId).orElseThrow(IllegalStateException::new);
    }

    @Override
    public double getV() {
        return getAttributes().getV();
    }

    @Override
    public Bus setV(double v) {
        getAttributes().setV(v);
        return this;
    }

    @Override
    public double getAngle() {
        return getAttributes().getAngle();
    }

    @Override
    public Bus setAngle(double angle) {
        getAttributes().setAngle(angle);
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
        return connectedComponent;
    }

    private CalculatedBusAttributes getAttributes() {
        return voltageLevelResource.getAttributes().getCalculatedBuses().get(calculatedBusNum);
    }

    int getConnectedComponentNum() {
        getNetwork().ensureConnectedComponentsUpToDate();
        return getAttributes().getConnectedComponentNumber();
    }

    void setConnectedComponentNum(int num) {
        getAttributes().setConnectedComponentNumber(num);
        voltageLevelResource.getAttributes().updateResource();
    }

    int getSynchronousComponentNum() {
        getNetwork().ensureSynchronousComponentsUpToDate();
        return getAttributes().getSynchronousComponentNumber();
    }

    public void setSynchronousComponentNum(int num) {
        getAttributes().setSynchronousComponentNumber(num);
        voltageLevelResource.getAttributes().updateResource();
    }

    @Override
    public boolean isInMainConnectedComponent() {
        getNetwork().ensureConnectedComponentsUpToDate();
        return getAttributes().getConnectedComponentNumber() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public Component getSynchronousComponent() {
        return synchronousComponent;
    }

    @Override
    public boolean isInMainSynchronousComponent() {
        getNetwork().ensureSynchronousComponentsUpToDate();
        return getAttributes().getSynchronousComponentNumber() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public int getConnectedTerminalCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Collection<Terminal> getConnectedTerminals() {
        return getAttributes().getVertices().stream()
                .map(v -> index.getIdentifiable(v.getId()))
                .filter(i -> i instanceof Connectable)
                .map(c -> ((Connectable) c).getTerminals())
                .flatMap(List<Terminal>::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Line> getLines() {
        return getLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Line> getLineStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.LINE)
                .map(v -> index.getLine(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.TWO_WINDINGS_TRANSFORMER)
                .map(v -> index.getTwoWindingsTransformer(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.THREE_WINDINGS_TRANSFORMER)
                .map(v -> index.getThreeWindingsTransformer(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.GENERATOR)
                .map(v -> index.getGenerator(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.BATTERY)
                .map(v -> index.getBattery(v.getId()).orElseThrow(IllegalAccessError::new));

    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.LOAD)
                .map(v -> index.getLoad(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.SHUNT_COMPENSATOR)
                .map(v -> index.getShuntCompensator(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.DANGLING_LINE)
                .map(v -> index.getDanglingLine(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.STATIC_VAR_COMPENSATOR)
                .map(v -> index.getStaticVarCompensator(v.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getAttributes().getVertices().stream()
                .filter(v -> v.getConnectableType() == ConnectableType.HVDC_CONVERTER_STATION)
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
                .filter(v -> v.getConnectableType() == ConnectableType.HVDC_CONVERTER_STATION)
                .map(v -> (VscConverterStation) index.getVscConverterStation(v.getId()).orElse(null))
                .filter(Objects::nonNull);
    }

    @Override
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        Objects.requireNonNull(visitor);
        for (Vertex vertex : getAttributes().getVertices()) {
            switch (vertex.getConnectableType()) {
                case BUSBAR_SECTION:
                    visitor.visitBusbarSection(index.getBusbarSection(vertex.getId()).orElseThrow(IllegalStateException::new));
                    break;
                case LINE:
                    visitor.visitLine(index.getLine(vertex.getId()).orElseThrow(IllegalStateException::new), Branch.Side.valueOf(vertex.getSide()));
                    break;
                case TWO_WINDINGS_TRANSFORMER:
                    visitor.visitTwoWindingsTransformer(index.getTwoWindingsTransformer(vertex.getId()).orElseThrow(IllegalStateException::new), Branch.Side.valueOf(vertex.getSide()));
                    break;
                case THREE_WINDINGS_TRANSFORMER:
                    visitor.visitThreeWindingsTransformer(index.getThreeWindingsTransformer(vertex.getId()).orElseThrow(IllegalStateException::new), ThreeWindingsTransformer.Side.valueOf(vertex.getSide()));
                    break;
                case GENERATOR:
                    visitor.visitGenerator(index.getGenerator(vertex.getId()).orElseThrow(IllegalStateException::new));
                    break;
                case BATTERY:
                    visitor.visitBattery(index.getBattery(vertex.getId()).orElseThrow(IllegalStateException::new));
                    break;
                case LOAD:
                    visitor.visitLoad(index.getLoad(vertex.getId()).orElseThrow(IllegalStateException::new));
                    break;
                case SHUNT_COMPENSATOR:
                    visitor.visitShuntCompensator(index.getShuntCompensator(vertex.getId()).orElseThrow(IllegalStateException::new));
                    break;
                case DANGLING_LINE:
                    visitor.visitDanglingLine(index.getDanglingLine(vertex.getId()).orElseThrow(IllegalStateException::new));
                    break;
                case STATIC_VAR_COMPENSATOR:
                    visitor.visitStaticVarCompensator(index.getStaticVarCompensator(vertex.getId()).orElseThrow(IllegalStateException::new));
                    break;
                case HVDC_CONVERTER_STATION:
                    visitor.visitHvdcConverterStation(index.getHvdcConverterStation(vertex.getId()).orElseThrow(IllegalStateException::new));
                    break;
                default:
                    throw new IllegalStateException("Unknown connectable type: " + vertex.getConnectableType());
            }
        }
    }

    @Override
    public void visitConnectedOrConnectableEquipments(TopologyVisitor visitor) {
        throw new UnsupportedOperationException("TODO");
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
        return "CalculateBus(" +
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
