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
import java.util.function.Consumer;
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

    private NetworkImpl network;

    private int workingVariantNum = Resource.INITIAL_VARIANT_NUM;

    class ObjectCache<T extends Identifiable<T>, U extends IdentifiableAttributes> {

        private final Map<String, T> objectsById = new HashMap<>();

        private final Consumer<Resource<U>> resourceCreator;

        private final Function<String, Optional<Resource<U>>> oneResourceGetter;

        private final Function<String, List<Resource<U>>> someResourcesGetter;

        private final Supplier<List<Resource<U>>> allResourcesGetter;

        private final Consumer<String> resourceRemover;

        private final Function<Resource<U>, T> objectCreator;

        ObjectCache(Consumer<Resource<U>> resourceCreator, Function<String, Optional<Resource<U>>> oneResourceGetter,
                    Function<String, List<Resource<U>>> someResourcesGetter, Supplier<List<Resource<U>>> allResourcesGetter,
                    Consumer<String> resourceRemover, Function<Resource<U>, T> objectCreator) {
            this.resourceCreator = Objects.requireNonNull(resourceCreator);
            this.oneResourceGetter = Objects.requireNonNull(oneResourceGetter);
            this.someResourcesGetter = someResourcesGetter;
            this.allResourcesGetter = Objects.requireNonNull(allResourcesGetter);
            this.resourceRemover = Objects.requireNonNull(resourceRemover);
            this.objectCreator = Objects.requireNonNull(objectCreator);
        }

        Collection<T> getLoaded() {
            return objectsById.values();
        }

        List<T> getAll() {
            List<Resource<U>> resources = allResourcesGetter.get();
            if (resources.size() != objectsById.size()) {
                for (Resource<U> resource : resources) {
                    if (!objectsById.containsKey(resource.getId())) {
                        objectsById.put(resource.getId(), objectCreator.apply(resource));
                    }
                }
            }
            return new ArrayList<>(objectsById.values());
        }

        List<T> getSome(String containerId) {
            List<Resource<U>> resources = someResourcesGetter.apply(containerId);
            List<T> some = new ArrayList<>(resources.size());
            for (Resource<U> resource : resources) {
                T obj = objectsById.get(resource.getId());
                if (obj == null) {
                    obj = objectCreator.apply(resource);
                    objectsById.put(obj.getId(), obj);
                }
                some.add(obj);
            }
            return some;
        }

        @SuppressWarnings("unchecked")
        <V extends T> Optional<V> getOne(String id) {
            V obj = (V) objectsById.get(id);
            if (obj == null) {
                Optional<Resource<U>> resource = oneResourceGetter.apply(id);
                obj = (V) resource.map(objectCreator).orElse(null);
                if (obj != null) {
                    objectsById.put(id, obj);
                }
            }
            return Optional.ofNullable(obj);
        }

        T create(Resource<U> resource) {
            if (objectsById.containsKey(resource.getId())) {
                throw new IllegalArgumentException("'" + resource.getId() + "' already exists");
            }
            resourceCreator.accept(resource);
            T obj = objectCreator.apply(resource);
            objectsById.put(resource.getId(), obj);
            notifyCreation(obj);
            return obj;
        }

        void remove(String id) {
            resourceRemover.accept(id);
            T obj = objectsById.get(id);
            if (obj != null) {
                ((AbstractIdentifiableImpl) obj).setResource(null);
            }
        }
    }

    private final ObjectCache<Substation, SubstationAttributes> substationCache;

    private final ObjectCache<VoltageLevel, VoltageLevelAttributes> voltageLevelCache;

    private final ObjectCache<Generator, GeneratorAttributes> generatorCache;

    private final ObjectCache<Battery, BatteryAttributes> batteryCache;

    private final ObjectCache<ShuntCompensator, ShuntCompensatorAttributes> shuntCompensatorCache;

    private final ObjectCache<VscConverterStation, VscConverterStationAttributes> vscConverterStationCache;

    private final ObjectCache<LccConverterStation, LccConverterStationAttributes> lccConverterStationCache;

    private final ObjectCache<StaticVarCompensator, StaticVarCompensatorAttributes> staticVarCompensatorCache;

    private final ObjectCache<Load, LoadAttributes> loadCache;

    private final ObjectCache<BusbarSection, BusbarSectionAttributes> busbarSectionCache;

    private final ObjectCache<Switch, SwitchAttributes> switchCache;

    private final ObjectCache<TwoWindingsTransformer, TwoWindingsTransformerAttributes> twoWindingsTransformerCache;

    private final ObjectCache<ThreeWindingsTransformer, ThreeWindingsTransformerAttributes> threeWindingsTransformerCache;

    private final ObjectCache<Line, LineAttributes> lineCache;

    private final ObjectCache<HvdcLine, HvdcLineAttributes> hvdcLineCache;

    private final ObjectCache<DanglingLine, DanglingLineAttributes> danglingLineCache;

    private final ObjectCache<Bus, ConfiguredBusAttributes> busCache;

    public NetworkObjectIndex(NetworkStoreClient storeClient) {
        this.storeClient = Objects.requireNonNull(storeClient);
        substationCache = new ObjectCache<>(resource -> storeClient.createSubstations(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getSubstation(network.getUuid(), workingVariantNum, id),
            null,
            () -> storeClient.getSubstations(network.getUuid(), workingVariantNum),
            id -> storeClient.removeSubstations(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> SubstationImpl.create(NetworkObjectIndex.this, resource));
        voltageLevelCache = new ObjectCache<>(resource -> storeClient.createVoltageLevels(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getVoltageLevel(network.getUuid(), workingVariantNum, id),
            substationId -> storeClient.getVoltageLevelsInSubstation(network.getUuid(), workingVariantNum, substationId),
            () -> storeClient.getVoltageLevels(network.getUuid(), workingVariantNum),
            id -> storeClient.removeVoltageLevels(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> VoltageLevelImpl.create(NetworkObjectIndex.this, resource));
        generatorCache = new ObjectCache<>(resource -> storeClient.createGenerators(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getGenerator(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelGenerators(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getGenerators(network.getUuid(), workingVariantNum),
            id -> storeClient.removeGenerators(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> GeneratorImpl.create(NetworkObjectIndex.this, resource));
        batteryCache = new ObjectCache<>(resource -> storeClient.createBatteries(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getBattery(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelBatteries(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getBatteries(network.getUuid(), workingVariantNum),
            id -> storeClient.removeBatteries(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> BatteryImpl.create(NetworkObjectIndex.this, resource));
        shuntCompensatorCache = new ObjectCache<>(resource -> storeClient.createShuntCompensators(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getShuntCompensator(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelShuntCompensators(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getShuntCompensators(network.getUuid(), workingVariantNum),
            id -> storeClient.removeShuntCompensators(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> ShuntCompensatorImpl.create(NetworkObjectIndex.this, resource));
        vscConverterStationCache = new ObjectCache<>(resource -> storeClient.createVscConverterStations(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getVscConverterStation(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelVscConverterStations(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getVscConverterStations(network.getUuid(), workingVariantNum),
            id -> storeClient.removeVscConverterStations(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> VscConverterStationImpl.create(NetworkObjectIndex.this, resource));
        lccConverterStationCache = new ObjectCache<>(resource -> storeClient.createLccConverterStations(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getLccConverterStation(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelLccConverterStations(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getLccConverterStations(network.getUuid(), workingVariantNum),
            id -> storeClient.removeLccConverterStations(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> LccConverterStationImpl.create(NetworkObjectIndex.this, resource));
        staticVarCompensatorCache = new ObjectCache<>(resource -> storeClient.createStaticVarCompensators(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getStaticVarCompensator(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelStaticVarCompensators(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getStaticVarCompensators(network.getUuid(), workingVariantNum),
            id -> storeClient.removeStaticVarCompensators(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> StaticVarCompensatorImpl.create(NetworkObjectIndex.this, resource));
        loadCache = new ObjectCache<>(resource -> storeClient.createLoads(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getLoad(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelLoads(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getLoads(network.getUuid(), workingVariantNum),
            id -> storeClient.removeLoads(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> LoadImpl.create(NetworkObjectIndex.this, resource));
        busbarSectionCache = new ObjectCache<>(resource -> storeClient.createBusbarSections(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getBusbarSection(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelBusbarSections(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getBusbarSections(network.getUuid(), workingVariantNum),
            id -> storeClient.removeBusBarSections(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> BusbarSectionImpl.create(NetworkObjectIndex.this, resource));
        switchCache = new ObjectCache<>(resource -> storeClient.createSwitches(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getSwitch(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelSwitches(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getSwitches(network.getUuid(), workingVariantNum),
            id -> storeClient.removeSwitches(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> SwitchImpl.create(NetworkObjectIndex.this, resource));
        twoWindingsTransformerCache = new ObjectCache<>(resource -> storeClient.createTwoWindingsTransformers(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getTwoWindingsTransformer(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelTwoWindingsTransformers(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getTwoWindingsTransformers(network.getUuid(), workingVariantNum),
            id -> storeClient.removeTwoWindingsTransformers(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> TwoWindingsTransformerImpl.create(NetworkObjectIndex.this, resource));
        threeWindingsTransformerCache = new ObjectCache<>(resource -> storeClient.createThreeWindingsTransformers(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getThreeWindingsTransformer(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelThreeWindingsTransformers(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getThreeWindingsTransformers(network.getUuid(), workingVariantNum),
            id -> storeClient.removeThreeWindingsTransformers(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> ThreeWindingsTransformerImpl.create(NetworkObjectIndex.this, resource));
        lineCache = new ObjectCache<>(resource -> storeClient.createLines(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getLine(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelLines(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getLines(network.getUuid(), workingVariantNum),
            id -> storeClient.removeLines(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            this::createLineOrTieLine);
        hvdcLineCache = new ObjectCache<>(resource -> storeClient.createHvdcLines(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getHvdcLine(network.getUuid(), workingVariantNum, id),
            null,
            () -> storeClient.getHvdcLines(network.getUuid(), workingVariantNum),
            id -> storeClient.removeHvdcLines(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> HvdcLineImpl.create(NetworkObjectIndex.this, resource));
        danglingLineCache = new ObjectCache<>(resource -> storeClient.createDanglingLines(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getDanglingLine(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelDanglingLines(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getDanglingLines(network.getUuid(), workingVariantNum),
            id -> storeClient.removeDanglingLines(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> DanglingLineImpl.create(NetworkObjectIndex.this, resource));
        busCache = new ObjectCache<>(resource -> storeClient.createConfiguredBuses(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getConfiguredBus(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelConfiguredBuses(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getConfiguredBuses(network.getUuid(), workingVariantNum),
            id -> storeClient.removeConfiguredBuses(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> ConfiguredBusImpl.create(NetworkObjectIndex.this, resource));
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

    public int getWorkingVariantNum() {
        return workingVariantNum;
    }

    public void setWorkingVariantNum(int workingVariantNum) {
        this.workingVariantNum = workingVariantNum;
        // TODO save loading strategy to resource to ba able to load resources of working variant the same
        // way a previous variant (one, some or all)
        for (Generator generator : generatorCache.getLoaded()) {
            Resource<GeneratorAttributes> workingVariantResource = storeClient.getGenerator(network.getUuid(), workingVariantNum, generator.getId()).orElseThrow();
            ((GeneratorImpl) generator).setResource(workingVariantResource);
        }
        for (Load load : loadCache.getLoaded()) {
            Resource<LoadAttributes> workingVariantResource = storeClient.getLoad(network.getUuid(), workingVariantNum, load.getId()).orElseThrow();
            ((LoadImpl) load).setResource(workingVariantResource);
        }
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
        return substationCache.getOne(id);
    }

    List<Substation> getSubstations() {
        return substationCache.getAll();
    }

    Substation createSubstation(Resource<SubstationAttributes> resource) {
        return substationCache.create(resource);
    }

    public void removeSubstation(String substationId) {
        substationCache.remove(substationId);
    }

    // voltage level

    Optional<VoltageLevelImpl> getVoltageLevel(String id) {
        return voltageLevelCache.getOne(id);
    }

    List<VoltageLevel> getVoltageLevels() {
        return voltageLevelCache.getAll();
    }

    List<VoltageLevel> getVoltageLevels(String substationId) {
        return voltageLevelCache.getSome(substationId);
    }

    VoltageLevel createVoltageLevel(Resource<VoltageLevelAttributes> resource) {
        return voltageLevelCache.create(resource);
    }

    public void removeVoltageLevel(String voltageLevelId) {
        voltageLevelCache.remove(voltageLevelId);
    }

    // generator

    Optional<GeneratorImpl> getGenerator(String id) {
        return generatorCache.getOne(id);
    }

    List<Generator> getGenerators() {
        return generatorCache.getAll();
    }

    List<Generator> getGenerators(String voltageLevelId) {
        return generatorCache.getSome(voltageLevelId);
    }

    Generator createGenerator(Resource<GeneratorAttributes> resource) {
        return generatorCache.create(resource);
    }

    public void removeGenerator(String generatorId) {
        generatorCache.remove(generatorId);
    }

    // battery

    Optional<BatteryImpl> getBattery(String id) {
        return batteryCache.getOne(id);
    }

    List<Battery> getBatteries() {
        return batteryCache.getAll();
    }

    List<Battery> getBatteries(String voltageLevelId) {
        return batteryCache.getSome(voltageLevelId);
    }

    Battery createBattery(Resource<BatteryAttributes> resource) {
        return batteryCache.create(resource);
    }

    public void removeBattery(String batteryId) {
        batteryCache.remove(batteryId);
    }

    // load

    Optional<LoadImpl> getLoad(String id) {
        return loadCache.getOne(id);
    }

    List<Load> getLoads() {
        return loadCache.getAll();
    }

    List<Load> getLoads(String voltageLevelId) {
        return loadCache.getSome(voltageLevelId);
    }

    Load createLoad(Resource<LoadAttributes> resource) {
        return loadCache.create(resource);
    }

    public void removeLoad(String loadId) {
        loadCache.remove(loadId);
    }

    // busbar section

    Optional<BusbarSectionImpl> getBusbarSection(String id) {
        return busbarSectionCache.getOne(id);
    }

    List<BusbarSection> getBusbarSections() {
        return busbarSectionCache.getAll();
    }

    List<BusbarSection> getBusbarSections(String voltageLevelId) {
        return busbarSectionCache.getSome(voltageLevelId);
    }

    BusbarSection createBusbarSection(Resource<BusbarSectionAttributes> resource) {
        return busbarSectionCache.create(resource);
    }

    public void removeBusBarSection(String busBarSectionId) {
        busbarSectionCache.remove(busBarSectionId);
    }

    // switch

    Optional<SwitchImpl> getSwitch(String id) {
        return switchCache.getOne(id);
    }

    List<Switch> getSwitches() {
        return switchCache.getAll();
    }

    List<Switch> getSwitches(String voltageLevelId) {
        return switchCache.getSome(voltageLevelId);
    }

    Switch createSwitch(Resource<SwitchAttributes> resource) {
        return switchCache.create(resource);
    }

    public void removeSwitch(String switchId) {
        switchCache.remove(switchId);
    }

    // 2 windings transformer

    Optional<TwoWindingsTransformerImpl> getTwoWindingsTransformer(String id) {
        return twoWindingsTransformerCache.getOne(id);
    }

    List<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return twoWindingsTransformerCache.getAll();
    }

    List<TwoWindingsTransformer> getTwoWindingsTransformers(String voltageLevelId) {
        return twoWindingsTransformerCache.getSome(voltageLevelId);
    }

    TwoWindingsTransformer createTwoWindingsTransformer(Resource<TwoWindingsTransformerAttributes> resource) {
        return twoWindingsTransformerCache.create(resource);
    }

    public void removeTwoWindingsTransformer(String twoWindingsTransformerId) {
        twoWindingsTransformerCache.remove(twoWindingsTransformerId);
    }

    // 3 windings transformer

    Optional<ThreeWindingsTransformerImpl> getThreeWindingsTransformer(String id) {
        return threeWindingsTransformerCache.getOne(id);
    }

    List<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return threeWindingsTransformerCache.getAll();
    }

    List<ThreeWindingsTransformer> getThreeWindingsTransformers(String voltageLevelId) {
        return threeWindingsTransformerCache.getSome(voltageLevelId);
    }

    ThreeWindingsTransformer createThreeWindingsTransformer(Resource<ThreeWindingsTransformerAttributes> resource) {
        return threeWindingsTransformerCache.create(resource);
    }

    public void removeThreeWindingsTransformer(String threeWindingsTransformerId) {
        threeWindingsTransformerCache.remove(threeWindingsTransformerId);
    }

    // line

    private Line createLineOrTieLine(Resource<LineAttributes> resource) {
        return resource.getAttributes().getMergedXnode() != null ? new TieLineImpl(this, resource) : new LineImpl(this, resource);
    }

    Optional<Line> getLine(String id) {
        return lineCache.getOne(id);
    }

    List<Line> getLines() {
        return lineCache.getAll();
    }

    List<Line> getLines(String voltageLevelId) {
        return lineCache.getSome(voltageLevelId);
    }

    Line createLine(Resource<LineAttributes> resource) {
        return lineCache.create(resource);
    }

    public void removeLine(String lineId) {
        lineCache.remove(lineId);
    }


    // shunt compensator

    Optional<ShuntCompensatorImpl> getShuntCompensator(String id) {
        return shuntCompensatorCache.getOne(id);
    }

    List<ShuntCompensator> getShuntCompensators() {
        return shuntCompensatorCache.getAll();
    }

    List<ShuntCompensator> getShuntCompensators(String voltageLevelId) {
        return shuntCompensatorCache.getSome(voltageLevelId);
    }

    ShuntCompensator createShuntCompensator(Resource<ShuntCompensatorAttributes> resource) {
        return shuntCompensatorCache.create(resource);
    }

    public void removeShuntCompensator(String shuntCompensatorId) {
        shuntCompensatorCache.remove(shuntCompensatorId);
    }

    // VSC converter station

    Optional<VscConverterStationImpl> getVscConverterStation(String id) {
        return vscConverterStationCache.getOne(id);
    }

    List<VscConverterStation> getVscConverterStations() {
        return vscConverterStationCache.getAll();
    }

    List<VscConverterStation> getVscConverterStations(String voltageLevelId) {
        return vscConverterStationCache.getSome(voltageLevelId);
    }

    public VscConverterStation createVscConverterStation(Resource<VscConverterStationAttributes> resource) {
        return vscConverterStationCache.create(resource);
    }

    public void removeVscConverterStation(String vscConverterStationId) {
        vscConverterStationCache.remove(vscConverterStationId);
    }

    // LCC converter station

    Optional<LccConverterStationImpl> getLccConverterStation(String id) {
        return lccConverterStationCache.getOne(id);
    }

    List<LccConverterStation> getLccConverterStations() {
        return lccConverterStationCache.getAll();
    }

    List<LccConverterStation> getLccConverterStations(String voltageLevelId) {
        return lccConverterStationCache.getSome(voltageLevelId);
    }

    public LccConverterStation createLccConverterStation(Resource<LccConverterStationAttributes> resource) {
        return lccConverterStationCache.create(resource);
    }

    public Optional<HvdcConverterStation> getHvdcConverterStation(String id) {
        HvdcConverterStation<?> station = getVscConverterStation(id).orElse(null);
        if (station == null) {
            station = getLccConverterStation(id).orElse(null);
        }
        return Optional.ofNullable(station);
    }

    public void removeLccConverterStation(String lccConverterStationId) {
        lccConverterStationCache.remove(lccConverterStationId);
    }

    // SVC

    Optional<StaticVarCompensatorImpl> getStaticVarCompensator(String id) {
        return staticVarCompensatorCache.getOne(id);
    }

    List<StaticVarCompensator> getStaticVarCompensators() {
        return staticVarCompensatorCache.getAll();
    }

    List<StaticVarCompensator> getStaticVarCompensators(String voltageLevelId) {
        return staticVarCompensatorCache.getSome(voltageLevelId);
    }

    public StaticVarCompensator createStaticVarCompensator(Resource<StaticVarCompensatorAttributes> resource) {
        return staticVarCompensatorCache.create(resource);
    }

    public void removeStaticVarCompensator(String staticVarCompensatorId) {
        staticVarCompensatorCache.remove(staticVarCompensatorId);
    }

    // HVDC line

    Optional<HvdcLineImpl> getHvdcLine(String id) {
        return hvdcLineCache.getOne(id);
    }

    List<HvdcLine> getHvdcLines() {
        return hvdcLineCache.getAll();
    }

    public HvdcLine createHvdcLine(Resource<HvdcLineAttributes> resource) {
        return hvdcLineCache.create(resource);
    }

    public void removeHvdcLine(String hvdcLineId) {
        hvdcLineCache.remove(hvdcLineId);
    }

    // Dangling line

    Optional<DanglingLineImpl> getDanglingLine(String id) {
        return danglingLineCache.getOne(id);
    }

    List<DanglingLine> getDanglingLines() {
        return danglingLineCache.getAll();
    }

    List<DanglingLine> getDanglingLines(String voltageLevelId) {
        return danglingLineCache.getSome(voltageLevelId);
    }

    public DanglingLine createDanglingLine(Resource<DanglingLineAttributes> resource) {
        return danglingLineCache.create(resource);
    }

    public Collection<Identifiable<?>> getIdentifiables() {
        return ImmutableList.<Identifiable<?>>builder()
                .addAll(getSubstations())
                .addAll(getVoltageLevels())
                .addAll(getGenerators())
                .addAll(getBatteries())
                .addAll(getShuntCompensators())
                .addAll(getVscConverterStations())
                .addAll(getLccConverterStations())
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

    public Connectable<?> getConnectable(String connectableId, ConnectableType connectableType) {
        switch (connectableType) {
            case BUSBAR_SECTION:
                return getBusbarSection(connectableId).orElse(null);
            case LINE:
                return getLine(connectableId).orElse(null);
            case TWO_WINDINGS_TRANSFORMER:
                return getTwoWindingsTransformer(connectableId).orElse(null);
            case THREE_WINDINGS_TRANSFORMER:
                return getThreeWindingsTransformer(connectableId).orElse(null);
            case GENERATOR:
                return getGenerator(connectableId).orElse(null);
            case BATTERY:
                return getBattery(connectableId).orElse(null);
            case LOAD:
                return getLoad(connectableId).orElse(null);
            case SHUNT_COMPENSATOR:
                return getShuntCompensator(connectableId).orElse(null);
            case DANGLING_LINE:
                return getDanglingLine(connectableId).orElse(null);
            case STATIC_VAR_COMPENSATOR:
                return getStaticVarCompensator(connectableId).orElse(null);
            case HVDC_CONVERTER_STATION:
                return getHvdcConverterStation(connectableId).orElse(null);
            default:
                throw new IllegalStateException("Unexpected connectable type:" + connectableType);
        }
    }

    /**
     * WARNING!!!!!!!!!!!!!!!!!!
     * This method should be used used with caution as it does not fit well with NONE mode pre-loading.
     * As it tries to search for an unknown typed element id in all per element cache and that most of the time
     * the element won't be in the cache (think case where we try to get an generator, for sure we won't find it
     * in load, shunt and all the other cache type) and consequently we end up with a lot of request to the server to search
     * for something that does not exist.
     */
    public Identifiable<?> getIdentifiable(String id) {
        Objects.requireNonNull(id);
        if (network.getId().equals(id)) {
            return network;
        }
        return getSubstation(id).map(Identifiable.class::cast)
                .or(() -> getVoltageLevel(id))
                .or(() -> getGenerator(id))
                .or(() -> getBattery(id))
                .or(() -> getShuntCompensator(id))
                .or(() -> getVscConverterStation(id))
                .or(() -> getLccConverterStation(id))
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
        danglingLineCache.remove(danglingLineId);
    }

    //buses

    Optional<Bus> getBus(String id) {
        return busCache.getOne(id);
    }

    List<Bus> getBuses() {
        return busCache.getAll();
    }

    List<Bus> getBuses(String voltageLevelId) {
        return busCache.getSome(voltageLevelId);
    }

    ConfiguredBusImpl createBus(Resource<ConfiguredBusAttributes> resource) {
        return (ConfiguredBusImpl) busCache.create(resource);
    }

    public void removeBus(String busId) {
        busCache.remove(busId);
    }

    static void checkId(String id) {
        if (id == null || id.isEmpty()) {
            throw new PowsyblException("Invalid id '" + id + "'");
        }
    }

    boolean contains(String id) {
        checkId(id);
        return getIdentifiable(network.getIdFromAlias(id)) != null;
    }

    @SuppressWarnings("unchecked")
    <T extends IdentifiableAttributes> void updateResource(Resource<T> resource) {
        switch (resource.getType()) {
            case NETWORK:
                updateNetworkResource((Resource<NetworkAttributes>) resource);
                break;
            case SUBSTATION:
                updateSubstationResource((Resource<SubstationAttributes>) resource);
                break;
            case VOLTAGE_LEVEL:
                updateVoltageLevelResource((Resource<VoltageLevelAttributes>) resource);
                break;
            case LOAD:
                updateLoadResource((Resource<LoadAttributes>) resource);
                break;
            case GENERATOR:
                updateGeneratorResource((Resource<GeneratorAttributes>) resource);
                break;
            case BATTERY:
                updateBatteryResource((Resource<BatteryAttributes>) resource);
                break;
            case SHUNT_COMPENSATOR:
                updateShuntCompensatorResource((Resource<ShuntCompensatorAttributes>) resource);
                break;
            case VSC_CONVERTER_STATION:
                updateVscConverterStationResource((Resource<VscConverterStationAttributes>) resource);
                break;
            case LCC_CONVERTER_STATION:
                updateLccConverterStationResource((Resource<LccConverterStationAttributes>) resource);
                break;
            case STATIC_VAR_COMPENSATOR:
                updateStaticVarCompensatorResource((Resource<StaticVarCompensatorAttributes>) resource);
                break;
            case BUSBAR_SECTION:
                updateBusbarSectionResource((Resource<BusbarSectionAttributes>) resource);
                break;
            case SWITCH:
                updateSwitchResource((Resource<SwitchAttributes>) resource);
                break;
            case TWO_WINDINGS_TRANSFORMER:
                updateTwoWindingsTransformerResource((Resource<TwoWindingsTransformerAttributes>) resource);
                break;
            case THREE_WINDINGS_TRANSFORMER:
                updateThreeWindingsTransformerResource((Resource<ThreeWindingsTransformerAttributes>) resource);
                break;
            case LINE:
                updateLineResource((Resource<LineAttributes>) resource);
                break;
            case HVDC_LINE:
                updateHvdcLineResource((Resource<HvdcLineAttributes>) resource);
                break;
            case DANGLING_LINE:
                updateDanglingLineResource((Resource<DanglingLineAttributes>) resource);
                break;
            case CONFIGURED_BUS:
                updateConfiguredBusResource((Resource<ConfiguredBusAttributes>) resource);
                break;
            default:
                throw new IllegalStateException("Unknown resource type: " + resource.getType());
        }
    }

    void updateNetworkResource(Resource<NetworkAttributes> resource) {
        storeClient.updateNetworks(Collections.singletonList(resource));
    }

    void updateVoltageLevelResource(Resource<VoltageLevelAttributes> resource) {
        storeClient.updateVoltageLevels(network.getUuid(), Collections.singletonList(resource));
    }

    void updateSwitchResource(Resource<SwitchAttributes> resource) {
        storeClient.updateSwitches(network.getUuid(), Collections.singletonList(resource));
    }

    void updateLineResource(Resource<LineAttributes> resource) {
        storeClient.updateLines(network.getUuid(), Collections.singletonList(resource));
    }

    void updateTwoWindingsTransformerResource(Resource<TwoWindingsTransformerAttributes> resource) {
        storeClient.updateTwoWindingsTransformers(network.getUuid(), Collections.singletonList(resource));
    }

    void updateThreeWindingsTransformerResource(Resource<ThreeWindingsTransformerAttributes> resource) {
        storeClient.updateThreeWindingsTransformers(network.getUuid(), Collections.singletonList(resource));
    }

    void updateDanglingLineResource(Resource<DanglingLineAttributes> resource) {
        storeClient.updateDanglingLines(network.getUuid(), Collections.singletonList(resource));
    }

    void updateGeneratorResource(Resource<GeneratorAttributes> resource) {
        storeClient.updateGenerators(network.getUuid(), Collections.singletonList(resource));
    }

    void updateBatteryResource(Resource<BatteryAttributes> resource) {
        storeClient.updateBatteries(network.getUuid(), Collections.singletonList(resource));
    }

    void updateStaticVarCompensatorResource(Resource<StaticVarCompensatorAttributes> resource) {
        storeClient.updateStaticVarCompensators(network.getUuid(), Collections.singletonList(resource));
    }

    void updateShuntCompensatorResource(Resource<ShuntCompensatorAttributes> resource) {
        storeClient.updateShuntCompensators(network.getUuid(), Collections.singletonList(resource));
    }

    void updateLccConverterStationResource(Resource<LccConverterStationAttributes> resource) {
        storeClient.updateLccConverterStations(network.getUuid(), Collections.singletonList(resource));
    }

    void updateVscConverterStationResource(Resource<VscConverterStationAttributes> resource) {
        storeClient.updateVscConverterStations(network.getUuid(), Collections.singletonList(resource));
    }

    void updateLoadResource(Resource<LoadAttributes> resource) {
        storeClient.updateLoads(network.getUuid(), Collections.singletonList(resource));
    }

    void updateConfiguredBusResource(Resource<ConfiguredBusAttributes> resource) {
        storeClient.updateConfiguredBuses(network.getUuid(), Collections.singletonList(resource));
    }

    void updateHvdcLineResource(Resource<HvdcLineAttributes> resource) {
        storeClient.updateHvdcLines(network.getUuid(), Collections.singletonList(resource));
    }

    void updateSubstationResource(Resource<SubstationAttributes> resource) {
        storeClient.updateSubstations(network.getUuid(), Collections.singletonList(resource));
    }

    void updateBusbarSectionResource(Resource<BusbarSectionAttributes> resource) {
        storeClient.updateBusbarSections(network.getUuid(), Collections.singletonList(resource));
    }
}
