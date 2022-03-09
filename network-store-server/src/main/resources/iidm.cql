CREATE TYPE IF NOT EXISTS threeWindingsTransformerPhaseAngleClock (
    phaseAngleClockLeg2 int,
    phaseAngleClockLeg3 int,
    );

CREATE TYPE IF NOT EXISTS twoWindingsTransformerPhaseAngleClock (
    phaseAngleClock int,
    );

CREATE TYPE IF NOT EXISTS cgmesSshMetadata (
    description text,
    sshVersion int,
    dependencies frozen<list<text>>,
    modelingAuthoritySet text
    );

CREATE TYPE IF NOT EXISTS cgmesSvMetadata (
    description text,
    svVersion int,
    dependencies frozen<list<text>>,
    modelingAuthoritySet text
);

CREATE TYPE IF NOT EXISTS cimCharacteristics (
    cgmesTopologyKind text,
    cimVersion int
);

CREATE TYPE IF NOT EXISTS terminalRef (
    connectableId text,
    side text
);

CREATE TYPE IF NOT EXISTS cgmesControlArea (
    id text,
    name text,
    energyIdentificationCodeEic text,
    terminals frozen<list<terminalRef>>,
    boundaries frozen<list<terminalRef>>,
    netInterchange double
);

CREATE TYPE IF NOT EXISTS cgmesControlAreas (
    controlAreas frozen<list<cgmesControlArea>>,
);

CREATE TYPE IF NOT EXISTS cgmesIidmMapping (
    equipmentSideTopologicalNodeMap frozen<map<terminalRef, text>>,
    busTopologicalNodeMap frozen<map<text, frozen<set<text>>>>,
    unmapped frozen<set<text>>
);

CREATE TYPE IF NOT EXISTS cgmesTapChanger (
    id text,
    combinedTapChangerId text,
    type text,
    hidden boolean,
    step int,
    controlId text
);

CREATE TABLE IF NOT EXISTS network (
    uuid uuid,
    variantNum int,
    id text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    idByAlias frozen<map<text, text>>,
    caseDate timestamp,
    forecastDistance int,
    sourceFormat text,
    connectedComponentsValid boolean,
    synchronousComponentsValid boolean,
    cgmesSvMetadata cgmesSvMetadata,
    cgmesSshMetadata cgmesSshMetadata,
    cgmesIidmMapping cgmesIidmMapping,
    cimCharacteristics cimCharacteristics,
    cgmesControlAreas cgmesControlAreas,
    variantId text,
    PRIMARY KEY (uuid, variantNum)
);

CREATE TYPE  IF NOT EXISTS entsoeArea (
    code text
);

CREATE TABLE IF NOT EXISTS substation (
    networkUuid uuid,
    variantNum int,
    id text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    country text,
    tso text,
    entsoeArea entsoeArea,
    geographicalTags frozen<set<text>>,
    PRIMARY KEY (networkUuid, variantNum, id)
);

CREATE TYPE IF NOT EXISTS internalConnection (
    node1 int,
    node2 int
);

CREATE TYPE IF NOT EXISTS vertex (
    id text,
    connectableType text,
    node int,
    bus text,
    side text,
);

CREATE TYPE IF NOT EXISTS calculatedBus (
    vertices frozen<set<vertex>>,
    ccNum int,
    scNum int,
    v double,
    angle double,
);

