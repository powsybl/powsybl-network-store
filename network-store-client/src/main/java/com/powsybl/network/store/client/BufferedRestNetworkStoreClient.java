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
public class BufferedRestNetworkStoreClient extends ForwardingNetworkStoreClient {

    private final List<Resource<NetworkAttributes>> networkResourcesToFlush = new ArrayList<>();

    private final Map<UUID, Resource<NetworkAttributes>> updateNetworkResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<SubstationAttributes>>> substationResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<VoltageLevelAttributes>>> voltageLevelResourcesToFlush = new HashMap<>();

    private final Map<UUID, List<Resource<VoltageLevelAttributes>>> updateVoltageLevelResourcesToFlush = new HashMap<>();

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

    public BufferedRestNetworkStoreClient(RestNetworkStoreClient delegate) {
        super(delegate);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        networkResourcesToFlush.addAll(networkResources);
    }

    private static <T extends IdentifiableAttributes> void update(List<Resource<T>> updatelistResources, Resource<T> resource) {
        if (!resource.getAttributes().isDirty()) {
            updatelistResources.add(resource);
        }
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        updateNetworkResourcesToFlush.remove(networkUuid);
        substationResourcesToFlush.remove(networkUuid);
        voltageLevelResourcesToFlush.remove(networkUuid);
        updateVoltageLevelResourcesToFlush.remove(networkUuid);
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

        delegate.deleteNetwork(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        substationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(substationResources);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(voltageLevelResources);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(switchResources);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        update(updateSwitchResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(busbarSectionResources);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(loadResources);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        update(updateLoadResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), loadResource);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(generatorResources);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        update(updateGeneratorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), generatorResource);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(twoWindingsTransformerResources);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        update(updateTwoWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), twoWindingsTransformerResource);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(threeWindingsTransformerResources);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        update(updateThreeWindingsTransformerResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), threeWindingsTransformerResource);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(lineResources);
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        update(updateLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), lineResource);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(shuntCompensatorResources);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        update(updateShuntCompensatorResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), shuntCompensatorResource);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(vscConverterStationResources);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        update(updateVscConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), vscConverterStationResource);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(lccConverterStationResources);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        update(updateLccConverterStationResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), lccConverterStationResource);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(svcResources);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        update(updateSvcResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()), staticVarCompensatorResource);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.computeIfAbsent(networkUuid, k -> new ArrayList<>()).addAll(hvdcLineResources);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
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
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
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
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
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
            delegate.createNetworks(networkResourcesToFlush);
            networkResourcesToFlush.clear();
        }
        if (!updateNetworkResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, Resource<NetworkAttributes>> e : updateNetworkResourcesToFlush.entrySet()) {
                delegate.updateNetwork(e.getKey(), e.getValue());
            }
            networkResourcesToFlush.clear();
        }

        flushResources(substationResourcesToFlush, delegate::createSubstations);
        flushResources(voltageLevelResourcesToFlush, delegate::createVoltageLevels);
        flushResources(updateVoltageLevelResourcesToFlush, delegate::updateVoltageLevels);
        flushResources(generatorResourcesToFlush, delegate::createGenerators);
        flushResources(updateGeneratorResourcesToFlush, delegate::updateGenerators);
        flushResources(loadResourcesToFlush, delegate::createLoads);
        flushResources(updateLoadResourcesToFlush, delegate::updateLoads);
        flushResources(busbarSectionResourcesToFlush, delegate::createBusbarSections);
        flushResources(switchResourcesToFlush, delegate::createSwitches);
        flushResources(updateSwitchResourcesToFlush, delegate::updateSwitches);
        flushResources(shuntCompensatorResourcesToFlush, delegate::createShuntCompensators);
        flushResources(updateShuntCompensatorResourcesToFlush, delegate::updateShuntCompensators);
        flushResources(svcResourcesToFlush, delegate::createStaticVarCompensators);
        flushResources(updateSvcResourcesToFlush, delegate::updateStaticVarCompensators);
        flushResources(vscConverterStationResourcesToFlush, delegate::createVscConverterStations);
        flushResources(updateVscConverterStationResourcesToFlush, delegate::updateVscConverterStations);
        flushResources(lccConverterStationResourcesToFlush, delegate::createLccConverterStations);
        flushResources(updateLccConverterStationResourcesToFlush, delegate::updateLccConverterStations);
        flushResources(danglingLineResourcesToFlush, delegate::createDanglingLines);
        flushResources(updateDanglingLineResourcesToFlush, delegate::updateDanglingLines);
        flushResources(hvdcLineResourcesToFlush, delegate::createHvdcLines);
        flushResources(updateHvdcLineResourcesToFlush, delegate::updateHvdcLines);
        flushResources(twoWindingsTransformerResourcesToFlush, delegate::createTwoWindingsTransformers);
        flushResources(updateTwoWindingsTransformerResourcesToFlush, delegate::updateTwoWindingsTransformers);
        flushResources(threeWindingsTransformerResourcesToFlush, delegate::createThreeWindingsTransformers);
        flushResources(updateThreeWindingsTransformerResourcesToFlush, delegate::updateThreeWindingsTransformers);
        flushResources(lineResourcesToFlush, delegate::createLines);
        flushResources(updateLineResourcesToFlush, delegate::updateLines);
        flushResources(busResourcesToFlush, delegate::createConfiguredBuses);
        flushResources(updateBusResourcesToFlush, delegate::updateConfiguredBuses);

        removeResources(danglingLineToRemove, delegate::removeDanglingLines);
    }
}
