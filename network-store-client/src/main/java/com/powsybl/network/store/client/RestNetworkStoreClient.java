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
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RestNetworkStoreClient extends AbstractRestNetworkStoreClient implements NetworkStoreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestNetworkStoreClient.class);

    private static final int RESOURCES_CREATION_CHUNK_SIZE = 1000;

    private final Resources resources;

    public RestNetworkStoreClient(RestTemplateBuilder restTemplateBuilder) {
        resources = new Resources(restTemplateBuilder.errorHandler(new RestTemplateResponseErrorHandler()).build());
    }

    // network

    private <T extends IdentifiableAttributes> void create(String target, String url, List<Resource<T>> resourceList, Object... uriVariables) {
        for (List<Resource<T>> resourcePartition : Lists.partition(resourceList, RESOURCES_CREATION_CHUNK_SIZE)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Creating {} {} resources ({})...", resourcePartition.size(), target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
            }
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                resources.create(url, resourcePartition, uriVariables);
            } catch (ResourceAccessException e) {
                LOGGER.error(e.toString(), e);
                // retry only one time
                LOGGER.info("Retrying...");
                resources.create(url, resourcePartition, uriVariables);
            }
            stopwatch.stop();
            LOGGER.info("{} {} resources created in {} ms", resourcePartition.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private  <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resources {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Resource<T>> resourceList = resources.getAll(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} {} resources loaded in {} ms", resourceList.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        for (Resource<T> resource : resourceList) {
            if (uriVariables.length > 0) {
                if (!(uriVariables[0] instanceof UUID)) {
                    throw new PowsyblException("First uri variable is not a network UUID");
                }
                resource.setNetworkUuid((UUID) uriVariables[0]);
            }
            resource.getAttributes().setResource(resource);
            resource.setStoreClient(this);
        }

        return resourceList;
    }

    private  <T extends IdentifiableAttributes> Optional<Resource<T>> get(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resource {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<Resource<T>> resource = resources.get(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} resource loaded in {} ms", target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        if (resource.isPresent()) {
            if (uriVariables.length == 0) {
                throw new PowsyblException("No uri variables provided");
            }
            if (!(uriVariables[0] instanceof UUID)) {
                throw new PowsyblException("First uri variable is not a network UUID");
            }
            resource.get().setNetworkUuid((UUID) uriVariables[0]);
            resource.get().setStoreClient(this);
            resource.get().getAttributes().setResource(resource.get());
        }
        return resource;
    }

    private int getTotalCount(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resource count {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        int count = resources.getTotalCount(target, url, uriVariables);
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
                resources.updateAll(url, resourcePartition, uriVariables);
            } catch (ResourceAccessException e) {
                LOGGER.error(e.toString(), e);
                // retry only one time
                LOGGER.info("Retrying...");
                resources.updateAll(url, resourcePartition, uriVariables);
            }
            stopwatch.stop();
            LOGGER.info("{} {} resources updated in {} ms", resourcePartition.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private static <T extends IdentifiableAttributes> List<Resource<T>> adapt(List<Resource<T>> listResources) {
        for (Resource<T> resource : listResources) {
            T attributesSpyer = AttributesSpyer.create(resource.getAttributes(), resource.getType());
            resource.setAttributes(attributesSpyer);
            attributesSpyer.setResource(resource);
        }
        return listResources;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T extends IdentifiableAttributes> Optional<Resource<T>> adapt(Optional<Resource<T>> resource) {
        if (resource.isPresent()) {
            T attributesSpyer = AttributesSpyer.create(resource.get().getAttributes(), resource.get().getType());
            resource.get().setAttributes(attributesSpyer);
            attributesSpyer.setResource(resource.get());
        }
        return resource;
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
        resources.delete("/networks/{networkUuid}", networkUuid);
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
    public int getSubstationCount(UUID networkUuid) {
        return getTotalCount("substation", "/networks/{networkUuid}/substations?limit=0", networkUuid);
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
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return getAll("busbar section", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/busbar-sections", networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("switch", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/switches", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("generator", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/generators", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("load", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/loads", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("shunt compensator", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/shunt-compensators", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("VSC converter station", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/vsc-converter-stations", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("static var compensator", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/static-var-compensators", networkUuid, voltageLevelId));
    }

    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("LCC converter station", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/lcc-converter-stations", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("2 windings transformer", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/2-windings-transformers", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("3 windings transformer", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/3-windings-transformers", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("line", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/lines", networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("dangling line", "/networks/{networkUuid}/voltage-levels/{voltageLevelId}/dangling-lines", networkUuid, voltageLevelId));
    }

    // switch

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        create("switch", "/networks/{networkUuid}/switches", switchResources, networkUuid);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return adapt(getAll("switch", "/networks/{networkUuid}/switches", networkUuid));
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return adapt(get("switch", "/networks/{networkUuid}/switches/{switchId}", networkUuid, switchId));
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return getTotalCount("switch", "/networks/{networkUuid}/switches?limit=0", networkUuid);
    }

    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        updateAll("switches", "/networks/{networkUuid}/switches", switchResources, networkUuid);
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
        return adapt(getAll("load", "/networks/{networkUuid}/loads", networkUuid));
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return adapt(get("load", "/networks/{networkUuid}/loads/{loadId}", networkUuid, loadId));
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return getTotalCount("load", "/networks/{networkUuid}/loads?limit=0", networkUuid);
    }

    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        updateAll("load", "/networks/{networkUuid}/loads", loadResources, networkUuid);
    }

    // generator

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        create("generator", "/networks/{networkUuid}/generators", generatorResources, networkUuid);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return adapt(getAll("generator", "/networks/{networkUuid}/generators", networkUuid));
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return adapt(get("generator", "/networks/{networkUuid}/generators/{generatorId}", networkUuid, generatorId));
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return getTotalCount("generator", "/networks/{networkUuid}/generators?limit=0", networkUuid);
    }

    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        updateAll("generator", "/networks/{networkUuid}/generators", generatorResources, networkUuid);
    }

    // 2 windings transformer

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        create("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers", twoWindingsTransformerResources, networkUuid);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return adapt(getAll("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers", networkUuid));
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return adapt(get("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers/{twoWindingsTransformerId}", networkUuid, twoWindingsTransformerId));
    }

    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        updateAll("2 windings transformer", "/networks/{networkUuid}/2-windings-transformers", twoWindingsTransformerResources, networkUuid);
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
        return adapt(getAll("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers", networkUuid));
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return adapt(get("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers/{threeWindingsTransformerId}", networkUuid, threeWindingsTransformerId));
    }

    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        updateAll("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers", threeWindingsTransformerResources, networkUuid);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return getTotalCount("3 windings transformer", "/networks/{networkUuid}/3-windings-transformers?limit=0", networkUuid);
    }

    // line

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        create("line", "/networks/{networkUuid}/lines", lineResources, networkUuid);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return adapt(getAll("line", "/networks/{networkUuid}/lines", networkUuid));
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return adapt(get("line", "/networks/{networkUuid}/lines/{lineId}", networkUuid, lineId));
    }

    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        updateAll("line", "/networks/{networkUuid}/lines", lineResources, networkUuid);
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
        return adapt(getAll("shunt compensator", "/networks/{networkUuid}/shunt-compensators", networkUuid));
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return adapt(get("shunt compensator", "/networks/{networkUuid}/shunt-compensators/{shuntCompensatorId}", networkUuid, shuntCompensatorId));
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return getTotalCount("shunt compensator", "/networks/{networkUuid}/shunt-compensators?limit=0", networkUuid);
    }

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
        return adapt(getAll("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations", networkUuid));
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return adapt(get("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations/{vscConverterStationId}", networkUuid, vscConverterStationId));
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return getTotalCount("VSC converter station", "/networks/{networkUuid}/vsc-converter-stations?limit=0", networkUuid);
    }

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
        return adapt(getAll("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations", networkUuid));
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return adapt(get("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations/{vscConverterStationId}", networkUuid, lccConverterStationId));
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return getTotalCount("LCC converter station", "/networks/{networkUuid}/lcc-converter-stations?limit=0", networkUuid);
    }

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
        return adapt(getAll("static var compensator", "/networks/{networkUuid}/static-var-compensators", networkUuid));
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return adapt(get("static compensator", "/networks/{networkUuid}/static-var-compensators/{staticVarCompensatorId}", networkUuid, staticVarCompensatorId));
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return getTotalCount("static var compensator", "/networks/{networkUuid}/static-var-compensators?limit=0", networkUuid);
    }

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
        return adapt(getAll("hvdc line", "/networks/{networkUuid}/hvdc-lines", networkUuid));
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return adapt(get("hvdc line", "/networks/{networkUuid}/hvdc-lines/{hvdcLineId}", networkUuid, hvdcLineId));
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return getTotalCount("hvdc line", "/networks/{networkUuid}/hvdc-lines?limit=0", networkUuid);
    }

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
        return adapt(getAll("dangling line", "/networks/{networkUuid}/dangling-lines", networkUuid));
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return adapt(get("dangling line", "/networks/{networkUuid}/dangling-lines/{danglingLineId}", networkUuid, danglingLineId));
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return getTotalCount("dangling line", "/networks/{networkUuid}/dangling-lines?limit=0", networkUuid);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        resources.delete("/networks/{networkUuid}/dangling-lines/{danglingLineId}", networkUuid, danglingLineId);
    }

    public void removeDanglingLines(UUID networkUuid, List<String> danglingLinesId) {
        danglingLinesId.forEach(danglingLineId -> removeDanglingLine(networkUuid, danglingLineId));
    }

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
        return adapt(getAll("bus", "/networks/{networkUuid}/configured-buses", networkUuid));
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return adapt(getAll("bus", "/networks/{networkUuid}/voltage-level/{voltageLevelId}/configured-buses", networkUuid, voltageLevelId));
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return adapt(get("bus", "/networks/{networkUuid}/configured-buses/{busId}", networkUuid, busId));
    }

    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        updateAll("bus", "/networks/{networkUuid}/configured-buses", busesResources, networkUuid);
    }

    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> resource) {
        updateSwitches(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> resource) {
        updateLines(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> resource) {
        updateTwoWindingsTransformers(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> resource) {
        updateThreeWindingsTransformers(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> resource) {
        updateDanglingLines(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> resource) {
        updateGenerators(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> resource) {
        updateStaticVarCompensators(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> resource) {
        updateShuntCompensators(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> resource) {
        updateLccConverterStations(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> resource) {
        updateVscConverterStations(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> resource) {
        updateLoads(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> resource) {
        updateConfiguredBuses(networkUuid, Arrays.asList(resource));
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> resource) {
        updateHvdcLines(networkUuid, Arrays.asList(resource));
    }
}
