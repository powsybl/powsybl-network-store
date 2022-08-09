/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class Mappings {

    private final TableMapping lineMappings = new TableMapping("line", ResourceType.LINE, LineAttributes::new);
    private final TableMapping loadMappings = new TableMapping("load", ResourceType.LOAD, LoadAttributes::new);
    private final TableMapping generatorMappings = new TableMapping("generator", ResourceType.GENERATOR, GeneratorAttributes::new);
    private final TableMapping switchMappings = new TableMapping("switch", ResourceType.SWITCH, SwitchAttributes::new);
    private final TableMapping substationMappings = new TableMapping("substation", ResourceType.SUBSTATION, SubstationAttributes::new);
    private final TableMapping networkMappings = new TableMapping("network", ResourceType.NETWORK, NetworkAttributes::new);
    private final TableMapping voltageLevelMappings = new TableMapping("voltageLevel", ResourceType.VOLTAGE_LEVEL, VoltageLevelAttributes::new);
    private final TableMapping batteryMappings = new TableMapping("battery", ResourceType.BATTERY, BatteryAttributes::new);
    private final TableMapping busbarSectionMappings = new TableMapping("busbarSection", ResourceType.BUSBAR_SECTION, BusbarSectionAttributes::new);
    private final TableMapping configuredBusMappings = new TableMapping("configuredBus", ResourceType.CONFIGURED_BUS, ConfiguredBusAttributes::new);
    private final TableMapping danglingLineMappings = new TableMapping("danglingLine", ResourceType.DANGLING_LINE, DanglingLineAttributes::new);
    private final TableMapping shuntCompensatorMappings = new TableMapping("shuntCompensator", ResourceType.SHUNT_COMPENSATOR, ShuntCompensatorAttributes::new);
    private final TableMapping vscConverterStationMappings = new TableMapping("vscConverterStation", ResourceType.VSC_CONVERTER_STATION, VscConverterStationAttributes::new);
    private final TableMapping lccConverterStationMappings = new TableMapping("lccConverterStation", ResourceType.LCC_CONVERTER_STATION, LccConverterStationAttributes::new);
    private final TableMapping staticVarCompensatorMappings = new TableMapping("staticVarCompensator", ResourceType.STATIC_VAR_COMPENSATOR, StaticVarCompensatorAttributes::new);
    private final TableMapping hvdcLineMappings = new TableMapping("hvdcLine", ResourceType.HVDC_LINE, HvdcLineAttributes::new);
    private final TableMapping twoWindingsTransformerMappings = new TableMapping("twoWindingsTransformer", ResourceType.TWO_WINDINGS_TRANSFORMER, TemporaryLimitAttributes::new);
    private final TableMapping threeWindingsTransformerMappings = new TableMapping("threeWindingsTransformer", ResourceType.THREE_WINDINGS_TRANSFORMER, ThreeWindingsTransformerAttributes::new);
    private final TableMapping temporaryLimitMappings = new TableMapping("temporaryLimit", ResourceType.TEMPORARY_LIMIT, TemporaryLimitAttributes::new);

    private final List<TableMapping> all = List.of(lineMappings,
                                                   loadMappings,
                                                   generatorMappings,
                                                   switchMappings,
                                                   substationMappings,
                                                   networkMappings,
                                                   voltageLevelMappings,
                                                   batteryMappings,
                                                   busbarSectionMappings,
                                                   configuredBusMappings,
                                                   danglingLineMappings,
                                                   shuntCompensatorMappings,
                                                   vscConverterStationMappings,
                                                   vscConverterStationMappings,
                                                   lccConverterStationMappings,
                                                   staticVarCompensatorMappings,
                                                   hvdcLineMappings,
                                                   twoWindingsTransformerMappings,
                                                   threeWindingsTransformerMappings,
                                                   temporaryLimitMappings);

    private final Map<String, TableMapping> mappingByTable = new LinkedHashMap<>();

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

    public TableMapping getTableMapping(String table) {
        Objects.requireNonNull(table);
        TableMapping tableMapping = mappingByTable.get(table);
        if (tableMapping == null) {
            throw new IllegalArgumentException("Unknown table: " + table);
        }
        return tableMapping;
    }

    public TableMapping getLineMappings() {
        return lineMappings;
    }

    private void createLineMappings() {
        lineMappings.addColumnMapping("name", new Mapping<>(String.class, LineAttributes::getName, LineAttributes::setName));
        lineMappings.addColumnMapping(VOLTAGE_LEVEL_ID_1, new Mapping<>(String.class, LineAttributes::getVoltageLevelId1, LineAttributes::setVoltageLevelId1));
        lineMappings.addColumnMapping(VOLTAGE_LEVEL_ID_2, new Mapping<>(String.class, LineAttributes::getVoltageLevelId2, LineAttributes::setVoltageLevelId2));
        lineMappings.addColumnMapping("bus1", new Mapping<>(String.class, LineAttributes::getBus1, LineAttributes::setBus1));
        lineMappings.addColumnMapping("bus2", new Mapping<>(String.class, LineAttributes::getBus2, LineAttributes::setBus2));
        lineMappings.addColumnMapping(CONNECTABLE_BUS_1, new Mapping<>(String.class, LineAttributes::getConnectableBus1, LineAttributes::setConnectableBus1));
        lineMappings.addColumnMapping(CONNECTABLE_BUS_2, new Mapping<>(String.class, LineAttributes::getConnectableBus2, LineAttributes::setConnectableBus2));
        lineMappings.addColumnMapping(BRANCH_STATUS, new Mapping<>(String.class, LineAttributes::getBranchStatus, LineAttributes::setBranchStatus));
        lineMappings.addColumnMapping("r", new Mapping<>(Double.class, LineAttributes::getR, LineAttributes::setR));
        lineMappings.addColumnMapping("x", new Mapping<>(Double.class, LineAttributes::getX, LineAttributes::setX));
        lineMappings.addColumnMapping("g1", new Mapping<>(Double.class, LineAttributes::getG1, LineAttributes::setG1));
        lineMappings.addColumnMapping("b1", new Mapping<>(Double.class, LineAttributes::getB1, LineAttributes::setB1));
        lineMappings.addColumnMapping("g2", new Mapping<>(Double.class, LineAttributes::getG2, LineAttributes::setG2));
        lineMappings.addColumnMapping("b2", new Mapping<>(Double.class, LineAttributes::getB2, LineAttributes::setB2));
        lineMappings.addColumnMapping("p1", new Mapping<>(Double.class, LineAttributes::getP1, LineAttributes::setP1));
        lineMappings.addColumnMapping("q1", new Mapping<>(Double.class, LineAttributes::getQ1, LineAttributes::setQ1));
        lineMappings.addColumnMapping("p2", new Mapping<>(Double.class, LineAttributes::getP2, LineAttributes::setP2));
        lineMappings.addColumnMapping("q2", new Mapping<>(Double.class, LineAttributes::getQ2, LineAttributes::setQ2));
        lineMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, LineAttributes::isFictitious, LineAttributes::setFictitious));
        lineMappings.addColumnMapping(NODE_1, new Mapping<>(Integer.class, LineAttributes::getNode1, LineAttributes::setNode1));
        lineMappings.addColumnMapping(NODE_2, new Mapping<>(Integer.class, LineAttributes::getNode2, LineAttributes::setNode2));
        lineMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, LineAttributes::getProperties, LineAttributes::setProperties));
        lineMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, LineAttributes::getAliasByType, LineAttributes::setAliasByType));
        lineMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, LineAttributes::getAliasesWithoutType, LineAttributes::setAliasesWithoutType));
        lineMappings.addColumnMapping(POSITION_1, new Mapping<>(ConnectablePositionAttributes.class, LineAttributes::getPosition1, LineAttributes::setPosition1));
        lineMappings.addColumnMapping(POSITION_2, new Mapping<>(ConnectablePositionAttributes.class, LineAttributes::getPosition2, LineAttributes::setPosition2));
        lineMappings.addColumnMapping("mergedXnode", new Mapping<>(MergedXnodeAttributes.class, LineAttributes::getMergedXnode, LineAttributes::setMergedXnode));
        lineMappings.addColumnMapping(CURRENT_LIMITS_1, new Mapping<>(LimitsAttributes.class, LineAttributes::getCurrentLimits1, LineAttributes::setCurrentLimits1));
        lineMappings.addColumnMapping(CURRENT_LIMITS_2, new Mapping<>(LimitsAttributes.class, LineAttributes::getCurrentLimits2, LineAttributes::setCurrentLimits2));
        lineMappings.addColumnMapping(APPARENT_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class, LineAttributes::getApparentPowerLimits1, LineAttributes::setApparentPowerLimits1));
        lineMappings.addColumnMapping(APPARENT_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class, LineAttributes::getApparentPowerLimits2, LineAttributes::setApparentPowerLimits2));
        lineMappings.addColumnMapping(ACTIVE_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class, LineAttributes::getActivePowerLimits1, LineAttributes::setActivePowerLimits1));
        lineMappings.addColumnMapping(ACTIVE_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class, LineAttributes::getActivePowerLimits2, LineAttributes::setActivePowerLimits2));
    }

    public TableMapping getLoadMappings() {
        return loadMappings;
    }

    private void createLoadMappings() {
        loadMappings.addColumnMapping("name", new Mapping<>(String.class, LoadAttributes::getName, LoadAttributes::setName));
        loadMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, LoadAttributes::getVoltageLevelId, LoadAttributes::setVoltageLevelId));
        loadMappings.addColumnMapping("bus", new Mapping<>(String.class, LoadAttributes::getBus, LoadAttributes::setBus));
        loadMappings.addColumnMapping(CONNECTABLE_BUS, new Mapping<>(String.class, LoadAttributes::getConnectableBus, LoadAttributes::setConnectableBus));
        loadMappings.addColumnMapping("p0", new Mapping<>(Double.class, LoadAttributes::getP0, LoadAttributes::setP0));
        loadMappings.addColumnMapping("q0", new Mapping<>(Double.class, LoadAttributes::getQ0, LoadAttributes::setQ0));
        loadMappings.addColumnMapping("loadType", new Mapping<>(LoadType.class, LoadAttributes::getLoadType, LoadAttributes::setLoadType));
        loadMappings.addColumnMapping("p", new Mapping<>(Double.class, LoadAttributes::getP, LoadAttributes::setP));
        loadMappings.addColumnMapping("q", new Mapping<>(Double.class, LoadAttributes::getQ, LoadAttributes::setQ));
        loadMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, LoadAttributes::isFictitious, LoadAttributes::setFictitious));
        loadMappings.addColumnMapping("node", new Mapping<>(Integer.class, LoadAttributes::getNode, LoadAttributes::setNode));
        loadMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, LoadAttributes::getProperties, LoadAttributes::setProperties));
        loadMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, LoadAttributes::getAliasByType, LoadAttributes::setAliasByType));
        loadMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, LoadAttributes::getAliasesWithoutType, LoadAttributes::setAliasesWithoutType));
        loadMappings.addColumnMapping(POSITION, new Mapping<>(ConnectablePositionAttributes.class, LoadAttributes::getPosition, LoadAttributes::setPosition));
        loadMappings.addColumnMapping("loadDetail", new Mapping<>(LoadDetailAttributes.class, LoadAttributes::getLoadDetail, LoadAttributes::setLoadDetail));
    }

    public TableMapping getGeneratorMappings() {
        return generatorMappings;
    }

    private void createGeneratorMappings() {
        generatorMappings.addColumnMapping("name", new Mapping<>(String.class, GeneratorAttributes::getName, GeneratorAttributes::setName));
        generatorMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, GeneratorAttributes::getVoltageLevelId, GeneratorAttributes::setVoltageLevelId));
        generatorMappings.addColumnMapping("bus", new Mapping<>(String.class, GeneratorAttributes::getBus, GeneratorAttributes::setBus));
        generatorMappings.addColumnMapping(CONNECTABLE_BUS, new Mapping<>(String.class, GeneratorAttributes::getConnectableBus, GeneratorAttributes::setConnectableBus));
        generatorMappings.addColumnMapping("minP", new Mapping<>(Double.class, GeneratorAttributes::getMinP, GeneratorAttributes::setMinP));
        generatorMappings.addColumnMapping("maxP", new Mapping<>(Double.class, GeneratorAttributes::getMaxP, GeneratorAttributes::setMaxP));
        generatorMappings.addColumnMapping("energySource", new Mapping<>(EnergySource.class, GeneratorAttributes::getEnergySource, GeneratorAttributes::setEnergySource));
        generatorMappings.addColumnMapping("p", new Mapping<>(Double.class, GeneratorAttributes::getP, GeneratorAttributes::setP));
        generatorMappings.addColumnMapping("q", new Mapping<>(Double.class, GeneratorAttributes::getQ, GeneratorAttributes::setQ));
        generatorMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, GeneratorAttributes::isFictitious, GeneratorAttributes::setFictitious));
        generatorMappings.addColumnMapping(VOLTAGE_REGULATOR_ON, new Mapping<>(Boolean.class, GeneratorAttributes::isVoltageRegulatorOn, GeneratorAttributes::setVoltageRegulatorOn));
        generatorMappings.addColumnMapping("targetP", new Mapping<>(Double.class, GeneratorAttributes::getTargetP, GeneratorAttributes::setTargetP));
        generatorMappings.addColumnMapping("targetQ", new Mapping<>(Double.class, GeneratorAttributes::getTargetQ, GeneratorAttributes::setTargetQ));
        generatorMappings.addColumnMapping("targetV", new Mapping<>(Double.class, GeneratorAttributes::getTargetV, GeneratorAttributes::setTargetV));
        generatorMappings.addColumnMapping("ratedS", new Mapping<>(Double.class, GeneratorAttributes::getRatedS, GeneratorAttributes::setRatedS));
        generatorMappings.addColumnMapping(MIN_MAX_REACIVE_LIMITS, new Mapping<>(ReactiveLimitsAttributes.class, (GeneratorAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (GeneratorAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        generatorMappings.addColumnMapping(REACTIVE_CAPABILITY_CURVE, new Mapping<>(ReactiveLimitsAttributes.class, (GeneratorAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (GeneratorAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        generatorMappings.addColumnMapping("activePowerControl", new Mapping<>(ActivePowerControlAttributes.class, GeneratorAttributes::getActivePowerControl, GeneratorAttributes::setActivePowerControl));
        generatorMappings.addColumnMapping(REGULATION_TERMINAL, new Mapping<>(TerminalRefAttributes.class, GeneratorAttributes::getRegulatingTerminal, GeneratorAttributes::setRegulatingTerminal));
        generatorMappings.addColumnMapping("coordinatedReactiveControl", new Mapping<>(CoordinatedReactiveControlAttributes.class, GeneratorAttributes::getCoordinatedReactiveControl, GeneratorAttributes::setCoordinatedReactiveControl));
        generatorMappings.addColumnMapping("remoteReactivePowerControl", new Mapping<>(RemoteReactivePowerControlAttributes.class, GeneratorAttributes::getRemoteReactivePowerControl, GeneratorAttributes::setRemoteReactivePowerControl));
        generatorMappings.addColumnMapping("entsoeCategory", new Mapping<>(GeneratorEntsoeCategoryAttributes.class, GeneratorAttributes::getEntsoeCategoryAttributes, GeneratorAttributes::setEntsoeCategoryAttributes));
        generatorMappings.addColumnMapping("node", new Mapping<>(Integer.class, GeneratorAttributes::getNode, GeneratorAttributes::setNode));
        generatorMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, GeneratorAttributes::getProperties, GeneratorAttributes::setProperties));
        generatorMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, GeneratorAttributes::getAliasByType, GeneratorAttributes::setAliasByType));
        generatorMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, GeneratorAttributes::getAliasesWithoutType, GeneratorAttributes::setAliasesWithoutType));
        generatorMappings.addColumnMapping(POSITION, new Mapping<>(ConnectablePositionAttributes.class, GeneratorAttributes::getPosition, GeneratorAttributes::setPosition));
        generatorMappings.addColumnMapping("generatorStartup", new Mapping<>(GeneratorStartupAttributes.class, GeneratorAttributes::getGeneratorStartupAttributes, GeneratorAttributes::setGeneratorStartupAttributes));
    }

    public TableMapping getSwitchMappings() {
        return switchMappings;
    }

    private void createSwitchMappings() {
        switchMappings.addColumnMapping("name", new Mapping<>(String.class, SwitchAttributes::getName, SwitchAttributes::setName));
        switchMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, SwitchAttributes::getVoltageLevelId, SwitchAttributes::setVoltageLevelId));
        switchMappings.addColumnMapping("bus1", new Mapping<>(String.class, SwitchAttributes::getBus1, SwitchAttributes::setBus1));
        switchMappings.addColumnMapping("bus2", new Mapping<>(String.class, SwitchAttributes::getBus2, SwitchAttributes::setBus2));
        switchMappings.addColumnMapping("kind", new Mapping<>(SwitchKind.class, SwitchAttributes::getKind, SwitchAttributes::setKind));
        switchMappings.addColumnMapping("open", new Mapping<>(Boolean.class, SwitchAttributes::isOpen, SwitchAttributes::setOpen));
        switchMappings.addColumnMapping("retained", new Mapping<>(Boolean.class, SwitchAttributes::isRetained, SwitchAttributes::setRetained));
        switchMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, SwitchAttributes::isFictitious, SwitchAttributes::setFictitious));
        switchMappings.addColumnMapping(NODE_1, new Mapping<>(Integer.class, SwitchAttributes::getNode1, SwitchAttributes::setNode1));
        switchMappings.addColumnMapping(NODE_2, new Mapping<>(Integer.class, SwitchAttributes::getNode2, SwitchAttributes::setNode2));
        switchMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, SwitchAttributes::getProperties, SwitchAttributes::setProperties));
        switchMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, SwitchAttributes::getAliasByType, SwitchAttributes::setAliasByType));
        switchMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, SwitchAttributes::getAliasesWithoutType, SwitchAttributes::setAliasesWithoutType));
    }

    public TableMapping getSubstationMappings() {
        return substationMappings;
    }

    private void createSubstationMappings() {
        substationMappings.addColumnMapping("name", new Mapping<>(String.class, SubstationAttributes::getName, SubstationAttributes::setName));
        substationMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, SubstationAttributes::isFictitious, SubstationAttributes::setFictitious));
        substationMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, SubstationAttributes::getProperties, SubstationAttributes::setProperties));
        substationMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, SubstationAttributes::getAliasByType, SubstationAttributes::setAliasByType));
        substationMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, SubstationAttributes::getAliasesWithoutType, SubstationAttributes::setAliasesWithoutType));
        substationMappings.addColumnMapping("country", new Mapping<>(Country.class, SubstationAttributes::getCountry, SubstationAttributes::setCountry));
        substationMappings.addColumnMapping("tso", new Mapping<>(String.class, SubstationAttributes::getTso, SubstationAttributes::setTso));
        substationMappings.addColumnMapping("geographicalTags", new Mapping<>(Set.class, SubstationAttributes::getGeographicalTags, SubstationAttributes::setGeographicalTags));
        substationMappings.addColumnMapping("entsoeArea", new Mapping<>(EntsoeAreaAttributes.class, SubstationAttributes::getEntsoeArea, SubstationAttributes::setEntsoeArea));
    }

    public TableMapping getNetworkMappings() {
        return networkMappings;
    }

    private void createNetworkMappings() {
        networkMappings.addColumnMapping("uuid", new Mapping<>(UUID.class, NetworkAttributes::getUuid, NetworkAttributes::setUuid));
        networkMappings.addColumnMapping("variantId", new Mapping<>(String.class, NetworkAttributes::getVariantId, NetworkAttributes::setVariantId));
        networkMappings.addColumnMapping("name", new Mapping<>(String.class, NetworkAttributes::getName, NetworkAttributes::setName));
        networkMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, NetworkAttributes::isFictitious, NetworkAttributes::setFictitious));
        networkMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, NetworkAttributes::getProperties, NetworkAttributes::setProperties));
        networkMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, NetworkAttributes::getAliasByType, NetworkAttributes::setAliasByType));
        networkMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, NetworkAttributes::getAliasesWithoutType, NetworkAttributes::setAliasesWithoutType));
        networkMappings.addColumnMapping("idByAlias", new Mapping<>(Map.class, NetworkAttributes::getIdByAlias, NetworkAttributes::setIdByAlias));
        networkMappings.addColumnMapping("caseDate", new Mapping<>(Instant.class, (NetworkAttributes attributes) -> attributes.getCaseDate().toDate().toInstant(),
            (NetworkAttributes attributes, Instant instant) -> attributes.setCaseDate(new DateTime(instant.toEpochMilli()))));
        networkMappings.addColumnMapping("forecastDistance", new Mapping<>(Integer.class, NetworkAttributes::getForecastDistance, NetworkAttributes::setForecastDistance));
        networkMappings.addColumnMapping("sourceFormat", new Mapping<>(String.class, NetworkAttributes::getSourceFormat, NetworkAttributes::setSourceFormat));
        networkMappings.addColumnMapping("connectedComponentsValid", new Mapping<>(Boolean.class, NetworkAttributes::isConnectedComponentsValid, NetworkAttributes::setConnectedComponentsValid));
        networkMappings.addColumnMapping("synchronousComponentsValid", new Mapping<>(Boolean.class, NetworkAttributes::isSynchronousComponentsValid, NetworkAttributes::setSynchronousComponentsValid));
        networkMappings.addColumnMapping("cgmesSvMetadata", new Mapping<>(CgmesSvMetadataAttributes.class, NetworkAttributes::getCgmesSvMetadata, NetworkAttributes::setCgmesSvMetadata));
        networkMappings.addColumnMapping("cgmesSshMetadata", new Mapping<>(CgmesSshMetadataAttributes.class, NetworkAttributes::getCgmesSshMetadata, NetworkAttributes::setCgmesSshMetadata));
        networkMappings.addColumnMapping("cimCharacteristics", new Mapping<>(CimCharacteristicsAttributes.class, NetworkAttributes::getCimCharacteristics, NetworkAttributes::setCimCharacteristics));
        networkMappings.addColumnMapping("cgmesControlAreas", new Mapping<>(CgmesControlAreasAttributes.class, NetworkAttributes::getCgmesControlAreas, NetworkAttributes::setCgmesControlAreas));
        networkMappings.addColumnMapping("baseVoltageMapping", new Mapping<>(BaseVoltageMappingAttributes.class, NetworkAttributes::getBaseVoltageMapping, NetworkAttributes::setBaseVoltageMapping));
    }

    public TableMapping getVoltageLevelMappings() {
        return voltageLevelMappings;
    }

    private void createVoltageLevelMappings() {
        voltageLevelMappings.addColumnMapping("substationId", new Mapping<>(String.class, VoltageLevelAttributes::getSubstationId, VoltageLevelAttributes::setSubstationId));
        voltageLevelMappings.addColumnMapping("name", new Mapping<>(String.class, VoltageLevelAttributes::getName, VoltageLevelAttributes::setName));
        voltageLevelMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, VoltageLevelAttributes::isFictitious, VoltageLevelAttributes::setFictitious));
        voltageLevelMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, VoltageLevelAttributes::getProperties, VoltageLevelAttributes::setProperties));
        voltageLevelMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, VoltageLevelAttributes::getAliasByType, VoltageLevelAttributes::setAliasByType));
        voltageLevelMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, VoltageLevelAttributes::getAliasesWithoutType, VoltageLevelAttributes::setAliasesWithoutType));
        voltageLevelMappings.addColumnMapping("nominalV", new Mapping<>(Double.class, VoltageLevelAttributes::getNominalV, VoltageLevelAttributes::setNominalV));
        voltageLevelMappings.addColumnMapping("lowVoltageLimit", new Mapping<>(Double.class, VoltageLevelAttributes::getLowVoltageLimit, VoltageLevelAttributes::setLowVoltageLimit));
        voltageLevelMappings.addColumnMapping("highVoltageLimit", new Mapping<>(Double.class, VoltageLevelAttributes::getHighVoltageLimit, VoltageLevelAttributes::setHighVoltageLimit));
        voltageLevelMappings.addColumnMapping("topologyKind", new Mapping<>(TopologyKind.class, VoltageLevelAttributes::getTopologyKind, VoltageLevelAttributes::setTopologyKind));
        voltageLevelMappings.addColumnMapping("internalConnections", new Mapping<>(List.class, VoltageLevelAttributes::getInternalConnections, VoltageLevelAttributes::setInternalConnections));
        voltageLevelMappings.addColumnMapping("calculatedBusesForBusView", new Mapping<>(List.class, VoltageLevelAttributes::getCalculatedBusesForBusView, VoltageLevelAttributes::setCalculatedBusesForBusView));
        voltageLevelMappings.addColumnMapping("nodeToCalculatedBusForBusView", new Mapping<>(null, VoltageLevelAttributes::getNodeToCalculatedBusForBusView, VoltageLevelAttributes::setNodeToCalculatedBusForBusView, Integer.class, Integer.class));
        voltageLevelMappings.addColumnMapping("busToCalculatedBusForBusView", new Mapping<>(null, VoltageLevelAttributes::getBusToCalculatedBusForBusView, VoltageLevelAttributes::setBusToCalculatedBusForBusView, String.class, Integer.class));
        voltageLevelMappings.addColumnMapping("calculatedBusesForBusBreakerView", new Mapping<>(List.class, VoltageLevelAttributes::getCalculatedBusesForBusBreakerView, VoltageLevelAttributes::setCalculatedBusesForBusBreakerView));
        voltageLevelMappings.addColumnMapping("nodeToCalculatedBusForBusBreakerView", new Mapping<>(null, VoltageLevelAttributes::getNodeToCalculatedBusForBusBreakerView, VoltageLevelAttributes::setNodeToCalculatedBusForBusBreakerView, Integer.class, Integer.class));
        voltageLevelMappings.addColumnMapping("busToCalculatedBusForBusBreakerView", new Mapping<>(null, VoltageLevelAttributes::getBusToCalculatedBusForBusBreakerView, VoltageLevelAttributes::setBusToCalculatedBusForBusBreakerView, String.class, Integer.class));
        voltageLevelMappings.addColumnMapping("slackTerminal", new Mapping<>(TerminalRefAttributes.class, VoltageLevelAttributes::getSlackTerminal, VoltageLevelAttributes::setSlackTerminal));
        voltageLevelMappings.addColumnMapping("calculatedBusesValid", new Mapping<>(Boolean.class, VoltageLevelAttributes::isCalculatedBusesValid, VoltageLevelAttributes::setCalculatedBusesValid));
    }

    public TableMapping getBatteryMappings() {
        return batteryMappings;
    }

    private void createBatteryMappings() {
        batteryMappings.addColumnMapping("name", new Mapping<>(String.class, BatteryAttributes::getName, BatteryAttributes::setName));
        batteryMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, BatteryAttributes::getVoltageLevelId, BatteryAttributes::setVoltageLevelId));
        batteryMappings.addColumnMapping("bus", new Mapping<>(String.class, BatteryAttributes::getBus, BatteryAttributes::setBus));
        batteryMappings.addColumnMapping(CONNECTABLE_BUS, new Mapping<>(String.class, BatteryAttributes::getConnectableBus, BatteryAttributes::setConnectableBus));
        batteryMappings.addColumnMapping("minP", new Mapping<>(Double.class, BatteryAttributes::getMinP, BatteryAttributes::setMinP));
        batteryMappings.addColumnMapping("maxP", new Mapping<>(Double.class, BatteryAttributes::getMaxP, BatteryAttributes::setMaxP));
        batteryMappings.addColumnMapping("targetP", new Mapping<>(Double.class, BatteryAttributes::getTargetP, BatteryAttributes::setTargetP));
        batteryMappings.addColumnMapping("targetQ", new Mapping<>(Double.class, BatteryAttributes::getTargetQ, BatteryAttributes::setTargetQ));
        batteryMappings.addColumnMapping("p", new Mapping<>(Double.class, BatteryAttributes::getP, BatteryAttributes::setP));
        batteryMappings.addColumnMapping("q", new Mapping<>(Double.class, BatteryAttributes::getQ, BatteryAttributes::setQ));
        batteryMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, BatteryAttributes::isFictitious, BatteryAttributes::setFictitious));
        batteryMappings.addColumnMapping(MIN_MAX_REACIVE_LIMITS, new Mapping<>(ReactiveLimitsAttributes.class, (BatteryAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (BatteryAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        batteryMappings.addColumnMapping(REACTIVE_CAPABILITY_CURVE, new Mapping<>(ReactiveLimitsAttributes.class, (BatteryAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (BatteryAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        batteryMappings.addColumnMapping("activePowerControl", new Mapping<>(ActivePowerControlAttributes.class, BatteryAttributes::getActivePowerControl, BatteryAttributes::setActivePowerControl));
        batteryMappings.addColumnMapping("node", new Mapping<>(Integer.class, BatteryAttributes::getNode, BatteryAttributes::setNode));
        batteryMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, BatteryAttributes::getProperties, BatteryAttributes::setProperties));
        batteryMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, BatteryAttributes::getAliasByType, BatteryAttributes::setAliasByType));
        batteryMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, BatteryAttributes::getAliasesWithoutType, BatteryAttributes::setAliasesWithoutType));
        batteryMappings.addColumnMapping(POSITION, new Mapping<>(ConnectablePositionAttributes.class, BatteryAttributes::getPosition, BatteryAttributes::setPosition));
    }

    public TableMapping getBusbarSectionMappings() {
        return busbarSectionMappings;
    }

    private void createBusbarSectionMappings() {
        busbarSectionMappings.addColumnMapping("name", new Mapping<>(String.class, BusbarSectionAttributes::getName, BusbarSectionAttributes::setName));
        busbarSectionMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, BusbarSectionAttributes::getVoltageLevelId, BusbarSectionAttributes::setVoltageLevelId));
        busbarSectionMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, BusbarSectionAttributes::isFictitious, BusbarSectionAttributes::setFictitious));
        busbarSectionMappings.addColumnMapping("node", new Mapping<>(Integer.class, BusbarSectionAttributes::getNode, BusbarSectionAttributes::setNode));
        busbarSectionMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, BusbarSectionAttributes::getProperties, BusbarSectionAttributes::setProperties));
        busbarSectionMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, BusbarSectionAttributes::getAliasByType, BusbarSectionAttributes::setAliasByType));
        busbarSectionMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, BusbarSectionAttributes::getAliasesWithoutType, BusbarSectionAttributes::setAliasesWithoutType));
        busbarSectionMappings.addColumnMapping(POSITION, new Mapping<>(BusbarSectionPositionAttributes.class, BusbarSectionAttributes::getPosition, BusbarSectionAttributes::setPosition));
    }

    public TableMapping getConfiguredBusMappings() {
        return configuredBusMappings;
    }

    private void createConfiguredBusMappings() {
        configuredBusMappings.addColumnMapping("name", new Mapping<>(String.class, ConfiguredBusAttributes::getName, ConfiguredBusAttributes::setName));
        configuredBusMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, ConfiguredBusAttributes::getVoltageLevelId, ConfiguredBusAttributes::setVoltageLevelId));
        configuredBusMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, ConfiguredBusAttributes::isFictitious, ConfiguredBusAttributes::setFictitious));
        configuredBusMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, ConfiguredBusAttributes::getProperties, ConfiguredBusAttributes::setProperties));
        configuredBusMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, ConfiguredBusAttributes::getAliasByType, ConfiguredBusAttributes::setAliasByType));
        configuredBusMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, ConfiguredBusAttributes::getAliasesWithoutType, ConfiguredBusAttributes::setAliasesWithoutType));
        configuredBusMappings.addColumnMapping("v", new Mapping<>(Double.class, ConfiguredBusAttributes::getV, ConfiguredBusAttributes::setV));
        configuredBusMappings.addColumnMapping("angle", new Mapping<>(Double.class, ConfiguredBusAttributes::getAngle, ConfiguredBusAttributes::setAngle));
    }

    public TableMapping getDanglingLineMappings() {
        return danglingLineMappings;
    }

    private void createDanglingLineMappings() {
        danglingLineMappings.addColumnMapping("name", new Mapping<>(String.class, DanglingLineAttributes::getName, DanglingLineAttributes::setName));
        danglingLineMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, DanglingLineAttributes::getVoltageLevelId, DanglingLineAttributes::setVoltageLevelId));
        danglingLineMappings.addColumnMapping("bus", new Mapping<>(String.class, DanglingLineAttributes::getBus, DanglingLineAttributes::setBus));
        danglingLineMappings.addColumnMapping(CONNECTABLE_BUS, new Mapping<>(String.class, DanglingLineAttributes::getConnectableBus, DanglingLineAttributes::setConnectableBus));
        danglingLineMappings.addColumnMapping("r", new Mapping<>(Double.class, DanglingLineAttributes::getR, DanglingLineAttributes::setR));
        danglingLineMappings.addColumnMapping("x", new Mapping<>(Double.class, DanglingLineAttributes::getX, DanglingLineAttributes::setX));
        danglingLineMappings.addColumnMapping("g", new Mapping<>(Double.class, DanglingLineAttributes::getG, DanglingLineAttributes::setG));
        danglingLineMappings.addColumnMapping("b", new Mapping<>(Double.class, DanglingLineAttributes::getB, DanglingLineAttributes::setB));
        danglingLineMappings.addColumnMapping("p0", new Mapping<>(Double.class, DanglingLineAttributes::getP0, DanglingLineAttributes::setP0));
        danglingLineMappings.addColumnMapping("q0", new Mapping<>(Double.class, DanglingLineAttributes::getQ0, DanglingLineAttributes::setQ0));
        danglingLineMappings.addColumnMapping("p", new Mapping<>(Double.class, DanglingLineAttributes::getP, DanglingLineAttributes::setP));
        danglingLineMappings.addColumnMapping("q", new Mapping<>(Double.class, DanglingLineAttributes::getQ, DanglingLineAttributes::setQ));
        danglingLineMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, DanglingLineAttributes::isFictitious, DanglingLineAttributes::setFictitious));
        danglingLineMappings.addColumnMapping("node", new Mapping<>(Integer.class, DanglingLineAttributes::getNode, DanglingLineAttributes::setNode));
        danglingLineMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, DanglingLineAttributes::getProperties, DanglingLineAttributes::setProperties));
        danglingLineMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, DanglingLineAttributes::getAliasByType, DanglingLineAttributes::setAliasByType));
        danglingLineMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, DanglingLineAttributes::getAliasesWithoutType, DanglingLineAttributes::setAliasesWithoutType));
        danglingLineMappings.addColumnMapping("generation", new Mapping<>(DanglingLineGenerationAttributes.class, DanglingLineAttributes::getGeneration, DanglingLineAttributes::setGeneration));
        danglingLineMappings.addColumnMapping("ucteXnodeCode", new Mapping<>(String.class, DanglingLineAttributes::getUcteXnodeCode, DanglingLineAttributes::setUcteXnodeCode));
        danglingLineMappings.addColumnMapping("currentLimits", new Mapping<>(LimitsAttributes.class, DanglingLineAttributes::getCurrentLimits, DanglingLineAttributes::setCurrentLimits));
        danglingLineMappings.addColumnMapping(POSITION, new Mapping<>(ConnectablePositionAttributes.class, DanglingLineAttributes::getPosition, DanglingLineAttributes::setPosition));
        danglingLineMappings.addColumnMapping("apparentPowerLimits", new Mapping<>(LimitsAttributes.class, DanglingLineAttributes::getApparentPowerLimits, DanglingLineAttributes::setApparentPowerLimits));
        danglingLineMappings.addColumnMapping("activePowerLimits", new Mapping<>(LimitsAttributes.class, DanglingLineAttributes::getActivePowerLimits, DanglingLineAttributes::setActivePowerLimits));
    }

    public TableMapping getShuntCompensatorMappings() {
        return shuntCompensatorMappings;
    }

    private void createShuntCompensatorMappings() {
        shuntCompensatorMappings.addColumnMapping("name", new Mapping<>(String.class, ShuntCompensatorAttributes::getName, ShuntCompensatorAttributes::setName));
        shuntCompensatorMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, ShuntCompensatorAttributes::getVoltageLevelId, ShuntCompensatorAttributes::setVoltageLevelId));
        shuntCompensatorMappings.addColumnMapping("bus", new Mapping<>(String.class, ShuntCompensatorAttributes::getBus, ShuntCompensatorAttributes::setBus));
        shuntCompensatorMappings.addColumnMapping(CONNECTABLE_BUS, new Mapping<>(String.class, ShuntCompensatorAttributes::getConnectableBus, ShuntCompensatorAttributes::setConnectableBus));
        shuntCompensatorMappings.addColumnMapping("p", new Mapping<>(Double.class, ShuntCompensatorAttributes::getP, ShuntCompensatorAttributes::setP));
        shuntCompensatorMappings.addColumnMapping("q", new Mapping<>(Double.class, ShuntCompensatorAttributes::getQ, ShuntCompensatorAttributes::setQ));
        shuntCompensatorMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, ShuntCompensatorAttributes::isFictitious, ShuntCompensatorAttributes::setFictitious));
        shuntCompensatorMappings.addColumnMapping(VOLTAGE_REGULATOR_ON, new Mapping<>(Boolean.class, ShuntCompensatorAttributes::isVoltageRegulatorOn, ShuntCompensatorAttributes::setVoltageRegulatorOn));
        shuntCompensatorMappings.addColumnMapping("targetV", new Mapping<>(Double.class, ShuntCompensatorAttributes::getTargetV, ShuntCompensatorAttributes::setTargetV));
        shuntCompensatorMappings.addColumnMapping("targetDeadband", new Mapping<>(Double.class, ShuntCompensatorAttributes::getTargetDeadband, ShuntCompensatorAttributes::setTargetDeadband));
        shuntCompensatorMappings.addColumnMapping(REGULATION_TERMINAL, new Mapping<>(TerminalRefAttributes.class, ShuntCompensatorAttributes::getRegulatingTerminal, ShuntCompensatorAttributes::setRegulatingTerminal));
        shuntCompensatorMappings.addColumnMapping("linearModel", new Mapping<>(ShuntCompensatorModelAttributes.class, (ShuntCompensatorAttributes attributes) ->
            attributes.getModel() instanceof ShuntCompensatorLinearModelAttributes ? attributes.getModel() : null,
            (ShuntCompensatorAttributes attributes, ShuntCompensatorModelAttributes model) -> {
                if (model instanceof ShuntCompensatorLinearModelAttributes) {
                    attributes.setModel(model);
                }
            }));
        shuntCompensatorMappings.addColumnMapping("nonLinearModel", new Mapping<>(ShuntCompensatorModelAttributes.class, (ShuntCompensatorAttributes attributes) ->
            attributes.getModel() instanceof ShuntCompensatorNonLinearModelAttributes ? attributes.getModel() : null,
            (ShuntCompensatorAttributes attributes, ShuntCompensatorModelAttributes model) -> {
                if (model instanceof ShuntCompensatorNonLinearModelAttributes) {
                    attributes.setModel(model);
                }
            }));
        shuntCompensatorMappings.addColumnMapping("node", new Mapping<>(Integer.class, ShuntCompensatorAttributes::getNode, ShuntCompensatorAttributes::setNode));
        shuntCompensatorMappings.addColumnMapping("sectionCount", new Mapping<>(Integer.class, ShuntCompensatorAttributes::getSectionCount, ShuntCompensatorAttributes::setSectionCount));
        shuntCompensatorMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, ShuntCompensatorAttributes::getProperties, ShuntCompensatorAttributes::setProperties));
        shuntCompensatorMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, ShuntCompensatorAttributes::getAliasByType, ShuntCompensatorAttributes::setAliasByType));
        shuntCompensatorMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, ShuntCompensatorAttributes::getAliasesWithoutType, ShuntCompensatorAttributes::setAliasesWithoutType));
        shuntCompensatorMappings.addColumnMapping(POSITION, new Mapping<>(ConnectablePositionAttributes.class, ShuntCompensatorAttributes::getPosition, ShuntCompensatorAttributes::setPosition));
    }

    public TableMapping getVscConverterStationMappings() {
        return vscConverterStationMappings;
    }

    private void createVscConverterStationMappings() {
        vscConverterStationMappings.addColumnMapping("name", new Mapping<>(String.class, VscConverterStationAttributes::getName, VscConverterStationAttributes::setName));
        vscConverterStationMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, VscConverterStationAttributes::getVoltageLevelId, VscConverterStationAttributes::setVoltageLevelId));
        vscConverterStationMappings.addColumnMapping("bus", new Mapping<>(String.class, VscConverterStationAttributes::getBus, VscConverterStationAttributes::setBus));
        vscConverterStationMappings.addColumnMapping(CONNECTABLE_BUS, new Mapping<>(String.class, VscConverterStationAttributes::getConnectableBus, VscConverterStationAttributes::setConnectableBus));
        vscConverterStationMappings.addColumnMapping(VOLTAGE_REGULATOR_ON, new Mapping<>(Boolean.class, VscConverterStationAttributes::getVoltageRegulatorOn, VscConverterStationAttributes::setVoltageRegulatorOn));
        vscConverterStationMappings.addColumnMapping("p", new Mapping<>(Double.class, VscConverterStationAttributes::getP, VscConverterStationAttributes::setP));
        vscConverterStationMappings.addColumnMapping("q", new Mapping<>(Double.class, VscConverterStationAttributes::getQ, VscConverterStationAttributes::setQ));
        vscConverterStationMappings.addColumnMapping("lossFactor", new Mapping<>(Float.class, VscConverterStationAttributes::getLossFactor, VscConverterStationAttributes::setLossFactor));
        vscConverterStationMappings.addColumnMapping("reactivePowerSetPoint", new Mapping<>(Double.class, VscConverterStationAttributes::getReactivePowerSetPoint, VscConverterStationAttributes::setReactivePowerSetPoint));
        vscConverterStationMappings.addColumnMapping("voltageSetPoint", new Mapping<>(Double.class, VscConverterStationAttributes::getVoltageSetPoint, VscConverterStationAttributes::setVoltageSetPoint));
        vscConverterStationMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, VscConverterStationAttributes::isFictitious, VscConverterStationAttributes::setFictitious));
        vscConverterStationMappings.addColumnMapping(MIN_MAX_REACIVE_LIMITS, new Mapping<>(ReactiveLimitsAttributes.class, (VscConverterStationAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (VscConverterStationAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        vscConverterStationMappings.addColumnMapping(REACTIVE_CAPABILITY_CURVE, new Mapping<>(ReactiveLimitsAttributes.class, (VscConverterStationAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (VscConverterStationAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        vscConverterStationMappings.addColumnMapping("node", new Mapping<>(Integer.class, VscConverterStationAttributes::getNode, VscConverterStationAttributes::setNode));
        vscConverterStationMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, VscConverterStationAttributes::getProperties, VscConverterStationAttributes::setProperties));
        vscConverterStationMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, VscConverterStationAttributes::getAliasByType, VscConverterStationAttributes::setAliasByType));
        vscConverterStationMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, VscConverterStationAttributes::getAliasesWithoutType, VscConverterStationAttributes::setAliasesWithoutType));
        vscConverterStationMappings.addColumnMapping(POSITION, new Mapping<>(ConnectablePositionAttributes.class, VscConverterStationAttributes::getPosition, VscConverterStationAttributes::setPosition));
    }

    public TableMapping getLccConverterStationMappings() {
        return lccConverterStationMappings;
    }

    private void createLccConverterStationMappings() {
        lccConverterStationMappings.addColumnMapping("name", new Mapping<>(String.class, LccConverterStationAttributes::getName, LccConverterStationAttributes::setName));
        lccConverterStationMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, LccConverterStationAttributes::getVoltageLevelId, LccConverterStationAttributes::setVoltageLevelId));
        lccConverterStationMappings.addColumnMapping("bus", new Mapping<>(String.class, LccConverterStationAttributes::getBus, LccConverterStationAttributes::setBus));
        lccConverterStationMappings.addColumnMapping(CONNECTABLE_BUS, new Mapping<>(String.class, LccConverterStationAttributes::getConnectableBus, LccConverterStationAttributes::setConnectableBus));
        lccConverterStationMappings.addColumnMapping("p", new Mapping<>(Double.class, LccConverterStationAttributes::getP, LccConverterStationAttributes::setP));
        lccConverterStationMappings.addColumnMapping("q", new Mapping<>(Double.class, LccConverterStationAttributes::getQ, LccConverterStationAttributes::setQ));
        lccConverterStationMappings.addColumnMapping("powerFactor", new Mapping<>(Float.class, LccConverterStationAttributes::getPowerFactor, LccConverterStationAttributes::setPowerFactor));
        lccConverterStationMappings.addColumnMapping("lossFactor", new Mapping<>(Float.class, LccConverterStationAttributes::getLossFactor, LccConverterStationAttributes::setLossFactor));
        lccConverterStationMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, LccConverterStationAttributes::isFictitious, LccConverterStationAttributes::setFictitious));
        lccConverterStationMappings.addColumnMapping("node", new Mapping<>(Integer.class, LccConverterStationAttributes::getNode, LccConverterStationAttributes::setNode));
        lccConverterStationMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, LccConverterStationAttributes::getProperties, LccConverterStationAttributes::setProperties));
        lccConverterStationMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, LccConverterStationAttributes::getAliasByType, LccConverterStationAttributes::setAliasByType));
        lccConverterStationMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, LccConverterStationAttributes::getAliasesWithoutType, LccConverterStationAttributes::setAliasesWithoutType));
        lccConverterStationMappings.addColumnMapping(POSITION, new Mapping<>(ConnectablePositionAttributes.class, LccConverterStationAttributes::getPosition, LccConverterStationAttributes::setPosition));
    }

    public TableMapping getStaticVarCompensatorMappings() {
        return staticVarCompensatorMappings;
    }

    private void createStaticVarCompensatorMappings() {
        staticVarCompensatorMappings.addColumnMapping("name", new Mapping<>(String.class, StaticVarCompensatorAttributes::getName, StaticVarCompensatorAttributes::setName));
        staticVarCompensatorMappings.addColumnMapping(VOLTAGE_LEVEL_ID, new Mapping<>(String.class, StaticVarCompensatorAttributes::getVoltageLevelId, StaticVarCompensatorAttributes::setVoltageLevelId));
        staticVarCompensatorMappings.addColumnMapping("bus", new Mapping<>(String.class, StaticVarCompensatorAttributes::getBus, StaticVarCompensatorAttributes::setBus));
        staticVarCompensatorMappings.addColumnMapping(CONNECTABLE_BUS, new Mapping<>(String.class, StaticVarCompensatorAttributes::getConnectableBus, StaticVarCompensatorAttributes::setConnectableBus));
        staticVarCompensatorMappings.addColumnMapping("bmin", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getBmin, StaticVarCompensatorAttributes::setBmin));
        staticVarCompensatorMappings.addColumnMapping("bmax", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getBmax, StaticVarCompensatorAttributes::setBmax));
        staticVarCompensatorMappings.addColumnMapping("voltageSetPoint", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getVoltageSetPoint, StaticVarCompensatorAttributes::setVoltageSetPoint));
        staticVarCompensatorMappings.addColumnMapping("reactivePowerSetPoint", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getReactivePowerSetPoint, StaticVarCompensatorAttributes::setReactivePowerSetPoint));
        staticVarCompensatorMappings.addColumnMapping("regulationMode", new Mapping<>(StaticVarCompensator.RegulationMode.class, StaticVarCompensatorAttributes::getRegulationMode, StaticVarCompensatorAttributes::setRegulationMode));
        staticVarCompensatorMappings.addColumnMapping("p", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getP, StaticVarCompensatorAttributes::setP));
        staticVarCompensatorMappings.addColumnMapping("q", new Mapping<>(Double.class, StaticVarCompensatorAttributes::getQ, StaticVarCompensatorAttributes::setQ));
        staticVarCompensatorMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, StaticVarCompensatorAttributes::isFictitious, StaticVarCompensatorAttributes::setFictitious));
        staticVarCompensatorMappings.addColumnMapping(REGULATION_TERMINAL, new Mapping<>(TerminalRefAttributes.class, StaticVarCompensatorAttributes::getRegulatingTerminal, StaticVarCompensatorAttributes::setRegulatingTerminal));
        staticVarCompensatorMappings.addColumnMapping("node", new Mapping<>(Integer.class, StaticVarCompensatorAttributes::getNode, StaticVarCompensatorAttributes::setNode));
        staticVarCompensatorMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, StaticVarCompensatorAttributes::getProperties, StaticVarCompensatorAttributes::setProperties));
        staticVarCompensatorMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, StaticVarCompensatorAttributes::getAliasByType, StaticVarCompensatorAttributes::setAliasByType));
        staticVarCompensatorMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, StaticVarCompensatorAttributes::getAliasesWithoutType, StaticVarCompensatorAttributes::setAliasesWithoutType));
        staticVarCompensatorMappings.addColumnMapping(POSITION, new Mapping<>(ConnectablePositionAttributes.class, StaticVarCompensatorAttributes::getPosition, StaticVarCompensatorAttributes::setPosition));
        staticVarCompensatorMappings.addColumnMapping("voltagePerReactivePowerControl", new Mapping<>(VoltagePerReactivePowerControlAttributes.class, StaticVarCompensatorAttributes::getVoltagePerReactiveControl, StaticVarCompensatorAttributes::setVoltagePerReactiveControl));
    }

    public TableMapping getHvdcLineMappings() {
        return hvdcLineMappings;
    }

    private void createHvdcLineMappings() {
        hvdcLineMappings.addColumnMapping("name", new Mapping<>(String.class, HvdcLineAttributes::getName, HvdcLineAttributes::setName));
        hvdcLineMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, HvdcLineAttributes::isFictitious, HvdcLineAttributes::setFictitious));
        hvdcLineMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, HvdcLineAttributes::getProperties, HvdcLineAttributes::setProperties));
        hvdcLineMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, HvdcLineAttributes::getAliasByType, HvdcLineAttributes::setAliasByType));
        hvdcLineMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, HvdcLineAttributes::getAliasesWithoutType, HvdcLineAttributes::setAliasesWithoutType));
        hvdcLineMappings.addColumnMapping("r", new Mapping<>(Double.class, HvdcLineAttributes::getR, HvdcLineAttributes::setR));
        hvdcLineMappings.addColumnMapping("nominalV", new Mapping<>(Double.class, HvdcLineAttributes::getNominalV, HvdcLineAttributes::setNominalV));
        hvdcLineMappings.addColumnMapping("activePowerSetpoint", new Mapping<>(Double.class, HvdcLineAttributes::getActivePowerSetpoint, HvdcLineAttributes::setActivePowerSetpoint));
        hvdcLineMappings.addColumnMapping("maxP", new Mapping<>(Double.class, HvdcLineAttributes::getMaxP, HvdcLineAttributes::setMaxP));
        hvdcLineMappings.addColumnMapping("convertersMode", new Mapping<>(HvdcLine.ConvertersMode.class, HvdcLineAttributes::getConvertersMode, HvdcLineAttributes::setConvertersMode));
        hvdcLineMappings.addColumnMapping("converterStationId1", new Mapping<>(String.class, HvdcLineAttributes::getConverterStationId1, HvdcLineAttributes::setConverterStationId1));
        hvdcLineMappings.addColumnMapping("converterStationId2", new Mapping<>(String.class, HvdcLineAttributes::getConverterStationId2, HvdcLineAttributes::setConverterStationId2));
        hvdcLineMappings.addColumnMapping("hvdcAngleDroopActivePowerControl", new Mapping<>(HvdcAngleDroopActivePowerControlAttributes.class, HvdcLineAttributes::getHvdcAngleDroopActivePowerControl, HvdcLineAttributes::setHvdcAngleDroopActivePowerControl));
        hvdcLineMappings.addColumnMapping("hvdcOperatorActivePowerRange", new Mapping<>(HvdcOperatorActivePowerRangeAttributes.class, HvdcLineAttributes::getHvdcOperatorActivePowerRange, HvdcLineAttributes::setHvdcOperatorActivePowerRange));
    }

    public TableMapping getTwoWindingsTransformerMappings() {
        return twoWindingsTransformerMappings;
    }

    private void createTwoWindingsTransformerMappings() {
        twoWindingsTransformerMappings.addColumnMapping("name", new Mapping<>(String.class, TwoWindingsTransformerAttributes::getName, TwoWindingsTransformerAttributes::setName));
        twoWindingsTransformerMappings.addColumnMapping(VOLTAGE_LEVEL_ID_1, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getVoltageLevelId1, TwoWindingsTransformerAttributes::setVoltageLevelId1));
        twoWindingsTransformerMappings.addColumnMapping(VOLTAGE_LEVEL_ID_2, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getVoltageLevelId2, TwoWindingsTransformerAttributes::setVoltageLevelId2));
        twoWindingsTransformerMappings.addColumnMapping("bus1", new Mapping<>(String.class, TwoWindingsTransformerAttributes::getBus1, TwoWindingsTransformerAttributes::setBus1));
        twoWindingsTransformerMappings.addColumnMapping("bus2", new Mapping<>(String.class, TwoWindingsTransformerAttributes::getBus2, TwoWindingsTransformerAttributes::setBus2));
        twoWindingsTransformerMappings.addColumnMapping(CONNECTABLE_BUS_1, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getConnectableBus1, TwoWindingsTransformerAttributes::setConnectableBus1));
        twoWindingsTransformerMappings.addColumnMapping(CONNECTABLE_BUS_2, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getConnectableBus2, TwoWindingsTransformerAttributes::setConnectableBus2));
        twoWindingsTransformerMappings.addColumnMapping(BRANCH_STATUS, new Mapping<>(String.class, TwoWindingsTransformerAttributes::getBranchStatus, TwoWindingsTransformerAttributes::setBranchStatus));
        twoWindingsTransformerMappings.addColumnMapping("r", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getR, TwoWindingsTransformerAttributes::setR));
        twoWindingsTransformerMappings.addColumnMapping("x", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getX, TwoWindingsTransformerAttributes::setX));
        twoWindingsTransformerMappings.addColumnMapping("g", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getG, TwoWindingsTransformerAttributes::setG));
        twoWindingsTransformerMappings.addColumnMapping("b", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getB, TwoWindingsTransformerAttributes::setB));
        twoWindingsTransformerMappings.addColumnMapping("ratedU1", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getRatedU1, TwoWindingsTransformerAttributes::setRatedU1));
        twoWindingsTransformerMappings.addColumnMapping("ratedU2", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getRatedU2, TwoWindingsTransformerAttributes::setRatedU2));
        twoWindingsTransformerMappings.addColumnMapping("ratedS", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getRatedS, TwoWindingsTransformerAttributes::setRatedS));
        twoWindingsTransformerMappings.addColumnMapping("p1", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getP1, TwoWindingsTransformerAttributes::setP1));
        twoWindingsTransformerMappings.addColumnMapping("q1", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getQ1, TwoWindingsTransformerAttributes::setQ1));
        twoWindingsTransformerMappings.addColumnMapping("p2", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getP2, TwoWindingsTransformerAttributes::setP2));
        twoWindingsTransformerMappings.addColumnMapping("q2", new Mapping<>(Double.class, TwoWindingsTransformerAttributes::getQ2, TwoWindingsTransformerAttributes::setQ2));
        twoWindingsTransformerMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, TwoWindingsTransformerAttributes::isFictitious, TwoWindingsTransformerAttributes::setFictitious));
        twoWindingsTransformerMappings.addColumnMapping(NODE_1, new Mapping<>(Integer.class, TwoWindingsTransformerAttributes::getNode1, TwoWindingsTransformerAttributes::setNode1));
        twoWindingsTransformerMappings.addColumnMapping(NODE_2, new Mapping<>(Integer.class, TwoWindingsTransformerAttributes::getNode2, TwoWindingsTransformerAttributes::setNode2));
        twoWindingsTransformerMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, TwoWindingsTransformerAttributes::getProperties, TwoWindingsTransformerAttributes::setProperties));
        twoWindingsTransformerMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, TwoWindingsTransformerAttributes::getAliasByType, TwoWindingsTransformerAttributes::setAliasByType));
        twoWindingsTransformerMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, TwoWindingsTransformerAttributes::getAliasesWithoutType, TwoWindingsTransformerAttributes::setAliasesWithoutType));
        twoWindingsTransformerMappings.addColumnMapping(POSITION_1, new Mapping<>(ConnectablePositionAttributes.class, TwoWindingsTransformerAttributes::getPosition1, TwoWindingsTransformerAttributes::setPosition1));
        twoWindingsTransformerMappings.addColumnMapping(POSITION_2, new Mapping<>(ConnectablePositionAttributes.class, TwoWindingsTransformerAttributes::getPosition2, TwoWindingsTransformerAttributes::setPosition2));
        twoWindingsTransformerMappings.addColumnMapping(CURRENT_LIMITS_1, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getCurrentLimits1, TwoWindingsTransformerAttributes::setCurrentLimits1));
        twoWindingsTransformerMappings.addColumnMapping(CURRENT_LIMITS_2, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getCurrentLimits2, TwoWindingsTransformerAttributes::setCurrentLimits2));
        twoWindingsTransformerMappings.addColumnMapping(APPARENT_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getApparentPowerLimits1, TwoWindingsTransformerAttributes::setApparentPowerLimits1));
        twoWindingsTransformerMappings.addColumnMapping(APPARENT_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getApparentPowerLimits2, TwoWindingsTransformerAttributes::setApparentPowerLimits2));
        twoWindingsTransformerMappings.addColumnMapping(ACTIVE_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getActivePowerLimits1, TwoWindingsTransformerAttributes::setActivePowerLimits1));
        twoWindingsTransformerMappings.addColumnMapping(ACTIVE_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class, TwoWindingsTransformerAttributes::getActivePowerLimits2, TwoWindingsTransformerAttributes::setActivePowerLimits2));
        twoWindingsTransformerMappings.addColumnMapping("cgmesTapChangers", new Mapping<>(List.class, TwoWindingsTransformerAttributes::getCgmesTapChangerAttributesList, TwoWindingsTransformerAttributes::setCgmesTapChangerAttributesList));
        twoWindingsTransformerMappings.addColumnMapping("phaseTapChanger", new Mapping<>(PhaseTapChangerAttributes.class, TwoWindingsTransformerAttributes::getPhaseTapChangerAttributes, TwoWindingsTransformerAttributes::setPhaseTapChangerAttributes));
        twoWindingsTransformerMappings.addColumnMapping("ratioTapChanger", new Mapping<>(RatioTapChangerAttributes.class, TwoWindingsTransformerAttributes::getRatioTapChangerAttributes, TwoWindingsTransformerAttributes::setRatioTapChangerAttributes));
        twoWindingsTransformerMappings.addColumnMapping("phaseAngleClock", new Mapping<>(TwoWindingsTransformerPhaseAngleClockAttributes.class, TwoWindingsTransformerAttributes::getPhaseAngleClockAttributes, TwoWindingsTransformerAttributes::setPhaseAngleClockAttributes));
    }

    public TableMapping getThreeWindingsTransformerMappings() {
        return threeWindingsTransformerMappings;
    }

    private void createThreeWindingsTransformerMappings() {
        threeWindingsTransformerMappings.addColumnMapping("name", new Mapping<>(String.class, ThreeWindingsTransformerAttributes::getName, ThreeWindingsTransformerAttributes::setName));
        threeWindingsTransformerMappings.addColumnMapping(BRANCH_STATUS, new Mapping<>(String.class, ThreeWindingsTransformerAttributes::getBranchStatus, ThreeWindingsTransformerAttributes::setBranchStatus));
        threeWindingsTransformerMappings.addColumnMapping("p1", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getP1, ThreeWindingsTransformerAttributes::setP1));
        threeWindingsTransformerMappings.addColumnMapping("q1", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getQ1, ThreeWindingsTransformerAttributes::setQ1));
        threeWindingsTransformerMappings.addColumnMapping("p2", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getP2, ThreeWindingsTransformerAttributes::setP2));
        threeWindingsTransformerMappings.addColumnMapping("q2", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getQ2, ThreeWindingsTransformerAttributes::setQ2));
        threeWindingsTransformerMappings.addColumnMapping("p3", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getP3, ThreeWindingsTransformerAttributes::setP3));
        threeWindingsTransformerMappings.addColumnMapping("q3", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getQ3, ThreeWindingsTransformerAttributes::setQ3));
        threeWindingsTransformerMappings.addColumnMapping("ratedU0", new Mapping<>(Double.class, ThreeWindingsTransformerAttributes::getRatedU0, ThreeWindingsTransformerAttributes::setRatedU0));
        threeWindingsTransformerMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, ThreeWindingsTransformerAttributes::isFictitious, ThreeWindingsTransformerAttributes::setFictitious));
        threeWindingsTransformerMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, ThreeWindingsTransformerAttributes::getProperties, ThreeWindingsTransformerAttributes::setProperties));
        threeWindingsTransformerMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, ThreeWindingsTransformerAttributes::getAliasByType, ThreeWindingsTransformerAttributes::setAliasByType));
        threeWindingsTransformerMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, ThreeWindingsTransformerAttributes::getAliasesWithoutType, ThreeWindingsTransformerAttributes::setAliasesWithoutType));
        threeWindingsTransformerMappings.addColumnMapping(POSITION_1, new Mapping<>(ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition1, ThreeWindingsTransformerAttributes::setPosition1));
        threeWindingsTransformerMappings.addColumnMapping(POSITION_2, new Mapping<>(ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition2, ThreeWindingsTransformerAttributes::setPosition2));
        threeWindingsTransformerMappings.addColumnMapping("position3", new Mapping<>(ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition3, ThreeWindingsTransformerAttributes::setPosition3));
        threeWindingsTransformerMappings.addColumnMapping("cgmesTapChangers", new Mapping<>(List.class, ThreeWindingsTransformerAttributes::getCgmesTapChangerAttributesList, ThreeWindingsTransformerAttributes::setCgmesTapChangerAttributesList));
        threeWindingsTransformerMappings.addColumnMapping("phaseAngleClock", new Mapping<>(ThreeWindingsTransformerPhaseAngleClockAttributes.class, ThreeWindingsTransformerAttributes::getPhaseAngleClock, ThreeWindingsTransformerAttributes::setPhaseAngleClock));
        threeWindingsTransformerMappings.addColumnMapping(VOLTAGE_LEVEL_ID_1, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg1().setVoltageLevelId(vId)));
        threeWindingsTransformerMappings.addColumnMapping(VOLTAGE_LEVEL_ID_2, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg2().setVoltageLevelId(vId)));
        threeWindingsTransformerMappings.addColumnMapping(VOLTAGE_LEVEL_ID_3, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg3().setVoltageLevelId(vId)));
        threeWindingsTransformerMappings.addColumnMapping(NODE_1, new Mapping<>(Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg1().setNode(node)));
        threeWindingsTransformerMappings.addColumnMapping(NODE_2, new Mapping<>(Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg2().setNode(node)));
        threeWindingsTransformerMappings.addColumnMapping("node3", new Mapping<>(Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg3().setNode(node)));
        threeWindingsTransformerMappings.addColumnMapping("bus1", new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg1().setBus(bus)));
        threeWindingsTransformerMappings.addColumnMapping("bus2", new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg2().setBus(bus)));
        threeWindingsTransformerMappings.addColumnMapping("bus3", new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg3().setBus(bus)));
        threeWindingsTransformerMappings.addColumnMapping(CONNECTABLE_BUS_1, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg1().setConnectableBus(bus)));
        threeWindingsTransformerMappings.addColumnMapping(CONNECTABLE_BUS_2, new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg2().setConnectableBus(bus)));
        threeWindingsTransformerMappings.addColumnMapping("connectableBus3", new Mapping<>(String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg3().setConnectableBus(bus)));
        threeWindingsTransformerMappings.addColumnMapping("r1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg1().setR(r)));
        threeWindingsTransformerMappings.addColumnMapping("r2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg2().setR(r)));
        threeWindingsTransformerMappings.addColumnMapping("r3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg3().setR(r)));
        threeWindingsTransformerMappings.addColumnMapping("x1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg1().setX(x)));
        threeWindingsTransformerMappings.addColumnMapping("x2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg2().setX(x)));
        threeWindingsTransformerMappings.addColumnMapping("x3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg3().setX(x)));
        threeWindingsTransformerMappings.addColumnMapping("g1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg1().setG(g)));
        threeWindingsTransformerMappings.addColumnMapping("g2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg2().setG(g)));
        threeWindingsTransformerMappings.addColumnMapping("g3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg3().setG(g)));
        threeWindingsTransformerMappings.addColumnMapping("b1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg1().setB(b)));
        threeWindingsTransformerMappings.addColumnMapping("b2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg2().setB(b)));
        threeWindingsTransformerMappings.addColumnMapping("b3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg3().setB(b)));
        threeWindingsTransformerMappings.addColumnMapping("ratedU1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg1().setRatedU(ratedU)));
        threeWindingsTransformerMappings.addColumnMapping("ratedU2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg2().setRatedU(ratedU)));
        threeWindingsTransformerMappings.addColumnMapping("ratedU3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg3().setRatedU(ratedU)));
        threeWindingsTransformerMappings.addColumnMapping("ratedS1", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg1().setRatedS(ratedS)));
        threeWindingsTransformerMappings.addColumnMapping("ratedS2", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg2().setRatedS(ratedS)));
        threeWindingsTransformerMappings.addColumnMapping("ratedS3", new Mapping<>(Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg3().setRatedS(ratedS)));
        threeWindingsTransformerMappings.addColumnMapping("phaseTapChanger1", new Mapping<>(PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg1().setPhaseTapChangerAttributes(phaseTapChanger)));
        threeWindingsTransformerMappings.addColumnMapping("phaseTapChanger2", new Mapping<>(PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg2().setPhaseTapChangerAttributes(phaseTapChanger)));
        threeWindingsTransformerMappings.addColumnMapping("phaseTapChanger3", new Mapping<>(PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg3().setPhaseTapChangerAttributes(phaseTapChanger)));
        threeWindingsTransformerMappings.addColumnMapping("ratioTapChanger1", new Mapping<>(RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg1().setRatioTapChangerAttributes(ratioTapChanger)));
        threeWindingsTransformerMappings.addColumnMapping("ratioTapChanger2", new Mapping<>(RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg2().setRatioTapChangerAttributes(ratioTapChanger)));
        threeWindingsTransformerMappings.addColumnMapping("ratioTapChanger3", new Mapping<>(RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg3().setRatioTapChangerAttributes(ratioTapChanger)));
        threeWindingsTransformerMappings.addColumnMapping(CURRENT_LIMITS_1, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setCurrentLimitsAttributes(limits)));
        threeWindingsTransformerMappings.addColumnMapping(CURRENT_LIMITS_2, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setCurrentLimitsAttributes(limits)));
        threeWindingsTransformerMappings.addColumnMapping("currentLimits3", new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setCurrentLimitsAttributes(limits)));
        threeWindingsTransformerMappings.addColumnMapping(APPARENT_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setApparentPowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.addColumnMapping(APPARENT_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setApparentPowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.addColumnMapping("apparentPowerLimits3", new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setApparentPowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.addColumnMapping(ACTIVE_POWER_LIMITS_1, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setActivePowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.addColumnMapping(ACTIVE_POWER_LIMITS_2, new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setActivePowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.addColumnMapping("activePowerLimits3", new Mapping<>(LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setActivePowerLimitsAttributes(limits)));
    }

    public TableMapping getTemporaryLimitMappings() {
        return temporaryLimitMappings;
    }

    private void createTemporaryLimitMappings() {
        temporaryLimitMappings.addColumnMapping("name", new Mapping<>(String.class, TemporaryLimitAttributes::getName, TemporaryLimitAttributes::setName));
        temporaryLimitMappings.addColumnMapping(FICTITIOUS, new Mapping<>(Boolean.class, TemporaryLimitAttributes::isFictitious, TemporaryLimitAttributes::setFictitious));
        temporaryLimitMappings.addColumnMapping(PROPERTIES, new Mapping<>(Map.class, TemporaryLimitAttributes::getProperties, TemporaryLimitAttributes::setProperties));
        temporaryLimitMappings.addColumnMapping(ALIASES_WITHOUT_TYPE, new Mapping<>(Set.class, TemporaryLimitAttributes::getAliasesWithoutType, TemporaryLimitAttributes::setAliasesWithoutType));
        temporaryLimitMappings.addColumnMapping(ALIAS_BY_TYPE, new Mapping<>(Map.class, TemporaryLimitAttributes::getAliasByType, TemporaryLimitAttributes::setAliasByType));
        temporaryLimitMappings.addColumnMapping("side", new Mapping<>(Integer.class, TemporaryLimitAttributes::getSide, TemporaryLimitAttributes::setSide));
        temporaryLimitMappings.addColumnMapping("limitType", new Mapping<>(String.class, TemporaryLimitAttributes::getLimitType, TemporaryLimitAttributes::setLimitType));
        temporaryLimitMappings.addColumnMapping("index", new Mapping<>(Integer.class, TemporaryLimitAttributes::getIndex, TemporaryLimitAttributes::setIndex));
        temporaryLimitMappings.addColumnMapping("value", new Mapping<>(Double.class, TemporaryLimitAttributes::getValue, TemporaryLimitAttributes::setValue));
        temporaryLimitMappings.addColumnMapping("acceptableDuration", new Mapping<>(Integer.class, TemporaryLimitAttributes::getAcceptableDuration, TemporaryLimitAttributes::setAcceptableDuration));
    }

    public Mappings() {
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
        createTemporaryLimitMappings();
        for (TableMapping tableMapping : all) {
            mappingByTable.put(tableMapping.getTable().toLowerCase(), tableMapping);
        }
    }
}
