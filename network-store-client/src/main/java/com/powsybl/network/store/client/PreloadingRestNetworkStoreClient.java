/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PreloadingRestNetworkStoreClient implements NetworkStoreClient {

    private final BufferedRestNetworkStoreClient restClient;

    private final CacheNetworkStoreClient cacheClient = new CacheNetworkStoreClient();

    private final Map<UUID, Set<ResourceType>> cachedResourceTypes = new HashMap<>();

    public PreloadingRestNetworkStoreClient(RestNetworkStoreClient restStoreClient) {
        this.restClient = new BufferedRestNetworkStoreClient(restStoreClient);
    }

    private void loadToCache(ResourceType resourceType, UUID networkUuid) {
        switch (resourceType) {
            case NETWORK:
                cacheClient.createNetworks(restClient.getNetworks());
                break;
            case SUBSTATION:
                cacheClient.createSubstations(networkUuid, restClient.getSubstations(networkUuid));
                break;
            case VOLTAGE_LEVEL:
                cacheClient.createVoltageLevels(networkUuid, restClient.getVoltageLevels(networkUuid));
                break;
            case LOAD:
                cacheClient.createLoads(networkUuid, restClient.getLoads(networkUuid));
                break;
            case GENERATOR:
                cacheClient.createGenerators(networkUuid, restClient.getGenerators(networkUuid));
                break;
            case SHUNT_COMPENSATOR:
                cacheClient.createShuntCompensators(networkUuid, restClient.getShuntCompensators(networkUuid));
                break;
            case VSC_CONVERTER_STATION:
                cacheClient.createVscConverterStations(networkUuid, restClient.getVscConverterStations(networkUuid));
                break;
            case LCC_CONVERTER_STATION:
                cacheClient.createLccConverterStations(networkUuid, restClient.getLccConverterStations(networkUuid));
                break;
            case STATIC_VAR_COMPENSATOR:
                cacheClient.createStaticVarCompensators(networkUuid, restClient.getStaticVarCompensators(networkUuid));
                break;
            case BUSBAR_SECTION:
                cacheClient.createBusbarSections(networkUuid, restClient.getBusbarSections(networkUuid));
                break;
            case SWITCH:
                cacheClient.createSwitches(networkUuid, restClient.getSwitches(networkUuid));
                break;
            case TWO_WINDINGS_TRANSFORMER:
                cacheClient.createTwoWindingsTransformers(networkUuid, restClient.getTwoWindingsTransformers(networkUuid));
                break;
            case THREE_WINDINGS_TRANSFORMER:
                cacheClient.createThreeWindingsTransformers(networkUuid, restClient.getThreeWindingsTransformers(networkUuid));
                break;
            case LINE:
                cacheClient.createLines(networkUuid, restClient.getLines(networkUuid));
                break;
            case HVDC_LINE:
                cacheClient.createHvdcLines(networkUuid, restClient.getHvdcLines(networkUuid));
                break;
            case DANGLING_LINE:
                cacheClient.createDanglingLines(networkUuid, restClient.getDanglingLines(networkUuid));
                break;
            case CONFIGURED_BUS:
                cacheClient.createConfiguredBuses(networkUuid, restClient.getConfiguredBuses(networkUuid));
                break;
        }
    }

    private void ensureCached(ResourceType resourceType, UUID networkUuid) {
        Objects.requireNonNull(resourceType);
        if (resourceType != ResourceType.NETWORK) {
            Objects.requireNonNull(networkUuid);
        }
        Set<ResourceType> resourceTypes = cachedResourceTypes.computeIfAbsent(networkUuid, k -> EnumSet.noneOf(ResourceType.class));
        if (!resourceTypes.contains(resourceType)) {
            loadToCache(resourceType, networkUuid);
            resourceTypes.add(resourceType);
        }
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        ensureCached(ResourceType.NETWORK, null);
        return cacheClient.getNetworks();
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        ensureCached(ResourceType.NETWORK, null);
        restClient.createNetworks(networkResources);
        cacheClient.createNetworks(networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        ensureCached(ResourceType.NETWORK, null);
        return cacheClient.getNetwork(networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        restClient.deleteNetwork(networkUuid);
        cacheClient.deleteNetwork(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        restClient.createSubstations(networkUuid, substationResources);
        cacheClient.createSubstations(networkUuid, substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return cacheClient.getSubstations(networkUuid);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return cacheClient.getSubstation(networkUuid, substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return cacheClient.getSubstationCount(networkUuid);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        restClient.createVoltageLevels(networkUuid, voltageLevelResources);
        cacheClient.createVoltageLevels(networkUuid, voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return cacheClient.getVoltageLevel(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return cacheClient.getVoltageLevels(networkUuid);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return cacheClient.getVoltageLevelsInSubstation(networkUuid, substationId);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return cacheClient.getVoltageLevelCount(networkUuid);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return cacheClient.getVoltageLevelBusbarSections(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return cacheClient.getVoltageLevelSwitches(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return cacheClient.getVoltageLevelGenerators(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return cacheClient.getVoltageLevelLoads(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return cacheClient.getVoltageLevelShuntCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return cacheClient.getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return cacheClient.getVoltageLevelVscConverterStation(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return cacheClient.getVoltageLevelLccConverterStation(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return cacheClient.getVoltageLevelLines(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return cacheClient.getVoltageLevelDanglingLines(networkUuid, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        restClient.createSwitches(networkUuid, switchResources);
        cacheClient.createSwitches(networkUuid, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return cacheClient.getSwitches(networkUuid);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return cacheClient.getSwitch(networkUuid, switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return cacheClient.getSwitchCount(networkUuid);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        restClient.createBusbarSections(networkUuid, busbarSectionResources);
        cacheClient.createBusbarSections(networkUuid, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return cacheClient.getBusbarSections(networkUuid);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return cacheClient.getBusbarSection(networkUuid, busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return cacheClient.getBusbarSectionCount(networkUuid);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        ensureCached(ResourceType.LOAD, networkUuid);
        restClient.createLoads(networkUuid, loadResources);
        cacheClient.createLoads(networkUuid, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return cacheClient.getLoads(networkUuid);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return cacheClient.getLoad(networkUuid, loadId);
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return cacheClient.getLoadCount(networkUuid);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        restClient.createGenerators(networkUuid, generatorResources);
        cacheClient.createGenerators(networkUuid, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return cacheClient.getGenerators(networkUuid);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return cacheClient.getGenerator(networkUuid, generatorId);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return cacheClient.getGeneratorCount(networkUuid);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        restClient.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        cacheClient.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getTwoWindingsTransformers(networkUuid);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getTwoWindingsTransformer(networkUuid, twoWindingsTransformerId);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getTwoWindingsTransformerCount(networkUuid);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        restClient.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        cacheClient.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getThreeWindingsTransformers(networkUuid);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getThreeWindingsTransformer(networkUuid, threeWindingsTransformerId);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getThreeWindingsTransformerCount(networkUuid);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        ensureCached(ResourceType.LINE, networkUuid);
        restClient.createLines(networkUuid, lineResources);
        cacheClient.createLines(networkUuid, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        ensureCached(ResourceType.LINE, networkUuid);
        return cacheClient.getLines(networkUuid);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return cacheClient.getLine(networkUuid, lineId);
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        ensureCached(ResourceType.LINE, networkUuid);
        return cacheClient.getLineCount(networkUuid);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        restClient.createShuntCompensators(networkUuid, shuntCompensatorResources);
        cacheClient.createShuntCompensators(networkUuid, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return cacheClient.getShuntCompensators(networkUuid);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return cacheClient.getShuntCompensator(networkUuid, shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return cacheClient.getShuntCompensatorCount(networkUuid);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        restClient.createVscConverterStations(networkUuid, vscConverterStationResources);
        cacheClient.createVscConverterStations(networkUuid, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return cacheClient.getVscConverterStations(networkUuid);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return cacheClient.getVscConverterStation(networkUuid, vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return cacheClient.getVscConverterStationCount(networkUuid);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        restClient.createLccConverterStations(networkUuid, lccConverterStationResources);
        cacheClient.createLccConverterStations(networkUuid, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return cacheClient.getLccConverterStations(networkUuid);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return cacheClient.getLccConverterStation(networkUuid, lccConverterStationId);
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return cacheClient.getLccConverterStationCount(networkUuid);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        restClient.createStaticVarCompensators(networkUuid, svcResources);
        cacheClient.createStaticVarCompensators(networkUuid, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return cacheClient.getStaticVarCompensators(networkUuid);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return cacheClient.getStaticVarCompensator(networkUuid, staticVarCompensatorId);
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return cacheClient.getStaticVarCompensatorCount(networkUuid);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        restClient.createHvdcLines(networkUuid, hvdcLineResources);
        cacheClient.createHvdcLines(networkUuid, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return cacheClient.getHvdcLines(networkUuid);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return cacheClient.getHvdcLine(networkUuid, hvdcLineId);
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return cacheClient.getHvdcLineCount(networkUuid);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        restClient.createDanglingLines(networkUuid, danglingLineResources);
        cacheClient.createDanglingLines(networkUuid, danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return cacheClient.getDanglingLines(networkUuid);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return cacheClient.getDanglingLine(networkUuid, danglingLineId);
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return cacheClient.getDanglingLineCount(networkUuid);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        restClient.removeDanglingLine(networkUuid, danglingLineId);
        cacheClient.removeDanglingLine(networkUuid, danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        restClient.createConfiguredBuses(networkUuid, busesResources);
        cacheClient.createConfiguredBuses(networkUuid, busesResources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return cacheClient.getConfiguredBuses(networkUuid);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return cacheClient.getVoltageLevelConfiguredBuses(networkUuid, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return cacheClient.getConfiguredBus(networkUuid, busId);
    }

    @Override
    public void flush() {
        restClient.flush();
        cacheClient.flush();
    }
}
