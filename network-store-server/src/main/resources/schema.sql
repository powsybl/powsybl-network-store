CREATE TABLE IF NOT EXISTS network (
    uuid uuid,
    variantNum int,
    id VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    idByAlias text,
    caseDate timestamp,
    forecastDistance int,
    sourceFormat VARCHAR(50),
    connectedComponentsValid boolean,
    synchronousComponentsValid boolean,
    cgmesSvMetadata text,
    cgmesSshMetadata text,
    cimCharacteristics text,
    cgmesControlAreas text,
    baseVoltageMapping text,
    variantId VARCHAR(255),
    PRIMARY KEY (uuid, variantNum)
);

CREATE TABLE IF NOT EXISTS substation (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    country text,
    tso VARCHAR(50),
    entsoeArea text,
    geographicalTags text,
    PRIMARY KEY (networkUuid, variantNum, id)
);

CREATE TABLE IF NOT EXISTS voltageLevel (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    substationId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    nominalV double precision,
    lowVoltageLimit double precision,
    highVoltageLimit double precision,
    topologyKind text,
    internalConnections text,
    calculatedBusesForBusView text,
    nodeToCalculatedBusForBusView text,
    busToCalculatedBusForBusView text,
    calculatedBusesForBusBreakerView text,
    nodeToCalculatedBusForBusBreakerView text,
    busToCalculatedBusForBusBreakerView text,
    calculatedBusesValid boolean,
    slackTerminal text,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on voltageLevel (networkUuid, variantNum, substationId);

CREATE TABLE IF NOT EXISTS generator (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    energySource text,
    minP double precision,
    maxP double precision,
    voltageRegulatorOn boolean,
    targetP double precision,
    targetQ double precision,
    targetV double precision,
    ratedS double precision,
    p double precision,
    q double precision,
    position text,
    minMaxReactiveLimits text,
    reactiveCapabilityCurve text,
    bus VARCHAR(255),
    connectableBus VARCHAR(255),
    activePowerControl text,
    regulatingTerminal text,
    coordinatedReactiveControl text,
    remoteReactivePowerControl text,
    entsoeCategory text,
    generatorStartup text,
    generatorShortCircuit text,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on generator (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS battery (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    minP double precision,
    maxP double precision,
    targetP double precision,
    targetQ double precision,
    p double precision,
    q double precision,
    position text,
    minMaxReactiveLimits text,
    reactiveCapabilityCurve text,
    bus VARCHAR(255),
    connectableBus VARCHAR(255),
    activePowerControl text,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on battery (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS load (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    loadType text,
    p0 double precision,
    q0 double precision,
    p double precision,
    q double precision,
    position text,
    bus VARCHAR(255),
    connectableBus VARCHAR(255),
    loadDetail text,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on load (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS shuntCompensator (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    linearModel text,
    nonLinearModel text,
    sectionCount int,
    p double precision,
    q double precision,
    position text,
    bus VARCHAR(255),
    connectableBus VARCHAR(255),
    regulatingTerminal text,
    voltageRegulatorOn boolean,
    targetV double precision,
    targetDeadband double precision,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on shuntCompensator (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS vscConverterStation (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    lossFactor real,
    voltageRegulatorOn boolean,
    reactivePowerSetPoint double precision,
    voltageSetPoint double precision,
    minMaxReactiveLimits text,
    reactiveCapabilityCurve text,
    p double precision,
    q double precision,
    position text,
    bus VARCHAR(255),
    connectableBus VARCHAR(255),
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on vscConverterStation (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS lccConverterStation (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    powerFactor real,
    lossFactor real,
    p double precision,
    q double precision,
    position text,
    bus VARCHAR(255),
    connectableBus VARCHAR(255),
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on lccConverterStation (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS staticVarCompensator (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    bMin double precision,
    bMax double precision,
    voltageSetPoint double precision,
    reactivePowerSetPoint double precision,
    regulationMode text,
    p double precision,
    q double precision,
    position text,
    bus VARCHAR(255),
    connectableBus VARCHAR(255),
    regulatingTerminal text,
    voltagePerReactivePowerControl text,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on staticVarCompensator (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS busbarSection (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    position text,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on busbarSection (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS switch (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    kind text,
    node1 int,
    node2 int,
    open boolean,
    retained boolean,
    fictitious boolean,
    bus1 VARCHAR(255),
    bus2 VARCHAR(255),
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on switch (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS twoWindingsTransformer (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId1 VARCHAR(255),
    voltageLevelId2 VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node1 int,
    node2 int,
    r double precision,
    x double precision,
    g double precision,
    b double precision,
    ratedU1 double precision,
    ratedU2 double precision,
    ratedS double precision,
    p1 double precision,
    q1 double precision,
    p2 double precision,
    q2 double precision,
    position1 text,
    position2 text,
    phaseTapChanger text,
    ratioTapChanger text,
    bus1 VARCHAR(255),
    bus2 VARCHAR(255),
    connectableBus1 VARCHAR(255),
    connectableBus2 VARCHAR(255),
    currentLimits1 text,
    currentLimits2 text,
    activePowerLimits1 text,
    activePowerLimits2 text,
    apparentPowerLimits1 text,
    apparentPowerLimits2 text,
    phaseAngleClock text,
    branchStatus VARCHAR(50),
    cgmesTapChangers text,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on twoWindingsTransformer (networkUuid, variantNum, voltageLevelId1);
create index on twoWindingsTransformer (networkUuid, variantNum, voltageLevelId2);


CREATE TABLE IF NOT EXISTS threeWindingsTransformer (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId1 VARCHAR(255),
    voltageLevelId2 VARCHAR(255),
    voltageLevelId3 VARCHAR(255),
    node1 int,
    node2 int,
    node3 int,
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    ratedU0 double precision,
    p1 double precision,
    q1 double precision,
    r1 double precision,
    x1 double precision,
    g1 double precision,
    b1 double precision,
    ratedU1 double precision,
    ratedS1 double precision,
    phaseTapChanger1 text,
    ratioTapChanger1 text,
    p2 double precision,
    q2 double precision,
    r2 double precision,
    x2 double precision,
    g2 double precision,
    b2 double precision,
    ratedU2 double precision,
    ratedS2 double precision,
    phaseTapChanger2 text,
    ratioTapChanger2 text,
    p3 double precision,
    q3 double precision,
    r3 double precision,
    x3 double precision,
    g3 double precision,
    b3 double precision,
    ratedU3 double precision,
    ratedS3 double precision,
    phaseTapChanger3 text,
    ratioTapChanger3 text,
    position1 text,
    position2 text,
    position3 text,
    currentLimits1 text,
    currentLimits2 text,
    currentLimits3 text,
    activePowerLimits1 text,
    activePowerLimits2 text,
    activePowerLimits3 text,
    apparentPowerLimits1 text,
    apparentPowerLimits2 text,
    apparentPowerLimits3 text,
    bus1 VARCHAR(255),
    connectableBus1 VARCHAR(255),
    bus2 VARCHAR(255),
    connectableBus2 VARCHAR(255),
    bus3 VARCHAR(255),
    connectableBus3 VARCHAR(255),
    phaseAngleClock text,
    branchStatus VARCHAR(50),
    cgmesTapChangers text,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on threeWindingsTransformer (networkUuid, variantNum, voltageLevelId1);
create index on threeWindingsTransformer (networkUuid, variantNum, voltageLevelId2);
create index on threeWindingsTransformer (networkUuid, variantNum, voltageLevelId3);


CREATE TABLE IF NOT EXISTS temporaryLimit (
    equipmentId VARCHAR(255) NOT NULL,
    equipmentType VARCHAR(255),
    networkUuid VARCHAR(255) NOT NULL,
    variantNum int NOT NULL,
    side int NOT NULL,
    limitType VARCHAR(50) NOT NULL,
    name VARCHAR(255),
    value double precision,
    acceptableDuration int NOT NULL,
    fictitious boolean,
    PRIMARY KEY (networkUuid, variantNum, equipmentId, side, acceptableDuration, limitType)
);

create index on temporaryLimit (networkUuid, variantNum, equipmentId);


CREATE TABLE IF NOT EXISTS line (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId1 VARCHAR(255),
    voltageLevelId2 VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node1 int,
    node2 int,
    r double precision,
    x double precision,
    g1 double precision,
    b1 double precision,
    g2 double precision,
    b2 double precision,
    p1 double precision,
    q1 double precision,
    p2 double precision,
    q2 double precision,
    position1 text,
    position2 text,
    bus1 VARCHAR(255),
    bus2 VARCHAR(255),
    connectableBus1 VARCHAR(255),
    connectableBus2 VARCHAR(255),
    mergedXnode text,
    currentLimits1 text, -- TODO this column will soon be obsolete
    currentLimits2 text, -- TODO this column will soon be obsolete
    activePowerLimits1 text, -- TODO this column will soon be obsolete
    activePowerLimits2 text, -- TODO this column will soon be obsolete
    apparentPowerLimits1 text, -- TODO this column will soon be obsolete
    apparentPowerLimits2 text, -- TODO this column will soon be obsolete
    -- The old permanent current values can be transfered from the JSON columns to their new double precision columns with this query :
    ----------------------------------------------------------------------------------------------
    -- update line l
    -- set
    --   permanentCurrentLimit1 = custom.extractedPermanentCurrentLimit1,
    --   permanentCurrentLimit2 = custom.extractedPermanentCurrentLimit2,
    --   permanentActivePowerLimit1 = custom.extractedPermanentActivePowerLimit1,
    --   permanentActivePowerLimit2 = custom.extractedPermanentActivePowerLimit2,
    --   permanentApparentPowerLimit1 = custom.extractedPermanentApparentPowerLimit1,
    --   permanentApparentPowerLimit2 = custom.extractedPermanentApparentPowerLimit2
    -- from (
    --   select
    --     id,
    --     cast(substring(currentLimits1, length('{"permanentLimit":')+1, position(',"temporaryLimits' in currentLimits1)-length('{"permanentLimit":')-1) as double precision) as extractedPermanentCurrentLimit1,
    --     cast(substring(currentLimits2, length('{"permanentLimit":')+1, position(',"temporaryLimits' in currentLimits2)-length('{"permanentLimit":')-1) as double precision) as extractedPermanentCurrentLimit2,
    --     cast(substring(activePowerLimits1, length('{"permanentLimit":')+1, position(',"temporaryLimits' in activePowerLimits1)-length('{"permanentLimit":')-1) as double precision) as extractedPermanentActivePowerLimit1,
    --     cast(substring(activePowerLimits2, length('{"permanentLimit":')+1, position(',"temporaryLimits' in activePowerLimits2)-length('{"permanentLimit":')-1) as double precision) as extractedPermanentActivePowerLimit2,
    --     cast(substring(apparentPowerLimits1, length('{"permanentLimit":')+1, position(',"temporaryLimits' in apparentPowerLimits1)-length('{"permanentLimit":')-1) as double precision) as extractedPermanentApparentPowerLimit1,
    --     cast(substring(apparentPowerLimits2, length('{"permanentLimit":')+1, position(',"temporaryLimits' in apparentPowerLimits2)-length('{"permanentLimit":')-1) as double precision) as extractedPermanentApparentPowerLimit2
    --   from line
    -- ) custom
    -- where custom.id = l.id;
    ----------------------------------------------------------------------------------------------
    permanentCurrentLimit1 double precision,
    permanentCurrentLimit2 double precision,
    permanentActivePowerLimit1 double precision,
    permanentActivePowerLimit2 double precision,
    permanentApparentPowerLimit1 double precision,
    permanentApparentPowerLimit2 double precision,
    branchStatus VARCHAR(50),
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on line (networkUuid, variantNum, voltageLevelId1);
create index on line (networkUuid, variantNum, voltageLevelId2);

CREATE TABLE IF NOT EXISTS hvdcLine (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    r double precision,
    convertersMode text,
    nominalV double precision,
    activePowerSetpoint double precision,
    maxP double precision,
    converterStationId1 VARCHAR(255),
    converterStationId2 VARCHAR(255),
    hvdcAngleDroopActivePowerControl text,
    hvdcOperatorActivePowerRange text,
    PRIMARY KEY (networkUuid, variantNum, id)
);

CREATE TABLE IF NOT EXISTS danglingLine (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    node int,
    p0 double precision,
    q0 double precision,
    r double precision,
    x double precision,
    g double precision,
    b double precision,
    generation text,
    ucteXNodeCode VARCHAR(255),
    currentLimits text,
    activePowerLimits text,
    apparentPowerLimits text,
    p double precision,
    q double precision,
    position text,
    bus VARCHAR(255),
    connectableBus VARCHAR(255),
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on danglingLine (networkUuid, variantNum, voltageLevelId);

CREATE TABLE IF NOT EXISTS configuredBus (
    networkUuid uuid,
    variantNum int,
    id VARCHAR(255),
    voltageLevelId VARCHAR(255),
    name VARCHAR(255),
    fictitious boolean,
    properties text,
    aliasesWithoutType text,
    aliasByType text,
    v double precision,
    angle double precision,
    PRIMARY KEY (networkUuid, variantNum, id)
);
create index on configuredBus (networkUuid, variantNum, voltageLevelId);


