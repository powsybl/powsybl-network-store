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

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class PreloadingNetworkStoreClient extends ForwardingNetworkStoreClient implements NetworkStoreClient {

    private final Map<UUID, Set<ResourceType>> cachedResourceTypes = new HashMap<>();

    public PreloadingNetworkStoreClient(NetworkStoreClient delegate) {
        super(new CachedNetworkStoreClient(delegate));
    }

    private void loadToCache(ResourceType resourceType, UUID networkUuid) {
        switch (resourceType) {
            case NETWORK:
                delegate.getNetworks();
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
        ensureCached(ResourceType.NETWORK, null);
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
    public int getSubstationCount(UUID networkUuid) {
        ensureCached(ResourceType.SUBSTATION, networkUuid);
        return delegate.getSubstationCount(networkUuid);
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
    public int getVoltageLevelCount(UUID networkUuid) {
        ensureCached(ResourceType.VOLTAGE_LEVEL, networkUuid);
        return delegate.getVoltageLevelCount(networkUuid);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return delegate.getVoltageLevelBusbarSections(networkUuid, voltageLevelId);
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, String busBarSectionId) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        delegate.removeBusBarSection(networkUuid, busBarSectionId);
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
    public void removeGenerator(UUID networkUuid, String generatorId) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        delegate.removeGenerator(networkUuid, generatorId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return delegate.getVoltageLevelLoads(networkUuid, voltageLevelId);
    }

    @Override
    public void removeLoad(UUID networkUuid, String loadId) {
        ensureCached(ResourceType.LOAD, networkUuid);
        delegate.removeLoad(networkUuid, loadId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return delegate.getVoltageLevelShuntCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public void removeShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        delegate.removeShuntCompensator(networkUuid, shuntCompensatorId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return delegate.getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        delegate.removeStaticVarCompensator(networkUuid, staticVarCompensatorId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return delegate.getVoltageLevelVscConverterStations(networkUuid, voltageLevelId);
    }

    @Override
    public void removeVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        delegate.removeVscConverterStation(networkUuid, vscConverterStationId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return delegate.getVoltageLevelLccConverterStations(networkUuid, voltageLevelId);
    }

    @Override
    public void removeLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        delegate.removeLccConverterStation(networkUuid, lccConverterStationId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        delegate.removeTwoWindingsTransformer(networkUuid, twoWindingsTransformerId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        delegate.removeThreeWindingsTransformer(networkUuid, threeWindingsTransformerId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        ensureCached(ResourceType.LINE, networkUuid);
        return delegate.getVoltageLevelLines(networkUuid, voltageLevelId);
    }

    @Override
    public void removeLine(UUID networkUuid, String lineId) {
        ensureCached(ResourceType.LINE, networkUuid);
        delegate.removeLine(networkUuid, lineId);
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
    public int getSwitchCount(UUID networkUuid) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        return delegate.getSwitchCount(networkUuid);
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        ensureCached(ResourceType.SWITCH, networkUuid);
        delegate.updateSwitch(networkUuid, switchResource);
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
    public int getBusbarSectionCount(UUID networkUuid) {
        ensureCached(ResourceType.BUSBAR_SECTION, networkUuid);
        return delegate.getBusbarSectionCount(networkUuid);
    }

    @Override
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
    public int getLoadCount(UUID networkUuid) {
        ensureCached(ResourceType.LOAD, networkUuid);
        return delegate.getLoadCount(networkUuid);
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        ensureCached(ResourceType.LOAD, networkUuid);
        delegate.updateLoad(networkUuid, loadResource);
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
    public int getGeneratorCount(UUID networkUuid) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        return delegate.getGeneratorCount(networkUuid);
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        ensureCached(ResourceType.GENERATOR, networkUuid);
        delegate.updateGenerator(networkUuid, generatorResource);
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
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        delegate.updateTwoWindingsTransformer(networkUuid, twoWindingsTransformerResource);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        ensureCached(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getTwoWindingsTransformerCount(networkUuid);
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
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        delegate.updateThreeWindingsTransformer(networkUuid, threeWindingsTransformerResource);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        ensureCached(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid);
        return delegate.getThreeWindingsTransformerCount(networkUuid);
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
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        ensureCached(ResourceType.LINE, networkUuid);
        delegate.updateLine(networkUuid, lineResource);
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        ensureCached(ResourceType.LINE, networkUuid);
        return delegate.getLineCount(networkUuid);
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
    public int getShuntCompensatorCount(UUID networkUuid) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        return delegate.getShuntCompensatorCount(networkUuid);
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        ensureCached(ResourceType.SHUNT_COMPENSATOR, networkUuid);
        delegate.updateShuntCompensator(networkUuid, shuntCompensatorResource);
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
    public int getVscConverterStationCount(UUID networkUuid) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        return delegate.getVscConverterStationCount(networkUuid);
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        ensureCached(ResourceType.VSC_CONVERTER_STATION, networkUuid);
        delegate.updateVscConverterStation(networkUuid, vscConverterStationResource);
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
    public int getLccConverterStationCount(UUID networkUuid) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        return delegate.getLccConverterStationCount(networkUuid);
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        ensureCached(ResourceType.LCC_CONVERTER_STATION, networkUuid);
        delegate.updateLccConverterStation(networkUuid, lccConverterStationResource);
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
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        return delegate.getStaticVarCompensatorCount(networkUuid);
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        ensureCached(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid);
        delegate.updateStaticVarCompensator(networkUuid, staticVarCompensatorResource);
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
    public void removeHvdcLine(UUID networkUuid, String hvdcLineId) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        delegate.removeHvdcLine(networkUuid, hvdcLineId);
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        return delegate.getHvdcLineCount(networkUuid);
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        ensureCached(ResourceType.HVDC_LINE, networkUuid);
        delegate.updateHvdcLine(networkUuid, hvdcLineResource);
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
    public int getDanglingLineCount(UUID networkUuid) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        return delegate.getDanglingLineCount(networkUuid);
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        delegate.updateDanglingLine(networkUuid, danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        ensureCached(ResourceType.DANGLING_LINE, networkUuid);
        delegate.removeDanglingLine(networkUuid, danglingLineId);
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
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        ensureCached(ResourceType.CONFIGURED_BUS, networkUuid);
        delegate.updateConfiguredBus(networkUuid, busesResource);
    }
}
