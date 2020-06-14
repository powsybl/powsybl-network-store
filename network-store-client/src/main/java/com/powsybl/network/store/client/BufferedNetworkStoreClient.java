/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BufferedNetworkStoreClient extends ForwardingNetworkStoreClient {

    private final Map<UUID, Resource<NetworkAttributes>> networkResourcesToFlush = new HashMap<>();

    private final Map<UUID, Resource<NetworkAttributes>> updateNetworkResourcesToFlush = new HashMap<>();

    private final NetworkIndex<CollectionBuffer<SubstationAttributes>> substationResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createSubstations, null, null));

    private final NetworkIndex<CollectionBuffer<VoltageLevelAttributes>> voltageLevelResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createVoltageLevels, delegate::updateVoltageLevels, null));

    private final NetworkIndex<CollectionBuffer<GeneratorAttributes>> generatorResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createGenerators, delegate::updateGenerators, null));

    private final NetworkIndex<CollectionBuffer<LoadAttributes>> loadResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createLoads, delegate::updateLoads, null));

    private final NetworkIndex<CollectionBuffer<BusbarSectionAttributes>> busbarSectionResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createBusbarSections, null, null));

    private final NetworkIndex<CollectionBuffer<SwitchAttributes>> switchResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createSwitches, delegate::updateSwitches, null));

    private final NetworkIndex<CollectionBuffer<ShuntCompensatorAttributes>> shuntCompensatorResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createShuntCompensators, delegate::updateShuntCompensators, null));

    private final NetworkIndex<CollectionBuffer<VscConverterStationAttributes>> vscConverterStationResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createVscConverterStations, delegate::updateVscConverterStations, null));

    private final NetworkIndex<CollectionBuffer<LccConverterStationAttributes>> lccConverterStationResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createLccConverterStations, delegate::updateLccConverterStations, null));

    private final NetworkIndex<CollectionBuffer<StaticVarCompensatorAttributes>> svcResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createStaticVarCompensators, delegate::updateStaticVarCompensators, null));

    private final NetworkIndex<CollectionBuffer<HvdcLineAttributes>> hvdcLineResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createHvdcLines, delegate::updateHvdcLines, null));

    private final NetworkIndex<CollectionBuffer<DanglingLineAttributes>> danglingLineResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createDanglingLines, delegate::updateDanglingLines, delegate::removeDanglingLines));

    private final NetworkIndex<CollectionBuffer<TwoWindingsTransformerAttributes>> twoWindingsTransformerResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createTwoWindingsTransformers, delegate::updateTwoWindingsTransformers, null));

    private final NetworkIndex<CollectionBuffer<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createThreeWindingsTransformers, delegate::updateThreeWindingsTransformers, null));

    private final NetworkIndex<CollectionBuffer<LineAttributes>> lineResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createLines, delegate::updateLines, null));

    private final NetworkIndex<CollectionBuffer<ConfiguredBusAttributes>> busResourcesToFlush
            = new NetworkIndex<>(uuid -> new CollectionBuffer<>(delegate::createConfiguredBuses, delegate::updateConfiguredBuses, null));

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
        substationResourcesToFlush.removeNetwork(networkUuid);
        voltageLevelResourcesToFlush.removeNetwork(networkUuid);
        generatorResourcesToFlush.removeNetwork(networkUuid);
        loadResourcesToFlush.removeNetwork(networkUuid);
        busbarSectionResourcesToFlush.removeNetwork(networkUuid);
        switchResourcesToFlush.removeNetwork(networkUuid);
        shuntCompensatorResourcesToFlush.removeNetwork(networkUuid);
        svcResourcesToFlush.removeNetwork(networkUuid);
        vscConverterStationResourcesToFlush.removeNetwork(networkUuid);
        lccConverterStationResourcesToFlush.removeNetwork(networkUuid);
        danglingLineResourcesToFlush.removeNetwork(networkUuid);
        hvdcLineResourcesToFlush.removeNetwork(networkUuid);
        twoWindingsTransformerResourcesToFlush.removeNetwork(networkUuid);
        threeWindingsTransformerResourcesToFlush.removeNetwork(networkUuid);
        lineResourcesToFlush.removeNetwork(networkUuid);
        busResourcesToFlush.removeNetwork(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        substationResourcesToFlush.getNetwork(networkUuid).create(substationResources);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        voltageLevelResourcesToFlush.getNetwork(networkUuid).create(voltageLevelResources);
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, Resource<VoltageLevelAttributes> voltageLevelResource) {
        voltageLevelResourcesToFlush.getNetwork(networkUuid).update(voltageLevelResource);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        switchResourcesToFlush.getNetwork(networkUuid).create(switchResources);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        switchResourcesToFlush.getNetwork(networkUuid).update(switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        busbarSectionResourcesToFlush.getNetwork(networkUuid).create(busbarSectionResources);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        loadResourcesToFlush.getNetwork(networkUuid).create(loadResources);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        loadResourcesToFlush.getNetwork(networkUuid).update(loadResource);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        generatorResourcesToFlush.getNetwork(networkUuid).create(generatorResources);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        generatorResourcesToFlush.getNetwork(networkUuid).update(generatorResource);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        twoWindingsTransformerResourcesToFlush.getNetwork(networkUuid).create(twoWindingsTransformerResources);
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        twoWindingsTransformerResourcesToFlush.getNetwork(networkUuid).update(twoWindingsTransformerResource);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        threeWindingsTransformerResourcesToFlush.getNetwork(networkUuid).create(threeWindingsTransformerResources);
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        threeWindingsTransformerResourcesToFlush.getNetwork(networkUuid).update(threeWindingsTransformerResource);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        lineResourcesToFlush.getNetwork(networkUuid).create(lineResources);
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        lineResourcesToFlush.getNetwork(networkUuid).update(lineResource);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        shuntCompensatorResourcesToFlush.getNetwork(networkUuid).create(shuntCompensatorResources);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        shuntCompensatorResourcesToFlush.getNetwork(networkUuid).update(shuntCompensatorResource);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        vscConverterStationResourcesToFlush.getNetwork(networkUuid).create(vscConverterStationResources);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        vscConverterStationResourcesToFlush.getNetwork(networkUuid).update(vscConverterStationResource);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        lccConverterStationResourcesToFlush.getNetwork(networkUuid).create(lccConverterStationResources);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        lccConverterStationResourcesToFlush.getNetwork(networkUuid).update(lccConverterStationResource);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        svcResourcesToFlush.getNetwork(networkUuid).create(svcResources);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        svcResourcesToFlush.getNetwork(networkUuid).update(staticVarCompensatorResource);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        hvdcLineResourcesToFlush.getNetwork(networkUuid).create(hvdcLineResources);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        hvdcLineResourcesToFlush.getNetwork(networkUuid).update(hvdcLineResource);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        danglingLineResourcesToFlush.getNetwork(networkUuid).create(danglingLineResources);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        danglingLineResourcesToFlush.getNetwork(networkUuid).update(danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        danglingLineResourcesToFlush.getNetwork(networkUuid).remove(danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        busResourcesToFlush.getNetwork(networkUuid).create(busesRessources);
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        busResourcesToFlush.getNetwork(networkUuid).update(busesResource);
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

        substationResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        voltageLevelResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        generatorResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        loadResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        busbarSectionResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        switchResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        shuntCompensatorResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        svcResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        vscConverterStationResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        lccConverterStationResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        danglingLineResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        hvdcLineResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        twoWindingsTransformerResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        threeWindingsTransformerResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        lineResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
        busResourcesToFlush.apply((networkUuid, buffer) -> buffer.flush(networkUuid));
    }
}
