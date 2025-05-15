/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public interface NetworkStoreClient {

    // network
    List<NetworkInfos> getNetworksInfos();

    void createNetworks(List<Resource<NetworkAttributes>> networkResources);

    List<VariantInfos> getVariantsInfos(UUID networkUuid);

    Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum);

    void deleteNetwork(UUID networkUuid);

    void deleteNetwork(UUID networkUuid, int variantNum);

    void updateNetworks(List<Resource<NetworkAttributes>> networkResources, AttributeFilter attributeFilter);

    void cloneNetwork(UUID networkUuid, int sourceVariantNum, int targetVariantNum, String targetVariantId);

    void cloneNetwork(UUID networkUuid, String sourceVariantId, String targetVariantId, boolean mayOverwrite);

    void cloneNetwork(UUID networkUuid, UUID sourceNetworkUuid, List<String> targetVariantIds);

    // substation

    void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources);

    List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum);

    Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId);

    void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources, AttributeFilter attributeFilter);

    void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId);

    // voltage level

    void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources);

    Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum);

    List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId);

    void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources, AttributeFilter attributeFilter);

    void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId);

    List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<GroundAttributes>> getVoltageLevelGrounds(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId);

    // switch

    void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources);

    List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum);

    Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId);

    void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources, AttributeFilter attributeFilter);

    void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId);

    // busbar section

    void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources);

    List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum);

    Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId);

    void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources, AttributeFilter attributeFilter);

    void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId);

    // load

    void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources);

    List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum);

    Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId);

    void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources, AttributeFilter attributeFilter);

    void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId);

    // generator

    void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources);

    List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum);

    Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId);

    void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources, AttributeFilter attributeFilter);

    void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId);

    // battery

    void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources);

    List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum);

    Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId);

    void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources, AttributeFilter attributeFilter);

    void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesIds);

    // 2 windings transformer

    void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources);

    List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum);

    Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId);

    void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources, AttributeFilter attributeFilter);

    void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId);

    // 3 windings transformer

    void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources);

    List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum);

    Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId);

    void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources, AttributeFilter attributeFilter);

    void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId);

    // line

    void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources);

    List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum);

    Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId);

    void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources, AttributeFilter attributeFilter);

    void removeLines(UUID networkUuid, int variantNum, List<String> linesId);

    // shunt compensator

    void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources);

    List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum);

    Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId);

    void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources, AttributeFilter attributeFilter);

    void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId);

    // VSC converter station

    void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources);

    List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum);

    Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId);

    void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources, AttributeFilter attributeFilter);

    void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId);

    // LCC converter station

    void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources);

    List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum);

    Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId);

    void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources, AttributeFilter attributeFilter);

    void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId);

    // SVC

    void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources);

    List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum);

    Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId);

    void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources, AttributeFilter attributeFilter);

    void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId);

    // HVDC line

    void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources);

    List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum);

    Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId);

    void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId);

    void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources, AttributeFilter attributeFilter);

    // Dangling line

    void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources);

    List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum);

    Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId);

    void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId);

    void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources, AttributeFilter attributeFilter);

    // Ground

    void createGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources);

    List<Resource<GroundAttributes>> getGrounds(UUID networkUuid, int variantNum);

    Optional<Resource<GroundAttributes>> getGround(UUID networkUuid, int variantNum, String groundId);

    void removeGrounds(UUID networkUuid, int variantNum, List<String> groundsId);

    void updateGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources, AttributeFilter attributeFilter);

    // Area

    void createAreas(UUID networkUuid, List<Resource<AreaAttributes>> areaResources);

    List<Resource<AreaAttributes>> getAreas(UUID networkUuid, int variantNum);

    Optional<Resource<AreaAttributes>> getArea(UUID networkUuid, int variantNum, String areaId);

    void removeAreas(UUID networkUuid, int variantNum, List<String> areasId);

    void updateAreas(UUID networkUuid, List<Resource<AreaAttributes>> areaResources, AttributeFilter attributeFilter);

    // Bus

    void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources);

    List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum);

    Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId);

    void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources, AttributeFilter attributeFilter);

    void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId);

    // Tie Lines

    void createTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources);

    List<Resource<TieLineAttributes>> getTieLines(UUID networkUuid, int variantNum);

    Optional<Resource<TieLineAttributes>> getTieLine(UUID networkUuid, int variantNum, String tieLineId);

    void removeTieLines(UUID networkUuid, int variantNum, List<String> tieLinesId);

    void updateTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources, AttributeFilter attributeFilter);

    // Extension Attributes

    /**
     * For one identifiable with a specific identifiable id, retrieves one extension attributes by its extension name.
     * @return {@link ExtensionAttributes} which is a subset of an identifiable resource. The extension attributes can be put in the
     * extensionAttributes map of an {@link IdentifiableAttributes} or used to load an extension.
     */
    Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName);

    /**
     * For all the identifiables of a specific resource type, retrieves one extension attributes by its extension name.
     * Used for preloading collection strategy.
     * @return A {@link Map} where keys are identifiable IDs and values are {@link ExtensionAttributes}.
     */
    Map<String, ExtensionAttributes> getAllExtensionsAttributesByResourceTypeAndExtensionName(UUID networkUuid, int variantNum, ResourceType resourceType, String extensionName);

    /**
     * For one identifiable with a specific identifiable id, retrieves all extension attributes of this identifiable.
     * @return A {@link Map} where keys are extension names and values are {@link ExtensionAttributes}.
     */
    Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId);

    /**
     * For all the identifiables of a specific resource type, retrieves all extension attributes of this identifiable.
     * Used for preloading collection strategy.
     * @return A {@link Map} where keys are identifiable IDs and values are {@link Map}s where keys are extension names and values are {@link ExtensionAttributes}.
     */
    Map<String, Map<String, ExtensionAttributes>> getAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType);

    void removeExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName);

    // Limits Attributes
    /**
     * For one identifiable with a specific identifiable id, retrieves one operational limits group attributes by its name.
     * @return {@link LimitsAttributes} which is a subset of an identifiable resource.
     */
    Optional<OperationalLimitsGroupAttributes> getOperationalLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String branchId, String operationalLimitGroupName, int side);

    /**
     * For all the identifiables of a specific resource type, retrieves all extension attributes of this identifiable.
     * Used for preloading collection strategy.
     * @return A {@link Map} where keys are identifiable IDs and values are {@link Map}s where keys are extension names and values are {@link ExtensionAttributes}.
     */
    Map<String, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes>> getAllOperationalLimitsGroupAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType);

    /**
     * For one identifiable with a specific identifiable id, retrieves one extension attributes by its extension name.
     * @return {@link LimitsAttributes} which is a subset of an identifiable resource. The extension attributes can be put in the
     * extensionAttributes map of an {@link IdentifiableAttributes} or used to load an extension.
     */
    Optional<OperationalLimitsGroupAttributes> getCurrentLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String branchId, String operationalLimitGroupName, int side);

    /**
     * For all the identifiables of a specific resource type, retrieves all extension attributes of this identifiable.
     * Used for preloading collection strategy.
     * @return A {@link Map} where keys are identifiable IDs and values are {@link Map}s where keys are extension names and values are {@link ExtensionAttributes}.
     */
    Map<String, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes>> getAllSelectedCurrentLimitsGroupAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType);

    Optional<Resource<IdentifiableAttributes>> getIdentifiable(UUID networkUuid, int variantNum, String id);

    List<String> getIdentifiablesIds(UUID networkUuid, int variantNum);

    void flush(UUID networkUuid);
}
