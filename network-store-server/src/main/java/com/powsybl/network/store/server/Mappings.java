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
import com.powsybl.network.store.model.CalculatedBusAttributes;
import com.powsybl.network.store.model.CgmesControlAreasAttributes;
import com.powsybl.network.store.model.CgmesIidmMappingAttributes;
import com.powsybl.network.store.model.CgmesSshMetadataAttributes;
import com.powsybl.network.store.model.CgmesSvMetadataAttributes;
import com.powsybl.network.store.model.CgmesTapChangerAttributes;
import com.powsybl.network.store.model.CimCharacteristicsAttributes;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.ConnectablePositionAttributes;
import com.powsybl.network.store.model.CoordinatedReactiveControlAttributes;
import com.powsybl.network.store.model.DanglingLineAttributes;
import com.powsybl.network.store.model.DanglingLineGenerationAttributes;
import com.powsybl.network.store.model.EntsoeAreaAttributes;
import com.powsybl.network.store.model.GeneratorAttributes;
import com.powsybl.network.store.model.HvdcAngleDroopActivePowerControlAttributes;
import com.powsybl.network.store.model.HvdcLineAttributes;
import com.powsybl.network.store.model.HvdcOperatorActivePowerRangeAttributes;
import com.powsybl.network.store.model.InternalConnectionAttributes;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class Mappings {
    Map<String, Mapping> lineMappings = new TreeMap<>();
    Map<String, Mapping> loadMappings = new TreeMap<>();
    Map<String, Mapping> generatorMappings = new TreeMap<>();
    Map<String, Mapping> switchMappings = new TreeMap<>();
    Map<String, Mapping> substationMappings = new TreeMap<>();
    Map<String, Mapping> networkMappings = new TreeMap<>();
    Map<String, Mapping> voltageLevelMappings = new TreeMap<>();
    Map<String, Mapping> batteryMappings = new TreeMap<>();
    Map<String, Mapping> busbarSectionMappings = new TreeMap<>();
    Map<String, Mapping> configuredBusMappings = new TreeMap<>();
    Map<String, Mapping> danglingLineMappings = new TreeMap<>();
    Map<String, Mapping> shuntCompensatorMappings = new TreeMap<>();
    Map<String, Mapping> vscConverterStationMappings = new TreeMap<>();
    Map<String, Mapping> lccConverterStationMappings = new TreeMap<>();
    Map<String, Mapping> staticVarCompensatorMappings = new TreeMap<>();
    Map<String, Mapping> hvdcLineMappings = new TreeMap<>();
    Map<String, Mapping> twoWindingsTransformerMappings = new TreeMap<>();
    Map<String, Mapping> threeWindingsTransformerMappings = new TreeMap<>();

    Map<String, String> mapStrStr = new HashMap<>();
    Set<String> setStr = new HashSet<>();
    List<InternalConnectionAttributes> internalConnections = new ArrayList<>();
    List<CalculatedBusAttributes> calculatedBuses = new ArrayList<>();
    Map<Integer, Integer> mapIntInt = new HashMap<>();
    Map<String, Integer> mapStrInt = new HashMap<>();
    List<CgmesTapChangerAttributes> cgmesTapChangers = new ArrayList<>();

    public Map<String, Mapping> getLineMappings() {
        return lineMappings;
    }

    private void createLineMappings() {
        lineMappings.put("name", new Mapping<>(1, String.class, LineAttributes::getName, LineAttributes::setName));
        lineMappings.put("voltageLevelId1", new Mapping<>(2, String.class, LineAttributes::getVoltageLevelId1, LineAttributes::setVoltageLevelId1));
        lineMappings.put("voltageLevelId2", new Mapping<>(3, String.class, LineAttributes::getVoltageLevelId2, LineAttributes::setVoltageLevelId2));
        lineMappings.put("bus1", new Mapping<>(4, String.class, LineAttributes::getBus1, LineAttributes::setBus1));
        lineMappings.put("bus2", new Mapping<>(5, String.class, LineAttributes::getBus2, LineAttributes::setBus2));
        lineMappings.put("connectableBus1", new Mapping<>(6, String.class, LineAttributes::getConnectableBus1, LineAttributes::setConnectableBus1));
        lineMappings.put("connectableBus2", new Mapping<>(7, String.class, LineAttributes::getConnectableBus2, LineAttributes::setConnectableBus2));
        lineMappings.put("branchStatus", new Mapping<>(8, String.class, LineAttributes::getBranchStatus, LineAttributes::setBranchStatus));
        lineMappings.put("r", new Mapping<>(9, Double.class, LineAttributes::getR, LineAttributes::setR));
        lineMappings.put("x", new Mapping<>(10, Double.class, LineAttributes::getX, LineAttributes::setX));
        lineMappings.put("g1", new Mapping<>(11, Double.class, LineAttributes::getG1, LineAttributes::setG1));
        lineMappings.put("b1", new Mapping<>(12, Double.class, LineAttributes::getB1, LineAttributes::setB1));
        lineMappings.put("g2", new Mapping<>(13, Double.class, LineAttributes::getG2, LineAttributes::setG2));
        lineMappings.put("b2", new Mapping<>(14, Double.class, LineAttributes::getB2, LineAttributes::setB2));
        lineMappings.put("p1", new Mapping<>(15, Double.class, LineAttributes::getP1, LineAttributes::setP1));
        lineMappings.put("q1", new Mapping<>(16, Double.class, LineAttributes::getQ1, LineAttributes::setQ1));
        lineMappings.put("p2", new Mapping<>(17, Double.class, LineAttributes::getP2, LineAttributes::setP2));
        lineMappings.put("q2", new Mapping<>(18, Double.class, LineAttributes::getQ2, LineAttributes::setQ2));
        lineMappings.put("fictitious", new Mapping<>(19, Boolean.class, LineAttributes::isFictitious, LineAttributes::setFictitious));
        lineMappings.put("node1", new Mapping<>(20, Integer.class, LineAttributes::getNode1, LineAttributes::setNode1));
        lineMappings.put("node2", new Mapping<>(21, Integer.class, LineAttributes::getNode2, LineAttributes::setNode2));
        lineMappings.put("properties", new Mapping<>(22, (Class<Map<String, String>>) mapStrStr.getClass(), LineAttributes::getProperties, LineAttributes::setProperties));
        lineMappings.put("aliasByType", new Mapping<>(23, (Class<Map<String, String>>) mapStrStr.getClass(), LineAttributes::getAliasByType, LineAttributes::setAliasByType));
        lineMappings.put("aliasesWithoutType", new Mapping<>(24, (Class<Set<String>>) setStr.getClass(), LineAttributes::getAliasesWithoutType, LineAttributes::setAliasesWithoutType));
        lineMappings.put("position1", new Mapping<>(25, ConnectablePositionAttributes.class, LineAttributes::getPosition1, LineAttributes::setPosition1));
        lineMappings.put("position2", new Mapping<>(26, ConnectablePositionAttributes.class, LineAttributes::getPosition2, LineAttributes::setPosition2));
        lineMappings.put("mergedXnode", new Mapping<>(27, MergedXnodeAttributes.class, LineAttributes::getMergedXnode, LineAttributes::setMergedXnode));
        lineMappings.put("currentLimits1", new Mapping<>(28, LimitsAttributes.class, LineAttributes::getCurrentLimits1, LineAttributes::setCurrentLimits1));
        lineMappings.put("currentLimits2", new Mapping<>(29, LimitsAttributes.class, LineAttributes::getCurrentLimits2, LineAttributes::setCurrentLimits2));
        lineMappings.put("apparentPowerLimits1", new Mapping<>(30, LimitsAttributes.class, LineAttributes::getApparentPowerLimits1, LineAttributes::setApparentPowerLimits1));
        lineMappings.put("apparentPowerLimits2", new Mapping<>(31, LimitsAttributes.class, LineAttributes::getApparentPowerLimits2, LineAttributes::setApparentPowerLimits2));
        lineMappings.put("activePowerLimits1", new Mapping<>(32, LimitsAttributes.class, LineAttributes::getActivePowerLimits1, LineAttributes::setActivePowerLimits1));
        lineMappings.put("activePowerLimits2", new Mapping<>(33, LimitsAttributes.class, LineAttributes::getActivePowerLimits2, LineAttributes::setActivePowerLimits2));
    }

    public Map<String, Mapping> getLoadMappings() {
        return loadMappings;
    }

    private void createLoadMappings() {
        loadMappings.put("name", new Mapping<>(1, String.class, LoadAttributes::getName, LoadAttributes::setName));
        loadMappings.put("voltageLevelId", new Mapping<>(2, String.class, LoadAttributes::getVoltageLevelId, LoadAttributes::setVoltageLevelId));
        loadMappings.put("bus", new Mapping<>(3, String.class, LoadAttributes::getBus, LoadAttributes::setBus));
        loadMappings.put("connectableBus", new Mapping<>(4, String.class, LoadAttributes::getConnectableBus, LoadAttributes::setConnectableBus));
        loadMappings.put("p0", new Mapping<>(5, Double.class, LoadAttributes::getP0, LoadAttributes::setP0));
        loadMappings.put("q0", new Mapping<>(6, Double.class, LoadAttributes::getQ0, LoadAttributes::setQ0));
        loadMappings.put("loadType", new Mapping<>(7, LoadType.class, LoadAttributes::getLoadType, LoadAttributes::setLoadType));
        loadMappings.put("p", new Mapping<>(8, Double.class, LoadAttributes::getP, LoadAttributes::setP));
        loadMappings.put("q", new Mapping<>(9, Double.class, LoadAttributes::getQ, LoadAttributes::setQ));
        loadMappings.put("fictitious", new Mapping<>(10, Boolean.class, LoadAttributes::isFictitious, LoadAttributes::setFictitious));
        loadMappings.put("node", new Mapping<>(11, Integer.class, LoadAttributes::getNode, LoadAttributes::setNode));
        loadMappings.put("properties", new Mapping<>(12, (Class<Map<String, String>>) mapStrStr.getClass(), LoadAttributes::getProperties, LoadAttributes::setProperties));
        loadMappings.put("aliasByType", new Mapping<>(13, (Class<Map<String, String>>) mapStrStr.getClass(), LoadAttributes::getAliasByType, LoadAttributes::setAliasByType));
        loadMappings.put("aliasesWithoutType", new Mapping<>(14, (Class<Set<String>>) setStr.getClass(), LoadAttributes::getAliasesWithoutType, LoadAttributes::setAliasesWithoutType));
        loadMappings.put("position", new Mapping<>(15, ConnectablePositionAttributes.class, LoadAttributes::getPosition, LoadAttributes::setPosition));
        loadMappings.put("loadDetail", new Mapping<>(16, LoadDetailAttributes.class, LoadAttributes::getLoadDetail, LoadAttributes::setLoadDetail));
    }

    public Map<String, Mapping> getGeneratorMappings() {
        return generatorMappings;
    }

    private void createGeneratorMappings() {
        generatorMappings.put("name", new Mapping<>(1, String.class, GeneratorAttributes::getName, GeneratorAttributes::setName));
        generatorMappings.put("voltageLevelId", new Mapping<>(2, String.class, GeneratorAttributes::getVoltageLevelId, GeneratorAttributes::setVoltageLevelId));
        generatorMappings.put("bus", new Mapping<>(3, String.class, GeneratorAttributes::getBus, GeneratorAttributes::setBus));
        generatorMappings.put("connectableBus", new Mapping<>(4, String.class, GeneratorAttributes::getConnectableBus, GeneratorAttributes::setConnectableBus));
        generatorMappings.put("minP", new Mapping<>(5, Double.class, GeneratorAttributes::getMinP, GeneratorAttributes::setMinP));
        generatorMappings.put("maxP", new Mapping<>(6, Double.class, GeneratorAttributes::getMaxP, GeneratorAttributes::setMaxP));
        generatorMappings.put("energySource", new Mapping<>(7, EnergySource.class, GeneratorAttributes::getEnergySource, GeneratorAttributes::setEnergySource));
        generatorMappings.put("p", new Mapping<>(8, Double.class, GeneratorAttributes::getP, GeneratorAttributes::setP));
        generatorMappings.put("q", new Mapping<>(9, Double.class, GeneratorAttributes::getQ, GeneratorAttributes::setQ));
        generatorMappings.put("fictitious", new Mapping<>(10, Boolean.class, GeneratorAttributes::isFictitious, GeneratorAttributes::setFictitious));
        generatorMappings.put("voltageRegulatorOn", new Mapping<>(11, Boolean.class, GeneratorAttributes::isVoltageRegulatorOn, GeneratorAttributes::setVoltageRegulatorOn));
        generatorMappings.put("targetP", new Mapping<>(12, Double.class, GeneratorAttributes::getTargetP, GeneratorAttributes::setTargetP));
        generatorMappings.put("targetQ", new Mapping<>(13, Double.class, GeneratorAttributes::getTargetQ, GeneratorAttributes::setTargetQ));
        generatorMappings.put("targetV", new Mapping<>(14, Double.class, GeneratorAttributes::getTargetV, GeneratorAttributes::setTargetV));
        generatorMappings.put("ratedS", new Mapping<>(15, Double.class, GeneratorAttributes::getRatedS, GeneratorAttributes::setRatedS));
        generatorMappings.put("minMaxReactiveLimits", new Mapping<>(16, ReactiveLimitsAttributes.class, (GeneratorAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (GeneratorAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        generatorMappings.put("reactiveCapabilityCurve", new Mapping<>(17, ReactiveLimitsAttributes.class, (GeneratorAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (GeneratorAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        generatorMappings.put("activePowerControl", new Mapping<>(18, ActivePowerControlAttributes.class, GeneratorAttributes::getActivePowerControl, GeneratorAttributes::setActivePowerControl));
        generatorMappings.put("regulatingTerminal", new Mapping<>(19, TerminalRefAttributes.class, GeneratorAttributes::getRegulatingTerminal, GeneratorAttributes::setRegulatingTerminal));
        generatorMappings.put("coordinatedReactiveControl", new Mapping<>(20, CoordinatedReactiveControlAttributes.class, GeneratorAttributes::getCoordinatedReactiveControl, GeneratorAttributes::setCoordinatedReactiveControl));
        generatorMappings.put("remoteReactivePowerControl", new Mapping<>(21, RemoteReactivePowerControlAttributes.class, GeneratorAttributes::getRemoteReactivePowerControl, GeneratorAttributes::setRemoteReactivePowerControl));
        generatorMappings.put("node", new Mapping<>(22, Integer.class, GeneratorAttributes::getNode, GeneratorAttributes::setNode));
        generatorMappings.put("properties", new Mapping<>(23, (Class<Map<String, String>>) mapStrStr.getClass(), GeneratorAttributes::getProperties, GeneratorAttributes::setProperties));
        generatorMappings.put("aliasByType", new Mapping<>(24, (Class<Map<String, String>>) mapStrStr.getClass(), GeneratorAttributes::getAliasByType, GeneratorAttributes::setAliasByType));
        generatorMappings.put("aliasesWithoutType", new Mapping<>(25, (Class<Set<String>>) setStr.getClass(), GeneratorAttributes::getAliasesWithoutType, GeneratorAttributes::setAliasesWithoutType));
        generatorMappings.put("position", new Mapping<>(26, ConnectablePositionAttributes.class, GeneratorAttributes::getPosition, GeneratorAttributes::setPosition));
    }

    public Map<String, Mapping> getSwitchMappings() {
        return switchMappings;
    }

    private void createSwitchMappings() {
        switchMappings.put("name", new Mapping<>(1, String.class, SwitchAttributes::getName, SwitchAttributes::setName));
        switchMappings.put("voltageLevelId", new Mapping<>(2, String.class, SwitchAttributes::getVoltageLevelId, SwitchAttributes::setVoltageLevelId));
        switchMappings.put("bus1", new Mapping<>(3, String.class, SwitchAttributes::getBus1, SwitchAttributes::setBus1));
        switchMappings.put("bus2", new Mapping<>(4, String.class, SwitchAttributes::getBus2, SwitchAttributes::setBus2));
        switchMappings.put("kind", new Mapping<>(5, SwitchKind.class, SwitchAttributes::getKind, SwitchAttributes::setKind));
        switchMappings.put("open", new Mapping<>(6, Boolean.class, SwitchAttributes::isOpen, SwitchAttributes::setOpen));
        switchMappings.put("retained", new Mapping<>(7, Boolean.class, SwitchAttributes::isRetained, SwitchAttributes::setRetained));
        switchMappings.put("fictitious", new Mapping<>(8, Boolean.class, SwitchAttributes::isFictitious, SwitchAttributes::setFictitious));
        switchMappings.put("node1", new Mapping<>(9, Integer.class, SwitchAttributes::getNode1, SwitchAttributes::setNode1));
        switchMappings.put("node2", new Mapping<>(10, Integer.class, SwitchAttributes::getNode2, SwitchAttributes::setNode2));
        switchMappings.put("properties", new Mapping<>(11, (Class<Map<String, String>>) mapStrStr.getClass(), SwitchAttributes::getProperties, SwitchAttributes::setProperties));
        switchMappings.put("aliasByType", new Mapping<>(12, (Class<Map<String, String>>) mapStrStr.getClass(), SwitchAttributes::getAliasByType, SwitchAttributes::setAliasByType));
        switchMappings.put("aliasesWithoutType", new Mapping<>(13, (Class<Set<String>>) setStr.getClass(), SwitchAttributes::getAliasesWithoutType, SwitchAttributes::setAliasesWithoutType));
    }

    public Map<String, Mapping> getSubstationMappings() {
        return substationMappings;
    }

    private void createSubstationMappings() {
        substationMappings.put("name", new Mapping<>(1, String.class, SubstationAttributes::getName, SubstationAttributes::setName));
        substationMappings.put("fictitious", new Mapping<>(2, Boolean.class, SubstationAttributes::isFictitious, SubstationAttributes::setFictitious));
        substationMappings.put("properties", new Mapping<>(3, (Class<Map<String, String>>) mapStrStr.getClass(), SubstationAttributes::getProperties, SubstationAttributes::setProperties));
        substationMappings.put("aliasByType", new Mapping<>(4, (Class<Map<String, String>>) mapStrStr.getClass(), SubstationAttributes::getAliasByType, SubstationAttributes::setAliasByType));
        substationMappings.put("aliasesWithoutType", new Mapping<>(5, (Class<Set<String>>) setStr.getClass(), SubstationAttributes::getAliasesWithoutType, SubstationAttributes::setAliasesWithoutType));
        substationMappings.put("country", new Mapping<>(6, Country.class, SubstationAttributes::getCountry, SubstationAttributes::setCountry));
        substationMappings.put("tso", new Mapping<>(7, String.class, SubstationAttributes::getTso, SubstationAttributes::setTso));
        substationMappings.put("geographicalTags", new Mapping<>(8, (Class<Set<String>>) setStr.getClass(), SubstationAttributes::getGeographicalTags, SubstationAttributes::setGeographicalTags));
        substationMappings.put("entsoeArea", new Mapping<>(9, EntsoeAreaAttributes.class, SubstationAttributes::getEntsoeArea, SubstationAttributes::setEntsoeArea));
    }

    public Map<String, Mapping> getNetworkMappings() {
        return networkMappings;
    }

    private void createNetworkMappings() {
        networkMappings.put("uuid", new Mapping<>(1, UUID.class, NetworkAttributes::getUuid, NetworkAttributes::setUuid));
        networkMappings.put("variantId", new Mapping<>(2, String.class, NetworkAttributes::getVariantId, NetworkAttributes::setVariantId));
        networkMappings.put("name", new Mapping<>(3, String.class, NetworkAttributes::getName, NetworkAttributes::setName));
        networkMappings.put("fictitious", new Mapping<>(4, Boolean.class, NetworkAttributes::isFictitious, NetworkAttributes::setFictitious));
        networkMappings.put("properties", new Mapping<>(5, (Class<Map<String, String>>) mapStrStr.getClass(), NetworkAttributes::getProperties, NetworkAttributes::setProperties));
        networkMappings.put("aliasByType", new Mapping<>(6, (Class<Map<String, String>>) mapStrStr.getClass(), NetworkAttributes::getAliasByType, NetworkAttributes::setAliasByType));
        networkMappings.put("aliasesWithoutType", new Mapping<>(7, (Class<Set<String>>) setStr.getClass(), NetworkAttributes::getAliasesWithoutType, NetworkAttributes::setAliasesWithoutType));
        networkMappings.put("idByAlias", new Mapping<>(8, (Class<Map<String, String>>) mapStrStr.getClass(), NetworkAttributes::getIdByAlias, NetworkAttributes::setIdByAlias));

        networkMappings.put("caseDate", new Mapping<>(9, DateTime.class, NetworkAttributes::getCaseDate, NetworkAttributes::setCaseDate));

        networkMappings.put("forecastDistance", new Mapping<>(10, Integer.class, NetworkAttributes::getForecastDistance, NetworkAttributes::setForecastDistance));
        networkMappings.put("sourceFormat", new Mapping<>(11, String.class, NetworkAttributes::getSourceFormat, NetworkAttributes::setSourceFormat));
        networkMappings.put("connectedComponentsValid", new Mapping<>(12, Boolean.class, NetworkAttributes::isConnectedComponentsValid, NetworkAttributes::setConnectedComponentsValid));
        networkMappings.put("synchronousComponentsValid", new Mapping<>(13, Boolean.class, NetworkAttributes::isSynchronousComponentsValid, NetworkAttributes::setSynchronousComponentsValid));
        networkMappings.put("cgmesSvMetadata", new Mapping<>(14, CgmesSvMetadataAttributes.class, NetworkAttributes::getCgmesSvMetadata, NetworkAttributes::setCgmesSvMetadata));
        networkMappings.put("cgmesSshMetadata", new Mapping<>(15, CgmesSshMetadataAttributes.class, NetworkAttributes::getCgmesSshMetadata, NetworkAttributes::setCgmesSshMetadata));
        networkMappings.put("cimCharacteristics", new Mapping<>(16, CimCharacteristicsAttributes.class, NetworkAttributes::getCimCharacteristics, NetworkAttributes::setCimCharacteristics));
        networkMappings.put("cgmesControlAreas", new Mapping<>(17, CgmesControlAreasAttributes.class, NetworkAttributes::getCgmesControlAreas, NetworkAttributes::setCgmesControlAreas));
        networkMappings.put("cgmesIidmMapping", new Mapping<>(18, CgmesIidmMappingAttributes.class, NetworkAttributes::getCgmesIidmMapping, NetworkAttributes::setCgmesIidmMapping));
    }

    public Map<String, Mapping> getVoltageLevelMappings() {
        return voltageLevelMappings;
    }

    private void createVoltageLevelMappings() {
        voltageLevelMappings.put("substationId", new Mapping<>(1, String.class, VoltageLevelAttributes::getSubstationId, VoltageLevelAttributes::setSubstationId));
        voltageLevelMappings.put("name", new Mapping<>(2, String.class, VoltageLevelAttributes::getName, VoltageLevelAttributes::setName));
        voltageLevelMappings.put("fictitious", new Mapping<>(3, Boolean.class, VoltageLevelAttributes::isFictitious, VoltageLevelAttributes::setFictitious));
        voltageLevelMappings.put("properties", new Mapping<>(4, (Class<Map<String, String>>) mapStrStr.getClass(), VoltageLevelAttributes::getProperties, VoltageLevelAttributes::setProperties));
        voltageLevelMappings.put("aliasByType", new Mapping<>(5, (Class<Map<String, String>>) mapStrStr.getClass(), VoltageLevelAttributes::getAliasByType, VoltageLevelAttributes::setAliasByType));
        voltageLevelMappings.put("aliasesWithoutType", new Mapping<>(6, (Class<Set<String>>) setStr.getClass(), VoltageLevelAttributes::getAliasesWithoutType, VoltageLevelAttributes::setAliasesWithoutType));
        voltageLevelMappings.put("nominalV", new Mapping<>(7, Double.class, VoltageLevelAttributes::getNominalV, VoltageLevelAttributes::setNominalV));
        voltageLevelMappings.put("lowVoltageLimit", new Mapping<>(8, Double.class, VoltageLevelAttributes::getLowVoltageLimit, VoltageLevelAttributes::setLowVoltageLimit));
        voltageLevelMappings.put("highVoltageLimit", new Mapping<>(9, Double.class, VoltageLevelAttributes::getHighVoltageLimit, VoltageLevelAttributes::setHighVoltageLimit));
        voltageLevelMappings.put("topologyKind", new Mapping<>(10, TopologyKind.class, VoltageLevelAttributes::getTopologyKind, VoltageLevelAttributes::setTopologyKind));
        voltageLevelMappings.put("internalConnections", new Mapping<>(11, (Class<List<InternalConnectionAttributes>>) internalConnections.getClass(), VoltageLevelAttributes::getInternalConnections, VoltageLevelAttributes::setInternalConnections));
        voltageLevelMappings.put("calculatedBusesForBusView", new Mapping<>(12, (Class<List<CalculatedBusAttributes>>) calculatedBuses.getClass(), VoltageLevelAttributes::getCalculatedBusesForBusView, VoltageLevelAttributes::setCalculatedBusesForBusView));
        voltageLevelMappings.put("nodeToCalculatedBusForBusView", new Mapping<>(13, (Class<Map<Integer, Integer>>) mapIntInt.getClass(), VoltageLevelAttributes::getNodeToCalculatedBusForBusView, VoltageLevelAttributes::setNodeToCalculatedBusForBusView));
        voltageLevelMappings.put("busToCalculatedBusForBusView", new Mapping<>(14, (Class<Map<String, Integer>>) mapStrInt.getClass(), VoltageLevelAttributes::getBusToCalculatedBusForBusView, VoltageLevelAttributes::setBusToCalculatedBusForBusView));
        voltageLevelMappings.put("calculatedBusesForBusBreakerView", new Mapping<>(15, (Class<List<CalculatedBusAttributes>>) calculatedBuses.getClass(), VoltageLevelAttributes::getCalculatedBusesForBusBreakerView, VoltageLevelAttributes::setCalculatedBusesForBusBreakerView));
        voltageLevelMappings.put("nodeToCalculatedBusForBusBreakerView", new Mapping<>(16, (Class<Map<Integer, Integer>>) mapIntInt.getClass(), VoltageLevelAttributes::getNodeToCalculatedBusForBusBreakerView, VoltageLevelAttributes::setNodeToCalculatedBusForBusBreakerView));
        voltageLevelMappings.put("busToCalculatedBusForBusBreakerView", new Mapping<>(17, (Class<Map<String, Integer>>) mapStrInt.getClass(), VoltageLevelAttributes::getBusToCalculatedBusForBusBreakerView, VoltageLevelAttributes::setBusToCalculatedBusForBusBreakerView));
        voltageLevelMappings.put("slackTerminal", new Mapping<>(18, TerminalRefAttributes.class, VoltageLevelAttributes::getSlackTerminal, VoltageLevelAttributes::setSlackTerminal));
        voltageLevelMappings.put("calculatedBusesValid", new Mapping<>(19, Boolean.class, VoltageLevelAttributes::isCalculatedBusesValid, VoltageLevelAttributes::setCalculatedBusesValid));
    }

    public Map<String, Mapping> getBatteryMappings() {
        return batteryMappings;
    }

    private void createBatteryMappings() {
        batteryMappings.put("name", new Mapping<>(1, String.class, BatteryAttributes::getName, BatteryAttributes::setName));
        batteryMappings.put("voltageLevelId", new Mapping<>(2, String.class, BatteryAttributes::getVoltageLevelId, BatteryAttributes::setVoltageLevelId));
        batteryMappings.put("bus", new Mapping<>(3, String.class, BatteryAttributes::getBus, BatteryAttributes::setBus));
        batteryMappings.put("connectableBus", new Mapping<>(4, String.class, BatteryAttributes::getConnectableBus, BatteryAttributes::setConnectableBus));
        batteryMappings.put("minP", new Mapping<>(5, Double.class, BatteryAttributes::getMinP, BatteryAttributes::setMinP));
        batteryMappings.put("maxP", new Mapping<>(6, Double.class, BatteryAttributes::getMaxP, BatteryAttributes::setMaxP));
        batteryMappings.put("p0", new Mapping<>(7, Double.class, BatteryAttributes::getP0, BatteryAttributes::setP0));
        batteryMappings.put("q0", new Mapping<>(8, Double.class, BatteryAttributes::getQ0, BatteryAttributes::setQ0));
        batteryMappings.put("p", new Mapping<>(9, Double.class, BatteryAttributes::getP, BatteryAttributes::setP));
        batteryMappings.put("q", new Mapping<>(10, Double.class, BatteryAttributes::getQ, BatteryAttributes::setQ));
        batteryMappings.put("fictitious", new Mapping<>(11, Boolean.class, BatteryAttributes::isFictitious, BatteryAttributes::setFictitious));
        batteryMappings.put("minMaxReactiveLimits", new Mapping<>(12, ReactiveLimitsAttributes.class, (BatteryAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (BatteryAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        batteryMappings.put("reactiveCapabilityCurve", new Mapping<>(13, ReactiveLimitsAttributes.class, (BatteryAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (BatteryAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        batteryMappings.put("activePowerControl", new Mapping<>(14, ActivePowerControlAttributes.class, BatteryAttributes::getActivePowerControl, BatteryAttributes::setActivePowerControl));
        batteryMappings.put("node", new Mapping<>(15, Integer.class, BatteryAttributes::getNode, BatteryAttributes::setNode));
        batteryMappings.put("properties", new Mapping<>(16, (Class<Map<String, String>>) mapStrStr.getClass(), BatteryAttributes::getProperties, BatteryAttributes::setProperties));
        batteryMappings.put("aliasByType", new Mapping<>(17, (Class<Map<String, String>>) mapStrStr.getClass(), BatteryAttributes::getAliasByType, BatteryAttributes::setAliasByType));
        batteryMappings.put("aliasesWithoutType", new Mapping<>(18, (Class<Set<String>>) setStr.getClass(), BatteryAttributes::getAliasesWithoutType, BatteryAttributes::setAliasesWithoutType));
        batteryMappings.put("position", new Mapping<>(19, ConnectablePositionAttributes.class, BatteryAttributes::getPosition, BatteryAttributes::setPosition));
    }

    public Map<String, Mapping> getBusbarSectionMappings() {
        return busbarSectionMappings;
    }

    private void createBusbarSectionMappings() {
        busbarSectionMappings.put("name", new Mapping<>(1, String.class, BusbarSectionAttributes::getName, BusbarSectionAttributes::setName));
        busbarSectionMappings.put("voltageLevelId", new Mapping<>(2, String.class, BusbarSectionAttributes::getVoltageLevelId, BusbarSectionAttributes::setVoltageLevelId));
        busbarSectionMappings.put("fictitious", new Mapping<>(3, Boolean.class, BusbarSectionAttributes::isFictitious, BusbarSectionAttributes::setFictitious));
        busbarSectionMappings.put("node", new Mapping<>(4, Integer.class, BusbarSectionAttributes::getNode, BusbarSectionAttributes::setNode));
        busbarSectionMappings.put("properties", new Mapping<>(5, (Class<Map<String, String>>) mapStrStr.getClass(), BusbarSectionAttributes::getProperties, BusbarSectionAttributes::setProperties));
        busbarSectionMappings.put("aliasByType", new Mapping<>(6, (Class<Map<String, String>>) mapStrStr.getClass(), BusbarSectionAttributes::getAliasByType, BusbarSectionAttributes::setAliasByType));
        busbarSectionMappings.put("aliasesWithoutType", new Mapping<>(7, (Class<Set<String>>) setStr.getClass(), BusbarSectionAttributes::getAliasesWithoutType, BusbarSectionAttributes::setAliasesWithoutType));
        busbarSectionMappings.put("position", new Mapping<>(8, BusbarSectionPositionAttributes.class, BusbarSectionAttributes::getPosition, BusbarSectionAttributes::setPosition));
    }

    public Map<String, Mapping> getConfiguredBusMappings() {
        return configuredBusMappings;
    }

    private void createConfiguredBusMappings() {
        configuredBusMappings.put("name", new Mapping<>(1, String.class, ConfiguredBusAttributes::getName, ConfiguredBusAttributes::setName));
        configuredBusMappings.put("voltageLevelId", new Mapping<>(2, String.class, ConfiguredBusAttributes::getVoltageLevelId, ConfiguredBusAttributes::setVoltageLevelId));
        configuredBusMappings.put("fictitious", new Mapping<>(3, Boolean.class, ConfiguredBusAttributes::isFictitious, ConfiguredBusAttributes::setFictitious));
        configuredBusMappings.put("properties", new Mapping<>(4, (Class<Map<String, String>>) mapStrStr.getClass(), ConfiguredBusAttributes::getProperties, ConfiguredBusAttributes::setProperties));
        configuredBusMappings.put("aliasByType", new Mapping<>(5, (Class<Map<String, String>>) mapStrStr.getClass(), ConfiguredBusAttributes::getAliasByType, ConfiguredBusAttributes::setAliasByType));
        configuredBusMappings.put("aliasesWithoutType", new Mapping<>(6, (Class<Set<String>>) setStr.getClass(), ConfiguredBusAttributes::getAliasesWithoutType, ConfiguredBusAttributes::setAliasesWithoutType));
        configuredBusMappings.put("v", new Mapping<>(7, Double.class, ConfiguredBusAttributes::getV, ConfiguredBusAttributes::setV));
        configuredBusMappings.put("angle", new Mapping<>(8, Double.class, ConfiguredBusAttributes::getAngle, ConfiguredBusAttributes::setAngle));
    }

    public Map<String, Mapping> getDanglingLineMappings() {
        return danglingLineMappings;
    }

    private void createDanglingLineMappings() {
        danglingLineMappings.put("name", new Mapping<>(1, String.class, DanglingLineAttributes::getName, DanglingLineAttributes::setName));
        danglingLineMappings.put("voltageLevelId", new Mapping<>(2, String.class, DanglingLineAttributes::getVoltageLevelId, DanglingLineAttributes::setVoltageLevelId));
        danglingLineMappings.put("bus", new Mapping<>(3, String.class, DanglingLineAttributes::getBus, DanglingLineAttributes::setBus));
        danglingLineMappings.put("connectableBus", new Mapping<>(4, String.class, DanglingLineAttributes::getConnectableBus, DanglingLineAttributes::setConnectableBus));
        danglingLineMappings.put("r", new Mapping<>(5, Double.class, DanglingLineAttributes::getR, DanglingLineAttributes::setR));
        danglingLineMappings.put("x", new Mapping<>(6, Double.class, DanglingLineAttributes::getX, DanglingLineAttributes::setX));
        danglingLineMappings.put("g", new Mapping<>(7, Double.class, DanglingLineAttributes::getG, DanglingLineAttributes::setG));
        danglingLineMappings.put("b", new Mapping<>(8, Double.class, DanglingLineAttributes::getB, DanglingLineAttributes::setB));
        danglingLineMappings.put("p0", new Mapping<>(9, Double.class, DanglingLineAttributes::getP0, DanglingLineAttributes::setP0));
        danglingLineMappings.put("q0", new Mapping<>(10, Double.class, DanglingLineAttributes::getQ0, DanglingLineAttributes::setQ0));
        danglingLineMappings.put("p", new Mapping<>(11, Double.class, DanglingLineAttributes::getP, DanglingLineAttributes::setP));
        danglingLineMappings.put("q", new Mapping<>(12, Double.class, DanglingLineAttributes::getQ, DanglingLineAttributes::setQ));
        danglingLineMappings.put("fictitious", new Mapping<>(13, Boolean.class, DanglingLineAttributes::isFictitious, DanglingLineAttributes::setFictitious));
        danglingLineMappings.put("node", new Mapping<>(14, Integer.class, DanglingLineAttributes::getNode, DanglingLineAttributes::setNode));
        danglingLineMappings.put("properties", new Mapping<>(15, (Class<Map<String, String>>) mapStrStr.getClass(), DanglingLineAttributes::getProperties, DanglingLineAttributes::setProperties));
        danglingLineMappings.put("aliasByType", new Mapping<>(16, (Class<Map<String, String>>) mapStrStr.getClass(), DanglingLineAttributes::getAliasByType, DanglingLineAttributes::setAliasByType));
        danglingLineMappings.put("aliasesWithoutType", new Mapping<>(17, (Class<Set<String>>) setStr.getClass(), DanglingLineAttributes::getAliasesWithoutType, DanglingLineAttributes::setAliasesWithoutType));
        danglingLineMappings.put("generation", new Mapping<>(18, DanglingLineGenerationAttributes.class, DanglingLineAttributes::getGeneration, DanglingLineAttributes::setGeneration));
        danglingLineMappings.put("ucteXnodeCode", new Mapping<>(19, String.class, DanglingLineAttributes::getUcteXnodeCode, DanglingLineAttributes::setUcteXnodeCode));
        danglingLineMappings.put("currentLimits", new Mapping<>(20, LimitsAttributes.class, DanglingLineAttributes::getCurrentLimits, DanglingLineAttributes::setCurrentLimits));
        danglingLineMappings.put("position", new Mapping<>(21, ConnectablePositionAttributes.class, DanglingLineAttributes::getPosition, DanglingLineAttributes::setPosition));
        danglingLineMappings.put("apparentPowerLimits", new Mapping<>(22, LimitsAttributes.class, DanglingLineAttributes::getApparentPowerLimits, DanglingLineAttributes::setApparentPowerLimits));
        danglingLineMappings.put("activePowerLimits", new Mapping<>(23, LimitsAttributes.class, DanglingLineAttributes::getActivePowerLimits, DanglingLineAttributes::setActivePowerLimits));
    }

    public Map<String, Mapping> getShuntCompensatorMappings() {
        return shuntCompensatorMappings;
    }

    private void createShuntCompensatorMappings() {
        shuntCompensatorMappings.put("name", new Mapping<>(1, String.class, ShuntCompensatorAttributes::getName, ShuntCompensatorAttributes::setName));
        shuntCompensatorMappings.put("voltageLevelId", new Mapping<>(2, String.class, ShuntCompensatorAttributes::getVoltageLevelId, ShuntCompensatorAttributes::setVoltageLevelId));
        shuntCompensatorMappings.put("bus", new Mapping<>(3, String.class, ShuntCompensatorAttributes::getBus, ShuntCompensatorAttributes::setBus));
        shuntCompensatorMappings.put("connectableBus", new Mapping<>(4, String.class, ShuntCompensatorAttributes::getConnectableBus, ShuntCompensatorAttributes::setConnectableBus));
        shuntCompensatorMappings.put("p", new Mapping<>(5, Double.class, ShuntCompensatorAttributes::getP, ShuntCompensatorAttributes::setP));
        shuntCompensatorMappings.put("q", new Mapping<>(6, Double.class, ShuntCompensatorAttributes::getQ, ShuntCompensatorAttributes::setQ));
        shuntCompensatorMappings.put("fictitious", new Mapping<>(7, Boolean.class, ShuntCompensatorAttributes::isFictitious, ShuntCompensatorAttributes::setFictitious));
        shuntCompensatorMappings.put("voltageRegulatorOn", new Mapping<>(8, Boolean.class, ShuntCompensatorAttributes::isVoltageRegulatorOn, ShuntCompensatorAttributes::setVoltageRegulatorOn));
        shuntCompensatorMappings.put("targetV", new Mapping<>(9, Double.class, ShuntCompensatorAttributes::getTargetV, ShuntCompensatorAttributes::setTargetV));
        shuntCompensatorMappings.put("targetDeadband", new Mapping<>(10, Double.class, ShuntCompensatorAttributes::getTargetDeadband, ShuntCompensatorAttributes::setTargetDeadband));
        shuntCompensatorMappings.put("regulatingTerminal", new Mapping<>(11, TerminalRefAttributes.class, ShuntCompensatorAttributes::getRegulatingTerminal, ShuntCompensatorAttributes::setRegulatingTerminal));
        shuntCompensatorMappings.put("linearModel", new Mapping<>(12, ShuntCompensatorModelAttributes.class, (ShuntCompensatorAttributes attributes) ->
            attributes.getModel() instanceof ShuntCompensatorLinearModelAttributes ? attributes.getModel() : null,
            (ShuntCompensatorAttributes attributes, ShuntCompensatorModelAttributes model) -> {
                if (model instanceof ShuntCompensatorLinearModelAttributes) {
                    attributes.setModel(model);
                }
            }));
        shuntCompensatorMappings.put("nonLinearModel", new Mapping<>(13, ShuntCompensatorModelAttributes.class, (ShuntCompensatorAttributes attributes) ->
            attributes.getModel() instanceof ShuntCompensatorNonLinearModelAttributes ? attributes.getModel() : null,
            (ShuntCompensatorAttributes attributes, ShuntCompensatorModelAttributes model) -> {
                if (model instanceof ShuntCompensatorNonLinearModelAttributes) {
                    attributes.setModel(model);
                }
            }));
        shuntCompensatorMappings.put("node", new Mapping<>(14, Integer.class, ShuntCompensatorAttributes::getNode, ShuntCompensatorAttributes::setNode));
        shuntCompensatorMappings.put("sectionCount", new Mapping<>(15, Integer.class, ShuntCompensatorAttributes::getSectionCount, ShuntCompensatorAttributes::setSectionCount));
        shuntCompensatorMappings.put("properties", new Mapping<>(16, (Class<Map<String, String>>) mapStrStr.getClass(), ShuntCompensatorAttributes::getProperties, ShuntCompensatorAttributes::setProperties));
        shuntCompensatorMappings.put("aliasByType", new Mapping<>(17, (Class<Map<String, String>>) mapStrStr.getClass(), ShuntCompensatorAttributes::getAliasByType, ShuntCompensatorAttributes::setAliasByType));
        shuntCompensatorMappings.put("aliasesWithoutType", new Mapping<>(18, (Class<Set<String>>) setStr.getClass(), ShuntCompensatorAttributes::getAliasesWithoutType, ShuntCompensatorAttributes::setAliasesWithoutType));
        shuntCompensatorMappings.put("position", new Mapping<>(19, ConnectablePositionAttributes.class, ShuntCompensatorAttributes::getPosition, ShuntCompensatorAttributes::setPosition));
    }

    public Map<String, Mapping> getVscConverterStationMappings() {
        return vscConverterStationMappings;
    }

    private void createVscConverterStationMappings() {
        vscConverterStationMappings.put("name", new Mapping<>(1, String.class, VscConverterStationAttributes::getName, VscConverterStationAttributes::setName));
        vscConverterStationMappings.put("voltageLevelId", new Mapping<>(2, String.class, VscConverterStationAttributes::getVoltageLevelId, VscConverterStationAttributes::setVoltageLevelId));
        vscConverterStationMappings.put("bus", new Mapping<>(3, String.class, VscConverterStationAttributes::getBus, VscConverterStationAttributes::setBus));
        vscConverterStationMappings.put("connectableBus", new Mapping<>(4, String.class, VscConverterStationAttributes::getConnectableBus, VscConverterStationAttributes::setConnectableBus));
        vscConverterStationMappings.put("voltageRegulatorOn", new Mapping<>(5, Boolean.class, VscConverterStationAttributes::getVoltageRegulatorOn, VscConverterStationAttributes::setVoltageRegulatorOn));
        vscConverterStationMappings.put("p", new Mapping<>(6, Double.class, VscConverterStationAttributes::getP, VscConverterStationAttributes::setP));
        vscConverterStationMappings.put("q", new Mapping<>(7, Double.class, VscConverterStationAttributes::getQ, VscConverterStationAttributes::setQ));
        vscConverterStationMappings.put("lossFactor", new Mapping<>(8, Float.class, VscConverterStationAttributes::getLossFactor, VscConverterStationAttributes::setLossFactor));
        vscConverterStationMappings.put("reactivePowerSetPoint", new Mapping<>(9, Double.class, VscConverterStationAttributes::getReactivePowerSetPoint, VscConverterStationAttributes::setReactivePowerSetPoint));
        vscConverterStationMappings.put("voltageSetPoint", new Mapping<>(10, Double.class, VscConverterStationAttributes::getVoltageSetPoint, VscConverterStationAttributes::setVoltageSetPoint));
        vscConverterStationMappings.put("fictitious", new Mapping<>(11, Boolean.class, VscConverterStationAttributes::isFictitious, VscConverterStationAttributes::setFictitious));
        vscConverterStationMappings.put("minMaxReactiveLimits", new Mapping<>(12, ReactiveLimitsAttributes.class, (VscConverterStationAttributes attributes) ->
            attributes.getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes ? attributes.getReactiveLimits() : null,
            (VscConverterStationAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof MinMaxReactiveLimitsAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        vscConverterStationMappings.put("reactiveCapabilityCurve", new Mapping<>(13, ReactiveLimitsAttributes.class, (VscConverterStationAttributes attributes) ->
            attributes.getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes ? attributes.getReactiveLimits() : null,
            (VscConverterStationAttributes attributes, ReactiveLimitsAttributes limits) -> {
                if (limits instanceof ReactiveCapabilityCurveAttributes) {
                    attributes.setReactiveLimits(limits);
                }
            }));
        vscConverterStationMappings.put("node", new Mapping<>(14, Integer.class, VscConverterStationAttributes::getNode, VscConverterStationAttributes::setNode));
        vscConverterStationMappings.put("properties", new Mapping<>(15, (Class<Map<String, String>>) mapStrStr.getClass(), VscConverterStationAttributes::getProperties, VscConverterStationAttributes::setProperties));
        vscConverterStationMappings.put("aliasByType", new Mapping<>(16, (Class<Map<String, String>>) mapStrStr.getClass(), VscConverterStationAttributes::getAliasByType, VscConverterStationAttributes::setAliasByType));
        vscConverterStationMappings.put("aliasesWithoutType", new Mapping<>(17, (Class<Set<String>>) setStr.getClass(), VscConverterStationAttributes::getAliasesWithoutType, VscConverterStationAttributes::setAliasesWithoutType));
        vscConverterStationMappings.put("position", new Mapping<>(18, ConnectablePositionAttributes.class, VscConverterStationAttributes::getPosition, VscConverterStationAttributes::setPosition));
    }

    public Map<String, Mapping> getLccConverterStationMappings() {
        return lccConverterStationMappings;
    }

    private void createLccConverterStationMappings() {
        lccConverterStationMappings.put("name", new Mapping<>(1, String.class, LccConverterStationAttributes::getName, LccConverterStationAttributes::setName));
        lccConverterStationMappings.put("voltageLevelId", new Mapping<>(2, String.class, LccConverterStationAttributes::getVoltageLevelId, LccConverterStationAttributes::setVoltageLevelId));
        lccConverterStationMappings.put("bus", new Mapping<>(3, String.class, LccConverterStationAttributes::getBus, LccConverterStationAttributes::setBus));
        lccConverterStationMappings.put("connectableBus", new Mapping<>(4, String.class, LccConverterStationAttributes::getConnectableBus, LccConverterStationAttributes::setConnectableBus));
        lccConverterStationMappings.put("p", new Mapping<>(5, Double.class, LccConverterStationAttributes::getP, LccConverterStationAttributes::setP));
        lccConverterStationMappings.put("q", new Mapping<>(6, Double.class, LccConverterStationAttributes::getQ, LccConverterStationAttributes::setQ));
        lccConverterStationMappings.put("powerFactor", new Mapping<>(7, Float.class, LccConverterStationAttributes::getPowerFactor, LccConverterStationAttributes::setPowerFactor));
        lccConverterStationMappings.put("lossFactor", new Mapping<>(8, Float.class, LccConverterStationAttributes::getLossFactor, LccConverterStationAttributes::setLossFactor));
        lccConverterStationMappings.put("fictitious", new Mapping<>(9, Boolean.class, LccConverterStationAttributes::isFictitious, LccConverterStationAttributes::setFictitious));
        lccConverterStationMappings.put("node", new Mapping<>(10, Integer.class, LccConverterStationAttributes::getNode, LccConverterStationAttributes::setNode));
        lccConverterStationMappings.put("properties", new Mapping<>(11, (Class<Map<String, String>>) mapStrStr.getClass(), LccConverterStationAttributes::getProperties, LccConverterStationAttributes::setProperties));
        lccConverterStationMappings.put("aliasByType", new Mapping<>(12, (Class<Map<String, String>>) mapStrStr.getClass(), LccConverterStationAttributes::getAliasByType, LccConverterStationAttributes::setAliasByType));
        lccConverterStationMappings.put("aliasesWithoutType", new Mapping<>(13, (Class<Set<String>>) setStr.getClass(), LccConverterStationAttributes::getAliasesWithoutType, LccConverterStationAttributes::setAliasesWithoutType));
        lccConverterStationMappings.put("position", new Mapping<>(14, ConnectablePositionAttributes.class, LccConverterStationAttributes::getPosition, LccConverterStationAttributes::setPosition));
    }

    public Map<String, Mapping> getStaticVarCompensatorMappings() {
        return staticVarCompensatorMappings;
    }

    private void createStaticVarCompensatorMappings() {
        staticVarCompensatorMappings.put("name", new Mapping<>(1, String.class, StaticVarCompensatorAttributes::getName, StaticVarCompensatorAttributes::setName));
        staticVarCompensatorMappings.put("voltageLevelId", new Mapping<>(2, String.class, StaticVarCompensatorAttributes::getVoltageLevelId, StaticVarCompensatorAttributes::setVoltageLevelId));
        staticVarCompensatorMappings.put("bus", new Mapping<>(3, String.class, StaticVarCompensatorAttributes::getBus, StaticVarCompensatorAttributes::setBus));
        staticVarCompensatorMappings.put("connectableBus", new Mapping<>(4, String.class, StaticVarCompensatorAttributes::getConnectableBus, StaticVarCompensatorAttributes::setConnectableBus));
        staticVarCompensatorMappings.put("bmin", new Mapping<>(5, Double.class, StaticVarCompensatorAttributes::getBmin, StaticVarCompensatorAttributes::setBmin));
        staticVarCompensatorMappings.put("bmax", new Mapping<>(6, Double.class, StaticVarCompensatorAttributes::getBmax, StaticVarCompensatorAttributes::setBmax));
        staticVarCompensatorMappings.put("voltageSetPoint", new Mapping<>(7, Double.class, StaticVarCompensatorAttributes::getVoltageSetPoint, StaticVarCompensatorAttributes::setVoltageSetPoint));
        staticVarCompensatorMappings.put("reactivePowerSetPoint", new Mapping<>(8, Double.class, StaticVarCompensatorAttributes::getReactivePowerSetPoint, StaticVarCompensatorAttributes::setReactivePowerSetPoint));
        staticVarCompensatorMappings.put("regulationMode", new Mapping<>(9, StaticVarCompensator.RegulationMode.class, StaticVarCompensatorAttributes::getRegulationMode, StaticVarCompensatorAttributes::setRegulationMode));
        staticVarCompensatorMappings.put("p", new Mapping<>(10, Double.class, StaticVarCompensatorAttributes::getP, StaticVarCompensatorAttributes::setP));
        staticVarCompensatorMappings.put("q", new Mapping<>(11, Double.class, StaticVarCompensatorAttributes::getQ, StaticVarCompensatorAttributes::setQ));
        staticVarCompensatorMappings.put("fictitious", new Mapping<>(12, Boolean.class, StaticVarCompensatorAttributes::isFictitious, StaticVarCompensatorAttributes::setFictitious));
        staticVarCompensatorMappings.put("regulatingTerminal", new Mapping<>(13, TerminalRefAttributes.class, StaticVarCompensatorAttributes::getRegulatingTerminal, StaticVarCompensatorAttributes::setRegulatingTerminal));
        staticVarCompensatorMappings.put("node", new Mapping<>(14, Integer.class, StaticVarCompensatorAttributes::getNode, StaticVarCompensatorAttributes::setNode));
        staticVarCompensatorMappings.put("properties", new Mapping<>(15, (Class<Map<String, String>>) mapStrStr.getClass(), StaticVarCompensatorAttributes::getProperties, StaticVarCompensatorAttributes::setProperties));
        staticVarCompensatorMappings.put("aliasByType", new Mapping<>(16, (Class<Map<String, String>>) mapStrStr.getClass(), StaticVarCompensatorAttributes::getAliasByType, StaticVarCompensatorAttributes::setAliasByType));
        staticVarCompensatorMappings.put("aliasesWithoutType", new Mapping<>(17, (Class<Set<String>>) setStr.getClass(), StaticVarCompensatorAttributes::getAliasesWithoutType, StaticVarCompensatorAttributes::setAliasesWithoutType));
        staticVarCompensatorMappings.put("position", new Mapping<>(18, ConnectablePositionAttributes.class, StaticVarCompensatorAttributes::getPosition, StaticVarCompensatorAttributes::setPosition));
        staticVarCompensatorMappings.put("voltagePerReactivePowerControl", new Mapping<>(19, VoltagePerReactivePowerControlAttributes.class, StaticVarCompensatorAttributes::getVoltagePerReactiveControl, StaticVarCompensatorAttributes::setVoltagePerReactiveControl));
    }

    public Map<String, Mapping> getHvdcLineMappings() {
        return hvdcLineMappings;
    }

    private void createHvdcLineMappings() {
        hvdcLineMappings.put("name", new Mapping<>(1, String.class, HvdcLineAttributes::getName, HvdcLineAttributes::setName));
        hvdcLineMappings.put("fictitious", new Mapping<>(2, Boolean.class, HvdcLineAttributes::isFictitious, HvdcLineAttributes::setFictitious));
        hvdcLineMappings.put("properties", new Mapping<>(3, (Class<Map<String, String>>) mapStrStr.getClass(), HvdcLineAttributes::getProperties, HvdcLineAttributes::setProperties));
        hvdcLineMappings.put("aliasByType", new Mapping<>(4, (Class<Map<String, String>>) mapStrStr.getClass(), HvdcLineAttributes::getAliasByType, HvdcLineAttributes::setAliasByType));
        hvdcLineMappings.put("aliasesWithoutType", new Mapping<>(5, (Class<Set<String>>) setStr.getClass(), HvdcLineAttributes::getAliasesWithoutType, HvdcLineAttributes::setAliasesWithoutType));
        hvdcLineMappings.put("r", new Mapping<>(6, Double.class, HvdcLineAttributes::getR, HvdcLineAttributes::setR));
        hvdcLineMappings.put("nominalV", new Mapping<>(7, Double.class, HvdcLineAttributes::getNominalV, HvdcLineAttributes::setNominalV));
        hvdcLineMappings.put("activePowerSetpoint", new Mapping<>(8, Double.class, HvdcLineAttributes::getActivePowerSetpoint, HvdcLineAttributes::setActivePowerSetpoint));
        hvdcLineMappings.put("maxP", new Mapping<>(9, Double.class, HvdcLineAttributes::getMaxP, HvdcLineAttributes::setMaxP));
        hvdcLineMappings.put("convertersMode", new Mapping<>(10, HvdcLine.ConvertersMode.class, HvdcLineAttributes::getConvertersMode, HvdcLineAttributes::setConvertersMode));
        hvdcLineMappings.put("converterStationId1", new Mapping<>(11, String.class, HvdcLineAttributes::getConverterStationId1, HvdcLineAttributes::setConverterStationId1));
        hvdcLineMappings.put("converterStationId2", new Mapping<>(12, String.class, HvdcLineAttributes::getConverterStationId2, HvdcLineAttributes::setConverterStationId2));
        hvdcLineMappings.put("hvdcAngleDroopActivePowerControl", new Mapping<>(13, HvdcAngleDroopActivePowerControlAttributes.class, HvdcLineAttributes::getHvdcAngleDroopActivePowerControl, HvdcLineAttributes::setHvdcAngleDroopActivePowerControl));
        hvdcLineMappings.put("hvdcOperatorActivePowerRange", new Mapping<>(14, HvdcOperatorActivePowerRangeAttributes.class, HvdcLineAttributes::getHvdcOperatorActivePowerRange, HvdcLineAttributes::setHvdcOperatorActivePowerRange));
    }

    public Map<String, Mapping> getTwoWindingsTransformerMappings() {
        return twoWindingsTransformerMappings;
    }

    private void createTwoWindingsTransformerMappings() {
        twoWindingsTransformerMappings.put("name", new Mapping<>(1, String.class, TwoWindingsTransformerAttributes::getName, TwoWindingsTransformerAttributes::setName));
        twoWindingsTransformerMappings.put("voltageLevelId1", new Mapping<>(2, String.class, TwoWindingsTransformerAttributes::getVoltageLevelId1, TwoWindingsTransformerAttributes::setVoltageLevelId1));
        twoWindingsTransformerMappings.put("voltageLevelId2", new Mapping<>(3, String.class, TwoWindingsTransformerAttributes::getVoltageLevelId2, TwoWindingsTransformerAttributes::setVoltageLevelId2));
        twoWindingsTransformerMappings.put("bus1", new Mapping<>(4, String.class, TwoWindingsTransformerAttributes::getBus1, TwoWindingsTransformerAttributes::setBus1));
        twoWindingsTransformerMappings.put("bus2", new Mapping<>(5, String.class, TwoWindingsTransformerAttributes::getBus2, TwoWindingsTransformerAttributes::setBus2));
        twoWindingsTransformerMappings.put("connectableBus1", new Mapping<>(6, String.class, TwoWindingsTransformerAttributes::getConnectableBus1, TwoWindingsTransformerAttributes::setConnectableBus1));
        twoWindingsTransformerMappings.put("connectableBus2", new Mapping<>(7, String.class, TwoWindingsTransformerAttributes::getConnectableBus2, TwoWindingsTransformerAttributes::setConnectableBus2));
        twoWindingsTransformerMappings.put("branchStatus", new Mapping<>(8, String.class, TwoWindingsTransformerAttributes::getBranchStatus, TwoWindingsTransformerAttributes::setBranchStatus));
        twoWindingsTransformerMappings.put("r", new Mapping<>(9, Double.class, TwoWindingsTransformerAttributes::getR, TwoWindingsTransformerAttributes::setR));
        twoWindingsTransformerMappings.put("x", new Mapping<>(10, Double.class, TwoWindingsTransformerAttributes::getX, TwoWindingsTransformerAttributes::setX));
        twoWindingsTransformerMappings.put("g", new Mapping<>(11, Double.class, TwoWindingsTransformerAttributes::getG, TwoWindingsTransformerAttributes::setG));
        twoWindingsTransformerMappings.put("b", new Mapping<>(12, Double.class, TwoWindingsTransformerAttributes::getB, TwoWindingsTransformerAttributes::setB));
        twoWindingsTransformerMappings.put("ratedU1", new Mapping<>(13, Double.class, TwoWindingsTransformerAttributes::getRatedU1, TwoWindingsTransformerAttributes::setRatedU1));
        twoWindingsTransformerMappings.put("ratedU2", new Mapping<>(14, Double.class, TwoWindingsTransformerAttributes::getRatedU2, TwoWindingsTransformerAttributes::setRatedU2));
        twoWindingsTransformerMappings.put("ratedS", new Mapping<>(15, Double.class, TwoWindingsTransformerAttributes::getRatedS, TwoWindingsTransformerAttributes::setRatedS));
        twoWindingsTransformerMappings.put("p1", new Mapping<>(16, Double.class, TwoWindingsTransformerAttributes::getP1, TwoWindingsTransformerAttributes::setP1));
        twoWindingsTransformerMappings.put("q1", new Mapping<>(17, Double.class, TwoWindingsTransformerAttributes::getQ1, TwoWindingsTransformerAttributes::setQ1));
        twoWindingsTransformerMappings.put("p2", new Mapping<>(18, Double.class, TwoWindingsTransformerAttributes::getP2, TwoWindingsTransformerAttributes::setP2));
        twoWindingsTransformerMappings.put("q2", new Mapping<>(19, Double.class, TwoWindingsTransformerAttributes::getQ2, TwoWindingsTransformerAttributes::setQ2));
        twoWindingsTransformerMappings.put("fictitious", new Mapping<>(20, Boolean.class, TwoWindingsTransformerAttributes::isFictitious, TwoWindingsTransformerAttributes::setFictitious));
        twoWindingsTransformerMappings.put("node1", new Mapping<>(21, Integer.class, TwoWindingsTransformerAttributes::getNode1, TwoWindingsTransformerAttributes::setNode1));
        twoWindingsTransformerMappings.put("node2", new Mapping<>(22, Integer.class, TwoWindingsTransformerAttributes::getNode2, TwoWindingsTransformerAttributes::setNode2));
        twoWindingsTransformerMappings.put("properties", new Mapping<>(23, (Class<Map<String, String>>) mapStrStr.getClass(), TwoWindingsTransformerAttributes::getProperties, TwoWindingsTransformerAttributes::setProperties));
        twoWindingsTransformerMappings.put("aliasByType", new Mapping<>(24, (Class<Map<String, String>>) mapStrStr.getClass(), TwoWindingsTransformerAttributes::getAliasByType, TwoWindingsTransformerAttributes::setAliasByType));
        twoWindingsTransformerMappings.put("aliasesWithoutType", new Mapping<>(25, (Class<Set<String>>) setStr.getClass(), TwoWindingsTransformerAttributes::getAliasesWithoutType, TwoWindingsTransformerAttributes::setAliasesWithoutType));
        twoWindingsTransformerMappings.put("position1", new Mapping<>(26, ConnectablePositionAttributes.class, TwoWindingsTransformerAttributes::getPosition1, TwoWindingsTransformerAttributes::setPosition1));
        twoWindingsTransformerMappings.put("position2", new Mapping<>(27, ConnectablePositionAttributes.class, TwoWindingsTransformerAttributes::getPosition2, TwoWindingsTransformerAttributes::setPosition2));
        twoWindingsTransformerMappings.put("currentLimits1", new Mapping<>(28, LimitsAttributes.class, TwoWindingsTransformerAttributes::getCurrentLimits1, TwoWindingsTransformerAttributes::setCurrentLimits1));
        twoWindingsTransformerMappings.put("currentLimits2", new Mapping<>(29, LimitsAttributes.class, TwoWindingsTransformerAttributes::getCurrentLimits2, TwoWindingsTransformerAttributes::setCurrentLimits2));
        twoWindingsTransformerMappings.put("apparentPowerLimits1", new Mapping<>(30, LimitsAttributes.class, TwoWindingsTransformerAttributes::getApparentPowerLimits1, TwoWindingsTransformerAttributes::setApparentPowerLimits1));
        twoWindingsTransformerMappings.put("apparentPowerLimits2", new Mapping<>(31, LimitsAttributes.class, TwoWindingsTransformerAttributes::getApparentPowerLimits2, TwoWindingsTransformerAttributes::setApparentPowerLimits2));
        twoWindingsTransformerMappings.put("activePowerLimits1", new Mapping<>(32, LimitsAttributes.class, TwoWindingsTransformerAttributes::getActivePowerLimits1, TwoWindingsTransformerAttributes::setActivePowerLimits1));
        twoWindingsTransformerMappings.put("activePowerLimits2", new Mapping<>(33, LimitsAttributes.class, TwoWindingsTransformerAttributes::getActivePowerLimits2, TwoWindingsTransformerAttributes::setActivePowerLimits2));
        twoWindingsTransformerMappings.put("cgmesTapChangers", new Mapping<>(34, (Class<List<CgmesTapChangerAttributes>>) cgmesTapChangers.getClass(), TwoWindingsTransformerAttributes::getCgmesTapChangerAttributesList, TwoWindingsTransformerAttributes::setCgmesTapChangerAttributesList));
        twoWindingsTransformerMappings.put("phaseTapChanger", new Mapping<>(35, PhaseTapChangerAttributes.class, TwoWindingsTransformerAttributes::getPhaseTapChangerAttributes, TwoWindingsTransformerAttributes::setPhaseTapChangerAttributes));
        twoWindingsTransformerMappings.put("ratioTapChanger", new Mapping<>(36, RatioTapChangerAttributes.class, TwoWindingsTransformerAttributes::getRatioTapChangerAttributes, TwoWindingsTransformerAttributes::setRatioTapChangerAttributes));
        twoWindingsTransformerMappings.put("phaseAngleClock", new Mapping<>(37, TwoWindingsTransformerPhaseAngleClockAttributes.class, TwoWindingsTransformerAttributes::getPhaseAngleClockAttributes, TwoWindingsTransformerAttributes::setPhaseAngleClockAttributes));
    }

    public Map<String, Mapping> getThreeWindingsTransformerMappings() {
        return threeWindingsTransformerMappings;
    }

    private void createThreeWindingsTransformerMappings() {
        threeWindingsTransformerMappings.put("name", new Mapping<>(1, String.class, ThreeWindingsTransformerAttributes::getName, ThreeWindingsTransformerAttributes::setName));
        threeWindingsTransformerMappings.put("branchStatus", new Mapping<>(2, String.class, ThreeWindingsTransformerAttributes::getBranchStatus, ThreeWindingsTransformerAttributes::setBranchStatus));
        threeWindingsTransformerMappings.put("p1", new Mapping<>(3, Double.class, ThreeWindingsTransformerAttributes::getP1, ThreeWindingsTransformerAttributes::setP1));
        threeWindingsTransformerMappings.put("q1", new Mapping<>(4, Double.class, ThreeWindingsTransformerAttributes::getQ1, ThreeWindingsTransformerAttributes::setQ1));
        threeWindingsTransformerMappings.put("p2", new Mapping<>(5, Double.class, ThreeWindingsTransformerAttributes::getP2, ThreeWindingsTransformerAttributes::setP2));
        threeWindingsTransformerMappings.put("q2", new Mapping<>(6, Double.class, ThreeWindingsTransformerAttributes::getQ2, ThreeWindingsTransformerAttributes::setQ2));
        threeWindingsTransformerMappings.put("p3", new Mapping<>(7, Double.class, ThreeWindingsTransformerAttributes::getP3, ThreeWindingsTransformerAttributes::setP3));
        threeWindingsTransformerMappings.put("q3", new Mapping<>(8, Double.class, ThreeWindingsTransformerAttributes::getQ3, ThreeWindingsTransformerAttributes::setQ3));
        threeWindingsTransformerMappings.put("ratedU0", new Mapping<>(9, Double.class, ThreeWindingsTransformerAttributes::getRatedU0, ThreeWindingsTransformerAttributes::setRatedU0));
        threeWindingsTransformerMappings.put("fictitious", new Mapping<>(10, Boolean.class, ThreeWindingsTransformerAttributes::isFictitious, ThreeWindingsTransformerAttributes::setFictitious));
        threeWindingsTransformerMappings.put("properties", new Mapping<>(11, (Class<Map<String, String>>) mapStrStr.getClass(), ThreeWindingsTransformerAttributes::getProperties, ThreeWindingsTransformerAttributes::setProperties));
        threeWindingsTransformerMappings.put("aliasByType", new Mapping<>(12, (Class<Map<String, String>>) mapStrStr.getClass(), ThreeWindingsTransformerAttributes::getAliasByType, ThreeWindingsTransformerAttributes::setAliasByType));
        threeWindingsTransformerMappings.put("aliasesWithoutType", new Mapping<>(13, (Class<Set<String>>) setStr.getClass(), ThreeWindingsTransformerAttributes::getAliasesWithoutType, ThreeWindingsTransformerAttributes::setAliasesWithoutType));
        threeWindingsTransformerMappings.put("position1", new Mapping<>(14, ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition1, ThreeWindingsTransformerAttributes::setPosition1));
        threeWindingsTransformerMappings.put("position2", new Mapping<>(15, ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition2, ThreeWindingsTransformerAttributes::setPosition2));
        threeWindingsTransformerMappings.put("position3", new Mapping<>(16, ConnectablePositionAttributes.class, ThreeWindingsTransformerAttributes::getPosition3, ThreeWindingsTransformerAttributes::setPosition3));
        threeWindingsTransformerMappings.put("cgmesTapChangers", new Mapping<>(17, (Class<List<CgmesTapChangerAttributes>>) cgmesTapChangers.getClass(), ThreeWindingsTransformerAttributes::getCgmesTapChangerAttributesList, ThreeWindingsTransformerAttributes::setCgmesTapChangerAttributesList));
        threeWindingsTransformerMappings.put("phaseAngleClock", new Mapping<>(18, ThreeWindingsTransformerPhaseAngleClockAttributes.class, ThreeWindingsTransformerAttributes::getPhaseAngleClock, ThreeWindingsTransformerAttributes::setPhaseAngleClock));

        threeWindingsTransformerMappings.put("voltageLevelId1", new Mapping<>(19, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg1().setVoltageLevelId(vId)));
        threeWindingsTransformerMappings.put("voltageLevelId2", new Mapping<>(20, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg2().setVoltageLevelId(vId)));
        threeWindingsTransformerMappings.put("voltageLevelId3", new Mapping<>(21, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getVoltageLevelId(),
            (ThreeWindingsTransformerAttributes attributes, String vId) -> attributes.getLeg3().setVoltageLevelId(vId)));

        threeWindingsTransformerMappings.put("node1", new Mapping<>(22, Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg1().setNode(node)));
        threeWindingsTransformerMappings.put("node2", new Mapping<>(23, Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg2().setNode(node)));
        threeWindingsTransformerMappings.put("node3", new Mapping<>(24, Integer.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getNode(),
            (ThreeWindingsTransformerAttributes attributes, Integer node) -> attributes.getLeg3().setNode(node)));

        threeWindingsTransformerMappings.put("bus1", new Mapping<>(25, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg1().setBus(bus)));
        threeWindingsTransformerMappings.put("bus2", new Mapping<>(26, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg2().setBus(bus)));
        threeWindingsTransformerMappings.put("bus3", new Mapping<>(27, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg3().setBus(bus)));

        threeWindingsTransformerMappings.put("connectableBus1", new Mapping<>(28, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg1().setConnectableBus(bus)));
        threeWindingsTransformerMappings.put("connectableBus2", new Mapping<>(29, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg2().setConnectableBus(bus)));
        threeWindingsTransformerMappings.put("connectableBus3", new Mapping<>(30, String.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getConnectableBus(),
            (ThreeWindingsTransformerAttributes attributes, String bus) -> attributes.getLeg3().setConnectableBus(bus)));

        threeWindingsTransformerMappings.put("r1", new Mapping<>(31, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg1().setR(r)));
        threeWindingsTransformerMappings.put("r2", new Mapping<>(32, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg2().setR(r)));
        threeWindingsTransformerMappings.put("r3", new Mapping<>(33, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getR(),
            (ThreeWindingsTransformerAttributes attributes, Double r) -> attributes.getLeg3().setR(r)));

        threeWindingsTransformerMappings.put("x1", new Mapping<>(34, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg1().setX(x)));
        threeWindingsTransformerMappings.put("x2", new Mapping<>(35, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg2().setX(x)));
        threeWindingsTransformerMappings.put("x3", new Mapping<>(36, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getX(),
            (ThreeWindingsTransformerAttributes attributes, Double x) -> attributes.getLeg3().setX(x)));

        threeWindingsTransformerMappings.put("g1", new Mapping<>(37, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg1().setG(g)));
        threeWindingsTransformerMappings.put("g2", new Mapping<>(38, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg2().setG(g)));
        threeWindingsTransformerMappings.put("g3", new Mapping<>(39, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getG(),
            (ThreeWindingsTransformerAttributes attributes, Double g) -> attributes.getLeg3().setG(g)));

        threeWindingsTransformerMappings.put("b1", new Mapping<>(40, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg1().setB(b)));
        threeWindingsTransformerMappings.put("b2", new Mapping<>(41, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg2().setB(b)));
        threeWindingsTransformerMappings.put("b3", new Mapping<>(42, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getB(),
            (ThreeWindingsTransformerAttributes attributes, Double b) -> attributes.getLeg3().setB(b)));

        threeWindingsTransformerMappings.put("ratedU1", new Mapping<>(43, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg1().setRatedU(ratedU)));
        threeWindingsTransformerMappings.put("ratedU2", new Mapping<>(44, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg2().setRatedU(ratedU)));
        threeWindingsTransformerMappings.put("ratedU3", new Mapping<>(45, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatedU(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedU) -> attributes.getLeg3().setRatedU(ratedU)));

        threeWindingsTransformerMappings.put("ratedS1", new Mapping<>(46, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg1().setRatedS(ratedS)));
        threeWindingsTransformerMappings.put("ratedS2", new Mapping<>(47, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg2().setRatedS(ratedS)));
        threeWindingsTransformerMappings.put("ratedS3", new Mapping<>(48, Double.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatedS(),
            (ThreeWindingsTransformerAttributes attributes, Double ratedS) -> attributes.getLeg3().setRatedS(ratedS)));

        threeWindingsTransformerMappings.put("phaseTapChanger1", new Mapping<>(49, PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg1().setPhaseTapChangerAttributes(phaseTapChanger)));
        threeWindingsTransformerMappings.put("phaseTapChanger2", new Mapping<>(50, PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg2().setPhaseTapChangerAttributes(phaseTapChanger)));
        threeWindingsTransformerMappings.put("phaseTapChanger3", new Mapping<>(51, PhaseTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getPhaseTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, PhaseTapChangerAttributes phaseTapChanger) -> attributes.getLeg3().setPhaseTapChangerAttributes(phaseTapChanger)));

        threeWindingsTransformerMappings.put("ratioTapChanger1", new Mapping<>(52, RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg1().setRatioTapChangerAttributes(ratioTapChanger)));
        threeWindingsTransformerMappings.put("ratioTapChanger2", new Mapping<>(53, RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg2().setRatioTapChangerAttributes(ratioTapChanger)));
        threeWindingsTransformerMappings.put("ratioTapChanger3", new Mapping<>(54, RatioTapChangerAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getRatioTapChangerAttributes(),
            (ThreeWindingsTransformerAttributes attributes, RatioTapChangerAttributes ratioTapChanger) -> attributes.getLeg3().setRatioTapChangerAttributes(ratioTapChanger)));

        threeWindingsTransformerMappings.put("currentLimits1", new Mapping<>(55, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setCurrentLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("currentLimits2", new Mapping<>(56, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setCurrentLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("currentLimits3", new Mapping<>(57, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getCurrentLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setCurrentLimitsAttributes(limits)));

        threeWindingsTransformerMappings.put("apparentPowerLimits1", new Mapping<>(58, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setApparentPowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("apparentPowerLimits2", new Mapping<>(59, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setApparentPowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("apparentPowerLimits3", new Mapping<>(60, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getApparentPowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setApparentPowerLimitsAttributes(limits)));

        threeWindingsTransformerMappings.put("activePowerLimits1", new Mapping<>(61, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg1().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg1().setActivePowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("activePowerLimits2", new Mapping<>(62, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg2().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg2().setActivePowerLimitsAttributes(limits)));
        threeWindingsTransformerMappings.put("activePowerLimits3", new Mapping<>(63, LimitsAttributes.class,
            (ThreeWindingsTransformerAttributes attributes) -> attributes.getLeg3().getActivePowerLimitsAttributes(),
            (ThreeWindingsTransformerAttributes attributes, LimitsAttributes limits) -> attributes.getLeg3().setActivePowerLimitsAttributes(limits)));
    }

    Mappings() {
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
}
