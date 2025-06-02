/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.network.store.model.*;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CachedNetworkStoreClient extends AbstractForwardingNetworkStoreClient<NetworkStoreClient> implements NetworkStoreClient {

    private static final int MAX_GET_IDENTIFIABLE_CALL_COUNT = 10;

    private final Map<UUID, List<VariantInfos>> variantsInfosByNetworkUuid = new HashMap<>();

    private final NetworkCollectionIndex<CollectionCache<NetworkAttributes>> networksCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    (networkUuid, variantNum, id) -> delegate.getNetwork(networkUuid, variantNum),
                    null,
                    (networkUuid, variantNum) -> delegate.getNetwork(networkUuid, variantNum).stream().collect(Collectors.toList()),
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<SubstationAttributes>> substationsCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getSubstation,
                    null,
                    delegate::getSubstations,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<VoltageLevelAttributes>> voltageLevelsCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getVoltageLevel,
                    delegate::getVoltageLevelsInSubstation,
                    delegate::getVoltageLevels,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<SwitchAttributes>> switchesCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getSwitch,
                    delegate::getVoltageLevelSwitches,
                    delegate::getSwitches,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<BusbarSectionAttributes>> busbarSectionsCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getBusbarSection,
                    delegate::getVoltageLevelBusbarSections,
                    delegate::getBusbarSections,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<LoadAttributes>> loadsCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getLoad,
                    delegate::getVoltageLevelLoads,
                    delegate::getLoads,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<GeneratorAttributes>> generatorsCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getGenerator,
                    delegate::getVoltageLevelGenerators,
                    delegate::getGenerators,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<BatteryAttributes>> batteriesCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getBattery,
                    delegate::getVoltageLevelBatteries,
                    delegate::getBatteries,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<TwoWindingsTransformerAttributes>> twoWindingsTransformerCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getTwoWindingsTransformer,
                    delegate::getVoltageLevelTwoWindingsTransformers,
                    delegate::getTwoWindingsTransformers,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<ThreeWindingsTransformerAttributes>> threeWindingsTransformerCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getThreeWindingsTransformer,
                    delegate::getVoltageLevelThreeWindingsTransformers,
                    delegate::getThreeWindingsTransformers,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<LineAttributes>> linesCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getLine,
                    delegate::getVoltageLevelLines,
                    delegate::getLines,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<ShuntCompensatorAttributes>> shuntCompensatorsCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getShuntCompensator,
                    delegate::getVoltageLevelShuntCompensators,
                    delegate::getShuntCompensators,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<VscConverterStationAttributes>> vscConverterStationCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getVscConverterStation,
                    delegate::getVoltageLevelVscConverterStations,
                    delegate::getVscConverterStations,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<LccConverterStationAttributes>> lccConverterStationCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getLccConverterStation,
                    delegate::getVoltageLevelLccConverterStations,
                    delegate::getLccConverterStations,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<StaticVarCompensatorAttributes>> staticVarCompensatorCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getStaticVarCompensator,
                    delegate::getVoltageLevelStaticVarCompensators,
                    delegate::getStaticVarCompensators,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<HvdcLineAttributes>> hvdcLinesCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getHvdcLine,
                    null,
                    delegate::getHvdcLines,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<DanglingLineAttributes>> danglingLinesCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getDanglingLine,
                    delegate::getVoltageLevelDanglingLines,
                    delegate::getDanglingLines,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<ConfiguredBusAttributes>> configuredBusesCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getConfiguredBus,
                    delegate::getVoltageLevelConfiguredBuses,
                    delegate::getConfiguredBuses,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<TieLineAttributes>> tieLinesCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getTieLine,
                    null,
                    delegate::getTieLines,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<GroundAttributes>> groundsCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getGround,
                    delegate::getVoltageLevelGrounds,
                    delegate::getGrounds,
                    delegate)
            );

    private final NetworkCollectionIndex<CollectionCache<AreaAttributes>> areasCache =
            new NetworkCollectionIndex<>(() -> new CollectionCache<>(
                    delegate::getArea,
                    null,
                    delegate::getAreas,
                    delegate)
            );

    private final Map<ResourceType, NetworkCollectionIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> voltageLevelContainersCaches = new EnumMap<>(ResourceType.class);

    private final Map<ResourceType, NetworkCollectionIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> networkContainersCaches = new EnumMap<>(ResourceType.class);

    private final Map<Pair<UUID, Integer>, MutableInt> identifiableCallCountByNetworkVariant = new HashMap<>();

    private final Map<Pair<UUID, Integer>, Set<String>> identifiablesIdsByNetworkVariant = new HashMap<>();

    public CachedNetworkStoreClient(NetworkStoreClient delegate) {
        super(delegate);
        voltageLevelContainersCaches.put(ResourceType.SWITCH, switchesCache);
        voltageLevelContainersCaches.put(ResourceType.BUSBAR_SECTION, busbarSectionsCache);
        voltageLevelContainersCaches.put(ResourceType.LOAD, loadsCache);
        voltageLevelContainersCaches.put(ResourceType.GENERATOR, generatorsCache);
        voltageLevelContainersCaches.put(ResourceType.BATTERY, batteriesCache);
        voltageLevelContainersCaches.put(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerCache);
        voltageLevelContainersCaches.put(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerCache);
        voltageLevelContainersCaches.put(ResourceType.LINE, linesCache);
        voltageLevelContainersCaches.put(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorsCache);
        voltageLevelContainersCaches.put(ResourceType.VSC_CONVERTER_STATION, vscConverterStationCache);
        voltageLevelContainersCaches.put(ResourceType.LCC_CONVERTER_STATION, lccConverterStationCache);
        voltageLevelContainersCaches.put(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorCache);
        voltageLevelContainersCaches.put(ResourceType.HVDC_LINE, hvdcLinesCache);
        voltageLevelContainersCaches.put(ResourceType.DANGLING_LINE, danglingLinesCache);
        voltageLevelContainersCaches.put(ResourceType.CONFIGURED_BUS, configuredBusesCache);
        voltageLevelContainersCaches.put(ResourceType.GROUND, groundsCache);

        networkContainersCaches.putAll(voltageLevelContainersCaches);
        networkContainersCaches.put(ResourceType.SUBSTATION, substationsCache);
        networkContainersCaches.put(ResourceType.VOLTAGE_LEVEL, voltageLevelsCache);
        networkContainersCaches.put(ResourceType.TIE_LINE, tieLinesCache);
        networkContainersCaches.put(ResourceType.AREA, areasCache);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        delegate.createNetworks(networkResources);
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            UUID networkUuid = networkResource.getAttributes().getUuid();
            int variantNum = networkResource.getVariantNum();
            networksCache.getCollection(networkUuid, variantNum).createResource(networkResource);
            addIdentifiableId(networkUuid, networkResource);

            // initialize network sub-collection cache to set to fully loaded
            networkContainersCaches.values().forEach(cache -> cache.getCollection(networkUuid, networkResource.getVariantNum()).init());

            variantsInfosByNetworkUuid.computeIfAbsent(networkUuid, k -> new ArrayList<>())
                    .add(new VariantInfos(networkResource.getAttributes().getVariantId(), networkResource.getVariantNum()));
        }
    }

    @Override
    public List<NetworkInfos> getNetworksInfos() {
        // no need to cache, because only use from command line admin tools
        return delegate.getNetworksInfos();
    }

    @Override
    public List<VariantInfos> getVariantsInfos(UUID networkUuid) {
        return variantsInfosByNetworkUuid.computeIfAbsent(networkUuid, delegate::getVariantsInfos);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum) {
        return networksCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum).stream().findFirst();
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        delegate.deleteNetwork(networkUuid);
        networksCache.removeCollection(networkUuid);
        networkContainersCaches.values().forEach(cache -> cache.removeCollection(networkUuid));
        variantsInfosByNetworkUuid.remove(networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        delegate.deleteNetwork(networkUuid, variantNum);
        networksCache.removeCollection(networkUuid, variantNum);
        networkContainersCaches.values().forEach(cache -> cache.removeCollection(networkUuid, variantNum));
        List<VariantInfos> variantsInfos = variantsInfosByNetworkUuid.get(networkUuid);
        if (variantsInfos != null) {
            variantsInfos.removeIf(infos -> infos.getNum() == variantNum);
        }
    }

    @Override
    public void updateNetworks(List<Resource<NetworkAttributes>> networkResources, AttributeFilter attributeFilter) {
        delegate.updateNetworks(networkResources, attributeFilter);
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            UUID networkUuid = networkResource.getAttributes().getUuid();
            int variantNum = networkResource.getVariantNum();
            networksCache.getCollection(networkUuid, variantNum).updateResource(networkResource);
        }
    }

    private static <T extends IdentifiableAttributes> void cloneCollection(NetworkCollectionIndex<CollectionCache<T>> cache, UUID networkUuid,
                                                                           int sourceVariantNum, int targetVariantNum, ObjectMapper objectMapper,
                                                                           Consumer<Resource<T>> resourcePostProcessor) {
        // clone resources from source variant collection
        CollectionCache<T> cloneCollection = cache.getCollection(networkUuid, sourceVariantNum)
                .clone(objectMapper, targetVariantNum, resourcePostProcessor);
        cache.addCollection(networkUuid, targetVariantNum, cloneCollection);
    }

    private static <T extends IdentifiableAttributes> void cloneCollection(NetworkCollectionIndex<CollectionCache<T>> cache, UUID networkUuid,
                                                                           int sourceVariantNum, int targetVariantNum, ObjectMapper objectMapper) {
        cloneCollection(cache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper, null);
    }

    @Override
    public void cloneNetwork(UUID networkUuid, int sourceVariantNum, int targetVariantNum, String targetVariantId) {
        delegate.cloneNetwork(networkUuid, sourceVariantNum, targetVariantNum, targetVariantId);
        SimpleModule module = new SimpleModule();
        module.addKeyDeserializer(OperationalLimitsGroupIdentifier.class, new OperationalLimitsGroupIdentifierDeserializer());
        var objectMapper = JsonUtil.createObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(module)
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        // clone each collection and re-assign variant number and id
        cloneCollection(switchesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(busbarSectionsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(loadsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(generatorsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(batteriesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(twoWindingsTransformerCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(threeWindingsTransformerCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(linesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(shuntCompensatorsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(vscConverterStationCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(lccConverterStationCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(staticVarCompensatorCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(hvdcLinesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(danglingLinesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(tieLinesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(areasCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(configuredBusesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(groundsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(substationsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(voltageLevelsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(networksCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper,
                networkResource -> {
                    NetworkAttributes networkAttributes = networkResource.getAttributes();
                    networkAttributes.setVariantId(targetVariantId);
                    if (networkAttributes.isFullVariant()) {
                        networkAttributes.setFullVariantNum(sourceVariantNum);
                    }
                });

        variantsInfosByNetworkUuid.computeIfAbsent(networkUuid, k -> new ArrayList<>())
                .add(new VariantInfos(targetVariantId, targetVariantNum));
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        delegate.createSubstations(networkUuid, substationResources);

        for (Resource<SubstationAttributes> substationResource : substationResources) {
            substationsCache.getCollection(networkUuid, substationResource.getVariantNum()).createResource(substationResource);
            addIdentifiableId(networkUuid, substationResource);

            // initialize voltage level cache to set to fully loaded
            voltageLevelsCache.getCollection(networkUuid, substationResource.getVariantNum()).initContainer(substationResource.getId());
        }
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return substationsCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return substationsCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, substationId);
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources, AttributeFilter attributeFilter) {
        delegate.updateSubstations(networkUuid, substationResources, attributeFilter);
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            substationsCache.getCollection(networkUuid, substationResource.getVariantNum()).updateResource(substationResource);
        }
    }

    @Override
    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        delegate.removeSubstations(networkUuid, variantNum, substationsId);
        substationsCache.getCollection(networkUuid, variantNum).removeResources(substationsId);
        removeIdentifiableIds(networkUuid, variantNum, substationsId);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        delegate.createVoltageLevels(networkUuid, voltageLevelResources);
        for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
            voltageLevelsCache.getCollection(networkUuid, voltageLevelResource.getVariantNum()).createResource(voltageLevelResource);
            addIdentifiableId(networkUuid, voltageLevelResource);
        }

        // initialize voltage level sub-collection cache to set to fully loaded
        voltageLevelContainersCaches.values().forEach(cache -> {
            for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
                cache.getCollection(networkUuid, voltageLevelResource.getVariantNum()).initContainer(voltageLevelResource.getId());
            }
        });
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return voltageLevelsCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return voltageLevelsCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources, AttributeFilter attributeFilter) {
        delegate.updateVoltageLevels(networkUuid, voltageLevelResources, attributeFilter);
        for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
            voltageLevelsCache.getCollection(networkUuid, voltageLevelResource.getVariantNum()).updateResource(voltageLevelResource);
        }
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        delegate.removeVoltageLevels(networkUuid, variantNum, voltageLevelsId);
        voltageLevelsCache.getCollection(networkUuid, variantNum).removeResources(voltageLevelsId);
        removeIdentifiableIds(networkUuid, variantNum, voltageLevelsId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        return voltageLevelsCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, substationId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return generatorsCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        delegate.removeGenerators(networkUuid, variantNum, generatorsId);
        generatorsCache.getCollection(networkUuid, variantNum).removeResources(generatorsId);
        removeIdentifiableIds(networkUuid, variantNum, generatorsId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return batteriesCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        delegate.removeBatteries(networkUuid, variantNum, batteriesId);
        batteriesCache.getCollection(networkUuid, variantNum).removeResources(batteriesId);
        removeIdentifiableIds(networkUuid, variantNum, batteriesId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return loadsCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        delegate.removeLoads(networkUuid, variantNum, loadsId);
        loadsCache.getCollection(networkUuid, variantNum).removeResources(loadsId);
        removeIdentifiableIds(networkUuid, variantNum, loadsId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return shuntCompensatorsCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        delegate.removeShuntCompensators(networkUuid, variantNum, shuntCompensatorsId);
        shuntCompensatorsCache.getCollection(networkUuid, variantNum).removeResources(shuntCompensatorsId);
        removeIdentifiableIds(networkUuid, variantNum, shuntCompensatorsId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return staticVarCompensatorCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        delegate.removeStaticVarCompensators(networkUuid, variantNum, staticVarCompensatorsId);
        staticVarCompensatorCache.getCollection(networkUuid, variantNum).removeResources(staticVarCompensatorsId);
        removeIdentifiableIds(networkUuid, variantNum, staticVarCompensatorsId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return vscConverterStationCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        delegate.removeVscConverterStations(networkUuid, variantNum, vscConverterStationsId);
        vscConverterStationCache.getCollection(networkUuid, variantNum).removeResources(vscConverterStationsId);
        removeIdentifiableIds(networkUuid, variantNum, vscConverterStationsId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return lccConverterStationCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        delegate.removeLccConverterStations(networkUuid, variantNum, lccConverterStationsId);
        lccConverterStationCache.getCollection(networkUuid, variantNum).removeResources(lccConverterStationsId);
        removeIdentifiableIds(networkUuid, variantNum, lccConverterStationsId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return twoWindingsTransformerCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        delegate.removeTwoWindingsTransformers(networkUuid, variantNum, twoWindingsTransformersId);
        twoWindingsTransformerCache.getCollection(networkUuid, variantNum).removeResources(twoWindingsTransformersId);
        removeIdentifiableIds(networkUuid, variantNum, twoWindingsTransformersId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return threeWindingsTransformerCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        delegate.removeThreeWindingsTransformers(networkUuid, variantNum, threeWindingsTransformersId);
        threeWindingsTransformerCache.getCollection(networkUuid, variantNum).removeResources(threeWindingsTransformersId);
        removeIdentifiableIds(networkUuid, variantNum, threeWindingsTransformersId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return linesCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        delegate.removeLines(networkUuid, variantNum, linesId);
        linesCache.getCollection(networkUuid, variantNum).removeResources(linesId);
        removeIdentifiableIds(networkUuid, variantNum, linesId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return danglingLinesCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<GroundAttributes>> getVoltageLevelGrounds(UUID networkUuid, int variantNum, String voltageLevelId) {
        return groundsCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        delegate.createSwitches(networkUuid, switchResources);
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            switchesCache.getCollection(networkUuid, switchResource.getVariantNum()).createResource(switchResource);
            addIdentifiableId(networkUuid, switchResource);
        }
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return switchesCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return switchesCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, switchId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return switchesCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources, AttributeFilter attributeFilter) {
        delegate.updateSwitches(networkUuid, switchResources, attributeFilter);
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            switchesCache.getCollection(networkUuid, switchResource.getVariantNum()).updateResource(switchResource);
        }
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        delegate.removeSwitches(networkUuid, variantNum, switchesId);
        switchesCache.getCollection(networkUuid, variantNum).removeResources(switchesId);
        removeIdentifiableIds(networkUuid, variantNum, switchesId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        delegate.createBusbarSections(networkUuid, busbarSectionResources);
        for (Resource<BusbarSectionAttributes> busbarSectionResource : busbarSectionResources) {
            busbarSectionsCache.getCollection(networkUuid, busbarSectionResource.getVariantNum()).createResource(busbarSectionResource);
            addIdentifiableId(networkUuid, busbarSectionResource);
        }
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busbarSectionsId) {
        delegate.removeBusBarSections(networkUuid, variantNum, busbarSectionsId);
        busbarSectionsCache.getCollection(networkUuid, variantNum).removeResources(busbarSectionsId);
        removeIdentifiableIds(networkUuid, variantNum, busbarSectionsId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return busbarSectionsCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return busbarSectionsCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, busbarSectionId);
    }

    @Override
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources, AttributeFilter attributeFilter) {
        delegate.updateBusbarSections(networkUuid, busbarSectionResources, attributeFilter);
        for (Resource<BusbarSectionAttributes> busbarSectionResource : busbarSectionResources) {
            busbarSectionsCache.getCollection(networkUuid, busbarSectionResource.getVariantNum()).updateResource(busbarSectionResource);
        }
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return busbarSectionsCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        delegate.createLoads(networkUuid, loadResources);
        for (Resource<LoadAttributes> loadResource : loadResources) {
            loadsCache.getCollection(networkUuid, loadResource.getVariantNum()).createResource(loadResource);
            addIdentifiableId(networkUuid, loadResource);
        }
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return loadsCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return loadsCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, loadId);
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources, AttributeFilter attributeFilter) {
        delegate.updateLoads(networkUuid, loadResources, attributeFilter);
        for (Resource<LoadAttributes> loadResource : loadResources) {
            loadsCache.getCollection(networkUuid, loadResource.getVariantNum()).updateResource(loadResource);
        }
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        delegate.createGenerators(networkUuid, generatorResources);
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            generatorsCache.getCollection(networkUuid, generatorResource.getVariantNum()).createResource(generatorResource);
            addIdentifiableId(networkUuid, generatorResource);
        }
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return generatorsCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return generatorsCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, generatorId);
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources, AttributeFilter attributeFilter) {
        delegate.updateGenerators(networkUuid, generatorResources, attributeFilter);
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            generatorsCache.getCollection(networkUuid, generatorResource.getVariantNum()).updateResource(generatorResource);
        }
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        delegate.createBatteries(networkUuid, batteryResources);
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            batteriesCache.getCollection(networkUuid, batteryResource.getVariantNum()).createResource(batteryResource);
            addIdentifiableId(networkUuid, batteryResource);
        }
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return batteriesCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return batteriesCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, batteryId);
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources, AttributeFilter attributeFilter) {
        delegate.updateBatteries(networkUuid, batteryResources, attributeFilter);
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            batteriesCache.getCollection(networkUuid, batteryResource.getVariantNum()).updateResource(batteryResource);
        }
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        delegate.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            twoWindingsTransformerCache.getCollection(networkUuid, twoWindingsTransformerResource.getVariantNum()).createResource(twoWindingsTransformerResource);
            addIdentifiableId(networkUuid, twoWindingsTransformerResource);
        }
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return twoWindingsTransformerCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return twoWindingsTransformerCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources, AttributeFilter attributeFilter) {
        delegate.updateTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources, attributeFilter);
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            twoWindingsTransformerCache.getCollection(networkUuid, twoWindingsTransformerResource.getVariantNum()).updateResource(twoWindingsTransformerResource);
        }
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        delegate.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            threeWindingsTransformerCache.getCollection(networkUuid, threeWindingsTransformerResource.getVariantNum()).createResource(threeWindingsTransformerResource);
            addIdentifiableId(networkUuid, threeWindingsTransformerResource);
        }
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        return threeWindingsTransformerCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        return threeWindingsTransformerCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, threeWindingsTransformerId);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources, AttributeFilter attributeFilter) {
        delegate.updateThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources, attributeFilter);
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            threeWindingsTransformerCache.getCollection(networkUuid, threeWindingsTransformerResource.getVariantNum()).updateResource(threeWindingsTransformerResource);
        }
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        delegate.createLines(networkUuid, lineResources);
        for (Resource<LineAttributes> lineResource : lineResources) {
            linesCache.getCollection(networkUuid, lineResource.getVariantNum()).createResource(lineResource);
            addIdentifiableId(networkUuid, lineResource);
        }
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return linesCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return linesCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, lineId);
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources, AttributeFilter attributeFilter) {
        delegate.updateLines(networkUuid, lineResources, attributeFilter);
        for (Resource<LineAttributes> lineResource : lineResources) {
            linesCache.getCollection(networkUuid, lineResource.getVariantNum()).updateResource(lineResource);
        }
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        delegate.createShuntCompensators(networkUuid, shuntCompensatorResources);
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            shuntCompensatorsCache.getCollection(networkUuid, shuntCompensatorResource.getVariantNum()).createResource(shuntCompensatorResource);
            addIdentifiableId(networkUuid, shuntCompensatorResource);
        }
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return shuntCompensatorsCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return shuntCompensatorsCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, shuntCompensatorId);
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources, AttributeFilter attributeFilter) {
        delegate.updateShuntCompensators(networkUuid, shuntCompensatorResources, attributeFilter);
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            shuntCompensatorsCache.getCollection(networkUuid, shuntCompensatorResource.getVariantNum()).updateResource(shuntCompensatorResource);
        }
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        delegate.createVscConverterStations(networkUuid, vscConverterStationResources);
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            vscConverterStationCache.getCollection(networkUuid, vscConverterStationResource.getVariantNum()).createResource(vscConverterStationResource);
            addIdentifiableId(networkUuid, vscConverterStationResource);
        }
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return vscConverterStationCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return vscConverterStationCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, vscConverterStationId);
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources, AttributeFilter attributeFilter) {
        delegate.updateVscConverterStations(networkUuid, vscConverterStationResources, attributeFilter);
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            vscConverterStationCache.getCollection(networkUuid, vscConverterStationResource.getVariantNum()).updateResource(vscConverterStationResource);
        }
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        delegate.createLccConverterStations(networkUuid, lccConverterStationResources);
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            lccConverterStationCache.getCollection(networkUuid, lccConverterStationResource.getVariantNum()).createResource(lccConverterStationResource);
            addIdentifiableId(networkUuid, lccConverterStationResource);
        }
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return lccConverterStationCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return lccConverterStationCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, lccConverterStationId);
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources, AttributeFilter attributeFilter) {
        delegate.updateLccConverterStations(networkUuid, lccConverterStationResources, attributeFilter);
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            lccConverterStationCache.getCollection(networkUuid, lccConverterStationResource.getVariantNum()).updateResource(lccConverterStationResource);
        }
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        delegate.createStaticVarCompensators(networkUuid, svcResources);
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            staticVarCompensatorCache.getCollection(networkUuid, svcResource.getVariantNum()).createResource(svcResource);
            addIdentifiableId(networkUuid, svcResource);
        }
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return staticVarCompensatorCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return staticVarCompensatorCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, staticVarCompensatorId);
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources, AttributeFilter attributeFilter) {
        delegate.updateStaticVarCompensators(networkUuid, svcResources, attributeFilter);
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            staticVarCompensatorCache.getCollection(networkUuid, svcResource.getVariantNum()).updateResource(svcResource);
        }
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        delegate.createHvdcLines(networkUuid, hvdcLineResources);
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            hvdcLinesCache.getCollection(networkUuid, hvdcLineResource.getVariantNum()).createResource(hvdcLineResource);
            addIdentifiableId(networkUuid, hvdcLineResource);
        }
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return hvdcLinesCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return hvdcLinesCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, hvdcLineId);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources, AttributeFilter attributeFilter) {
        delegate.updateHvdcLines(networkUuid, hvdcLineResources, attributeFilter);
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            hvdcLinesCache.getCollection(networkUuid, hvdcLineResource.getVariantNum()).updateResource(hvdcLineResource);
        }
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        delegate.removeHvdcLines(networkUuid, variantNum, hvdcLinesId);
        hvdcLinesCache.getCollection(networkUuid, variantNum).removeResources(hvdcLinesId);
        removeIdentifiableIds(networkUuid, variantNum, hvdcLinesId);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        delegate.createDanglingLines(networkUuid, danglingLineResources);
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            danglingLinesCache.getCollection(networkUuid, danglingLineResource.getVariantNum()).createResource(danglingLineResource);
            addIdentifiableId(networkUuid, danglingLineResource);
        }
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return danglingLinesCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return danglingLinesCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, danglingLineId);
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources, AttributeFilter attributeFilter) {
        delegate.updateDanglingLines(networkUuid, danglingLineResources, attributeFilter);
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            danglingLinesCache.getCollection(networkUuid, danglingLineResource.getVariantNum()).updateResource(danglingLineResource);
        }
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        delegate.removeDanglingLines(networkUuid, variantNum, danglingLinesId);
        danglingLinesCache.getCollection(networkUuid, variantNum).removeResources(danglingLinesId);
        removeIdentifiableIds(networkUuid, variantNum, danglingLinesId);
    }

    @Override
    public void createTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources) {
        delegate.createTieLines(networkUuid, tieLineResources);
        for (Resource<TieLineAttributes> tieLineResource : tieLineResources) {
            tieLinesCache.getCollection(networkUuid, tieLineResource.getVariantNum()).createResource(tieLineResource);
            addIdentifiableId(networkUuid, tieLineResource);
        }
    }

    @Override
    public List<Resource<TieLineAttributes>> getTieLines(UUID networkUuid, int variantNum) {
        return tieLinesCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<TieLineAttributes>> getTieLine(UUID networkUuid, int variantNum, String tieLineId) {
        return tieLinesCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, tieLineId);
    }

    @Override
    public void removeTieLines(UUID networkUuid, int variantNum, List<String> tieLinesId) {
        delegate.removeTieLines(networkUuid, variantNum, tieLinesId);
        tieLinesCache.getCollection(networkUuid, variantNum).removeResources(tieLinesId);
        removeIdentifiableIds(networkUuid, variantNum, tieLinesId);
    }

    @Override
    public void updateTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources, AttributeFilter attributeFilter) {
        delegate.updateTieLines(networkUuid, tieLineResources, attributeFilter);
        for (Resource<TieLineAttributes> tieLineResource : tieLineResources) {
            tieLinesCache.getCollection(networkUuid, tieLineResource.getVariantNum()).updateResource(tieLineResource);
        }
    }

    // Area
    @Override
    public void createAreas(UUID networkUuid, List<Resource<AreaAttributes>> areaResources) {
        delegate.createAreas(networkUuid, areaResources);
        for (Resource<AreaAttributes> areaResource : areaResources) {
            areasCache.getCollection(networkUuid, areaResource.getVariantNum()).createResource(areaResource);
            addIdentifiableId(networkUuid, areaResource);
        }
    }

    @Override
    public List<Resource<AreaAttributes>> getAreas(UUID networkUuid, int variantNum) {
        return areasCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<AreaAttributes>> getArea(UUID networkUuid, int variantNum, String areaId) {
        return areasCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, areaId);
    }

    @Override
    public void removeAreas(UUID networkUuid, int variantNum, List<String> areaIds) {
        delegate.removeAreas(networkUuid, variantNum, areaIds);
        areasCache.getCollection(networkUuid, variantNum).removeResources(areaIds);
        removeIdentifiableIds(networkUuid, variantNum, areaIds);
    }

    @Override
    public void updateAreas(UUID networkUuid, List<Resource<AreaAttributes>> areaResources, AttributeFilter attributeFilter) {
        delegate.updateAreas(networkUuid, areaResources, attributeFilter);
        for (Resource<AreaAttributes> areaResource : areaResources) {
            areasCache.getCollection(networkUuid, areaResource.getVariantNum()).updateResource(areaResource);
        }
    }

    @Override
    public void createGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources) {
        delegate.createGrounds(networkUuid, groundResources);
        for (Resource<GroundAttributes> groundResource : groundResources) {
            groundsCache.getCollection(networkUuid, groundResource.getVariantNum()).createResource(groundResource);
        }
    }

    @Override
    public List<Resource<GroundAttributes>> getGrounds(UUID networkUuid, int variantNum) {
        return groundsCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<GroundAttributes>> getGround(UUID networkUuid, int variantNum, String groundId) {
        return groundsCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, groundId);
    }

    @Override
    public void updateGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundAttributes, AttributeFilter attributeFilter) {
        delegate.updateGrounds(networkUuid, groundAttributes, attributeFilter);
        for (Resource<GroundAttributes> groundResource : groundAttributes) {
            groundsCache.getCollection(networkUuid, groundResource.getVariantNum()).updateResource(groundResource);
        }
    }

    @Override
    public void removeGrounds(UUID networkUuid, int variantNum, List<String> groundsId) {
        delegate.removeGrounds(networkUuid, variantNum, groundsId);
        groundsCache.getCollection(networkUuid, variantNum).removeResources(groundsId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources) {
        delegate.createConfiguredBuses(networkUuid, busResources);
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            configuredBusesCache.getCollection(networkUuid, busResource.getVariantNum()).createResource(busResource);
            addIdentifiableId(networkUuid, busResource);
        }
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return configuredBusesCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return configuredBusesCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return configuredBusesCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, busId);
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources, AttributeFilter attributeFilter) {
        delegate.updateConfiguredBuses(networkUuid, busResources, attributeFilter);
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            configuredBusesCache.getCollection(networkUuid, busResource.getVariantNum()).updateResource(busResource);
        }
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        delegate.removeConfiguredBuses(networkUuid, variantNum, busesId);
        configuredBusesCache.getCollection(networkUuid, variantNum).removeResources(busesId);
        removeIdentifiableIds(networkUuid, variantNum, busesId);
    }

    @Override
    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        return getCache(resourceType).getCollection(networkUuid, variantNum).getExtensionAttributes(networkUuid, variantNum, resourceType, identifiableId, extensionName);
    }

    public void loadAllExtensionsAttributesByResourceTypeAndExtensionName(UUID networkUuid, int variantNum, ResourceType resourceType, String extensionName) {
        getCache(resourceType).getCollection(networkUuid, variantNum).loadAllExtensionsAttributesByResourceTypeAndExtensionName(networkUuid, variantNum, resourceType, extensionName);
    }

    @Override
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId) {
        return getCache(resourceType).getCollection(networkUuid, variantNum).getAllExtensionsAttributesByIdentifiableId(networkUuid, variantNum, resourceType, identifiableId);
    }

    public void loadAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType) {
        getCache(resourceType).getCollection(networkUuid, variantNum).loadAllExtensionsAttributesByResourceType(networkUuid, variantNum, resourceType);
    }

    @Override
    public void removeExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        getCache(resourceType).getCollection(networkUuid, variantNum).removeExtensionAttributesByExtensionName(identifiableId, extensionName);
        delegate.removeExtensionAttributes(networkUuid, variantNum, resourceType, identifiableId, extensionName);
    }

    // limits
    @Override
    public Optional<OperationalLimitsGroupAttributes> getOperationalLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String operationalLimitGroupName, int side) {
        return getCache(resourceType).getCollection(networkUuid, variantNum).getOperationalLimitsAttributes(networkUuid, variantNum, resourceType, identifiableId, operationalLimitGroupName, side);
    }

    @Override
    public Optional<OperationalLimitsGroupAttributes> getSelectedOperationalLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String operationalLimitGroupName, int side) {
        return getCache(resourceType).getCollection(networkUuid, variantNum).getSelectedOperationalLimitsAttributes(networkUuid, variantNum, resourceType, identifiableId, operationalLimitGroupName, side);
    }

    @Override
    public Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes> getAllOperationalLimitsGroupAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType) {
        return getCache(resourceType).getCollection(networkUuid, variantNum).getAllOperationalLimitsGroupAttributesByResourceType(networkUuid, variantNum, resourceType);
    }

    @Override
    public Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes> getAllSelectedOperationalLimitsGroupAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType) {
        return getCache(resourceType).getCollection(networkUuid, variantNum).getAllSelectedOperationalLimitsGroupAttributesByResourceType(networkUuid, variantNum, resourceType);
    }

    private NetworkCollectionIndex<? extends CollectionCache<?>> getCache(ResourceType resourceType) {
        return switch (resourceType) {
            case NETWORK -> networksCache;
            case SUBSTATION -> substationsCache;
            case VOLTAGE_LEVEL -> voltageLevelsCache;
            case SWITCH -> switchesCache;
            case BUSBAR_SECTION -> busbarSectionsCache;
            case LOAD -> loadsCache;
            case GENERATOR -> generatorsCache;
            case BATTERY -> batteriesCache;
            case TWO_WINDINGS_TRANSFORMER -> twoWindingsTransformerCache;
            case THREE_WINDINGS_TRANSFORMER -> threeWindingsTransformerCache;
            case LINE -> linesCache;
            case SHUNT_COMPENSATOR -> shuntCompensatorsCache;
            case VSC_CONVERTER_STATION -> vscConverterStationCache;
            case LCC_CONVERTER_STATION -> lccConverterStationCache;
            case STATIC_VAR_COMPENSATOR -> staticVarCompensatorCache;
            case HVDC_LINE -> hvdcLinesCache;
            case DANGLING_LINE -> danglingLinesCache;
            case CONFIGURED_BUS -> configuredBusesCache;
            case TIE_LINE -> tieLinesCache;
            case GROUND -> groundsCache;
            case AREA -> areasCache;
        };
    }

    private void addIdentifiableId(UUID networkUuid, Resource<?> resource) {
        var p = Pair.of(networkUuid, resource.getVariantNum());
        Set<String> identifiableIds = identifiablesIdsByNetworkVariant.get(p);
        if (identifiableIds != null) {
            identifiableIds.add(resource.getId());
        }
    }

    private void removeIdentifiableIds(UUID networkUuid, int variantNum, List<String> ids) {
        var p = Pair.of(networkUuid, variantNum);
        Set<String> identifiableIds = identifiablesIdsByNetworkVariant.get(p);
        if (identifiableIds != null) {
            identifiableIds.removeAll(ids);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Resource<IdentifiableAttributes>> getIdentifiable(UUID networkUuid, int variantNum, String id) {
        // check if resource is present in one of the caches
        boolean allCollectionsAreFullyLoaded = true;
        for (var cache : networkContainersCaches.values()) {
            var collection = cache.getCollection(networkUuid, variantNum);
            if (collection.isResourceLoaded(id)) {
                return collection.getResource(networkUuid, variantNum, id).map(r -> (Resource<IdentifiableAttributes>) r);
            }
            if (!collection.isFullyLoaded()) {
                allCollectionsAreFullyLoaded = false;
            }
        }

        if (allCollectionsAreFullyLoaded) {
            return Optional.empty();
        }

        // when too many getIdentifiable call go through the cache (and consequently go to the server REST API),
        // we prefer to load all IDs from the server to be able to check that an ID exists on the server before
        // getting it from the server
        var p = Pair.of(networkUuid, variantNum);
        Set<String> identifiablesIds = identifiablesIdsByNetworkVariant.get(p);
        if (identifiablesIds == null && identifiableCallCountByNetworkVariant.getOrDefault(p, new MutableInt()).getValue() > MAX_GET_IDENTIFIABLE_CALL_COUNT) {
            identifiablesIds = new HashSet<>(delegate.getIdentifiablesIds(networkUuid, variantNum));
            identifiablesIdsByNetworkVariant.put(p, identifiablesIds);
        }

        if (identifiablesIds != null && !identifiablesIds.contains(id)) {
            return Optional.empty();
        }

        // if not in one of the caches, get resource from delegate and if present add in corresponding cache
        Optional<Resource<IdentifiableAttributes>> resource = delegate.getIdentifiable(networkUuid, variantNum, id);
        resource.ifPresent(r -> {
            CollectionCache<IdentifiableAttributes> collection = (CollectionCache<IdentifiableAttributes>) networkContainersCaches.get(r.getType()).getCollection(networkUuid, variantNum);
            // we already checked that the resource is not in the cache so we can directly put it in the cache
            collection.addOrReplaceResource(r);
        });

        identifiableCallCountByNetworkVariant.computeIfAbsent(p, k -> new MutableInt())
                .increment();

        return resource;
    }
}
