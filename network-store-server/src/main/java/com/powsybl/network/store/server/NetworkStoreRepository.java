/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.update.Assignment;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Repository
public class NetworkStoreRepository {

    private static final int BATCH_SIZE = 1000;

    @Autowired
    private CqlSession session;

    private PreparedStatement psInsertNetwork;
    private PreparedStatement psUpdateNetwork;
    private PreparedStatement psInsertSubstation;
    private PreparedStatement psUpdateSubstation;
    private PreparedStatement psInsertVoltageLevel;
    private PreparedStatement psUpdateVoltageLevel;
    private PreparedStatement psInsertGenerator;
    private PreparedStatement psUpdateGenerator;
    private PreparedStatement psInsertBattery;
    private PreparedStatement psUpdateBattery;
    private PreparedStatement psInsertLoad;
    private PreparedStatement psUpdateLoad;
    private PreparedStatement psInsertShuntCompensator;
    private PreparedStatement psUpdateShuntCompensator;
    private PreparedStatement psInsertVscConverterStation;
    private PreparedStatement psUpdateVscConverterStation;
    private PreparedStatement psInsertLccConverterStation;
    private PreparedStatement psUpdateLccConverterStation;
    private PreparedStatement psInsertStaticVarCompensator;
    private PreparedStatement psUpdateStaticVarCompensator;
    private PreparedStatement psInsertBusbarSection;
    private PreparedStatement psUpdateBusbarSection;
    private PreparedStatement psInsertSwitch;
    private PreparedStatement psUpdateSwitch;
    private PreparedStatement psInsertTwoWindingsTransformer;
    private PreparedStatement psUpdateTwoWindingsTransformer;
    private PreparedStatement psInsertThreeWindingsTransformer;
    private PreparedStatement psUpdateThreeWindingsTransformer;
    private PreparedStatement psInsertLine;
    private PreparedStatement psUpdateLines;
    private PreparedStatement psInsertHvdcLine;
    private PreparedStatement psUpdateHvdcLine;
    private PreparedStatement psInsertDanglingLine;
    private PreparedStatement psUpdateDanglingLine;
    private PreparedStatement psInsertConfiguredBus;
    private PreparedStatement psUpdateConfiguredBus;

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

