/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.google.common.base.Stopwatch;
import com.powsybl.network.store.client.util.ExecutorUtil;
import com.powsybl.network.store.iidm.impl.AbstractForwardingNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkCollectionIndex;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Per collection preloading.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class PreloadingNetworkStoreClient extends AbstractForwardingNetworkStoreClient implements NetworkStoreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreloadingNetworkStoreClient.class);

    static final Set<ResourceType> RESOURCE_TYPES_NEEDED_FOR_BUS_VIEW = Set.of(
        ResourceType.SUBSTATION,
        ResourceType.VOLTAGE_LEVEL,
        ResourceType.LOAD,
        ResourceType.GENERATOR,
        ResourceType.BATTERY,
        ResourceType.SHUNT_COMPENSATOR,
        ResourceType.VSC_CONVERTER_STATION,
        ResourceType.LCC_CONVERTER_STATION,
        ResourceType.STATIC_VAR_COMPENSATOR,
        ResourceType.BUSBAR_SECTION, // FIXME this should not be in the list but as connectable visitor also visit busbar sections we need to keep it
        ResourceType.GROUND,
        ResourceType.TWO_WINDINGS_TRANSFORMER,
        ResourceType.THREE_WINDINGS_TRANSFORMER,
        ResourceType.LINE,
        ResourceType.HVDC_LINE,
        ResourceType.DANGLING_LINE,
        ResourceType.TIE_LINE
    );

    private final boolean allCollectionsNeededForBusView;

    private final ExecutorService executorService;

    private final NetworkCollectionIndex<Set<ResourceType>> cachedResourceTypes
            = new NetworkCollectionIndex<>(() -> EnumSet.noneOf(ResourceType.class));

    public PreloadingNetworkStoreClient(CachedNetworkStoreClient delegate, boolean allCollectionsNeededForBusView,
                                        ExecutorService executorService) {
        super(delegate);
        this.allCollectionsNeededForBusView = allCollectionsNeededForBusView;
        this.executorService = Objects.requireNonNull(executorService);
    }

    private void loadToCache(ResourceType resourceType, UUID networkUuid, int variantNum) {
        switch (resourceType) {
            case SUBSTATION -> delegate.getSubstations(networkUuid, variantNum);
            case VOLTAGE_LEVEL -> delegate.getVoltageLevels(networkUuid, variantNum);
            case LOAD -> delegate.getLoads(networkUuid, variantNum);
            case GENERATOR -> delegate.getGenerators(networkUuid, variantNum);
            case BATTERY -> delegate.getBatteries(networkUuid, variantNum);
            case SHUNT_COMPENSATOR -> delegate.getShuntCompensators(networkUuid, variantNum);
            case VSC_CONVERTER_STATION -> delegate.getVscConverterStations(networkUuid, variantNum);
            case LCC_CONVERTER_STATION -> delegate.getLccConverterStations(networkUuid, variantNum);
            case STATIC_VAR_COMPENSATOR -> delegate.getStaticVarCompensators(networkUuid, variantNum);
            case BUSBAR_SECTION -> delegate.getBusbarSections(networkUuid, variantNum);
            case SWITCH -> delegate.getSwitches(networkUuid, variantNum);
            case GROUND -> delegate.getGrounds(networkUuid, variantNum);
            case TWO_WINDINGS_TRANSFORMER -> delegate.getTwoWindingsTransformers(networkUuid, variantNum);
            case THREE_WINDINGS_TRANSFORMER -> delegate.getThreeWindingsTransformers(networkUuid, variantNum);
            case LINE -> delegate.getLines(networkUuid, variantNum);
            case HVDC_LINE -> delegate.getHvdcLines(networkUuid, variantNum);
            case DANGLING_LINE -> delegate.getDanglingLines(networkUuid, variantNum);
            case CONFIGURED_BUS -> delegate.getConfiguredBuses(networkUuid, variantNum);
            case TIE_LINE -> delegate.getTieLines(networkUuid, variantNum);
            default -> {
                // Do nothing
            }
        }
    }

    private void loadAllCollectionsNeededForBusView(UUID networkUuid, int variantNum, Set<ResourceType> resourceTypes) {
        // directly load all collections
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Future<?>> futures = new ArrayList<>(RESOURCE_TYPES_NEEDED_FOR_BUS_VIEW.size());
        for (ResourceType resourceType : RESOURCE_TYPES_NEEDED_FOR_BUS_VIEW) {
            futures.add(executorService.submit(() -> loadToCache(resourceType, networkUuid, variantNum)));
        }
        ExecutorUtil.waitAllFutures(futures);
        resourceTypes.addAll(RESOURCE_TYPES_NEEDED_FOR_BUS_VIEW);
        stopwatch.stop();
        LOGGER.info("All collections needed for bus view loaded in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    boolean isResourceTypeCached(UUID networkUuid, int variantNum, ResourceType resourceType) {
        Set<ResourceType> resourceTypes = cachedResourceTypes.getCollection(networkUuid, variantNum);
        Objects.requireNonNull(resourceType);
        return resourceTypes.contains(resourceType);
    }

    private void ensureCached(ResourceType resourceType, UUID networkUuid, int variantNum) {
        Objects.requireNonNull(resourceType);
        Objects.requireNonNull(networkUuid);
        Set<ResourceType> resourceTypes = cachedResourceTypes.getCollection(networkUuid, variantNum);
        if (!resourceTypes.contains(resourceType)) {
            if (allCollectionsNeededForBusView && RESOURCE_TYPES_NEEDED_FOR_BUS_VIEW.contains(resourceType)) {
                loadAllCollectionsNeededForBusView(networkUuid, variantNum, resourceTypes);
            } else {
                loadToCache(resourceType, networkUuid, variantNum);
                resourceTypes.add(resourceType);
            }
        }
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        delegate.deleteNetwork(networkUuid);
        cachedResourceTypes.removeCollection(networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        delegate.deleteNetwork(networkUuid, variantNum);
        cachedResourceTypes.removeCollection(networkUuid, variantNum);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            ensureCached(ResourceType.SUBSTATION, networkUuid, substationResource.getVariantNum());
        }
        delegate.createSubstations(networkUuid, substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SUBSTATION, networkUuid, variantNum);
        return delegate.getSubstations(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid, variantNum);
        return delegate.getSubstation(networkUuid, variantNum, substationId);
    }

    @Override
    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid, variantNum);
        delegate.removeSubstations(networkUuid, variantNum, substationsId);
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources, AttributeFilter attributeFilter) {
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            ensureCached(ResourceType.SUBSTATION, networkUuid, substationResource.getVariantNum());
        }
        delegate.updateSubstations(networkUuid, substationResources, attributeFilter);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        for (Resource<VoltageLevelAttributes> voltageLevelResource : voltageLevelResources) {
            ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid, voltageLevelResource.getVariantNum());
        }
        delegate.createVoltageLevels(networkUuid, voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid, variantNum);
        return delegate.getVoltageLevel(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid, variantNum);
        return delegate.getVoltageLevels(networkUuid, variantNum);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid, variantNum);
        return delegate.getVoltageLevelsInSubstation(networkUuid, variantNum, substationId);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid, variantNum);
        delegate.removeVoltageLevels(networkUuid, variantNum, voltageLevelsId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, variantNum);
        return delegate.getVoltageLevelBusbarSections(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, variantNum);
        delegate.removeBusBarSections(networkUuid, variantNum, busBarSectionsId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.SWITCH, networkUuid, variantNum);
        return delegate.getVoltageLevelSwitches(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.GENERATOR, networkUuid, variantNum);
        return delegate.getVoltageLevelGenerators(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        ensureCached(ResourceType.GENERATOR, networkUuid, variantNum);
        delegate.removeGenerators(networkUuid, variantNum, generatorsId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        return delegate.getVoltageLevelBatteries(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        delegate.removeBatteries(networkUuid, variantNum, batteriesId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        return delegate.getVoltageLevelLoads(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        delegate.removeLoads(networkUuid, variantNum, loadsId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        return delegate.getVoltageLevelShuntCompensators(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        delegate.removeShuntCompensators(networkUuid, variantNum, shuntCompensatorsId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        return delegate.getVoltageLevelStaticVarCompensators(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        delegate.removeStaticVarCompensators(networkUuid, variantNum, staticVarCompensatorsId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getVoltageLevelVscConverterStations(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        delegate.removeVscConverterStations(networkUuid, variantNum, vscConverterStationsId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getVoltageLevelLccConverterStations(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        delegate.removeLccConverterStations(networkUuid, variantNum, lccConverterStationsId);
    }

    @Override
    public List<Resource<GroundAttributes>> getVoltageLevelGrounds(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.GROUND, networkUuid, variantNum);
        return delegate.getVoltageLevelGrounds(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeGrounds(UUID networkUuid, int variantNum, List<String> groundsId) {
        ensureCached(ResourceType.GROUND, networkUuid, variantNum);
        delegate.removeGrounds(networkUuid, variantNum, groundsId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        delegate.removeTwoWindingsTransformers(networkUuid, variantNum, twoWindingsTransformersId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        delegate.removeThreeWindingsTransformers(networkUuid, variantNum, threeWindingsTransformersId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        return delegate.getVoltageLevelLines(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        delegate.removeLines(networkUuid, variantNum, linesId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        return delegate.getVoltageLevelDanglingLines(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            ensureCached(ResourceType.SWITCH, networkUuid, switchResource.getVariantNum());
        }
        delegate.createSwitches(networkUuid, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SWITCH, networkUuid, variantNum);
        return delegate.getSwitches(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        ensureCached(ResourceType.SWITCH, networkUuid, variantNum);
        return delegate.getSwitch(networkUuid, variantNum, switchId);
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources, AttributeFilter attributeFilter) {
        for (Resource<SwitchAttributes> switchResource : switchResources) {
            ensureCached(ResourceType.SWITCH, networkUuid, switchResource.getVariantNum());
        }
        delegate.updateSwitches(networkUuid, switchResources, attributeFilter);
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        ensureCached(ResourceType.SWITCH, networkUuid, variantNum);
        delegate.removeSwitches(networkUuid, variantNum, switchesId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        for (Resource<BusbarSectionAttributes> busbarSectionResource : busbarSectionResources) {
            ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, busbarSectionResource.getVariantNum());
        }
        delegate.createBusbarSections(networkUuid, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, variantNum);
        return delegate.getBusbarSections(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, variantNum);
        return delegate.getBusbarSection(networkUuid, variantNum, busbarSectionId);
    }

    @Override
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources, AttributeFilter attributeFilter) {
        for (Resource<BusbarSectionAttributes> busbarSectionResource : busbarSectionResources) {
            ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, busbarSectionResource.getVariantNum());
        }
        delegate.updateBusbarSections(networkUuid, busbarSectionResources, attributeFilter);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        for (Resource<LoadAttributes> loadResource : loadResources) {
            ensureCached(ResourceType.LOAD, networkUuid, loadResource.getVariantNum());
        }
        delegate.createLoads(networkUuid, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        return delegate.getLoads(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        return delegate.getLoad(networkUuid, variantNum, loadId);
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources, AttributeFilter attributeFilter) {
        for (Resource<LoadAttributes> loadResource : loadResources) {
            ensureCached(ResourceType.LOAD, networkUuid, loadResource.getVariantNum());
        }
        delegate.updateLoads(networkUuid, loadResources, attributeFilter);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            ensureCached(ResourceType.GENERATOR, networkUuid, generatorResource.getVariantNum());
        }
        delegate.createGenerators(networkUuid, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.GENERATOR, networkUuid, variantNum);
        return delegate.getGenerators(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        ensureCached(ResourceType.GENERATOR, networkUuid, variantNum);
        return delegate.getGenerator(networkUuid, variantNum, generatorId);
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources, AttributeFilter attributeFilter) {
        for (Resource<GeneratorAttributes> generatorResource : generatorResources) {
            ensureCached(ResourceType.GENERATOR, networkUuid, generatorResource.getVariantNum());
        }
        delegate.updateGenerators(networkUuid, generatorResources, attributeFilter);
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            ensureCached(ResourceType.BATTERY, networkUuid, batteryResource.getVariantNum());
        }
        delegate.createBatteries(networkUuid, batteryResources);
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        return delegate.getBatteries(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        return delegate.getBattery(networkUuid, variantNum, batteryId);
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources, AttributeFilter attributeFilter) {
        for (Resource<BatteryAttributes> batteryResource : batteryResources) {
            ensureCached(ResourceType.BATTERY, networkUuid, batteryResource.getVariantNum());
        }
        delegate.updateBatteries(networkUuid, batteryResources, attributeFilter);
    }

    @Override
    public void createGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources) {
        for (Resource<GroundAttributes> groundResource : groundResources) {
            ensureCached(ResourceType.GROUND, networkUuid, groundResource.getVariantNum());
        }
        delegate.createGrounds(networkUuid, groundResources);
    }

    @Override
    public List<Resource<GroundAttributes>> getGrounds(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.GROUND, networkUuid, variantNum);
        return delegate.getGrounds(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<GroundAttributes>> getGround(UUID networkUuid, int variantNum, String groundId) {
        ensureCached(ResourceType.GROUND, networkUuid, variantNum);
        return delegate.getGround(networkUuid, variantNum, groundId);
    }

    @Override
    public void updateGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources, AttributeFilter attributeFilter) {
        for (Resource<GroundAttributes> groundResource : groundResources) {
            ensureCached(ResourceType.GROUND, networkUuid, groundResource.getVariantNum());
        }
        delegate.updateGrounds(networkUuid, groundResources, attributeFilter);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, twoWindingsTransformerResource.getVariantNum());
        }
        delegate.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getTwoWindingsTransformers(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getTwoWindingsTransformer(networkUuid, variantNum, twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources, AttributeFilter attributeFilter) {
        for (Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource : twoWindingsTransformerResources) {
            ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, twoWindingsTransformerResource.getVariantNum());
        }
        delegate.updateTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources, attributeFilter);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, threeWindingsTransformerResource.getVariantNum());
        }
        delegate.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getThreeWindingsTransformers(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getThreeWindingsTransformer(networkUuid, variantNum, threeWindingsTransformerId);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources, AttributeFilter attributeFilter) {
        for (Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource : threeWindingsTransformerResources) {
            ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, threeWindingsTransformerResource.getVariantNum());
        }
        delegate.updateThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources, attributeFilter);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        for (Resource<LineAttributes> lineResource : lineResources) {
            ensureCached(ResourceType.LINE, networkUuid, lineResource.getVariantNum());
        }
        delegate.createLines(networkUuid, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        return delegate.getLines(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        return delegate.getLine(networkUuid, variantNum, lineId);
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources, AttributeFilter attributeFilter) {
        for (Resource<LineAttributes> lineResource : lineResources) {
            ensureCached(ResourceType.LINE, networkUuid, lineResource.getVariantNum());
        }
        delegate.updateLines(networkUuid, lineResources, attributeFilter);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, shuntCompensatorResource.getVariantNum());
        }
        delegate.createShuntCompensators(networkUuid, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        return delegate.getShuntCompensators(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        return delegate.getShuntCompensator(networkUuid, variantNum, shuntCompensatorId);
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources, AttributeFilter attributeFilter) {
        for (Resource<ShuntCompensatorAttributes> shuntCompensatorResource : shuntCompensatorResources) {
            ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, shuntCompensatorResource.getVariantNum());
        }
        delegate.updateShuntCompensators(networkUuid, shuntCompensatorResources, attributeFilter);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, vscConverterStationResource.getVariantNum());
        }
        delegate.createVscConverterStations(networkUuid, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getVscConverterStations(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getVscConverterStation(networkUuid, variantNum, vscConverterStationId);
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources, AttributeFilter attributeFilter) {
        for (Resource<VscConverterStationAttributes> vscConverterStationResource : vscConverterStationResources) {
            ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, vscConverterStationResource.getVariantNum());
        }
        delegate.updateVscConverterStations(networkUuid, vscConverterStationResources, attributeFilter);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, lccConverterStationResource.getVariantNum());
        }
        delegate.createLccConverterStations(networkUuid, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getLccConverterStations(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getLccConverterStation(networkUuid, variantNum, lccConverterStationId);
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources, AttributeFilter attributeFilter) {
        for (Resource<LccConverterStationAttributes> lccConverterStationResource : lccConverterStationResources) {
            ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, lccConverterStationResource.getVariantNum());
        }
        delegate.updateLccConverterStations(networkUuid, lccConverterStationResources, attributeFilter);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, svcResource.getVariantNum());
        }
        delegate.createStaticVarCompensators(networkUuid, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        return delegate.getStaticVarCompensators(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        return delegate.getStaticVarCompensator(networkUuid, variantNum, staticVarCompensatorId);
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources, AttributeFilter attributeFilter) {
        for (Resource<StaticVarCompensatorAttributes> svcResource : svcResources) {
            ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, svcResource.getVariantNum());
        }
        delegate.updateStaticVarCompensators(networkUuid, svcResources, attributeFilter);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            ensureCached(ResourceType.HVDC_LINE, networkUuid, hvdcLineResource.getVariantNum());
        }
        delegate.createHvdcLines(networkUuid, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid, variantNum);
        return delegate.getHvdcLines(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid, variantNum);
        return delegate.getHvdcLine(networkUuid, variantNum, hvdcLineId);
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid, variantNum);
        delegate.removeHvdcLines(networkUuid, variantNum, hvdcLinesId);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources, AttributeFilter attributeFilter) {
        for (Resource<HvdcLineAttributes> hvdcLineResource : hvdcLineResources) {
            ensureCached(ResourceType.HVDC_LINE, networkUuid, hvdcLineResource.getVariantNum());
        }
        delegate.updateHvdcLines(networkUuid, hvdcLineResources, attributeFilter);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            ensureCached(ResourceType.DANGLING_LINE, networkUuid, danglingLineResource.getVariantNum());
        }
        delegate.createDanglingLines(networkUuid, danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        return delegate.getDanglingLines(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        return delegate.getDanglingLine(networkUuid, variantNum, danglingLineId);
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources, AttributeFilter attributeFilter) {
        for (Resource<DanglingLineAttributes> danglingLineResource : danglingLineResources) {
            ensureCached(ResourceType.DANGLING_LINE, networkUuid, danglingLineResource.getVariantNum());
        }
        delegate.updateDanglingLines(networkUuid, danglingLineResources, attributeFilter);
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        delegate.removeDanglingLines(networkUuid, variantNum, danglingLinesId);
    }

    @Override
    public void createTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources) {
        for (Resource<TieLineAttributes> tieLineResource : tieLineResources) {
            ensureCached(ResourceType.TIE_LINE, networkUuid, tieLineResource.getVariantNum());
        }
        delegate.createTieLines(networkUuid, tieLineResources);
    }

    @Override
    public List<Resource<TieLineAttributes>> getTieLines(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.TIE_LINE, networkUuid, variantNum);
        return delegate.getTieLines(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<TieLineAttributes>> getTieLine(UUID networkUuid, int variantNum, String tieLineId) {
        ensureCached(ResourceType.TIE_LINE, networkUuid, variantNum);
        return delegate.getTieLine(networkUuid, variantNum, tieLineId);
    }

    @Override
    public void removeTieLines(UUID networkUuid, int variantNum, List<String> tieLinesId) {
        ensureCached(ResourceType.TIE_LINE, networkUuid, variantNum);
        delegate.removeTieLines(networkUuid, variantNum, tieLinesId);
    }

    @Override
    public void updateTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources, AttributeFilter attributeFilter) {
        for (Resource<TieLineAttributes> tieLineResource : tieLineResources) {
            ensureCached(ResourceType.TIE_LINE, networkUuid, tieLineResource.getVariantNum());
        }
        delegate.updateTieLines(networkUuid, tieLineResources, attributeFilter);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources) {
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, busResource.getVariantNum());
        }
        delegate.createConfiguredBuses(networkUuid, busResources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, variantNum);
        return delegate.getConfiguredBuses(networkUuid, variantNum);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, variantNum);
        return delegate.getVoltageLevelConfiguredBuses(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, variantNum);
        return delegate.getConfiguredBus(networkUuid, variantNum, busId);
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busResources, AttributeFilter attributeFilter) {
        for (Resource<ConfiguredBusAttributes> busResource : busResources) {
            ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, busResource.getVariantNum());
        }
        delegate.updateConfiguredBuses(networkUuid, busResources, attributeFilter);
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> configuredBusesId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, variantNum);
        delegate.removeConfiguredBuses(networkUuid, variantNum, configuredBusesId);
    }

    @Override
    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        delegate.getAllExtensionsAttributesByResourceTypeAndExtensionName(networkUuid, variantNum, resourceType, extensionName);
        return delegate.getExtensionAttributes(networkUuid, variantNum, resourceType, identifiableId, extensionName);
    }

    @Override
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType resourceType, String id) {
        delegate.getAllExtensionsAttributesByResourceType(networkUuid, variantNum, resourceType);
        return delegate.getAllExtensionsAttributesByIdentifiableId(networkUuid, variantNum, resourceType, id);
    }

    @Override
    public Optional<OperationalLimitsGroupAttributes> getOperationalLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String branchId, String operationalLimitGroupId, int side) {
        delegate.getAllOperationalLimitsGroupAttributesByResourceType(networkUuid, variantNum, resourceType);
        return delegate.getOperationalLimitsGroupAttributes(networkUuid, variantNum, resourceType, branchId, operationalLimitGroupId, side);
    }

    @Override
    public Optional<OperationalLimitsGroupAttributes> getCurrentLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String branchId, String operationalLimitGroupId, int side) {
        delegate.getSelectedCurrentLimitsGroupAttributesByResourceType(networkUuid, variantNum, resourceType);
        return delegate.getOperationalLimitsGroupAttributes(networkUuid, variantNum, resourceType, branchId, operationalLimitGroupId, side);
    }
}
