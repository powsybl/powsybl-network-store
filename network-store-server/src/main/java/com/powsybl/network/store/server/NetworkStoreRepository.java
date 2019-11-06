/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.datastax.driver.core.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.powsybl.network.store.server.CassandraConstants.KEYSPACE_IIDM;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Repository
public class NetworkStoreRepository {

    private static final int BATCH_SIZE = 1000;

    @Autowired
    private Session session;

    private PreparedStatement psInsertNetwork;
    private PreparedStatement psInsertSubstation;
    private PreparedStatement psInsertVoltageLevel;
    private PreparedStatement psInsertGenerator;
    private PreparedStatement psInsertLoad;
    private PreparedStatement psInsertShuntCompensator;
    private PreparedStatement psInsertBusbarSection;
    private PreparedStatement psInsertSwitch;
    private PreparedStatement psInsertTwoWindingsTransformer;
    private PreparedStatement psInsertLine;

    @PostConstruct
    void prepareStatements() {
        psInsertNetwork = session.prepare(insertInto(KEYSPACE_IIDM, "network")
                .value("id", bindMarker())
                .value("properties", bindMarker())
                .value("caseDate", bindMarker())
                .value("forecastDistance", bindMarker())
                .value("sourceFormat", bindMarker()));
        psInsertSubstation = session.prepare(insertInto(KEYSPACE_IIDM, "substation")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
                .value("country", bindMarker())
                .value("tso", bindMarker()));
        psInsertVoltageLevel = session.prepare(insertInto(KEYSPACE_IIDM, "voltageLevel")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("substationId", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
                .value("nominalV", bindMarker())
                .value("lowVoltageLimit", bindMarker())
                .value("highVoltageLimit", bindMarker())
                .value("topologyKind", bindMarker()));
        psInsertGenerator = session.prepare(insertInto(KEYSPACE_IIDM, "generator")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
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
                .value("position", bindMarker()));
        psInsertLoad = session.prepare(insertInto(KEYSPACE_IIDM, "load")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
                .value("node", bindMarker())
                .value("loadType", bindMarker())
                .value("p0", bindMarker())
                .value("q0", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker()));
        psInsertShuntCompensator = session.prepare(insertInto(KEYSPACE_IIDM, "shuntCompensator")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
                .value("node", bindMarker())
                .value("bPerSection", bindMarker())
                .value("maximumSectionCount", bindMarker())
                .value("currentSectionCount", bindMarker())
                .value("p", bindMarker())
                .value("q", bindMarker())
                .value("position", bindMarker()));
        psInsertBusbarSection = session.prepare(insertInto(KEYSPACE_IIDM, "busbarSection")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
                .value("node", bindMarker())
                .value("position", bindMarker()));
        psInsertSwitch = session.prepare(insertInto(KEYSPACE_IIDM, "switch")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
                .value("node1", bindMarker())
                .value("node2", bindMarker())
                .value("open", bindMarker())
                .value("retained", bindMarker())
                .value("fictitious", bindMarker())
                .value("kind", bindMarker()));
        psInsertTwoWindingsTransformer = session.prepare(insertInto(KEYSPACE_IIDM, "twoWindingsTransformer")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId1", bindMarker())
                .value("voltageLevelId2", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
                .value("node1", bindMarker())
                .value("node2", bindMarker())
                .value("r", bindMarker())
                .value("x", bindMarker())
                .value("g", bindMarker())
                .value("b", bindMarker())
                .value("ratedU1", bindMarker())
                .value("ratedU2", bindMarker())
                .value("p1", bindMarker())
                .value("q1", bindMarker())
                .value("p2", bindMarker())
                .value("q2", bindMarker())
                .value("position1", bindMarker())
                .value("position2", bindMarker()));
        psInsertLine = session.prepare(insertInto(KEYSPACE_IIDM, "line")
                .value("networkId", bindMarker())
                .value("id", bindMarker())
                .value("voltageLevelId1", bindMarker())
                .value("voltageLevelId2", bindMarker())
                .value("name", bindMarker())
                .value("properties", bindMarker())
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
                .value("position2", bindMarker()));
    }

    // network

    public List<Resource<NetworkAttributes>> getNetworks() {
        ResultSet resultSet = session.execute(select("id",
                                                     "properties",
                                                     "caseDate",
                                                     "forecastDistance",
                                                     "sourceFormat")
                .from(KEYSPACE_IIDM, "network"));
        List<Resource<NetworkAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.networkBuilder()
                    .id(row.getString(0))
                    .attributes(NetworkAttributes.builder()
                            .properties(row.getMap(1, String.class, String.class))
                            .caseDate(new DateTime(row.getTimestamp(2)))
                            .forecastDistance(row.getInt(3))
                            .sourceFormat(row.getString(4))
                            .build())
                    .build());
        }
        return resources;
    }

