/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class CachedRestNetworkStoreClient implements NetworkStoreClient {

    private final BufferedRestNetworkStoreClient bufferedRestNetworkStoreClient;

    public CachedRestNetworkStoreClient(BufferedRestNetworkStoreClient bufferedRestNetworkStoreClient) {
        this.bufferedRestNetworkStoreClient = Objects.requireNonNull(bufferedRestNetworkStoreClient);
    }

    private final NetworkCacheHandler cacheHandler = new NetworkCacheHandler();

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        return bufferedRestNetworkStoreClient.getNetworks();
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        bufferedRestNetworkStoreClient.createNetworks(networkResources);
        networkResources.forEach(resource -> cacheHandler.getNetworkCache(resource.getAttributes().getUuid()).setNetworkResource(resource));
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getNetworkResource(() -> bufferedRestNetworkStoreClient.getNetwork(networkUuid));
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        bufferedRestNetworkStoreClient.deleteNetwork(networkUuid);
        cacheHandler.invalidateNetworkCache(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        bufferedRestNetworkStoreClient.createSubstations(networkUuid, substationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SUBSTATION, substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SUBSTATION, () -> bufferedRestNetworkStoreClient.getSubstations(networkUuid));
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SUBSTATION, substationId, id ->  bufferedRestNetworkStoreClient.getSubstation(networkUuid, id));
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getSubstationCount(networkUuid);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        bufferedRestNetworkStoreClient.createVoltageLevels(networkUuid, voltageLevelResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.VOLTAGE_LEVEL, voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.VOLTAGE_LEVEL, voltageLevelId, id ->  bufferedRestNetworkStoreClient.getVoltageLevel(networkUuid, id));
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.VOLTAGE_LEVEL, () -> bufferedRestNetworkStoreClient.getVoltageLevels(networkUuid));    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.VOLTAGE_LEVEL, substationId, id -> bufferedRestNetworkStoreClient.getVoltageLevelsInSubstation(networkUuid, id));
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getVoltageLevelCount(networkUuid);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.BUSBAR_SECTION, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelBusbarSections(networkUuid, id));
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.SWITCH, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelSwitches(networkUuid, id));
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.GENERATOR, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelGenerators(networkUuid, id));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LOAD, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelLoads(networkUuid, id));
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.SHUNT_COMPENSATOR, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelShuntCompensators(networkUuid, id));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.STATIC_VAR_COMPENSATOR, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelStaticVarCompensators(networkUuid, id));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.VSC_CONVERTER_STATION, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelVscConverterStation(networkUuid, id));
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LCC_CONVERTER_STATION, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelLccConverterStation(networkUuid, id));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.TWO_WINDINGS_TRANSFORMER, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelTwoWindingsTransformers(networkUuid, id));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.THREE_WINDINGS_TRANSFORMER, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelThreeWindingsTransformers(networkUuid, id));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LINE, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelLines(networkUuid, id));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.DANGLING_LINE, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelDanglingLines(networkUuid, id));
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        bufferedRestNetworkStoreClient.createSwitches(networkUuid, switchResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SWITCH, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SWITCH, () -> bufferedRestNetworkStoreClient.getSwitches(networkUuid));
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SWITCH, switchId, id ->  bufferedRestNetworkStoreClient.getSwitch(networkUuid, id));
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getSwitchCount(networkUuid);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        bufferedRestNetworkStoreClient.createBusbarSections(networkUuid, busbarSectionResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.BUSBAR_SECTION, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.BUSBAR_SECTION, () -> bufferedRestNetworkStoreClient.getBusbarSections(networkUuid));
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.BUSBAR_SECTION, busbarSectionId, id ->  bufferedRestNetworkStoreClient.getBusbarSection(networkUuid, id));
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getBusbarSectionCount(networkUuid);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        bufferedRestNetworkStoreClient.createLoads(networkUuid, loadResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LOAD, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LOAD, () -> bufferedRestNetworkStoreClient.getLoads(networkUuid));
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LOAD, loadId, id ->  bufferedRestNetworkStoreClient.getLoad(networkUuid, id));
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getLoadCount(networkUuid);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        bufferedRestNetworkStoreClient.createGenerators(networkUuid, generatorResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.GENERATOR, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.GENERATOR, () -> bufferedRestNetworkStoreClient.getGenerators(networkUuid));
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.GENERATOR, generatorId, id ->  bufferedRestNetworkStoreClient.getGenerator(networkUuid, id));
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getGeneratorCount(networkUuid);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        bufferedRestNetworkStoreClient.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.TWO_WINDINGS_TRANSFORMER, () -> bufferedRestNetworkStoreClient.getTwoWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerId, id ->  bufferedRestNetworkStoreClient.getTwoWindingsTransformer(networkUuid, id));
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getTwoWindingsTransformerCount(networkUuid);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        bufferedRestNetworkStoreClient.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.THREE_WINDINGS_TRANSFORMER, () -> bufferedRestNetworkStoreClient.getThreeWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerId, id ->  bufferedRestNetworkStoreClient.getThreeWindingsTransformer(networkUuid, id));
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getThreeWindingsTransformerCount(networkUuid);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        bufferedRestNetworkStoreClient.createLines(networkUuid, lineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LINE, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LINE, () -> bufferedRestNetworkStoreClient.getLines(networkUuid));
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LINE, lineId, id ->  bufferedRestNetworkStoreClient.getLine(networkUuid, id));
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getLineCount(networkUuid);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        bufferedRestNetworkStoreClient.createShuntCompensators(networkUuid, shuntCompensatorResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SHUNT_COMPENSATOR, () -> bufferedRestNetworkStoreClient.getShuntCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorId, id ->  bufferedRestNetworkStoreClient.getShuntCompensator(networkUuid, id));
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getShuntCompensatorCount(networkUuid);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        bufferedRestNetworkStoreClient.createVscConverterStations(networkUuid, vscConverterStationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.VSC_CONVERTER_STATION, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.VSC_CONVERTER_STATION, () -> bufferedRestNetworkStoreClient.getVscConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.VSC_CONVERTER_STATION, vscConverterStationId, id ->  bufferedRestNetworkStoreClient.getVscConverterStation(networkUuid, id));
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getVscConverterStationCount(networkUuid);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        bufferedRestNetworkStoreClient.createLccConverterStations(networkUuid, lccConverterStationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LCC_CONVERTER_STATION, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LCC_CONVERTER_STATION, () -> bufferedRestNetworkStoreClient.getLccConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LCC_CONVERTER_STATION, lccConverterStationId, id ->  bufferedRestNetworkStoreClient.getLccConverterStation(networkUuid, id));
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getLccConverterStationCount(networkUuid);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        bufferedRestNetworkStoreClient.createStaticVarCompensators(networkUuid, svcResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.STATIC_VAR_COMPENSATOR, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.STATIC_VAR_COMPENSATOR, () -> bufferedRestNetworkStoreClient.getStaticVarCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorId, id ->  bufferedRestNetworkStoreClient.getStaticVarCompensator(networkUuid, id));
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getStaticVarCompensatorCount(networkUuid);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        bufferedRestNetworkStoreClient.createHvdcLines(networkUuid, hvdcLineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.HVDC_LINE, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.HVDC_LINE, () -> bufferedRestNetworkStoreClient.getHvdcLines(networkUuid));
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.HVDC_LINE, hvdcLineId, id ->  bufferedRestNetworkStoreClient.getHvdcLine(networkUuid, id));
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getHvdcLineCount(networkUuid);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        bufferedRestNetworkStoreClient.createDanglingLines(networkUuid, danglingLineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.DANGLING_LINE, danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.DANGLING_LINE, () -> bufferedRestNetworkStoreClient.getDanglingLines(networkUuid));
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.DANGLING_LINE, danglingLineId, id ->  bufferedRestNetworkStoreClient.getDanglingLine(networkUuid, id));
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getDanglingLineCount(networkUuid);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        bufferedRestNetworkStoreClient.removeDanglingLine(networkUuid, danglingLineId);
        cacheHandler.getNetworkCache(networkUuid).removeResource(ResourceType.DANGLING_LINE, danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        bufferedRestNetworkStoreClient.createConfiguredBuses(networkUuid, busesRessources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.CONFIGURED_BUS, busesRessources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.CONFIGURED_BUS, () -> bufferedRestNetworkStoreClient.getConfiguredBuses(networkUuid));
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return bufferedRestNetworkStoreClient.getVoltageLevelConfiguredBuses(networkUuid, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.CONFIGURED_BUS, busId, id ->  bufferedRestNetworkStoreClient.getConfiguredBus(networkUuid, id));
    }

    @Override
    public void flush() {
        bufferedRestNetworkStoreClient.flush();
    }
}
