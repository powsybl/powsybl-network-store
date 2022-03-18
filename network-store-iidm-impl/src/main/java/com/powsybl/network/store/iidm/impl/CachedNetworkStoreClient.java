/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CachedNetworkStoreClient extends AbstractForwardingNetworkStoreClient implements NetworkStoreClient {

    private final Map<UUID, List<VariantInfos>> variantsInfosByNetworkUuid = new HashMap<>();

    private final NetworkCollectionIndex<CollectionCache<NetworkAttributes>> networksCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        (networkUuid, variantNum, id) -> delegate.getNetwork(networkUuid, variantNum),
        null,
        (networkUuid, variantNum) -> delegate.getNetwork(networkUuid, variantNum).stream().collect(Collectors.toList())));

    private final NetworkCollectionIndex<CollectionCache<SubstationAttributes>> substationsCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getSubstation,
        null,
        delegate::getSubstations));

    private final NetworkCollectionIndex<CollectionCache<VoltageLevelAttributes>> voltageLevelsCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getVoltageLevel,
        delegate::getVoltageLevelsInSubstation,
        delegate::getVoltageLevels));

    private final NetworkCollectionIndex<CollectionCache<SwitchAttributes>> switchesCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getSwitch,
        delegate::getVoltageLevelSwitches,
        delegate::getSwitches));

    private final NetworkCollectionIndex<CollectionCache<BusbarSectionAttributes>> busbarSectionsCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getBusbarSection,
        delegate::getVoltageLevelBusbarSections,
        delegate::getBusbarSections));

    private final NetworkCollectionIndex<CollectionCache<LoadAttributes>> loadsCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getLoad,
        delegate::getVoltageLevelLoads,
        delegate::getLoads));

    private final NetworkCollectionIndex<CollectionCache<GeneratorAttributes>> generatorsCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getGenerator,
        delegate::getVoltageLevelGenerators,
        delegate::getGenerators));

    private final NetworkCollectionIndex<CollectionCache<BatteryAttributes>> batteriesCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getBattery,
        delegate::getVoltageLevelBatteries,
        delegate::getBatteries));

    private final NetworkCollectionIndex<CollectionCache<TwoWindingsTransformerAttributes>> twoWindingsTransformerCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getTwoWindingsTransformer,
        delegate::getVoltageLevelTwoWindingsTransformers,
        delegate::getTwoWindingsTransformers));

    private final NetworkCollectionIndex<CollectionCache<ThreeWindingsTransformerAttributes>> threeWindingsTranqformerCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getThreeWindingsTransformer,
        delegate::getVoltageLevelThreeWindingsTransformers,
        delegate::getThreeWindingsTransformers));

    private final NetworkCollectionIndex<CollectionCache<LineAttributes>> linesCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getLine,
        delegate::getVoltageLevelLines,
        delegate::getLines));

    private final NetworkCollectionIndex<CollectionCache<ShuntCompensatorAttributes>> shuntCompensatorsCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getShuntCompensator,
        delegate::getVoltageLevelShuntCompensators,
        delegate::getShuntCompensators));

    private final NetworkCollectionIndex<CollectionCache<VscConverterStationAttributes>> vscConverterStationCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getVscConverterStation,
        delegate::getVoltageLevelVscConverterStations,
        delegate::getVscConverterStations));

    private final NetworkCollectionIndex<CollectionCache<LccConverterStationAttributes>> lccConverterStationCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getLccConverterStation,
        delegate::getVoltageLevelLccConverterStations,
        delegate::getLccConverterStations));

    private final NetworkCollectionIndex<CollectionCache<StaticVarCompensatorAttributes>> staticVarCompensatorCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getStaticVarCompensator,
        delegate::getVoltageLevelStaticVarCompensators,
        delegate::getStaticVarCompensators));

    private final NetworkCollectionIndex<CollectionCache<HvdcLineAttributes>> hvdcLinesCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getHvdcLine,
        null,
        delegate::getHvdcLines));

    private final NetworkCollectionIndex<CollectionCache<DanglingLineAttributes>> danglingLinesCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getDanglingLine,
        delegate::getVoltageLevelDanglingLines,
        delegate::getDanglingLines));

    private final NetworkCollectionIndex<CollectionCache<ConfiguredBusAttributes>> configuredBusesCache = new NetworkCollectionIndex<>(() -> new CollectionCache<>(
        delegate::getConfiguredBus,
        delegate::getVoltageLevelConfiguredBuses,
        delegate::getConfiguredBuses));

    private final List<NetworkCollectionIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> voltageLevelContainersCaches = List.of(
            switchesCache,
            busbarSectionsCache,
            loadsCache,
            generatorsCache,
            batteriesCache,
            twoWindingsTransformerCache,
            threeWindingsTranqformerCache,
            linesCache,
            shuntCompensatorsCache,
            vscConverterStationCache,
            lccConverterStationCache,
            staticVarCompensatorCache,
            hvdcLinesCache,
            danglingLinesCache,
            configuredBusesCache
    );

    private final List<NetworkCollectionIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> networkContainersCaches = ImmutableList.<NetworkCollectionIndex<? extends CollectionCache<? extends IdentifiableAttributes>>>builder()
            .add(substationsCache)
            .add(voltageLevelsCache)
            .addAll(voltageLevelContainersCaches)
            .build();

    public CachedNetworkStoreClient(NetworkStoreClient delegate) {
        super(delegate);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        delegate.createNetworks(networkResources);
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            UUID networkUuid = networkResource.getAttributes().getUuid();
            int variantNum = networkResource.getVariantNum();
            networksCache.getCollection(networkUuid, variantNum).createResource(networkResource);

            // initialize network sub-collection cache to set to fully loaded
            networkContainersCaches.forEach(cache -> cache.getCollection(networkUuid, networkResource.getVariantNum()).init());

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
        networkContainersCaches.forEach(cache -> cache.removeCollection(networkUuid));
        variantsInfosByNetworkUuid.remove(networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        delegate.deleteNetwork(networkUuid, variantNum);
        networksCache.removeCollection(networkUuid, variantNum);
        networkContainersCaches.forEach(cache -> cache.removeCollection(networkUuid, variantNum));
        List<VariantInfos> variantsInfos = variantsInfosByNetworkUuid.get(networkUuid);
        if (variantsInfos != null) {
            variantsInfos.removeIf(infos -> infos.getNum() == variantNum);
        }
    }

    @Override
    public void updateNetworks(List<Resource<NetworkAttributes>> networkResources) {
        delegate.updateNetworks(networkResources);
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

        var objectMapper = JsonUtil.createObjectMapper();
        objectMapper.registerModule(new JodaModule());

        //TODO THIS IS WRONG, if an object has been created only the new variant, we must create it
        // clone each collection and re-assign variant number and id
        cloneCollection(switchesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(busbarSectionsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(loadsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(generatorsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(batteriesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(twoWindingsTransformerCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(threeWindingsTranqformerCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(linesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(shuntCompensatorsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(vscConverterStationCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(lccConverterStationCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(staticVarCompensatorCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(hvdcLinesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(danglingLinesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(configuredBusesCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(substationsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(voltageLevelsCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneCollection(networksCache, networkUuid, sourceVariantNum, targetVariantNum, objectMapper,
            networkResource -> networkResource.getAttributes().setVariantId(targetVariantId));

        variantsInfosByNetworkUuid.computeIfAbsent(networkUuid, k -> new ArrayList<>())
                .add(new VariantInfos(targetVariantId, targetVariantNum));
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        delegate.createSubstations(networkUuid, substationResources);

        for (Resource<SubstationAttributes> substationResource : substationResources) {
            substationsCache.getCollection(networkUuid, substationResource.getVariantNum()).createResource(substationResource);

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
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        delegate.updateSubstations(networkUuid, substationResources);
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            substationsCache.getCollection(networkUuid, substationResource.getVariantNum()).updateResource(substationResource);
        }
    }

    @Override
    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        delegate.removeSubstations(networkUuid, variantNum, substationsId);
        substationsCache.getCollection(networkUuid, variantNum).removeResources(substationsId);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        delegate.createVoltageLevels(networkUuid, voltageLevelResources);
        for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
            voltageLevelsCache.getCollection(networkUuid, voltageLevelResource.getVariantNum()).createResource(voltageLevelResource);
        }

        // initialize voltage level sub-collection cache to set to fully loaded
        voltageLevelContainersCaches.forEach(cache -> {
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
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        delegate.updateVoltageLevels(networkUuid, voltageLevelResources);
        for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
            voltageLevelsCache.getCollection(networkUuid, voltageLevelResource.getVariantNum()).updateResource(voltageLevelResource);
        }
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        delegate.removeVoltageLevels(networkUuid, variantNum, voltageLevelsId);
        voltageLevelsCache.getCollection(networkUuid, variantNum).removeResources(voltageLevelsId);
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
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return batteriesCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        delegate.removeBatteries(networkUuid, variantNum, batteriesId);
        batteriesCache.getCollection(networkUuid, variantNum).removeResources(batteriesId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return loadsCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        delegate.removeLoads(networkUuid, variantNum, loadsId);
        loadsCache.getCollection(networkUuid, variantNum).removeResources(loadsId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return shuntCompensatorsCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        delegate.removeShuntCompensators(networkUuid, variantNum, shuntCompensatorsId);
        shuntCompensatorsCache.getCollection(networkUuid, variantNum).removeResources(shuntCompensatorsId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return staticVarCompensatorCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        delegate.removeStaticVarCompensators(networkUuid, variantNum, staticVarCompensatorsId);
        staticVarCompensatorCache.getCollection(networkUuid, variantNum).removeResources(staticVarCompensatorsId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return vscConverterStationCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        delegate.removeVscConverterStations(networkUuid, variantNum, vscConverterStationsId);
        vscConverterStationCache.getCollection(networkUuid, variantNum).removeResources(vscConverterStationsId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return lccConverterStationCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        delegate.removeLccConverterStations(networkUuid, variantNum, lccConverterStationsId);
        lccConverterStationCache.getCollection(networkUuid, variantNum).removeResources(lccConverterStationsId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return twoWindingsTransformerCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        delegate.removeTwoWindingsTransformers(networkUuid, variantNum, twoWindingsTransformersId);
        twoWindingsTransformerCache.getCollection(networkUuid, variantNum).removeResources(twoWindingsTransformersId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return threeWindingsTranqformerCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        delegate.removeThreeWindingsTransformers(networkUuid, variantNum, threeWindingsTransformersId);
        threeWindingsTranqformerCache.getCollection(networkUuid, variantNum).removeResources(threeWindingsTransformersId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return linesCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        delegate.removeLines(networkUuid, variantNum, linesId);
        linesCache.getCollection(networkUuid, variantNum).removeResources(linesId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return danglingLinesCache.getCollection(networkUuid, variantNum).getContainerResources(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        delegate.createSwitches(networkUuid, switchResources);
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            switchesCache.getCollection(networkUuid, switchResource.getVariantNum()).createResource(switchResource);
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
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        delegate.updateSwitches(networkUuid, switchResources);
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            switchesCache.getCollection(networkUuid, switchResource.getVariantNum()).updateResource(switchResource);
        }
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        delegate.removeSwitches(networkUuid, variantNum, switchesId);
        switchesCache.getCollection(networkUuid, variantNum).removeResources(switchesId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        delegate.createBusbarSections(networkUuid, busbarSectionResources);
        for (Resource<BusbarSectionAttributes>  busbarSectionResource : busbarSectionResources) {
            busbarSectionsCache.getCollection(networkUuid, busbarSectionResource.getVariantNum()).createResource(busbarSectionResource);
        }
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busbarSectionsId) {
        delegate.removeBusBarSections(networkUuid, variantNum, busbarSectionsId);
        busbarSectionsCache.getCollection(networkUuid, variantNum).removeResources(busbarSectionsId);
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
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        delegate.updateBusbarSections(networkUuid, busbarSectionResources);
        for (Resource<BusbarSectionAttributes>  busbarSectionResource : busbarSectionResources) {
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
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        delegate.updateLoads(networkUuid, loadResources);
        for (Resource<LoadAttributes> loadResource : loadResources) {
            loadsCache.getCollection(networkUuid, loadResource.getVariantNum()).updateResource(loadResource);
        }
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        delegate.createGenerators(networkUuid, generatorResources);
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            generatorsCache.getCollection(networkUuid, generatorResource.getVariantNum()).createResource(generatorResource);
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
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        delegate.updateGenerators(networkUuid, generatorResources);
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            generatorsCache.getCollection(networkUuid, generatorResource.getVariantNum()).updateResource(generatorResource);
        }
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        delegate.createBatteries(networkUuid, batteryResources);
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            batteriesCache.getCollection(networkUuid, batteryResource.getVariantNum()).createResource(batteryResource);
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
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        delegate.updateBatteries(networkUuid, batteryResources);
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            batteriesCache.getCollection(networkUuid, batteryResource.getVariantNum()).updateResource(batteryResource);
        }
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        delegate.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            twoWindingsTransformerCache.getCollection(networkUuid, twoWindingsTransformerResource.getVariantNum()).createResource(twoWindingsTransformerResource);
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
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        delegate.updateTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            twoWindingsTransformerCache.getCollection(networkUuid, twoWindingsTransformerResource.getVariantNum()).updateResource(twoWindingsTransformerResource);
        }
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        delegate.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            threeWindingsTranqformerCache.getCollection(networkUuid, threeWindingsTransformerResource.getVariantNum()).createResource(threeWindingsTransformerResource);
        }
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        return threeWindingsTranqformerCache.getCollection(networkUuid, variantNum).getResources(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        return threeWindingsTranqformerCache.getCollection(networkUuid, variantNum).getResource(networkUuid, variantNum, threeWindingsTransformerId);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        delegate.updateThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            threeWindingsTranqformerCache.getCollection(networkUuid, threeWindingsTransformerResource.getVariantNum()).updateResource(threeWindingsTransformerResource);
        }
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        delegate.createLines(networkUuid, lineResources);
        for (Resource<LineAttributes> lineResource : lineResources) {
            linesCache.getCollection(networkUuid, lineResource.getVariantNum()).createResource(lineResource);
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
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        delegate.updateLines(networkUuid, lineResources);
        for (Resource<LineAttributes> lineResource : lineResources) {
            linesCache.getCollection(networkUuid, lineResource.getVariantNum()).updateResource(lineResource);
        }
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        delegate.createShuntCompensators(networkUuid, shuntCompensatorResources);
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            shuntCompensatorsCache.getCollection(networkUuid, shuntCompensatorResource.getVariantNum()).createResource(shuntCompensatorResource);
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
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        delegate.updateShuntCompensators(networkUuid, shuntCompensatorResources);
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            shuntCompensatorsCache.getCollection(networkUuid, shuntCompensatorResource.getVariantNum()).updateResource(shuntCompensatorResource);
        }
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        delegate.createVscConverterStations(networkUuid, vscConverterStationResources);
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            vscConverterStationCache.getCollection(networkUuid, vscConverterStationResource.getVariantNum()).createResource(vscConverterStationResource);
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
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        delegate.updateVscConverterStations(networkUuid, vscConverterStationResources);
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            vscConverterStationCache.getCollection(networkUuid, vscConverterStationResource.getVariantNum()).updateResource(vscConverterStationResource);
        }
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        delegate.createLccConverterStations(networkUuid, lccConverterStationResources);
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            lccConverterStationCache.getCollection(networkUuid, lccConverterStationResource.getVariantNum()).createResource(lccConverterStationResource);
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
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        delegate.updateLccConverterStations(networkUuid, lccConverterStationResources);
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            lccConverterStationCache.getCollection(networkUuid, lccConverterStationResource.getVariantNum()).updateResource(lccConverterStationResource);
        }
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        delegate.createStaticVarCompensators(networkUuid, svcResources);
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            staticVarCompensatorCache.getCollection(networkUuid, svcResource.getVariantNum()).createResource(svcResource);
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
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        delegate.updateStaticVarCompensators(networkUuid, svcResources);
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            staticVarCompensatorCache.getCollection(networkUuid, svcResource.getVariantNum()).updateResource(svcResource);
        }
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        delegate.createHvdcLines(networkUuid, hvdcLineResources);
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            hvdcLinesCache.getCollection(networkUuid, hvdcLineResource.getVariantNum()).createResource(hvdcLineResource);
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
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        delegate.updateHvdcLines(networkUuid, hvdcLineResources);
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            hvdcLinesCache.getCollection(networkUuid, hvdcLineResource.getVariantNum()).updateResource(hvdcLineResource);
        }
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        delegate.removeHvdcLines(networkUuid, variantNum, hvdcLinesId);
        hvdcLinesCache.getCollection(networkUuid, variantNum).removeResources(hvdcLinesId);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        delegate.createDanglingLines(networkUuid, danglingLineResources);
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            danglingLinesCache.getCollection(networkUuid, danglingLineResource.getVariantNum()).createResource(danglingLineResource);
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
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        delegate.updateDanglingLines(networkUuid, danglingLineResources);
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            danglingLinesCache.getCollection(networkUuid, danglingLineResource.getVariantNum()).updateResource(danglingLineResource);
        }
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        delegate.removeDanglingLines(networkUuid, variantNum, danglingLinesId);
        danglingLinesCache.getCollection(networkUuid, variantNum).removeResources(danglingLinesId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources) {
        delegate.createConfiguredBuses(networkUuid, busResources);
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            configuredBusesCache.getCollection(networkUuid, busResource.getVariantNum()).createResource(busResource);
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
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources) {
        delegate.updateConfiguredBuses(networkUuid, busResources);
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            configuredBusesCache.getCollection(networkUuid, busResource.getVariantNum()).updateResource(busResource);
        }
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        delegate.removeConfiguredBuses(networkUuid, variantNum, busesId);
        configuredBusesCache.getCollection(networkUuid, variantNum).removeResources(busesId);
    }
}