CREATE TABLE IF NOT EXISTS voltageLevel (
    networkUuid uuid,
    variantNum int,
    id text,
    substationId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    nominalV double,
    lowVoltageLimit double,
    highVoltageLimit double,
    topologyKind text,
    internalConnections frozen<list<internalConnection>>,
    calculatedBusesForBusView frozen<list<calculatedBus>>,
    nodeToCalculatedBusForBusView frozen<map<int, int>>,
    busToCalculatedBusForBusView frozen<map<text, int>>,
    calculatedBusesForBusBreakerView frozen<list<calculatedBus>>,
    nodeToCalculatedBusForBusBreakerView frozen<map<int, int>>,
    busToCalculatedBusForBusBreakerView frozen<map<text, int>>,
    calculatedBusesValid boolean,
    slackTerminal terminalRef,
    PRIMARY KEY (networkUuid, variantNum, id, substationId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS voltageLevelBySubstation AS
    SELECT networkUuid, variantNum, id, substationId, name, fictitious, properties, aliasesWithoutType, aliasByType, nominalV, lowVoltageLimit, highVoltageLimit, topologyKind, internalConnections, calculatedBusesForBusView, nodeToCalculatedBusForBusView, busToCalculatedBusForBusView, calculatedBusesForBusBreakerView, nodeToCalculatedBusForBusBreakerView, busToCalculatedBusForBusBreakerView, calculatedBusesValid, slackTerminal
    FROM voltageLevel
    WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND substationId IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, substationId, id);

CREATE TYPE IF NOT EXISTS connectablePosition (
    label text,
    orderNum int,
    direction text,
);

CREATE TYPE IF NOT EXISTS minMaxReactiveLimits (
    minQ double,
    maxQ double,
    );

CREATE TYPE IF NOT EXISTS reactiveCapabilityCurvePoint (
    p double,
    minQ double,
    maxQ double,
    );

CREATE TYPE IF NOT EXISTS reactiveCapabilityCurve (
    points frozen<map<double, reactiveCapabilityCurvePoint>>,
    );

CREATE TYPE IF NOT EXISTS temporaryLimit (
    name text,
    value double,
    acceptableDuration int,
    fictitious boolean
    );

CREATE TYPE IF NOT EXISTS currentLimits (
    permanentLimit double,
    temporaryLimits frozen<map<int, temporaryLimit>>,
    );

CREATE TYPE  IF NOT EXISTS mergedXnode (
    rdp double,
    xdp double,
    xnodeP1 double,
    xnodeQ1 double,
    xnodeP2 double,
    xnodeQ2 double,
    line1Name text,
    line2Name text,
    ucteXnodeCode text,
    );

CREATE TYPE IF NOT EXISTS activePowerControl (
    participate boolean,
    droop float,
);

CREATE TYPE IF NOT EXISTS coordinatedReactiveControl (
    qPercent double,
);

CREATE TYPE IF NOT EXISTS remoteReactivePowerControl (
    targetQ double,
    regulatingTerminal frozen<terminalRef>,
    enabled boolean,
);

CREATE TABLE IF NOT EXISTS generator (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    energySource text,
    minP double,
    maxP double,
    voltageRegulatorOn boolean,
    targetP double,
    targetQ double,
    targetV double,
    ratedS double,
    p double,
    q double,
    position connectablePosition,
    minMaxReactiveLimits minMaxReactiveLimits,
    reactiveCapabilityCurve reactiveCapabilityCurve,
    bus text,
    connectableBus text,
    activePowerControl activePowerControl,
    regulatingTerminal terminalRef,
    coordinatedReactiveControl coordinatedReactiveControl,
    remoteReactivePowerControl remoteReactivePowerControl,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS generatorByVoltageLevel AS
    SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, energySource, minP, maxP, voltageRegulatorOn, targetP, targetQ, targetV, ratedS, p, q, position, minMaxReactiveLimits, reactiveCapabilityCurve, bus, connectableBus, activePowerControl, regulatingTerminal, coordinatedReactiveControl, remoteReactivePowerControl
    FROM generator
    WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS battery (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    minP double,
    maxP double,
    p0 double,
    q0 double,
    p double,
    q double,
    position connectablePosition,
    minMaxReactiveLimits minMaxReactiveLimits,
    reactiveCapabilityCurve reactiveCapabilityCurve,
    bus text,
    connectableBus text,
    activePowerControl activePowerControl,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS batteryByVoltageLevel AS
    SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, minP, maxP, p0, q0, p, q, position, minMaxReactiveLimits, reactiveCapabilityCurve, bus, connectableBus, activePowerControl
    FROM battery
    WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS loadDetail (
    fixedActivePower float,
    fixedReactivePower float,
    variableActivePower float,
    variableReactivePower float
    );

CREATE TABLE IF NOT EXISTS load (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    loadType text,
    p0 double,
    q0 double,
    p double,
    q double,
    position connectablePosition,
    bus text,
    connectableBus text,
    loadDetail loadDetail,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS loadByVoltageLevel AS
    SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, loadType, p0, q0, p, q, position, bus, connectableBus, loadDetail
    FROM load
    WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS shuntCompensatorLinearModel (
    bPerSection double,
    gPerSection double,
    maximumSectionCount int,
    );

CREATE TYPE IF NOT EXISTS shuntCompensatorNonLinearSection (
    b double,
    g double,
    );

CREATE TYPE IF NOT EXISTS shuntCompensatorNonLinearModel (
    sections frozen<list<shuntCompensatorNonLinearSection>>,
    );

CREATE TABLE IF NOT EXISTS shuntCompensator (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    linearModel shuntCompensatorLinearModel,
    nonLinearModel shuntCompensatorNonLinearModel,
    sectionCount int,
    p double,
    q double,
    position connectablePosition,
    bus text,
    connectableBus text,
    regulatingTerminal terminalRef,
    voltageRegulatorOn boolean,
    targetV double,
    targetDeadband double,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS shuntCompensatorByVoltageLevel AS
SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, linearModel, nonLinearModel, sectionCount, p, q, position, bus, connectableBus, regulatingTerminal, voltageRegulatorOn, targetV, targetDeadband
FROM shuntCompensator
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS vscConverterStation (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    lossFactor float,
    voltageRegulatorOn boolean,
    reactivePowerSetPoint double,
    voltageSetPoint double,
    minMaxReactiveLimits minMaxReactiveLimits,
    reactiveCapabilityCurve reactiveCapabilityCurve,
    p double,
    q double,
    position connectablePosition,
    bus text,
    connectableBus text,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS vscConverterStationByVoltageLevel AS
SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, lossFactor, voltageRegulatorOn, reactivePowerSetPoint, voltageSetPoint, minMaxReactiveLimits, reactiveCapabilityCurve, p, q, position, bus, connectableBus
FROM vscConverterStation
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS lccConverterStation (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    powerFactor float,
    lossFactor float,
    p double,
    q double,
    position connectablePosition,
    bus text,
    connectableBus text,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS lccConverterStationByVoltageLevel AS
SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, powerFactor, lossFactor, p, q, position, bus, connectableBus
FROM lccConverterStation
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS voltagePerReactivePowerControl (
    slope double,
);

CREATE TABLE IF NOT EXISTS staticVarCompensator (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    bMin double,
    bMax double,
    voltageSetPoint double,
    reactivePowerSetPoint double,
    regulationMode text,
    p double,
    q double,
    position connectablePosition,
    bus text,
    connectableBus text,
    regulatingTerminal terminalRef,
    voltagePerReactivePowerControl voltagePerReactivePowerControl,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS staticVarCompensatorByVoltageLevel AS
SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, bMin, bMax, voltageSetPoint, reactivePowerSetPoint, regulationMode, p, q, position, bus, connectableBus, regulatingTerminal, voltagePerReactivePowerControl
FROM staticVarCompensator
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS busbarSectionPosition (
    busbarIndex int,
    sectionIndex int,
);

CREATE TABLE IF NOT EXISTS busbarSection (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    position busbarSectionPosition,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS busbarSectionByVoltageLevel AS
    SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, position
    FROM busbarSection
    WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS switch (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    kind text,
    node1 int,
    node2 int,
    open boolean,
    retained boolean,
    fictitious boolean,
    bus1 text,
    bus2 text,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS switchByVoltageLevel AS
    SELECT networkUuid, variantNum, id, voltageLevelId, name, properties, aliasesWithoutType, aliasByType, kind, node1, node2, open, retained, fictitious, bus1, bus2
    FROM switch
    WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS phaseTapChangerStep (
    rho double,
    r double,
    x double,
    g double,
    b double,
    alpha double
);

CREATE type IF NOT EXISTS phaseTapChanger (
    lowTapPosition int,
    tapPosition int,
    regulationMode text,
    steps frozen<list<phaseTapChangerStep>>,
    regulationValue double,
    regulating boolean,
    targetDeadband double,
    regulatingTerminal frozen<terminalRef>
);

CREATE TYPE IF NOT EXISTS ratioTapChangerStep (
    rho double,
    r double,
    x double,
    g double,
    b double
);

CREATE type IF NOT EXISTS ratioTapChanger (
    lowTapPosition int,
    tapPosition int,
    steps frozen<list<ratioTapChangerStep>>,
    loadTapChangingCapabilities boolean,
    regulating boolean,
    targetDeadband double,
    targetV double,
    regulatingTerminal frozen<terminalRef>
);

CREATE TABLE IF NOT EXISTS twoWindingsTransformer (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId1 text,
    voltageLevelId2 text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node1 int,
    node2 int,
    r double,
    x double,
    g double,
    b double,
    ratedU1 double,
    ratedU2 double,
    ratedS double,
    p1 double,
    q1 double,
    p2 double,
    q2 double,
    position1 connectablePosition,
    position2 connectablePosition,
    phaseTapChanger phaseTapChanger,
    ratioTapChanger ratioTapChanger,
    bus1 text,
    bus2 text,
    connectableBus1 text,
    connectableBus2 text,
    currentLimits1 currentLimits,
    currentLimits2 currentLimits,
    activePowerLimits1 currentLimits,
    activePowerLimits2 currentLimits,
    apparentPowerLimits1 currentLimits,
    apparentPowerLimits2 currentLimits,
    phaseAngleClock twoWindingsTransformerPhaseAngleClock,
    branchStatus text,
    cgmesTapChangers frozen<list<cgmesTapChanger>>,
    PRIMARY KEY (networkUuid, variantNum, id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS twoWindingsTransformerByVoltageLevel1 AS
SELECT networkUuid, variantNum, id, voltageLevelId1, voltageLevelId2, name, fictitious, properties, aliasesWithoutType, aliasByType, node1, node2, r, x, g, b, ratedU1, ratedU2, ratedS, p1, q1, p2, q2, position1, position2, phaseTapChanger, ratioTapChanger, bus1, bus2, connectableBus1, connectableBus2, currentLimits1, currentLimits2, activePowerLimits1, activePowerLimits2, apparentPowerLimits1, apparentPowerLimits2, phaseAngleClock, branchStatus, cgmesTapChangers
FROM twoWindingsTransformer
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId1 IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId1, id);

CREATE MATERIALIZED VIEW IF NOT EXISTS twoWindingsTransformerByVoltageLevel2 AS
SELECT networkUuid, variantNum, id, voltageLevelId1, voltageLevelId2, name, fictitious, properties, aliasesWithoutType, aliasByType, node1, node2, r, x, g, b, ratedU1, ratedU2, ratedS, p1, q1, p2, q2, position1, position2, phaseTapChanger, ratioTapChanger, bus1, bus2, connectableBus1, connectableBus2, currentLimits1, currentLimits2, activePowerLimits1, activePowerLimits2, apparentPowerLimits1, apparentPowerLimits2, phaseAngleClock, branchStatus, cgmesTapChangers
FROM twoWindingsTransformer
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId2 IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId2, id);

CREATE TABLE IF NOT EXISTS threeWindingsTransformer (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId1 text,
    voltageLevelId2 text,
    voltageLevelId3 text,
    node1 int,
    node2 int,
    node3 int,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    ratedU0 double,
    p1 double,
    q1 double,
    r1 double,
    x1 double,
    g1 double,
    b1 double,
    ratedU1 double,
    ratedS1 double,
    phaseTapChanger1 phaseTapChanger,
    ratioTapChanger1 ratioTapChanger,
    p2 double,
    q2 double,
    r2 double,
    x2 double,
    g2 double,
    b2 double,
    ratedU2 double,
    ratedS2 double,
    phaseTapChanger2 phaseTapChanger,
    ratioTapChanger2 ratioTapChanger,
    p3 double,
    q3 double,
    r3 double,
    x3 double,
    g3 double,
    b3 double,
    ratedU3 double,
    ratedS3 double,
    phaseTapChanger3 phaseTapChanger,
    ratioTapChanger3 ratioTapChanger,
    position1 connectablePosition,
    position2 connectablePosition,
    position3 connectablePosition,
    currentLimits1 currentLimits,
    currentLimits2 currentLimits,
    currentLimits3 currentLimits,
    activePowerLimits1 currentLimits,
    activePowerLimits2 currentLimits,
    activePowerLimits3 currentLimits,
    apparentPowerLimits1 currentLimits,
    apparentPowerLimits2 currentLimits,
    apparentPowerLimits3 currentLimits,
    bus1 text,
    connectableBus1 text,
    bus2 text,
    connectableBus2 text,
    bus3 text,
    connectableBus3 text,
    phaseAngleClock threeWindingsTransformerPhaseAngleClock,
    branchStatus text,
    cgmesTapChangers frozen<list<cgmesTapChanger>>,
    PRIMARY KEY (networkUuid, variantNum, id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS threeWindingsTransformerByVoltageLevel1 AS
SELECT networkUuid, variantNum, id, voltageLevelId1, voltageLevelId2, voltageLevelId3, name, fictitious, properties, aliasesWithoutType, aliasByType, node1, node2, node3, ratedU0, p1, q1, r1, x1, g1, b1, ratedU1, ratedS1, phaseTapChanger1, ratioTapChanger1, p2, q2, r2, x2, g2, b2, phaseTapChanger2, ratioTapChanger2, ratedU2, ratedS2, p3, q3, r3, x3, g3, b3, phaseTapChanger3, ratioTapChanger3, ratedU3, ratedS3, position1, position2, position3, currentLimits1, currentLimits2, currentLimits3, activePowerLimits1, activePowerLimits2, activePowerLimits3, apparentPowerLimits1, apparentPowerLimits2, apparentPowerLimits3, bus1, connectableBus1, bus2, connectableBus2, bus3, connectableBus3, phaseAngleClock, branchStatus, cgmesTapChangers
FROM threeWindingsTransformer
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId1 IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId1, id);

CREATE MATERIALIZED VIEW IF NOT EXISTS threeWindingsTransformerByVoltageLevel2 AS
SELECT networkUuid, variantNum, id, voltageLevelId1, voltageLevelId2, voltageLevelId3, name, fictitious, properties, aliasesWithoutType, aliasByType, node1, node2, node3, ratedU0, p1, q1, r1, x1, g1, b1, ratedU1, ratedS1, phaseTapChanger1, ratioTapChanger1, p2, q2, r2, x2, g2, b2, ratedU2, ratedS2, phaseTapChanger2, ratioTapChanger2, p3, q3, r3, x3, g3, b3, ratedU3, ratedS3, phaseTapChanger3, ratioTapChanger3, position1, position2, position3, currentLimits1, currentLimits2, currentLimits3, activePowerLimits1, activePowerLimits2, activePowerLimits3, apparentPowerLimits1, apparentPowerLimits2, apparentPowerLimits3, bus1, connectableBus1, bus2, connectableBus2, bus3, connectableBus3, phaseAngleClock, branchStatus, cgmesTapChangers
FROM threeWindingsTransformer
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId2 IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId2, id);

CREATE MATERIALIZED VIEW IF NOT EXISTS threeWindingsTransformerByVoltageLevel3 AS
SELECT networkUuid, variantNum, id, voltageLevelId1, voltageLevelId2, voltageLevelId3, name, fictitious, properties, aliasesWithoutType, aliasByType, node1, node2, node3, ratedU0, p1, q1, r1, x1, g1, b1, ratedU1, ratedS1, phaseTapChanger1, ratioTapChanger1, p2, q2, r2, x2, g2, b2, ratedU2, ratedS2, phaseTapChanger2, ratioTapChanger2, p3, q3, r3, x3, g3, b3, ratedU3, ratedS3, phaseTapChanger3, ratioTapChanger3, position1, position2, position3, currentLimits1, currentLimits2, currentLimits3, activePowerLimits1, activePowerLimits2, activePowerLimits3, apparentPowerLimits1, apparentPowerLimits2, apparentPowerLimits3, bus1, connectableBus1, bus2, connectableBus2, bus3, connectableBus3, phaseAngleClock, branchStatus, cgmesTapChangers
FROM threeWindingsTransformer
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId3 IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId3, id);

CREATE TABLE IF NOT EXISTS line (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId1 text,
    voltageLevelId2 text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node1 int,
    node2 int,
    r double,
    x double,
    g1 double,
    b1 double,
    g2 double,
    b2 double,
    p1 double,
    q1 double,
    p2 double,
    q2 double,
    position1 connectablePosition,
    position2 connectablePosition,
    bus1 text,
    bus2 text,
    connectableBus1 text,
    connectableBus2 text,
    mergedXnode mergedXnode,
    currentLimits1 currentLimits,
    currentLimits2 currentLimits,
    activePowerLimits1 currentLimits,
    activePowerLimits2 currentLimits,
    apparentPowerLimits1 currentLimits,
    apparentPowerLimits2 currentLimits,
    branchStatus text,
    PRIMARY KEY (networkUuid, variantNum, id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS lineByVoltageLevel1 AS
SELECT networkUuid, variantNum, id, voltageLevelId1, voltageLevelId2, name, fictitious, properties, aliasesWithoutType, aliasByType, node1, node2, r, x, g1, b1, g2, b2, p1, q1, p2, q2, position1, position2, bus1, bus2, connectableBus1, connectableBus2, mergedXnode, currentLimits1, currentLimits2, activePowerLimits1, activePowerLimits2, apparentPowerLimits1, apparentPowerLimits2, branchStatus
FROM line
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId1 IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId1, id);

CREATE MATERIALIZED VIEW IF NOT EXISTS lineByVoltageLevel2 AS
SELECT networkUuid, variantNum, id, voltageLevelId1, voltageLevelId2, name, fictitious, properties, aliasesWithoutType, aliasByType, node1, node2, r, x, g1, b1, g2, b2, p1, q1, p2, q2, position1, position2, bus1, bus2, connectableBus1, connectableBus2, mergedXnode, currentLimits1, currentLimits2, activePowerLimits1, activePowerLimits2, apparentPowerLimits1, apparentPowerLimits2, branchStatus
FROM line
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId2 IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId2, id);

CREATE TYPE IF NOT EXISTS hvdcAngleDroopActivePowerControl (
    p0 float,
    droop float,
    enabled boolean
);

CREATE TYPE IF NOT EXISTS hvdcOperatorActivePowerRange (
    oprFromCS1toCS2 float,
    oprFromCS2toCS1 float
);

CREATE TABLE IF NOT EXISTS hvdcLine (
    networkUuid uuid,
    variantNum int,
    id text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    r double,
    convertersMode text,
    nominalV double,
    activePowerSetpoint double,
    maxP double,
    converterStationId1 text,
    converterStationId2 text,
    hvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl,
    hvdcOperatorActivePowerRange hvdcOperatorActivePowerRange,
    PRIMARY KEY (networkUuid, variantNum, id)
);

CREATE TYPE IF NOT EXISTS danglingLineGeneration (
    minP double,
    maxP double,
    targetP double,
    targetQ double,
    targetV double,
    voltageRegulatorOn boolean,
    minMaxReactiveLimits frozen<minMaxReactiveLimits>,
    reactiveCapabilityCurve frozen<reactiveCapabilityCurve>,
    );

CREATE TABLE IF NOT EXISTS danglingLine (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    node int,
    p0 double,
    q0 double,
    r double,
    x double,
    g double,
    b double,
    generation danglingLineGeneration,
    ucteXNodeCode text,
    currentLimits currentLimits,
    activePowerLimits currentLimits,
    apparentPowerLimits currentLimits,
    p double,
    q double,
    position connectablePosition,
    bus text,
    connectableBus text,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS danglingLineByVoltageLevel AS
SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, node, p0, q0, r, x, g, b, generation, ucteXNodeCode, currentLimits, activePowerLimits, apparentPowerLimits, p, q, position, bus, connectableBus
FROM danglingLine
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS configuredBus (
    networkUuid uuid,
    variantNum int,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    aliasesWithoutType frozen<set<text>>,
    aliasByType frozen<map<text, text>>,
    v double,
    angle double,
    PRIMARY KEY (networkUuid, variantNum, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS configuredBusByVoltageLevel AS
SELECT networkUuid, variantNum, id, voltageLevelId, name, fictitious, properties, aliasesWithoutType, aliasByType, v, angle
FROM configuredBus
WHERE networkUuid IS NOT NULL AND variantNum IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
PRIMARY KEY (networkUuid, variantNum, voltageLevelId, id);

