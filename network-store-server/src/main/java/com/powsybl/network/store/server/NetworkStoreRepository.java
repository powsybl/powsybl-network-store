/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.powsybl.network.store.server.QueryBuilder.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Repository
public class NetworkStoreRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkStoreRepository.class);

    @Autowired
    public NetworkStoreRepository(DataSource ds) {
        try {
            this.session = new Session(ds.getConnection());
        } catch (SQLException e) {
            throw new PowsyblException(e);
        }
    }

    private Session session;

    private static final int BATCH_SIZE = 1000;

    private PreparedStatement psInsertNetwork;
    private Supplier<java.sql.PreparedStatement> psCloneNetworkSupplier;
    private PreparedStatement psUpdateNetwork;
    private PreparedStatement psInsertSubstation;
    private Supplier<java.sql.PreparedStatement> psCloneSubstationSupplier;
    private PreparedStatement psUpdateSubstation;
    private PreparedStatement psInsertVoltageLevel;
    private Supplier<java.sql.PreparedStatement> psCloneVoltageLevelSupplier;
    private PreparedStatement psUpdateVoltageLevel;
    private PreparedStatement psInsertGenerator;
    private Supplier<java.sql.PreparedStatement> psCloneGeneratorSupplier;
    private PreparedStatement psUpdateGenerator;
    private PreparedStatement psInsertBattery;
    private Supplier<java.sql.PreparedStatement> psCloneBatterySupplier;
    private PreparedStatement psUpdateBattery;
    private PreparedStatement psInsertLoad;
    private Supplier<java.sql.PreparedStatement> psCloneLoadSupplier;
    private PreparedStatement psUpdateLoad;
    private PreparedStatement psInsertShuntCompensator;
    private Supplier<java.sql.PreparedStatement> psCloneShuntCompensatorSupplier;
    private PreparedStatement psUpdateShuntCompensator;
    private PreparedStatement psInsertVscConverterStation;
    private Supplier<java.sql.PreparedStatement> psCloneVscConverterStationSupplier;
    private PreparedStatement psUpdateVscConverterStation;
    private PreparedStatement psInsertLccConverterStation;
    private Supplier<java.sql.PreparedStatement> psCloneLccConverterStationSupplier;
    private PreparedStatement psUpdateLccConverterStation;
    private PreparedStatement psInsertStaticVarCompensator;
    private Supplier<java.sql.PreparedStatement> psCloneStaticVarCompensatorSupplier;
    private PreparedStatement psUpdateStaticVarCompensator;
    private PreparedStatement psInsertBusbarSection;
    private Supplier<java.sql.PreparedStatement> psCloneBusbarSectionSupplier;
    private PreparedStatement psUpdateBusbarSection;
    private PreparedStatement psInsertSwitch;
    private Supplier<java.sql.PreparedStatement> psCloneSwitchSupplier;
    private PreparedStatement psUpdateSwitch;
    private PreparedStatement psInsertTwoWindingsTransformer;
    private Supplier<java.sql.PreparedStatement> psCloneTwoWindingsTransformerSupplier;
    private PreparedStatement psUpdateTwoWindingsTransformer;
    private PreparedStatement psInsertThreeWindingsTransformer;
    private Supplier<java.sql.PreparedStatement> psCloneThreeWindingsTransformerSupplier;
    private PreparedStatement psUpdateThreeWindingsTransformer;
    private PreparedStatement psInsertLine;
    private Supplier<java.sql.PreparedStatement> psCloneLineSupplier;
    private PreparedStatement psUpdateLines;
    private PreparedStatement psInsertHvdcLine;
    private Supplier<java.sql.PreparedStatement> psCloneHvdcLineSupplier;
    private PreparedStatement psUpdateHvdcLine;
    private PreparedStatement psInsertDanglingLine;
    private Supplier<java.sql.PreparedStatement> psCloneDanglingLineSupplier;
    private PreparedStatement psUpdateDanglingLine;
    private PreparedStatement psInsertConfiguredBus;
    private Supplier<java.sql.PreparedStatement> psCloneConfiguredBusSupplier;
    private PreparedStatement psUpdateConfiguredBus;

    private final Map<String, PreparedStatement> insertPreparedStatements = new LinkedHashMap<>();
    private final Map<String, Supplier<java.sql.PreparedStatement>> clonePreparedStatementsSupplier = new LinkedHashMap<>();

    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String CONNECTABLE_BUS = "connectableBus";
    private static final String LOAD_DETAIL = "loadDetail";
    private static final String LINEAR_MODEL = "linearModel";
    private static final String NON_LINEAR_MODEL = "nonLinearModel";
    private static final String SECTION_COUNT = "sectionCount";
    private static final String GENERATION = "generation";
    private static final String SLACK_TERMINAL = "slackTerminal";
    private static final String CGMES_SV_METADATA = "cgmesSvMetadata";
    private static final String CGMES_SSH_METADATA = "cgmesSshMetadata";
    private static final String CGMES_IIDM_MAPPING = "cgmesIidmMapping";
    private static final String CIM_CHARACTERISTICS = "cimCharacteristics";
    private static final String CGMES_CONTROL_AREAS = "cgmesControlAreas";
    private static final String ALIASES_WITHOUT_TYPE = "aliasesWithoutType";
    private static final String ALIAS_BY_TYPE = "aliasByType";
    private static final String ID_BY_ALIAS = "idByAlias";
    private static final String ACTIVE_POWER_LIMITS = "activePowerLimits";
    private static final String ACTIVE_POWER_LIMITS1 = "activePowerLimits1";
    private static final String ACTIVE_POWER_LIMITS2 = "activePowerLimits2";
    private static final String ACTIVE_POWER_LIMITS3 = "activePowerLimits3";
    private static final String APPARENT_POWER_LIMITS = "apparentPowerLimits";
    private static final String APPARENT_POWER_LIMITS1 = "apparentPowerLimits1";
    private static final String APPARENT_POWER_LIMITS2 = "apparentPowerLimits2";
    private static final String APPARENT_POWER_LIMITS3 = "apparentPowerLimits3";
    private static final String ACTIVE_POWER_CONTROL = "activePowerControl";
    private static final String TARGET_DEADBAND = "targetDeadband";
    private static final String CURRENT_LIMITS1 = "currentLimits1";
    private static final String CURRENT_LIMITS2 = "currentLimits2";
    private static final String CURRENT_LIMITS3 = "currentLimits3";
    private static final String PHASE_ANGLE_CLOCK = "phaseAngleClock";
    private static final String HVDC_ANGLE_DROOP_ACTIVE_POWER_CONTROL = "hvdcAngleDroopActivePowerControl";
    private static final String HVDC_OPERATOR_ACTIVE_POWER_RANGE = "hvdcOperatorActivePowerRange";
    private static final String NETWORK = "network";
    private static final String SUBSTATION = "substation";
    private static final String VOLTAGE_LEVEL = "voltageLevel";
    private static final String GENERATOR = "generator";
    private static final String BATTERY = "battery";
    private static final String SHUNT_COMPENSATOR = "shuntCompensator";
    private static final String VSC_CONVERTER_STATION = "vscConverterStation";
    private static final String LCC_CONVERTER_STATION = "lccConverterStation";
    private static final String STATIC_VAR_COMPENSATOR = "staticVarCompensator";
    private static final String BUSBAR_SECTION = "busbarSection";
    private static final String SWITCH = "switch";
    private static final String TWO_WINDINGS_TRANSFORMER = "twoWindingsTransformer";
    private static final String THREE_WINDINGS_TRANSFORMER = "threeWindingsTransformer";
    private static final String HVDC_LINE = "hvdcLine";
    private static final String DANGLING_LINE = "danglingLine";
    private static final String CONFIGURED_BUS = "configuredBus";
    private static final String LOAD = "load";
    private static final String LINE = "line";
    private static final String BRANCH_STATUS = "branchStatus";
    private static final String VARIANT_NUM = "variantNum";
    private static final String VARIANT_ID = "variantId";
    private static final String CGMES_TAP_CHANGERS = "cgmesTapChangers";

    private static final List<String> ELEMENT_TABLES = List.of(SUBSTATION, VOLTAGE_LEVEL, BUSBAR_SECTION, CONFIGURED_BUS, SWITCH, GENERATOR, BATTERY, LOAD, SHUNT_COMPENSATOR,
            STATIC_VAR_COMPENSATOR, VSC_CONVERTER_STATION, LCC_CONVERTER_STATION, TWO_WINDINGS_TRANSFORMER,
            THREE_WINDINGS_TRANSFORMER, LINE, HVDC_LINE, DANGLING_LINE);

    private static final List<String> ALL_TABLES = ImmutableList.<String>builder()
            .add(NETWORK)
            .addAll(ELEMENT_TABLES)
            .build();

    private Mappings mappings = new Mappings();

    @PostConstruct
    void prepareStatements() {
        // network

        Set<String> keysNetworks = mappings.getNetworkMappings().keySet();
        Insert insertNetwork = insertInto(NETWORK)
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysNetworks.forEach(k -> insertNetwork.value(k, bindMarker()));
        psInsertNetwork = session.prepare(insertNetwork.build());
        insertPreparedStatements.put(NETWORK, psInsertNetwork);

        // TODO : simplify with keysNetworks ???
        psCloneNetworkSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                        "insert into network(" +
                          VARIANT_NUM + ", " +
                          VARIANT_ID + ", " +
                          "uuid" + ", " +
                          "id" + ", " +
                          "fictitious" + ", " +
                          "properties" + ", " +
                          ALIASES_WITHOUT_TYPE + ", " +
                          ALIAS_BY_TYPE + ", " +
                          ID_BY_ALIAS + ", " +
                          "caseDate" + ", " +
                          "forecastDistance" + ", " +
                          "sourceFormat" + ", " +
                          "connectedComponentsValid" + ", " +
                          "synchronousComponentsValid" + ", " +
                          CGMES_SV_METADATA + ", " +
                          CGMES_SSH_METADATA + ", " +
                          CIM_CHARACTERISTICS + ", " +
                          CGMES_CONTROL_AREAS + ", " +
                          CGMES_IIDM_MAPPING + ") " +
                          "select" + " " +

                          "?" + ", " +
                          "?" + ", " +
                          "uuid" + ", " +
                          "id" + ", " +
                          "fictitious" + ", " +
                          "properties" + ", " +
                          ALIASES_WITHOUT_TYPE + ", " +
                          ALIAS_BY_TYPE + ", " +
                          ID_BY_ALIAS + ", " +
                          "caseDate" + ", " +
                          "forecastDistance" + ", " +
                          "sourceFormat" + ", " +
                          "connectedComponentsValid" + ", " +
                          "synchronousComponentsValid" + ", " +
                          CGMES_SV_METADATA + ", " +
                          CGMES_SSH_METADATA + ", " +
                          CIM_CHARACTERISTICS + ", " +
                          CGMES_CONTROL_AREAS + ", " +
                          CGMES_IIDM_MAPPING + " " +
                          "from network" + " " +
                          "where uuid = ? and variantNum = ?"
                        );
            } catch (SQLException e) {
                throw new PowsyblException(e);
            }
        };

        Update updateNetwork = update(NETWORK).set(Assignment.setColumn("id", bindMarker()));
        keysNetworks.forEach(k -> {
            if (!k.equals("uuid") && !k.equals("variantId")) {
                updateNetwork.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateNetwork
            .whereColumn("uuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker());
        psUpdateNetwork = session.prepare(updateNetwork.build());

        // substation

        Set<String> keysSubstations = mappings.getSubstationMappings().keySet();
        Insert insertSubstation = insertInto(SUBSTATION)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysSubstations.forEach(k -> insertSubstation.value(k, bindMarker()));
        psInsertSubstation = session.prepare(insertSubstation.build());
        insertPreparedStatements.put(SUBSTATION, psInsertSubstation);

        psCloneSubstationSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + SUBSTATION + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysSubstations) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysSubstations) +
                        " from " + SUBSTATION + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(SUBSTATION, psCloneSubstationSupplier);

        Update updateSubstation = update(SUBSTATION);
        keysSubstations.forEach(k -> updateSubstation.set(Assignment.setColumn(k, bindMarker())));
        updateSubstation
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker());
        psUpdateSubstation = session.prepare(updateSubstation.build());

        // voltage level

        Set<String> keysVoltageLevels = mappings.getVoltageLevelMappings().keySet();
        Insert insertVoltageLevel = insertInto(VOLTAGE_LEVEL)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysVoltageLevels.forEach(k -> insertVoltageLevel.value(k, bindMarker()));
        psInsertVoltageLevel = session.prepare(insertVoltageLevel.build());
        insertPreparedStatements.put(VOLTAGE_LEVEL, psInsertVoltageLevel);

        psCloneVoltageLevelSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + VOLTAGE_LEVEL + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysVoltageLevels) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysVoltageLevels) +
                        " from " + VOLTAGE_LEVEL + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(VOLTAGE_LEVEL, psCloneVoltageLevelSupplier);

        Update updateVoltageLevel = update(VOLTAGE_LEVEL);
        keysVoltageLevels.forEach(k -> {
            if (!k.equals("substationId")) {
                updateVoltageLevel.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateVoltageLevel
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("substationId").isEqualTo(bindMarker());
        psUpdateVoltageLevel = session.prepare(updateVoltageLevel.build());

        // generator

        Set<String> keysGenerators = mappings.getGeneratorMappings().keySet();
        Insert insertGenerator = insertInto(GENERATOR)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysGenerators.forEach(k -> insertGenerator.value(k, bindMarker()));
        psInsertGenerator = session.prepare(insertGenerator.build());
        insertPreparedStatements.put(GENERATOR, psInsertGenerator);

        psCloneGeneratorSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + GENERATOR + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysGenerators) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysGenerators) +
                        " from " + GENERATOR + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(GENERATOR, psCloneGeneratorSupplier);

        Update updateGenerator = update(GENERATOR);
        keysGenerators.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateGenerator.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateGenerator
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateGenerator = session.prepare(updateGenerator.build());

        // battery

        Set<String> keysBatteries = mappings.getBatteryMappings().keySet();
        Insert insertBattery = insertInto(BATTERY)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysBatteries.forEach(k -> insertBattery.value(k, bindMarker()));
        psInsertBattery = session.prepare(insertBattery.build());
        insertPreparedStatements.put(BATTERY, psInsertBattery);

        psCloneBatterySupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + BATTERY + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysBatteries) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysBatteries) +
                        " from " + BATTERY + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(BATTERY, psCloneBatterySupplier);

        Update updateBattery = update(BATTERY);
        keysBatteries.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateBattery.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateBattery
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateBattery = session.prepare(updateBattery.build());

        // load

        Set<String> keysLoads = mappings.getLoadMappings().keySet();
        Insert insertLoad = insertInto(LOAD)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysLoads.forEach(k -> insertLoad.value(k, bindMarker()));
        psInsertLoad = session.prepare(insertLoad.build());
        insertPreparedStatements.put(LOAD, psInsertLoad);

        psCloneLoadSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + LOAD + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysLoads) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysLoads) +
                        " from " + LOAD + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(LOAD, psCloneLoadSupplier);

        Update updateLoad = update(LOAD);
        keysLoads.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateLoad.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateLoad
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateLoad = session.prepare(updateLoad.build());

        // shunt compensator

        Set<String> keysShuntCompensators = mappings.getShuntCompensatorMappings().keySet();
        Insert insertShuntCompensator = insertInto(SHUNT_COMPENSATOR)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysShuntCompensators.forEach(k -> insertShuntCompensator.value(k, bindMarker()));
        psInsertShuntCompensator = session.prepare(insertShuntCompensator.build());
        insertPreparedStatements.put(SHUNT_COMPENSATOR, psInsertShuntCompensator);

        psCloneShuntCompensatorSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + SHUNT_COMPENSATOR + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysShuntCompensators) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysShuntCompensators) +
                        " from " + SHUNT_COMPENSATOR + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(SHUNT_COMPENSATOR, psCloneShuntCompensatorSupplier);

        Update updateShuntCompensator = update(SHUNT_COMPENSATOR);
        keysShuntCompensators.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateShuntCompensator.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateShuntCompensator
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateShuntCompensator = session.prepare(updateShuntCompensator.build());

        // vsc converter station

        Set<String> keysVscConverterStations = mappings.getVscConverterStationMappings().keySet();
        Insert insertVscConverterStation = insertInto(VSC_CONVERTER_STATION)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysVscConverterStations.forEach(k -> insertVscConverterStation.value(k, bindMarker()));
        psInsertVscConverterStation = session.prepare(insertVscConverterStation.build());
        insertPreparedStatements.put(VSC_CONVERTER_STATION, psInsertVscConverterStation);

        psCloneVscConverterStationSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + VSC_CONVERTER_STATION + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysVscConverterStations) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysVscConverterStations) +
                        " from " + VSC_CONVERTER_STATION + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(VSC_CONVERTER_STATION, psCloneVscConverterStationSupplier);

        Update updateVscConverterStation = update(VSC_CONVERTER_STATION);
        keysVscConverterStations.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateVscConverterStation.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateVscConverterStation
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateVscConverterStation = session.prepare(updateVscConverterStation.build());

        // lcc converter station

        Set<String> keysLccConverterStations = mappings.getLccConverterStationMappings().keySet();
        Insert insertLccConverterStation = insertInto(LCC_CONVERTER_STATION)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysLccConverterStations.forEach(k -> insertLccConverterStation.value(k, bindMarker()));
        psInsertLccConverterStation = session.prepare(insertLccConverterStation.build());
        insertPreparedStatements.put(LCC_CONVERTER_STATION, psInsertLccConverterStation);

        psCloneLccConverterStationSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + LCC_CONVERTER_STATION + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysLccConverterStations) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysLccConverterStations) +
                        " from " + LCC_CONVERTER_STATION + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(LCC_CONVERTER_STATION, psCloneLccConverterStationSupplier);

        Update updateLccConverterStation = update(LCC_CONVERTER_STATION);
        keysLccConverterStations.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateLccConverterStation.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateLccConverterStation
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateLccConverterStation = session.prepare(updateLccConverterStation.build());

        // static var compensator

        Set<String> keysStaticVarCompensators = mappings.getStaticVarCompensatorMappings().keySet();
        Insert insertStaticVarCompensator = insertInto(STATIC_VAR_COMPENSATOR)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysStaticVarCompensators.forEach(k -> insertStaticVarCompensator.value(k, bindMarker()));
        psInsertStaticVarCompensator = session.prepare(insertStaticVarCompensator.build());
        insertPreparedStatements.put(STATIC_VAR_COMPENSATOR, psInsertStaticVarCompensator);

        psCloneStaticVarCompensatorSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + STATIC_VAR_COMPENSATOR + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysStaticVarCompensators) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysStaticVarCompensators) +
                        " from " + STATIC_VAR_COMPENSATOR + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(STATIC_VAR_COMPENSATOR, psCloneStaticVarCompensatorSupplier);

        Update updateStaticVarCompensator = update(STATIC_VAR_COMPENSATOR);
        keysStaticVarCompensators.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateStaticVarCompensator.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateStaticVarCompensator
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateStaticVarCompensator = session.prepare(updateStaticVarCompensator.build());

        // busbar section

        Set<String> keysBusbarSections = mappings.getBusbarSectionMappings().keySet();
        Insert insertBusbarSection = insertInto(BUSBAR_SECTION)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysBusbarSections.forEach(k -> insertBusbarSection.value(k, bindMarker()));
        psInsertBusbarSection = session.prepare(insertBusbarSection.build());
        insertPreparedStatements.put(BUSBAR_SECTION, psInsertBusbarSection);

        psCloneBusbarSectionSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + BUSBAR_SECTION + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysBusbarSections) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysBusbarSections) +
                        " from " + BUSBAR_SECTION + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(BUSBAR_SECTION, psCloneBusbarSectionSupplier);

        Update updateBusbarSection = update(BUSBAR_SECTION);
        keysBusbarSections.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateBusbarSection.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateBusbarSection
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateBusbarSection = session.prepare(updateBusbarSection.build());

        // switch

        Set<String> keysSwitches = mappings.getSwitchMappings().keySet();
        Insert insertSwitch = insertInto(SWITCH)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysSwitches.forEach(k -> insertSwitch.value(k, bindMarker()));
        psInsertSwitch = session.prepare(insertSwitch.build());
        insertPreparedStatements.put(SWITCH, psInsertSwitch);

        psCloneSwitchSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + SWITCH + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysSwitches) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysSwitches) +
                        " from " + SWITCH + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(SWITCH, psCloneSwitchSupplier);

        Update updateSwitch = update(SWITCH);
        keysSwitches.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateSwitch.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateSwitch
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateSwitch = session.prepare(updateSwitch.build());

        // two windings transformer

        Set<String> keysTwoWindingsTransformer = mappings.getTwoWindingsTransformerMappings().keySet();
        Insert insertTwoWindingsTransformer = insertInto(TWO_WINDINGS_TRANSFORMER)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysTwoWindingsTransformer.forEach(k -> insertTwoWindingsTransformer.value(k, bindMarker()));
        psInsertTwoWindingsTransformer = session.prepare(insertTwoWindingsTransformer.build());
        insertPreparedStatements.put(TWO_WINDINGS_TRANSFORMER, psInsertTwoWindingsTransformer);

        psCloneTwoWindingsTransformerSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + TWO_WINDINGS_TRANSFORMER + "(" +
                        VARIANT_NUM + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysTwoWindingsTransformer) +
                        ")" +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysTwoWindingsTransformer) +
                        " from " + TWO_WINDINGS_TRANSFORMER + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(TWO_WINDINGS_TRANSFORMER, psCloneTwoWindingsTransformerSupplier);

        Update updateTwoWindingsTransformer = update(TWO_WINDINGS_TRANSFORMER);
        keysTwoWindingsTransformer.forEach(k -> updateTwoWindingsTransformer.set(Assignment.setColumn(k, bindMarker())));
        updateTwoWindingsTransformer
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker());
        psUpdateTwoWindingsTransformer = session.prepare(updateTwoWindingsTransformer.build());

        // three windings transformer

        Set<String> keysThreeWindingsTransformer = mappings.getThreeWindingsTransformerMappings().keySet();
        Insert insertThreeWindingsTransformer = insertInto(THREE_WINDINGS_TRANSFORMER)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysThreeWindingsTransformer.forEach(k -> insertThreeWindingsTransformer.value(k, bindMarker()));
        psInsertThreeWindingsTransformer = session.prepare(insertThreeWindingsTransformer.build());
        insertPreparedStatements.put(THREE_WINDINGS_TRANSFORMER, psInsertThreeWindingsTransformer);

        psCloneThreeWindingsTransformerSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + THREE_WINDINGS_TRANSFORMER + "(" +
                        VARIANT_NUM + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysThreeWindingsTransformer) +
                        ")" +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysThreeWindingsTransformer) +
                        " from " + THREE_WINDINGS_TRANSFORMER + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(THREE_WINDINGS_TRANSFORMER, psCloneThreeWindingsTransformerSupplier);

        Update updateThreeWindingsTransformer = update(THREE_WINDINGS_TRANSFORMER);
        keysThreeWindingsTransformer.forEach(k -> updateThreeWindingsTransformer.set(Assignment.setColumn(k, bindMarker())));
        updateThreeWindingsTransformer
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker());
        psUpdateThreeWindingsTransformer = session.prepare(updateThreeWindingsTransformer.build());

        // line

        Set<String> keysLines = mappings.getLineMappings().keySet();
        Insert insertLine = insertInto(LINE)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysLines.forEach(k -> insertLine.value(k, bindMarker()));
        psInsertLine = session.prepare(insertLine.build());
        insertPreparedStatements.put(LINE, psInsertLine);

        psCloneLineSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + LINE + "(" +
                        VARIANT_NUM + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysLines) +
                        ")" +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysLines) +
                        " from " + LINE + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(LINE, psCloneLineSupplier);

        Update updateLine = update(LINE);
        keysLines.forEach(k -> updateLine.set(Assignment.setColumn(k, bindMarker())));
        updateLine
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker());
        psUpdateLines = session.prepare(updateLine.build());

        // hvdc line

        Set<String> keysHvdcLines = mappings.getHvdcLineMappings().keySet();
        Insert insertHvdcLine = insertInto(HVDC_LINE)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysHvdcLines.forEach(k -> insertHvdcLine.value(k, bindMarker()));
        psInsertHvdcLine = session.prepare(insertHvdcLine.build());
        insertPreparedStatements.put(HVDC_LINE, psInsertHvdcLine);

        psCloneHvdcLineSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + HVDC_LINE + "(" +
                        VARIANT_NUM + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysHvdcLines) +
                        ")" +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysHvdcLines) +
                        " from " + HVDC_LINE + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(HVDC_LINE, psCloneHvdcLineSupplier);

        Update updateHvdcLine = update(HVDC_LINE);
        keysHvdcLines.forEach(k -> updateHvdcLine.set(Assignment.setColumn(k, bindMarker())));
        updateHvdcLine
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker());
        psUpdateHvdcLine = session.prepare(updateHvdcLine.build());

        // dangling line

        Set<String> keysDanglingLines = mappings.getDanglingLineMappings().keySet();
        Insert insertDanglingLine = insertInto(DANGLING_LINE)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysDanglingLines.forEach(k -> insertDanglingLine.value(k, bindMarker()));
        psInsertDanglingLine = session.prepare(insertDanglingLine.build());
        insertPreparedStatements.put(DANGLING_LINE, psInsertDanglingLine);

        psCloneDanglingLineSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + DANGLING_LINE + "(" +
                        VARIANT_NUM + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysDanglingLines) +
                        ")" +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysDanglingLines) +
                        " from " + DANGLING_LINE + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(DANGLING_LINE, psCloneDanglingLineSupplier);

        Update updateDanglingLine = update(DANGLING_LINE);
        keysDanglingLines.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateDanglingLine.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateDanglingLine
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateDanglingLine = session.prepare(updateDanglingLine.build());

        // configured bus

        Set<String> keysConfiguredBuses = mappings.getConfiguredBusMappings().keySet();
        Insert insertConfiguredBus = insertInto(CONFIGURED_BUS)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysConfiguredBuses.forEach(k -> insertConfiguredBus.value(k, bindMarker()));
        psInsertConfiguredBus = session.prepare(insertConfiguredBus.build());
        insertPreparedStatements.put(CONFIGURED_BUS, psInsertConfiguredBus);

        psCloneConfiguredBusSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + CONFIGURED_BUS + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysConfiguredBuses) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keysConfiguredBuses) +
                        " from " + CONFIGURED_BUS + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(CONFIGURED_BUS, psCloneConfiguredBusSupplier);

        Update updateConfiguredBus = update(CONFIGURED_BUS);
        keysConfiguredBuses.forEach(k -> {
            if (!k.equals("voltageLevelId")) {
                updateConfiguredBus.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateConfiguredBus
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker())
            .whereColumn("voltageLevelId").isEqualTo(bindMarker());
        psUpdateConfiguredBus = session.prepare(updateConfiguredBus.build());
    }

    // TODO remove and cleanup: we are not using cassandra anymore
    // obsolete: This method unsets the null valued columns of a bound statement in order to avoid creation of tombstones
    // obsolete: It must be used only for statements used for creation, not for those used for update
    private static PreparedStatement unsetNullValues(PreparedStatement bs) {
        return bs;
    }

    private static String emptyStringForNullValue(String value) {
        return value == null ? "" : value;
    }

    private static String nullValueForEmptyString(String value) {
        return StringUtils.isBlank(value) ? null : value;
    }

    // network

    /**
     * Get all networks infos.
     */
    public List<NetworkInfos> getNetworksInfos() {
        SimpleStatement simpleStatement = selectFrom(NETWORK).columns("uuid", "id")
                .whereColumn(VARIANT_NUM).isEqualTo(literal(Resource.INITIAL_VARIANT_NUM))
                .allowFiltering()
                .build();
        try (ResultSet resultSet = session.execute(simpleStatement)) {
            List<NetworkInfos> networksInfos = new ArrayList<>();
            for (Row row : resultSet) {
                networksInfos.add(new NetworkInfos(row.getUuid(0), row.getString(1)));
            }
            return networksInfos;
        }
    }

    public List<VariantInfos> getVariantsInfos(UUID networkUuid) {
        try (ResultSet resultSet = session.execute(selectFrom(NETWORK).columns(VARIANT_ID, VARIANT_NUM)
                .whereColumn("uuid").isEqualTo(literal(networkUuid)).build())) {
            List<VariantInfos> variantsInfos = new ArrayList<>();
            for (Row row : resultSet) {
                variantsInfos.add(new VariantInfos(row.getString(0), row.getInt(1)));
            }
            return variantsInfos;
        }
    }

    public Optional<Resource<NetworkAttributes>> getNetwork(UUID uuid, int variantNum) {
        Map<String, Mapping> mappingNetworks = mappings.getNetworkMappings();
        Set<String> columnMappings = new HashSet<>(mappingNetworks.keySet());
        columnMappings.add("id");
        try (ResultSet resultSet = session.execute(selectFrom(NETWORK)
            .columns(columnMappings.toArray(new String[0]))
            .whereColumn("uuid").isEqualTo(literal(uuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                NetworkAttributes networkAttributes = new NetworkAttributes();
                mappingNetworks.entrySet().forEach(entry -> {
                    if (!entry.getKey().equals("caseDate")) {
                        entry.getValue().set(networkAttributes, one.get(entry.getKey(), entry.getValue().getClassR()));
                    } else {
                        entry.getValue().set(networkAttributes, new DateTime(one.getInstant("caseDate").toEpochMilli()));
                    }
                });
                return Optional.of(Resource.networkBuilder()
                    .id(one.get("id", String.class))
                    .variantNum(variantNum)
                    .attributes(networkAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public void createNetworks(List<Resource<NetworkAttributes>> resources) {
        Map<String, Mapping> networkMappings = mappings.getNetworkMappings();
        Set<String> keysNetworks = networkMappings.keySet();

        for (List<Resource<NetworkAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<NetworkAttributes> resource : subresources) {
                NetworkAttributes networkAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysNetworks.forEach(key -> {
                    if (!key.equals("caseDate")) {
                        values.add(networkMappings.get(key).get(networkAttributes));
                    } else {
                        values.add(((DateTime) networkMappings.get(key).get(networkAttributes)).toDate().toInstant());
                    }
                });

                boundStatements.add(unsetNullValues(psInsertNetwork.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void updateNetworks(List<Resource<NetworkAttributes>> resources) {
        Map<String, Mapping> networkMappings = mappings.getNetworkMappings();
        Set<String> keysNetworks = networkMappings.keySet();

        for (List<Resource<NetworkAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<NetworkAttributes> resource : subresources) {
                NetworkAttributes networkAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(resource.getId());
                keysNetworks.forEach(key -> {
                    if (!key.equals("uuid") && !key.equals("variantId")) {
                        if (!key.equals("caseDate")) {
                            values.add(networkMappings.get(key).get(networkAttributes));
                        } else {
                            values.add(((DateTime) networkMappings.get(key).get(networkAttributes)).toDate().toInstant());
                        }
                    }
                });
                values.add(networkAttributes.getUuid());
                values.add(resource.getVariantNum());

                boundStatements.add(unsetNullValues(psUpdateNetwork.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteNetwork(UUID uuid) {
        session.execute(deleteFrom(NETWORK).whereColumn("uuid").isEqualTo(literal(uuid)).build());
        for (String table : ELEMENT_TABLES) {
            session.execute(deleteFrom(table).whereColumn("networkUuid").isEqualTo(literal(uuid)).build());
        }
    }

    /**
     * Just delete one variant of the network
     */
    public void deleteNetwork(UUID uuid, int variantNum) {
        if (variantNum == Resource.INITIAL_VARIANT_NUM) {
            throw new IllegalArgumentException("Cannot delete initial variant");
        }
        session.execute(deleteFrom(NETWORK)
                .whereColumn("uuid").isEqualTo(literal(uuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build());
        for (String table : ELEMENT_TABLES) {
            session.execute(deleteFrom(table)
                    .whereColumn("networkUuid").isEqualTo(literal(uuid))
                    .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                    .build());
        }
    }

    @SuppressWarnings("javasecurity:S5145")
    public void cloneNetwork(UUID uuid, int sourceVariantNum, int targetVariantNum, String targetVariantId) {
        String nonNullTargetVariantId = targetVariantId == null ? "variant-" + UUID.randomUUID() : targetVariantId;
        LOGGER.info("Cloning network {} variant {} to variant {} ({})", uuid, sourceVariantNum, targetVariantNum, nonNullTargetVariantId);

        var stopwatch = Stopwatch.createStarted();

        try {
            java.sql.PreparedStatement psCloneNetwork = psCloneNetworkSupplier.get();
            psCloneNetwork.setInt(1, targetVariantNum);
            psCloneNetwork.setString(2, nonNullTargetVariantId);
            psCloneNetwork.setObject(3, uuid);
            psCloneNetwork.setInt(4, sourceVariantNum);
            psCloneNetwork.executeUpdate();

            for (Supplier<java.sql.PreparedStatement> psSupplier : clonePreparedStatementsSupplier.values()) {
                java.sql.PreparedStatement ps = psSupplier.get();
                ps.setInt(1, targetVariantNum);
                ps.setObject(2, uuid);
                ps.setInt(3, sourceVariantNum);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PowsyblException(e);
        }

        stopwatch.stop();
        LOGGER.info("Network clone done in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public <T extends IdentifiableAttributes> void createEquipments(UUID networkUuid, List<Resource<T>> resources,
                                                                    Map<String, Mapping> mappings, PreparedStatement psInsert) {
        Set<String> keys = mappings.keySet();

        for (List<Resource<T>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<T> resource : subresources) {
                T attributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keys.forEach(key -> values.add(mappings.get(key).get(attributes)));

                boundStatements.add(unsetNullValues(psInsert.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public <T extends IdentifiableAttributes> Optional<Resource<T>> getEquipment(UUID networkUuid, int variantNum, String equipmentId,
                                                                                 Map<String, Mapping> mappings, String tableName,
                                                                                 Resource.Builder<T> builder,
                                                                                 Class<T> classz) {
        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(mappings.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(equipmentId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                T attributes = classz.getDeclaredConstructor().newInstance();
                mappings.entrySet().forEach(entry -> entry.getValue().set(attributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(builder
                    .id(equipmentId)
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return Optional.empty();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getEquipments(UUID networkUuid, int variantNum,
                                                                              Map<String, Mapping> mappings, String tableName,
                                                                              Resource.Builder<T> builder,
                                                                              Class<T> classz) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = classz.getDeclaredConstructor().newInstance();

                mappings.entrySet().forEach(entry -> entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(builder
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Collections.emptyList();
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getContainerEquipments(UUID networkUuid, int variantNum, String containerId,
                                                                                       String containerColumnName,
                                                                                       Map<String, Mapping> mappings, String tableName,
                                                                                       Resource.Builder<T> builder,
                                                                                       Class<T> classz) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn(containerColumnName).isEqualTo(literal(containerId))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = classz.getDeclaredConstructor().newInstance();

                mappings.entrySet().forEach(entry -> entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(builder
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Collections.emptyList();
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getVoltageLevelEquipmentsWithSide(UUID networkUuid, int variantNum, String voltageLevelId,
                                                                                                  Branch.Side side,
                                                                                                  Map<String, Mapping> mappings, String tableName,
                                                                                                  Resource.Builder<T> builder,
                                                                                                  Class<T> classz) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId" + (side == Branch.Side.ONE ? 1 : 2)).isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = classz.getDeclaredConstructor().newInstance();

                mappings.entrySet().forEach(entry -> entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(builder
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Collections.emptyList();
        }
    }

    public <T extends IdentifiableAttributes & Contained> void updateEquipments(UUID networkUuid, List<Resource<T>> resources,
                                                                                Map<String, Mapping> mappings, PreparedStatement psUpdate,
                                                                                String columnToExclude) {
        Set<String> keys = mappings.keySet();

        for (List<Resource<T>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<T> resource : subresources) {
                T attributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keys.forEach(key -> {
                    if (!key.equals(columnToExclude)) {
                        values.add(mappings.get(key).get(attributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getContainerIds().stream().findAny().get());

                boundStatements.add(unsetNullValues(psUpdate.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public <T extends IdentifiableAttributes> void updateEquipments2(UUID networkUuid, List<Resource<T>> resources,
                                                                     Map<String, Mapping> mappings, PreparedStatement psUpdate) {
        Set<String> keys = mappings.keySet();

        for (List<Resource<T>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<T> resource : subresources) {
                T attributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keys.forEach(key -> values.add(mappings.get(key).get(attributes)));
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());

                boundStatements.add(unsetNullValues(psUpdate.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteEquipment(UUID networkUuid, int variantNum, String equipmentId, String tableName) {
        session.execute(deleteFrom(tableName)
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(equipmentId))
            .build());
    }

    // substation

    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getSubstationMappings(), SUBSTATION, Resource.substationBuilder(), SubstationAttributes.class);
    }

    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return getEquipment(networkUuid, variantNum, substationId, mappings.getSubstationMappings(),
            SUBSTATION, Resource.substationBuilder(), SubstationAttributes.class);
    }

    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getSubstationMappings(), psInsertSubstation);
    }

    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        updateEquipments2(networkUuid, resources, mappings.getSubstationMappings(), psUpdateSubstation);
    }

    public void deleteSubstation(UUID networkUuid, int variantNum, String substationId) {
        deleteEquipment(networkUuid, variantNum, substationId, SUBSTATION);
    }

    // voltage level

    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getVoltageLevelMappings(), psInsertVoltageLevel);
    }

    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getVoltageLevelMappings(), psUpdateVoltageLevel, "substationId");
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum, String substationId) {
        return getContainerEquipments(networkUuid, variantNum, substationId, "substationId", mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes.class);
    }

    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getEquipment(networkUuid, variantNum, voltageLevelId, mappings.getVoltageLevelMappings(),
            VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes.class);
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes.class);
    }

    public void deleteVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        deleteEquipment(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL);
    }

    // generator

    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getGeneratorMappings(), psInsertGenerator);
    }

    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return getEquipment(networkUuid, variantNum, generatorId, mappings.getGeneratorMappings(),
            GENERATOR, Resource.generatorBuilder(), GeneratorAttributes.class);
    }

    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getGeneratorMappings(), GENERATOR, Resource.generatorBuilder(), GeneratorAttributes.class);
    }

    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getGeneratorMappings(), GENERATOR, Resource.generatorBuilder(), GeneratorAttributes.class);
    }

    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getGeneratorMappings(), psUpdateGenerator, "voltageLevelId");
    }

    public void deleteGenerator(UUID networkUuid, int variantNum, String generatorId) {
        deleteEquipment(networkUuid, variantNum, generatorId, GENERATOR);
    }

    // battery

    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getBatteryMappings(), psInsertBattery);
    }

    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return getEquipment(networkUuid, variantNum, batteryId, mappings.getBatteryMappings(),
            BATTERY, Resource.batteryBuilder(), BatteryAttributes.class);
    }

    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getBatteryMappings(), BATTERY, Resource.batteryBuilder(), BatteryAttributes.class);
    }

    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getBatteryMappings(), BATTERY, Resource.batteryBuilder(), BatteryAttributes.class);
    }

    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getBatteryMappings(), psUpdateBattery, "voltageLevelId");
    }

    public void deleteBattery(UUID networkUuid, int variantNum, String batteryId) {
        deleteEquipment(networkUuid, variantNum, batteryId, BATTERY);
    }

    // load

    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getLoadMappings(), psInsertLoad);
    }

    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return getEquipment(networkUuid, variantNum, loadId, mappings.getLoadMappings(),
                           LOAD, Resource.loadBuilder(), LoadAttributes.class);
    }

    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getLoadMappings(), LOAD, Resource.loadBuilder(), LoadAttributes.class);
    }

    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getLoadMappings(), LOAD, Resource.loadBuilder(), LoadAttributes.class);
    }

    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getLoadMappings(), psUpdateLoad, "voltageLevelId");
    }

    public void deleteLoad(UUID networkUuid, int variantNum, String loadId) {
        deleteEquipment(networkUuid, variantNum, loadId, LOAD);
    }

    // shunt compensator

    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getShuntCompensatorMappings(), psInsertShuntCompensator);
    }

    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return getEquipment(networkUuid, variantNum, shuntCompensatorId, mappings.getShuntCompensatorMappings(),
                           SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes.class);
    }

    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes.class);
    }

    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes.class);
    }

    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getShuntCompensatorMappings(), psUpdateShuntCompensator, "voltageLevelId");
    }

    public void deleteShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        deleteEquipment(networkUuid, variantNum, shuntCompensatorId, SHUNT_COMPENSATOR);
    }

    // VSC converter station

    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getVscConverterStationMappings(), psInsertVscConverterStation);
    }

    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return getEquipment(networkUuid, variantNum, vscConverterStationId, mappings.getVscConverterStationMappings(),
                           VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes.class);
    }

    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes.class);
    }

    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes.class);
    }

    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getVscConverterStationMappings(), psUpdateVscConverterStation, "voltageLevelId");
    }

    public void deleteVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        deleteEquipment(networkUuid, variantNum, vscConverterStationId, VSC_CONVERTER_STATION);
    }

    // LCC converter station

    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getLccConverterStationMappings(), psInsertLccConverterStation);
    }

    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return getEquipment(networkUuid, variantNum, lccConverterStationId, mappings.getLccConverterStationMappings(),
                           LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes.class);
    }

    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes.class);
    }

    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes.class);
    }

    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getLccConverterStationMappings(), psUpdateLccConverterStation, "voltageLevelId");
    }

    public void deleteLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        deleteEquipment(networkUuid, variantNum, lccConverterStationId, LCC_CONVERTER_STATION);
    }

    // static var compensators

    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getStaticVarCompensatorMappings(), psInsertStaticVarCompensator);
    }

    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return getEquipment(networkUuid, variantNum, staticVarCompensatorId, mappings.getStaticVarCompensatorMappings(),
            STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes.class);
    }

    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes.class);
    }

    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes.class);
    }

    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getStaticVarCompensatorMappings(), psUpdateStaticVarCompensator, "voltageLevelId");
    }

    public void deleteStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        deleteEquipment(networkUuid, variantNum, staticVarCompensatorId, STATIC_VAR_COMPENSATOR);
    }

    // busbar section

    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getBusbarSectionMappings(), psInsertBusbarSection);
    }

    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getBusbarSectionMappings(), psUpdateBusbarSection, "voltageLevelId");
    }

    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return getEquipment(networkUuid, variantNum, busbarSectionId, mappings.getBusbarSectionMappings(),
            BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes.class);
    }

    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getBusbarSectionMappings(), BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes.class);
    }

    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getBusbarSectionMappings(), BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes.class);
    }

    public void deleteBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        deleteEquipment(networkUuid, variantNum, busBarSectionId, BUSBAR_SECTION);
    }

    // switch

    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getSwitchMappings(), psInsertSwitch);
    }

    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return getEquipment(networkUuid, variantNum, switchId, mappings.getSwitchMappings(),
            SWITCH, Resource.switchBuilder(), SwitchAttributes.class);
    }

    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getSwitchMappings(), SWITCH, Resource.switchBuilder(), SwitchAttributes.class);
    }

    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getSwitchMappings(), SWITCH, Resource.switchBuilder(), SwitchAttributes.class);
    }

    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getSwitchMappings(), psUpdateSwitch, "voltageLevelId");
    }

    public void deleteSwitch(UUID networkUuid, int variantNum, String switchId) {
        deleteEquipment(networkUuid, variantNum, switchId, SWITCH);
    }

    // 2 windings transformer

    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getTwoWindingsTransformerMappings(), psInsertTwoWindingsTransformer);
    }

    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return getEquipment(networkUuid, variantNum, twoWindingsTransformerId, mappings.getTwoWindingsTransformerMappings(),
            TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes.class);
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes.class);
    }

    private List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, Branch.Side side, String voltageLevelId) {
        return getVoltageLevelEquipmentsWithSide(networkUuid, variantNum, voltageLevelId, side, mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes.class);
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return ImmutableList.<Resource<TwoWindingsTransformerAttributes>>builder().addAll(
                ImmutableSet.<Resource<TwoWindingsTransformerAttributes>>builder()
                        .addAll(getVoltageLevelTwoWindingsTransformers(networkUuid, variantNum, Branch.Side.ONE, voltageLevelId))
                        .addAll(getVoltageLevelTwoWindingsTransformers(networkUuid, variantNum, Branch.Side.TWO, voltageLevelId))
                        .build())
                .build();
    }

    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        updateEquipments2(networkUuid, resources, mappings.getTwoWindingsTransformerMappings(), psUpdateTwoWindingsTransformer);
    }

    public void deleteTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        deleteEquipment(networkUuid, variantNum, twoWindingsTransformerId, TWO_WINDINGS_TRANSFORMER);
    }

    // 3 windings transformer

    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getThreeWindingsTransformerMappings(), psInsertThreeWindingsTransformer);
    }

    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        Map<String, Mapping> threeWindingsTransformerMappings = mappings.getThreeWindingsTransformerMappings();
        try (ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
            .columns(threeWindingsTransformerMappings.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(threeWindingsTransformerId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());

                threeWindingsTransformerMappings.entrySet().forEach(entry -> entry.getValue().set(threeWindingsTransformerAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.threeWindingsTransformerBuilder()
                    .id(threeWindingsTransformerId)
                    .variantNum(variantNum)
                    .attributes(threeWindingsTransformerAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        Map<String, Mapping> threeWindingsTransformerMappings = mappings.getThreeWindingsTransformerMappings();
        Set<String> columns = new HashSet<>(threeWindingsTransformerMappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<ThreeWindingsTransformerAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());

                threeWindingsTransformerMappings.entrySet().forEach(entry -> entry.getValue().set(threeWindingsTransformerAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.threeWindingsTransformerBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(threeWindingsTransformerAttributes)
                    .build());
            }
            return resources;
        }
    }

    private List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, ThreeWindingsTransformer.Side side, String voltageLevelId) {
        Map<String, Mapping> threeWindingsTransformerMappings = mappings.getThreeWindingsTransformerMappings();
        Set<String> columns = new HashSet<>(threeWindingsTransformerMappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId" + (side == ThreeWindingsTransformer.Side.ONE ? 1 : (side == ThreeWindingsTransformer.Side.TWO ? 2 : 3))).isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<ThreeWindingsTransformerAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());

                threeWindingsTransformerMappings.entrySet().forEach(entry -> entry.getValue().set(threeWindingsTransformerAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.threeWindingsTransformerBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(threeWindingsTransformerAttributes)
                    .build());
            }
            return resources;
        }
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return ImmutableList.<Resource<ThreeWindingsTransformerAttributes>>builder().addAll(
                ImmutableSet.<Resource<ThreeWindingsTransformerAttributes>>builder()
                        .addAll(getVoltageLevelThreeWindingsTransformers(networkUuid, variantNum, ThreeWindingsTransformer.Side.ONE, voltageLevelId))
                        .addAll(getVoltageLevelThreeWindingsTransformers(networkUuid, variantNum, ThreeWindingsTransformer.Side.TWO, voltageLevelId))
                        .addAll(getVoltageLevelThreeWindingsTransformers(networkUuid, variantNum, ThreeWindingsTransformer.Side.THREE, voltageLevelId))
                        .build())
                .build();
    }

    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources) {
        updateEquipments2(networkUuid, resources, mappings.getThreeWindingsTransformerMappings(), psUpdateThreeWindingsTransformer);
    }

    public void deleteThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        deleteEquipment(networkUuid, variantNum, threeWindingsTransformerId, THREE_WINDINGS_TRANSFORMER);
    }

    // line

    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getLineMappings(), psInsertLine);
    }

    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return getEquipment(networkUuid, variantNum, lineId, mappings.getLineMappings(),
            LINE, Resource.lineBuilder(), LineAttributes.class);
    }

    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getLineMappings(), LINE, Resource.lineBuilder(), LineAttributes.class);
    }

    private List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, Branch.Side side, String voltageLevelId) {
        return getVoltageLevelEquipmentsWithSide(networkUuid, variantNum, voltageLevelId, side, mappings.getLineMappings(), LINE, Resource.lineBuilder(), LineAttributes.class);
    }

    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return ImmutableList.<Resource<LineAttributes>>builder().addAll(
                ImmutableSet.<Resource<LineAttributes>>builder()
                        .addAll(getVoltageLevelLines(networkUuid, variantNum, Branch.Side.ONE, voltageLevelId))
                        .addAll(getVoltageLevelLines(networkUuid, variantNum, Branch.Side.TWO, voltageLevelId))
                        .build())
                .build();
    }

    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        updateEquipments2(networkUuid, resources, mappings.getLineMappings(), psUpdateLines);
    }

    public void deleteLine(UUID networkUuid, int variantNum, String lineId) {
        deleteEquipment(networkUuid, variantNum, lineId, LINE);
    }

    // Hvdc line

    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getHvdcLineMappings(), HVDC_LINE, Resource.hvdcLineBuilder(), HvdcLineAttributes.class);
    }

    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return getEquipment(networkUuid, variantNum, hvdcLineId, mappings.getHvdcLineMappings(),
                           HVDC_LINE, Resource.hvdcLineBuilder(), HvdcLineAttributes.class);
    }

    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getHvdcLineMappings(), psInsertHvdcLine);
    }

    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        updateEquipments2(networkUuid, resources, mappings.getHvdcLineMappings(), psUpdateHvdcLine);
    }

    public void deleteHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        deleteEquipment(networkUuid, variantNum, hvdcLineId, HVDC_LINE);
    }

    // Dangling line

    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getDanglingLineMappings(), DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes.class);
    }

    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return getEquipment(networkUuid, variantNum, danglingLineId, mappings.getDanglingLineMappings(),
                           DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes.class);
    }

    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getDanglingLineMappings(), DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes.class);
    }

    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getDanglingLineMappings(), psInsertDanglingLine);
    }

    public void deleteDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        deleteEquipment(networkUuid, variantNum, danglingLineId, DANGLING_LINE);
    }

    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getDanglingLineMappings(), psUpdateDanglingLine, "voltageLevelId");
    }

    // configured buses

    public void createBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getConfiguredBusMappings(), psInsertConfiguredBus);
    }

    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return getEquipment(networkUuid, variantNum, busId, mappings.getConfiguredBusMappings(),
                           CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes.class);
    }

    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getConfiguredBusMappings(), CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes.class);
    }

    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getConfiguredBusMappings(), CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes.class);
    }

    public void updateBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getConfiguredBusMappings(), psUpdateConfiguredBus, "voltageLevelId");
    }

    public void deleteBus(UUID networkUuid, int variantNum, String configuredBusId) {
        deleteEquipment(networkUuid, variantNum, configuredBusId, CONFIGURED_BUS);
    }
}
