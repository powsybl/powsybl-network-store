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

    private void removeAll(String url, UUID networkUuid, List<String> ids) {
        for (String id : ids) {
            restClient.delete(url, networkUuid, id);
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

    public void removeSubstations(UUID networkUuid, List<String> substationsId) {
        removeAll("/networks/{networkUuid}/substations/{substationId}", networkUuid, substationsId);
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
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelsResources) {
        updateAll("voltage level", "/networks/{networkUuid}/voltage-levels", voltageLevelsResources, networkUuid);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, List<String> voltageLevelsId) {
        removeAll("/networks/{networkUuid}/voltage-levels/{voltageLevelId}", networkUuid, voltageLevelsId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return getAll("busbar section", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/busbar-sections", networkUuid, voltageLevelId);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, List<String> busBarSectionsId) {
        removeAll("/networks/{networkUuid}/busbar-sections/{busBarSectionId}", networkUuid, busBarSectionsId);
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
    public void removeGenerators(UUID networkUuid, List<String> generatorsId) {
        removeAll("/networks/{networkUuid}/generators/{generatorId}", networkUuid, generatorsId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, String voltageLevelId) {
        return getAll("battery", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/batteries", networkUuid, voltageLevelId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, List<String> batteriesId) {
        removeAll("/networks/{networkUuid}/batteries/{batteryId}", networkUuid, batteriesId);
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
    public void removeShuntCompensators(UUID networkUuid, List<String> shuntCompensatorsId) {
        removeAll("/networks/{networkUuid}/shunt-compensators/{shuntCompensatorId}", networkUuid, shuntCompensatorsId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId) {
        return getAll("VSC converter station", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/vsc-converter-stations", networkUuid, voltageLevelId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, List<String> vscConverterStationsId) {
        removeAll("/networks/{networkUuid}/vsc-converter-stations/{vscConverterStationId}", networkUuid, vscConverterStationsId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return getAll("static var compensator", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/static-var-compensators", networkUuid, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, List<String> staticVarCompensatorsId) {
        removeAll("/networks/{networkUuid}/static-var-compensators/{staticVarCompensatorId}", networkUuid, staticVarCompensatorsId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId) {
        return getAll("LCC converter station", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/lcc-converter-stations", networkUuid, voltageLevelId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, List<String> lccConverterStationsId) {
        removeAll("/networks/{networkUuid}/lcc-converter-stations/{lccConverterStationId}", networkUuid, lccConverterStationsId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return getAll("2 windings transformer", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/2-windings-transformers", networkUuid, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, List<String> twoWindingsTransformersId) {
        removeAll("/networks/{networkUuid}/2-windings-transformers/{twoWindingsTransformerId}", networkUuid, twoWindingsTransformersId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return getAll("3 windings transformer", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/3-windings-transformers", networkUuid, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, List<String> threeWindingsTransformersId) {
        removeAll("/networks/{networkUuid}/3-windings-transformers/{threeWindingsTransformerId}", networkUuid, threeWindingsTransformersId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return getAll("line", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/lines", networkUuid, voltageLevelId);
    }

    @Override
    public void removeLines(UUID networkUuid, List<String> linesId) {
        removeAll("/networks/{networkUuid}/lines/{lineId}", networkUuid, linesId);
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
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        updateAll("switches", "/networks/{networkUuid}/switches", switchResources, networkUuid);
    }

    @Override
    public void removeSwitches(UUID networkUuid, List<String> switchesId) {
        removeAll("/networks/{networkUuid}/switches/{switchId}", networkUuid, switchesId);
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
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        updateAll("load", "/networks/{networkUuid}/loads", loadResources, networkUuid);
    }

    @Override
    public void removeLoads(UUID networkUuid, List<String> loadsId) {
        removeAll("/networks/{networkUuid}/loads/{loadId}", networkUuid, loadsId);
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
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        updateAll("generator", "/networks/{networkUuid}/generators", generatorResources, networkUuid);
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
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        updateAll("battery", "/networks/{networkUuid}/batteries", batteryResources, networkUuid);
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
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        updateAll("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers", threeWindingsTransformerResources, networkUuid);
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
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        updateAll("shunt compensator", "/networks/{networkUuid}/shunt-compensators", shuntCompensatorResources, networkUuid);
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
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        updateAll("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations", vscConverterStationResources, networkUuid);
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
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        updateAll("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations", lccConverterStationResources, networkUuid);
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
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        updateAll("static var compensator", "/networks/{networkUuid}/static-var-compensators", staticVarCompensatorResources, networkUuid);
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
    public void removeHvdcLines(UUID networkUuid, List<String> hvdcLinesId) {
        removeAll("/networks/{networkUuid}/hvdc-lines/{hvdcLineId}", networkUuid, hvdcLinesId);
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
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return getAll("dangling line", "/networks/{networkUuid}/dangling-lines", networkUuid);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return get("dangling line", "/networks/{networkUuid}/dangling-lines/{danglingLineId}", networkUuid, danglingLineId);
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, List<String> danglingLinesId) {
        removeAll("/networks/{networkUuid}/dangling-lines/{danglingLineId}", networkUuid, danglingLinesId);
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
    public void removeConfiguredBuses(UUID networkUuid, List<String> busesId) {
        removeAll("/networks/{networkUuid}/configured-buses/{busId}", networkUuid, busesId);
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
