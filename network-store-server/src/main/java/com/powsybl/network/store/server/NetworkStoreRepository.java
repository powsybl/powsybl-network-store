/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import java.lang.reflect.InvocationTargetException;
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

    private PreparedStatement psInsertNetwork;
    private Supplier<java.sql.PreparedStatement> psCloneNetworkSupplier;
    private PreparedStatement psUpdateNetwork;
    private PreparedStatement psInsertSubstation;
    private PreparedStatement psUpdateSubstation;
    private PreparedStatement psInsertVoltageLevel;
    private PreparedStatement psUpdateVoltageLevel;
    private PreparedStatement psInsertGenerator;
    private PreparedStatement psUpdateGenerator;
    private PreparedStatement psInsertBattery;
    private PreparedStatement psUpdateBattery;
    private PreparedStatement psInsertLoad;
    private PreparedStatement psUpdateLoad;
    private PreparedStatement psInsertShuntCompensator;
    private PreparedStatement psUpdateShuntCompensator;
    private PreparedStatement psInsertVscConverterStation;
    private PreparedStatement psUpdateVscConverterStation;
    private PreparedStatement psInsertLccConverterStation;
    private PreparedStatement psUpdateLccConverterStation;
    private PreparedStatement psInsertStaticVarCompensator;
    private PreparedStatement psUpdateStaticVarCompensator;
    private PreparedStatement psInsertBusbarSection;
    private PreparedStatement psUpdateBusbarSection;
    private PreparedStatement psInsertSwitch;
    private PreparedStatement psUpdateSwitch;
    private PreparedStatement psInsertTwoWindingsTransformer;
    private PreparedStatement psUpdateTwoWindingsTransformer;
    private PreparedStatement psInsertThreeWindingsTransformer;
    private PreparedStatement psUpdateThreeWindingsTransformer;
    private PreparedStatement psInsertLine;
    private PreparedStatement psUpdateLines;
    private PreparedStatement psInsertHvdcLine;
    private PreparedStatement psUpdateHvdcLine;
    private PreparedStatement psInsertDanglingLine;
    private PreparedStatement psUpdateDanglingLine;
    private PreparedStatement psInsertConfiguredBus;
    private PreparedStatement psUpdateConfiguredBus;

    private final Map<String, Supplier<java.sql.PreparedStatement>> clonePreparedStatementsSupplier = new LinkedHashMap<>();

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

    private static final List<String> ELEMENT_TABLES = List.of(SUBSTATION, VOLTAGE_LEVEL, BUSBAR_SECTION, CONFIGURED_BUS, SWITCH, GENERATOR, BATTERY, LOAD, SHUNT_COMPENSATOR,
            STATIC_VAR_COMPENSATOR, VSC_CONVERTER_STATION, LCC_CONVERTER_STATION, TWO_WINDINGS_TRANSFORMER,
            THREE_WINDINGS_TRANSFORMER, LINE, HVDC_LINE, DANGLING_LINE);

    private Mappings mappings = new Mappings();

    private PreparedStatement buildInsertStatement(Map<String, Mapping> mapping, String tableName) {
        Set<String> keys = mapping.keySet();
        Insert insert = insertInto(tableName)
            .value("networkUuid", bindMarker())
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keys.forEach(k -> insert.value(k, bindMarker()));
        return session.prepare(insert.build());
    }

    private Supplier<java.sql.PreparedStatement> buildCloneStatement(Map<String, Mapping> mapping, String tableName) {
        Set<String> keys = mapping.keySet();
        return () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into " + tableName + "(" +
                        VARIANT_NUM + ", " +
                        "networkUuid" + ", " +
                        "id" + ", " +
                        String.join(",", keys) +
                        ") " +
                        "select " +
                        "?" + "," +
                        "networkUuid" + "," +
                        "id" + "," +
                        String.join(",", keys) +
                        " from " + tableName + " " +
                        "where networkUuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
                update.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        Update newUpdate = update
            .whereColumn("networkUuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker())
            .whereColumn("id").isEqualTo(bindMarker());
        if (columnToAddToWhereClause != null) {
            newUpdate = newUpdate.whereColumn(columnToAddToWhereClause).isEqualTo(bindMarker());
        }
        return session.prepare(newUpdate.build());
    }

    @PostConstruct
    void prepareStatements() {
        // network

        Set<String> keysNetworks = mappings.getNetworkMappings().keySet();
        Insert insertNetwork = insertInto(NETWORK)
            .value(VARIANT_NUM, bindMarker())
            .value("id", bindMarker());
        keysNetworks.forEach(k -> insertNetwork.value(k, bindMarker()));
        psInsertNetwork = session.prepare(insertNetwork.build());

        psCloneNetworkSupplier = () -> {
            try {
                return session.conn.prepareStatement(
                    "insert into network(" +
                        VARIANT_NUM + ", " +
                        VARIANT_ID + ", " +
                        "uuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysNetworks.stream().filter(k -> !k.equals("uuid") && !k.equals(VARIANT_ID) && !k.equals("name")).collect(Collectors.toList())) +
                        ") " +
                        "select" + " " +
                        "?" + ", " +
                        "?" + ", " +
                        "uuid" + ", " +
                        "id" + ", " +
                        String.join(",", keysNetworks.stream().filter(k -> !k.equals("uuid") && !k.equals(VARIANT_ID) && !k.equals("name")).collect(Collectors.toList())) +
                        " from network" + " " +
                        "where uuid = ? and variantNum = ?"
                );
            } catch (SQLException e) {
                throw new PowsyblException(e);
            }
        };

        Update updateNetwork = update(NETWORK).set(Assignment.setColumn("id", bindMarker()));
        keysNetworks.forEach(k -> {
            if (!k.equals("uuid") && !k.equals("variantId")) {
                updateNetwork.set(Assignment.setColumn(k, bindMarker()));
            }
        });
        updateNetwork
            .whereColumn("uuid").isEqualTo(bindMarker())
            .whereColumn(VARIANT_NUM).isEqualTo(bindMarker());
        psUpdateNetwork = session.prepare(updateNetwork.build());

        // substation

        psInsertSubstation = buildInsertStatement(mappings.getSubstationMappings(), SUBSTATION);
        clonePreparedStatementsSupplier.put(SUBSTATION, buildCloneStatement(mappings.getSubstationMappings(), SUBSTATION));
        psUpdateSubstation = buildUpdateStatement(mappings.getSubstationMappings(), SUBSTATION, null);

        // voltage level

        psInsertVoltageLevel = buildInsertStatement(mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL);
        clonePreparedStatementsSupplier.put(VOLTAGE_LEVEL, buildCloneStatement(mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL));
        psUpdateVoltageLevel = buildUpdateStatement(mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL, "substationId");

        // generator

        psInsertGenerator = buildInsertStatement(mappings.getGeneratorMappings(), GENERATOR);
        clonePreparedStatementsSupplier.put(GENERATOR, buildCloneStatement(mappings.getGeneratorMappings(), GENERATOR));
        psUpdateGenerator = buildUpdateStatement(mappings.getGeneratorMappings(), GENERATOR, "voltageLevelId");

        // battery

        psInsertBattery = buildInsertStatement(mappings.getBatteryMappings(), BATTERY);
        clonePreparedStatementsSupplier.put(BATTERY, buildCloneStatement(mappings.getBatteryMappings(), BATTERY));
        psUpdateBattery = buildUpdateStatement(mappings.getBatteryMappings(), BATTERY, "voltageLevelId");

        // load

        psInsertLoad = buildInsertStatement(mappings.getLoadMappings(), LOAD);
        clonePreparedStatementsSupplier.put(LOAD, buildCloneStatement(mappings.getLoadMappings(), LOAD));
        psUpdateLoad = buildUpdateStatement(mappings.getLoadMappings(), LOAD, "voltageLevelId");

        // shunt compensator

        psInsertShuntCompensator = buildInsertStatement(mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR);
        clonePreparedStatementsSupplier.put(SHUNT_COMPENSATOR, buildCloneStatement(mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR));
        psUpdateShuntCompensator = buildUpdateStatement(mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR, "voltageLevelId");

        // vsc converter station

        psInsertVscConverterStation = buildInsertStatement(mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION);
        clonePreparedStatementsSupplier.put(VSC_CONVERTER_STATION, buildCloneStatement(mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION));
        psUpdateVscConverterStation = buildUpdateStatement(mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION, "voltageLevelId");

        // lcc converter station

        psInsertLccConverterStation  = buildInsertStatement(mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION);
        clonePreparedStatementsSupplier.put(LCC_CONVERTER_STATION, buildCloneStatement(mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION));
        psUpdateLccConverterStation = buildUpdateStatement(mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION, "voltageLevelId");

        // static var compensator

        psInsertStaticVarCompensator = buildInsertStatement(mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR);
        clonePreparedStatementsSupplier.put(STATIC_VAR_COMPENSATOR, buildCloneStatement(mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR));
        psUpdateStaticVarCompensator = buildUpdateStatement(mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR, "voltageLevelId");

        // busbar section

        psInsertBusbarSection = buildInsertStatement(mappings.getBusbarSectionMappings(), BUSBAR_SECTION);
        clonePreparedStatementsSupplier.put(BUSBAR_SECTION, buildCloneStatement(mappings.getBusbarSectionMappings(), BUSBAR_SECTION));
        psUpdateBusbarSection = buildUpdateStatement(mappings.getBusbarSectionMappings(), BUSBAR_SECTION, "voltageLevelId");

        // switch

        psInsertSwitch  = buildInsertStatement(mappings.getSwitchMappings(), SWITCH);
        clonePreparedStatementsSupplier.put(SWITCH, buildCloneStatement(mappings.getSwitchMappings(), SWITCH));
        psUpdateSwitch = buildUpdateStatement(mappings.getSwitchMappings(), SWITCH, "voltageLevelId");

        // two windings transformer

        psInsertTwoWindingsTransformer = buildInsertStatement(mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER);
        clonePreparedStatementsSupplier.put(TWO_WINDINGS_TRANSFORMER, buildCloneStatement(mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER));
        psUpdateTwoWindingsTransformer = buildUpdateStatement(mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER, null);

        // three windings transformer

        psInsertThreeWindingsTransformer = buildInsertStatement(mappings.getThreeWindingsTransformerMappings(), THREE_WINDINGS_TRANSFORMER);
        clonePreparedStatementsSupplier.put(THREE_WINDINGS_TRANSFORMER, buildCloneStatement(mappings.getThreeWindingsTransformerMappings(), THREE_WINDINGS_TRANSFORMER));
        psUpdateThreeWindingsTransformer = buildUpdateStatement(mappings.getThreeWindingsTransformerMappings(), THREE_WINDINGS_TRANSFORMER, null);

        // line

        psInsertLine = buildInsertStatement(mappings.getLineMappings(), LINE);
        clonePreparedStatementsSupplier.put(LINE, buildCloneStatement(mappings.getLineMappings(), LINE));
        psUpdateLines = buildUpdateStatement(mappings.getLineMappings(), LINE, null);

        // hvdc line

        psInsertHvdcLine = buildInsertStatement(mappings.getHvdcLineMappings(), HVDC_LINE);
        clonePreparedStatementsSupplier.put(HVDC_LINE, buildCloneStatement(mappings.getHvdcLineMappings(), HVDC_LINE));
        psUpdateHvdcLine = buildUpdateStatement(mappings.getHvdcLineMappings(), HVDC_LINE, null);

        // dangling line

        psInsertDanglingLine = buildInsertStatement(mappings.getDanglingLineMappings(), DANGLING_LINE);
        clonePreparedStatementsSupplier.put(DANGLING_LINE, buildCloneStatement(mappings.getDanglingLineMappings(), DANGLING_LINE));
        psUpdateDanglingLine = buildUpdateStatement(mappings.getDanglingLineMappings(), DANGLING_LINE, "voltageLevelId");

        // configured bus

        psInsertConfiguredBus = buildInsertStatement(mappings.getConfiguredBusMappings(), CONFIGURED_BUS);
        clonePreparedStatementsSupplier.put(CONFIGURED_BUS, buildCloneStatement(mappings.getConfiguredBusMappings(), CONFIGURED_BUS));
        psUpdateConfiguredBus = buildUpdateStatement(mappings.getConfiguredBusMappings(), CONFIGURED_BUS, "voltageLevelId");
    }

    // network

    /**
     * Get all networks infos.
     */
    public List<NetworkInfos> getNetworksInfos() {
        SimpleStatement simpleStatement = selectFrom(NETWORK).columns("uuid", "id")
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
                .whereColumn("uuid").isEqualTo(literal(networkUuid)).build())) {
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
        columnMappings.add("id");
        try (ResultSet resultSet = session.execute(selectFrom(NETWORK)
            .columns(columnMappings.toArray(new String[0]))
            .whereColumn("uuid").isEqualTo(literal(uuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                NetworkAttributes networkAttributes = new NetworkAttributes();
                mappingNetworks.entrySet().forEach(entry -> entry.getValue().set(networkAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.networkBuilder()
                    .id(one.get("id", String.class))
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

        for (List<Resource<NetworkAttributes>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<NetworkAttributes> resource : subresources) {
                NetworkAttributes networkAttributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                values.add(resource.getId());
                keysNetworks.forEach(key -> {
                    if (!key.equals("uuid") && !key.equals("variantId")) {
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
        session.execute(deleteFrom(NETWORK).whereColumn("uuid").isEqualTo(literal(uuid)).build());
        for (String table : ELEMENT_TABLES) {
            session.execute(deleteFrom(table).whereColumn("networkUuid").isEqualTo(literal(uuid)).build());
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
                .whereColumn("uuid").isEqualTo(literal(uuid))
                .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
                .build());
        for (String table : ELEMENT_TABLES) {
            session.execute(deleteFrom(table)
                    .whereColumn("networkUuid").isEqualTo(literal(uuid))
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

    public <T extends IdentifiableAttributes> void createEquipments(UUID networkUuid, List<Resource<T>> resources,
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

    public <T extends IdentifiableAttributes> Optional<Resource<T>> getEquipment(UUID networkUuid, int variantNum, String equipmentId,
                                                                                 Map<String, Mapping> mappings, String tableName,
                                                                                 Resource.Builder<T> builder,
                                                                                 Class<T> classz) {
        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(mappings.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(equipmentId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                T attributes = classz.getDeclaredConstructor().newInstance();
                mappings.entrySet().forEach(entry -> entry.getValue().set(attributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(builder
                    .id(equipmentId)
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return Optional.empty();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getEquipments(UUID networkUuid, int variantNum,
                                                                              Map<String, Mapping> mappings, String tableName,
                                                                              Resource.Builder<T> builder,
                                                                              Class<T> classz) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = classz.getDeclaredConstructor().newInstance();

                mappings.entrySet().forEach(entry -> entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(builder
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Collections.emptyList();
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getContainerEquipments(UUID networkUuid, int variantNum, String containerId,
                                                                                       String containerColumnName,
                                                                                       Map<String, Mapping> mappings, String tableName,
                                                                                       Resource.Builder<T> builder,
                                                                                       Class<T> classz) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn(containerColumnName).isEqualTo(literal(containerId))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = classz.getDeclaredConstructor().newInstance();

                mappings.entrySet().forEach(entry -> entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(builder
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Collections.emptyList();
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getVoltageLevelEquipmentsWithSide(UUID networkUuid, int variantNum, String voltageLevelId,
                                                                                                  Branch.Side side,
                                                                                                  Map<String, Mapping> mappings, String tableName,
                                                                                                  Resource.Builder<T> builder,
                                                                                                  Class<T> classz) {
        Set<String> columns = new HashSet<>(mappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(tableName)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId" + (side == Branch.Side.ONE ? 1 : 2)).isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<T>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                T attributes = classz.getDeclaredConstructor().newInstance();

                mappings.entrySet().forEach(entry -> entry.getValue().set(attributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(builder
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(attributes)
                    .build());
            }
            return resources;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Collections.emptyList();
        }
    }

    public <T extends IdentifiableAttributes & Contained> void updateEquipments(UUID networkUuid, List<Resource<T>> resources,
                                                                                Map<String, Mapping> mappings, PreparedStatement psUpdate,
                                                                                String columnToExclude) {
        Set<String> keys = mappings.keySet();

        for (List<Resource<T>> subresources : Lists.partition(resources, BATCH_SIZE)) {
            BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED);
            List<BoundStatement> boundStatements = new ArrayList<>();
            for (Resource<T> resource : subresources) {
                T attributes = resource.getAttributes();
                List<Object> values = new ArrayList<>();
                keys.forEach(key -> {
                    if (!key.equals(columnToExclude)) {
                        values.add(mappings.get(key).get(attributes));
                    }
                });
                values.add(networkUuid);
                values.add(resource.getVariantNum());
                values.add(resource.getId());
                values.add(resource.getAttributes().getContainerIds().stream().findAny().get());

                boundStatements.add(psUpdate.bind(values.toArray(new Object[0])));
            }
            batch = batch.addAll(boundStatements);
            session.execute(batch);
        }
    }

    public <T extends IdentifiableAttributes> void updateEquipments2(UUID networkUuid, List<Resource<T>> resources,
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

    public void deleteEquipment(UUID networkUuid, int variantNum, String equipmentId, String tableName) {
        session.execute(deleteFrom(tableName)
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(equipmentId))
            .build());
    }

    // substation

    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getSubstationMappings(), SUBSTATION, Resource.substationBuilder(), SubstationAttributes.class);
    }

    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return getEquipment(networkUuid, variantNum, substationId, mappings.getSubstationMappings(),
            SUBSTATION, Resource.substationBuilder(), SubstationAttributes.class);
    }

    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getSubstationMappings(), psInsertSubstation);
    }

    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> resources) {
        updateEquipments2(networkUuid, resources, mappings.getSubstationMappings(), psUpdateSubstation);
    }

    public void deleteSubstation(UUID networkUuid, int variantNum, String substationId) {
        deleteEquipment(networkUuid, variantNum, substationId, SUBSTATION);
    }

    // voltage level

    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getVoltageLevelMappings(), psInsertVoltageLevel);
    }

    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getVoltageLevelMappings(), psUpdateVoltageLevel, "substationId");
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum, String substationId) {
        return getContainerEquipments(networkUuid, variantNum, substationId, "substationId", mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes.class);
    }

    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getEquipment(networkUuid, variantNum, voltageLevelId, mappings.getVoltageLevelMappings(),
            VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes.class);
    }

    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getVoltageLevelMappings(), VOLTAGE_LEVEL, Resource.voltageLevelBuilder(), VoltageLevelAttributes.class);
    }

    public void deleteVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        deleteEquipment(networkUuid, variantNum, voltageLevelId, VOLTAGE_LEVEL);
    }

    // generator

    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getGeneratorMappings(), psInsertGenerator);
    }

    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return getEquipment(networkUuid, variantNum, generatorId, mappings.getGeneratorMappings(),
            GENERATOR, Resource.generatorBuilder(), GeneratorAttributes.class);
    }

    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getGeneratorMappings(), GENERATOR, Resource.generatorBuilder(), GeneratorAttributes.class);
    }

    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getGeneratorMappings(), GENERATOR, Resource.generatorBuilder(), GeneratorAttributes.class);
    }

    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getGeneratorMappings(), psUpdateGenerator, "voltageLevelId");
    }

    public void deleteGenerator(UUID networkUuid, int variantNum, String generatorId) {
        deleteEquipment(networkUuid, variantNum, generatorId, GENERATOR);
    }

    // battery

    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getBatteryMappings(), psInsertBattery);
    }

    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return getEquipment(networkUuid, variantNum, batteryId, mappings.getBatteryMappings(),
            BATTERY, Resource.batteryBuilder(), BatteryAttributes.class);
    }

    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getBatteryMappings(), BATTERY, Resource.batteryBuilder(), BatteryAttributes.class);
    }

    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getBatteryMappings(), BATTERY, Resource.batteryBuilder(), BatteryAttributes.class);
    }

    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getBatteryMappings(), psUpdateBattery, "voltageLevelId");
    }

    public void deleteBattery(UUID networkUuid, int variantNum, String batteryId) {
        deleteEquipment(networkUuid, variantNum, batteryId, BATTERY);
    }

    // load

    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getLoadMappings(), psInsertLoad);
    }

    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return getEquipment(networkUuid, variantNum, loadId, mappings.getLoadMappings(),
                           LOAD, Resource.loadBuilder(), LoadAttributes.class);
    }

    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getLoadMappings(), LOAD, Resource.loadBuilder(), LoadAttributes.class);
    }

    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getLoadMappings(), LOAD, Resource.loadBuilder(), LoadAttributes.class);
    }

    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getLoadMappings(), psUpdateLoad, "voltageLevelId");
    }

    public void deleteLoad(UUID networkUuid, int variantNum, String loadId) {
        deleteEquipment(networkUuid, variantNum, loadId, LOAD);
    }

    // shunt compensator

    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getShuntCompensatorMappings(), psInsertShuntCompensator);
    }

    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return getEquipment(networkUuid, variantNum, shuntCompensatorId, mappings.getShuntCompensatorMappings(),
                           SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes.class);
    }

    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes.class);
    }

    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getShuntCompensatorMappings(), SHUNT_COMPENSATOR, Resource.shuntCompensatorBuilder(), ShuntCompensatorAttributes.class);
    }

    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getShuntCompensatorMappings(), psUpdateShuntCompensator, "voltageLevelId");
    }

    public void deleteShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        deleteEquipment(networkUuid, variantNum, shuntCompensatorId, SHUNT_COMPENSATOR);
    }

    // VSC converter station

    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getVscConverterStationMappings(), psInsertVscConverterStation);
    }

    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return getEquipment(networkUuid, variantNum, vscConverterStationId, mappings.getVscConverterStationMappings(),
                           VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes.class);
    }

    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes.class);
    }

    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getVscConverterStationMappings(), VSC_CONVERTER_STATION, Resource.vscConverterStationBuilder(), VscConverterStationAttributes.class);
    }

    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getVscConverterStationMappings(), psUpdateVscConverterStation, "voltageLevelId");
    }

    public void deleteVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        deleteEquipment(networkUuid, variantNum, vscConverterStationId, VSC_CONVERTER_STATION);
    }

    // LCC converter station

    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getLccConverterStationMappings(), psInsertLccConverterStation);
    }

    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return getEquipment(networkUuid, variantNum, lccConverterStationId, mappings.getLccConverterStationMappings(),
                           LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes.class);
    }

    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes.class);
    }

    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getLccConverterStationMappings(), LCC_CONVERTER_STATION, Resource.lccConverterStationBuilder(), LccConverterStationAttributes.class);
    }

    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getLccConverterStationMappings(), psUpdateLccConverterStation, "voltageLevelId");
    }

    public void deleteLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        deleteEquipment(networkUuid, variantNum, lccConverterStationId, LCC_CONVERTER_STATION);
    }

    // static var compensators

    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getStaticVarCompensatorMappings(), psInsertStaticVarCompensator);
    }

    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return getEquipment(networkUuid, variantNum, staticVarCompensatorId, mappings.getStaticVarCompensatorMappings(),
            STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes.class);
    }

    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes.class);
    }

    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getStaticVarCompensatorMappings(), STATIC_VAR_COMPENSATOR, Resource.staticVarCompensatorBuilder(), StaticVarCompensatorAttributes.class);
    }

    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getStaticVarCompensatorMappings(), psUpdateStaticVarCompensator, "voltageLevelId");
    }

    public void deleteStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        deleteEquipment(networkUuid, variantNum, staticVarCompensatorId, STATIC_VAR_COMPENSATOR);
    }

    // busbar section

    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getBusbarSectionMappings(), psInsertBusbarSection);
    }

    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getBusbarSectionMappings(), psUpdateBusbarSection, "voltageLevelId");
    }

    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return getEquipment(networkUuid, variantNum, busbarSectionId, mappings.getBusbarSectionMappings(),
            BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes.class);
    }

    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getBusbarSectionMappings(), BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes.class);
    }

    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getBusbarSectionMappings(), BUSBAR_SECTION, Resource.busbarSectionBuilder(), BusbarSectionAttributes.class);
    }

    public void deleteBusBarSection(UUID networkUuid, int variantNum, String busBarSectionId) {
        deleteEquipment(networkUuid, variantNum, busBarSectionId, BUSBAR_SECTION);
    }

    // switch

    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getSwitchMappings(), psInsertSwitch);
    }

    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return getEquipment(networkUuid, variantNum, switchId, mappings.getSwitchMappings(),
            SWITCH, Resource.switchBuilder(), SwitchAttributes.class);
    }

    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getSwitchMappings(), SWITCH, Resource.switchBuilder(), SwitchAttributes.class);
    }

    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getSwitchMappings(), SWITCH, Resource.switchBuilder(), SwitchAttributes.class);
    }

    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getSwitchMappings(), psUpdateSwitch, "voltageLevelId");
    }

    public void deleteSwitch(UUID networkUuid, int variantNum, String switchId) {
        deleteEquipment(networkUuid, variantNum, switchId, SWITCH);
    }

    // 2 windings transformer

    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getTwoWindingsTransformerMappings(), psInsertTwoWindingsTransformer);
    }

    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return getEquipment(networkUuid, variantNum, twoWindingsTransformerId, mappings.getTwoWindingsTransformerMappings(),
            TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes.class);
    }

    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes.class);
    }

    private List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, Branch.Side side, String voltageLevelId) {
        return getVoltageLevelEquipmentsWithSide(networkUuid, variantNum, voltageLevelId, side, mappings.getTwoWindingsTransformerMappings(), TWO_WINDINGS_TRANSFORMER, Resource.twoWindingsTransformerBuilder(), TwoWindingsTransformerAttributes.class);
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
        updateEquipments2(networkUuid, resources, mappings.getTwoWindingsTransformerMappings(), psUpdateTwoWindingsTransformer);
    }

    public void deleteTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        deleteEquipment(networkUuid, variantNum, twoWindingsTransformerId, TWO_WINDINGS_TRANSFORMER);
    }

    // 3 windings transformer

    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getThreeWindingsTransformerMappings(), psInsertThreeWindingsTransformer);
    }

    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        Map<String, Mapping> threeWindingsTransformerMappings = mappings.getThreeWindingsTransformerMappings();
        try (ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
            .columns(threeWindingsTransformerMappings.keySet().toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("id").isEqualTo(literal(threeWindingsTransformerId))
            .build())) {
            Row one = resultSet.one();
            if (one != null) {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());

                threeWindingsTransformerMappings.entrySet().forEach(entry -> entry.getValue().set(threeWindingsTransformerAttributes, one.get(entry.getKey(), entry.getValue().getClassR())));
                return Optional.of(Resource.threeWindingsTransformerBuilder()
                    .id(threeWindingsTransformerId)
                    .variantNum(variantNum)
                    .attributes(threeWindingsTransformerAttributes)
                    .build());
            }
            return Optional.empty();
        }
    }

    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        Map<String, Mapping> threeWindingsTransformerMappings = mappings.getThreeWindingsTransformerMappings();
        Set<String> columns = new HashSet<>(threeWindingsTransformerMappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .build())) {
            List<Resource<ThreeWindingsTransformerAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());

                threeWindingsTransformerMappings.entrySet().forEach(entry -> entry.getValue().set(threeWindingsTransformerAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));
                resources.add(Resource.threeWindingsTransformerBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(threeWindingsTransformerAttributes)
                    .build());
            }
            return resources;
        }
    }

    private List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, ThreeWindingsTransformer.Side side, String voltageLevelId) {
        Map<String, Mapping> threeWindingsTransformerMappings = mappings.getThreeWindingsTransformerMappings();
        Set<String> columns = new HashSet<>(threeWindingsTransformerMappings.keySet());
        columns.add("id");

        try (ResultSet resultSet = session.execute(selectFrom(THREE_WINDINGS_TRANSFORMER)
            .columns(columns.toArray(new String[0]))
            .whereColumn("networkUuid").isEqualTo(literal(networkUuid))
            .whereColumn(VARIANT_NUM).isEqualTo(literal(variantNum))
            .whereColumn("voltageLevelId" + (side == ThreeWindingsTransformer.Side.ONE ? 1 : (side == ThreeWindingsTransformer.Side.TWO ? 2 : 3))).isEqualTo(literal(voltageLevelId))
            .build())) {
            List<Resource<ThreeWindingsTransformerAttributes>> resources = new ArrayList<>();
            for (Row row : resultSet) {
                ThreeWindingsTransformerAttributes threeWindingsTransformerAttributes = new ThreeWindingsTransformerAttributes();
                threeWindingsTransformerAttributes.setLeg1(LegAttributes.builder().legNumber(1).build());
                threeWindingsTransformerAttributes.setLeg2(LegAttributes.builder().legNumber(2).build());
                threeWindingsTransformerAttributes.setLeg3(LegAttributes.builder().legNumber(3).build());

                threeWindingsTransformerMappings.entrySet().forEach(entry -> entry.getValue().set(threeWindingsTransformerAttributes, row.get(entry.getKey(), entry.getValue().getClassR())));

                resources.add(Resource.threeWindingsTransformerBuilder()
                    .id(row.getString("id"))
                    .variantNum(variantNum)
                    .attributes(threeWindingsTransformerAttributes)
                    .build());
            }
            return resources;
        }
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
        updateEquipments2(networkUuid, resources, mappings.getThreeWindingsTransformerMappings(), psUpdateThreeWindingsTransformer);
    }

    public void deleteThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        deleteEquipment(networkUuid, variantNum, threeWindingsTransformerId, THREE_WINDINGS_TRANSFORMER);
    }

    // line

    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getLineMappings(), psInsertLine);
    }

    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return getEquipment(networkUuid, variantNum, lineId, mappings.getLineMappings(),
            LINE, Resource.lineBuilder(), LineAttributes.class);
    }

    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getLineMappings(), LINE, Resource.lineBuilder(), LineAttributes.class);
    }

    private List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, Branch.Side side, String voltageLevelId) {
        return getVoltageLevelEquipmentsWithSide(networkUuid, variantNum, voltageLevelId, side, mappings.getLineMappings(), LINE, Resource.lineBuilder(), LineAttributes.class);
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
        updateEquipments2(networkUuid, resources, mappings.getLineMappings(), psUpdateLines);
    }

    public void deleteLine(UUID networkUuid, int variantNum, String lineId) {
        deleteEquipment(networkUuid, variantNum, lineId, LINE);
    }

    // Hvdc line

    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getHvdcLineMappings(), HVDC_LINE, Resource.hvdcLineBuilder(), HvdcLineAttributes.class);
    }

    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return getEquipment(networkUuid, variantNum, hvdcLineId, mappings.getHvdcLineMappings(),
                           HVDC_LINE, Resource.hvdcLineBuilder(), HvdcLineAttributes.class);
    }

    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getHvdcLineMappings(), psInsertHvdcLine);
    }

    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources) {
        updateEquipments2(networkUuid, resources, mappings.getHvdcLineMappings(), psUpdateHvdcLine);
    }

    public void deleteHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        deleteEquipment(networkUuid, variantNum, hvdcLineId, HVDC_LINE);
    }

    // Dangling line

    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getDanglingLineMappings(), DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes.class);
    }

    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return getEquipment(networkUuid, variantNum, danglingLineId, mappings.getDanglingLineMappings(),
                           DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes.class);
    }

    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getDanglingLineMappings(), DANGLING_LINE, Resource.danglingLineBuilder(), DanglingLineAttributes.class);
    }

    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getDanglingLineMappings(), psInsertDanglingLine);
    }

    public void deleteDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        deleteEquipment(networkUuid, variantNum, danglingLineId, DANGLING_LINE);
    }

    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getDanglingLineMappings(), psUpdateDanglingLine, "voltageLevelId");
    }

    // configured buses

    public void createBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        createEquipments(networkUuid, resources, mappings.getConfiguredBusMappings(), psInsertConfiguredBus);
    }

    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return getEquipment(networkUuid, variantNum, busId, mappings.getConfiguredBusMappings(),
                           CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes.class);
    }

    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return getEquipments(networkUuid, variantNum, mappings.getConfiguredBusMappings(), CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes.class);
    }

    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getContainerEquipments(networkUuid, variantNum, voltageLevelId, "voltageLevelId", mappings.getConfiguredBusMappings(), CONFIGURED_BUS, Resource.configuredBusBuilder(), ConfiguredBusAttributes.class);
    }

    public void updateBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources) {
        updateEquipments(networkUuid, resources, mappings.getConfiguredBusMappings(), psUpdateConfiguredBus, "voltageLevelId");
    }

    public void deleteBus(UUID networkUuid, int variantNum, String configuredBusId) {
        deleteEquipment(networkUuid, variantNum, configuredBusId, CONFIGURED_BUS);
    }
}
