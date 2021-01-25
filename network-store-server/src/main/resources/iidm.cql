CREATE TYPE IF NOT EXISTS iidm.cgmesSvMetadata (
    description text,
    svVersion int,
    dependencies frozen<list<text>>,
    modelingAuthoritySet text
);

CREATE TYPE IF NOT EXISTS iidm.cimCharacteristics (
    cgmesTopologyKind text,
    cimVersion int
);

CREATE TABLE IF NOT EXISTS iidm.network (
    uuid uuid,
    id text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    caseDate timestamp,
    forecastDistance int,
    sourceFormat text,
    connectedComponentsValid boolean,
    synchronousComponentsValid boolean,
    cgmesSvMetadata iidm.cgmesSvMetadata,
    cimCharacteristics iidm.cimCharacteristics,
    PRIMARY KEY (uuid)
);

CREATE TYPE  IF NOT EXISTS iidm.entsoeArea (
    code text
);

CREATE TABLE IF NOT EXISTS iidm.substation (
    networkUuid uuid,
    id text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    country text,
    tso text,
    entsoeArea iidm.entsoeArea,
    PRIMARY KEY (networkUuid, id)
);

CREATE TYPE IF NOT EXISTS iidm.terminalRef (
    connectableId text,
    side text
);

CREATE TYPE IF NOT EXISTS iidm.internalConnection (
    node1 int,
    node2 int
);

CREATE TYPE IF NOT EXISTS iidm.vertex (
    id text,
    connectableType text,
    node int,
    bus text,
    side text,
);

CREATE TYPE IF NOT EXISTS iidm.calculatedBus (
    vertices frozen<set<iidm.vertex>>,
    ccNum int,
    scNum int,
    v double,
    angle double,
);

