/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CachedNetworkStoreClient extends ForwardingNetworkStoreClient implements NetworkStoreClient {

    private final Map<UUID, Resource<NetworkAttributes>> networkResources = new HashMap<>();

    private boolean networksFullyLoaded = false;

    private final NetworkCollectionIndex<CollectionCache<SubstationAttributes>> substationsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) -> delegate.getSubstation(networkUuid, variantNum, id),
        null,
        (variantNum) -> delegate.getSubstations(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<VoltageLevelAttributes>> voltageLevelsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getVoltageLevel(networkUuid, variantNum, id),
        (variantNum, substationId) -> delegate.getVoltageLevelsInSubstation(networkUuid, variantNum, substationId),
        (variantNum) -> delegate.getVoltageLevels(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<SwitchAttributes>> switchesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getSwitch(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelSwitches(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getSwitches(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<BusbarSectionAttributes>> busbarSectionsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getBusbarSection(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelBusbarSections(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getBusbarSections(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<LoadAttributes>> loadsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getLoad(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelLoads(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getLoads(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<GeneratorAttributes>> generatorsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getGenerator(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelGenerators(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getGenerators(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<BatteryAttributes>> batteriesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getBattery(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelBatteries(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getBatteries(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<TwoWindingsTransformerAttributes>> twoWindingsTransformerCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getTwoWindingsTransformer(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getTwoWindingsTransformers(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<ThreeWindingsTransformerAttributes>> threeWindingsTranqformerCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getThreeWindingsTransformer(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getThreeWindingsTransformers(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<LineAttributes>> linesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getLine(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelLines(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getLines(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<ShuntCompensatorAttributes>> shuntCompensatorsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getShuntCompensator(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelShuntCompensators(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getShuntCompensators(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<VscConverterStationAttributes>> vscConverterStationCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getVscConverterStation(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelVscConverterStations(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getVscConverterStations(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<LccConverterStationAttributes>> lccConverterStationCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getLccConverterStation(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelLccConverterStations(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getLccConverterStations(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<StaticVarCompensatorAttributes>> staticVarCompensatorCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getStaticVarCompensator(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelStaticVarCompensators(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getStaticVarCompensators(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<HvdcLineAttributes>> hvdcLinesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getHvdcLine(networkUuid, variantNum, id),
        null,
        (variantNum) -> delegate.getHvdcLines(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<DanglingLineAttributes>> danglingLinesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getDanglingLine(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelDanglingLines(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getDanglingLines(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionCache<ConfiguredBusAttributes>> configuredBusesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        (variantNum, id) ->  delegate.getConfiguredBus(networkUuid, variantNum, id),
        (variantNum, voltageLevelId) -> delegate.getVoltageLevelConfiguredBuses(networkUuid, variantNum, voltageLevelId),
        (variantNum) -> delegate.getConfiguredBuses(networkUuid, variantNum)));

    private final List<NetworkCollectionIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> networkContainersCache = Arrays.asList(
            substationsCache,
            voltageLevelsCache,
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

    private final List<NetworkCollectionIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> voltageLevelContainersCache = Arrays.asList(
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

    public CachedNetworkStoreClient(NetworkStoreClient delegate) {
        super(delegate);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        delegate.createNetworks(networkResources);
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            UUID networkUuid = networkResource.getAttributes().getUuid();
            int variantNum = networkResource.getAttributes().getVariantNum();

            this.networkResources.put(networkUuid, variantNum, networkResource);

            // initialize network sub-collection cache to set to fully loaded
            networkContainersCache.forEach(cache -> cache.getCollection(networkUuid).init(variantNum));
        }
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks(int variantNum) {
        if (!networksFullyLoaded) {
            for (Resource<NetworkAttributes> network : delegate.getNetworks(variantNum)) {
                networkResources.put(network.getAttributes().getUuid(), network);
            }
            networksFullyLoaded = true;
        }
        return new ArrayList<>(networkResources.values());
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum) {
        Resource<NetworkAttributes> networkResource = networkResources.get(networkUuid);
        if (networkResource == null) {
            networkResource = delegate.getNetwork(networkUuid, variantNum).orElse(null);
            networkResources.put(networkUuid, variantNum, networkResource);
        }
        return Optional.ofNullable(networkResource);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        delegate.deleteNetwork(networkUuid);
        networkResources.remove(networkUuid);
        networkContainersCache.forEach(cache -> cache.removeCollection(networkUuid));
    }

    @Override
    public void updateNetwork(UUID networkUuid, int variantNum, Resource<NetworkAttributes> networkResource) {
        delegate.updateNetwork(networkUuid,  variantNum, networkResource);
        networkResources.put(networkResource.getAttributes().getUuid(), networkResource);
    }

    @Override
    public void createSubstations(UUID networkUuid, int variantNum, List<Resource<SubstationAttributes>> substationResources) {
        delegate.createSubstations(networkUuid,  variantNum, substationResources);
        substationsCache.getCollection(networkUuid).createResources(variantNum, substationResources);

        // initialize voltage level cache to set to fully loaded
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            voltageLevelsCache.getCollection(networkUuid).initContainer(variantNum, substationResource.getId());
        }
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return substationsCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return substationsCache.getCollection(networkUuid).getResource(variantNum, substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid, int variantNum) {
        return substationsCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void removeSubstation(UUID networkUuid, int variantNum, String substationId) {
        delegate.removeSubstation(networkUuid,  variantNum, substationId);
        substationsCache.getCollection(networkUuid).removeResource(variantNum, substationId);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        delegate.createVoltageLevels(networkUuid,  variantNum, voltageLevelResources);
        voltageLevelsCache.getCollection(networkUuid).createResources(variantNum, voltageLevelResources);

        // initialize voltage level sub-collection cache to set to fully loaded
        voltageLevelContainersCache.forEach(cache -> {
            for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
                cache.getCollection(networkUuid).initContainer(voltageLevelResource.getId());
            }
        });
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return voltageLevelsCache.getCollection(networkUuid).getResource(variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return voltageLevelsCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, int variantNum, Resource<VoltageLevelAttributes> voltageLevelResource) {
        delegate.updateVoltageLevel(networkUuid,  variantNum, voltageLevelResource);
        voltageLevelsCache.getCollection(networkUuid).updateResource(variantNum, voltageLevelResource);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid, int variantNum) {
        return voltageLevelsCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void removeVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        delegate.removeVoltageLevel(networkUuid,  variantNum, voltageLevelId);
        voltageLevelsCache.getCollection(networkUuid).removeResource(variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        return voltageLevelsCache.getCollection(networkUuid).getContainerResources(variantNum, substationId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return generatorsCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeGenerator(UUID networkUuid, int variantNum, String generatorId) {
        delegate.removeGenerator(networkUuid,  variantNum, generatorId);
        generatorsCache.getCollection(networkUuid).removeResource(variantNum, generatorId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return batteriesCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeBattery(UUID networkUuid, int variantNum, String batteryId) {
        delegate.removeBattery(networkUuid,  variantNum, batteryId);
        batteriesCache.getCollection(networkUuid).removeResource(variantNum, batteryId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return loadsCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeLoad(UUID networkUuid, int variantNum, String loadId) {
        delegate.removeLoad(networkUuid,  variantNum, loadId);
        loadsCache.getCollection(networkUuid).removeResource(variantNum, loadId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return shuntCompensatorsCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        delegate.removeShuntCompensator(networkUuid,  variantNum, shuntCompensatorId);
        shuntCompensatorsCache.getCollection(networkUuid).removeResource(variantNum, shuntCompensatorId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return staticVarCompensatorCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        delegate.removeStaticVarCompensator(networkUuid,  variantNum, staticVarCompensatorId);
        staticVarCompensatorCache.getCollection(networkUuid).removeResource(variantNum, staticVarCompensatorId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return vscConverterStationCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        delegate.removeVscConverterStation(networkUuid,  variantNum, vscConverterStationId);
        vscConverterStationCache.getCollection(networkUuid).removeResource(variantNum, vscConverterStationId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return lccConverterStationCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        delegate.removeLccConverterStation(networkUuid,  variantNum, lccConverterStationId);
        lccConverterStationCache.getCollection(networkUuid).removeResource(variantNum, lccConverterStationId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return twoWindingsTransformerCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        delegate.removeTwoWindingsTransformer(networkUuid,  variantNum, twoWindingsTransformerId);
        twoWindingsTransformerCache.getCollection(networkUuid).removeResource(variantNum, twoWindingsTransformerId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return threeWindingsTranqformerCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        delegate.removeThreeWindingsTransformer(networkUuid,  variantNum, threeWindingsTransformerId);
        threeWindingsTranqformerCache.getCollection(networkUuid).removeResource(variantNum, threeWindingsTransformerId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return linesCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void removeLine(UUID networkUuid, int variantNum, String lineId) {
        delegate.removeLine(networkUuid,  variantNum, lineId);
        linesCache.getCollection(networkUuid).removeResource(variantNum, lineId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return danglingLinesCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources) {
        delegate.createSwitches(networkUuid,  variantNum, switchResources);
        switchesCache.getCollection(networkUuid).createResources(variantNum, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return switchesCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return switchesCache.getCollection(networkUuid).getResource(variantNum, switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid, int variantNum) {
        return switchesCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return switchesCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void updateSwitch(UUID networkUuid, int variantNum, Resource<SwitchAttributes> switchResource) {
        delegate.updateSwitch(networkUuid,  variantNum, switchResource);
        switchesCache.getCollection(networkUuid).updateResource(variantNum, switchResource);
    }

    @Override
    public void removeSwitch(UUID networkUuid, int variantNum, String switchId) {
        delegate.removeSwitch(networkUuid,  variantNum, switchId);
        switchesCache.getCollection(networkUuid).removeResource(variantNum, switchId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, int variantNum, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        delegate.createBusbarSections(networkUuid,  variantNum, busbarSectionResources);
        busbarSectionsCache.getCollection(networkUuid).createResources(variantNum, busbarSectionResources);
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        delegate.removeBusBarSection(networkUuid,  variantNum, busbarSectionId);
        busbarSectionsCache.getCollection(networkUuid).removeResource(variantNum, busbarSectionId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return busbarSectionsCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return busbarSectionsCache.getCollection(networkUuid).getResource(variantNum, busbarSectionId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return busbarSectionsCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public void createLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources) {
        delegate.createLoads(networkUuid,  variantNum, loadResources);
        loadsCache.getCollection(networkUuid).createResources(variantNum, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return loadsCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return loadsCache.getCollection(networkUuid).getResource(variantNum, loadId);
    }

    @Override
    public void updateLoad(UUID networkUuid, int variantNum, Resource<LoadAttributes> loadResource) {
        delegate.updateLoad(networkUuid,  variantNum, loadResource);
        loadsCache.getCollection(networkUuid).updateResource(variantNum, loadResource);
    }

    @Override
    public int getLoadCount(UUID networkUuid, int variantNum) {
        return loadsCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources) {
        delegate.createGenerators(networkUuid,  variantNum, generatorResources);
        generatorsCache.getCollection(networkUuid).createResources(variantNum, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return generatorsCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return generatorsCache.getCollection(networkUuid).getResource(variantNum, generatorId);
    }

    @Override
    public void updateGenerator(UUID networkUuid, int variantNum, Resource<GeneratorAttributes> generatorResource) {
        delegate.updateGenerator(networkUuid,  variantNum, generatorResource);
        generatorsCache.getCollection(networkUuid).updateResource(variantNum, generatorResource);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid, int variantNum) {
        return generatorsCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createBatteries(UUID networkUuid, int variantNum, List<Resource<BatteryAttributes>> batteryResources) {
        delegate.createBatteries(networkUuid,  variantNum, batteryResources);
        batteriesCache.getCollection(networkUuid).createResources(variantNum, batteryResources);
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return batteriesCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return batteriesCache.getCollection(networkUuid).getResource(variantNum, batteryId);
    }

    @Override
    public void updateBattery(UUID networkUuid, int variantNum, Resource<BatteryAttributes> batteryResource) {
        delegate.updateBattery(networkUuid,  variantNum, batteryResource);
        batteriesCache.getCollection(networkUuid).updateResource(variantNum, batteryResource);
    }

    @Override
    public int getBatteryCount(UUID networkUuid, int variantNum) {
        return batteriesCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        delegate.createTwoWindingsTransformers(networkUuid,  variantNum, twoWindingsTransformerResources);
        twoWindingsTransformerCache.getCollection(networkUuid).createResources(variantNum, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return twoWindingsTransformerCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return twoWindingsTransformerCache.getCollection(networkUuid).getResource(variantNum, twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, int variantNum, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        delegate.updateTwoWindingsTransformer(networkUuid,  variantNum, twoWindingsTransformerResource);
        twoWindingsTransformerCache.getCollection(networkUuid).updateResource(variantNum, twoWindingsTransformerResource);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid, int variantNum) {
        return twoWindingsTransformerCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        delegate.createThreeWindingsTransformers(networkUuid,  variantNum, threeWindingsTransformerResources);
        threeWindingsTranqformerCache.getCollection(networkUuid).createResources(variantNum, threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        return threeWindingsTranqformerCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        return threeWindingsTranqformerCache.getCollection(networkUuid).getResource(variantNum, threeWindingsTransformerId);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, int variantNum, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        delegate.updateThreeWindingsTransformer(networkUuid,  variantNum, threeWindingsTransformerResource);
        threeWindingsTranqformerCache.getCollection(networkUuid).updateResource(variantNum, threeWindingsTransformerResource);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid, int variantNum) {
        return threeWindingsTranqformerCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResources) {
        delegate.createLines(networkUuid,  variantNum, lineResources);
        linesCache.getCollection(networkUuid).createResources(variantNum, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return linesCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return linesCache.getCollection(networkUuid).getResource(variantNum, lineId);
    }

    @Override
    public void updateLine(UUID networkUuid, int variantNum, Resource<LineAttributes> lineResource) {
        delegate.updateLine(networkUuid,  variantNum, lineResource);
        linesCache.getCollection(networkUuid).updateResource(variantNum, lineResource);
    }

    @Override
    public int getLineCount(UUID networkUuid, int variantNum) {
        return linesCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        delegate.createShuntCompensators(networkUuid,  variantNum, shuntCompensatorResources);
        shuntCompensatorsCache.getCollection(networkUuid).createResources(variantNum, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return shuntCompensatorsCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return shuntCompensatorsCache.getCollection(networkUuid).getResource(variantNum, shuntCompensatorId);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, int variantNum, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        delegate.updateShuntCompensator(networkUuid,  variantNum, shuntCompensatorResource);
        shuntCompensatorsCache.getCollection(networkUuid).updateResource(variantNum, shuntCompensatorResource);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid, int variantNum) {
        return shuntCompensatorsCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        delegate.createVscConverterStations(networkUuid,  variantNum, vscConverterStationResources);
        vscConverterStationCache.getCollection(networkUuid).createResources(variantNum, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return vscConverterStationCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return vscConverterStationCache.getCollection(networkUuid).getResource(variantNum, vscConverterStationId);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, int variantNum, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        delegate.updateVscConverterStation(networkUuid,  variantNum, vscConverterStationResource);
        vscConverterStationCache.getCollection(networkUuid).updateResource(variantNum, vscConverterStationResource);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid, int variantNum) {
        return vscConverterStationCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        delegate.createLccConverterStations(networkUuid,  variantNum, lccConverterStationResources);
        lccConverterStationCache.getCollection(networkUuid).createResources(variantNum, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return lccConverterStationCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return lccConverterStationCache.getCollection(networkUuid).getResource(variantNum, lccConverterStationId);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, int variantNum, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        delegate.updateLccConverterStation(networkUuid,  variantNum, lccConverterStationResource);
        lccConverterStationCache.getCollection(networkUuid).updateResource(variantNum, lccConverterStationResource);
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid, int variantNum) {
        return lccConverterStationCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        delegate.createStaticVarCompensators(networkUuid,  variantNum, svcResources);
        staticVarCompensatorCache.getCollection(networkUuid).createResources(variantNum, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return staticVarCompensatorCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return staticVarCompensatorCache.getCollection(networkUuid).getResource(variantNum, staticVarCompensatorId);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, int variantNum, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        delegate.updateStaticVarCompensator(networkUuid,  variantNum, staticVarCompensatorResource);
        staticVarCompensatorCache.getCollection(networkUuid).updateResource(variantNum, staticVarCompensatorResource);
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid, int variantNum) {
        return staticVarCompensatorCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        delegate.createHvdcLines(networkUuid,  variantNum, hvdcLineResources);
        hvdcLinesCache.getCollection(networkUuid).createResources(variantNum, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return hvdcLinesCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return hvdcLinesCache.getCollection(networkUuid).getResource(variantNum, hvdcLineId);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, int variantNum, Resource<HvdcLineAttributes> hvdcLineResource) {
        delegate.updateHvdcLine(networkUuid,  variantNum, hvdcLineResource);
        hvdcLinesCache.getCollection(networkUuid).updateResource(variantNum, hvdcLineResource);
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid, int variantNum) {
        return hvdcLinesCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void removeHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        delegate.removeHvdcLine(networkUuid,  variantNum, hvdcLineId);
        hvdcLinesCache.getCollection(networkUuid).removeResource(variantNum, hvdcLineId);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        delegate.createDanglingLines(networkUuid,  variantNum, danglingLineResources);
        danglingLinesCache.getCollection(networkUuid).createResources(variantNum, danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return danglingLinesCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return danglingLinesCache.getCollection(networkUuid).getResource(variantNum, danglingLineId);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, int variantNum, Resource<DanglingLineAttributes> danglingLineResource) {
        delegate.updateDanglingLine(networkUuid,  variantNum, danglingLineResource);
        danglingLinesCache.getCollection(networkUuid).updateResource(variantNum, danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        delegate.removeDanglingLine(networkUuid,  variantNum, danglingLineId);
        danglingLinesCache.getCollection(networkUuid).removeResource(variantNum, danglingLineId);
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid, int variantNum) {
        return danglingLinesCache.getCollection(networkUuid).getResourceCount(variantNum);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        delegate.createConfiguredBuses(networkUuid,  variantNum, busesRessources);
        configuredBusesCache.getCollection(networkUuid).createResources(variantNum, busesRessources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return configuredBusesCache.getCollection(networkUuid).getResources(variantNum) ;
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return configuredBusesCache.getCollection(networkUuid).getContainerResources(variantNum, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return configuredBusesCache.getCollection(networkUuid).getResource(variantNum, busId);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, int variantNum, Resource<ConfiguredBusAttributes> busesResource) {
        delegate.updateConfiguredBus(networkUuid,  variantNum, busesResource);
        configuredBusesCache.getCollection(networkUuid).updateResource(variantNum, busesResource);
    }

    @Override
    public void removeConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        delegate.removeConfiguredBus(networkUuid,  variantNum, busId);
        configuredBusesCache.getCollection(networkUuid).removeResource(variantNum, busId);
    }
}
