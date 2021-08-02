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

import java.util.List;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class BufferedNetworkStoreClient extends AbstractForwardingNetworkStoreClient {

    private final NetworkCollectionIndex<CollectionBuffer<NetworkAttributes>> networkResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(delegate::createNetworks,
                delegate::updateNetworks,
                ids -> delegate.deleteNetwork(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionBuffer<SubstationAttributes>> substationResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createSubstations(networkUuid, resources),
                resources -> delegate.updateSubstations(networkUuid, resources),
                ids -> delegate.removeSubstations(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<VoltageLevelAttributes>> voltageLevelResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createVoltageLevels(networkUuid, resources),
                resources -> delegate.updateVoltageLevels(networkUuid, resources),
                ids -> delegate.removeVoltageLevels(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<GeneratorAttributes>> generatorResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createGenerators(networkUuid, resources),
                resources -> delegate.updateGenerators(networkUuid, resources),
                ids -> delegate.removeGenerators(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<BatteryAttributes>> batteryResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createBatteries(networkUuid, resources),
                resources -> delegate.updateBatteries(networkUuid, resources),
                ids -> delegate.removeBatteries(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<LoadAttributes>> loadResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createLoads(networkUuid, resources),
                resources -> delegate.updateLoads(networkUuid, resources),
                ids -> delegate.removeLoads(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<BusbarSectionAttributes>> busbarSectionResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createBusbarSections(networkUuid, resources),
                resources -> delegate.updateBusbarSections(networkUuid, resources),
                ids -> delegate.removeBusBarSections(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<SwitchAttributes>> switchResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createSwitches(networkUuid, resources),
                resources -> delegate.updateSwitches(networkUuid, resources),
                ids -> delegate.removeSwitches(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<ShuntCompensatorAttributes>> shuntCompensatorResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createShuntCompensators(networkUuid, resources),
                resources -> delegate.updateShuntCompensators(networkUuid, resources),
                ids -> delegate.removeShuntCompensators(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<VscConverterStationAttributes>> vscConverterStationResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createVscConverterStations(networkUuid, resources),
                resources -> delegate.updateVscConverterStations(networkUuid, resources),
                ids -> delegate.removeVscConverterStations(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<LccConverterStationAttributes>> lccConverterStationResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createLccConverterStations(networkUuid, resources),
                resources -> delegate.updateLccConverterStations(networkUuid, resources),
                ids -> delegate.removeLccConverterStations(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<StaticVarCompensatorAttributes>> svcResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createStaticVarCompensators(networkUuid, resources),
                resources -> delegate.updateStaticVarCompensators(networkUuid, resources),
                ids -> delegate.removeStaticVarCompensators(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<HvdcLineAttributes>> hvdcLineResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createHvdcLines(networkUuid, resources),
                resources -> delegate.updateHvdcLines(networkUuid, resources),
                ids -> delegate.removeHvdcLines(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<DanglingLineAttributes>> danglingLineResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createDanglingLines(networkUuid, resources),
                resources -> delegate.updateDanglingLines(networkUuid, resources),
                ids -> delegate.removeDanglingLines(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<TwoWindingsTransformerAttributes>> twoWindingsTransformerResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createTwoWindingsTransformers(networkUuid, resources),
                resources -> delegate.updateTwoWindingsTransformers(networkUuid, resources),
                ids -> delegate.removeTwoWindingsTransformers(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createThreeWindingsTransformers(networkUuid, resources),
                resources -> delegate.updateThreeWindingsTransformers(networkUuid, resources),
                ids -> delegate.removeThreeWindingsTransformers(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<LineAttributes>> lineResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createLines(networkUuid, resources),
                resources -> delegate.updateLines(networkUuid, resources),
                ids -> delegate.removeLines(networkUuid, variantNum, ids)));

    private final NetworkCollectionIndex<CollectionBuffer<ConfiguredBusAttributes>> busResourcesToFlush
            = new NetworkCollectionIndex<>((networkUuid, variantNum) -> new CollectionBuffer<>(resources -> delegate.createConfiguredBuses(networkUuid, resources),
                resources -> delegate.updateConfiguredBuses(networkUuid, resources),
                ids -> delegate.removeConfiguredBuses(networkUuid, variantNum, ids)));

    private final List<NetworkCollectionIndex<? extends CollectionBuffer<? extends IdentifiableAttributes>>> allBuffers = List.of(
            networkResourcesToFlush,
            substationResourcesToFlush,
            voltageLevelResourcesToFlush,
            generatorResourcesToFlush,
            batteryResourcesToFlush,
            loadResourcesToFlush,
            busbarSectionResourcesToFlush,
            switchResourcesToFlush,
            shuntCompensatorResourcesToFlush,
            svcResourcesToFlush,
            vscConverterStationResourcesToFlush,
            lccConverterStationResourcesToFlush,
            danglingLineResourcesToFlush,
            hvdcLineResourcesToFlush,
            twoWindingsTransformerResourcesToFlush,
            threeWindingsTransformerResourcesToFlush,
            lineResourcesToFlush,
            busResourcesToFlush);

    public BufferedNetworkStoreClient(NetworkStoreClient delegate) {
        super(delegate);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            UUID networkUuid = networkResource.getAttributes().getUuid();
            int variantNum = networkResource.getVariantNum();
            networkResourcesToFlush.getCollection(networkUuid, variantNum).create(networkResource);
        }
    }

    @Override
    public void updateNetworks(List<Resource<NetworkAttributes>> networkResources) {
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            UUID networkUuid = networkResource.getAttributes().getUuid();
            int variantNum = networkResource.getVariantNum();
            networkResourcesToFlush.getCollection(networkUuid, variantNum).update(networkResource);
        }
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        // clear buffers as server side delete network already remove all equipments of the network
        allBuffers.forEach(buffer -> buffer.removeCollection(networkUuid));
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        // clear buffers as server side delete network already remove all equipments of the network
        allBuffers.forEach(buffer -> buffer.removeCollection(networkUuid, variantNum));
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            substationResourcesToFlush.getCollection(networkUuid, substationResource.getVariantNum()).create(substationResource);
        }
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            substationResourcesToFlush.getCollection(networkUuid, substationResource.getVariantNum()).update(substationResource);
        }
    }

    @Override
    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        substationResourcesToFlush.getCollection(networkUuid, variantNum).remove(substationsId);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
            voltageLevelResourcesToFlush.getCollection(networkUuid, voltageLevelResource.getVariantNum()).create(voltageLevelResource);
        }
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
            voltageLevelResourcesToFlush.getCollection(networkUuid, voltageLevelResource.getVariantNum()).update(voltageLevelResource);
        }
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        voltageLevelResourcesToFlush.getCollection(networkUuid, variantNum).remove(voltageLevelsId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            switchResourcesToFlush.getCollection(networkUuid, switchResource.getVariantNum()).create(switchResource);
        }
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            switchResourcesToFlush.getCollection(networkUuid, switchResource.getVariantNum()).update(switchResource);
        }
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        switchResourcesToFlush.getCollection(networkUuid, variantNum).remove(switchesId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        for (Resource<BusbarSectionAttributes> busbarSectionResource : busbarSectionResources) {
            busbarSectionResourcesToFlush.getCollection(networkUuid, busbarSectionResource.getVariantNum()).create(busbarSectionResource);
        }
    }

    @Override
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        for (Resource<BusbarSectionAttributes> busbarSectionResource : busbarSectionResources) {
            busbarSectionResourcesToFlush.getCollection(networkUuid, busbarSectionResource.getVariantNum()).update(busbarSectionResource);
        }
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        busbarSectionResourcesToFlush.getCollection(networkUuid, variantNum).remove(busBarSectionsId);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        for (Resource<LoadAttributes> loadResource : loadResources) {
            loadResourcesToFlush.getCollection(networkUuid, loadResource.getVariantNum()).create(loadResource);
        }
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        for (Resource<LoadAttributes> loadResource : loadResources) {
            loadResourcesToFlush.getCollection(networkUuid, loadResource.getVariantNum()).update(loadResource);
        }
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        loadResourcesToFlush.getCollection(networkUuid, variantNum).remove(loadsId);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            generatorResourcesToFlush.getCollection(networkUuid, generatorResource.getVariantNum()).create(generatorResource);
        }
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            generatorResourcesToFlush.getCollection(networkUuid, generatorResource.getVariantNum()).update(generatorResource);
        }
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        generatorResourcesToFlush.getCollection(networkUuid, variantNum).remove(generatorsId);
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            batteryResourcesToFlush.getCollection(networkUuid, batteryResource.getVariantNum()).create(batteryResource);
        }
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            batteryResourcesToFlush.getCollection(networkUuid, batteryResource.getVariantNum()).update(batteryResource);
        }
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        batteryResourcesToFlush.getCollection(networkUuid, variantNum).remove(batteriesId);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            twoWindingsTransformerResourcesToFlush.getCollection(networkUuid, twoWindingsTransformerResource.getVariantNum()).create(twoWindingsTransformerResource);
        }
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            twoWindingsTransformerResourcesToFlush.getCollection(networkUuid, twoWindingsTransformerResource.getVariantNum()).update(twoWindingsTransformerResource);
        }
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid, variantNum).remove(twoWindingsTransformersId);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            threeWindingsTransformerResourcesToFlush.getCollection(networkUuid, threeWindingsTransformerResource.getVariantNum()).create(threeWindingsTransformerResource);
        }
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            threeWindingsTransformerResourcesToFlush.getCollection(networkUuid, threeWindingsTransformerResource.getVariantNum()).update(threeWindingsTransformerResource);
        }
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        threeWindingsTransformerResourcesToFlush.getCollection(networkUuid, variantNum).remove(threeWindingsTransformersId);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        for (Resource<LineAttributes> lineResource : lineResources) {
            lineResourcesToFlush.getCollection(networkUuid, lineResource.getVariantNum()).create(lineResource);
        }
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        for (Resource<LineAttributes> lineResource : lineResources) {
            lineResourcesToFlush.getCollection(networkUuid, lineResource.getVariantNum()).update(lineResource);
        }
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        lineResourcesToFlush.getCollection(networkUuid, variantNum).remove(linesId);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            shuntCompensatorResourcesToFlush.getCollection(networkUuid, shuntCompensatorResource.getVariantNum()).create(shuntCompensatorResource);
        }
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            shuntCompensatorResourcesToFlush.getCollection(networkUuid, shuntCompensatorResource.getVariantNum()).update(shuntCompensatorResource);
        }
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        shuntCompensatorResourcesToFlush.getCollection(networkUuid, variantNum).remove(shuntCompensatorsId);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            vscConverterStationResourcesToFlush.getCollection(networkUuid, vscConverterStationResource.getVariantNum()).create(vscConverterStationResource);
        }
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            vscConverterStationResourcesToFlush.getCollection(networkUuid, vscConverterStationResource.getVariantNum()).update(vscConverterStationResource);
        }
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        vscConverterStationResourcesToFlush.getCollection(networkUuid, variantNum).remove(vscConverterStationsId);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            lccConverterStationResourcesToFlush.getCollection(networkUuid, lccConverterStationResource.getVariantNum()).create(lccConverterStationResource);
        }
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            lccConverterStationResourcesToFlush.getCollection(networkUuid, lccConverterStationResource.getVariantNum()).update(lccConverterStationResource);
        }
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        lccConverterStationResourcesToFlush.getCollection(networkUuid, variantNum).remove(lccConverterStationsId);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            svcResourcesToFlush.getCollection(networkUuid, svcResource.getVariantNum()).create(svcResource);
        }
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            svcResourcesToFlush.getCollection(networkUuid, svcResource.getVariantNum()).update(svcResource);
        }
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        svcResourcesToFlush.getCollection(networkUuid, variantNum).remove(staticVarCompensatorsId);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            hvdcLineResourcesToFlush.getCollection(networkUuid, hvdcLineResource.getVariantNum()).create(hvdcLineResource);
        }
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            hvdcLineResourcesToFlush.getCollection(networkUuid, hvdcLineResource.getVariantNum()).update(hvdcLineResource);
        }
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        hvdcLineResourcesToFlush.getCollection(networkUuid, variantNum).remove(hvdcLinesId);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            danglingLineResourcesToFlush.getCollection(networkUuid, danglingLineResource.getVariantNum()).create(danglingLineResource);
        }
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            danglingLineResourcesToFlush.getCollection(networkUuid, danglingLineResource.getVariantNum()).update(danglingLineResource);
        }
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        danglingLineResourcesToFlush.getCollection(networkUuid, variantNum).remove(danglingLinesId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources) {
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            busResourcesToFlush.getCollection(networkUuid, busResource.getVariantNum()).create(busResource);
        }
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources) {
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            busResourcesToFlush.getCollection(networkUuid, busResource.getVariantNum()).update(busResource);
        }
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        busResourcesToFlush.getCollection(networkUuid, variantNum).remove(busesId);
    }

    @Override
    public void flush() {
        allBuffers.forEach(buffer -> buffer.applyToCollection((p, buffer2) -> buffer2.flush()));
    }
}
