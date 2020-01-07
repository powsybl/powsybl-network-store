/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.extras.codecs.joda.InstantCodec;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.network.store.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver;

import java.nio.ByteBuffer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Configuration
@PropertySource(value = {"classpath:cassandra.properties"})
@PropertySource(value = {"file:/config/cassandra.properties"}, ignoreResourceNotFound = true)
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Override
    protected String getKeyspaceName() {
        return CassandraConstants.KEYSPACE_IIDM;
    }

    @Bean
    public CassandraClusterFactoryBean cluster(Environment env) {
        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setContactPoints(env.getRequiredProperty("cassandra.contact-points"));
        cluster.setPort(Integer.parseInt(env.getRequiredProperty("cassandra.port")));

        CodecRegistry codecRegistry = new CodecRegistry();
        cluster.setClusterBuilderConfigurer(builder -> {
            builder.withCodecRegistry(codecRegistry);
            Cluster cluster1 = builder.build();

            UserType connectablePositionType = cluster1.getMetadata().getKeyspace(CassandraConstants.KEYSPACE_IIDM).getUserType("connectablePosition");
            TypeCodec<UDTValue> connectablePositionTypeCodec = codecRegistry.codecFor(connectablePositionType);
            ConnectablePositionCodec connectablePositionCodec = new ConnectablePositionCodec(connectablePositionTypeCodec, ConnectablePositionAttributes.class);
            codecRegistry.register(connectablePositionCodec);

            UserType busBarSectionPositionType = cluster1.getMetadata().getKeyspace(CassandraConstants.KEYSPACE_IIDM).getUserType("busBarSectionPosition");
            TypeCodec<UDTValue> busBarSectionPositionTypeCodec = codecRegistry.codecFor(busBarSectionPositionType);
            BusbarSectionPositionCodec busbarSectionPositionCodec = new BusbarSectionPositionCodec(busBarSectionPositionTypeCodec, BusbarSectionPositionAttributes.class);
            codecRegistry.register(busbarSectionPositionCodec);

            UserType minMaxReactiveLimitsType = cluster1.getMetadata().getKeyspace(CassandraConstants.KEYSPACE_IIDM).getUserType("minMaxReactiveLimits");
            TypeCodec<UDTValue> minMaxReactiveLimitsTypeCodec = codecRegistry.codecFor(minMaxReactiveLimitsType);
            MinMaxReactiveLimitsCodec minMaxReactiveLimitsCodec = new MinMaxReactiveLimitsCodec(minMaxReactiveLimitsTypeCodec, MinMaxReactiveLimitsAttributes.class);
            codecRegistry.register(minMaxReactiveLimitsCodec);

            UserType reactiveCapabilityCurveType = cluster1.getMetadata().getKeyspace(CassandraConstants.KEYSPACE_IIDM).getUserType("reactiveCapabilityCurve");
            TypeCodec<UDTValue> reactiveCapabilityCurveTypeCodec = codecRegistry.codecFor(reactiveCapabilityCurveType);
            ReactiveCapabilityCurveCodec reactiveCapabilityCurveCodec = new ReactiveCapabilityCurveCodec(reactiveCapabilityCurveTypeCodec, ReactiveCapabilityCurveAttributes.class);
            codecRegistry.register(reactiveCapabilityCurveCodec);

            UserType reactiveCapabilityCurvePointType = cluster1.getMetadata().getKeyspace(CassandraConstants.KEYSPACE_IIDM).getUserType("reactiveCapabilityCurvePoint");
            TypeCodec<UDTValue> reactiveCapabilityCurvePointTypeCodec = codecRegistry.codecFor(reactiveCapabilityCurvePointType);
            ReactiveCapabilityCurvePointCodec reactiveCapabilityCurvePointCodec = new ReactiveCapabilityCurvePointCodec(reactiveCapabilityCurvePointTypeCodec, ReactiveCapabilityCurvePointAttributes.class);
            codecRegistry.register(reactiveCapabilityCurvePointCodec);

            codecRegistry.register(InstantCodec.instance);
            return builder;
        });

        return cluster;
    }

    @Bean
    public CassandraMappingContext cassandraMapping(Environment env) {
        CassandraMappingContext mappingContext =  new CassandraMappingContext();
        mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cluster(env).getObject(), CassandraConstants.KEYSPACE_IIDM));
        return mappingContext;
    }

    private static class ConnectablePositionCodec extends TypeCodec<ConnectablePositionAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public ConnectablePositionCodec(TypeCodec<UDTValue> innerCodec, Class<ConnectablePositionAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(ConnectablePositionAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public ConnectablePositionAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toConnectablePosition(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public ConnectablePositionAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty()  ? null : toConnectablePosition(innerCodec.parse(value));
        }

        @Override
        public String format(ConnectablePositionAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected ConnectablePositionAttributes toConnectablePosition(UDTValue value) {
            return value == null ? null : new ConnectablePositionAttributes(
                    value.getString("label"),
                    value.getInt("orderNum"),
                    ConnectableDirection.valueOf(value.getString("direction"))
                    );
        }

        protected UDTValue toUDTValue(ConnectablePositionAttributes value) {
            return value == null ? null : userType.newValue()
                    .setString("label", value.getLabel())
                    .setInt("orderNum", value.getOrder())
                    .setString("direction", value.getDirection().toString());
        }
    }

    private static class BusbarSectionPositionCodec extends TypeCodec<BusbarSectionPositionAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public BusbarSectionPositionCodec(TypeCodec<UDTValue> innerCodec, Class<BusbarSectionPositionAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(BusbarSectionPositionAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public BusbarSectionPositionAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toBusbarSectionPosition(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public BusbarSectionPositionAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toBusbarSectionPosition(innerCodec.parse(value));
        }

        @Override
        public String format(BusbarSectionPositionAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected BusbarSectionPositionAttributes toBusbarSectionPosition(UDTValue value) {
            return value == null ? null : new BusbarSectionPositionAttributes(value.getInt("busbarIndex"), value.getInt("sectionIndex"));
        }

        protected UDTValue toUDTValue(BusbarSectionPositionAttributes value) {
            return value == null ? null : userType.newValue().setInt("busbarIndex", value.getBusbarIndex()).setInt("sectionIndex", value.getSectionIndex());
        }
    }

    private static class MinMaxReactiveLimitsCodec extends TypeCodec<MinMaxReactiveLimitsAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public MinMaxReactiveLimitsCodec(TypeCodec<UDTValue> innerCodec, Class<MinMaxReactiveLimitsAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(MinMaxReactiveLimitsAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public MinMaxReactiveLimitsAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toMinMaxReactiveLimits(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public MinMaxReactiveLimitsAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toMinMaxReactiveLimits(innerCodec.parse(value));
        }

        @Override
        public String format(MinMaxReactiveLimitsAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected MinMaxReactiveLimitsAttributes toMinMaxReactiveLimits(UDTValue value) {
            return value == null ? null : new MinMaxReactiveLimitsAttributes(ReactiveLimitsKind.MIN_MAX, value.getDouble("minQ"), value.getDouble("maxQ"));
        }

        protected UDTValue toUDTValue(MinMaxReactiveLimitsAttributes value) {
            return value == null ? null : userType.newValue().setString("kind", value.getKind().toString()).setDouble("minQ", value.getMinQ()).setDouble("maxQ", value.getMaxQ());
        }
    }

    private static class ReactiveCapabilityCurveCodec extends TypeCodec<ReactiveCapabilityCurveAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public ReactiveCapabilityCurveCodec(TypeCodec<UDTValue> innerCodec, Class<ReactiveCapabilityCurveAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(ReactiveCapabilityCurveAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public ReactiveCapabilityCurveAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toReactiveCapabilityCurve(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public ReactiveCapabilityCurveAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toReactiveCapabilityCurve(innerCodec.parse(value));
        }

        @Override
        public String format(ReactiveCapabilityCurveAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected ReactiveCapabilityCurveAttributes toReactiveCapabilityCurve(UDTValue value) {
            return value == null ? null : new ReactiveCapabilityCurveAttributes(ReactiveLimitsKind.CURVE, value.getDouble("minQ"), value.getDouble("maxQ"), value.getMap("points", Double.class, ReactiveCapabilityCurve.Point.class), value.getInt("pointCount"), value.getDouble("minP"), value.getDouble("maxP"));
        }

        protected UDTValue toUDTValue(ReactiveCapabilityCurveAttributes value) {
            return value == null ? null : userType.newValue().setString("kind", value.getKind().toString()).setDouble("minQ", value.getMinQ()).setDouble("maxQ", value.getMaxQ()).setMap("points", value.getPoints()).setInt("pointCount", value.getPointCount()).setDouble("minP", value.getMinP()).setDouble("maxP", value.getMaxP());
        }
    }

    private static class ReactiveCapabilityCurvePointCodec extends TypeCodec<ReactiveCapabilityCurvePointAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public ReactiveCapabilityCurvePointCodec(TypeCodec<UDTValue> innerCodec, Class<ReactiveCapabilityCurvePointAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(ReactiveCapabilityCurvePointAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public ReactiveCapabilityCurvePointAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toReactiveCapabilityCurvePoint(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public ReactiveCapabilityCurvePointAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toReactiveCapabilityCurvePoint(innerCodec.parse(value));
        }

        @Override
        public String format(ReactiveCapabilityCurvePointAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected ReactiveCapabilityCurvePointAttributes toReactiveCapabilityCurvePoint(UDTValue value) {
            return value == null ? null : new ReactiveCapabilityCurvePointAttributes(value.getDouble("p"), value.getDouble("minQ"), value.getDouble("maxQ"));
        }

        protected UDTValue toUDTValue(ReactiveCapabilityCurvePointAttributes value) {
            return value == null ? null : userType.newValue().setDouble("p", value.getP()).setDouble("minQ", value.getMinQ()).setDouble("maxQ", value.getMaxQ());
        }
    }
}
