/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.AbstractForwardingNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkCollectionIndex;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class BufferedNetworkStoreClient extends AbstractForwardingNetworkStoreClient {

    private final Map<UUID, Resource<NetworkAttributes>> networkResourcesToFlush = new HashMap<>();

    private final Map<UUID, Resource<NetworkAttributes>> updateNetworkResourcesToFlush = new HashMap<>();

    private final NetworkCollectionIndex<CollectionBuffer<SubstationAttributes>> substationResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createSubstations, delegate::updateSubstations, delegate::removeSubstations));

    private final NetworkCollectionIndex<CollectionBuffer<VoltageLevelAttributes>> voltageLevelResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createVoltageLevels, delegate::updateVoltageLevels, delegate::removeVoltageLevels));

    private final NetworkCollectionIndex<CollectionBuffer<GeneratorAttributes>> generatorResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createGenerators, delegate::updateGenerators, delegate::removeGenerators));

    private final NetworkCollectionIndex<CollectionBuffer<BatteryAttributes>> batteryResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createBatteries, delegate::updateBatteries, delegate::removeBatteries));

    private final NetworkCollectionIndex<CollectionBuffer<LoadAttributes>> loadResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createLoads, delegate::updateLoads, delegate::removeLoads));

    private final NetworkCollectionIndex<CollectionBuffer<BusbarSectionAttributes>> busbarSectionResourcesToFlush
            = new NetworkCollectionIndex<>(uuid -> new CollectionBuffer<>(delegate::createBusbarSections, delegate::updateBusbarSections, delegate::removeBusBarSections));

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
    public void removeSubstations(UUID networkUuid, List<String> substationsId) {
        substationResourcesToFlush.getCollection(networkUuid).remove(substationsId);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.getCollection(networkUuid).create(voltageLevelResources);
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.getCollection(networkUuid).update(voltageLevelResources);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, List<String> voltageLevelsId) {
        voltageLevelResourcesToFlush.getCollection(networkUuid).remove(voltageLevelsId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.getCollection(networkUuid).create(switchResources);
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.getCollection(networkUuid).update(switchResources);
    }

    @Override
    public void removeSwitches(UUID networkUuid, List<String> switchesId) {
        switchResourcesToFlush.getCollection(networkUuid).remove(switchesId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.getCollection(networkUuid).create(busbarSectionResources);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, List<String> busBarSectionsId) {
        busbarSectionResourcesToFlush.getCollection(networkUuid).remove(busBarSectionsId);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.getCollection(networkUuid).create(loadResources);
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.getCollection(networkUuid).update(loadResources);
    }

    @Override
    public void removeLoads(UUID networkUuid, List<String> loadsId) {
        loadResourcesToFlush.getCollection(networkUuid).remove(loadsId);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.getCollection(networkUuid).create(generatorResources);
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.getCollection(networkUuid).update(generatorResources);
    }

    @Override
    public void removeGenerators(UUID networkUuid, List<String> generatorsId) {
        generatorResourcesToFlush.getCollection(networkUuid).remove(generatorsId);
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        batteryResourcesToFlush.getCollection(networkUuid).create(batteryResources);
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        batteryResourcesToFlush.getCollection(networkUuid).update(batteryResources);
    }

    @Override
    public void removeBatteries(UUID networkUuid, List<String> batteriesId) {
        batteryResourcesToFlush.getCollection(networkUuid).remove(batteriesId);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).create(twoWindingsTransformerResources);
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).update(twoWindingsTransformerResources);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, List<String> twoWindingsTransformersId) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid).remove(twoWindingsTransformersId);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).create(threeWindingsTransformerResources);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).update(threeWindingsTransformerResources);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, List<String> threeWindingsTransformersId) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid).remove(threeWindingsTransformersId);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.getCollection(networkUuid).create(lineResources);
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.getCollection(networkUuid).update(lineResources);
    }

    @Override
    public void removeLines(UUID networkUuid, List<String> linesId) {
        lineResourcesToFlush.getCollection(networkUuid).remove(linesId);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).create(shuntCompensatorResources);
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).update(shuntCompensatorResources);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, List<String> shuntCompensatorsId) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid).remove(shuntCompensatorsId);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).create(vscConverterStationResources);
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).update(vscConverterStationResources);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, List<String> vscConverterStationsId) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid).remove(vscConverterStationsId);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).create(lccConverterStationResources);
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).update(lccConverterStationResources);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, List<String> lccConverterStationsId) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid).remove(lccConverterStationsId);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.getCollection(networkUuid).create(svcResources);
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        svcResourcesToFlush.getCollection(networkUuid).update(staticVarCompensatorResources);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, List<String> staticVarCompensatorsId) {
        svcResourcesToFlush.getCollection(networkUuid).remove(staticVarCompensatorsId);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).create(hvdcLineResources);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).update(hvdcLineResources);
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, List<String> hvdcLinesId) {
        hvdcLineResourcesToFlush.getCollection(networkUuid).remove(hvdcLinesId);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        danglingLineResourcesToFlush.getCollection(networkUuid).create(danglingLineResources);
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        danglingLineResourcesToFlush.getCollection(networkUuid).update(danglingLineResources);
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, List<String> danglingLinesId) {
        danglingLineResourcesToFlush.getCollection(networkUuid).remove(danglingLinesId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        busResourcesToFlush.getCollection(networkUuid).create(busesRessources);
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        busResourcesToFlush.getCollection(networkUuid).update(busesResources);
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, List<String> busesId) {
        busResourcesToFlush.getCollection(networkUuid).remove(busesId);
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
