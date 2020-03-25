package com.powsybl.network.store.client;

import com.powsybl.network.store.model.*;

import java.util.*;

public class LazyLoadingRestNetworkStoreClient implements NetworkStoreClient {

    private final BufferedRestNetworkStoreClient bufferedRestNetworkStoreClient;

    public LazyLoadingRestNetworkStoreClient(BufferedRestNetworkStoreClient bufferedRestNetworkStoreClient) {
        this.bufferedRestNetworkStoreClient = Objects.requireNonNull(bufferedRestNetworkStoreClient);
    }

    private final Map<UUID, NetworkCache> networksCache = new HashMap<>();

    public void invalidateNetworkCache(UUID networkUuid) {
        if (networksCache.containsKey(networkUuid)) {
            networksCache.get(networkUuid).invalidate();
        }
        networksCache.remove(networkUuid);
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        return bufferedRestNetworkStoreClient.getNetworks();
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        bufferedRestNetworkStoreClient.createNetworks(networkResources);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getNetwork(networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        bufferedRestNetworkStoreClient.deleteNetwork(networkUuid);
        invalidateNetworkCache(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        bufferedRestNetworkStoreClient.createSubstations(networkUuid, substationResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.SUBSTATION, substationResources);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.SUBSTATION, () -> bufferedRestNetworkStoreClient.getSubstations(networkUuid));
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.SUBSTATION, substationId, id ->  bufferedRestNetworkStoreClient.getSubstation(networkUuid, id));
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getSubstationCount(networkUuid);
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        bufferedRestNetworkStoreClient.createVoltageLevels(networkUuid, voltageLevelResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.VOLTAGE_LEVEL, voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.VOLTAGE_LEVEL, voltageLevelId, id ->  bufferedRestNetworkStoreClient.getVoltageLevel(networkUuid, id));
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.VOLTAGE_LEVEL, () -> bufferedRestNetworkStoreClient.getVoltageLevels(networkUuid));    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return bufferedRestNetworkStoreClient.getVoltageLevelsInSubstation(networkUuid, substationId);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getVoltageLevelCount(networkUuid);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.BUSBAR_SECTION, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelBusbarSections(networkUuid, id));
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.SWITCH, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelSwitches(networkUuid, id));
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.GENERATOR, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelGenerators(networkUuid, id));
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.LOAD, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelLoads(networkUuid, id));
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.SHUNT_COMPENSATOR, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelShuntCompensators(networkUuid, id));
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.STATIC_VAR_COMPENSATOR, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelStaticVarCompensators(networkUuid, id));
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.VSC_CONVERTER_STATION, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelVscConverterStation(networkUuid, id));
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.LCC_CONVERTER_STATION, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelLccConverterStation(networkUuid, id));
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.TWO_WINDINGS_TRANSFORMER, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelTwoWindingsTransformers(networkUuid, id));
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.THREE_WINDINGS_TRANSFORMER, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelThreeWindingsTransformers(networkUuid, id));
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.LINE, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelLines(networkUuid, id));
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResourcesByVoltageId(ResourceType.DANGLING_LINE, voltageLevelId, id -> bufferedRestNetworkStoreClient.getVoltageLevelDanglingLines(networkUuid, id));
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        bufferedRestNetworkStoreClient.createSwitches(networkUuid, switchResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.SWITCH, switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.SWITCH, () -> bufferedRestNetworkStoreClient.getSwitches(networkUuid));
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.SWITCH, switchId, id ->  bufferedRestNetworkStoreClient.getSwitch(networkUuid, id));
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getSwitchCount(networkUuid);
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        bufferedRestNetworkStoreClient.createBusbarSections(networkUuid, busbarSectionResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.BUSBAR_SECTION, busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.BUSBAR_SECTION, () -> bufferedRestNetworkStoreClient.getBusbarSections(networkUuid));
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.BUSBAR_SECTION, busbarSectionId, id ->  bufferedRestNetworkStoreClient.getBusbarSection(networkUuid, id));
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getBusbarSectionCount(networkUuid);
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        bufferedRestNetworkStoreClient.createLoads(networkUuid, loadResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.LOAD, loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.LOAD, () -> bufferedRestNetworkStoreClient.getLoads(networkUuid));
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.LOAD, loadId, id ->  bufferedRestNetworkStoreClient.getLoad(networkUuid, id));
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getLoadCount(networkUuid);
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        bufferedRestNetworkStoreClient.createGenerators(networkUuid, generatorResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.GENERATOR, generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.GENERATOR, () -> bufferedRestNetworkStoreClient.getGenerators(networkUuid));
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.GENERATOR, generatorId, id ->  bufferedRestNetworkStoreClient.getGenerator(networkUuid, id));
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getGeneratorCount(networkUuid);
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        bufferedRestNetworkStoreClient.createTwoWindingsTransformers(networkUuid, twoWindingsTransformerResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.TWO_WINDINGS_TRANSFORMER, () -> bufferedRestNetworkStoreClient.getTwoWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerId, id ->  bufferedRestNetworkStoreClient.getTwoWindingsTransformer(networkUuid, id));
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getTwoWindingsTransformerCount(networkUuid);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        bufferedRestNetworkStoreClient.createThreeWindingsTransformers(networkUuid, threeWindingsTransformerResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.THREE_WINDINGS_TRANSFORMER, () -> bufferedRestNetworkStoreClient.getThreeWindingsTransformers(networkUuid));
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerId, id ->  bufferedRestNetworkStoreClient.getThreeWindingsTransformer(networkUuid, id));
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getThreeWindingsTransformerCount(networkUuid);
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        bufferedRestNetworkStoreClient.createLines(networkUuid, lineResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.LINE, lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.LINE, () -> bufferedRestNetworkStoreClient.getLines(networkUuid));
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.LINE, lineId, id ->  bufferedRestNetworkStoreClient.getLine(networkUuid, id));
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getLineCount(networkUuid);
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        bufferedRestNetworkStoreClient.createShuntCompensators(networkUuid, shuntCompensatorResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.SHUNT_COMPENSATOR, () -> bufferedRestNetworkStoreClient.getShuntCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.SHUNT_COMPENSATOR, shuntCompensatorId, id ->  bufferedRestNetworkStoreClient.getShuntCompensator(networkUuid, id));
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getShuntCompensatorCount(networkUuid);
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        bufferedRestNetworkStoreClient.createVscConverterStations(networkUuid, vscConverterStationResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.VSC_CONVERTER_STATION, vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.VSC_CONVERTER_STATION, () -> bufferedRestNetworkStoreClient.getVscConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.VSC_CONVERTER_STATION, vscConverterStationId, id ->  bufferedRestNetworkStoreClient.getVscConverterStation(networkUuid, id));
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getVscConverterStationCount(networkUuid);
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        bufferedRestNetworkStoreClient.createLccConverterStations(networkUuid, lccConverterStationResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.LCC_CONVERTER_STATION, lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.LCC_CONVERTER_STATION, () -> bufferedRestNetworkStoreClient.getLccConverterStations(networkUuid));
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.LCC_CONVERTER_STATION, lccConverterStationId, id ->  bufferedRestNetworkStoreClient.getLccConverterStation(networkUuid, id));
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getLccConverterStationCount(networkUuid);
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        bufferedRestNetworkStoreClient.createStaticVarCompensators(networkUuid, svcResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.STATIC_VAR_COMPENSATOR, svcResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.STATIC_VAR_COMPENSATOR, () -> bufferedRestNetworkStoreClient.getStaticVarCompensators(networkUuid));
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.STATIC_VAR_COMPENSATOR, staticVarCompensatorId, id ->  bufferedRestNetworkStoreClient.getStaticVarCompensator(networkUuid, id));
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getStaticVarCompensatorCount(networkUuid);
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        bufferedRestNetworkStoreClient.createHvdcLines(networkUuid, hvdcLineResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.HVDC_LINE, hvdcLineResources);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.HVDC_LINE, () -> bufferedRestNetworkStoreClient.getHvdcLines(networkUuid));
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.HVDC_LINE, hvdcLineId, id ->  bufferedRestNetworkStoreClient.getHvdcLine(networkUuid, id));
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getHvdcLineCount(networkUuid);
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        bufferedRestNetworkStoreClient.createDanglingLines(networkUuid, danglingLineResources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.DANGLING_LINE, danglingLineResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.DANGLING_LINE, () -> bufferedRestNetworkStoreClient.getDanglingLines(networkUuid));
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.DANGLING_LINE, danglingLineId, id ->  bufferedRestNetworkStoreClient.getDanglingLine(networkUuid, id));
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return bufferedRestNetworkStoreClient.getDanglingLineCount(networkUuid);
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        bufferedRestNetworkStoreClient.createConfiguredBuses(networkUuid, busesRessources);
        networksCache.computeIfAbsent(networkUuid, NetworkCache::new).addResources(ResourceType.CONFIGURED_BUS, busesRessources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getAllResources(ResourceType.CONFIGURED_BUS, () -> bufferedRestNetworkStoreClient.getConfiguredBuses(networkUuid));
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return bufferedRestNetworkStoreClient.getVoltageLevelConfiguredBuses(networkUuid, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return networksCache.computeIfAbsent(networkUuid, NetworkCache::new).getResource(ResourceType.CONFIGURED_BUS, busId, id ->  bufferedRestNetworkStoreClient.getConfiguredBus(networkUuid, id));
    }

    @Override
    public void flush() {
        bufferedRestNetworkStoreClient.flush();
    }
}
