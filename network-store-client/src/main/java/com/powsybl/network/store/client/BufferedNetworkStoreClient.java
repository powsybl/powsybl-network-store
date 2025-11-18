/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Stopwatch;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.network.store.client.util.ExecutorUtil;
import com.powsybl.network.store.iidm.impl.AbstractForwardingNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkCollectionIndex;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class BufferedNetworkStoreClient extends AbstractForwardingNetworkStoreClient<RestNetworkStoreClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferedNetworkStoreClient.class);

    private final NetworkCollectionIndex<CollectionBuffer<NetworkAttributes>> networkResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>((networkUuid, resources) -> delegate.createNetworks(resources),
                (networkUuid, resources, attributeFilter) -> delegate.updateNetworks(resources, attributeFilter),
                (networkUuid, variantNum, ids) -> delegate.deleteNetwork(networkUuid, variantNum)));

    private final NetworkCollectionIndex<CollectionBuffer<SubstationAttributes>> substationResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createSubstations,
                delegate::updateSubstations,
                delegate::removeSubstations));

    private final NetworkCollectionIndex<CollectionBuffer<VoltageLevelAttributes>> voltageLevelResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createVoltageLevels,
                delegate::updateVoltageLevels,
                delegate::removeVoltageLevels));

    private final NetworkCollectionIndex<CollectionBuffer<GeneratorAttributes>> generatorResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createGenerators,
                delegate::updateGenerators,
                delegate::removeGenerators));

    private final NetworkCollectionIndex<CollectionBuffer<BatteryAttributes>> batteryResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createBatteries,
                delegate::updateBatteries,
                delegate::removeBatteries));

    private final NetworkCollectionIndex<CollectionBuffer<LoadAttributes>> loadResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createLoads,
                delegate::updateLoads,
                delegate::removeLoads));

    private final NetworkCollectionIndex<CollectionBuffer<BusbarSectionAttributes>> busbarSectionResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createBusbarSections,
                delegate::updateBusbarSections,
                delegate::removeBusBarSections));

    private final NetworkCollectionIndex<CollectionBuffer<SwitchAttributes>> switchResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createSwitches,
                delegate::updateSwitches,
                delegate::removeSwitches));

    private final NetworkCollectionIndex<CollectionBuffer<ShuntCompensatorAttributes>> shuntCompensatorResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createShuntCompensators,
                delegate::updateShuntCompensators,
                delegate::removeShuntCompensators));

    private final NetworkCollectionIndex<CollectionBuffer<VscConverterStationAttributes>> vscConverterStationResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createVscConverterStations,
                delegate::updateVscConverterStations,
                delegate::removeVscConverterStations));

    private final NetworkCollectionIndex<CollectionBuffer<LccConverterStationAttributes>> lccConverterStationResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createLccConverterStations,
                delegate::updateLccConverterStations,
                delegate::removeLccConverterStations));

    private final NetworkCollectionIndex<CollectionBuffer<StaticVarCompensatorAttributes>> svcResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createStaticVarCompensators,
                delegate::updateStaticVarCompensators,
                delegate::removeStaticVarCompensators));

    private final NetworkCollectionIndex<CollectionBuffer<HvdcLineAttributes>> hvdcLineResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createHvdcLines,
                delegate::updateHvdcLines,
                delegate::removeHvdcLines));

    private final NetworkCollectionIndex<CollectionBuffer<DanglingLineAttributes>> danglingLineResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createDanglingLines,
                delegate::updateDanglingLines,
                delegate::removeDanglingLines));

    private final NetworkCollectionIndex<CollectionBuffer<GroundAttributes>> groundResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createGrounds,
                delegate::updateGrounds,
                delegate::removeGrounds));

    private final NetworkCollectionIndex<CollectionBuffer<TwoWindingsTransformerAttributes>> twoWindingsTransformerResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createTwoWindingsTransformers,
                delegate::updateTwoWindingsTransformers,
                delegate::removeTwoWindingsTransformers));

    private final NetworkCollectionIndex<CollectionBuffer<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createThreeWindingsTransformers,
                delegate::updateThreeWindingsTransformers,
                delegate::removeThreeWindingsTransformers));

    private final NetworkCollectionIndex<CollectionBuffer<LineAttributes>> lineResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createLines,
                delegate::updateLines,
                delegate::removeLines));

    private final NetworkCollectionIndex<CollectionBuffer<ConfiguredBusAttributes>> busResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createConfiguredBuses,
                delegate::updateConfiguredBuses,
                delegate::removeConfiguredBuses));

    private final NetworkCollectionIndex<CollectionBuffer<TieLineAttributes>> tieLineResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createTieLines,
                delegate::updateTieLines,
                delegate::removeTieLines));

    private final NetworkCollectionIndex<CollectionBuffer<AreaAttributes>> areaResourcesToFlush
            = new NetworkCollectionIndex<>(() -> new CollectionBuffer<>(delegate::createAreas,
                delegate::updateAreas,
                delegate::removeAreas));

    private final NetworkCollectionIndex<ExternalAttributesCollectionBuffer<Map<Integer, Set<String>>>> operationalLimitsToFlush =
            new NetworkCollectionIndex<>(() -> new ExternalAttributesCollectionBuffer<>(delegate::removeOperationalLimitsGroupAttributes,
                    (globalMap, mapToAdd) ->
                            mapToAdd.forEach((branchId, limitSetBySide) ->
                                limitSetBySide.forEach((side, limitIdSet) ->
                                        globalMap.computeIfAbsent(branchId, s -> new HashMap<>())
                                                .computeIfAbsent(side, s -> new HashSet<>())
                                                .addAll(limitIdSet)))));

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
            groundResourcesToFlush,
            twoWindingsTransformerResourcesToFlush,
            threeWindingsTransformerResourcesToFlush,
            lineResourcesToFlush,
            busResourcesToFlush,
            tieLineResourcesToFlush,
            areaResourcesToFlush);

    private final ExecutorService executorService;

    public BufferedNetworkStoreClient(RestNetworkStoreClient delegate, ExecutorService executorService) {
        super(delegate);
        this.executorService = Objects.requireNonNull(executorService);
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
    public void updateNetworks(List<Resource<NetworkAttributes>> networkResources, AttributeFilter attributeFilter) {
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            UUID networkUuid = networkResource.getAttributes().getUuid();
            int variantNum = networkResource.getVariantNum();
            networkResourcesToFlush.getCollection(networkUuid, variantNum).update(networkResource, attributeFilter);
        }
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        delegate.deleteNetwork(networkUuid);
        // clear buffers as server side delete network already remove all equipments of the network
        allBuffers.forEach(buffer -> buffer.removeCollection(networkUuid));
        operationalLimitsToFlush.removeCollection(networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        delegate.deleteNetwork(networkUuid, variantNum);
        // clear buffers as server side delete network already remove all equipments of the network
        allBuffers.forEach(buffer -> buffer.removeCollection(networkUuid, variantNum));
        operationalLimitsToFlush.removeCollection(networkUuid, variantNum);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            substationResourcesToFlush.getCollection(networkUuid, substationResource.getVariantNum()).create(substationResource);
        }
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources, AttributeFilter attributeFilter) {
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            substationResourcesToFlush.getCollection(networkUuid, substationResource.getVariantNum()).update(substationResource, attributeFilter);
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
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources, AttributeFilter attributeFilter) {
        for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
            voltageLevelResourcesToFlush.getCollection(networkUuid, voltageLevelResource.getVariantNum()).update(voltageLevelResource, attributeFilter);
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
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources, AttributeFilter attributeFilter) {
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            switchResourcesToFlush.getCollection(networkUuid, switchResource.getVariantNum()).update(switchResource, attributeFilter);
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
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources, AttributeFilter attributeFilter) {
        for (Resource<BusbarSectionAttributes> busbarSectionResource : busbarSectionResources) {
            busbarSectionResourcesToFlush.getCollection(networkUuid, busbarSectionResource.getVariantNum()).update(busbarSectionResource, attributeFilter);
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
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources, AttributeFilter attributeFilter) {
        for (Resource<LoadAttributes> loadResource : loadResources) {
            loadResourcesToFlush.getCollection(networkUuid, loadResource.getVariantNum()).update(loadResource, attributeFilter);
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
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources, AttributeFilter attributeFilter) {
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            generatorResourcesToFlush.getCollection(networkUuid, generatorResource.getVariantNum()).update(generatorResource, attributeFilter);
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
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources, AttributeFilter attributeFilter) {
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            batteryResourcesToFlush.getCollection(networkUuid, batteryResource.getVariantNum()).update(batteryResource, attributeFilter);
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
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources, AttributeFilter attributeFilter) {
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            twoWindingsTransformerResourcesToFlush.getCollection(networkUuid, twoWindingsTransformerResource.getVariantNum()).update(twoWindingsTransformerResource, attributeFilter);
        }
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        twoWindingsTransformerResourcesToFlush.getCollection(networkUuid, variantNum).remove(twoWindingsTransformersId);
    }

    // Grounds

    @Override
    public void createGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources) {
        for (Resource<GroundAttributes> groundResource : groundResources) {
            groundResourcesToFlush.getCollection(networkUuid, groundResource.getVariantNum()).create(groundResource);
        }
    }

    @Override
    public void updateGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources, AttributeFilter attributeFilter) {
        for (Resource<GroundAttributes> groundResource : groundResources) {
            groundResourcesToFlush.getCollection(networkUuid, groundResource.getVariantNum()).update(groundResource, attributeFilter);
        }
    }

    @Override
    public void removeGrounds(UUID networkUuid, int variantNum, List<String> groundsId) {
        groundResourcesToFlush.getCollection(networkUuid, variantNum).remove(groundsId);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            threeWindingsTransformerResourcesToFlush.getCollection(networkUuid, threeWindingsTransformerResource.getVariantNum()).create(threeWindingsTransformerResource);
        }
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources, AttributeFilter attributeFilter) {
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            threeWindingsTransformerResourcesToFlush.getCollection(networkUuid, threeWindingsTransformerResource.getVariantNum()).update(threeWindingsTransformerResource, attributeFilter);
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
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources, AttributeFilter attributeFilter) {
        for (Resource<LineAttributes> lineResource : lineResources) {
            lineResourcesToFlush.getCollection(networkUuid, lineResource.getVariantNum()).update(lineResource, attributeFilter);
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
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources, AttributeFilter attributeFilter) {
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            shuntCompensatorResourcesToFlush.getCollection(networkUuid, shuntCompensatorResource.getVariantNum()).update(shuntCompensatorResource, attributeFilter);
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
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources, AttributeFilter attributeFilter) {
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            vscConverterStationResourcesToFlush.getCollection(networkUuid, vscConverterStationResource.getVariantNum()).update(vscConverterStationResource, attributeFilter);
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
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources, AttributeFilter attributeFilter) {
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            lccConverterStationResourcesToFlush.getCollection(networkUuid, lccConverterStationResource.getVariantNum()).update(lccConverterStationResource, attributeFilter);
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
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources, AttributeFilter attributeFilter) {
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            svcResourcesToFlush.getCollection(networkUuid, svcResource.getVariantNum()).update(svcResource, attributeFilter);
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
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources, AttributeFilter attributeFilter) {
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            hvdcLineResourcesToFlush.getCollection(networkUuid, hvdcLineResource.getVariantNum()).update(hvdcLineResource, attributeFilter);
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
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources, AttributeFilter attributeFilter) {
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            danglingLineResourcesToFlush.getCollection(networkUuid, danglingLineResource.getVariantNum()).update(danglingLineResource, attributeFilter);
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
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources, AttributeFilter attributeFilter) {
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            busResourcesToFlush.getCollection(networkUuid, busResource.getVariantNum()).update(busResource, attributeFilter);
        }
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        busResourcesToFlush.getCollection(networkUuid, variantNum).remove(busesId);
    }

    @Override
    public void createTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources) {
        for (Resource<TieLineAttributes> tieLineResource : tieLineResources) {
            tieLineResourcesToFlush.getCollection(networkUuid, tieLineResource.getVariantNum()).create(tieLineResource);
        }
    }

    @Override
    public void updateTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources, AttributeFilter attributeFilter) {
        for (Resource<TieLineAttributes> tieLineResource : tieLineResources) {
            tieLineResourcesToFlush.getCollection(networkUuid, tieLineResource.getVariantNum()).update(tieLineResource, attributeFilter);
        }
    }

    @Override
    public void removeTieLines(UUID networkUuid, int variantNum, List<String> tieLinesId) {
        tieLineResourcesToFlush.getCollection(networkUuid, variantNum).remove(tieLinesId);
    }

    // Areas
    @Override
    public void createAreas(UUID networkUuid, List<Resource<AreaAttributes>> areaResources) {
        for (Resource<AreaAttributes> areaResource : areaResources) {
            areaResourcesToFlush.getCollection(networkUuid, areaResource.getVariantNum()).create(areaResource);
        }
    }

    @Override
    public void updateAreas(UUID networkUuid, List<Resource<AreaAttributes>> areaResources, AttributeFilter attributeFilter) {
        for (Resource<AreaAttributes> areaResource : areaResources) {
            areaResourcesToFlush.getCollection(networkUuid, areaResource.getVariantNum()).update(areaResource, attributeFilter);
        }
    }

    @Override
    public void removeAreas(UUID networkUuid, int variantNum, List<String> areasId) {
        areaResourcesToFlush.getCollection(networkUuid, variantNum).remove(areasId);
    }

    @Override
    public void removeOperationalLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, Map<String, Map<Integer, Set<String>>> operationalLimitsGroupsToDelete) {
        operationalLimitsToFlush.getCollection(networkUuid, variantNum).remove(operationalLimitsGroupsToDelete, resourceType);
    }

    @Override
    public void flush(UUID networkUuid) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Future<?>> futures = new ArrayList<>(allBuffers.size());
        for (var buffer : allBuffers) {
            futures.add(executorService.submit(() -> buffer.applyToCollection(networkUuid, (variantNum, b) -> b.flush(networkUuid, variantNum))));
        }
        futures.add(executorService.submit(() -> operationalLimitsToFlush.applyToCollection(networkUuid, (variantNum, b) -> b.flush(networkUuid, variantNum))));
        ExecutorUtil.waitAllFutures(futures);
        stopwatch.stop();
        LOGGER.info("All buffers flushed in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    private static <T extends IdentifiableAttributes> void cloneBuffer(NetworkCollectionIndex<CollectionBuffer<T>> buffer, UUID networkUuid,
                                                                       int sourceVariantNum, int targetVariantNum, ObjectMapper objectMapper,
                                                                       Consumer<Resource<T>> resourcePostProcessor) {
        // clone resources from source variant collection
        var clonedCollection = buffer.getCollection(networkUuid, sourceVariantNum)
            .clone(objectMapper, targetVariantNum, resourcePostProcessor);
        buffer.addCollection(networkUuid, targetVariantNum, clonedCollection);
    }

    private static <T extends IdentifiableAttributes> void cloneBuffer(NetworkCollectionIndex<CollectionBuffer<T>> buffer, UUID networkUuid,
                                                                       int sourceVariantNum, int targetVariantNum, ObjectMapper objectMapper) {
        cloneBuffer(buffer, networkUuid, sourceVariantNum, targetVariantNum, objectMapper, null);
    }

    @Override
    public void cloneNetwork(UUID networkUuid, int sourceVariantNum, int targetVariantNum, String targetVariantId) {
        delegate.cloneNetwork(networkUuid, sourceVariantNum, targetVariantNum, targetVariantId);

        var objectMapper = JsonUtil.createObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        //can't use allBuffers because of generics compile error...
        cloneBuffer(switchResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(busbarSectionResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(loadResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(generatorResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(batteryResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(groundResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(twoWindingsTransformerResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(threeWindingsTransformerResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(lineResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(shuntCompensatorResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(vscConverterStationResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(lccConverterStationResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(svcResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(hvdcLineResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(danglingLineResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(busResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(substationResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(voltageLevelResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(tieLineResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(areaResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper);
        cloneBuffer(networkResourcesToFlush, networkUuid, sourceVariantNum, targetVariantNum, objectMapper,
                networkResource -> {
                    NetworkAttributes networkAttributes = networkResource.getAttributes();
                    networkAttributes.setVariantId(targetVariantId);
                    if (networkAttributes.isFullVariant()) {
                        networkAttributes.setFullVariantNum(sourceVariantNum);
                    }
                });
        ExternalAttributesCollectionBuffer<Map<Integer, Set<String>>> clonedCollection =
                new ExternalAttributesCollectionBuffer<>(operationalLimitsToFlush.getCollection(networkUuid, sourceVariantNum));
        operationalLimitsToFlush.addCollection(networkUuid, targetVariantNum, clonedCollection);
    }

    @Override
    public List<String> getIdentifiablesIds(UUID networkUuid, int variantNum) {
        List<String> identifiablesIds = super.getIdentifiablesIds(networkUuid, variantNum);
        for (var buffer : allBuffers) {
            CollectionBuffer<? extends IdentifiableAttributes> collection = buffer.getCollection(networkUuid, variantNum);
            identifiablesIds.addAll(collection.getCreateResourcesIds());
            identifiablesIds.removeAll(collection.getRemoveResourcesIds());
        }
        return identifiablesIds;
    }
}
