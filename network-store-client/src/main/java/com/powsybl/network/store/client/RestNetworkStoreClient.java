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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class RestNetworkStoreClient implements NetworkStoreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestNetworkStoreClient.class);

    private static final int RESOURCES_CREATION_CHUNK_SIZE = 1000;

    private final RestClient restClient;

    private NetworkStoreClient self = this;

    public RestNetworkStoreClient(RestTemplateBuilder restTemplateBuilder) {
        restClient = new RestClient(restTemplateBuilder.errorHandler(new RestTemplateResponseErrorHandler()).build());
    }

    @Override
    public void setSelf(NetworkStoreClient self) {
        this.self = self;
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

    private <T extends IdentifiableAttributes> void addAttributeSpyer(Resource<T> resource, ResourceUpdater resourceUpdater) {
        resource.setResourceUpdater(resourceUpdater);

        if (resource.getAttributes() instanceof AbstractAttributes) {
            T spiedAttributes = AttributesSpyer.spy(resource.getAttributes(), resource.getType());
            resource.setAttributes(spiedAttributes);
            spiedAttributes.setResource(resource);
        } else {
            resource.getAttributes().setResource(resource);
        }
    }

    private static UUID getNetworkUuid(Object[] uriVariables) {
        if (uriVariables.length == 0) {
            throw new PowsyblException("No uri variables provided");
        }
        if (!(uriVariables[0] instanceof UUID)) {
            throw new PowsyblException("First uri variable is not a network UUID");
        }
        return (UUID) uriVariables[0];
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resources {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Resource<T>> resourceList = restClient.getAll(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} {} resources loaded in {} ms", resourceList.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));

        UUID networkUuid = getNetworkUuid(uriVariables);
        ResourceUpdater resourceUpdater = new ResourceUpdaterImpl(networkUuid, self);
        for (Resource<T> resource : resourceList) {
            addAttributeSpyer(resource, resourceUpdater);
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
            UUID networkUuid = getNetworkUuid(uriVariables);
            ResourceUpdater resourceUpdater = new ResourceUpdaterImpl(networkUuid, self);
            addAttributeSpyer(r, resourceUpdater);
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
    public List<Resource<NetworkAttributes>> getNetworks(int variantNum) {
        return getAll("network", "/networks/{variantNum}", variantNum);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        create("network", "/networks", networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum) {
        return get("network", "/networks/{networkUuid}/{variantNum}", networkUuid, variantNum);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        restClient.delete("/networks/{networkUuid}/{variantNum}", networkUuid, variantNum);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        restClient.delete("/networks/{networkUuid}", networkUuid);
    }

    @Override
    public void updateNetwork(UUID networkUuid, int variantNum, Resource<NetworkAttributes> networkResource) {
        updateAll("network", "/networks/{networkUuid}/{variantNum}", Collections.singletonList(networkResource), networkUuid, variantNum);
    }

    // substation

    @Override
    public void createSubstations(UUID networkUuid, int variantNum, List<Resource<SubstationAttributes>> substationResources) {
        create("substation", "/networks/{networkUuid}/{variantNum}/substations", substationResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return getAll("substation", "/networks/{networkUuid}/{variantNum}/substations", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return get("substation", "/networks/{networkUuid}/{variantNum}/substations/{substationId}", networkUuid, variantNum, substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid, int variantNum) {
        return getTotalCount("substation", "/networks/{networkUuid}/{variantNum}/substations?limit=0", networkUuid, variantNum);
    }

    @Override
    public void removeSubstation(UUID networkUuid, int variantNum, String substationId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/substations/{substationId}", networkUuid, variantNum, substationId);
    }

    @Override
    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        substationsId.forEach(substationId -> removeSubstation(networkUuid, variantNum, substationId));
    }

    // voltage level

    @Override
    public void createVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        create("voltage level", "/networks/{networkUuid}/{variantNum}/voltage-levels", voltageLevelResources, networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return get("voltage level", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return getAll("voltage level", "/networks/{networkUuid}/{variantNum}/voltage-levels", networkUuid, variantNum);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        return getAll("voltage level", "/networks/{networkUuid}/{variantNum}/substations/{substationId}/voltage-levels", networkUuid, variantNum, substationId);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid, int variantNum) {
        return getTotalCount("voltage level", "/networks/{networkUuid}/{variantNum}/voltage-levels?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelsResources) {
        updateAll("voltage level", "/networks/{networkUuid}/{variantNum}/voltage-levels", voltageLevelsResources, networkUuid, variantNum);
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, int variantNum, Resource<VoltageLevelAttributes> voltageLevelResource) {
        updateVoltageLevels(networkUuid, variantNum, Collections.singletonList(voltageLevelResource));
    }

    @Override
    public void removeVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        voltageLevelsId.forEach(voltageLevelId -> removeVoltageLevel(networkUuid, variantNum, voltageLevelId));
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("busbar section", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/busbar-sections", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/busbar-sections/{busBarSectionId}", networkUuid, variantNum, busBarSectionId);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        busBarSectionsId.forEach(busBarSectionId -> removeBusBarSection(networkUuid, variantNum, busBarSectionId));
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("switch", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/switches", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("generator", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/generators", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeGenerator(UUID networkUuid, int variantNum, String generatorId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/generators/{generatorId}", networkUuid, variantNum, generatorId);
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        generatorsId.forEach(generatorId -> removeGenerator(networkUuid, variantNum, generatorId));
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("battery", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/batteries", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBattery(UUID networkUuid, int variantNum, String batteryId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/batteries/{batteryId}", networkUuid, variantNum, batteryId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        batteriesId.forEach(batteryId -> removeBattery(networkUuid, variantNum, batteryId));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("load", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/loads", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("shunt compensator", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/shunt-compensators", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/shunt-compensators/{shuntCompensatorId}", networkUuid, variantNum, shuntCompensatorId);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        shuntCompensatorsId.forEach(shuntCompensatorId -> removeShuntCompensator(networkUuid, variantNum, shuntCompensatorId));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("VSC converter station", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/vsc-converter-stations", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/vsc-converter-stations/{vscConverterStationId}", networkUuid, variantNum, vscConverterStationId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        vscConverterStationsId.forEach(vscConverterStationId -> removeVscConverterStation(networkUuid, variantNum, vscConverterStationId));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("static var compensator", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/static-var-compensators", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/static-var-compensators/{staticVarCompensatorId}", networkUuid, variantNum, staticVarCompensatorId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        staticVarCompensatorsId.forEach(staticVarCompensatorId -> removeStaticVarCompensator(networkUuid, variantNum, staticVarCompensatorId));
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("LCC converter station", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/lcc-converter-stations", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/lcc-converter-stations/{lccConverterStationId}", networkUuid, variantNum, lccConverterStationId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        lccConverterStationsId.forEach(lccConverterStationId -> removeLccConverterStation(networkUuid, variantNum, lccConverterStationId));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("2 windings transformer", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/2-windings-transformers", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/2-windings-transformers/{twoWindingsTransformerId}", networkUuid, variantNum, twoWindingsTransformerId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        twoWindingsTransformersId.forEach(twoWindingsTransformerId -> removeTwoWindingsTransformer(networkUuid, variantNum, twoWindingsTransformerId));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("3 windings transformer", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/3-windings-transformers", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/3-windings-transformers/{threeWindingsTransformerId}", networkUuid, variantNum, threeWindingsTransformerId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        threeWindingsTransformersId.forEach(threeWindingsTransformerId -> removeThreeWindingsTransformer(networkUuid, variantNum, threeWindingsTransformerId));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("line", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/lines", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLine(UUID networkUuid, int variantNum, String lineId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/lines/{lineId}", networkUuid, variantNum, lineId);
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        linesId.forEach(lineId -> removeLine(networkUuid, variantNum, lineId));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("dangling line", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/dangling-lines", networkUuid, variantNum, voltageLevelId);
    }

    // switch

    @Override
    public void createSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources) {
        create("switch", "/networks/{networkUuid}/{variantNum}/switches", switchResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return getAll("switch", "/networks/{networkUuid}/{variantNum}/switches", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return get("switch", "/networks/{networkUuid}/{variantNum}/switches/{switchId}", networkUuid, variantNum, switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid, int variantNum) {
        return getTotalCount("switch", "/networks/{networkUuid}/{variantNum}/switches?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources) {
        updateAll("switches", "/networks/{networkUuid}/{variantNum}/switches", switchResources, networkUuid, variantNum);
    }

    @Override
    public void updateSwitch(UUID networkUuid, int variantNum, Resource<SwitchAttributes> resource) {
        updateSwitches(networkUuid, variantNum, Collections.singletonList(resource));
    }

    @Override
    public void removeSwitch(UUID networkUuid, int variantNum, String switchId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/switches/{switchId}", networkUuid, variantNum, switchId);
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        switchesId.forEach(switchId -> removeSwitch(networkUuid, variantNum, switchId));
    }

    // busbar section

    @Override
    public void createBusbarSections(UUID networkUuid, int variantNum, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        create("busbar section", "/networks/{networkUuid}/{variantNum}/busbar-sections", busbarSectionResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return getAll("busbar section", "/networks/{networkUuid}/{variantNum}/busbar-sections", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return get("busbar section", "/networks/{networkUuid}/{variantNum}/busbar-sections/{busbarSectionId}", networkUuid, variantNum, busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid, int variantNum) {
        return getTotalCount("busbar section", "/networks/{networkUuid}/{variantNum}/busbar-sections?limit=0", networkUuid, variantNum);
    }

    // load

    @Override
    public void createLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources) {
        create("load", "/networks/{networkUuid}/{variantNum}/loads", loadResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return getAll("load", "/networks/{networkUuid}/{variantNum}/loads", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return get("load", "/networks/{networkUuid}/{variantNum}/loads/{loadId}", networkUuid, variantNum, loadId);
    }

    @Override
    public int getLoadCount(UUID networkUuid, int variantNum) {
        return getTotalCount("load", "/networks/{networkUuid}/{variantNum}/loads?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources) {
        updateAll("load", "/networks/{networkUuid}/{variantNum}/loads", loadResources, networkUuid, variantNum);
    }

    @Override
    public void updateLoad(UUID networkUuid, int variantNum, Resource<LoadAttributes> resource) {
        updateLoads(networkUuid, variantNum, Collections.singletonList(resource));
    }

    @Override
    public void removeLoad(UUID networkUuid, int variantNum, String loadId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/loads/{loadId}", networkUuid, variantNum, loadId);
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        loadsId.forEach(loadId -> removeLoad(networkUuid, variantNum, loadId));
    }

    // generator

    @Override
    public void createGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources) {
        create("generator", "/networks/{networkUuid}/{variantNum}/generators", generatorResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return getAll("generator", "/networks/{networkUuid}/{variantNum}/generators", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return get("generator", "/networks/{networkUuid}/{variantNum}/generators/{generatorId}", networkUuid, variantNum, generatorId);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid, int variantNum) {
        return getTotalCount("generator", "/networks/{networkUuid}/{variantNum}/generators?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources) {
        updateAll("generator", "/networks/{networkUuid}/{variantNum}/generators", generatorResources, networkUuid, variantNum);
    }

    @Override
    public void updateGenerator(UUID networkUuid, int variantNum, Resource<GeneratorAttributes> resource) {
        updateGenerators(networkUuid, variantNum, Collections.singletonList(resource));
    }

    // battery

    @Override
    public void createBatteries(UUID networkUuid, int variantNum, List<Resource<BatteryAttributes>> batteryResources) {
        create("battery", "/networks/{networkUuid}/{variantNum}/batteries", batteryResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return getAll("battery", "/networks/{networkUuid}/{variantNum}/batteries", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return get("battery", "/networks/{networkUuid}/{variantNum}/batteries/{batteryId}", networkUuid, variantNum, batteryId);
    }

    @Override
    public int getBatteryCount(UUID networkUuid, int variantNum) {
        return getTotalCount("battery", "/networks/{networkUuid}/{variantNum}/batteries?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateBatteries(UUID networkUuid, int variantNum, List<Resource<BatteryAttributes>> batteryResources) {
        updateAll("battery", "/networks/{networkUuid}/{variantNum}/batteries", batteryResources, networkUuid, variantNum);
    }

    @Override
    public void updateBattery(UUID networkUuid, int variantNum, Resource<BatteryAttributes> resource) {
        updateBatteries(networkUuid, variantNum, Collections.singletonList(resource));
    }

    // 2 windings transformer

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        create("2 windings transformer", "/networks/{networkUuid}/{variantNum}/2-windings-transformers", twoWindingsTransformerResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return getAll("2 windings transformer", "/networks/{networkUuid}/{variantNum}/2-windings-transformers", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return get("2 windings transformer", "/networks/{networkUuid}/{variantNum}/2-windings-transformers/{twoWindingsTransformerId}", networkUuid, variantNum, twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        updateAll("2 windings transformer", "/networks/{networkUuid}/{variantNum}/2-windings-transformers", twoWindingsTransformerResources, networkUuid, variantNum);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, int variantNum, Resource<TwoWindingsTransformerAttributes> resource) {
        updateTwoWindingsTransformers(networkUuid, variantNum, Collections.singletonList(resource));
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid, int variantNum) {
        return getTotalCount("2 windings transformer", "/networks/{networkUuid}/{variantNum}/2-windings-transformers?limit=0", networkUuid, variantNum);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        create("3 windings transformer", "/networks/{networkUuid}/{variantNum}/3-windings-transformers", threeWindingsTransformerResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        return getAll("3 windings transformer", "/networks/{networkUuid}/{variantNum}/3-windings-transformers", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        return get("3 windings transformer", "/networks/{networkUuid}/{variantNum}/3-windings-transformers/{threeWindingsTransformerId}", networkUuid, variantNum, threeWindingsTransformerId);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid, int variantNum) {
        return getTotalCount("3 windings transformer", "/networks/{networkUuid}/{variantNum}/3-windings-transformers?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        updateAll("3 windings transformer", "/networks/{networkUuid}/{variantNum}/3-windings-transformers", threeWindingsTransformerResources, networkUuid, variantNum);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, int variantNum, Resource<ThreeWindingsTransformerAttributes> resource) {
        updateThreeWindingsTransformers(networkUuid, variantNum, Collections.singletonList(resource));
    }

    // line

    @Override
    public void createLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResources) {
        create("line", "/networks/{networkUuid}/{variantNum}/lines", lineResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return getAll("line", "/networks/{networkUuid}/{variantNum}/lines", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return get("line", "/networks/{networkUuid}/{variantNum}/lines/{lineId}", networkUuid, variantNum, lineId);
    }

    @Override
    public void updateLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResources) {
        updateAll("line", "/networks/{networkUuid}/{variantNum}/lines", lineResources, networkUuid, variantNum);
    }

    @Override
    public void updateLine(UUID networkUuid, int variantNum, Resource<LineAttributes> resource) {
        updateLines(networkUuid, variantNum, Collections.singletonList(resource));
    }

    @Override
    public int getLineCount(UUID networkUuid, int variantNum) {
        return getTotalCount("line", "/networks/{networkUuid}/{variantNum}/lines?limit=0", networkUuid, variantNum);
    }

    // shunt compensator

    @Override
    public void createShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        create("shunt compensator", "/networks/{networkUuid}/{variantNum}/shunt-compensators", shuntCompensatorResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return getAll("shunt compensator", "/networks/{networkUuid}/{variantNum}/shunt-compensators", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return get("shunt compensator", "/networks/{networkUuid}/{variantNum}/shunt-compensators/{shuntCompensatorId}", networkUuid, variantNum, shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid, int variantNum) {
        return getTotalCount("shunt compensator", "/networks/{networkUuid}/{variantNum}/shunt-compensators?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        updateAll("shunt compensator", "/networks/{networkUuid}/{variantNum}/shunt-compensators", shuntCompensatorResources, networkUuid, variantNum);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, int variantNum, Resource<ShuntCompensatorAttributes> resource) {
        updateShuntCompensators(networkUuid, variantNum, Collections.singletonList(resource));
    }

    // VSC converter station

    @Override
    public void createVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        create("VSC converter station", "/networks/{networkUuid}/{variantNum}/vsc-converter-stations", vscConverterStationResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return getAll("VSC converter station", "/networks/{networkUuid}/{variantNum}/vsc-converter-stations", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return get("VSC converter station", "/networks/{networkUuid}/{variantNum}/vsc-converter-stations/{vscConverterStationId}", networkUuid, variantNum, vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid, int variantNum) {
        return getTotalCount("VSC converter station", "/networks/{networkUuid}/{variantNum}/vsc-converter-stations?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        updateAll("VSC converter station", "/networks/{networkUuid}/{variantNum}/vsc-converter-stations", vscConverterStationResources, networkUuid, variantNum);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, int variantNum, Resource<VscConverterStationAttributes> resource) {
        updateVscConverterStations(networkUuid, variantNum, Collections.singletonList(resource));
    }

    // LCC converter station

    @Override
    public void createLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        create("LCC converter station", "/networks/{networkUuid}/{variantNum}/lcc-converter-stations", lccConverterStationResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return getAll("LCC converter station", "/networks/{networkUuid}/{variantNum}/lcc-converter-stations", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return get("LCC converter station", "/networks/{networkUuid}/{variantNum}/lcc-converter-stations/{vscConverterStationId}", networkUuid, variantNum, lccConverterStationId);
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid, int variantNum) {
        return getTotalCount("LCC converter station", "/networks/{networkUuid}/{variantNum}/lcc-converter-stations?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        updateAll("LCC converter station", "/networks/{networkUuid}/{variantNum}/lcc-converter-stations", lccConverterStationResources, networkUuid, variantNum);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, int variantNum, Resource<LccConverterStationAttributes> resource) {
        updateLccConverterStations(networkUuid, variantNum, Collections.singletonList(resource));
    }

    // SVC

    @Override
    public void createStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        create("static var compensator", "/networks/{networkUuid}/{variantNum}/static-var-compensators", staticVarCompensatorResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return getAll("static var compensator", "/networks/{networkUuid}/{variantNum}/static-var-compensators", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return get("static compensator", "/networks/{networkUuid}/{variantNum}/static-var-compensators/{staticVarCompensatorId}", networkUuid, variantNum, staticVarCompensatorId);
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid, int variantNum) {
        return getTotalCount("static var compensator", "/networks/{networkUuid}/{variantNum}/static-var-compensators?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        updateAll("static var compensator", "/networks/{networkUuid}/{variantNum}/static-var-compensators", staticVarCompensatorResources, networkUuid, variantNum);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, int variantNum, Resource<StaticVarCompensatorAttributes> resource) {
        updateStaticVarCompensators(networkUuid, variantNum, Collections.singletonList(resource));
    }

    // HVDC line

    @Override
    public void createHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        create("hvdc line", "/networks/{networkUuid}/{variantNum}/hvdc-lines", hvdcLineResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return getAll("hvdc line", "/networks/{networkUuid}/{variantNum}/hvdc-lines", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return get("hvdc line", "/networks/{networkUuid}/{variantNum}/hvdc-lines/{hvdcLineId}", networkUuid, variantNum, hvdcLineId);
    }

    @Override
    public void removeHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/hvdc-lines/{hvdcLineId}", networkUuid, variantNum, hvdcLineId);
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        hvdcLinesId.forEach(hvdcLineId -> removeHvdcLine(networkUuid, variantNum, hvdcLineId));
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid, int variantNum) {
        return getTotalCount("hvdc line", "/networks/{networkUuid}/{variantNum}/hvdc-lines?limit=0", networkUuid, variantNum);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        updateAll("hvdc line", "/networks/{networkUuid}/{variantNum}/hvdc-lines", hvdcLineResources, networkUuid, variantNum);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, int variantNum, Resource<HvdcLineAttributes> resource) {
        updateHvdcLines(networkUuid, variantNum, Collections.singletonList(resource));
    }

    // Dangling line

    @Override
    public void createDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        create("dangling line", "/networks/{networkUuid}/{variantNum}/dangling-lines", danglingLineResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return getAll("dangling line", "/networks/{networkUuid}/{variantNum}/dangling-lines", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return get("dangling line", "/networks/{networkUuid}/{variantNum}/dangling-lines/{danglingLineId}", networkUuid, variantNum, danglingLineId);
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid, int variantNum) {
        return getTotalCount("dangling line", "/networks/{networkUuid}/{variantNum}/dangling-lines?limit=0", networkUuid, variantNum);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/dangling-lines/{danglingLineId}", networkUuid, variantNum, danglingLineId);
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        danglingLinesId.forEach(danglingLineId -> removeDanglingLine(networkUuid, variantNum, danglingLineId));
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        updateAll("dangling line", "/networks/{networkUuid}/{variantNum}/dangling-lines", danglingLineResources, networkUuid, variantNum);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, int variantNum, Resource<DanglingLineAttributes> resource) {
        updateDanglingLines(networkUuid, variantNum, Collections.singletonList(resource));
    }

    //ConfiguredBus

    @Override
    public void createConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesResources) {
        create("bus", "/networks/{networkUuid}/{variantNum}/configured-buses", busesResources, networkUuid, variantNum);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return getAll("bus", "/networks/{networkUuid}/{variantNum}/configured-buses", networkUuid, variantNum);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("bus", "/networks/{networkUuid}/{variantNum}/voltage-level/{voltageLevelId}/configured-buses", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return get("bus", "/networks/{networkUuid}/{variantNum}/configured-buses/{busId}", networkUuid, variantNum, busId);
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesResources) {
        updateAll("bus", "/networks/{networkUuid}/{variantNum}/configured-buses", busesResources, networkUuid, variantNum);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, int variantNum, Resource<ConfiguredBusAttributes> resource) {
        updateConfiguredBuses(networkUuid, variantNum, Collections.singletonList(resource));
    }

    @Override
    public void removeConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/configured-buses/{busId}", networkUuid, variantNum, busId);
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        busesId.forEach(busId -> removeConfiguredBus(networkUuid, variantNum, busId));
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
