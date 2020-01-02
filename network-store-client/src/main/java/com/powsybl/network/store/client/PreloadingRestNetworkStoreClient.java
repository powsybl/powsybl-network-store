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
                break;
            case GENERATOR:
                break;
            case SHUNT_COMPENSATOR:
                break;
            case VSC_CONVERTER_STATION:
                break;
            case STATIC_VAR_COMPENSATOR:
                break;
            case BUSBAR_SECTION:
                break;
            case SWITCH:
                break;
            case TWO_WINDINGS_TRANSFORMER:
                cacheClient.createTwoWindingsTransformers(networkUuid, restClient.getTwoWindingsTransformers(networkUuid));
                break;
            case LINE:
                cacheClient.createLines(networkUuid, restClient.getLines(networkUuid));
                break;
            case HVDC_LINE:
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
        cachedResourceTypes.remove(networkUuid);
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
        return restClient.getVoltageLevelBusbarSections(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return restClient.getVoltageLevelSwitches(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return restClient.getVoltageLevelGenerators(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return restClient.getVoltageLevelLoads(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return restClient.getVoltageLevelShuntCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return restClient.getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        return restClient.getVoltageLevelVscConverterStation(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        return restClient.getVoltageLevelLccConverterStation(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return cacheClient.getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return cacheClient.getVoltageLevelLines(networkUuid, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        restClient.createSwitches(networkUuid, switchResources);
        cacheClient.createSwitches(networkUuid, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return restClient.getSwitches(networkUuid);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return restClient.getSwitch(networkUuid, switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return restClient.getSwitchCount(networkUuid);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        restClient.createBusbarSections(networkUuid, busbarSectionResources);
        cacheClient.createBusbarSections(networkUuid, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return restClient.getBusbarSections(networkUuid);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return restClient.getBusbarSection(networkUuid, busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        return restClient.getBusbarSectionCount(networkUuid);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        restClient.createLoads(networkUuid, loadResources);
        cacheClient.createLoads(networkUuid, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return restClient.getLoads(networkUuid);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return restClient.getLoad(networkUuid, loadId);
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return restClient.getLoadCount(networkUuid);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        restClient.createGenerators(networkUuid, generatorResources);
        cacheClient.createGenerators(networkUuid, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return restClient.getGenerators(networkUuid);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return restClient.getGenerator(networkUuid, generatorId);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return restClient.getGeneratorCount(networkUuid);
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
        restClient.createShuntCompensators(networkUuid, shuntCompensatorResources);
        cacheClient.createShuntCompensators(networkUuid, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return restClient.getShuntCompensators(networkUuid);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return restClient.getShuntCompensator(networkUuid, shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return restClient.getShuntCompensatorCount(networkUuid);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        restClient.createVscConverterStations(networkUuid, vscConverterStationResources);
        cacheClient.createVscConverterStations(networkUuid, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return restClient.getVscConverterStations(networkUuid);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return restClient.getVscConverterStation(networkUuid, vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return restClient.getVscConverterStationCount(networkUuid);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        restClient.createLccConverterStations(networkUuid, lccConverterStationResources);
        cacheClient.createLccConverterStations(networkUuid, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return restClient.getLccConverterStations(networkUuid);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return restClient.getLccConverterStation(networkUuid, lccConverterStationId);
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return restClient.getLccConverterStationCount(networkUuid);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        restClient.createStaticVarCompensators(networkUuid, svcResources);
        cacheClient.createStaticVarCompensators(networkUuid, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return restClient.getStaticVarCompensators(networkUuid);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return restClient.getStaticVarCompensator(networkUuid, staticVarCompensatorId);
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return restClient.getStaticVarCompensatorCount(networkUuid);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        restClient.createHvdcLines(networkUuid, hvdcLineResources);
        cacheClient.createHvdcLines(networkUuid, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return restClient.getHvdcLines(networkUuid);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return restClient.getHvdcLine(networkUuid, hvdcLineId);
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return restClient.getHvdcLineCount(networkUuid);
    }

    @Override
    public void flush() {
        restClient.flush();
        cacheClient.flush();
    }
}
