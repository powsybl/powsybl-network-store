/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.iidm.impl.ResourceUpdaterImpl;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class RestNetworkStoreClient implements NetworkStoreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestNetworkStoreClient.class);

    private static final int RESOURCES_CREATION_CHUNK_SIZE = 1000;

    private final RestClient restClient;

    private ResourceUpdater resourceUpdater;

    public RestNetworkStoreClient(RestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient);
        resourceUpdater = new ResourceUpdaterImpl(this);
    }

    @Override
    public void setSelf(NetworkStoreClient self) {
        resourceUpdater = new ResourceUpdaterImpl(self);
    }

    // network

    private <T extends IdentifiableAttributes> void create(String target, String url, List<Resource<T>> resourceList, Object... uriVariables) {
        for (List<Resource<T>> resourcePartition : Lists.partition(resourceList, RESOURCES_CREATION_CHUNK_SIZE)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Creating {} {} resources ({})...", resourcePartition.size(), target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
            }
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                restClient.create(url, resourcePartition, uriVariables);
            } catch (ResourceAccessException e) {
                LOGGER.error(e.toString(), e);
                // retry only one time
                LOGGER.info("Retrying...");
                restClient.create(url, resourcePartition, uriVariables);
            }
            stopwatch.stop();
            LOGGER.info("{} {} resources created in {} ms", resourcePartition.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private <T extends IdentifiableAttributes> void addAttributeSpyer(Resource<T> resource) {
        resource.setResourceUpdater(resourceUpdater);

        if (resource.getAttributes() instanceof AbstractAttributes) {
            T spiedAttributes = AttributesSpyer.spy(resource.getAttributes(), resource.getType());
            resource.setAttributes(spiedAttributes);
            spiedAttributes.setResource(resource);
        } else {
            resource.getAttributes().setResource(resource);
        }
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resources {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Resource<T>> resourceList = restClient.getAll(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} {} resources loaded in {} ms", resourceList.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        for (Resource<T> resource : resourceList) {
            if (uriVariables.length > 0) {
                if (!(uriVariables[0] instanceof UUID)) {
                    throw new PowsyblException("First uri variable is not a network UUID");
                }
                resource.setNetworkUuid((UUID) uriVariables[0]);
            }

            addAttributeSpyer(resource);
        }

        return resourceList;
    }

    private  <T extends IdentifiableAttributes> Optional<Resource<T>> get(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resource {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<Resource<T>> resource = restClient.get(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} resource loaded in {} ms", target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        resource.ifPresent(r -> {
            if (uriVariables.length == 0) {
                throw new PowsyblException("No uri variables provided");
            }
            if (!(uriVariables[0] instanceof UUID)) {
                throw new PowsyblException("First uri variable is not a network UUID");
            }
            r.setNetworkUuid((UUID) uriVariables[0]);

            addAttributeSpyer(r);
        });
        return resource;
    }

    private int getTotalCount(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resource count {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        int count = restClient.getTotalCount(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} resource count loaded in {} ms", target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return count;
    }

    private <T extends IdentifiableAttributes> void updateAll(String target, String url, List<Resource<T>> resourceList, Object... uriVariables) {
        for (List<Resource<T>> resourcePartition : Lists.partition(resourceList, RESOURCES_CREATION_CHUNK_SIZE)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Updating {} {} resources ({})...", resourcePartition.size(), target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
            }
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                restClient.updateAll(url, resourcePartition, uriVariables);
            } catch (ResourceAccessException e) {
                LOGGER.error(e.toString(), e);
                // retry only one time
                LOGGER.info("Retrying...");
                restClient.updateAll(url, resourcePartition, uriVariables);
            }
            stopwatch.stop();
            LOGGER.info("{} {} resources updated in {} ms", resourcePartition.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        List<Resource<NetworkAttributes>> listeRes = getAll("network", "/networks");
        for (Resource<NetworkAttributes> resource : listeRes) {
            resource.setNetworkUuid(resource.getAttributes().getUuid());
        }
        return listeRes;
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        create("network", "/networks", networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        return get("network", "/networks/{networkUuid}", networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        restClient.delete("/networks/{networkUuid}", networkUuid);
    }

    @Override
    public void updateNetwork(UUID networkUuid, Resource<NetworkAttributes> networkResource) {
        updateAll("network", "/networks/{networkUuid}", Collections.singletonList(networkResource), networkUuid);
    }

    // substation

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        create("substation", "/networks/{networkUuid}/substations", substationResources, networkUuid);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return getAll("substation", "/networks/{networkUuid}/substations", networkUuid);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return get("substation", "/networks/{networkUuid}/substations/{substationId}", networkUuid, substationId);
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        updateAll("substation", "/networks/{networkUuid}/substations/", substationResources, networkUuid);
    }

    @Override
    public void updateSubstation(UUID networkUuid, Resource<SubstationAttributes> resource) {
        updateSubstations(networkUuid, Collections.singletonList(resource));
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        return getTotalCount("substation", "/networks/{networkUuid}/substations?limit=0", networkUuid);
    }

    @Override
    public void removeSubstation(UUID networkUuid, String substationId) {
        restClient.delete("/networks/{networkUuid}/substations/{substationId}", networkUuid, substationId);
    }

    @Override
    public void removeSubstations(UUID networkUuid, List<String> substationsId) {
        substationsId.forEach(substationId -> removeSubstation(networkUuid, substationId));
    }

    // voltage level

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        create("voltage level", "/networks/{networkUuid}/voltage-levels", voltageLevelResources, networkUuid);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return get("voltage level", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}", networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return getAll("voltage level", "/networks/{networkUuid}/voltage-levels", networkUuid);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return getAll("voltage level", "/networks/{networkUuid}/substations/{substationId}/voltage-levels", networkUuid, substationId);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        return getTotalCount("voltage level", "/networks/{networkUuid}/voltage-levels?limit=0", networkUuid);
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelsResources) {
        updateAll("voltage level", "/networks/{networkUuid}/voltage-levels", voltageLevelsResources, networkUuid);
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, Resource<VoltageLevelAttributes> voltageLevelResource) {
        updateVoltageLevels(networkUuid, Collections.singletonList(voltageLevelResource));
    }

    @Override
    public void removeVoltageLevel(UUID networkUuid, String voltageLevelId) {
        restClient.delete("/networks/{networkUuid}/voltage-levels/{voltageLevelId}", networkUuid, voltageLevelId);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, List<String> voltageLevelsId) {
        voltageLevelsId.forEach(voltageLevelId -> removeVoltageLevel(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return getAll("busbar section", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/busbar-sections", networkUuid, voltageLevelId);
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, String busBarSectionId) {
        restClient.delete("/networks/{networkUuid}/busbar-sections/{busBarSectionId}", networkUuid, busBarSectionId);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, List<String> busBarSectionsId) {
        busBarSectionsId.forEach(busBarSectionId -> removeBusBarSection(networkUuid, busBarSectionId));
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return getAll("switch", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/switches", networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return getAll("generator", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/generators", networkUuid, voltageLevelId);
    }

    @Override
    public void removeGenerator(UUID networkUuid, String generatorId) {
        restClient.delete("/networks/{networkUuid}/generators/{generatorId}", networkUuid, generatorId);
    }

    @Override
    public void removeGenerators(UUID networkUuid, List<String> generatorsId) {
        generatorsId.forEach(generatorId -> removeGenerator(networkUuid, generatorId));
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, String voltageLevelId) {
        return getAll("battery", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/batteries", networkUuid, voltageLevelId);
    }

    @Override
    public void removeBattery(UUID networkUuid, String batteryId) {
        restClient.delete("/networks/{networkUuid}/batteries/{batteryId}", networkUuid, batteryId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, List<String> batteriesId) {
        batteriesId.forEach(batteryId -> removeBattery(networkUuid, batteryId));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return getAll("load", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/loads", networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return getAll("shunt compensator", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/shunt-compensators", networkUuid, voltageLevelId);
    }

    @Override
    public void removeShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        restClient.delete("/networks/{networkUuid}/shunt-compensators/{shuntCompensatorId}", networkUuid, shuntCompensatorId);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, List<String> shuntCompensatorsId) {
        shuntCompensatorsId.forEach(shuntCompensatorId -> removeShuntCompensator(networkUuid, shuntCompensatorId));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId) {
        return getAll("VSC converter station", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/vsc-converter-stations", networkUuid, voltageLevelId);
    }

    @Override
    public void removeVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        restClient.delete("/networks/{networkUuid}/vsc-converter-stations/{vscConverterStationId}", networkUuid, vscConverterStationId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, List<String> vscConverterStationsId) {
        vscConverterStationsId.forEach(vscConverterStationId -> removeVscConverterStation(networkUuid, vscConverterStationId));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return getAll("static var compensator", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/static-var-compensators", networkUuid, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        restClient.delete("/networks/{networkUuid}/static-var-compensators/{staticVarCompensatorId}", networkUuid, staticVarCompensatorId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, List<String> staticVarCompensatorsId) {
        staticVarCompensatorsId.forEach(staticVarCompensatorId -> removeStaticVarCompensator(networkUuid, staticVarCompensatorId));
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId) {
        return getAll("LCC converter station", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/lcc-converter-stations", networkUuid, voltageLevelId);
    }

    @Override
    public void removeLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        restClient.delete("/networks/{networkUuid}/lcc-converter-stations/{lccConverterStationId}", networkUuid, lccConverterStationId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, List<String> lccConverterStationsId) {
        lccConverterStationsId.forEach(lccConverterStationId -> removeLccConverterStation(networkUuid, lccConverterStationId));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return getAll("2 windings transformer", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/2-windings-transformers", networkUuid, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        restClient.delete("/networks/{networkUuid}/2-windings-transformers/{twoWindingsTransformerId}", networkUuid, twoWindingsTransformerId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, List<String> twoWindingsTransformersId) {
        twoWindingsTransformersId.forEach(twoWindingsTransformerId -> removeTwoWindingsTransformer(networkUuid, twoWindingsTransformerId));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return getAll("3 windings transformer", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/3-windings-transformers", networkUuid, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        restClient.delete("/networks/{networkUuid}/3-windings-transformers/{threeWindingsTransformerId}", networkUuid, threeWindingsTransformerId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, List<String> threeWindingsTransformersId) {
        threeWindingsTransformersId.forEach(threeWindingsTransformerId -> removeThreeWindingsTransformer(networkUuid, threeWindingsTransformerId));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return getAll("line", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/lines", networkUuid, voltageLevelId);
    }

    @Override
    public void removeLine(UUID networkUuid, String lineId) {
        restClient.delete("/networks/{networkUuid}/lines/{lineId}", networkUuid, lineId);
    }

    @Override
    public void removeLines(UUID networkUuid, List<String> linesId) {
        linesId.forEach(lineId -> removeLine(networkUuid, lineId));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return getAll("dangling line", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/dangling-lines", networkUuid, voltageLevelId);
    }

    // switch

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        create("switch", "/networks/{networkUuid}/switches", switchResources, networkUuid);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return getAll("switch", "/networks/{networkUuid}/switches", networkUuid);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return get("switch", "/networks/{networkUuid}/switches/{switchId}", networkUuid, switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return getTotalCount("switch", "/networks/{networkUuid}/switches?limit=0", networkUuid);
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        updateAll("switches", "/networks/{networkUuid}/switches", switchResources, networkUuid);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> resource) {
        updateSwitches(networkUuid, Collections.singletonList(resource));
    }

    @Override
    public void removeSwitch(UUID networkUuid, String switchId) {
        restClient.delete("/networks/{networkUuid}/switches/{switchId}", networkUuid, switchId);
    }

    @Override
    public void removeSwitches(UUID networkUuid, List<String> switchesId) {
        switchesId.forEach(switchId -> removeSwitch(networkUuid, switchId));
    }

    // busbar section

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        create("busbar section", "/networks/{networkUuid}/busbar-sections", busbarSectionResources, networkUuid);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return getAll("busbar section", "/networks/{networkUuid}/busbar-sections", networkUuid);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return get("busbar section", "/networks/{networkUuid}/busbar-sections/{busbarSectionId}", networkUuid, busbarSectionId);
    }

    @Override
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        updateAll("busbar section", "/networks/{networkUuid}/busbar-sections", busbarSectionResources, networkUuid);
    }

    @Override
    public void updateBusbarSection(UUID networkUuid, Resource<BusbarSectionAttributes> resource) {
        updateBusbarSections(networkUuid, Collections.singletonList(resource));
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        return getTotalCount("busbar section", "/networks/{networkUuid}/busbar-sections?limit=0", networkUuid);
    }

    // load

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        create("load", "/networks/{networkUuid}/loads", loadResources, networkUuid);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return getAll("load", "/networks/{networkUuid}/loads", networkUuid);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return get("load", "/networks/{networkUuid}/loads/{loadId}", networkUuid, loadId);
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return getTotalCount("load", "/networks/{networkUuid}/loads?limit=0", networkUuid);
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        updateAll("load", "/networks/{networkUuid}/loads", loadResources, networkUuid);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> resource) {
        updateLoads(networkUuid, Collections.singletonList(resource));
    }

    @Override
    public void removeLoad(UUID networkUuid, String loadId) {
        restClient.delete("/networks/{networkUuid}/loads/{loadId}", networkUuid, loadId);
    }

    @Override
    public void removeLoads(UUID networkUuid, List<String> loadsId) {
        loadsId.forEach(loadId -> removeLoad(networkUuid, loadId));
    }

    // generator

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        create("generator", "/networks/{networkUuid}/generators", generatorResources, networkUuid);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return getAll("generator", "/networks/{networkUuid}/generators", networkUuid);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return get("generator", "/networks/{networkUuid}/generators/{generatorId}", networkUuid, generatorId);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return getTotalCount("generator", "/networks/{networkUuid}/generators?limit=0", networkUuid);
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        updateAll("generator", "/networks/{networkUuid}/generators", generatorResources, networkUuid);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> resource) {
        updateGenerators(networkUuid, Collections.singletonList(resource));
    }

    // battery

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        create("battery", "/networks/{networkUuid}/batteries", batteryResources, networkUuid);
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid) {
        return getAll("battery", "/networks/{networkUuid}/batteries", networkUuid);
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, String batteryId) {
        return get("battery", "/networks/{networkUuid}/batteries/{batteryId}", networkUuid, batteryId);
    }

    @Override
    public int getBatteryCount(UUID networkUuid) {
        return getTotalCount("battery", "/networks/{networkUuid}/batteries?limit=0", networkUuid);
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        updateAll("battery", "/networks/{networkUuid}/batteries", batteryResources, networkUuid);
    }

    @Override
    public void updateBattery(UUID networkUuid, Resource<BatteryAttributes> resource) {
        updateBatteries(networkUuid, Collections.singletonList(resource));
    }

    // 2 windings transformer

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        create("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers", twoWindingsTransformerResources, networkUuid);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return getAll("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers", networkUuid);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return get("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers/{twoWindingsTransformerId}", networkUuid, twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        updateAll("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers", twoWindingsTransformerResources, networkUuid);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> resource) {
        updateTwoWindingsTransformers(networkUuid, Collections.singletonList(resource));
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return getTotalCount("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers?limit=0", networkUuid);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        create("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers", threeWindingsTransformerResources, networkUuid);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return getAll("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers", networkUuid);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return get("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers/{threeWindingsTransformerId}", networkUuid, threeWindingsTransformerId);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return getTotalCount("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers?limit=0", networkUuid);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        updateAll("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers", threeWindingsTransformerResources, networkUuid);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> resource) {
        updateThreeWindingsTransformers(networkUuid, Collections.singletonList(resource));
    }

    // line

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        create("line", "/networks/{networkUuid}/lines", lineResources, networkUuid);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return getAll("line", "/networks/{networkUuid}/lines", networkUuid);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return get("line", "/networks/{networkUuid}/lines/{lineId}", networkUuid, lineId);
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        updateAll("line", "/networks/{networkUuid}/lines", lineResources, networkUuid);
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> resource) {
        updateLines(networkUuid, Collections.singletonList(resource));
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return getTotalCount("line", "/networks/{networkUuid}/lines?limit=0", networkUuid);
    }

    // shunt compensator

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        create("shunt compensator", "/networks/{networkUuid}/shunt-compensators", shuntCompensatorResources, networkUuid);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return getAll("shunt compensator", "/networks/{networkUuid}/shunt-compensators", networkUuid);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return get("shunt compensator", "/networks/{networkUuid}/shunt-compensators/{shuntCompensatorId}", networkUuid, shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return getTotalCount("shunt compensator", "/networks/{networkUuid}/shunt-compensators?limit=0", networkUuid);
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        updateAll("shunt compensator", "/networks/{networkUuid}/shunt-compensators", shuntCompensatorResources, networkUuid);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> resource) {
        updateShuntCompensators(networkUuid, Collections.singletonList(resource));
    }

    // VSC converter station

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        create("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations", vscConverterStationResources, networkUuid);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return getAll("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations", networkUuid);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return get("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations/{vscConverterStationId}", networkUuid, vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return getTotalCount("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations?limit=0", networkUuid);
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        updateAll("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations", vscConverterStationResources, networkUuid);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> resource) {
        updateVscConverterStations(networkUuid, Collections.singletonList(resource));
    }

    // LCC converter station

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        create("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations", lccConverterStationResources, networkUuid);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return getAll("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations", networkUuid);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return get("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations/{vscConverterStationId}", networkUuid, lccConverterStationId);
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return getTotalCount("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations?limit=0", networkUuid);
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        updateAll("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations", lccConverterStationResources, networkUuid);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> resource) {
        updateLccConverterStations(networkUuid, Collections.singletonList(resource));
    }

    // SVC

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        create("static var compensator", "/networks/{networkUuid}/static-var-compensators", staticVarCompensatorResources, networkUuid);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return getAll("static var compensator", "/networks/{networkUuid}/static-var-compensators", networkUuid);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return get("static compensator", "/networks/{networkUuid}/static-var-compensators/{staticVarCompensatorId}", networkUuid, staticVarCompensatorId);
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return getTotalCount("static var compensator", "/networks/{networkUuid}/static-var-compensators?limit=0", networkUuid);
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        updateAll("static var compensator", "/networks/{networkUuid}/static-var-compensators", staticVarCompensatorResources, networkUuid);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> resource) {
        updateStaticVarCompensators(networkUuid, Collections.singletonList(resource));
    }

    // HVDC line

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        create("hvdc line", "/networks/{networkUuid}/hvdc-lines", hvdcLineResources, networkUuid);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return getAll("hvdc line", "/networks/{networkUuid}/hvdc-lines", networkUuid);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return get("hvdc line", "/networks/{networkUuid}/hvdc-lines/{hvdcLineId}", networkUuid, hvdcLineId);
    }

    @Override
    public void removeHvdcLine(UUID networkUuid, String hvdcLineId) {
        restClient.delete("/networks/{networkUuid}/hvdc-lines/{hvdcLineId}", networkUuid, hvdcLineId);
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, List<String> hvdcLinesId) {
        hvdcLinesId.forEach(hvdcLineId -> removeHvdcLine(networkUuid, hvdcLineId));
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return getTotalCount("hvdc line", "/networks/{networkUuid}/hvdc-lines?limit=0", networkUuid);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        updateAll("hvdc line", "/networks/{networkUuid}/hvdc-lines", hvdcLineResources, networkUuid);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> resource) {
        updateHvdcLines(networkUuid, Collections.singletonList(resource));
    }

    // Dangling line

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        create("dangling line", "/networks/{networkUuid}/dangling-lines", danglingLineResources, networkUuid);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return getAll("dangling line", "/networks/{networkUuid}/dangling-lines", networkUuid);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return get("dangling line", "/networks/{networkUuid}/dangling-lines/{danglingLineId}", networkUuid, danglingLineId);
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return getTotalCount("dangling line", "/networks/{networkUuid}/dangling-lines?limit=0", networkUuid);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        restClient.delete("/networks/{networkUuid}/dangling-lines/{danglingLineId}", networkUuid, danglingLineId);
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, List<String> danglingLinesId) {
        danglingLinesId.forEach(danglingLineId -> removeDanglingLine(networkUuid, danglingLineId));
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        updateAll("dangling line", "/networks/{networkUuid}/dangling-lines", danglingLineResources, networkUuid);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> resource) {
        updateDanglingLines(networkUuid, Collections.singletonList(resource));
    }

    //ConfiguredBus

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        create("bus", "/networks/{networkUuid}/configured-buses", busesResources, networkUuid);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return getAll("bus", "/networks/{networkUuid}/configured-buses", networkUuid);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return getAll("bus", "/networks/{networkUuid}/voltage-level/{voltageLevelId}/configured-buses", networkUuid, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return get("bus", "/networks/{networkUuid}/configured-buses/{busId}", networkUuid, busId);
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        updateAll("bus", "/networks/{networkUuid}/configured-buses", busesResources, networkUuid);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> resource) {
        updateConfiguredBuses(networkUuid, Collections.singletonList(resource));
    }

    @Override
    public void removeConfiguredBus(UUID networkUuid, String busId) {
        restClient.delete("/networks/{networkUuid}/configured-buses/{busId}", networkUuid, busId);
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, List<String> busesId) {
        busesId.forEach(busId -> removeConfiguredBus(networkUuid, busId));
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