    public Optional<Resource<NetworkAttributes>> getNetwork(String id) {
        ResultSet resultSet = session.execute(select("properties",
                                                     "caseDate",
                                                     "forecastDistance",
                                                     "sourceFormat")
                .from(KEYSPACE_IIDM, "network")
                .where(eq("id", id)));
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.networkBuilder()
                    .id(id)
                    .attributes(NetworkAttributes.builder()
                            .properties(one.getMap(0, String.class, String.class))
                            .caseDate(new DateTime(one.getTimestamp(1)))
                            .forecastDistance(one.getInt(2))
                            .sourceFormat(one.getString(3))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public void createNetworks(List<Resource<NetworkAttributes>> resources) {
        for (List<Resource<NetworkAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<NetworkAttributes> resource : subresources) {
                batch.add(psInsertNetwork.bind(
                        resource.getId(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getCaseDate().toDate(),
                        resource.getAttributes().getForecastDistance(),
                        resource.getAttributes().getSourceFormat()
                        ));
            }
            session.execute(batch);
        }
    }

    public void deleteNetwork(String id) {
        BatchStatement batch = new BatchStatement();
        batch.add(delete().from("network").where(eq("id", id)));
        batch.add(delete().from("substation").where(eq("networkId", id)));
        batch.add(delete().from("voltageLevel").where(eq("networkId", id)));
        batch.add(delete().from("busbarSection").where(eq("networkId", id)));
        batch.add(delete().from("switch").where(eq("networkId", id)));
        batch.add(delete().from("generator").where(eq("networkId", id)));
        batch.add(delete().from("load").where(eq("networkId", id)));
        batch.add(delete().from("twoWindingsTransformer").where(eq("networkId", id)));
        batch.add(delete().from("line").where(eq("networkId", id)));
        session.execute(batch);
    }

    // substation

    public List<Resource<SubstationAttributes>> getSubstations(String networkId) {
        ResultSet resultSet = session.execute(select("id", "name", "properties", "country", "tso").from(KEYSPACE_IIDM, "substation")
                .where(eq("networkId", networkId)));
        List<Resource<SubstationAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.substationBuilder()
                    .id(row.getString(0))
                    .attributes(SubstationAttributes.builder()
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .country(Country.valueOf(row.getString(3)))
                            .tso(row.getString(4))
                            .build())
                    .build());
        }
        return resources;
    }

    public Optional<Resource<SubstationAttributes>> getSubstation(String networkId, String substationId) {
        ResultSet resultSet = session.execute(select("name",
                                                     "properties",
                                                     "country",
                                                     "tso")
                .from(KEYSPACE_IIDM, "substation")
                .where(eq("networkId", networkId)).and(eq("id", substationId)));
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.substationBuilder()
                    .id(substationId)
                    .attributes(SubstationAttributes.builder()
                            .name(one.getString(0))
                            .properties(one.getMap(1, String.class, String.class))
                            .country(Country.valueOf(one.getString(2)))
                            .tso(one.getString(3))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public void createSubstations(String networkId, List<Resource<SubstationAttributes>> resources) {
        for (List<Resource<SubstationAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<SubstationAttributes> resource : subresources) {
                batch.add(psInsertSubstation.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getCountry().toString(),
                        resource.getAttributes().getTso()
                        ));
            }
            session.execute(batch);
        }
    }

