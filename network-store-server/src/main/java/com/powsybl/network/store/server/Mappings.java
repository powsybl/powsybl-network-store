/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.network.store.model.ActivePowerControlAttributes;
import com.powsybl.network.store.model.BatteryAttributes;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.BusbarSectionPositionAttributes;
import com.powsybl.network.store.model.CgmesControlAreasAttributes;
import com.powsybl.network.store.model.CgmesSshMetadataAttributes;
import com.powsybl.network.store.model.CgmesSvMetadataAttributes;
import com.powsybl.network.store.model.CimCharacteristicsAttributes;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.ConnectablePositionAttributes;
import com.powsybl.network.store.model.CoordinatedReactiveControlAttributes;
import com.powsybl.network.store.model.DanglingLineAttributes;
import com.powsybl.network.store.model.DanglingLineGenerationAttributes;
import com.powsybl.network.store.model.EntsoeAreaAttributes;
import com.powsybl.network.store.model.GeneratorAttributes;
import com.powsybl.network.store.model.GeneratorEntsoeCategoryAttributes;
import com.powsybl.network.store.model.HvdcAngleDroopActivePowerControlAttributes;
import com.powsybl.network.store.model.HvdcLineAttributes;
import com.powsybl.network.store.model.HvdcOperatorActivePowerRangeAttributes;
import com.powsybl.network.store.model.LccConverterStationAttributes;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.LoadDetailAttributes;
import com.powsybl.network.store.model.MergedXnodeAttributes;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.PhaseTapChangerAttributes;
import com.powsybl.network.store.model.RatioTapChangerAttributes;
import com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes;
import com.powsybl.network.store.model.ReactiveLimitsAttributes;
import com.powsybl.network.store.model.RemoteReactivePowerControlAttributes;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.ShuntCompensatorLinearModelAttributes;
import com.powsybl.network.store.model.ShuntCompensatorModelAttributes;
import com.powsybl.network.store.model.ShuntCompensatorNonLinearModelAttributes;
import com.powsybl.network.store.model.StaticVarCompensatorAttributes;
import com.powsybl.network.store.model.SubstationAttributes;
import com.powsybl.network.store.model.SwitchAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;
import com.powsybl.network.store.model.ThreeWindingsTransformerAttributes;
import com.powsybl.network.store.model.ThreeWindingsTransformerPhaseAngleClockAttributes;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;
import com.powsybl.network.store.model.TwoWindingsTransformerPhaseAngleClockAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;
import com.powsybl.network.store.model.VoltagePerReactivePowerControlAttributes;
import com.powsybl.network.store.model.VscConverterStationAttributes;
import org.joda.time.DateTime;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class Mappings {
    private static Mappings instance = null;

    private final Map<String, Mapping> lineMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> loadMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> generatorMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> switchMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> substationMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> networkMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> voltageLevelMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> batteryMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> busbarSectionMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> configuredBusMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> danglingLineMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> shuntCompensatorMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> vscConverterStationMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> lccConverterStationMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> staticVarCompensatorMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> hvdcLineMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> twoWindingsTransformerMappings = new LinkedHashMap<>();
    private final Map<String, Mapping> threeWindingsTransformerMappings = new LinkedHashMap<>();

    private static final String VOLTAGE_LEVEL_ID = "voltageLevelId";
    private static final String VOLTAGE_LEVEL_ID_1 = "voltageLevelId1";
    private static final String VOLTAGE_LEVEL_ID_2 = "voltageLevelId2";
    private static final String VOLTAGE_LEVEL_ID_3 = "voltageLevelId3";
    private static final String CONNECTABLE_BUS = "connectableBus";
    private static final String CONNECTABLE_BUS_1 = "connectableBus1";
    private static final String CONNECTABLE_BUS_2 = "connectableBus2";
    private static final String BRANCH_STATUS = "branchStatus";
    private static final String FICTITIOUS = "fictitious";
    private static final String NODE_1 = "node1";
    private static final String NODE_2 = "node2";
    private static final String PROPERTIES = "properties";
    private static final String ALIAS_BY_TYPE = "aliasByType";
    private static final String ALIASES_WITHOUT_TYPE = "aliasesWithoutType";
    private static final String POSITION = "position";
    private static final String POSITION_1 = "position1";
    private static final String POSITION_2 = "position2";
    private static final String CURRENT_LIMITS_1 = "currentLimits1";
    private static final String CURRENT_LIMITS_2 = "currentLimits2";
    private static final String APPARENT_POWER_LIMITS_1 = "apparentPowerLimits1";
    private static final String APPARENT_POWER_LIMITS_2 = "apparentPowerLimits2";
    private static final String ACTIVE_POWER_LIMITS_1 = "activePowerLimits1";
    private static final String ACTIVE_POWER_LIMITS_2 = "activePowerLimits2";
    private static final String VOLTAGE_REGULATOR_ON = "voltageRegulatorOn";
    private static final String MIN_MAX_REACIVE_LIMITS = "minMaxReactiveLimits";
    private static final String REACTIVE_CAPABILITY_CURVE = "reactiveCapabilityCurve";
    private static final String REGULATION_TERMINAL = "regulatingTerminal";

    public Map<String, Mapping> getLineMappings() {
        return lineMappings;
    }

    private void createLineMappings() {
        lineMappings.put("name", new Mapping<>(String.class, LineAttributes::getName, LineAttributes::setName));
        lineMappings.put(VOLTAGE_LEVEL_ID_1, new Mapping<>(String.class, LineAttributes::getVoltageLevelId1, LineAttributes::setVoltageLevelId1));
        lineMappings.put(VOLTAGE_LEVEL_ID_2, new Mapping<>(String.class, LineAttributes::getVoltageLevelId2, LineAttributes::setVoltageLevelId2));
        lineMappings.put("bus1", new Mapping<>(String.class, LineAttributes::getBus1, LineAttributes::setBus1));
        lineMappings.put("bus2", new Mapping<>(String.class, LineAttributes::getBus2, LineAttributes::setBus2));
        lineMappings.put(CONNECTABLE_BUS_1, new Mapping<>(String.class, LineAttributes::getConnectableBus1, LineAttributes::setConnectableBus1));
        lineMappings.put(CONNECTABLE_BUS_2, new Mapping<>(String.class, LineAttributes::getConnectableBus2, LineAttributes::setConnectableBus2));
        lineMappings.put(BRANCH_STATUS, new Mapping<>(String.class, LineAttributes::getBranchStatus, LineAttributes::setBranchStatus));
        lineMappings.put("r", new Mapping<>(Double.class, LineAttributes::getR, LineAttributes::setR));
        lineMappings.put("x", new Mapping<>(Double.class, LineAttributes::getX, LineAttributes::setX));
        lineMappings.put("g1", new Mapping<>(Double.class, LineAttributes::getG1, LineAttributes::setG1));
        lineMappings.put("b1", new Mapping<>(Double.class, LineAttributes::getB1, LineAttributes::setB1));
        lineMappings.put("g2", new Mapping<>(Double.class, LineAttributes::getG2, LineAttributes::setG2));
        lineMappings.put("b2", new Mapping<>(Double.class, LineAttributes::getB2, LineAttributes::setB2));
        lineMappings.put("p1", new Mapping<>(Double.class, LineAttributes::getP1, LineAttributes::setP1));
        lineMappings.put("q1", new Mapping<>(Double.class, LineAttributes::getQ1, LineAttributes::setQ1));
        lineMappings.put("p2", new Mapping<>(Double.class, LineAttributes::getP2, LineAttributes::setP2));
        lineMappings.put("q2", new Mapping<>(Double.class, LineAttributes::getQ2, LineAttributes::setQ2));
        lineMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, LineAttributes::isFictitious, LineAttributes::setFictitious));
        lineMappings.put(NODE_1, new Mapping<>(Integer.class, LineAttributes::getNode1, LineAttributes::setNode1));
        lineMappings.put(NODE_2, new Mapping<>(Integer.class, LineAttributes::getNode2, LineAttributes::setNode2));
        lineMappings.put(PROPERTIES, new Mapping<>(Map.class, LineAttributes::getProperties, LineAttributes::setProperties));
        lineMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, LineAttributes::getAliasByType, LineAttributes::setAliasByType));
        lineMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, LineAttributes::getAliasesWithoutType, LineAttributes::setAliasesWithoutType));
        lineMappings.put(POSITION_1, new Mapping<>(ConnectablePositionAttributes.class, LineAttributes::getPosition1, LineAttributes::setPosition1));
        lineMappings.put(POSITION_2, new Mapping<>(ConnectablePositionAttributes.class, LineAttributes::getPosition2, LineAttributes::setPosition2));
        lineMappings.put("mergedXnode", new Mapping<>(MergedXnodeAttributes.class, LineAttributes::getMergedXnode, LineAttributes::setMergedXnode));
        lineMappings.put(CURRENT_LIMITS_1, new Mapping<>(LimitsAttributes.class, LineAttributes::getCurrentLimits1, LineAttributes::setCurrentLimits1));
        lineMappings.put(CURRENT_LIMITS_2, new Mapping<>(LimitsAttributes.class, LineAttributes::getCurrentLimits2, LineAttributes::setCurrentLimits2));
        lineMappings.put(APPARENT_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class, LineAttributes::getApparentPowerLimits1, LineAttributes::setApparentPowerLimits1));
        lineMappings.put(APPARENT_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class, LineAttributes::getApparentPowerLimits2, LineAttributes::setApparentPowerLimits2));
        lineMappings.put(ACTIVE_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class, LineAttributes::getActivePowerLimits1, LineAttributes::setActivePowerLimits1));
        lineMappings.put(ACTIVE_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class, LineAttributes::getActivePowerLimits2, LineAttributes::setActivePowerLimits2));
    }

    public Map<String, Mapping> getLoadMappings() {
        return loadMappings;
    }

    private void createLoadMappings() {
        loadMappings.put("name", new Mapping<>(String.class, LoadAttributes::getName, LoadAttributes::setName));
        loadMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, LoadAttributes::getVoltageLevelId, LoadAttributes::setVoltageLevelId));
        loadMappings.put("bus", new Mapping<>(String.class, LoadAttributes::getBus, LoadAttributes::setBus));
        loadMappings.put(CONNECTABLE_BUS, new Mapping<>(String.class, LoadAttributes::getConnectableBus, LoadAttributes::setConnectableBus));
        loadMappings.put("p0", new Mapping<>(Double.class, LoadAttributes::getP0, LoadAttributes::setP0));
        loadMappings.put("q0", new Mapping<>(Double.class, LoadAttributes::getQ0, LoadAttributes::setQ0));
        loadMappings.put("loadType", new Mapping<>(LoadType.class, LoadAttributes::getLoadType, LoadAttributes::setLoadType));
        loadMappings.put("p", new Mapping<>(Double.class, LoadAttributes::getP, LoadAttributes::setP));
        loadMappings.put("q", new Mapping<>(Double.class, LoadAttributes::getQ, LoadAttributes::setQ));
        loadMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, LoadAttributes::isFictitious, LoadAttributes::setFictitious));
        loadMappings.put("node", new Mapping<>(Integer.class, LoadAttributes::getNode, LoadAttributes::setNode));
        loadMappings.put(PROPERTIES, new Mapping<>(Map.class, LoadAttributes::getProperties, LoadAttributes::setProperties));
        loadMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, LoadAttributes::getAliasByType, LoadAttributes::setAliasByType));
        loadMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, LoadAttributes::getAliasesWithoutType, LoadAttributes::setAliasesWithoutType));
        loadMappings.put(POSITION, new Mapping<>(ConnectablePositionAttributes.class, LoadAttributes::getPosition, LoadAttributes::setPosition));
        loadMappings.put("loadDetail", new Mapping<>(LoadDetailAttributes.class, LoadAttributes::getLoadDetail, LoadAttributes::setLoadDetail));
    }

    public Map<String, Mapping> getGeneratorMappings() {
        return generatorMappings;
    }

    private void createGeneratorMappings() {
        generatorMappings.put("name", new Mapping<>(String.class, GeneratorAttributes::getName, GeneratorAttributes::setName));
        generatorMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, GeneratorAttributes::getVoltageLevelId, GeneratorAttributes::setVoltageLevelId));
        generatorMappings.put("bus", new Mapping<>(String.class, GeneratorAttributes::getBus, GeneratorAttributes::setBus));
        generatorMappings.put(CONNECTABLE_BUS, new Mapping<>(String.class, GeneratorAttributes::getConnectableBus, GeneratorAttributes::setConnectableBus));
        generatorMappings.put("minP", new Mapping<>(Double.class, GeneratorAttributes::getMinP, GeneratorAttributes::setMinP));
        generatorMappings.put("maxP", new Mapping<>(Double.class, GeneratorAttributes::getMaxP, GeneratorAttributes::setMaxP));
        generatorMappings.put("energySource", new Mapping<>(EnergySource.class, GeneratorAttributes::getEnergySource, GeneratorAttributes::setEnergySource));
        generatorMappings.put("p", new Mapping<>(Double.class, GeneratorAttributes::getP, GeneratorAttributes::setP));
        generatorMappings.put("q", new Mapping<>(Double.class, GeneratorAttributes::getQ, GeneratorAttributes::setQ));
        generatorMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, GeneratorAttributes::isFictitious, GeneratorAttributes::setFictitious));
        generatorMappings.put(VOLTAGE_REGULATOR_ON, new Mapping<>(Boolean.class, GeneratorAttributes::isVoltageRegulatorOn, GeneratorAttributes::setVoltageRegulatorOn));
        generatorMappings.put("targetP", new Mapping<>(Double.class, GeneratorAttributes::getTargetP, GeneratorAttributes::setTargetP));
        generatorMappings.put("targetQ", new Mapping<>(Double.class, GeneratorAttributes::getTargetQ, GeneratorAttributes::setTargetQ));
        generatorMappings.put("targetV", new Mapping<>(Double.class, GeneratorAttributes::getTargetV, GeneratorAttributes::setTargetV));
        generatorMappings.put("ratedS", new Mapping<>(Double.class, GeneratorAttributes::getRatedS, GeneratorAttributes::setRatedS));
        generatorMappings.put(MIN_MAX_REACIVE_LIMITS, new Mapping<>(ReactiveLimitsAttributes.class, (GeneratorAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (GeneratorAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        generatorMappings.put(REACTIVE_CAPABILITY_CURVE, new Mapping<>(ReactiveLimitsAttributes.class, (GeneratorAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (GeneratorAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        generatorMappings.put("activePowerControl", new Mapping<>(ActivePowerControlAttributes.class, GeneratorAttributes::getActivePowerControl, GeneratorAttributes::setActivePowerControl));
        generatorMappings.put(REGULATION_TERMINAL, new Mapping<>(TerminalRefAttributes.class, GeneratorAttributes::getRegulatingTerminal, GeneratorAttributes::setRegulatingTerminal));
        generatorMappings.put("coordinatedReactiveControl", new Mapping<>(CoordinatedReactiveControlAttributes.class, GeneratorAttributes::getCoordinatedReactiveControl, GeneratorAttributes::setCoordinatedReactiveControl));
        generatorMappings.put("remoteReactivePowerControl", new Mapping<>(RemoteReactivePowerControlAttributes.class, GeneratorAttributes::getRemoteReactivePowerControl, GeneratorAttributes::setRemoteReactivePowerControl));
        generatorMappings.put("entsoeCategory", new Mapping<>(GeneratorEntsoeCategoryAttributes.class, GeneratorAttributes::getEntsoeCategoryAttributes, GeneratorAttributes::setEntsoeCategoryAttributes));
        generatorMappings.put("node", new Mapping<>(Integer.class, GeneratorAttributes::getNode, GeneratorAttributes::setNode));
        generatorMappings.put(PROPERTIES, new Mapping<>(Map.class, GeneratorAttributes::getProperties, GeneratorAttributes::setProperties));
        generatorMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, GeneratorAttributes::getAliasByType, GeneratorAttributes::setAliasByType));
        generatorMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, GeneratorAttributes::getAliasesWithoutType, GeneratorAttributes::setAliasesWithoutType));
        generatorMappings.put(POSITION, new Mapping<>(ConnectablePositionAttributes.class, GeneratorAttributes::getPosition, GeneratorAttributes::setPosition));
    }

    public Map<String, Mapping> getSwitchMappings() {
        return switchMappings;
    }

    private void createSwitchMappings() {
        switchMappings.put("name", new Mapping<>(String.class, SwitchAttributes::getName, SwitchAttributes::setName));
        switchMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, SwitchAttributes::getVoltageLevelId, SwitchAttributes::setVoltageLevelId));
        switchMappings.put("bus1", new Mapping<>(String.class, SwitchAttributes::getBus1, SwitchAttributes::setBus1));
        switchMappings.put("bus2", new Mapping<>(String.class, SwitchAttributes::getBus2, SwitchAttributes::setBus2));
        switchMappings.put("kind", new Mapping<>(SwitchKind.class, SwitchAttributes::getKind, SwitchAttributes::setKind));
        switchMappings.put("open", new Mapping<>(Boolean.class, SwitchAttributes::isOpen, SwitchAttributes::setOpen));
        switchMappings.put("retained", new Mapping<>(Boolean.class, SwitchAttributes::isRetained, SwitchAttributes::setRetained));
        switchMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, SwitchAttributes::isFictitious, SwitchAttributes::setFictitious));
        switchMappings.put(NODE_1, new Mapping<>(Integer.class, SwitchAttributes::getNode1, SwitchAttributes::setNode1));
        switchMappings.put(NODE_2, new Mapping<>(Integer.class, SwitchAttributes::getNode2, SwitchAttributes::setNode2));
        switchMappings.put(PROPERTIES, new Mapping<>(Map.class, SwitchAttributes::getProperties, SwitchAttributes::setProperties));
        switchMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, SwitchAttributes::getAliasByType, SwitchAttributes::setAliasByType));
        switchMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, SwitchAttributes::getAliasesWithoutType, SwitchAttributes::setAliasesWithoutType));
    }

    public Map<String, Mapping> getSubstationMappings() {
        return substationMappings;
    }

    private void createSubstationMappings() {
        substationMappings.put("name", new Mapping<>(String.class, SubstationAttributes::getName, SubstationAttributes::setName));
        substationMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, SubstationAttributes::isFictitious, SubstationAttributes::setFictitious));
        substationMappings.put(PROPERTIES, new Mapping<>(Map.class, SubstationAttributes::getProperties, SubstationAttributes::setProperties));
        substationMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, SubstationAttributes::getAliasByType, SubstationAttributes::setAliasByType));
        substationMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, SubstationAttributes::getAliasesWithoutType, SubstationAttributes::setAliasesWithoutType));
        substationMappings.put("country", new Mapping<>(Country.class, SubstationAttributes::getCountry, SubstationAttributes::setCountry));
        substationMappings.put("tso", new Mapping<>(String.class, SubstationAttributes::getTso, SubstationAttributes::setTso));
        substationMappings.put("geographicalTags", new Mapping<>(Set.class, SubstationAttributes::getGeographicalTags, SubstationAttributes::setGeographicalTags));
        substationMappings.put("entsoeArea", new Mapping<>(EntsoeAreaAttributes.class, SubstationAttributes::getEntsoeArea, SubstationAttributes::setEntsoeArea));
    }

    public Map<String, Mapping> getNetworkMappings() {
        return networkMappings;
    }

    private void createNetworkMappings() {
        networkMappings.put("uuid", new Mapping<>(UUID.class, NetworkAttributes::getUuid, NetworkAttributes::setUuid));
        networkMappings.put("variantId", new Mapping<>(String.class, NetworkAttributes::getVariantId, NetworkAttributes::setVariantId));
        networkMappings.put("name", new Mapping<>(String.class, NetworkAttributes::getName, NetworkAttributes::setName));
        networkMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, NetworkAttributes::isFictitious, NetworkAttributes::setFictitious));
        networkMappings.put(PROPERTIES, new Mapping<>(Map.class, NetworkAttributes::getProperties, NetworkAttributes::setProperties));
        networkMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, NetworkAttributes::getAliasByType, NetworkAttributes::setAliasByType));
        networkMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, NetworkAttributes::getAliasesWithoutType, NetworkAttributes::setAliasesWithoutType));
        networkMappings.put("idByAlias", new Mapping<>(Map.class, NetworkAttributes::getIdByAlias, NetworkAttributes::setIdByAlias));
        networkMappings.put("caseDate", new Mapping<>(Instant.class, (NetworkAttributes attributes) -> attributes.getCaseDate().toDate().toInstant(),
            (NetworkAttributes attributes, Instant instant) -> attributes.setCaseDate(new DateTime(instant.toEpochMilli()))));
        networkMappings.put("forecastDistance", new Mapping<>(Integer.class, NetworkAttributes::getForecastDistance, NetworkAttributes::setForecastDistance));
        networkMappings.put("sourceFormat", new Mapping<>(String.class, NetworkAttributes::getSourceFormat, NetworkAttributes::setSourceFormat));
        networkMappings.put("connectedComponentsValid", new Mapping<>(Boolean.class, NetworkAttributes::isConnectedComponentsValid, NetworkAttributes::setConnectedComponentsValid));
        networkMappings.put("synchronousComponentsValid", new Mapping<>(Boolean.class, NetworkAttributes::isSynchronousComponentsValid, NetworkAttributes::setSynchronousComponentsValid));
        networkMappings.put("cgmesSvMetadata", new Mapping<>(CgmesSvMetadataAttributes.class, NetworkAttributes::getCgmesSvMetadata, NetworkAttributes::setCgmesSvMetadata));
        networkMappings.put("cgmesSshMetadata", new Mapping<>(CgmesSshMetadataAttributes.class, NetworkAttributes::getCgmesSshMetadata, NetworkAttributes::setCgmesSshMetadata));
        networkMappings.put("cimCharacteristics", new Mapping<>(CimCharacteristicsAttributes.class, NetworkAttributes::getCimCharacteristics, NetworkAttributes::setCimCharacteristics));
        networkMappings.put("cgmesControlAreas", new Mapping<>(CgmesControlAreasAttributes.class, NetworkAttributes::getCgmesControlAreas, NetworkAttributes::setCgmesControlAreas));
    }

    public Map<String, Mapping> getVoltageLevelMappings() {
        return voltageLevelMappings;
    }

    private void createVoltageLevelMappings() {
        voltageLevelMappings.put("substationId", new Mapping<>(String.class, VoltageLevelAttributes::getSubstationId, VoltageLevelAttributes::setSubstationId));
        voltageLevelMappings.put("name", new Mapping<>(String.class, VoltageLevelAttributes::getName, VoltageLevelAttributes::setName));
        voltageLevelMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, VoltageLevelAttributes::isFictitious, VoltageLevelAttributes::setFictitious));
        voltageLevelMappings.put(PROPERTIES, new Mapping<>(Map.class, VoltageLevelAttributes::getProperties, VoltageLevelAttributes::setProperties));
        voltageLevelMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, VoltageLevelAttributes::getAliasByType, VoltageLevelAttributes::setAliasByType));
        voltageLevelMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, VoltageLevelAttributes::getAliasesWithoutType, VoltageLevelAttributes::setAliasesWithoutType));
        voltageLevelMappings.put("nominalV", new Mapping<>(Double.class, VoltageLevelAttributes::getNominalV, VoltageLevelAttributes::setNominalV));
        voltageLevelMappings.put("lowVoltageLimit", new Mapping<>(Double.class, VoltageLevelAttributes::getLowVoltageLimit, VoltageLevelAttributes::setLowVoltageLimit));
        voltageLevelMappings.put("highVoltageLimit", new Mapping<>(Double.class, VoltageLevelAttributes::getHighVoltageLimit, VoltageLevelAttributes::setHighVoltageLimit));
        voltageLevelMappings.put("topologyKind", new Mapping<>(TopologyKind.class, VoltageLevelAttributes::getTopologyKind, VoltageLevelAttributes::setTopologyKind));
        voltageLevelMappings.put("internalConnections", new Mapping<>(List.class, VoltageLevelAttributes::getInternalConnections, VoltageLevelAttributes::setInternalConnections));
        voltageLevelMappings.put("calculatedBusesForBusView", new Mapping<>(List.class, VoltageLevelAttributes::getCalculatedBusesForBusView, VoltageLevelAttributes::setCalculatedBusesForBusView));
        voltageLevelMappings.put("nodeToCalculatedBusForBusView", new Mapping<>(null, VoltageLevelAttributes::getNodeToCalculatedBusForBusView, VoltageLevelAttributes::setNodeToCalculatedBusForBusView, Integer.class, Integer.class));
        voltageLevelMappings.put("busToCalculatedBusForBusView", new Mapping<>(null, VoltageLevelAttributes::getBusToCalculatedBusForBusView, VoltageLevelAttributes::setBusToCalculatedBusForBusView, String.class, Integer.class));
        voltageLevelMappings.put("calculatedBusesForBusBreakerView", new Mapping<>(List.class, VoltageLevelAttributes::getCalculatedBusesForBusBreakerView, VoltageLevelAttributes::setCalculatedBusesForBusBreakerView));
        voltageLevelMappings.put("nodeToCalculatedBusForBusBreakerView", new Mapping<>(null, VoltageLevelAttributes::getNodeToCalculatedBusForBusBreakerView, VoltageLevelAttributes::setNodeToCalculatedBusForBusBreakerView, Integer.class, Integer.class));
        voltageLevelMappings.put("busToCalculatedBusForBusBreakerView", new Mapping<>(null, VoltageLevelAttributes::getBusToCalculatedBusForBusBreakerView, VoltageLevelAttributes::setBusToCalculatedBusForBusBreakerView, String.class, Integer.class));
        voltageLevelMappings.put("slackTerminal", new Mapping<>(TerminalRefAttributes.class, VoltageLevelAttributes::getSlackTerminal, VoltageLevelAttributes::setSlackTerminal));
        voltageLevelMappings.put("calculatedBusesValid", new Mapping<>(Boolean.class, VoltageLevelAttributes::isCalculatedBusesValid, VoltageLevelAttributes::setCalculatedBusesValid));
    }

    public Map<String, Mapping> getBatteryMappings() {
        return batteryMappings;
    }

    private void createBatteryMappings() {
        batteryMappings.put("name", new Mapping<>(String.class, BatteryAttributes::getName, BatteryAttributes::setName));
        batteryMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, BatteryAttributes::getVoltageLevelId, BatteryAttributes::setVoltageLevelId));
        batteryMappings.put("bus", new Mapping<>(String.class, BatteryAttributes::getBus, BatteryAttributes::setBus));
        batteryMappings.put(CONNECTABLE_BUS, new Mapping<>(String.class, BatteryAttributes::getConnectableBus, BatteryAttributes::setConnectableBus));
        batteryMappings.put("minP", new Mapping<>(Double.class, BatteryAttributes::getMinP, BatteryAttributes::setMinP));
        batteryMappings.put("maxP", new Mapping<>(Double.class, BatteryAttributes::getMaxP, BatteryAttributes::setMaxP));
        batteryMappings.put("p0", new Mapping<>(Double.class, BatteryAttributes::getP0, BatteryAttributes::setP0));
        batteryMappings.put("q0", new Mapping<>(Double.class, BatteryAttributes::getQ0, BatteryAttributes::setQ0));
        batteryMappings.put("p", new Mapping<>(Double.class, BatteryAttributes::getP, BatteryAttributes::setP));
        batteryMappings.put("q", new Mapping<>(Double.class, BatteryAttributes::getQ, BatteryAttributes::setQ));
        batteryMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, BatteryAttributes::isFictitious, BatteryAttributes::setFictitious));
        batteryMappings.put(MIN_MAX_REACIVE_LIMITS, new Mapping<>(ReactiveLimitsAttributes.class, (BatteryAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (BatteryAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        batteryMappings.put(REACTIVE_CAPABILITY_CURVE, new Mapping<>(ReactiveLimitsAttributes.class, (BatteryAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (BatteryAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        batteryMappings.put("activePowerControl", new Mapping<>(ActivePowerControlAttributes.class, BatteryAttributes::getActivePowerControl, BatteryAttributes::setActivePowerControl));
        batteryMappings.put("node", new Mapping<>(Integer.class, BatteryAttributes::getNode, BatteryAttributes::setNode));
        batteryMappings.put(PROPERTIES, new Mapping<>(Map.class, BatteryAttributes::getProperties, BatteryAttributes::setProperties));
        batteryMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, BatteryAttributes::getAliasByType, BatteryAttributes::setAliasByType));
        batteryMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, BatteryAttributes::getAliasesWithoutType, BatteryAttributes::setAliasesWithoutType));
        batteryMappings.put(POSITION, new Mapping<>(ConnectablePositionAttributes.class, BatteryAttributes::getPosition, BatteryAttributes::setPosition));
    }

    public Map<String, Mapping> getBusbarSectionMappings() {
        return busbarSectionMappings;
    }

    private void createBusbarSectionMappings() {
        busbarSectionMappings.put("name", new Mapping<>(String.class, BusbarSectionAttributes::getName, BusbarSectionAttributes::setName));
        busbarSectionMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, BusbarSectionAttributes::getVoltageLevelId, BusbarSectionAttributes::setVoltageLevelId));
        busbarSectionMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, BusbarSectionAttributes::isFictitious, BusbarSectionAttributes::setFictitious));
        busbarSectionMappings.put("node", new Mapping<>(Integer.class, BusbarSectionAttributes::getNode, BusbarSectionAttributes::setNode));
        busbarSectionMappings.put(PROPERTIES, new Mapping<>(Map.class, BusbarSectionAttributes::getProperties, BusbarSectionAttributes::setProperties));
        busbarSectionMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, BusbarSectionAttributes::getAliasByType, BusbarSectionAttributes::setAliasByType));
        busbarSectionMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, BusbarSectionAttributes::getAliasesWithoutType, BusbarSectionAttributes::setAliasesWithoutType));
        busbarSectionMappings.put(POSITION, new Mapping<>(BusbarSectionPositionAttributes.class, BusbarSectionAttributes::getPosition, BusbarSectionAttributes::setPosition));
    }

    public Map<String, Mapping> getConfiguredBusMappings() {
        return configuredBusMappings;
    }

    private void createConfiguredBusMappings() {
        configuredBusMappings.put("name", new Mapping<>(String.class, ConfiguredBusAttributes::getName, ConfiguredBusAttributes::setName));
        configuredBusMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, ConfiguredBusAttributes::getVoltageLevelId, ConfiguredBusAttributes::setVoltageLevelId));
        configuredBusMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, ConfiguredBusAttributes::isFictitious, ConfiguredBusAttributes::setFictitious));
        configuredBusMappings.put(PROPERTIES, new Mapping<>(Map.class, ConfiguredBusAttributes::getProperties, ConfiguredBusAttributes::setProperties));
        configuredBusMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, ConfiguredBusAttributes::getAliasByType, ConfiguredBusAttributes::setAliasByType));
        configuredBusMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, ConfiguredBusAttributes::getAliasesWithoutType, ConfiguredBusAttributes::setAliasesWithoutType));
        configuredBusMappings.put("v", new Mapping<>(Double.class, ConfiguredBusAttributes::getV, ConfiguredBusAttributes::setV));
        configuredBusMappings.put("angle", new Mapping<>(Double.class, ConfiguredBusAttributes::getAngle, ConfiguredBusAttributes::setAngle));
    }

    public Map<String, Mapping> getDanglingLineMappings() {
        return danglingLineMappings;
    }

    private void createDanglingLineMappings() {
        danglingLineMappings.put("name", new Mapping<>(String.class, DanglingLineAttributes::getName, DanglingLineAttributes::setName));
        danglingLineMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, DanglingLineAttributes::getVoltageLevelId, DanglingLineAttributes::setVoltageLevelId));
        danglingLineMappings.put("bus", new Mapping<>(String.class, DanglingLineAttributes::getBus, DanglingLineAttributes::setBus));
        danglingLineMappings.put(CONNECTABLE_BUS, new Mapping<>(String.class, DanglingLineAttributes::getConnectableBus, DanglingLineAttributes::setConnectableBus));
        danglingLineMappings.put("r", new Mapping<>(Double.class, DanglingLineAttributes::getR, DanglingLineAttributes::setR));
        danglingLineMappings.put("x", new Mapping<>(Double.class, DanglingLineAttributes::getX, DanglingLineAttributes::setX));
        danglingLineMappings.put("g", new Mapping<>(Double.class, DanglingLineAttributes::getG, DanglingLineAttributes::setG));
        danglingLineMappings.put("b", new Mapping<>(Double.class, DanglingLineAttributes::getB, DanglingLineAttributes::setB));
        danglingLineMappings.put("p0", new Mapping<>(Double.class, DanglingLineAttributes::getP0, DanglingLineAttributes::setP0));
        danglingLineMappings.put("q0", new Mapping<>(Double.class, DanglingLineAttributes::getQ0, DanglingLineAttributes::setQ0));
        danglingLineMappings.put("p", new Mapping<>(Double.class, DanglingLineAttributes::getP, DanglingLineAttributes::setP));
        danglingLineMappings.put("q", new Mapping<>(Double.class, DanglingLineAttributes::getQ, DanglingLineAttributes::setQ));
        danglingLineMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, DanglingLineAttributes::isFictitious, DanglingLineAttributes::setFictitious));
        danglingLineMappings.put("node", new Mapping<>(Integer.class, DanglingLineAttributes::getNode, DanglingLineAttributes::setNode));
        danglingLineMappings.put(PROPERTIES, new Mapping<>(Map.class, DanglingLineAttributes::getProperties, DanglingLineAttributes::setProperties));
        danglingLineMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, DanglingLineAttributes::getAliasByType, DanglingLineAttributes::setAliasByType));
        danglingLineMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, DanglingLineAttributes::getAliasesWithoutType, DanglingLineAttributes::setAliasesWithoutType));
        danglingLineMappings.put("generation", new Mapping<>(DanglingLineGenerationAttributes.class, DanglingLineAttributes::getGeneration, DanglingLineAttributes::setGeneration));
        danglingLineMappings.put("ucteXnodeCode", new Mapping<>(String.class, DanglingLineAttributes::getUcteXnodeCode, DanglingLineAttributes::setUcteXnodeCode));
        danglingLineMappings.put("currentLimits", new Mapping<>(LimitsAttributes.class, DanglingLineAttributes::getCurrentLimits, DanglingLineAttributes::setCurrentLimits));
        danglingLineMappings.put(POSITION, new Mapping<>(ConnectablePositionAttributes.class, DanglingLineAttributes::getPosition, DanglingLineAttributes::setPosition));
        danglingLineMappings.put("apparentPowerLimits", new Mapping<>(LimitsAttributes.class, DanglingLineAttributes::getApparentPowerLimits, DanglingLineAttributes::setApparentPowerLimits));
        danglingLineMappings.put("activePowerLimits", new Mapping<>(LimitsAttributes.class, DanglingLineAttributes::getActivePowerLimits, DanglingLineAttributes::setActivePowerLimits));
    }

    public Map<String, Mapping> getShuntCompensatorMappings() {
        return shuntCompensatorMappings;
    }

    private void createShuntCompensatorMappings() {
        shuntCompensatorMappings.put("name", new Mapping<>(String.class, ShuntCompensatorAttributes::getName, ShuntCompensatorAttributes::setName));
        shuntCompensatorMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, ShuntCompensatorAttributes::getVoltageLevelId, ShuntCompensatorAttributes::setVoltageLevelId));
        shuntCompensatorMappings.put("bus", new Mapping<>(String.class, ShuntCompensatorAttributes::getBus, ShuntCompensatorAttributes::setBus));
        shuntCompensatorMappings.put(CONNECTABLE_BUS, new Mapping<>(String.class, ShuntCompensatorAttributes::getConnectableBus, ShuntCompensatorAttributes::setConnectableBus));
        shuntCompensatorMappings.put("p", new Mapping<>(Double.class, ShuntCompensatorAttributes::getP, ShuntCompensatorAttributes::setP));
        shuntCompensatorMappings.put("q", new Mapping<>(Double.class, ShuntCompensatorAttributes::getQ, ShuntCompensatorAttributes::setQ));
        shuntCompensatorMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, ShuntCompensatorAttributes::isFictitious, ShuntCompensatorAttributes::setFictitious));
        shuntCompensatorMappings.put(VOLTAGE_REGULATOR_ON, new Mapping<>(Boolean.class, ShuntCompensatorAttributes::isVoltageRegulatorOn, ShuntCompensatorAttributes::setVoltageRegulatorOn));
        shuntCompensatorMappings.put("targetV", new Mapping<>(Double.class, ShuntCompensatorAttributes::getTargetV, ShuntCompensatorAttributes::setTargetV));
        shuntCompensatorMappings.put("targetDeadband", new Mapping<>(Double.class, ShuntCompensatorAttributes::getTargetDeadband, ShuntCompensatorAttributes::setTargetDeadband));
        shuntCompensatorMappings.put(REGULATION_TERMINAL, new Mapping<>(TerminalRefAttributes.class, ShuntCompensatorAttributes::getRegulatingTerminal, ShuntCompensatorAttributes::setRegulatingTerminal));
        shuntCompensatorMappings.put("linearModel", new Mapping<>(ShuntCompensatorModelAttributes.class, (ShuntCompensatorAttributes attributes) ->
            attributes.getModel() instanceof ShuntCompensatorLinearModelAttributes ? attributes.getModel() : null,
            (ShuntCompensatorAttributes attributes, ShuntCompensatorModelAttributes model) -> {
                if (model instanceof ShuntCompensatorLinearModelAttributes) {
                    attributes.setModel(model);
                }
            }));
        shuntCompensatorMappings.put("nonLinearModel", new Mapping<>(ShuntCompensatorModelAttributes.class, (ShuntCompensatorAttributes attributes) ->
            attributes.getModel() instanceof ShuntCompensatorNonLinearModelAttributes ? attributes.getModel() : null,
            (ShuntCompensatorAttributes attributes, ShuntCompensatorModelAttributes model) -> {
                if (model instanceof ShuntCompensatorNonLinearModelAttributes) {
                    attributes.setModel(model);
                }
            }));
        shuntCompensatorMappings.put("node", new Mapping<>(Integer.class, ShuntCompensatorAttributes::getNode, ShuntCompensatorAttributes::setNode));
        shuntCompensatorMappings.put("sectionCount", new Mapping<>(Integer.class, ShuntCompensatorAttributes::getSectionCount, ShuntCompensatorAttributes::setSectionCount));
        shuntCompensatorMappings.put(PROPERTIES, new Mapping<>(Map.class, ShuntCompensatorAttributes::getProperties, ShuntCompensatorAttributes::setProperties));
        shuntCompensatorMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, ShuntCompensatorAttributes::getAliasByType, ShuntCompensatorAttributes::setAliasByType));
        shuntCompensatorMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, ShuntCompensatorAttributes::getAliasesWithoutType, ShuntCompensatorAttributes::setAliasesWithoutType));
        shuntCompensatorMappings.put(POSITION, new Mapping<>(ConnectablePositionAttributes.class, ShuntCompensatorAttributes::getPosition, ShuntCompensatorAttributes::setPosition));
    }

    public Map<String, Mapping> getVscConverterStationMappings() {
        return vscConverterStationMappings;
    }

    private void createVscConverterStationMappings() {
        vscConverterStationMappings.put("name", new Mapping<>(String.class, VscConverterStationAttributes::getName, VscConverterStationAttributes::setName));
        vscConverterStationMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, VscConverterStationAttributes::getVoltageLevelId, VscConverterStationAttributes::setVoltageLevelId));
        vscConverterStationMappings.put("bus", new Mapping<>(String.class, VscConverterStationAttributes::getBus, VscConverterStationAttributes::setBus));
        vscConverterStationMappings.put(CONNECTABLE_BUS, new Mapping<>(String.class, VscConverterStationAttributes::getConnectableBus, VscConverterStationAttributes::setConnectableBus));
        vscConverterStationMappings.put(VOLTAGE_REGULATOR_ON, new Mapping<>(Boolean.class, VscConverterStationAttributes::getVoltageRegulatorOn, VscConverterStationAttributes::setVoltageRegulatorOn));
        vscConverterStationMappings.put("p", new Mapping<>(Double.class, VscConverterStationAttributes::getP, VscConverterStationAttributes::setP));
        vscConverterStationMappings.put("q", new Mapping<>(Double.class, VscConverterStationAttributes::getQ, VscConverterStationAttributes::setQ));
        vscConverterStationMappings.put("lossFactor", new Mapping<>(Float.class, VscConverterStationAttributes::getLossFactor, VscConverterStationAttributes::setLossFactor));
        vscConverterStationMappings.put("reactivePowerSetPoint", new Mapping<>(Double.class, VscConverterStationAttributes::getReactivePowerSetPoint, VscConverterStationAttributes::setReactivePowerSetPoint));
        vscConverterStationMappings.put("voltageSetPoint", new Mapping<>(Double.class, VscConverterStationAttributes::getVoltageSetPoint, VscConverterStationAttributes::setVoltageSetPoint));
        vscConverterStationMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, VscConverterStationAttributes::isFictitious, VscConverterStationAttributes::setFictitious));
        vscConverterStationMappings.put(MIN_MAX_REACIVE_LIMITS, new Mapping<>(ReactiveLimitsAttributes.class, (VscConverterStationAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (VscConverterStationAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        vscConverterStationMappings.put(REACTIVE_CAPABILITY_CURVE, new Mapping<>(ReactiveLimitsAttributes.class, (VscConverterStationAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (VscConverterStationAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        vscConverterStationMappings.put("node", new Mapping<>(Integer.class, VscConverterStationAttributes::getNode, VscConverterStationAttributes::setNode));
        vscConverterStationMappings.put(PROPERTIES, new Mapping<>(Map.class, VscConverterStationAttributes::getProperties, VscConverterStationAttributes::setProperties));
        vscConverterStationMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, VscConverterStationAttributes::getAliasByType, VscConverterStationAttributes::setAliasByType));
        vscConverterStationMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, VscConverterStationAttributes::getAliasesWithoutType, VscConverterStationAttributes::setAliasesWithoutType));
        vscConverterStationMappings.put(POSITION, new Mapping<>(ConnectablePositionAttributes.class, VscConverterStationAttributes::getPosition, VscConverterStationAttributes::setPosition));
    }

    public Map<String, Mapping> getLccConverterStationMappings() {
        return lccConverterStationMappings;
    }

    private void createLccConverterStationMappings() {
        lccConverterStationMappings.put("name", new Mapping<>(String.class, LccConverterStationAttributes::getName, LccConverterStationAttributes::setName));
        lccConverterStationMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, LccConverterStationAttributes::getVoltageLevelId, LccConverterStationAttributes::setVoltageLevelId));
        lccConverterStationMappings.put("bus", new Mapping<>(String.class, LccConverterStationAttributes::getBus, LccConverterStationAttributes::setBus));
        lccConverterStationMappings.put(CONNECTABLE_BUS, new Mapping<>(String.class, LccConverterStationAttributes::getConnectableBus, LccConverterStationAttributes::setConnectableBus));
        lccConverterStationMappings.put("p", new Mapping<>(Double.class, LccConverterStationAttributes::getP, LccConverterStationAttributes::setP));
        lccConverterStationMappings.put("q", new Mapping<>(Double.class, LccConverterStationAttributes::getQ, LccConverterStationAttributes::setQ));
        lccConverterStationMappings.put("powerFactor", new Mapping<>(Float.class, LccConverterStationAttributes::getPowerFactor, LccConverterStationAttributes::setPowerFactor));
        lccConverterStationMappings.put("lossFactor", new Mapping<>(Float.class, LccConverterStationAttributes::getLossFactor, LccConverterStationAttributes::setLossFactor));
        lccConverterStationMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, LccConverterStationAttributes::isFictitious, LccConverterStationAttributes::setFictitious));
        lccConverterStationMappings.put("node", new Mapping<>(Integer.class, LccConverterStationAttributes::getNode, LccConverterStationAttributes::setNode));
        lccConverterStationMappings.put(PROPERTIES, new Mapping<>(Map.class, LccConverterStationAttributes::getProperties, LccConverterStationAttributes::setProperties));
        lccConverterStationMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, LccConverterStationAttributes::getAliasByType, LccConverterStationAttributes::setAliasByType));
        lccConverterStationMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, LccConverterStationAttributes::getAliasesWithoutType, LccConverterStationAttributes::setAliasesWithoutType));
        lccConverterStationMappings.put(POSITION, new Mapping<>(ConnectablePositionAttributes.class, LccConverterStationAttributes::getPosition, LccConverterStationAttributes::setPosition));
    }

    public Map<String, Mapping> getStaticVarCompensatorMappings() {
        return staticVarCompensatorMappings;
    }

    private void createStaticVarCompensatorMappings() {
        staticVarCompensatorMappings.put("name", new Mapping<>(String.class, StaticVarCompensatorAttributes::getName, StaticVarCompensatorAttributes::setName));
        staticVarCompensatorMappings.put(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, StaticVarCompensatorAttributes::getVoltageLevelId, StaticVarCompensatorAttributes::setVoltageLevelId));
        staticVarCompensatorMappings.put("bus", new Mapping<>(String.class, StaticVarCompensatorAttributes::getBus, StaticVarCompensatorAttributes::setBus));
        staticVarCompensatorMappings.put(CONNECTABLE_BUS, new Mapping<>(String.class, StaticVarCompensatorAttributes::getConnectableBus, StaticVarCompensatorAttributes::setConnectableBus));
        staticVarCompensatorMappings.put("bmin", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getBmin, StaticVarCompensatorAttributes::setBmin));
        staticVarCompensatorMappings.put("bmax", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getBmax, StaticVarCompensatorAttributes::setBmax));
        staticVarCompensatorMappings.put("voltageSetPoint", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getVoltageSetPoint, StaticVarCompensatorAttributes::setVoltageSetPoint));
        staticVarCompensatorMappings.put("reactivePowerSetPoint", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getReactivePowerSetPoint, StaticVarCompensatorAttributes::setReactivePowerSetPoint));
        staticVarCompensatorMappings.put("regulationMode", new Mapping<>(StaticVarCompensator.RegulationMode.class, StaticVarCompensatorAttributes::getRegulationMode, StaticVarCompensatorAttributes::setRegulationMode));
        staticVarCompensatorMappings.put("p", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getP, StaticVarCompensatorAttributes::setP));
        staticVarCompensatorMappings.put("q", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getQ, StaticVarCompensatorAttributes::setQ));
        staticVarCompensatorMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, StaticVarCompensatorAttributes::isFictitious, StaticVarCompensatorAttributes::setFictitious));
        staticVarCompensatorMappings.put(REGULATION_TERMINAL, new Mapping<>(TerminalRefAttributes.class, StaticVarCompensatorAttributes::getRegulatingTerminal, StaticVarCompensatorAttributes::setRegulatingTerminal));
        staticVarCompensatorMappings.put("node", new Mapping<>(Integer.class, StaticVarCompensatorAttributes::getNode, StaticVarCompensatorAttributes::setNode));
        staticVarCompensatorMappings.put(PROPERTIES, new Mapping<>(Map.class, StaticVarCompensatorAttributes::getProperties, StaticVarCompensatorAttributes::setProperties));
        staticVarCompensatorMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, StaticVarCompensatorAttributes::getAliasByType, StaticVarCompensatorAttributes::setAliasByType));
        staticVarCompensatorMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, StaticVarCompensatorAttributes::getAliasesWithoutType, StaticVarCompensatorAttributes::setAliasesWithoutType));
        staticVarCompensatorMappings.put(POSITION, new Mapping<>(ConnectablePositionAttributes.class, StaticVarCompensatorAttributes::getPosition, StaticVarCompensatorAttributes::setPosition));
        staticVarCompensatorMappings.put("voltagePerReactivePowerControl", new Mapping<>(VoltagePerReactivePowerControlAttributes.class, StaticVarCompensatorAttributes::getVoltagePerReactiveControl, StaticVarCompensatorAttributes::setVoltagePerReactiveControl));
    }

    public Map<String, Mapping> getHvdcLineMappings() {
        return hvdcLineMappings;
    }

    private void createHvdcLineMappings() {
        hvdcLineMappings.put("name", new Mapping<>(String.class, HvdcLineAttributes::getName, HvdcLineAttributes::setName));
        hvdcLineMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, HvdcLineAttributes::isFictitious, HvdcLineAttributes::setFictitious));
        hvdcLineMappings.put(PROPERTIES, new Mapping<>(Map.class, HvdcLineAttributes::getProperties, HvdcLineAttributes::setProperties));
        hvdcLineMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, HvdcLineAttributes::getAliasByType, HvdcLineAttributes::setAliasByType));
        hvdcLineMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, HvdcLineAttributes::getAliasesWithoutType, HvdcLineAttributes::setAliasesWithoutType));
        hvdcLineMappings.put("r", new Mapping<>(Double.class, HvdcLineAttributes::getR, HvdcLineAttributes::setR));
        hvdcLineMappings.put("nominalV", new Mapping<>(Double.class, HvdcLineAttributes::getNominalV, HvdcLineAttributes::setNominalV));
        hvdcLineMappings.put("activePowerSetpoint", new Mapping<>(Double.class, HvdcLineAttributes::getActivePowerSetpoint, HvdcLineAttributes::setActivePowerSetpoint));
        hvdcLineMappings.put("maxP", new Mapping<>(Double.class, HvdcLineAttributes::getMaxP, HvdcLineAttributes::setMaxP));
        hvdcLineMappings.put("convertersMode", new Mapping<>(HvdcLine.ConvertersMode.class, HvdcLineAttributes::getConvertersMode, HvdcLineAttributes::setConvertersMode));
        hvdcLineMappings.put("converterStationId1", new Mapping<>(String.class, HvdcLineAttributes::getConverterStationId1, HvdcLineAttributes::setConverterStationId1));
        hvdcLineMappings.put("converterStationId2", new Mapping<>(String.class, HvdcLineAttributes::getConverterStationId2, HvdcLineAttributes::setConverterStationId2));
        hvdcLineMappings.put("hvdcAngleDroopActivePowerControl", new Mapping<>(HvdcAngleDroopActivePowerControlAttributes.class, HvdcLineAttributes::getHvdcAngleDroopActivePowerControl, HvdcLineAttributes::setHvdcAngleDroopActivePowerControl));
        hvdcLineMappings.put("hvdcOperatorActivePowerRange", new Mapping<>(HvdcOperatorActivePowerRangeAttributes.class, HvdcLineAttributes::getHvdcOperatorActivePowerRange, HvdcLineAttributes::setHvdcOperatorActivePowerRange));
    }

    public Map<String, Mapping> getTwoWindingsTransformerMappings() {
        return twoWindingsTransformerMappings;
    }

    private void createTwoWindingsTransformerMappings() {
        twoWindingsTransformerMappings.put("name", new Mapping<>(String.class, TwoWindingsTransformerAttributes::getName, TwoWindingsTransformerAttributes::setName));
        twoWindingsTransformerMappings.put(VOLTAGE_LEVEL_ID_1, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getVoltageLevelId1, TwoWindingsTransformerAttributes::setVoltageLevelId1));
        twoWindingsTransformerMappings.put(VOLTAGE_LEVEL_ID_2, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getVoltageLevelId2, TwoWindingsTransformerAttributes::setVoltageLevelId2));
        twoWindingsTransformerMappings.put("bus1", new Mapping<>(String.class, TwoWindingsTransformerAttributes::getBus1, TwoWindingsTransformerAttributes::setBus1));
        twoWindingsTransformerMappings.put("bus2", new Mapping<>(String.class, TwoWindingsTransformerAttributes::getBus2, TwoWindingsTransformerAttributes::setBus2));
        twoWindingsTransformerMappings.put(CONNECTABLE_BUS_1, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getConnectableBus1, TwoWindingsTransformerAttributes::setConnectableBus1));
        twoWindingsTransformerMappings.put(CONNECTABLE_BUS_2, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getConnectableBus2, TwoWindingsTransformerAttributes::setConnectableBus2));
        twoWindingsTransformerMappings.put(BRANCH_STATUS, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getBranchStatus, TwoWindingsTransformerAttributes::setBranchStatus));
        twoWindingsTransformerMappings.put("r", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getR, TwoWindingsTransformerAttributes::setR));
        twoWindingsTransformerMappings.put("x", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getX, TwoWindingsTransformerAttributes::setX));
        twoWindingsTransformerMappings.put("g", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getG, TwoWindingsTransformerAttributes::setG));
        twoWindingsTransformerMappings.put("b", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getB, TwoWindingsTransformerAttributes::setB));
        twoWindingsTransformerMappings.put("ratedU1", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getRatedU1, TwoWindingsTransformerAttributes::setRatedU1));
        twoWindingsTransformerMappings.put("ratedU2", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getRatedU2, TwoWindingsTransformerAttributes::setRatedU2));
        twoWindingsTransformerMappings.put("ratedS", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getRatedS, TwoWindingsTransformerAttributes::setRatedS));
        twoWindingsTransformerMappings.put("p1", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getP1, TwoWindingsTransformerAttributes::setP1));
        twoWindingsTransformerMappings.put("q1", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getQ1, TwoWindingsTransformerAttributes::setQ1));
        twoWindingsTransformerMappings.put("p2", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getP2, TwoWindingsTransformerAttributes::setP2));
        twoWindingsTransformerMappings.put("q2", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getQ2, TwoWindingsTransformerAttributes::setQ2));
        twoWindingsTransformerMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, TwoWindingsTransformerAttributes::isFictitious, TwoWindingsTransformerAttributes::setFictitious));
        twoWindingsTransformerMappings.put(NODE_1, new Mapping<>(Integer.class, TwoWindingsTransformerAttributes::getNode1, TwoWindingsTransformerAttributes::setNode1));
        twoWindingsTransformerMappings.put(NODE_2, new Mapping<>(Integer.class, TwoWindingsTransformerAttributes::getNode2, TwoWindingsTransformerAttributes::setNode2));
        twoWindingsTransformerMappings.put(PROPERTIES, new Mapping<>(Map.class, TwoWindingsTransformerAttributes::getProperties, TwoWindingsTransformerAttributes::setProperties));
        twoWindingsTransformerMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, TwoWindingsTransformerAttributes::getAliasByType, TwoWindingsTransformerAttributes::setAliasByType));
        twoWindingsTransformerMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, TwoWindingsTransformerAttributes::getAliasesWithoutType, TwoWindingsTransformerAttributes::setAliasesWithoutType));
        twoWindingsTransformerMappings.put(POSITION_1, new Mapping<>(ConnectablePositionAttributes.class, TwoWindingsTransformerAttributes::getPosition1, TwoWindingsTransformerAttributes::setPosition1));
        twoWindingsTransformerMappings.put(POSITION_2, new Mapping<>(ConnectablePositionAttributes.class, TwoWindingsTransformerAttributes::getPosition2, TwoWindingsTransformerAttributes::setPosition2));
        twoWindingsTransformerMappings.put(CURRENT_LIMITS_1, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getCurrentLimits1, TwoWindingsTransformerAttributes::setCurrentLimits1));
        twoWindingsTransformerMappings.put(CURRENT_LIMITS_2, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getCurrentLimits2, TwoWindingsTransformerAttributes::setCurrentLimits2));
        twoWindingsTransformerMappings.put(APPARENT_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getApparentPowerLimits1, TwoWindingsTransformerAttributes::setApparentPowerLimits1));
        twoWindingsTransformerMappings.put(APPARENT_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getApparentPowerLimits2, TwoWindingsTransformerAttributes::setApparentPowerLimits2));
        twoWindingsTransformerMappings.put(ACTIVE_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getActivePowerLimits1, TwoWindingsTransformerAttributes::setActivePowerLimits1));
        twoWindingsTransformerMappings.put(ACTIVE_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getActivePowerLimits2, TwoWindingsTransformerAttributes::setActivePowerLimits2));
        twoWindingsTransformerMappings.put("cgmesTapChangers", new Mapping<>(List.class, TwoWindingsTransformerAttributes::getCgmesTapChangerAttributesList, TwoWindingsTransformerAttributes::setCgmesTapChangerAttributesList));
        twoWindingsTransformerMappings.put("phaseTapChanger", new Mapping<>(PhaseTapChangerAttributes.class, TwoWindingsTransformerAttributes::getPhaseTapChangerAttributes, TwoWindingsTransformerAttributes::setPhaseTapChangerAttributes));
        twoWindingsTransformerMappings.put("ratioTapChanger", new Mapping<>(RatioTapChangerAttributes.class, TwoWindingsTransformerAttributes::getRatioTapChangerAttributes, TwoWindingsTransformerAttributes::setRatioTapChangerAttributes));
        twoWindingsTransformerMappings.put("phaseAngleClock", new Mapping<>(TwoWindingsTransformerPhaseAngleClockAttributes.class, TwoWindingsTransformerAttributes::getPhaseAngleClockAttributes, TwoWindingsTransformerAttributes::setPhaseAngleClockAttributes));
    }

    public Map<String, Mapping> getThreeWindingsTransformerMappings() {
        return threeWindingsTransformerMappings;
    }

    private void createThreeWindingsTransformerMappings() {
        threeWindingsTransformerMappings.put("name", new Mapping<>(String.class, ThreeWindingsTransformerAttributes::getName, ThreeWindingsTransformerAttributes::setName));
        threeWindingsTransformerMappings.put(BRANCH_STATUS, new Mapping<>(String.class, ThreeWindingsTransformerAttributes::getBranchStatus, ThreeWindingsTransformerAttributes::setBranchStatus));
        threeWindingsTransformerMappings.put("p1", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getP1, ThreeWindingsTransformerAttributes::setP1));
        threeWindingsTransformerMappings.put("q1", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getQ1, ThreeWindingsTransformerAttributes::setQ1));
        threeWindingsTransformerMappings.put("p2", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getP2, ThreeWindingsTransformerAttributes::setP2));
        threeWindingsTransformerMappings.put("q2", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getQ2, ThreeWindingsTransformerAttributes::setQ2));
        threeWindingsTransformerMappings.put("p3", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getP3, ThreeWindingsTransformerAttributes::setP3));
        threeWindingsTransformerMappings.put("q3", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getQ3, ThreeWindingsTransformerAttributes::setQ3));
        threeWindingsTransformerMappings.put("ratedU0", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getRatedU0, ThreeWindingsTransformerAttributes::setRatedU0));
        threeWindingsTransformerMappings.put(FICTITIOUS, new Mapping<>(Boolean.class, ThreeWindingsTransformerAttributes::isFictitious, ThreeWindingsTransformerAttributes::setFictitious));
        threeWindingsTransformerMappings.put(PROPERTIES, new Mapping<>(Map.class, ThreeWindingsTransformerAttributes::getProperties, ThreeWindingsTransformerAttributes::setProperties));
        threeWindingsTransformerMappings.put(ALIAS_BY_TYPE, new Mapping<>(Map.class, ThreeWindingsTransformerAttributes::getAliasByType, ThreeWindingsTransformerAttributes::setAliasByType));
        threeWindingsTransformerMappings.put(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, ThreeWindingsTransformerAttributes::getAliasesWithoutType, ThreeWindingsTransformerAttributes::setAliasesWithoutType));
        threeWindingsTransformerMappings.put(POSITION_1, new Mapping<>(ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition1, ThreeWindingsTransformerAttributes::setPosition1));
        threeWindingsTransformerMappings.put(POSITION_2, new Mapping<>(ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition2, ThreeWindingsTransformerAttributes::setPosition2));
        threeWindingsTransformerMappings.put("position3", new Mapping<>(ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition3, ThreeWindingsTransformerAttributes::setPosition3));
        threeWindingsTransformerMappings.put("cgmesTapChangers", new Mapping<>(List.class, ThreeWindingsTransformerAttributes::getCgmesTapChangerAttributesList, ThreeWindingsTransformerAttributes::setCgmesTapChangerAttributesList));
        threeWindingsTransformerMappings.put("phaseAngleClock", new Mapping<>(ThreeWindingsTransformerPhaseAngleClockAttributes.class, ThreeWindingsTransformerAttributes::getPhaseAngleClock, ThreeWindingsTransformerAttributes::setPhaseAngleClock));
        threeWindingsTransformerMappings.put(VOLTAGE_LEVEL_ID_1, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg1().setVoltageLevelId(vId)));
        threeWindingsTransformerMappings.put(VOLTAGE_LEVEL_ID_2, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg2().setVoltageLevelId(vId)));
        threeWindingsTransformerMappings.put(VOLTAGE_LEVEL_ID_3, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg3().setVoltageLevelId(vId)));
        threeWindingsTransformerMappings.put(NODE_1, new Mapping<>(Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg1().setNode(node)));
        threeWindingsTransformerMappings.put(NODE_2, new Mapping<>(Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg2().setNode(node)));
        threeWindingsTransformerMappings.put("node3", new Mapping<>(Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg3().setNode(node)));
        threeWindingsTransformerMappings.put("bus1", new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg1().setBus(bus)));
        threeWindingsTransformerMappings.put("bus2", new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg2().setBus(bus)));
        threeWindingsTransformerMappings.put("bus3", new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg3().setBus(bus)));
        threeWindingsTransformerMappings.put(CONNECTABLE_BUS_1, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg1().setConnectableBus(bus)));
        threeWindingsTransformerMappings.put(CONNECTABLE_BUS_2, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg2().setConnectableBus(bus)));
        threeWindingsTransformerMappings.put("connectableBus3", new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg3().setConnectableBus(bus)));
        threeWindingsTransformerMappings.put("r1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg1().setR(r)));
        threeWindingsTransformerMappings.put("r2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg2().setR(r)));
        threeWindingsTransformerMappings.put("r3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg3().setR(r)));
        threeWindingsTransformerMappings.put("x1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg1().setX(x)));
        threeWindingsTransformerMappings.put("x2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg2().setX(x)));
        threeWindingsTransformerMappings.put("x3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg3().setX(x)));
        threeWindingsTransformerMappings.put("g1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg1().setG(g)));
        threeWindingsTransformerMappings.put("g2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg2().setG(g)));
        threeWindingsTransformerMappings.put("g3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg3().setG(g)));
        threeWindingsTransformerMappings.put("b1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg1().setB(b)));
        threeWindingsTransformerMappings.put("b2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg2().setB(b)));
        threeWindingsTransformerMappings.put("b3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg3().setB(b)));
        threeWindingsTransformerMappings.put("ratedU1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg1().setRatedU(ratedU)));
        threeWindingsTransformerMappings.put("ratedU2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg2().setRatedU(ratedU)));
        threeWindingsTransformerMappings.put("ratedU3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg3().setRatedU(ratedU)));
        threeWindingsTransformerMappings.put("ratedS1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg1().setRatedS(ratedS)));
        threeWindingsTransformerMappings.put("ratedS2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg2().setRatedS(ratedS)));
        threeWindingsTransformerMappings.put("ratedS3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg3().setRatedS(ratedS)));
        threeWindingsTransformerMappings.put("phaseTapChanger1", new Mapping<>(PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg1().setPhaseTapChangerAttributes(phaseTapChanger)));
        threeWindingsTransformerMappings.put("phaseTapChanger2", new Mapping<>(PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg2().setPhaseTapChangerAttributes(phaseTapChanger)));
        threeWindingsTransformerMappings.put("phaseTapChanger3", new Mapping<>(PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg3().setPhaseTapChangerAttributes(phaseTapChanger)));
        threeWindingsTransformerMappings.put("ratioTapChanger1", new Mapping<>(RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg1().setRatioTapChangerAttributes(ratioTapChanger)));
        threeWindingsTransformerMappings.put("ratioTapChanger2", new Mapping<>(RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg2().setRatioTapChangerAttributes(ratioTapChanger)));
        threeWindingsTransformerMappings.put("ratioTapChanger3", new Mapping<>(RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg3().setRatioTapChangerAttributes(ratioTapChanger)));
        threeWindingsTransformerMappings.put(CURRENT_LIMITS_1, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setCurrentLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put(CURRENT_LIMITS_2, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setCurrentLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("currentLimits3", new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setCurrentLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put(APPARENT_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setApparentPowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put(APPARENT_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setApparentPowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("apparentPowerLimits3", new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setApparentPowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put(ACTIVE_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setActivePowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put(ACTIVE_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setActivePowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("activePowerLimits3", new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setActivePowerLimitsAttributes(limits)));
    }

    private Mappings() {
        createLineMappings();
        createLoadMappings();
        createGeneratorMappings();
        createSwitchMappings();
        createSubstationMappings();
        createNetworkMappings();
        createBatteryMappings();
        createVoltageLevelMappings();
        createBusbarSectionMappings();
        createConfiguredBusMappings();
        createDanglingLineMappings();
        createShuntCompensatorMappings();
        createVscConverterStationMappings();
        createLccConverterStationMappings();
        createStaticVarCompensatorMappings();
        createHvdcLineMappings();
        createTwoWindingsTransformerMappings();
        createThreeWindingsTransformerMappings();
    }

    public static Mappings getInstance() {
        if (instance == null) {
            instance = new Mappings();
        }
        return instance;
    }
}
