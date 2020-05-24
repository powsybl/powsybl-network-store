/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class CachedRestNetworkStoreClient extends ForwardingNetworkStoreClient implements NetworkStoreClient {

    public CachedRestNetworkStoreClient(NetworkStoreClient delegate) {
        super(delegate);
    }

    private final NetworkCacheHandler cacheHandler = new NetworkCacheHandler();

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        super.createNetworks(networkResources);
        networkResources.forEach(resource -> cacheHandler.getNetworkCache(resource.getAttributes().getUuid()).setNetworkResource(resource));
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getNetworkResource(() -> delegate.getNetwork(networkUuid));
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        super.deleteNetwork(networkUuid);
        cacheHandler.invalidateNetworkCache(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        super.createSubstations(networkUuid, substationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SUBSTATION, substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SUBSTATION, () -> delegate.getSubstations(networkUuid));
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SUBSTATION, substationId, id ->  delegate.getSubstation(networkUuid, id));
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        super.createVoltageLevels(networkUuid, voltageLevelResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.VOLTAGE_LEVEL, voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.VOLTAGE_LEVEL, voltageLevelId, id ->  super.getVoltageLevel(networkUuid, id));
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.VOLTAGE_LEVEL, () -> delegate.getVoltageLevels(networkUuid));    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.VOLTAGE_LEVEL, substationId, id -> delegate.getVoltageLevelsInSubstation(networkUuid, id));
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.BUSBAR_SECTION, voltageLevelId, id -> delegate.getVoltageLevelBusbarSections(networkUuid, id));
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.SWITCH, voltageLevelId, id -> delegate.getVoltageLevelSwitches(networkUuid, id));
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.GENERATOR, voltageLevelId, id -> delegate.getVoltageLevelGenerators(networkUuid, id));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LOAD, voltageLevelId, id -> delegate.getVoltageLevelLoads(networkUuid, id));
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.SHUNT_COMPENSATOR, voltageLevelId, id -> delegate.getVoltageLevelShuntCompensators(networkUuid, id));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.STATIC_VAR_COMPENSATOR, voltageLevelId, id -> delegate.getVoltageLevelStaticVarCompensators(networkUuid, id));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.VSC_CONVERTER_STATION, voltageLevelId, id -> delegate.getVoltageLevelVscConverterStation(networkUuid, id));
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LCC_CONVERTER_STATION, voltageLevelId, id -> delegate.getVoltageLevelLccConverterStation(networkUuid, id));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.TWO_WINDINGS_TRANSFORMER, voltageLevelId, id -> delegate.getVoltageLevelTwoWindingsTransformers(networkUuid, id));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.THREE_WINDINGS_TRANSFORMER, voltageLevelId, id -> delegate.getVoltageLevelThreeWindingsTransformers(networkUuid, id));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.LINE, voltageLevelId, id -> delegate.getVoltageLevelLines(networkUuid, id));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.DANGLING_LINE, voltageLevelId, id -> delegate.getVoltageLevelDanglingLines(networkUuid, id));
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        super.createSwitches(networkUuid, switchResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SWITCH, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SWITCH, () -> delegate.getSwitches(networkUuid));
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SWITCH, switchId, id ->  delegate.getSwitch(networkUuid, id));
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        super.updateSwitch(networkUuid, switchResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.SWITCH, switchResource);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        super.createBusbarSections(networkUuid, busbarSectionResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.BUSBAR_SECTION, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.BUSBAR_SECTION, () -> delegate.getBusbarSections(networkUuid));
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.BUSBAR_SECTION, busbarSectionId, id ->  delegate.getBusbarSection(networkUuid, id));
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        super.createLoads(networkUuid, loadResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LOAD, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LOAD, () -> delegate.getLoads(networkUuid));
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LOAD, loadId, id ->  delegate.getLoad(networkUuid, id));
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        super.updateLoad(networkUuid, loadResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.LOAD, loadResource);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        super.createGenerators(networkUuid, generatorResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.GENERATOR, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.GENERATOR, () -> delegate.getGenerators(networkUuid));
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.GENERATOR, generatorId, id ->  delegate.getGenerator(networkUuid, id));
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        super.updateGenerator(networkUuid, generatorResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.GENERATOR, generatorResource);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        super.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.TWO_WINDINGS_TRANSFORMER, () -> delegate.getTwoWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerId, id ->  delegate.getTwoWindingsTransformer(networkUuid, id));
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        super.updateTwoWindingsTransformer(networkUuid, twoWindingsTransformerResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerResource);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        super.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.THREE_WINDINGS_TRANSFORMER, () -> delegate.getThreeWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerId, id ->  delegate.getThreeWindingsTransformer(networkUuid, id));
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        super.updateThreeWindingsTransformer(networkUuid, threeWindingsTransformerResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerResource);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        super.createLines(networkUuid, lineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LINE, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LINE, () -> delegate.getLines(networkUuid));
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LINE, lineId, id ->  delegate.getLine(networkUuid, id));
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        super.updateLine(networkUuid, lineResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.LINE, lineResource);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        super.createShuntCompensators(networkUuid, shuntCompensatorResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.SHUNT_COMPENSATOR, () -> delegate.getShuntCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorId, id ->  delegate.getShuntCompensator(networkUuid, id));
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        super.updateShuntCompensator(networkUuid, shuntCompensatorResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorResource);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        super.createVscConverterStations(networkUuid, vscConverterStationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.VSC_CONVERTER_STATION, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.VSC_CONVERTER_STATION, () -> delegate.getVscConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.VSC_CONVERTER_STATION, vscConverterStationId, id ->  delegate.getVscConverterStation(networkUuid, id));
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        super.updateVscConverterStation(networkUuid, vscConverterStationResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.VSC_CONVERTER_STATION, vscConverterStationResource);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        super.createLccConverterStations(networkUuid, lccConverterStationResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.LCC_CONVERTER_STATION, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.LCC_CONVERTER_STATION, () -> delegate.getLccConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.LCC_CONVERTER_STATION, lccConverterStationId, id ->  delegate.getLccConverterStation(networkUuid, id));
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        super.updateLccConverterStation(networkUuid, lccConverterStationResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.LCC_CONVERTER_STATION, lccConverterStationResource);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        super.createStaticVarCompensators(networkUuid, svcResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.STATIC_VAR_COMPENSATOR, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.STATIC_VAR_COMPENSATOR, () -> delegate.getStaticVarCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorId, id ->  delegate.getStaticVarCompensator(networkUuid, id));
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        super.updateStaticVarCompensator(networkUuid, staticVarCompensatorResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorResource);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        super.createHvdcLines(networkUuid, hvdcLineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.HVDC_LINE, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.HVDC_LINE, () -> delegate.getHvdcLines(networkUuid));
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.HVDC_LINE, hvdcLineId, id ->  delegate.getHvdcLine(networkUuid, id));
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        super.updateHvdcLine(networkUuid, hvdcLineResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.HVDC_LINE, hvdcLineResource);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        super.createDanglingLines(networkUuid, danglingLineResources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.DANGLING_LINE, danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.DANGLING_LINE, () -> delegate.getDanglingLines(networkUuid));
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.DANGLING_LINE, danglingLineId, id ->  delegate.getDanglingLine(networkUuid, id));
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        super.updateDanglingLine(networkUuid, danglingLineResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.DANGLING_LINE, danglingLineResource);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        super.removeDanglingLine(networkUuid, danglingLineId);
        cacheHandler.getNetworkCache(networkUuid).removeResource(ResourceType.DANGLING_LINE, danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        super.createConfiguredBuses(networkUuid, busesRessources);
        cacheHandler.getNetworkCache(networkUuid).addResources(ResourceType.CONFIGURED_BUS, busesRessources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return cacheHandler.getNetworkCache(networkUuid).getAllResources(ResourceType.CONFIGURED_BUS, () -> delegate.getConfiguredBuses(networkUuid));
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return cacheHandler.getNetworkCache(networkUuid).getResourcesByContainerId(ResourceType.CONFIGURED_BUS, voltageLevelId, id -> delegate.getVoltageLevelConfiguredBuses(networkUuid, id));
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return cacheHandler.getNetworkCache(networkUuid).getResource(ResourceType.CONFIGURED_BUS, busId, id ->  delegate.getConfiguredBus(networkUuid, id));
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        super.updateConfiguredBus(networkUuid, busesResource);
        cacheHandler.getNetworkCache(networkUuid).addResource(ResourceType.CONFIGURED_BUS, busesResource);
    }
}
