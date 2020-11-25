/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class OfflineNetworkStoreClient implements NetworkStoreClient {

    @Override
    public void setSelf(NetworkStoreClient self) {
        // nothing to do
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        return Collections.emptyList();
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        // nothing to do
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        return Optional.empty();
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        // nothing to do
    }

    @Override
    public void updateNetwork(UUID networkUuid, Resource<NetworkAttributes> networkResource) {
        // nothing to do
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        // nothing to do
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return Optional.empty();
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void removeSubstation(UUID networkUuid, String substationId) {
        // nothing to do
    }

    @Override
    public void removeSubstations(UUID networkUuid, List<String> substationsId) {
        // nothing to do
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        // nothing to do
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return Optional.empty();
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return Collections.emptyList();
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, Resource<VoltageLevelAttributes> voltageLevelResource) {
        // nothing to do
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        // nothing to do
    }

    @Override
    public void removeVoltageLevel(UUID networkUuid, String voltageLevelId) {
        // nothing to do
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, List<String> voltageLevelsId) {
        // nothing to do
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, String busBarSectionId) {
        // nothing to do
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, List<String> busBarSectionsId) {
        // nothing to do
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeGenerator(UUID networkUuid, String generatorsId) {
        // nothing to do
    }

    @Override
    public void removeGenerators(UUID networkUuid, List<String> generatorsId) {
        // nothing to do
    }

    @Override
    public void removeBatteries(UUID networkUuid, List<String> batteriesId) {
        // nothing to do
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeBattery(UUID networkUuid, String batteriesId) {
        // nothing to do
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeLoad(UUID networkUuid, String loadId) {
        // nothing to do
    }

    @Override
    public void removeLoads(UUID networkUuid, List<String> loadsId) {
        // nothing to do
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        // nothing to do
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, List<String> shuntCompensatorsId) {
        // nothing to do
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        // nothing to do
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, List<String> vscConverterStationsId) {
        // nothing to do
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        // nothing to do
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, List<String> staticVarCompensatorsId) {
        // nothing to do
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        // nothing to do
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, List<String> lccConverterStationsId) {
        // nothing to do
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        // nothing to do
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, List<String> twoWindingsTransformersId) {
        // nothing to do
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        // nothing to do
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, List<String> threeWindingsTransformersId) {
        // nothing to do
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeLine(UUID networkUuid, String lineId) {
        // nothing to do
    }

    @Override
    public void removeLines(UUID networkUuid, List<String> linesId) {
        // nothing to do
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        // nothing to do
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return Optional.empty();
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> switchResource) {
        // nothing to do
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        // nothing to do
    }

    @Override
    public void removeSwitch(UUID networkUuid, String switchId) {
        // nothing to do
    }

    @Override
    public void removeSwitches(UUID networkUuid, List<String> switchesId) {
        // nothing to do
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        // nothing to do
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return Optional.empty();
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        // nothing to do
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return Optional.empty();
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateLoad(UUID networkUuid, Resource<LoadAttributes> loadResource) {
        // nothing to do
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        // nothing to do
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        // nothing to do
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return Optional.empty();
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> generatorResource) {
        // nothing to do
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        // nothing to do
    }

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        // nothing to do
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, String batteryId) {
        return Optional.empty();
    }

    @Override
    public int getBatteryCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateBattery(UUID networkUuid, Resource<BatteryAttributes> batteryResource) {
        // nothing to do
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        // nothing to do
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        // nothing to do
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return Optional.empty();
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        // nothing to do
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        // nothing to do
    }

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        // nothing to do
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return Optional.empty();
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        // nothing to do
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResourceq) {
        // nothing to do
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        // nothing to do
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return Optional.empty();
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateLine(UUID networkUuid, Resource<LineAttributes> lineResource) {
        // nothing to do
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResourceq) {
        // nothing to do
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        // nothing to do
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return Optional.empty();
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        // nothing to do
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        // nothing to do
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        // nothing to do
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return Optional.empty();
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        // nothing to do
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        // nothing to do
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        // nothing to do
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return Optional.empty();
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        // nothing to do
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        // nothing to do
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        // nothing to do
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return Optional.empty();
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        // nothing to do
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        // nothing to do
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        // nothing to do
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return Optional.empty();
    }

    @Override
    public void removeHvdcLine(UUID networkUuid, String hvdcLineId) {
        // nothing to do
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, List<String> hvdcLinesId) {
        // nothing to do
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> hvdcLineResource) {
        // nothing to do
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        // nothing to do
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        // nothing to do
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return Optional.empty();
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return 0;
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        // nothing to do
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, List<String> danglingLinesId) {
        // nothing to do
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> danglingLineResource) {
        // nothing to do
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        // nothing to do
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesRessources) {
        // nothing to do
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return Optional.empty();
    }

    @Override
    public void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> busesResource) {
        // nothing to do
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        // nothing to do
    }

    @Override
    public void removeConfiguredBus(UUID networkUuid, String busId) {
        // nothing to do
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, List<String> busesId) {
        // nothing to do
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
