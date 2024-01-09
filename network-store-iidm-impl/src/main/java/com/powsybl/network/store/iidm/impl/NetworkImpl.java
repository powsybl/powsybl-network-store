/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.cgmes.extensions.BaseVoltageMapping;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.network.store.iidm.impl.extensions.BaseVoltageMappingImpl;
import com.powsybl.network.store.model.BaseVoltageMappingAttributes;
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

import static com.powsybl.iidm.network.util.TieLineUtil.*;
/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkImpl extends AbstractNetwork<NetworkAttributes> implements Network, Validable {

    private final List<NetworkListener> listeners = new ArrayList<>();

    public NetworkImpl(NetworkStoreClient storeClient, Resource<NetworkAttributes> resource) {
        super(new NetworkObjectIndex(storeClient), resource);
        index.setNetwork(this);
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
        updateResource(res -> getIdByAlias(res).put(alias, id));
    }

    public void removeAlias(String alias) {
        updateResource(res -> getIdByAlias(res).remove(alias));
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
    public DateTime getCaseDate() {
        return getResource().getAttributes().getCaseDate();
    }

    @Override
    public Network setCaseDate(DateTime date) {
        ValidationUtil.checkCaseDate(this, date);
        updateResource(res -> res.getAttributes().setCaseDate(date));
        return this;
    }

    @Override
    public int getForecastDistance() {
        return getResource().getAttributes().getForecastDistance();
    }

    @Override
    public Network setForecastDistance(int forecastDistance) {
        ValidationUtil.checkForecastDistance(this, forecastDistance);
        updateResource(res -> res.getAttributes().setForecastDistance(forecastDistance));
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
    public VoltageAngleLimitAdder newVoltageAngleLimit() {
        throw new UnsupportedOperationException("Voltage angle limit are not supported in this implementation");
    }

    //TODO implement
    @Override
    public Iterable<VoltageAngleLimit> getVoltageAngleLimits() {
        throw new UnsupportedOperationException("Voltage angle limit are not supported in this implementation");
    }

    @Override
    public Stream<VoltageAngleLimit> getVoltageAngleLimitsStream() {
        throw new UnsupportedOperationException("Voltage angle limit are not supported in this implementation");
    }

    @Override
    public VoltageAngleLimit getVoltageAngleLimit(String s) {
        throw new UnsupportedOperationException("Voltage angle limit are not supported in this implementation");
    }

    @Override
    public Collection<Network> getSubnetworks() {
        return index.getSubnetworks();
    }

    @Override
    public Network getSubnetwork(String id) {
        return index.getSubnetwork(id).orElse(null);
    }

    @Override
    public Network createSubnetwork(String subnetworkId, String name, String sourceFormat) {
        Resource<SubnetworkAttributes> resourceSubNetwork = Resource.subnetwokBuilder()
                .id(subnetworkId)
                .variantNum(index.getWorkingVariantNum())
                .parentNetwork(this.getId())
                .attributes(SubnetworkAttributes.builder()
                        .uuid(UUID.randomUUID())
                        .build())
                .build();
        return index.createSubnetwork(resourceSubNetwork);
    }

    private static void checkIndependentNetwork(Network network) {
        if (network instanceof SubnetworkImpl) {
            throw new IllegalArgumentException("The network " + network.getId() + " is already a subnetwork");
        }
        if (!network.getSubnetworks().isEmpty()) {
            throw new IllegalArgumentException("The network " + network.getId() + " already contains subnetworks: not supported");
        }
    }

    class DanglingLinePair {
        String id;
        String name;
        String dl1Id;
        String dl2Id;
        Map<String, String> aliases;
        Properties properties = new Properties();
    }

    private void pairDanglingLines(List<DanglingLinePair> danglingLinePairs, DanglingLine dl1, DanglingLine dl2, Map<String, List<DanglingLine>> dl1byXnodeCode) {
        if (dl1 != null) {
            if (dl1.getPairingKey() != null) {
                dl1byXnodeCode.get(dl1.getPairingKey()).remove(dl1);
            }
            DanglingLinePair l = new DanglingLinePair();
            l.id = buildMergedId(dl1.getId(), dl2.getId());
            l.name = buildMergedName(dl1.getId(), dl2.getId(), dl1.getOptionalName().orElse(null), dl2.getOptionalName().orElse(null));
            l.dl1Id = dl1.getId();
            l.dl2Id = dl2.getId();
            l.aliases = new HashMap<>();
            // No need to merge properties or aliases because we keep the original dangling lines after merge
            danglingLinePairs.add(l);

            /*if (dl1.getId().equals(dl2.getId())) { // if identical IDs, rename dangling lines
                ((DanglingLineImpl) dl1).replaceId(l.dl1Id + "_1");
                ((DanglingLineImpl) dl2).replaceId(l.dl2Id + "_2");
                l.dl1Id = dl1.getId();
                l.dl2Id = dl2.getId();
            }*/
        }
    }

    private void replaceDanglingLineByTieLine(List<DanglingLinePair> lines) {
        for (DanglingLinePair danglingLinePair : lines) {
            TieLine l = newTieLine()
                    .setId(danglingLinePair.id)
                    .setEnsureIdUnicity(true)
                    .setName(danglingLinePair.name)
                    .setDanglingLine1(danglingLinePair.dl1Id)
                    .setDanglingLine2(danglingLinePair.dl2Id)
                    .add();
            danglingLinePair.properties.forEach((key, val) -> l.setProperty(key.toString(), val.toString()));
            danglingLinePair.aliases.forEach((alias, type) -> {
                if (type.isEmpty()) {
                    l.addAlias(alias);
                } else {
                    l.addAlias(alias, type);
                }
            });
        }
    }

    private static void createSubnetwork(NetworkImpl parent, NetworkImpl original) {
        // Handles the case of creating a subnetwork for itself without duplicating the id
        String idSubNetwork = parent != original ? original.getId() : Identifiables.getUniqueId(original.getId(), parent.getIndex()::contains);
        parent.createSubnetwork(idSubNetwork, "", original.getSourceFormat());
    }

    static Network merge(String id, String name, Network... networks) {
        if (networks == null || networks.length < 2) {
            throw new IllegalArgumentException("At least 2 networks are expected");
        }
        NetworkImpl mergedNetwork = (NetworkImpl) Network.create(id, networks[0].getSourceFormat());
        for (Network other : networks) {
            mergedNetwork.merge(other);
        }

        return mergedNetwork;
    }

    private void merge(Network other) {
        checkIndependentNetwork(other);
        NetworkImpl otherNetwork = (NetworkImpl) other;

        // this check must not be done on the number of variants but on the size
        // of the internal variant array because the network can have only
        // one variant but an internal array with a size greater than one and
        // some re-usable variants
        if (getVariantManager().getVariantIds().size() != 1 || otherNetwork.getVariantManager().getVariantIds().size() != 1) {
            throw new PowsyblException("Merging of multi-variants network is not supported");
        }

        // try to find dangling lines couples
        List<DanglingLinePair> lines = new ArrayList<>();
        Map<String, List<DanglingLine>> dl1byXnodeCode = new HashMap<>();

        for (DanglingLine dl1 : getDanglingLines(DanglingLineFilter.ALL)) {
            if (dl1.getPairingKey() != null) {
                dl1byXnodeCode.computeIfAbsent(dl1.getPairingKey(), k -> new ArrayList<>()).add(dl1);
            }
        }
        for (DanglingLine dl2 : Lists.newArrayList(other.getDanglingLines(DanglingLineFilter.ALL))) {
            findAndAssociateDanglingLines(dl2, dl1byXnodeCode::get, (dll1, dll2) -> pairDanglingLines(lines, dll1, dll2, dl1byXnodeCode));
        }

        // create a subnetwork for the other network
        createSubnetwork(this, otherNetwork);

        // merge the indexes
        index.merge(otherNetwork.index);

        replaceDanglingLineByTieLine(lines);
    }

    public void merge(Network... others) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Network detach() {
        throw new IllegalStateException("This network is already detached.");
    }

    @Override
    public boolean isDetachable() {
        return false;
    }

    @Override
    public Set<Identifiable<?>> getBoundaryElements() {
        return getDanglingLineStream(DanglingLineFilter.UNPAIRED).collect(Collectors.toSet());
    }

    @Override
    public boolean isBoundaryElement(Identifiable<?> identifiable) {
        return identifiable.getType() == IdentifiableType.DANGLING_LINE && !((DanglingLine) identifiable).isPaired();
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

    private void update(ComponentType componentType, boolean isBusView) {
        // build graph
        Graph<Identifiable, Object> graph = new Pseudograph<>(Object.class);

        for (VoltageLevel vl : getVoltageLevels()) {
            for (Bus bus : isBusView ? vl.getBusView().getBuses() : vl.getBusBreakerView().getBuses()) {
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
            updateResource(res -> res.getAttributes().setConnectedComponentsValid(true));
        }
    }

    void ensureSynchronousComponentsUpToDate(boolean isBusView) {
        var resource = getResource();
        if (!resource.getAttributes().isSynchronousComponentsValid()) {
            update(ComponentType.SYNCHRONOUS, isBusView);
            updateResource(res -> res.getAttributes().setSynchronousComponentsValid(true));
        }
    }

    void invalidateComponents() {
        updateResource(res -> {
            res.getAttributes().setConnectedComponentsValid(false);
            res.getAttributes().setSynchronousComponentsValid(false);
        });
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
        if (type == BaseVoltageMapping.class) {
            return createBaseVoltageMapping();
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
        if (name.equals("baseVoltageMapping")) {
            return createBaseVoltageMapping();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<Network>> E createCgmesSvMetadata() {
        E extension = null;
        var resource = getResource();
        CgmesSvMetadataAttributes attributes = resource.getAttributes().getCgmesSvMetadata();
        if (attributes != null) {
            extension = (E) new CgmesSvMetadataImpl(this);
        }
        return extension;
    }

    private <E extends Extension<Network>> E createCgmesSshMetadata() {
        E extension = null;
        var resource = getResource();
        CgmesSshMetadataAttributes attributes = resource.getAttributes().getCgmesSshMetadata();
        if (attributes != null) {
            extension = (E) new CgmesSshMetadataImpl(this);
        }
        return extension;
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

    private <E extends Extension<Network>> E createCgmesControlAreas() {
        E extension = null;
        var resource = getResource();
        CgmesControlAreasAttributes attributes = resource.getAttributes().getCgmesControlAreas();
        if (attributes != null) {
            extension = (E) new CgmesControlAreasImpl(this);
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
}
