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
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BufferedRestNetworkStoreClient extends ForwardingNetworkStoreClient {

    static class CollectionBuffer<T extends IdentifiableAttributes> {

        private final BiConsumer<UUID, List<Resource<T>>> createFct;

        private final BiConsumer<UUID, List<Resource<T>>> updateFct;

        private final BiConsumer<UUID, List<String>> removeFct;

        private final Map<UUID, Map<String, Resource<T>>> createResources = new HashMap<>();

        private final Map<UUID, Map<String, Resource<T>>> updateResources = new HashMap<>();

        private final Map<UUID, Set<String>> removeResources = new HashMap<>();

        public CollectionBuffer(BiConsumer<UUID, List<Resource<T>>> createFct,
                                BiConsumer<UUID, List<Resource<T>>> updateFct,
                                BiConsumer<UUID, List<String>> removeFct) {
            this.createFct = Objects.requireNonNull(createFct);
            this.updateFct = updateFct;
            this.removeFct = removeFct;
        }

        private Map<String, Resource<T>> getCreateResources(UUID networkUuid) {
            return createResources.computeIfAbsent(networkUuid, k -> new HashMap<>());
        }

        private Map<String, Resource<T>> getUpdateResources(UUID networkUuid) {
            return updateResources.computeIfAbsent(networkUuid, k -> new HashMap<>());
        }

        private Set<String> getRemoveResources(UUID networkUuid) {
            return removeResources.computeIfAbsent(networkUuid, k -> new HashSet<>());
        }

        void create(UUID networkUuid, List<Resource<T>> resources) {
            Map<String, Resource<T>> networkCreateResources = getCreateResources(networkUuid);
            for (Resource<T> resource : resources) {
                networkCreateResources.put(resource.getId(), resource);
            }
        }

        void update(UUID networkUuid, Resource<T> resource) {
            update(networkUuid, Collections.singletonList(resource));
        }

        void update(UUID networkUuid, List<Resource<T>> resources) {
            Map<String, Resource<T>> networkCreateResources = getCreateResources(networkUuid);
            Map<String, Resource<T>> networkUpdateResources = getUpdateResources(networkUuid);
            for (Resource<T> resource : resources) {
                // do not update the resource if a creation resource is already in the buffer
                // (so we don't need to generate an update as the resource has not yet been created
                // on server side and is still on client buffer)
                if (!networkCreateResources.containsKey(resource.getId())) {
                    networkUpdateResources.put(resource.getId(), resource);
                }
            }
        }

        void remove(UUID networkUuid, String resourceId) {
            remove(networkUuid, Collections.singletonList(resourceId));
        }

        void remove(UUID networkUuid, List<String> resourceIds) {
            Map<String, Resource<T>> networkCreateResources = getCreateResources(networkUuid);
            Map<String, Resource<T>> networkUpdateResources = getUpdateResources(networkUuid);
            Set<String> networkRemoveResources = getRemoveResources(networkUuid);
            for (String resourceId : resourceIds) {
                // remove directly from the creation buffer if possible, otherwise remove from the server"
                if (networkCreateResources.remove(resourceId) == null) {
                    networkRemoveResources.add(resourceId);

                    // no need to update the resource on server side if we remove it just after
                    networkUpdateResources.remove(resourceId);
                }
            }
        }

        void clear(UUID networkUuid) {
            createResources.remove(networkUuid);
            updateResources.remove(networkUuid);
            removeResources.remove(networkUuid);
        }

        void flush() {
            Set<UUID> networkUuids = new HashSet<>();
            networkUuids.addAll(createResources.keySet());
            networkUuids.addAll(updateResources.keySet());
            networkUuids.addAll(removeResources.keySet());
            for (UUID networkUuid : networkUuids) {
                Collection<Resource<T>> toCreate = getCreateResources(networkUuid).values();
                Collection<Resource<T>> toUpdate = getUpdateResources(networkUuid).values();
                Collection<String> toRemove = getRemoveResources(networkUuid);
                if (removeFct != null && !toRemove.isEmpty()) {
                    removeFct.accept(networkUuid, new ArrayList<>(toRemove));
                }
                if (!toCreate.isEmpty()) {
                    createFct.accept(networkUuid, new ArrayList<>(toCreate));
                }
                if (updateFct != null && !toUpdate.isEmpty()) {
                    updateFct.accept(networkUuid, new ArrayList<>(toUpdate));
                }
            }
            createResources.clear();
            updateResources.clear();
            removeResources.clear();
        }
    }

    private final Map<UUID, Resource<NetworkAttributes>> networkResourcesToFlush = new HashMap<>();

    private final Map<UUID, Resource<NetworkAttributes>> updateNetworkResourcesToFlush = new HashMap<>();

    private final CollectionBuffer<SubstationAttributes> substationResourcesToFlush
            = new CollectionBuffer<>(delegate::createSubstations, null, null);

    private final CollectionBuffer<VoltageLevelAttributes> voltageLevelResourcesToFlush
            = new CollectionBuffer<>(delegate::createVoltageLevels, delegate::updateVoltageLevels, null);

    private final CollectionBuffer<GeneratorAttributes> generatorResourcesToFlush
            = new CollectionBuffer<>(delegate::createGenerators, delegate::updateGenerators, null);

    private final CollectionBuffer<LoadAttributes> loadResourcesToFlush
            = new CollectionBuffer<>(delegate::createLoads, delegate::updateLoads, null);

    private final CollectionBuffer<BusbarSectionAttributes> busbarSectionResourcesToFlush
            = new CollectionBuffer<>(delegate::createBusbarSections, null, null);

    private final CollectionBuffer<SwitchAttributes> switchResourcesToFlush
            = new CollectionBuffer<>(delegate::createSwitches, delegate::updateSwitches, null);

    private final CollectionBuffer<ShuntCompensatorAttributes> shuntCompensatorResourcesToFlush
            = new CollectionBuffer<>(delegate::createShuntCompensators, delegate::updateShuntCompensators, null);

    private final CollectionBuffer<VscConverterStationAttributes> vscConverterStationResourcesToFlush
            = new CollectionBuffer<>(delegate::createVscConverterStations, delegate::updateVscConverterStations, null);

    private final CollectionBuffer<LccConverterStationAttributes> lccConverterStationResourcesToFlush
            = new CollectionBuffer<>(delegate::createLccConverterStations, delegate::updateLccConverterStations, null);

    private final CollectionBuffer<StaticVarCompensatorAttributes> svcResourcesToFlush
            = new CollectionBuffer<>(delegate::createStaticVarCompensators, delegate::updateStaticVarCompensators, null);

    private final CollectionBuffer<HvdcLineAttributes> hvdcLineResourcesToFlush
            = new CollectionBuffer<>(delegate::createHvdcLines, delegate::updateHvdcLines, null);

    private final CollectionBuffer<DanglingLineAttributes> danglingLineResourcesToFlush
            = new CollectionBuffer<>(delegate::createDanglingLines, delegate::updateDanglingLines, delegate::removeDanglingLines);

    private final CollectionBuffer<TwoWindingsTransformerAttributes> twoWindingsTransformerResourcesToFlush
            = new CollectionBuffer<>(delegate::createTwoWindingsTransformers, delegate::updateTwoWindingsTransformers, null);

    private final CollectionBuffer<ThreeWindingsTransformerAttributes> threeWindingsTransformerResourcesToFlush
            = new CollectionBuffer<>(delegate::createThreeWindingsTransformers, delegate::updateThreeWindingsTransformers, null);

    private final CollectionBuffer<LineAttributes> lineResourcesToFlush
            = new CollectionBuffer<>(delegate::createLines, delegate::updateLines, null);

    private final CollectionBuffer<ConfiguredBusAttributes> busResourcesToFlush
            = new CollectionBuffer<>(delegate::createConfiguredBuses, delegate::updateConfiguredBuses, null);

    public BufferedRestNetworkStoreClient(RestNetworkStoreClient delegate) {
        super(delegate);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            networkResourcesToFlush.put(networkResource.getAttributes().getUuid(), networkResource);
        }
    }

    @Override
    public void updateNetwork(UUID networkUuid, Resource<NetworkAttributes> networkResource) {
        updateNetworkResourcesToFlush.put(networkUuid, networkResource);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        // only delete network on server if not in the creation buffer
        if (networkResourcesToFlush.remove(networkUuid) == null) {
            delegate.deleteNetwork(networkUuid);
        }
        updateNetworkResourcesToFlush.remove(networkUuid);

        // clear buffers as server side delete network already remove all equipments of the network
        substationResourcesToFlush.clear(networkUuid);
        voltageLevelResourcesToFlush.clear(networkUuid);
        generatorResourcesToFlush.clear(networkUuid);
        loadResourcesToFlush.clear(networkUuid);
        busbarSectionResourcesToFlush.clear(networkUuid);
        switchResourcesToFlush.clear(networkUuid);
        shuntCompensatorResourcesToFlush.clear(networkUuid);
        svcResourcesToFlush.clear(networkUuid);
        vscConverterStationResourcesToFlush.clear(networkUuid);
        lccConverterStationResourcesToFlush.clear(networkUuid);
        danglingLineResourcesToFlush.clear(networkUuid);
        hvdcLineResourcesToFlush.clear(networkUuid);
        twoWindingsTransformerResourcesToFlush.clear(networkUuid);
        threeWindingsTransformerResourcesToFlush.clear(networkUuid);
        lineResourcesToFlush.clear(networkUuid);
        busResourcesToFlush.clear(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        substationResourcesToFlush.create(networkUuid, substationResources);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.create(networkUuid, voltageLevelResources);
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, Resource<VoltageLevelAttributes> voltageLevelResource) {
        voltageLevelResourcesToFlush.update(networkUuid, voltageLevelResource);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.create(networkUuid, switchResources);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        switchResourcesToFlush.update(networkUuid, switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.create(networkUuid, busbarSectionResources);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.create(networkUuid, loadResources);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        loadResourcesToFlush.update(networkUuid, loadResource);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.create(networkUuid, generatorResources);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        generatorResourcesToFlush.update(networkUuid, generatorResource);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.create(networkUuid, twoWindingsTransformerResources);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        twoWindingsTransformerResourcesToFlush.update(networkUuid, twoWindingsTransformerResource);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.create(networkUuid, threeWindingsTransformerResources);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        threeWindingsTransformerResourcesToFlush.update(networkUuid, threeWindingsTransformerResource);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.create(networkUuid, lineResources);
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        lineResourcesToFlush.update(networkUuid, lineResource);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.create(networkUuid, shuntCompensatorResources);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        shuntCompensatorResourcesToFlush.update(networkUuid, shuntCompensatorResource);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.create(networkUuid, vscConverterStationResources);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        vscConverterStationResourcesToFlush.update(networkUuid, vscConverterStationResource);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.create(networkUuid, lccConverterStationResources);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        lccConverterStationResourcesToFlush.update(networkUuid, lccConverterStationResource);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.create(networkUuid, svcResources);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        svcResourcesToFlush.update(networkUuid, staticVarCompensatorResource);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.create(networkUuid, hvdcLineResources);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        hvdcLineResourcesToFlush.update(networkUuid, hvdcLineResource);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        danglingLineResourcesToFlush.create(networkUuid, danglingLineResources);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        danglingLineResourcesToFlush.update(networkUuid, danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        danglingLineResourcesToFlush.remove(networkUuid, danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        busResourcesToFlush.create(networkUuid, busesRessources);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        busResourcesToFlush.update(networkUuid, busesResource);
    }

    @Override
    public void flush() {
        if (!networkResourcesToFlush.isEmpty()) {
            delegate.createNetworks(new ArrayList<>(networkResourcesToFlush.values()));
        }
        if (!updateNetworkResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, Resource<NetworkAttributes>> e : updateNetworkResourcesToFlush.entrySet()) {
                if (!networkResourcesToFlush.containsKey(e.getKey())) {
                    delegate.updateNetwork(e.getKey(), e.getValue());
                }
            }
        }
        networkResourcesToFlush.clear();
        updateNetworkResourcesToFlush.clear();

        substationResourcesToFlush.flush();
        voltageLevelResourcesToFlush.flush();
        generatorResourcesToFlush.flush();
        loadResourcesToFlush.flush();
        busbarSectionResourcesToFlush.flush();
        switchResourcesToFlush.flush();
        shuntCompensatorResourcesToFlush.flush();
        svcResourcesToFlush.flush();
        vscConverterStationResourcesToFlush.flush();
        lccConverterStationResourcesToFlush.flush();
        danglingLineResourcesToFlush.flush();
        hvdcLineResourcesToFlush.flush();
        twoWindingsTransformerResourcesToFlush.flush();
        threeWindingsTransformerResourcesToFlush.flush();
        lineResourcesToFlush.flush();
        busResourcesToFlush.flush();
    }
}
