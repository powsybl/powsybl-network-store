/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
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

    public RestNetworkStoreClient(RestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient);
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

    private <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resources {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Resource<T>> resourceList = restClient.getAll(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} {} resources loaded in {} ms", resourceList.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
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
        return resource;
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

    private void removeAll(String url, UUID networkUuid, int variantNum, List<String> ids) {
        for (String id : ids) {
            restClient.delete(url, networkUuid, variantNum, id);
        }
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        return getAll("network", "/networks");
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
    public void deleteNetwork(UUID networkUuid) {
        restClient.delete("/networks/{networkUuid}", networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        restClient.delete("/networks/{networkUuid}/{variantNum}", networkUuid, variantNum);
    }

    @Override
    public void updateNetworks(List<Resource<NetworkAttributes>> networkResources) {
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            updateAll("network", "/networks/{networkUuid}", Collections.singletonList(networkResource), networkResource.getAttributes().getUuid());
        }
    }

    @Override
    public void cloneNetwork(UUID networkUuid, int sourceVariantNum, int targetVariantNum) {
        // TODO
    }

    // substation

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        create("substation", "/networks/{networkUuid}/substations", substationResources, networkUuid);
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
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        updateAll("substation", "/networks/{networkUuid}/substations/", substationResources, networkUuid);
    }

    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/substations/{substationId}", networkUuid, variantNum, substationsId);
    }

    // voltage level

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        create("voltage level", "/networks/{networkUuid}/voltage-levels", voltageLevelResources, networkUuid);
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
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelsResources) {
        updateAll("voltage level", "/networks/{networkUuid}/voltage-levels", voltageLevelsResources, networkUuid);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}", networkUuid, variantNum, voltageLevelsId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("busbar section", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/busbar-sections", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/busbar-sections/{busBarSectionId}", networkUuid, variantNum, busBarSectionsId);
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
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/generators/{generatorId}", networkUuid, variantNum, generatorsId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("battery", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/batteries", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        removeAll("/networks/{networkUuid}/{variantNum}/batteries/{batteryId}", networkUuid, variantNum, batteriesId);
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
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/shunt-compensators/{shuntCompensatorId}", networkUuid, variantNum, shuntCompensatorsId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("VSC converter station", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/vsc-converter-stations", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/vsc-converter-stations/{vscConverterStationId}", networkUuid, variantNum, vscConverterStationsId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("static var compensator", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/static-var-compensators", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/static-var-compensators/{staticVarCompensatorId}", networkUuid, variantNum, staticVarCompensatorsId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("LCC converter station", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/lcc-converter-stations", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/lcc-converter-stations/{lccConverterStationId}", networkUuid, variantNum, lccConverterStationsId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("2 windings transformer", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/2-windings-transformers", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        removeAll("/networks/{networkUuid}/{variantNum}/2-windings-transformers/{twoWindingsTransformerId}", networkUuid, variantNum, twoWindingsTransformersId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("3 windings transformer", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/3-windings-transformers", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        removeAll("/networks/{networkUuid}/{variantNum}/3-windings-transformers/{threeWindingsTransformerId}", networkUuid, variantNum, threeWindingsTransformersId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("line", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/lines", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        removeAll("/networks/{networkUuid}/{variantNum}/lines/{lineId}", networkUuid, variantNum, linesId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("dangling line", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/dangling-lines", networkUuid, variantNum, voltageLevelId);
    }

    // switch

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        create("switch", "/networks/{networkUuid}/switches", switchResources, networkUuid);
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
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        updateAll("switches", "/networks/{networkUuid}/switches", switchResources, networkUuid);
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        removeAll("/networks/{networkUuid}/{variantNum}/switches/{switchId}", networkUuid, variantNum, switchesId);
    }

    // busbar section

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        create("busbar section", "/networks/{networkUuid}/busbar-sections", busbarSectionResources, networkUuid);
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
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        updateAll("busbar section", "/networks/{networkUuid}/busbar-sections", busbarSectionResources, networkUuid);
    }

    // load

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        create("load", "/networks/{networkUuid}/loads", loadResources, networkUuid);
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
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        updateAll("load", "/networks/{networkUuid}/loads", loadResources, networkUuid);
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        removeAll("/networks/{networkUuid}/{variantNum}/loads/{loadId}", networkUuid, variantNum, loadsId);
    }

    // generator

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        create("generator", "/networks/{networkUuid}/generators", generatorResources, networkUuid);
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
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        updateAll("generator", "/networks/{networkUuid}/generators", generatorResources, networkUuid);
    }

    // battery

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        create("battery", "/networks/{networkUuid}/batteries", batteryResources, networkUuid);
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
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        updateAll("battery", "/networks/{networkUuid}/batteries", batteryResources, networkUuid);
    }

    // 2 windings transformer

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        create("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers", twoWindingsTransformerResources, networkUuid);
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
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        updateAll("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers", twoWindingsTransformerResources, networkUuid);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        create("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers", threeWindingsTransformerResources, networkUuid);
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
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        updateAll("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers", threeWindingsTransformerResources, networkUuid);
    }

    // line

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        create("line", "/networks/{networkUuid}/lines", lineResources, networkUuid);
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
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        updateAll("line", "/networks/{networkUuid}/lines", lineResources, networkUuid);
    }

    // shunt compensator

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        create("shunt compensator", "/networks/{networkUuid}/shunt-compensators", shuntCompensatorResources, networkUuid);
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
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        updateAll("shunt compensator", "/networks/{networkUuid}/shunt-compensators", shuntCompensatorResources, networkUuid);
    }

    // VSC converter station

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        create("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations", vscConverterStationResources, networkUuid);
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
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        updateAll("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations", vscConverterStationResources, networkUuid);
    }

    // LCC converter station

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        create("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations", lccConverterStationResources, networkUuid);
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
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        updateAll("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations", lccConverterStationResources, networkUuid);
    }

    // SVC

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        create("static var compensator", "/networks/{networkUuid}/static-var-compensators", staticVarCompensatorResources, networkUuid);
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
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        updateAll("static var compensator", "/networks/{networkUuid}/static-var-compensators", staticVarCompensatorResources, networkUuid);
    }

    // HVDC line

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        create("hvdc line", "/networks/{networkUuid}/hvdc-lines", hvdcLineResources, networkUuid);
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
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        removeAll("/networks/{networkUuid}/{variantNum}/hvdc-lines/{hvdcLineId}", networkUuid, variantNum, hvdcLinesId);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        updateAll("hvdc line", "/networks/{networkUuid}/hvdc-lines", hvdcLineResources, networkUuid);
    }

    // Dangling line

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        create("dangling line", "/networks/{networkUuid}/dangling-lines", danglingLineResources, networkUuid);
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
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        removeAll("/networks/{networkUuid}/{variantNum}/dangling-lines/{danglingLineId}", networkUuid, variantNum, danglingLinesId);
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        updateAll("dangling line", "/networks/{networkUuid}/dangling-lines", danglingLineResources, networkUuid);
    }

    //ConfiguredBus

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        create("bus", "/networks/{networkUuid}/configured-buses", busesResources, networkUuid);
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
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        updateAll("bus", "/networks/{networkUuid}/configured-buses", busesResources, networkUuid);
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        removeAll("/networks/{networkUuid}/{variantNum}/configured-buses/{busId}", networkUuid, variantNum, busesId);
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
