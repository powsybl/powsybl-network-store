/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.*;
import com.powsybl.network.store.model.utils.VariantUtils;
import com.powsybl.network.store.server.exceptions.JsonApiErrorResponseException;
import com.powsybl.network.store.server.exceptions.UncheckedSqlException;
import com.powsybl.ws.commons.LogUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.powsybl.network.store.server.Mappings.*;
import static com.powsybl.network.store.server.QueryCatalog.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Repository
public class NetworkStoreRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkStoreRepository.class);

    @Autowired
    public NetworkStoreRepository(DataSource dataSource, ObjectMapper mapper, Mappings mappings) {
        this.dataSource = dataSource;
        this.mapper = mapper;
        this.mappings = mappings;
    }

    private final DataSource dataSource;

    private final ObjectMapper mapper;

    private final Mappings mappings;

    private static final int BATCH_SIZE = 1000;

    private static final String SUBSTATION_ID = "substationid";

    private static boolean isCustomTypeJsonified(Class<?> clazz) {
        return !(
            Integer.class.equals(clazz) || Long.class.equals(clazz)
                    || Float.class.equals(clazz) || Double.class.equals(clazz)
                    || String.class.equals(clazz) || Boolean.class.equals(clazz)
                    || UUID.class.equals(clazz)
                    || Date.class.isAssignableFrom(clazz) // java.util.Date and java.sql.Date
            );
    }

    private void bindValues(PreparedStatement statement, List<Object> values) throws SQLException {
        int idx = 0;
        for (Object o : values) {
            if (o instanceof Instant) {
                Instant d = (Instant) o;
                statement.setDate(++idx, new java.sql.Date(d.toEpochMilli()));
            } else if (o == null || !isCustomTypeJsonified(o.getClass())) {
                statement.setObject(++idx, o);
            } else {
                try {
                    statement.setObject(++idx, mapper.writeValueAsString(o));
                } catch (JsonProcessingException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    private void bindAttributes(ResultSet resultSet, int columnIndex, ColumnMapping columnMapping, IdentifiableAttributes attributes) {
        try {
            Object value = null;
            if (columnMapping.getClassR() == null || isCustomTypeJsonified(columnMapping.getClassR())) {
                String str = resultSet.getString(columnIndex);
                if (str != null) {
                    if (columnMapping.getClassMapKey() != null && columnMapping.getClassMapValue() != null) {
                        value = mapper.readValue(str, mapper.getTypeFactory().constructMapType(Map.class, columnMapping.getClassMapKey(), columnMapping.getClassMapValue()));
                    } else {
                        if (columnMapping.getClassR() == null) {
                            throw new PowsyblException("Invalid mapping config");
                        }
                        if (columnMapping.getClassR() == Instant.class) {
                            value = resultSet.getTimestamp(columnIndex).toInstant();
                        } else {
                            value = mapper.readValue(str, columnMapping.getClassR());
                        }
                    }
                }
            } else {
                value = resultSet.getObject(columnIndex, columnMapping.getClassR());
            }
            if (value != null) {
                columnMapping.set(attributes, value);
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // network

    /**
     * Get all networks infos.
     */
    public List<NetworkInfos> getNetworksInfos() {
        try (var connection = dataSource.getConnection()) {
            try (var stmt = connection.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery(QueryCatalog.buildGetNetworkInfos())) {
                    List<NetworkInfos> networksInfos = new ArrayList<>();
                    while (resultSet.next()) {
                        networksInfos.add(new NetworkInfos(resultSet.getObject(1, UUID.class),
                                                           resultSet.getString(2)));
                    }
                    return networksInfos;
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public List<VariantInfos> getVariantsInfos(UUID networkUuid) {
        try (var connection = dataSource.getConnection()) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildGetVariantsInfos())) {
                preparedStmt.setObject(1, networkUuid);
                try (ResultSet resultSet = preparedStmt.executeQuery()) {
                    List<VariantInfos> variantsInfos = new ArrayList<>();
                    while (resultSet.next()) {
                        variantsInfos.add(new VariantInfos(resultSet.getString(1), resultSet.getInt(2)));
                    }
                    return variantsInfos;
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public Optional<Resource<NetworkAttributes>> getNetwork(UUID uuid, int variantNum) {
        var networkMapping = mappings.getNetworkMappings();
        try (var connection = dataSource.getConnection()) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildGetNetworkQuery(networkMapping.getColumnsMapping().keySet()))) {
                preparedStmt.setObject(1, uuid);
                preparedStmt.setInt(2, variantNum);
                try (ResultSet resultSet = preparedStmt.executeQuery()) {
                    if (resultSet.next()) {
                        NetworkAttributes attributes = new NetworkAttributes();
                        MutableInt columnIndex = new MutableInt(2);
                        networkMapping.getColumnsMapping().forEach((columnName, columnMapping) -> {
                            bindAttributes(resultSet, columnIndex.getValue(), columnMapping, attributes);
                            columnIndex.increment();
                        });
                        return Optional.of(Resource.networkBuilder()
                                .id(resultSet.getString(1)) // id is first
                                .variantNum(variantNum)
                                .attributes(attributes)
                                .build());
                    }
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @FunctionalInterface
    interface SqlExecutor {

        void execute(Connection connection) throws SQLException;
    }

    private static void restoreAutoCommitQuietly(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.error("Exception during autocommit restoration, please check next exception", e);
        }
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            LOGGER.error("Exception during rollback, please check next exception", e);
        }
    }

    private static void executeWithoutAutoCommit(Connection connection, SqlExecutor executor) throws SQLException {
        connection.setAutoCommit(false);
        try {
            executor.execute(connection);
            connection.commit();
        } catch (Exception e) {
            rollbackQuietly(connection);
            throw new RuntimeException(e);
        } finally {
            restoreAutoCommitQuietly(connection);
        }
    }

    private void executeWithoutAutoCommit(SqlExecutor executor) {
        try (var connection = dataSource.getConnection()) {
            executeWithoutAutoCommit(connection, executor);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public void createNetworks(List<Resource<NetworkAttributes>> resources) {
        executeWithoutAutoCommit(connection -> createNetworks(connection, resources));
    }

    private void createNetworks(Connection connection, List<Resource<NetworkAttributes>> resources) throws SQLException {
        var tableMapping = mappings.getNetworkMappings();
        try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildInsertNetworkQuery(tableMapping.getTable(), tableMapping.getColumnsMapping().keySet()))) {
            List<Object> values = new ArrayList<>(2 + tableMapping.getColumnsMapping().size());
            for (List<Resource<NetworkAttributes>> subResources : Lists.partition(resources, BATCH_SIZE)) {
                for (Resource<NetworkAttributes> resource : subResources) {
                    NetworkAttributes attributes = resource.getAttributes();
                    values.clear();
                    values.add(resource.getVariantNum());
                    values.add(resource.getId());
                    for (var mapping : tableMapping.getColumnsMapping().values()) {
                        values.add(mapping.get(attributes));
                    }
                    bindValues(preparedStmt, values);
                    preparedStmt.addBatch();
                }
                preparedStmt.executeBatch();
            }
        }
    }

    public void updateNetworks(List<Resource<NetworkAttributes>> resources) {
        executeWithoutAutoCommit(connection -> {
            TableMapping networkMapping = mappings.getNetworkMappings();
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildUpdateNetworkQuery(networkMapping.getColumnsMapping().keySet()))) {
                List<Object> values = new ArrayList<>(3 + networkMapping.getColumnsMapping().size());
                for (List<Resource<NetworkAttributes>> subResources : Lists.partition(resources, BATCH_SIZE)) {
                    for (Resource<NetworkAttributes> resource : subResources) {
                        NetworkAttributes attributes = resource.getAttributes();
                        values.clear();
                        values.add(resource.getId());
                        for (var e : networkMapping.getColumnsMapping().entrySet()) {
                            String columnName = e.getKey();
                            var mapping = e.getValue();
                            if (!columnName.equals(UUID_COLUMN) && !columnName.equals(VARIANT_ID_COLUMN)) {
                                values.add(mapping.get(attributes));
                            }
                        }
                        values.add(attributes.getUuid());
                        values.add(resource.getVariantNum());
                        bindValues(preparedStmt, values);
                        preparedStmt.addBatch();
                    }
                    preparedStmt.executeBatch();
                }
            }
        });
    }

    public void deleteNetwork(UUID uuid) {
        try (var connection = dataSource.getConnection()) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildDeleteNetworkQuery())) {
                preparedStmt.setObject(1, uuid);
                preparedStmt.execute();
            }
            for (String table : ELEMENT_TABLES) {
                try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildDeleteIdentifiablesQuery(table))) {
                    preparedStmt.setObject(1, uuid);
                    preparedStmt.execute();
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    /**
     * Just delete one variant of the network
     */
    public void deleteNetwork(UUID uuid, int variantNum) {
        if (variantNum == Resource.INITIAL_VARIANT_NUM) {
            throw new IllegalArgumentException("Cannot delete initial variant");
        }
        try (var connection = dataSource.getConnection()) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildDeleteNetworkVariantQuery())) {
                preparedStmt.setObject(1, uuid);
                preparedStmt.setInt(2, variantNum);
                preparedStmt.execute();
            }
            for (String table : ELEMENT_TABLES) {
                try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildDeleteIdentifiablesVariantQuery(table))) {
                    preparedStmt.setObject(1, uuid);
                    preparedStmt.setInt(2, variantNum);
                    preparedStmt.execute();
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public void cloneNetwork(UUID targetNetworkUuid, UUID sourceNetworkUuid, List<String> targetVariantIds) {
        LOGGER.info("Cloning network {} to network {} with variants {}", sourceNetworkUuid, targetNetworkUuid,
                targetVariantIds.stream().map(LogUtils::sanitizeParam).collect(Collectors.toList()));

        var stopwatch = Stopwatch.createStarted();

        List<VariantInfos> variantsInfoList = getVariantsInfos(sourceNetworkUuid).stream()
                .filter(v -> targetVariantIds.contains(v.getId()))
                .sorted(Comparator.comparing(VariantInfos::getNum))
                .collect(Collectors.toList());

        Set<String> variantsNotFound = new HashSet<>(targetVariantIds);
        List<VariantInfos> newNetworkVariants = new ArrayList<>();

        executeWithoutAutoCommit(connection -> {
            for (VariantInfos variantInfos : variantsInfoList) {
                Resource<NetworkAttributes> sourceNetworkAttribute = getNetwork(sourceNetworkUuid, variantInfos.getNum()).orElseThrow(() -> new PowsyblException("Cannot retrieve source network attributes uuid : " + sourceNetworkUuid + ", variantId : " + variantInfos.getId()));
                sourceNetworkAttribute.getAttributes().setUuid(targetNetworkUuid);
                sourceNetworkAttribute.setVariantNum(VariantUtils.findFistAvailableVariantNum(newNetworkVariants));

                newNetworkVariants.add(new VariantInfos(sourceNetworkAttribute.getAttributes().getVariantId(), sourceNetworkAttribute.getVariantNum()));
                variantsNotFound.remove(sourceNetworkAttribute.getAttributes().getVariantId());

                createNetworks(connection, List.of(sourceNetworkAttribute));
                cloneNetworkElements(connection, sourceNetworkUuid, targetNetworkUuid, sourceNetworkAttribute.getVariantNum(), variantInfos.getNum());
            }
        });

        variantsNotFound.forEach(variantNotFound -> LOGGER.warn("The network {} has no variant ID named : {}, thus it has not been cloned", sourceNetworkUuid, variantNotFound));

        stopwatch.stop();
        LOGGER.info("Network clone done in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void cloneNetworkVariant(UUID uuid, int sourceVariantNum, int targetVariantNum, String targetVariantId) {
        String nonNullTargetVariantId = targetVariantId == null ? "variant-" + UUID.randomUUID() : targetVariantId;
        LOGGER.info("Cloning network {} variant {} to variant {}", uuid, sourceVariantNum, targetVariantNum);

        var stopwatch = Stopwatch.createStarted();

        try (var connection = dataSource.getConnection()) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildCloneNetworksQuery(mappings.getNetworkMappings().getColumnsMapping().keySet()))) {
                preparedStmt.setInt(1, targetVariantNum);
                preparedStmt.setString(2, nonNullTargetVariantId);
                preparedStmt.setObject(3, uuid);
                preparedStmt.setInt(4, sourceVariantNum);
                preparedStmt.execute();
            }

            cloneNetworkElements(connection, uuid, uuid, sourceVariantNum, targetVariantNum);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }

        stopwatch.stop();
        LOGGER.info("Network variant clone done in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void cloneNetworkElements(Connection connection, UUID uuid, UUID targetUuid, int sourceVariantNum, int targetVariantNum) throws SQLException {
        for (String tableName : ELEMENT_TABLES) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildCloneIdentifiablesQuery(tableName, mappings.getTableMapping(tableName.toLowerCase()).getColumnsMapping().keySet()))) {
                preparedStmt.setInt(1, targetVariantNum);
                preparedStmt.setObject(2, targetUuid);
                preparedStmt.setObject(3, uuid);
                preparedStmt.setInt(4, sourceVariantNum);
                preparedStmt.execute();
            }
        }
    }

    public void cloneNetwork(UUID networkUuid, String sourceVariantId, String targetVariantId, boolean mayOverwrite) {
        List<VariantInfos> variantsInfos = getVariantsInfos(networkUuid);
        Optional<VariantInfos> targetVariant = VariantUtils.getVariant(targetVariantId, variantsInfos);
        if (targetVariant.isPresent()) {
            if (!mayOverwrite) {
                throw new JsonApiErrorResponseException(ErrorObject.cloneOverExisting(targetVariantId));
            } else {
                if (Resource.INITIAL_VARIANT_NUM == targetVariant.get().getNum()) {
                    throw new JsonApiErrorResponseException(ErrorObject.cloneOverInitialForbidden());
                }
                deleteNetwork(networkUuid, targetVariant.get().getNum());
            }
        }
        int sourceVariantNum = VariantUtils.getVariantNum(sourceVariantId, variantsInfos);
        int targetVariantNum = VariantUtils.findFistAvailableVariantNum(variantsInfos);
        cloneNetworkVariant(networkUuid, sourceVariantNum, targetVariantNum, targetVariantId);
    }

    public <T extends IdentifiableAttributes> void createIdentifiables(UUID networkUuid, List<Resource<T>> resources,
                                                                       TableMapping tableMapping) {
        try (var connection = dataSource.getConnection()) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildInsertIdentifiableQuery(tableMapping.getTable(), tableMapping.getColumnsMapping().keySet()))) {
                List<Object> values = new ArrayList<>(3 + tableMapping.getColumnsMapping().size());
                for (List<Resource<T>> subResources : Lists.partition(resources, BATCH_SIZE)) {
                    for (Resource<T> resource : subResources) {
                        T attributes = resource.getAttributes();
                        values.clear();
                        values.add(networkUuid);
                        values.add(resource.getVariantNum());
                        values.add(resource.getId());
                        for (var mapping : tableMapping.getColumnsMapping().values()) {
                            values.add(mapping.get(attributes));
                        }
                        bindValues(preparedStmt, values);
                        preparedStmt.addBatch();
                    }
                    preparedStmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private <T extends IdentifiableAttributes> Optional<Resource<T>> getIdentifiable(UUID networkUuid, int variantNum, String equipmentId,
                                                                                     TableMapping tableMapping) {
        try (var connection = dataSource.getConnection()) {
            var preparedStmt = connection.prepareStatement(QueryCatalog.buildGetIdentifiableQuery(tableMapping.getTable(), tableMapping.getColumnsMapping().keySet()));
            preparedStmt.setObject(1, networkUuid);
            preparedStmt.setInt(2, variantNum);
            preparedStmt.setString(3, equipmentId);
            try (ResultSet resultSet = preparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    T attributes = (T) tableMapping.getAttributesSupplier().get();
                    MutableInt columnIndex = new MutableInt(1);
                    tableMapping.getColumnsMapping().forEach((columnName, columnMapping) -> {
                        bindAttributes(resultSet, columnIndex.getValue(), columnMapping, attributes);
                        columnIndex.increment();
                    });
                    Resource.Builder<T> resourceBuilder = (Resource.Builder<T>) tableMapping.getResourceBuilder();
                    return Optional.of(resourceBuilder
                            .id(equipmentId)
                            .variantNum(variantNum)
                            .attributes(attributes)
                            .build());
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiablesInternal(int variantNum, PreparedStatement preparedStmt, TableMapping tableMapping) throws SQLException {
        try (ResultSet resultSet = preparedStmt.executeQuery()) {
            List<Resource<T>> resources = new ArrayList<>();
            while (resultSet.next()) {
                // first is ID
                String id = resultSet.getString(1);
                T attributes = (T) tableMapping.getAttributesSupplier().get();
                MutableInt columnIndex = new MutableInt(2);
                tableMapping.getColumnsMapping().forEach((columnName, columnMapping) -> {
                    bindAttributes(resultSet, columnIndex.getValue(), columnMapping, attributes);
                    columnIndex.increment();
                });
                Resource.Builder<T> resourceBuilder = (Resource.Builder<T>) tableMapping.getResourceBuilder();
                resources.add(resourceBuilder
                        .id(id)
                        .variantNum(variantNum)
                        .attributes(attributes)
                        .build());
            }
            return resources;
        }
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiables(UUID networkUuid, int variantNum,
                                                                                  TableMapping tableMapping) {
        try (var connection = dataSource.getConnection()) {
            var preparedStmt = connection.prepareStatement(QueryCatalog.buildGetIdentifiablesQuery(tableMapping.getTable(), tableMapping.getColumnsMapping().keySet()));
            preparedStmt.setObject(1, networkUuid);
            preparedStmt.setInt(2, variantNum);
            return getIdentifiablesInternal(variantNum, preparedStmt, tableMapping);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiablesInContainer(UUID networkUuid, int variantNum, String containerId,
                                                                                             String containerColumn,
                                                                                             TableMapping tableMapping) {
        return getIdentifiablesInContainer(networkUuid, variantNum, List.of(containerId), Set.of(containerColumn), tableMapping);
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiablesInContainer(UUID networkUuid, int variantNum, List<String> containerIds,
                                                                                             Set<String> containerColumns,
                                                                                             TableMapping tableMapping) {
        try (var connection = dataSource.getConnection()) {
            var preparedStmt = connection.prepareStatement(QueryCatalog.buildGetIdentifiablesInContainerQuery(tableMapping.getTable(), tableMapping.getColumnsMapping().keySet(), containerColumns, containerIds.size()));
            preparedStmt.setObject(1, networkUuid);
            preparedStmt.setInt(2, variantNum);
            for (int i = 0; i < containerColumns.size(); i++) {
                for (int j = 0; j < containerIds.size(); j++) {
                    String containerId = containerIds.get(j);
                    preparedStmt.setString(3 + i * containerIds.size() + j, containerId);
                }
            }
            return getIdentifiablesInternal(variantNum, preparedStmt, tableMapping);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiablesInVoltageLevel(UUID networkUuid, int variantNum, List<String> voltageLevelIds, TableMapping tableMapping) {
        return getIdentifiablesInContainer(networkUuid, variantNum, voltageLevelIds, tableMapping.getVoltageLevelIdColumns(), tableMapping);
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getIdentifiablesInVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId, TableMapping tableMapping) {
        return getIdentifiablesInContainer(networkUuid, variantNum, List.of(voltageLevelId), tableMapping.getVoltageLevelIdColumns(), tableMapping);
    }

    public <T extends IdentifiableAttributes & Contained> void updateIdentifiables(UUID networkUuid, List<Resource<T>> resources,
                                                                                   TableMapping tableMapping, String columnToAddToWhereClause) {
        try (var connection = dataSource.getConnection()) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildUpdateIdentifiableQuery(tableMapping.getTable(), tableMapping.getColumnsMapping().keySet(), columnToAddToWhereClause))) {
                List<Object> values = new ArrayList<>(4 + tableMapping.getColumnsMapping().size());
                for (List<Resource<T>> subResources : Lists.partition(resources, BATCH_SIZE)) {
                    for (Resource<T> resource : subResources) {
                        T attributes = resource.getAttributes();
                        values.clear();
                        for (var e : tableMapping.getColumnsMapping().entrySet()) {
                            String columnName = e.getKey();
                            var mapping = e.getValue();
                            if (!columnName.equals(columnToAddToWhereClause)) {
                                values.add(mapping.get(attributes));
                            }
                        }
                        values.add(networkUuid);
                        values.add(resource.getVariantNum());
                        values.add(resource.getId());
                        values.add(resource.getAttributes().getContainerIds().iterator().next());
                        bindValues(preparedStmt, values);
                        preparedStmt.addBatch();
                    }
                    preparedStmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public <T extends IdentifiableAttributes> void updateIdentifiables(UUID networkUuid, List<Resource<T>> resources,
                                                                       TableMapping tableMapping) {
        executeWithoutAutoCommit(connection -> {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildUpdateIdentifiableQuery(tableMapping.getTable(), tableMapping.getColumnsMapping().keySet(), null))) {
                List<Object> values = new ArrayList<>(3 + tableMapping.getColumnsMapping().size());
                for (List<Resource<T>> subResources : Lists.partition(resources, BATCH_SIZE)) {
                    for (Resource<T> resource : subResources) {
                        T attributes = resource.getAttributes();
                        values.clear();
                        for (var mapping : tableMapping.getColumnsMapping().values()) {
                            values.add(mapping.get(attributes));
                        }
                        values.add(networkUuid);
                        values.add(resource.getVariantNum());
                        values.add(resource.getId());
                        bindValues(preparedStmt, values);
                        preparedStmt.addBatch();
                    }
                    preparedStmt.executeBatch();
                }
            }
        });
    }

    public void deleteIdentifiable(UUID networkUuid, int variantNum, String id, String tableName) {
        try (var connection = dataSource.getConnection()) {
            try (var preparedStmt = connection.prepareStatement(QueryCatalog.buildDeleteIdentifiableQuery(tableName))) {
                preparedStmt.setObject(1, networkUuid);
                preparedStmt.setInt(2, variantNum);
                preparedStmt.setString(3, id);
                preparedStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    // substation

    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getSubstationMappings());
    }

    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return getIdentifiable(networkUuid, variantNum, substationId, mappings.getSubstationMappings());
    }

    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getSubstationMappings());
    }

    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getSubstationMappings());
    }

    public void deleteSubstation(UUID networkUuid, int variantNum, String substationId) {
        deleteIdentifiable(networkUuid, variantNum, substationId, SUBSTATION_TABLE);
    }

    // voltage level

    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getVoltageLevelMappings());
    }

    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getVoltageLevelMappings(), SUBSTATION_ID);
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum, String substationId) {
        return getIdentifiablesInContainer(networkUuid, variantNum, substationId, SUBSTATION_ID, mappings.getVoltageLevelMappings());
    }

    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiable(networkUuid, variantNum, voltageLevelId, mappings.getVoltageLevelMappings());
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getVoltageLevelMappings());
    }

    public void deleteVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        deleteIdentifiable(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL_TABLE);
    }

    // generator

    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getGeneratorMappings());
    }

    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return getIdentifiable(networkUuid, variantNum, generatorId, mappings.getGeneratorMappings());
    }

    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getGeneratorMappings());
    }

    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getGeneratorMappings());
    }

    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getGeneratorMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteGenerator(UUID networkUuid, int variantNum, String generatorId) {
        deleteIdentifiable(networkUuid, variantNum, generatorId, GENERATOR_TABLE);
    }

    // battery

    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getBatteryMappings());
    }

    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return getIdentifiable(networkUuid, variantNum, batteryId, mappings.getBatteryMappings());
    }

    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getBatteryMappings());
    }

    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getBatteryMappings());
    }

    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getBatteryMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteBattery(UUID networkUuid, int variantNum, String batteryId) {
        deleteIdentifiable(networkUuid, variantNum, batteryId, BATTERY_TABLE);
    }

    // load

    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getLoadMappings());
    }

    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return getIdentifiable(networkUuid, variantNum, loadId, mappings.getLoadMappings());
    }

    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getLoadMappings());
    }

    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getLoadMappings());
    }

    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getLoadMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteLoad(UUID networkUuid, int variantNum, String loadId) {
        deleteIdentifiable(networkUuid, variantNum, loadId, LOAD_TABLE);
    }

    // shunt compensator

    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getShuntCompensatorMappings());
    }

    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return getIdentifiable(networkUuid, variantNum, shuntCompensatorId, mappings.getShuntCompensatorMappings());
    }

    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getShuntCompensatorMappings());
    }

    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getShuntCompensatorMappings());
    }

    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getShuntCompensatorMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        deleteIdentifiable(networkUuid, variantNum, shuntCompensatorId, SHUNT_COMPENSATOR_TABLE);
    }

    // VSC converter station

    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getVscConverterStationMappings());
    }

    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return getIdentifiable(networkUuid, variantNum, vscConverterStationId, mappings.getVscConverterStationMappings());
    }

    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getVscConverterStationMappings());
    }

    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getVscConverterStationMappings());
    }

    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getVscConverterStationMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        deleteIdentifiable(networkUuid, variantNum, vscConverterStationId, VSC_CONVERTER_STATION_TABLE);
    }

    // LCC converter station

    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getLccConverterStationMappings());
    }

    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return getIdentifiable(networkUuid, variantNum, lccConverterStationId, mappings.getLccConverterStationMappings());
    }

    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getLccConverterStationMappings());
    }

    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getLccConverterStationMappings());
    }

    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getLccConverterStationMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        deleteIdentifiable(networkUuid, variantNum, lccConverterStationId, LCC_CONVERTER_STATION_TABLE);
    }

    // static var compensators

    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getStaticVarCompensatorMappings());
    }

    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return getIdentifiable(networkUuid, variantNum, staticVarCompensatorId, mappings.getStaticVarCompensatorMappings());
    }

    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getStaticVarCompensatorMappings());
    }

    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getStaticVarCompensatorMappings());
    }

    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getStaticVarCompensatorMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        deleteIdentifiable(networkUuid, variantNum, staticVarCompensatorId, STATIC_VAR_COMPENSATOR_TABLE);
    }

    // busbar section

    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getBusbarSectionMappings());
    }

    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getBusbarSectionMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return getIdentifiable(networkUuid, variantNum, busbarSectionId, mappings.getBusbarSectionMappings());
    }

    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getBusbarSectionMappings());
    }

    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getBusbarSectionMappings());
    }

    public void deleteBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        deleteIdentifiable(networkUuid, variantNum, busBarSectionId, BUSBAR_SECTION_TABLE);
    }

    // switch

    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getSwitchMappings());
    }

    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return getIdentifiable(networkUuid, variantNum, switchId, mappings.getSwitchMappings());
    }

    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getSwitchMappings());
    }

    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getSwitchMappings());
    }

    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getSwitchMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteSwitch(UUID networkUuid, int variantNum, String switchId) {
        deleteIdentifiable(networkUuid, variantNum, switchId, SWITCH_TABLE);
    }

    // 2 windings transformer

    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getTwoWindingsTransformerMappings());
    }

    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return getIdentifiable(networkUuid, variantNum, twoWindingsTransformerId, mappings.getTwoWindingsTransformerMappings());
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getTwoWindingsTransformerMappings());
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getTwoWindingsTransformerMappings());
    }

    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getTwoWindingsTransformerMappings());
    }

    public void deleteTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        deleteIdentifiable(networkUuid, variantNum, twoWindingsTransformerId, TWO_WINDINGS_TRANSFORMER_TABLE);
    }

    // 3 windings transformer

    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getThreeWindingsTransformerMappings());
    }

    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        return getIdentifiable(networkUuid, variantNum, threeWindingsTransformerId, mappings.getThreeWindingsTransformerMappings());
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getThreeWindingsTransformerMappings());
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getThreeWindingsTransformerMappings());
    }

    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getThreeWindingsTransformerMappings());
    }

    public void deleteThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        deleteIdentifiable(networkUuid, variantNum, threeWindingsTransformerId, THREE_WINDINGS_TRANSFORMER_TABLE);
    }

    // line

    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getLineMappings());
    }

    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return getIdentifiable(networkUuid, variantNum, lineId, mappings.getLineMappings());
    }

    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getLineMappings());
    }

    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getLineMappings());
    }

    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getLineMappings());
    }

    public void deleteLine(UUID networkUuid, int variantNum, String lineId) {
        deleteIdentifiable(networkUuid, variantNum, lineId, LINE_TABLE);
    }

    // Hvdc line

    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getHvdcLineMappings());
    }

    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return getIdentifiable(networkUuid, variantNum, hvdcLineId, mappings.getHvdcLineMappings());
    }

    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getHvdcLineMappings());
    }

    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getHvdcLineMappings());
    }

    public void deleteHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        deleteIdentifiable(networkUuid, variantNum, hvdcLineId, HVDC_LINE_TABLE);
    }

    // Dangling line

    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getDanglingLineMappings());
    }

    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return getIdentifiable(networkUuid, variantNum, danglingLineId, mappings.getDanglingLineMappings());
    }

    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getDanglingLineMappings());
    }

    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getDanglingLineMappings());
    }

    public void deleteDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        deleteIdentifiable(networkUuid, variantNum, danglingLineId, DANGLING_LINE_TABLE);
    }

    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getDanglingLineMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    // configured buses

    public void createBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        createIdentifiables(networkUuid, resources, mappings.getConfiguredBusMappings());
    }

    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return getIdentifiable(networkUuid, variantNum, busId, mappings.getConfiguredBusMappings());
    }

    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return getIdentifiables(networkUuid, variantNum, mappings.getConfiguredBusMappings());
    }

    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelId, mappings.getConfiguredBusMappings());
    }

    public void updateBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        updateIdentifiables(networkUuid, resources, mappings.getConfiguredBusMappings(), VOLTAGE_LEVEL_ID_COLUMN);
    }

    public void deleteBus(UUID networkUuid, int variantNum, String configuredBusId) {
        deleteIdentifiable(networkUuid, variantNum, configuredBusId, CONFIGURED_BUS_TABLE);
    }

    private static String getNonEmptyTable(ResultSet resultSet) throws SQLException {
        var metaData = resultSet.getMetaData();
        for (int col = 4; col <= metaData.getColumnCount(); col++) { // skip 3 first columns corresponding to first inner select
            if (metaData.getColumnName(col).equalsIgnoreCase(ID_COLUMN) && resultSet.getObject(col) != null) {
                return metaData.getTableName(col).toLowerCase();
            }
        }
        return null;
    }

    private static Map<Pair<String, String>, Integer> getColumnIndexByTableNameAndColumnName(ResultSet resultSet, String tableName) throws SQLException {
        Map<Pair<String, String>, Integer> columnIndexes = new HashMap<>();
        var metaData = resultSet.getMetaData();
        for (int col = 1; col <= metaData.getColumnCount(); col++) {
            if (metaData.getTableName(col).equalsIgnoreCase(tableName)) {
                columnIndexes.put(Pair.of(tableName, metaData.getColumnName(col).toLowerCase()), col);
            }
        }
        return columnIndexes;
    }

    public Optional<Resource<IdentifiableAttributes>> getIdentifiable(UUID networkUuid, int variantNum, String id) {
        try (var connection = dataSource.getConnection()) {
            var preparedStmt = connection.prepareStatement(QueryCatalog.buildGetIdentifiableForAllTablesQuery());
            preparedStmt.setObject(1, networkUuid);
            preparedStmt.setInt(2, variantNum);
            preparedStmt.setString(3, id);
            try (ResultSet resultSet = preparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    String tableName = getNonEmptyTable(resultSet);
                    if (tableName != null) {
                        TableMapping tableMapping = mappings.getTableMapping(tableName);
                        var columnIndexByTableAndColumnName = getColumnIndexByTableNameAndColumnName(resultSet, tableName);

                        IdentifiableAttributes attributes = tableMapping.getAttributesSupplier().get();
                        tableMapping.getColumnsMapping().forEach((columnName, columnMapping) -> {
                            Integer columnIndex = columnIndexByTableAndColumnName.get(Pair.of(tableName, columnName.toLowerCase()));
                            if (columnIndex == null) {
                                throw new PowsyblException("Column '" + columnName.toLowerCase() + "' of table '" + tableName + "' not found");
                            }
                            bindAttributes(resultSet, columnIndex, columnMapping, attributes);
                        });

                        return Optional.of(new Resource.Builder<>(tableMapping.getResourceType())
                                .id(id)
                                .variantNum(variantNum)
                                .attributes(attributes)
                                .build());
                    }
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
        return Optional.empty();
    }

    private List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        try (var connection = dataSource.getConnection()) {
            var voltageLevelMapping = mappings.getVoltageLevelMappings();
            var preparedStmt = connection.prepareStatement(QueryCatalog.buildGetVoltageLevelsInSubstationQuery(voltageLevelMapping.getColumnsMapping().keySet()));
            preparedStmt.setObject(1, networkUuid);
            preparedStmt.setInt(2, variantNum);
            preparedStmt.setString(3, substationId);
            List<Resource<VoltageLevelAttributes>> resources = new ArrayList<>(3);
            try (ResultSet resultSet = preparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    VoltageLevelAttributes attributes = new VoltageLevelAttributes();
                    MutableInt columnIndex = new MutableInt(2);
                    voltageLevelMapping.getColumnsMapping().forEach((columnName, columnMapping) -> {
                        bindAttributes(resultSet, columnIndex.getValue(), columnMapping, attributes);
                        columnIndex.increment();
                    });
                    resources.add(Resource.voltageLevelBuilder()
                            .id(resultSet.getString(1))
                            .variantNum(variantNum)
                            .attributes(attributes)
                            .build());
                }
            }
            return resources;
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends IdentifiableAttributes> Resource<IdentifiableAttributes> downcast(Resource<T> r) {
        return (Resource<IdentifiableAttributes>) r;
    }

    public List<Resource<IdentifiableAttributes>> getIdentifiablesWithSameSubstationAs(UUID networkUuid, int variantNum, String id) {
        Map<String, Resource<IdentifiableAttributes>> resourcesById = new LinkedHashMap<>();

        Resource<IdentifiableAttributes> resource = getIdentifiable(networkUuid, variantNum, id)
                .orElseThrow(() -> new PowsyblException("Identifiable '" + id + "' not found"));
        resourcesById.put(resource.getId(), resource);

        // get substation from given identifiable
        String substationId;
        if (resource.getType() == ResourceType.SUBSTATION) {
            substationId = id;
        } else if (resource.getType() == ResourceType.VOLTAGE_LEVEL) {
            substationId = ((VoltageLevelAttributes) resource.getAttributes()).getSubstationId();
        } else if (resource.getAttributes() instanceof InjectionAttributes) {
            String voltageLevelId = ((InjectionAttributes) resource.getAttributes()).getVoltageLevelId();
            Resource<VoltageLevelAttributes> voltageLevelResource = getVoltageLevel(networkUuid, variantNum, voltageLevelId).orElseThrow();
            substationId = voltageLevelResource.getAttributes().getSubstationId();
        } else {
            throw new PowsyblException("TODO");
        }

        // get list of voltage levels contained in this substation
        var voltageLevelResources = getVoltageLevelsInSubstation(networkUuid, variantNum, substationId);
        for (var voltageLevelResource : voltageLevelResources) {
            resourcesById.put(voltageLevelResource.getId(), downcast(voltageLevelResource));
        }

        // get identifiables contained in these voltage levels
        List<String> voltageLevelIds = voltageLevelResources.stream().map(Resource::getId).collect(Collectors.toList());
        for (var tableMapping : mappings.getNoneContainerTableMapping()) {
            for (Resource<IdentifiableAttributes> identifiableResource : getIdentifiablesInVoltageLevel(networkUuid, variantNum, voltageLevelIds, tableMapping)) {
                resourcesById.put(identifiableResource.getId(), identifiableResource);
            }
        }

        return new ArrayList<>(resourcesById.values());
    }
}