CREATE TABLE IF NOT EXISTS iidm.voltageLevel (
    networkUuid uuid,
    id text,
    substationId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    nominalV double,
    lowVoltageLimit double,
    highVoltageLimit double,
    topologyKind text,
    internalConnections frozen<list<iidm.internalConnection>>,
    calculatedBuses frozen<list<iidm.calculatedBus>>,
    nodeToCalculatedBus frozen<map<int, int>>,
    busToCalculatedBus frozen<map<text, int>>,
    calculatedBusesValid boolean,
    slackTerminal iidm.terminalRef,
    PRIMARY KEY (networkUuid, id, substationId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.voltageLevelBySubstation AS
    SELECT networkUuid, id, substationId, name, fictitious, properties, nominalV, lowVoltageLimit, highVoltageLimit, topologyKind, internalConnections, calculatedBuses, nodeToCalculatedBus, busToCalculatedBus, calculatedBusesValid, slackTerminal
    FROM iidm.voltageLevel
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND substationId IS NOT NULL
    PRIMARY KEY (networkUuid, substationId, id);

CREATE TYPE IF NOT EXISTS iidm.connectablePosition (
    label text,
    orderNum int,
    direction text,
);

CREATE TYPE IF NOT EXISTS iidm.minMaxReactiveLimits (
    minQ double,
    maxQ double,
    );

CREATE TYPE IF NOT EXISTS iidm.reactiveCapabilityCurvePoint (
    p double,
    minQ double,
    maxQ double,
    );

CREATE TYPE IF NOT EXISTS iidm.reactiveCapabilityCurve (
    points frozen<map<double, iidm.reactiveCapabilityCurvePoint>>,
    );

CREATE TYPE IF NOT EXISTS iidm.temporaryLimit (
    name text,
    value double,
    acceptableDuration int,
    fictitious boolean
    );

CREATE TYPE IF NOT EXISTS iidm.currentLimits (
    permanentLimit double,
    temporaryLimits frozen<map<int, iidm.temporaryLimit>>,
    );

CREATE TYPE  IF NOT EXISTS iidm.mergedXnode (
    rdp float,
    xdp float,
    xnodeP1 double,
    xnodeQ1 double,
    xnodeP2 double,
    xnodeQ2 double,
    line1Id text,
    line1Name text,
    line1Fictitious boolean,
    line1B1 double,
    line1B2 double,
    line1G1 double,
    line1G2 double,
    line2Id text,
    line2Name text,
    line2Fictitious boolean,
    line2B1 double,
    line2B2 double,
    line2G1 double,
    line2G2 double,
    ucteXnodeCode text,
    );

CREATE TYPE IF NOT EXISTS iidm.activePowerControl (
    participate boolean,
    droop float,
);

CREATE TYPE IF NOT EXISTS iidm.coordinatedReactiveControl (
    qPercent double,
);

CREATE TABLE IF NOT EXISTS iidm.generator (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
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
    position iidm.connectablePosition,
    minMaxReactiveLimits iidm.minMaxReactiveLimits,
    reactiveCapabilityCurve iidm.reactiveCapabilityCurve,
    bus text,
    connectableBus text,
    activePowerControl iidm.activePowerControl,
    regulatingTerminal iidm.terminalRef,
    coordinatedReactiveControl iidm.coordinatedReactiveControl,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.generatorByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, energySource, minP, maxP, voltageRegulatorOn, targetP, targetQ, targetV, ratedS, p, q, position, minMaxReactiveLimits, reactiveCapabilityCurve, bus, connectableBus, activePowerControl, regulatingTerminal, coordinatedReactiveControl
    FROM iidm.generator
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS iidm.battery (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node int,
    minP double,
    maxP double,
    p0 double,
    q0 double,
    p double,
    q double,
    position iidm.connectablePosition,
    minMaxReactiveLimits iidm.minMaxReactiveLimits,
    reactiveCapabilityCurve iidm.reactiveCapabilityCurve,
    bus text,
    connectableBus text,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.batteryByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, minP, maxP, p0, q0, p, q, position, minMaxReactiveLimits, reactiveCapabilityCurve, bus, connectableBus
    FROM iidm.battery
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS iidm.loadDetail (
    fixedActivePower float,
    fixedReactivePower float,
    variableActivePower float,
    variableReactivePower float
    );

CREATE TABLE IF NOT EXISTS iidm.load (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node int,
    loadType text,
    p0 double,
    q0 double,
    p double,
    q double,
    position iidm.connectablePosition,
    bus text,
    connectableBus text,
    loadDetail iidm.loadDetail,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.loadByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, loadType, p0, q0, p, q, position, bus, connectableBus, loadDetail
    FROM iidm.load
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS iidm.shuntCompensatorLinearModel (
    bPerSection double,
    gPerSection double,
    maximumSectionCount int,
    );

CREATE TYPE IF NOT EXISTS iidm.shuntCompensatorNonLinearSection (
    b double,
    g double,
    );

CREATE TYPE IF NOT EXISTS iidm.shuntCompensatorNonLinearModel (
    sections frozen<list<iidm.shuntCompensatorNonLinearSection>>,
    );

CREATE TABLE IF NOT EXISTS iidm.shuntCompensator (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node int,
    linearModel iidm.shuntCompensatorLinearModel,
    nonLinearModel iidm.shuntCompensatorNonLinearModel,
    sectionCount int,
    p double,
    q double,
    position iidm.connectablePosition,
    bus text,
    connectableBus text,
    regulatingTerminal iidm.terminalRef,
    voltageRegulatorOn boolean,
    targetV double,
    targetDeadband double,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.shuntCompensatorByVoltageLevel AS
SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, linearModel, nonLinearModel, sectionCount, p, q, position, bus, connectableBus, regulatingTerminal, voltageRegulatorOn, targetV, targetDeadband
FROM iidm.shuntCompensator
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS iidm.vscConverterStation (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node int,
    lossFactor float,
    voltageRegulatorOn boolean,
    reactivePowerSetPoint double,
    voltageSetPoint double,
    minMaxReactiveLimits iidm.minMaxReactiveLimits,
    reactiveCapabilityCurve iidm.reactiveCapabilityCurve,
    p double,
    q double,
    position iidm.connectablePosition,
    bus text,
    connectableBus text,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.vscConverterStationByVoltageLevel AS
SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, lossFactor, voltageRegulatorOn, reactivePowerSetPoint, voltageSetPoint, minMaxReactiveLimits, reactiveCapabilityCurve, p, q, position, bus, connectableBus
FROM iidm.vscConverterStation
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS iidm.lccConverterStation (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node int,
    powerFactor float,
    lossFactor float,
    p double,
    q double,
    position iidm.connectablePosition,
    bus text,
    connectableBus text,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.lccConverterStationByVoltageLevel AS
SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, powerFactor, lossFactor, p, q, position, bus, connectableBus
FROM iidm.lccConverterStation
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS iidm.voltagePerReactivePowerControl (
    slope double,
);

CREATE TABLE IF NOT EXISTS iidm.staticVarCompensator (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node int,
    bMin double,
    bMax double,
    voltageSetPoint double,
    reactivePowerSetPoint double,
    regulationMode text,
    p double,
    q double,
    position iidm.connectablePosition,
    bus text,
    connectableBus text,
    regulatingTerminal iidm.terminalRef,
    voltagePerReactivePowerControl iidm.voltagePerReactivePowerControl,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.staticVarCompensatorByVoltageLevel AS
SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, bMin, bMax, voltageSetPoint, reactivePowerSetPoint, regulationMode, p, q, position, bus, connectableBus, regulatingTerminal, voltagePerReactivePowerControl
FROM iidm.staticVarCompensator
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TYPE IF NOT EXISTS iidm.busbarSectionPosition (
    busbarIndex int,
    sectionIndex int,
);

CREATE TABLE IF NOT EXISTS iidm.busbarSection (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node int,
    position iidm.busbarSectionPosition,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.busbarSectionByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, position
    FROM iidm.busbarSection
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS iidm.switch (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    properties frozen<map<text, text>>,
    kind text,
    node1 int,
    node2 int,
    open boolean,
    retained boolean,
    fictitious boolean,
    bus1 text,
    bus2 text,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.switchByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, properties, kind, node1, node2, open, retained, fictitious, bus1, bus2
    FROM iidm.switch
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TYPE iidm.phaseTapChangerStep (
    rho double,
    r double,
    x double,
    g double,
    b double,
    alpha double
);

CREATE type iidm.phaseTapChanger (
    lowTapPosition int,
    tapPosition int,
    regulationMode text,
    steps frozen<list<iidm.phaseTapChangerStep>>,
    regulationValue double,
    regulating boolean,
    targetDeadband double,
    regulatingTerminal frozen<iidm.terminalRef>
);

CREATE TYPE iidm.ratioTapChangerStep (
    rho double,
    r double,
    x double,
    g double,
    b double
);

CREATE type iidm.ratioTapChanger (
    lowTapPosition int,
    tapPosition int,
    steps frozen<list<iidm.ratioTapChangerStep>>,
    loadTapChangingCapabilities boolean,
    regulating boolean,
    targetDeadband double,
    targetV double,
    regulatingTerminal frozen<iidm.terminalRef>
);

CREATE TABLE IF NOT EXISTS iidm.twoWindingsTransformer (
    networkUuid uuid,
    id text,
    voltageLevelId1 text,
    voltageLevelId2 text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node1 int,
    node2 int,
    r double,
    x double,
    g double,
    b double,
    ratedU1 double,
    ratedU2 double,
    p1 double,
    q1 double,
    p2 double,
    q2 double,
    position1 iidm.connectablePosition,
    position2 iidm.connectablePosition,
    phaseTapChanger iidm.phaseTapChanger,
    ratioTapChanger iidm.ratioTapChanger,
    bus1 text,
    bus2 text,
    connectableBus1 text,
    connectableBus2 text,
    currentLimits1 iidm.currentLimits,
    currentLimits2 iidm.currentLimits,
    PRIMARY KEY (networkUuid, id)
);

CREATE MATERIALIZED VIEW iidm.twoWindingsTransformerByVoltageLevel1 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, name, fictitious, properties, node1, node2, r, x, g, b, ratedU1, ratedU2, p1, q1, p2, q2, position1, position2, phaseTapChanger, ratioTapChanger, bus1, bus2, connectableBus1, connectableBus2, currentLimits1, currentLimits2
FROM iidm.twoWindingsTransformer
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId1 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId1, id);

CREATE MATERIALIZED VIEW iidm.twoWindingsTransformerByVoltageLevel2 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, name, fictitious, properties, node1, node2, r, x, g, b, ratedU1, ratedU2, p1, q1, p2, q2, position1, position2, phaseTapChanger, ratioTapChanger, bus1, bus2, connectableBus1, connectableBus2, currentLimits1, currentLimits2
FROM iidm.twoWindingsTransformer
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId2 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId2, id);

CREATE TABLE IF NOT EXISTS iidm.threeWindingsTransformer (
    networkUuid uuid,
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
    ratedU0 double,
    p1 double,
    q1 double,
    r1 double,
    x1 double,
    g1 double,
    b1 double,
    ratedU1 double,
    ratedS1 double,
    phaseTapChanger1 iidm.phaseTapChanger,
    ratioTapChanger1 iidm.ratioTapChanger,
    p2 double,
    q2 double,
    r2 double,
    x2 double,
    g2 double,
    b2 double,
    ratedU2 double,
    ratedS2 double,
    phaseTapChanger2 iidm.phaseTapChanger,
    ratioTapChanger2 iidm.ratioTapChanger,
    p3 double,
    q3 double,
    r3 double,
    x3 double,
    g3 double,
    b3 double,
    ratedU3 double,
    ratedS3 double,
    phaseTapChanger3 iidm.phaseTapChanger,
    ratioTapChanger3 iidm.ratioTapChanger,
    position1 iidm.connectablePosition,
    position2 iidm.connectablePosition,
    position3 iidm.connectablePosition,
    currentLimits1 iidm.currentLimits,
    currentLimits2 iidm.currentLimits,
    currentLimits3 iidm.currentLimits,
    bus1 text,
    connectableBus1 text,
    bus2 text,
    connectableBus2 text,
    bus3 text,
    connectableBus3 text,
    PRIMARY KEY (networkUuid, id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.threeWindingsTransformerByVoltageLevel1 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, voltageLevelId3, name, fictitious, properties, node1, node2, node3, ratedU0, p1, q1, r1, x1, g1, b1, ratedU1, ratedS1, phaseTapChanger1, ratioTapChanger1, p2, q2, r2, x2, g2, b2, phaseTapChanger2, ratioTapChanger2, ratedU2, ratedS2, p3, q3, r3, x3, g3, b3, phaseTapChanger3, ratioTapChanger3, ratedU3, ratedS3, position1, position2, position3, currentLimits1, currentLimits2, currentLimits3, bus1, connectableBus1, bus2, connectableBus2, bus3, connectableBus3
FROM iidm.threeWindingsTransformer
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId1 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId1, id);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.threeWindingsTransformerByVoltageLevel2 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, voltageLevelId3, name, fictitious, properties, node1, node2, node3, ratedU0, p1, q1, r1, x1, g1, b1, ratedU1, ratedS1, phaseTapChanger1, ratioTapChanger1, p2, q2, r2, x2, g2, b2, ratedU2, ratedS2, phaseTapChanger2, ratioTapChanger2, p3, q3, r3, x3, g3, b3, ratedU3, ratedS3, phaseTapChanger3, ratioTapChanger3, position1, position2, position3, currentLimits1, currentLimits2, currentLimits3, bus1, connectableBus1, bus2, connectableBus2, bus3, connectableBus3
FROM iidm.threeWindingsTransformer
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId2 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId2, id);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.threeWindingsTransformerByVoltageLevel3 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, voltageLevelId3, name, fictitious, properties, node1, node2, node3, ratedU0, p1, q1, r1, x1, g1, b1, ratedU1, ratedS1, phaseTapChanger1, ratioTapChanger1, p2, q2, r2, x2, g2, b2, ratedU2, ratedS2, phaseTapChanger2, ratioTapChanger2, p3, q3, r3, x3, g3, b3, ratedU3, ratedS3, phaseTapChanger3, ratioTapChanger3, position1, position2, position3, currentLimits1, currentLimits2, currentLimits3, bus1, connectableBus1, bus2, connectableBus2, bus3, connectableBus3
FROM iidm.threeWindingsTransformer
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId3 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId3, id);

CREATE TABLE IF NOT EXISTS iidm.line (
    networkUuid uuid,
    id text,
    voltageLevelId1 text,
    voltageLevelId2 text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
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
    position1 iidm.connectablePosition,
    position2 iidm.connectablePosition,
    bus1 text,
    bus2 text,
    connectableBus1 text,
    connectableBus2 text,
    mergedXnode iidm.mergedXnode,
    currentLimits1 iidm.currentLimits,
    currentLimits2 iidm.currentLimits,
    PRIMARY KEY (networkUuid, id)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.lineByVoltageLevel1 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, name, fictitious, properties, node1, node2, r, x, g1, b1, g2, b2, p1, q1, p2, q2, position1, position2, bus1, bus2, connectableBus1, connectableBus2, mergedXnode, currentLimits1, currentLimits2
FROM iidm.line
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId1 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId1, id);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.lineByVoltageLevel2 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, name, fictitious, properties, node1, node2, r, x, g1, b1, g2, b2, p1, q1, p2, q2, position1, position2, bus1, bus2, connectableBus1, connectableBus2, mergedXnode, currentLimits1, currentLimits2
FROM iidm.line
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId2 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId2, id);

CREATE TABLE IF NOT EXISTS iidm.hvdcLine (
    networkUuid uuid,
    id text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    r double,
    convertersMode text,
    nominalV double,
    activePowerSetpoint double,
    maxP double,
    converterStationId1 text,
    converterStationId2 text,
    PRIMARY KEY (networkUuid, id)
);

CREATE TYPE IF NOT EXISTS iidm.danglingLineGeneration (
    minP double,
    maxP double,
    targetP double,
    targetQ double,
    targetV double,
    voltageRegulatorOn boolean,
    minMaxReactiveLimits frozen<iidm.minMaxReactiveLimits>,
    reactiveCapabilityCurve frozen<iidm.reactiveCapabilityCurve>,
    );

CREATE TABLE IF NOT EXISTS iidm.danglingLine (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    node int,
    p0 double,
    q0 double,
    r double,
    x double,
    g double,
    b double,
    generation iidm.danglingLineGeneration,
    ucteXNodeCode text,
    currentLimits iidm.currentLimits,
    p double,
    q double,
    position iidm.connectablePosition,
    bus text,
    connectableBus text,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.danglingLineByVoltageLevel AS
SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, node, p0, q0, r, x, g, b, generation, ucteXNodeCode, currentLimits, p, q, position, bus, connectableBus
FROM iidm.danglingLine
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS iidm.configuredBus (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    fictitious boolean,
    properties frozen<map<text, text>>,
    v double,
    angle double,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS iidm.configuredBusByVoltageLevel AS
SELECT networkUuid, id, voltageLevelId, name, fictitious, properties, v, angle
FROM iidm.configuredBus
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
PRIMARY KEY (networkUuid, voltageLevelId, id);

