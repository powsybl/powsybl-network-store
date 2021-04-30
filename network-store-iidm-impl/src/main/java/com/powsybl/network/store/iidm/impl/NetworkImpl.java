/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.iidm.impl.extensions.CgmesControlAreasImpl;
import com.powsybl.network.store.iidm.impl.extensions.CgmesSshMetadataImpl;
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

    public Map<String, String> getIdByAlias() {
        NetworkAttributes attributes = resource.getAttributes();
        if (attributes.getIdByAlias() ==  null) {
            attributes.setIdByAlias(new HashMap<>());
        }
        return attributes.getIdByAlias();
    }

    public void addAlias(String alias, String id) {
        getIdByAlias().put(alias, id);
        updateResource();
    }

    public void removeAlias(String alias) {
        getIdByAlias().remove(alias);
        updateResource();
    }

    public boolean checkAliasUnicity(AbstractIdentifiableImpl obj, String alias) {
        Objects.requireNonNull(alias);
        Identifiable<?> identifiable = getIdentifiable(alias);
        if (identifiable != null) {
            if (identifiable.equals(obj)) {
                // Silently ignore affecting the objects id to its own aliases
                return false;
            }
            String message = String.format("Object (%s) with alias '%s' cannot be created because alias already refers to object (%s) with ID '%s'",
                    obj.getClass(),
                    alias,
                    identifiable.getClass(),
                    identifiable.getId());
            throw new PowsyblException(message);
        }
        return true;
    }

    public String getIdFromAlias(String alias) {
        Objects.requireNonNull(alias);
        return getIdByAlias().get(alias) == null ? alias : getIdByAlias().get(alias);
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
            return getBusStream().filter(b -> b.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public Collection<Component> getConnectedComponents() { // FIXME : need a reference bus by component
            return getBusStream().map(Bus::getConnectedComponent).collect(Collectors.toList());
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
        updateResource();
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
        updateResource();
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
        return index.getSubstations().size();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return getSubstations(Optional.ofNullable(country).map(Country::getName).orElse(null), tsoId, geographicalTags);
    }

    @Override
    public Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags) {
        return getSubstationStream().filter(substation -> {
            if (country != null && !country.equals(substation.getCountry().map(Country::getName).orElse(""))) {
                return false;
            }
            if (tsoId != null && !tsoId.equals(substation.getTso())) {
                return false;
            }
            for (String tag : geographicalTags) {
                if (!substation.getGeographicalTags().contains(tag)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public Substation getSubstation(String id) {
        return index.getSubstation(getIdFromAlias(id)).orElse(null);
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
        return index.getVoltageLevels().size();
    }

    @Override
    public VoltageLevel getVoltageLevel(String id) {
        return index.getVoltageLevel(getIdFromAlias(id)).orElse(null);
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
        return index.getGenerators().size();
    }

    @Override
    public Generator getGenerator(String id) {
        return index.getGenerator(getIdFromAlias(id)).orElse(null);
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
        return index.getBatteries().size();
    }

    @Override
    public Battery getBattery(String id) {
        return index.getBattery(getIdFromAlias(id)).orElse(null);
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
        return index.getLoads().size();
    }

    @Override
    public Load getLoad(String id) {
        return index.getLoad(getIdFromAlias(id)).orElse(null);
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
        return index.getShuntCompensators().size();
    }

    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        return index.getShuntCompensator(getIdFromAlias(id)).orElse(null);
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
        return index.getDanglingLines().size();
    }

    @Override
    public DanglingLine getDanglingLine(String id) {
        return index.getDanglingLine(getIdFromAlias(id)).orElse(null);
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
        return index.getStaticVarCompensators().size();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        return index.getStaticVarCompensator(getIdFromAlias(id)).orElse(null);
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
        return index.getBusbarSections().size();
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
        return index.getLccConverterStations().size() + index.getVscConverterStations().size();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(String id) {
        return index.getHvdcConverterStation(getIdFromAlias(id)).orElse(null);
    }

    @Override
    public List<LccConverterStation> getLccConverterStations() {
        return index.getLccConverterStations();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getLccConverterStations().stream();
    }

    @Override
    public int getLccConverterStationCount() {
        return index.getLccConverterStations().size();
    }

    @Override
    public LccConverterStation getLccConverterStation(String id) {
        return index.getLccConverterStation(getIdFromAlias(id)).orElse(null);
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        return index.getBusbarSection(getIdFromAlias(id)).orElse(null);
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
        return index.getSwitches().size();
    }

    @Override
    public Switch getSwitch(String id) {
        return index.getSwitch(getIdFromAlias(id)).orElse(null);
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
        return index.getLines().size();
    }

    @Override
    public Line getLine(String id) {
        return index.getLine(getIdFromAlias(id)).orElse(null);
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
        return index.getTwoWindingsTransformers().size();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        return index.getTwoWindingsTransformer(getIdFromAlias(id)).orElse(null);
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
        return index.getThreeWindingsTransformers().size();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        return index.getThreeWindingsTransformer(getIdFromAlias(id)).orElse(null);
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
        return index.getHvdcLines().size();
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        return index.getHvdcLine(getIdFromAlias(id)).orElse(null);
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
        return index.getVscConverterStations().size();
    }

    @Override
    public VscConverterStation getVscConverterStation(String id) {
        return index.getVscConverterStation(getIdFromAlias(id)).orElse(null);
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
                .map(Branch.class::cast)
                .orElseGet(() -> index.getTwoWindingsTransformer(branchId)
                        .orElse(null));
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        return index.getIdentifiable(getIdFromAlias(id));
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
                        .map(Bus.class::cast)
                        .collect(Collectors.toSet()))
                .sorted((o1, o2) -> o2.size() - o1.size()) // Main component is the first
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
            updateResource();
        }
    }

    void ensureSynchronousComponentsUpToDate() {
        if (!resource.getAttributes().isSynchronousComponentsValid()) {
            update(ComponentType.SYNCHRONOUS);
            resource.getAttributes().setSynchronousComponentsValid(true);
            updateResource();
        }
    }

    void invalidateComponents() {
        resource.getAttributes().setConnectedComponentsValid(false);
        resource.getAttributes().setSynchronousComponentsValid(false);
        updateResource();
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
            updateResource();
        }
        if (type == CgmesSshMetadata.class) {
            CgmesSshMetadata cgmesSshMetadata = (CgmesSshMetadata) extension;
            resource.getAttributes().setCgmesSshMetadata(
                    CgmesSshMetadataAttributes.builder()
                            .description(cgmesSshMetadata.getDescription())
                            .sshVersion(cgmesSshMetadata.getSshVersion())
                            .dependencies(cgmesSshMetadata.getDependencies())
                            .modelingAuthoritySet(cgmesSshMetadata.getModelingAuthoritySet())
                            .build());
            updateResource();
        }
        if (type == CimCharacteristics.class) {
            CimCharacteristics cimCharacteristics = (CimCharacteristics) extension;
            resource.getAttributes().setCimCharacteristics(
                    CimCharacteristicsAttributes.builder()
                            .cgmesTopologyKind(cimCharacteristics.getTopologyKind())
                            .cimVersion(cimCharacteristics.getCimVersion())
                            .build());
            updateResource();
        }
        if (type == CgmesControlAreas.class) {
            resource.getAttributes().setCgmesControlAreas(new CgmesControlAreasAttributes());
            updateResource();
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
        extension = createCgmesSshMetadata();
        if (extension != null) {
            extensions.add(extension);
        }
        extension = createCimCharacteristics();
        if (extension != null) {
            extensions.add(extension);
        }
        extension = createCgmesControlAreas();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public <E extends Extension<Network>> E getExtension(Class<? super E> type) {
        if (type == CgmesSvMetadata.class) {
            return createCgmesSvMetadata();
        }
        if (type == CgmesSshMetadata.class) {
            return createCgmesSshMetadata();
        }
        if (type == CimCharacteristics.class) {
            return createCimCharacteristics();
        }
        if (type == CgmesControlAreas.class) {
            return createCgmesControlAreas();
        }
        return super.getExtension(type);
    }

    @Override
    public <E extends Extension<Network>> E getExtensionByName(String name) {
        if (name.equals("cgmesSvMetadata")) {
            return createCgmesSvMetadata();
        }
        if (name.equals("cgmesSshMetadata")) {
            return createCgmesSshMetadata();
        }
        if (name.equals("cimCharacteristics")) {
            return createCimCharacteristics();
        }
        if (name.equals("cgmesControlAreas")) {
            return createCgmesControlAreas();
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

    private <E extends Extension<Network>> E createCgmesSshMetadata() {
        E extension = null;
        CgmesSshMetadataAttributes attributes = resource.getAttributes().getCgmesSshMetadata();
        if (attributes != null) {
            extension = (E) new CgmesSshMetadataImpl(this);
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

    private <E extends Extension<Network>> E createCgmesControlAreas() {
        E extension = null;
        CgmesControlAreasAttributes attributes = resource.getAttributes().getCgmesControlAreas();
        if (attributes != null) {
            extension = (E) new CgmesControlAreasImpl(this);
        }
        return extension;
    }

    public NetworkImpl initCgmesSvMetadataAttributes(String description, int svVersion, List<String> dependencies, String modelingAuthoritySet) {
        resource.getAttributes().setCgmesSvMetadata(new CgmesSvMetadataAttributes(description, svVersion, dependencies, modelingAuthoritySet));
        updateResource();
        return this;
    }

    public NetworkImpl initCgmesSshMetadataAttributes(String description, int sshVersion, List<String> dependencies, String modelingAuthoritySet) {
        resource.getAttributes().setCgmesSshMetadata(new CgmesSshMetadataAttributes(description, sshVersion, dependencies, modelingAuthoritySet));
        updateResource();
        return this;
    }

    public NetworkImpl initCimCharacteristicsAttributes(CgmesTopologyKind cgmesTopologyKind, int cimVersion) {
        resource.getAttributes().setCimCharacteristics(new CimCharacteristicsAttributes(cgmesTopologyKind, cimVersion));
        updateResource();
        return this;
    }
}
