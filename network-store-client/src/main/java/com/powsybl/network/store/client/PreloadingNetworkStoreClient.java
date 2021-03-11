/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.AbstractForwardingNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class PreloadingNetworkStoreClient extends AbstractForwardingNetworkStoreClient implements NetworkStoreClient {

    private final Map<UUID, Set<ResourceType>> cachedResourceTypes = new HashMap<>();

    public PreloadingNetworkStoreClient(NetworkStoreClient delegate) {
        super(new CachedNetworkStoreClient(delegate));
    }

    private void loadToCache(ResourceType resourceType, UUID networkUuid) {
        switch (resourceType) {
            case NETWORK:
                if (networkUuid == null) {
                    delegate.getNetworks();
                } else {
                    delegate.getNetwork(networkUuid); // we only need to load the network with the specified UUID
                }
                break;
            case SUBSTATION:
                delegate.getSubstations(networkUuid);
                break;
            case VOLTAGE_LEVEL:
                delegate.getVoltageLevels(networkUuid);
                break;
            case LOAD:
                delegate.getLoads(networkUuid);
                break;
            case GENERATOR:
                delegate.getGenerators(networkUuid);
                break;
            case BATTERY:
                delegate.getBatteries(networkUuid);
                break;
            case SHUNT_COMPENSATOR:
                delegate.getShuntCompensators(networkUuid);
                break;
            case VSC_CONVERTER_STATION:
                delegate.getVscConverterStations(networkUuid);
                break;
            case LCC_CONVERTER_STATION:
                delegate.getLccConverterStations(networkUuid);
                break;
            case STATIC_VAR_COMPENSATOR:
                delegate.getStaticVarCompensators(networkUuid);
                break;
            case BUSBAR_SECTION:
                delegate.getBusbarSections(networkUuid);
                break;
            case SWITCH:
                delegate.getSwitches(networkUuid);
                break;
            case TWO_WINDINGS_TRANSFORMER:
                delegate.getTwoWindingsTransformers(networkUuid);
                break;
            case THREE_WINDINGS_TRANSFORMER:
                delegate.getThreeWindingsTransformers(networkUuid);
                break;
            case LINE:
                delegate.getLines(networkUuid);
                break;
            case HVDC_LINE:
                delegate.getHvdcLines(networkUuid);
                break;
            case DANGLING_LINE:
                delegate.getDanglingLines(networkUuid);
                break;
            case CONFIGURED_BUS:
                delegate.getConfiguredBuses(networkUuid);
                break;
        }
    }

    private void ensureCached(ResourceType resourceType, UUID networkUuid) {
        Objects.requireNonNull(resourceType);
        if (resourceType != ResourceType.NETWORK) {
            Objects.requireNonNull(networkUuid);
        }
        Set<ResourceType> resourceTypes = cachedResourceTypes.computeIfAbsent(networkUuid, k -> EnumSet.noneOf(ResourceType.class));
        if (!resourceTypes.contains(resourceType)) {
            loadToCache(resourceType, networkUuid);
            resourceTypes.add(resourceType);
        }
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        ensureCached(ResourceType.NETWORK, null);
        return delegate.getNetworks();
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        ensureCached(ResourceType.NETWORK, null);
        delegate.createNetworks(networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        ensureCached(ResourceType.NETWORK, networkUuid);
        return delegate.getNetwork(networkUuid);
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
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return delegate.getSubstations(networkUuid);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return delegate.getSubstation(networkUuid, substationId);
    }

    @Override
    public void removeSubstations(UUID networkUuid, List<String> substationsId) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        delegate.removeSubstations(networkUuid, substationsId);
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
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return delegate.getVoltageLevel(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return delegate.getVoltageLevels(networkUuid);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return delegate.getVoltageLevelsInSubstation(networkUuid, substationId);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, List<String> voltageLevelsId) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        delegate.removeVoltageLevels(networkUuid, voltageLevelsId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return delegate.getVoltageLevelBusbarSections(networkUuid, voltageLevelId);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, List<String> busBarSectionsId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        delegate.removeBusBarSections(networkUuid, busBarSectionsId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return delegate.getVoltageLevelSwitches(networkUuid, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return delegate.getVoltageLevelGenerators(networkUuid, voltageLevelId);
    }

    @Override
    public void removeGenerators(UUID networkUuid, List<String> generatorsId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        delegate.removeGenerators(networkUuid, generatorsId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        return delegate.getVoltageLevelBatteries(networkUuid, voltageLevelId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, List<String> batteriesId) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        delegate.removeBatteries(networkUuid, batteriesId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return delegate.getVoltageLevelLoads(networkUuid, voltageLevelId);
    }

    @Override
    public void removeLoads(UUID networkUuid, List<String> loadsId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        delegate.removeLoads(networkUuid, loadsId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return delegate.getVoltageLevelShuntCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, List<String> shuntCompensatorsId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        delegate.removeShuntCompensators(networkUuid, shuntCompensatorsId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return delegate.getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, List<String> staticVarCompensatorsId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        delegate.removeStaticVarCompensators(networkUuid, staticVarCompensatorsId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return delegate.getVoltageLevelVscConverterStations(networkUuid, voltageLevelId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, List<String> vscConverterStationsId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        delegate.removeVscConverterStations(networkUuid, vscConverterStationsId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return delegate.getVoltageLevelLccConverterStations(networkUuid, voltageLevelId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, List<String> lccConverterStationsId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        delegate.removeLccConverterStations(networkUuid, lccConverterStationsId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, List<String> twoWindingsTransformersId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        delegate.removeTwoWindingsTransformers(networkUuid, twoWindingsTransformersId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, List<String> threeWindingsTransformersId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        delegate.removeThreeWindingsTransformers(networkUuid, threeWindingsTransformersId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return delegate.getVoltageLevelLines(networkUuid, voltageLevelId);
    }

    @Override
    public void removeLines(UUID networkUuid, List<String> linesId) {
        ensureCached(ResourceType.LINE, networkUuid);
        delegate.removeLines(networkUuid, linesId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return delegate.getVoltageLevelDanglingLines(networkUuid, voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        delegate.createSwitches(networkUuid, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return delegate.getSwitches(networkUuid);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return delegate.getSwitch(networkUuid, switchId);
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        delegate.updateSwitches(networkUuid, switchResources);
    }

    @Override
    public void removeSwitches(UUID networkUuid, List<String> switchesId) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        delegate.removeSwitches(networkUuid, switchesId);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        delegate.createBusbarSections(networkUuid, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return delegate.getBusbarSections(networkUuid);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return delegate.getBusbarSection(networkUuid, busbarSectionId);
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
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return delegate.getLoads(networkUuid);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return delegate.getLoad(networkUuid, loadId);
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
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return delegate.getGenerators(networkUuid);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return delegate.getGenerator(networkUuid, generatorId);
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
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        return delegate.getBatteries(networkUuid);
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, String batteryId) {
        ensureCached(ResourceType.BATTERY, networkUuid);
        return delegate.getBattery(networkUuid, batteryId);
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
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getTwoWindingsTransformers(networkUuid);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getTwoWindingsTransformer(networkUuid, twoWindingsTransformerId);
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
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getThreeWindingsTransformers(networkUuid);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getThreeWindingsTransformer(networkUuid, threeWindingsTransformerId);
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
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        ensureCached(ResourceType.LINE, networkUuid);
        return delegate.getLines(networkUuid);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return delegate.getLine(networkUuid, lineId);
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
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return delegate.getShuntCompensators(networkUuid);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return delegate.getShuntCompensator(networkUuid, shuntCompensatorId);
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
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return delegate.getVscConverterStations(networkUuid);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return delegate.getVscConverterStation(networkUuid, vscConverterStationId);
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
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return delegate.getLccConverterStations(networkUuid);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return delegate.getLccConverterStation(networkUuid, lccConverterStationId);
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
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return delegate.getStaticVarCompensators(networkUuid);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return delegate.getStaticVarCompensator(networkUuid, staticVarCompensatorId);
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
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return delegate.getHvdcLines(networkUuid);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return delegate.getHvdcLine(networkUuid, hvdcLineId);
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, List<String> hvdcLinesId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        delegate.removeHvdcLines(networkUuid, hvdcLinesId);
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
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return delegate.getDanglingLines(networkUuid);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return delegate.getDanglingLine(networkUuid, danglingLineId);
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        delegate.updateDanglingLines(networkUuid, danglingLineResources);
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, List<String> danglingLinesId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        delegate.removeDanglingLines(networkUuid, danglingLinesId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        delegate.createConfiguredBuses(networkUuid, busesResources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return delegate.getConfiguredBuses(networkUuid);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return delegate.getVoltageLevelConfiguredBuses(networkUuid, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        return delegate.getConfiguredBus(networkUuid, busId);
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        delegate.updateConfiguredBuses(networkUuid, busesResources);
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, List<String> configuredBusesId) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        delegate.removeConfiguredBuses(networkUuid, configuredBusesId);
    }

}
