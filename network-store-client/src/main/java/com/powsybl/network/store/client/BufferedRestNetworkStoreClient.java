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

    private final Map<UUID, List<Resource<StaticVarCompensatorAttributes>>> svcResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<HvdcLineAttributes>>> hvdcLineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<TwoWindingsTransformerAttributes>>> twoWindingsTransformerResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LineAttributes>>> lineResourcesToFlush = new HashMap<>();

    public BufferedRestNetworkStoreClient(RestNetworkStoreClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        flush();
        return client.getNetworks();
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        networkResourcesToFlush.addAll(networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        flush();
        return client.getNetwork(networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        flush(); // FIXME clear resources belonging to the network instead of flush
        client.deleteNetwork(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        substationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        flush();
        return client.getSubstations(networkUuid);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        flush();
        return client.getSubstation(networkUuid, substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        flush();
        return client.getSubstationCount(networkUuid);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevel(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        flush();
        return client.getVoltageLevels(networkUuid);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        flush();
        return client.getVoltageLevelsInSubstation(networkUuid, substationId);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        flush();
        return client.getVoltageLevelCount(networkUuid);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevelBusbarSections(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevelSwitches(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevelGenerators(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevelLoads(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevelShuntCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevelVscConverterStation(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        flush();
        return client.getVoltageLevelLines(networkUuid, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        flush();
        return client.getSwitches(networkUuid);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        flush();
        return client.getSwitch(networkUuid, switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        flush();
        return client.getSwitchCount(networkUuid);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        flush();
        return client.getBusbarSections(networkUuid);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        flush();
        return client.getBusbarSection(networkUuid, busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        flush();
        return client.getBusbarSectionCount(networkUuid);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        flush();
        return client.getLoads(networkUuid);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        flush();
        return client.getLoad(networkUuid, loadId);
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        flush();
        return client.getLoadCount(networkUuid);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        flush();
        return client.getGenerators(networkUuid);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        flush();
        return client.getGenerator(networkUuid, generatorId);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        flush();
        return client.getGeneratorCount(networkUuid);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        flush();
        return client.getTwoWindingsTransformers(networkUuid);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        flush();
        return client.getTwoWindingsTransformer(networkUuid, twoWindingsTransformerId);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        flush();
        return client.getTwoWindingsTransformerCount(networkUuid);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        flush();
        return client.getLines(networkUuid);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        flush();
        return client.getLine(networkUuid, lineId);
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        flush();
        return client.getLineCount(networkUuid);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        flush();
        return client.getShuntCompensators(networkUuid);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        flush();
        return client.getShuntCompensator(networkUuid, shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        flush();
        return client.getShuntCompensatorCount(networkUuid);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        flush();
        return client.getVscConverterStations(networkUuid);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        flush();
        return client.getVscConverterStation(networkUuid, vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        flush();
        return client.getVscConverterStationCount(networkUuid);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(svcResources);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(hvdcLineResources);
    }

    @Override
    public void flush() {
        if (!networkResourcesToFlush.isEmpty()) {
            client.createNetworks(networkResourcesToFlush);
            networkResourcesToFlush.clear();
        }

        if (!substationResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<SubstationAttributes>>> e : substationResourcesToFlush.entrySet()) {
                client.createSubstations(e.getKey(), e.getValue());
            }
            substationResourcesToFlush.clear();
        }

        if (!voltageLevelResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<VoltageLevelAttributes>>> e : voltageLevelResourcesToFlush.entrySet()) {
                client.createVoltageLevels(e.getKey(), e.getValue());
            }
            voltageLevelResourcesToFlush.clear();
        }

        if (!generatorResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<GeneratorAttributes>>> e : generatorResourcesToFlush.entrySet()) {
                client.createGenerators(e.getKey(), e.getValue());
            }
            generatorResourcesToFlush.clear();
        }

        if (!loadResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<LoadAttributes>>> e : loadResourcesToFlush.entrySet()) {
                client.createLoads(e.getKey(), e.getValue());
            }
            loadResourcesToFlush.clear();
        }

        if (!busbarSectionResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<BusbarSectionAttributes>>> e : busbarSectionResourcesToFlush.entrySet()) {
                client.createBusbarSections(e.getKey(), e.getValue());
            }
            busbarSectionResourcesToFlush.clear();
        }

        if (!switchResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<SwitchAttributes>>> e : switchResourcesToFlush.entrySet()) {
                client.createSwitches(e.getKey(), e.getValue());
            }
            switchResourcesToFlush.clear();
        }

        if (!shuntCompensatorResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<ShuntCompensatorAttributes>>> e : shuntCompensatorResourcesToFlush.entrySet()) {
                client.createShuntCompensators(e.getKey(), e.getValue());
            }
            shuntCompensatorResourcesToFlush.clear();
        }

        if (!svcResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<StaticVarCompensatorAttributes>>> e : svcResourcesToFlush.entrySet()) {
                client.createStaticVarCompensators(e.getKey(), e.getValue());
            }
            svcResourcesToFlush.clear();
        }

        if (!vscConverterStationResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<VscConverterStationAttributes>>> e : vscConverterStationResourcesToFlush.entrySet()) {
                client.createVscConverterStations(e.getKey(), e.getValue());
            }
            vscConverterStationResourcesToFlush.clear();
        }

        if (!hvdcLineResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<HvdcLineAttributes>>> e : hvdcLineResourcesToFlush.entrySet()) {
                client.createHvdcLines(e.getKey(), e.getValue());
            }
            hvdcLineResourcesToFlush.clear();
        }

        if (!twoWindingsTransformerResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<TwoWindingsTransformerAttributes>>> e : twoWindingsTransformerResourcesToFlush.entrySet()) {
                client.createTwoWindingsTransformers(e.getKey(), e.getValue());
            }
            twoWindingsTransformerResourcesToFlush.clear();
        }

        if (!lineResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, List<Resource<LineAttributes>>> e : lineResourcesToFlush.entrySet()) {
                client.createLines(e.getKey(), e.getValue());
            }
            lineResourcesToFlush.clear();
        }
    }
}
