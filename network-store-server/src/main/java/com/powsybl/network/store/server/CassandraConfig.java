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
import com.powsybl.network.store.model.BusbarSectionPositionAttributes;
import com.powsybl.network.store.model.ConnectableDirection;
import com.powsybl.network.store.model.ConnectablePositionAttributes;
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

}
