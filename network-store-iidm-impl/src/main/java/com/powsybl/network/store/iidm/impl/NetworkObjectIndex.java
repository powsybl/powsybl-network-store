/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A network global object index that guaranty a single instance of identifiable per network.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkObjectIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkObjectIndex.class);

    private final NetworkStoreClient storeClient;

    private final ResourceUpdater resourceUpdater;

    private NetworkImpl network;

    private final Map<String, Substation> substationById = new HashMap<>();

    private final Map<String, VoltageLevel> voltageLevelById = new HashMap<>();

    private final Map<String, Generator> generatorById = new HashMap<>();

    private final Map<String, Battery> batteryById = new HashMap<>();

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
        resourceUpdater = new ResourceUpdaterImpl(storeClient);
    }

    public NetworkStoreClient getStoreClient() {
        return storeClient;
    }

    public ResourceUpdater getResourceUpdater() {
        return resourceUpdater;
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
        notifyCreation(obj);
        return obj;
    }

    void notifyCreation(Identifiable<?> identifiable) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onCreation(identifiable);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    void notifyRemoval(Identifiable<?> identifiable) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onRemoval(identifiable);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    void notifyUpdate(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            for (NetworkListener listener : network.getListeners()) {
                try {
                    listener.onUpdate(identifiable, attribute, oldValue, newValue);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }
    }

    void notifyUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            for (NetworkListener listener : network.getListeners()) {
                try {
                    listener.onUpdate(identifiable, attribute, variantId, oldValue, newValue);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }
    }

    void notifyElementAdded(Identifiable<?> identifiable, Supplier<String> attribute, Object newValue) {
        if (!network.getListeners().isEmpty()) {
            notifyElementAdded(identifiable, attribute.get(), newValue);
        }
    }

    void notifyElementAdded(Identifiable<?> identifiable, String attribute, Object newValue) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onElementAdded(identifiable, attribute, newValue);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    void notifyElementReplaced(Identifiable<?> identifiable, Supplier<String> attribute, Object oldValue, Object newValue) {
        if (!network.getListeners().isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyElementReplaced(identifiable, attribute.get(), oldValue, newValue);
        }
    }

    void notifyElementReplaced(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onElementReplaced(identifiable, attribute, oldValue, newValue);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
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

    public void removeGenerator(String generatorId) {
        storeClient.removeGenerator(network.getUuid(), generatorId);
        generatorById.remove(generatorId);
    }

    // battery

    Optional<BatteryImpl> getBattery(String id) {
        return getOne(id, batteryById,
            () -> storeClient.getBattery(network.getUuid(), id),
            resource -> BatteryImpl.create(this, resource));
    }

    List<Battery> getBatteries() {
        return getAll(batteryById,
            () -> storeClient.getBatteries(network.getUuid()),
            resource -> BatteryImpl.create(this, resource));
    }

    int getBatteryCount() {
        return storeClient.getBatteryCount(network.getUuid());
    }

    List<Battery> getBatteries(String voltageLevelId) {
        return getSome(batteryById,
            () -> storeClient.getVoltageLevelBatteries(network.getUuid(), voltageLevelId),
            resource -> BatteryImpl.create(this, resource));
    }

    Battery createBattery(Resource<BatteryAttributes> resource) {
        return create(batteryById, resource, r -> {
            storeClient.createBatteries(network.getUuid(), Collections.singletonList(r));
            return BatteryImpl.create(this, r);
        });
    }

    public void removeBattery(String batteryId) {
        storeClient.removeBattery(network.getUuid(), batteryId);
        batteryById.remove(batteryId);
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

    public void removeLoad(String loadId) {
        storeClient.removeLoad(network.getUuid(), loadId);
        loadById.remove(loadId);
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

    public void removeBusBarSection(String busBarSectionId) {
        storeClient.removeBusBarSection(network.getUuid(), busBarSectionId);
        busbarSectionById.remove(busBarSectionId);
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

    public void removeTwoWindingsTransformer(String twoWindingsTransformerId) {
        storeClient.removeTwoWindingsTransformer(network.getUuid(), twoWindingsTransformerId);
        twoWindingsTransformerById.remove(twoWindingsTransformerId);
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

    public void removeThreeWindingsTransformer(String threeWindingsTransformerId) {
        storeClient.removeThreeWindingsTransformer(network.getUuid(), threeWindingsTransformerId);
        threeWindingsTransformerById.remove(threeWindingsTransformerId);
    }

    // line

    private Line createLineOrTieLine(Resource<LineAttributes> resource) {
        return resource.getAttributes().getMergedXnode() != null ? new TieLineImpl(this, resource) : new LineImpl(this, resource);
    }

    Optional<Line> getLine(String id) {
        return getOne(id, lineById,
            () -> storeClient.getLine(network.getUuid(), id),
            this::createLineOrTieLine);
    }

    List<Line> getLines() {
        return getAll(lineById,
            () -> storeClient.getLines(network.getUuid()),
            this::createLineOrTieLine);
    }

    int getLineCount() {
        return storeClient.getLineCount(network.getUuid());
    }

    List<Line> getLines(String voltageLevelId) {
        return getSome(lineById,
            () -> storeClient.getVoltageLevelLines(network.getUuid(), voltageLevelId),
            this::createLineOrTieLine);
    }

    Line createLine(Resource<LineAttributes> resource) {
        return create(lineById, resource, r -> {
            storeClient.createLines(network.getUuid(), Collections.singletonList(r));
            return createLineOrTieLine(r);
        });
    }

    public void removeLine(String lineId) {
        storeClient.removeLine(network.getUuid(), lineId);
        lineById.remove(lineId);
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

    public void removeShuntCompensator(String shuntCompensatorId) {
        storeClient.removeShuntCompensator(network.getUuid(), shuntCompensatorId);
        shuntCompensatorById.remove(shuntCompensatorId);
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
            () -> storeClient.getVoltageLevelVscConverterStations(network.getUuid(), voltageLevelId),
            resource -> VscConverterStationImpl.create(this, resource));
    }

    public VscConverterStation createVscConverterStation(Resource<VscConverterStationAttributes> resource) {
        return create(vscConverterStationById, resource, r -> {
            storeClient.createVscConverterStations(network.getUuid(), Collections.singletonList(r));
            return VscConverterStationImpl.create(this, r);
        });
    }

    public void removeVscConverterStation(String vscConverterStationId) {
        storeClient.removeVscConverterStation(network.getUuid(), vscConverterStationId);
        vscConverterStationById.remove(vscConverterStationId);
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
            () -> storeClient.getVoltageLevelLccConverterStations(network.getUuid(), voltageLevelId),
            resource -> LccConverterStationImpl.create(this, resource));
    }

    public LccConverterStation createLccConverterStation(Resource<LccConverterStationAttributes> resource) {
        return create(lccConverterStationById, resource, r -> {
            storeClient.createLccConverterStations(network.getUuid(), Collections.singletonList(r));
            return LccConverterStationImpl.create(this, r);
        });
    }

    public Optional<HvdcConverterStation> getHvdcConverterStation(String id) {
        HvdcConverterStation<?> station = getVscConverterStation(id).orElse(null);
        if (station == null) {
            station = getLccConverterStation(id).orElse(null);
        }
        return Optional.ofNullable(station);
    }

    public void removeLccConverterStation(String lccConverterStationId) {
        storeClient.removeLccConverterStation(network.getUuid(), lccConverterStationId);
        lccConverterStationById.remove(lccConverterStationId);
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

    public void removeStaticVarCompensator(String staticVarCompensatorId) {
        storeClient.removeStaticVarCompensator(network.getUuid(), staticVarCompensatorId);
        staticVarCompensatorById.remove(staticVarCompensatorId);
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

    public void removeHvdcLine(String hvdcLineId) {
        storeClient.removeHvdcLine(network.getUuid(), hvdcLineId);
        hvdcLineById.remove(hvdcLineId);
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

    public Collection<Identifiable<?>> getIdentifiables() {
        return ImmutableList.<Identifiable<?>>builder()
                .addAll(getSubstations())
                .addAll(getVoltageLevels())
                .addAll(getGenerators())
                .addAll(getBatteries())
                .addAll(getShuntCompensators())
                .addAll(getVscConverterStations())
                .addAll(getStaticVarCompensators())
                .addAll(getLoads())
                .addAll(getBusbarSections())
                .addAll(getSwitches())
                .addAll(getTwoWindingsTransformers())
                .addAll(getThreeWindingsTransformers())
                .addAll(getLines())
                .addAll(getHvdcLines())
                .addAll(getDanglingLines())
                .build();
    }

    public Identifiable<?> getIdentifiable(String id) {
        Objects.requireNonNull(id);
        if (network.getId().equals(id)) {
            return network;
        }
        return getSubstation(id).map(s -> (Identifiable) s)
                .or(() -> getVoltageLevel(id))
                .or(() -> getGenerator(id))
                .or(() -> getBattery(id))
                .or(() -> getShuntCompensator(id))
                .or(() -> getVscConverterStation(id))
                .or(() -> getStaticVarCompensator(id))
                .or(() -> getLoad(id))
                .or(() -> getBusbarSection(id))
                .or(() -> getSwitch(id))
                .or(() -> getTwoWindingsTransformer(id))
                .or(() -> getThreeWindingsTransformer(id))
                .or(() -> getLine(id))
                .or(() -> getHvdcLine(id))
                .or(() -> getDanglingLine(id))
                .orElse(null);
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

    static void checkId(String id) {
        if (id == null || id.isEmpty()) {
            throw new PowsyblException("Invalid id '" + id + "'");
        }
    }

    boolean contains(String id) {
        checkId(id);
        return getIdentifiable(id) != null;
    }
}
