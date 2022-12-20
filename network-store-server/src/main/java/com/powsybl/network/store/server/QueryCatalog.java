/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.network.store.model.Resource;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.network.store.server.Mappings.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class QueryCatalog {

    private static final String MINIMAL_VALUE_REQUIREMENT_ERROR = "Function should not be called without at least one value.";

    static final String VARIANT_ID_COLUMN = "variantId";
    static final String UUID_COLUMN = "uuid";
    static final String NETWORK_UUID_COLUMN = "networkUuid";
    static final String VARIANT_NUM_COLUMN = "variantNum";
    static final String ID_COLUMN = "id";
    static final String VOLTAGE_LEVEL_ID_COLUMN = "voltageLevelId";
    static final String VOLTAGE_LEVEL_ID_1_COLUMN = "voltageLevelId1";
    static final String VOLTAGE_LEVEL_ID_2_COLUMN = "voltageLevelId2";
    static final String VOLTAGE_LEVEL_ID_3_COLUMN = "voltageLevelId3";
    static final String NAME_COLUMN = "name";
    static final String EQUIPMENT_TYPE_COLUMN = "equipmentType";
    static final String EQUIPMENT_ID_COLUMN = "equipmentId";
    static final String INDEX_COLUMN = "index";
    static final String TAPCHANGER_TYPE_COLUMN = "tapChangerType";
    static final String ALPHA_COLUMN = "alpha";

    static final String TAP_CHANGER_STEP_TABLE = "tapChangerStep";

    private QueryCatalog() {
    }

    public static String buildGetIdentifiableQuery(String tableName, Collection<String> columns) {
        return "select " +
                String.join(", ", columns) +
                " from " + tableName +
                " where " + NETWORK_UUID_COLUMN + " = ?" +
                " and " + VARIANT_NUM_COLUMN + " = ?" +
                " and " + ID_COLUMN + " = ?";
    }

    public static String buildGetNetworkQuery(Collection<String> columns) {
        return "select " + ID_COLUMN + ", " +
                String.join(", ", columns) +
                " from " + NETWORK_TABLE +
                " where " + UUID_COLUMN + " = ?" +
                " and " + VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildGetIdentifiablesQuery(String tableName, Collection<String> columns) {
        return "select " + ID_COLUMN + ", " +
                String.join(", ", columns) +
                " from " + tableName +
                " where " + NETWORK_UUID_COLUMN + " = ?" +
                " and " + VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildGetIdentifiablesInContainerQuery(String tableName, Collection<String> columns, Set<String> containerColumns) {
        StringBuilder sql = new StringBuilder()
                .append("select ").append(ID_COLUMN).append(", ")
                .append(String.join(", ", columns))
                .append(" from ").append(tableName)
                .append(" where ").append(NETWORK_UUID_COLUMN).append(" = ?")
                .append(" and ").append(VARIANT_NUM_COLUMN).append(" = ?")
                .append(" and (");
        var it = containerColumns.iterator();
        while (it.hasNext()) {
            String containerColumn = it.next();
            sql.append(containerColumn).append(" = ?");
            if (it.hasNext()) {
                sql.append(" or ");
            }
        }
        sql.append(")");
        return sql.toString();
    }

    public static String buildDeleteIdentifiableQuery(String tableName) {
        return "delete from " +
                tableName +
                " where " + NETWORK_UUID_COLUMN + " = ?" +
                " and " + VARIANT_NUM_COLUMN + " = ?" +
                " and " + ID_COLUMN + " = ?";
    }

    public static String buildDeleteNetworkQuery() {
        return "delete from " + NETWORK_TABLE + " where " + UUID_COLUMN + " = ?";
    }

    public static String buildDeleteNetworkVariantQuery() {
        return "delete from " + NETWORK_TABLE + " where " + UUID_COLUMN + " = ? and " + VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildDeleteIdentifiablesQuery(String tableName) {
        return "delete from " + tableName + " where " + NETWORK_UUID_COLUMN + " = ?";
    }

    public static String buildDeleteIdentifiablesVariantQuery(String tableName) {
        return "delete from " + tableName + " where " + NETWORK_UUID_COLUMN + " = ? and " + VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildInsertNetworkQuery(String tableName, Collection<String> columns) {
        return "insert into " + tableName +
                "(" + VARIANT_NUM_COLUMN + ", " + ID_COLUMN + ", " + String.join(", ", columns) +
                ") values (?, ?, " + columns.stream().map(s -> "?").collect(Collectors.joining(", "))
                + ")";
    }

    public static String buildInsertIdentifiableQuery(String tableName, Collection<String> columns) {
        return "insert into " + tableName +
                "(" + NETWORK_UUID_COLUMN + ", " + VARIANT_NUM_COLUMN + ", " + ID_COLUMN + ", " + String.join(", ", columns) +
                ") values (?, ?, ?, " + columns.stream().map(s -> "?").collect(Collectors.joining(", "))
                + ")";
    }

    public static String buildGetIdentifiableForAllTablesQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from (select ?::uuid networkUuid, ?::int variantNum, ?::varchar id) a");
        for (String table : ELEMENT_TABLES) {
            sql.append(" left outer join ").append(table)
                    .append(" on a.id = ")
                    .append(table)
                    .append(".id and a.networkUuid = ")
                    .append(table)
                    .append(".networkUuid and a.variantNum = ")
                    .append(table)
                    .append(".variantNum");
        }
        return sql.toString();
    }

    public static String buildGetNetworkInfos() {
        return "select " + UUID_COLUMN + ", " + ID_COLUMN +
                " from " + NETWORK_TABLE +
                " where " + VARIANT_NUM_COLUMN + " = " + Resource.INITIAL_VARIANT_NUM;
    }

    public static String buildGetVariantsInfos() {
        return "select " + VARIANT_ID_COLUMN + ", " + VARIANT_NUM_COLUMN +
                " from " + NETWORK_TABLE +
                " where " + UUID_COLUMN + " = ?";
    }

    public static String buildUpdateIdentifiableQuery(String tableName, Collection<String> columns, String columnToAddToWhereClause) {
        StringBuilder query = new StringBuilder("update ")
                .append(tableName)
                .append(" set ");
        var it = columns.iterator();
        while (it.hasNext()) {
            String column = it.next();
            if (!column.equals(columnToAddToWhereClause)) {
                query.append(column).append(" = ?");
                if (it.hasNext()) {
                    query.append(", ");
                }
            }
        }
        query.append(" where ").append(NETWORK_UUID_COLUMN).append(" = ? and ")
                .append(VARIANT_NUM_COLUMN).append(" = ? and ")
                .append(ID_COLUMN).append(" = ?");
        if (columnToAddToWhereClause != null) {
            query.append(" and ").append(columnToAddToWhereClause).append(" = ?");
        }
        return query.toString();
    }

    public static String buildUpdateInjectionSvQuery(String tableName) {
        return "update " +
                tableName +
                " set p = ?" +
                ", q = ?" +
                " where " + NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                ID_COLUMN + " = ?";
    }

    public static String buildUpdateBranchSvQuery(String tableName) {
        return "update " +
                tableName +
                " set p1 = ?" +
                ", q1 = ?" +
                ", p2 = ?" +
                ", q2 = ?" +
                " where " + NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                ID_COLUMN + " = ?";
    }

    public static String buildUpdateThreeWindingsTransformerSvQuery() {
        return "update " +
                THREE_WINDINGS_TRANSFORMER_TABLE +
                " set p1 = ?" +
                ", q1 = ?" +
                ", p2 = ?" +
                ", q2 = ?" +
                ", p3 = ?" +
                ", q3 = ?" +
                " where " + NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                ID_COLUMN + " = ?";
    }

    public static String buildUpdateVoltageLevelSvQuery() {
        return "update " +
                VOLTAGE_LEVEL_TABLE +
                " set calculatedbusesforbusview = ?" +
                ", calculatedbusesforbusbreakerview = ?" +
                " where " + NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                ID_COLUMN + " = ?";
    }

    public static String buildUpdateNetworkQuery(Collection<String> columns) {
        StringBuilder query = new StringBuilder("update ")
                .append(NETWORK_TABLE)
                .append(" set ").append(ID_COLUMN).append(" = ?");
        columns.forEach(column -> {
            if (!column.equals(UUID_COLUMN) && !column.equals(VARIANT_ID_COLUMN)) {
                query.append(", ").append(column).append(" = ?");
            }
        });
        query.append(" where ").append(UUID_COLUMN).append(" = ?")
                .append(" and ").append(VARIANT_NUM_COLUMN).append(" = ?");
        return query.toString();
    }

    public static String buildCloneIdentifiablesQuery(String tableName, Collection<String> columns) {
        return "insert into " + tableName + "(" +
                VARIANT_NUM_COLUMN + ", " +
                NETWORK_UUID_COLUMN + ", " +
                ID_COLUMN + ", " +
                String.join(",", columns) +
                ") " +
                "select " +
                "?" + "," +
                "?" + "," +
                ID_COLUMN + "," +
                String.join(",", columns) +
                " from " + tableName + " " +
                "where " + NETWORK_UUID_COLUMN + " = ? and " + VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildCloneNetworksQuery(Collection<String> columns) {
        return "insert into network(" +
                VARIANT_NUM_COLUMN + ", " +
                VARIANT_ID_COLUMN + ", " +
                UUID_COLUMN + ", " +
                ID_COLUMN + ", " +
                columns.stream().filter(column -> !column.equals(UUID_COLUMN) && !column.equals(VARIANT_ID_COLUMN) && !column.equals(NAME_COLUMN)).collect(Collectors.joining(",")) +
                ") " +
                "select" + " " +
                "?" + ", " +
                "?" + ", " +
                UUID_COLUMN + ", " +
                ID_COLUMN + ", " +
                columns.stream().filter(column -> !column.equals(UUID_COLUMN) && !column.equals(VARIANT_ID_COLUMN) && !column.equals(NAME_COLUMN)).collect(Collectors.joining(",")) +
                " from network" + " " +
                "where uuid = ? and " + VARIANT_NUM_COLUMN + " = ?";
    }

    // Temporary Limits
    public static String buildCloneTemporaryLimitsQuery() {
        return "insert into temporarylimit(" + EQUIPMENT_ID_COLUMN + ", " + EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + ", " + VARIANT_NUM_COLUMN + ", side, limitType, " + NAME_COLUMN +
                ", value_, acceptableDuration, fictitious) " + "select " + EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", ?, ?, side, limitType, " + NAME_COLUMN +
                ", value_, acceptableDuration, fictitious from temporarylimit where " + NETWORK_UUID_COLUMN +
                " = ? and " + VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildTemporaryLimitQuery(String columnNameForWhereClause) {
        return "select " + EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + ", " +
                VARIANT_NUM_COLUMN + ", " +
                "side, limitType, " +
                NAME_COLUMN + ", " +
                "value_, acceptableDuration, fictitious " +
                "from temporarylimit where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                columnNameForWhereClause + " = ?";
    }

    public static String buildTemporaryLimitWithInClauseQuery(String columnNameForInClause, int numberOfValues) {
        if (numberOfValues < 1) {
            throw new IllegalArgumentException(MINIMAL_VALUE_REQUIREMENT_ERROR);
        }
        return "select " + EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + ", " +
                VARIANT_NUM_COLUMN + ", " +
                "side, limitType, " +
                NAME_COLUMN + ", " +
                "value_, acceptableDuration, fictitious " +
                "from temporarylimit where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                columnNameForInClause + " in (" +
                "?, ".repeat(numberOfValues - 1) + "?)";
    }

    public static String buildInsertTemporaryLimitsQuery() {
        return "insert into temporarylimit(" +
                EQUIPMENT_ID_COLUMN + ", " + EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + " ," +
                VARIANT_NUM_COLUMN + ", side, limitType, " +
                NAME_COLUMN + ", value_, acceptableDuration, fictitious)" +
                " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    public static String buildDeleteTemporaryLimitsVariantEquipmentINQuery(int numberOfValues) {
        if (numberOfValues < 1) {
            throw new IllegalArgumentException(MINIMAL_VALUE_REQUIREMENT_ERROR);
        }
        return "delete from temporarylimit where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                EQUIPMENT_ID_COLUMN + " in (" +
                "?, ".repeat(numberOfValues - 1) + "?)";
    }

    public static String buildDeleteTemporaryLimitsVariantQuery() {
        return "delete from temporarylimit where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildDeleteTemporaryLimitsQuery() {
        return "delete from temporarylimit where " +
                NETWORK_UUID_COLUMN + " = ?";
    }

    // Reactive Capability Curve Point
    public static String buildCloneReactiveCapabilityCurvePointsQuery() {
        return "insert into ReactiveCapabilityCurvePoint(" + EQUIPMENT_ID_COLUMN + ", " + EQUIPMENT_TYPE_COLUMN +
                ", " + NETWORK_UUID_COLUMN + ", " + VARIANT_NUM_COLUMN + ", minQ, maxQ, p) select " +
                EQUIPMENT_ID_COLUMN + ", " + EQUIPMENT_TYPE_COLUMN +
                ", ?, ?, minQ, maxQ, p from ReactiveCapabilityCurvePoint where " + NETWORK_UUID_COLUMN +
                " = ? and " + VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildReactiveCapabilityCurvePointQuery(String columnNameForWhereClause) {
        return "select " + EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + ", " +
                VARIANT_NUM_COLUMN + ", " +
                "minQ, maxQ, p " +
                "from ReactiveCapabilityCurvePoint where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                columnNameForWhereClause + " = ?";
    }

    public static String buildReactiveCapabilityCurvePointWithInClauseQuery(String columnNameForInClause, int numberOfValues) {
        if (numberOfValues < 1) {
            throw new IllegalArgumentException(MINIMAL_VALUE_REQUIREMENT_ERROR);
        }
        return "select " + EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + ", " +
                VARIANT_NUM_COLUMN + ", " +
                "minQ, maxQ, p " +
                "from ReactiveCapabilityCurvePoint where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                columnNameForInClause + " in (" +
                "?, ".repeat(numberOfValues - 1) + "?)";
    }

    public static String buildInsertReactiveCapabilityCurvePointsQuery() {
        return "insert into ReactiveCapabilityCurvePoint(" +
                EQUIPMENT_ID_COLUMN + ", " + EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + " ," +
                VARIANT_NUM_COLUMN + ", minQ, maxQ, p)" +
                " values (?, ?, ?, ?, ?, ?, ?)";
    }

    public static String buildDeleteReactiveCapabilityCurvePointsVariantEquipmentINQuery(int numberOfValues) {
        if (numberOfValues < 1) {
            throw new IllegalArgumentException(MINIMAL_VALUE_REQUIREMENT_ERROR);
        }
        return "delete from ReactiveCapabilityCurvePoint where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                EQUIPMENT_ID_COLUMN + " in (" +
                "?, ".repeat(numberOfValues - 1) + "?)";
    }

    public static String buildDeleteReactiveCapabilityCurvePointsVariantQuery() {
        return "delete from ReactiveCapabilityCurvePoint where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildDeleteReactiveCapabilityCurvePointsQuery() {
        return "delete from ReactiveCapabilityCurvePoint where " +
                NETWORK_UUID_COLUMN + " = ?";
    }

    // Tap Changer Steps
    public static String buildCloneTapChangerStepQuery() {
        return "insert into " + TAP_CHANGER_STEP_TABLE + "(" +
                EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + "," +
                VARIANT_NUM_COLUMN + "," +
                INDEX_COLUMN + ", " +
                "side" + ", " +
                TAPCHANGER_TYPE_COLUMN + ", " +
                "rho" + ", " +
                "r" + ", " +
                "x" + ", " +
                "g" + ", " +
                "b" + ", " +
                ALPHA_COLUMN + ") " +
                "select " +
                EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", " +
                "?" + "," +
                "?" + "," +
                INDEX_COLUMN + ", " +
                "side" + ", " +
                TAPCHANGER_TYPE_COLUMN + ", " +
                "rho" + ", " +
                "r" + ", " +
                "x" + ", " +
                "g" + ", " +
                "b" + ", " +
                ALPHA_COLUMN +
                " from " + TAP_CHANGER_STEP_TABLE + " " +
                "where " +
                NETWORK_UUID_COLUMN + " = ?" + " and " +
                VARIANT_NUM_COLUMN + " = ? ";
    }

    public static String buildTapChangerStepQuery(String columnNameForWhereClause) {
        return "select " +
            EQUIPMENT_ID_COLUMN + ", " +
            EQUIPMENT_TYPE_COLUMN + ", " +
            NETWORK_UUID_COLUMN + "," +
            VARIANT_NUM_COLUMN + "," +
            INDEX_COLUMN + ", " +
            "side" + ", " +
            TAPCHANGER_TYPE_COLUMN + ", " +
            "rho" + ", " +
            "r" + ", " +
            "x" + ", " +
            "g" + ", " +
            "b" + ", " +
            ALPHA_COLUMN +
            " from " + TAP_CHANGER_STEP_TABLE + " " +
            "where " +
            NETWORK_UUID_COLUMN + " = ?" + " and " +
            VARIANT_NUM_COLUMN + " = ? and " +
            columnNameForWhereClause + " = ?";
    }

    public static String buildTapChangerStepWithInClauseQuery(String columnNameForInClause, int numberOfValues) {
        if (numberOfValues < 1) {
            throw new IllegalArgumentException(MINIMAL_VALUE_REQUIREMENT_ERROR);
        }
        return "select " +
                EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + "," +
                VARIANT_NUM_COLUMN + "," +
                INDEX_COLUMN + ", " +
                "side" + ", " +
                TAPCHANGER_TYPE_COLUMN + ", " +
                "rho" + ", " +
                "r" + ", " +
                "x" + ", " +
                "g" + ", " +
                "b" + ", " +
                ALPHA_COLUMN +
                " from " + TAP_CHANGER_STEP_TABLE + " " +
                "where " +
                NETWORK_UUID_COLUMN + " = ?" + " and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                columnNameForInClause + " in (" +
                "?, ".repeat(numberOfValues - 1) + "?)";
    }

    public static String buildInsertTapChangerStepQuery() {
        return "insert into " + TAP_CHANGER_STEP_TABLE +
                "(" +
                EQUIPMENT_ID_COLUMN + ", " +
                EQUIPMENT_TYPE_COLUMN + ", " +
                NETWORK_UUID_COLUMN + "," +
                VARIANT_NUM_COLUMN + "," +
                INDEX_COLUMN + ", " +
                "side" + ", " +
                TAPCHANGER_TYPE_COLUMN + ", " +
                "rho" + ", " +
                "r" + ", " +
                "x" + ", " +
                "g" + ", " +
                "b" + ", " +
                ALPHA_COLUMN + ")" +
                " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    public static String buildDeleteTapChangerStepQuery() {
        return "delete from " + TAP_CHANGER_STEP_TABLE +
               " where " +
               NETWORK_UUID_COLUMN + " = ?";
    }

    public static String buildDeleteTapChangerStepVariantQuery() {
        return "delete from " + TAP_CHANGER_STEP_TABLE +
               " where " +
               NETWORK_UUID_COLUMN + " = ?" + " and " +
               VARIANT_NUM_COLUMN + " = ?";
    }

    public static String buildDeleteTapChangerStepVariantEquipmentINQuery(int numberOfValues) {
        if (numberOfValues < 1) {
            throw new IllegalArgumentException(MINIMAL_VALUE_REQUIREMENT_ERROR);
        }
        return "delete from " + TAP_CHANGER_STEP_TABLE +
                " where " +
                NETWORK_UUID_COLUMN + " = ? and " +
                VARIANT_NUM_COLUMN + " = ? and " +
                EQUIPMENT_ID_COLUMN + " in (" +
                "?, ".repeat(numberOfValues - 1) + "?)";
    }
}
