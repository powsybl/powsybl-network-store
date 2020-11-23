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
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum) {
        return Optional.empty();
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        // nothing to do
    }

    @Override
    public void updateNetwork(UUID networkUuid, int variantNum, Resource<NetworkAttributes> networkResource) {
        // nothing to do
    }

    @Override
    public void createSubstations(UUID networkUuid, int variantNum, List<Resource<SubstationAttributes>> substationResources) {
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
    public int getSubstationCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
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
    public int getVoltageLevelCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateVoltageLevel(UUID networkUuid, int variantNum, Resource<VoltageLevelAttributes> voltageLevelResource) {
        // nothing to do
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, int variantNum, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        // nothing to do
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        // nothing to do
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        // nothing to do
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
    public void removeGenerator(UUID networkUuid, int variantNum, String generatorsId) {
        // nothing to do
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
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
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return Collections.emptyList();
    }

    @Override
    public void removeLoad(UUID networkUuid, int variantNum, String loadId) {
        // nothing to do
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
    public void removeShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        // nothing to do
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
    public void removeVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        // nothing to do
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
    public void removeStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        // nothing to do
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
    public void removeLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        // nothing to do
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
    public void removeTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        // nothing to do
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
    public void removeThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        // nothing to do
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
    public void removeLine(UUID networkUuid, int variantNum, String lineId) {
        // nothing to do
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
    public void createSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources) {
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
    public int getSwitchCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateSwitch(UUID networkUuid, int variantNum, Resource<SwitchAttributes> switchResource) {
        // nothing to do
    }

    @Override
    public void updateSwitches(UUID networkUuid, int variantNum, List<Resource<SwitchAttributes>> switchResources) {
        // nothing to do
    }

    @Override
    public void createBusbarSections(UUID networkUuid, int variantNum, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
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
    public int getBusbarSectionCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void createLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources) {
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
    public int getLoadCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateLoad(UUID networkUuid, int variantNum, Resource<LoadAttributes> loadResource) {
        // nothing to do
    }

    @Override
    public void updateLoads(UUID networkUuid, int variantNum, List<Resource<LoadAttributes>> loadResources) {
        // nothing to do
    }

    @Override
    public void createGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources) {
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
    public int getGeneratorCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateGenerator(UUID networkUuid, int variantNum, Resource<GeneratorAttributes> generatorResource) {
        // nothing to do
    }

    @Override
    public void updateGenerators(UUID networkUuid, int variantNum, List<Resource<GeneratorAttributes>> generatorResources) {
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
    public void createTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
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
    public int getTwoWindingsTransformerCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateTwoWindingsTransformer(UUID networkUuid, int variantNum, Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        // nothing to do
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        // nothing to do
    }

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
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
    public int getThreeWindingsTransformerCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateThreeWindingsTransformer(UUID networkUuid, int variantNum, Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerResource) {
        // nothing to do
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, int variantNum, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResourceq) {
        // nothing to do
    }

    @Override
    public void createLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResources) {
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
    public int getLineCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateLine(UUID networkUuid, int variantNum, Resource<LineAttributes> lineResource) {
        // nothing to do
    }

    @Override
    public void updateLines(UUID networkUuid, int variantNum, List<Resource<LineAttributes>> lineResourceq) {
        // nothing to do
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
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
    public int getShuntCompensatorCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateShuntCompensator(UUID networkUuid, int variantNum, Resource<ShuntCompensatorAttributes> shuntCompensatorResource) {
        // nothing to do
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, int variantNum, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        // nothing to do
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
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
    public int getVscConverterStationCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateVscConverterStation(UUID networkUuid, int variantNum, Resource<VscConverterStationAttributes> vscConverterStationResource) {
        // nothing to do
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, int variantNum, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        // nothing to do
    }

    @Override
    public void createLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
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
    public int getLccConverterStationCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateLccConverterStation(UUID networkUuid, int variantNum, Resource<LccConverterStationAttributes> lccConverterStationResource) {
        // nothing to do
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, int variantNum, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        // nothing to do
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
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
    public int getStaticVarCompensatorCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateStaticVarCompensator(UUID networkUuid, int variantNum, Resource<StaticVarCompensatorAttributes> staticVarCompensatorResource) {
        // nothing to do
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, int variantNum, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        // nothing to do
    }

    @Override
    public void createHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
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
    public void removeHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        // nothing to do
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        // nothing to do
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void updateHvdcLine(UUID networkUuid, int variantNum, Resource<HvdcLineAttributes> hvdcLineResource) {
        // nothing to do
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, int variantNum, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        // nothing to do
    }

    @Override
    public void createDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources) {
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
    public int getDanglingLineCount(UUID networkUuid, int variantNum) {
        return 0;
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        // nothing to do
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        // nothing to do
    }

    @Override
    public void updateDanglingLine(UUID networkUuid, int variantNum, Resource<DanglingLineAttributes> danglingLineResource) {
        // nothing to do
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, int variantNum, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        // nothing to do
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesRessources) {
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
    public void updateConfiguredBus(UUID networkUuid, int variantNum, Resource<ConfiguredBusAttributes> busesResource) {
        // nothing to do
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, int variantNum, List<Resource<ConfiguredBusAttributes>> busesResources) {
        // nothing to do
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
