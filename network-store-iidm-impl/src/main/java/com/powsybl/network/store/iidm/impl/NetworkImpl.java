/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.powsybl.cgmes.extensions.BaseVoltageMapping;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.network.store.iidm.impl.extensions.BaseVoltageMappingImpl;
import com.powsybl.network.store.iidm.impl.extensions.CimCharacteristicsImpl;
import com.powsybl.network.store.model.BaseVoltageMappingAttributes;
import com.powsybl.network.store.model.CimCharacteristicsAttributes;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkImpl extends AbstractIdentifiableImpl<Network, NetworkAttributes> implements Network, Validable {

    private final BusBreakerView busBreakerView = new BusBreakerViewImpl();

    private final BusView busView = new BusViewImpl();

    private ValidationLevel minValidationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;

    private final List<NetworkListener> listeners = new ArrayList<>();

    private AbstractReportNodeContext reporterContext;

    private final Map<String, SubnetworkImpl> subnetworks = new LinkedHashMap<>();

    public NetworkImpl(NetworkStoreClient storeClient, Resource<NetworkAttributes> resource) {
        super(new NetworkObjectIndex(storeClient), resource);
        this.reporterContext = new SimpleReportNodeContext();
        index.setNetwork(this);
    }

    public NetworkImpl(NetworkObjectIndex index, Resource<NetworkAttributes> resource) {
        super(index, resource);
    }

    public static NetworkImpl create(NetworkStoreClient storeClient, Resource<NetworkAttributes> resource) {
        return new NetworkImpl(storeClient, resource);
    }

    public Map<String, String> getIdByAlias() {
        return getIdByAlias(getResource());
    }

    public Map<String, String> getIdByAlias(Resource<NetworkAttributes> resource) {
        NetworkAttributes attributes = resource.getAttributes();
        if (attributes.getIdByAlias() == null) {
            attributes.setIdByAlias(new HashMap<>());
        }
        return attributes.getIdByAlias();
    }

    public void addAlias(String alias, String id) {
        var oldValue = alias + ":" + (getResource().getAttributes().getIdByAlias() != null ? getResource().getAttributes().getIdByAlias().get(alias) : null);
        updateResource(res -> getIdByAlias(res).put(alias, id),
            "alias", oldValue, alias + ":" + id);
    }

    public void removeAlias(String alias) {
        var oldValue = alias + ":" + (getResource().getAttributes().getIdByAlias() != null ? getResource().getAttributes().getIdByAlias().get(alias) : null);
        updateResource(res -> getIdByAlias(res).remove(alias),
            "alias", oldValue, null);
    }

    public boolean checkAliasUnicity(AbstractIdentifiableImpl<?, ?> obj, String alias) {
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
        public int getBusCount() {
            return (int) getBusStream().count();
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
            Optional<Bus> busInBusBreakerTopo = index.getConfiguredBus(id).map(Function.identity()); // start search in BB topo
            return busInBusBreakerTopo.or(() -> getVoltageLevelStream().map(vl -> vl.getBusBreakerView().getBus(id)) // fallback to search in NB topo
                                                                       .filter(Objects::nonNull)
                                                                       .findFirst())
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

        private Map<String, Bus> getIdToBusMap() {
            return getBusStream().collect(ImmutableMap.toImmutableMap(Bus::getId, Functions.identity()));
        }

        @Override
        public Bus getBus(String id) {
            // checking cached data first
            Map<String, Bus> cachesBuses = getResource().getAttributes().getBusCache();
            if (cachesBuses != null) {
                return cachesBuses.get(id);
            }
            // if cache not existing, creating it
            Map<String, Bus> idToBusMap = getIdToBusMap();
            getResource().getAttributes().setBusCache(idToBusMap);
            return idToBusMap.get(id);
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            return getBusStream().map(Bus::getConnectedComponent)
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingInt(Component::getNum))), ArrayList::new));
        }

        @Override
        public Collection<Component> getSynchronousComponents() {
            return getBusStream().map(Bus::getSynchronousComponent)
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingInt(Component::getNum))), ArrayList::new));
        }
    }

    @Override
    public NetworkObjectIndex getIndex() {
        return index;
    }

    public UUID getUuid() {
        return getResource().getAttributes().getUuid();
    }

    @Override
    public String getId() {
        return getResource().getId();
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.NETWORK;
    }

    @Override
    public ZonedDateTime getCaseDate() {
        return getResource().getAttributes().getCaseDate();
    }

    @Override
    public Network setCaseDate(ZonedDateTime date) {
        ValidationUtil.checkCaseDate(this, date);
        ZonedDateTime oldValue = getCaseDate();
        updateResource(res -> res.getAttributes().setCaseDate(date),
            "caseDate", oldValue, date);
        return this;
    }

    @Override
    public int getForecastDistance() {
        return getResource().getAttributes().getForecastDistance();
    }

    @Override
    public Network setForecastDistance(int forecastDistance) {
        ValidationUtil.checkForecastDistance(this, forecastDistance);
        int oldValue = getForecastDistance();
        updateResource(res -> res.getAttributes().setForecastDistance(forecastDistance),
            "forecastDistance", oldValue, forecastDistance);
        return this;
    }

    @Override
    public String getSourceFormat() {
        return getResource().getAttributes().getSourceFormat();
    }

    @Override
    public VariantManagerImpl getVariantManager() {
        return new VariantManagerImpl(index);
    }

    @Override
    public void allowReportNodeContextMultiThreadAccess(boolean allow) {
        this.reporterContext = Networks.allowReportNodeContextMultiThreadAccess(this.reporterContext, allow);
    }

    @Override
    public ReportNodeContext getReportNodeContext() {
        return this.reporterContext;
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
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).collect(Collectors.toList());
    }

    @Override
    public List<DanglingLine> getDanglingLines() {
        return index.getDanglingLines();
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return index.getDanglingLines().stream().filter(danglingLineFilter.getPredicate());
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
    public Iterable<TieLine> getTieLines() {
        return index.getTieLines();
    }

    @Override
    public Stream<TieLine> getTieLineStream() {
        return index.getTieLines().stream();
    }

    @Override
    public int getTieLineCount() {
        return index.getTieLines().size();
    }

    @Override
    public TieLine getTieLine(String s) {
        return index.getTieLine(getIdFromAlias(s)).orElse(null);
    }

    @Override
    public TieLineAdder newTieLine() {
        return new TieLineAdderImpl(index);
    }

    @Override
    public LineAdder newLine() {
        return new LineAdderImpl(index);
    }

    @Override
    public LineAdder newLine(Line line) {
        return new LineAdderImpl(index, line);
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
                .addAll(index.getTieLines())
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
        return index.getBranch(branchId);
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
    public VoltageLevelAdder newVoltageLevel() {
        return new VoltageLevelAdderImpl(index, null);
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
    public VoltageAngleLimitAdder newVoltageAngleLimit() {
        throw new UnsupportedOperationException("TODO");
    }

    //TODO implement
    @Override
    public Iterable<VoltageAngleLimit> getVoltageAngleLimits() {
        return new ArrayList<>();
    }

    //TODO implement
    @Override
    public Stream<VoltageAngleLimit> getVoltageAngleLimitsStream() {
        return Stream.empty();
    }

    //TODO implement
    @Override
    public VoltageAngleLimit getVoltageAngleLimit(String s) {
        return null;
    }

    @Override
    public Network createSubnetwork(String subnetworkId, String name, String sourceFormat) {
        if (subnetworks.containsKey(subnetworkId)) {
            throw new IllegalArgumentException("The network already contains another subnetwork of id " + subnetworkId);
        }
        SubnetworkImpl subnetwork = new SubnetworkImpl(subnetworkId, getIndex(), getResource(), sourceFormat);
        subnetworks.put(subnetworkId, subnetwork);
        return subnetwork;
    }

    @Override
    public Network detach() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isDetachable() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Set<Identifiable<?>> getBoundaryElements() {
        return new HashSet<>();
    }

    @Override
    public boolean isBoundaryElement(Identifiable<?> identifiable) {
        return false;
    }

    @Override
    public void flatten() {
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

    private void update(ComponentType componentType, boolean isBusView) {
        // build graph
        Graph<Identifiable, Object> graph = new Pseudograph<>(Object.class);

        for (VoltageLevel vl : getVoltageLevels()) {
            for (Bus bus : isBusView ? vl.getBusView().getBuses() : vl.getBusBreakerView().getBuses()) {
                graph.addVertex(bus);

                bus.visitConnectedEquipments(new DefaultTopologyVisitor() {

                    @Override
                    public void visitLine(Line line, TwoSides side) {
                        graph.addVertex(line);
                        graph.addEdge(bus, line, new Object());
                    }

                    @Override
                    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
                        graph.addVertex(transformer);
                        graph.addEdge(bus, transformer, new Object());
                    }

                    @Override
                    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {
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

                    @Override
                    public void visitDanglingLine(DanglingLine danglingLine) {
                        if (danglingLine.isPaired()) {
                            TieLine tieLine = danglingLine.getTieLine().orElseThrow();
                            graph.addVertex(tieLine);
                            graph.addEdge(bus, tieLine, new Object());
                        }
                    }
                });
            }

            if (!isBusView) {
                for (Switch sw : vl.getBusBreakerView().getSwitches()) {
                    Bus bus1 = vl.getBusBreakerView().getBus1(sw.getId());
                    Bus bus2 = vl.getBusBreakerView().getBus2(sw.getId());
                    graph.addEdge(bus1, bus2, new Object());
                }
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

    void ensureConnectedComponentsUpToDate(boolean isBusView) {
        var resource = getResource();
        if (!resource.getAttributes().isConnectedComponentsValid()) {
            update(ComponentType.CONNECTED, isBusView);
            updateResourceWithoutNotification(res -> res.getAttributes().setConnectedComponentsValid(true));
        }
    }

    void ensureSynchronousComponentsUpToDate(boolean isBusView) {
        var resource = getResource();
        if (!resource.getAttributes().isSynchronousComponentsValid()) {
            update(ComponentType.SYNCHRONOUS, isBusView);
            updateResourceWithoutNotification(res -> res.getAttributes().setSynchronousComponentsValid(true));
        }
    }

    void invalidateComponents() {
        updateResourceWithoutNotification(res -> res.getAttributes().setConnectedComponentsValid(false));
        updateResourceWithoutNotification(res -> res.getAttributes().setSynchronousComponentsValid(false));
    }

    void invalidateCalculatedBuses() {
        invalidateComponents();
        getResource().getAttributes().setBusCache(null);
    }

    @Override
    public <E extends Extension<Network>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createCimCharacteristics();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public <E extends Extension<Network>> E getExtension(Class<? super E> type) {
        if (type == CimCharacteristics.class) {
            return createCimCharacteristics();
        }
        if (type == BaseVoltageMapping.class) {
            return createBaseVoltageMapping();
        }
        return super.getExtension(type);
    }

    @Override
    public <E extends Extension<Network>> E getExtensionByName(String name) {
        if (name.equals("cimCharacteristics")) {
            return createCimCharacteristics();
        }
        if (name.equals("baseVoltageMapping")) {
            return createBaseVoltageMapping();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<Network>> E createCimCharacteristics() {
        E extension = null;
        var resource = getResource();
        CimCharacteristicsAttributes attributes = resource.getAttributes().getCimCharacteristics();
        if (attributes != null) {
            extension = (E) new CimCharacteristicsImpl(this);
        }
        return extension;
    }

    private <E extends Extension<Network>> E createBaseVoltageMapping() {
        E extension = null;
        var resource = getResource();
        BaseVoltageMappingAttributes attributes = resource.getAttributes().getBaseVoltageMapping();
        if (attributes != null) {
            extension = (E) new BaseVoltageMappingImpl(this);
        }
        return extension;
    }

    @Override
    public Iterable<OverloadManagementSystem> getOverloadManagementSystems() {
        // FIXME: implement
        return Collections.emptyList();
    }

    @Override
    public Stream<OverloadManagementSystem> getOverloadManagementSystemStream() {
        // FIXME: implement
        return Stream.empty();
    }

    @Override
    public int getOverloadManagementSystemCount() {
        // FIXME: implement
        return 0;
    }

    @Override
    public OverloadManagementSystem getOverloadManagementSystem(String id) {
        // FIXME: implement
        return null;
    }

    @Override
    public Iterable<Ground> getGrounds() {
        return index.getGrounds();
    }

    @Override
    public Stream<Ground> getGroundStream() {
        return index.getGrounds().stream();
    }

    @Override
    public int getGroundCount() {
        return index.getGrounds().size();
    }

    @Override
    public Ground getGround(String id) {
        return index.getGround(getIdFromAlias(id)).orElse(null);
    }

    @Override
    public Iterable<String> getAreaTypes() {
        return getAreaTypeStream().toList();
    }

    @Override
    public Stream<String> getAreaTypeStream() {
        return getAreaStream().map(Area::getAreaType).distinct();
    }

    @Override
    public int getAreaTypeCount() {
        return (int) getAreaTypeStream().count();
    }

    @Override
    public AreaAdder newArea() {
        return new AreaAdderImpl(index);
    }

    @Override
    public Iterable<Area> getAreas() {
        return getAreaStream().toList();
    }

    @Override
    public Stream<Area> getAreaStream() {
        return index.getAreas().stream();
    }

    @Override
    public Network setMinimumAcceptableValidationLevel(ValidationLevel minLevel) {
        // TODO implement this to comply with the test in AbstractNetworkTest testSetMinimumAcceptableValidationLevelOnInvalidatedNetwork()
        Objects.requireNonNull(minLevel);
        ValidationLevel currentLevel = getValidationLevel();
        if (currentLevel.compareTo(minLevel) < 0) {
            throw new ValidationException(this, "Network should be corrected in order to correspond to validation level " + minLevel);
        }
        this.minValidationLevel = minLevel;
        return this;
    }

    ValidationLevel getMinValidationLevel() {
        return minValidationLevel;
    }

    @Override
    public Area getArea(String id) {
        return index.getArea(getIdFromAlias(id)).orElse(null);
    }

    @Override
    public int getAreaCount() {
        return getAreaStream().toList().size();
    }

    // DC modelling
    // These methods will allow for more detailed modeling of HVDCs.
    //  This is a long-term work on the powsybl side.
    //  It is too early to implement it on the network-store side and the need remains to be verified.
    @Override
    public DcNodeAdder newDcNode() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public Iterable<DcNode> getDcNodes() {
        // FIXME: if needed implement detailed dc model
        // needed for export in https://github.com/powsybl/powsybl-core/blob/main/iidm/iidm-serde/src/main/java/com/powsybl/iidm/serde/NetworkSerDe.java#L398
        return Collections.emptyList();
    }

    @Override
    public Stream<DcNode> getDcNodeStream() {
        // FIXME: if needed implement detailed dc model
        return Stream.empty();
    }

    @Override
    public int getDcNodeCount() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public DcNode getDcNode(String s) {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public DcLineAdder newDcLine() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public Iterable<DcLine> getDcLines() {
        // FIXME: if needed implement detailed dc model
        // needed for export in https://github.com/powsybl/powsybl-core/blob/main/iidm/iidm-serde/src/main/java/com/powsybl/iidm/serde/NetworkSerDe.java#L422
        return Collections.emptyList();
    }

    @Override
    public Stream<DcLine> getDcLineStream() {
        // FIXME: if needed implement detailed dc model
        return Stream.empty();
    }

    @Override
    public int getDcLineCount() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public DcLine getDcLine(String s) {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public DcSwitchAdder newDcSwitch() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public Iterable<DcSwitch> getDcSwitches() {
        // FIXME: if needed implement detailed dc model
        // needed for export in https://github.com/powsybl/powsybl-core/blob/main/iidm/iidm-serde/src/main/java/com/powsybl/iidm/serde/NetworkSerDe.java#L434
        return Collections.emptyList();
    }

    @Override
    public Stream<DcSwitch> getDcSwitchStream() {
        // FIXME: if needed implement detailed dc model
        return Stream.empty();
    }

    @Override
    public int getDcSwitchCount() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public DcSwitch getDcSwitch(String s) {
        // FIXME: if needed implement detailed dc model
        return null;
    }

    @Override
    public DcGroundAdder newDcGround() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public Iterable<DcGround> getDcGrounds() {
        // FIXME: if needed implement detailed dc model
        // needed for export in https://github.com/powsybl/powsybl-core/blob/main/iidm/iidm-serde/src/main/java/com/powsybl/iidm/serde/NetworkSerDe.java#L410
        return Collections.emptyList();
    }

    @Override
    public Stream<DcGround> getDcGroundStream() {
        // FIXME: if needed implement detailed dc model
        return Stream.empty();
    }

    @Override
    public int getDcGroundCount() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public DcGround getDcGround(String s) {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public Iterable<LineCommutatedConverter> getLineCommutatedConverters() {
        // FIXME: if needed implement detailed dc model
        return Collections.emptyList();
    }

    @Override
    public Stream<LineCommutatedConverter> getLineCommutatedConverterStream() {
        // FIXME: if needed implement detailed dc model
        return Stream.empty();
    }

    @Override
    public int getLineCommutatedConverterCount() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public LineCommutatedConverter getLineCommutatedConverter(String s) {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public Iterable<VoltageSourceConverter> getVoltageSourceConverters() {
        // FIXME: if needed implement detailed dc model
        return Collections.emptyList();
    }

    @Override
    public Stream<VoltageSourceConverter> getVoltageSourceConverterStream() {
        // FIXME: if needed implement detailed dc model
        return Stream.empty();
    }

    @Override
    public int getVoltageSourceConverterCount() {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public VoltageSourceConverter getVoltageSourceConverter(String s) {
        throw new PowsyblException("Detailed DC network not implemented");
    }

    @Override
    public DcBus getDcBus(String id) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<DcBus> getDcBuses() {
        // FIXME: if needed implement detailed dc model
        return Collections.emptyList();
    }

    @Override
    public Stream<DcBus> getDcBusStream() {
        // FIXME: if needed implement detailed dc model
        return Stream.empty();
    }

    @Override
    public int getDcBusCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Collection<Component> getDcComponents() {
        // FIXME: if needed implement detailed dc model
        return Collections.emptyList();
    }

    @Override
    public Iterable<DcConnectable> getDcConnectables() {
        // FIXME: if needed implement detailed dc model
        // needed for cgmes export in https://github.com/powsybl/powsybl-core/blob/main/cgmes/cgmes-conversion/src/main/java/com/powsybl/cgmes/conversion/export/CgmesExportContext.java#L362
        return Collections.emptyList();
    }
}
