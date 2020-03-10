/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BufferedRestNetworkStoreClient implements NetworkStoreClient {

    private final RestNetworkStoreClient client;

    private final List<Resource<NetworkAttributes>> networkResourcesToFlush = new ArrayList<>();

    private final Map<UUID, List<Resource<SubstationAttributes>>> substationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<VoltageLevelAttributes>>> voltageLevelResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<GeneratorAttributes>>> generatorResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LoadAttributes>>> loadResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<BusbarSectionAttributes>>> busbarSectionResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<SwitchAttributes>>> switchResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ShuntCompensatorAttributes>>> shuntCompensatorResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<VscConverterStationAttributes>>> vscConverterStationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LccConverterStationAttributes>>> lccConverterStationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<StaticVarCompensatorAttributes>>> svcResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<HvdcLineAttributes>>> hvdcLineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<DanglingLineAttributes>>> danglingLineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<TwoWindingsTransformerAttributes>>> twoWindingsTransformerResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ThreeWindingsTransformerAttributes>>> threeWindingsTransformerResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LineAttributes>>> lineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ConfiguredBusAttributes>>> busResourcesToFlush = new HashMap<>();

    private final Map<UUID, NetworkCache> networksCache = new HashMap<>();

    public BufferedRestNetworkStoreClient(RestNetworkStoreClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        return client.getNetworks();
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        networkResourcesToFlush.addAll(networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        return client.getNetwork(networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        substationResourcesToFlush.remove(networkUuid);
        voltageLevelResourcesToFlush.remove(networkUuid);
        generatorResourcesToFlush.remove(networkUuid);
        loadResourcesToFlush.remove(networkUuid);
        busbarSectionResourcesToFlush.remove(networkUuid);
        switchResourcesToFlush.remove(networkUuid);
        shuntCompensatorResourcesToFlush.remove(networkUuid);
        svcResourcesToFlush.remove(networkUuid);
        vscConverterStationResourcesToFlush.remove(networkUuid);
        lccConverterStationResourcesToFlush.remove(networkUuid);
        danglingLineResourcesToFlush.remove(networkUuid);
        hvdcLineResourcesToFlush.remove(networkUuid);
        twoWindingsTransformerResourcesToFlush.remove(networkUuid);
        lineResourcesToFlush.remove(networkUuid);

        client.deleteNetwork(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        substationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return client.getSubstations(networkUuid);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return client.getSubstation(networkUuid, substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        return client.getSubstationCount(networkUuid);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return client.getVoltageLevel(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return client.getVoltageLevels(networkUuid);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return client.getVoltageLevelsInSubstation(networkUuid, substationId);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        return client.getVoltageLevelCount(networkUuid);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.BUSBAR_SECTION, voltageLevelId, id -> client.getVoltageLevelBusbarSections(networkUuid, id));
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.SWITCH, voltageLevelId, id -> client.getVoltageLevelSwitches(networkUuid, id));
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.GENERATOR, voltageLevelId, id -> client.getVoltageLevelGenerators(networkUuid, id));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.LOAD, voltageLevelId, id -> client.getVoltageLevelLoads(networkUuid, id));
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.SHUNT_COMPENSATOR, voltageLevelId, id -> client.getVoltageLevelShuntCompensators(networkUuid, id));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.STATIC_VAR_COMPENSATOR, voltageLevelId, id -> client.getVoltageLevelStaticVarCompensators(networkUuid, id));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.VSC_CONVERTER_STATION, voltageLevelId, id -> client.getVoltageLevelVscConverterStation(networkUuid, id));
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.LCC_CONVERTER_STATION, voltageLevelId, id -> client.getVoltageLevelLccConverterStation(networkUuid, id));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.TWO_WINDINGS_TRANSFORMER, voltageLevelId, id -> client.getVoltageLevelTwoWindingsTransformers(networkUuid, id));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.THREE_WINDINGS_TRANSFORMER, voltageLevelId, id -> client.getVoltageLevelThreeWindingsTransformers(networkUuid, id));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.LINE, voltageLevelId, id -> client.getVoltageLevelLines(networkUuid, id));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResourcesByVoltageId(ResourceType.DANGLING_LINE, voltageLevelId, id -> client.getVoltageLevelDanglingLines(networkUuid, id));
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.SWITCH, () -> client.getSwitches(networkUuid));
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.SWITCH, switchId, id ->  client.getSwitch(networkUuid, id));
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return client.getSwitchCount(networkUuid);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.BUSBAR_SECTION, () -> client.getBusbarSections(networkUuid));
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.BUSBAR_SECTION, busbarSectionId, s ->  client.getBusbarSection(networkUuid, busbarSectionId));
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        return client.getBusbarSectionCount(networkUuid);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.LOAD, () -> client.getLoads(networkUuid));
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.LOAD, loadId, s ->  client.getLoad(networkUuid, loadId));
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return client.getLoadCount(networkUuid);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.GENERATOR, () -> client.getGenerators(networkUuid));
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.GENERATOR, generatorId, s ->  client.getGenerator(networkUuid, generatorId));
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return client.getGeneratorCount(networkUuid);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.TWO_WINDINGS_TRANSFORMER, () -> client.getTwoWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerId, s ->  client.getTwoWindingsTransformer(networkUuid, twoWindingsTransformerId));
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return client.getTwoWindingsTransformerCount(networkUuid);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.THREE_WINDINGS_TRANSFORMER, () -> client.getThreeWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerId, s ->  client.getThreeWindingsTransformer(networkUuid, threeWindingsTransformerId));
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return client.getThreeWindingsTransformerCount(networkUuid);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.LINE, () -> client.getLines(networkUuid));
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.LINE, lineId, s ->  client.getLine(networkUuid, lineId));
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return client.getLineCount(networkUuid);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.SHUNT_COMPENSATOR, () -> client.getShuntCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorId, s ->  client.getShuntCompensator(networkUuid, shuntCompensatorId));
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return client.getShuntCompensatorCount(networkUuid);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.VSC_CONVERTER_STATION, () -> client.getVscConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.VSC_CONVERTER_STATION, vscConverterStationId, s ->  client.getVscConverterStation(networkUuid, vscConverterStationId));
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return client.getVscConverterStationCount(networkUuid);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.LCC_CONVERTER_STATION, () -> client.getLccConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.LCC_CONVERTER_STATION, lccConverterStationId, s ->  client.getLccConverterStation(networkUuid, lccConverterStationId));
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return client.getLccConverterStationCount(networkUuid);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.STATIC_VAR_COMPENSATOR, () -> client.getStaticVarCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorId, s ->  client.getStaticVarCompensator(networkUuid, staticVarCompensatorId));
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return client.getStaticVarCompensatorCount(networkUuid);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.HVDC_LINE, () -> client.getHvdcLines(networkUuid));
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.HVDC_LINE, hvdcLineId, s ->  client.getHvdcLine(networkUuid, hvdcLineId));
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return client.getHvdcLineCount(networkUuid);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        danglingLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.DANGLING_LINE, () -> client.getDanglingLines(networkUuid));
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.DANGLING_LINE, danglingLineId, s ->  client.getDanglingLine(networkUuid, danglingLineId));
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return client.getDanglingLineCount(networkUuid);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        busResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(busesRessources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getAllResources(ResourceType.CONFIGURED_BUS, () -> client.getConfiguredBuses(networkUuid));
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return client.getConfiguredBuses(networkUuid, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return networksCache.computeIfAbsent(networkUuid, k -> new NetworkCache(k)).getResource(ResourceType.CONFIGURED_BUS, busId, s ->  client.getConfiguredBus(networkUuid, busId));
    }

    private static <T extends IdentifiableAttributes> void flushResources(Map<UUID, List<Resource<T>>> resourcesToFlush,
                                                                          BiConsumer<UUID, List<Resource<T>>> createFct) {
        if (!resourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<T>>> e : resourcesToFlush.entrySet()) {
                createFct.accept(e.getKey(), e.getValue());
            }
            resourcesToFlush.clear();
        }
    }

    @Override
    public void flush() {
        if (!networkResourcesToFlush.isEmpty()) {
            client.createNetworks(networkResourcesToFlush);
            networkResourcesToFlush.clear();
        }

        flushResources(substationResourcesToFlush, client::createSubstations);
        flushResources(voltageLevelResourcesToFlush, client::createVoltageLevels);
        flushResources(generatorResourcesToFlush, client::createGenerators);
        flushResources(loadResourcesToFlush, client::createLoads);
        flushResources(busbarSectionResourcesToFlush, client::createBusbarSections);
        flushResources(switchResourcesToFlush, client::createSwitches);
        flushResources(shuntCompensatorResourcesToFlush, client::createShuntCompensators);
        flushResources(svcResourcesToFlush, client::createStaticVarCompensators);
        flushResources(vscConverterStationResourcesToFlush, client::createVscConverterStations);
        flushResources(lccConverterStationResourcesToFlush, client::createLccConverterStations);
        flushResources(danglingLineResourcesToFlush, client::createDanglingLines);
        flushResources(hvdcLineResourcesToFlush, client::createHvdcLines);
        flushResources(twoWindingsTransformerResourcesToFlush, client::createTwoWindingsTransformers);
        flushResources(threeWindingsTransformerResourcesToFlush, client::createThreeWindingsTransformers);
        flushResources(lineResourcesToFlush, client::createLines);
        flushResources(busResourcesToFlush, client::createConfiguredBuses);
    }
}
