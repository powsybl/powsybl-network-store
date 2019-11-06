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

    private final Map<String, Set<ResourceType>> cachedResourceTypes = new HashMap<>();

    public PreloadingRestNetworkStoreClient(RestNetworkStoreClient restStoreClient) {
        this.restClient = new BufferedRestNetworkStoreClient(restStoreClient);
    }

    private void loadToCache(ResourceType resourceType, String networkId) {
        switch (resourceType) {
            case NETWORK:
                cacheClient.createNetworks(restClient.getNetworks());
                break;
            case SUBSTATION:
                cacheClient.createSubstations(networkId, restClient.getSubstations(networkId));
                break;
            case VOLTAGE_LEVEL:
                cacheClient.createVoltageLevels(networkId, restClient.getVoltageLevels(networkId));
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
                cacheClient.createTwoWindingsTransformers(networkId, restClient.getTwoWindingsTransformers(networkId));
                break;
            case LINE:
                cacheClient.createLines(networkId, restClient.getLines(networkId));
                break;
            case HVDC_LINE:
                break;
        }
    }

    private void ensureCached(ResourceType resourceType, String networkId) {
        Objects.requireNonNull(resourceType);
        if (resourceType != ResourceType.NETWORK) {
            Objects.requireNonNull(networkId);
        }
        Set<ResourceType> resourceTypes = cachedResourceTypes.computeIfAbsent(networkId, k -> EnumSet.noneOf(ResourceType.class));
        if (!resourceTypes.contains(resourceType)) {
            loadToCache(resourceType, networkId);
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
    public Optional<Resource<NetworkAttributes>> getNetwork(String networkId) {
        ensureCached(ResourceType.NETWORK, null);
        return cacheClient.getNetwork(networkId);
    }

    @Override
    public void deleteNetwork(String id) {
        restClient.deleteNetwork(id);
        cacheClient.deleteNetwork(id);
        cachedResourceTypes.remove(id);
    }

    @Override
    public void createSubstations(String networkId, List<Resource<SubstationAttributes>> substationResources) {
        ensureCached(ResourceType.SUBSTATION, networkId);
        restClient.createSubstations(networkId, substationResources);
        cacheClient.createSubstations(networkId, substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(String networkId) {
        ensureCached(ResourceType.SUBSTATION, networkId);
        return cacheClient.getSubstations(networkId);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(String networkId, String substationId) {
        ensureCached(ResourceType.SUBSTATION, networkId);
        return cacheClient.getSubstation(networkId, substationId);
    }

    @Override
    public int getSubstationCount(String networkId) {
        ensureCached(ResourceType.SUBSTATION, networkId);
        return cacheClient.getSubstationCount(networkId);
    }

    @Override
    public void createVoltageLevels(String networkId, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkId);
        restClient.createVoltageLevels(networkId, voltageLevelResources);
        cacheClient.createVoltageLevels(networkId, voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(String networkId, String voltageLevelId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkId);
        return cacheClient.getVoltageLevel(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(String networkId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkId);
        return cacheClient.getVoltageLevels(networkId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(String networkId, String substationId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkId);
        return cacheClient.getVoltageLevelsInSubstation(networkId, substationId);
    }

    @Override
    public int getVoltageLevelCount(String networkId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkId);
        return cacheClient.getVoltageLevelCount(networkId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(String networkId, String voltageLevelId) {
        return restClient.getVoltageLevelBusbarSections(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(String networkId, String voltageLevelId) {
        return restClient.getVoltageLevelSwitches(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(String networkId, String voltageLevelId) {
        return restClient.getVoltageLevelGenerators(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(String networkId, String voltageLevelId) {
        return restClient.getVoltageLevelLoads(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(String networkId, String voltageLevelId) {
        return restClient.getVoltageLevelShuntCompensators(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(String networkId, String voltageLevelId) {
        return restClient.getVoltageLevelVscConverterStation(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(String networkId, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkId);
        return cacheClient.getVoltageLevelTwoWindingsTransformers(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(String networkId, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkId);
        return cacheClient.getVoltageLevelLines(networkId, voltageLevelId);
    }

    @Override
    public void createSwitches(String networkId, List<Resource<SwitchAttributes>> switchResources) {
        restClient.createSwitches(networkId, switchResources);
        cacheClient.createSwitches(networkId, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(String networkId) {
        return restClient.getSwitches(networkId);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(String networkId, String switchId) {
        return restClient.getSwitch(networkId, switchId);
    }

    @Override
    public int getSwitchCount(String networkId) {
        return restClient.getSwitchCount(networkId);
    }

    @Override
    public void createBusbarSections(String networkId, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        restClient.createBusbarSections(networkId, busbarSectionResources);
        cacheClient.createBusbarSections(networkId, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(String networkId) {
        return restClient.getBusbarSections(networkId);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(String networkId, String busbarSectionId) {
        return restClient.getBusbarSection(networkId, busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(String networkId) {
        return restClient.getBusbarSectionCount(networkId);
    }

    @Override
    public void createLoads(String networkId, List<Resource<LoadAttributes>> loadResources) {
        restClient.createLoads(networkId, loadResources);
        cacheClient.createLoads(networkId, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(String networkId) {
        return restClient.getLoads(networkId);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(String networkId, String loadId) {
        return restClient.getLoad(networkId, loadId);
    }

    @Override
    public int getLoadCount(String networkId) {
        return restClient.getLoadCount(networkId);
    }

    @Override
    public void createGenerators(String networkId, List<Resource<GeneratorAttributes>> generatorResources) {
        restClient.createGenerators(networkId, generatorResources);
        cacheClient.createGenerators(networkId, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(String networkId) {
        return restClient.getGenerators(networkId);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(String networkId, String generatorId) {
        return restClient.getGenerator(networkId, generatorId);
    }

    @Override
    public int getGeneratorCount(String networkId) {
        return restClient.getGeneratorCount(networkId);
    }

    @Override
    public void createTwoWindingsTransformers(String networkId, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkId);
        restClient.createTwoWindingsTransformers(networkId, twoWindingsTransformerResources);
        cacheClient.createTwoWindingsTransformers(networkId, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(String networkId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkId);
        return cacheClient.getTwoWindingsTransformers(networkId);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(String networkId, String twoWindingsTransformerId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkId);
        return cacheClient.getTwoWindingsTransformer(networkId, twoWindingsTransformerId);
    }

    @Override
    public int getTwoWindingsTransformerCount(String networkId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkId);
        return cacheClient.getTwoWindingsTransformerCount(networkId);
    }

    @Override
    public void createLines(String networkId, List<Resource<LineAttributes>> lineResources) {
        ensureCached(ResourceType.LINE, networkId);
        restClient.createLines(networkId, lineResources);
        cacheClient.createLines(networkId, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(String networkId) {
        ensureCached(ResourceType.LINE, networkId);
        return cacheClient.getLines(networkId);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(String networkId, String lineId) {
        ensureCached(ResourceType.LINE, networkId);
        return cacheClient.getLine(networkId, lineId);
    }

    @Override
    public int getLineCount(String networkId) {
        ensureCached(ResourceType.LINE, networkId);
        return cacheClient.getLineCount(networkId);
    }

    @Override
    public void createShuntCompensators(String networkId, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        restClient.createShuntCompensators(networkId, shuntCompensatorResources);
        cacheClient.createShuntCompensators(networkId, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(String networkId) {
        return restClient.getShuntCompensators(networkId);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(String networkId, String shuntCompensatorId) {
        return restClient.getShuntCompensator(networkId, shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(String networkId) {
        return restClient.getShuntCompensatorCount(networkId);
    }

    @Override
    public void createVscConverterStations(String networkId, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        restClient.createVscConverterStations(networkId, vscConverterStationResources);
        cacheClient.createVscConverterStations(networkId, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(String networkId) {
        return restClient.getVscConverterStations(networkId);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(String networkId, String vscConverterStationId) {
        return restClient.getVscConverterStation(networkId, vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(String networkId) {
        return restClient.getVscConverterStationCount(networkId);
    }

    @Override
    public void createStaticVarCompensators(String networkId, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        restClient.createStaticVarCompensators(networkId, svcResources);
        cacheClient.createStaticVarCompensators(networkId, svcResources);
    }

    @Override
    public void createHvdcLines(String networkId, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        restClient.createHvdcLines(networkId, hvdcLineResources);
        cacheClient.createHvdcLines(networkId, hvdcLineResources);
    }

    @Override
    public void flush() {
        restClient.flush();
        cacheClient.flush();
    }
}
