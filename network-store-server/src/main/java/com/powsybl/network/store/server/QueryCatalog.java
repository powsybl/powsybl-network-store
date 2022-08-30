/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.network.store.model.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class QueryCatalog {

    static final String VARIANT_ID = "variantId";
    static final String UUID_STR = "uuid";
    static final String NETWORK = "network";
    static final String SUBSTATION = "substation";
    static final String VOLTAGE_LEVEL = "voltageLevel";
    static final String GENERATOR = "generator";
    static final String BATTERY = "battery";
    static final String SHUNT_COMPENSATOR = "shuntCompensator";
    static final String VSC_CONVERTER_STATION = "vscConverterStation";
    static final String LCC_CONVERTER_STATION = "lccConverterStation";
    static final String STATIC_VAR_COMPENSATOR = "staticVarCompensator";
    static final String BUSBAR_SECTION = "busbarSection";
    static final String SWITCH = "switch";
    static final String TWO_WINDINGS_TRANSFORMER = "twoWindingsTransformer";
    static final String THREE_WINDINGS_TRANSFORMER = "threeWindingsTransformer";
    static final String TEMPORARY_LIMIT = "temporaryLimit";
    static final String HVDC_LINE = "hvdcLine";
    static final String DANGLING_LINE = "danglingLine";
    static final String CONFIGURED_BUS = "configuredBus";
    static final String LOAD = "load";
    static final String LINE = "line";

    static final List<String> ELEMENT_TABLES = List.of(SUBSTATION, VOLTAGE_LEVEL, BUSBAR_SECTION, CONFIGURED_BUS, SWITCH, GENERATOR, BATTERY, LOAD, SHUNT_COMPENSATOR,
            STATIC_VAR_COMPENSATOR, VSC_CONVERTER_STATION, LCC_CONVERTER_STATION, TWO_WINDINGS_TRANSFORMER,
            THREE_WINDINGS_TRANSFORMER, TEMPORARY_LIMIT, LINE, HVDC_LINE, DANGLING_LINE);

    static final String NETWORK_UUID = "networkUuid";
    static final String VARIANT_NUM = "variantNum";
    static final String ID_STR = "id";
    static final String VOLTAGE_LEVEL_ID = "voltageLevelId";
    static final String NAME = "name";
    static final String EQUIPMENT_TYPE = "equipmentType";
    static final String EQUIPMENT_ID = "equipmentId";

    private QueryCatalog() {
    }

    public static String buildGetIdentifiableQuery(String tableName, Collection<String> columns) {
        return "select " +
                String.join(", ", columns) +
                " from " + tableName +
                " where " + NETWORK_UUID + " = ?" +
                " and " + VARIANT_NUM + " = ?" +
                " and " + ID_STR + " = ?";
    }

    public static String buildGetNetworkQuery(Collection<String> columns) {
        return "select " + ID_STR + ", " +
                String.join(", ", columns) +
                " from " + NETWORK +
                " where " + UUID_STR + " = ?" +
                " and " + VARIANT_NUM + " = ?";
    }

    public static String buildGetIdentifiablesQuery(String tableName, Collection<String> columns) {
        return "select " + ID_STR + ", " +
                String.join(", ", columns) +
                " from " + tableName +
                " where " + NETWORK_UUID + " = ?" +
                " and " + VARIANT_NUM + " = ?";
    }

    public static String buildGetIdentifiablesInContainerQuery(String tableName, Collection<String> columns, Set<String> containerColumns) {
        StringBuilder sql = new StringBuilder()
                .append("select ").append(ID_STR).append(", ")
                .append(String.join(", ", columns))
                .append(" from ").append(tableName)
                .append(" where ").append(NETWORK_UUID).append(" = ?")
                .append(" and ").append(VARIANT_NUM).append(" = ?")
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
                " where " + NETWORK_UUID + " = ?" +
                " and " + VARIANT_NUM + " = ?" +
                " and " + ID_STR + " = ?";
    }

    public static String buildDeleteNetworkQuery() {
        return "delete from " + NETWORK + " where " + UUID_STR + " = ?";
    }

    public static String buildDeleteNetworkVariantQuery() {
        return "delete from " + NETWORK + " where " + UUID_STR + " = ? and " + VARIANT_NUM + " = ?";
    }

    public static String buildDeleteIdentifiablesQuery(String tableName) {
        return "delete from " + tableName + " where " + NETWORK_UUID + " = ?";
    }

    public static String buildDeleteIdentifiablesVariantQuery(String tableName) {
        return "delete from " + tableName + " where " + NETWORK_UUID + " = ? and " + VARIANT_NUM + " = ?";
    }

    public static String buildInsertNetworkQuery(String tableName, Collection<String> columns) {
        return "insert into " + tableName +
                "(" + VARIANT_NUM + ", " + ID_STR + ", " + String.join(", ", columns) +
                ") values (?, ?, " + columns.stream().map(s -> "?").collect(Collectors.joining(", "))
                + ")";
    }

    public static String buildInsertIdentifiableQuery(String tableName, Collection<String> columns) {
        return "insert into " + tableName +
                "(" + NETWORK_UUID + ", " + VARIANT_NUM + ", " + ID_STR + ", " + String.join(", ", columns) +
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
        return "select " + UUID_STR + ", " + ID_STR +
                " from " + NETWORK +
                " where " + VARIANT_NUM + " = " + Resource.INITIAL_VARIANT_NUM;
    }

    public static String buildGetVariantsInfos() {
        return "select " + VARIANT_ID + ", " + VARIANT_NUM +
                " from " + NETWORK +
                " where " + UUID_STR + " = ?";
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
        query.append(" where ").append(NETWORK_UUID).append(" = ? and ")
                .append(VARIANT_NUM).append(" = ? and ")
                .append(ID_STR).append(" = ?");
        if (columnToAddToWhereClause != null) {
            query.append(" and ").append(columnToAddToWhereClause).append(" = ?");
        }
        return query.toString();
    }

    public static String buildUpdateNetworkQuery(Collection<String> columns) {
        StringBuilder query = new StringBuilder("update ")
                .append(NETWORK)
                .append(" set ").append(ID_STR).append(" = ?");
        columns.forEach(column -> {
            if (!column.equals(UUID_STR) && !column.equals(VARIANT_ID)) {
                query.append(", ").append(column).append(" = ?");
            }
        });
        query.append(" where ").append(UUID_STR).append(" = ?")
                .append(" and ").append(VARIANT_NUM).append(" = ?");
        return query.toString();
    }

    public static String buildCloneIdentifiablesQuery(String tableName, Collection<String> columns) {
        return "insert into " + tableName + "(" +
                VARIANT_NUM + ", " +
                NETWORK_UUID + ", " +
                ID_STR + ", " +
                String.join(",", columns) +
                ") " +
                "select " +
                "?" + "," +
                "?" + "," +
                ID_STR + "," +
                String.join(",", columns) +
                " from " + tableName + " " +
                "where networkUuid = ? and variantNum = ?";
    }

    public static String buildCloneNetworksQuery(Collection<String> columns) {
        return "insert into network(" +
                VARIANT_NUM + ", " +
                VARIANT_ID + ", " +
                UUID_STR + ", " +
                ID_STR + ", " +
                columns.stream().filter(column -> !column.equals(UUID_STR) && !column.equals(VARIANT_ID) && !column.equals(NAME)).collect(Collectors.joining(",")) +
                ") " +
                "select" + " " +
                "?" + ", " +
                "?" + ", " +
                UUID_STR + ", " +
                ID_STR + ", " +
                columns.stream().filter(column -> !column.equals(UUID_STR) && !column.equals(VARIANT_ID) && !column.equals(NAME)).collect(Collectors.joining(",")) +
                " from network" + " " +
                "where uuid = ? and variantNum = ?";
    }

    public static String buildTemporaryLimitQuery(Collection<String> columns) {
        StringBuilder query = new StringBuilder("select equipmentId, equipmentType, networkUuid, variantNum, side, ")
                .append("limitType, name, value, acceptableDuration, fictitious ")
                .append("from temporarylimit where ")
                .append("networkUuid = ? and variantNum = ? ");
        columns.forEach(column -> query.append("and ").append(column).append(" = ? "));
        return query.toString();

    }
}
