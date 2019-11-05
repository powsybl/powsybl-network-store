/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RestNetworkStoreClient implements NetworkStoreClient {

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

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        return getAll("network", "/networks");
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        create("network", "/networks", networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(String networkId) {
        return get("network", "/networks/{networkId}", networkId);
    }

    @Override
    public void deleteNetwork(String id) {
        resources.delete("/networks/{networkId}", id);
    }

    // substation

    @Override
    public void createSubstations(String networkId, List<Resource<SubstationAttributes>> substationResources) {
        create("substation", "/networks/{networkId}/substations", substationResources, networkId);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(String networkId) {
        return getAll("substation", "/networks/{networkId}/substations", networkId);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(String networkId, String substationId) {
        return get("substation", "/networks/{networkId}/substations/{substationId}", networkId, substationId);
    }

    @Override
    public int getSubstationCount(String networkId) {
        return getTotalCount("substation", "/networks/{networkId}/substations?limit=0", networkId);
    }

    // voltage level

    @Override
    public void createVoltageLevels(String networkId, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        create("voltage level", "/networks/{networkId}/voltage-levels", voltageLevelResources, networkId);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(String networkId, String voltageLevelId) {
        return get("voltage level", "/networks/{networkId}/voltage-levels/{voltageLevelId}", networkId, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(String networkId) {
        return getAll("voltage level", "/networks/{networkId}/voltage-levels", networkId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(String networkId, String substationId) {
        return getAll("voltage level", "/networks/{networkId}/substations/{substationId}/voltage-levels", networkId, substationId);
    }

    @Override
    public int getVoltageLevelCount(String networkId) {
        return getTotalCount("voltage level", "/networks/{networkId}/voltage-levels?limit=0", networkId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(String networkId, String voltageLevelId) {
        return getAll("busbar section", "/networks/{networkId}/voltage-levels/{voltageLevelId}/busbar-sections", networkId, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(String networkId, String voltageLevelId) {
        return getAll("switch", "/networks/{networkId}/voltage-levels/{voltageLevelId}/switches", networkId, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(String networkId, String voltageLevelId) {
        return getAll("generator", "/networks/{networkId}/voltage-levels/{voltageLevelId}/generators", networkId, voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(String networkId, String voltageLevelId) {
        return getAll("load", "/networks/{networkId}/voltage-levels/{voltageLevelId}/loads", networkId, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(String networkId, String voltageLevelId) {
        return getAll("shunt compensator", "/networks/{networkId}/voltage-levels/{voltageLevelId}/shunt-compensators", networkId, voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(String networkId, String voltageLevelId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(String networkId, String voltageLevelId) {
        return getAll("2 windings transformer", "/networks/{networkId}/voltage-levels/{voltageLevelId}/2-windings-transformers", networkId, voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(String networkId, String voltageLevelId) {
        return getAll("line", "/networks/{networkId}/voltage-levels/{voltageLevelId}/lines", networkId, voltageLevelId);
    }

    // switch

    @Override
    public void createSwitches(String networkId, List<Resource<SwitchAttributes>> switchResources) {
        create("switch", "/networks/{networkId}/switches", switchResources, networkId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(String networkId) {
        return getAll("switch", "/networks/{networkId}/switches", networkId);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(String networkId, String switchId) {
        return get("switch", "/networks/{networkId}/switches/{switchId}", networkId, switchId);
    }

    @Override
    public int getSwitchCount(String networkId) {
        return getTotalCount("switch", "/networks/{networkId}/switches?limit=0", networkId);
    }

    // busbar section

    @Override
    public void createBusbarSections(String networkId, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        create("busbar section", "/networks/{networkId}/busbar-sections", busbarSectionResources, networkId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(String networkId) {
        return getAll("busbar section", "/networks/{networkId}/busbar-sections", networkId);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(String networkId, String busbarSectionId) {
        return get("busbar section", "/networks/{networkId}/busbar-sections/{busbarSectionId}", networkId, busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(String networkId) {
        return getTotalCount("busbar section", "/networks/{networkId}/busbar-sections?limit=0", networkId);
    }

    // load

    @Override
    public void createLoads(String networkId, List<Resource<LoadAttributes>> loadResources) {
        create("load", "/networks/{networkId}/loads", loadResources, networkId);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(String networkId) {
        return getAll("load", "/networks/{networkId}/loads", networkId);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(String networkId, String loadId) {
        return get("load", "/networks/{networkId}/loads/{loadId}", networkId, loadId);
    }

    @Override
    public int getLoadCount(String networkId) {
        return getTotalCount("load", "/networks/{networkId}/loads?limit=0", networkId);
    }

    // generator

    @Override
    public void createGenerators(String networkId, List<Resource<GeneratorAttributes>> generatorResources) {
        create("generator", "/networks/{networkId}/generators", generatorResources, networkId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(String networkId) {
        return getAll("generator", "/networks/{networkId}/generators", networkId);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(String networkId, String generatorId) {
        return get("generator", "/networks/{networkId}/generators/{generatorId}", networkId, generatorId);
    }

    @Override
    public int getGeneratorCount(String networkId) {
        return getTotalCount("generator", "/networks/{networkId}/generators?limit=0", networkId);
    }

    // 2 windings transformer

    @Override
    public void createTwoWindingsTransformers(String networkId, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        create("2 windings transformer", "/networks/{networkId}/2-windings-transformers", twoWindingsTransformerResources, networkId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(String networkId) {
        return getAll("2 windings transformer", "/networks/{networkId}/2-windings-transformers", networkId);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(String networkId, String twoWindingsTransformerId) {
        return get("2 windings transformer", "/networks/{networkId}/2-windings-transformers/{twoWindingsTransformerId}", networkId, twoWindingsTransformerId);
    }

    @Override
    public int getTwoWindingsTransformerCount(String networkId) {
        return getTotalCount("2 windings transformer", "/networks/{networkId}/2-windings-transformers?limit=0", networkId);
    }

    // line

    @Override
    public void createLines(String networkId, List<Resource<LineAttributes>> lineResources) {
        create("line", "/networks/{networkId}/lines", lineResources, networkId);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(String networkId) {
        return getAll("line", "/networks/{networkId}/lines", networkId);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(String networkId, String lineId) {
        return get("line", "/networks/{networkId}/lines/{lineId}", networkId, lineId);
    }

    @Override
    public int getLineCount(String networkId) {
        return getTotalCount("line", "/networks/{networkId}/lines?limit=0", networkId);
    }

    // shunt compensator

    @Override
    public void createShuntCompensators(String networkId, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        create("shunt compensator", "/networks/{networkId}/shunt-compensators", shuntCompensatorResources, networkId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(String networkId) {
        return getAll("shunt compensator", "/networks/{networkId}/shunt-compensators", networkId);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(String networkId, String shuntCompensatorId) {
        return get("shunt compensator", "/networks/{networkId}/shunt-compensators/{shuntCompensatorId}", networkId, shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(String networkId) {
        return getTotalCount("shunt compensator", "/networks/{networkId}/shunt-compensators?limit=0", networkId);
    }

    // VSC converter station

    @Override
    public void createVscConverterStations(String networkId, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        // TODO
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(String networkId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(String networkId, String vscConverterStationId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getVscConverterStationCount(String networkId) {
        throw new UnsupportedOperationException("TODO");
    }

    // SVC

    @Override
    public void createStaticVarCompensators(String networkId, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        // TODO
    }

    // HVDC line

    @Override
    public void createHvdcLines(String networkId, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        // TODO
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
