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
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
            throw new PowsyblException(e);
        }
    }

    private Session session;

    private static final int BATCH_SIZE = 1000;

    private Supplier<java.sql.PreparedStatement> psCloneNetworkSupplier;

    private final Map<String, Supplier<java.sql.PreparedStatement>> clonePreparedStatementsSupplier = new LinkedHashMap<>();
    private final Map<String, PreparedStatement> insertPreparedStatements = new LinkedHashMap<>();
    private final Map<String, PreparedStatement> updatePreparedStatements = new LinkedHashMap<>();

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
    private static final String VARIANT_NUM = "variantNum";
    private static final String VARIANT_ID = "variantId";

    private static final String VOLTAGE_LEVEL_ID = "voltageLevelId";
    private static final String NETWORK_UUID = "networkUuid";
    private static final String SUBSTATION_ID = "substationid";
    private static final String UUID_STR = "uuid";
    private static final String ID_STR = "id";
    private static final String NAME = "name";
    private static final String UNABLE_GET_VALUE_MESSAGE = "Unable to get value from attribute ";

    private static final List<String> ELEMENT_TABLES = List.of(SUBSTATION, VOLTAGE_LEVEL, BUSBAR_SECTION, CONFIGURED_BUS, SWITCH, GENERATOR, BATTERY, LOAD, SHUNT_COMPENSATOR,
            STATIC_VAR_COMPENSATOR, VSC_CONVERTER_STATION, LCC_CONVERTER_STATION, TWO_WINDINGS_TRANSFORMER,
            THREE_WINDINGS_TRANSFORMER, LINE, HVDC_LINE, DANGLING_LINE);

    private Mappings mappings = Mappings.getInstance();

    private PreparedStatement buildInsertStatement(Map<String, Mapping> mapping, String tableName) {
        Set<String> keys = mapping.keySet();
        Insert insert = insertInto(tableName)
            .value(NETWORK_UUID)
            .value(VARIANT_NUM)
            .value(ID_STR);
        keys.forEach(k -> insert.value(k));
        return session.prepare(insert.build());
    }

    private Supplier<java.sql.PreparedStatement> buildCloneStatement(Map<String, Mapping> mapping, String tableName) {
        Set<String> keys = mapping.keySet();
        return () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + tableName + "(" +
                        VARIANT_NUM + ", " +
                        NETWORK_UUID + ", " +
                        ID_STR + ", " +
                        String.join(",", keys) +
                        ") " +
                        "select " +
                        "?" + "," +
                        NETWORK_UUID + "," +
                        ID_STR + "," +
                        String.join(",", keys) +
                        " from " + tableName + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new PowsyblException("Unable to create the clone prepared statement " + e.getMessage());
            }
        };
    }

    private PreparedStatement buildUpdateStatement(Map<String, Mapping> mapping,
                                                   String tableName,
                                                   String columnToAddToWhereClause) {
        Set<String> keys = mapping.keySet();
        Update update = update(tableName);
        keys.forEach(k -> {
            if (columnToAddToWhereClause == null || !k.equals(columnToAddToWhereClause)) {
                update.set(Assignment.setColumn(k));
            }
        });
        Update newUpdate = update
            .whereColumn(NETWORK_UUID).isEqualTo()
            .whereColumn(VARIANT_NUM).isEqualTo()
            .whereColumn(ID_STR).isEqualTo();
        if (columnToAddToWhereClause != null) {
            newUpdate = newUpdate.whereColumn(columnToAddToWhereClause).isEqualTo();
        }
        return session.prepare(newUpdate.build());
    }

    @PostConstruct
    void prepareStatements() {
        // network

        Set<String> keysNetworks = mappings.getNetworkMappings().keySet();
        Insert insertNetwork = insertInto(NETWORK)
            .value(VARIANT_NUM)
            .value(ID_STR);
        keysNetworks.forEach(k -> insertNetwork.value(k));
        insertPreparedStatements.put(NETWORK, session.prepare(insertNetwork.build()));

        psCloneNetworkSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into network(" +
                        VARIANT_NUM + ", " +
                        VARIANT_ID + ", " +
                        UUID_STR + ", " +
                        ID_STR + ", " +
                        String.join(",", keysNetworks.stream().filter(k -> !k.equals(UUID_STR) && !k.equals(VARIANT_ID) && !k.equals(NAME)).collect(Collectors.toList())) +
                        ") " +
                        "select" + " " +
                        "?" + ", " +
                        "?" + ", " +
                        UUID_STR + ", " +
                        ID_STR + ", " +
                        String.join(",", keysNetworks.stream().filter(k -> !k.equals(UUID_STR) && !k.equals(VARIANT_ID) && !k.equals(NAME)).collect(Collectors.toList())) +
                        " from network" + " " +
                        "where uuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new PowsyblException("Unable to create the network clone prepared statement " + e.getMessage());
            }
        };

        Update updateNetwork = update(NETWORK).set(Assignment.setColumn(ID_STR));
        keysNetworks.forEach(k -> {
            if (!k.equals(UUID_STR) && !k.equals(VARIANT_ID)) {
                updateNetwork.set(Assignment.setColumn(k));
            }
        });
        updateNetwork
            .whereColumn(UUID_STR).isEqualTo()
            .whereColumn(VARIANT_NUM).isEqualTo();
        updatePreparedStatements.put(NETWORK, session.prepare(updateNetwork.build()));

        // substation

        insertPreparedStatements.put(SUBSTATION, buildInsertStatement(mappings.getSubstationMappings(), SUBSTATION));
        clonePreparedStatementsSupplier.put(SUBSTATION, buildCloneStatement(mappings.getSubstationMappings(), SUBSTATION));
        updatePreparedStatements.put(SUBSTATION, buildUpdateStatement(mappings.getSubstationMappings(), SUBSTATION, null));

        // voltage level

        insertPreparedStatements.put(VOLTAGE_LEVEL, buildInsertStatement(mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL));
        clonePreparedStatementsSupplier.put(VOLTAGE_LEVEL, buildCloneStatement(mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL));
        updatePreparedStatements.put(VOLTAGE_LEVEL, buildUpdateStatement(mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL, SUBSTATION_ID));

        // generator

        insertPreparedStatements.put(GENERATOR, buildInsertStatement(mappings.getGeneratorMappings(), GENERATOR));
        clonePreparedStatementsSupplier.put(GENERATOR, buildCloneStatement(mappings.getGeneratorMappings(), GENERATOR));
        updatePreparedStatements.put(GENERATOR, buildUpdateStatement(mappings.getGeneratorMappings(), GENERATOR, VOLTAGE_LEVEL_ID));

        // battery

        insertPreparedStatements.put(BATTERY, buildInsertStatement(mappings.getBatteryMappings(), BATTERY));
        clonePreparedStatementsSupplier.put(BATTERY, buildCloneStatement(mappings.getBatteryMappings(), BATTERY));
        updatePreparedStatements.put(BATTERY, buildUpdateStatement(mappings.getBatteryMappings(), BATTERY, VOLTAGE_LEVEL_ID));

        // load

        insertPreparedStatements.put(LOAD, buildInsertStatement(mappings.getLoadMappings(), LOAD));
        clonePreparedStatementsSupplier.put(LOAD, buildCloneStatement(mappings.getLoadMappings(), LOAD));
        updatePreparedStatements.put(LOAD, buildUpdateStatement(mappings.getLoadMappings(), LOAD, VOLTAGE_LEVEL_ID));

        // shunt compensator

        insertPreparedStatements.put(SHUNT_COMPENSATOR, buildInsertStatement(mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR));
        clonePreparedStatementsSupplier.put(SHUNT_COMPENSATOR, buildCloneStatement(mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR));
        updatePreparedStatements.put(SHUNT_COMPENSATOR, buildUpdateStatement(mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR, VOLTAGE_LEVEL_ID));

        // vsc converter station

        insertPreparedStatements.put(VSC_CONVERTER_STATION, buildInsertStatement(mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION));
        clonePreparedStatementsSupplier.put(VSC_CONVERTER_STATION, buildCloneStatement(mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION));
        updatePreparedStatements.put(VSC_CONVERTER_STATION, buildUpdateStatement(mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION, VOLTAGE_LEVEL_ID));

        // lcc converter station

        insertPreparedStatements.put(LCC_CONVERTER_STATION, buildInsertStatement(mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION));
        clonePreparedStatementsSupplier.put(LCC_CONVERTER_STATION, buildCloneStatement(mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION));
        updatePreparedStatements.put(LCC_CONVERTER_STATION, buildUpdateStatement(mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION, VOLTAGE_LEVEL_ID));

        // static var compensator

        insertPreparedStatements.put(STATIC_VAR_COMPENSATOR, buildInsertStatement(mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR));
        clonePreparedStatementsSupplier.put(STATIC_VAR_COMPENSATOR, buildCloneStatement(mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR));
        updatePreparedStatements.put(STATIC_VAR_COMPENSATOR, buildUpdateStatement(mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR, VOLTAGE_LEVEL_ID));

        // busbar section

        insertPreparedStatements.put(BUSBAR_SECTION, buildInsertStatement(mappings.getBusbarSectionMappings(), BUSBAR_SECTION));
        clonePreparedStatementsSupplier.put(BUSBAR_SECTION, buildCloneStatement(mappings.getBusbarSectionMappings(), BUSBAR_SECTION));
        updatePreparedStatements.put(BUSBAR_SECTION, buildUpdateStatement(mappings.getBusbarSectionMappings(), BUSBAR_SECTION, VOLTAGE_LEVEL_ID));

        // switch

        insertPreparedStatements.put(SWITCH, buildInsertStatement(mappings.getSwitchMappings(), SWITCH));
        clonePreparedStatementsSupplier.put(SWITCH, buildCloneStatement(mappings.getSwitchMappings(), SWITCH));
        updatePreparedStatements.put(SWITCH, buildUpdateStatement(mappings.getSwitchMappings(), SWITCH, VOLTAGE_LEVEL_ID));

        // two windings transformer

        insertPreparedStatements.put(TWO_WINDINGS_TRANSFORMER, buildInsertStatement(mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER));
        clonePreparedStatementsSupplier.put(TWO_WINDINGS_TRANSFORMER, buildCloneStatement(mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER));
        updatePreparedStatements.put(TWO_WINDINGS_TRANSFORMER, buildUpdateStatement(mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER, null));

        // three windings transformer

        insertPreparedStatements.put(THREE_WINDINGS_TRANSFORMER, buildInsertStatement(mappings.getThreeWindingsTransformerMappings(), THREE_WINDINGS_TRANSFORMER));
        clonePreparedStatementsSupplier.put(THREE_WINDINGS_TRANSFORMER, buildCloneStatement(mappings.getThreeWindingsTransformerMappings(), THREE_WINDINGS_TRANSFORMER));
        updatePreparedStatements.put(THREE_WINDINGS_TRANSFORMER, buildUpdateStatement(mappings.getThreeWindingsTransformerMappings(), THREE_WINDINGS_TRANSFORMER, null));

        // line

        insertPreparedStatements.put(LINE, buildInsertStatement(mappings.getLineMappings(), LINE));
        clonePreparedStatementsSupplier.put(LINE, buildCloneStatement(mappings.getLineMappings(), LINE));
        updatePreparedStatements.put(LINE, buildUpdateStatement(mappings.getLineMappings(), LINE, null));

        // hvdc line

        insertPreparedStatements.put(HVDC_LINE, buildInsertStatement(mappings.getHvdcLineMappings(), HVDC_LINE));
        clonePreparedStatementsSupplier.put(HVDC_LINE, buildCloneStatement(mappings.getHvdcLineMappings(), HVDC_LINE));
        updatePreparedStatements.put(HVDC_LINE, buildUpdateStatement(mappings.getHvdcLineMappings(), HVDC_LINE, null));

        // dangling line

        insertPreparedStatements.put(DANGLING_LINE, buildInsertStatement(mappings.getDanglingLineMappings(), DANGLING_LINE));
        clonePreparedStatementsSupplier.put(DANGLING_LINE, buildCloneStatement(mappings.getDanglingLineMappings(), DANGLING_LINE));
        updatePreparedStatements.put(DANGLING_LINE, buildUpdateStatement(mappings.getDanglingLineMappings(), DANGLING_LINE, VOLTAGE_LEVEL_ID));

        // configured bus

        insertPreparedStatements.put(CONFIGURED_BUS, buildInsertStatement(mappings.getConfiguredBusMappings(), CONFIGURED_BUS));
        clonePreparedStatementsSupplier.put(CONFIGURED_BUS, buildCloneStatement(mappings.getConfiguredBusMappings(), CONFIGURED_BUS));
        updatePreparedStatements.put(CONFIGURED_BUS, buildUpdateStatement(mappings.getConfiguredBusMappings(), CONFIGURED_BUS, VOLTAGE_LEVEL_ID));
    }

    // network

    /**
     * Get all networks infos.
     */
    public List<NetworkInfos> getNetworksInfos() {
        SimpleStatement simpleStatement = selectFrom(NETWORK).columns(UUID_STR, ID_STR)
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
                .whereColumn(UUID_STR).isEqualTo(literal(networkUuid)).build())) {
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
        columnMappings.add(ID_STR);
        try (ResultSet resultSet = session.execute(selectFrom(NETWORK)
            .columns(columnMappings.toArray(new String[0]))
            .whereColumn(UUID_STR).isEqualTo(literal(uuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                NetworkAttributes networkAttributes = new NetworkAttributes();
                mappingNetworks.entrySet().forEach(entry -> entry.getValue().set(networkAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.networkBuilder()
                    .id(one.get(ID_STR, String.class))
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
        PreparedStatement psInsertNetwork = insertPreparedStatements.get(NETWORK);

        for (List<Resource<NetworkAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<NetworkAttributes> resource : subresources) {
                NetworkAttributes networkAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keysNetworks.forEach(key -> values.add(networkMappings.get(key).get(networkAttributes)));

                boundStatements.add(psInsertNetwork.bind(values.toArray(new Object[0])));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void updateNetworks(List<Resource<NetworkAttributes>> resources) {
        Map<String, Mapping> networkMappings = mappings.getNetworkMappings();
        Set<String> keysNetworks = networkMappings.keySet();
        PreparedStatement psUpdateNetwork = updatePreparedStatements.get(NETWORK);

        for (List<Resource<NetworkAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<NetworkAttributes> resource : subresources) {
                NetworkAttributes networkAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(resource.getId());
                keysNetworks.forEach(key -> {
                    if (!key.equals(UUID_STR) && !key.equals(VARIANT_ID)) {
                        values.add(networkMappings.get(key).get(networkAttributes));
                    }
                });
                values.add(networkAttributes.getUuid());
                values.add(resource.getVariantNum());

                boundStatements.add(psUpdateNetwork.bind(values.toArray(new Object[0])));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteNetwork(UUID uuid) {
        session.execute(deleteFrom(NETWORK).whereColumn(UUID_STR).isEqualTo(literal(uuid)).build());
        for (String table : ELEMENT_TABLES) {
            session.execute(deleteFrom(table).whereColumn(NETWORK_UUID).isEqualTo(literal(uuid)).build());
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
                .whereColumn(UUID_STR).isEqualTo(literal(uuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build());
        for (String table : ELEMENT_TABLES) {
            session.execute(deleteFrom(table)
                    .whereColumn(NETWORK_UUID).isEqualTo(literal(uuid))
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
            throw new PowsyblException(e);
        }

        stopwatch.stop();
        LOGGER.info("Network clone done in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public <T extends IdentifiableAttributes> void createIdentifiables(UUID networkUuid, List<Resource<T>> resources,
                                                                       Map<String, Mapping> mappings, PreparedStatement psInsert) {
        Set<String> keys = mappings.keySet();

        for (List<Resource<T>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<T> resource : subresources) {
                T attributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                keys.forEach(key -> values.add(mappings.get(key).get(attributes)));

                boundStatements.add(psInsert.bind(values.toArray(new Object[0])));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public <T extends IdentifiableAttributes> Optional<Resource<T>> getIdentifiable(UUID networkUuid, int variantNum, String equipmentId,
                                                                                    Map<String, Mapping> mappings, String tableName,
                                                                                    Resource.Builder<T> resourceBuilder,
                                                                                    Supplier<T> attributesSupplier) {
        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(mappings.keySet().toArray(new String[0]))
            .whereColumn(NETWORK_UUID).isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn(ID_STR).isEqualTo(literal(equipmentId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                T attributes = attributesSupplier.get();
                mappings.entrySet().forEach(entry -> {
                    if (entry.getValue().getClassR() != null) {
                        entry.getValue().set(attributes, one.get(entry.getKey(), entry.getValue().getClassR()));
                    } else if (entry.getValue().getClassMapKey() != null && entry.getValue().getClassMapValue() != null) {
                        entry.getValue().set(attributes, one.getMap(entry.getKey(), entry.getValue().getClassMapKey(), entry.getValue().getClassMapValue()));
                    } else {
                        throw new PowsyblException(UNABLE_GET_VALUE_MESSAGE + entry.getKey());
                    }
                });
                return Optional.of(resourceBuilder
                    .id(equipmentId)
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiables(UUID networkUuid, int variantNum,
                                                                                 Map<String, Mapping> mappings, String tableName,
                                                                                 Resource.Builder<T> resourceBuilder,
                                                                                 Supplier<T> attributesSupplier) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add(ID_STR);

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn(NETWORK_UUID).isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = attributesSupplier.get();
                mappings.entrySet().forEach(entry -> {
                    if (entry.getValue().getClassR() != null) {
                        entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR()));
                    } else if (entry.getValue().getClassMapKey() != null && entry.getValue().getClassMapValue() != null) {
                        entry.getValue().set(attributes, row.getMap(entry.getKey(), entry.getValue().getClassMapKey(), entry.getValue().getClassMapValue()));
                    } else {
                        throw new PowsyblException(UNABLE_GET_VALUE_MESSAGE + entry.getKey());
                    }
                });
                resources.add(resourceBuilder
                    .id(row.getString(ID_STR))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiablesInContainer(UUID networkUuid, int variantNum, String containerId,
                                                                                            String containerColumnName,
                                                                                            Map<String, Mapping> mappings, String tableName,
                                                                                            Resource.Builder<T> resourceBuilder,
                                                                                            Supplier<T> attributesSupplier) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add(ID_STR);

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn(NETWORK_UUID).isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn(containerColumnName).isEqualTo(literal(containerId))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = attributesSupplier.get();
                mappings.entrySet().forEach(entry -> {
                    if (entry.getValue().getClassR() != null) {
                        entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR()));
                    } else if (entry.getValue().getClassMapKey() != null && entry.getValue().getClassMapValue() != null) {
                        entry.getValue().set(attributes, row.getMap(entry.getKey(), entry.getValue().getClassMapKey(), entry.getValue().getClassMapValue()));
                    } else {
                        throw new PowsyblException(UNABLE_GET_VALUE_MESSAGE + entry.getKey());
                    }
                });
                resources.add(resourceBuilder
                    .id(row.getString(ID_STR))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiablesWithSide(UUID networkUuid, int variantNum, String voltageLevelId,
                                                                                         String side,
                                                                                         Map<String, Mapping> mappings, String tableName,
                                                                                         Resource.Builder<T> resourceBuilder,
                                                                                         Supplier<T> attributesSupplier) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add(ID_STR);

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn(NETWORK_UUID).isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn(VOLTAGE_LEVEL_ID + side).isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = attributesSupplier.get();
                mappings.entrySet().forEach(entry -> {
                    if (entry.getValue().getClassR() != null) {
                        entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR()));
                    } else if (entry.getValue().getClassMapKey() != null && entry.getValue().getClassMapValue() != null) {
                        entry.getValue().set(attributes, row.getMap(entry.getKey(), entry.getValue().getClassMapKey(), entry.getValue().getClassMapValue()));
                    } else {
                        throw new PowsyblException(UNABLE_GET_VALUE_MESSAGE + entry.getKey());
                    }
                });
                resources.add(resourceBuilder
                    .id(row.getString(ID_STR))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        }
    }

    public <T extends IdentifiableAttributes & Contained> void updateIdentifiables(UUID networkUuid, List<Resource<T>> resources,
                                                                                   Map<String, Mapping> mappings, PreparedStatement psUpdate,
                                                                                   String columnToAddToWhereClause) {
        Set<String> keys = mappings.keySet();

        for (List<Resource<T>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<T> resource : subresources) {
                T attributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keys.forEach(key -> {
                    if (!key.equals(columnToAddToWhereClause)) {
                        values.add(mappings.get(key).get(attributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getContainerIds().stream().findFirst().orElseThrow());

                boundStatements.add(psUpdate.bind(values.toArray(new Object[0])));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public <T extends IdentifiableAttributes> void updateIdentifiables2(UUID networkUuid, List<Resource<T>> resources,
                                                                        Map<String, Mapping> mappings, PreparedStatement psUpdate) {
        Set<String> keys = mappings.keySet();

        for (List<Resource<T>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<T> resource : subresources) {
                T attributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keys.forEach(key -> values.add(mappings.get(key).get(attributes)));
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());

                boundStatements.add(psUpdate.bind(values.toArray(new Object[0])));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public void deleteIdentifiable(UUID networkUuid, int variantNum, String equipmentId, String tableName) {
        session.execute(deleteFrom(tableName)
            .whereColumn(NETWORK_UUID).isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn(ID_STR).isEqualTo(literal(equipmentId))
            .build());
    }

    // substation

    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getSubstationMappings(), SUBSTATION, Resource.substationBuilder(), SubstationAttributes::new);
    }

    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return getIdentifiable(networkUuid, variantNum, substationId, mappings.getSubstationMappings(),
            SUBSTATION, Resource.substationBuilder(), SubstationAttributes::new);
    }

    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getSubstationMappings(), insertPreparedStatements.get(SUBSTATION));
    }

    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        updateIdentifiables2(networkUuid, resources, mappings.getSubstationMappings(), updatePreparedStatements.get(SUBSTATION));
    }

    public void deleteSubstation(UUID networkUuid, int variantNum, String substationId) {
        deleteIdentifiable(networkUuid, variantNum, substationId, SUBSTATION);
    }

    // voltage level

    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getVoltageLevelMappings(), insertPreparedStatements.get(VOLTAGE_LEVEL));
    }

    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getVoltageLevelMappings(), updatePreparedStatements.get(VOLTAGE_LEVEL), SUBSTATION_ID);
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum, String substationId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, substationId, SUBSTATION_ID, mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes::new);
    }

    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiable(networkUuid, variantNum, voltageLevelId, mappings.getVoltageLevelMappings(),
            VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes::new);
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes::new);
    }

    public void deleteVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        deleteIdentifiable(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL);
    }

    // generator

    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getGeneratorMappings(), insertPreparedStatements.get(GENERATOR));
    }

    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return getIdentifiable(networkUuid, variantNum, generatorId, mappings.getGeneratorMappings(),
            GENERATOR, Resource.generatorBuilder(), GeneratorAttributes::new);
    }

    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getGeneratorMappings(), GENERATOR, Resource.generatorBuilder(), GeneratorAttributes::new);
    }

    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getGeneratorMappings(), GENERATOR, Resource.generatorBuilder(), GeneratorAttributes::new);
    }

    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getGeneratorMappings(), updatePreparedStatements.get(GENERATOR), VOLTAGE_LEVEL_ID);
    }

    public void deleteGenerator(UUID networkUuid, int variantNum, String generatorId) {
        deleteIdentifiable(networkUuid, variantNum, generatorId, GENERATOR);
    }

    // battery

    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getBatteryMappings(), insertPreparedStatements.get(BATTERY));
    }

    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return getIdentifiable(networkUuid, variantNum, batteryId, mappings.getBatteryMappings(),
            BATTERY, Resource.batteryBuilder(), BatteryAttributes::new);
    }

    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getBatteryMappings(), BATTERY, Resource.batteryBuilder(), BatteryAttributes::new);
    }

    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getBatteryMappings(), BATTERY, Resource.batteryBuilder(), BatteryAttributes::new);
    }

    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getBatteryMappings(), updatePreparedStatements.get(BATTERY), VOLTAGE_LEVEL_ID);
    }

    public void deleteBattery(UUID networkUuid, int variantNum, String batteryId) {
        deleteIdentifiable(networkUuid, variantNum, batteryId, BATTERY);
    }

    // load

    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getLoadMappings(), insertPreparedStatements.get(LOAD));
    }

    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return getIdentifiable(networkUuid, variantNum, loadId, mappings.getLoadMappings(),
                           LOAD, Resource.loadBuilder(), LoadAttributes::new);
    }

    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getLoadMappings(), LOAD, Resource.loadBuilder(), LoadAttributes::new);
    }

    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getLoadMappings(), LOAD, Resource.loadBuilder(), LoadAttributes::new);
    }

    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getLoadMappings(), updatePreparedStatements.get(LOAD), VOLTAGE_LEVEL_ID);
    }

    public void deleteLoad(UUID networkUuid, int variantNum, String loadId) {
        deleteIdentifiable(networkUuid, variantNum, loadId, LOAD);
    }

    // shunt compensator

    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getShuntCompensatorMappings(), insertPreparedStatements.get(SHUNT_COMPENSATOR));
    }

    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return getIdentifiable(networkUuid, variantNum, shuntCompensatorId, mappings.getShuntCompensatorMappings(),
                           SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes::new);
    }

    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes::new);
    }

    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes::new);
    }

    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getShuntCompensatorMappings(), updatePreparedStatements.get(SHUNT_COMPENSATOR), VOLTAGE_LEVEL_ID);
    }

    public void deleteShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        deleteIdentifiable(networkUuid, variantNum, shuntCompensatorId, SHUNT_COMPENSATOR);
    }

    // VSC converter station

    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getVscConverterStationMappings(), insertPreparedStatements.get(VSC_CONVERTER_STATION));
    }

    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return getIdentifiable(networkUuid, variantNum, vscConverterStationId, mappings.getVscConverterStationMappings(),
                           VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes::new);
    }

    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes::new);
    }

    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes::new);
    }

    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getVscConverterStationMappings(), updatePreparedStatements.get(VSC_CONVERTER_STATION), VOLTAGE_LEVEL_ID);
    }

    public void deleteVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        deleteIdentifiable(networkUuid, variantNum, vscConverterStationId, VSC_CONVERTER_STATION);
    }

    // LCC converter station

    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getLccConverterStationMappings(), insertPreparedStatements.get(LCC_CONVERTER_STATION));
    }

    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return getIdentifiable(networkUuid, variantNum, lccConverterStationId, mappings.getLccConverterStationMappings(),
                           LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes::new);
    }

    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes::new);
    }

    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes::new);
    }

    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getLccConverterStationMappings(), updatePreparedStatements.get(LCC_CONVERTER_STATION), VOLTAGE_LEVEL_ID);
    }

    public void deleteLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        deleteIdentifiable(networkUuid, variantNum, lccConverterStationId, LCC_CONVERTER_STATION);
    }

    // static var compensators

    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getStaticVarCompensatorMappings(), insertPreparedStatements.get(STATIC_VAR_COMPENSATOR));
    }

    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return getIdentifiable(networkUuid, variantNum, staticVarCompensatorId, mappings.getStaticVarCompensatorMappings(),
            STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes::new);
    }

    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes::new);
    }

    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes::new);
    }

    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getStaticVarCompensatorMappings(), updatePreparedStatements.get(STATIC_VAR_COMPENSATOR), VOLTAGE_LEVEL_ID);
    }

    public void deleteStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        deleteIdentifiable(networkUuid, variantNum, staticVarCompensatorId, STATIC_VAR_COMPENSATOR);
    }

    // busbar section

    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getBusbarSectionMappings(), insertPreparedStatements.get(BUSBAR_SECTION));
    }

    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getBusbarSectionMappings(), updatePreparedStatements.get(BUSBAR_SECTION), VOLTAGE_LEVEL_ID);
    }

    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return getIdentifiable(networkUuid, variantNum, busbarSectionId, mappings.getBusbarSectionMappings(),
            BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes::new);
    }

    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getBusbarSectionMappings(), BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes::new);
    }

    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getBusbarSectionMappings(), BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes::new);
    }

    public void deleteBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        deleteIdentifiable(networkUuid, variantNum, busBarSectionId, BUSBAR_SECTION);
    }

    // switch

    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getSwitchMappings(), insertPreparedStatements.get(SWITCH));
    }

    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return getIdentifiable(networkUuid, variantNum, switchId, mappings.getSwitchMappings(),
            SWITCH, Resource.switchBuilder(), SwitchAttributes::new);
    }

    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getSwitchMappings(), SWITCH, Resource.switchBuilder(), SwitchAttributes::new);
    }

    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getSwitchMappings(), SWITCH, Resource.switchBuilder(), SwitchAttributes::new);
    }

    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getSwitchMappings(), updatePreparedStatements.get(SWITCH), VOLTAGE_LEVEL_ID);
    }

    public void deleteSwitch(UUID networkUuid, int variantNum, String switchId) {
        deleteIdentifiable(networkUuid, variantNum, switchId, SWITCH);
    }

    // 2 windings transformer

    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getTwoWindingsTransformerMappings(), insertPreparedStatements.get(TWO_WINDINGS_TRANSFORMER));
    }

    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return getIdentifiable(networkUuid, variantNum, twoWindingsTransformerId, mappings.getTwoWindingsTransformerMappings(),
            TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes::new);
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes::new);
    }

    private List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, Branch.Side side, String voltageLevelId) {
        return getIdentifiablesWithSide(networkUuid, variantNum, voltageLevelId, side == Branch.Side.ONE ? "1" : "2", mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes::new);
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
        updateIdentifiables2(networkUuid, resources, mappings.getTwoWindingsTransformerMappings(), updatePreparedStatements.get(TWO_WINDINGS_TRANSFORMER));
    }

    public void deleteTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        deleteIdentifiable(networkUuid, variantNum, twoWindingsTransformerId, TWO_WINDINGS_TRANSFORMER);
    }

    // 3 windings transformer

    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getThreeWindingsTransformerMappings(), insertPreparedStatements.get(THREE_WINDINGS_TRANSFORMER));
    }

    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        return getIdentifiable(networkUuid, variantNum, threeWindingsTransformerId, mappings.getThreeWindingsTransformerMappings(),
            THREE_WINDINGS_TRANSFORMER, Resource.threeWindingsTransformerBuilder(), () -> {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());
                return threeWindingsTransformerAttributes;
            });
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getThreeWindingsTransformerMappings(), THREE_WINDINGS_TRANSFORMER,
            Resource.threeWindingsTransformerBuilder(), () -> {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());
                return threeWindingsTransformerAttributes;
            });
    }

    private List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, ThreeWindingsTransformer.Side side, String voltageLevelId) {
        String sideStr = "";
        if (side == ThreeWindingsTransformer.Side.ONE) {
            sideStr = "1";
        } else {
            sideStr = side == ThreeWindingsTransformer.Side.TWO ? "2" : "3";
        }
        return getIdentifiablesWithSide(networkUuid, variantNum, voltageLevelId, sideStr, mappings.getThreeWindingsTransformerMappings(), THREE_WINDINGS_TRANSFORMER,
            Resource.threeWindingsTransformerBuilder(), () -> {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());
                return threeWindingsTransformerAttributes;
            });
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
        updateIdentifiables2(networkUuid, resources, mappings.getThreeWindingsTransformerMappings(), updatePreparedStatements.get(THREE_WINDINGS_TRANSFORMER));
    }

    public void deleteThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        deleteIdentifiable(networkUuid, variantNum, threeWindingsTransformerId, THREE_WINDINGS_TRANSFORMER);
    }

    // line

    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getLineMappings(), insertPreparedStatements.get(LINE));
    }

    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return getIdentifiable(networkUuid, variantNum, lineId, mappings.getLineMappings(),
            LINE, Resource.lineBuilder(), LineAttributes::new);
    }

    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getLineMappings(), LINE, Resource.lineBuilder(), LineAttributes::new);
    }

    private List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, Branch.Side side, String voltageLevelId) {
        return getIdentifiablesWithSide(networkUuid, variantNum, voltageLevelId, side == Branch.Side.ONE ? "1" : "2", mappings.getLineMappings(), LINE, Resource.lineBuilder(), LineAttributes::new);
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
        updateIdentifiables2(networkUuid, resources, mappings.getLineMappings(), updatePreparedStatements.get(LINE));
    }

    public void deleteLine(UUID networkUuid, int variantNum, String lineId) {
        deleteIdentifiable(networkUuid, variantNum, lineId, LINE);
    }

    // Hvdc line

    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getHvdcLineMappings(), HVDC_LINE, Resource.hvdcLineBuilder(), HvdcLineAttributes::new);
    }

    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return getIdentifiable(networkUuid, variantNum, hvdcLineId, mappings.getHvdcLineMappings(),
                           HVDC_LINE, Resource.hvdcLineBuilder(), HvdcLineAttributes::new);
    }

    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getHvdcLineMappings(), insertPreparedStatements.get(HVDC_LINE));
    }

    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        updateIdentifiables2(networkUuid, resources, mappings.getHvdcLineMappings(), updatePreparedStatements.get(HVDC_LINE));
    }

    public void deleteHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        deleteIdentifiable(networkUuid, variantNum, hvdcLineId, HVDC_LINE);
    }

    // Dangling line

    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getDanglingLineMappings(), DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes::new);
    }

    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return getIdentifiable(networkUuid, variantNum, danglingLineId, mappings.getDanglingLineMappings(),
                           DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes::new);
    }

    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getDanglingLineMappings(), DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes::new);
    }

    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getDanglingLineMappings(), insertPreparedStatements.get(DANGLING_LINE));
    }

    public void deleteDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        deleteIdentifiable(networkUuid, variantNum, danglingLineId, DANGLING_LINE);
    }

    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getDanglingLineMappings(), updatePreparedStatements.get(DANGLING_LINE), VOLTAGE_LEVEL_ID);
    }

    // configured buses

    public void createBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getConfiguredBusMappings(), insertPreparedStatements.get(CONFIGURED_BUS));
    }

    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return getIdentifiable(networkUuid, variantNum, busId, mappings.getConfiguredBusMappings(),
                           CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes::new);
    }

    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getConfiguredBusMappings(), CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes::new);
    }

    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_ID, mappings.getConfiguredBusMappings(), CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes::new);
    }

    public void updateBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getConfiguredBusMappings(), updatePreparedStatements.get(CONFIGURED_BUS), VOLTAGE_LEVEL_ID);
    }

    public void deleteBus(UUID networkUuid, int variantNum, String configuredBusId) {
        deleteIdentifiable(networkUuid, variantNum, configuredBusId, CONFIGURED_BUS);
    }
}
