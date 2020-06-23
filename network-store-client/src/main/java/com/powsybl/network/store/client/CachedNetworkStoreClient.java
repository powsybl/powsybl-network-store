/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class CachedNetworkStoreClient extends ForwardingNetworkStoreClient implements NetworkStoreClient {

    private final Map<UUID, Resource<NetworkAttributes>> networkResources = new HashMap<>();

    private boolean networksFullyLoaded = false;

    private final NetworkCollectionIndex<CollectionCache<SubstationAttributes>> substationsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id -> delegate.getSubstation(networkUuid, id),
        null,
        () -> delegate.getSubstations(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<VoltageLevelAttributes>> voltageLevelsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getVoltageLevel(networkUuid, id),
        substationId -> delegate.getVoltageLevelsInSubstation(networkUuid, substationId),
        () -> delegate.getVoltageLevels(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<SwitchAttributes>> switchesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getSwitch(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelSwitches(networkUuid, voltageLevelId),
        () -> delegate.getSwitches(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<BusbarSectionAttributes>> busbarSectionsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getBusbarSection(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelBusbarSections(networkUuid, voltageLevelId),
        () -> delegate.getBusbarSections(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<LoadAttributes>> loadsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getLoad(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelLoads(networkUuid, voltageLevelId),
        () -> delegate.getLoads(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<GeneratorAttributes>> generatorsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getGenerator(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelGenerators(networkUuid, voltageLevelId),
        () -> delegate.getGenerators(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<TwoWindingsTransformerAttributes>> twoWindingsTransformerCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getTwoWindingsTransformer(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelId),
        () -> delegate.getTwoWindingsTransformers(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<ThreeWindingsTransformerAttributes>> threeWindingsTranqformerCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getThreeWindingsTransformer(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelId),
        () -> delegate.getThreeWindingsTransformers(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<LineAttributes>> linesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getLine(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelLines(networkUuid, voltageLevelId),
        () -> delegate.getLines(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<ShuntCompensatorAttributes>> shuntCompensatorsCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getShuntCompensator(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelShuntCompensators(networkUuid, voltageLevelId),
        () -> delegate.getShuntCompensators(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<VscConverterStationAttributes>> vscConverterStationCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getVscConverterStation(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelVscConverterStations(networkUuid, voltageLevelId),
        () -> delegate.getVscConverterStations(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<LccConverterStationAttributes>> lccConverterStationCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getLccConverterStation(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelLccConverterStations(networkUuid, voltageLevelId),
        () -> delegate.getLccConverterStations(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<StaticVarCompensatorAttributes>> staticVarCompensatorCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getStaticVarCompensator(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelId),
        () -> delegate.getStaticVarCompensators(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<HvdcLineAttributes>> hvdcLinesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getHvdcLine(networkUuid, id),
        null,
        () -> delegate.getHvdcLines(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<DanglingLineAttributes>> danglingLinesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getDanglingLine(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelDanglingLines(networkUuid, voltageLevelId),
        () -> delegate.getDanglingLines(networkUuid)));

    private final NetworkCollectionIndex<CollectionCache<ConfiguredBusAttributes>> configuredBusesCache = new NetworkCollectionIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getConfiguredBus(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelConfiguredBuses(networkUuid, voltageLevelId),
        () -> delegate.getConfiguredBuses(networkUuid)));

    private final List<NetworkCollectionIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> networkContainersCache = Arrays.asList(
            substationsCache,
            voltageLevelsCache,
            switchesCache,
            busbarSectionsCache,
            loadsCache,
            generatorsCache,
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
            this.networkResources.put(networkUuid, networkResource);

            // initialize network sub-collection cache to set to fully loaded
            networkContainersCache.forEach(cache -> cache.getCollection(networkUuid).init());
        }
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        if (!networksFullyLoaded) {
            for (Resource<NetworkAttributes> network : delegate.getNetworks()) {
                networkResources.put(network.getAttributes().getUuid(), network);
            }
            networksFullyLoaded = true;
        }
        return new ArrayList<>(networkResources.values());
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        Resource<NetworkAttributes> networkResource = networkResources.get(networkUuid);
        if (networkResource == null) {
            networkResource = delegate.getNetwork(networkUuid).orElse(null);
            networkResources.put(networkUuid, networkResource);
        }
        return Optional.ofNullable(networkResource);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        delegate.deleteNetwork(networkUuid);
        networkResources.remove(networkUuid);
        networkContainersCache.forEach(cache -> cache.removeCollection(networkUuid));
    }

    @Override
    public void updateNetwork(UUID networkUuid, Resource<NetworkAttributes> networkResource) {
        delegate.updateNetwork(networkUuid, networkResource);
        networkResources.put(networkResource.getAttributes().getUuid(), networkResource);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        delegate.createSubstations(networkUuid, substationResources);
        substationsCache.getCollection(networkUuid).createResources(substationResources);

        // initialize voltage level cache to set to fully loaded
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            voltageLevelsCache.getCollection(networkUuid).initContainer(substationResource.getId());
        }
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return substationsCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return substationsCache.getCollection(networkUuid).getResource(substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        return substationsCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        delegate.createVoltageLevels(networkUuid, voltageLevelResources);
        voltageLevelsCache.getCollection(networkUuid).createResources(voltageLevelResources);

        // initialize voltage level sub-collection cache to set to fully loaded
        voltageLevelContainersCache.forEach(cache -> {
            for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
                cache.getCollection(networkUuid).initContainer(voltageLevelResource.getId());
            }
        });
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return voltageLevelsCache.getCollection(networkUuid).getResource(voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return voltageLevelsCache.getCollection(networkUuid).getResources();
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, Resource<VoltageLevelAttributes> voltageLevelResource) {
        delegate.updateVoltageLevel(networkUuid, voltageLevelResource);
        voltageLevelsCache.getCollection(networkUuid).updateResource(voltageLevelResource);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        return voltageLevelsCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return voltageLevelsCache.getCollection(networkUuid).getContainerResources(substationId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return generatorsCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return loadsCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return shuntCompensatorsCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return staticVarCompensatorCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId) {
        return vscConverterStationCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId) {
        return lccConverterStationCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return twoWindingsTransformerCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return threeWindingsTranqformerCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return linesCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return danglingLinesCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        delegate.createSwitches(networkUuid, switchResources);
        switchesCache.getCollection(networkUuid).createResources(switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return switchesCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return switchesCache.getCollection(networkUuid).getResource(switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return switchesCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return switchesCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        delegate.updateSwitch(networkUuid, switchResource);
        switchesCache.getCollection(networkUuid).updateResource(switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        delegate.createBusbarSections(networkUuid, busbarSectionResources);
        busbarSectionsCache.getCollection(networkUuid).createResources(busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return busbarSectionsCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return busbarSectionsCache.getCollection(networkUuid).getResource(busbarSectionId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return busbarSectionsCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        delegate.createLoads(networkUuid, loadResources);
        loadsCache.getCollection(networkUuid).createResources(loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return loadsCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return loadsCache.getCollection(networkUuid).getResource(loadId);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        delegate.updateLoad(networkUuid, loadResource);
        loadsCache.getCollection(networkUuid).updateResource(loadResource);
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return loadsCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        delegate.createGenerators(networkUuid, generatorResources);
        generatorsCache.getCollection(networkUuid).createResources(generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return generatorsCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return generatorsCache.getCollection(networkUuid).getResource(generatorId);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        delegate.updateGenerator(networkUuid, generatorResource);
        generatorsCache.getCollection(networkUuid).updateResource(generatorResource);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return generatorsCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        delegate.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        twoWindingsTransformerCache.getCollection(networkUuid).createResources(twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return twoWindingsTransformerCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return twoWindingsTransformerCache.getCollection(networkUuid).getResource(twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        delegate.updateTwoWindingsTransformer(networkUuid, twoWindingsTransformerResource);
        twoWindingsTransformerCache.getCollection(networkUuid).updateResource(twoWindingsTransformerResource);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return twoWindingsTransformerCache.getCollection(networkUuid).getResourceCount();
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        delegate.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        threeWindingsTranqformerCache.getCollection(networkUuid).createResources(threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return threeWindingsTranqformerCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return threeWindingsTranqformerCache.getCollection(networkUuid).getResource(threeWindingsTransformerId);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        delegate.updateThreeWindingsTransformer(networkUuid, threeWindingsTransformerResource);
        threeWindingsTranqformerCache.getCollection(networkUuid).updateResource(threeWindingsTransformerResource);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return threeWindingsTranqformerCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        delegate.createLines(networkUuid, lineResources);
        linesCache.getCollection(networkUuid).createResources(lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return linesCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return linesCache.getCollection(networkUuid).getResource(lineId);
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        delegate.updateLine(networkUuid, lineResource);
        linesCache.getCollection(networkUuid).updateResource(lineResource);
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return linesCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        delegate.createShuntCompensators(networkUuid, shuntCompensatorResources);
        shuntCompensatorsCache.getCollection(networkUuid).createResources(shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return shuntCompensatorsCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return shuntCompensatorsCache.getCollection(networkUuid).getResource(shuntCompensatorId);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        delegate.updateShuntCompensator(networkUuid, shuntCompensatorResource);
        shuntCompensatorsCache.getCollection(networkUuid).updateResource(shuntCompensatorResource);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return shuntCompensatorsCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        delegate.createVscConverterStations(networkUuid, vscConverterStationResources);
        vscConverterStationCache.getCollection(networkUuid).createResources(vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return vscConverterStationCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return vscConverterStationCache.getCollection(networkUuid).getResource(vscConverterStationId);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        delegate.updateVscConverterStation(networkUuid, vscConverterStationResource);
        vscConverterStationCache.getCollection(networkUuid).updateResource(vscConverterStationResource);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return vscConverterStationCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        delegate.createLccConverterStations(networkUuid, lccConverterStationResources);
        lccConverterStationCache.getCollection(networkUuid).createResources(lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return lccConverterStationCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return lccConverterStationCache.getCollection(networkUuid).getResource(lccConverterStationId);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        delegate.updateLccConverterStation(networkUuid, lccConverterStationResource);
        lccConverterStationCache.getCollection(networkUuid).updateResource(lccConverterStationResource);
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return lccConverterStationCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        delegate.createStaticVarCompensators(networkUuid, svcResources);
        staticVarCompensatorCache.getCollection(networkUuid).createResources(svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return staticVarCompensatorCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return staticVarCompensatorCache.getCollection(networkUuid).getResource(staticVarCompensatorId);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        delegate.updateStaticVarCompensator(networkUuid, staticVarCompensatorResource);
        staticVarCompensatorCache.getCollection(networkUuid).updateResource(staticVarCompensatorResource);
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return staticVarCompensatorCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        delegate.createHvdcLines(networkUuid, hvdcLineResources);
        hvdcLinesCache.getCollection(networkUuid).createResources(hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return hvdcLinesCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return hvdcLinesCache.getCollection(networkUuid).getResource(hvdcLineId);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        delegate.updateHvdcLine(networkUuid, hvdcLineResource);
        hvdcLinesCache.getCollection(networkUuid).updateResource(hvdcLineResource);
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return hvdcLinesCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        delegate.createDanglingLines(networkUuid, danglingLineResources);
        danglingLinesCache.getCollection(networkUuid).createResources(danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return danglingLinesCache.getCollection(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return danglingLinesCache.getCollection(networkUuid).getResource(danglingLineId);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        delegate.updateDanglingLine(networkUuid, danglingLineResource);
        danglingLinesCache.getCollection(networkUuid).updateResource(danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        delegate.removeDanglingLine(networkUuid, danglingLineId);
        danglingLinesCache.getCollection(networkUuid).removeResource(danglingLineId);
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return danglingLinesCache.getCollection(networkUuid).getResourceCount();
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        delegate.createConfiguredBuses(networkUuid, busesRessources);
        configuredBusesCache.getCollection(networkUuid).createResources(busesRessources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return configuredBusesCache.getCollection(networkUuid).getResources();
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return configuredBusesCache.getCollection(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return configuredBusesCache.getCollection(networkUuid).getResource(busId);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        delegate.updateConfiguredBus(networkUuid, busesResource);
        configuredBusesCache.getCollection(networkUuid).updateResource(busesResource);
    }
}
