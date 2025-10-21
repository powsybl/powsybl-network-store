/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /* this field is not redundant with the field network above, it is needed to keep the networkUuid in case we delete
    the current variant, so we can fetch the network when we switch variants */
    private UUID networkUuid;

    private enum LoadingGranularity {
        ONE,
        SOME,
        ALL
    }

    private static final class LoadingInfos {

        private final LoadingGranularity granularity;

        private final String containerId;

        private LoadingInfos(LoadingGranularity granularity, String containerId) {
            this.granularity = Objects.requireNonNull(granularity);
            this.containerId = containerId;
        }

        private LoadingGranularity getGranularity() {
            return granularity;
        }

        private String getContainerId() {
            return containerId;
        }

        static LoadingInfos createOne() {
            return new LoadingInfos(LoadingGranularity.ONE, null);
        }

        static LoadingInfos createSome(String containerId) {
            return new LoadingInfos(LoadingGranularity.SOME, containerId);
        }

        static LoadingInfos createAll() {
            return new LoadingInfos(LoadingGranularity.ALL, null);
        }
    }

    class ObjectCache<I extends Identifiable<I>, T extends AbstractIdentifiableImpl<I, U>, U extends IdentifiableAttributes> {

        private final Map<String, T> objectsById = new LinkedHashMap<>();

        private final Map<String, LoadingInfos> loadingInfosByObjectId = new HashMap<>();

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

        private void updateLoadingInfos(String id, LoadingInfos loadingInfos) {
            LoadingInfos oldLoadingInfos = loadingInfosByObjectId.get(id);
            if (oldLoadingInfos == null || loadingInfos.getGranularity().ordinal() > oldLoadingInfos.getGranularity().ordinal()) {
                loadingInfosByObjectId.put(id, loadingInfos);
            }
        }

        void setResourcesToObjects() {
            // load resources of working variant the same granularity as previous variant (one, some or all)
            // we first synthesize loading granularity of all resources of the collection to next only call loading
            // methods with the highest granularity of loading
            LoadingGranularity largestGranulary = null;
            Set<String> containerIds = new HashSet<>();
            for (String id : objectsById.keySet()) {
                var loadingInfos = loadingInfosByObjectId.get(id);
                if (loadingInfos != null) {
                    if (largestGranulary == null || loadingInfos.getGranularity().ordinal() > largestGranulary.ordinal()) {
                        largestGranulary = loadingInfos.getGranularity();
                    }
                    if (loadingInfos.getGranularity() == LoadingGranularity.SOME) {
                        containerIds.add(loadingInfos.getContainerId());
                    }
                }
            }
            if (largestGranulary != null) {
                if (largestGranulary == LoadingGranularity.ALL) {
                    allResourcesGetter.get();
                } else if (largestGranulary == LoadingGranularity.SOME) {
                    for (String containerId : containerIds) {
                        someResourcesGetter.apply(containerId);
                    }
                }
            }

            for (Map.Entry<String, T> e : objectsById.entrySet()) {
                String id = e.getKey();
                T obj = e.getValue();
                Resource<U> workingVariantResource = oneResourceGetter.apply(id).orElse(null);
                obj.setResource(workingVariantResource);
            }
        }

        Stream<T> getAll() {
            List<Resource<U>> resources = allResourcesGetter.get();
            if (resources.size() != objectsById.size()) {
                var loadingInfos = LoadingInfos.createAll();
                for (Resource<U> resource : resources) {
                    if (!objectsById.containsKey(resource.getId())) {
                        objectsById.put(resource.getId(), objectCreator.apply(resource));
                    }
                    // save loading granularity
                    updateLoadingInfos(resource.getId(), loadingInfos);
                }
            }
            return objectsById.values().stream()
                    .filter(obj -> obj.getNullableResource() != null); // to discard removed objects in the current variant
        }

        Stream<T> getSome(String containerId) {
            List<Resource<U>> resources = someResourcesGetter.apply(containerId);
            var loadingInfos = LoadingInfos.createSome(containerId);
            return resources.stream().map(resource -> {
                T obj = objectsById.get(resource.getId());
                if (obj == null) {
                    obj = objectCreator.apply(resource);
                    objectsById.put(obj.getId(), obj);
                }
                // save loading granularity
                updateLoadingInfos(resource.getId(), loadingInfos);
                return obj;
            })
            .filter(obj -> obj.getNullableResource() != null); // to discard removed objects in the current variant
        }

        T add(Resource<U> resource) {
            T obj = objectsById.get(resource.getId());
            if (obj == null) {
                obj = objectCreator.apply(resource);
                objectsById.put(resource.getId(), obj);
                // save loading granularity
                updateLoadingInfos(resource.getId(), LoadingInfos.createOne());
            }
            return obj;
        }

        Optional<T> getOne(String id) {
            T obj = objectsById.get(id);
            if (obj == null) {
                Optional<Resource<U>> resource = oneResourceGetter.apply(id);
                obj = resource.map(objectCreator).orElse(null);
                if (obj != null) {
                    objectsById.put(id, obj);
                }
                // save loading granularity
                updateLoadingInfos(id, LoadingInfos.createOne());
            }
            return Optional.ofNullable(obj)
                    .filter(o -> o.getNullableResource() != null); // to discard removed objects in the current variant
        }

        T create(Resource<U> resource) {
            T obj = objectsById.get(resource.getId());
            if (obj != null) {
                if (obj.getNullableResource() != null) {
                    throw new IllegalArgumentException("'" + resource.getId() + "' already exists");
                } else {
                    // reuse previously created object
                    resourceCreator.accept(resource);
                    obj.setResource(resource);
                }
            } else {
                obj = objectCreator.apply(resource);
                resourceCreator.accept(resource);
                objectsById.put(resource.getId(), obj);
            }
            notifyCreation(obj);
            return obj;
        }

        void remove(String id) {
            resourceRemover.accept(id);
            T obj = objectsById.get(id);
            if (obj != null) {
                // to reuse the object from one variant to another one just set the resource to null
                // and keep the object in the cache
                obj.setResource(null);
            }
        }

        boolean isLoaded(String id) {
            return objectsById.containsKey(id);
        }
    }

    private final ObjectCache<Substation, SubstationImpl, SubstationAttributes> substationCache;

    private final ObjectCache<VoltageLevel, VoltageLevelImpl, VoltageLevelAttributes> voltageLevelCache;

    private final ObjectCache<Generator, GeneratorImpl, GeneratorAttributes> generatorCache;

    private final ObjectCache<Battery, BatteryImpl, BatteryAttributes> batteryCache;

    private final ObjectCache<ShuntCompensator, ShuntCompensatorImpl, ShuntCompensatorAttributes> shuntCompensatorCache;

    private final ObjectCache<VscConverterStation, VscConverterStationImpl, VscConverterStationAttributes> vscConverterStationCache;

    private final ObjectCache<LccConverterStation, LccConverterStationImpl, LccConverterStationAttributes> lccConverterStationCache;

    private final ObjectCache<StaticVarCompensator, StaticVarCompensatorImpl, StaticVarCompensatorAttributes> staticVarCompensatorCache;

    private final ObjectCache<Load, LoadImpl, LoadAttributes> loadCache;

    private final ObjectCache<BusbarSection, BusbarSectionImpl, BusbarSectionAttributes> busbarSectionCache;

    private final ObjectCache<Switch, SwitchImpl, SwitchAttributes> switchCache;

    private final ObjectCache<TwoWindingsTransformer, TwoWindingsTransformerImpl, TwoWindingsTransformerAttributes> twoWindingsTransformerCache;

    private final ObjectCache<ThreeWindingsTransformer, ThreeWindingsTransformerImpl, ThreeWindingsTransformerAttributes> threeWindingsTransformerCache;

    private final ObjectCache<Line, LineImpl, LineAttributes> lineCache;

    private final ObjectCache<TieLine, TieLineImpl, TieLineAttributes> tieLineCache;

    private final ObjectCache<HvdcLine, HvdcLineImpl, HvdcLineAttributes> hvdcLineCache;

    private final ObjectCache<DanglingLine, DanglingLineImpl, DanglingLineAttributes> danglingLineCache;

    private final ObjectCache<Ground, GroundImpl, GroundAttributes> groundCache;

    private final ObjectCache<Bus, ConfiguredBusImpl, ConfiguredBusAttributes> configuredBusCache;

    private final ObjectCache<Area, AreaImpl, AreaAttributes> areaCache;

    private final Map<ResourceType, ObjectCache> objectCachesByResourceType = new EnumMap<>(ResourceType.class);

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
        groundCache = new ObjectCache<>(resource -> storeClient.createGrounds(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getGround(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelGrounds(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getGrounds(network.getUuid(), workingVariantNum),
            id -> storeClient.removeGrounds(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> GroundImpl.create(NetworkObjectIndex.this, resource));
        configuredBusCache = new ObjectCache<>(resource -> storeClient.createConfiguredBuses(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getConfiguredBus(network.getUuid(), workingVariantNum, id),
            voltageLevelId -> storeClient.getVoltageLevelConfiguredBuses(network.getUuid(), workingVariantNum, voltageLevelId),
            () -> storeClient.getConfiguredBuses(network.getUuid(), workingVariantNum),
            id -> storeClient.removeConfiguredBuses(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> ConfiguredBusImpl.create(NetworkObjectIndex.this, resource));
        tieLineCache = new ObjectCache<>(resource -> storeClient.createTieLines(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getTieLine(network.getUuid(), workingVariantNum, id),
            null,
            () -> storeClient.getTieLines(network.getUuid(), workingVariantNum),
            id -> storeClient.removeTieLines(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> TieLineImpl.create(NetworkObjectIndex.this, resource));
        areaCache = new ObjectCache<>(resource -> storeClient.createAreas(network.getUuid(), Collections.singletonList(resource)),
            id -> storeClient.getArea(network.getUuid(), workingVariantNum, id),
            null,
            () -> storeClient.getAreas(network.getUuid(), workingVariantNum),
            id -> storeClient.removeAreas(network.getUuid(), workingVariantNum, Collections.singletonList(id)),
            resource -> AreaImpl.create(NetworkObjectIndex.this, resource));

        objectCachesByResourceType.put(ResourceType.SUBSTATION, substationCache);
        objectCachesByResourceType.put(ResourceType.VOLTAGE_LEVEL, voltageLevelCache);
        objectCachesByResourceType.put(ResourceType.GENERATOR, generatorCache);
        objectCachesByResourceType.put(ResourceType.BATTERY, batteryCache);
        objectCachesByResourceType.put(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorCache);
        objectCachesByResourceType.put(ResourceType.VSC_CONVERTER_STATION, vscConverterStationCache);
        objectCachesByResourceType.put(ResourceType.LCC_CONVERTER_STATION, lccConverterStationCache);
        objectCachesByResourceType.put(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorCache);
        objectCachesByResourceType.put(ResourceType.LOAD, loadCache);
        objectCachesByResourceType.put(ResourceType.BUSBAR_SECTION, busbarSectionCache);
        objectCachesByResourceType.put(ResourceType.SWITCH, switchCache);
        objectCachesByResourceType.put(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerCache);
        objectCachesByResourceType.put(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerCache);
        objectCachesByResourceType.put(ResourceType.LINE, lineCache);
        objectCachesByResourceType.put(ResourceType.HVDC_LINE, hvdcLineCache);
        objectCachesByResourceType.put(ResourceType.DANGLING_LINE, danglingLineCache);
        objectCachesByResourceType.put(ResourceType.GROUND, groundCache);
        objectCachesByResourceType.put(ResourceType.CONFIGURED_BUS, configuredBusCache);
        objectCachesByResourceType.put(ResourceType.TIE_LINE, tieLineCache);
        objectCachesByResourceType.put(ResourceType.AREA, areaCache);
    }

    public NetworkStoreClient getStoreClient() {
        return storeClient;
    }

    public void setNetwork(NetworkImpl network) {
        this.network = Objects.requireNonNull(network);
        this.networkUuid = network.getUuid();
    }

    NetworkImpl getNetwork() {
        return network;
    }

    UUID getNetworkUuid() {
        return networkUuid;
    }

    public int getWorkingVariantNum() {
        return workingVariantNum;
    }

    public void setWorkingVariantNum(int workingVariantNum) {
        this.workingVariantNum = workingVariantNum;
        if (workingVariantNum != -1) {
            network.setResource(storeClient.getNetwork(networkUuid, workingVariantNum).orElseThrow());
            substationCache.setResourcesToObjects();
            voltageLevelCache.setResourcesToObjects();
            generatorCache.setResourcesToObjects();
            batteryCache.setResourcesToObjects();
            shuntCompensatorCache.setResourcesToObjects();
            vscConverterStationCache.setResourcesToObjects();
            lccConverterStationCache.setResourcesToObjects();
            staticVarCompensatorCache.setResourcesToObjects();
            loadCache.setResourcesToObjects();
            busbarSectionCache.setResourcesToObjects();
            switchCache.setResourcesToObjects();
            twoWindingsTransformerCache.setResourcesToObjects();
            threeWindingsTransformerCache.setResourcesToObjects();
            lineCache.setResourcesToObjects();
            hvdcLineCache.setResourcesToObjects();
            danglingLineCache.setResourcesToObjects();
            groundCache.setResourcesToObjects();
            configuredBusCache.setResourcesToObjects();
            areaCache.setResourcesToObjects();
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

    void notifyBeforeRemoval(Identifiable<?> identifiable) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.beforeRemoval(identifiable);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    void notifyAfterRemoval(String id) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.afterRemoval(id);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    public void notifyExtensionBeforeRemoval(Extension<?> extension) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onExtensionBeforeRemoval(extension);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    public void notifyExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onExtensionAfterRemoval(identifiable, extensionName);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    public void notifyExtensionUpdate(Extension<?> extension, String attribute, String variantId, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            for (NetworkListener listener : network.getListeners()) {
                try {
                    listener.onExtensionUpdate(extension, attribute, variantId, oldValue, newValue);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }
    }

    public void notifyExtensionCreation(Extension<?> extension) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onExtensionCreation(extension);
            } catch (Exception t) {
                LOGGER.error(t.toString(), t);
            }
        }
    }

    public void notifyUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
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

    void notifyPropertyAdded(Identifiable<?> identifiable, Supplier<String> attribute, Object newValue) {
        if (!network.getListeners().isEmpty()) {
            notifyPropertyAdded(identifiable, attribute.get(), newValue);
        }
    }

    void notifyPropertyAdded(Identifiable<?> identifiable, String attribute, Object newValue) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onPropertyAdded(identifiable, attribute, newValue);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    void notifyPropertyReplaced(Identifiable<?> identifiable, Supplier<String> attribute, Object oldValue, Object newValue) {
        if (!network.getListeners().isEmpty() && !Objects.equals(oldValue, newValue)) {
            notifyPropertyReplaced(identifiable, attribute.get(), oldValue, newValue);
        }
    }

    void notifyPropertyReplaced(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onPropertyReplaced(identifiable, attribute, oldValue, newValue);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    void notifyPropertyRemoved(Identifiable<?> identifiable, Supplier<String> attribute, Object oldValue) {
        if (!network.getListeners().isEmpty()) {
            notifyPropertyRemoved(identifiable, attribute.get(), oldValue);
        }
    }

    void notifyPropertyRemoved(Identifiable<?> identifiable, String attribute, Object oldValue) {
        for (NetworkListener listener : network.getListeners()) {
            try {
                listener.onPropertyRemoved(identifiable, attribute, oldValue);
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
        return substationCache.getAll().collect(Collectors.toList());
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
        return voltageLevelCache.getAll().collect(Collectors.toList());
    }

    List<VoltageLevel> getVoltageLevels(String substationId) {
        return voltageLevelCache.getSome(substationId).collect(Collectors.toList());
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
        return generatorCache.getAll().collect(Collectors.toList());
    }

    List<Generator> getGenerators(String voltageLevelId) {
        return generatorCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    GeneratorImpl createGenerator(Resource<GeneratorAttributes> resource) {
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
        return batteryCache.getAll().collect(Collectors.toList());
    }

    List<Battery> getBatteries(String voltageLevelId) {
        return batteryCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    BatteryImpl createBattery(Resource<BatteryAttributes> resource) {
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
        return loadCache.getAll().collect(Collectors.toList());
    }

    List<Load> getLoads(String voltageLevelId) {
        return loadCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    LoadImpl createLoad(Resource<LoadAttributes> resource) {
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
        return busbarSectionCache.getAll().collect(Collectors.toList());
    }

    List<BusbarSection> getBusbarSections(String voltageLevelId) {
        return busbarSectionCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    BusbarSectionImpl createBusbarSection(Resource<BusbarSectionAttributes> resource) {
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
        return switchCache.getAll().collect(Collectors.toList());
    }

    List<Switch> getSwitches(String voltageLevelId) {
        return switchCache.getSome(voltageLevelId).collect(Collectors.toList());
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
        return twoWindingsTransformerCache.getAll().collect(Collectors.toList());
    }

    List<TwoWindingsTransformer> getTwoWindingsTransformers(String voltageLevelId) {
        return twoWindingsTransformerCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    TwoWindingsTransformerImpl createTwoWindingsTransformer(Resource<TwoWindingsTransformerAttributes> resource) {
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
        return threeWindingsTransformerCache.getAll().collect(Collectors.toList());
    }

    List<ThreeWindingsTransformer> getThreeWindingsTransformers(String voltageLevelId) {
        return threeWindingsTransformerCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    ThreeWindingsTransformerImpl createThreeWindingsTransformer(Resource<ThreeWindingsTransformerAttributes> resource) {
        return threeWindingsTransformerCache.create(resource);
    }

    public void removeThreeWindingsTransformer(String threeWindingsTransformerId) {
        threeWindingsTransformerCache.remove(threeWindingsTransformerId);
    }

    // line

    private LineImpl createLineOrTieLine(Resource<LineAttributes> resource) {
        return new LineImpl(this, resource);
    }

    Optional<LineImpl> getLine(String id) {
        return lineCache.getOne(id);
    }

    List<Line> getLines() {
        return lineCache.getAll().collect(Collectors.toList());
    }

    List<Line> getLines(String voltageLevelId) {
        return lineCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    LineImpl createLine(Resource<LineAttributes> resource) {
        return lineCache.create(resource);
    }

    public void removeLine(String lineId) {
        lineCache.remove(lineId);
    }

    Optional<TieLineImpl> getTieLine(String id) {
        return tieLineCache.getOne(id);
    }

    List<TieLine> getTieLines() {
        return tieLineCache.getAll().collect(Collectors.toList());
    }

    TieLineImpl createTieLine(Resource<TieLineAttributes> resource) {
        return tieLineCache.create(resource);
    }

    public void removeTieLine(String tieLineId) {
        tieLineCache.remove(tieLineId);
    }


    // shunt compensator

    Optional<ShuntCompensatorImpl> getShuntCompensator(String id) {
        return shuntCompensatorCache.getOne(id);
    }

    List<ShuntCompensator> getShuntCompensators() {
        return shuntCompensatorCache.getAll().collect(Collectors.toList());
    }

    List<ShuntCompensator> getShuntCompensators(String voltageLevelId) {
        return shuntCompensatorCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    ShuntCompensatorImpl createShuntCompensator(Resource<ShuntCompensatorAttributes> resource) {
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
        return vscConverterStationCache.getAll().collect(Collectors.toList());
    }

    List<VscConverterStation> getVscConverterStations(String voltageLevelId) {
        return vscConverterStationCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    public VscConverterStationImpl createVscConverterStation(Resource<VscConverterStationAttributes> resource) {
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
        return lccConverterStationCache.getAll().collect(Collectors.toList());
    }

    List<LccConverterStation> getLccConverterStations(String voltageLevelId) {
        return lccConverterStationCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    public LccConverterStationImpl createLccConverterStation(Resource<LccConverterStationAttributes> resource) {
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
        return staticVarCompensatorCache.getAll().collect(Collectors.toList());
    }

    List<StaticVarCompensator> getStaticVarCompensators(String voltageLevelId) {
        return staticVarCompensatorCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    public StaticVarCompensatorImpl createStaticVarCompensator(Resource<StaticVarCompensatorAttributes> resource) {
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
        return hvdcLineCache.getAll().collect(Collectors.toList());
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
        return danglingLineCache.getAll().collect(Collectors.toList());
    }

    List<DanglingLine> getDanglingLines(String voltageLevelId) {
        return danglingLineCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    public DanglingLineImpl createDanglingLine(Resource<DanglingLineAttributes> resource) {
        return danglingLineCache.create(resource);
    }

    // Ground

    Optional<GroundImpl> getGround(String id) {
        return groundCache.getOne(id);
    }

    List<Ground> getGrounds() {
        return groundCache.getAll().collect(Collectors.toList());
    }

    List<Ground> getGrounds(String voltageLevelId) {
        return groundCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    public GroundImpl createGround(Resource<GroundAttributes> resource) {
        return groundCache.create(resource);
    }

    public void removeGround(String groundId) {
        groundCache.remove(groundId);
    }

    // Area

    Optional<AreaImpl> getArea(String id) {
        return areaCache.getOne(id);
    }

    List<Area> getAreas() {
        return areaCache.getAll().collect(Collectors.toList());
    }

    public AreaImpl createArea(Resource<AreaAttributes> resource) {
        return areaCache.create(resource);
    }

    public void removeArea(String areaId) {
        areaCache.remove(areaId);
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
                .addAll(getGrounds())
                .addAll(getAreas())
                .addAll(getConfiguredBuses())
                .build();
    }

    public Connectable<?> getConnectable(String connectableId, IdentifiableType connectableType) {
        return switch (connectableType) {
            case BUSBAR_SECTION -> getBusbarSection(connectableId).orElse(null);
            case LINE -> getLine(connectableId).orElse(null);
            case TWO_WINDINGS_TRANSFORMER -> getTwoWindingsTransformer(connectableId).orElse(null);
            case THREE_WINDINGS_TRANSFORMER -> getThreeWindingsTransformer(connectableId).orElse(null);
            case GENERATOR -> getGenerator(connectableId).orElse(null);
            case BATTERY -> getBattery(connectableId).orElse(null);
            case LOAD -> getLoad(connectableId).orElse(null);
            case SHUNT_COMPENSATOR -> getShuntCompensator(connectableId).orElse(null);
            case DANGLING_LINE -> getDanglingLine(connectableId).orElse(null);
            case GROUND -> getGround(connectableId).orElse(null);
            case STATIC_VAR_COMPENSATOR -> getStaticVarCompensator(connectableId).orElse(null);
            case HVDC_CONVERTER_STATION -> getHvdcConverterStation(connectableId).orElse(null);
            default -> throw new IllegalStateException("Unexpected connectable type:" + connectableType);
        };
    }

    public Branch<?> getBranch(String branchId) {
        //FIXME strange structure ?
        // first try in the line cache, then in 2 windings transformer cache, then load from server
        if (lineCache.isLoaded(branchId)) {
            return lineCache.getOne(branchId).orElse(null);
        } else if (twoWindingsTransformerCache.isLoaded(branchId)) {
            return twoWindingsTransformerCache.getOne(branchId).orElse(null);
        } else if (tieLineCache.isLoaded(branchId)) {
            return tieLineCache.getOne(branchId).orElse(null);
        } else {
            Branch<?> b = lineCache.getOne(branchId)
                    .map(Branch.class::cast)
                    .orElseGet(() -> twoWindingsTransformerCache.getOne(branchId)
                            .orElse(null));
            if (b == null) {
                return tieLineCache.getOne(branchId).orElse(null);
            } else {
                return b;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Identifiable<?> getIdentifiable(String id) {
        Objects.requireNonNull(id);

        if (network.getId().equals(id)) {
            return network;
        }

        // check before that object is in one of the cache to avoid a useless query to the back end
        for (var objectCache : objectCachesByResourceType.values()) {
            if (objectCache.isLoaded(id)) {
                return (Identifiable<?>) objectCache.getOne(id).orElse(null);
            }
        }

        // load resource
        Resource<IdentifiableAttributes> resource = storeClient.getIdentifiable(network.getUuid(), workingVariantNum, id).orElse(null);
        if (resource != null) {
            // and update the corresponding cache
            var objectCache = objectCachesByResourceType.get(resource.getType());
            return objectCache.add(resource);
        }

        return null;
    }

    public void removeDanglingLine(String danglingLineId) {
        danglingLineCache.remove(danglingLineId);
    }

    // configured buses

    Optional<ConfiguredBusImpl> getConfiguredBus(String id) {
        return configuredBusCache.getOne(id);
    }

    List<Bus> getConfiguredBuses() {
        return configuredBusCache.getAll().collect(Collectors.toList());
    }

    List<Bus> getConfiguredBuses(String voltageLevelId) {
        return configuredBusCache.getSome(voltageLevelId).collect(Collectors.toList());
    }

    ConfiguredBusImpl createConfiguredBus(Resource<ConfiguredBusAttributes> resource) {
        return configuredBusCache.create(resource);
    }

    public void removeConfiguredBus(String busId) {
        configuredBusCache.remove(busId);
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
    <T extends IdentifiableAttributes> void updateResource(Resource<T> resource, AttributeFilter attributeFilter) {
        switch (resource.getType()) {
            case NETWORK -> updateNetworkResource((Resource<NetworkAttributes>) resource, attributeFilter);
            case SUBSTATION -> updateSubstationResource((Resource<SubstationAttributes>) resource, attributeFilter);
            case VOLTAGE_LEVEL -> updateVoltageLevelResource((Resource<VoltageLevelAttributes>) resource, attributeFilter);
            case LOAD -> updateLoadResource((Resource<LoadAttributes>) resource, attributeFilter);
            case GENERATOR -> updateGeneratorResource((Resource<GeneratorAttributes>) resource, attributeFilter);
            case BATTERY -> updateBatteryResource((Resource<BatteryAttributes>) resource, attributeFilter);
            case SHUNT_COMPENSATOR -> updateShuntCompensatorResource((Resource<ShuntCompensatorAttributes>) resource, attributeFilter);
            case VSC_CONVERTER_STATION -> updateVscConverterStationResource((Resource<VscConverterStationAttributes>) resource, attributeFilter);
            case LCC_CONVERTER_STATION -> updateLccConverterStationResource((Resource<LccConverterStationAttributes>) resource, attributeFilter);
            case STATIC_VAR_COMPENSATOR -> updateStaticVarCompensatorResource((Resource<StaticVarCompensatorAttributes>) resource, attributeFilter);
            case BUSBAR_SECTION -> updateBusbarSectionResource((Resource<BusbarSectionAttributes>) resource, attributeFilter);
            case SWITCH -> updateSwitchResource((Resource<SwitchAttributes>) resource, attributeFilter);
            case TWO_WINDINGS_TRANSFORMER -> updateTwoWindingsTransformerResource((Resource<TwoWindingsTransformerAttributes>) resource, attributeFilter);
            case THREE_WINDINGS_TRANSFORMER -> updateThreeWindingsTransformerResource((Resource<ThreeWindingsTransformerAttributes>) resource, attributeFilter);
            case LINE -> updateLineResource((Resource<LineAttributes>) resource, attributeFilter);
            case HVDC_LINE -> updateHvdcLineResource((Resource<HvdcLineAttributes>) resource, attributeFilter);
            case DANGLING_LINE -> updateDanglingLineResource((Resource<DanglingLineAttributes>) resource, attributeFilter);
            case GROUND -> updateGroundResource((Resource<GroundAttributes>) resource, attributeFilter);
            case CONFIGURED_BUS -> updateConfiguredBusResource((Resource<ConfiguredBusAttributes>) resource, attributeFilter);
            case TIE_LINE -> updateTieLineResource((Resource<TieLineAttributes>) resource, attributeFilter);
            case AREA -> updateAreaResource((Resource<AreaAttributes>) resource, attributeFilter);
            default -> throw new IllegalStateException("Unknown resource type: " + resource.getType());
        }
    }

    void updateNetworkResource(Resource<NetworkAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateNetworks(Collections.singletonList(resource), attributeFilter);
    }

    void updateVoltageLevelResource(Resource<VoltageLevelAttributes> resource) {
        updateVoltageLevelResource(resource, null);
    }

    void updateVoltageLevelResource(Resource<VoltageLevelAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateVoltageLevels(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateSwitchResource(Resource<SwitchAttributes> resource) {
        updateSwitchResource(resource, null);
    }

    void updateSwitchResource(Resource<SwitchAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateSwitches(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateLineResource(Resource<LineAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateLines(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateTwoWindingsTransformerResource(Resource<TwoWindingsTransformerAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateTwoWindingsTransformers(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateThreeWindingsTransformerResource(Resource<ThreeWindingsTransformerAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateThreeWindingsTransformers(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateDanglingLineResource(Resource<DanglingLineAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateDanglingLines(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateGroundResource(Resource<GroundAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateGrounds(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateAreaResource(Resource<AreaAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateAreas(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateTieLineResource(Resource<TieLineAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateTieLines(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateGeneratorResource(Resource<GeneratorAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateGenerators(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateBatteryResource(Resource<BatteryAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateBatteries(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateStaticVarCompensatorResource(Resource<StaticVarCompensatorAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateStaticVarCompensators(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateShuntCompensatorResource(Resource<ShuntCompensatorAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateShuntCompensators(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateLccConverterStationResource(Resource<LccConverterStationAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateLccConverterStations(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateVscConverterStationResource(Resource<VscConverterStationAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateVscConverterStations(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateLoadResource(Resource<LoadAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateLoads(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateConfiguredBusResource(Resource<ConfiguredBusAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateConfiguredBuses(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateHvdcLineResource(Resource<HvdcLineAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateHvdcLines(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateSubstationResource(Resource<SubstationAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateSubstations(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    void updateBusbarSectionResource(Resource<BusbarSectionAttributes> resource, AttributeFilter attributeFilter) {
        storeClient.updateBusbarSections(network.getUuid(), Collections.singletonList(resource), attributeFilter);
    }

    public void loadExtensionAttributes(ResourceType type, String identifiableId, String extensionName) {
        storeClient.getExtensionAttributes(network.getUuid(), workingVariantNum, type, identifiableId, extensionName);
    }

    public void loadAllExtensionsAttributesByIdentifiableId(ResourceType type, String identifiableId) {
        storeClient.getAllExtensionsAttributesByIdentifiableId(network.getUuid(), workingVariantNum, type, identifiableId);
    }

    public void removeExtensionAttributes(ResourceType type, String identifiableId, String extensionName) {
        storeClient.removeExtensionAttributes(network.getUuid(), workingVariantNum, type, Map.of(identifiableId, Set.of(extensionName)));
    }

    public void loadOperationalLimitsGroupAttributes(ResourceType type, String branchId, String operationalLimitGroupName, int side) {
        storeClient.getOperationalLimitsGroupAttributes(network.getUuid(), workingVariantNum, type, branchId, operationalLimitGroupName, side);
    }

    public void loadOperationalLimitsGroupAttributesForBranchSide(ResourceType type, String branchId, int side) {
        storeClient.getOperationalLimitsGroupAttributesForBranchSide(network.getUuid(), workingVariantNum, type, branchId, side);
    }

    public void loadSelectedOperationalLimitsGroupAttributes(ResourceType type, String branchId, String operationalLimitGroupName, int side) {
        storeClient.getSelectedOperationalLimitsGroupAttributes(network.getUuid(), workingVariantNum, type, branchId, operationalLimitGroupName, side);
    }

    public void removeOperationalLimitsGroupAttributes(ResourceType type, String branchId, String operationalLimitGroupName, int side) {
        storeClient.removeOperationalLimitsGroupAttributes(network.getUuid(), workingVariantNum, type, Map.of(branchId, Map.of(side, Set.of(operationalLimitGroupName))));
    }
}
