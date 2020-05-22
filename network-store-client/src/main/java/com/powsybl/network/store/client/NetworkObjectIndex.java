/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A network global object index that guaranty a single instance of identifiable per network.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkObjectIndex {

    private final NetworkStoreClient storeClient;

    private NetworkImpl network;

    private final Map<String, Substation> substationById = new HashMap<>();

    private final Map<String, VoltageLevel> voltageLevelById = new HashMap<>();

    private final Map<String, Generator> generatorById = new HashMap<>();

    private final Map<String, ShuntCompensator> shuntCompensatorById = new HashMap<>();

    private final Map<String, VscConverterStation> vscConverterStationById = new HashMap<>();

    private final Map<String, LccConverterStation> lccConverterStationById = new HashMap<>();

    private final Map<String, StaticVarCompensator> staticVarCompensatorById = new HashMap<>();

    private final Map<String, Load> loadById = new HashMap<>();

    private final Map<String, BusbarSection> busbarSectionById = new HashMap<>();

    private final Map<String, Switch> switchById = new HashMap<>();

    private final Map<String, TwoWindingsTransformer> twoWindingsTransformerById = new HashMap<>();

    private final Map<String, ThreeWindingsTransformer> threeWindingsTransformerById = new HashMap<>();

    private final Map<String, Line> lineById = new HashMap<>();

    private final Map<String, HvdcLine> hvdcLineById = new HashMap<>();

    private final Map<String, DanglingLine> danglingLineById = new HashMap<>();

    private final Map<String, Bus> busesById = new HashMap<>();

    public NetworkObjectIndex(NetworkStoreClient storeClient) {
        this.storeClient = Objects.requireNonNull(storeClient);
    }

    public NetworkStoreClient getStoreClient() {
        return storeClient;
    }

    public void setNetwork(NetworkImpl network) {
        this.network = Objects.requireNonNull(network);
    }

    NetworkImpl getNetwork() {
        return network;
    }

    private static <T extends Identifiable<T>, U extends IdentifiableAttributes> List<T> getAll(Map<String, T> objectsById,
                                                                                                Supplier<List<Resource<U>>> resourcesSupplier,
                                                                                                Function<Resource<U>, T> objectCreator) {
        List<Resource<U>> resources = resourcesSupplier.get();
        if (resources.size() != objectsById.size()) {
            for (Resource<U> resource : resources) {
                if (!objectsById.containsKey(resource.getId())) {
                    objectsById.put(resource.getId(), objectCreator.apply(resource));
                }
            }
        }
        return new ArrayList<>(objectsById.values());
    }

    private static <T extends Identifiable<T>, U extends IdentifiableAttributes> List<T> getSome(Map<String, T> objectsById,
                                                                                                 Supplier<List<Resource<U>>> resourcesSupplier,
                                                                                                 Function<Resource<U>, T> objectCreator) {
        List<Resource<U>> resources = resourcesSupplier.get();
        List<T> some = new ArrayList<>(resources.size());
        for (Resource<U> resource : resources) {
            T object = objectsById.get(resource.getId());
            if (object == null) {
                object = objectCreator.apply(resource);
                objectsById.put(object.getId(), object);
            }
            some.add(object);
        }
        return some;
    }

    @SuppressWarnings("unchecked")
    private <T extends Identifiable<T>, U extends IdentifiableAttributes, V extends T> Optional<V> getOne(String id,
                                                                                                          Map<String, T> objectsById,
                                                                                                          Supplier<Optional<Resource<U>>> resourceSupplier,
                                                                                                          Function<Resource<U>, V> objectCreator) {
        V obj = (V) objectsById.get(id);
        if (obj == null) {
            obj = resourceSupplier.get().map(objectCreator).orElse(null);
            if (obj != null) {
                objectsById.put(id, obj);
            }
        }
        return Optional.ofNullable(obj);
    }

    private <T extends Identifiable<T>, U extends IdentifiableAttributes> T create(Map<String, T> objectsById,
                                                                                   Resource<U> resource,
                                                                                   Function<Resource<U>, T> objectCreator) {
        if (objectsById.containsKey(resource.getId())) {
            throw new IllegalArgumentException("'" + resource.getId() + "' already exists");
        }
        T obj = objectCreator.apply(resource);
        objectsById.put(resource.getId(), obj);
        return obj;
    }

    // substation

    Optional<SubstationImpl> getSubstation(String id) {
        return getOne(id, substationById,
            () -> storeClient.getSubstation(network.getUuid(), id),
            resource -> SubstationImpl.create(this, resource));
    }

    List<Substation> getSubstations() {
        return getAll(substationById,
            () -> storeClient.getSubstations(network.getUuid()),
            resource -> SubstationImpl.create(this, resource));
    }

    int getSubstationCount() {
        return storeClient.getSubstationCount(network.getUuid());
    }

    Substation createSubstation(Resource<SubstationAttributes> resource) {
        return create(substationById, resource, r -> {
            storeClient.createSubstations(network.getUuid(), Collections.singletonList(r));
            return SubstationImpl.create(NetworkObjectIndex.this, r);
        });
    }

    // voltage level

    Optional<VoltageLevelImpl> getVoltageLevel(String id) {
        return getOne(id, voltageLevelById,
            () -> storeClient.getVoltageLevel(network.getUuid(), id),
            resource -> VoltageLevelImpl.create(this, resource));
    }

    List<VoltageLevel> getVoltageLevels() {
        return getAll(voltageLevelById,
            () -> storeClient.getVoltageLevels(network.getUuid()),
            resource -> VoltageLevelImpl.create(this, resource));
    }

    int getVoltageLevelCount() {
        return storeClient.getVoltageLevelCount(network.getUuid());
    }

    List<VoltageLevel> getVoltageLevels(String substationId) {
        return getSome(voltageLevelById,
            () -> storeClient.getVoltageLevelsInSubstation(network.getUuid(), substationId),
            resource -> VoltageLevelImpl.create(this, resource));
    }

    VoltageLevel createVoltageLevel(Resource<VoltageLevelAttributes> resource) {
        return create(voltageLevelById, resource, r -> {
            storeClient.createVoltageLevels(network.getUuid(), Collections.singletonList(r));
            return VoltageLevelImpl.create(this, r);
        });
    }

    // generator

    Optional<GeneratorImpl> getGenerator(String id) {
        return getOne(id, generatorById,
            () -> storeClient.getGenerator(network.getUuid(), id),
            resource -> GeneratorImpl.create(this, resource));
    }

    List<Generator> getGenerators() {
        return getAll(generatorById,
            () -> storeClient.getGenerators(network.getUuid()),
            resource -> GeneratorImpl.create(this, resource));
    }

    int getGeneratorCount() {
        return storeClient.getGeneratorCount(network.getUuid());
    }

    List<Generator> getGenerators(String voltageLevelId) {
        return getSome(generatorById,
            () -> storeClient.getVoltageLevelGenerators(network.getUuid(), voltageLevelId),
            resource -> GeneratorImpl.create(this, resource));
    }

    Generator createGenerator(Resource<GeneratorAttributes> resource) {
        return create(generatorById, resource, r -> {
            storeClient.createGenerators(network.getUuid(), Collections.singletonList(r));
            return GeneratorImpl.create(this, r);
        });
    }

    // load

    Optional<LoadImpl> getLoad(String id) {
        return getOne(id, loadById,
            () -> storeClient.getLoad(network.getUuid(), id),
            resource -> LoadImpl.create(this, resource));
    }

    List<Load> getLoads() {
        return getAll(loadById,
            () -> storeClient.getLoads(network.getUuid()),
            resource -> LoadImpl.create(this, resource));
    }

    int getLoadCount() {
        return storeClient.getLoadCount(network.getUuid());
    }

    List<Load> getLoads(String voltageLevelId) {
        return getSome(loadById,
            () -> storeClient.getVoltageLevelLoads(network.getUuid(), voltageLevelId),
            resource -> LoadImpl.create(this, resource));
    }

    Load createLoad(Resource<LoadAttributes> resource) {
        return create(loadById, resource, r -> {
            storeClient.createLoads(network.getUuid(), Collections.singletonList(r));
            return LoadImpl.create(this, r);
        });
    }

    // busbar section

    Optional<BusbarSectionImpl> getBusbarSection(String id) {
        return getOne(id, busbarSectionById,
            () -> storeClient.getBusbarSection(network.getUuid(), id),
            resource -> BusbarSectionImpl.create(this, resource));
    }

    List<BusbarSection> getBusbarSections() {
        return getAll(busbarSectionById,
            () -> storeClient.getBusbarSections(network.getUuid()),
            resource -> BusbarSectionImpl.create(this, resource));
    }

    int getBusbarSectionCount() {
        return storeClient.getBusbarSectionCount(network.getUuid());
    }

    List<BusbarSection> getBusbarSections(String voltageLevelId) {
        return getSome(busbarSectionById,
            () -> storeClient.getVoltageLevelBusbarSections(network.getUuid(), voltageLevelId),
            resource -> BusbarSectionImpl.create(this, resource));
    }

    BusbarSection createBusbarSection(Resource<BusbarSectionAttributes> resource) {
        return create(busbarSectionById, resource, r -> {
            storeClient.createBusbarSections(network.getUuid(), Collections.singletonList(r));
            return BusbarSectionImpl.create(this, r);
        });
    }

    // switch

    Optional<SwitchImpl> getSwitch(String id) {
        return getOne(id, switchById,
            () -> storeClient.getSwitch(network.getUuid(), id),
            resource -> SwitchImpl.create(this, resource));
    }

    List<Switch> getSwitches() {
        return getAll(switchById,
            () -> storeClient.getSwitches(network.getUuid()),
            resource -> SwitchImpl.create(this, resource));
    }

    int getSwitchCount() {
        return storeClient.getSwitchCount(network.getUuid());
    }

    List<Switch> getSwitches(String voltageLevelId) {
        return getSome(switchById,
            () -> storeClient.getVoltageLevelSwitches(network.getUuid(), voltageLevelId),
            resource -> SwitchImpl.create(this, resource));
    }

    Switch createSwitch(Resource<SwitchAttributes> resource) {
        return create(switchById, resource, r -> {
            storeClient.createSwitches(network.getUuid(), Collections.singletonList(r));
            return SwitchImpl.create(this, r);
        });
    }

    // 2 windings transformer

    Optional<TwoWindingsTransformerImpl> getTwoWindingsTransformer(String id) {
        return getOne(id, twoWindingsTransformerById,
            () -> storeClient.getTwoWindingsTransformer(network.getUuid(), id),
            resource -> TwoWindingsTransformerImpl.create(this, resource));
    }

    List<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getAll(twoWindingsTransformerById,
            () -> storeClient.getTwoWindingsTransformers(network.getUuid()),
            resource -> TwoWindingsTransformerImpl.create(this, resource));
    }

    int getTwoWindingsTransformerCount() {
        return storeClient.getTwoWindingsTransformerCount(network.getUuid());
    }

    List<TwoWindingsTransformer> getTwoWindingsTransformers(String voltageLevelId) {
        return getSome(twoWindingsTransformerById,
            () -> storeClient.getVoltageLevelTwoWindingsTransformers(network.getUuid(), voltageLevelId),
            resource -> TwoWindingsTransformerImpl.create(this, resource));
    }

    TwoWindingsTransformer createTwoWindingsTransformer(Resource<TwoWindingsTransformerAttributes> resource) {
        return create(twoWindingsTransformerById, resource, r -> {
            storeClient.createTwoWindingsTransformers(network.getUuid(), Collections.singletonList(r));
            return TwoWindingsTransformerImpl.create(this, r);
        });
    }

    // 3 windings transformer

    Optional<ThreeWindingsTransformerImpl> getThreeWindingsTransformer(String id) {
        return getOne(id, threeWindingsTransformerById,
            () -> storeClient.getThreeWindingsTransformer(network.getUuid(), id),
            resource -> ThreeWindingsTransformerImpl.create(this, resource));
    }

    List<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getAll(threeWindingsTransformerById,
            () -> storeClient.getThreeWindingsTransformers(network.getUuid()),
            resource -> ThreeWindingsTransformerImpl.create(this, resource));
    }

    int getThreeWindingsTransformerCount() {
        return storeClient.getThreeWindingsTransformerCount(network.getUuid());
    }

    List<ThreeWindingsTransformer> getThreeWindingsTransformers(String voltageLevelId) {
        return getSome(threeWindingsTransformerById,
            () -> storeClient.getVoltageLevelThreeWindingsTransformers(network.getUuid(), voltageLevelId),
            resource -> ThreeWindingsTransformerImpl.create(this, resource));
    }

    ThreeWindingsTransformer createThreeWindingsTransformer(Resource<ThreeWindingsTransformerAttributes> resource) {
        return create(threeWindingsTransformerById, resource, r -> {
            storeClient.createThreeWindingsTransformers(network.getUuid(), Collections.singletonList(r));
            return ThreeWindingsTransformerImpl.create(this, r);
        });
    }

    // line

    Optional<LineImpl> getLine(String id) {
        return getOne(id, lineById,
            () -> storeClient.getLine(network.getUuid(), id),
            resource -> LineImpl.create(this, resource));
    }

    List<Line> getLines() {
        return getAll(lineById,
            () -> storeClient.getLines(network.getUuid()),
            resource -> LineImpl.create(this, resource));
    }

    int getLineCount() {
        return storeClient.getLineCount(network.getUuid());
    }

    List<Line> getLines(String voltageLevelId) {
        return getSome(lineById,
            () -> storeClient.getVoltageLevelLines(network.getUuid(), voltageLevelId),
            resource -> LineImpl.create(this, resource));
    }

    Line createLine(Resource<LineAttributes> resource) {
        return create(lineById, resource, r -> {
            storeClient.createLines(network.getUuid(), Collections.singletonList(r));
            return LineImpl.create(this, r);
        });
    }

    // shunt compensator

    Optional<ShuntCompensatorImpl> getShuntCompensator(String id) {
        return getOne(id, shuntCompensatorById,
            () -> storeClient.getShuntCompensator(network.getUuid(), id),
            resource -> ShuntCompensatorImpl.create(this, resource));
    }

    List<ShuntCompensator> getShuntCompensators() {
        return getAll(shuntCompensatorById,
            () -> storeClient.getShuntCompensators(network.getUuid()),
            resource -> ShuntCompensatorImpl.create(this, resource));
    }

    int getShuntCompensatorCount() {
        return storeClient.getShuntCompensatorCount(network.getUuid());
    }

    List<ShuntCompensator> getShuntCompensators(String voltageLevelId) {
        return getSome(shuntCompensatorById,
            () -> storeClient.getVoltageLevelShuntCompensators(network.getUuid(), voltageLevelId),
            resource -> ShuntCompensatorImpl.create(this, resource));
    }

    ShuntCompensator createShuntCompensator(Resource<ShuntCompensatorAttributes> resource) {
        return create(shuntCompensatorById, resource, r -> {
            storeClient.createShuntCompensators(network.getUuid(), Collections.singletonList(r));
            return ShuntCompensatorImpl.create(this, r);
        });
    }

    // VSC converter station

    Optional<VscConverterStationImpl> getVscConverterStation(String id) {
        return getOne(id, vscConverterStationById,
            () -> storeClient.getVscConverterStation(network.getUuid(), id),
            resource -> VscConverterStationImpl.create(this, resource));
    }

    List<VscConverterStation> getVscConverterStations() {
        return getAll(vscConverterStationById,
            () -> storeClient.getVscConverterStations(network.getUuid()),
            resource -> VscConverterStationImpl.create(this, resource));
    }

    int getVscConverterStationCount() {
        return storeClient.getVscConverterStationCount(network.getUuid());
    }

    List<VscConverterStation> getVscConverterStations(String voltageLevelId) {
        return getSome(vscConverterStationById,
            () -> storeClient.getVoltageLevelVscConverterStation(network.getUuid(), voltageLevelId),
            resource -> VscConverterStationImpl.create(this, resource));
    }

    public VscConverterStation createVscConverterStation(Resource<VscConverterStationAttributes> resource) {
        return create(vscConverterStationById, resource, r -> {
            storeClient.createVscConverterStations(network.getUuid(), Collections.singletonList(r));
            return VscConverterStationImpl.create(this, r);
        });
    }

    // LCC converter station

    Optional<LccConverterStationImpl> getLccConverterStation(String id) {
        return getOne(id, lccConverterStationById,
            () -> storeClient.getLccConverterStation(network.getUuid(), id),
            resource -> LccConverterStationImpl.create(this, resource));
    }

    List<LccConverterStation> getLccConverterStations() {
        return getAll(lccConverterStationById,
            () -> storeClient.getLccConverterStations(network.getUuid()),
            resource -> LccConverterStationImpl.create(this, resource));
    }

    int getLccConverterStationCount() {
        return storeClient.getLccConverterStationCount(network.getUuid());
    }

    List<LccConverterStation> getLccConverterStations(String voltageLevelId) {
        return getSome(lccConverterStationById,
            () -> storeClient.getVoltageLevelLccConverterStation(network.getUuid(), voltageLevelId),
            resource -> LccConverterStationImpl.create(this, resource));
    }

    public LccConverterStation createLccConverterStation(Resource<LccConverterStationAttributes> resource) {
        return create(lccConverterStationById, resource, r -> {
            storeClient.createLccConverterStations(network.getUuid(), Collections.singletonList(r));
            return LccConverterStationImpl.create(this, r);
        });
    }

    // SVC

    Optional<StaticVarCompensatorImpl> getStaticVarCompensator(String id) {
        return getOne(id, staticVarCompensatorById,
            () -> storeClient.getStaticVarCompensator(network.getUuid(), id),
            resource -> StaticVarCompensatorImpl.create(this, resource));
    }

    List<StaticVarCompensator> getStaticVarCompensators() {
        return getAll(staticVarCompensatorById,
            () -> storeClient.getStaticVarCompensators(network.getUuid()),
            resource -> StaticVarCompensatorImpl.create(this, resource));
    }

    int getStaticVarCompensatorCount() {
        return storeClient.getStaticVarCompensatorCount(network.getUuid());
    }

    List<StaticVarCompensator> getStaticVarCompensators(String voltageLevelId) {
        return getSome(staticVarCompensatorById,
            () -> storeClient.getVoltageLevelStaticVarCompensators(network.getUuid(), voltageLevelId),
            resource -> StaticVarCompensatorImpl.create(this, resource));
    }

    public StaticVarCompensator createStaticVarCompensator(Resource<StaticVarCompensatorAttributes> resource) {
        return create(staticVarCompensatorById, resource, r -> {
            storeClient.createStaticVarCompensators(network.getUuid(), Collections.singletonList(r));
            return StaticVarCompensatorImpl.create(this, r);
        });
    }

    // HVDC line

    Optional<HvdcLineImpl> getHvdcLine(String id) {
        return getOne(id, hvdcLineById,
            () -> storeClient.getHvdcLine(network.getUuid(), id),
            resource -> HvdcLineImpl.create(this, resource));
    }

    List<HvdcLine> getHvdcLines() {
        return getAll(hvdcLineById,
            () -> storeClient.getHvdcLines(network.getUuid()),
            resource -> HvdcLineImpl.create(this, resource));
    }

    int getHvdcLineCount() {
        return storeClient.getHvdcLineCount(network.getUuid());
    }

    public HvdcLine createHvdcLine(Resource<HvdcLineAttributes> resource) {
        return create(hvdcLineById, resource, r -> {
            storeClient.createHvdcLines(network.getUuid(), Collections.singletonList(r));
            return HvdcLineImpl.create(this, r);
        });
    }

    // Dangling line

    Optional<DanglingLineImpl> getDanglingLine(String id) {
        return getOne(id, danglingLineById,
            () -> storeClient.getDanglingLine(network.getUuid(), id),
            resource -> DanglingLineImpl.create(this, resource));
    }

    List<DanglingLine> getDanglingLines() {
        return getAll(danglingLineById,
            () -> storeClient.getDanglingLines(network.getUuid()),
            resource -> DanglingLineImpl.create(this, resource));
    }

    List<DanglingLine> getDanglingLines(String voltageLevelId) {
        return getSome(danglingLineById,
            () -> storeClient.getVoltageLevelDanglingLines(network.getUuid(), voltageLevelId),
            resource -> DanglingLineImpl.create(this, resource));
    }

    int getDanglingLineCount() {
        return storeClient.getDanglingLineCount(network.getUuid());
    }

    public DanglingLine createDanglingLine(Resource<DanglingLineAttributes> resource) {
        return create(danglingLineById, resource, r -> {
            storeClient.createDanglingLines(network.getUuid(), Collections.singletonList(r));
            return DanglingLineImpl.create(this, r);
        });
    }

    public Identifiable<?> getIdentifiable(String id) {
        for (Map<String, ? extends Identifiable> map : Arrays.asList(substationById,
                                                                     voltageLevelById,
                                                                     generatorById,
                                                                     shuntCompensatorById,
                                                                     vscConverterStationById,
                                                                     staticVarCompensatorById,
                                                                     loadById,
                                                                     busbarSectionById,
                                                                     switchById,
                                                                     twoWindingsTransformerById,
                                                                     threeWindingsTransformerById,
                                                                     lineById,
                                                                     hvdcLineById,
                                                                     danglingLineById)) {
            Identifiable identifiable = map.get(id);
            if (identifiable != null) {
                return identifiable;
            }
        }
        return null;
    }

    public void removeDanglingLine(String danglingLineId) {
        storeClient.removeDanglingLine(network.getUuid(), danglingLineId);
        danglingLineById.remove(danglingLineId);
    }

    //buses

    Optional<Bus> getBus(String id) {
        return getOne(id, busesById,
            () -> storeClient.getConfiguredBus(network.getUuid(), id),
            resource -> ConfiguredBusImpl.create(this, resource));
    }

    List<Bus> getBuses() {
        return getAll(busesById,
            () -> storeClient.getConfiguredBuses(network.getUuid()),
            resource -> ConfiguredBusImpl.create(this, resource));
    }

    List<Bus> getBuses(String voltageLevelId) {
        return getSome(busesById,
            () -> storeClient.getVoltageLevelConfiguredBuses(network.getUuid(), voltageLevelId),
            resource -> ConfiguredBusImpl.create(this, resource));
    }

    ConfiguredBusImpl createBus(Resource<ConfiguredBusAttributes> resource) {
        return (ConfiguredBusImpl) create(busesById, resource, r -> {
            storeClient.createConfiguredBuses(network.getUuid(), Collections.singletonList(r));
            return ConfiguredBusImpl.create(this, r);
        });
    }
}
