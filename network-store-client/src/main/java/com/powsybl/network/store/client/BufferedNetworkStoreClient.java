/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.ForwardingNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkCollectionIndex;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class BufferedNetworkStoreClient extends ForwardingNetworkStoreClient {

    private final Map<UUID, Resource<NetworkAttributes>> networkResourcesToFlush = new HashMap<>();

    private final Map<UUID, Resource<NetworkAttributes>> updateNetworkResourcesToFlush = new HashMap<>();

    private final NetworkCollectionIndex<CollectionBuffer<SubstationAttributes>> substationResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createSubstations, null, null));

    private final NetworkCollectionIndex<CollectionBuffer<VoltageLevelAttributes>> voltageLevelResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createVoltageLevels, delegate::updateVoltageLevels, null));

    private final NetworkCollectionIndex<CollectionBuffer<GeneratorAttributes>> generatorResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createGenerators, delegate::updateGenerators, delegate::removeGenerators));

    private final NetworkCollectionIndex<CollectionBuffer<BatteryAttributes>> batteryResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createBatteries, delegate::updateBatteries, delegate::removeBatteries));

    private final NetworkCollectionIndex<CollectionBuffer<LoadAttributes>> loadResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createLoads, delegate::updateLoads, delegate::removeLoads));

    private final NetworkCollectionIndex<CollectionBuffer<BusbarSectionAttributes>> busbarSectionResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createBusbarSections, null, delegate::removeBusBarSections));

    private final NetworkCollectionIndex<CollectionBuffer<SwitchAttributes>> switchResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createSwitches, delegate::updateSwitches, null));

    private final NetworkCollectionIndex<CollectionBuffer<ShuntCompensatorAttributes>> shuntCompensatorResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createShuntCompensators, delegate::updateShuntCompensators, delegate::removeShuntCompensators));

    private final NetworkCollectionIndex<CollectionBuffer<VscConverterStationAttributes>> vscConverterStationResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createVscConverterStations, delegate::updateVscConverterStations, delegate::removeVscConverterStations));

    private final NetworkCollectionIndex<CollectionBuffer<LccConverterStationAttributes>> lccConverterStationResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createLccConverterStations, delegate::updateLccConverterStations, delegate::removeLccConverterStations));

    private final NetworkCollectionIndex<CollectionBuffer<StaticVarCompensatorAttributes>> svcResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createStaticVarCompensators, delegate::updateStaticVarCompensators, delegate::removeStaticVarCompensators));

    private final NetworkCollectionIndex<CollectionBuffer<HvdcLineAttributes>> hvdcLineResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createHvdcLines, delegate::updateHvdcLines, delegate::removeHvdcLines));

    private final NetworkCollectionIndex<CollectionBuffer<DanglingLineAttributes>> danglingLineResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createDanglingLines, delegate::updateDanglingLines, delegate::removeDanglingLines));

    private final NetworkCollectionIndex<CollectionBuffer<TwoWindingsTransformerAttributes>> twoWindingsTransformerResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createTwoWindingsTransformers, delegate::updateTwoWindingsTransformers, delegate::removeTwoWindingsTransformers));

    private final NetworkCollectionIndex<CollectionBuffer<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createThreeWindingsTransformers, delegate::updateThreeWindingsTransformers, delegate::removeThreeWindingsTransformers));

    private final NetworkCollectionIndex<CollectionBuffer<LineAttributes>> lineResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createLines, delegate::updateLines, delegate::removeLines));

    private final NetworkCollectionIndex<CollectionBuffer<ConfiguredBusAttributes>> busResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createConfiguredBuses, delegate::updateConfiguredBuses, null));

    public BufferedNetworkStoreClient(NetworkStoreClient delegate) {
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
        if (!networkResourcesToFlush.containsKey(networkUuid)) {
            updateNetworkResourcesToFlush.put(networkUuid, networkResource);
        }
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        // only delete network on server if not in the creation buffer
        if (networkResourcesToFlush.remove(networkUuid) == null) {
            updateNetworkResourcesToFlush.remove(networkUuid);

            delegate.deleteNetwork(networkUuid);
        }

        // clear buffers as server side delete network already remove all equipments of the network
        substationResourcesToFlush.removeCollection(networkUuid);
        voltageLevelResourcesToFlush.removeCollection(networkUuid);
        generatorResourcesToFlush.removeCollection(networkUuid);
        batteryResourcesToFlush.removeCollection(networkUuid);
        loadResourcesToFlush.removeCollection(networkUuid);
        busbarSectionResourcesToFlush.removeCollection(networkUuid);
        switchResourcesToFlush.removeCollection(networkUuid);
        shuntCompensatorResourcesToFlush.removeCollection(networkUuid);
        svcResourcesToFlush.removeCollection(networkUuid);
        vscConverterStationResourcesToFlush.removeCollection(networkUuid);
        lccConverterStationResourcesToFlush.removeCollection(networkUuid);
        danglingLineResourcesToFlush.removeCollection(networkUuid);
        hvdcLineResourcesToFlush.removeCollection(networkUuid);
        twoWindingsTransformerResourcesToFlush.removeCollection(networkUuid);
        threeWindingsTransformerResourcesToFlush.removeCollection(networkUuid);
        lineResourcesToFlush.removeCollection(networkUuid);
        busResourcesToFlush.removeCollection(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        substationResourcesToFlush.getCollection(networkUuid).create(substationResources);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.getCollection(networkUuid).create(voltageLevelResources);
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, Resource<VoltageLevelAttributes> voltageLevelResource) {
        voltageLevelResourcesToFlush.getCollection(networkUuid).update(voltageLevelResource);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.getCollection(networkUuid).create(switchResources);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        switchResourcesToFlush.getCollection(networkUuid).update(switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.getCollection(networkUuid).create(busbarSectionResources);
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, String busBarSectionId) {
        busbarSectionResourcesToFlush.getCollection(networkUuid).remove(busBarSectionId);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.getCollection(networkUuid).create(loadResources);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        loadResourcesToFlush.getCollection(networkUuid).update(loadResource);
    }

    @Override
    public void removeLoad(UUID networkUuid, String loadId) {
        loadResourcesToFlush.getCollection(networkUuid).remove(loadId);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.getCollection(networkUuid).create(generatorResources);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        generatorResourcesToFlush.getCollection(networkUuid).update(generatorResource);
    }

    @Override
    public void removeGenerator(UUID networkUuid, String generatorId) {
        generatorResourcesToFlush.getCollection(networkUuid).remove(generatorId);
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        batteryResourcesToFlush.getCollection(networkUuid).create(batteryResources);
    }

    @Override
    public void updateBattery(UUID networkUuid, Resource<BatteryAttributes> batteryResource) {
        batteryResourcesToFlush.getCollection(networkUuid).update(batteryResource);
    }

    @Override
    public void removeBattery(UUID networkUuid, String batteryId) {
        batteryResourcesToFlush.getCollection(networkUuid).remove(batteryId);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).create(twoWindingsTransformerResources);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).update(twoWindingsTransformerResource);
    }

    @Override
    public void removeTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).remove(twoWindingsTransformerId);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).create(threeWindingsTransformerResources);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).update(threeWindingsTransformerResource);
    }

    @Override
    public void removeThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).remove(threeWindingsTransformerId);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.getCollection(networkUuid).create(lineResources);
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        lineResourcesToFlush.getCollection(networkUuid).update(lineResource);
    }

    @Override
    public void removeLine(UUID networkUuid, String lineId) {
        lineResourcesToFlush.getCollection(networkUuid).remove(lineId);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).create(shuntCompensatorResources);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).update(shuntCompensatorResource);
    }

    @Override
    public void removeShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).remove(shuntCompensatorId);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).create(vscConverterStationResources);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).update(vscConverterStationResource);
    }

    @Override
    public void removeVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).remove(vscConverterStationId);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).create(lccConverterStationResources);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).update(lccConverterStationResource);
    }

    @Override
    public void removeLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).remove(lccConverterStationId);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.getCollection(networkUuid).create(svcResources);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        svcResourcesToFlush.getCollection(networkUuid).update(staticVarCompensatorResource);
    }

    @Override
    public void removeStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        svcResourcesToFlush.getCollection(networkUuid).remove(staticVarCompensatorId);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).create(hvdcLineResources);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).update(hvdcLineResource);
    }

    @Override
    public void removeHvdcLine(UUID networkUuid, String hvdcLineId) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).remove(hvdcLineId);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        danglingLineResourcesToFlush.getCollection(networkUuid).create(danglingLineResources);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        danglingLineResourcesToFlush.getCollection(networkUuid).update(danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        danglingLineResourcesToFlush.getCollection(networkUuid).remove(danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        busResourcesToFlush.getCollection(networkUuid).create(busesRessources);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        busResourcesToFlush.getCollection(networkUuid).update(busesResource);
    }

    @Override
    public void flush() {
        if (!networkResourcesToFlush.isEmpty()) {
            delegate.createNetworks(new ArrayList<>(networkResourcesToFlush.values()));
        }
        if (!updateNetworkResourcesToFlush.isEmpty()) {
            for (Map.Entry<UUID, Resource<NetworkAttributes>> e : updateNetworkResourcesToFlush.entrySet()) {
                delegate.updateNetwork(e.getKey(), e.getValue());
            }
        }
        networkResourcesToFlush.clear();
        updateNetworkResourcesToFlush.clear();

        substationResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        voltageLevelResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        generatorResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        batteryResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        loadResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        busbarSectionResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        switchResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        shuntCompensatorResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        svcResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        vscConverterStationResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        lccConverterStationResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        danglingLineResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        hvdcLineResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        twoWindingsTransformerResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        threeWindingsTransformerResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        lineResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
        busResourcesToFlush.applyToCollection((networkUuid, buffer) -> buffer.flush(networkUuid));
    }
}
