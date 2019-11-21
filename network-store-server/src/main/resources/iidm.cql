-- for single node deployments
CREATE KEYSPACE IF NOT EXISTS iidm WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
-- for multi datacenters deployments
-- CREATE KEYSPACE IF NOT EXISTS iidm WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'dc1' : 1, 'dc2' : 1 };

CREATE TABLE IF NOT EXISTS iidm.network (
    uuid uuid,
    id text,
    properties frozen<map<text, text>>,
    caseDate timestamp,
    forecastDistance int,
    sourceFormat text,
    PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS iidm.substation (
    networkUuid uuid,
    id text,
    name text,
    properties frozen<map<text, text>>,
    country text,
    tso text,
    PRIMARY KEY (networkUuid, id)
);

CREATE TABLE IF NOT EXISTS iidm.voltageLevel (
    networkUuid uuid,
    id text,
    substationId text,
    name text,
    properties frozen<map<text, text>>,
    nominalV double,
    lowVoltageLimit double,
    highVoltageLimit double,
    topologyKind text,
    PRIMARY KEY (networkUuid, id, substationId)
);

CREATE MATERIALIZED VIEW iidm.voltageLevelBySubstation AS
    SELECT networkUuid, id, substationId, name, properties, nominalV, lowVoltageLimit, highVoltageLimit, topologyKind
    FROM iidm.voltageLevel
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND substationId IS NOT NULL
    PRIMARY KEY (networkUuid, substationId, id);

CREATE TYPE iidm.connectablePosition (
    label text,
    orderNum int,
    direction text,
);

CREATE TABLE IF NOT EXISTS iidm.generator (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
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
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW iidm.generatorByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, properties, node, energySource, minP, maxP, voltageRegulatorOn, targetP, targetQ, targetV, ratedS, p, q, position
    FROM iidm.generator
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS iidm.load (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    properties frozen<map<text, text>>,
    node int,
    loadType text,
    p0 double,
    q0 double,
    p double,
    q double,
    position iidm.connectablePosition,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW iidm.loadByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, properties, node, loadType, p0, q0, p, q, position
    FROM iidm.load
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS iidm.shuntCompensator (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    properties frozen<map<text, text>>,
    node int,
    bPerSection double,
    maximumSectionCount int,
    currentSectionCount int,
    p double,
    q double,
    position iidm.connectablePosition,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW iidm.shuntCompensatorByVoltageLevel AS
SELECT networkUuid, id, voltageLevelId, name, properties, node, bPerSection, maximumSectionCount, currentSectionCount, p, q, position
FROM iidm.shuntCompensator
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TYPE iidm.busbarSectionPosition (
    busbarIndex int,
    sectionIndex int,
);

CREATE TABLE IF NOT EXISTS iidm.busbarSection (
    networkUuid uuid,
    id text,
    voltageLevelId text,
    name text,
    properties frozen<map<text, text>>,
    node int,
    position frozen<iidm.busbarSectionPosition>,
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW iidm.busbarSectionByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, properties, node, position
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
    PRIMARY KEY (networkUuid, id, voltageLevelId)
);

CREATE MATERIALIZED VIEW iidm.switchByVoltageLevel AS
    SELECT networkUuid, id, voltageLevelId, name, properties, kind, node1, node2, open, retained, fictitious
    FROM iidm.switch
    WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId IS NOT NULL
        PRIMARY KEY (networkUuid, voltageLevelId, id);

CREATE TABLE IF NOT EXISTS iidm.twoWindingsTransformer (
    networkUuid uuid,
    id text,
    voltageLevelId1 text,
    voltageLevelId2 text,
    name text,
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
    PRIMARY KEY (networkUuid, id)
);

CREATE MATERIALIZED VIEW iidm.twoWindingsTransformerByVoltageLevel1 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, name, properties, node1, node2, r, x, g, b, ratedU1, ratedU2, p1, q1, p2, q2, position1, position2
FROM iidm.twoWindingsTransformer
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId1 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId1, id);

CREATE MATERIALIZED VIEW iidm.twoWindingsTransformerByVoltageLevel2 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, name, properties, node1, node2, r, x, g, b, ratedU1, ratedU2, p1, q1, p2, q2, position1, position2
FROM iidm.twoWindingsTransformer
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId2 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId2, id);

CREATE TABLE IF NOT EXISTS iidm.line (
    networkUuid uuid,
    id text,
    voltageLevelId1 text,
    voltageLevelId2 text,
    name text,
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
    PRIMARY KEY (networkUuid, id)
);

CREATE MATERIALIZED VIEW iidm.lineByVoltageLevel1 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, name, properties, node1, node2, r, x, g1, b1, g2, b2, p1, q1, p2, q2, position1, position2
FROM iidm.line
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId1 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId1, id);

CREATE MATERIALIZED VIEW iidm.lineByVoltageLevel2 AS
SELECT networkUuid, id, voltageLevelId1, voltageLevelId2, name, properties, node1, node2, r, x, g1, b1, g2, b2, p1, q1, p2, q2, position1, position2
FROM iidm.line
WHERE networkUuid IS NOT NULL AND id IS NOT NULL AND voltageLevelId2 IS NOT NULL
    PRIMARY KEY (networkUuid, voltageLevelId2, id);
