/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PreloadingRestNetworkStoreClient extends AbstractRestNetworkStoreClient implements NetworkStoreClient {

    private final BufferedRestNetworkStoreClient restClient;

    private final NetworkCacheHandler cacheHandler = new NetworkCacheHandler();

    private final Map<UUID, Set<ResourceType>> cachedResourceTypes = new HashMap<>();

    public PreloadingRestNetworkStoreClient(RestNetworkStoreClient restStoreClient) {
        this.restClient = new BufferedRestNetworkStoreClient(restStoreClient);
    }

    private void loadToCache(ResourceType resourceType, UUID networkUuid) {
        switch (resourceType) {
            case NETWORK:
                restClient.getNetworks().forEach(network -> cacheHandler.getNetworkCache(network.getAttributes().getUuid()).setNetworkResource(network));
                break;
            case SUBSTATION:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getSubstations(networkUuid));
                break;
            case VOLTAGE_LEVEL:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getVoltageLevels(networkUuid));
                break;
            case LOAD:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getLoads(networkUuid));
                break;
            case GENERATOR:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getGenerators(networkUuid));
                break;
            case SHUNT_COMPENSATOR:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getShuntCompensators(networkUuid));
                break;
            case VSC_CONVERTER_STATION:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getVscConverterStations(networkUuid));
                break;
            case LCC_CONVERTER_STATION:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getLccConverterStations(networkUuid));
                break;
            case STATIC_VAR_COMPENSATOR:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getStaticVarCompensators(networkUuid));
                break;
            case BUSBAR_SECTION:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getBusbarSections(networkUuid));
                break;
            case SWITCH:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getSwitches(networkUuid));
                break;
            case TWO_WINDINGS_TRANSFORMER:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getTwoWindingsTransformers(networkUuid));
                break;
            case THREE_WINDINGS_TRANSFORMER:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getThreeWindingsTransformers(networkUuid));
                break;
            case LINE:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getLines(networkUuid));
                break;
            case HVDC_LINE:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getHvdcLines(networkUuid));
                break;
            case DANGLING_LINE:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getDanglingLines(networkUuid));
                break;
            case CONFIGURED_BUS:
                cacheHandler.getNetworkCache(networkUuid).fillResources(resourceType, restClient.getConfiguredBuses(networkUuid));
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

    private <T extends IdentifiableAttributes> List<Resource<T>> adapt(List<Resource<T>> listResources) {
        listResources.forEach(resource -> resource.setStoreClient(this));
        return listResources;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T extends IdentifiableAttributes> Optional<Resource<T>> adapt(Optional<Resource<T>> resource) {
        if (resource.isPresent()) {
            resource.get().setStoreClient(this);
        }
        return resource;
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        ensureCached(ResourceType.NETWORK, null);
        return cacheHandler.getNetworkCaches().values().stream().map(NetworkCache::getNetworkResource).collect(Collectors.toList());
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        ensureCached(ResourceType.NETWORK, null);
        restClient.createNetworks(networkResources);
        networkResources.forEach(resource -> cacheHandler.getNetworkCache(resource.getAttributes().getUuid()).setNetworkResource(resource));
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        ensureCached(ResourceType.NETWORK, null);
        return Optional.ofNullable(cacheHandler.getNetworkCache(networkUuid).getNetworkResource());
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        restClient.deleteNetwork(networkUuid);
        cacheHandler.invalidateNetworkCache(networkUuid);
        cachedResourceTypes.remove(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        restClient.createSubstations(networkUuid, substationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SUBSTATION, substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SUBSTATION);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SUBSTATION, substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.SUBSTATION);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        restClient.createVoltageLevels(networkUuid, voltageLevelResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.VOLTAGE_LEVEL, voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.VOLTAGE_LEVEL, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.VOLTAGE_LEVEL);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.VOLTAGE_LEVEL, substationId);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.VOLTAGE_LEVEL);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.BUSBAR_SECTION, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.SWITCH, voltageLevelId));
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.GENERATOR, voltageLevelId));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LOAD, voltageLevelId));
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.SHUNT_COMPENSATOR, voltageLevelId));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.STATIC_VAR_COMPENSATOR, voltageLevelId));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.VSC_CONVERTER_STATION, voltageLevelId));
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LCC_CONVERTER_STATION, voltageLevelId));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.TWO_WINDINGS_TRANSFORMER, voltageLevelId));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.THREE_WINDINGS_TRANSFORMER, voltageLevelId));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LINE, voltageLevelId));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.DANGLING_LINE, voltageLevelId));
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        restClient.createSwitches(networkUuid, switchResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SWITCH, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SWITCH));
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SWITCH, switchId));
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.SWITCH);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        restClient.updateSwitch(networkUuid, switchResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.SWITCH, switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        restClient.createBusbarSections(networkUuid, busbarSectionResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.BUSBAR_SECTION, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.BUSBAR_SECTION);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.BUSBAR_SECTION, busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.BUSBAR_SECTION);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        ensureCached(ResourceType.LOAD, networkUuid);
        restClient.createLoads(networkUuid, loadResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LOAD, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LOAD));
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LOAD, loadId));
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.LOAD);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        ensureCached(ResourceType.LOAD, networkUuid);
        restClient.updateLoad(networkUuid, loadResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.LOAD, loadResource);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        restClient.createGenerators(networkUuid, generatorResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.GENERATOR, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.GENERATOR));
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.GENERATOR, generatorId));
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.GENERATOR);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        restClient.updateGenerator(networkUuid, generatorResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.GENERATOR, generatorResource);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        restClient.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.TWO_WINDINGS_TRANSFORMER));
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerId));
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        restClient.updateTwoWindingsTransformer(networkUuid, twoWindingsTransformerResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerResource);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.TWO_WINDINGS_TRANSFORMER);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        restClient.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.THREE_WINDINGS_TRANSFORMER));
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerId));
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        restClient.updateThreeWindingsTransformer(networkUuid, threeWindingsTransformerResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerResource);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.THREE_WINDINGS_TRANSFORMER);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        ensureCached(ResourceType.LINE, networkUuid);
        restClient.createLines(networkUuid, lineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LINE, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        ensureCached(ResourceType.LINE, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LINE));
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LINE, lineId));
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        ensureCached(ResourceType.LINE, networkUuid);
        restClient.updateLine(networkUuid, lineResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.LINE, lineResource);
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        ensureCached(ResourceType.LINE, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.LINE);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        restClient.createShuntCompensators(networkUuid, shuntCompensatorResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SHUNT_COMPENSATOR));
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorId));
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.SHUNT_COMPENSATOR);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        restClient.updateShuntCompensator(networkUuid, shuntCompensatorResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorResource);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        restClient.createVscConverterStations(networkUuid, vscConverterStationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.VSC_CONVERTER_STATION, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.VSC_CONVERTER_STATION));
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.VSC_CONVERTER_STATION, vscConverterStationId));
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.VSC_CONVERTER_STATION);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        restClient.updateVscConverterStation(networkUuid, vscConverterStationResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.VSC_CONVERTER_STATION, vscConverterStationResource);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        restClient.createLccConverterStations(networkUuid, lccConverterStationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LCC_CONVERTER_STATION, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LCC_CONVERTER_STATION));
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LCC_CONVERTER_STATION, lccConverterStationId));
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.LCC_CONVERTER_STATION);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        restClient.updateLccConverterStation(networkUuid, lccConverterStationResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.LCC_CONVERTER_STATION, lccConverterStationResource);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        restClient.createStaticVarCompensators(networkUuid, svcResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.STATIC_VAR_COMPENSATOR, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.STATIC_VAR_COMPENSATOR));
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorId));
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.STATIC_VAR_COMPENSATOR);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        restClient.updateStaticVarCompensator(networkUuid, staticVarCompensatorResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorResource);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        restClient.createHvdcLines(networkUuid, hvdcLineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.HVDC_LINE, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.HVDC_LINE));
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.HVDC_LINE, hvdcLineId));
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.HVDC_LINE);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        restClient.updateHvdcLine(networkUuid, hvdcLineResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.HVDC_LINE, hvdcLineResource);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        restClient.createDanglingLines(networkUuid, danglingLineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.DANGLING_LINE, danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.DANGLING_LINE));
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.DANGLING_LINE, danglingLineId));
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return cacheHandler.getNetworkCache(networkUuid).getResourceCount(ResourceType.DANGLING_LINE);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        restClient.updateDanglingLine(networkUuid, danglingLineResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.DANGLING_LINE, danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        restClient.removeDanglingLine(networkUuid, danglingLineId);
        cacheHandler.getNetworkCache(networkUuid).removeResource(ResourceType.DANGLING_LINE, danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        restClient.createConfiguredBuses(networkUuid, busesResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.CONFIGURED_BUS, busesResources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.CONFIGURED_BUS));
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.CONFIGURED_BUS, voltageLevelId));
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return adapt(cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.CONFIGURED_BUS, busId));
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        restClient.updateConfiguredBus(networkUuid, busesResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.CONFIGURED_BUS, busesResource);
    }

    @Override
    public void flush() {
        restClient.flush();
    }
}
