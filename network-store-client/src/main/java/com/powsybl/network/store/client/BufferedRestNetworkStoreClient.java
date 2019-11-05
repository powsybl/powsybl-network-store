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

    private final Map<String, List<Resource<SubstationAttributes>>> substationResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<VoltageLevelAttributes>>> voltageLevelResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<GeneratorAttributes>>> generatorResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<LoadAttributes>>> loadResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<BusbarSectionAttributes>>> busbarSectionResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<SwitchAttributes>>> switchResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<ShuntCompensatorAttributes>>> shuntCompensatorResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<VscConverterStationAttributes>>> vscConverterStationResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<StaticVarCompensatorAttributes>>> svcResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<HvdcLineAttributes>>> hvdcLineResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<TwoWindingsTransformerAttributes>>> twoWindingsTransformerResourcesToFlush = new HashMap<>();

    private final Map<String, List<Resource<LineAttributes>>> lineResourcesToFlush = new HashMap<>();

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
    public Optional<Resource<NetworkAttributes>> getNetwork(String networkId) {
        flush();
        return client.getNetwork(networkId);
    }

    @Override
    public void deleteNetwork(String id) {
        flush(); // FIXME clear resources belonging to the network instead of flush
        client.deleteNetwork(id);
    }

    @Override
    public void createSubstations(String networkId, List<Resource<SubstationAttributes>> substationResources) {
        substationResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(String networkId) {
        flush();
        return client.getSubstations(networkId);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(String networkId, String substationId) {
        flush();
        return client.getSubstation(networkId, substationId);
    }

    @Override
    public int getSubstationCount(String networkId) {
        flush();
        return client.getSubstationCount(networkId);
    }

    @Override
    public void createVoltageLevels(String networkId, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevel(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(String networkId) {
        flush();
        return client.getVoltageLevels(networkId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(String networkId, String substationId) {
        flush();
        return client.getVoltageLevelsInSubstation(networkId, substationId);
    }

    @Override
    public int getVoltageLevelCount(String networkId) {
        flush();
        return client.getVoltageLevelCount(networkId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevelBusbarSections(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevelSwitches(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevelGenerators(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevelLoads(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevelShuntCompensators(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevelVscConverterStation(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevelTwoWindingsTransformers(networkId, voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(String networkId, String voltageLevelId) {
        flush();
        return client.getVoltageLevelLines(networkId, voltageLevelId);
    }

    @Override
    public void createSwitches(String networkId, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(String networkId) {
        flush();
        return client.getSwitches(networkId);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(String networkId, String switchId) {
        flush();
        return client.getSwitch(networkId, switchId);
    }

    @Override
    public int getSwitchCount(String networkId) {
        flush();
        return client.getSwitchCount(networkId);
    }

    @Override
    public void createBusbarSections(String networkId, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(String networkId) {
        flush();
        return client.getBusbarSections(networkId);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(String networkId, String busbarSectionId) {
        flush();
        return client.getBusbarSection(networkId, busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(String networkId) {
        flush();
        return client.getBusbarSectionCount(networkId);
    }

    @Override
    public void createLoads(String networkId, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(String networkId) {
        flush();
        return client.getLoads(networkId);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(String networkId, String loadId) {
        flush();
        return client.getLoad(networkId, loadId);
    }

    @Override
    public int getLoadCount(String networkId) {
        flush();
        return client.getLoadCount(networkId);
    }

    @Override
    public void createGenerators(String networkId, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(String networkId) {
        flush();
        return client.getGenerators(networkId);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(String networkId, String generatorId) {
        flush();
        return client.getGenerator(networkId, generatorId);
    }

    @Override
    public int getGeneratorCount(String networkId) {
        flush();
        return client.getGeneratorCount(networkId);
    }

    @Override
    public void createTwoWindingsTransformers(String networkId, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(String networkId) {
        flush();
        return client.getTwoWindingsTransformers(networkId);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(String networkId, String twoWindingsTransformerId) {
        flush();
        return client.getTwoWindingsTransformer(networkId, twoWindingsTransformerId);
    }

    @Override
    public int getTwoWindingsTransformerCount(String networkId) {
        flush();
        return client.getTwoWindingsTransformerCount(networkId);
    }

    @Override
    public void createLines(String networkId, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(String networkId) {
        flush();
        return client.getLines(networkId);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(String networkId, String lineId) {
        flush();
        return client.getLine(networkId, lineId);
    }

    @Override
    public int getLineCount(String networkId) {
        flush();
        return client.getLineCount(networkId);
    }

    @Override
    public void createShuntCompensators(String networkId, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(String networkId) {
        flush();
        return client.getShuntCompensators(networkId);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(String networkId, String shuntCompensatorId) {
        flush();
        return client.getShuntCompensator(networkId, shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(String networkId) {
        flush();
        return client.getShuntCompensatorCount(networkId);
    }

    @Override
    public void createVscConverterStations(String networkId, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(String networkId) {
        flush();
        return client.getVscConverterStations(networkId);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(String networkId, String vscConverterStationId) {
        flush();
        return client.getVscConverterStation(networkId, vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(String networkId) {
        flush();
        return client.getVscConverterStationCount(networkId);
    }

    @Override
    public void createStaticVarCompensators(String networkId, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(svcResources);
    }

    @Override
    public void createHvdcLines(String networkId, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.computeIfAbsent(networkId, k -> new ArrayList<>()).addAll(hvdcLineResources);
    }

    @Override
    public void flush() {
        if (!networkResourcesToFlush.isEmpty()) {
            client.createNetworks(networkResourcesToFlush);
            networkResourcesToFlush.clear();
        }

        if (!substationResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<SubstationAttributes>>> e : substationResourcesToFlush.entrySet()) {
                client.createSubstations(e.getKey(), e.getValue());
            }
            substationResourcesToFlush.clear();
        }

        if (!voltageLevelResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<VoltageLevelAttributes>>> e : voltageLevelResourcesToFlush.entrySet()) {
                client.createVoltageLevels(e.getKey(), e.getValue());
            }
            voltageLevelResourcesToFlush.clear();
        }

        if (!generatorResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<GeneratorAttributes>>> e : generatorResourcesToFlush.entrySet()) {
                client.createGenerators(e.getKey(), e.getValue());
            }
            generatorResourcesToFlush.clear();
        }

        if (!loadResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<LoadAttributes>>> e : loadResourcesToFlush.entrySet()) {
                client.createLoads(e.getKey(), e.getValue());
            }
            loadResourcesToFlush.clear();
        }

        if (!busbarSectionResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<BusbarSectionAttributes>>> e : busbarSectionResourcesToFlush.entrySet()) {
                client.createBusbarSections(e.getKey(), e.getValue());
            }
            busbarSectionResourcesToFlush.clear();
        }

        if (!switchResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<SwitchAttributes>>> e : switchResourcesToFlush.entrySet()) {
                client.createSwitches(e.getKey(), e.getValue());
            }
            switchResourcesToFlush.clear();
        }

        if (!shuntCompensatorResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<ShuntCompensatorAttributes>>> e : shuntCompensatorResourcesToFlush.entrySet()) {
                client.createShuntCompensators(e.getKey(), e.getValue());
            }
            shuntCompensatorResourcesToFlush.clear();
        }

        if (!svcResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<StaticVarCompensatorAttributes>>> e : svcResourcesToFlush.entrySet()) {
                client.createStaticVarCompensators(e.getKey(), e.getValue());
            }
            svcResourcesToFlush.clear();
        }

        if (!vscConverterStationResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<VscConverterStationAttributes>>> e : vscConverterStationResourcesToFlush.entrySet()) {
                client.createVscConverterStations(e.getKey(), e.getValue());
            }
            vscConverterStationResourcesToFlush.clear();
        }

        if (!hvdcLineResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<HvdcLineAttributes>>> e : hvdcLineResourcesToFlush.entrySet()) {
                client.createHvdcLines(e.getKey(), e.getValue());
            }
            hvdcLineResourcesToFlush.clear();
        }

        if (!twoWindingsTransformerResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<TwoWindingsTransformerAttributes>>> e : twoWindingsTransformerResourcesToFlush.entrySet()) {
                client.createTwoWindingsTransformers(e.getKey(), e.getValue());
            }
            twoWindingsTransformerResourcesToFlush.clear();
        }

        if (!lineResourcesToFlush.isEmpty()) {
            for (Map.Entry<String, List<Resource<LineAttributes>>> e : lineResourcesToFlush.entrySet()) {
                client.createLines(e.getKey(), e.getValue());
            }
            lineResourcesToFlush.clear();
        }
    }
}
