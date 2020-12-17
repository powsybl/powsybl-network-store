/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.ForwardingNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class PreloadingNetworkStoreClient extends ForwardingNetworkStoreClient implements NetworkStoreClient {

    private final Map<Pair<UUID, Integer>, Set<ResourceType>> cachedResourceTypes = new HashMap<>();

    public PreloadingNetworkStoreClient(NetworkStoreClient delegate) {
        super(new CachedNetworkStoreClient(delegate));
    }

    private void loadToCache(ResourceType resourceType, UUID networkUuid, int variantNum) {
        switch (resourceType) {
            case NETWORK:
                delegate.getNetworks(variantNum);
                break;
            case SUBSTATION:
                delegate.getSubstations(networkUuid, variantNum);
                break;
            case VOLTAGE_LEVEL:
                delegate.getVoltageLevels(networkUuid, variantNum);
                break;
            case LOAD:
                delegate.getLoads(networkUuid, variantNum);
                break;
            case GENERATOR:
                delegate.getGenerators(networkUuid, variantNum);
                break;
            case BATTERY:
                delegate.getBatteries(networkUuid, variantNum);
                break;
            case SHUNT_COMPENSATOR:
                delegate.getShuntCompensators(networkUuid, variantNum);
                break;
            case VSC_CONVERTER_STATION:
                delegate.getVscConverterStations(networkUuid, variantNum);
                break;
            case LCC_CONVERTER_STATION:
                delegate.getLccConverterStations(networkUuid, variantNum);
                break;
            case STATIC_VAR_COMPENSATOR:
                delegate.getStaticVarCompensators(networkUuid, variantNum);
                break;
            case BUSBAR_SECTION:
                delegate.getBusbarSections(networkUuid, variantNum);
                break;
            case SWITCH:
                delegate.getSwitches(networkUuid, variantNum);
                break;
            case TWO_WINDINGS_TRANSFORMER:
                delegate.getTwoWindingsTransformers(networkUuid, variantNum);
                break;
            case THREE_WINDINGS_TRANSFORMER:
                delegate.getThreeWindingsTransformers(networkUuid, variantNum);
                break;
            case LINE:
                delegate.getLines(networkUuid, variantNum);
                break;
            case HVDC_LINE:
                delegate.getHvdcLines(networkUuid, variantNum);
                break;
            case DANGLING_LINE:
                delegate.getDanglingLines(networkUuid, variantNum);
                break;
            case CONFIGURED_BUS:
                delegate.getConfiguredBuses(networkUuid, variantNum);
                break;
        }
    }

    private void ensureCached(ResourceType resourceType, UUID networkUuid, int variantNum) {
        Objects.requireNonNull(resourceType);
        if (resourceType != ResourceType.NETWORK) {
            Objects.requireNonNull(networkUuid);
        }
        Set<ResourceType> resourceTypes = cachedResourceTypes.computeIfAbsent(Pair.of(networkUuid, variantNum), k -> EnumSet.noneOf(ResourceType.class));
        if (!resourceTypes.contains(resourceType)) {
            loadToCache(resourceType, networkUuid, variantNum);
            resourceTypes.add(resourceType);
        }
    }

    private void clearCache(UUID networkUuid, int variantNum) {
        cachedResourceTypes.remove(Pair.of(networkUuid, variantNum));
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks(int variantNum) {
        ensureCached(ResourceType.NETWORK, null, variantNum);
        return delegate.getNetworks(variantNum);
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        ensureCached(ResourceType.NETWORK, null);
        delegate.createNetworks(networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.NETWORK, null, variantNum);
        return delegate.getNetwork(networkUuid, variantNum);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        delegate.deleteNetwork(networkUuid, variantNum);
        clearCache(networkUuid, variantNum);
    }

    @Override
    public void createSubstations(UUID networkUuid, int variantNum, List<Resource<SubstationAttributes>> substationResources) {
        ensureCached(ResourceType.SUBSTATION, networkUuid, variantNum);
        delegate.createSubstations(networkUuid, variantNum, substationResources);
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
    public int getSubstationCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SUBSTATION, networkUuid, variantNum);
        return delegate.getSubstationCount(networkUuid, variantNum);
    }

    @Override
    public void removeSubstation(UUID networkUuid, int variantNum, String substationId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid, variantNum);
        delegate.removeSubstation(networkUuid, variantNum, substationId);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid, variantNum);
        delegate.createVoltageLevels(networkUuid, variantNum, voltageLevelResources);
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
    public int getVoltageLevelCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid, variantNum);
        return delegate.getVoltageLevelCount(networkUuid, variantNum);
    }

    @Override
    public void removeVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid, variantNum);
        delegate.removeVoltageLevel(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, variantNum);
        return delegate.getVoltageLevelBusbarSections(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, variantNum);
        delegate.removeBusBarSection(networkUuid, variantNum, busBarSectionId);
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
    public void removeGenerator(UUID networkUuid, int variantNum, String generatorId) {
        ensureCached(ResourceType.GENERATOR, networkUuid, variantNum);
        delegate.removeGenerator(networkUuid, variantNum, generatorId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        return delegate.getVoltageLevelBatteries(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBattery(UUID networkUuid, int variantNum, String batteryId) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        delegate.removeBattery(networkUuid, variantNum, batteryId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        return delegate.getVoltageLevelLoads(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLoad(UUID networkUuid, int variantNum, String loadId) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        delegate.removeLoad(networkUuid, variantNum, loadId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        return delegate.getVoltageLevelShuntCompensators(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        delegate.removeShuntCompensator(networkUuid, variantNum, shuntCompensatorId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        return delegate.getVoltageLevelStaticVarCompensators(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        delegate.removeStaticVarCompensator(networkUuid, variantNum, staticVarCompensatorId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getVoltageLevelVscConverterStations(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        delegate.removeVscConverterStation(networkUuid, variantNum, vscConverterStationId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getVoltageLevelLccConverterStations(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        delegate.removeLccConverterStation(networkUuid, variantNum, lccConverterStationId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        delegate.removeTwoWindingsTransformer(networkUuid, variantNum, twoWindingsTransformerId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        delegate.removeThreeWindingsTransformer(networkUuid, variantNum, threeWindingsTransformerId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        return delegate.getVoltageLevelLines(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLine(UUID networkUuid, int variantNum, String lineId) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        delegate.removeLine(networkUuid, variantNum, lineId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        return delegate.getVoltageLevelDanglingLines(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources) {
        ensureCached(ResourceType.SWITCH, networkUuid, variantNum);
        delegate.createSwitches(networkUuid, variantNum, switchResources);
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
    public int getSwitchCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SWITCH, networkUuid, variantNum);
        return delegate.getSwitchCount(networkUuid, variantNum);
    }

    @Override
    public void updateSwitch(UUID networkUuid, int variantNum, Resource<SwitchAttributes> switchResource) {
        ensureCached(ResourceType.SWITCH, networkUuid, variantNum);
        delegate.updateSwitch(networkUuid, variantNum, switchResource);
    }

    @Override
    public void removeSwitch(UUID networkUuid, int variantNum, String switchId) {
        ensureCached(ResourceType.SWITCH, networkUuid, variantNum);
        delegate.removeSwitch(networkUuid, variantNum, switchId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, int variantNum, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, variantNum);
        delegate.createBusbarSections(networkUuid, variantNum, busbarSectionResources);
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
    public int getBusbarSectionCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid, variantNum);
        return delegate.getBusbarSectionCount(networkUuid, variantNum);
    }

    @Override
    public void createLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        delegate.createLoads(networkUuid, variantNum, loadResources);
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
    public int getLoadCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        return delegate.getLoadCount(networkUuid, variantNum);
    }

    @Override
    public void updateLoad(UUID networkUuid, int variantNum, Resource<LoadAttributes> loadResource) {
        ensureCached(ResourceType.LOAD, networkUuid, variantNum);
        delegate.updateLoad(networkUuid, variantNum, loadResource);
    }

    @Override
    public void createGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources) {
        ensureCached(ResourceType.GENERATOR, networkUuid, variantNum);
        delegate.createGenerators(networkUuid, variantNum, generatorResources);
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
    public int getGeneratorCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.GENERATOR, networkUuid, variantNum);
        return delegate.getGeneratorCount(networkUuid, variantNum);
    }

    @Override
    public void updateGenerator(UUID networkUuid, int variantNum, Resource<GeneratorAttributes> generatorResource) {
        ensureCached(ResourceType.GENERATOR, networkUuid, variantNum);
        delegate.updateGenerator(networkUuid, variantNum, generatorResource);
    }

    @Override
    public void createBatteries(UUID networkUuid, int variantNum, List<Resource<BatteryAttributes>> batteryResources) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        delegate.createBatteries(networkUuid, variantNum, batteryResources);
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
    public int getBatteryCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        return delegate.getBatteryCount(networkUuid, variantNum);
    }

    @Override
    public void updateBattery(UUID networkUuid, int variantNum, Resource<BatteryAttributes> batteryResource) {
        ensureCached(ResourceType.BATTERY, networkUuid, variantNum);
        delegate.updateBattery(networkUuid, variantNum, batteryResource);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        delegate.createTwoWindingsTransformers(networkUuid, variantNum, twoWindingsTransformerResources);
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
    public void updateTwoWindingsTransformer(UUID networkUuid, int variantNum, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        delegate.updateTwoWindingsTransformer(networkUuid, variantNum, twoWindingsTransformerResource);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getTwoWindingsTransformerCount(networkUuid, variantNum);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        delegate.createThreeWindingsTransformers(networkUuid, variantNum, threeWindingsTransformerResources);
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
    public void updateThreeWindingsTransformer(UUID networkUuid, int variantNum, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        delegate.updateThreeWindingsTransformer(networkUuid, variantNum, threeWindingsTransformerResource);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, variantNum);
        return delegate.getThreeWindingsTransformerCount(networkUuid, variantNum);
    }

    @Override
    public void createLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResources) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        delegate.createLines(networkUuid, variantNum, lineResources);
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
    public void updateLine(UUID networkUuid, int variantNum, Resource<LineAttributes> lineResource) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        delegate.updateLine(networkUuid, variantNum, lineResource);
    }

    @Override
    public int getLineCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LINE, networkUuid, variantNum);
        return delegate.getLineCount(networkUuid, variantNum);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        delegate.createShuntCompensators(networkUuid, variantNum, shuntCompensatorResources);
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
    public int getShuntCompensatorCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        return delegate.getShuntCompensatorCount(networkUuid, variantNum);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, int variantNum, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid, variantNum);
        delegate.updateShuntCompensator(networkUuid, variantNum, shuntCompensatorResource);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        delegate.createVscConverterStations(networkUuid, variantNum, vscConverterStationResources);
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
    public int getVscConverterStationCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getVscConverterStationCount(networkUuid, variantNum);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, int variantNum, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid, variantNum);
        delegate.updateVscConverterStation(networkUuid, variantNum, vscConverterStationResource);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        delegate.createLccConverterStations(networkUuid, variantNum, lccConverterStationResources);
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
    public int getLccConverterStationCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        return delegate.getLccConverterStationCount(networkUuid, variantNum);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, int variantNum, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid, variantNum);
        delegate.updateLccConverterStation(networkUuid, variantNum, lccConverterStationResource);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        delegate.createStaticVarCompensators(networkUuid, variantNum, svcResources);
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
    public int getStaticVarCompensatorCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        return delegate.getStaticVarCompensatorCount(networkUuid, variantNum);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, int variantNum, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, variantNum);
        delegate.updateStaticVarCompensator(networkUuid, variantNum, staticVarCompensatorResource);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid, variantNum);
        delegate.createHvdcLines(networkUuid, variantNum, hvdcLineResources);
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
    public void removeHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid, variantNum);
        delegate.removeHvdcLine(networkUuid, variantNum, hvdcLineId);
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid, variantNum);
        return delegate.getHvdcLineCount(networkUuid, variantNum);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, int variantNum, Resource<HvdcLineAttributes> hvdcLineResource) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid, variantNum);
        delegate.updateHvdcLine(networkUuid, variantNum, hvdcLineResource);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        delegate.createDanglingLines(networkUuid, variantNum, danglingLineResources);
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
    public int getDanglingLineCount(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        return delegate.getDanglingLineCount(networkUuid, variantNum);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, int variantNum, Resource<DanglingLineAttributes> danglingLineResource) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        delegate.updateDanglingLine(networkUuid, variantNum, danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid, variantNum);
        delegate.removeDanglingLine(networkUuid, variantNum, danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesResources) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, variantNum);
        delegate.createConfiguredBuses(networkUuid, variantNum, busesResources);
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
    public void updateConfiguredBus(UUID networkUuid, int variantNum, Resource<ConfiguredBusAttributes> busesResource) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, variantNum);
        delegate.updateConfiguredBus(networkUuid, variantNum, busesResource);
    }

    @Override
    public void removeConfiguredBus(UUID networkUuid, int variantNum, String configuredBusId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid, variantNum);
        delegate.removeConfiguredBus(networkUuid, variantNum, configuredBusId);
    }

}
