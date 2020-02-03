/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import org.joda.time.DateTime;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkImpl extends AbstractIdentifiableImpl<Network, NetworkAttributes> implements Network {

    public NetworkImpl(NetworkStoreClient storeClient, Resource<NetworkAttributes> resource) {
        super(new NetworkObjectIndex(storeClient), resource);
        index.setNetwork(this);
    }

    static NetworkImpl create(NetworkStoreClient storeClient, Resource<NetworkAttributes> resource) {
        return new NetworkImpl(storeClient, resource);
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
    public DateTime getCaseDate() {
        return resource.getAttributes().getCaseDate();
    }

    @Override
    public Network setCaseDate(DateTime date) {
        resource.getAttributes().setCaseDate(date);
        return this;
    }

    @Override
    public int getForecastDistance() {
        return resource.getAttributes().getForecastDistance();
    }

    @Override
    public Network setForecastDistance(int forecastDistance) {
        resource.getAttributes().setForecastDistance(forecastDistance);
        return this;
    }

    @Override
    public String getSourceFormat() {
        return resource.getAttributes().getSourceFormat();
    }

    @Override
    public VariantManager getVariantManager() {
        throw new UnsupportedOperationException("TODO");
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

    @Override
    public Iterable<Battery> getBatteries() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getBatteryCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Battery getBattery(String id) {
        throw new UnsupportedOperationException("TODO");
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
        HvdcConverterStation<?> hvdcConverterStation =  index.getLccConverterStation(id).orElse(null);
        if (hvdcConverterStation == null) {
            hvdcConverterStation = index.getVscConverterStation(id).orElse(null);
        }
        return hvdcConverterStation;
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
        throw new UnsupportedOperationException("TODO");
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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BusView getBusView() {
        throw new UnsupportedOperationException("TODO");
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
        //throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void removeListener(NetworkListener listener) {
        throw new UnsupportedOperationException("TODO");
    }
}
