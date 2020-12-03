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
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class BufferedNetworkStoreClient extends ForwardingNetworkStoreClient {

    private final NetworkCollectionIndex<NetworkCollectionBuffer> networkResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new NetworkCollectionBuffer());

    private final NetworkCollectionIndex<CollectionBuffer<SubstationAttributes>> substationResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createSubstations, null, delegate::removeSubstations));

    private final NetworkCollectionIndex<CollectionBuffer<VoltageLevelAttributes>> voltageLevelResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createVoltageLevels, delegate::updateVoltageLevels, delegate::removeVoltageLevels));

    private final NetworkCollectionIndex<CollectionBuffer<GeneratorAttributes>> generatorResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createGenerators, delegate::updateGenerators, delegate::removeGenerators));

    private final NetworkCollectionIndex<CollectionBuffer<BatteryAttributes>> batteryResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createBatteries, delegate::updateBatteries, delegate::removeBatteries));

    private final NetworkCollectionIndex<CollectionBuffer<LoadAttributes>> loadResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createLoads, delegate::updateLoads, delegate::removeLoads));

    private final NetworkCollectionIndex<CollectionBuffer<BusbarSectionAttributes>> busbarSectionResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createBusbarSections, null, delegate::removeBusBarSections));

    private final NetworkCollectionIndex<CollectionBuffer<SwitchAttributes>> switchResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createSwitches, delegate::updateSwitches, delegate::removeSwitches));

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
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createConfiguredBuses, delegate::updateConfiguredBuses, delegate::removeConfiguredBuses));

    public BufferedNetworkStoreClient(NetworkStoreClient delegate) {
        super(delegate);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            networkResourcesToFlush.getCollection(networkResource.getAttributes().getUuid()).create(networkResource);
        }
    }

    @Override
    public void updateNetwork(UUID networkUuid, int variantNum, Resource<NetworkAttributes> networkResource) {
        networkResourcesToFlush.getCollection(networkResource.getAttributes().getUuid()).update(networkResource);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        networkResourcesToFlush.getCollection(networkUuid).delete();

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
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        // TODO
    }

    @Override
    public void createSubstations(UUID networkUuid, int variantNum, List<Resource<SubstationAttributes>> substationResources) {
        substationResourcesToFlush.getCollection(networkUuid).create(substationResources, variantNum);
    }

    @Override
    public void removeSubstation(UUID networkUuid, int variantNum, String substationId) {
        substationResourcesToFlush.getCollection(networkUuid).remove(substationId, variantNum);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.getCollection(networkUuid).create(voltageLevelResources, variantNum);
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, int variantNum, Resource<VoltageLevelAttributes> voltageLevelResource) {
        voltageLevelResourcesToFlush.getCollection(networkUuid).update(voltageLevelResource, variantNum);
    }

    @Override
    public void removeVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        voltageLevelResourcesToFlush.getCollection(networkUuid).remove(voltageLevelId, variantNum);
    }

    @Override
    public void createSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.getCollection(networkUuid).create(switchResources, variantNum);
    }

    @Override
    public void updateSwitch(UUID networkUuid, int variantNum, Resource<SwitchAttributes> switchResource) {
        switchResourcesToFlush.getCollection(networkUuid).update(switchResource, variantNum);
    }

    @Override
    public void removeSwitch(UUID networkUuid, int variantNum, String switchId) {
        switchResourcesToFlush.getCollection(networkUuid).remove(switchId, variantNum);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, int variantNum, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.getCollection(networkUuid).create(busbarSectionResources, variantNum);
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        busbarSectionResourcesToFlush.getCollection(networkUuid).remove(busBarSectionId, variantNum);
    }

    @Override
    public void createLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.getCollection(networkUuid).create(loadResources, variantNum);
    }

    @Override
    public void updateLoad(UUID networkUuid, int variantNum, Resource<LoadAttributes> loadResource) {
        loadResourcesToFlush.getCollection(networkUuid).update(loadResource, variantNum);
    }

    @Override
    public void removeLoad(UUID networkUuid, int variantNum, String loadId) {
        loadResourcesToFlush.getCollection(networkUuid).remove(loadId, variantNum);
    }

    @Override
    public void createGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.getCollection(networkUuid).create(generatorResources, variantNum);
    }

    @Override
    public void updateGenerator(UUID networkUuid, int variantNum, Resource<GeneratorAttributes> generatorResource) {
        generatorResourcesToFlush.getCollection(networkUuid).update(generatorResource, variantNum);
    }

    @Override
    public void removeGenerator(UUID networkUuid, int variantNum, String generatorId) {
        generatorResourcesToFlush.getCollection(networkUuid).remove(generatorId, variantNum);
    }

    @Override
    public void createBatteries(UUID networkUuid, int variantNum, List<Resource<BatteryAttributes>> batteryResources) {
        batteryResourcesToFlush.getCollection(networkUuid).create(batteryResources, variantNum);
    }

    @Override
    public void updateBattery(UUID networkUuid, int variantNum, Resource<BatteryAttributes> batteryResource) {
        batteryResourcesToFlush.getCollection(networkUuid).update(batteryResource, variantNum);
    }

    @Override
    public void removeBattery(UUID networkUuid, int variantNum, String batteryId) {
        batteryResourcesToFlush.getCollection(networkUuid).remove(batteryId, variantNum);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).create(twoWindingsTransformerResources, variantNum);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, int variantNum, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).update(twoWindingsTransformerResource, variantNum);
    }

    @Override
    public void removeTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).remove(twoWindingsTransformerId, variantNum);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).create(threeWindingsTransformerResources, variantNum);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, int variantNum, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).update(threeWindingsTransformerResource, variantNum);
    }

    @Override
    public void removeThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).remove(threeWindingsTransformerId, variantNum);
    }

    @Override
    public void createLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.getCollection(networkUuid).create(lineResources, variantNum);
    }

    @Override
    public void updateLine(UUID networkUuid, int variantNum, Resource<LineAttributes> lineResource) {
        lineResourcesToFlush.getCollection(networkUuid).update(lineResource, variantNum);
    }

    @Override
    public void removeLine(UUID networkUuid, int variantNum, String lineId) {
        lineResourcesToFlush.getCollection(networkUuid).remove(lineId, variantNum);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).create(shuntCompensatorResources, variantNum);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, int variantNum, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).update(shuntCompensatorResource, variantNum);
    }

    @Override
    public void removeShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).remove(shuntCompensatorId, variantNum);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).create(vscConverterStationResources, variantNum);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, int variantNum, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).update(vscConverterStationResource, variantNum);
    }

    @Override
    public void removeVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).remove(vscConverterStationId, variantNum);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).create(lccConverterStationResources, variantNum);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, int variantNum, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).update(lccConverterStationResource, variantNum);
    }

    @Override
    public void removeLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).remove(lccConverterStationId, variantNum);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.getCollection(networkUuid).create(svcResources, variantNum);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, int variantNum, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        svcResourcesToFlush.getCollection(networkUuid).update(staticVarCompensatorResource, variantNum);
    }

    @Override
    public void removeStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        svcResourcesToFlush.getCollection(networkUuid).remove(staticVarCompensatorId, variantNum);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).create(hvdcLineResources, variantNum);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, int variantNum, Resource<HvdcLineAttributes> hvdcLineResource) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).update(hvdcLineResource, variantNum);
    }

    @Override
    public void removeHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).remove(hvdcLineId, variantNum);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        danglingLineResourcesToFlush.getCollection(networkUuid).create(danglingLineResources, variantNum);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, int variantNum, Resource<DanglingLineAttributes> danglingLineResource) {
        danglingLineResourcesToFlush.getCollection(networkUuid).update(danglingLineResource, variantNum);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        danglingLineResourcesToFlush.getCollection(networkUuid).remove(danglingLineId, variantNum);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        busResourcesToFlush.getCollection(networkUuid).create(busesRessources, variantNum);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, int variantNum, Resource<ConfiguredBusAttributes> busesResource) {
        busResourcesToFlush.getCollection(networkUuid).update(busesResource, variantNum);
    }

    @Override
    public void removeConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        busResourcesToFlush.getCollection(networkUuid).remove(busId, variantNum);
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
