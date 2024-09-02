/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@Setter
class MockNetworkStoreClient implements NetworkStoreClient {
    private final ActivePowerControlAttributes apc1;
    private final ActivePowerControlAttributes apc2;
    private final OperatingStatusAttributes os1;
    private boolean extensionAttributeLoaderCalled = false;
    private boolean extensionAttributesLoaderByResourceTypeAndNameCalled = false;
    private boolean extensionAttributesLoaderByIdCalled = false;
    private boolean extensionAttributesLoaderByResourceTypeCalled = false;

    public MockNetworkStoreClient(ActivePowerControlAttributes apc1, ActivePowerControlAttributes apc2, OperatingStatusAttributes os1) {
        this.apc1 = apc1;
        this.apc2 = apc2;
        this.os1 = os1;
    }

    // Methods used in tests
    @Override
    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        extensionAttributeLoaderCalled = true;
        if (identifiableId.equals("l1") && extensionName.equals("operatingStatus")) {
            return Optional.of(os1);
        } else if (identifiableId.equals("l1") && extensionName.equals("activePowerControl")) {
            return Optional.of(apc1);
        } else if (identifiableId.equals("l2") && extensionName.equals("activePowerControl")) {
            return Optional.of(apc2);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByResourceTypeAndExtensionName(UUID networkUuid, int variantNum, ResourceType resourceType, String extensionName) {
        extensionAttributesLoaderByResourceTypeAndNameCalled = true;
        if (extensionName.equals("activePowerControl")) {
            return Map.of("l1", apc1, "l2", apc2);
        }
        return Map.of();
    }

    @Override
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId) {
        extensionAttributesLoaderByIdCalled = true;
        if (identifiableId.equals("l1")) {
            return Map.of("activePowerControl", apc1, "operatingStatus", os1);
        } else if (identifiableId.equals("l2")) {
            return Map.of("activePowerControl", apc2);
        }
        return Map.of();
    }

    @Override
    public Map<String, Map<String, ExtensionAttributes>> getAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType) {
        extensionAttributesLoaderByResourceTypeCalled = true;
        if (resourceType == ResourceType.LOAD) {
            return Map.of("l1", Map.of("activePowerControl", apc1, "operatingStatus", os1), "l2", Map.of("activePowerControl", apc2));
        }
        return Map.of();
    }

    // Methods below are not used in tests
    @Override
    public void removeExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<NetworkInfos> getNetworksInfos() {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<VariantInfos> getVariantsInfos(UUID networkUuid) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateNetworks(List<Resource<NetworkAttributes>> networkResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void cloneNetwork(UUID networkUuid, int sourceVariantNum, int targetVariantNum, String targetVariantId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void cloneNetwork(UUID networkUuid, String sourceVariantId, String targetVariantId, boolean mayOverwrite) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void cloneNetwork(UUID networkUuid, UUID sourceNetworkUuid, List<String> targetVariantIds) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<GroundAttributes>> getVoltageLevelGrounds(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesIds) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<GroundAttributes>> getGrounds(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<GroundAttributes>> getGround(UUID networkUuid, int variantNum, String groundId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeGrounds(UUID networkUuid, int variantNum, List<String> groundsId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void createTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<Resource<TieLineAttributes>> getTieLines(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<TieLineAttributes>> getTieLine(UUID networkUuid, int variantNum, String tieLineId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void removeTieLines(UUID networkUuid, int variantNum, List<String> tieLinesId) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void updateTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources, AttributeFilter attributeFilter) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Optional<Resource<IdentifiableAttributes>> getIdentifiable(UUID networkUuid, int variantNum, String id) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public List<String> getIdentifiablesIds(UUID networkUuid, int variantNum) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public void flush(UUID networkUuid) {
        throw new UnsupportedOperationException("Unimplemented method");
    }
}
