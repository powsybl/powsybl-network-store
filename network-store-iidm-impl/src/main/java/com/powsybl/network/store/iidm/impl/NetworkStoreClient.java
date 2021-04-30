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

    // network

    List<Resource<NetworkAttributes>> getNetworks();

    void createNetworks(List<Resource<NetworkAttributes>> networkResources);

    Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid);

    void deleteNetwork(UUID networkUuid);

    void updateNetwork(UUID networkUuid, Resource<NetworkAttributes> networkResource);

    // substation

    void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources);

    List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid);

    Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId);

    void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources);

    void removeSubstations(UUID networkUuid, List<String> substationsId);

    // voltage level

    void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources);

    Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId);

    List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid);

    List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId);

    void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources);

    void removeVoltageLevels(UUID networkUuid, List<String> voltageLevelsId);

    List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId);

    List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId);

    List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId);

    List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, String voltageLevelId);

    List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId);

    List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId);

    List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId);

    List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId);

    List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId);

    List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId);

    List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId);

    List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId);

    List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId);

    List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId);

    // switch

    void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources);

    List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid);

    Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId);

    void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources);

    void removeSwitches(UUID networkUuid, List<String> switchesId);

    // busbar section

    void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources);

    List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid);

    Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId);

    void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources);

    void removeBusBarSections(UUID networkUuid, List<String> busBarSectionsId);

    // load

    void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources);

    List<Resource<LoadAttributes>> getLoads(UUID networkUuid);

    Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId);

    void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources);

    void removeLoads(UUID networkUuid, List<String> loadsId);

    // generator

    void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources);

    List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid);

    Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId);

    void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources);

    void removeGenerators(UUID networkUuid, List<String> generatorsId);

    // battery

    void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources);

    List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid);

    Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, String batteryId);

    void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources);

    void removeBatteries(UUID networkUuid, List<String> batteriesIds);

    // 2 windings transformer

    void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources);

    List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid);

    Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId);

    void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources);

    void removeTwoWindingsTransformers(UUID networkUuid, List<String> twoWindingsTransformersId);

    // 3 windings transformer

    void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources);

    List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid);

    Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId);

    void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResourceq);

    void removeThreeWindingsTransformers(UUID networkUuid, List<String> threeWindingsTransformersId);

    // line

    void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources);

    List<Resource<LineAttributes>> getLines(UUID networkUuid);

    Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId);

    void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResourceq);

    void removeLines(UUID networkUuid, List<String> linesId);

    // shunt compensator

    void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources);

    List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid);

    Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId);

    void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources);

    void removeShuntCompensators(UUID networkUuid, List<String> shuntCompensatorsId);

    // VSC converter station

    void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources);

    List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid);

    Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId);

    void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources);

    void removeVscConverterStations(UUID networkUuid, List<String> vscConverterStationsId);

    // LCC converter station

    void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources);

    List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid);

    Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId);

    void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources);

    void removeLccConverterStations(UUID networkUuid, List<String> lccConverterStationsId);

    // SVC

    void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources);

    List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid);

    Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId);

    void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources);

    void removeStaticVarCompensators(UUID networkUuid, List<String> staticVarCompensatorsId);

    // HVDC line

    void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources);

    List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid);

    Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId);

    void removeHvdcLines(UUID networkUuid, List<String> hvdcLinesId);

    void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources);

    // Dangling line

    void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources);

    List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid);

    Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId);

    void removeDanglingLines(UUID networkUuid, List<String> danglingLinesId);

    void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources);

    // Bus

    void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources);

    List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid);

    Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId);

    void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources);

    void removeConfiguredBuses(UUID networkUuid, List<String> busesId);

    void flush();

}
