/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import java.util.function.Supplier;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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
            throw new RuntimeException(e);
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
                throw new RuntimeException(e);
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
        clonePreparedStatementsSupplier.put(SHUNT_COMPENSATOR, psCloneSubstationSupplier);

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

        psInsertVscConverterStation = session.prepare(insertInto(VSC_CONVERTER_STATION)
                .value("networkUuid", bindMarker())
                .value(VARIANT_NUM, bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value("lossFactor", bindMarker())
                .value("voltageRegulatorOn", bindMarker())
                .value("reactivePowerSetPoint", bindMarker())
                .value("voltageSetPoint", bindMarker())
                .value("minMaxReactiveLimits", bindMarker())
                .value("reactiveCapabilityCurve", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker())
                .value("bus", bindMarker())
                .value(CONNECTABLE_BUS, bindMarker())
                .build());
        insertPreparedStatements.put(VSC_CONVERTER_STATION, psInsertVscConverterStation);
        psCloneVscConverterStationSupplier = () -> {
            try {
                return session.conn.prepareStatement(
    "insert into " + VSC_CONVERTER_STATION + "(" +

                    VARIANT_NUM + ", " +
                    "networkUuid" + ", " +
                    "id" + ", " +
                    "voltageLevelId" + ", " +
                    "name" + ", " +
                    "fictitious" + ", " +
                    "properties" + ", " +
                    ALIASES_WITHOUT_TYPE + ", " +
                    ALIAS_BY_TYPE + ", " +
                    "node" + ", " +
                    "lossFactor" + ", " +
                    "voltageRegulatorOn" + ", " +
                    "reactivePowerSetPoint" + ", " +
                    "voltageSetPoint" + ", " +
                    "minMaxReactiveLimits" + ", " +
                    "reactiveCapabilityCurve" + ", " +
                    "p" + ", " +
                    "q" + ", " +
                    "position" + ", " +
                    "bus" + ", " +
                    CONNECTABLE_BUS + ") " +

                    "select " +

                    "?" + ", " +
                    "networkUuid" + ", " +
                    "id" + ", " +
                    "voltageLevelId" + ", " +
                    "name" + ", " +
                    "fictitious" + ", " +
                    "properties" + ", " +
                    ALIASES_WITHOUT_TYPE + ", " +
                    ALIAS_BY_TYPE + ", " +
                    "node" + ", " +
                    "lossFactor" + ", " +
                    "voltageRegulatorOn" + ", " +
                    "reactivePowerSetPoint" + ", " +
                    "voltageSetPoint" + ", " +
                    "minMaxReactiveLimits" + ", " +
                    "reactiveCapabilityCurve" + ", " +
                    "p" + ", " +
                    "q" + ", " +
                    "position" + ", " +
                    "bus" + ", " +
                    CONNECTABLE_BUS + " " +
                    "from " + VSC_CONVERTER_STATION + " " +
                    "where networkUuid = ? and variantNum = ?"
                        );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(VSC_CONVERTER_STATION, psCloneVscConverterStationSupplier);

        psUpdateVscConverterStation = session.prepare(update(VSC_CONVERTER_STATION)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn("lossFactor", bindMarker()))
                .set(Assignment.setColumn("voltageRegulatorOn", bindMarker()))
                .set(Assignment.setColumn("reactivePowerSetPoint", bindMarker()))
                .set(Assignment.setColumn("voltageSetPoint", bindMarker()))
                .set(Assignment.setColumn("minMaxReactiveLimits", bindMarker()))
                .set(Assignment.setColumn("reactiveCapabilityCurve", bindMarker()))
                .set(Assignment.setColumn("p", bindMarker()))
                .set(Assignment.setColumn("q", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .set(Assignment.setColumn("bus", bindMarker()))
                .set(Assignment.setColumn(CONNECTABLE_BUS, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        // lcc converter station

        psInsertLccConverterStation = session.prepare(insertInto(LCC_CONVERTER_STATION)
                .value("networkUuid", bindMarker())
                .value(VARIANT_NUM, bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value("powerFactor", bindMarker())
                .value("lossFactor", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker())
                .value("bus", bindMarker())
                .value(CONNECTABLE_BUS, bindMarker())
                .build());
        insertPreparedStatements.put(LCC_CONVERTER_STATION, psInsertLccConverterStation);
        psCloneLccConverterStationSupplier = () -> {
            try {
                return session.conn.prepareStatement(
    "insert into " + LCC_CONVERTER_STATION + "(" +

                    VARIANT_NUM + ", " +
                    "networkUuid" + ", " +
                    "id" + ", " +
                    "voltageLevelId" + ", " +
                    "name" + ", " +
                    "fictitious" + ", " +
                    "properties" + ", " +
                    ALIASES_WITHOUT_TYPE + ", " +
                    ALIAS_BY_TYPE + ", " +
                    "node" + ", " +
                    "powerFactor" + ", " +
                    "lossFactor" + ", " +
                    "p" + ", " +
                    "q" + ", " +
                    "position" + ", " +
                    "bus" + ", " +
                    CONNECTABLE_BUS + ") " +
               "select " +
                    "?" + ", " +
                    "networkUuid" + ", " +
                    "id" + ", " +
                    "voltageLevelId" + ", " +
                    "name" + ", " +
                    "fictitious" + ", " +
                    "properties" + ", " +
                    ALIASES_WITHOUT_TYPE + ", " +
                    ALIAS_BY_TYPE + ", " +
                    "node" + ", " +
                    "powerFactor" + ", " +
                    "lossFactor" + ", " +
                    "p" + ", " +
                    "q" + ", " +
                    "position" + ", " +
                    "bus" + ", " +
                    CONNECTABLE_BUS + " " +
                "from " + LCC_CONVERTER_STATION + " " +
                "where networkUuid = ? and variantNum = ?"
                        );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(LCC_CONVERTER_STATION, psCloneLccConverterStationSupplier);

        psUpdateLccConverterStation = session.prepare(update(LCC_CONVERTER_STATION)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn("powerFactor", bindMarker()))
                .set(Assignment.setColumn("lossFactor", bindMarker()))
                .set(Assignment.setColumn("p", bindMarker()))
                .set(Assignment.setColumn("q", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .set(Assignment.setColumn("bus", bindMarker()))
                .set(Assignment.setColumn(CONNECTABLE_BUS, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        // static var compensator

        psInsertStaticVarCompensator = session.prepare(insertInto(STATIC_VAR_COMPENSATOR)
                .value("networkUuid", bindMarker())
                .value(VARIANT_NUM, bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value("bMin", bindMarker())
                .value("bMax", bindMarker())
                .value("voltageSetPoint", bindMarker())
                .value("reactivePowerSetPoint", bindMarker())
                .value("regulationMode", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker())
                .value("bus", bindMarker())
                .value(CONNECTABLE_BUS, bindMarker())
                .value(REGULATING_TERMINAL, bindMarker())
                .value("voltagePerReactivePowerControl", bindMarker())
                .build());
        insertPreparedStatements.put(STATIC_VAR_COMPENSATOR, psInsertStaticVarCompensator);
        psCloneStaticVarCompensatorSupplier = () -> {
            try {
                return session.conn.prepareStatement(
    "insert into " + STATIC_VAR_COMPENSATOR + "(" +

                    VARIANT_NUM + ", " +
                    "networkUuid" + ", " +
                    "id" + ", " +
                    "voltageLevelId" + ", " +
                    "name" + ", " +
                    "fictitious" + ", " +
                    "properties" + ", " +
                    ALIASES_WITHOUT_TYPE + ", " +
                    ALIAS_BY_TYPE + ", " +
                    "node" + ", " +
                    "bMin" + ", " +
                    "bMax" + ", " +
                    "voltageSetPoint" + ", " +
                    "reactivePowerSetPoint" + ", " +
                    "regulationMode" + ", " +
                    "p" + ", " +
                    "q" + ", " +
                    "position" + ", " +
                    "bus" + ", " +
                    CONNECTABLE_BUS + ", " +
                    REGULATING_TERMINAL + ", " +
                    "voltagePerReactivePowerControl" + ") " +
                "select " +
                    "?" + ", " +
                    "networkUuid" + ", " +
                    "id" + ", " +
                    "voltageLevelId" + ", " +
                    "name" + ", " +
                    "fictitious" + ", " +
                    "properties" + ", " +
                    ALIASES_WITHOUT_TYPE + ", " +
                    ALIAS_BY_TYPE + ", " +
                    "node" + ", " +
                    "bMin" + ", " +
                    "bMax" + ", " +
                    "voltageSetPoint" + ", " +
                    "reactivePowerSetPoint" + ", " +
                    "regulationMode" + ", " +
                    "p" + ", " +
                    "q" + ", " +
                    "position" + ", " +
                    "bus" + ", " +
                    CONNECTABLE_BUS + ", " +
                    REGULATING_TERMINAL + ", " +
                    "voltagePerReactivePowerControl" + " " +
                "from " + STATIC_VAR_COMPENSATOR + " " +
                    "where networkUuid = ? and variantNum = ?"
                        );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(STATIC_VAR_COMPENSATOR, psCloneStaticVarCompensatorSupplier);

        psUpdateStaticVarCompensator = session.prepare(update(STATIC_VAR_COMPENSATOR)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn("bMin", bindMarker()))
                .set(Assignment.setColumn("bMax", bindMarker()))
                .set(Assignment.setColumn("voltageSetPoint", bindMarker()))
                .set(Assignment.setColumn("reactivePowerSetPoint", bindMarker()))
                .set(Assignment.setColumn("regulationMode", bindMarker()))
                .set(Assignment.setColumn("p", bindMarker()))
                .set(Assignment.setColumn("q", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .set(Assignment.setColumn("bus", bindMarker()))
                .set(Assignment.setColumn(CONNECTABLE_BUS, bindMarker()))
                .set(Assignment.setColumn(REGULATING_TERMINAL, bindMarker()))
                .set(Assignment.setColumn("voltagePerReactivePowerControl", bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

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

        psInsertTwoWindingsTransformer = session.prepare(insertInto(TWO_WINDINGS_TRANSFORMER)
                .value("networkUuid", bindMarker())
                .value(VARIANT_NUM, bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId1", bindMarker())
                .value("voltageLevelId2", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node1", bindMarker())
                .value("node2", bindMarker())
                .value("r", bindMarker())
                .value("x", bindMarker())
                .value("g", bindMarker())
                .value("b", bindMarker())
                .value("ratedU1", bindMarker())
                .value("ratedU2", bindMarker())
                .value("ratedS", bindMarker())
                .value("p1", bindMarker())
                .value("q1", bindMarker())
                .value("p2", bindMarker())
                .value("q2", bindMarker())
                .value("position1", bindMarker())
                .value("position2", bindMarker())
                .value("phaseTapChanger", bindMarker())
                .value("ratioTapChanger", bindMarker())
                .value("bus1", bindMarker())
                .value("bus2", bindMarker())
                .value("connectableBus1", bindMarker())
                .value("connectableBus2", bindMarker())
                .value(CURRENT_LIMITS1, bindMarker())
                .value(CURRENT_LIMITS2, bindMarker())
                .value(PHASE_ANGLE_CLOCK, bindMarker())
                .value(ACTIVE_POWER_LIMITS1, bindMarker())
                .value(ACTIVE_POWER_LIMITS2, bindMarker())
                .value(APPARENT_POWER_LIMITS1, bindMarker())
                .value(APPARENT_POWER_LIMITS2, bindMarker())
                .value(BRANCH_STATUS, bindMarker())
                .value(CGMES_TAP_CHANGERS, bindMarker())
                .build());
        insertPreparedStatements.put(TWO_WINDINGS_TRANSFORMER, psInsertTwoWindingsTransformer);
        psCloneTwoWindingsTransformerSupplier = () -> {
            try {
                return session.conn.prepareStatement(
    "insert into " + TWO_WINDINGS_TRANSFORMER + "(" +

                    VARIANT_NUM + ", " +
                    "networkUuid" + ", " +
                    "id" + ", " +
                    "voltageLevelId1" + ", " +
                    "voltageLevelId2" + ", " +
                    "name" + ", " +
                    "fictitious" + ", " +
                    "properties" + ", " +
                    ALIASES_WITHOUT_TYPE + ", " +
                    ALIAS_BY_TYPE + ", " +
                    "node1" + ", " +
                    "node2" + ", " +
                    "r" + ", " +
                    "x" + ", " +
                    "g" + ", " +
                    "b" + ", " +
                    "ratedU1" + ", " +
                    "ratedU2" + ", " +
                    "ratedS" + ", " +
                    "p1" + ", " +
                    "q1" + ", " +
                    "p2" + ", " +
                    "q2" + ", " +
                    "position1" + ", " +
                    "position2" + ", " +
                    "phaseTapChanger" + ", " +
                    "ratioTapChanger" + ", " +
                    "bus1" + ", " +
                    "bus2" + ", " +
                    "connectableBus1" + ", " +
                    "connectableBus2" + ", " +
                    CURRENT_LIMITS1 + ", " +
                    CURRENT_LIMITS2 + ", " +
                    PHASE_ANGLE_CLOCK + ", " +
                    ACTIVE_POWER_LIMITS1 + ", " +
                    ACTIVE_POWER_LIMITS2 + ", " +
                    APPARENT_POWER_LIMITS1 + ", " +
                    APPARENT_POWER_LIMITS2 + ", " +
                    BRANCH_STATUS + ", " +
                    CGMES_TAP_CHANGERS + ") " +
                "select " +
                    "?" + ", " +
                    "networkUuid" + ", " +
                    "id" + ", " +
                    "voltageLevelId1" + ", " +
                    "voltageLevelId2" + ", " +
                    "name" + ", " +
                    "fictitious" + ", " +
                    "properties" + ", " +
                    ALIASES_WITHOUT_TYPE + ", " +
                    ALIAS_BY_TYPE + ", " +
                    "node1" + ", " +
                    "node2" + ", " +
                    "r" + ", " +
                    "x" + ", " +
                    "g" + ", " +
                    "b" + ", " +
                    "ratedU1" + ", " +
                    "ratedU2" + ", " +
                    "ratedS" + ", " +
                    "p1" + ", " +
                    "q1" + ", " +
                    "p2" + ", " +
                    "q2" + ", " +
                    "position1" + ", " +
                    "position2" + ", " +
                    "phaseTapChanger" + ", " +
                    "ratioTapChanger" + ", " +
                    "bus1" + ", " +
                    "bus2" + ", " +
                    "connectableBus1" + ", " +
                    "connectableBus2" + ", " +
                    CURRENT_LIMITS1 + ", " +
                    CURRENT_LIMITS2 + ", " +
                    PHASE_ANGLE_CLOCK + ", " +
                    ACTIVE_POWER_LIMITS1 + ", " +
                    ACTIVE_POWER_LIMITS2 + ", " +
                    APPARENT_POWER_LIMITS1 + ", " +
                    APPARENT_POWER_LIMITS2 + ", " +
                    BRANCH_STATUS + ", " +
                    CGMES_TAP_CHANGERS + " " +
                    "from " + TWO_WINDINGS_TRANSFORMER + " " +
                    "where networkUuid = ? and variantNum = ?"
                        );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(TWO_WINDINGS_TRANSFORMER, psCloneTwoWindingsTransformerSupplier);

        psUpdateTwoWindingsTransformer = session.prepare(update(TWO_WINDINGS_TRANSFORMER)
                .set(Assignment.setColumn("voltageLevelId1", bindMarker()))
                .set(Assignment.setColumn("voltageLevelId2", bindMarker()))
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node1", bindMarker()))
                .set(Assignment.setColumn("node2", bindMarker()))
                .set(Assignment.setColumn("r", bindMarker()))
                .set(Assignment.setColumn("x", bindMarker()))
                .set(Assignment.setColumn("g", bindMarker()))
                .set(Assignment.setColumn("b", bindMarker()))
                .set(Assignment.setColumn("ratedU1", bindMarker()))
                .set(Assignment.setColumn("ratedU2", bindMarker()))
                .set(Assignment.setColumn("ratedS", bindMarker()))
                .set(Assignment.setColumn("p1", bindMarker()))
                .set(Assignment.setColumn("q1", bindMarker()))
                .set(Assignment.setColumn("p2", bindMarker()))
                .set(Assignment.setColumn("q2", bindMarker()))
                .set(Assignment.setColumn("position1", bindMarker()))
                .set(Assignment.setColumn("position2", bindMarker()))
                .set(Assignment.setColumn("phaseTapChanger", bindMarker()))
                .set(Assignment.setColumn("ratioTapChanger", bindMarker()))
                .set(Assignment.setColumn("bus1", bindMarker()))
                .set(Assignment.setColumn("bus2", bindMarker()))
                .set(Assignment.setColumn("connectableBus1", bindMarker()))
                .set(Assignment.setColumn("connectableBus2", bindMarker()))
                .set(Assignment.setColumn(CURRENT_LIMITS1, bindMarker()))
                .set(Assignment.setColumn(CURRENT_LIMITS2, bindMarker()))
                .set(Assignment.setColumn(PHASE_ANGLE_CLOCK, bindMarker()))
                .set(Assignment.setColumn(ACTIVE_POWER_LIMITS1, bindMarker()))
                .set(Assignment.setColumn(ACTIVE_POWER_LIMITS2, bindMarker()))
                .set(Assignment.setColumn(APPARENT_POWER_LIMITS1, bindMarker()))
                .set(Assignment.setColumn(APPARENT_POWER_LIMITS2, bindMarker()))
                .set(Assignment.setColumn(BRANCH_STATUS, bindMarker()))
                .set(Assignment.setColumn(CGMES_TAP_CHANGERS, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .build());

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
                throw new RuntimeException(e);
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

        psInsertHvdcLine = session.prepare(insertInto(HVDC_LINE)
                .value("networkUuid", bindMarker())
                .value(VARIANT_NUM, bindMarker())
                .value("id", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("r", bindMarker())
                .value("convertersMode", bindMarker())
                .value("nominalV", bindMarker())
                .value("activePowerSetpoint", bindMarker())
                .value("maxP", bindMarker())
                .value("converterStationId1", bindMarker())
                .value("converterStationId2", bindMarker())
                .value(HVDC_ANGLE_DROOP_ACTIVE_POWER_CONTROL, bindMarker())
                .value(HVDC_OPERATOR_ACTIVE_POWER_RANGE, bindMarker())
                .build());
        insertPreparedStatements.put(HVDC_LINE, psInsertHvdcLine);
        psCloneHvdcLineSupplier = () -> {
            try {
                return session.conn.prepareStatement(
    "insert into " + HVDC_LINE + "(" +

                    VARIANT_NUM + "," +
                    "networkUuid" + "," +
                    "id" + "," +
                    "name" + "," +
                    "fictitious" + "," +
                    "properties" + "," +
                    ALIASES_WITHOUT_TYPE + "," +
                    ALIAS_BY_TYPE + "," +
                    "r" + "," +
                    "convertersMode" + "," +
                    "nominalV" + "," +
                    "activePowerSetpoint" + "," +
                    "maxP" + "," +
                    "converterStationId1" + "," +
                    "converterStationId2" + "," +
                    HVDC_ANGLE_DROOP_ACTIVE_POWER_CONTROL + "," +
                    HVDC_OPERATOR_ACTIVE_POWER_RANGE + ")" +

                "select " +
                    "?" + "," +
                    "networkUuid" + "," +
                    "id" + "," +
                    "name" + "," +
                    "fictitious" + "," +
                    "properties" + "," +
                    ALIASES_WITHOUT_TYPE + "," +
                    ALIAS_BY_TYPE + "," +
                    "r" + "," +
                    "convertersMode" + "," +
                    "nominalV" + "," +
                    "activePowerSetpoint" + "," +
                    "maxP" + "," +
                    "converterStationId1" + "," +
                    "converterStationId2" + "," +
                    HVDC_ANGLE_DROOP_ACTIVE_POWER_CONTROL + "," +
                    HVDC_OPERATOR_ACTIVE_POWER_RANGE + " " +
                    "from " + HVDC_LINE + " " +
                    "where networkUuid = ? and variantNum = ?"
                        );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        clonePreparedStatementsSupplier.put(HVDC_LINE, psCloneHvdcLineSupplier);

        psUpdateHvdcLine = session.prepare(update(HVDC_LINE)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("r", bindMarker()))
                .set(Assignment.setColumn("convertersMode", bindMarker()))
                .set(Assignment.setColumn("nominalV", bindMarker()))
                .set(Assignment.setColumn("activePowerSetpoint", bindMarker()))
                .set(Assignment.setColumn("maxP", bindMarker()))
                .set(Assignment.setColumn("converterStationId1", bindMarker()))
                .set(Assignment.setColumn("converterStationId2", bindMarker()))
                .set(Assignment.setColumn(HVDC_ANGLE_DROOP_ACTIVE_POWER_CONTROL, bindMarker()))
                .set(Assignment.setColumn(HVDC_OPERATOR_ACTIVE_POWER_RANGE, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .build());

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
            throw new RuntimeException(e);
        }

        stopwatch.stop();
        LOGGER.info("Network clone done in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    // substation

    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingSubstations = mappings.getSubstationMappings();
        Set<String> columns = new HashSet<>(mappingSubstations.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(SUBSTATION)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<SubstationAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                SubstationAttributes substationAttributes = new SubstationAttributes();
                mappingSubstations.entrySet().forEach(entry -> entry.getValue().set(substationAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.substationBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(substationAttributes)
                    .build());
            }
            return resources;
        }
    }

    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        Map<String, Mapping> mappingSubstations = mappings.getSubstationMappings();
        try (ResultSet resultSet = session.execute(selectFrom(SUBSTATION)
            .columns(mappingSubstations.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(substationId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                SubstationAttributes substationAttributes = new SubstationAttributes();
                mappingSubstations.entrySet().forEach(entry -> entry.getValue().set(substationAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.substationBuilder()
                    .id(substationId)
                    .variantNum(variantNum)
                    .attributes(substationAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        Map<String, Mapping> substationMappings = mappings.getSubstationMappings();
        Set<String> keysSubstations = substationMappings.keySet();

        for (List<Resource<SubstationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<SubstationAttributes> resource : subresources) {
                SubstationAttributes substationAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysSubstations.forEach(key -> values.add(substationMappings.get(key).get(substationAttributes)));

                boundStatements.add(unsetNullValues(psInsertSubstation.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        Map<String, Mapping> substationMappings = mappings.getSubstationMappings();
        Set<String> keysSubstations = substationMappings.keySet();

        for (List<Resource<SubstationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<SubstationAttributes> resource : subresources) {
                SubstationAttributes substationAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysSubstations.forEach(key -> values.add(substationMappings.get(key).get(substationAttributes)));
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());

                boundStatements.add(unsetNullValues(psUpdateSubstation.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteSubstation(UUID networkUuid, int variantNum, String substationId) {
        session.execute(deleteFrom(SUBSTATION)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(substationId))
                .build());
    }

    // voltage level

    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        Map<String, Mapping> voltageLevelMappings = mappings.getVoltageLevelMappings();
        Set<String> keysVoltageLevels = voltageLevelMappings.keySet();

        for (List<Resource<VoltageLevelAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<VoltageLevelAttributes> resource : subresources) {
                VoltageLevelAttributes voltageLevelAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysVoltageLevels.forEach(key -> values.add(voltageLevelMappings.get(key).get(voltageLevelAttributes)));

                boundStatements.add(unsetNullValues(psInsertVoltageLevel.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        Map<String, Mapping> voltageLevelMappings = mappings.getVoltageLevelMappings();
        Set<String> keysVoltageLevels = voltageLevelMappings.keySet();

        for (List<Resource<VoltageLevelAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<VoltageLevelAttributes> resource : subresources) {
                VoltageLevelAttributes voltageLevelAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysVoltageLevels.forEach(key -> {
                    if (!key.equals("substationId")) {
                        values.add(voltageLevelMappings.get(key).get(voltageLevelAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getSubstationId());

                boundStatements.add(unsetNullValues(psUpdateVoltageLevel.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum, String substationId) {
        Map<String, Mapping> mappingVoltageLevels = mappings.getVoltageLevelMappings();
        Set<String> columns = new HashSet<>(mappingVoltageLevels.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(VOLTAGE_LEVEL)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("substationId").isEqualTo(literal(substationId))
            .build())) {
            List<Resource<VoltageLevelAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                VoltageLevelAttributes voltageLevelAttributes = new VoltageLevelAttributes();
                mappingVoltageLevels.entrySet().forEach(entry -> entry.getValue().set(voltageLevelAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.voltageLevelBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(voltageLevelAttributes)
                    .build());
            }
            return resources;
        }
    }

    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingVoltageLevels = mappings.getVoltageLevelMappings();
        try (ResultSet resultSet = session.execute(selectFrom(VOLTAGE_LEVEL)
            .columns(mappingVoltageLevels.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(voltageLevelId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                VoltageLevelAttributes voltageLevelAttributes = new VoltageLevelAttributes();
                mappingVoltageLevels.entrySet().forEach(entry -> entry.getValue().set(voltageLevelAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.voltageLevelBuilder()
                    .id(voltageLevelId)
                    .variantNum(variantNum)
                    .attributes(voltageLevelAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingVoltageLevels = mappings.getVoltageLevelMappings();
        Set<String> columns = new HashSet<>(mappingVoltageLevels.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(VOLTAGE_LEVEL)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<VoltageLevelAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                VoltageLevelAttributes voltageLevelAttributes = new VoltageLevelAttributes();
                mappingVoltageLevels.entrySet().forEach(entry -> entry.getValue().set(voltageLevelAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.voltageLevelBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(voltageLevelAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void deleteVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        session.execute(deleteFrom(VOLTAGE_LEVEL)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(voltageLevelId))
                .build());
    }

    // generator

    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        Map<String, Mapping> generatorMappings = mappings.getGeneratorMappings();
        Set<String> keysGenerators = generatorMappings.keySet();

        for (List<Resource<GeneratorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<GeneratorAttributes> resource : subresources) {
                GeneratorAttributes generatorAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysGenerators.forEach(key -> values.add(generatorMappings.get(key).get(generatorAttributes)));

                boundStatements.add(unsetNullValues(psInsertGenerator.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        Map<String, Mapping> mappingGenerators = mappings.getGeneratorMappings();
        try (ResultSet resultSet = session.execute(selectFrom(GENERATOR)
            .columns(mappingGenerators.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(generatorId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                GeneratorAttributes generatorAttributes = new GeneratorAttributes();
                mappingGenerators.entrySet().forEach(entry -> entry.getValue().set(generatorAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.generatorBuilder()
                    .id(generatorId)
                    .variantNum(variantNum)
                    .attributes(generatorAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingGenerators = mappings.getGeneratorMappings();
        Set<String> columns = new HashSet<>(mappingGenerators.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(GENERATOR)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<GeneratorAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                GeneratorAttributes generatorAttributes = new GeneratorAttributes();
                mappingGenerators.entrySet().forEach(entry -> entry.getValue().set(generatorAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.generatorBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(generatorAttributes)
                    .build());
            }
            return resources;
        }
    }

    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingGenerators = mappings.getGeneratorMappings();
        Set<String> columns = new HashSet<>(mappingGenerators.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(GENERATOR)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<GeneratorAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                GeneratorAttributes generatorAttributes = new GeneratorAttributes();
                mappingGenerators.entrySet().forEach(entry -> entry.getValue().set(generatorAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.generatorBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(generatorAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        Map<String, Mapping> generatorMappings = mappings.getGeneratorMappings();
        Set<String> keysGenerators = generatorMappings.keySet();

        for (List<Resource<GeneratorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<GeneratorAttributes> resource : subresources) {
                GeneratorAttributes generatorAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysGenerators.forEach(key -> {
                    if (!key.equals("voltageLevelId")) {
                        values.add(generatorMappings.get(key).get(generatorAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getVoltageLevelId());

                boundStatements.add(unsetNullValues(psUpdateGenerator.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteGenerator(UUID networkUuid, int variantNum, String generatorId) {
        session.execute(deleteFrom(GENERATOR)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(generatorId))
                .build());
    }

    // battery

    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        Map<String, Mapping> batteryMappings = mappings.getBatteryMappings();
        Set<String> keysBatteries = batteryMappings.keySet();

        for (List<Resource<BatteryAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<BatteryAttributes> resource : subresources) {
                BatteryAttributes batteryAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysBatteries.forEach(key -> values.add(batteryMappings.get(key).get(batteryAttributes)));

                boundStatements.add(unsetNullValues(psInsertBattery.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        Map<String, Mapping> mappingBatteries = mappings.getBatteryMappings();
        try (ResultSet resultSet = session.execute(selectFrom(BATTERY)
            .columns(mappingBatteries.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(batteryId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                BatteryAttributes batteryAttributes = new BatteryAttributes();
                mappingBatteries.entrySet().forEach(entry -> entry.getValue().set(batteryAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.batteryBuilder()
                    .id(batteryId)
                    .variantNum(variantNum)
                    .attributes(batteryAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingBatteries = mappings.getBatteryMappings();
        Set<String> columns = new HashSet<>(mappingBatteries.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(BATTERY)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<BatteryAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                BatteryAttributes batteryAttributes = new BatteryAttributes();
                mappingBatteries.entrySet().forEach(entry -> entry.getValue().set(batteryAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.batteryBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(batteryAttributes)
                    .build());
            }
            return resources;
        }
    }

    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingBatteries = mappings.getBatteryMappings();
        Set<String> columns = new HashSet<>(mappingBatteries.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(BATTERY)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<BatteryAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                BatteryAttributes batteryAttributes = new BatteryAttributes();
                mappingBatteries.entrySet().forEach(entry -> entry.getValue().set(batteryAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.batteryBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(batteryAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        Map<String, Mapping> batteryMappings = mappings.getBatteryMappings();
        Set<String> keysBatteries = batteryMappings.keySet();

        for (List<Resource<BatteryAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<BatteryAttributes> resource : subresources) {
                BatteryAttributes batteryAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysBatteries.forEach(key -> {
                    if (!key.equals("voltageLevelId")) {
                        values.add(batteryMappings.get(key).get(batteryAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getVoltageLevelId());

                boundStatements.add(unsetNullValues(psUpdateBattery.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteBattery(UUID networkUuid, int variantNum, String batteryId) {
        session.execute(deleteFrom(BATTERY)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(batteryId))
                .build());
    }

    // load

    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        Map<String, Mapping> loadMappings = mappings.getLoadMappings();
        Set<String> keysLoads = loadMappings.keySet();

        for (List<Resource<LoadAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<LoadAttributes> resource : subresources) {
                LoadAttributes loadAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysLoads.forEach(key -> values.add(loadMappings.get(key).get(loadAttributes)));

                boundStatements.add(unsetNullValues(psInsertLoad.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        Map<String, Mapping> mappingLoads = mappings.getLoadMappings();
        try (ResultSet resultSet = session.execute(selectFrom(LOAD)
            .columns(mappingLoads.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(loadId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                LoadAttributes loadAttributes = new LoadAttributes();
                mappingLoads.entrySet().forEach(entry -> entry.getValue().set(loadAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.loadBuilder()
                    .id(loadId)
                    .variantNum(variantNum)
                    .attributes(loadAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingLoads = mappings.getLoadMappings();
        Set<String> columns = new HashSet<>(mappingLoads.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(LOAD)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<LoadAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                LoadAttributes loadAttributes = new LoadAttributes();
                mappingLoads.entrySet().forEach(entry -> entry.getValue().set(loadAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.loadBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(loadAttributes)
                    .build());
            }
            return resources;
        }
    }

    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingLoads = mappings.getLoadMappings();
        Set<String> columns = new HashSet<>(mappingLoads.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(LOAD)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<LoadAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                LoadAttributes loadAttributes = new LoadAttributes();
                mappingLoads.entrySet().forEach(entry -> entry.getValue().set(loadAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.loadBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(loadAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        Map<String, Mapping> loadMappings = mappings.getLoadMappings();
        Set<String> keysLoads = loadMappings.keySet();

        for (List<Resource<LoadAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<LoadAttributes> resource : subresources) {
                LoadAttributes loadAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysLoads.forEach(key -> {
                    if (!key.equals("voltageLevelId")) {
                        values.add(loadMappings.get(key).get(loadAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getVoltageLevelId());

                boundStatements.add(unsetNullValues(psUpdateLoad.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteLoad(UUID networkUuid, int variantNum, String loadId) {
        session.execute(deleteFrom(LOAD)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(loadId))
                .build());
    }

    // shunt compensator

    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        Map<String, Mapping> shuntCompensatorMappings = mappings.getShuntCompensatorMappings();
        Set<String> keysShuntCompensators = shuntCompensatorMappings.keySet();

        for (List<Resource<ShuntCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<ShuntCompensatorAttributes> resource : subresources) {
                ShuntCompensatorAttributes shuntCompensatorAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysShuntCompensators.forEach(key -> values.add(shuntCompensatorMappings.get(key).get(shuntCompensatorAttributes)));

                boundStatements.add(unsetNullValues(psInsertShuntCompensator.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        Map<String, Mapping> mappingsShuntCompensators = mappings.getShuntCompensatorMappings();
        try (ResultSet resultSet = session.execute(selectFrom(SHUNT_COMPENSATOR)
            .columns(mappingsShuntCompensators.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(shuntCompensatorId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                ShuntCompensatorAttributes shuntCompensatorAttributes = new ShuntCompensatorAttributes();
                mappingsShuntCompensators.entrySet().forEach(entry -> entry.getValue().set(shuntCompensatorAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.shuntCompensatorBuilder()
                    .id(shuntCompensatorId)
                    .variantNum(variantNum)
                    .attributes(shuntCompensatorAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingsShuntCompensators = mappings.getShuntCompensatorMappings();
        Set<String> columns = new HashSet<>(mappingsShuntCompensators.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(SHUNT_COMPENSATOR)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<ShuntCompensatorAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                ShuntCompensatorAttributes shuntCompensatorAttributes = new ShuntCompensatorAttributes();
                mappingsShuntCompensators.entrySet().forEach(entry -> entry.getValue().set(shuntCompensatorAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.shuntCompensatorBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(shuntCompensatorAttributes)
                    .build());
            }
            return resources;
        }
    }

    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingsShuntCompensators = mappings.getShuntCompensatorMappings();
        Set<String> columns = new HashSet<>(mappingsShuntCompensators.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(SHUNT_COMPENSATOR)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<ShuntCompensatorAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                ShuntCompensatorAttributes shuntCompensatorAttributes = new ShuntCompensatorAttributes();
                mappingsShuntCompensators.entrySet().forEach(entry -> entry.getValue().set(shuntCompensatorAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.shuntCompensatorBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(shuntCompensatorAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        Map<String, Mapping> shuntCompensatorMappings = mappings.getShuntCompensatorMappings();
        Set<String> keysShuntCompensators = shuntCompensatorMappings.keySet();

        for (List<Resource<ShuntCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<ShuntCompensatorAttributes> resource : subresources) {
                ShuntCompensatorAttributes shuntCompensatorAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysShuntCompensators.forEach(key -> {
                    if (!key.equals("voltageLevelId")) {
                        values.add(shuntCompensatorMappings.get(key).get(shuntCompensatorAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getVoltageLevelId());

                boundStatements.add(unsetNullValues(psUpdateShuntCompensator.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        session.execute(deleteFrom(SHUNT_COMPENSATOR)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(shuntCompensatorId))
                .build());
    }

    // VSC converter station

    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        for (List<Resource<VscConverterStationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<VscConverterStationAttributes> resource : subresources) {
                ReactiveLimitsAttributes reactiveLimits = resource.getAttributes().getReactiveLimits();
                boundStatements.add(unsetNullValues(psInsertVscConverterStation.bind(
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getLossFactor(),
                        resource.getAttributes().getVoltageRegulatorOn(),
                        resource.getAttributes().getReactivePowerSetPoint(),
                        resource.getAttributes().getVoltageSetPoint(),
                        reactiveLimits != null && reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX ? reactiveLimits : null,
                        reactiveLimits != null && reactiveLimits.getKind() == ReactiveLimitsKind.CURVE ? reactiveLimits : null,
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus()
                )));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        try (ResultSet resultSet = session.execute(selectFrom(VSC_CONVERTER_STATION)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "lossFactor",
                        "voltageRegulatorOn",
                        "reactivePowerSetPoint",
                        "voltageSetPoint",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(vscConverterStationId))
                .build())) {
            Row row = resultSet.one();
            if (row != null) {
                MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(8, MinMaxReactiveLimitsAttributes.class);
                ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(9, ReactiveCapabilityCurveAttributes.class);
                return Optional.of(Resource.vscConverterStationBuilder()
                        .id(vscConverterStationId)
                        .variantNum(variantNum)
                        .attributes(VscConverterStationAttributes.builder()
                                .voltageLevelId(row.getString(0))
                                .name(row.getString(1))
                                .properties(row.getMap(2, String.class, String.class))
                                .node(row.get(3, Integer.class))
                                .lossFactor(row.getFloat(4))
                                .voltageRegulatorOn(row.getBoolean(5))
                                .reactivePowerSetPoint(row.getDouble(6))
                                .voltageSetPoint(row.getDouble(7))
                                .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                                .p(row.getDouble(10))
                                .q(row.getDouble(11))
                                .position(row.get(12, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(13)))
                                .connectableBus(row.getString(14))
                                .fictitious(row.getBoolean(15))
                                .aliasesWithoutType(row.getSet(16, String.class))
                                .aliasByType(row.getMap(17, String.class, String.class))
                                .build())
                        .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        try (ResultSet resultSet = session.execute(selectFrom(VSC_CONVERTER_STATION)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "lossFactor",
                        "voltageRegulatorOn",
                        "reactivePowerSetPoint",
                        "voltageSetPoint",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build())) {
            List<Resource<VscConverterStationAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(9, MinMaxReactiveLimitsAttributes.class);
                ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(10, ReactiveCapabilityCurveAttributes.class);
                resources.add(Resource.vscConverterStationBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(VscConverterStationAttributes.builder()
                                .voltageLevelId(row.getString(1))
                                .name(row.getString(2))
                                .properties(row.getMap(3, String.class, String.class))
                                .node(row.get(4, Integer.class))
                                .lossFactor(row.getFloat(5))
                                .voltageRegulatorOn(row.getBoolean(6))
                                .reactivePowerSetPoint(row.getDouble(7))
                                .voltageSetPoint(row.getDouble(8))
                                .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                                .p(row.getDouble(11))
                                .q(row.getDouble(12))
                                .position(row.get(13, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(14)))
                                .connectableBus(row.getString(15))
                                .fictitious(row.getBoolean(16))
                                .aliasesWithoutType(row.getSet(17, String.class))
                                .aliasByType(row.getMap(18, String.class, String.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        try (ResultSet resultSet = session.execute(selectFrom("vscConverterStationByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        "lossFactor",
                        "voltageRegulatorOn",
                        "reactivePowerSetPoint",
                        "voltageSetPoint",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build())) {
            List<Resource<VscConverterStationAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(8, MinMaxReactiveLimitsAttributes.class);
                ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(9, ReactiveCapabilityCurveAttributes.class);
                resources.add(Resource.vscConverterStationBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(VscConverterStationAttributes.builder()
                                .voltageLevelId(voltageLevelId)
                                .name(row.getString(1))
                                .properties(row.getMap(2, String.class, String.class))
                                .node(row.get(3, Integer.class))
                                .lossFactor(row.getFloat(4))
                                .voltageRegulatorOn(row.getBoolean(5))
                                .reactivePowerSetPoint(row.getDouble(6))
                                .voltageSetPoint(row.getDouble(7))
                                .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                                .p(row.getDouble(10))
                                .q(row.getDouble(11))
                                .position(row.get(12, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(13)))
                                .connectableBus(row.getString(14))
                                .fictitious(row.getBoolean(15))
                                .aliasesWithoutType(row.getSet(16, String.class))
                                .aliasByType(row.getMap(17, String.class, String.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        for (List<Resource<VscConverterStationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<VscConverterStationAttributes> resource : subresources) {
                ReactiveLimitsAttributes reactiveLimits = resource.getAttributes().getReactiveLimits();
                boundStatements.add(unsetNullValues(psUpdateVscConverterStation.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getLossFactor(),
                        resource.getAttributes().getVoltageRegulatorOn(),
                        resource.getAttributes().getReactivePowerSetPoint(),
                        resource.getAttributes().getVoltageSetPoint(),
                        reactiveLimits != null && reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX ? reactiveLimits : null,
                        reactiveLimits != null && reactiveLimits.getKind() == ReactiveLimitsKind.CURVE ? reactiveLimits : null,
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        session.execute(deleteFrom(VSC_CONVERTER_STATION)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(vscConverterStationId))
                .build());
    }

    // LCC converter station

    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        for (List<Resource<LccConverterStationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<LccConverterStationAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psInsertLccConverterStation.bind(
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getPowerFactor(),
                        resource.getAttributes().getLossFactor(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus()
                )));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        try (ResultSet resultSet = session.execute(selectFrom(LCC_CONVERTER_STATION)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "powerFactor",
                        "lossFactor",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(lccConverterStationId))
                .build())) {
            Row row = resultSet.one();
            if (row != null) {
                return Optional.of(Resource.lccConverterStationBuilder()
                        .id(lccConverterStationId)
                        .variantNum(variantNum)
                        .attributes(LccConverterStationAttributes.builder()
                                .voltageLevelId(row.getString(0))
                                .name(row.getString(1))
                                .properties(row.getMap(2, String.class, String.class))
                                .node(row.get(3, Integer.class))
                                .powerFactor(row.getFloat(4))
                                .lossFactor(row.getFloat(5))
                                .p(row.getDouble(6))
                                .q(row.getDouble(7))
                                .position(row.get(8, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(9)))
                                .connectableBus(row.getString(10))
                                .fictitious(row.getBoolean(11))
                                .aliasesWithoutType(row.getSet(12, String.class))
                                .aliasByType(row.getMap(13, String.class, String.class))
                                .build())
                        .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        try (ResultSet resultSet = session.execute(selectFrom(LCC_CONVERTER_STATION)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "powerFactor",
                        "lossFactor",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build())) {
            List<Resource<LccConverterStationAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.lccConverterStationBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(LccConverterStationAttributes.builder()
                                .voltageLevelId(row.getString(1))
                                .name(row.getString(2))
                                .properties(row.getMap(3, String.class, String.class))
                                .node(row.get(4, Integer.class))
                                .powerFactor(row.getFloat(5))
                                .lossFactor(row.getFloat(6))
                                .p(row.getDouble(7))
                                .q(row.getDouble(8))
                                .position(row.get(9, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(10)))
                                .connectableBus(row.getString(11))
                                .fictitious(row.getBoolean(12))
                                .aliasesWithoutType(row.getSet(13, String.class))
                                .aliasByType(row.getMap(14, String.class, String.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        try (ResultSet resultSet = session.execute(selectFrom("lccConverterStationByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        "powerFactor",
                        "lossFactor",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build())) {
            List<Resource<LccConverterStationAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.lccConverterStationBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(LccConverterStationAttributes.builder()
                                .voltageLevelId(voltageLevelId)
                                .name(row.getString(1))
                                .properties(row.getMap(2, String.class, String.class))
                                .node(row.get(3, Integer.class))
                                .powerFactor(row.getFloat(4))
                                .lossFactor(row.getFloat(5))
                                .p(row.getDouble(6))
                                .q(row.getDouble(7))
                                .position(row.get(8, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(9)))
                                .connectableBus(row.getString(10))
                                .fictitious(row.getBoolean(11))
                                .aliasesWithoutType(row.getSet(12, String.class))
                                .aliasByType(row.getMap(13, String.class, String.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        for (List<Resource<LccConverterStationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<LccConverterStationAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psUpdateLccConverterStation.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getPowerFactor(),
                        resource.getAttributes().getLossFactor(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        session.execute(deleteFrom(LCC_CONVERTER_STATION)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(lccConverterStationId))
                .build());
    }

    // static var compensators

    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        for (List<Resource<StaticVarCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<StaticVarCompensatorAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psInsertStaticVarCompensator.bind(
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getBmin(),
                        resource.getAttributes().getBmax(),
                        resource.getAttributes().getVoltageSetPoint(),
                        resource.getAttributes().getReactivePowerSetPoint(),
                        resource.getAttributes().getRegulationMode().toString(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getRegulatingTerminal(),
                        resource.getAttributes().getVoltagePerReactiveControl()
                )));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        try (ResultSet resultSet = session.execute(selectFrom(STATIC_VAR_COMPENSATOR)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "bMin",
                        "bMax",
                        "voltageSetPoint",
                        "reactivePowerSetPoint",
                        "regulationMode",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        REGULATING_TERMINAL,
                        "voltagePerReactivePowerControl",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(staticVarCompensatorId))
                .build())) {
            Row row = resultSet.one();
            if (row != null) {
                return Optional.of(Resource.staticVarCompensatorBuilder()
                        .id(staticVarCompensatorId)
                        .variantNum(variantNum)
                        .attributes(StaticVarCompensatorAttributes.builder()
                                .voltageLevelId(row.getString(0))
                                .name(row.getString(1))
                                .properties(row.getMap(2, String.class, String.class))
                                .node(row.get(3, Integer.class))
                                .bmin(row.getDouble(4))
                                .bmax(row.getDouble(5))
                                .voltageSetPoint(row.getDouble(6))
                                .reactivePowerSetPoint(row.getDouble(7))
                                .regulationMode(StaticVarCompensator.RegulationMode.valueOf(row.getString(8)))
                                .p(row.getDouble(9))
                                .q(row.getDouble(10))
                                .position(row.get(11, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(12)))
                                .connectableBus(row.getString(13))
                                .regulatingTerminal(row.get(14, TerminalRefAttributes.class))
                                .voltagePerReactiveControl(row.get(15, VoltagePerReactivePowerControlAttributes.class))
                                .fictitious(row.getBoolean(16))
                                .aliasesWithoutType(row.getSet(17, String.class))
                                .aliasByType(row.getMap(18, String.class, String.class))
                                .build())
                        .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        try (ResultSet resultSet = session.execute(selectFrom(STATIC_VAR_COMPENSATOR)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "bMin",
                        "bMax",
                        "voltageSetPoint",
                        "reactivePowerSetPoint",
                        "regulationMode",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        REGULATING_TERMINAL,
                        "voltagePerReactivePowerControl",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build())) {
            List<Resource<StaticVarCompensatorAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.staticVarCompensatorBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(StaticVarCompensatorAttributes.builder()
                                .voltageLevelId(row.getString(1))
                                .name(row.getString(2))
                                .properties(row.getMap(3, String.class, String.class))
                                .node(row.get(4, Integer.class))
                                .bmin(row.getDouble(5))
                                .bmax(row.getDouble(6))
                                .voltageSetPoint(row.getDouble(7))
                                .reactivePowerSetPoint(row.getDouble(8))
                                .regulationMode(StaticVarCompensator.RegulationMode.valueOf(row.getString(9)))
                                .p(row.getDouble(10))
                                .q(row.getDouble(11))
                                .position(row.get(12, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(13)))
                                .connectableBus(row.getString(14))
                                .regulatingTerminal(row.get(15, TerminalRefAttributes.class))
                                .voltagePerReactiveControl(row.get(16, VoltagePerReactivePowerControlAttributes.class))
                                .fictitious(row.getBoolean(17))
                                .aliasesWithoutType(row.getSet(18, String.class))
                                .aliasByType(row.getMap(19, String.class, String.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        try (ResultSet resultSet = session.execute(selectFrom("staticVarCompensatorByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        "bMin",
                        "bMax",
                        "voltageSetPoint",
                        "reactivePowerSetPoint",
                        "regulationMode",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        REGULATING_TERMINAL,
                        "voltagePerReactivePowerControl",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build())) {
            List<Resource<StaticVarCompensatorAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.staticVarCompensatorBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(StaticVarCompensatorAttributes.builder()
                                .voltageLevelId(voltageLevelId)
                                .name(row.getString(1))
                                .properties(row.getMap(2, String.class, String.class))
                                .node(row.get(3, Integer.class))
                                .bmin(row.getDouble(4))
                                .bmax(row.getDouble(5))
                                .voltageSetPoint(row.getDouble(6))
                                .reactivePowerSetPoint(row.getDouble(7))
                                .regulationMode(StaticVarCompensator.RegulationMode.valueOf(row.getString(8)))
                                .p(row.getDouble(9))
                                .q(row.getDouble(10))
                                .position(row.get(11, ConnectablePositionAttributes.class))
                                .bus(nullValueForEmptyString(row.getString(12)))
                                .connectableBus(row.getString(13))
                                .regulatingTerminal(row.get(14, TerminalRefAttributes.class))
                                .voltagePerReactiveControl(row.get(15, VoltagePerReactivePowerControlAttributes.class))
                                .fictitious(row.getBoolean(16))
                                .aliasesWithoutType(row.getSet(17, String.class))
                                .aliasByType(row.getMap(18, String.class, String.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        for (List<Resource<StaticVarCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<StaticVarCompensatorAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psUpdateStaticVarCompensator.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getBmin(),
                        resource.getAttributes().getBmax(),
                        resource.getAttributes().getVoltageSetPoint(),
                        resource.getAttributes().getReactivePowerSetPoint(),
                        resource.getAttributes().getRegulationMode().toString(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getRegulatingTerminal(),
                        resource.getAttributes().getVoltagePerReactiveControl(),
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        session.execute(deleteFrom(STATIC_VAR_COMPENSATOR)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(staticVarCompensatorId))
                .build());
    }

    // busbar section

    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        Map<String, Mapping> busbarSectionMappings = mappings.getBusbarSectionMappings();
        Set<String> keysBusbarSections = busbarSectionMappings.keySet();

        for (List<Resource<BusbarSectionAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<BusbarSectionAttributes> resource : subresources) {
                BusbarSectionAttributes busbarSectionAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysBusbarSections.forEach(key -> values.add(busbarSectionMappings.get(key).get(busbarSectionAttributes)));

                boundStatements.add(unsetNullValues(psInsertBusbarSection.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        Map<String, Mapping> busbarSectionMappings = mappings.getBusbarSectionMappings();
        Set<String> keysBusbarSections = busbarSectionMappings.keySet();

        for (List<Resource<BusbarSectionAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<BusbarSectionAttributes> resource : subresources) {
                BusbarSectionAttributes busbarSectionAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysBusbarSections.forEach(key -> {
                    if (!key.equals("voltageLevelId")) {
                        values.add(busbarSectionMappings.get(key).get(busbarSectionAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getVoltageLevelId());

                boundStatements.add(unsetNullValues(psUpdateBusbarSection.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        Map<String, Mapping> mappingBusbarSections = mappings.getBusbarSectionMappings();
        try (ResultSet resultSet = session.execute(selectFrom(BUSBAR_SECTION)
            .columns(mappingBusbarSections.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(busbarSectionId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                BusbarSectionAttributes busbarSectionAttributes = new BusbarSectionAttributes();
                mappingBusbarSections.entrySet().forEach(entry -> entry.getValue().set(busbarSectionAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.busbarSectionBuilder()
                    .id(busbarSectionId)
                    .variantNum(variantNum)
                    .attributes(busbarSectionAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingBusbarSections = mappings.getBusbarSectionMappings();
        Set<String> columns = new HashSet<>(mappingBusbarSections.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(BUSBAR_SECTION)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<BusbarSectionAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                BusbarSectionAttributes busbarSectionAttributes = new BusbarSectionAttributes();
                mappingBusbarSections.entrySet().forEach(entry -> entry.getValue().set(busbarSectionAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.busbarSectionBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(busbarSectionAttributes)
                    .build());
            }
            return resources;
        }
    }

    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingsBusbarSection = mappings.getBusbarSectionMappings();
        Set<String> columns = new HashSet<>(mappingsBusbarSection.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(BUSBAR_SECTION)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<BusbarSectionAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                BusbarSectionAttributes busbarSectionAttributes = new BusbarSectionAttributes();
                mappingsBusbarSection.entrySet().forEach(entry -> entry.getValue().set(busbarSectionAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.busbarSectionBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(busbarSectionAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void deleteBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        session.execute(deleteFrom(BUSBAR_SECTION)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(busBarSectionId))
                .build());
    }

    // switch

    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        Map<String, Mapping> switchMappings = mappings.getSwitchMappings();
        Set<String> keysSwitches = switchMappings.keySet();

        for (List<Resource<SwitchAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<SwitchAttributes> resource : subresources) {
                SwitchAttributes switchAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysSwitches.forEach(key -> values.add(switchMappings.get(key).get(switchAttributes)));

                boundStatements.add(unsetNullValues(psInsertSwitch.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        Map<String, Mapping> mappingSwitches = mappings.getSwitchMappings();
        try (ResultSet resultSet = session.execute(selectFrom(SWITCH)
            .columns(mappingSwitches.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(switchId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                SwitchAttributes switchAttributes = new SwitchAttributes();
                mappingSwitches.entrySet().forEach(entry -> entry.getValue().set(switchAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.switchBuilder()
                    .id(switchId)
                    .variantNum(variantNum)
                    .attributes(switchAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingSwitches = mappings.getSwitchMappings();
        Set<String> columns = new HashSet<>(mappingSwitches.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(SWITCH)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<SwitchAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                SwitchAttributes switchAttributes = new SwitchAttributes();
                mappingSwitches.entrySet().forEach(entry -> entry.getValue().set(switchAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.switchBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(switchAttributes)
                    .build());
            }
            return resources;
        }
    }

    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingSwitches = mappings.getSwitchMappings();
        Set<String> columns = new HashSet<>(mappingSwitches.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(SWITCH)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<SwitchAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                SwitchAttributes switchAttributes = new SwitchAttributes();
                mappingSwitches.entrySet().forEach(entry -> entry.getValue().set(switchAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.switchBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(switchAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        Map<String, Mapping> switchMappings = mappings.getSwitchMappings();
        Set<String> keysSwitches = switchMappings.keySet();

        for (List<Resource<SwitchAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<SwitchAttributes> resource : subresources) {
                SwitchAttributes switchAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysSwitches.forEach(key -> {
                    if (!key.equals("voltageLevelId")) {
                        values.add(switchMappings.get(key).get(switchAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getVoltageLevelId());

                boundStatements.add(unsetNullValues(psUpdateSwitch.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteSwitch(UUID networkUuid, int variantNum, String switchId) {
        session.execute(deleteFrom(SWITCH)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(switchId))
                .build());
    }

    // 2 windings transformer

    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        for (List<Resource<TwoWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<TwoWindingsTransformerAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psInsertTwoWindingsTransformer.bind(
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId1(),
                        resource.getAttributes().getVoltageLevelId2(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode1(),
                        resource.getAttributes().getNode2(),
                        resource.getAttributes().getR(),
                        resource.getAttributes().getX(),
                        resource.getAttributes().getG(),
                        resource.getAttributes().getB(),
                        resource.getAttributes().getRatedU1(),
                        resource.getAttributes().getRatedU2(),
                        resource.getAttributes().getRatedS(),
                        resource.getAttributes().getP1(),
                        resource.getAttributes().getQ1(),
                        resource.getAttributes().getP2(),
                        resource.getAttributes().getQ2(),
                        resource.getAttributes().getPosition1(),
                        resource.getAttributes().getPosition2(),
                        resource.getAttributes().getPhaseTapChangerAttributes(),
                        resource.getAttributes().getRatioTapChangerAttributes(),
                        emptyStringForNullValue(resource.getAttributes().getBus1()),
                        emptyStringForNullValue(resource.getAttributes().getBus2()),
                        resource.getAttributes().getConnectableBus1(),
                        resource.getAttributes().getConnectableBus2(),
                        resource.getAttributes().getCurrentLimits1(),
                        resource.getAttributes().getCurrentLimits2(),
                        resource.getAttributes().getPhaseAngleClockAttributes(),
                        resource.getAttributes().getActivePowerLimits1(),
                        resource.getAttributes().getActivePowerLimits2(),
                        resource.getAttributes().getApparentPowerLimits1(),
                        resource.getAttributes().getApparentPowerLimits2(),
                        resource.getAttributes().getBranchStatus(),
                        resource.getAttributes().getCgmesTapChangerAttributesList()
                )));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        try (ResultSet resultSet = session.execute(selectFrom(TWO_WINDINGS_TRANSFORMER)
                .columns(
                        "voltageLevelId1",
                        "voltageLevelId2",
                        "name",
                        "properties",
                        "node1",
                        "node2",
                        "r",
                        "x",
                        "g",
                        "b",
                        "ratedU1",
                        "ratedU2",
                        "p1",
                        "q1",
                        "p2",
                        "q2",
                        "position1",
                        "position2",
                        "phaseTapChanger",
                        "ratioTapChanger",
                        "bus1",
                        "bus2",
                        "connectableBus1",
                        "connectableBus2",
                        "currentLimits1",
                        "currentLimits2",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "ratedS",
                        "phaseAngleClock",
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        BRANCH_STATUS,
                        CGMES_TAP_CHANGERS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(twoWindingsTransformerId))
                .build())) {
            Row one = resultSet.one();
            if (one != null) {
                return Optional.of(Resource.twoWindingsTransformerBuilder()
                        .id(twoWindingsTransformerId)
                        .variantNum(variantNum)
                        .attributes(TwoWindingsTransformerAttributes.builder()
                                .voltageLevelId1(one.getString(0))
                                .voltageLevelId2(one.getString(1))
                                .name(one.getString(2))
                                .properties(one.getMap(3, String.class, String.class))
                                .node1(one.get(4, Integer.class))
                                .node2(one.get(5, Integer.class))
                                .r(one.getDouble(6))
                                .x(one.getDouble(7))
                                .g(one.getDouble(8))
                                .b(one.getDouble(9))
                                .ratedU1(one.getDouble(10))
                                .ratedU2(one.getDouble(11))
                                .p1(one.getDouble(12))
                                .q1(one.getDouble(13))
                                .p2(one.getDouble(14))
                                .q2(one.getDouble(15))
                                .position1(one.get(16, ConnectablePositionAttributes.class))
                                .position2(one.get(17, ConnectablePositionAttributes.class))
                                .phaseTapChangerAttributes(one.get(18, PhaseTapChangerAttributes.class))
                                .ratioTapChangerAttributes(one.get(19, RatioTapChangerAttributes.class))
                                .bus1(nullValueForEmptyString(one.getString(20)))
                                .bus2(nullValueForEmptyString(one.getString(21)))
                                .connectableBus1(one.getString(22))
                                .connectableBus2(one.getString(23))
                                .currentLimits1(one.get(24, LimitsAttributes.class))
                                .currentLimits2(one.get(25, LimitsAttributes.class))
                                .fictitious(one.getBoolean(26))
                                .aliasesWithoutType(one.getSet(27, String.class))
                                .aliasByType(one.getMap(28, String.class, String.class))
                                .ratedS(one.getDouble(29))
                                .phaseAngleClockAttributes(one.get(30, TwoWindingsTransformerPhaseAngleClockAttributes.class))
                                .activePowerLimits1(one.get(31, LimitsAttributes.class))
                                .activePowerLimits2(one.get(32, LimitsAttributes.class))
                                .apparentPowerLimits1(one.get(33, LimitsAttributes.class))
                                .apparentPowerLimits2(one.get(34, LimitsAttributes.class))
                                .branchStatus(one.getString(35))
                                .cgmesTapChangerAttributesList(one.getList(36, CgmesTapChangerAttributes.class))
                                .build())
                        .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        try (ResultSet resultSet = session.execute(selectFrom(TWO_WINDINGS_TRANSFORMER)
                .columns(
                        "id",
                        "voltageLevelId1",
                        "voltageLevelId2",
                        "name",
                        "properties",
                        "node1",
                        "node2",
                        "r",
                        "x",
                        "g",
                        "b",
                        "ratedU1",
                        "ratedU2",
                        "p1",
                        "q1",
                        "p2",
                        "q2",
                        "position1",
                        "position2",
                        "phaseTapChanger",
                        "ratioTapChanger",
                        "bus1",
                        "bus2",
                        "connectableBus1",
                        "connectableBus2",
                        "currentLimits1",
                        "currentLimits2",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "ratedS",
                        "phaseAngleClock",
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        BRANCH_STATUS,
                        CGMES_TAP_CHANGERS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build())) {
            List<Resource<TwoWindingsTransformerAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.twoWindingsTransformerBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(TwoWindingsTransformerAttributes.builder()
                                .voltageLevelId1(row.getString(1))
                                .voltageLevelId2(row.getString(2))
                                .name(row.getString(3))
                                .properties(row.getMap(4, String.class, String.class))
                                .node1(row.get(5, Integer.class))
                                .node2(row.get(6, Integer.class))
                                .r(row.getDouble(7))
                                .x(row.getDouble(8))
                                .g(row.getDouble(9))
                                .b(row.getDouble(10))
                                .ratedU1(row.getDouble(11))
                                .ratedU2(row.getDouble(12))
                                .p1(row.getDouble(13))
                                .q1(row.getDouble(14))
                                .p2(row.getDouble(15))
                                .q2(row.getDouble(16))
                                .position1(row.get(17, ConnectablePositionAttributes.class))
                                .position2(row.get(18, ConnectablePositionAttributes.class))
                                .phaseTapChangerAttributes(row.get(19, PhaseTapChangerAttributes.class))
                                .ratioTapChangerAttributes(row.get(20, RatioTapChangerAttributes.class))
                                .bus1(nullValueForEmptyString(row.getString(21)))
                                .bus2(nullValueForEmptyString(row.getString(22)))
                                .connectableBus1(row.getString(23))
                                .connectableBus2(row.getString(24))
                                .currentLimits1(row.get(25, LimitsAttributes.class))
                                .currentLimits2(row.get(26, LimitsAttributes.class))
                                .fictitious(row.getBoolean(27))
                                .aliasesWithoutType(row.getSet(28, String.class))
                                .aliasByType(row.getMap(29, String.class, String.class))
                                .ratedS(row.getDouble(30))
                                .phaseAngleClockAttributes(row.get(31, TwoWindingsTransformerPhaseAngleClockAttributes.class))
                                .activePowerLimits1(row.get(32, LimitsAttributes.class))
                                .activePowerLimits2(row.get(33, LimitsAttributes.class))
                                .apparentPowerLimits1(row.get(34, LimitsAttributes.class))
                                .apparentPowerLimits2(row.get(35, LimitsAttributes.class))
                                .branchStatus(row.getString(36))
                                .cgmesTapChangerAttributesList(row.getList(37, CgmesTapChangerAttributes.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    private List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, Branch.Side side, String voltageLevelId) {
        try (ResultSet resultSet = session.execute(selectFrom("twoWindingsTransformerByVoltageLevel" + (side == Branch.Side.ONE ? 1 : 2))
                .columns(
                        "id",
                        "voltageLevelId" + (side == Branch.Side.ONE ? 2 : 1),
                        "name",
                        "properties",
                        "node1",
                        "node2",
                        "r",
                        "x",
                        "g",
                        "b",
                        "ratedU1",
                        "ratedU2",
                        "p1",
                        "q1",
                        "p2",
                        "q2",
                        "position1",
                        "position2",
                        "phaseTapChanger",
                        "ratioTapChanger",
                        "bus1",
                        "bus2",
                        "connectableBus1",
                        "connectableBus2",
                        "currentLimits1",
                        "currentLimits2",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "ratedS",
                        "phaseAngleClock",
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        BRANCH_STATUS,
                        CGMES_TAP_CHANGERS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("voltageLevelId" + (side == Branch.Side.ONE ? 1 : 2)).isEqualTo(literal(voltageLevelId))
                .build())) {
            List<Resource<TwoWindingsTransformerAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.twoWindingsTransformerBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(TwoWindingsTransformerAttributes.builder()
                                .voltageLevelId1(side == Branch.Side.ONE ? voltageLevelId : row.getString(1))
                                .voltageLevelId2(side == Branch.Side.TWO ? voltageLevelId : row.getString(1))
                                .name(row.getString(2))
                                .properties(row.getMap(3, String.class, String.class))
                                .node1(row.get(4, Integer.class))
                                .node2(row.get(5, Integer.class))
                                .r(row.getDouble(6))
                                .x(row.getDouble(7))
                                .g(row.getDouble(8))
                                .b(row.getDouble(9))
                                .ratedU1(row.getDouble(10))
                                .ratedU2(row.getDouble(11))
                                .p1(row.getDouble(12))
                                .q1(row.getDouble(13))
                                .p2(row.getDouble(14))
                                .q2(row.getDouble(15))
                                .position1(row.get(16, ConnectablePositionAttributes.class))
                                .position2(row.get(17, ConnectablePositionAttributes.class))
                                .phaseTapChangerAttributes(row.get(18, PhaseTapChangerAttributes.class))
                                .ratioTapChangerAttributes(row.get(19, RatioTapChangerAttributes.class))
                                .bus1(nullValueForEmptyString(row.getString(20)))
                                .bus2(nullValueForEmptyString(row.getString(21)))
                                .connectableBus1(row.getString(22))
                                .connectableBus2(row.getString(23))
                                .currentLimits1(row.get(24, LimitsAttributes.class))
                                .currentLimits2(row.get(25, LimitsAttributes.class))
                                .fictitious(row.getBoolean(26))
                                .aliasesWithoutType(row.getSet(27, String.class))
                                .aliasByType(row.getMap(28, String.class, String.class))
                                .ratedS(row.getDouble(29))
                                .phaseAngleClockAttributes(row.get(30, TwoWindingsTransformerPhaseAngleClockAttributes.class))
                                .activePowerLimits1(row.get(31, LimitsAttributes.class))
                                .activePowerLimits2(row.get(32, LimitsAttributes.class))
                                .apparentPowerLimits1(row.get(33, LimitsAttributes.class))
                                .apparentPowerLimits2(row.get(34, LimitsAttributes.class))
                                .branchStatus(row.getString(35))
                                .cgmesTapChangerAttributesList(row.getList(36, CgmesTapChangerAttributes.class))
                                .build())
                        .build());
            }
            return resources;
        }
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
        for (List<Resource<TwoWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<TwoWindingsTransformerAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psUpdateTwoWindingsTransformer.bind(
                        resource.getAttributes().getVoltageLevelId1(),
                        resource.getAttributes().getVoltageLevelId2(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode1(),
                        resource.getAttributes().getNode2(),
                        resource.getAttributes().getR(),
                        resource.getAttributes().getX(),
                        resource.getAttributes().getG(),
                        resource.getAttributes().getB(),
                        resource.getAttributes().getRatedU1(),
                        resource.getAttributes().getRatedU2(),
                        resource.getAttributes().getRatedS(),
                        resource.getAttributes().getP1(),
                        resource.getAttributes().getQ1(),
                        resource.getAttributes().getP2(),
                        resource.getAttributes().getQ2(),
                        resource.getAttributes().getPosition1(),
                        resource.getAttributes().getPosition2(),
                        resource.getAttributes().getPhaseTapChangerAttributes(),
                        resource.getAttributes().getRatioTapChangerAttributes(),
                        emptyStringForNullValue(resource.getAttributes().getBus1()),
                        emptyStringForNullValue(resource.getAttributes().getBus2()),
                        resource.getAttributes().getConnectableBus1(),
                        resource.getAttributes().getConnectableBus2(),
                        resource.getAttributes().getCurrentLimits1(),
                        resource.getAttributes().getCurrentLimits2(),
                        resource.getAttributes().getPhaseAngleClockAttributes(),
                        resource.getAttributes().getActivePowerLimits1(),
                        resource.getAttributes().getActivePowerLimits2(),
                        resource.getAttributes().getApparentPowerLimits1(),
                        resource.getAttributes().getApparentPowerLimits2(),
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

    public void deleteTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        session.execute(deleteFrom(TWO_WINDINGS_TRANSFORMER)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(twoWindingsTransformerId))
                .build());
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
        session.execute(deleteFrom(THREE_WINDINGS_TRANSFORMER)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(threeWindingsTransformerId))
                .build());
    }

    // line

    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        Map<String, Mapping> lineMappings = mappings.getLineMappings();
        Set<String> keysLines = lineMappings.keySet();

        for (List<Resource<LineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<LineAttributes> resource : subresources) {
                LineAttributes lineAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysLines.forEach(key -> values.add(lineMappings.get(key).get(lineAttributes)));

                boundStatements.add(unsetNullValues(psInsertLine.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        Map<String, Mapping> mappingLines = mappings.getLineMappings();
        try (ResultSet resultSet = session.execute(selectFrom(LINE)
            .columns(mappingLines.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(lineId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                LineAttributes lineAttributes = new LineAttributes();
                mappingLines.entrySet().forEach(entry -> entry.getValue().set(lineAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.lineBuilder()
                    .id(lineId)
                    .variantNum(variantNum)
                    .attributes(lineAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingLines = mappings.getLineMappings();
        Set<String> columns = new HashSet<>(mappingLines.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(LINE)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<LineAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                LineAttributes lineAttributes = new LineAttributes();
                mappingLines.entrySet().forEach(entry -> entry.getValue().set(lineAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.lineBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(lineAttributes)
                    .build());
            }
            return resources;
        }
    }

    private List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, Branch.Side side, String voltageLevelId) {
        Map<String, Mapping> mappingLines = mappings.getLineMappings();
        Set<String> columns = new HashSet<>(mappingLines.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(LINE)
                .columns(columns.toArray(new String[0]))
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("voltageLevelId" + (side == Branch.Side.ONE ? 1 : 2)).isEqualTo(literal(voltageLevelId))
                .build())) {
            List<Resource<LineAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                LineAttributes lineAttributes = new LineAttributes();
                mappingLines.entrySet().forEach(entry -> entry.getValue().set(lineAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.lineBuilder()
                        .id(row.getString("id"))
                        .variantNum(variantNum)
                        .attributes(lineAttributes)
                        .build());
            }
            return resources;
        }
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
        Map<String, Mapping> lineMappings = mappings.getLineMappings();
        Set<String> keysLines = lineMappings.keySet();

        for (List<Resource<LineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<LineAttributes> resource : subresources) {
                LineAttributes lineAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysLines.forEach(key -> values.add(lineMappings.get(key).get(lineAttributes)));
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());

                boundStatements.add(unsetNullValues(psUpdateLines.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteLine(UUID networkUuid, int variantNum, String lineId) {
        session.execute(deleteFrom(LINE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(lineId))
                .build());
    }

    // Hvdc line

    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        try (ResultSet resultSet = session.execute(selectFrom(HVDC_LINE)
                .columns(
                        "id",
                        "name",
                        "properties",
                        "r",
                        "convertersMode",
                        "nominalV",
                        "activePowerSetpoint",
                        "maxP",
                        "converterStationId1",
                        "converterStationId2",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        HVDC_ANGLE_DROOP_ACTIVE_POWER_CONTROL,
                        HVDC_OPERATOR_ACTIVE_POWER_RANGE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build())) {
            List<Resource<HvdcLineAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                resources.add(Resource.hvdcLineBuilder()
                        .id(row.getString(0))
                        .variantNum(variantNum)
                        .attributes(HvdcLineAttributes.builder()
                                .name(row.getString(1))
                                .properties(row.getMap(2, String.class, String.class))
                                .r(row.getDouble(3))
                                .convertersMode(HvdcLine.ConvertersMode.valueOf(row.getString(4)))
                                .nominalV(row.getDouble(5))
                                .activePowerSetpoint(row.getDouble(6))
                                .maxP(row.getDouble(7))
                                .converterStationId1(row.getString(8))
                                .converterStationId2(row.getString(9))
                                .fictitious(row.getBoolean(10))
                                .aliasesWithoutType(row.getSet(11, String.class))
                                .aliasByType(row.getMap(12, String.class, String.class))
                                .hvdcAngleDroopActivePowerControl(row.get(13, HvdcAngleDroopActivePowerControlAttributes.class))
                                .hvdcOperatorActivePowerRange(row.get(14, HvdcOperatorActivePowerRangeAttributes.class))
                                .build())
                        .build());
            }
            return resources;
        }
    }

    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        try (ResultSet resultSet = session.execute(selectFrom(HVDC_LINE)
                .columns(
                        "name",
                        "properties",
                        "r",
                        "convertersMode",
                        "nominalV",
                        "activePowerSetpoint",
                        "maxP",
                        "converterStationId1",
                        "converterStationId2",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        HVDC_ANGLE_DROOP_ACTIVE_POWER_CONTROL,
                        HVDC_OPERATOR_ACTIVE_POWER_RANGE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(hvdcLineId))
                .build())) {
            Row one = resultSet.one();
            if (one != null) {
                return Optional.of(Resource.hvdcLineBuilder()
                        .id(hvdcLineId)
                        .variantNum(variantNum)
                        .attributes(HvdcLineAttributes.builder()
                                .name(one.getString(0))
                                .properties(one.getMap(1, String.class, String.class))
                                .r(one.getDouble(2))
                                .convertersMode(HvdcLine.ConvertersMode.valueOf(one.getString(3)))
                                .nominalV(one.getDouble(4))
                                .activePowerSetpoint(one.getDouble(5))
                                .maxP(one.getDouble(6))
                                .converterStationId1(one.getString(7))
                                .converterStationId2(one.getString(8))
                                .fictitious(one.getBoolean(9))
                                .aliasesWithoutType(one.getSet(10, String.class))
                                .aliasByType(one.getMap(11, String.class, String.class))
                                .hvdcAngleDroopActivePowerControl(one.get(12, HvdcAngleDroopActivePowerControlAttributes.class))
                                .hvdcOperatorActivePowerRange(one.get(13, HvdcOperatorActivePowerRangeAttributes.class))
                                .build())
                        .build());
            }
            return Optional.empty();
        }
    }

    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        for (List<Resource<HvdcLineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<HvdcLineAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psInsertHvdcLine.bind(
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getR(),
                        resource.getAttributes().getConvertersMode().toString(),
                        resource.getAttributes().getNominalV(),
                        resource.getAttributes().getActivePowerSetpoint(),
                        resource.getAttributes().getMaxP(),
                        resource.getAttributes().getConverterStationId1(),
                        resource.getAttributes().getConverterStationId2(),
                        resource.getAttributes().getHvdcAngleDroopActivePowerControl(),
                        resource.getAttributes().getHvdcOperatorActivePowerRange()
                )));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        for (List<Resource<HvdcLineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<HvdcLineAttributes> resource : subresources) {
                boundStatements.add(unsetNullValues(psUpdateHvdcLine.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getR(),
                        resource.getAttributes().getConvertersMode().toString(),
                        resource.getAttributes().getNominalV(),
                        resource.getAttributes().getActivePowerSetpoint(),
                        resource.getAttributes().getMaxP(),
                        resource.getAttributes().getConverterStationId1(),
                        resource.getAttributes().getConverterStationId2(),
                        resource.getAttributes().getHvdcAngleDroopActivePowerControl(),
                        resource.getAttributes().getHvdcOperatorActivePowerRange(),
                        networkUuid,
                        resource.getVariantNum(),
                        resource.getId())
                ));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        session.execute(deleteFrom(HVDC_LINE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(hvdcLineId))
                .build());
    }

    // Dangling line

    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        Map<String, Mapping> danglingLineMappings = mappings.getDanglingLineMappings();
        Set<String> columns = new HashSet<>(danglingLineMappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(DANGLING_LINE)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<DanglingLineAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                DanglingLineAttributes danglingLineAttributes = new DanglingLineAttributes();
                danglingLineMappings.entrySet().forEach(entry -> entry.getValue().set(danglingLineAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.danglingLineBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(danglingLineAttributes)
                    .build());
            }
            return resources;
        }
    }

    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        Map<String, Mapping> danglingLineMappings = mappings.getDanglingLineMappings();
        try (ResultSet resultSet = session.execute(selectFrom(DANGLING_LINE)
            .columns(danglingLineMappings.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(danglingLineId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                DanglingLineAttributes danglingLineAttributes = new DanglingLineAttributes();
                danglingLineMappings.entrySet().forEach(entry -> entry.getValue().set(danglingLineAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.danglingLineBuilder()
                    .id(danglingLineId)
                    .variantNum(variantNum)
                    .attributes(danglingLineAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingDanglineLines = mappings.getDanglingLineMappings();
        Set<String> columns = new HashSet<>(mappingDanglineLines.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(DANGLING_LINE)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<DanglingLineAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                DanglingLineAttributes danglingLineAttributes = new DanglingLineAttributes();
                mappingDanglineLines.entrySet().forEach(entry -> entry.getValue().set(danglingLineAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.danglingLineBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(danglingLineAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        Map<String, Mapping> danglingLineMappings = mappings.getDanglingLineMappings();
        Set<String> keysDanglingLines = danglingLineMappings.keySet();

        for (List<Resource<DanglingLineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<DanglingLineAttributes> resource : subresources) {
                DanglingLineAttributes danglingLineAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysDanglingLines.forEach(key -> values.add(danglingLineMappings.get(key).get(danglingLineAttributes)));

                boundStatements.add(unsetNullValues(psInsertDanglingLine.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        session.execute(deleteFrom(DANGLING_LINE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(danglingLineId))
                .build());
    }

    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        Map<String, Mapping> danglingLineMappings = mappings.getDanglingLineMappings();
        Set<String> keysGenerators = danglingLineMappings.keySet();

        for (List<Resource<DanglingLineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<DanglingLineAttributes> resource : subresources) {
                DanglingLineAttributes danglingLineAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysGenerators.forEach(key -> {
                    if (!key.equals("voltageLevelId")) {
                        values.add(danglingLineMappings.get(key).get(danglingLineAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getVoltageLevelId());

                boundStatements.add(unsetNullValues(psUpdateDanglingLine.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    // configured buses

    public void createBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        Map<String, Mapping> configuredBusMappings = mappings.getConfiguredBusMappings();
        Set<String> keysConfiguredBuses = configuredBusMappings.keySet();

        for (List<Resource<ConfiguredBusAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<ConfiguredBusAttributes> resource : subresources) {
                ConfiguredBusAttributes configuredBusAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysConfiguredBuses.forEach(key -> values.add(configuredBusMappings.get(key).get(configuredBusAttributes)));

                boundStatements.add(unsetNullValues(psInsertConfiguredBus.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        Map<String, Mapping> mappingsConfiguredBuses = mappings.getConfiguredBusMappings();
        try (ResultSet resultSet = session.execute(selectFrom(CONFIGURED_BUS)
            .columns(mappingsConfiguredBuses.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(busId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                ConfiguredBusAttributes configuredBusAttributes = new ConfiguredBusAttributes();
                mappingsConfiguredBuses.entrySet().forEach(entry -> entry.getValue().set(configuredBusAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.configuredBusBuilder()
                    .id(busId)
                    .variantNum(variantNum)
                    .attributes(configuredBusAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        Map<String, Mapping> mappingConfiguredBuses = mappings.getConfiguredBusMappings();
        Set<String> columns = new HashSet<>(mappingConfiguredBuses.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(CONFIGURED_BUS)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<ConfiguredBusAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                ConfiguredBusAttributes configuredBusAttributes = new ConfiguredBusAttributes();
                mappingConfiguredBuses.entrySet().forEach(entry -> entry.getValue().set(configuredBusAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.configuredBusBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(configuredBusAttributes)
                    .build());
            }
            return resources;
        }
    }

    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        Map<String, Mapping> mappingConfiguredBuses = mappings.getConfiguredBusMappings();
        Set<String> columns = new HashSet<>(mappingConfiguredBuses.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(CONFIGURED_BUS)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<ConfiguredBusAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                ConfiguredBusAttributes configuredBusAttributes = new ConfiguredBusAttributes();
                mappingConfiguredBuses.entrySet().forEach(entry -> entry.getValue().set(configuredBusAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.configuredBusBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(configuredBusAttributes)
                    .build());
            }
            return resources;
        }
    }

    public void updateBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        Map<String, Mapping> configuredBusMappings = mappings.getConfiguredBusMappings();
        Set<String> keysConfiguredBuses = configuredBusMappings.keySet();

        for (List<Resource<ConfiguredBusAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<ConfiguredBusAttributes> resource : subresources) {
                ConfiguredBusAttributes configuredBusAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keysConfiguredBuses.forEach(key -> {
                    if (!key.equals("voltageLevelId")) {
                        values.add(configuredBusMappings.get(key).get(configuredBusAttributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getVoltageLevelId());

                boundStatements.add(unsetNullValues(psUpdateConfiguredBus.bind(values.toArray(new Object[0]))));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteBus(UUID networkUuid, int variantNum, String configuredBusId) {
        session.execute(deleteFrom(CONFIGURED_BUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .whereColumn("id").isEqualTo(literal(configuredBusId))
                .build());
    }
}