    @PostConstruct
    void prepareStatements() {
        psInsertNetwork = session.prepare(insertInto(NETWORK)
                .value("uuid", bindMarker())
                .value("id", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value(ID_BY_ALIAS, bindMarker())
                .value("caseDate", bindMarker())
                .value("forecastDistance", bindMarker())
                .value("sourceFormat", bindMarker())
                .value("connectedComponentsValid", bindMarker())
                .value("synchronousComponentsValid", bindMarker())
                .value(CGMES_SV_METADATA, bindMarker())
                .value(CGMES_SSH_METADATA, bindMarker())
                .value(CIM_CHARACTERISTICS, bindMarker())
                .value(CGMES_CONTROL_AREAS, bindMarker())
                .value(CGMES_IIDM_MAPPING, bindMarker()).build());

        psUpdateNetwork = session.prepare(update(NETWORK)
                .set(Assignment.setColumn("id", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn(ID_BY_ALIAS, bindMarker()))
                .set(Assignment.setColumn("caseDate", bindMarker()))
                .set(Assignment.setColumn("forecastDistance", bindMarker()))
                .set(Assignment.setColumn("sourceFormat", bindMarker()))
                .set(Assignment.setColumn("connectedComponentsValid", bindMarker()))
                .set(Assignment.setColumn("synchronousComponentsValid", bindMarker()))
                .set(Assignment.setColumn(CGMES_SV_METADATA, bindMarker()))
                .set(Assignment.setColumn(CGMES_SSH_METADATA, bindMarker()))
                .set(Assignment.setColumn(CIM_CHARACTERISTICS, bindMarker()))
                .set(Assignment.setColumn(CGMES_CONTROL_AREAS, bindMarker()))
                .set(Assignment.setColumn(CGMES_IIDM_MAPPING, bindMarker()))
                .whereColumn("uuid").isEqualTo(bindMarker()).build());

        psInsertSubstation = session.prepare(insertInto(SUBSTATION)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("country", bindMarker())
                .value("tso", bindMarker())
                .value("entsoeArea", bindMarker())
                .value("geographicalTags", bindMarker())
                .build());

        psUpdateSubstation = session.prepare(update(SUBSTATION)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("country", bindMarker()))
                .set(Assignment.setColumn("tso", bindMarker()))
                .set(Assignment.setColumn("entsoeArea", bindMarker()))
                .set(Assignment.setColumn("geographicalTags", bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .build());

        psInsertVoltageLevel = session.prepare(insertInto(VOLTAGE_LEVEL)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("substationId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("nominalV", bindMarker())
                .value("lowVoltageLimit", bindMarker())
                .value("highVoltageLimit", bindMarker())
                .value("topologyKind", bindMarker())
                .value("internalConnections", bindMarker())
                .value("calculatedBusesForBusView", bindMarker())
                .value("nodeToCalculatedBusForBusView", bindMarker())
                .value("busToCalculatedBusForBusView", bindMarker())
                .value("calculatedBusesForBusBreakerView", bindMarker())
                .value("nodeToCalculatedBusForBusBreakerView", bindMarker())
                .value("busToCalculatedBusForBusBreakerView", bindMarker())
                .value("calculatedBusesValid", bindMarker())
                .value(SLACK_TERMINAL, bindMarker())
                .build());

        psUpdateVoltageLevel = session.prepare(update(VOLTAGE_LEVEL)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("nominalV", bindMarker()))
                .set(Assignment.setColumn("lowVoltageLimit", bindMarker()))
                .set(Assignment.setColumn("highVoltageLimit", bindMarker()))
                .set(Assignment.setColumn("topologyKind", bindMarker()))
                .set(Assignment.setColumn("internalConnections", bindMarker()))
                .set(Assignment.setColumn("calculatedBusesForBusView", bindMarker()))
                .set(Assignment.setColumn("nodeToCalculatedBusForBusView", bindMarker()))
                .set(Assignment.setColumn("busToCalculatedBusForBusView", bindMarker()))
                .set(Assignment.setColumn("calculatedBusesForBusBreakerView", bindMarker()))
                .set(Assignment.setColumn("nodeToCalculatedBusForBusBreakerView", bindMarker()))
                .set(Assignment.setColumn("busToCalculatedBusForBusBreakerView", bindMarker()))
                .set(Assignment.setColumn("calculatedBusesValid", bindMarker()))
                .set(Assignment.setColumn(SLACK_TERMINAL, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("substationId").isEqualTo(bindMarker())
                .build());

        psInsertGenerator = session.prepare(insertInto(GENERATOR)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value("energySource", bindMarker())
                .value("minP", bindMarker())
                .value("maxP", bindMarker())
                .value("voltageRegulatorOn", bindMarker())
                .value("targetP", bindMarker())
                .value("targetQ", bindMarker())
                .value("targetV", bindMarker())
                .value("ratedS", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker())
                .value("minMaxReactiveLimits", bindMarker())
                .value("reactiveCapabilityCurve", bindMarker())
                .value("bus", bindMarker())
                .value(CONNECTABLE_BUS, bindMarker())
                .value(ACTIVE_POWER_CONTROL, bindMarker())
                .value(REGULATING_TERMINAL, bindMarker())
                .value("coordinatedReactiveControl", bindMarker())
                .build());

        psUpdateGenerator = session.prepare(update(GENERATOR)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn("energySource", bindMarker()))
                .set(Assignment.setColumn("minP", bindMarker()))
                .set(Assignment.setColumn("maxP", bindMarker()))
                .set(Assignment.setColumn("voltageRegulatorOn", bindMarker()))
                .set(Assignment.setColumn("targetP", bindMarker()))
                .set(Assignment.setColumn("targetQ", bindMarker()))
                .set(Assignment.setColumn("targetV", bindMarker()))
                .set(Assignment.setColumn("ratedS", bindMarker()))
                .set(Assignment.setColumn("p", bindMarker()))
                .set(Assignment.setColumn("q", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .set(Assignment.setColumn("minMaxReactiveLimits", bindMarker()))
                .set(Assignment.setColumn("reactiveCapabilityCurve", bindMarker()))
                .set(Assignment.setColumn("bus", bindMarker()))
                .set(Assignment.setColumn(CONNECTABLE_BUS, bindMarker()))
                .set(Assignment.setColumn("activePowerControl", bindMarker()))
                .set(Assignment.setColumn(REGULATING_TERMINAL, bindMarker()))
                .set(Assignment.setColumn("coordinatedReactiveControl", bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertBattery = session.prepare(insertInto(BATTERY)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value("minP", bindMarker())
                .value("maxP", bindMarker())
                .value("p0", bindMarker())
                .value("q0", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker())
                .value("minMaxReactiveLimits", bindMarker())
                .value("reactiveCapabilityCurve", bindMarker())
                .value("bus", bindMarker())
                .value(CONNECTABLE_BUS, bindMarker())
                .value("activePowerControl", bindMarker())
                .build());

        psUpdateBattery = session.prepare(update(BATTERY)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn("minP", bindMarker()))
                .set(Assignment.setColumn("maxP", bindMarker()))
                .set(Assignment.setColumn("p0", bindMarker()))
                .set(Assignment.setColumn("q0", bindMarker()))
                .set(Assignment.setColumn("p", bindMarker()))
                .set(Assignment.setColumn("q", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .set(Assignment.setColumn("minMaxReactiveLimits", bindMarker()))
                .set(Assignment.setColumn("reactiveCapabilityCurve", bindMarker()))
                .set(Assignment.setColumn("bus", bindMarker()))
                .set(Assignment.setColumn(CONNECTABLE_BUS, bindMarker()))
                .set(Assignment.setColumn("activePowerControl", bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertLoad = session.prepare(insertInto(LOAD)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value("loadType", bindMarker())
                .value("p0", bindMarker())
                .value("q0", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker())
                .value("bus", bindMarker())
                .value(CONNECTABLE_BUS, bindMarker())
                .value(LOAD_DETAIL, bindMarker())
                .build());

        psUpdateLoad = session.prepare(update(LOAD)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn("loadType", bindMarker()))
                .set(Assignment.setColumn("p0", bindMarker()))
                .set(Assignment.setColumn("q0", bindMarker()))
                .set(Assignment.setColumn("p", bindMarker()))
                .set(Assignment.setColumn("q", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .set(Assignment.setColumn("bus", bindMarker()))
                .set(Assignment.setColumn(CONNECTABLE_BUS, bindMarker()))
                .set(Assignment.setColumn(LOAD_DETAIL, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertShuntCompensator = session.prepare(insertInto(SHUNT_COMPENSATOR)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value(LINEAR_MODEL, bindMarker())
                .value(NON_LINEAR_MODEL, bindMarker())
                .value(SECTION_COUNT, bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker())
                .value("bus", bindMarker())
                .value(CONNECTABLE_BUS, bindMarker())
                .value(REGULATING_TERMINAL, bindMarker())
                .value("voltageRegulatorOn", bindMarker())
                .value("targetV", bindMarker())
                .value(TARGET_DEADBAND, bindMarker())
                .build());

        psUpdateShuntCompensator = session.prepare(update(SHUNT_COMPENSATOR)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn(LINEAR_MODEL, bindMarker()))
                .set(Assignment.setColumn(NON_LINEAR_MODEL, bindMarker()))
                .set(Assignment.setColumn(SECTION_COUNT, bindMarker()))
                .set(Assignment.setColumn("p", bindMarker()))
                .set(Assignment.setColumn("q", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .set(Assignment.setColumn("bus", bindMarker()))
                .set(Assignment.setColumn(CONNECTABLE_BUS, bindMarker()))
                .set(Assignment.setColumn(REGULATING_TERMINAL, bindMarker()))
                .set(Assignment.setColumn("voltageRegulatorOn", bindMarker()))
                .set(Assignment.setColumn("targetV", bindMarker()))
                .set(Assignment.setColumn(TARGET_DEADBAND, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertVscConverterStation = session.prepare(insertInto(VSC_CONVERTER_STATION)
                .value("networkUuid", bindMarker())
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
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertLccConverterStation = session.prepare(insertInto(LCC_CONVERTER_STATION)
                .value("networkUuid", bindMarker())
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
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertStaticVarCompensator = session.prepare(insertInto(STATIC_VAR_COMPENSATOR)
                .value("networkUuid", bindMarker())
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
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertBusbarSection = session.prepare(insertInto(BUSBAR_SECTION)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value("position", bindMarker())
                .build());

        psUpdateBusbarSection = session.prepare(update(BUSBAR_SECTION)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertSwitch = session.prepare(insertInto(SWITCH)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node1", bindMarker())
                .value("node2", bindMarker())
                .value("open", bindMarker())
                .value("retained", bindMarker())
                .value("fictitious", bindMarker())
                .value("kind", bindMarker())
                .value("bus1", bindMarker())
                .value("bus2", bindMarker())
                .build());

        psUpdateSwitch = session.prepare(update(SWITCH)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node1", bindMarker()))
                .set(Assignment.setColumn("node2", bindMarker()))
                .set(Assignment.setColumn("open", bindMarker()))
                .set(Assignment.setColumn("retained", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("kind", bindMarker()))
                .set(Assignment.setColumn("bus1", bindMarker()))
                .set(Assignment.setColumn("bus2", bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertTwoWindingsTransformer = session.prepare(insertInto(TWO_WINDINGS_TRANSFORMER)
                .value("networkUuid", bindMarker())
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
                .build());

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
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .build());

        psInsertThreeWindingsTransformer = session.prepare(insertInto(THREE_WINDINGS_TRANSFORMER)
                .value("networkUuid", bindMarker())
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
                .build());

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
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .build());

        psInsertLine = session.prepare(insertInto(LINE)
                .value("networkUuid", bindMarker())
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
                .value("g1", bindMarker())
                .value("b1", bindMarker())
                .value("g2", bindMarker())
                .value("b2", bindMarker())
                .value("p1", bindMarker())
                .value("q1", bindMarker())
                .value("p2", bindMarker())
                .value("q2", bindMarker())
                .value("position1", bindMarker())
                .value("position2", bindMarker())
                .value("bus1", bindMarker())
                .value("bus2", bindMarker())
                .value("connectableBus1", bindMarker())
                .value("connectableBus2", bindMarker())
                .value("mergedXnode", bindMarker())
                .value(CURRENT_LIMITS1, bindMarker())
                .value(CURRENT_LIMITS2, bindMarker())
                .value(ACTIVE_POWER_LIMITS1, bindMarker())
                .value(ACTIVE_POWER_LIMITS2, bindMarker())
                .value(APPARENT_POWER_LIMITS1, bindMarker())
                .value(APPARENT_POWER_LIMITS2, bindMarker())
                .value(BRANCH_STATUS, bindMarker())
                .build());

        psUpdateLines = session.prepare(update(LINE)
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
                .set(Assignment.setColumn("g1", bindMarker()))
                .set(Assignment.setColumn("b1", bindMarker()))
                .set(Assignment.setColumn("g2", bindMarker()))
                .set(Assignment.setColumn("b2", bindMarker()))
                .set(Assignment.setColumn("p1", bindMarker()))
                .set(Assignment.setColumn("q1", bindMarker()))
                .set(Assignment.setColumn("p2", bindMarker()))
                .set(Assignment.setColumn("q2", bindMarker()))
                .set(Assignment.setColumn("position1", bindMarker()))
                .set(Assignment.setColumn("position2", bindMarker()))
                .set(Assignment.setColumn("bus1", bindMarker()))
                .set(Assignment.setColumn("bus2", bindMarker()))
                .set(Assignment.setColumn("connectableBus1", bindMarker()))
                .set(Assignment.setColumn("connectableBus2", bindMarker()))
                .set(Assignment.setColumn("mergedXnode", bindMarker()))
                .set(Assignment.setColumn("currentLimits1", bindMarker()))
                .set(Assignment.setColumn("currentLimits2", bindMarker()))
                .set(Assignment.setColumn(ACTIVE_POWER_LIMITS1, bindMarker()))
                .set(Assignment.setColumn(ACTIVE_POWER_LIMITS2, bindMarker()))
                .set(Assignment.setColumn(APPARENT_POWER_LIMITS1, bindMarker()))
                .set(Assignment.setColumn(APPARENT_POWER_LIMITS2, bindMarker()))
                .set(Assignment.setColumn(BRANCH_STATUS, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .build());

        psInsertHvdcLine = session.prepare(insertInto(HVDC_LINE)
                .value("networkUuid", bindMarker())
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
                .whereColumn("id").isEqualTo(bindMarker())
                .build());

        psInsertDanglingLine = session.prepare(insertInto(DANGLING_LINE)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("node", bindMarker())
                .value("p0", bindMarker())
                .value("q0", bindMarker())
                .value("r", bindMarker())
                .value("x", bindMarker())
                .value("g", bindMarker())
                .value("b", bindMarker())
                .value(GENERATION, bindMarker())
                .value("ucteXNodeCode", bindMarker())
                .value("currentLimits", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker())
                .value("bus", bindMarker())
                .value(CONNECTABLE_BUS, bindMarker())
                .value(ACTIVE_POWER_LIMITS, bindMarker())
                .value(APPARENT_POWER_LIMITS, bindMarker())
                .build());

        psUpdateDanglingLine = session.prepare(update(DANGLING_LINE)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("node", bindMarker()))
                .set(Assignment.setColumn("p0", bindMarker()))
                .set(Assignment.setColumn("q0", bindMarker()))
                .set(Assignment.setColumn("r", bindMarker()))
                .set(Assignment.setColumn("x", bindMarker()))
                .set(Assignment.setColumn("g", bindMarker()))
                .set(Assignment.setColumn("b", bindMarker()))
                .set(Assignment.setColumn(GENERATION, bindMarker()))
                .set(Assignment.setColumn("ucteXNodeCode", bindMarker()))
                .set(Assignment.setColumn("currentLimits", bindMarker()))
                .set(Assignment.setColumn("p", bindMarker()))
                .set(Assignment.setColumn("q", bindMarker()))
                .set(Assignment.setColumn("position", bindMarker()))
                .set(Assignment.setColumn("bus", bindMarker()))
                .set(Assignment.setColumn(CONNECTABLE_BUS, bindMarker()))
                .set(Assignment.setColumn(ACTIVE_POWER_LIMITS, bindMarker()))
                .set(Assignment.setColumn(APPARENT_POWER_LIMITS, bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

        psInsertConfiguredBus = session.prepare(insertInto(CONFIGURED_BUS)
                .value("networkUuid", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("fictitious", bindMarker())
                .value("properties", bindMarker())
                .value(ALIASES_WITHOUT_TYPE, bindMarker())
                .value(ALIAS_BY_TYPE, bindMarker())
                .value("v", bindMarker())
                .value("angle", bindMarker())
                .build());

        psUpdateConfiguredBus = session.prepare(update(CONFIGURED_BUS)
                .set(Assignment.setColumn("name", bindMarker()))
                .set(Assignment.setColumn("fictitious", bindMarker()))
                .set(Assignment.setColumn("properties", bindMarker()))
                .set(Assignment.setColumn(ALIASES_WITHOUT_TYPE, bindMarker()))
                .set(Assignment.setColumn(ALIAS_BY_TYPE, bindMarker()))
                .set(Assignment.setColumn("v", bindMarker()))
                .set(Assignment.setColumn("angle", bindMarker()))
                .whereColumn("networkUuid").isEqualTo(bindMarker())
                .whereColumn("id").isEqualTo(bindMarker())
                .whereColumn("voltageLevelId").isEqualTo(bindMarker())
                .build());

    }

    // This method unsets the null valued columns of a bound statement in order to avoid creation of tombstones
    // It must be used only for statements used for creation, not for those used for update
    private static BoundStatement unsetNullValues(BoundStatement bs) {
        BoundStatement boundStatement = bs;
        ColumnDefinitions colDef = boundStatement.getPreparedStatement().getVariableDefinitions();
        for (int i = 0; i < colDef.size(); i++) {
            if (boundStatement.isNull(colDef.get(i).getName())) {
                boundStatement = boundStatement.unset(colDef.get(i).getName());
            }
        }
        return boundStatement;
    }

    private static String emptyStringForNullValue(String value) {
        return value == null ? "" : value;
    }

    private static String nullValueForEmptyString(String value) {
        return StringUtils.isBlank(value) ? null : value;
    }

    // network

    public List<Resource<NetworkAttributes>> getNetworks() {
        SimpleStatement simpleStatement = selectFrom(NETWORK).columns(
                "uuid",
                "id",
                "properties",
                ALIASES_WITHOUT_TYPE,
                ALIAS_BY_TYPE,
                "caseDate",
                "forecastDistance",
                "sourceFormat",
                "connectedComponentsValid",
                "synchronousComponentsValid",
                CGMES_SV_METADATA,
                CGMES_SSH_METADATA,
                CIM_CHARACTERISTICS,
                "fictitious",
                ID_BY_ALIAS,
                CGMES_CONTROL_AREAS,
                CGMES_IIDM_MAPPING).build();
        ResultSet resultSet = session.execute(simpleStatement);

        List<Resource<NetworkAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.networkBuilder()
                    .id(row.getString(1))
                    .attributes(NetworkAttributes.builder()
                            .uuid(row.getUuid(0))
                            .properties(row.getMap(2, String.class, String.class))
                            .aliasesWithoutType(row.getSet(3, String.class))
                            .aliasByType(row.getMap(4, String.class, String.class))
                            .caseDate(new DateTime(row.getInstant(5).toEpochMilli()))
                            .forecastDistance(row.getInt(6))
                            .sourceFormat(row.getString(7))
                            .connectedComponentsValid(row.getBoolean(8))
                            .synchronousComponentsValid(row.getBoolean(9))
                            .cgmesSvMetadata(row.get(10, CgmesSvMetadataAttributes.class))
                            .cgmesSshMetadata(row.get(11, CgmesSshMetadataAttributes.class))
                            .cimCharacteristics(row.get(12, CimCharacteristicsAttributes.class))
                            .fictitious(row.getBoolean(13))
                            .idByAlias(row.getMap(14, String.class, String.class))
                            .cgmesControlAreas(row.get(15, CgmesControlAreasAttributes.class))
                            .cgmesIidmMapping(row.get(16, CgmesIidmMappingAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public Optional<Resource<NetworkAttributes>> getNetwork(UUID uuid) {
        ResultSet resultSet = session.execute(selectFrom(NETWORK).columns(
                "id",
                "properties",
                ALIASES_WITHOUT_TYPE,
                ALIAS_BY_TYPE,
                "caseDate",
                "forecastDistance",
                "sourceFormat",
                "connectedComponentsValid",
                "synchronousComponentsValid",
                CGMES_SV_METADATA,
                CGMES_SSH_METADATA,
                CIM_CHARACTERISTICS,
                "fictitious",
                ID_BY_ALIAS,
                CGMES_CONTROL_AREAS,
                CGMES_IIDM_MAPPING)
                .whereColumn("uuid").isEqualTo(literal(uuid)).build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.networkBuilder()
                    .id(one.getString(0))
                    .attributes(NetworkAttributes.builder()
                            .uuid(uuid)
                            .properties(one.getMap(1, String.class, String.class))
                            .aliasesWithoutType(one.getSet(2, String.class))
                            .aliasByType(one.getMap(3, String.class, String.class))
                            .caseDate(new DateTime(one.getInstant(4).toEpochMilli()))
                            .forecastDistance(one.getInt(5))
                            .sourceFormat(one.getString(6))
                            .connectedComponentsValid(one.getBoolean(7))
                            .synchronousComponentsValid(one.getBoolean(8))
                            .cgmesSvMetadata(one.get(9, CgmesSvMetadataAttributes.class))
                            .cgmesSshMetadata(one.get(10, CgmesSshMetadataAttributes.class))
                            .cimCharacteristics(one.get(11, CimCharacteristicsAttributes.class))
                            .fictitious(one.getBoolean(12))
                            .idByAlias(one.getMap(13, String.class, String.class))
                            .cgmesControlAreas(one.get(14, CgmesControlAreasAttributes.class))
                            .cgmesIidmMapping(one.get(15, CgmesIidmMappingAttributes.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public void createNetworks(List<Resource<NetworkAttributes>> resources) {
        for (List<Resource<NetworkAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<NetworkAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertNetwork.bind(
                        resource.getAttributes().getUuid(),
                        resource.getId(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getIdByAlias(),
                        resource.getAttributes().getCaseDate().toDate().toInstant(),
                        resource.getAttributes().getForecastDistance(),
                        resource.getAttributes().getSourceFormat(),
                        resource.getAttributes().isConnectedComponentsValid(),
                        resource.getAttributes().isSynchronousComponentsValid(),
                        resource.getAttributes().getCgmesSvMetadata(),
                        resource.getAttributes().getCgmesSshMetadata(),
                        resource.getAttributes().getCimCharacteristics(),
                        resource.getAttributes().getCgmesControlAreas(),
                        resource.getAttributes().getCgmesIidmMapping()
                )));
            }
            session.execute(batch);
        }
    }

    public void updateNetworks(List<Resource<NetworkAttributes>> resources) {
        for (List<Resource<NetworkAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<NetworkAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateNetwork.bind(
                        resource.getId(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getIdByAlias(),
                        resource.getAttributes().getCaseDate().toDate().toInstant(),
                        resource.getAttributes().getForecastDistance(),
                        resource.getAttributes().getSourceFormat(),
                        resource.getAttributes().isConnectedComponentsValid(),
                        resource.getAttributes().isSynchronousComponentsValid(),
                        resource.getAttributes().getCgmesSvMetadata(),
                        resource.getAttributes().getCgmesSshMetadata(),
                        resource.getAttributes().getCimCharacteristics(),
                        resource.getAttributes().getCgmesControlAreas(),
                        resource.getAttributes().getCgmesIidmMapping(),
                        resource.getAttributes().getUuid())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteNetwork(UUID uuid) {
        BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
        batch = batch.add(deleteFrom(NETWORK).whereColumn("uuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(SUBSTATION).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(VOLTAGE_LEVEL).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(BUSBAR_SECTION).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(SWITCH).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(GENERATOR).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(BATTERY).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(LOAD).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(SHUNT_COMPENSATOR).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(STATIC_VAR_COMPENSATOR).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(VSC_CONVERTER_STATION).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(LCC_CONVERTER_STATION).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(TWO_WINDINGS_TRANSFORMER).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(THREE_WINDINGS_TRANSFORMER).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(LINE).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(HVDC_LINE).whereColumn("networkUuid").isEqualTo(literal(uuid)).build())
                .add(deleteFrom(DANGLING_LINE).whereColumn("networkUuid").isEqualTo(literal(uuid)).build());

        session.execute(batch);
    }

    // substation

    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        ResultSet resultSet = session.execute(
                selectFrom(SUBSTATION)
                        .columns(
                                "id",
                                "name",
                                "properties",
                                ALIASES_WITHOUT_TYPE,
                                ALIAS_BY_TYPE,
                                "country",
                                "tso",
                                "entsoeArea",
                                "fictitious",
                                "geographicalTags")
                        .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                        .build());
        List<Resource<SubstationAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.substationBuilder()
                    .id(row.getString(0))
                    .attributes(SubstationAttributes.builder()
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .aliasesWithoutType(row.getSet(3, String.class))
                            .aliasByType(row.getMap(4, String.class, String.class))
                            .country(row.getString(5) != null ? Country.valueOf(row.getString(5)) : null)
                            .tso(row.getString(6))
                            .entsoeArea(row.get(7, EntsoeAreaAttributes.class))
                            .fictitious(row.getBoolean(8))
                            .geographicalTags(row.getSet(9, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        ResultSet resultSet = session.execute(selectFrom(SUBSTATION)
                .columns("name",
                        "properties",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "country",
                        "tso",
                        "entsoeArea",
                        "fictitious",
                        "geographicalTags")
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(substationId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.substationBuilder()
                    .id(substationId)
                    .attributes(SubstationAttributes.builder()
                            .name(one.getString(0))
                            .properties(one.getMap(1, String.class, String.class))
                            .aliasesWithoutType(one.getSet(2, String.class))
                            .aliasByType(one.getMap(3, String.class, String.class))
                            .country(one.getString(4) != null ? Country.valueOf(one.getString(4)) : null)
                            .tso(one.getString(5))
                            .entsoeArea(one.get(6, EntsoeAreaAttributes.class))
                            .fictitious(one.getBoolean(7))
                            .geographicalTags(one.getSet(8, String.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        for (List<Resource<SubstationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<SubstationAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertSubstation.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getCountry() != null ? resource.getAttributes().getCountry().toString() : null,
                        resource.getAttributes().getTso(),
                        resource.getAttributes().getEntsoeArea(),
                        resource.getAttributes().getGeographicalTags()
                )));
            }
            session.execute(batch);
        }
    }

    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        for (List<Resource<SubstationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<SubstationAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateSubstation.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getCountry() != null ? resource.getAttributes().getCountry().toString() : null,
                        resource.getAttributes().getTso(),
                        resource.getAttributes().getEntsoeArea(),
                        resource.getAttributes().getGeographicalTags(),
                        networkUuid,
                        resource.getId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteSubstation(UUID networkUuid, String substationId) {
        session.execute(deleteFrom(SUBSTATION)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(substationId))
                .build());
    }

    // voltage level

    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        for (List<Resource<VoltageLevelAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<VoltageLevelAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertVoltageLevel.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getSubstationId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNominalV(),
                        resource.getAttributes().getLowVoltageLimit(),
                        resource.getAttributes().getHighVoltageLimit(),
                        resource.getAttributes().getTopologyKind().toString(),
                        resource.getAttributes().getInternalConnections(),
                        resource.getAttributes().getCalculatedBusesForBusView(),
                        resource.getAttributes().getNodeToCalculatedBusForBusView(),
                        resource.getAttributes().getBusToCalculatedBusForBusView(),
                        resource.getAttributes().getCalculatedBusesForBusBreakerView(),
                        resource.getAttributes().getNodeToCalculatedBusForBusBreakerView(),
                        resource.getAttributes().getBusToCalculatedBusForBusBreakerView(),
                        resource.getAttributes().isCalculatedBusesValid(),
                        resource.getAttributes().getSlackTerminal()
                )));
            }
            session.execute(batch);
        }
    }

    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        for (List<Resource<VoltageLevelAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<VoltageLevelAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateVoltageLevel.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNominalV(),
                        resource.getAttributes().getLowVoltageLimit(),
                        resource.getAttributes().getHighVoltageLimit(),
                        resource.getAttributes().getTopologyKind().toString(),
                        resource.getAttributes().getInternalConnections(),
                        resource.getAttributes().getCalculatedBusesForBusView(),
                        resource.getAttributes().getNodeToCalculatedBusForBusView(),
                        resource.getAttributes().getBusToCalculatedBusForBusView(),
                        resource.getAttributes().getCalculatedBusesForBusBreakerView(),
                        resource.getAttributes().getNodeToCalculatedBusForBusBreakerView(),
                        resource.getAttributes().getBusToCalculatedBusForBusBreakerView(),
                        resource.getAttributes().isCalculatedBusesValid(),
                        resource.getAttributes().getSlackTerminal(),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getSubstationId())
                ));
            }
            session.execute(batch);
        }
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, String substationId) {
        ResultSet resultSet = session.execute(selectFrom("voltageLevelBySubstation")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "nominalV",
                        "lowVoltageLimit",
                        "highVoltageLimit",
                        "topologyKind",
                        "internalConnections",
                        "calculatedBusesForBusView",
                        "nodeToCalculatedBusForBusView",
                        "busToCalculatedBusForBusView",
                        "calculatedBusesForBusBreakerView",
                        "nodeToCalculatedBusForBusBreakerView",
                        "busToCalculatedBusForBusBreakerView",
                        "calculatedBusesValid",
                        "fictitious",
                        SLACK_TERMINAL,
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("substationId").isEqualTo(literal(substationId))
                .build());
        List<Resource<VoltageLevelAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.voltageLevelBuilder()
                    .id(row.getString(0))
                    .attributes(VoltageLevelAttributes.builder()
                            .substationId(substationId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .nominalV(row.getDouble(3))
                            .lowVoltageLimit(row.getDouble(4))
                            .highVoltageLimit(row.getDouble(5))
                            .topologyKind(TopologyKind.valueOf(row.getString(6)))
                            .internalConnections(row.getList(7, InternalConnectionAttributes.class))
                            .calculatedBusesForBusView(row.isNull(8) ? null : row.getList(8, CalculatedBusAttributes.class))
                            .nodeToCalculatedBusForBusView(row.isNull(9) ? null : row.getMap(9, Integer.class, Integer.class))
                            .busToCalculatedBusForBusView(row.isNull(10) ? null : row.getMap(10, String.class, Integer.class))
                            .calculatedBusesForBusBreakerView(row.isNull(11) ? null : row.getList(11, CalculatedBusAttributes.class))
                            .nodeToCalculatedBusForBusBreakerView(row.isNull(12) ? null : row.getMap(12, Integer.class, Integer.class))
                            .busToCalculatedBusForBusBreakerView(row.isNull(13) ? null : row.getMap(13, String.class, Integer.class))
                            .calculatedBusesValid(row.getBoolean(14))
                            .fictitious(row.getBoolean(15))
                            .slackTerminal(row.get(16, TerminalRefAttributes.class))
                            .aliasesWithoutType(row.getSet(17, String.class))
                            .aliasByType(row.getMap(18, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom(VOLTAGE_LEVEL)
                .columns(
                        "substationId",
                        "name",
                        "properties",
                        "nominalV",
                        "lowVoltageLimit",
                        "highVoltageLimit",
                        "topologyKind",
                        "internalConnections",
                        "calculatedBusesForBusView",
                        "nodeToCalculatedBusForBusView",
                        "busToCalculatedBusForBusView",
                        "calculatedBusesForBusBreakerView",
                        "nodeToCalculatedBusForBusBreakerView",
                        "busToCalculatedBusForBusBreakerView",
                        "calculatedBusesValid",
                        "fictitious",
                        SLACK_TERMINAL,
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(voltageLevelId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.voltageLevelBuilder()
                    .id(voltageLevelId)
                    .attributes(VoltageLevelAttributes.builder()
                            .substationId(one.getString(0))
                            .name(one.getString(1))
                            .properties(one.getMap(2, String.class, String.class))
                            .nominalV(one.getDouble(3))
                            .lowVoltageLimit(one.getDouble(4))
                            .highVoltageLimit(one.getDouble(5))
                            .topologyKind(TopologyKind.valueOf(one.getString(6)))
                            .internalConnections(one.getList(7, InternalConnectionAttributes.class))
                            .calculatedBusesForBusView(one.isNull(8) ? null : one.getList(8, CalculatedBusAttributes.class))
                            .nodeToCalculatedBusForBusView(one.isNull(9) ? null : one.getMap(9, Integer.class, Integer.class))
                            .busToCalculatedBusForBusView(one.isNull(10) ? null : one.getMap(10, String.class, Integer.class))
                            .calculatedBusesForBusBreakerView(one.isNull(11) ? null : one.getList(11, CalculatedBusAttributes.class))
                            .nodeToCalculatedBusForBusBreakerView(one.isNull(12) ? null : one.getMap(12, Integer.class, Integer.class))
                            .busToCalculatedBusForBusBreakerView(one.isNull(13) ? null : one.getMap(13, String.class, Integer.class))
                            .calculatedBusesValid(one.getBoolean(14))
                            .fictitious(one.getBoolean(15))
                            .slackTerminal(one.get(16, TerminalRefAttributes.class))
                            .aliasesWithoutType(one.getSet(17, String.class))
                            .aliasByType(one.getMap(18, String.class, String.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(VOLTAGE_LEVEL)
                .columns(
                        "id",
                        "substationId",
                        "name",
                        "properties",
                        "nominalV",
                        "lowVoltageLimit",
                        "highVoltageLimit",
                        "topologyKind",
                        "internalConnections",
                        "calculatedBusesForBusView",
                        "nodeToCalculatedBusForBusView",
                        "busToCalculatedBusForBusView",
                        "calculatedBusesForBusBreakerView",
                        "nodeToCalculatedBusForBusBreakerView",
                        "busToCalculatedBusForBusBreakerView",
                        "calculatedBusesValid",
                        "fictitious",
                        SLACK_TERMINAL,
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<VoltageLevelAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.voltageLevelBuilder()
                    .id(row.getString(0))
                    .attributes(VoltageLevelAttributes.builder()
                            .substationId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .nominalV(row.getDouble(4))
                            .lowVoltageLimit(row.getDouble(5))
                            .highVoltageLimit(row.getDouble(6))
                            .topologyKind(TopologyKind.valueOf(row.getString(7)))
                            .internalConnections(row.getList(8, InternalConnectionAttributes.class))
                            .calculatedBusesForBusView(row.isNull(9) ? null : row.getList(9, CalculatedBusAttributes.class))
                            .nodeToCalculatedBusForBusView(row.isNull(10) ? null : row.getMap(10, Integer.class, Integer.class))
                            .busToCalculatedBusForBusView(row.isNull(11) ? null : row.getMap(11, String.class, Integer.class))
                            .calculatedBusesForBusBreakerView(row.isNull(12) ? null : row.getList(12, CalculatedBusAttributes.class))
                            .nodeToCalculatedBusForBusBreakerView(row.isNull(13) ? null : row.getMap(13, Integer.class, Integer.class))
                            .busToCalculatedBusForBusBreakerView(row.isNull(14) ? null : row.getMap(14, String.class, Integer.class))
                            .calculatedBusesValid(row.getBoolean(15))
                            .fictitious(row.getBoolean(16))
                            .slackTerminal(row.get(17, TerminalRefAttributes.class))
                            .aliasesWithoutType(row.getSet(18, String.class))
                            .aliasByType(row.getMap(19, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void deleteVoltageLevel(UUID networkUuid, String voltageLevelId) {
        session.execute(deleteFrom(VOLTAGE_LEVEL)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(voltageLevelId))
                .build());
    }

    // generator

    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        for (List<Resource<GeneratorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<GeneratorAttributes> resource : subresources) {
                ReactiveLimitsAttributes reactiveLimits = resource.getAttributes().getReactiveLimits();
                batch = batch.add(unsetNullValues(psInsertGenerator.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getEnergySource().toString(),
                        resource.getAttributes().getMinP(),
                        resource.getAttributes().getMaxP(),
                        resource.getAttributes().isVoltageRegulatorOn(),
                        resource.getAttributes().getTargetP(),
                        resource.getAttributes().getTargetQ(),
                        resource.getAttributes().getTargetV(),
                        resource.getAttributes().getRatedS(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX ? reactiveLimits : null,
                        reactiveLimits.getKind() == ReactiveLimitsKind.CURVE ? reactiveLimits : null,
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getActivePowerControl(),
                        resource.getAttributes().getRegulatingTerminal(),
                        resource.getAttributes().getCoordinatedReactiveControl())));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        ResultSet resultSet = session.execute(selectFrom(GENERATOR)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "energySource",
                        "minP",
                        "maxP",
                        "voltageRegulatorOn",
                        "targetP",
                        "targetQ",
                        "targetV",
                        "ratedS",
                        "p",
                        "q",
                        "position",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "bus",
                        CONNECTABLE_BUS,
                        "activePowerControl",
                        REGULATING_TERMINAL,
                        "coordinatedReactiveControl",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(generatorId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = one.get(15, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = one.get(16, ReactiveCapabilityCurveAttributes.class);
            return Optional.of(Resource.generatorBuilder()
                    .id(generatorId)
                    .attributes(GeneratorAttributes.builder()
                            .voltageLevelId(one.getString(0))
                            .name(one.getString(1))
                            .properties(one.getMap(2, String.class, String.class))
                            .node(one.get(3, Integer.class))
                            .energySource(EnergySource.valueOf(one.getString(4)))
                            .minP(one.getDouble(5))
                            .maxP(one.getDouble(6))
                            .voltageRegulatorOn(one.getBoolean(7))
                            .targetP(one.getDouble(8))
                            .targetQ(one.getDouble(9))
                            .targetV(one.getDouble(10))
                            .ratedS(one.getDouble(11))
                            .p(one.getDouble(12))
                            .q(one.getDouble(13))
                            .position(one.get(14, ConnectablePositionAttributes.class))
                            .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                            .bus(nullValueForEmptyString(one.getString(17)))
                            .connectableBus(one.getString(18))
                            .activePowerControl(one.get(19, ActivePowerControlAttributes.class))
                            .regulatingTerminal(one.get(20, TerminalRefAttributes.class))
                            .coordinatedReactiveControl(one.get(21, CoordinatedReactiveControlAttributes.class))
                            .fictitious(one.getBoolean(22))
                            .aliasesWithoutType(one.getSet(23, String.class))
                            .aliasByType(one.getMap(24, String.class, String.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(GENERATOR)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "energySource",
                        "minP",
                        "maxP",
                        "voltageRegulatorOn",
                        "targetP",
                        "targetQ",
                        "targetV",
                        "ratedS",
                        "p",
                        "q",
                        "position",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "bus",
                        CONNECTABLE_BUS,
                        "activePowerControl",
                        REGULATING_TERMINAL,
                        "coordinatedReactiveControl",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<GeneratorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(16, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(17, ReactiveCapabilityCurveAttributes.class);
            resources.add(Resource.generatorBuilder()
                    .id(row.getString(0))
                    .attributes(GeneratorAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.get(4, Integer.class))
                            .energySource(EnergySource.valueOf(row.getString(5)))
                            .minP(row.getDouble(6))
                            .maxP(row.getDouble(7))
                            .voltageRegulatorOn(row.getBoolean(8))
                            .targetP(row.getDouble(9))
                            .targetQ(row.getDouble(10))
                            .targetV(row.getDouble(11))
                            .ratedS(row.getDouble(12))
                            .p(row.getDouble(13))
                            .q(row.getDouble(14))
                            .position(row.get(15, ConnectablePositionAttributes.class))
                            .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                            .bus(nullValueForEmptyString(row.getString(18)))
                            .connectableBus(row.getString(19))
                            .activePowerControl(row.get(20, ActivePowerControlAttributes.class))
                            .regulatingTerminal(row.get(21, TerminalRefAttributes.class))
                            .coordinatedReactiveControl(row.get(22, CoordinatedReactiveControlAttributes.class))
                            .fictitious(row.getBoolean(23))
                            .aliasesWithoutType(row.getSet(24, String.class))
                            .aliasByType(row.getMap(25, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("generatorByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        "energySource",
                        "minP",
                        "maxP",
                        "voltageRegulatorOn",
                        "targetP",
                        "targetQ",
                        "targetV",
                        "ratedS",
                        "p",
                        "q",
                        "position",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "bus",
                        CONNECTABLE_BUS,
                        "activePowerControl",
                        REGULATING_TERMINAL,
                        "coordinatedReactiveControl",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<GeneratorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(15, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(16, ReactiveCapabilityCurveAttributes.class);
            resources.add(Resource.generatorBuilder()
                    .id(row.getString(0))
                    .attributes(GeneratorAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.get(3, Integer.class))
                            .energySource(EnergySource.valueOf(row.getString(4)))
                            .minP(row.getDouble(5))
                            .maxP(row.getDouble(6))
                            .voltageRegulatorOn(row.getBoolean(7))
                            .targetP(row.getDouble(8))
                            .targetQ(row.getDouble(9))
                            .targetV(row.getDouble(10))
                            .ratedS(row.getDouble(11))
                            .p(row.getDouble(12))
                            .q(row.getDouble(13))
                            .position(row.get(14, ConnectablePositionAttributes.class))
                            .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                            .bus(nullValueForEmptyString(row.getString(17)))
                            .connectableBus(row.getString(18))
                            .activePowerControl(row.get(19, ActivePowerControlAttributes.class))
                            .regulatingTerminal(row.get(20, TerminalRefAttributes.class))
                            .coordinatedReactiveControl(row.get(21, CoordinatedReactiveControlAttributes.class))
                            .aliasesWithoutType(row.getSet(22, String.class))
                            .aliasByType(row.getMap(23, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        for (List<Resource<GeneratorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<GeneratorAttributes> resource : subresources) {
                ReactiveLimitsAttributes reactiveLimits = resource.getAttributes().getReactiveLimits();
                batch = batch.add(unsetNullValues(psUpdateGenerator.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getEnergySource().toString(),
                        resource.getAttributes().getMinP(),
                        resource.getAttributes().getMaxP(),
                        resource.getAttributes().isVoltageRegulatorOn(),
                        resource.getAttributes().getTargetP(),
                        resource.getAttributes().getTargetQ(),
                        resource.getAttributes().getTargetV(),
                        resource.getAttributes().getRatedS(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX ? reactiveLimits : null,
                        reactiveLimits.getKind() == ReactiveLimitsKind.CURVE ? reactiveLimits : null,
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getActivePowerControl(),
                        resource.getAttributes().getRegulatingTerminal(),
                        resource.getAttributes().getCoordinatedReactiveControl(),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteGenerator(UUID networkUuid, String generatorId) {
        session.execute(deleteFrom(GENERATOR)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(generatorId))
                .build());
    }

    // battery

    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        for (List<Resource<BatteryAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<BatteryAttributes> resource : subresources) {
                ReactiveLimitsAttributes reactiveLimits = resource.getAttributes().getReactiveLimits();
                batch = batch.add(unsetNullValues(psInsertBattery.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getMinP(),
                        resource.getAttributes().getMaxP(),
                        resource.getAttributes().getP0(),
                        resource.getAttributes().getQ0(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX ? reactiveLimits : null,
                        reactiveLimits.getKind() == ReactiveLimitsKind.CURVE ? reactiveLimits : null,
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getActivePowerControl())));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, String batteryId) {
        ResultSet resultSet = session.execute(selectFrom(BATTERY)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "minP",
                        "maxP",
                        "p0",
                        "q0",
                        "p",
                        "q",
                        "position",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "activePowerControl")
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(batteryId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = one.get(11, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = one.get(12, ReactiveCapabilityCurveAttributes.class);
            return Optional.of(Resource.batteryBuilder()
                    .id(batteryId)
                    .attributes(BatteryAttributes.builder()
                            .voltageLevelId(one.getString(0))
                            .name(one.getString(1))
                            .properties(one.getMap(2, String.class, String.class))
                            .node(one.get(3, Integer.class))
                            .minP(one.getDouble(4))
                            .maxP(one.getDouble(5))
                            .p0(one.getDouble(6))
                            .q0(one.getDouble(7))
                            .p(one.getDouble(8))
                            .q(one.getDouble(9))
                            .position(one.get(10, ConnectablePositionAttributes.class))
                            .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                            .bus(nullValueForEmptyString(one.getString(13)))
                            .connectableBus(one.getString(14))
                            .fictitious(one.getBoolean(15))
                            .aliasesWithoutType(one.getSet(16, String.class))
                            .aliasByType(one.getMap(17, String.class, String.class))
                            .activePowerControl(one.get(18, ActivePowerControlAttributes.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(BATTERY)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "minP",
                        "maxP",
                        "p0",
                        "q0",
                        "p",
                        "q",
                        "position",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "activePowerControl")
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<BatteryAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(12, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(13, ReactiveCapabilityCurveAttributes.class);
            resources.add(Resource.batteryBuilder()
                    .id(row.getString(0))
                    .attributes(BatteryAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.get(4, Integer.class))
                            .minP(row.getDouble(5))
                            .maxP(row.getDouble(6))
                            .p0(row.getDouble(7))
                            .q0(row.getDouble(8))
                            .p(row.getDouble(9))
                            .q(row.getDouble(10))
                            .position(row.get(11, ConnectablePositionAttributes.class))
                            .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                            .bus(nullValueForEmptyString(row.getString(14)))
                            .connectableBus(row.getString(15))
                            .fictitious(row.getBoolean(16))
                            .aliasesWithoutType(row.getSet(17, String.class))
                            .aliasByType(row.getMap(18, String.class, String.class))
                            .activePowerControl(row.get(19, ActivePowerControlAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("batteryByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        "minP",
                        "maxP",
                        "p0",
                        "q0",
                        "p",
                        "q",
                        "position",
                        "minMaxReactiveLimits",
                        "reactiveCapabilityCurve",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        "activePowerControl")
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<BatteryAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(11, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(12, ReactiveCapabilityCurveAttributes.class);
            resources.add(Resource.batteryBuilder()
                    .id(row.getString(0))
                    .attributes(BatteryAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.get(3, Integer.class))
                            .minP(row.getDouble(4))
                            .maxP(row.getDouble(5))
                            .p0(row.getDouble(6))
                            .q0(row.getDouble(7))
                            .p(row.getDouble(8))
                            .q(row.getDouble(9))
                            .position(row.get(10, ConnectablePositionAttributes.class))
                            .reactiveLimits(minMaxReactiveLimitsAttributes != null ? minMaxReactiveLimitsAttributes : reactiveCapabilityCurveAttributes)
                            .bus(nullValueForEmptyString(row.getString(13)))
                            .connectableBus(row.getString(14))
                            .fictitious(row.getBoolean(15))
                            .aliasesWithoutType(row.getSet(16, String.class))
                            .aliasByType(row.getMap(17, String.class, String.class))
                            .activePowerControl(row.get(18, ActivePowerControlAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        for (List<Resource<BatteryAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<BatteryAttributes> resource : subresources) {
                ReactiveLimitsAttributes reactiveLimits = resource.getAttributes().getReactiveLimits();
                batch = batch.add(unsetNullValues(psUpdateBattery.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getMinP(),
                        resource.getAttributes().getMaxP(),
                        resource.getAttributes().getP0(),
                        resource.getAttributes().getQ0(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX ? reactiveLimits : null,
                        reactiveLimits.getKind() == ReactiveLimitsKind.CURVE ? reactiveLimits : null,
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getActivePowerControl(),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteBattery(UUID networkUuid, String batteryId) {
        session.execute(deleteFrom(BATTERY)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(batteryId))
                .build());
    }

    // load

    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        for (List<Resource<LoadAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<LoadAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertLoad.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getLoadType().toString(),
                        resource.getAttributes().getP0(),
                        resource.getAttributes().getQ0(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getLoadDetail()
                )));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        ResultSet resultSet = session.execute(selectFrom(LOAD)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "loadType",
                        "p0",
                        "q0",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        LOAD_DETAIL,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(loadId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.loadBuilder()
                    .id(loadId)
                    .attributes(LoadAttributes.builder()
                            .voltageLevelId(one.getString(0))
                            .name(one.getString(1))
                            .properties(one.getMap(2, String.class, String.class))
                            .node(one.get(3, Integer.class))
                            .loadType(LoadType.valueOf(one.getString(4)))
                            .p0(one.getDouble(5))
                            .q0(one.getDouble(6))
                            .p(one.getDouble(7))
                            .q(one.getDouble(8))
                            .position(one.get(9, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(one.getString(10)))
                            .connectableBus(one.getString(11))
                            .loadDetail(one.get(12, LoadDetailAttributes.class))
                            .fictitious(one.getBoolean(13))
                            .aliasesWithoutType(one.getSet(14, String.class))
                            .aliasByType(one.getMap(15, String.class, String.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(LOAD)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "loadType",
                        "p0",
                        "q0",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        LOAD_DETAIL,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<LoadAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.loadBuilder()
                    .id(row.getString(0))
                    .attributes(LoadAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.get(4, Integer.class))
                            .loadType(LoadType.valueOf(row.getString(5)))
                            .p0(row.getDouble(6))
                            .q0(row.getDouble(7))
                            .p(row.getDouble(8))
                            .q(row.getDouble(9))
                            .position(row.get(10, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(row.getString(11)))
                            .connectableBus(row.getString(12))
                            .loadDetail(row.get(13, LoadDetailAttributes.class))
                            .fictitious(row.getBoolean(14))
                            .aliasesWithoutType(row.getSet(15, String.class))
                            .aliasByType(row.getMap(16, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("loadByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        "loadType",
                        "p0",
                        "q0",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        LOAD_DETAIL,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<LoadAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.loadBuilder()
                    .id(row.getString(0))
                    .attributes(LoadAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.get(3, Integer.class))
                            .loadType(LoadType.valueOf(row.getString(4)))
                            .p0(row.getDouble(5))
                            .q0(row.getDouble(6))
                            .p(row.getDouble(7))
                            .q(row.getDouble(8))
                            .position(row.get(9, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(row.getString(10)))
                            .connectableBus(row.getString(11))
                            .loadDetail(row.get(12, LoadDetailAttributes.class))
                            .fictitious(row.getBoolean(13))
                            .aliasesWithoutType(row.getSet(14, String.class))
                            .aliasByType(row.getMap(15, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        for (List<Resource<LoadAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<LoadAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateLoad.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getLoadType().toString(),
                        resource.getAttributes().getP0(),
                        resource.getAttributes().getQ0(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getLoadDetail(),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteLoad(UUID networkUuid, String loadId) {
        session.execute(deleteFrom(LOAD)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(loadId))
                .build());
    }

    // shunt compensator

    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        for (List<Resource<ShuntCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<ShuntCompensatorAttributes> resource : subresources) {
                ShuntCompensatorModelAttributes shuntCompensatorModel = resource.getAttributes().getModel();
                batch = batch.add(unsetNullValues(psInsertShuntCompensator.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        shuntCompensatorModel.getType() == ShuntCompensatorModelType.LINEAR ? shuntCompensatorModel : null,
                        shuntCompensatorModel.getType() == ShuntCompensatorModelType.NON_LINEAR ? shuntCompensatorModel : null,
                        resource.getAttributes().getSectionCount(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getRegulatingTerminal(),
                        resource.getAttributes().isVoltageRegulatorOn(),
                        resource.getAttributes().getTargetV(),
                        resource.getAttributes().getTargetDeadband()
                )));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        ResultSet resultSet = session.execute(selectFrom(SHUNT_COMPENSATOR)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        LINEAR_MODEL,
                        NON_LINEAR_MODEL,
                        SECTION_COUNT,
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        REGULATING_TERMINAL,
                        "voltageRegulatorOn",
                        "targetV",
                        "targetDeadband",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(shuntCompensatorId))
                .build());
        Row row = resultSet.one();
        if (row != null) {
            ShuntCompensatorLinearModelAttributes shuntCompensatorLinearModelAttributes = row.get(4, ShuntCompensatorLinearModelAttributes.class);
            ShuntCompensatorNonLinearModelAttributes shuntCompensatorNonLinearModelAttributes = row.get(5, ShuntCompensatorNonLinearModelAttributes.class);
            return Optional.of(Resource.shuntCompensatorBuilder()
                    .id(shuntCompensatorId)
                    .attributes(ShuntCompensatorAttributes.builder()
                            .voltageLevelId(row.getString(0))
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.get(3, Integer.class))
                            .model(shuntCompensatorLinearModelAttributes != null ? shuntCompensatorLinearModelAttributes : shuntCompensatorNonLinearModelAttributes)
                            .sectionCount(row.getInt(6))
                            .p(row.getDouble(7))
                            .q(row.getDouble(8))
                            .position(row.get(9, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(row.getString(10)))
                            .connectableBus(row.getString(11))
                            .regulatingTerminal(row.get(12, TerminalRefAttributes.class))
                            .voltageRegulatorOn(row.getBoolean(13))
                            .targetV(row.getDouble(14))
                            .targetDeadband(row.getDouble(15))
                            .fictitious(row.getBoolean(16))
                            .aliasesWithoutType(row.getSet(17, String.class))
                            .aliasByType(row.getMap(18, String.class, String.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(SHUNT_COMPENSATOR)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        LINEAR_MODEL,
                        NON_LINEAR_MODEL,
                        SECTION_COUNT,
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        REGULATING_TERMINAL,
                        "voltageRegulatorOn",
                        "targetV",
                        "targetDeadband",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<ShuntCompensatorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            ShuntCompensatorLinearModelAttributes shuntCompensatorLinearModelAttributes = row.get(5, ShuntCompensatorLinearModelAttributes.class);
            ShuntCompensatorNonLinearModelAttributes shuntCompensatorNonLinearModelAttributes = row.get(6, ShuntCompensatorNonLinearModelAttributes.class);
            resources.add(Resource.shuntCompensatorBuilder()
                    .id(row.getString(0))
                    .attributes(ShuntCompensatorAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.get(4, Integer.class))
                            .model(shuntCompensatorLinearModelAttributes != null ? shuntCompensatorLinearModelAttributes : shuntCompensatorNonLinearModelAttributes)
                            .sectionCount(row.getInt(7))
                            .p(row.getDouble(8))
                            .q(row.getDouble(9))
                            .position(row.get(10, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(row.getString(11)))
                            .connectableBus(row.getString(12))
                            .regulatingTerminal(row.get(13, TerminalRefAttributes.class))
                            .voltageRegulatorOn(row.getBoolean(14))
                            .targetV(row.getDouble(15))
                            .targetDeadband(row.getDouble(16))
                            .fictitious(row.getBoolean(17))
                            .aliasesWithoutType(row.getSet(18, String.class))
                            .aliasByType(row.getMap(19, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("shuntCompensatorByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        LINEAR_MODEL,
                        NON_LINEAR_MODEL,
                        SECTION_COUNT,
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        REGULATING_TERMINAL,
                        "voltageRegulatorOn",
                        "targetV",
                        "targetDeadband",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<ShuntCompensatorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            ShuntCompensatorLinearModelAttributes shuntCompensatorLinearModelAttributes = row.get(4, ShuntCompensatorLinearModelAttributes.class);
            ShuntCompensatorNonLinearModelAttributes shuntCompensatorNonLinearModelAttributes = row.get(5, ShuntCompensatorNonLinearModelAttributes.class);
            resources.add(Resource.shuntCompensatorBuilder()
                    .id(row.getString(0))
                    .attributes(ShuntCompensatorAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.get(3, Integer.class))
                            .model(shuntCompensatorLinearModelAttributes != null ? shuntCompensatorLinearModelAttributes : shuntCompensatorNonLinearModelAttributes)
                            .sectionCount(row.getInt(6))
                            .p(row.getDouble(7))
                            .q(row.getDouble(8))
                            .position(row.get(9, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(row.getString(10)))
                            .connectableBus(row.getString(11))
                            .regulatingTerminal(row.get(12, TerminalRefAttributes.class))
                            .voltageRegulatorOn(row.getBoolean(13))
                            .targetV(row.getDouble(14))
                            .targetDeadband(row.getDouble(15))
                            .fictitious(row.getBoolean(16))
                            .aliasesWithoutType(row.getSet(17, String.class))
                            .aliasByType(row.getMap(18, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        for (List<Resource<ShuntCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<ShuntCompensatorAttributes> resource : subresources) {
                ShuntCompensatorModelAttributes shuntCompensatorModel = resource.getAttributes().getModel();
                batch = batch.add(unsetNullValues(psUpdateShuntCompensator.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        shuntCompensatorModel.getType() == ShuntCompensatorModelType.LINEAR ? shuntCompensatorModel : null,
                        shuntCompensatorModel.getType() == ShuntCompensatorModelType.NON_LINEAR ? shuntCompensatorModel : null,
                        resource.getAttributes().getSectionCount(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getRegulatingTerminal(),
                        resource.getAttributes().isVoltageRegulatorOn(),
                        resource.getAttributes().getTargetV(),
                        resource.getAttributes().getTargetDeadband(),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        session.execute(deleteFrom(SHUNT_COMPENSATOR)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(shuntCompensatorId))
                .build());
    }

    // VSC converter station

    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        for (List<Resource<VscConverterStationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<VscConverterStationAttributes> resource : subresources) {
                ReactiveLimitsAttributes reactiveLimits = resource.getAttributes().getReactiveLimits();
                batch = batch.add(unsetNullValues(psInsertVscConverterStation.bind(
                        networkUuid,
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
            session.execute(batch);
        }
    }

    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        ResultSet resultSet = session.execute(selectFrom(VSC_CONVERTER_STATION)
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
                .whereColumn("id").isEqualTo(literal(vscConverterStationId))
                .build());
        Row row = resultSet.one();
        if (row != null) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(8, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(9, ReactiveCapabilityCurveAttributes.class);
            return Optional.of(Resource.vscConverterStationBuilder()
                    .id(vscConverterStationId)
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

    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(VSC_CONVERTER_STATION)
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
                .build());
        List<Resource<VscConverterStationAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(9, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(10, ReactiveCapabilityCurveAttributes.class);
            resources.add(Resource.vscConverterStationBuilder()
                    .id(row.getString(0))
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

    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("vscConverterStationByVoltageLevel")
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
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<VscConverterStationAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            MinMaxReactiveLimitsAttributes minMaxReactiveLimitsAttributes = row.get(8, MinMaxReactiveLimitsAttributes.class);
            ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = row.get(9, ReactiveCapabilityCurveAttributes.class);
            resources.add(Resource.vscConverterStationBuilder()
                    .id(row.getString(0))
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

    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        for (List<Resource<VscConverterStationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<VscConverterStationAttributes> resource : subresources) {
                ReactiveLimitsAttributes reactiveLimits = resource.getAttributes().getReactiveLimits();
                batch = batch.add(unsetNullValues(psUpdateVscConverterStation.bind(
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
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        session.execute(deleteFrom(VSC_CONVERTER_STATION)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(vscConverterStationId))
                .build());
    }

    // LCC converter station

    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        for (List<Resource<LccConverterStationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<LccConverterStationAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertLccConverterStation.bind(
                        networkUuid,
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
            session.execute(batch);
        }
    }

    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        ResultSet resultSet = session.execute(selectFrom(LCC_CONVERTER_STATION)
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
                .whereColumn("id").isEqualTo(literal(lccConverterStationId))
                .build());
        Row row = resultSet.one();
        if (row != null) {
            return Optional.of(Resource.lccConverterStationBuilder()
                    .id(lccConverterStationId)
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

    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(LCC_CONVERTER_STATION)
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
                .build());
        List<Resource<LccConverterStationAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.lccConverterStationBuilder()
                    .id(row.getString(0))
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

    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("lccConverterStationByVoltageLevel")
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
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<LccConverterStationAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.lccConverterStationBuilder()
                    .id(row.getString(0))
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

    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        for (List<Resource<LccConverterStationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<LccConverterStationAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateLccConverterStation.bind(
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
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        session.execute(deleteFrom(LCC_CONVERTER_STATION)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(lccConverterStationId))
                .build());
    }

    // static var compensators

    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        for (List<Resource<StaticVarCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<StaticVarCompensatorAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertStaticVarCompensator.bind(
                        networkUuid,
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
            session.execute(batch);
        }
    }

    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        ResultSet resultSet = session.execute(selectFrom(STATIC_VAR_COMPENSATOR)
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
                .whereColumn("id").isEqualTo(literal(staticVarCompensatorId))
                .build());
        Row row = resultSet.one();
        if (row != null) {
            return Optional.of(Resource.staticVarCompensatorBuilder()
                    .id(staticVarCompensatorId)
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

    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(STATIC_VAR_COMPENSATOR)
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
                .build());
        List<Resource<StaticVarCompensatorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.staticVarCompensatorBuilder()
                    .id(row.getString(0))
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

    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("staticVarCompensatorByVoltageLevel")
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
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<StaticVarCompensatorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.staticVarCompensatorBuilder()
                    .id(row.getString(0))
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

    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        for (List<Resource<StaticVarCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<StaticVarCompensatorAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateStaticVarCompensator.bind(
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
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        session.execute(deleteFrom(STATIC_VAR_COMPENSATOR)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(staticVarCompensatorId))
                .build());
    }

    // busbar section

    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        for (List<Resource<BusbarSectionAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<BusbarSectionAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertBusbarSection.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getPosition()
                )));
            }
            session.execute(batch);
        }
    }

    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        for (List<Resource<BusbarSectionAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<BusbarSectionAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateBusbarSection.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getPosition(),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        ResultSet resultSet = session.execute(selectFrom(BUSBAR_SECTION)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "position",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(busbarSectionId))
                .build());
        Row row = resultSet.one();
        if (row != null) {
            return Optional.of(Resource.busbarSectionBuilder()
                    .id(busbarSectionId)
                    .attributes(BusbarSectionAttributes.builder()
                            .voltageLevelId(row.getString(0))
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.getInt(3))
                            .position(row.get(4, BusbarSectionPositionAttributes.class))
                            .fictitious(row.getBoolean(5))
                            .aliasesWithoutType(row.getSet(6, String.class))
                            .aliasByType(row.getMap(7, String.class, String.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(BUSBAR_SECTION)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "position",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<BusbarSectionAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.busbarSectionBuilder()
                    .id(row.getString(0))
                    .attributes(BusbarSectionAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.getInt(4))
                            .position(row.get(5, BusbarSectionPositionAttributes.class))
                            .fictitious(row.getBoolean(6))
                            .aliasesWithoutType(row.getSet(7, String.class))
                            .aliasByType(row.getMap(8, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("busbarSectionByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        "position",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<BusbarSectionAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.busbarSectionBuilder()
                    .id(row.getString(0))
                    .attributes(BusbarSectionAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.getInt(3))
                            .position(row.get(4, BusbarSectionPositionAttributes.class))
                            .fictitious(row.getBoolean(5))
                            .aliasesWithoutType(row.getSet(6, String.class))
                            .aliasByType(row.getMap(7, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void deleteBusBarSection(UUID networkUuid, String busBarSectionId) {
        session.execute(deleteFrom(BUSBAR_SECTION)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(busBarSectionId))
                .build());
    }

    // switch

    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        for (List<Resource<SwitchAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<SwitchAttributes> resource : subresources) {
                String kind = resource.getAttributes().getKind() != null ? resource.getAttributes().getKind().toString() : null;
                batch = batch.add(unsetNullValues(psInsertSwitch.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode1(),
                        resource.getAttributes().getNode2(),
                        resource.getAttributes().isOpen(),
                        resource.getAttributes().isRetained(),
                        resource.getAttributes().isFictitious(),
                        kind,
                        emptyStringForNullValue(resource.getAttributes().getBus1()),
                        emptyStringForNullValue(resource.getAttributes().getBus2())
                )));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        ResultSet resultSet = session.execute(selectFrom(SWITCH)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "kind",
                        "node1",
                        "node2",
                        "open",
                        "retained",
                        "fictitious",
                        "bus1",
                        "bus2",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(switchId))
                .build());
        Row row = resultSet.one();
        if (row != null) {
            return Optional.of(Resource.switchBuilder()
                    .id(switchId)
                    .attributes(SwitchAttributes.builder()
                            .voltageLevelId(row.getString(0))
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .kind(SwitchKind.valueOf(row.getString(3)))
                            .node1(row.get(4, Integer.class))
                            .node2(row.get(5, Integer.class))
                            .open(row.getBoolean(6))
                            .retained(row.getBoolean(7))
                            .fictitious(row.getBoolean(8))
                            .bus1(nullValueForEmptyString(row.getString(9)))
                            .bus2(nullValueForEmptyString(row.getString(10)))
                            .aliasesWithoutType(row.getSet(11, String.class))
                            .aliasByType(row.getMap(12, String.class, String.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(SWITCH)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "kind",
                        "node1",
                        "node2",
                        "open",
                        "retained",
                        "fictitious",
                        "bus1",
                        "bus2",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<SwitchAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.switchBuilder()
                    .id(row.getString(0))
                    .attributes(SwitchAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .kind(SwitchKind.valueOf(row.getString(4)))
                            .node1(row.get(5, Integer.class))
                            .node2(row.get(6, Integer.class))
                            .open(row.getBoolean(7))
                            .retained(row.getBoolean(8))
                            .fictitious(row.getBoolean(9))
                            .bus1(nullValueForEmptyString(row.getString(10)))
                            .bus2(nullValueForEmptyString(row.getString(11)))
                            .aliasesWithoutType(row.getSet(12, String.class))
                            .aliasByType(row.getMap(13, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("switchByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "kind",
                        "node1",
                        "node2",
                        "open",
                        "retained",
                        "fictitious",
                        "bus1",
                        "bus2",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<SwitchAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.switchBuilder()
                    .id(row.getString(0))
                    .attributes(SwitchAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .kind(SwitchKind.valueOf(row.getString(3)))
                            .node1(row.get(4, Integer.class))
                            .node2(row.get(5, Integer.class))
                            .open(row.getBoolean(6))
                            .retained(row.getBoolean(7))
                            .fictitious(row.getBoolean(8))
                            .bus1(nullValueForEmptyString(row.getString(9)))
                            .bus2(nullValueForEmptyString(row.getString(10)))
                            .aliasesWithoutType(row.getSet(11, String.class))
                            .aliasByType(row.getMap(12, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        for (List<Resource<SwitchAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<SwitchAttributes> resource : subresources) {
                String kind = resource.getAttributes().getKind() != null ? resource.getAttributes().getKind().toString() : null;
                batch = batch.add(unsetNullValues(psUpdateSwitch.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode1(),
                        resource.getAttributes().getNode2(),
                        resource.getAttributes().isOpen(),
                        resource.getAttributes().isRetained(),
                        resource.getAttributes().isFictitious(),
                        kind,
                        emptyStringForNullValue(resource.getAttributes().getBus1()),
                        emptyStringForNullValue(resource.getAttributes().getBus2()),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteSwitch(UUID networkUuid, String switchId) {
        session.execute(deleteFrom(SWITCH)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(switchId))
                .build());
    }

    // 2 windings transformer

    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        for (List<Resource<TwoWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<TwoWindingsTransformerAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertTwoWindingsTransformer.bind(
                        networkUuid,
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
                        resource.getAttributes().getBranchStatus()
                )));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        ResultSet resultSet = session.execute(selectFrom(TWO_WINDINGS_TRANSFORMER)
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
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(twoWindingsTransformerId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.twoWindingsTransformerBuilder()
                    .id(twoWindingsTransformerId)
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
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(TWO_WINDINGS_TRANSFORMER)
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
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<TwoWindingsTransformerAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.twoWindingsTransformerBuilder()
                    .id(row.getString(0))
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
                            .build())
                    .build());
        }
        return resources;
    }

    private List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, Branch.Side side, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("twoWindingsTransformerByVoltageLevel" + (side == Branch.Side.ONE ? 1 : 2))
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
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId" + (side == Branch.Side.ONE ? 1 : 2)).isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<TwoWindingsTransformerAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.twoWindingsTransformerBuilder()
                    .id(row.getString(0))
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
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return ImmutableList.<Resource<TwoWindingsTransformerAttributes>>builder().addAll(
                ImmutableSet.<Resource<TwoWindingsTransformerAttributes>>builder()
                        .addAll(getVoltageLevelTwoWindingsTransformers(networkUuid, Branch.Side.ONE, voltageLevelId))
                        .addAll(getVoltageLevelTwoWindingsTransformers(networkUuid, Branch.Side.TWO, voltageLevelId))
                        .build())
                .build();
    }

    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        for (List<Resource<TwoWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<TwoWindingsTransformerAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateTwoWindingsTransformer.bind(
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
                        networkUuid,
                        resource.getId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        session.execute(deleteFrom(TWO_WINDINGS_TRANSFORMER)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(twoWindingsTransformerId))
                .build());
    }

    // 3 windings transformer

    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources) {
        for (List<Resource<ThreeWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<ThreeWindingsTransformerAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertThreeWindingsTransformer.bind(
                        networkUuid,
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
                        resource.getAttributes().getBranchStatus()
                )));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
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
                        "phaseTapChanger1",
                        "ratioTapChanger1",
                        "voltageLevelId3",
                        "node3",
                        "r3",
                        "x3",
                        "g3",
                        "b3",
                        "ratedU3",
                        "p3",
                        "q3",
                        "phaseTapChanger1",
                        "ratioTapChanger1",
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
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(threeWindingsTransformerId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.threeWindingsTransformerBuilder()
                    .id(threeWindingsTransformerId)
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
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
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
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<ThreeWindingsTransformerAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.threeWindingsTransformerBuilder()
                    .id(row.getString(0))
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
                            .build())
                    .build());
        }
        return resources;
    }

    private List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, ThreeWindingsTransformer.Side side, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("threeWindingsTransformerByVoltageLevel" + (side == ThreeWindingsTransformer.Side.ONE ? 1 : (side == ThreeWindingsTransformer.Side.TWO ? 2 : 3)))
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
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId" + (side == ThreeWindingsTransformer.Side.ONE ? 1 : (side == ThreeWindingsTransformer.Side.TWO ? 2 : 3))).isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<ThreeWindingsTransformerAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.threeWindingsTransformerBuilder()
                    .id(row.getString(0))
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
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return ImmutableList.<Resource<ThreeWindingsTransformerAttributes>>builder().addAll(
                ImmutableSet.<Resource<ThreeWindingsTransformerAttributes>>builder()
                        .addAll(getVoltageLevelThreeWindingsTransformers(networkUuid, ThreeWindingsTransformer.Side.ONE, voltageLevelId))
                        .addAll(getVoltageLevelThreeWindingsTransformers(networkUuid, ThreeWindingsTransformer.Side.TWO, voltageLevelId))
                        .addAll(getVoltageLevelThreeWindingsTransformers(networkUuid, ThreeWindingsTransformer.Side.THREE, voltageLevelId))
                        .build())
                .build();
    }

    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources) {
        for (List<Resource<ThreeWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<ThreeWindingsTransformerAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateThreeWindingsTransformer.bind(
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
                        networkUuid,
                        resource.getId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        session.execute(deleteFrom(THREE_WINDINGS_TRANSFORMER)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(threeWindingsTransformerId))
                .build());
    }

    // line

    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        for (List<Resource<LineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<LineAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertLine.bind(
                        networkUuid,
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
                        resource.getAttributes().getG1(),
                        resource.getAttributes().getB1(),
                        resource.getAttributes().getG2(),
                        resource.getAttributes().getB2(),
                        resource.getAttributes().getP1(),
                        resource.getAttributes().getQ1(),
                        resource.getAttributes().getP2(),
                        resource.getAttributes().getQ2(),
                        resource.getAttributes().getPosition1(),
                        resource.getAttributes().getPosition2(),
                        emptyStringForNullValue(resource.getAttributes().getBus1()),
                        emptyStringForNullValue(resource.getAttributes().getBus2()),
                        resource.getAttributes().getConnectableBus1(),
                        resource.getAttributes().getConnectableBus2(),
                        resource.getAttributes().getMergedXnode(),
                        resource.getAttributes().getCurrentLimits1(),
                        resource.getAttributes().getCurrentLimits2(),
                        resource.getAttributes().getActivePowerLimits1(),
                        resource.getAttributes().getActivePowerLimits2(),
                        resource.getAttributes().getApparentPowerLimits1(),
                        resource.getAttributes().getApparentPowerLimits2(),
                        resource.getAttributes().getBranchStatus()
                )));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        ResultSet resultSet = session.execute(selectFrom(LINE)
                .columns(
                        "voltageLevelId1",
                        "voltageLevelId2",
                        "name",
                        "properties",
                        "node1",
                        "node2",
                        "r",
                        "x",
                        "g1",
                        "b1",
                        "g2",
                        "b2",
                        "p1",
                        "q1",
                        "p2",
                        "q2",
                        "position1",
                        "position2",
                        "bus1",
                        "bus2",
                        "connectableBus1",
                        "connectableBus2",
                        "mergedXnode",
                        "currentLimits1",
                        "currentLimits2",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(lineId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.lineBuilder()
                    .id(lineId)
                    .attributes(LineAttributes.builder()
                            .voltageLevelId1(one.getString(0))
                            .voltageLevelId2(one.getString(1))
                            .name(one.getString(2))
                            .properties(one.getMap(3, String.class, String.class))
                            .node1(one.get(4, Integer.class))
                            .node2(one.get(5, Integer.class))
                            .r(one.getDouble(6))
                            .x(one.getDouble(7))
                            .g1(one.getDouble(8))
                            .b1(one.getDouble(9))
                            .g2(one.getDouble(10))
                            .b2(one.getDouble(11))
                            .p1(one.getDouble(12))
                            .q1(one.getDouble(13))
                            .p2(one.getDouble(14))
                            .q2(one.getDouble(15))
                            .position1(one.get(16, ConnectablePositionAttributes.class))
                            .position2(one.get(17, ConnectablePositionAttributes.class))
                            .bus1(nullValueForEmptyString(one.getString(18)))
                            .bus2(nullValueForEmptyString(one.getString(19)))
                            .connectableBus1(one.getString(20))
                            .connectableBus2(one.getString(21))
                            .mergedXnode(one.get(22, MergedXnodeAttributes.class))
                            .currentLimits1(one.get(23, LimitsAttributes.class))
                            .currentLimits2(one.get(24, LimitsAttributes.class))
                            .fictitious(one.getBoolean(25))
                            .aliasesWithoutType(one.getSet(26, String.class))
                            .aliasByType(one.getMap(27, String.class, String.class))
                            .activePowerLimits1(one.get(28, LimitsAttributes.class))
                            .activePowerLimits2(one.get(29, LimitsAttributes.class))
                            .apparentPowerLimits1(one.get(30, LimitsAttributes.class))
                            .apparentPowerLimits2(one.get(31, LimitsAttributes.class))
                            .branchStatus(one.getString(32))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(LINE)
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
                        "g1",
                        "b1",
                        "g2",
                        "b2",
                        "p1",
                        "q1",
                        "p2",
                        "q2",
                        "position1",
                        "position2",
                        "bus1",
                        "bus2",
                        "connectableBus1",
                        "connectableBus2",
                        "mergedXnode",
                        "currentLimits1",
                        "currentLimits2",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<LineAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.lineBuilder()
                    .id(row.getString(0))
                    .attributes(LineAttributes.builder()
                            .voltageLevelId1(row.getString(1))
                            .voltageLevelId2(row.getString(2))
                            .name(row.getString(3))
                            .properties(row.getMap(4, String.class, String.class))
                            .node1(row.get(5, Integer.class))
                            .node2(row.get(6, Integer.class))
                            .r(row.getDouble(7))
                            .x(row.getDouble(8))
                            .g1(row.getDouble(9))
                            .b1(row.getDouble(10))
                            .g2(row.getDouble(11))
                            .b2(row.getDouble(12))
                            .p1(row.getDouble(13))
                            .q1(row.getDouble(14))
                            .p2(row.getDouble(15))
                            .q2(row.getDouble(16))
                            .position1(row.get(17, ConnectablePositionAttributes.class))
                            .position2(row.get(18, ConnectablePositionAttributes.class))
                            .bus1(nullValueForEmptyString(row.getString(19)))
                            .bus2(nullValueForEmptyString(row.getString(20)))
                            .connectableBus1(row.getString(21))
                            .connectableBus2(row.getString(22))
                            .mergedXnode(row.get(23, MergedXnodeAttributes.class))
                            .currentLimits1(row.get(24, LimitsAttributes.class))
                            .currentLimits2(row.get(25, LimitsAttributes.class))
                            .fictitious(row.getBoolean(26))
                            .aliasesWithoutType(row.getSet(27, String.class))
                            .aliasByType(row.getMap(28, String.class, String.class))
                            .activePowerLimits1(row.get(29, LimitsAttributes.class))
                            .activePowerLimits2(row.get(30, LimitsAttributes.class))
                            .apparentPowerLimits1(row.get(31, LimitsAttributes.class))
                            .apparentPowerLimits2(row.get(32, LimitsAttributes.class))
                            .branchStatus(row.getString(33))
                            .build())
                    .build());
        }
        return resources;
    }

    private List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, Branch.Side side, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("lineByVoltageLevel" + (side == Branch.Side.ONE ? 1 : 2))
                .columns(
                        "id",
                        "voltageLevelId" + (side == Branch.Side.ONE ? 2 : 1),
                        "name",
                        "properties",
                        "node1",
                        "node2",
                        "r",
                        "x",
                        "g1",
                        "b1",
                        "g2",
                        "b2",
                        "p1",
                        "q1",
                        "p2",
                        "q2",
                        "position1",
                        "position2",
                        "bus1",
                        "bus2",
                        "connectableBus1",
                        "connectableBus2",
                        "mergedXnode",
                        "currentLimits1",
                        "currentLimits2",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        ACTIVE_POWER_LIMITS1,
                        ACTIVE_POWER_LIMITS2,
                        APPARENT_POWER_LIMITS1,
                        APPARENT_POWER_LIMITS2,
                        BRANCH_STATUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId" + (side == Branch.Side.ONE ? 1 : 2)).isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<LineAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.lineBuilder()
                    .id(row.getString(0))
                    .attributes(LineAttributes.builder()
                            .voltageLevelId1(side == Branch.Side.ONE ? voltageLevelId : row.getString(1))
                            .voltageLevelId2(side == Branch.Side.TWO ? voltageLevelId : row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node1(row.get(4, Integer.class))
                            .node2(row.get(5, Integer.class))
                            .r(row.getDouble(6))
                            .x(row.getDouble(7))
                            .g1(row.getDouble(8))
                            .b1(row.getDouble(9))
                            .g2(row.getDouble(10))
                            .b2(row.getDouble(11))
                            .p1(row.getDouble(12))
                            .q1(row.getDouble(13))
                            .p2(row.getDouble(14))
                            .q2(row.getDouble(15))
                            .position1(row.get(16, ConnectablePositionAttributes.class))
                            .position2(row.get(17, ConnectablePositionAttributes.class))
                            .bus1(nullValueForEmptyString(row.getString(18)))
                            .bus2(nullValueForEmptyString(row.getString(19)))
                            .connectableBus1(row.getString(20))
                            .connectableBus2(row.getString(21))
                            .mergedXnode(row.get(22, MergedXnodeAttributes.class))
                            .currentLimits1(row.get(23, LimitsAttributes.class))
                            .currentLimits2(row.get(24, LimitsAttributes.class))
                            .fictitious(row.getBoolean(25))
                            .aliasesWithoutType(row.getSet(26, String.class))
                            .aliasByType(row.getMap(27, String.class, String.class))
                            .activePowerLimits1(row.get(28, LimitsAttributes.class))
                            .activePowerLimits2(row.get(29, LimitsAttributes.class))
                            .apparentPowerLimits1(row.get(30, LimitsAttributes.class))
                            .apparentPowerLimits2(row.get(31, LimitsAttributes.class))
                            .branchStatus(row.getString(32))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return ImmutableList.<Resource<LineAttributes>>builder().addAll(
                ImmutableSet.<Resource<LineAttributes>>builder()
                        .addAll(getVoltageLevelLines(networkUuid, Branch.Side.ONE, voltageLevelId))
                        .addAll(getVoltageLevelLines(networkUuid, Branch.Side.TWO, voltageLevelId))
                        .build())
                .build();
    }

    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        for (List<Resource<LineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<LineAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateLines.bind(
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
                        resource.getAttributes().getG1(),
                        resource.getAttributes().getB1(),
                        resource.getAttributes().getG2(),
                        resource.getAttributes().getB2(),
                        resource.getAttributes().getP1(),
                        resource.getAttributes().getQ1(),
                        resource.getAttributes().getP2(),
                        resource.getAttributes().getQ2(),
                        resource.getAttributes().getPosition1(),
                        resource.getAttributes().getPosition2(),
                        emptyStringForNullValue(resource.getAttributes().getBus1()),
                        emptyStringForNullValue(resource.getAttributes().getBus2()),
                        resource.getAttributes().getConnectableBus1(),
                        resource.getAttributes().getConnectableBus2(),
                        resource.getAttributes().getMergedXnode(),
                        resource.getAttributes().getCurrentLimits1(),
                        resource.getAttributes().getCurrentLimits2(),
                        resource.getAttributes().getActivePowerLimits1(),
                        resource.getAttributes().getActivePowerLimits2(),
                        resource.getAttributes().getApparentPowerLimits1(),
                        resource.getAttributes().getApparentPowerLimits2(),
                        resource.getAttributes().getBranchStatus(),
                        networkUuid,
                        resource.getId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteLine(UUID networkUuid, String lineId) {
        session.execute(deleteFrom(LINE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(lineId))
                .build());
    }

    // Hvdc line

    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(HVDC_LINE)
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
                .build());
        List<Resource<HvdcLineAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.hvdcLineBuilder()
                    .id(row.getString(0))
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

    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        ResultSet resultSet = session.execute(selectFrom(HVDC_LINE)
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
                .whereColumn("id").isEqualTo(literal(hvdcLineId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.hvdcLineBuilder()
                    .id(hvdcLineId)
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

    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        for (List<Resource<HvdcLineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<HvdcLineAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertHvdcLine.bind(
                        networkUuid,
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
            session.execute(batch);
        }
    }

    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        for (List<Resource<HvdcLineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<HvdcLineAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateHvdcLine.bind(
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
                        resource.getId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteHvdcLine(UUID networkUuid, String hvdcLineId) {
        session.execute(deleteFrom(HVDC_LINE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(hvdcLineId))
                .build());
    }

    // Dangling line

    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(DANGLING_LINE)
                .columns(
                        "id",
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "p0",
                        "q0",
                        "r",
                        "x",
                        "g",
                        "b",
                        GENERATION,
                        "ucteXNodeCode",
                        "currentLimits",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        ACTIVE_POWER_LIMITS,
                        APPARENT_POWER_LIMITS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<DanglingLineAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.danglingLineBuilder()
                    .id(row.getString(0))
                    .attributes(DanglingLineAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.get(4, Integer.class))
                            .p0(row.getDouble(5))
                            .q0(row.getDouble(6))
                            .r(row.getDouble(7))
                            .x(row.getDouble(8))
                            .g(row.getDouble(9))
                            .b(row.getDouble(10))
                            .generation(row.get(11, DanglingLineGenerationAttributes.class))
                            .ucteXnodeCode(row.getString(12))
                            .currentLimits(row.get(13, LimitsAttributes.class))
                            .p(row.getDouble(14))
                            .q(row.getDouble(15))
                            .position(row.get(16, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(row.getString(17)))
                            .connectableBus(row.getString(18))
                            .fictitious(row.getBool(19))
                            .aliasesWithoutType(row.getSet(20, String.class))
                            .aliasByType(row.getMap(21, String.class, String.class))
                            .activePowerLimits(row.get(22, LimitsAttributes.class))
                            .apparentPowerLimits(row.get(23, LimitsAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        ResultSet resultSet = session.execute(selectFrom(DANGLING_LINE)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "node",
                        "p0",
                        "q0",
                        "r",
                        "x",
                        "g",
                        "b",
                        GENERATION,
                        "ucteXNodeCode",
                        "currentLimits",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        ACTIVE_POWER_LIMITS,
                        APPARENT_POWER_LIMITS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(danglingLineId))
                .build());
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.danglingLineBuilder()
                    .id(danglingLineId)
                    .attributes(DanglingLineAttributes.builder()
                            .voltageLevelId(one.getString(0))
                            .name(one.getString(1))
                            .properties(one.getMap(2, String.class, String.class))
                            .node(one.get(3, Integer.class))
                            .p0(one.getDouble(4))
                            .q0(one.getDouble(5))
                            .r(one.getDouble(6))
                            .x(one.getDouble(7))
                            .g(one.getDouble(8))
                            .b(one.getDouble(9))
                            .generation(one.get(10, DanglingLineGenerationAttributes.class))
                            .ucteXnodeCode(one.getString(11))
                            .currentLimits(one.get(12, LimitsAttributes.class))
                            .p(one.getDouble(13))
                            .q(one.getDouble(14))
                            .position(one.get(15, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(one.getString(16)))
                            .connectableBus(one.getString(17))
                            .fictitious(one.getBoolean(18))
                            .aliasesWithoutType(one.getSet(19, String.class))
                            .aliasByType(one.getMap(20, String.class, String.class))
                            .activePowerLimits(one.get(21, LimitsAttributes.class))
                            .apparentPowerLimits(one.get(22, LimitsAttributes.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(selectFrom("danglingLineByVoltageLevel")
                .columns(
                        "id",
                        "name",
                        "properties",
                        "node",
                        "p0",
                        "q0",
                        "r",
                        "x",
                        "g",
                        "b",
                        GENERATION,
                        "ucteXNodeCode",
                        "currentLimits",
                        "p",
                        "q",
                        "position",
                        "bus",
                        CONNECTABLE_BUS,
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE,
                        ACTIVE_POWER_LIMITS,
                        APPARENT_POWER_LIMITS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                .build());
        List<Resource<DanglingLineAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.danglingLineBuilder()
                    .id(row.getString(0))
                    .attributes(DanglingLineAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.get(3, Integer.class))
                            .p0(row.getDouble(4))
                            .q0(row.getDouble(5))
                            .r(row.getDouble(6))
                            .x(row.getDouble(7))
                            .g(row.getDouble(8))
                            .b(row.getDouble(9))
                            .generation(row.get(10, DanglingLineGenerationAttributes.class))
                            .ucteXnodeCode(row.getString(11))
                            .currentLimits(row.get(12, LimitsAttributes.class))
                            .p(row.getDouble(13))
                            .q(row.getDouble(14))
                            .position(row.get(15, ConnectablePositionAttributes.class))
                            .bus(nullValueForEmptyString(row.getString(16)))
                            .connectableBus(row.getString(17))
                            .fictitious(row.getBoolean(18))
                            .aliasesWithoutType(row.getSet(19, String.class))
                            .aliasByType(row.getMap(20, String.class, String.class))
                            .activePowerLimits(row.get(21, LimitsAttributes.class))
                            .apparentPowerLimits(row.get(22, LimitsAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        for (List<Resource<DanglingLineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<DanglingLineAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertDanglingLine.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getP0(),
                        resource.getAttributes().getQ0(),
                        resource.getAttributes().getR(),
                        resource.getAttributes().getX(),
                        resource.getAttributes().getG(),
                        resource.getAttributes().getB(),
                        resource.getAttributes().getGeneration(),
                        resource.getAttributes().getUcteXnodeCode(),
                        resource.getAttributes().getCurrentLimits(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getActivePowerLimits(),
                        resource.getAttributes().getApparentPowerLimits()
                )));
            }
            session.execute(batch);
        }
    }

    public void deleteDanglingLine(UUID networkUuid, String danglingLineId) {
        session.execute(deleteFrom(DANGLING_LINE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(danglingLineId))
                .build());
    }

    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        for (List<Resource<DanglingLineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<DanglingLineAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateDanglingLine.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getP0(),
                        resource.getAttributes().getQ0(),
                        resource.getAttributes().getR(),
                        resource.getAttributes().getX(),
                        resource.getAttributes().getG(),
                        resource.getAttributes().getB(),
                        resource.getAttributes().getGeneration(),
                        resource.getAttributes().getUcteXnodeCode(),
                        resource.getAttributes().getCurrentLimits(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition(),
                        emptyStringForNullValue(resource.getAttributes().getBus()),
                        resource.getAttributes().getConnectableBus(),
                        resource.getAttributes().getActivePowerLimits(),
                        resource.getAttributes().getApparentPowerLimits(),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    //Buses

    public void createBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        for (List<Resource<ConfiguredBusAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<ConfiguredBusAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psInsertConfiguredBus.bind(
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getV(),
                        resource.getAttributes().getAngle()
                )));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        ResultSet resultSet = session.execute(selectFrom(CONFIGURED_BUS)
                .columns(
                        "voltageLevelId",
                        "name",
                        "properties",
                        "v",
                        "angle",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(busId)).build());
        Row row = resultSet.one();
        if (row != null) {
            return Optional.of(Resource.configuredBusBuilder()
                    .id(busId)
                    .attributes(ConfiguredBusAttributes.builder()
                            .voltageLevelId(row.getString(0))
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .v(row.getDouble(3))
                            .angle(row.getDouble(4))
                            .fictitious(row.getBoolean(5))
                            .aliasesWithoutType(row.getSet(6, String.class))
                            .aliasByType(row.getMap(7, String.class, String.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        ResultSet resultSet = session.execute(selectFrom(CONFIGURED_BUS)
                .columns("id",
                        "name",
                        "voltageLevelId",
                        "v",
                        "angle",
                        "properties",
                        "fictitious",
                        ALIASES_WITHOUT_TYPE,
                        ALIAS_BY_TYPE)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .build());
        List<Resource<ConfiguredBusAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.configuredBusBuilder()
                    .id(row.getString(0))
                    .attributes(ConfiguredBusAttributes.builder()
                            .id(row.getString(0))
                            .name(row.getString(1))
                            .voltageLevelId(row.getString(2))
                            .v(row.getDouble(3))
                            .angle(row.getDouble(4))
                            .properties(row.getMap(5, String.class, String.class))
                            .fictitious(row.getBoolean(6))
                            .aliasesWithoutType(row.getSet(7, String.class))
                            .aliasByType(row.getMap(8, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelBuses(UUID networkUuid, String voltageLevelId) {
        ResultSet resultSet = session.execute(
                selectFrom("configuredBusByVoltageLevel")
                        .columns("id",
                                "name",
                                "v",
                                "angle",
                                "fictitious",
                                "properties",
                                ALIASES_WITHOUT_TYPE,
                                ALIAS_BY_TYPE)
                        .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                        .whereColumn("voltageLevelId").isEqualTo(literal(voltageLevelId))
                        .build());
        List<Resource<ConfiguredBusAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.configuredBusBuilder()
                    .id(row.getString(0))
                    .attributes(ConfiguredBusAttributes.builder()
                            .id(row.getString(0))
                            .name(row.getString(1))
                            .voltageLevelId(voltageLevelId)
                            .v(row.getDouble(2))
                            .angle(row.getDouble(3))
                            .fictitious(row.getBoolean(4))
                            .properties(row.getMap(5, String.class, String.class))
                            .aliasesWithoutType(row.getSet(6, String.class))
                            .aliasByType(row.getMap(7, String.class, String.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public void updateBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        for (List<Resource<ConfiguredBusAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            for (Resource<ConfiguredBusAttributes> resource : subresources) {
                batch = batch.add(unsetNullValues(psUpdateConfiguredBus.bind(
                        resource.getAttributes().getName(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getAliasesWithoutType(),
                        resource.getAttributes().getAliasByType(),
                        resource.getAttributes().getV(),
                        resource.getAttributes().getAngle(),
                        networkUuid,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId())
                ));
            }
            session.execute(batch);
        }
    }

    public void deleteBus(UUID networkUuid, String configuredBusId) {
        session.execute(deleteFrom(CONFIGURED_BUS)
                .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
                .whereColumn("id").isEqualTo(literal(configuredBusId))
                .build());
    }

}
