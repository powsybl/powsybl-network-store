/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.powsybl.cgmes.conversion.elements.CgmesTopologyKind;
import com.powsybl.cgmes.conversion.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.conversion.extensions.CimCharacteristics;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.iidm.impl.extensions.CgmesSvMetadataImpl;
import com.powsybl.network.store.iidm.impl.extensions.CimCharacteristicsImpl;
import com.powsybl.network.store.model.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.joda.time.DateTime;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkImpl extends AbstractIdentifiableImpl<Network, NetworkAttributes> implements Network, Validable {

    private final BusBreakerView busBreakerView = new BusBreakerViewImpl();

    private final BusView busView = new BusViewImpl();

    private final List<NetworkListener> listeners = new ArrayList<>();

    public NetworkImpl(NetworkStoreClient storeClient, Resource<NetworkAttributes> resource) {
        super(new NetworkObjectIndex(storeClient), resource);
        index.setNetwork(this);
    }

    public static NetworkImpl create(NetworkStoreClient storeClient, Resource<NetworkAttributes> resource) {
        return new NetworkImpl(storeClient, resource);
    }

    class BusBreakerViewImpl implements BusBreakerView {

        @Override
        public Iterable<Bus> getBuses() {
            return getBusStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getBusStream());
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return getSwitchStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getSwitchStream());
        }

        @Override
        public int getSwitchCount() {
            return (int) getSwitchStream().count();
        }

        @Override
        public Bus getBus(String id) {
            return getVoltageLevelStream().map(vl -> vl.getBusBreakerView().getBus(id))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
    }

    class BusViewImpl implements Network.BusView {

        @Override
        public Iterable<Bus> getBuses() {
            return getBusStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusView().getBusStream());
        }

        @Override
        public Bus getBus(String id) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            throw new UnsupportedOperationException("TODO");
        }
    }

    public NetworkObjectIndex getIndex() {
        return index;
    }

    public UUID getUuid() {
        return resource.getAttributes().getUuid();
    }

    @Override
    public String getId() {
        return resource.getId();
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.NETWORK;
    }

    @Override
    protected String getTypeDescription() {
        return "Network";
    }

    @Override
    public DateTime getCaseDate() {
        return resource.getAttributes().getCaseDate();
    }

    @Override
    public Network setCaseDate(DateTime date) {
        ValidationUtil.checkCaseDate(this, date);
        resource.getAttributes().setCaseDate(date);
        return this;
    }

    @Override
    public int getForecastDistance() {
        return resource.getAttributes().getForecastDistance();
    }

    @Override
    public Network setForecastDistance(int forecastDistance) {
        ValidationUtil.checkForecastDistance(this, forecastDistance);
        resource.getAttributes().setForecastDistance(forecastDistance);
        return this;
    }

    @Override
    public String getSourceFormat() {
        return resource.getAttributes().getSourceFormat();
    }

    @Override
    public VariantManagerImpl getVariantManager() {
        return new VariantManagerImpl();
    }

    // country

    @Override
    public Set<Country> getCountries() {
        return getSubstations()
                .stream()
                .map(Substation::getNullableCountry)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public int getCountryCount() {
        return getCountries().size();
    }

    // substation

    @Override
    public SubstationAdder newSubstation() {
        return new SubstationAdderImpl(index);
    }

    @Override
    public List<Substation> getSubstations() {
        return index.getSubstations();
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return getSubstations().stream();
    }

    @Override
    public int getSubstationCount() {
        return index.getSubstationCount();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Substation getSubstation(String id) {
        return index.getSubstation(id).orElse(null);
    }

    // voltage level

    @Override
    public List<VoltageLevel> getVoltageLevels() {
        return index.getVoltageLevels();
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return getVoltageLevels().stream();
    }

    @Override
    public int getVoltageLevelCount() {
        return index.getVoltageLevelCount();
    }

    @Override
    public VoltageLevel getVoltageLevel(String id) {
        return index.getVoltageLevel(id).orElse(null);
    }

    // generator

    @Override
    public List<Generator> getGenerators() {
        return index.getGenerators();
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getGenerators().stream();
    }

    @Override
    public int getGeneratorCount() {
        return index.getGeneratorCount();
    }

    @Override
    public Generator getGenerator(String id) {
        return index.getGenerator(id).orElse(null);
    }

    // battery

    @Override
    public List<Battery> getBatteries() {
        return index.getBatteries();
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getBatteries().stream();
    }

    @Override
    public int getBatteryCount() {
        return index.getBatteryCount();
    }

    @Override
    public Battery getBattery(String id) {
        return index.getBattery(id).orElse(null);
    }

    // load

    @Override
    public List<Load> getLoads() {
        return index.getLoads();
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getLoads().stream();
    }

    @Override
    public int getLoadCount() {
        return index.getLoadCount();
    }

    @Override
    public Load getLoad(String id) {
        return index.getLoad(id).orElse(null);
    }

    // shunt compensator

    @Override
    public List<ShuntCompensator> getShuntCompensators() {
        return index.getShuntCompensators();
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getShuntCompensators().stream();
    }

    @Override
    public int getShuntCompensatorCount() {
        return index.getShuntCompensatorCount();
    }

    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        return index.getShuntCompensator(id).orElse(null);
    }

    @Override
    public List<DanglingLine> getDanglingLines() {
        return index.getDanglingLines();
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return index.getDanglingLines().stream();
    }

    @Override
    public int getDanglingLineCount() {
        return index.getDanglingLineCount();
    }

    @Override
    public DanglingLine getDanglingLine(String id) {
        return index.getDanglingLine(id).orElse(null);
    }

    @Override
    public List<StaticVarCompensator> getStaticVarCompensators() {
        return index.getStaticVarCompensators();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getStaticVarCompensators().stream();
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return index.getStaticVarCompensatorCount();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        return index.getStaticVarCompensator(id).orElse(null);
    }

    // busbar sections

    @Override
    public List<BusbarSection> getBusbarSections() {
        return index.getBusbarSections();
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return getBusbarSections().stream();
    }

    @Override
    public int getBusbarSectionCount() {
        return index.getBusbarSectionCount();
    }

    @Override
    public List<HvdcConverterStation<?>> getHvdcConverterStations() {
        List<HvdcConverterStation<?>> hvdcConverterStationsList = new ArrayList<>();
        hvdcConverterStationsList.addAll(getLccConverterStations());
        hvdcConverterStationsList.addAll(getVscConverterStations());
        return hvdcConverterStationsList;
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return getHvdcConverterStations().stream();
    }

    @Override
    public int getHvdcConverterStationCount() {
        return index.getLccConverterStationCount() + index.getVscConverterStationCount();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(String id) {
        return index.getHvdcConverterStation(id).orElse(null);
    }

    @Override
    public List<LccConverterStation> getLccConverterStations() {
        return index.getLccConverterStations();
    }

    @Override
    public Stream<LccConverterStation>  getLccConverterStationStream() {
        return getLccConverterStations().stream();
    }

    @Override
    public int getLccConverterStationCount() {
        return index.getLccConverterStationCount();
    }

    @Override
    public LccConverterStation getLccConverterStation(String id) {
        return index.getLccConverterStation(id).orElse(null);
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        return index.getBusbarSection(id).orElse(null);
    }

    // switch

    @Override
    public List<Switch> getSwitches() {
        return index.getSwitches();
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return getSwitches().stream();
    }

    @Override
    public int getSwitchCount() {
        return index.getSwitchCount();
    }

    @Override
    public Switch getSwitch(String id) {
        return index.getSwitch(id).orElse(null);
    }

    // line

    @Override
    public List<Line> getLines() {
        return index.getLines();
    }

    @Override
    public Stream<Line> getLineStream() {
        return getLines().stream();
    }

    @Override
    public int getLineCount() {
        return index.getLineCount();
    }

    @Override
    public Line getLine(String id) {
        return index.getLine(id).orElse(null);
    }

    @Override
    public TieLineAdder newTieLine() {
        return new TieLineAdderImpl(index);
    }

    @Override
    public LineAdder newLine() {
        return new LineAdderImpl(index);
    }

    // 2 windings transformer

    @Override
    public List<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return index.getTwoWindingsTransformers();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getTwoWindingsTransformers().stream();
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return index.getTwoWindingsTransformerCount();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        return index.getTwoWindingsTransformer(id).orElse(null);
    }

    @Override
    public List<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return index.getThreeWindingsTransformers();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getThreeWindingsTransformers().stream();
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return index.getThreeWindingsTransformerCount();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        return index.getThreeWindingsTransformer(id).orElse(null);
    }

    // HVDC line

    @Override
    public List<HvdcLine> getHvdcLines() {
        return index.getHvdcLines();
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return getHvdcLines().stream();
    }

    @Override
    public int getHvdcLineCount() {
        return index.getHvdcLineCount();
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        return index.getHvdcLine(id).orElse(null);
    }

    @Override
    public HvdcLineAdder newHvdcLine() {
        return new HvdcLineAdderImpl(index);
    }

    @Override
    public HvdcLine getHvdcLine(HvdcConverterStation converterStation) {
        Objects.requireNonNull(converterStation);
        return getHvdcLineStream()
                .filter(l -> l.getConverterStation1().getId().equals(converterStation.getId()) || l.getConverterStation2().getId().equals(converterStation.getId()))
                .findFirst()
                .orElse(null);
    }

    // VSC converter station

    @Override
    public List<VscConverterStation> getVscConverterStations() {
        return index.getVscConverterStations();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getVscConverterStations().stream();
    }

    @Override
    public int getVscConverterStationCount() {
        return index.getVscConverterStationCount();
    }

    @Override
    public VscConverterStation getVscConverterStation(String id) {
        return index.getVscConverterStation(id).orElse(null);
    }

    @Override
    public List<Branch> getBranches() {
        return ImmutableList.<Branch>builder()
                .addAll(index.getLines())
                .addAll(index.getTwoWindingsTransformers())
                .build();
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return getBranches().stream();
    }

    @Override
    public int getBranchCount() {
        return getBranches().size();
    }

    @Override
    public Branch getBranch(String branchId) {
        return index.getLine(branchId)
                .map(l -> (Branch) l)
                .orElseGet(() -> index.getTwoWindingsTransformer(branchId)
                                      .orElse(null));
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        return index.getIdentifiable(id);
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return index.getIdentifiables();
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
    public void merge(Network other) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void merge(Network... others) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addListener(NetworkListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    public List<NetworkListener> getListeners() {
        return listeners;
    }

    @Override
    public <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        return getConnectableStream(clazz).collect(Collectors.toList());
    }

    @Override
    public <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        return index.getIdentifiables().stream().filter(clazz::isInstance).map(clazz::cast);
    }

    @Override
    public <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        return Ints.checkedCast(getConnectableStream(clazz).count());
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return getConnectables(Connectable.class);
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return getConnectableStream(Connectable.class);
    }

    @Override
    public int getConnectableCount() {
        return Ints.checkedCast(getConnectableStream().count());
    }

    private void update(ComponentType componentType) {
        // build graph
        Graph<Identifiable, Object> graph = new Pseudograph<>(Object.class);

        for (VoltageLevel vl : getVoltageLevels()) {
            for (Bus bus : vl.getBusView().getBuses()) {
                graph.addVertex(bus);

                bus.visitConnectedEquipments(new DefaultTopologyVisitor() {

                    @Override
                    public void visitLine(Line line, Branch.Side side) {
                        graph.addVertex(line);
                        graph.addEdge(bus, line, new Object());
                    }

                    @Override
                    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
                        graph.addVertex(transformer);
                        graph.addEdge(bus, transformer, new Object());
                    }

                    @Override
                    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                        graph.addVertex(transformer);
                        graph.addEdge(bus, transformer, new Object());
                    }

                    @Override
                    public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                        if (componentType == ComponentType.CONNECTED) {
                            graph.addVertex(converterStation.getHvdcLine());
                            graph.addEdge(bus, converterStation.getHvdcLine(), new Object());
                        }
                    }
                });
            }
        }

        // calculate components
        List<Set<Bus>> sets = new ConnectivityInspector<>(graph).connectedSets()
                .stream()
                .map(set -> set.stream().filter(v -> v instanceof Bus)
                        .map(v -> (Bus) v)
                        .collect(Collectors.toSet()))
                .sorted((o1, o2) -> o2.size() - o1.size())
                .collect(Collectors.toList());

        // associate components to buses
        for (int num = 0; num < sets.size(); num++) {
            Set<Bus> buses = sets.get(num);
            for (Bus bus : buses) {
                if (componentType == ComponentType.CONNECTED) {
                    ((CalculatedBus) bus).setConnectedComponentNum(num);
                } else {
                    ((CalculatedBus) bus).setSynchronousComponentNum(num);
                }
            }
        }
    }

    void ensureConnectedComponentsUpToDate() {
        if (!resource.getAttributes().isConnectedComponentsValid()) {
            update(ComponentType.CONNECTED);
            resource.getAttributes().setConnectedComponentsValid(true);
        }
    }

    void ensureSynchronousComponentsUpToDate() {
        if (!resource.getAttributes().isSynchronousComponentsValid()) {
            update(ComponentType.SYNCHRONOUS);
            resource.getAttributes().setSynchronousComponentsValid(true);
        }
    }

    void invalidateComponents() {
        resource.getAttributes().setConnectedComponentsValid(false);
        resource.getAttributes().setSynchronousComponentsValid(false);
    }

    @Override
    public <E extends Extension<Network>> void addExtension(Class<? super E> type, E extension) {
        if (type == CgmesSvMetadata.class) {
            CgmesSvMetadata cgmesSvMetadata = (CgmesSvMetadata) extension;
            resource.getAttributes().setCgmesSvMetadata(
                    CgmesSvMetadataAttributes.builder()
                            .description(cgmesSvMetadata.getDescription())
                            .svVersion(cgmesSvMetadata.getSvVersion())
                            .dependencies(cgmesSvMetadata.getDependencies())
                            .modelingAuthoritySet(cgmesSvMetadata.getModelingAuthoritySet())
                            .build());
        }
        if (type == CimCharacteristics.class) {
            CimCharacteristics cimCharacteristics = (CimCharacteristics) extension;
            resource.getAttributes().setCimCharacteristics(
                    CimCharacteristicsAttributes.builder()
                            .cgmesTopologyKind(cimCharacteristics.getTopologyKind())
                            .cimVersion(cimCharacteristics.getCimVersion())
                            .build());
        }
        super.addExtension(type, extension);
    }

    @Override
    public <E extends Extension<Network>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createCgmesSvMetadata();
        if (extension != null) {
            extensions.add(extension);
        }
        extension = createCimCharacteristics();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public <E extends Extension<Network>> E getExtension(Class<? super E> type) {
        if (type == CgmesSvMetadata.class) {
            return (E) createCgmesSvMetadata();
        }
        if (type == CimCharacteristics.class) {
            return (E) createCimCharacteristics();
        }
        return super.getExtension(type);
    }

    @Override
    public <E extends Extension<Network>> E getExtensionByName(String name) {
        if (name.equals("cgmesSvMetadata")) {
            return (E) createCgmesSvMetadata();
        }
        if (name.equals("cimCharacteristics")) {
            return (E) createCimCharacteristics();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<Network>> E createCgmesSvMetadata() {
        E extension = null;
        CgmesSvMetadataAttributes attributes = resource.getAttributes().getCgmesSvMetadata();
        if (attributes != null) {
            extension = (E) new CgmesSvMetadataImpl(this);
        }
        return extension;
    }

    private <E extends Extension<Network>> E createCimCharacteristics() {
        E extension = null;
        CimCharacteristicsAttributes attributes = resource.getAttributes().getCimCharacteristics();
        if (attributes != null) {
            extension = (E) new CimCharacteristicsImpl(this);
        }
        return extension;
    }

    public NetworkImpl initCgmesSvMetadataAttributes(String description, int svVersion, List<String> dependencies, String modelingAuthoritySet) {
        resource.getAttributes().setCgmesSvMetadata(new CgmesSvMetadataAttributes(description, svVersion, dependencies, modelingAuthoritySet));
        return this;
    }

    public NetworkImpl initCimCharacteristicsAttributes(CgmesTopologyKind cgmesTopologyKind, int cimVersion) {
        resource.getAttributes().setCimCharacteristics(new CimCharacteristicsAttributes(cgmesTopologyKind, cimVersion));
        return this;
    }
}
