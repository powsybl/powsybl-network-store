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

    private final NetworkIndex<CollectionCache<SubstationAttributes>> substationsCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id -> delegate.getSubstation(networkUuid, id),
        null,
        () -> delegate.getSubstations(networkUuid)));

    private final NetworkIndex<CollectionCache<VoltageLevelAttributes>> voltageLevelsCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getVoltageLevel(networkUuid, id),
        substationId -> delegate.getVoltageLevelsInSubstation(networkUuid, substationId),
        () -> delegate.getVoltageLevels(networkUuid)));

    private final NetworkIndex<CollectionCache<SwitchAttributes>> switchesCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getSwitch(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelSwitches(networkUuid, voltageLevelId),
        () -> delegate.getSwitches(networkUuid)));

    private final NetworkIndex<CollectionCache<BusbarSectionAttributes>> busbarSectionsCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getBusbarSection(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelBusbarSections(networkUuid, voltageLevelId),
        () -> delegate.getBusbarSections(networkUuid)));

    private final NetworkIndex<CollectionCache<LoadAttributes>> loadsCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getLoad(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelLoads(networkUuid, voltageLevelId),
        () -> delegate.getLoads(networkUuid)));

    private final NetworkIndex<CollectionCache<GeneratorAttributes>> generatorsCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getGenerator(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelGenerators(networkUuid, voltageLevelId),
        () -> delegate.getGenerators(networkUuid)));

    private final NetworkIndex<CollectionCache<TwoWindingsTransformerAttributes>> twoWindingsTransformerCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getTwoWindingsTransformer(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelId),
        () -> delegate.getTwoWindingsTransformers(networkUuid)));

    private final NetworkIndex<CollectionCache<ThreeWindingsTransformerAttributes>> threeWindingsTranqformerCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getThreeWindingsTransformer(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelId),
        () -> delegate.getThreeWindingsTransformers(networkUuid)));

    private final NetworkIndex<CollectionCache<LineAttributes>> linesCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getLine(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelLines(networkUuid, voltageLevelId),
        () -> delegate.getLines(networkUuid)));

    private final NetworkIndex<CollectionCache<ShuntCompensatorAttributes>> shuntCompensatorsCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getShuntCompensator(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelShuntCompensators(networkUuid, voltageLevelId),
        () -> delegate.getShuntCompensators(networkUuid)));

    private final NetworkIndex<CollectionCache<VscConverterStationAttributes>> vscConverterStationCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getVscConverterStation(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelVscConverterStations(networkUuid, voltageLevelId),
        () -> delegate.getVscConverterStations(networkUuid)));

    private final NetworkIndex<CollectionCache<LccConverterStationAttributes>> lccConverterStationCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getLccConverterStation(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelLccConverterStations(networkUuid, voltageLevelId),
        () -> delegate.getLccConverterStations(networkUuid)));

    private final NetworkIndex<CollectionCache<StaticVarCompensatorAttributes>> staticVarCompensatorCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getStaticVarCompensator(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelId),
        () -> delegate.getStaticVarCompensators(networkUuid)));

    private final NetworkIndex<CollectionCache<HvdcLineAttributes>> hvdcLinesCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getHvdcLine(networkUuid, id),
        null,
        () -> delegate.getHvdcLines(networkUuid)));

    private final NetworkIndex<CollectionCache<DanglingLineAttributes>> danglingLinesCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getDanglingLine(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelDanglingLines(networkUuid, voltageLevelId),
        () -> delegate.getDanglingLines(networkUuid)));

    private final NetworkIndex<CollectionCache<ConfiguredBusAttributes>> configuredBusesCache = new NetworkIndex<>(networkUuid -> new CollectionCache<>(
        id ->  delegate.getConfiguredBus(networkUuid, id),
        voltageLevelId -> delegate.getVoltageLevelConfiguredBuses(networkUuid, voltageLevelId),
        () -> delegate.getConfiguredBuses(networkUuid)));

    private final List<NetworkIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> networkContainersCache = Arrays.asList(
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

    private final List<NetworkIndex<? extends CollectionCache<? extends IdentifiableAttributes>>> voltageLevelContainersCache = Arrays.asList(
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
            networkContainersCache.forEach(cache -> cache.getNetwork(networkUuid).init());
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
        networkContainersCache.forEach(cache -> cache.removeNetwork(networkUuid));
    }

    @Override
    public void updateNetwork(UUID networkUuid, Resource<NetworkAttributes> networkResource) {
        delegate.updateNetwork(networkUuid, networkResource);
        networkResources.put(networkResource.getAttributes().getUuid(), networkResource);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        delegate.createSubstations(networkUuid, substationResources);
        substationsCache.getNetwork(networkUuid).createResources(substationResources);

        // initialize voltage level cache to set to fully loaded
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            voltageLevelsCache.getNetwork(networkUuid).initContainer(substationResource.getId());
        }
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return substationsCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return substationsCache.getNetwork(networkUuid).getResource(substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        return substationsCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        delegate.createVoltageLevels(networkUuid, voltageLevelResources);
        voltageLevelsCache.getNetwork(networkUuid).createResources(voltageLevelResources);

        // initialize voltage level sub-collection cache to set to fully loaded
        voltageLevelContainersCache.forEach(cache -> {
            for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
                cache.getNetwork(networkUuid).initContainer(voltageLevelResource.getId());
            }
        });
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return voltageLevelsCache.getNetwork(networkUuid).getResource(voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return voltageLevelsCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, Resource<VoltageLevelAttributes> voltageLevelResource) {
        delegate.updateVoltageLevel(networkUuid, voltageLevelResource);
        voltageLevelsCache.getNetwork(networkUuid).updateResources(voltageLevelResource);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        return voltageLevelsCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return voltageLevelsCache.getNetwork(networkUuid).getContainerResources(substationId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return generatorsCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return loadsCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return shuntCompensatorsCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return staticVarCompensatorCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId) {
        return vscConverterStationCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId) {
        return lccConverterStationCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return twoWindingsTransformerCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return threeWindingsTranqformerCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return linesCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return danglingLinesCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        delegate.createSwitches(networkUuid, switchResources);
        switchesCache.getNetwork(networkUuid).createResources(switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return switchesCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return switchesCache.getNetwork(networkUuid).getResource(switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return switchesCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return switchesCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        delegate.updateSwitch(networkUuid, switchResource);
        switchesCache.getNetwork(networkUuid).updateResources(switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        delegate.createBusbarSections(networkUuid, busbarSectionResources);
        busbarSectionsCache.getNetwork(networkUuid).createResources(busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return busbarSectionsCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return busbarSectionsCache.getNetwork(networkUuid).getResource(busbarSectionId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return busbarSectionsCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        delegate.createLoads(networkUuid, loadResources);
        loadsCache.getNetwork(networkUuid).createResources(loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return loadsCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return loadsCache.getNetwork(networkUuid).getResource(loadId);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        delegate.updateLoad(networkUuid, loadResource);
        loadsCache.getNetwork(networkUuid).updateResources(loadResource);
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return loadsCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        delegate.createGenerators(networkUuid, generatorResources);
        generatorsCache.getNetwork(networkUuid).createResources(generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return generatorsCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return generatorsCache.getNetwork(networkUuid).getResource(generatorId);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        delegate.updateGenerator(networkUuid, generatorResource);
        generatorsCache.getNetwork(networkUuid).updateResources(generatorResource);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return generatorsCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        delegate.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        twoWindingsTransformerCache.getNetwork(networkUuid).createResources(twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return twoWindingsTransformerCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return twoWindingsTransformerCache.getNetwork(networkUuid).getResource(twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        delegate.updateTwoWindingsTransformer(networkUuid, twoWindingsTransformerResource);
        twoWindingsTransformerCache.getNetwork(networkUuid).updateResources(twoWindingsTransformerResource);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return twoWindingsTransformerCache.getNetwork(networkUuid).getResourceCount();
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        delegate.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        threeWindingsTranqformerCache.getNetwork(networkUuid).createResources(threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return threeWindingsTranqformerCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return threeWindingsTranqformerCache.getNetwork(networkUuid).getResource(threeWindingsTransformerId);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        delegate.updateThreeWindingsTransformer(networkUuid, threeWindingsTransformerResource);
        threeWindingsTranqformerCache.getNetwork(networkUuid).updateResources(threeWindingsTransformerResource);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return threeWindingsTranqformerCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        delegate.createLines(networkUuid, lineResources);
        linesCache.getNetwork(networkUuid).createResources(lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return linesCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return linesCache.getNetwork(networkUuid).getResource(lineId);
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        delegate.updateLine(networkUuid, lineResource);
        linesCache.getNetwork(networkUuid).updateResources(lineResource);
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return linesCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        delegate.createShuntCompensators(networkUuid, shuntCompensatorResources);
        shuntCompensatorsCache.getNetwork(networkUuid).createResources(shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return shuntCompensatorsCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return shuntCompensatorsCache.getNetwork(networkUuid).getResource(shuntCompensatorId);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        delegate.updateShuntCompensator(networkUuid, shuntCompensatorResource);
        shuntCompensatorsCache.getNetwork(networkUuid).updateResources(shuntCompensatorResource);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return shuntCompensatorsCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        delegate.createVscConverterStations(networkUuid, vscConverterStationResources);
        vscConverterStationCache.getNetwork(networkUuid).createResources(vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return vscConverterStationCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return vscConverterStationCache.getNetwork(networkUuid).getResource(vscConverterStationId);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        delegate.updateVscConverterStation(networkUuid, vscConverterStationResource);
        vscConverterStationCache.getNetwork(networkUuid).updateResources(vscConverterStationResource);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return vscConverterStationCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        delegate.createLccConverterStations(networkUuid, lccConverterStationResources);
        lccConverterStationCache.getNetwork(networkUuid).createResources(lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return lccConverterStationCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return lccConverterStationCache.getNetwork(networkUuid).getResource(lccConverterStationId);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        delegate.updateLccConverterStation(networkUuid, lccConverterStationResource);
        lccConverterStationCache.getNetwork(networkUuid).updateResources(lccConverterStationResource);
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return lccConverterStationCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        delegate.createStaticVarCompensators(networkUuid, svcResources);
        staticVarCompensatorCache.getNetwork(networkUuid).createResources(svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return staticVarCompensatorCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return staticVarCompensatorCache.getNetwork(networkUuid).getResource(staticVarCompensatorId);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        delegate.updateStaticVarCompensator(networkUuid, staticVarCompensatorResource);
        staticVarCompensatorCache.getNetwork(networkUuid).updateResources(staticVarCompensatorResource);
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return staticVarCompensatorCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        delegate.createHvdcLines(networkUuid, hvdcLineResources);
        hvdcLinesCache.getNetwork(networkUuid).createResources(hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return hvdcLinesCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return hvdcLinesCache.getNetwork(networkUuid).getResource(hvdcLineId);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        delegate.updateHvdcLine(networkUuid, hvdcLineResource);
        hvdcLinesCache.getNetwork(networkUuid).updateResources(hvdcLineResource);
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return hvdcLinesCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        delegate.createDanglingLines(networkUuid, danglingLineResources);
        danglingLinesCache.getNetwork(networkUuid).createResources(danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return danglingLinesCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return danglingLinesCache.getNetwork(networkUuid).getResource(danglingLineId);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        delegate.updateDanglingLine(networkUuid, danglingLineResource);
        danglingLinesCache.getNetwork(networkUuid).updateResources(danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        delegate.removeDanglingLine(networkUuid, danglingLineId);
        danglingLinesCache.getNetwork(networkUuid).removeResource(danglingLineId);
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return danglingLinesCache.getNetwork(networkUuid).getResourceCount();
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        delegate.createConfiguredBuses(networkUuid, busesRessources);
        configuredBusesCache.getNetwork(networkUuid).createResources(busesRessources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return configuredBusesCache.getNetwork(networkUuid).getResources();
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return configuredBusesCache.getNetwork(networkUuid).getContainerResources(voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return configuredBusesCache.getNetwork(networkUuid).getResource(busId);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        delegate.updateConfiguredBus(networkUuid, busesResource);
        configuredBusesCache.getNetwork(networkUuid).updateResources(busesResource);
    }
}