    // voltage level

    public void createVoltageLevels(String networkId, List<Resource<VoltageLevelAttributes>> resources) {
        for (List<Resource<VoltageLevelAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<VoltageLevelAttributes> resource : subresources) {
                batch.add(psInsertVoltageLevel.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getSubstationId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getNominalV(),
                        resource.getAttributes().getLowVoltageLimit(),
                        resource.getAttributes().getHighVoltageLimit(),
                        resource.getAttributes().getTopologyKind().toString()
                        ));
            }
            session.execute(batch);
        }
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(String networkId, String substationId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "name",
                                                     "properties",
                                                     "nominalV",
                                                     "lowVoltageLimit",
                                                     "highVoltageLimit",
                                                     "topologyKind")
                .from(KEYSPACE_IIDM, "voltageLevelBySubstation")
                .where(eq("networkId", networkId)).and(eq("substationId", substationId)));
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
                            .build())
                    .build());
        }
        return resources;
    }

    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(String networkId, String voltageLevelId) {
        ResultSet resultSet = session.execute(select("substationId",
                                                     "name",
                                                     "properties",
                                                     "nominalV",
                                                     "lowVoltageLimit",
                                                     "highVoltageLimit",
                                                     "topologyKind")
                .from(KEYSPACE_IIDM, "voltageLevel")
                .where(eq("networkId", networkId)).and(eq("id", voltageLevelId)));
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
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(String networkId) {
        ResultSet resultSet = session.execute(select("id",
                "substationId",
                "name",
                "properties",
                "nominalV",
                "lowVoltageLimit",
                "highVoltageLimit",
                "topologyKind")
                .from(KEYSPACE_IIDM, "voltageLevel")
                .where(eq("networkId", networkId)));
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
                            .build())
                    .build());
        }
        return resources;
    }

    // generator

    public void createGenerators(String networkId, List<Resource<GeneratorAttributes>> resources) {
        for (List<Resource<GeneratorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<GeneratorAttributes> resource : subresources) {
                batch.add(psInsertGenerator.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
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
                        resource.getAttributes().getPosition()
                        ));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<GeneratorAttributes>> getGenerator(String networkId, String generatorId) {
        ResultSet resultSet = session.execute(select("voltageLevelId",
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
                                                     "position")
                .from(KEYSPACE_IIDM, "generator")
                .where(eq("networkId", networkId)).and(eq("id", generatorId)));
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.generatorBuilder()
                    .id(generatorId)
                    .attributes(GeneratorAttributes.builder()
                            .voltageLevelId(one.getString(0))
                            .name(one.getString(1))
                            .properties(one.getMap(2, String.class, String.class))
                            .node(one.getInt(3))
                            .energySource(EnergySource.valueOf(one.getString(4)))
                            .minP(one.getDouble(5))
                            .maxP(one.getDouble(6))
                            .voltageRegulatorOn(one.getBool(7))
                            .targetP(one.getDouble(8))
                            .targetQ(one.getDouble(9))
                            .targetV(one.getDouble(10))
                            .ratedS(one.getDouble(11))
                            .p(one.getDouble(12))
                            .q(one.getDouble(13))
                            .position(one.get(14, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<GeneratorAttributes>> getGenerators(String networkId) {
        ResultSet resultSet = session.execute(select("id",
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
                                                     "position")
                .from(KEYSPACE_IIDM, "generator")
                .where(eq("networkId", networkId)));
        List<Resource<GeneratorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.generatorBuilder()
                    .id(row.getString(0))
                    .attributes(GeneratorAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.getInt(4))
                            .energySource(EnergySource.valueOf(row.getString(5)))
                            .minP(row.getDouble(6))
                            .maxP(row.getDouble(7))
                            .voltageRegulatorOn(row.getBool(8))
                            .targetP(row.getDouble(9))
                            .targetQ(row.getDouble(10))
                            .targetV(row.getDouble(11))
                            .ratedS(row.getDouble(12))
                            .p(row.getDouble(13))
                            .q(row.getDouble(14))
                            .position(row.get(15, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(String networkId, String voltageLevelId) {
        ResultSet resultSet = session.execute(select("id",
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
                                                     "position")
                .from(KEYSPACE_IIDM, "generatorByVoltageLevel")
                .where(eq("networkId", networkId)).and(eq("voltageLevelId", voltageLevelId)));
        List<Resource<GeneratorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.generatorBuilder()
                    .id(row.getString(0))
                    .attributes(GeneratorAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.getInt(3))
                            .energySource(EnergySource.valueOf(row.getString(4)))
                            .minP(row.getDouble(5))
                            .maxP(row.getDouble(6))
                            .voltageRegulatorOn(row.getBool(7))
                            .targetP(row.getDouble(8))
                            .targetQ(row.getDouble(9))
                            .targetV(row.getDouble(10))
                            .ratedS(row.getDouble(11))
                            .p(row.getDouble(12))
                            .q(row.getDouble(13))
                            .position(row.get(14, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    // load

    public void createLoads(String networkId, List<Resource<LoadAttributes>> resources) {
        for (List<Resource<LoadAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<LoadAttributes> resource : subresources) {
                batch.add(psInsertLoad.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getLoadType().toString(),
                        resource.getAttributes().getP0(),
                        resource.getAttributes().getQ0(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition()
                        ));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<LoadAttributes>> getLoad(String networkId, String loadId) {
        ResultSet resultSet = session.execute(select("voltageLevelId",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "loadType",
                                                     "p0",
                                                     "q0",
                                                     "p",
                                                     "q",
                                                     "position")
                .from(KEYSPACE_IIDM, "load")
                .where(eq("networkId", networkId)).and(eq("id", loadId)));
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.loadBuilder()
                    .id(loadId)
                    .attributes(LoadAttributes.builder()
                            .voltageLevelId(one.getString(0))
                            .name(one.getString(1))
                            .properties(one.getMap(2, String.class, String.class))
                            .node(one.getInt(3))
                            .loadType(LoadType.valueOf(one.getString(4)))
                            .p0(one.getDouble(5))
                            .q0(one.getDouble(6))
                            .p(one.getDouble(7))
                            .q(one.getDouble(8))
                            .position(one.get(9, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<LoadAttributes>> getLoads(String networkId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "voltageLevelId",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "loadType",
                                                     "p0",
                                                     "q0",
                                                     "p",
                                                     "q",
                                                     "position")
                .from(KEYSPACE_IIDM, "load")
                .where(eq("networkId", networkId)));
        List<Resource<LoadAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.loadBuilder()
                    .id(row.getString(0))
                    .attributes(LoadAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.getInt(4))
                            .loadType(LoadType.valueOf(row.getString(5)))
                            .p0(row.getDouble(6))
                            .q0(row.getDouble(7))
                            .p(row.getDouble(8))
                            .q(row.getDouble(9))
                            .position(row.get(10, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<LoadAttributes>> getVoltageLevelLoads(String networkId, String voltageLevelId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "loadType",
                                                     "p0",
                                                     "q0",
                                                     "p",
                                                     "q",
                                                     "position")
                .from(KEYSPACE_IIDM, "loadByVoltageLevel")
                .where(eq("networkId", networkId)).and(eq("voltageLevelId", voltageLevelId)));
        List<Resource<LoadAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.loadBuilder()
                    .id(row.getString(0))
                    .attributes(LoadAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.getInt(3))
                            .loadType(LoadType.valueOf(row.getString(4)))
                            .p0(row.getDouble(5))
                            .q0(row.getDouble(6))
                            .p(row.getDouble(7))
                            .q(row.getDouble(8))
                            .position(row.get(9, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    // shunt compensator

    public void createShuntCompensators(String networkId, List<Resource<ShuntCompensatorAttributes>> resources) {
        for (List<Resource<ShuntCompensatorAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<ShuntCompensatorAttributes> resource : subresources) {
                batch.add(psInsertShuntCompensator.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getBPerSection(),
                        resource.getAttributes().getMaximumSectionCount(),
                        resource.getAttributes().getCurrentSectionCount(),
                        resource.getAttributes().getP(),
                        resource.getAttributes().getQ(),
                        resource.getAttributes().getPosition()
                        ));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(String networkId, String shuntCompensatorId) {
        ResultSet resultSet = session.execute(select("voltageLevelId",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "bPerSection",
                                                     "maximumSectionCount",
                                                     "currentSectionCount",
                                                     "p",
                                                     "q",
                                                     "position")
                .from(KEYSPACE_IIDM, "shuntCompensator")
                .where(eq("networkId", networkId)).and(eq("id", shuntCompensatorId)));
        Row row = resultSet.one();
        if (row != null) {
            return Optional.of(Resource.shuntCompensatorBuilder()
                    .id(shuntCompensatorId)
                    .attributes(ShuntCompensatorAttributes.builder()
                            .voltageLevelId(row.getString(0))
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.getInt(3))
                            .bPerSection(row.getDouble(4))
                            .maximumSectionCount(row.getInt(5))
                            .currentSectionCount(row.getInt(6))
                            .p(row.getDouble(7))
                            .q(row.getDouble(8))
                            .position(row.get(9, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(String networkId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "voltageLevelId",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "bPerSection",
                                                     "maximumSectionCount",
                                                     "currentSectionCount",
                                                     "p",
                                                     "q",
                                                     "position")
                .from(KEYSPACE_IIDM, "shuntCompensator")
                .where(eq("networkId", networkId)));
        List<Resource<ShuntCompensatorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.shuntCompensatorBuilder()
                    .id(row.getString(0))
                    .attributes(ShuntCompensatorAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node(row.getInt(4))
                            .bPerSection(row.getDouble(5))
                            .maximumSectionCount(row.getInt(6))
                            .currentSectionCount(row.getInt(7))
                            .p(row.getDouble(8))
                            .q(row.getDouble(9))
                            .position(row.get(10, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(String networkId, String voltageLevelId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "bPerSection",
                                                     "maximumSectionCount",
                                                     "currentSectionCount",
                                                     "p",
                                                     "q",
                                                     "position")
                .from(KEYSPACE_IIDM, "shuntCompensatorByVoltageLevel")
                .where(eq("networkId", networkId)).and(eq("voltageLevelId", voltageLevelId)));
        List<Resource<ShuntCompensatorAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.shuntCompensatorBuilder()
                    .id(row.getString(0))
                    .attributes(ShuntCompensatorAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.getInt(3))
                            .bPerSection(row.getDouble(4))
                            .maximumSectionCount(row.getInt(5))
                            .currentSectionCount(row.getInt(6))
                            .p(row.getDouble(7))
                            .q(row.getDouble(8))
                            .position(row.get(9, ConnectablePositionAttributes.class))
                            .build())
                    .build());
        }
        return resources;
    }

    // busbar section

    public void createBusbarSections(String networkId, List<Resource<BusbarSectionAttributes>> resources) {
        for (List<Resource<BusbarSectionAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<BusbarSectionAttributes> resource : subresources) {
                batch.add(psInsertBusbarSection.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getNode(),
                        resource.getAttributes().getPosition()
                        ));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(String networkId, String busbarSectionId) {
        ResultSet resultSet = session.execute(select("voltageLevelId",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "position")
                .from(KEYSPACE_IIDM, "busbarSection")
                .where(eq("networkId", networkId)).and(eq("id", busbarSectionId)));
        Row row = resultSet.one();
        if (row != null) {
            return Optional.of(Resource.busbarSectionBuilder()
                    .id(busbarSectionId)
                    .attributes(BusbarSectionAttributes.builder()
                            .voltageLevelId(row.getString(0))
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .node(row.getInt(3))
                            .position(row.get(9, BusbarSectionPositionAttributes.class))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<BusbarSectionAttributes>> getBusbarSections(String networkId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "voltageLevelId",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "position")
                .from(KEYSPACE_IIDM, "busbarSection")
                .where(eq("networkId", networkId)));
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
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(String networkId, String voltageLevelId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "name",
                                                     "properties",
                                                     "node",
                                                     "position")
                .from(KEYSPACE_IIDM, "busbarSectionByVoltageLevel")
                .where(eq("networkId", networkId)).and(eq("voltageLevelId", voltageLevelId)));
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
                            .build())
                    .build());
        }
        return resources;
    }

    // switch

    public void createSwitches(String networkId, List<Resource<SwitchAttributes>> resources) {
        for (List<Resource<SwitchAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<SwitchAttributes> resource : subresources) {
                batch.add(psInsertSwitch.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getNode1(),
                        resource.getAttributes().getNode2(),
                        resource.getAttributes().isOpen(),
                        resource.getAttributes().isRetained(),
                        resource.getAttributes().isFictitious(),
                        resource.getAttributes().getKind().toString()
                        ));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<SwitchAttributes>> getSwitch(String networkId, String switchId) {
        ResultSet resultSet = session.execute(select("voltageLevelId",
                                                     "name",
                                                     "properties",
                                                     "kind",
                                                     "node1",
                                                     "node2",
                                                     "open",
                                                     "retained",
                                                     "fictitious")
                .from(KEYSPACE_IIDM, "switch")
                .where(eq("networkId", networkId)));
        Row row = resultSet.one();
        if (row != null) {
            return Optional.of(Resource.switchBuilder()
                    .id(switchId)
                    .attributes(SwitchAttributes.builder()
                            .voltageLevelId(row.getString(0))
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .kind(SwitchKind.valueOf(row.getString(3)))
                            .node1(row.getInt(4))
                            .node2(row.getInt(5))
                            .open(row.getBool(6))
                            .retained(row.getBool(7))
                            .fictitious(row.getBool(8))
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<SwitchAttributes>> getSwitches(String networkId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "voltageLevelId",
                                                     "name",
                                                     "properties",
                                                     "kind",
                                                     "node1",
                                                     "node2",
                                                     "open",
                                                     "retained",
                                                     "fictitious")
                .from(KEYSPACE_IIDM, "switch")
                .where(eq("networkId", networkId)));
        List<Resource<SwitchAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.switchBuilder()
                    .id(row.getString(0))
                    .attributes(SwitchAttributes.builder()
                            .voltageLevelId(row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .kind(SwitchKind.valueOf(row.getString(4)))
                            .node1(row.getInt(5))
                            .node2(row.getInt(6))
                            .open(row.getBool(7))
                            .retained(row.getBool(8))
                            .fictitious(row.getBool(9))
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(String networkId, String voltageLevelId) {
        ResultSet resultSet = session.execute(select("id",
                                                     "name",
                                                     "properties",
                                                     "kind",
                                                     "node1",
                                                     "node2",
                                                     "open",
                                                     "retained",
                                                     "fictitious")
                .from(KEYSPACE_IIDM, "switchByVoltageLevel")
                .where(eq("networkId", networkId)).and(eq("voltageLevelId", voltageLevelId)));
        List<Resource<SwitchAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.switchBuilder()
                    .id(row.getString(0))
                    .attributes(SwitchAttributes.builder()
                            .voltageLevelId(voltageLevelId)
                            .name(row.getString(1))
                            .properties(row.getMap(2, String.class, String.class))
                            .kind(SwitchKind.valueOf(row.getString(3)))
                            .node1(row.getInt(4))
                            .node2(row.getInt(5))
                            .open(row.getBool(6))
                            .retained(row.getBool(7))
                            .fictitious(row.getBool(8))
                            .build())
                    .build());
        }
        return resources;
    }

    // 2 windings transformer

    public void createTwoWindingsTransformers(String networkId, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        for (List<Resource<TwoWindingsTransformerAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<TwoWindingsTransformerAttributes> resource : subresources) {
                batch.add(psInsertTwoWindingsTransformer.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId1(),
                        resource.getAttributes().getVoltageLevelId2(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
                        resource.getAttributes().getNode1(),
                        resource.getAttributes().getNode2(),
                        resource.getAttributes().getR(),
                        resource.getAttributes().getX(),
                        resource.getAttributes().getG(),
                        resource.getAttributes().getB(),
                        resource.getAttributes().getRatedU1(),
                        resource.getAttributes().getRatedU2(),
                        resource.getAttributes().getP1(),
                        resource.getAttributes().getQ1(),
                        resource.getAttributes().getP2(),
                        resource.getAttributes().getQ2(),
                        resource.getAttributes().getPosition1(),
                        resource.getAttributes().getPosition2()
                        ));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(String networkId, String twoWindingsTransformerId) {
        ResultSet resultSet = session.execute(select("voltageLevelId1",
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
                                                     "position2")
                .from(KEYSPACE_IIDM, "twoWindingsTransformer")
                .where(eq("networkId", networkId)).and(eq("id", twoWindingsTransformerId)));
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.twoWindingsTransformerBuilder()
                    .id(twoWindingsTransformerId)
                    .attributes(TwoWindingsTransformerAttributes.builder()
                            .voltageLevelId1(one.getString(0))
                            .voltageLevelId2(one.getString(1))
                            .name(one.getString(2))
                            .properties(one.getMap(3, String.class, String.class))
                            .node1(one.getInt(4))
                            .node2(one.getInt(5))
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
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(String networkId) {
        ResultSet resultSet = session.execute(select("id",
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
                                                     "position2")
                .from(KEYSPACE_IIDM, "twoWindingsTransformer")
                .where(eq("networkId", networkId)));
        List<Resource<TwoWindingsTransformerAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.twoWindingsTransformerBuilder()
                    .id(row.getString(0))
                    .attributes(TwoWindingsTransformerAttributes.builder()
                            .voltageLevelId1(row.getString(1))
                            .voltageLevelId2(row.getString(2))
                            .name(row.getString(3))
                            .properties(row.getMap(4, String.class, String.class))
                            .node1(row.getInt(5))
                            .node2(row.getInt(6))
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
                            .build())
                    .build());
        }
        return resources;
    }

    private List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(String networkId, Branch.Side side, String voltageLevelId) {
        ResultSet resultSet = session.execute(select("id",
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
                                                     "position2")
                .from(KEYSPACE_IIDM, "twoWindingsTransformerByVoltageLevel" + (side == Branch.Side.ONE ? 1 : 2))
                .where(eq("networkId", networkId)).and(eq("voltageLevelId" + (side == Branch.Side.ONE ? 1 : 2), voltageLevelId)));
        List<Resource<TwoWindingsTransformerAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.twoWindingsTransformerBuilder()
                    .id(row.getString(0))
                    .attributes(TwoWindingsTransformerAttributes.builder()
                            .voltageLevelId1(side == Branch.Side.ONE ? voltageLevelId : row.getString(1))
                            .voltageLevelId2(side == Branch.Side.TWO ? voltageLevelId : row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node1(row.getInt(4))
                            .node2(row.getInt(5))
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
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(String networkId, String voltageLevelId) {
        return ImmutableList.<Resource<TwoWindingsTransformerAttributes>>builder()
                .addAll(getVoltageLevelTwoWindingsTransformers(networkId, Branch.Side.ONE, voltageLevelId))
                .addAll(getVoltageLevelTwoWindingsTransformers(networkId, Branch.Side.TWO, voltageLevelId))
                .build();
    }

    // line

    public void createLines(String networkId, List<Resource<LineAttributes>> resources) {
        for (List<Resource<LineAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);
            for (Resource<LineAttributes> resource : subresources) {
                batch.add(psInsertLine.bind(
                        networkId,
                        resource.getId(),
                        resource.getAttributes().getVoltageLevelId1(),
                        resource.getAttributes().getVoltageLevelId2(),
                        resource.getAttributes().getName(),
                        resource.getAttributes().getProperties(),
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
                        resource.getAttributes().getPosition2()
                        ));
            }
            session.execute(batch);
        }
    }

    public Optional<Resource<LineAttributes>> getLine(String networkId, String lineId) {
        ResultSet resultSet = session.execute(select("voltageLevelId1",
                                                     "voltageLevelId2",
                                                     "name",
                                                     "properties",
                                                     "node1",
                                                     "node2",
                                                     "r",
                                                     "x",
                                                     "g1",
                                                     "b1",
                                                     "g1",
                                                     "b1",
                                                     "p1",
                                                     "q1",
                                                     "p2",
                                                     "q2",
                                                     "position1",
                                                     "position2")
                .from(KEYSPACE_IIDM, "line")
                .where(eq("networkId", networkId)).and(eq("id", lineId)));
        Row one = resultSet.one();
        if (one != null) {
            return Optional.of(Resource.lineBuilder()
                    .id(lineId)
                    .attributes(LineAttributes.builder()
                            .voltageLevelId1(one.getString(0))
                            .voltageLevelId2(one.getString(1))
                            .name(one.getString(2))
                            .properties(one.getMap(3, String.class, String.class))
                            .node1(one.getInt(4))
                            .node2(one.getInt(5))
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
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    public List<Resource<LineAttributes>> getLines(String networkId) {
        ResultSet resultSet = session.execute(select("id",
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
                                                     "position2")
                .from(KEYSPACE_IIDM, "line")
                .where(eq("networkId", networkId)));
        List<Resource<LineAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.lineBuilder()
                    .id(row.getString(0))
                    .attributes(LineAttributes.builder()
                            .voltageLevelId1(row.getString(1))
                            .voltageLevelId2(row.getString(2))
                            .name(row.getString(3))
                            .properties(row.getMap(4, String.class, String.class))
                            .node1(row.getInt(5))
                            .node2(row.getInt(6))
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
                            .build())
                    .build());
        }
        return resources;
    }

    private List<Resource<LineAttributes>> getVoltageLevelLines(String networkId, Branch.Side side, String voltageLevelId) {
        ResultSet resultSet = session.execute(select("id",
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
                                                     "position2")
                .from(KEYSPACE_IIDM, "lineByVoltageLevel" + (side == Branch.Side.ONE ? 1 : 2))
                .where(eq("networkId", networkId)).and(eq("voltageLevelId" + (side == Branch.Side.ONE ? 1 : 2), voltageLevelId)));
        List<Resource<LineAttributes>> resources = new ArrayList<>();
        for (Row row : resultSet) {
            resources.add(Resource.lineBuilder()
                    .id(row.getString(0))
                    .attributes(LineAttributes.builder()
                            .voltageLevelId1(side == Branch.Side.ONE ? voltageLevelId : row.getString(1))
                            .voltageLevelId2(side == Branch.Side.TWO ? voltageLevelId : row.getString(1))
                            .name(row.getString(2))
                            .properties(row.getMap(3, String.class, String.class))
                            .node1(row.getInt(4))
                            .node2(row.getInt(5))
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
                            .build())
                    .build());
        }
        return resources;
    }

    public List<Resource<LineAttributes>> getVoltageLevelLines(String networkId, String voltageLevelId) {
        return ImmutableList.<Resource<LineAttributes>>builder()
                .addAll(getVoltageLevelLines(networkId, Branch.Side.ONE, voltageLevelId))
                .addAll(getVoltageLevelLines(networkId, Branch.Side.TWO, voltageLevelId))
                .build();
    }

}
