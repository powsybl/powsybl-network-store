/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import com.powsybl.network.store.model.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface NetworkStoreClient {

    // network

    List<Resource<NetworkAttributes>> getNetworks();

    void createNetworks(List<Resource<NetworkAttributes>> networkResources);

    Optional<Resource<NetworkAttributes>> getNetwork(String networkId);

    void deleteNetwork(String id);

    // substation

    void createSubstations(String networkId, List<Resource<SubstationAttributes>> substationResources);

    List<Resource<SubstationAttributes>> getSubstations(String networkId);

    Optional<Resource<SubstationAttributes>> getSubstation(String networkId, String substationId);

    int getSubstationCount(String networkId);

    // voltage level

    void createVoltageLevels(String networkId, List<Resource<VoltageLevelAttributes>> voltageLevelResources);

    Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(String networkId, String voltageLevelId);

    List<Resource<VoltageLevelAttributes>> getVoltageLevels(String networkId);

    List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(String networkId, String substationId);

    int getVoltageLevelCount(String networkId);

    List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(String networkId, String voltageLevelId);

    List<Resource<SwitchAttributes>> getVoltageLevelSwitches(String networkId, String voltageLevelId);

    List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(String networkId, String voltageLevelId);

    List<Resource<LoadAttributes>> getVoltageLevelLoads(String networkId, String voltageLevelId);

    List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(String networkId, String voltageLevelId);

    List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(String networkId, String voltageLevelId);

    List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(String networkId, String voltageLevelId);

    List<Resource<LineAttributes>> getVoltageLevelLines(String networkId, String voltageLevelId);

    // switch

    void createSwitches(String networkId, List<Resource<SwitchAttributes>> switchResources);

    List<Resource<SwitchAttributes>> getSwitches(String networkId);

    Optional<Resource<SwitchAttributes>> getSwitch(String networkId, String switchId);

    int getSwitchCount(String networkId);

    // busbar section

    void createBusbarSections(String networkId, List<Resource<BusbarSectionAttributes>> busbarSectionResources);

    List<Resource<BusbarSectionAttributes>> getBusbarSections(String networkId);

    Optional<Resource<BusbarSectionAttributes>> getBusbarSection(String networkId, String busbarSectionId);

    int getBusbarSectionCount(String networkId);

    // load

    void createLoads(String networkId, List<Resource<LoadAttributes>> loadResources);

    List<Resource<LoadAttributes>> getLoads(String networkId);

    Optional<Resource<LoadAttributes>> getLoad(String networkId, String loadId);

    int getLoadCount(String networkId);

    // generator

    void createGenerators(String networkId, List<Resource<GeneratorAttributes>> generatorResources);

    List<Resource<GeneratorAttributes>> getGenerators(String networkId);

    Optional<Resource<GeneratorAttributes>> getGenerator(String networkId, String generatorId);

    int getGeneratorCount(String networkId);

    // 2 windings transformer

    void createTwoWindingsTransformers(String networkId, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources);

    List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(String networkId);

    Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(String networkId, String twoWindingsTransformerId);

    int getTwoWindingsTransformerCount(String networkId);

    // line

    void createLines(String networkId, List<Resource<LineAttributes>> lineResources);

    List<Resource<LineAttributes>> getLines(String networkId);

    Optional<Resource<LineAttributes>> getLine(String networkId, String lineId);

    int getLineCount(String networkId);

    // shunt compensator

    void createShuntCompensators(String networkId, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources);

    List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(String networkId);

    Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(String networkId, String shuntCompensatorId);

    int getShuntCompensatorCount(String networkId);

    // VSC converter station

    void createVscConverterStations(String networkId, List<Resource<VscConverterStationAttributes>> vscConverterStationResources);

    List<Resource<VscConverterStationAttributes>> getVscConverterStations(String networkId);

    Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(String networkId, String vscConverterStationId);

    int getVscConverterStationCount(String networkId);

    // SVC

    void createStaticVarCompensators(String networkId, List<Resource<StaticVarCompensatorAttributes>> svcResources);

    // HVDC line

    void createHvdcLines(String networkId, List<Resource<HvdcLineAttributes>> hvdcLineResources);

    void flush();
}
