/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public interface NetworkStoreClient {

    // to find top level impl in case of delegation
    void setSelf(NetworkStoreClient self);

    // network

    List<Resource<NetworkAttributes>> getNetworks(int variantNum);

    void createNetworks(List<Resource<NetworkAttributes>> networkResources);

    Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum);

    void deleteNetwork(UUID networkUuid, int variantNum);

    void deleteNetwork(UUID networkUuid);

    void updateNetwork(UUID networkUuid, int variantNum, Resource<NetworkAttributes> networkResource);

    // substation

    void createSubstations(UUID networkUuid, int variantNum, List<Resource<SubstationAttributes>> substationResources);

    List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum);

    Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId);

    int getSubstationCount(UUID networkUuid, int variantNum);

    void removeSubstation(UUID networkUuid, int variantNum, String substationId);

    void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId);

    // voltage level

    void createVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelResources);

    Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId);

    List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum);

    List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId);

    int getVoltageLevelCount(UUID networkUuid, int variantNum);

    void updateVoltageLevel(UUID networkUuid, int variantNum, Resource<VoltageLevelAttributes> voltageLevelResource);

    void updateVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelResources);

    List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId);

    void removeVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId);

    void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId);

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

    List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId);

    // switch

    void createSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources);

    List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum);

    Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId);

    int getSwitchCount(UUID networkUuid, int variantNum);

    void updateSwitch(UUID networkUuid, int variantNum, Resource<SwitchAttributes> switchResource);

    void updateSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources);

    void removeSwitch(UUID networkUuid, int variantNum, String switchId);

    void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId);

    // busbar section

    void createBusbarSections(UUID networkUuid, int variantNum, List<Resource<BusbarSectionAttributes>> busbarSectionResources);

    List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum);

    Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId);

    int getBusbarSectionCount(UUID networkUuid, int variantNum);

    void removeBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId);

    void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId);

    // load

    void createLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources);

    List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum);

    Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId);

    int getLoadCount(UUID networkUuid, int variantNum);

    void updateLoad(UUID networkUuid, int variantNum, Resource<LoadAttributes> loadResource);

    void updateLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources);

    void removeLoad(UUID networkUuid, int variantNum, String loadId);

    void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId);

    // generator

    void createGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources);

    List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum);

    Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId);

    int getGeneratorCount(UUID networkUuid, int variantNum);

    void updateGenerator(UUID networkUuid, int variantNum, Resource<GeneratorAttributes> generatorResource);

    void updateGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources);

    void removeGenerator(UUID networkUuid, int variantNum, String generatorId);

    void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId);

    // battery

    void createBatteries(UUID networkUuid, int variantNum, List<Resource<BatteryAttributes>> batteryResources);

    List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum);

    Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId);

    int getBatteryCount(UUID networkUuid, int variantNum);

    void updateBattery(UUID networkUuid, int variantNum, Resource<BatteryAttributes> batteryResource);

    void updateBatteries(UUID networkUuid, int variantNum, List<Resource<BatteryAttributes>> batteryResources);

    void removeBattery(UUID networkUuid, int variantNum, String batteryId);

    void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesIds);

    // 2 windings transformer

    void createTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources);

    List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum);

    Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId);

    int getTwoWindingsTransformerCount(UUID networkUuid, int variantNum);

    void updateTwoWindingsTransformer(UUID networkUuid, int variantNum, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource);

    void updateTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources);

    void removeTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId);

    void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId);

    // 3 windings transformer

    void createThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources);

    List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum);

    Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId);

    int getThreeWindingsTransformerCount(UUID networkUuid, int variantNum);

    void updateThreeWindingsTransformer(UUID networkUuid, int variantNum, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource);

    void updateThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResourceq);

    void removeThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId);

    void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId);

    // line

    void createLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResources);

    List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum);

    Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId);

    int getLineCount(UUID networkUuid, int variantNum);

    void updateLine(UUID networkUuid, int variantNum, Resource<LineAttributes> lineResource);

    void updateLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResourceq);

    void removeLine(UUID networkUuid, int variantNum, String lineId);

    void removeLines(UUID networkUuid, int variantNum, List<String> linesId);

    // shunt compensator

    void createShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources);

    List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum);

    Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId);

    int getShuntCompensatorCount(UUID networkUuid, int variantNum);

    void updateShuntCompensator(UUID networkUuid, int variantNum, Resource<ShuntCompensatorAttributes> shuntCompensatorResource);

    void updateShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources);

    void removeShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId);

    void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId);

    // VSC converter station

    void createVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources);

    List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum);

    Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId);

    int getVscConverterStationCount(UUID networkUuid, int variantNum);

    void updateVscConverterStation(UUID networkUuid, int variantNum, Resource<VscConverterStationAttributes> vscConverterStationResource);

    void updateVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources);

    void removeVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId);

    void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId);

    // LCC converter station

    void createLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources);

    List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum);

    Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId);

    int getLccConverterStationCount(UUID networkUuid, int variantNum);

    void updateLccConverterStation(UUID networkUuid, int variantNum, Resource<LccConverterStationAttributes> lccConverterStationResource);

    void updateLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources);

    void removeLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId);

    void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId);

    // SVC

    void createStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> svcResources);

    List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum);

    Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId);

    int getStaticVarCompensatorCount(UUID networkUuid, int variantNum);

    void updateStaticVarCompensator(UUID networkUuid, int variantNum, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource);

    void updateStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources);

    void removeStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId);

    void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId);

    // HVDC line

    void createHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources);

    List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum);

    Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId);

    int getHvdcLineCount(UUID networkUuid, int variantNum);

    void removeHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId);

    void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId);

    void updateHvdcLine(UUID networkUuid, int variantNum, Resource<HvdcLineAttributes> hvdcLineResource);

    void updateHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources);

    // Dangling line

    void createDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources);

    List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum);

    Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId);

    int getDanglingLineCount(UUID networkUuid, int variantNum);

    void removeDanglingLine(UUID networkUuid, int variantNum, String danglingLineId);

    void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId);

    void updateDanglingLine(UUID networkUuid, int variantNum, Resource<DanglingLineAttributes> danglingLineResource);

    void updateDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources);

    // Bus

    void createConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesRessources);

    List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum);

    Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId);

    void updateConfiguredBus(UUID networkUuid, int variantNum, Resource<ConfiguredBusAttributes> busesResource);

    void updateConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesResources);

    void removeConfiguredBus(UUID networkUuid, int variantNum, String busId);

    void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId);

    void flush();

}
