/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CalculateBus implements Bus {

    private final NetworkObjectIndex index;

    private String voltageLevelId;

    private final String id;

    private final String name;

    private final List<Vertex> vertices;

    public CalculateBus(NetworkObjectIndex index, String voltageLevelId, String id, String name, List<Vertex> vertices) {
        this.index = index;
        this.voltageLevelId = voltageLevelId;
        this.id = id;
        this.name = name;
        this.vertices = vertices;
    }

    @Override
    public Network getNetwork() {
        return index.getNetwork();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name != null ? name : id;
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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Bus setV(double v) {
        // TODO
        return this;
    }

    @Override
    public double getAngle() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Bus setAngle(double angle) {
        // TODO
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
    public void visitConnectedEquipments(TopologyVisitor visitor) {
        Objects.requireNonNull(visitor);
        for (Vertex vertex : vertices) {
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
                    throw new UnsupportedOperationException("TODO");
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
                    HvdcConverterStation<?> station = index.getVscConverterStation(vertex.getId()).orElse(null);
                    if (station == null) {
                        station = index.getLccConverterStation(vertex.getId()).orElseThrow(IllegalStateException::new);
                    }
                    visitor.visitHvdcConverterStation(station);
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
}
