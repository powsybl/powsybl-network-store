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
    static final String HVDC_LINE = "hvdcLine";
    static final String DANGLING_LINE = "danglingLine";
    static final String CONFIGURED_BUS = "configuredBus";
    static final String LOAD = "load";
    static final String LINE = "line";

    static final List<String> ELEMENT_TABLES = List.of(SUBSTATION, VOLTAGE_LEVEL, BUSBAR_SECTION, CONFIGURED_BUS, SWITCH, GENERATOR, BATTERY, LOAD, SHUNT_COMPENSATOR,
            STATIC_VAR_COMPENSATOR, VSC_CONVERTER_STATION, LCC_CONVERTER_STATION, TWO_WINDINGS_TRANSFORMER,
            THREE_WINDINGS_TRANSFORMER, LINE, HVDC_LINE, DANGLING_LINE);

    static final String NETWORK_UUID = "networkUuid";
    static final String VARIANT_NUM = "variantNum";
    static final String ID_STR = "id";
    static final String VOLTAGE_LEVEL_ID = "voltageLevelId";

    private QueryCatalog() {
    }

    public static String buildGetIdentifiableQuery(Collection<String> columns, String tableName) {
        StringBuilder builder = new StringBuilder("select");
        var it = columns.iterator();
        while (it.hasNext()) {
            String column = it.next();
            builder.append(" ").append(column);
            if (it.hasNext()) {
                builder.append(",");
            }
        }
        builder.append(" from ").append(tableName)
                .append(" where ").append(NETWORK_UUID).append(" = ?")
                .append(" and ").append(VARIANT_NUM).append(" = ?")
                .append(" and ").append(ID_STR).append(" = ?");
        return builder.toString();
    }

    public static String buildGetIdentifiablesQuery(Collection<String> columns, String tableName) {
        StringBuilder builder = new StringBuilder("select ").append(ID_STR);
        for (String column : columns) {
            builder.append(", ").append(column);
        }
        builder.append(" from ").append(tableName)
                .append(" where ").append(NETWORK_UUID).append(" = ?")
                .append(" and ").append(VARIANT_NUM).append(" = ?");
        return builder.toString();
    }

    public static String buildGetIdentifiablesInContainerQuery(Collection<String> columns, String tableName, String containerColumnName) {
        StringBuilder builder = new StringBuilder("select ").append(ID_STR);
        for (String column : columns) {
            builder.append(", ").append(column);
        }
        builder.append(" from ").append(tableName)
                .append(" where ").append(NETWORK_UUID).append(" = ?")
                .append(" and ").append(VARIANT_NUM).append(" = ?")
                .append(" and ").append(containerColumnName).append(" = ?");
        return builder.toString();
    }

    public static String buildGetIdentifiablesWithSideQuery(Collection<String> columns, String tableName, String side) {
        StringBuilder builder = new StringBuilder("select ").append(ID_STR);
        for (String column : columns) {
            builder.append(", ").append(column);
        }
        builder.append(" from ").append(tableName)
                .append(" where ").append(NETWORK_UUID).append(" = ?")
                .append(" and ").append(VARIANT_NUM).append(" = ?")
                .append(" and ").append(VOLTAGE_LEVEL_ID).append(side).append(" = ?");
        return builder.toString();
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

    public static String buildDeleteIdentifiablesQuery(String table) {
        return "delete from " + table + " where " + NETWORK_UUID + " = ?";
    }

    public static String buildDeleteIdentifiablesVariantQuery(String table) {
        return "delete from " + table + " where " + NETWORK_UUID + " = ? and " + VARIANT_NUM + " = ?";
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
}
