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
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.PhaseTapChanger;
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
import java.util.TreeMap;

import static com.powsybl.network.store.server.CassandraConstants.MIN_MAX_REACTIVE_LIMITS;
import static com.powsybl.network.store.server.CassandraConstants.REACTIVE_CAPABILITY_CURVE;
import static com.powsybl.network.store.server.CassandraConstants.TARGET_V;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
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

            UserType terminalRefType = keyspace.getUserType(CassandraConstants.TERMINAL_REF);
            TypeCodec<UDTValue> terminalRefTypeCodec = codecRegistry.codecFor(terminalRefType);
            TerminalRefCodec terminalRefCodec = new TerminalRefCodec(terminalRefTypeCodec, TerminalRefAttributes.class);
            codecRegistry.register(terminalRefCodec);

            UserType connectablePositionType = keyspace.getUserType("connectablePosition");
            TypeCodec<UDTValue> connectablePositionTypeCodec = codecRegistry.codecFor(connectablePositionType);
            ConnectablePositionCodec connectablePositionCodec = new ConnectablePositionCodec(connectablePositionTypeCodec, ConnectablePositionAttributes.class);
            codecRegistry.register(connectablePositionCodec);

            UserType busBarSectionPositionType = keyspace.getUserType("busBarSectionPosition");
            TypeCodec<UDTValue> busBarSectionPositionTypeCodec = codecRegistry.codecFor(busBarSectionPositionType);
            BusbarSectionPositionCodec busbarSectionPositionCodec = new BusbarSectionPositionCodec(busBarSectionPositionTypeCodec, BusbarSectionPositionAttributes.class);
            codecRegistry.register(busbarSectionPositionCodec);

            UserType minMaxReactiveLimitsType = keyspace.getUserType(MIN_MAX_REACTIVE_LIMITS);
            TypeCodec<UDTValue> minMaxReactiveLimitsTypeCodec = codecRegistry.codecFor(minMaxReactiveLimitsType);
            MinMaxReactiveLimitsCodec minMaxReactiveLimitsCodec = new MinMaxReactiveLimitsCodec(minMaxReactiveLimitsTypeCodec, MinMaxReactiveLimitsAttributes.class);
            codecRegistry.register(minMaxReactiveLimitsCodec);

            UserType reactiveCapabilityCurveType = keyspace.getUserType(REACTIVE_CAPABILITY_CURVE);
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

            UserType internalConnectionType = keyspace.getUserType("internalConnection");
            TypeCodec<UDTValue> internalConnectionTypeCodec = codecRegistry.codecFor(internalConnectionType);
            InternalConnectionCodec internalConnectionCodec = new InternalConnectionCodec(internalConnectionTypeCodec, InternalConnectionAttributes.class);
            codecRegistry.register(internalConnectionCodec);

            UserType mergedXnodeType = keyspace.getUserType("mergedXnode");
            TypeCodec<UDTValue> mergedXnodeTypeCodec = codecRegistry.codecFor(mergedXnodeType);
            MergedXnodeCodec mergedXnodeCodec = new MergedXnodeCodec(mergedXnodeTypeCodec, MergedXnodeAttributes.class);
            codecRegistry.register(mergedXnodeCodec);

            UserType calculatedBusType = keyspace.getUserType("calculatedBus");
            TypeCodec<UDTValue> calculatedBusTypeCodec = codecRegistry.codecFor(calculatedBusType);
            CalculatedBusCodec calculatedBusCodec = new CalculatedBusCodec(calculatedBusTypeCodec, CalculatedBusAttributes.class);
            codecRegistry.register(calculatedBusCodec);

            UserType vertexType = keyspace.getUserType("vertex");
            TypeCodec<UDTValue> vertexTypeCodec = codecRegistry.codecFor(vertexType);
            VertexCodec vertexCodec = new VertexCodec(vertexTypeCodec, Vertex.class);
            codecRegistry.register(vertexCodec);

            UserType activePowerControlType = keyspace.getUserType("activePowerControl");
            TypeCodec<UDTValue> activePowerControlTypeCodec = codecRegistry.codecFor(activePowerControlType);
            ActivePowerControlCodec activePowerControlCodec = new ActivePowerControlCodec(activePowerControlTypeCodec, ActivePowerControlAttributes.class);
            codecRegistry.register(activePowerControlCodec);

            UserType entsoeAreaType = keyspace.getUserType("entsoeArea");
            TypeCodec<UDTValue> entsoeAreaTypeCodec = codecRegistry.codecFor(entsoeAreaType);
            EntsoeAreaCodec entsoeAreaCodec = new EntsoeAreaCodec(entsoeAreaTypeCodec, EntsoeAreaAttributes.class);
            codecRegistry.register(entsoeAreaCodec);

            UserType shuntCompensatorLinearModelType = keyspace.getUserType("shuntCompensatorLinearModel");
            TypeCodec<UDTValue> shuntCompensatorLinearModelTypeCodec = codecRegistry.codecFor(shuntCompensatorLinearModelType);
            ShuntCompensatorLinearModelCodec shuntCompensatorLinearModelCodec = new ShuntCompensatorLinearModelCodec(shuntCompensatorLinearModelTypeCodec, ShuntCompensatorLinearModelAttributes.class);
            codecRegistry.register(shuntCompensatorLinearModelCodec);

            UserType shuntCompensatorNonLinearModelType = keyspace.getUserType("shuntCompensatorNonLinearModel");
            TypeCodec<UDTValue> shuntCompensatorNonLinearModelTypeCodec = codecRegistry.codecFor(shuntCompensatorNonLinearModelType);
            ShuntCompensatorNonLinearModelCodec shuntCompensatorNonLinearModelCodec = new ShuntCompensatorNonLinearModelCodec(shuntCompensatorNonLinearModelTypeCodec, ShuntCompensatorNonLinearModelAttributes.class);
            codecRegistry.register(shuntCompensatorNonLinearModelCodec);

            UserType shuntCompensatorNonLinearSectionType = keyspace.getUserType("shuntCompensatorNonLinearSection");
            TypeCodec<UDTValue> shuntCompensatorNonLinearSectionTypeCodec = codecRegistry.codecFor(shuntCompensatorNonLinearSectionType);
            ShuntCompensatorNonLinearSectionCodec shuntCompensatorNonLinearSectionCodec = new ShuntCompensatorNonLinearSectionCodec(shuntCompensatorNonLinearSectionTypeCodec, ShuntCompensatorNonLinearSectionAttributes.class);
            codecRegistry.register(shuntCompensatorNonLinearSectionCodec);

            UserType coordinatedReactiveControlType = keyspace.getUserType("coordinatedReactiveControl");
            TypeCodec<UDTValue> coordinatedReactiveControlTypeCodec = codecRegistry.codecFor(coordinatedReactiveControlType);
            CoordinatedReactiveControlCodec coordinatedReactiveControlCodec = new CoordinatedReactiveControlCodec(coordinatedReactiveControlTypeCodec, CoordinatedReactiveControlAttributes.class);
            codecRegistry.register(coordinatedReactiveControlCodec);

            UserType voltagePerReactivePowerControlType = keyspace.getUserType("voltagePerReactivePowerControl");
            TypeCodec<UDTValue> voltagePerReactivePowerControlTypeCodec = codecRegistry.codecFor(voltagePerReactivePowerControlType);
            VoltagePerReactivePowerControlCodec voltagePerReactivePowerControlCodec = new VoltagePerReactivePowerControlCodec(voltagePerReactivePowerControlTypeCodec, VoltagePerReactivePowerControlAttributes.class);
            codecRegistry.register(voltagePerReactivePowerControlCodec);

            UserType danglingLineGenerationType = keyspace.getUserType("danglingLineGeneration");
            TypeCodec<UDTValue> danglingLineGenerationTypeCodec = codecRegistry.codecFor(danglingLineGenerationType);
            DanglingLineGenerationCodec danglingLineGenerationCodec = new DanglingLineGenerationCodec(danglingLineGenerationTypeCodec, DanglingLineGenerationAttributes.class);
            codecRegistry.register(danglingLineGenerationCodec);

            UserType loadDetailType = keyspace.getUserType("loadDetail");
            TypeCodec<UDTValue> loadDetailTypeCodec = codecRegistry.codecFor(loadDetailType);
            LoadDetailCodec loadDetailCodec = new LoadDetailCodec(loadDetailTypeCodec, LoadDetailAttributes.class);
            codecRegistry.register(loadDetailCodec);

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

    private static class TerminalRefCodec extends TypeCodec<TerminalRefAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public TerminalRefCodec(TypeCodec<UDTValue> innerCodec, Class<TerminalRefAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(TerminalRefAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public TerminalRefAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toTerminalRef(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public TerminalRefAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toTerminalRef(innerCodec.parse(value));
        }

        @Override
        public String format(TerminalRefAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected TerminalRefAttributes toTerminalRef(UDTValue value) {
            return value == null ? null : new TerminalRefAttributes(value.getString("connectableId"), value.getString("side"));
        }

        protected UDTValue toUDTValue(TerminalRefAttributes value) {
            return  value == null ? null : userType.newValue().setString("connectableId", value.getConnectableId()).setString("side", value.getSide());
        }
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
                    .build();
        }

        protected UDTValue toUDTValue(PhaseTapChangerStepAttributes value) {
            return value == null ? null : userType.newValue()
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
                    .regulatingTerminal(value.get(CassandraConstants.REGULATING_TERMINAL, TerminalRefAttributes.class))
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
                    .setList("steps", value.getSteps())
                    .set(CassandraConstants.REGULATING_TERMINAL, value.getRegulatingTerminal(), TerminalRefAttributes.class);
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
                    .targetV(value.getDouble(TARGET_V))
                    .regulatingTerminal(value.get(CassandraConstants.REGULATING_TERMINAL, TerminalRefAttributes.class))
                    .build();
        }

        protected UDTValue toUDTValue(RatioTapChangerAttributes value) {
            return value == null ? null : userType.newValue()
                    .setInt("lowTapPosition", value.getLowTapPosition())
                    .setInt("tapPosition", value.getTapPosition())
                    .setDouble("targetDeadband", value.getTargetDeadband())
                    .setBool("regulating", value.isRegulating())
                    .setList("steps", value.getSteps())
                    .setDouble(TARGET_V, value.getTargetV())
                    .setBool("loadTapChangingCapabilities", value.isLoadTapChangingCapabilities())
                    .set(CassandraConstants.REGULATING_TERMINAL, value.getRegulatingTerminal(), TerminalRefAttributes.class);
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
                    .build();
        }

        protected UDTValue toUDTValue(RatioTapChangerStepAttributes value) {
            return value == null ? null : userType.newValue()
                    .setDouble("rho", value.getRho())
                    .setDouble("r", value.getR())
                    .setDouble("x", value.getX())
                    .setDouble("r", value.getR())
                    .setDouble("g", value.getG())
                    .setDouble("b", value.getB());
        }
    }

    private static class InternalConnectionCodec extends TypeCodec<InternalConnectionAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public InternalConnectionCodec(TypeCodec<UDTValue> innerCodec, Class<InternalConnectionAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(InternalConnectionAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public InternalConnectionAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toInternalConnection(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public InternalConnectionAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toInternalConnection(innerCodec.parse(value));
        }

        @Override
        public String format(InternalConnectionAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected InternalConnectionAttributes toInternalConnection(UDTValue value) {
            return value == null ? null : new InternalConnectionAttributes(value.getInt("node1"), value.getInt("node2"));
        }

        protected UDTValue toUDTValue(InternalConnectionAttributes value) {
            return value == null ? null : userType.newValue().setInt("node1", value.getNode1()).setInt("node2", value.getNode2());
        }
    }

    private static class MergedXnodeCodec extends TypeCodec<MergedXnodeAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public MergedXnodeCodec(TypeCodec<UDTValue> innerCodec, Class<MergedXnodeAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(MergedXnodeAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public MergedXnodeAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toMergedXnode(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public MergedXnodeAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toMergedXnode(innerCodec.parse(value));
        }

        @Override
        public String format(MergedXnodeAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected MergedXnodeAttributes toMergedXnode(UDTValue value) {
            return value == null ? null : new MergedXnodeAttributes(
                    value.getFloat("rdp"),
                    value.getFloat("xdp"),
                    value.getDouble("xnodeP1"),
                    value.getDouble("xnodeQ1"),
                    value.getDouble("xnodeP2"),
                    value.getDouble("xnodeQ2"),
                    value.getString("line1Name"),
                    value.getString("line2Name"),
                    value.getString("ucteXnodeCode"));
        }

        protected UDTValue toUDTValue(MergedXnodeAttributes value) {
            return value == null ? null : userType.newValue()
                    .setFloat("rdp", value.getRdp())
                    .setFloat("xdp", value.getXdp())
                    .setDouble("xnodeP1", value.getXnodeP1())
                    .setDouble("xnodeQ1", value.getXnodeQ1())
                    .setDouble("xnodeP2", value.getXnodeP2())
                    .setDouble("xnodeQ2", value.getXnodeQ2())
                    .setString("line1Name", value.getLine1Name())
                    .setString("line2Name", value.getLine2Name())
                    .setString("ucteXnodeCode", value.getCode());
        }
    }

    private static class VertexCodec extends TypeCodec<Vertex> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public VertexCodec(TypeCodec<UDTValue> innerCodec, Class<Vertex> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(Vertex value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public Vertex deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toVertex(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public Vertex parse(String value) {
            return value == null || value.isEmpty() ? null : toVertex(innerCodec.parse(value));
        }

        @Override
        public String format(Vertex value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected Vertex toVertex(UDTValue value) {
            if (value == null) {
                return null;
            }
            return new Vertex(
                    value.getString("id"),
                    ConnectableType.valueOf(value.getString("connectableType")),
                    value.isNull("node") ? null : value.getInt("node"),
                    value.isNull("bus") ? null : value.getString("bus"),
                    value.getString("side"));
        }

        protected UDTValue toUDTValue(Vertex value) {
            if (value == null) {
                return null;
            }
            UDTValue udtValue = userType.newValue()
                    .setString("id", value.getId())
                    .setString("connectableType", value.getConnectableType().name())
                    .setString("side", value.getSide());
            if (value.getNode() != null) {
                udtValue.setInt("node", value.getNode());
            }
            if (value.getBus() != null) {
                udtValue.setString("bus", value.getBus());
            }
            return udtValue;
        }
    }

    private static class CalculatedBusCodec extends TypeCodec<CalculatedBusAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public CalculatedBusCodec(TypeCodec<UDTValue> innerCodec, Class<CalculatedBusAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(CalculatedBusAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public CalculatedBusAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toCalculatedBus(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public CalculatedBusAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toCalculatedBus(innerCodec.parse(value));
        }

        @Override
        public String format(CalculatedBusAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected CalculatedBusAttributes toCalculatedBus(UDTValue value) {
            if (value == null) {
                return null;
            }
            return new CalculatedBusAttributes(
                    value.getSet("vertices", Vertex.class),
                    value.isNull("ccNum") ? null : value.getInt("ccNum"),
                    value.isNull("scNum") ? null : value.getInt("scNum"),
                    value.getDouble("v"),
                    value.getDouble("angle"));
        }

        protected UDTValue toUDTValue(CalculatedBusAttributes value) {
            if (value == null) {
                return null;
            }

            UDTValue udtValue = userType.newValue()
                    .setSet("vertices", value.getVertices())
                    .setDouble("v", value.getV())
                    .setDouble("angle", value.getAngle());
            if (value.getConnectedComponentNumber() != null) {
                udtValue.setInt("ccNum", value.getConnectedComponentNumber());
            }
            if (value.getSynchronousComponentNumber() != null) {
                udtValue.setInt("scNum", value.getSynchronousComponentNumber());
            }
            return udtValue;
        }
    }

    private static class ActivePowerControlCodec extends TypeCodec<ActivePowerControlAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public ActivePowerControlCodec(TypeCodec<UDTValue> innerCodec, Class<ActivePowerControlAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(ActivePowerControlAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public ActivePowerControlAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toActivePowerControl(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public ActivePowerControlAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toActivePowerControl(innerCodec.parse(value));
        }

        @Override
        public String format(ActivePowerControlAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected ActivePowerControlAttributes toActivePowerControl(UDTValue value) {
            if (value == null) {
                return null;
            }
            return new ActivePowerControlAttributes(
                    value.getBool("participate"),
                    value.getFloat("droop"));
        }

        protected UDTValue toUDTValue(ActivePowerControlAttributes value) {
            if (value == null) {
                return null;
            }
            return userType.newValue()
                    .setBool("participate", value.isParticipate())
                    .setFloat("droop", value.getDroop());
        }
    }

    private static class EntsoeAreaCodec extends TypeCodec<EntsoeAreaAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public EntsoeAreaCodec(TypeCodec<UDTValue> innerCodec, Class<EntsoeAreaAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(EntsoeAreaAttributes value, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public EntsoeAreaAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
            return toEntsoeArea(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public EntsoeAreaAttributes parse(String value) throws InvalidTypeException {
            return value == null || value.isEmpty() ? null : toEntsoeArea(innerCodec.parse(value));
        }

        @Override
        public String format(EntsoeAreaAttributes value) throws InvalidTypeException {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected EntsoeAreaAttributes toEntsoeArea(UDTValue value) {
            return value == null ? null : new EntsoeAreaAttributes(
                    value.getString("code"));
        }

        protected UDTValue toUDTValue(EntsoeAreaAttributes value) {
            return value == null ? null : userType.newValue()
                    .setString("code", value.getCode());
        }
    }

    private static class ShuntCompensatorLinearModelCodec extends TypeCodec<ShuntCompensatorLinearModelAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public ShuntCompensatorLinearModelCodec(TypeCodec<UDTValue> innerCodec, Class<ShuntCompensatorLinearModelAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(ShuntCompensatorLinearModelAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public ShuntCompensatorLinearModelAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toShuntCompensatorLinearModel(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public ShuntCompensatorLinearModelAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toShuntCompensatorLinearModel(innerCodec.parse(value));
        }

        @Override
        public String format(ShuntCompensatorLinearModelAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected ShuntCompensatorLinearModelAttributes toShuntCompensatorLinearModel(UDTValue value) {
            return value == null ? null : new ShuntCompensatorLinearModelAttributes(
                    value.getDouble("bPerSection"),
                    value.getDouble("gPerSection"),
                    value.getInt("maximumSectionCount"));
        }

        protected UDTValue toUDTValue(ShuntCompensatorLinearModelAttributes value) {
            return value == null ? null : userType.newValue()
                    .setDouble("bPerSection", value.getBPerSection())
                    .setDouble("gPerSection", value.getGPerSection())
                    .setInt("maximumSectionCount", value.getMaximumSectionCount());
        }
    }

    private static class ShuntCompensatorNonLinearSectionCodec extends TypeCodec<ShuntCompensatorNonLinearSectionAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public ShuntCompensatorNonLinearSectionCodec(TypeCodec<UDTValue> innerCodec, Class<ShuntCompensatorNonLinearSectionAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(ShuntCompensatorNonLinearSectionAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public ShuntCompensatorNonLinearSectionAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toShuntCompensatorNonLinearSection(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public ShuntCompensatorNonLinearSectionAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toShuntCompensatorNonLinearSection(innerCodec.parse(value));
        }

        @Override
        public String format(ShuntCompensatorNonLinearSectionAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected ShuntCompensatorNonLinearSectionAttributes toShuntCompensatorNonLinearSection(UDTValue value) {
            return value == null ? null : ShuntCompensatorNonLinearSectionAttributes.builder()
                    .b(value.getDouble("b"))
                    .g(value.getDouble("g"))
                    .build();
        }

        protected UDTValue toUDTValue(ShuntCompensatorNonLinearSectionAttributes value) {
            return value == null ? null : userType.newValue()
                    .setDouble("b", value.getB())
                    .setDouble("g", value.getG());
        }
    }

    private static class ShuntCompensatorNonLinearModelCodec extends TypeCodec<ShuntCompensatorNonLinearModelAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public ShuntCompensatorNonLinearModelCodec(TypeCodec<UDTValue> innerCodec, Class<ShuntCompensatorNonLinearModelAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(ShuntCompensatorNonLinearModelAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public ShuntCompensatorNonLinearModelAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toShuntCompensatorNonLinearModel(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public ShuntCompensatorNonLinearModelAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toShuntCompensatorNonLinearModel(innerCodec.parse(value));
        }

        @Override
        public String format(ShuntCompensatorNonLinearModelAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected ShuntCompensatorNonLinearModelAttributes toShuntCompensatorNonLinearModel(UDTValue value) {
            return value == null ? null : ShuntCompensatorNonLinearModelAttributes.builder()
                    .sections(value.getList("sections", ShuntCompensatorNonLinearSectionAttributes.class))
                    .build();
        }

        protected UDTValue toUDTValue(ShuntCompensatorNonLinearModelAttributes value) {
            return value == null ? null : userType.newValue()
                    .setList("sections", value.getSections());
        }
    }

    private static class CoordinatedReactiveControlCodec extends TypeCodec<CoordinatedReactiveControlAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public CoordinatedReactiveControlCodec(TypeCodec<UDTValue> innerCodec, Class<CoordinatedReactiveControlAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(CoordinatedReactiveControlAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public CoordinatedReactiveControlAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toCoordinatedReactiveControl(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public CoordinatedReactiveControlAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toCoordinatedReactiveControl(innerCodec.parse(value));
        }

        @Override
        public String format(CoordinatedReactiveControlAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected CoordinatedReactiveControlAttributes toCoordinatedReactiveControl(UDTValue value) {
            if (value == null) {
                return null;
            }
            return new CoordinatedReactiveControlAttributes(value.getDouble("qPercent"));
        }

        protected UDTValue toUDTValue(CoordinatedReactiveControlAttributes value) {
            if (value == null) {
                return null;
            }
            return userType.newValue()
                    .setDouble("qPercent", value.getQPercent());
        }
    }

    private static class VoltagePerReactivePowerControlCodec extends TypeCodec<VoltagePerReactivePowerControlAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public VoltagePerReactivePowerControlCodec(TypeCodec<UDTValue> innerCodec, Class<VoltagePerReactivePowerControlAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(VoltagePerReactivePowerControlAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public VoltagePerReactivePowerControlAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toVoltagePerReactiveControl(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public VoltagePerReactivePowerControlAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toVoltagePerReactiveControl(innerCodec.parse(value));
        }

        @Override
        public String format(VoltagePerReactivePowerControlAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected VoltagePerReactivePowerControlAttributes toVoltagePerReactiveControl(UDTValue value) {
            if (value == null) {
                return null;
            }
            return new VoltagePerReactivePowerControlAttributes(value.getDouble("slope"));
        }

        protected UDTValue toUDTValue(VoltagePerReactivePowerControlAttributes value) {
            if (value == null) {
                return null;
            }
            return userType.newValue()
                    .setDouble("slope", value.getSlope());
        }
    }

    private static class DanglingLineGenerationCodec extends TypeCodec<DanglingLineGenerationAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public DanglingLineGenerationCodec(TypeCodec<UDTValue> innerCodec, Class<DanglingLineGenerationAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(DanglingLineGenerationAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public DanglingLineGenerationAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toGeneration(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public DanglingLineGenerationAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toGeneration(innerCodec.parse(value));
        }

        @Override
        public String format(DanglingLineGenerationAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected DanglingLineGenerationAttributes toGeneration(UDTValue value) {
            if (value == null) {
                return null;
            }
            DanglingLineGenerationAttributes attributes = new DanglingLineGenerationAttributes(
                    value.getDouble("minP"),
                    value.getDouble("maxP"),
                    value.getDouble("targetP"),
                    value.getDouble("targetQ"),
                    value.getDouble(TARGET_V),
                    value.getBool("voltageRegulatorOn"),
                    null);

            ReactiveLimitsAttributes limitsAttributes;
            if (!value.isNull(MIN_MAX_REACTIVE_LIMITS)) {
                MinMaxReactiveLimitsAttributes minMaxLimits = value.get(MIN_MAX_REACTIVE_LIMITS, MinMaxReactiveLimitsAttributes.class);
                limitsAttributes = minMaxLimits;
            } else {
                ReactiveCapabilityCurveAttributes reactiveLimits = value.get(REACTIVE_CAPABILITY_CURVE, ReactiveCapabilityCurveAttributes.class);
                limitsAttributes = reactiveLimits;
            }

            attributes.setReactiveLimits(limitsAttributes);
            return attributes;
        }

        protected UDTValue toUDTValue(DanglingLineGenerationAttributes value) {
            if (value == null) {
                return null;
            }

            UDTValue udtValue = userType.newValue()
                    .setDouble("minP", value.getMinP())
                    .setDouble("maxP", value.getMaxP())
                    .setDouble("targetP", value.getTargetP())
                    .setDouble("targetQ", value.getTargetQ())
                    .setDouble(TARGET_V, value.getTargetV())
                    .setBool("voltageRegulatorOn", value.isVoltageRegulationOn());

            if (value.getReactiveLimits() != null) {
                if (value.getReactiveLimits().getKind() == ReactiveLimitsKind.MIN_MAX) {
                    udtValue.set(MIN_MAX_REACTIVE_LIMITS, (MinMaxReactiveLimitsAttributes) value.getReactiveLimits(), MinMaxReactiveLimitsAttributes.class);
                } else {
                    udtValue.set(REACTIVE_CAPABILITY_CURVE, (ReactiveCapabilityCurveAttributes) value.getReactiveLimits(), ReactiveCapabilityCurveAttributes.class);
                }
            }
            return udtValue;
        }
    }

    private static class LoadDetailCodec extends TypeCodec<LoadDetailAttributes> {

        private final TypeCodec<UDTValue> innerCodec;

        private final UserType userType;

        public LoadDetailCodec(TypeCodec<UDTValue> innerCodec, Class<LoadDetailAttributes> javaType) {
            super(innerCodec.getCqlType(), javaType);
            this.innerCodec = innerCodec;
            this.userType = (UserType) innerCodec.getCqlType();
        }

        @Override
        public ByteBuffer serialize(LoadDetailAttributes value, ProtocolVersion protocolVersion) {
            return innerCodec.serialize(toUDTValue(value), protocolVersion);
        }

        @Override
        public LoadDetailAttributes deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
            return toGeneration(innerCodec.deserialize(bytes, protocolVersion));
        }

        @Override
        public LoadDetailAttributes parse(String value) {
            return value == null || value.isEmpty() ? null : toGeneration(innerCodec.parse(value));
        }

        @Override
        public String format(LoadDetailAttributes value) {
            return value == null ? null : innerCodec.format(toUDTValue(value));
        }

        protected LoadDetailAttributes toGeneration(UDTValue value) {
            if (value == null) {
                return null;
            }
            LoadDetailAttributes attributes = new LoadDetailAttributes(
                    value.getFloat("fixedActivePower"),
                    value.getFloat("fixedReactivePower"),
                    value.getFloat("variableActivePower"),
                    value.getFloat("variableReactivePower"));

            return attributes;
        }

        protected UDTValue toUDTValue(LoadDetailAttributes value) {
            if (value == null) {
                return null;
            }

            UDTValue udtValue = userType.newValue()
                    .setFloat("fixedActivePower", value.getFixedActivePower())
                    .setFloat("fixedReactivePower", value.getFixedReactivePower())
                    .setFloat("variableActivePower", value.getVariableActivePower())
                    .setFloat("variableReactivePower", value.getVariableReactivePower());

            return udtValue;
        }
    }

}
