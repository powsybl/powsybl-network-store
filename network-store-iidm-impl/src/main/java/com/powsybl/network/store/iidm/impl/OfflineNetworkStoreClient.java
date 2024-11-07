/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class OfflineNetworkStoreClient implements NetworkStoreClient {

    @Override
    public List<NetworkInfos> getNetworksInfos() {
        return Collections.emptyList();
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        // nothing to do
    }

    @Override
    public List<VariantInfos> getVariantsInfos(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum) {
        return Optional.empty();
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        // nothing to do
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        // nothing to do
    }

    @Override
    public void updateNetworks(List<Resource<NetworkAttributes>> networkResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void cloneNetwork(UUID networkUuid, int sourceVariantNum, int targetVariantNum, String targetVariantId, VariantMode variantMode) {
        // nothing to do
    }

    @Override
    public void cloneNetwork(UUID networkUuid, String sourceVariantId, String targetVariantId, boolean mayOverwrite, VariantMode variantMode) {
        // nothing to do
    }

    @Override
    public void cloneNetwork(UUID networkUuid, UUID sourceNetworkUuid, List<String> targetVariantIds) {
        // nothing to do
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        // nothing to do
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return Optional.empty();
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        // nothing to do
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        // nothing to do
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Optional.empty();
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        return Collections.emptyList();
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        // nothing to do
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        // nothing to do
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        // nothing to do
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        // nothing to do
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        // nothing to do
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        // nothing to do
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        // nothing to do
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        // nothing to do
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        // nothing to do
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        // nothing to do
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        // nothing to do
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        // nothing to do
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return Optional.empty();
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        // nothing to do
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        // nothing to do
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return Optional.empty();
    }

    @Override
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        // nothing to do
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        // nothing to do
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return Optional.empty();
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        // nothing to do
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return Optional.empty();
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        // nothing to do
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return Optional.empty();
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        // nothing to do
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return Optional.empty();
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        // nothing to do
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        return Optional.empty();
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        // nothing to do
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return Optional.empty();
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        // nothing to do
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return Optional.empty();
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        // nothing to do
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return Optional.empty();
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        // nothing to do
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return Optional.empty();
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        // nothing to do
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return Optional.empty();
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        // nothing to do
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return Optional.empty();
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        // nothing to do
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        // nothing to do
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return Optional.empty();
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        // nothing to do
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        // nothing to do
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return Optional.empty();
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        // nothing to do
    }

    @Override
    public void createTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources) {
        // nothing to do
    }

    @Override
    public List<Resource<TieLineAttributes>> getTieLines(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<TieLineAttributes>> getTieLine(UUID networkUuid, int variantNum, String tieLineId) {
        return Optional.empty();
    }

    @Override
    public void removeTieLines(UUID networkUuid, int variantNum, List<String> tieLinesId) {
        // nothing to do
    }

    @Override
    public void updateTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources, AttributeFilter attributeFilter) {
        // nothing to do
    }

    @Override
    public Optional<Resource<IdentifiableAttributes>> getIdentifiable(UUID networkUuid, int variantNum, String id) {
        return Optional.empty();
    }

    @Override
    public List<String> getIdentifiablesIds(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public void flush(UUID networkUuid) {
        // nothing to do
    }

    @Override
    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        return Optional.empty();
    }

    @Override
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByResourceTypeAndExtensionName(UUID uuid, int variantNum, ResourceType resourceType, String extensionName) {
        return Map.of();
    }

    @Override
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId) {
        return Map.of();
    }

    @Override
    public Map<String, Map<String, ExtensionAttributes>> getAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType) {
        return Map.of();
    }

    @Override
    public void removeExtensionAttributes(UUID uuid, int workingVariantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        // nothing to do
    }

    @Override
    public List<Resource<GroundAttributes>> getVoltageLevelGrounds(UUID networkUuid, int variantNum,
            String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void createGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources) {
        // nothing to do
    }

    @Override
    public List<Resource<GroundAttributes>> getGrounds(UUID networkUuid, int variantNum) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<GroundAttributes>> getGround(UUID networkUuid, int variantNum, String groundId) {
        return Optional.empty();
    }

    @Override
    public void removeGrounds(UUID networkUuid, int variantNum, List<String> groundsId) {
        // nothing to do
    }

    @Override
    public void updateGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources,
            AttributeFilter attributeFilter) {
        // nothing to do
    }
}
