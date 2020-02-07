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
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.PhaseTapChanger;
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
import java.util.TreeMap;

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

            KeyspaceMetadata keyspace = cluster1.getMetadata().getKeyspace(CassandraConstants.KEYSPACE_IIDM);
            if (keyspace == null) {
                throw new PowsyblException("Keyspace '" + CassandraConstants.KEYSPACE_IIDM + "' not found");
            }

            UserType connectablePositionType = keyspace.getUserType("connectablePosition");
            TypeCodec<UDTValue> connectablePositionTypeCodec = codecRegistry.codecFor(connectablePositionType);
            ConnectablePositionCodec connectablePositionCodec = new ConnectablePositionCodec(connectablePositionTypeCodec, ConnectablePositionAttributes.class);
            codecRegistry.register(connectablePositionCodec);

            UserType busBarSectionPositionType = keyspace.getUserType("busBarSectionPosition");
            TypeCodec<UDTValue> busBarSectionPositionTypeCodec = codecRegistry.codecFor(busBarSectionPositionType);
            BusbarSectionPositionCodec busbarSectionPositionCodec = new BusbarSectionPositionCodec(busBarSectionPositionTypeCodec, BusbarSectionPositionAttributes.class);
            codecRegistry.register(busbarSectionPositionCodec);

            UserType minMaxReactiveLimitsType = keyspace.getUserType("minMaxReactiveLimits");
            TypeCodec<UDTValue> minMaxReactiveLimitsTypeCodec = codecRegistry.codecFor(minMaxReactiveLimitsType);
            MinMaxReactiveLimitsCodec minMaxReactiveLimitsCodec = new MinMaxReactiveLimitsCodec(minMaxReactiveLimitsTypeCodec, MinMaxReactiveLimitsAttributes.class);
            codecRegistry.register(minMaxReactiveLimitsCodec);

            UserType reactiveCapabilityCurveType = keyspace.getUserType("reactiveCapabilityCurve");
            TypeCodec<UDTValue> reactiveCapabilityCurveTypeCodec = codecRegistry.codecFor(reactiveCapabilityCurveType);
            ReactiveCapabilityCurveCodec reactiveCapabilityCurveCodec = new ReactiveCapabilityCurveCodec(reactiveCapabilityCurveTypeCodec, ReactiveCapabilityCurveAttributes.class);
            codecRegistry.register(reactiveCapabilityCurveCodec);

            UserType reactiveCapabilityCurvePointType = keyspace.getUserType("reactiveCapabilityCurvePoint");
            TypeCodec<UDTValue> reactiveCapabilityCurvePointTypeCodec = codecRegistry.codecFor(reactiveCapabilityCurvePointType);
            ReactiveCapabilityCurvePointCodec reactiveCapabilityCurvePointCodec = new ReactiveCapabilityCurvePointCodec(reactiveCapabilityCurvePointTypeCodec, ReactiveCapabilityCurvePointAttributes.class);
            codecRegistry.register(reactiveCapabilityCurvePointCodec);

            UserType currentLimitsType = keyspace.getUserType("currentLimits");
            TypeCodec<UDTValue> currentLimitsTypeCodec = codecRegistry.codecFor(currentLimitsType);
            CurrentLimitsCodec currentLimitsCodec = new CurrentLimitsCodec(currentLimitsTypeCodec, CurrentLimitsAttributes.class);
            codecRegistry.register(currentLimitsCodec);

            UserType temporaryCurrentLimitType = keyspace.getUserType("temporaryLimit");
            TypeCodec<UDTValue> temporaryCurrentLimitTypeCodec = codecRegistry.codecFor(temporaryCurrentLimitType);
            TemporaryCurrentLimitCodec temporaryCurrentLimitCodec = new TemporaryCurrentLimitCodec(temporaryCurrentLimitTypeCodec, TemporaryCurrentLimitAttributes.class);
            codecRegistry.register(temporaryCurrentLimitCodec);

            UserType phaseTapChangerStepType = keyspace.getUserType("phaseTapChangerStep");
            TypeCodec<UDTValue> phaseTapChangerStepTypeCodec = codecRegistry.codecFor(phaseTapChangerStepType);
            PhaseTapChangerStepCodec phaseTapChangerStepCodec = new PhaseTapChangerStepCodec(phaseTapChangerStepTypeCodec, PhaseTapChangerStepAttributes.class);
            codecRegistry.register(phaseTapChangerStepCodec);

            UserType phaseTapChangerType = keyspace.getUserType("phaseTapChanger");
            TypeCodec<UDTValue> phaseTapChangerTypeCodec = codecRegistry.codecFor(phaseTapChangerType);
            PhaseTapChangerCodec phaseTapChangerCodec = new PhaseTapChangerCodec(phaseTapChangerTypeCodec, PhaseTapChangerAttributes.class);
            codecRegistry.register(phaseTapChangerCodec);

            UserType ratioTapChangerStepType = keyspace.getUserType("ratioTapChangerStep");
            TypeCodec<UDTValue> ratioTapChangerStepTypeCodec = codecRegistry.codecFor(ratioTapChangerStepType);
            RatioTapChangerStepCodec ratioTapChangerStepCodec = new RatioTapChangerStepCodec(ratioTapChangerStepTypeCodec, RatioTapChangerStepAttributes.class);
            codecRegistry.register(ratioTapChangerStepCodec);

            UserType ratioTapChangerType = keyspace.getUserType("ratioTapChanger");
            TypeCodec<UDTValue> ratioTapChangerTypeCodec = codecRegistry.codecFor(ratioTapChangerType);
            RatioTapChangerCodec ratioTapChangerCodec = new RatioTapChangerCodec(ratioTapChangerTypeCodec, RatioTapChangerAttributes.class);
            codecRegistry.register(ratioTapChangerCodec);

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
            return value == null ? null : new MinMaxReactiveLimitsAttributes(value.getDouble("minQ"), value.getDouble("maxQ"));
        }

        protected UDTValue toUDTValue(MinMaxReactiveLimitsAttributes value) {
            return value == null ? null : userType.newValue().setDouble("minQ", value.getMinQ()).setDouble("maxQ", value.getMaxQ());
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
            return value == null ? null : new ReactiveCapabilityCurveAttributes(new TreeMap<>(value.getMap("points", Double.class, ReactiveCapabilityCurvePointAttributes.class)));
        }

        protected UDTValue toUDTValue(ReactiveCapabilityCurveAttributes value) {
            return value == null ? null : userType.newValue().setMap("points", value.getPoints(), Double.class, ReactiveCapabilityCurvePointAttributes.class);
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

    private static class CurrentLimitsCodec extends TypeCodec<CurrentLimitsAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public CurrentLimitsCodec(TypeCodec<UDTValue> innerCodec, Class<CurrentLimitsAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(CurrentLimitsAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public CurrentLimitsAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toCurrentLimits(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public CurrentLimitsAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toCurrentLimits(innerCodec.parse(value));
        }

        @Override
        public String format(CurrentLimitsAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected CurrentLimitsAttributes toCurrentLimits(UDTValue value) {
            return value == null ? null : new CurrentLimitsAttributes(value.getDouble("permanentLimit"), new TreeMap<>(value.getMap("temporaryLimits", Integer.class, TemporaryCurrentLimitAttributes.class)));
        }

        protected UDTValue toUDTValue(CurrentLimitsAttributes value) {
            return value == null ? null : userType.newValue().setDouble("permanentLimit", value.getPermanentLimit()).setMap("temporaryLimits", value.getTemporaryLimits(), Integer.class, TemporaryCurrentLimitAttributes.class);
        }
    }

    private static class TemporaryCurrentLimitCodec extends TypeCodec<TemporaryCurrentLimitAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public TemporaryCurrentLimitCodec(TypeCodec<UDTValue> innerCodec, Class<TemporaryCurrentLimitAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(TemporaryCurrentLimitAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public TemporaryCurrentLimitAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toTemporaryCurrentLimit(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public TemporaryCurrentLimitAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toTemporaryCurrentLimit(innerCodec.parse(value));
        }

        @Override
        public String format(TemporaryCurrentLimitAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected TemporaryCurrentLimitAttributes toTemporaryCurrentLimit(UDTValue value) {
            return value == null ? null : new TemporaryCurrentLimitAttributes(value.getString("name"), value.getDouble("value"), value.getInt("acceptableDuration"), value.getBool("fictitious"));
        }

        protected UDTValue toUDTValue(TemporaryCurrentLimitAttributes value) {
            return value == null ? null : userType.newValue().setString("name", value.getName()).setDouble("value", value.getValue()).setInt("acceptableDuration", value.getAcceptableDuration()).setBool("fictitious", value.isFictitious());
        }
    }

    private static class PhaseTapChangerStepCodec extends TypeCodec<PhaseTapChangerStepAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public PhaseTapChangerStepCodec(TypeCodec<UDTValue> innerCodec, Class<PhaseTapChangerStepAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(PhaseTapChangerStepAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public PhaseTapChangerStepAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toPhaseTapChangerStep(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public PhaseTapChangerStepAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toPhaseTapChangerStep(innerCodec.parse(value));
        }

        @Override
        public String format(PhaseTapChangerStepAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected PhaseTapChangerStepAttributes toPhaseTapChangerStep(UDTValue value) {
            return value == null ? null : PhaseTapChangerStepAttributes.builder()
                    .x(value.getDouble("x"))
                    .b(value.getDouble("b"))
                    .g(value.getDouble("g"))
                    .r(value.getDouble("r"))
                    .alpha(value.getDouble("alpha"))
                    .rho(value.getDouble("rho"))
                    .position(value.getInt("position"))
                    .build();
        }

        protected UDTValue toUDTValue(PhaseTapChangerStepAttributes value) {
            return value == null ? null : userType.newValue()
                    .setInt("position", value.getPosition())
                    .setDouble("rho", value.getRho())
                    .setDouble("r", value.getR())
                    .setDouble("x", value.getX())
                    .setDouble("r", value.getR())
                    .setDouble("g", value.getG())
                    .setDouble("b", value.getB())
                    .setDouble("alpha", value.getAlpha());
        }
    }

    private static class PhaseTapChangerCodec extends TypeCodec<PhaseTapChangerAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public PhaseTapChangerCodec(TypeCodec<UDTValue> innerCodec, Class<PhaseTapChangerAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(PhaseTapChangerAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public PhaseTapChangerAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toPhaseTapChanger(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public PhaseTapChangerAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toPhaseTapChanger(innerCodec.parse(value));
        }

        @Override
        public String format(PhaseTapChangerAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected PhaseTapChangerAttributes toPhaseTapChanger(UDTValue value) {
            return value == null ? null : PhaseTapChangerAttributes.builder()
                    .targetDeadband(value.getDouble("targetDeadband"))
                    .tapPosition(value.getInt("tapPosition"))
                    .regulationValue(value.getDouble("regulationValue"))
                    .regulationMode(PhaseTapChanger.RegulationMode.valueOf(value.getString("regulationMode")))
                    .regulating(value.getBool("regulating"))
                    .lowTapPosition(value.getInt("lowTapPosition"))
                    .steps(value.getList("steps", PhaseTapChangerStepAttributes.class))
                    .build();
        }

        protected UDTValue toUDTValue(PhaseTapChangerAttributes value) {
            return value == null ? null : userType.newValue()
                    .setInt("lowTapPosition", value.getLowTapPosition())
                    .setDouble("regulationValue", value.getRegulationValue())
                    .setInt("tapPosition", value.getTapPosition())
                    .setDouble("targetDeadband", value.getTargetDeadband())
                    .setString("regulationMode", value.getRegulationMode().toString())
                    .setBool("regulating", value.isRegulating())
                    .setList("steps", value.getSteps());
        }
    }

    private static class RatioTapChangerCodec extends TypeCodec<RatioTapChangerAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public RatioTapChangerCodec(TypeCodec<UDTValue> innerCodec, Class<RatioTapChangerAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(RatioTapChangerAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public RatioTapChangerAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toRatioTapChanger(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public RatioTapChangerAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toRatioTapChanger(innerCodec.parse(value));
        }

        @Override
        public String format(RatioTapChangerAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected RatioTapChangerAttributes toRatioTapChanger(UDTValue value) {
            return value == null ? null : RatioTapChangerAttributes.builder()
                    .targetDeadband(value.getDouble("targetDeadband"))
                    .tapPosition(value.getInt("tapPosition"))
                    .regulating(value.getBool("regulating"))
                    .lowTapPosition(value.getInt("lowTapPosition"))
                    .steps(value.getList("steps", RatioTapChangerStepAttributes.class))
                    .loadTapChangingCapabilities(value.getBool("loadTapChangingCapabilities"))
                    .targetV(value.getDouble("targetV"))
                    .build();
        }

        protected UDTValue toUDTValue(RatioTapChangerAttributes value) {
            return value == null ? null : userType.newValue()
                    .setInt("lowTapPosition", value.getLowTapPosition())
                    .setInt("tapPosition", value.getTapPosition())
                    .setDouble("targetDeadband", value.getTargetDeadband())
                    .setBool("regulating", value.isRegulating())
                    .setList("steps", value.getSteps())
                    .setDouble("targetV", value.getTargetV())
                    .setBool("loadTapChangingCapabilities", value.isLoadTapChangingCapabilities());
        }
    }

    private static class RatioTapChangerStepCodec extends TypeCodec<RatioTapChangerStepAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public RatioTapChangerStepCodec(TypeCodec<UDTValue> innerCodec, Class<RatioTapChangerStepAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(RatioTapChangerStepAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public RatioTapChangerStepAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toRatioTapChangerStep(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public RatioTapChangerStepAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toRatioTapChangerStep(innerCodec.parse(value));
        }

        @Override
        public String format(RatioTapChangerStepAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected RatioTapChangerStepAttributes toRatioTapChangerStep(UDTValue value) {
            return value == null ? null : RatioTapChangerStepAttributes.builder()
                    .x(value.getDouble("x"))
                    .b(value.getDouble("b"))
                    .g(value.getDouble("g"))
                    .r(value.getDouble("r"))
                    .rho(value.getDouble("rho"))
                    .position(value.getInt("position"))
                    .build();
        }

        protected UDTValue toUDTValue(RatioTapChangerStepAttributes value) {
            return value == null ? null : userType.newValue()
                    .setInt("position", value.getPosition())
                    .setDouble("rho", value.getRho())
                    .setDouble("r", value.getR())
                    .setDouble("x", value.getX())
                    .setDouble("r", value.getR())
                    .setDouble("g", value.getG())
                    .setDouble("b", value.getB());
        }
    }
}
