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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkObjectIndex {

    private final NetworkStoreClient storeClient;

    private NetworkImpl network;

    // global equipment index by id
    // whatever the query we store the object here to guaranty a single instance of identifiable per network
    private final Map<String, Substation> substationById = new HashMap<>();

    private final Map<String, VoltageLevel> voltageLevelById = new HashMap<>();

    private final Map<String, Generator> generatorById = new HashMap<>();

    private final Map<String, ShuntCompensator> shuntCompensatorById = new HashMap<>();

    private final Map<String, VscConverterStation> vscConverterStationById = new HashMap<>();

    private final Map<String, StaticVarCompensator> staticVarCompensatorById = new HashMap<>();

    private final Map<String, Load> loadById = new HashMap<>();

    private final Map<String, BusbarSection> busbarSectionById = new HashMap<>();

    private final Map<String, Switch> switchById = new HashMap<>();

    private final Map<String, TwoWindingsTransformer> twoWindingsTransformerById = new HashMap<>();

    private final Map<String, Line> lineById = new HashMap<>();

    private final Map<String, HvdcLine> hvdcLineById = new HashMap<>();

    // network level index
    // we store here data only coming from a full query (all objects for a give, type)
    private List<Substation> substations;

    private List<VoltageLevel> voltageLevels;

    private List<Generator> generators;

    private List<ShuntCompensator> shuntCompensators;

    private List<VscConverterStation> vscConverterStations;

    private List<StaticVarCompensator> staticVarCompensators;

    private List<Load> loads;

    private List<BusbarSection> busbarSections;

    private List<Switch> switches;

    private List<TwoWindingsTransformer> twoWindingsTransformers;

    private List<Line> lines;

    private List<HvdcLine> hvdcLines;

    // substation/voltage level relationship
    // we store here data from a single substation query but also from a full voltage level query
    private final Map<String, List<VoltageLevel>> voltageLevelsBySubstation = new HashMap<>();

    // voltage level/equipments relationship
    // we store here data from a voltage level view query but also from a global equipment query
    private final Map<String, List<Generator>> generatorsByVoltageLevel = new HashMap<>();

    private final Map<String, List<Load>> loadsByVoltageLevel = new HashMap<>();

    private final Map<String, List<ShuntCompensator>> shuntCompensatorsByVoltageLevel = new HashMap<>();

    private final Map<String, List<VscConverterStation>> vscConverterStationsByVoltageLevel = new HashMap<>();

    private final Map<String, List<StaticVarCompensator>> staticVarCompensatorsByVoltageLevel = new HashMap<>();

    private final Map<String, List<BusbarSection>> busbarSectionsByVoltageLevel = new HashMap<>();

    private final Map<String, List<Switch>> switchesByVoltageLevel = new HashMap<>();

    private final Map<String, List<TwoWindingsTransformer>> twoWindingsTransformersByVoltageLevel = new HashMap<>();

    private final Map<String, List<Line>> linesByVoltageLevel = new HashMap<>();

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

    @SuppressWarnings("unchecked")
    private <T extends Identifiable<T>, U extends IdentifiableAttributes, V extends T> Optional<V> getOne(String id,
                                                                                                          Map<String, T> objectsById,
                                                                                                          Supplier<Optional<Resource<U>>> resourceSupplier,
                                                                                                          Function<Resource<U>, V> objectCreator) {
        V obj = (V) objectsById.get(id);
        if (obj == null) {
            return resourceSupplier.get().map(objectCreator);
        }
        return Optional.of(obj);
    }

    private <T extends Identifiable<T>, U extends IdentifiableAttributes> List<T> getAll(List<T> objects,
                                                                                         Map<String, T> objectsById,
                                                                                         Map<String, List<T>> objectsByVoltageLevel,
                                                                                         Supplier<List<Resource<U>>> resourcesSupplier,
                                                                                         Function<Resource<U>, T> objectCreator,
                                                                                         BiConsumer<Resource<U>, T> resourcePostProcessor) {
        if (objects != null) {
            return objects;
        }
        List<T> list2 = new ArrayList<>();
        List<Resource<U>> resources = resourcesSupplier.get();
        for (Resource<U> resource : resources) {
            T obj = objectsById.get(resource.getId());
            if (obj == null) {
                obj = objectCreator.apply(resource);

                // index by id
                objectsById.put(obj.getId(), obj);

                // index by voltage level
                if (objectsByVoltageLevel != null) {
                    if (resource.getAttributes() instanceof ConnectableAttributes) {
                        ConnectableAttributes connectableAttributes = (ConnectableAttributes) resource.getAttributes();
                        objectsByVoltageLevel.computeIfAbsent(connectableAttributes.getVoltageLevelId(), k -> new ArrayList<>())
                                .add(obj);
                    }
                }
            }
            list2.add(obj);
            if (resourcePostProcessor != null) {
                resourcePostProcessor.accept(resource, obj);
            }
        }
        return list2;
    }

    private <T extends Identifiable<T>, U extends IdentifiableAttributes> List<T> getAllConnectedToVoltageLevel(String voltageLevelId,
                                                                                                                Map<String, T> objectsById,
                                                                                                                Map<String, List<T>> objectsByVoltageLevel,
                                                                                                                Supplier<List<Resource<U>>> resourcesSupplier,
                                                                                                                Function<Resource<U>, T> objectCreator) {
        List<T> list = objectsByVoltageLevel.get(voltageLevelId);
        if (list != null) {
            return list;
        }
        list = new ArrayList<>();
        objectsByVoltageLevel.put(voltageLevelId, list);
        List<Resource<U>> resources = resourcesSupplier.get();
        for (Resource<U> resource : resources) {
            T one = objectsById.get(resource.getId());
            if (one == null) {
                one = objectCreator.apply(resource);
                objectsById.put(one.getId(), one);
            }
            list.add(one);
        }
        return list;
    }

    private <T extends Identifiable<T>> int getCount(List<T> objects, IntSupplier countSupplier) {
        if (objects == null) {
            return countSupplier.getAsInt();
        }
        return objects.size();
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
            () -> storeClient.getSubstation(network.getId(), id),
            resource -> SubstationImpl.create(this, resource));
    }

    List<Substation> getSubstations() {
        substations = getAll(substations, substationById, null,
            () -> storeClient.getSubstations(network.getId()),
            resource -> SubstationImpl.create(this, resource),
            null);
        return substations;
    }

    int getSubstationCount() {
        return getCount(substations, () -> storeClient.getSubstationCount(network.getId()));
    }

    Substation createSubstation(Resource<SubstationAttributes> resource) {
        return create(substationById, resource, r -> {
            storeClient.createSubstations(network.getId(), Collections.singletonList(r));
            return SubstationImpl.create(NetworkObjectIndex.this, r);
        });
    }

    // voltage level

    Optional<VoltageLevelImpl> getVoltageLevel(String id) {
        return getOne(id, voltageLevelById,
            () -> storeClient.getVoltageLevel(network.getId(), id),
            resource -> VoltageLevelImpl.create(this, resource));
    }

    List<VoltageLevel> getVoltageLevels() {
        voltageLevels = getAll(voltageLevels, voltageLevelById, null,
            () -> storeClient.getVoltageLevels(network.getId()),
            resource -> VoltageLevelImpl.create(this, resource),
            (resource, voltageLevel) -> voltageLevelsBySubstation.computeIfAbsent(resource.getAttributes().getSubstationId(), k -> new ArrayList<>())
                                                                 .add(voltageLevel));
        return voltageLevels;
    }

    int getVoltageLevelCount() {
        return getCount(voltageLevels, () -> storeClient.getVoltageLevelCount(network.getId()));
    }

    List<VoltageLevel> getVoltageLevels(String substationId) {
        return voltageLevelsBySubstation.computeIfAbsent(substationId, id -> {
            List<VoltageLevel> list = new ArrayList<>();
            voltageLevelsBySubstation.put(id, list);
            for (Resource<VoltageLevelAttributes> resource : storeClient.getVoltageLevelsInSubstation(network.getId(), id)) {
                VoltageLevel vl = voltageLevelById.get(resource.getId());
                if (vl == null) {
                    vl = VoltageLevelImpl.create(this, resource);
                    voltageLevelById.put(id, vl);
                }
                list.add(vl);
            }
            return list;
        });
    }

    VoltageLevel createVoltageLevel(Resource<VoltageLevelAttributes> resource) {
        return create(voltageLevelById, resource, r -> {
            storeClient.createVoltageLevels(network.getId(), Collections.singletonList(r));
            return VoltageLevelImpl.create(this, r);
        });
    }

    // generator

    Optional<GeneratorImpl> getGenerator(String id) {
        return getOne(id, generatorById,
            () -> storeClient.getGenerator(network.getId(), id),
            resource -> GeneratorImpl.create(this, resource));
    }

    List<Generator> getGenerators() {
        generators = getAll(generators, generatorById, generatorsByVoltageLevel,
            () -> storeClient.getGenerators(network.getId()),
            resource -> GeneratorImpl.create(this, resource),
            null);
        return generators;
    }

    int getGeneratorCount() {
        return getCount(generators, () -> storeClient.getGeneratorCount(network.getId()));
    }

    List<Generator> getGenerators(String voltageLevelId) {
        return getAllConnectedToVoltageLevel(voltageLevelId, generatorById, generatorsByVoltageLevel,
            () -> storeClient.getVoltageLevelGenerators(network.getId(), voltageLevelId),
            resource -> GeneratorImpl.create(this, resource));
    }

    Generator createGenerator(Resource<GeneratorAttributes> resource) {
        return create(generatorById, resource, r -> {
            storeClient.createGenerators(network.getId(), Collections.singletonList(r));
            return GeneratorImpl.create(this, r);
        });
    }

    // load

    Optional<LoadImpl> getLoad(String id) {
        return getOne(id, loadById,
            () -> storeClient.getLoad(network.getId(), id),
            resource -> LoadImpl.create(this, resource));
    }

    List<Load> getLoads() {
        loads = getAll(loads, loadById, loadsByVoltageLevel,
            () -> storeClient.getLoads(network.getId()),
            resource -> LoadImpl.create(this, resource),
            null);
        return loads;
    }

    int getLoadCount() {
        return getCount(loads, () -> storeClient.getLoadCount(network.getId()));
    }

    List<Load> getLoads(String voltageLevelId) {
        return getAllConnectedToVoltageLevel(voltageLevelId, loadById, loadsByVoltageLevel,
            () -> storeClient.getVoltageLevelLoads(network.getId(), voltageLevelId),
            resource -> LoadImpl.create(this, resource));
    }

    Load createLoad(Resource<LoadAttributes> resource) {
        return create(loadById, resource, r -> {
            storeClient.createLoads(network.getId(), Collections.singletonList(r));
            return LoadImpl.create(this, r);
        });
    }

    // busbar section

    Optional<BusbarSectionImpl> getBusbarSection(String id) {
        return getOne(id, busbarSectionById,
            () -> storeClient.getBusbarSection(network.getId(), id),
            resource -> BusbarSectionImpl.create(this, resource));
    }

    List<BusbarSection> getBusbarSections() {
        busbarSections = getAll(busbarSections, busbarSectionById, busbarSectionsByVoltageLevel,
            () -> storeClient.getBusbarSections(network.getId()),
            resource -> BusbarSectionImpl.create(this, resource),
            null);
        return busbarSections;
    }

    int getBusbarSectionCount() {
        return getCount(busbarSections, () -> storeClient.getBusbarSectionCount(network.getId()));
    }

    List<BusbarSection> getBusbarSections(String voltageLevelId) {
        return getAllConnectedToVoltageLevel(voltageLevelId, busbarSectionById, busbarSectionsByVoltageLevel,
            () -> storeClient.getVoltageLevelBusbarSections(network.getId(), voltageLevelId),
            resource -> BusbarSectionImpl.create(this, resource));
    }

    BusbarSection createBusbarSection(Resource<BusbarSectionAttributes> resource) {
        return create(busbarSectionById, resource, r -> {
            storeClient.createBusbarSections(network.getId(), Collections.singletonList(r));
            return BusbarSectionImpl.create(this, r);
        });
    }

    // switch

    Optional<SwitchImpl> getSwitch(String id) {
        return getOne(id, switchById,
            () -> storeClient.getSwitch(network.getId(), id),
            resource -> SwitchImpl.create(this, resource));
    }

    List<Switch> getSwitches() {
        switches = getAll(switches, switchById, switchesByVoltageLevel,
            () -> storeClient.getSwitches(network.getId()),
            resource -> SwitchImpl.create(this, resource),
            null);
        return switches;
    }

    int getSwitchCount() {
        return getCount(switches, () -> storeClient.getSwitchCount(network.getId()));
    }

    List<Switch> getSwitches(String voltageLevelId) {
        return getAllConnectedToVoltageLevel(voltageLevelId, switchById, switchesByVoltageLevel,
            () -> storeClient.getVoltageLevelSwitches(network.getId(), voltageLevelId),
            resource -> SwitchImpl.create(this, resource));
    }

    Switch createSwitch(Resource<SwitchAttributes> resource) {
        return create(switchById, resource, r -> {
            storeClient.createSwitches(network.getId(), Collections.singletonList(r));
            return SwitchImpl.create(this, r);
        });
    }

    // transformer

    Optional<TwoWindingsTransformerImpl> getTwoWindingsTransformer(String id) {
        return getOne(id, twoWindingsTransformerById,
            () -> storeClient.getTwoWindingsTransformer(network.getId(), id),
            resource -> TwoWindingsTransformerImpl.create(this, resource));
    }

    List<TwoWindingsTransformer> getTwoWindingsTransformers() {
        twoWindingsTransformers = getAll(twoWindingsTransformers, twoWindingsTransformerById, twoWindingsTransformersByVoltageLevel,
            () -> storeClient.getTwoWindingsTransformers(network.getId()),
            resource -> TwoWindingsTransformerImpl.create(this, resource),
            null);
        return twoWindingsTransformers;
    }

    int getTwoWindingsTransformerCount() {
        return getCount(twoWindingsTransformers, () -> storeClient.getTwoWindingsTransformerCount(network.getId()));
    }

    List<TwoWindingsTransformer> getTwoWindingsTransformers(String voltageLevelId) {
        return getAllConnectedToVoltageLevel(voltageLevelId, twoWindingsTransformerById, twoWindingsTransformersByVoltageLevel,
            () -> storeClient.getVoltageLevelTwoWindingsTransformers(network.getId(), voltageLevelId),
            resource -> TwoWindingsTransformerImpl.create(this, resource));
    }

    TwoWindingsTransformer createTwoWindingsTransformer(Resource<TwoWindingsTransformerAttributes> resource) {
        return create(twoWindingsTransformerById, resource, r -> {
            storeClient.createTwoWindingsTransformers(network.getId(), Collections.singletonList(r));
            return TwoWindingsTransformerImpl.create(this, r);
        });
    }

    // line

    Optional<LineImpl> getLine(String id) {
        return getOne(id, lineById,
            () -> storeClient.getLine(network.getId(), id),
            resource -> LineImpl.create(this, resource));
    }

    List<Line> getLines() {
        lines = getAll(lines, lineById, linesByVoltageLevel,
            () -> storeClient.getLines(network.getId()),
            resource -> LineImpl.create(this, resource),
            null);
        return lines;
    }

    int getLineCount() {
        return getCount(lines, () -> storeClient.getLineCount(network.getId()));
    }

    List<Line> getLines(String voltageLevelId) {
        return getAllConnectedToVoltageLevel(voltageLevelId, lineById, linesByVoltageLevel,
            () -> storeClient.getVoltageLevelLines(network.getId(), voltageLevelId),
            resource -> LineImpl.create(this, resource));
    }

    Line createLine(Resource<LineAttributes> resource) {
        return create(lineById, resource, r -> {
            storeClient.createLines(network.getId(), Collections.singletonList(r));
            return LineImpl.create(this, r);
        });
    }

    // shunt compensator

    Optional<ShuntCompensatorImpl> getShuntCompensator(String id) {
        return getOne(id, shuntCompensatorById,
            () -> storeClient.getShuntCompensator(network.getId(), id),
            resource -> ShuntCompensatorImpl.create(this, resource));
    }

    List<ShuntCompensator> getShuntCompensators() {
        shuntCompensators = getAll(shuntCompensators, shuntCompensatorById, shuntCompensatorsByVoltageLevel,
            () -> storeClient.getShuntCompensators(network.getId()),
            resource -> ShuntCompensatorImpl.create(this, resource),
            null);
        return shuntCompensators;
    }

    int getShuntCompensatorCount() {
        return getCount(shuntCompensators, () -> storeClient.getShuntCompensatorCount(network.getId()));
    }

    List<ShuntCompensator> getShuntCompensators(String voltageLevelId) {
        return getAllConnectedToVoltageLevel(voltageLevelId, shuntCompensatorById, shuntCompensatorsByVoltageLevel,
            () -> storeClient.getVoltageLevelShuntCompensators(network.getId(), voltageLevelId),
            resource -> ShuntCompensatorImpl.create(this, resource));
    }

    ShuntCompensator createShuntCompensator(Resource<ShuntCompensatorAttributes> resource) {
        return create(shuntCompensatorById, resource, r -> {
            storeClient.createShuntCompensators(network.getId(), Collections.singletonList(r));
            return ShuntCompensatorImpl.create(this, r);
        });
    }

    // VSC converter station

    Optional<VscConverterStationImpl> getVscConverterStation(String id) {
        return getOne(id, vscConverterStationById,
            () -> storeClient.getVscConverterStation(network.getId(), id),
            resource -> VscConverterStationImpl.create(this, resource));
    }

    List<VscConverterStation> getVscConverterStations() {
        vscConverterStations = getAll(vscConverterStations, vscConverterStationById, vscConverterStationsByVoltageLevel,
            () -> storeClient.getVscConverterStations(network.getId()),
            resource -> VscConverterStationImpl.create(this, resource),
            null);
        return vscConverterStations;
    }

    int getVscConverterStationCount() {
        return getCount(vscConverterStations, () -> storeClient.getVscConverterStationCount(network.getId()));
    }

    List<VscConverterStation> getVscConverterStations(String voltageLevelId) {
        return getAllConnectedToVoltageLevel(voltageLevelId, vscConverterStationById, vscConverterStationsByVoltageLevel,
            () -> storeClient.getVoltageLevelVscConverterStation(network.getId(), voltageLevelId),
            resource -> VscConverterStationImpl.create(this, resource));
    }

    public VscConverterStation createVscConverterStation(Resource<VscConverterStationAttributes> resource) {
        return create(vscConverterStationById, resource, r -> {
            storeClient.createVscConverterStations(network.getId(), Collections.singletonList(r));
            return VscConverterStationImpl.create(this, r);
        });
    }

    // SVC

    public StaticVarCompensator createStaticVarCompensator(Resource<StaticVarCompensatorAttributes> resource) {
        return create(staticVarCompensatorById, resource, r -> {
            storeClient.createStaticVarCompensators(network.getId(), Collections.singletonList(r));
            return StaticVarCompensatorImpl.create(this, r);
        });
    }

    // HVDC line

    public HvdcLine createHvdcLine(Resource<HvdcLineAttributes> resource) {
        return create(hvdcLineById, resource, r -> {
            storeClient.createHvdcLines(network.getId(), Collections.singletonList(r));
            return HvdcLineImpl.create(this, r);
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
                                                                     lineById,
                                                                     hvdcLineById)) {
            Identifiable identifiable = map.get(id);
            if (identifiable != null) {
                return identifiable;
            }
        }
        return null;
    }
}
