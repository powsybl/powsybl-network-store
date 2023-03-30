/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.ShortIdDictionary;
import com.powsybl.network.store.iidm.impl.extensions.IdentifiableShortCircuitImpl;
import com.powsybl.network.store.iidm.impl.extensions.SlackTerminalImpl;
import com.powsybl.network.store.model.IdentifiableShortCircuitAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TerminalRefAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VoltageLevelImpl extends AbstractIdentifiableImpl<VoltageLevel, VoltageLevelAttributes> implements VoltageLevel {

    private final NodeBreakerViewImpl nodeBreakerView;

    private final BusBreakerViewImpl busBreakerView;

    private final VoltageLevelBusViewImpl busView;

    public VoltageLevelImpl(NetworkObjectIndex index, Resource<VoltageLevelAttributes> resource) {
        super(index, resource);
        nodeBreakerView = NodeBreakerViewImpl.create(resource.getAttributes().getTopologyKind(), this, index);
        busBreakerView = BusBreakerViewImpl.create(resource.getAttributes().getTopologyKind(), this, index);
        busView = new VoltageLevelBusViewImpl(index, this);
    }

    static VoltageLevelImpl create(NetworkObjectIndex index, Resource<VoltageLevelAttributes> resource) {
        return new VoltageLevelImpl(index, resource);
    }

    void invalidateCalculatedBuses() {
        updateResource(res -> res.getAttributes().setCalculatedBusesValid(false));
        getNetwork().invalidateComponents();
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.VOLTAGE_LEVEL;
    }

    @Override
    public Optional<Substation> getSubstation() {
        String substationId = getResource().getAttributes().getSubstationId();
        return substationId == null ? Optional.empty() : index.getSubstation(substationId).map(Function.identity());
    }

    @Override
    public double getNominalV() {
        return getResource().getAttributes().getNominalV();
    }

    @Override
    public VoltageLevel setNominalV(double nominalV) {
        var resource = getResource();
        ValidationUtil.checkNominalV(this, nominalV);
        double oldValue = resource.getAttributes().getNominalV();
        if (nominalV != oldValue) {
            updateResource(res -> res.getAttributes().setNominalV(nominalV));
            index.notifyUpdate(this, "nominalV", oldValue, nominalV);
        }
        return this;
    }

    @Override
    public double getLowVoltageLimit() {
        return getResource().getAttributes().getLowVoltageLimit();
    }

    @Override
    public VoltageLevel setLowVoltageLimit(double lowVoltageLimit) {
        var resource = getResource();
        ValidationUtil.checkVoltageLimits(this, lowVoltageLimit, getHighVoltageLimit());
        double oldValue = resource.getAttributes().getLowVoltageLimit();
        if (lowVoltageLimit != oldValue) {
            updateResource(res -> res.getAttributes().setLowVoltageLimit(lowVoltageLimit));
            index.notifyUpdate(this, "lowVoltageLimit", oldValue, lowVoltageLimit);
        }
        return this;
    }

    @Override
    public double getHighVoltageLimit() {
        return getResource().getAttributes().getHighVoltageLimit();
    }

    @Override
    public VoltageLevel setHighVoltageLimit(double highVoltageLimit) {
        var resource = getResource();
        ValidationUtil.checkVoltageLimits(this, getLowVoltageLimit(), highVoltageLimit);
        double oldValue = resource.getAttributes().getHighVoltageLimit();
        if (highVoltageLimit != oldValue) {
            updateResource(res -> res.getAttributes().setHighVoltageLimit(highVoltageLimit));
            index.notifyUpdate(this, "highVoltageLimit", oldValue, highVoltageLimit);
        }
        return this;
    }

    @Override
    public TopologyKind getTopologyKind() {
        return getResource().getAttributes().getTopologyKind();
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
        return index.getSwitches(getResource().getId());
    }

    @Override
    public int getSwitchCount() {
        return getSwitches().size();
    }

    @Override
    public LoadAdder newLoad() {
        return new LoadAdderImpl(getResource(), index);
    }

    @Override
    public List<Load> getLoads() {
        return index.getLoads(getResource().getId());
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
        return new GeneratorAdderImpl(getResource(), index);
    }

    @Override
    public List<Generator> getGenerators() {
        return index.getGenerators(getResource().getId());
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
        return new BatteryAdderImpl(getResource(), index);
    }

    @Override
    public List<Battery> getBatteries() {
        return index.getBatteries(getResource().getId());
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
        return new ShuntCompensatorAdderImpl(getResource(), index);
    }

    @Override
    public List<ShuntCompensator> getShuntCompensators() {
        return index.getShuntCompensators(getResource().getId());
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
        return new DanglingLineAdderImpl(getResource(), index);
    }

    @Override
    public List<DanglingLine> getDanglingLines() {
        return index.getDanglingLines(getResource().getId());
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
        return new VscConverterStationAdderImpl(getResource(), index);
    }

    @Override
    public List<VscConverterStation> getVscConverterStations() {
        return index.getVscConverterStations(getResource().getId());
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
        return new LccConverterStationAdderImpl(getResource(), index);
    }

    @Override
    public List<LccConverterStation> getLccConverterStations() {
        return index.getLccConverterStations(getResource().getId());
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
        return new StaticVarCompensatorAdderImpl(getResource(), index);
    }

    @Override
    public List<StaticVarCompensator> getStaticVarCompensators() {
        return index.getStaticVarCompensators(getResource().getId());
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
    public List<Line> getLines() {
        return index.getLines(getResource().getId());
    }

    @Override
    public Stream<Line> getLineStream() {
        return getLines().stream();
    }

    @Override
    public int getLineCount() {
        return getLines().size();
    }

    @Override
    public List<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return index.getTwoWindingsTransformers(getResource().getId());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getTwoWindingsTransformers().stream();
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return getTwoWindingsTransformers().size();
    }

    @Override
    public List<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return index.getThreeWindingsTransformers(getResource().getId());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getThreeWindingsTransformers().stream();
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return getThreeWindingsTransformers().size();
    }

    @Override
    public List<Connectable> getConnectables() {
        List<Connectable> connectables = new ArrayList<>();
        var resource = getResource();
        if (resource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            connectables.addAll(nodeBreakerView.getBusbarSections());
        }
        connectables.addAll(getGenerators());
        connectables.addAll(getBatteries());
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
        var resource = getResource();
        if (clazz == Branch.class) {
            return (List<T>) ImmutableList.<Branch>builder()
                    .addAll(index.getTwoWindingsTransformers(resource.getId()))
                    .addAll(index.getLines(resource.getId()))
                    .build();
        } else if (clazz == Generator.class) {
            return (List<T>) getGenerators();
        } else if (clazz == Battery.class) {
            return (List<T>) getBatteries();
        } else if (clazz == Load.class) {
            return (List<T>) getLoads();
        } else if (clazz == ShuntCompensator.class) {
            return (List<T>) getShuntCompensators();
        } else if (clazz == HvdcConverterStation.class) {
            return (List<T>) ImmutableList.<HvdcConverterStation>builder()
                    .addAll(getVscConverterStations())
                    .addAll(getLccConverterStations())
                    .build();
        } else if (clazz == VscConverterStation.class) {
            return (List<T>) getVscConverterStations();
        } else if (clazz == LccConverterStation.class) {
            return (List<T>) getLccConverterStations();
        } else if (clazz == StaticVarCompensator.class) {
            return (List<T>) getStaticVarCompensators();
        } else if (clazz == TwoWindingsTransformer.class) {
            return (List<T>) index.getTwoWindingsTransformers(resource.getId());
        } else if (clazz == ThreeWindingsTransformer.class) {
            return (List<T>) index.getThreeWindingsTransformers(resource.getId());
        } else if (clazz == DanglingLine.class) {
            return (List<T>) getDanglingLines();
        } else if (clazz == Line.class) {
            return (List<T>) index.getLines(resource.getId());
        } else if (clazz == BusbarSection.class) {
            if (resource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                return (List<T>) index.getBusbarSections(resource.getId());
            } else {
                throw new PowsyblException("No BusbarSection in a bus breaker topology");
            }
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
        return getConnectableStream(aClass).filter(c -> id.equals(c.getId())).findFirst().orElse(null);
    }

    @Override
    public void visitEquipments(TopologyVisitor visitor) {
        var resource = getResource();
        if (resource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            for (BusbarSection busbarSection : index.getBusbarSections(resource.getId())) {
                visitor.visitBusbarSection(busbarSection);
            }
        }
        for (Generator generator : getGenerators()) {
            visitor.visitGenerator(generator);
        }
        for (Battery battery : getBatteries()) {
            visitor.visitBattery(battery);
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
            if (twt.getTerminal(Branch.Side.ONE).getVoltageLevel().getId().equals(resource.getId())) {
                visitor.visitTwoWindingsTransformer(twt, Branch.Side.ONE);
            }
            if (twt.getTerminal(Branch.Side.TWO).getVoltageLevel().getId().equals(resource.getId())) {
                visitor.visitTwoWindingsTransformer(twt, Branch.Side.TWO);
            }
        }
        for (ThreeWindingsTransformer twt : index.getThreeWindingsTransformers(resource.getId())) {
            if (twt.getTerminal(ThreeWindingsTransformer.Side.ONE).getVoltageLevel().getId().equals(resource.getId())) {
                visitor.visitThreeWindingsTransformer(twt, ThreeWindingsTransformer.Side.ONE);
            }
            if (twt.getTerminal(ThreeWindingsTransformer.Side.TWO).getVoltageLevel().getId().equals(resource.getId())) {
                visitor.visitThreeWindingsTransformer(twt, ThreeWindingsTransformer.Side.TWO);
            }
            if (twt.getTerminal(ThreeWindingsTransformer.Side.THREE).getVoltageLevel().getId().equals(resource.getId())) {
                visitor.visitThreeWindingsTransformer(twt, ThreeWindingsTransformer.Side.THREE);
            }
        }
        for (Line line : index.getLines(resource.getId())) {
            if (line.getTerminal(Branch.Side.ONE).getVoltageLevel().getId().equals(resource.getId())) {
                visitor.visitLine(line, Branch.Side.ONE);
            }
            if (line.getTerminal(Branch.Side.TWO).getVoltageLevel().getId().equals(resource.getId())) {
                visitor.visitLine(line, Branch.Side.TWO);
            }
        }
        for (DanglingLine danglingLine : getDanglingLines()) {
            visitor.visitDanglingLine(danglingLine);
        }
    }

    private <E extends Extension<VoltageLevel>> void addIfNotNull(Collection<E> list, E extension) {
        if (extension != null) {
            list.add(extension);
        }
    }

    @Override
    public <E extends Extension<VoltageLevel>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        addIfNotNull(extensions, createSlackTerminal());
        addIfNotNull(extensions, createIdentifiableShortCircuitExtension());
        return extensions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<VoltageLevel>> E getExtension(Class<? super E> type) {
        if (type == SlackTerminal.class) {
            return createSlackTerminal();
        } else if (type == IdentifiableShortCircuit.class) {
            return createIdentifiableShortCircuitExtension();
        }
        return super.getExtension(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<VoltageLevel>> E getExtensionByName(String name) {
        if (name.equals("slackTerminal")) {
            return (E) createSlackTerminal();
        } else if (name.equals("identifiableShortCircuit")) {
            return (E) createIdentifiableShortCircuitExtension();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<VoltageLevel>> E createSlackTerminal() {
        E extension = null;
        var resource = getResource();
        TerminalRefAttributes attributes = resource.getAttributes().getSlackTerminal();
        if (attributes != null) {
            extension = (E) new SlackTerminalImpl(this);
        }

        return extension;
    }

    private <E extends Extension<VoltageLevel>> E createIdentifiableShortCircuitExtension() {
        E extension = null;
        var resource = getResource();
        IdentifiableShortCircuitAttributes attributes = resource.getAttributes().getIdentifiableShortCircuitAttributes();
        if (attributes != null) {
            extension = (E) new IdentifiableShortCircuitImpl<>(this);
        }
        return extension;
    }

    public Terminal getTerminal(TerminalRefAttributes tra) {
        return TerminalRefUtils.getTerminal(index, tra);
    }

    @Override
    public void remove() {
        VoltageLevelUtil.checkRemovability(this);

        var resource = getResource();
        index.notifyBeforeRemoval(this);

        // Remove all connectables
        List<Connectable> connectables = Lists.newArrayList(getConnectables());
        for (Connectable connectable : connectables) {
            connectable.remove();
        }

        // Remove the topology
        removeTopology();

        // Remove this voltage level from the network
        index.removeVoltageLevel(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    private void removeTopology() {
        var resource = getResource();
        if (resource.getAttributes().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            getBusBreakerView().removeAllSwitches();
            getBusBreakerView().removeAllBuses();
        } else if (resource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            getNodeBreakerView().getSwitches().forEach(s -> {
                getNodeBreakerView().removeSwitch(s.getId());
            });
        }
    }
}
