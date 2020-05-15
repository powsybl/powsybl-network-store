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
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BufferedRestNetworkStoreClient extends AbstractRestNetworkStoreClient implements NetworkStoreClient {

    private final RestNetworkStoreClient client;

    private final List<Resource<NetworkAttributes>> networkResourcesToFlush = new ArrayList<>();

    private final Map<UUID, List<Resource<SubstationAttributes>>> substationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<VoltageLevelAttributes>>> voltageLevelResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<GeneratorAttributes>>> generatorResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<GeneratorAttributes>>> updateGeneratorResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LoadAttributes>>> loadResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LoadAttributes>>> updateLoadResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<BusbarSectionAttributes>>> busbarSectionResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<SwitchAttributes>>> switchResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<SwitchAttributes>>> updateSwitchResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ShuntCompensatorAttributes>>> shuntCompensatorResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ShuntCompensatorAttributes>>> updateShuntCompensatorResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<VscConverterStationAttributes>>> vscConverterStationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<VscConverterStationAttributes>>> updateVscConverterStationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LccConverterStationAttributes>>> lccConverterStationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LccConverterStationAttributes>>> updateLccConverterStationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<StaticVarCompensatorAttributes>>> svcResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<StaticVarCompensatorAttributes>>> updateSvcResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<HvdcLineAttributes>>> hvdcLineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<HvdcLineAttributes>>> updateHvdcLineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<DanglingLineAttributes>>> danglingLineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<DanglingLineAttributes>>> updateDanglingLineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<String>> danglingLineToRemove = new HashMap<>();

    private final Map<UUID, List<Resource<TwoWindingsTransformerAttributes>>> twoWindingsTransformerResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<TwoWindingsTransformerAttributes>>> updateTwoWindingsTransformerResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ThreeWindingsTransformerAttributes>>> threeWindingsTransformerResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ThreeWindingsTransformerAttributes>>> updateThreeWindingsTransformerResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LineAttributes>>> lineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<LineAttributes>>> updateLineResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ConfiguredBusAttributes>>> busResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<ConfiguredBusAttributes>>> updateBusResourcesToFlush = new HashMap<>();

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

    private static <T extends IdentifiableAttributes> void update(List<Resource<T>> updatelistResources, Resource<T> resource) {
        boolean found = false;
        for (Resource<T> updateResource : updatelistResources) {
            if (updateResource.getId().equals(resource.getId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            updatelistResources.add(resource);
        }
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        substationResourcesToFlush.remove(networkUuid);
        voltageLevelResourcesToFlush.remove(networkUuid);
        generatorResourcesToFlush.remove(networkUuid);
        updateGeneratorResourcesToFlush.remove(networkUuid);
        loadResourcesToFlush.remove(networkUuid);
        updateLoadResourcesToFlush.remove(networkUuid);
        busbarSectionResourcesToFlush.remove(networkUuid);
        switchResourcesToFlush.remove(networkUuid);
        updateSwitchResourcesToFlush.remove(networkUuid);
        shuntCompensatorResourcesToFlush.remove(networkUuid);
        updateShuntCompensatorResourcesToFlush.remove(networkUuid);
        svcResourcesToFlush.remove(networkUuid);
        updateSvcResourcesToFlush.remove(networkUuid);
        vscConverterStationResourcesToFlush.remove(networkUuid);
        updateVscConverterStationResourcesToFlush.remove(networkUuid);
        lccConverterStationResourcesToFlush.remove(networkUuid);
        updateLccConverterStationResourcesToFlush.remove(networkUuid);
        danglingLineResourcesToFlush.remove(networkUuid);
        updateDanglingLineResourcesToFlush.remove(networkUuid);
        hvdcLineResourcesToFlush.remove(networkUuid);
        updateHvdcLineResourcesToFlush.remove(networkUuid);
        twoWindingsTransformerResourcesToFlush.remove(networkUuid);
        updateTwoWindingsTransformerResourcesToFlush.remove(networkUuid);
        threeWindingsTransformerResourcesToFlush.remove(networkUuid);
        updateThreeWindingsTransformerResourcesToFlush.remove(networkUuid);
        lineResourcesToFlush.remove(networkUuid);
        updateLineResourcesToFlush.remove(networkUuid);
        busResourcesToFlush.remove(networkUuid);
        updateBusResourcesToFlush.remove(networkUuid);

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
        return client.getVoltageLevelBusbarSections(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelSwitches(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelGenerators(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelLoads(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelShuntCompensators(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelVscConverterStation(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelLccConverterStation(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelLines(networkUuid, voltageLevelId));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelDanglingLines(networkUuid, voltageLevelId));
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return adapt(client.getSwitches(networkUuid));
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return adapt(client.getSwitch(networkUuid, switchId));
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return client.getSwitchCount(networkUuid);
    }

    @Override
    public void updateSwitches(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        update(updateSwitchResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return client.getBusbarSections(networkUuid);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return client.getBusbarSection(networkUuid, busbarSectionId);
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
        return adapt(client.getLoads(networkUuid));
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return adapt(client.getLoad(networkUuid, loadId));
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return client.getLoadCount(networkUuid);
    }

    @Override
    public void updateLoads(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        update(updateLoadResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), loadResource);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return adapt(client.getGenerators(networkUuid));
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return adapt(client.getGenerator(networkUuid, generatorId));
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return client.getGeneratorCount(networkUuid);
    }

    @Override
    public void updateGenerators(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        update(updateGeneratorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), generatorResource);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return adapt(client.getTwoWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return adapt(client.getTwoWindingsTransformer(networkUuid, twoWindingsTransformerId));
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return client.getTwoWindingsTransformerCount(networkUuid);
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        update(updateTwoWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), twoWindingsTransformerResource);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return adapt(client.getThreeWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return adapt(client.getThreeWindingsTransformer(networkUuid, threeWindingsTransformerId));
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return client.getThreeWindingsTransformerCount(networkUuid);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        update(updateThreeWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), threeWindingsTransformerResource);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return adapt(client.getLines(networkUuid));
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return adapt(client.getLine(networkUuid, lineId));
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return client.getLineCount(networkUuid);
    }

    @Override
    public void updateLines(UUID networkUuid, Resource<LineAttributes> lineResource) {
        update(updateLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), lineResource);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return adapt(client.getShuntCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return adapt(client.getShuntCompensator(networkUuid, shuntCompensatorId));
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return client.getShuntCompensatorCount(networkUuid);
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        update(updateShuntCompensatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), shuntCompensatorResource);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return adapt(client.getVscConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return adapt(client.getVscConverterStation(networkUuid, vscConverterStationId));
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return client.getVscConverterStationCount(networkUuid);
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        update(updateVscConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), vscConverterStationResource);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return adapt(client.getLccConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return adapt(client.getLccConverterStation(networkUuid, lccConverterStationId));
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return client.getLccConverterStationCount(networkUuid);
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        update(updateLccConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), lccConverterStationResource);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return adapt(client.getStaticVarCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return adapt(client.getStaticVarCompensator(networkUuid, staticVarCompensatorId));
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return client.getStaticVarCompensatorCount(networkUuid);
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        update(updateSvcResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), staticVarCompensatorResource);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return adapt(client.getHvdcLines(networkUuid));
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return adapt(client.getHvdcLine(networkUuid, hvdcLineId));
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return client.getHvdcLineCount(networkUuid);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        update(updateHvdcLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), hvdcLineResource);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        if (danglingLineToRemove.get(networkUuid) != null) {
            int dlToRemoveSize = danglingLineToRemove.size();
            danglingLineResources.forEach(danglingLineResource -> danglingLineToRemove.get(networkUuid).remove(danglingLineResource.getId()));
            if (dlToRemoveSize != danglingLineToRemove.size()) {
                return;
            }
        }
        danglingLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return adapt(client.getDanglingLines(networkUuid));
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return adapt(client.getDanglingLine(networkUuid, danglingLineId));
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return client.getDanglingLineCount(networkUuid);
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        update(updateDanglingLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        if (updateDanglingLineResourcesToFlush.get(networkUuid) != null) {
            List<Resource<DanglingLineAttributes>> dlToRemove =
                    updateDanglingLineResourcesToFlush.get(networkUuid).stream().filter(danglingLineAttributesResource -> danglingLineAttributesResource.getId().equals(danglingLineId)).collect(Collectors.toList());
            dlToRemove.forEach(danglingLineAttributesResource -> updateDanglingLineResourcesToFlush.get(networkUuid).remove(danglingLineAttributesResource));
        }

        if (danglingLineResourcesToFlush.get(networkUuid) != null) {
            List<Resource<DanglingLineAttributes>> dlToRemove =
                    danglingLineResourcesToFlush.get(networkUuid).stream().filter(danglingLineAttributesResource -> danglingLineAttributesResource.getId().equals(danglingLineId)).collect(Collectors.toList());
            dlToRemove.forEach(danglingLineAttributesResource -> danglingLineResourcesToFlush.get(networkUuid).remove(danglingLineAttributesResource));
            if (!dlToRemove.isEmpty()) {
                return;
            }
        }
        danglingLineToRemove.computeIfAbsent(networkUuid, k -> new ArrayList<>()).add(danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        busResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(busesRessources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return adapt(client.getConfiguredBuses(networkUuid));
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return adapt(client.getVoltageLevelConfiguredBuses(networkUuid, voltageLevelId));
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return adapt(client.getConfiguredBus(networkUuid, busId));
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        update(updateBusResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), busesResource);
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

    private static void removeResources(Map<UUID, List<String>> resourcesToDelete,
                                                                          BiConsumer<UUID, List<String>> deleteFct) {
        if (!resourcesToDelete.isEmpty()) {
            for (Map.Entry<UUID, List<String>> e : resourcesToDelete.entrySet()) {
                deleteFct.accept(e.getKey(), e.getValue());
            }
            resourcesToDelete.clear();
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
        flushResources(updateGeneratorResourcesToFlush, client::updateGenerators);
        flushResources(loadResourcesToFlush, client::createLoads);
        flushResources(updateLoadResourcesToFlush, client::updateLoads);
        flushResources(busbarSectionResourcesToFlush, client::createBusbarSections);
        flushResources(switchResourcesToFlush, client::createSwitches);
        flushResources(updateSwitchResourcesToFlush, client::updateSwitches);
        flushResources(shuntCompensatorResourcesToFlush, client::createShuntCompensators);
        flushResources(updateShuntCompensatorResourcesToFlush, client::updateShuntCompensators);
        flushResources(svcResourcesToFlush, client::createStaticVarCompensators);
        flushResources(updateSvcResourcesToFlush, client::updateStaticVarCompensators);
        flushResources(vscConverterStationResourcesToFlush, client::createVscConverterStations);
        flushResources(updateVscConverterStationResourcesToFlush, client::updateVscConverterStations);
        flushResources(lccConverterStationResourcesToFlush, client::createLccConverterStations);
        flushResources(updateLccConverterStationResourcesToFlush, client::updateLccConverterStations);
        flushResources(danglingLineResourcesToFlush, client::createDanglingLines);
        flushResources(updateDanglingLineResourcesToFlush, client::updateDanglingLines);
        flushResources(hvdcLineResourcesToFlush, client::createHvdcLines);
        flushResources(updateHvdcLineResourcesToFlush, client::updateHvdcLines);
        flushResources(twoWindingsTransformerResourcesToFlush, client::createTwoWindingsTransformers);
        flushResources(updateTwoWindingsTransformerResourcesToFlush, client::updateTwoWindingsTransformers);
        flushResources(threeWindingsTransformerResourcesToFlush, client::createThreeWindingsTransformers);
        flushResources(updateThreeWindingsTransformerResourcesToFlush, client::updateThreeWindingsTransformers);
        flushResources(lineResourcesToFlush, client::createLines);
        flushResources(updateLineResourcesToFlush, client::updateLines);
        flushResources(busResourcesToFlush, client::createConfiguredBuses);
        flushResources(updateBusResourcesToFlush, client::updateConfiguredBuses);

        removeResources(danglingLineToRemove, client::removeDanglingLines);
    }
}
