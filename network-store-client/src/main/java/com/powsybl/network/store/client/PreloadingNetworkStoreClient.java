/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.AbstractForwardingNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class PreloadingNetworkStoreClient extends AbstractForwardingNetworkStoreClient implements NetworkStoreClient {

    private final Map<UUID, Set<ResourceType>> cachedResourceTypes = new HashMap<>();

    public PreloadingNetworkStoreClient(CachedNetworkStoreClient delegate) {
        super(delegate);
    }

    private void loadToCache(ResourceType resourceType, UUID networkUuid, int variantNum) {
        switch (resourceType) {
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

    private void ensureCached(ResourceType resourceType, UUID networkUuid) {
        Objects.requireNonNull(resourceType);
        Objects.requireNonNull(networkUuid);
        Set<ResourceType> resourceTypes = cachedResourceTypes.computeIfAbsent(networkUuid, k -> EnumSet.noneOf(ResourceType.class));
        if (!resourceTypes.contains(resourceType)) {
            loadToCache(resourceType, networkUuid, Resource.INITIAL_VARIANT_NUM);
            resourceTypes.add(resourceType);
        }
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        return delegate.getNetworks();
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        delegate.deleteNetwork(networkUuid);
        cachedResourceTypes.remove(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        delegate.createSubstations(networkUuid, substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return delegate.getSubstations(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return delegate.getSubstation(networkUuid, variantNum, substationId);
    }

    @Override
    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        delegate.removeSubstations(networkUuid, variantNum, substationsId);
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        delegate.updateSubstations(networkUuid, substationResources);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        delegate.createVoltageLevels(networkUuid, voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return delegate.getVoltageLevel(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return delegate.getVoltageLevels(networkUuid, variantNum);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return delegate.getVoltageLevelsInSubstation(networkUuid, variantNum, substationId);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        delegate.removeVoltageLevels(networkUuid, variantNum, voltageLevelsId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return delegate.getVoltageLevelBusbarSections(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        delegate.removeBusBarSections(networkUuid, variantNum, busBarSectionsId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return delegate.getVoltageLevelSwitches(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return delegate.getVoltageLevelGenerators(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        delegate.removeGenerators(networkUuid, variantNum, generatorsId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        return delegate.getVoltageLevelBatteries(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        delegate.removeBatteries(networkUuid, variantNum, batteriesId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return delegate.getVoltageLevelLoads(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        delegate.removeLoads(networkUuid, variantNum, loadsId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return delegate.getVoltageLevelShuntCompensators(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        delegate.removeShuntCompensators(networkUuid, variantNum, shuntCompensatorsId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return delegate.getVoltageLevelStaticVarCompensators(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        delegate.removeStaticVarCompensators(networkUuid, variantNum, staticVarCompensatorsId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return delegate.getVoltageLevelVscConverterStations(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        delegate.removeVscConverterStations(networkUuid, variantNum, vscConverterStationsId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return delegate.getVoltageLevelLccConverterStations(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        delegate.removeLccConverterStations(networkUuid, variantNum, lccConverterStationsId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        delegate.removeTwoWindingsTransformers(networkUuid, variantNum, twoWindingsTransformersId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        delegate.removeThreeWindingsTransformers(networkUuid, variantNum, threeWindingsTransformersId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return delegate.getVoltageLevelLines(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        ensureCached(ResourceType.LINE, networkUuid);
        delegate.removeLines(networkUuid, variantNum, linesId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return delegate.getVoltageLevelDanglingLines(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        delegate.createSwitches(networkUuid, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return delegate.getSwitches(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return delegate.getSwitch(networkUuid, variantNum, switchId);
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        delegate.updateSwitches(networkUuid, switchResources);
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        delegate.removeSwitches(networkUuid, variantNum, switchesId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        delegate.createBusbarSections(networkUuid, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return delegate.getBusbarSections(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return delegate.getBusbarSection(networkUuid, variantNum, busbarSectionId);
    }

    @Override
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionsResource) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        delegate.updateBusbarSections(networkUuid, busbarSectionsResource);
    }

    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        ensureCached(ResourceType.LOAD, networkUuid);
        delegate.createLoads(networkUuid, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return delegate.getLoads(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return delegate.getLoad(networkUuid, variantNum, loadId);
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        ensureCached(ResourceType.LOAD, networkUuid);
        delegate.updateLoads(networkUuid, loadResources);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        delegate.createGenerators(networkUuid, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return delegate.getGenerators(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return delegate.getGenerator(networkUuid, variantNum, generatorId);
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        delegate.updateGenerators(networkUuid, generatorResources);
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        delegate.createBatteries(networkUuid, batteryResources);
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        return delegate.getBatteries(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        return delegate.getBattery(networkUuid, variantNum, batteryId);
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        delegate.updateBatteries(networkUuid, batteryResources);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        delegate.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getTwoWindingsTransformers(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getTwoWindingsTransformer(networkUuid, variantNum, twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        delegate.updateTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        delegate.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getThreeWindingsTransformers(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getThreeWindingsTransformer(networkUuid, variantNum, threeWindingsTransformerId);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        delegate.updateThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        ensureCached(ResourceType.LINE, networkUuid);
        delegate.createLines(networkUuid, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LINE, networkUuid);
        return delegate.getLines(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return delegate.getLine(networkUuid, variantNum, lineId);
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        ensureCached(ResourceType.LINE, networkUuid);
        delegate.updateLines(networkUuid, lineResources);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        delegate.createShuntCompensators(networkUuid, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return delegate.getShuntCompensators(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return delegate.getShuntCompensator(networkUuid, variantNum, shuntCompensatorId);
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        delegate.updateShuntCompensators(networkUuid, shuntCompensatorResources);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        delegate.createVscConverterStations(networkUuid, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return delegate.getVscConverterStations(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return delegate.getVscConverterStation(networkUuid, variantNum, vscConverterStationId);
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        delegate.updateVscConverterStations(networkUuid, vscConverterStationResources);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        delegate.createLccConverterStations(networkUuid, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return delegate.getLccConverterStations(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return delegate.getLccConverterStation(networkUuid, variantNum, lccConverterStationId);
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        delegate.updateLccConverterStations(networkUuid, lccConverterStationResources);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        delegate.createStaticVarCompensators(networkUuid, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return delegate.getStaticVarCompensators(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return delegate.getStaticVarCompensator(networkUuid, variantNum, staticVarCompensatorId);
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        delegate.updateStaticVarCompensators(networkUuid, staticVarCompensatorResources);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        delegate.createHvdcLines(networkUuid, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return delegate.getHvdcLines(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return delegate.getHvdcLine(networkUuid, variantNum, hvdcLineId);
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        delegate.removeHvdcLines(networkUuid, variantNum, hvdcLinesId);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        delegate.updateHvdcLines(networkUuid, hvdcLineResources);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        delegate.createDanglingLines(networkUuid, danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return delegate.getDanglingLines(networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return delegate.getDanglingLine(networkUuid, variantNum, danglingLineId);
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        delegate.updateDanglingLines(networkUuid, danglingLineResources);
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        delegate.removeDanglingLines(networkUuid, variantNum, danglingLinesId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        delegate.createConfiguredBuses(networkUuid, busesResources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return delegate.getConfiguredBuses(networkUuid, variantNum);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return delegate.getVoltageLevelConfiguredBuses(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return delegate.getConfiguredBus(networkUuid, variantNum, busId);
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        delegate.updateConfiguredBuses(networkUuid, busesResources);
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> configuredBusesId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        delegate.removeConfiguredBuses(networkUuid, variantNum, configuredBusesId);
    }

}
