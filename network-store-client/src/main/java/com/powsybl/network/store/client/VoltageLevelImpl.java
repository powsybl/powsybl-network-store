/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VoltageLevelImpl extends AbstractIdentifiableImpl<VoltageLevel, VoltageLevelAttributes> implements VoltageLevel {

    private final NodeBreakerViewImpl nodeBreakerView;

    private final BusBreakerViewImpl busBreakerView;

    private final BusView busView;

    public VoltageLevelImpl(NetworkObjectIndex index, Resource<VoltageLevelAttributes> resource) {
        super(index, resource);
        nodeBreakerView = NodeBreakerViewImpl.create(resource.getAttributes().getTopologyKind(), resource, index);
        busBreakerView = BusBreakerViewImpl.create(resource.getAttributes().getTopologyKind(), resource, index);
        busView = new VoltageLevelBusViewImpl(index, resource);
    }

    static VoltageLevelImpl create(NetworkObjectIndex index, Resource<VoltageLevelAttributes> resource) {
        return new VoltageLevelImpl(index, resource);
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.VOLTAGE_LEVEL;
    }

    @Override
    public Substation getSubstation() {
        return index.getSubstation(resource.getAttributes().getSubstationId()).orElseThrow(AssertionError::new);
    }

    @Override
    public double getNominalV() {
        return resource.getAttributes().getNominalV();
    }

    @Override
    public VoltageLevel setNominalV(double nominalV) {
        resource.getAttributes().setNominalV(nominalV);
        return this;
    }

    @Override
    public double getLowVoltageLimit() {
        return resource.getAttributes().getLowVoltageLimit();
    }

    @Override
    public VoltageLevel setLowVoltageLimit(double lowVoltageLimit) {
        resource.getAttributes().setLowVoltageLimit(lowVoltageLimit);
        return this;
    }

    @Override
    public double getHighVoltageLimit() {
        return resource.getAttributes().getHighVoltageLimit();
    }

    @Override
    public VoltageLevel setHighVoltageLimit(double highVoltageLimit) {
        resource.getAttributes().setHighVoltageLimit(highVoltageLimit);
        return this;
    }

    @Override
    public TopologyKind getTopologyKind() {
        return resource.getAttributes().getTopologyKind();
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusView getBusView() {
        return busView;
    }

    @Override
    public void printTopology() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void printTopology(PrintStream out, ShortIdDictionary dict) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void exportTopology(Path file) throws IOException {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void exportTopology(Writer writer, Random random) throws IOException {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void exportTopology(Writer writer) throws IOException {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Switch> getSwitches() {
        return index.getSwitches(resource.getId());
    }

    @Override
    public int getSwitchCount() {
        return getSwitches().size();
    }

    @Override
    public LoadAdder newLoad() {
        return new LoadAdderImpl(resource, index);
    }

    @Override
    public List<Load> getLoads() {
        return index.getLoads(resource.getId());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getLoads().stream();
    }

    @Override
    public int getLoadCount() {
        return index.getLoads().size();
    }

    @Override
    public GeneratorAdder newGenerator() {
        return new GeneratorAdderImpl(resource, index);
    }

    @Override
    public List<Generator> getGenerators() {
        return index.getGenerators(resource.getId());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getGenerators().stream();
    }

    @Override
    public int getGeneratorCount() {
        return getGenerators().size();
    }

    @Override
    public BatteryAdder newBattery() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Battery> getBatteries() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getBatteries().stream();
    }

    @Override
    public int getBatteryCount() {
        return getBatteries().size();
    }

    @Override
    public ShuntCompensatorAdder newShuntCompensator() {
        return new ShuntCompensatorAdderImpl(resource, index);
    }

    @Override
    public List<ShuntCompensator> getShuntCompensators() {
        return index.getShuntCompensators(resource.getId());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getShuntCompensators().stream();
    }

    @Override
    public int getShuntCompensatorCount() {
        return getShuntCompensators().size();
    }

    @Override
    public DanglingLineAdder newDanglingLine() {
        return new DanglingLineAdderImpl(resource, index);
    }

    @Override
    public List<DanglingLine> getDanglingLines() {
        return index.getDanglingLines(resource.getId());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getDanglingLines().stream();
    }

    @Override
    public int getDanglingLineCount() {
        return getDanglingLines().size();
    }

    @Override
    public VscConverterStationAdder newVscConverterStation() {
        return new VscConverterStationAdderImpl(resource, index);
    }

    @Override
    public List<VscConverterStation> getVscConverterStations() {
        return index.getVscConverterStations(resource.getId());
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getVscConverterStations().stream();
    }

    @Override
    public int getVscConverterStationCount() {
        return getVscConverterStations().size();
    }

    @Override
    public LccConverterStationAdder newLccConverterStation() {
        return new LccConverterStationAdderImpl(resource, index);
    }

    @Override
    public List<LccConverterStation> getLccConverterStations() {
        return index.getLccConverterStations(resource.getId());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getLccConverterStations().stream();
    }

    @Override
    public int getLccConverterStationCount() {
        return getLccConverterStations().size();
    }

    @Override
    public StaticVarCompensatorAdder newStaticVarCompensator() {
        return new StaticVarCompensatorAdderImpl(resource, index);
    }

    @Override
    public List<StaticVarCompensator> getStaticVarCompensators() {
        return index.getStaticVarCompensators(resource.getId());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getStaticVarCompensators().stream();
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return getStaticVarCompensators().size();
    }

    @Override
    public List<Connectable> getConnectables() {
        List<Connectable> connectables = new ArrayList<>();
        if (resource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            connectables.addAll(nodeBreakerView.getBusbarSections());
        }
        connectables.addAll(getGenerators());
        connectables.addAll(getLoads());
        connectables.addAll(getShuntCompensators());
        connectables.addAll(getVscConverterStations());
        connectables.addAll(getLccConverterStations());
        connectables.addAll(getStaticVarCompensators());
        connectables.addAll(index.getTwoWindingsTransformers(resource.getId()));
        connectables.addAll(index.getThreeWindingsTransformers(resource.getId()));
        connectables.addAll(getDanglingLines());
        connectables.addAll(index.getLines(resource.getId()));
        return connectables;
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return getConnectables().stream();
    }

    @Override
    public int getConnectableCount() {
        return getConnectables().size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Connectable> List<T> getConnectables(Class<T> clazz) {
        if (clazz == Branch.class) {
            return (List<T>) ImmutableList.<Branch>builder()
                    .addAll(index.getTwoWindingsTransformers(resource.getId()))
                            .addAll(index.getLines(resource.getId()))
                            .build();
        }
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz) {
        return getConnectables(clazz).stream();
    }

    @Override
    public <T extends Connectable> int getConnectableCount(Class<T> clazz) {
        return getConnectables(clazz).size();
    }

    @Override
    public <T extends Connectable> T getConnectable(String id, Class<T> aClass) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        if (resource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            for (BusbarSection busbarSection : index.getBusbarSections(resource.getId())) {
                visitor.visitBusbarSection(busbarSection);
            }
        }
        for (Generator generator : getGenerators()) {
            visitor.visitGenerator(generator);
        }
        for (Load load : getLoads()) {
            visitor.visitLoad(load);
        }
        for (ShuntCompensator sc : getShuntCompensators()) {
            visitor.visitShuntCompensator(sc);
        }
        for (StaticVarCompensator svc : getStaticVarCompensators()) {
            visitor.visitStaticVarCompensator(svc);
        }
        for (VscConverterStation station : getVscConverterStations()) {
            visitor.visitHvdcConverterStation(station);
        }
        for (LccConverterStation station : getLccConverterStations()) {
            visitor.visitHvdcConverterStation(station);
        }
        for (TwoWindingsTransformer twt : index.getTwoWindingsTransformers(resource.getId())) {
            visitor.visitTwoWindingsTransformer(twt, twt.getSide(twt.getTerminal(resource.getId())));
        }
        for (ThreeWindingsTransformer twt : index.getThreeWindingsTransformers(resource.getId())) {
            ThreeWindingsTransformer.Side side = null;
            if (twt.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getId().equals(resource.getId())) {
                side = ThreeWindingsTransformer.Side.ONE;
            } else if (twt.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getId().equals(resource.getId())) {
                side = ThreeWindingsTransformer.Side.TWO;
            } else if (twt.getTerminal(ThreeWindingsTransformer.Side.THREE).getVoltageLevel().getId().equals(resource.getId())) {
                side = ThreeWindingsTransformer.Side.THREE;
            }
            visitor.visitThreeWindingsTransformer(twt, side);
        }
        for (Line line : index.getLines(resource.getId())) {
            visitor.visitLine(line, line.getSide(line.getTerminal(resource.getId())));
        }
        for (DanglingLine danglingLine : getDanglingLines()) {
            visitor.visitDanglingLine(danglingLine);
        }
        // TODO battery
    }
}
