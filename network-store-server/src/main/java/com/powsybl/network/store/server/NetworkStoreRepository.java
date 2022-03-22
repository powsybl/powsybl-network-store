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

        psInsertThreeWindingsTransformer = session.prepare(insertInto(THREE_WINDINGS_TRANSFORMER)
                .value("networkUuid", bindMarker())
                .value(VARIANT_NUM, bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId1", bindMarker())
                .value("voltageLevelId2", bindMarker())
                .value("voltageLevelId3", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node1", bindMarker())
                .value("node2", bindMarker())
                .value("node3", bindMarker())
                .value("ratedU0", bindMarker())
                .value("p1", bindMarker())
                .value("q1", bindMarker())
                .value("r1", bindMarker())
                .value("x1", bindMarker())
                .value("g1", bindMarker())
                .value("b1", bindMarker())
                .value("ratedU1", bindMarker())
                .value("ratedS1", bindMarker())
                .value("phaseTapChanger1", bindMarker())
                .value("ratioTapChanger1", bindMarker())
                .value("p2", bindMarker())
                .value("q2", bindMarker())
                .value("r2", bindMarker())
                .value("x2", bindMarker())
                .value("g2", bindMarker())
                .value("b2", bindMarker())
                .value("ratedU2", bindMarker())
                .value("ratedS2", bindMarker())
                .value("phaseTapChanger2", bindMarker())
                .value("ratioTapChanger2", bindMarker())
                .value("p3", bindMarker())
                .value("q3", bindMarker())
                .value("r3", bindMarker())
                .value("x3", bindMarker())
                .value("g3", bindMarker())
                .value("b3", bindMarker())
                .value("ratedU3", bindMarker())
                .value("ratedS3", bindMarker())
                .value("phaseTapChanger3", bindMarker())
                .value("ratioTapChanger3", bindMarker())
                .value("position1", bindMarker())
                .value("position2", bindMarker())
                .value("position3", bindMarker())
                .value(CURRENT_LIMITS1, bindMarker())
                .value(CURRENT_LIMITS2, bindMarker())
                .value(CURRENT_LIMITS3, bindMarker())
                .value("bus1", bindMarker())
                .value("connectableBus1", bindMarker())
                .value("bus2", bindMarker())
                .value("connectableBus2", bindMarker())
                .value("bus3", bindMarker())
                .value("connectableBus3", bindMarker())
                .value(PHASE_ANGLE_CLOCK, bindMarker())
                .value(ACTIVE_POWER_LIMITS1, bindMarker())
                .value(ACTIVE_POWER_LIMITS2, bindMarker())
                .value(ACTIVE_POWER_LIMITS3, bindMarker())
                .value(APPARENT_POWER_LIMITS1, bindMarker())
                .value(APPARENT_POWER_LIMITS2, bindMarker())
                .value(APPARENT_POWER_LIMITS3, bindMarker())
                .value(BRANCH_STATUS, bindMarker())
                .value(CGMES_TAP_CHANGERS, bindMarker())
                .build());
        insertPreparedStatements.put(THREE_WINDINGS_TRANSFORMER, psInsertThreeWindingsTransformer);
        psCloneThreeWindingsTransformerSupplier = () -> {
            try {
                return session.conn.prepareStatement(
    "insert into " + THREE_WINDINGS_TRANSFORMER + "(" +

                    VARIANT_NUM + "," +
                    "networkUuid" + "," +
                    "id" + "," +
                    "voltageLevelId1" + "," +
                    "voltageLevelId2" + "," +
                    "voltageLevelId3" + "," +
                    "name" + "," +
                    "fictitious" + "," +
                    "properties" + "," +
                    ALIASES_WITHOUT_TYPE + "," +
                    ALIAS_BY_TYPE + "," +
                    "node1" + "," +
                    "node2" + "," +
                    "node3" + "," +
                    "ratedU0" + "," +
                    "p1" + "," +
                    "q1" + "," +
                    "r1" + "," +
                    "x1" + "," +
                    "g1" + "," +
                    "b1" + "," +
                    "ratedU1" + "," +
                    "ratedS1" + "," +
                    "phaseTapChanger1" + "," +
                    "ratioTapChanger1" + "," +
                    "p2" + "," +
                    "q2" + "," +
                    "r2" + "," +
                    "x2" + "," +
                    "g2" + "," +
                    "b2" + "," +
                    "ratedU2" + "," +
                    "ratedS2" + "," +
                    "phaseTapChanger2" + "," +
                    "ratioTapChanger2" + "," +
                    "p3" + "," +
                    "q3" + "," +
                    "r3" + "," +
                    "x3" + "," +
                    "g3" + "," +
                    "b3" + "," +
                    "ratedU3" + "," +
                    "ratedS3" + "," +
                    "phaseTapChanger3" + "," +
                    "ratioTapChanger3" + "," +
                    "position1" + "," +
                    "position2" + "," +
                    "position3" + "," +
                    CURRENT_LIMITS1 + "," +
                    CURRENT_LIMITS2 + "," +
                    CURRENT_LIMITS3 + "," +
                    "bus1" + "," +
                    "connectableBus1" + "," +
                    "bus2" + "," +
                    "connectableBus2" + "," +
                    "bus3" + "," +
                    "connectableBus3" + "," +
                    PHASE_ANGLE_CLOCK + "," +
                    ACTIVE_POWER_LIMITS1 + "," +
                    ACTIVE_POWER_LIMITS2 + "," +
                    ACTIVE_POWER_LIMITS3 + "," +
                    APPARENT_POWER_LIMITS1 + "," +
                    APPARENT_POWER_LIMITS2 + "," +
                    APPARENT_POWER_LIMITS3 + "," +
                    BRANCH_STATUS + "," +
                    CGMES_TAP_CHANGERS + ")" +
            "select " +
                    "?" + "," +
                    "networkUuid" + "," +
                    "id" + "," +
                    "voltageLevelId1" + "," +
                    "voltageLevelId2" + "," +
                    "voltageLevelId3" + "," +
                    "name" + "," +
                    "fictitious" + "," +
                    "properties" + "," +
                    ALIASES_WITHOUT_TYPE + "," +
                    ALIAS_BY_TYPE + "," +
                    "node1" + "," +
                    "node2" + "," +
                    "node3" + "," +
                    "ratedU0" + "," +
                    "p1" + "," +
                    "q1" + "," +
                    "r1" + "," +
                    "x1" + "," +
                    "g1" + "," +
                    "b1" + "," +
                    "ratedU1" + "," +
                    "ratedS1" + "," +
                    "phaseTapChanger1" + "," +
                    "ratioTapChanger1" + "," +
                    "p2" + "," +
                    "q2" + "," +
                    "r2" + "," +
                    "x2" + "," +
                    "g2" + "," +
                    "b2" + "," +
                    "ratedU2" + "," +
                    "ratedS2" + "," +
                    "phaseTapChanger2" + "," +
                    "ratioTapChanger2" + "," +
                    "p3" + "," +
                    "q3" + "," +
                    "r3" + "," +
                    "x3" + "," +
                    "g3" + "," +
                    "b3" + "," +
                    "ratedU3" + "," +
                    "ratedS3" + "," +
                    "phaseTapChanger3" + "," +
                    "ratioTapChanger3" + "," +
                    "position1" + "," +
                    "position2" + "," +
                    "position3" + "," +
                    CURRENT_LIMITS1 + "," +
                    CURRENT_LIMITS2 + "," +
                    CURRENT_LIMITS3 + "," +
                    "bus1" + "," +
                    "connectableBus1" + "," +
                    "bus2" + "," +
                    "connectableBus2" + "," +
                    "bus3" + "," +
                    "connectableBus3" + "," +
                    PHASE_ANGLE_CLOCK + "," +
                    ACTIVE_POWER_LIMITS1 + "," +
                    ACTIVE_POWER_LIMITS2 + "," +
                    ACTIVE_POWER_LIMITS3 + "," +
                    APPARENT_POWER_LIMITS1 + "," +
                    APPARENT_POWER_LIMITS2 + "," +
                    APPARENT_POWER_LIMITS3 + "," +
                    BRANCH_STATUS + "," +
                    CGMES_TAP_CHANGERS + " " +
                "from " + THREE_WINDINGS_TRANSFORMER + " " +
                    "where networkUuid = ? and variantNum = ?"
                        );
            } catch (SQLException e) {
                throw new PowsyblException(e);
            }
        };
        clonePreparedStatementsSupplier.put(THREE_WINDINGS_TRANSFORMER, psCloneThreeWindingsTransformerSupplier);

        psUpdateThreeWindingsTransformer = session.prepare(update(THREE_WINDINGS_TRANSFORMER)
                .set(Assignment.setColumn("voltageLevelId1", bindMarker()))
                .set(Assignment.setColumn("voltageLevelId2", bindMarker()))
                .set(Assignment.setColumn("voltageLevelId3", bindMarker()))
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node1", bindMarker()))
                .set(Assignment.setColumn("node2", bindMarker()))
                .set(Assignment.setColumn("node3", bindMarker()))
                .set(Assignment.setColumn("ratedU0", bindMarker()))
                .set(Assignment.setColumn("p1", bindMarker()))
                .set(Assignment.setColumn("q1", bindMarker()))
                .set(Assignment.setColumn("r1", bindMarker()))
                .set(Assignment.setColumn("x1", bindMarker()))
                .set(Assignment.setColumn("g1", bindMarker()))
                .set(Assignment.setColumn("b1", bindMarker()))
                .set(Assignment.setColumn("ratedU1", bindMarker()))
                .set(Assignment.setColumn("ratedS1", bindMarker()))
                .set(Assignment.setColumn("phaseTapChanger1", bindMarker()))
                .set(Assignment.setColumn("ratioTapChanger1", bindMarker()))
                .set(Assignment.setColumn("p2", bindMarker()))
                .set(Assignment.setColumn("q2", bindMarker()))
                .set(Assignment.setColumn("r2", bindMarker()))
                .set(Assignment.setColumn("x2", bindMarker()))
                .set(Assignment.setColumn("g2", bindMarker()))
                .set(Assignment.setColumn("b2", bindMarker()))
                .set(Assignment.setColumn("ratedU2", bindMarker()))
                .set(Assignment.setColumn("ratedS2", bindMarker()))
                .set(Assignment.setColumn("phaseTapChanger2", bindMarker()))
                .set(Assignment.setColumn("ratioTapChanger2", bindMarker()))
                .set(Assignment.setColumn("p3", bindMarker()))
                .set(Assignment.setColumn("q3", bindMarker()))
                .set(Assignment.setColumn("r3", bindMarker()))
                .set(Assignment.setColumn("x3", bindMarker()))
                .set(Assignment.setColumn("g3", bindMarker()))
                .set(Assignment.setColumn("b3", bindMarker()))
                .set(Assignment.setColumn("ratedU3", bindMarker()))
                .set(Assignment.setColumn("ratedS3", bindMarker()))
                .set(Assignment.setColumn("phaseTapChanger3", bindMarker()))
                .set(Assignment.setColumn("ratioTapChanger3", bindMarker()))
                .set(Assignment.setColumn("position1", bindMarker()))
                .set(Assignment.setColumn("position2", bindMarker()))
                .set(Assignment.setColumn("position3", bindMarker()))
                .set(Assignment.setColumn(CURRENT_LIMITS1, bindMarker()))
                .set(Assignment.setColumn(CURRENT_LIMITS2, bindMarker()))
                .set(Assignment.setColumn(CURRENT_LIMITS3, bindMarker()))
                .set(Assignment.setColumn("bus1", bindMarker()))
                .set(Assignment.setColumn("connectableBus1", bindMarker()))
                .set(Assignment.setColumn("bus2", bindMarker()))
                .set(Assignment.setColumn("connectableBus2", bindMarker()))
                .set(Assignment.setColumn("bus3", bindMarker()))
                .set(Assignment.setColumn("connectableBus3", bindMarker()))
                .set(Assignment.setColumn(PHASE_ANGLE_CLOCK, bindMarker()))
                .set(Assignment.setColumn(ACTIVE_POWER_LIMITS1, bindMarker()))
                .set(Assignment.setColumn(ACTIVE_POWER_LIMITS2, bindMarker()))
                .set(Assignment.setColumn(ACTIVE_POWER_LIMITS3, bindMarker()))
                .set(Assignment.setColumn(APPARENT_POWER_LIMITS1, bindMarker()))
                .set(Assignment.setColumn(APPARENT_POWER_LIMITS2, bindMarker()))
                .set(Assignment.setColumn(APPARENT_POWER_LIMITS3, bindMarker()))
                .set(Assignment.setColumn(BRANCH_STATUS, bindMarker()))
                .set(Assignment.setColumn(CGMES_TAP_CHANGERS, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .build());

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
                    if (!entry.getKey().equals("caseDate")) {  // TODO : find a better way to deal with DateTime field ???
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
                    if (!key.equals("caseDate")) {  // TODO : find a better way to deal with DateTime field ???
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
                        if (!key.equals("caseDate")) {  // TODO : find a better way to deal with DateTime field ???
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
        } catch (InstantiationException e) {
            return Optional.empty();
        } catch (IllegalAccessException e) {
            return Optional.empty();
        } catch (InvocationTargetException e) {
            return Optional.empty();
        } catch (NoSuchMethodException e) {
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
        } catch (InvocationTargetException e) {
            return Collections.emptyList();
        } catch (InstantiationException e) {
            return Collections.emptyList();
        } catch (IllegalAccessException e) {
            return Collections.emptyList();
        } catch (NoSuchMethodException e) {
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
        } catch (InvocationTargetException e) {
            return Collections.emptyList();
        } catch (InstantiationException e) {
            return Collections.emptyList();
        } catch (IllegalAccessException e) {
            return Collections.emptyList();
        } catch (NoSuchMethodException e) {
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
        } catch (InvocationTargetException e) {
            return Collections.emptyList();
        } catch (InstantiationException e) {
            return Collections.emptyList();
        } catch (IllegalAccessException e) {
            return Collections.emptyList();
        } catch (NoSuchMethodException e) {
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
        for (List<Resource<ThreeWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<ThreeWindingsTransformerAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psInsertThreeWindingsTransformer.bind(
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getLeg1().getVoltageLevelId(),
                        resource.getAttributes().getLeg2().getVoltageLevelId(),
                        resource.getAttributes().getLeg3().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getLeg1().getNode(),
                        resource.getAttributes().getLeg2().getNode(),
                        resource.getAttributes().getLeg3().getNode(),
                        resource.getAttributes().getRatedU0(),
                        resource.getAttributes().getP1(),
                        resource.getAttributes().getQ1(),
                        resource.getAttributes().getLeg1().getR(),
                        resource.getAttributes().getLeg1().getX(),
                        resource.getAttributes().getLeg1().getG(),
                        resource.getAttributes().getLeg1().getB(),
                        resource.getAttributes().getLeg1().getRatedU(),
                        resource.getAttributes().getLeg1().getRatedS(),
                        resource.getAttributes().getLeg1().getPhaseTapChangerAttributes(),
                        resource.getAttributes().getLeg1().getRatioTapChangerAttributes(),
                        resource.getAttributes().getP2(),
                        resource.getAttributes().getQ2(),
                        resource.getAttributes().getLeg2().getR(),
                        resource.getAttributes().getLeg2().getX(),
                        resource.getAttributes().getLeg2().getG(),
                        resource.getAttributes().getLeg2().getB(),
                        resource.getAttributes().getLeg2().getRatedU(),
                        resource.getAttributes().getLeg2().getRatedS(),
                        resource.getAttributes().getLeg2().getPhaseTapChangerAttributes(),
                        resource.getAttributes().getLeg2().getRatioTapChangerAttributes(),
                        resource.getAttributes().getP3(),
                        resource.getAttributes().getQ3(),
                        resource.getAttributes().getLeg3().getR(),
                        resource.getAttributes().getLeg3().getX(),
                        resource.getAttributes().getLeg3().getG(),
                        resource.getAttributes().getLeg3().getB(),
                        resource.getAttributes().getLeg3().getRatedU(),
                        resource.getAttributes().getLeg3().getRatedS(),
                        resource.getAttributes().getLeg3().getPhaseTapChangerAttributes(),
                        resource.getAttributes().getLeg3().getRatioTapChangerAttributes(),
                        resource.getAttributes().getPosition1(),
                        resource.getAttributes().getPosition2(),
                        resource.getAttributes().getPosition3(),
                        resource.getAttributes().getLeg1().getCurrentLimitsAttributes(),
                        resource.getAttributes().getLeg2().getCurrentLimitsAttributes(),
                        resource.getAttributes().getLeg3().getCurrentLimitsAttributes(),
                        emptyStringForNullValue(resource.getAttributes().getLeg1().getBus()),
                        resource.getAttributes().getLeg1().getConnectableBus(),
                        emptyStringForNullValue(resource.getAttributes().getLeg2().getBus()),
                        resource.getAttributes().getLeg2().getConnectableBus(),
                        emptyStringForNullValue(resource.getAttributes().getLeg3().getBus()),
                        resource.getAttributes().getLeg3().getConnectableBus(),
                        resource.getAttributes().getPhaseAngleClock(),
                        resource.getAttributes().getLeg1().getActivePowerLimitsAttributes(),
                        resource.getAttributes().getLeg2().getActivePowerLimitsAttributes(),
                        resource.getAttributes().getLeg3().getActivePowerLimitsAttributes(),
                        resource.getAttributes().getLeg1().getApparentPowerLimitsAttributes(),
                        resource.getAttributes().getLeg2().getApparentPowerLimitsAttributes(),
                        resource.getAttributes().getLeg3().getApparentPowerLimitsAttributes(),
                        resource.getAttributes().getBranchStatus(),
                        resource.getAttributes().getCgmesTapChangerAttributesList()
                )));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        try (ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
                .columns(
                        "name",
                        "properties",
                        "ratedU0",
                        "voltageLevelId1",
                        "node1",
                        "r1",
                        "x1",
                        "g1",
                        "b1",
                        "ratedU1",
                        "p1",
                        "q1",
                        "phaseTapChanger1",
                        "ratioTapChanger1",
                        "voltageLevelId2",
                        "node2",
                        "r2",
                        "x2",
                        "g2",
                        "b2",
                        "ratedU2",
                        "p2",
                        "q2",
                        "phaseTapChanger2",
                        "ratioTapChanger2",
                        "voltageLevelId3",
                        "node3",
                        "r3",
                        "x3",
                        "g3",
                        "b3",
                        "ratedU3",
                        "p3",
                        "q3",
                        "phaseTapChanger3",
                        "ratioTapChanger3",
                        "position1",
                        "position2",
                        "position3",
                        "currentLimits1",
                        "currentLimits2",
                        "currentLimits3",
                        "bus1",
                        "connectableBus1",
                        "bus2",
                        "connectableBus2",
                        "bus3",
                        "connectableBus3",
                        "ratedS1",
                        "ratedS2",
                        "ratedS3",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "phaseAngleClock",
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        ACTIVE_POWER_LIMITS3,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS3,
                        BRANCH_STATUS,
                        CGMES_TAP_CHANGERS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(threeWindingsTransformerId))
                .build())) {
            Row one = resultSet.one();
            if (one != null) {
                return Optional.of(Resource.threeWindingsTransformerBuilder()
                        .id(threeWindingsTransformerId)
                        .variantNum(variantNum)
                        .attributes(ThreeWindingsTransformerAttributes.builder()
                                .name(one.getString(0))
                                .properties(one.getMap(1, String.class, String.class))
                                .ratedU0(one.getDouble(2))
                                .leg1(LegAttributes.builder()
                                        .legNumber(1)
                                        .voltageLevelId(one.getString(3))
                                        .node(one.get(4, Integer.class))
                                        .r(one.getDouble(5))
                                        .x(one.getDouble(6))
                                        .g(one.getDouble(7))
                                        .b(one.getDouble(8))
                                        .ratedU(one.getDouble(9))
                                        .ratedS(one.getDouble(48))
                                        .phaseTapChangerAttributes(one.get(12, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(one.get(13, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(one.get(39, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(one.getString(42)))
                                        .connectableBus(one.getString(43))
                                        .activePowerLimitsAttributes(one.get(55, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(one.get(58, LimitsAttributes.class))
                                        .build())
                                .p1(one.getDouble(10))
                                .q1(one.getDouble(11))
                                .leg2(LegAttributes.builder()
                                        .legNumber(2)
                                        .voltageLevelId(one.getString(14))
                                        .node(one.get(15, Integer.class))
                                        .r(one.getDouble(16))
                                        .x(one.getDouble(17))
                                        .g(one.getDouble(18))
                                        .b(one.getDouble(19))
                                        .ratedU(one.getDouble(20))
                                        .ratedS(one.getDouble(49))
                                        .phaseTapChangerAttributes(one.get(23, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(one.get(24, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(one.get(40, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(one.getString(44)))
                                        .connectableBus(one.getString(45))
                                        .activePowerLimitsAttributes(one.get(56, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(one.get(59, LimitsAttributes.class))
                                        .build())
                                .p2(one.getDouble(21))
                                .q2(one.getDouble(22))
                                .leg3(LegAttributes.builder()
                                        .legNumber(3)
                                        .voltageLevelId(one.getString(25))
                                        .node(one.get(26, Integer.class))
                                        .r(one.getDouble(27))
                                        .x(one.getDouble(28))
                                        .g(one.getDouble(29))
                                        .b(one.getDouble(30))
                                        .ratedU(one.getDouble(31))
                                        .ratedS(one.getDouble(50))
                                        .phaseTapChangerAttributes(one.get(34, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(one.get(35, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(one.get(41, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(one.getString(46)))
                                        .connectableBus(one.getString(47))
                                        .activePowerLimitsAttributes(one.get(57, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(one.get(60, LimitsAttributes.class))
                                        .build())
                                .p3(one.getDouble(32))
                                .q3(one.getDouble(33))
                                .position1(one.get(36, ConnectablePositionAttributes.class))
                                .position2(one.get(37, ConnectablePositionAttributes.class))
                                .position3(one.get(38, ConnectablePositionAttributes.class))
                                .fictitious(one.getBoolean(51))
                                .aliasesWithoutType(one.getSet(52, String.class))
                                .aliasByType(one.getMap(53, String.class, String.class))
                                .phaseAngleClock(one.get(54, ThreeWindingsTransformerPhaseAngleClockAttributes.class))
                                .branchStatus(one.getString(61))
                                .cgmesTapChangerAttributesList(one.getList(62, CgmesTapChangerAttributes.class))
                                .build())
                        .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        try (ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
                .columns(
                        "id",
                        "name",
                        "properties",
                        "ratedU0",
                        "voltageLevelId1",
                        "node1",
                        "r1",
                        "x1",
                        "g1",
                        "b1",
                        "ratedU1",
                        "p1",
                        "q1",
                        "phaseTapChanger1",
                        "ratioTapChanger1",
                        "voltageLevelId2",
                        "node2",
                        "r2",
                        "x2",
                        "g2",
                        "b2",
                        "ratedU2",
                        "p2",
                        "q2",
                        "phaseTapChanger2",
                        "ratioTapChanger2",
                        "voltageLevelId3",
                        "node3",
                        "r3",
                        "x3",
                        "g3",
                        "b3",
                        "ratedU3",
                        "p3",
                        "q3",
                        "phaseTapChanger3",
                        "ratioTapChanger3",
                        "position1",
                        "position2",
                        "position3",
                        "currentLimits1",
                        "currentLimits2",
                        "currentLimits3",
                        "bus1",
                        "connectableBus1",
                        "bus2",
                        "connectableBus2",
                        "bus3",
                        "connectableBus3",
                        "ratedS1",
                        "ratedS2",
                        "ratedS3",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "phaseAngleClock",
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        ACTIVE_POWER_LIMITS3,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS3,
                        BRANCH_STATUS,
                        CGMES_TAP_CHANGERS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build())) {
            List<Resource<ThreeWindingsTransformerAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.threeWindingsTransformerBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(ThreeWindingsTransformerAttributes.builder()
                                .name(row.getString(1))
                                .properties(row.getMap(2, String.class, String.class))
                                .ratedU0(row.getDouble(3))
                                .leg1(LegAttributes.builder()
                                        .legNumber(1)
                                        .voltageLevelId(row.getString(4))
                                        .node(row.get(5, Integer.class))
                                        .r(row.getDouble(6))
                                        .x(row.getDouble(7))
                                        .g(row.getDouble(8))
                                        .b(row.getDouble(9))
                                        .ratedU(row.getDouble(10))
                                        .ratedS(row.getDouble(49))
                                        .phaseTapChangerAttributes(row.get(13, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(row.get(14, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(row.get(40, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(row.getString(43)))
                                        .connectableBus(row.getString(44))
                                        .activePowerLimitsAttributes(row.get(56, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(row.get(59, LimitsAttributes.class))
                                        .build())
                                .p1(row.getDouble(11))
                                .q1(row.getDouble(12))
                                .leg2(LegAttributes.builder()
                                        .legNumber(2)
                                        .voltageLevelId(row.getString(15))
                                        .node(row.get(16, Integer.class))
                                        .r(row.getDouble(17))
                                        .x(row.getDouble(18))
                                        .g(row.getDouble(19))
                                        .b(row.getDouble(20))
                                        .ratedU(row.getDouble(21))
                                        .ratedS(row.getDouble(50))
                                        .phaseTapChangerAttributes(row.get(24, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(row.get(25, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(row.get(41, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(row.getString(45)))
                                        .connectableBus(row.getString(46))
                                        .activePowerLimitsAttributes(row.get(57, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(row.get(60, LimitsAttributes.class))
                                        .build())
                                .p2(row.getDouble(22))
                                .q2(row.getDouble(23))
                                .leg3(LegAttributes.builder()
                                        .legNumber(3)
                                        .voltageLevelId(row.getString(26))
                                        .node(row.get(27, Integer.class))
                                        .r(row.getDouble(28))
                                        .x(row.getDouble(29))
                                        .g(row.getDouble(30))
                                        .b(row.getDouble(31))
                                        .ratedU(row.getDouble(32))
                                        .ratedS(row.getDouble(51))
                                        .phaseTapChangerAttributes(row.get(35, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(row.get(36, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(row.get(42, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(row.getString(47)))
                                        .connectableBus(row.getString(48))
                                        .activePowerLimitsAttributes(row.get(58, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(row.get(61, LimitsAttributes.class))
                                        .build())
                                .p3(row.getDouble(33))
                                .q3(row.getDouble(34))
                                .position1(row.get(37, ConnectablePositionAttributes.class))
                                .position2(row.get(38, ConnectablePositionAttributes.class))
                                .position3(row.get(39, ConnectablePositionAttributes.class))
                                .fictitious(row.getBoolean(52))
                                .aliasesWithoutType(row.getSet(53, String.class))
                                .aliasByType(row.getMap(54, String.class, String.class))
                                .phaseAngleClock(row.get(55, ThreeWindingsTransformerPhaseAngleClockAttributes.class))
                                .branchStatus(row.getString(62))
                                .cgmesTapChangerAttributesList(row.getList(63, CgmesTapChangerAttributes.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    private List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, ThreeWindingsTransformer.Side side, String voltageLevelId) {
        try (ResultSet resultSet = session.execute(selectFrom("threeWindingsTransformerByVoltageLevel" + (side == ThreeWindingsTransformer.Side.ONE ? 1 : (side == ThreeWindingsTransformer.Side.TWO ? 2 : 3)))
                .columns(
                        "id",
                        "voltageLevelId" + (side == ThreeWindingsTransformer.Side.ONE ? 2 : 1),
                        "voltageLevelId" + (side == ThreeWindingsTransformer.Side.ONE ? 3 : (side == ThreeWindingsTransformer.Side.TWO ? 3 : 2)),
                        "name",
                        "properties",
                        "ratedU0",
                        "node1",
                        "r1",
                        "x1",
                        "g1",
                        "b1",
                        "ratedU1",
                        "p1",
                        "q1",
                        "phaseTapChanger1",
                        "ratioTapChanger1",
                        "node2",
                        "r2",
                        "x2",
                        "g2",
                        "b2",
                        "ratedU2",
                        "p2",
                        "q2",
                        "phaseTapChanger2",
                        "ratioTapChanger2",
                        "node3",
                        "r3",
                        "x3",
                        "g3",
                        "b3",
                        "ratedU3",
                        "p3",
                        "q3",
                        "phaseTapChanger3",
                        "ratioTapChanger3",
                        "position1",
                        "position2",
                        "position3",
                        "currentLimits1",
                        "currentLimits2",
                        "currentLimits3",
                        "bus1",
                        "connectableBus1",
                        "bus2",
                        "connectableBus2",
                        "bus3",
                        "connectableBus3",
                        "ratedS1",
                        "ratedS2",
                        "ratedS3",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "phaseAngleClock",
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        ACTIVE_POWER_LIMITS3,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS3,
                        BRANCH_STATUS,
                        CGMES_TAP_CHANGERS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("voltageLevelId" + (side == ThreeWindingsTransformer.Side.ONE ? 1 : (side == ThreeWindingsTransformer.Side.TWO ? 2 : 3))).isEqualTo(literal(voltageLevelId))
                .build())) {
            List<Resource<ThreeWindingsTransformerAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.threeWindingsTransformerBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(ThreeWindingsTransformerAttributes.builder()
                                .name(row.getString(3))
                                .properties(row.getMap(4, String.class, String.class))
                                .ratedU0(row.getDouble(5))
                                .leg1(LegAttributes.builder()
                                        .legNumber(1)
                                        .voltageLevelId(side == ThreeWindingsTransformer.Side.ONE ? voltageLevelId : row.getString(1))
                                        .node(row.get(6, Integer.class))
                                        .r(row.getDouble(7))
                                        .x(row.getDouble(8))
                                        .g(row.getDouble(9))
                                        .b(row.getDouble(10))
                                        .ratedU(row.getDouble(11))
                                        .ratedS(row.getDouble(48))
                                        .phaseTapChangerAttributes(row.get(14, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(row.get(15, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(row.get(39, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(row.getString(42)))
                                        .connectableBus(row.getString(43))
                                        .activePowerLimitsAttributes(row.get(55, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(row.get(58, LimitsAttributes.class))
                                        .build())
                                .p1(row.getDouble(12))
                                .q1(row.getDouble(13))
                                .leg2(LegAttributes.builder()
                                        .legNumber(2)
                                        .voltageLevelId(side == ThreeWindingsTransformer.Side.TWO ? voltageLevelId : (side == ThreeWindingsTransformer.Side.ONE ? row.getString(1) : row.getString(2)))
                                        .node(row.get(16, Integer.class))
                                        .r(row.getDouble(17))
                                        .x(row.getDouble(18))
                                        .g(row.getDouble(19))
                                        .b(row.getDouble(20))
                                        .ratedU(row.getDouble(21))
                                        .ratedS(row.getDouble(49))
                                        .phaseTapChangerAttributes(row.get(24, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(row.get(25, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(row.get(40, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(row.getString(44)))
                                        .connectableBus(row.getString(45))
                                        .activePowerLimitsAttributes(row.get(56, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(row.get(59, LimitsAttributes.class))
                                        .build())
                                .p2(row.getDouble(22))
                                .q2(row.getDouble(23))
                                .leg3(LegAttributes.builder()
                                        .legNumber(3)
                                        .voltageLevelId(side == ThreeWindingsTransformer.Side.THREE ? voltageLevelId : row.getString(2))
                                        .node(row.get(26, Integer.class))
                                        .r(row.getDouble(27))
                                        .x(row.getDouble(28))
                                        .g(row.getDouble(29))
                                        .b(row.getDouble(30))
                                        .ratedU(row.getDouble(31))
                                        .ratedS(row.getDouble(50))
                                        .phaseTapChangerAttributes(row.get(34, PhaseTapChangerAttributes.class))
                                        .ratioTapChangerAttributes(row.get(35, RatioTapChangerAttributes.class))
                                        .currentLimitsAttributes(row.get(41, LimitsAttributes.class))
                                        .bus(nullValueForEmptyString(row.getString(46)))
                                        .connectableBus(row.getString(47))
                                        .activePowerLimitsAttributes(row.get(57, LimitsAttributes.class))
                                        .apparentPowerLimitsAttributes(row.get(60, LimitsAttributes.class))
                                        .build())
                                .p3(row.getDouble(32))
                                .q3(row.getDouble(33))
                                .position1(row.get(36, ConnectablePositionAttributes.class))
                                .position2(row.get(37, ConnectablePositionAttributes.class))
                                .position3(row.get(38, ConnectablePositionAttributes.class))
                                .fictitious(row.getBoolean(51))
                                .aliasesWithoutType(row.getSet(52, String.class))
                                .aliasByType(row.getMap(53, String.class, String.class))
                                .phaseAngleClock(row.get(54, ThreeWindingsTransformerPhaseAngleClockAttributes.class))
                                .branchStatus(row.getString(61))
                                .cgmesTapChangerAttributesList(row.getList(62, CgmesTapChangerAttributes.class))
                                .build())
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
        for (List<Resource<ThreeWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<ThreeWindingsTransformerAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psUpdateThreeWindingsTransformer.bind(
                        resource.getAttributes().getLeg1().getVoltageLevelId(),
                        resource.getAttributes().getLeg2().getVoltageLevelId(),
                        resource.getAttributes().getLeg3().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getLeg1().getNode(),
                        resource.getAttributes().getLeg2().getNode(),
                        resource.getAttributes().getLeg3().getNode(),
                        resource.getAttributes().getRatedU0(),
                        resource.getAttributes().getP1(),
                        resource.getAttributes().getQ1(),
                        resource.getAttributes().getLeg1().getR(),
                        resource.getAttributes().getLeg1().getX(),
                        resource.getAttributes().getLeg1().getG(),
                        resource.getAttributes().getLeg1().getB(),
                        resource.getAttributes().getLeg1().getRatedU(),
                        resource.getAttributes().getLeg1().getRatedS(),
                        resource.getAttributes().getLeg1().getPhaseTapChangerAttributes(),
                        resource.getAttributes().getLeg1().getRatioTapChangerAttributes(),
                        resource.getAttributes().getP2(),
                        resource.getAttributes().getQ2(),
                        resource.getAttributes().getLeg2().getR(),
                        resource.getAttributes().getLeg2().getX(),
                        resource.getAttributes().getLeg2().getG(),
                        resource.getAttributes().getLeg2().getB(),
                        resource.getAttributes().getLeg2().getRatedU(),
                        resource.getAttributes().getLeg2().getRatedS(),
                        resource.getAttributes().getLeg2().getPhaseTapChangerAttributes(),
                        resource.getAttributes().getLeg2().getRatioTapChangerAttributes(),
                        resource.getAttributes().getP3(),
                        resource.getAttributes().getQ3(),
                        resource.getAttributes().getLeg3().getR(),
                        resource.getAttributes().getLeg3().getX(),
                        resource.getAttributes().getLeg3().getG(),
                        resource.getAttributes().getLeg3().getB(),
                        resource.getAttributes().getLeg3().getRatedU(),
                        resource.getAttributes().getLeg3().getRatedS(),
                        resource.getAttributes().getLeg3().getPhaseTapChangerAttributes(),
                        resource.getAttributes().getLeg3().getRatioTapChangerAttributes(),
                        resource.getAttributes().getPosition1(),
                        resource.getAttributes().getPosition2(),
                        resource.getAttributes().getPosition3(),
                        resource.getAttributes().getLeg1().getCurrentLimitsAttributes(),
                        resource.getAttributes().getLeg2().getCurrentLimitsAttributes(),
                        resource.getAttributes().getLeg3().getCurrentLimitsAttributes(),
                        resource.getAttributes().getLeg1().getBus(),
                        resource.getAttributes().getLeg1().getConnectableBus(),
                        resource.getAttributes().getLeg2().getBus(),
                        resource.getAttributes().getLeg2().getConnectableBus(),
                        resource.getAttributes().getLeg3().getBus(),
                        resource.getAttributes().getLeg3().getConnectableBus(),
                        resource.getAttributes().getPhaseAngleClock(),
                        resource.getAttributes().getLeg1().getActivePowerLimitsAttributes(),
                        resource.getAttributes().getLeg2().getActivePowerLimitsAttributes(),
                        resource.getAttributes().getLeg3().getActivePowerLimitsAttributes(),
                        resource.getAttributes().getLeg1().getApparentPowerLimitsAttributes(),
                        resource.getAttributes().getLeg2().getApparentPowerLimitsAttributes(),
                        resource.getAttributes().getLeg3().getApparentPowerLimitsAttributes(),
                        resource.getAttributes().getBranchStatus(),
                        resource.getAttributes().getCgmesTapChangerAttributesList(),
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId())
                ));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
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
