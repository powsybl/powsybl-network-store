/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import com.datastax.oss.driver.api.core.type.codec.registry.MutableCodecRegistry;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.type.codec.registry.DefaultCodecRegistry;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.network.store.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.config.SessionFactoryFactoryBean;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;

import java.util.Map;
import java.util.Set;
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

    @Value("${cassandra-keyspace:iidm}")
    private String keyspaceName;

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Bean
    public CqlSessionFactoryBean cassandraSession(Environment env) {
        CqlSessionFactoryBean session = new CqlSessionFactoryBean();
        session.setContactPoints(env.getRequiredProperty("cassandra.contact-points"));
        session.setPort(Integer.parseInt(env.getRequiredProperty("cassandra.port")));
        session.setLocalDatacenter(env.getRequiredProperty("cassandra.datacenter"));
        session.setKeyspaceName(getKeyspaceName());
        return session;
    }

    @Bean
    public SessionFactoryFactoryBean cassandraSessionFactory(CqlSession session, CassandraConverter converter) {
        SessionFactoryFactoryBean sessionFactory = new SessionFactoryFactoryBean();
        sessionFactory.setSession(session);
        sessionFactory.setConverter(converter);

        CodecRegistry codecRegistry = session.getContext().getCodecRegistry();

        UserDefinedType minMaxReactiveLimitsUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("minMaxReactiveLimits"))
                        .orElseThrow(IllegalStateException::new);
        // The "inner" codec that handles the conversions from CQL from/to UdtValue
        TypeCodec<UdtValue> innerCodec = codecRegistry.codecFor(minMaxReactiveLimitsUdt);
        // The mapping codec that will handle the conversions from/to UdtValue and minMaxReactiveLimits
        MinMaxReactiveLimitsCodec minMaxReactiveLimitsCodec = new MinMaxReactiveLimitsCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(minMaxReactiveLimitsCodec);

        UserDefinedType reactiveCapabilityCurveUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("reactiveCapabilityCurve"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(reactiveCapabilityCurveUdt);
        ReactiveCapabilityCurveCodec reactiveCapabilityCurveCodec = new ReactiveCapabilityCurveCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(reactiveCapabilityCurveCodec);

        UserDefinedType terminalRefUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("terminalRef"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(terminalRefUdt);
        TerminalRefCodec terminalRefCodec = new TerminalRefCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(terminalRefCodec);

        UserDefinedType connectablePositionUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("connectablePosition"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(connectablePositionUdt);
        ConnectablePositionCodec connectablePositionCodec = new ConnectablePositionCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(connectablePositionCodec);

        UserDefinedType busbarSectionPositionUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("busbarSectionPosition"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(busbarSectionPositionUdt);
        BusbarSectionPositionCodec busbarSectionPositionCodec = new BusbarSectionPositionCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(busbarSectionPositionCodec);

        UserDefinedType reactiveCapabilityCurvePointUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("reactiveCapabilityCurvePoint"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(reactiveCapabilityCurvePointUdt);
        ReactiveCapabilityCurvePointCodec reactiveCapabilityCurvePointCodec = new ReactiveCapabilityCurvePointCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(reactiveCapabilityCurvePointCodec);

        UserDefinedType currentLimitsUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("currentLimits"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(currentLimitsUdt);
        CurrentLimitsCodec currentLimitsCodec = new CurrentLimitsCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(currentLimitsCodec);

        UserDefinedType temporaryLimitUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("temporaryLimit"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(temporaryLimitUdt);
        TemporaryCurrentLimitCodec temporaryCurrentLimitCodec = new TemporaryCurrentLimitCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(temporaryCurrentLimitCodec);

        UserDefinedType phaseTapChangerStepUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("phaseTapChangerStep"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(phaseTapChangerStepUdt);
        PhaseTapChangerStepCodec phaseTapChangerStepCodec = new PhaseTapChangerStepCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(phaseTapChangerStepCodec);

        UserDefinedType phaseTapChangerUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("phaseTapChanger"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(phaseTapChangerUdt);
        PhaseTapChangerCodec phaseTapChangerCodec = new PhaseTapChangerCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(phaseTapChangerCodec);

        UserDefinedType ratioTapChangerUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("ratioTapChanger"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(ratioTapChangerUdt);
        RatioTapChangerCodec ratioTapChangerCodec = new RatioTapChangerCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(ratioTapChangerCodec);

        UserDefinedType ratioTapChangerStepUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("ratioTapChangerStep"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(ratioTapChangerStepUdt);
        RatioTapChangerStepCodec ratioTapChangerStepCodec = new RatioTapChangerStepCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(ratioTapChangerStepCodec);

        UserDefinedType internalConnectionUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("internalConnection"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(internalConnectionUdt);
        InternalConnectionCodec internalConnectionCodec = new InternalConnectionCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(internalConnectionCodec);

        UserDefinedType mergedXnodeUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("mergedXnode"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(mergedXnodeUdt);
        MergedXnodeCodec mergedXnodeCodec = new MergedXnodeCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(mergedXnodeCodec);

        UserDefinedType vertexUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("vertex"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(vertexUdt);
        VertexCodec vertexCodec = new VertexCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(vertexCodec);

        UserDefinedType calculatedBusUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("calculatedBus"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(calculatedBusUdt);
        CalculatedBusCodec calculatedBusCodec = new CalculatedBusCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(calculatedBusCodec);

        UserDefinedType activePowerControlUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("activePowerControl"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(activePowerControlUdt);
        ActivePowerControlCodec activePowerControlCodec = new ActivePowerControlCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(activePowerControlCodec);

        UserDefinedType entsoeAreaUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("entsoeArea"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(entsoeAreaUdt);
        EntsoeAreaCodec entsoeAreaCodec = new EntsoeAreaCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(entsoeAreaCodec);

        UserDefinedType shuntCompensatorLinearModelUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("shuntCompensatorLinearModel"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(shuntCompensatorLinearModelUdt);
        ShuntCompensatorLinearModelCodec shuntCompensatorLinearModelCodec = new ShuntCompensatorLinearModelCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(shuntCompensatorLinearModelCodec);

        UserDefinedType shuntCompensatorNonLinearSectionUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("shuntCompensatorNonLinearSection"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(shuntCompensatorNonLinearSectionUdt);
        ShuntCompensatorNonLinearSectionCodec shuntCompensatorNonLinearSectionCodec = new ShuntCompensatorNonLinearSectionCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(shuntCompensatorNonLinearSectionCodec);

        UserDefinedType shuntCompensatorNonLinearModelUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("shuntCompensatorNonLinearModel"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(shuntCompensatorNonLinearModelUdt);
        ShuntCompensatorNonLinearModelCodec shuntCompensatorNonLinearModelCodec = new ShuntCompensatorNonLinearModelCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(shuntCompensatorNonLinearModelCodec);

        UserDefinedType coordinatedReactiveControlUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("coordinatedReactiveControl"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(coordinatedReactiveControlUdt);
        CoordinatedReactiveControlCodec coordinatedReactiveControlCodec = new CoordinatedReactiveControlCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(coordinatedReactiveControlCodec);

        UserDefinedType remoteReactivePowerControlUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("remoteReactivePowerControl"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(remoteReactivePowerControlUdt);
        RemoteReactivePowerControlCodec remoteReactivePowerControlCodec = new RemoteReactivePowerControlCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(remoteReactivePowerControlCodec);

        UserDefinedType voltagePerReactivePowerControlUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("voltagePerReactivePowerControl"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(voltagePerReactivePowerControlUdt);
        VoltagePerReactivePowerControlCodec voltagePerReactivePowerControlCodec = new VoltagePerReactivePowerControlCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(voltagePerReactivePowerControlCodec);

        UserDefinedType danglingLineGenerationUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("danglingLineGeneration"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(danglingLineGenerationUdt);
        DanglingLineGenerationCodec danglingLineGenerationCodec = new DanglingLineGenerationCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(danglingLineGenerationCodec);

        UserDefinedType loadDetailUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("loadDetail"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(loadDetailUdt);
        LoadDetailCodec loadDetailCodec = new LoadDetailCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(loadDetailCodec);

        UserDefinedType cgmesSvMetadataUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("cgmesSvMetadata"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(cgmesSvMetadataUdt);
        CgmesSvMetadataCodec cgmesSvMetadataCodec = new CgmesSvMetadataCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(cgmesSvMetadataCodec);

        UserDefinedType cgmesSshMetadataUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("cgmesSshMetadata"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(cgmesSshMetadataUdt);
        CgmesSshMetadataCodec cgmesSshMetadataCodec = new CgmesSshMetadataCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(cgmesSshMetadataCodec);

        UserDefinedType cimCharacteristicsUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("cimCharacteristics"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(cimCharacteristicsUdt);
        CimCharacteristicsCodec cimCharacteristicsCodec = new CimCharacteristicsCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(cimCharacteristicsCodec);

        UserDefinedType threeWindingsTransformerPhaseAngleClockUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("threeWindingsTransformerPhaseAngleClock"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(threeWindingsTransformerPhaseAngleClockUdt);
        ThreeWindingsTransformerPhaseAngleClockCodec threeWindingsTransformerPhaseAngleClock = new ThreeWindingsTransformerPhaseAngleClockCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(threeWindingsTransformerPhaseAngleClock);

        UserDefinedType twoWindingsTransformerPhaseAngleClockUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("twoWindingsTransformerPhaseAngleClock"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(twoWindingsTransformerPhaseAngleClockUdt);
        TwoWindingsTransformerPhaseAngleClockCodec twoWindingsTransformerPhaseAngleClockCodec = new TwoWindingsTransformerPhaseAngleClockCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(twoWindingsTransformerPhaseAngleClockCodec);

        UserDefinedType hvdcAngleDroopActivePowerControlUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("hvdcAngleDroopActivePowerControl"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(hvdcAngleDroopActivePowerControlUdt);
        HvdcAngleDroopActivePowerControlCodec hvdcAngleDroopActivePowerControlCodec = new HvdcAngleDroopActivePowerControlCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(hvdcAngleDroopActivePowerControlCodec);

        UserDefinedType hvdcOperatorActivePowerRangeUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("hvdcOperatorActivePowerRange"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(hvdcOperatorActivePowerRangeUdt);
        HvdcOperatorActivePowerRangeCodec hvdcOperatorActivePowerRangeCodec = new HvdcOperatorActivePowerRangeCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(hvdcOperatorActivePowerRangeCodec);

        UserDefinedType cgmesControlAreasUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("cgmesControlAreas"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(cgmesControlAreasUdt);
        CgmesControlAreasCodec cgmesControlAreasCodec = new CgmesControlAreasCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(cgmesControlAreasCodec);

        UserDefinedType cgmesControlAreaUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("cgmesControlArea"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(cgmesControlAreaUdt);
        CgmesControlAreaCodec cgmesControlAreaCodec = new CgmesControlAreaCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(cgmesControlAreaCodec);

        UserDefinedType cgmesIidmMappingUdt =
                session
                        .getMetadata()
                        .getKeyspace(getKeyspaceName())
                        .flatMap(ks -> ks.getUserDefinedType("cgmesIidmMapping"))
                        .orElseThrow(IllegalStateException::new);
        innerCodec = codecRegistry.codecFor(cgmesIidmMappingUdt);
        CgmesIidmMappingCodec cgmesIidmMappingCodec = new CgmesIidmMappingCodec(innerCodec);
        ((MutableCodecRegistry) codecRegistry).register(cgmesIidmMappingCodec);

        return sessionFactory;
    }

    @Bean
    public CassandraConverter cassandraConverter(CassandraMappingContext mappingContext) {
        MappingCassandraConverter mappingCassandraConverter = new MappingCassandraConverter(mappingContext);
        CodecRegistry codecRegistry = new DefaultCodecRegistry("");
        mappingCassandraConverter.setCodecRegistry(codecRegistry);
        return mappingCassandraConverter;
    }

    private static class TerminalRefCodec extends MappingCodec<UdtValue, TerminalRefAttributes> {

        public TerminalRefCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(TerminalRefAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected TerminalRefAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new TerminalRefAttributes(value.getString("connectableId"), value.getString("side"));
        }

        @Override
        protected UdtValue outerToInner(TerminalRefAttributes value) {
            return  value == null ? null : getCqlType().newValue().setString("connectableId", value.getConnectableId()).setString("side", value.getSide());
        }
    }

    private static class ConnectablePositionCodec extends MappingCodec<UdtValue, ConnectablePositionAttributes> {

        public ConnectablePositionCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(ConnectablePositionAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected ConnectablePositionAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new ConnectablePositionAttributes(
                    value.getString("label"),
                    value.getInt("orderNum"),
                    ConnectableDirection.valueOf(value.getString("direction"))
            );
        }

        @Override
        protected UdtValue outerToInner(ConnectablePositionAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setString("label", value.getLabel())
                    .setInt("orderNum", value.getOrder())
                    .setString("direction", value.getDirection().toString());
        }
    }

    private static class BusbarSectionPositionCodec extends MappingCodec<UdtValue, BusbarSectionPositionAttributes> {

        public BusbarSectionPositionCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(BusbarSectionPositionAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected BusbarSectionPositionAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new BusbarSectionPositionAttributes(value.getInt("busbarIndex"), value.getInt("sectionIndex"));
        }

        @Override
        protected UdtValue outerToInner(BusbarSectionPositionAttributes value) {
            return value == null ? null : getCqlType().newValue().setInt("busbarIndex", value.getBusbarIndex()).setInt("sectionIndex", value.getSectionIndex());
        }
    }

    private static class MinMaxReactiveLimitsCodec extends MappingCodec<UdtValue, MinMaxReactiveLimitsAttributes> {

        public MinMaxReactiveLimitsCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(MinMaxReactiveLimitsAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected MinMaxReactiveLimitsAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new MinMaxReactiveLimitsAttributes(value.getDouble("minQ"), value.getDouble("maxQ"));
        }

        @Override
        protected UdtValue outerToInner(MinMaxReactiveLimitsAttributes value) {
            return value == null ? null : getCqlType().newValue().setDouble("minQ", value.getMinQ()).setDouble("maxQ", value.getMaxQ());
        }
    }

    private static class ReactiveCapabilityCurveCodec extends MappingCodec<UdtValue, ReactiveCapabilityCurveAttributes> {

        public ReactiveCapabilityCurveCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(ReactiveCapabilityCurveAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected ReactiveCapabilityCurveAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new ReactiveCapabilityCurveAttributes(
                    new TreeMap<>(value.getMap("points", Double.class, ReactiveCapabilityCurvePointAttributes.class)));
        }

        @Override
        protected UdtValue outerToInner(ReactiveCapabilityCurveAttributes value) {
            return value == null ? null : getCqlType().newValue().setMap("points", value.getPoints(), Double.class, ReactiveCapabilityCurvePointAttributes.class);
        }
    }

    private static class ReactiveCapabilityCurvePointCodec extends MappingCodec<UdtValue, ReactiveCapabilityCurvePointAttributes> {

        public ReactiveCapabilityCurvePointCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(ReactiveCapabilityCurvePointAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected ReactiveCapabilityCurvePointAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new ReactiveCapabilityCurvePointAttributes(value.getDouble("p"), value.getDouble("minQ"), value.getDouble("maxQ"));
        }

        @Override
        protected UdtValue outerToInner(ReactiveCapabilityCurvePointAttributes value) {
            return value == null ? null : getCqlType().newValue().setDouble("p", value.getP()).setDouble("minQ", value.getMinQ()).setDouble("maxQ", value.getMaxQ());
        }
    }

    private static class CurrentLimitsCodec extends MappingCodec<UdtValue, LimitsAttributes> {

        public CurrentLimitsCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(LimitsAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected LimitsAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new LimitsAttributes(value.getDouble("permanentLimit"), new TreeMap<>(value.getMap("temporaryLimits", Integer.class, TemporaryCurrentLimitAttributes.class)));
        }

        @Override
        protected UdtValue outerToInner(LimitsAttributes value) {
            return value == null ? null : getCqlType().newValue().setDouble("permanentLimit", value.getPermanentLimit()).setMap("temporaryLimits", value.getTemporaryLimits(), Integer.class, TemporaryCurrentLimitAttributes.class);
        }
    }

    private static class TemporaryCurrentLimitCodec extends MappingCodec<UdtValue, TemporaryCurrentLimitAttributes> {

        public TemporaryCurrentLimitCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(TemporaryCurrentLimitAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected TemporaryCurrentLimitAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new TemporaryCurrentLimitAttributes(value.getString("name"), value.getDouble("value"), value.getInt("acceptableDuration"), value.getBoolean("fictitious"));
        }

        @Override
        protected UdtValue outerToInner(TemporaryCurrentLimitAttributes value) {
            return value == null ? null : getCqlType().newValue().setString("name", value.getName()).setDouble("value", value.getValue()).setInt("acceptableDuration", value.getAcceptableDuration()).setBoolean("fictitious", value.isFictitious());
        }
    }

    private static class PhaseTapChangerStepCodec extends MappingCodec<UdtValue, PhaseTapChangerStepAttributes> {

        public PhaseTapChangerStepCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(PhaseTapChangerStepAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected PhaseTapChangerStepAttributes innerToOuter(UdtValue value) {
            return value == null ? null : PhaseTapChangerStepAttributes.builder()
                    .x(value.getDouble("x"))
                    .b(value.getDouble("b"))
                    .g(value.getDouble("g"))
                    .r(value.getDouble("r"))
                    .alpha(value.getDouble("alpha"))
                    .rho(value.getDouble("rho"))
                    .build();
        }

        @Override
        protected UdtValue outerToInner(PhaseTapChangerStepAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setDouble("rho", value.getRho())
                    .setDouble("r", value.getR())
                    .setDouble("x", value.getX())
                    .setDouble("r", value.getR())
                    .setDouble("g", value.getG())
                    .setDouble("b", value.getB())
                    .setDouble("alpha", value.getAlpha());
        }

    }

    private static class PhaseTapChangerCodec extends MappingCodec<UdtValue, PhaseTapChangerAttributes> {

        public PhaseTapChangerCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(PhaseTapChangerAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected PhaseTapChangerAttributes innerToOuter(UdtValue value) {
            return value == null ? null : PhaseTapChangerAttributes.builder()
                    .targetDeadband(value.getDouble("targetDeadband"))
                    .tapPosition(value.getInt("tapPosition"))
                    .regulationValue(value.getDouble("regulationValue"))
                    .regulationMode(PhaseTapChanger.RegulationMode.valueOf(value.getString("regulationMode")))
                    .regulating(value.getBoolean("regulating"))
                    .lowTapPosition(value.getInt("lowTapPosition"))
                    .steps(value.getList("steps", PhaseTapChangerStepAttributes.class))
                    .regulatingTerminal(value.get(CassandraConstants.REGULATING_TERMINAL, TerminalRefAttributes.class))
                    .build();
        }

        @Override
        protected UdtValue outerToInner(PhaseTapChangerAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setInt("lowTapPosition", value.getLowTapPosition())
                    .setDouble("regulationValue", value.getRegulationValue())
                    .setInt("tapPosition", value.getTapPosition())
                    .setDouble("targetDeadband", value.getTargetDeadband())
                    .setString("regulationMode", value.getRegulationMode().toString())
                    .setBoolean("regulating", value.isRegulating())
                    .setList("steps", value.getSteps(), PhaseTapChangerStepAttributes.class)
                    .set(CassandraConstants.REGULATING_TERMINAL, value.getRegulatingTerminal(), TerminalRefAttributes.class);
        }
    }

    private static class RatioTapChangerCodec extends MappingCodec<UdtValue, RatioTapChangerAttributes> {

        public RatioTapChangerCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(RatioTapChangerAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected RatioTapChangerAttributes innerToOuter(UdtValue value) {
            return value == null ? null : RatioTapChangerAttributes.builder()
                    .targetDeadband(value.getDouble("targetDeadband"))
                    .tapPosition(value.getInt("tapPosition"))
                    .regulating(value.getBoolean("regulating"))
                    .lowTapPosition(value.getInt("lowTapPosition"))
                    .steps(value.getList("steps", RatioTapChangerStepAttributes.class))
                    .loadTapChangingCapabilities(value.getBoolean("loadTapChangingCapabilities"))
                    .targetV(value.getDouble(TARGET_V))
                    .regulatingTerminal(value.get(CassandraConstants.REGULATING_TERMINAL, TerminalRefAttributes.class))
                    .build();
        }

        @Override
        protected UdtValue outerToInner(RatioTapChangerAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setInt("lowTapPosition", value.getLowTapPosition())
                    .setInt("tapPosition", value.getTapPosition())
                    .setDouble("targetDeadband", value.getTargetDeadband())
                    .setBoolean("regulating", value.isRegulating())
                    .setList("steps", value.getSteps(), RatioTapChangerStepAttributes.class)
                    .setDouble(TARGET_V, value.getTargetV())
                    .setBoolean("loadTapChangingCapabilities", value.isLoadTapChangingCapabilities())
                    .set(CassandraConstants.REGULATING_TERMINAL, value.getRegulatingTerminal(), TerminalRefAttributes.class);
        }
    }

    private static class RatioTapChangerStepCodec extends MappingCodec<UdtValue, RatioTapChangerStepAttributes> {

        public RatioTapChangerStepCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(RatioTapChangerStepAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected RatioTapChangerStepAttributes innerToOuter(UdtValue value) {
            return value == null ? null : RatioTapChangerStepAttributes.builder()
                    .x(value.getDouble("x"))
                    .b(value.getDouble("b"))
                    .g(value.getDouble("g"))
                    .r(value.getDouble("r"))
                    .rho(value.getDouble("rho"))
                    .build();
        }

        @Override
        protected UdtValue outerToInner(RatioTapChangerStepAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setDouble("rho", value.getRho())
                    .setDouble("r", value.getR())
                    .setDouble("x", value.getX())
                    .setDouble("r", value.getR())
                    .setDouble("g", value.getG())
                    .setDouble("b", value.getB());
        }
    }

    private static class InternalConnectionCodec extends MappingCodec<UdtValue, InternalConnectionAttributes> {

        public InternalConnectionCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(InternalConnectionAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected InternalConnectionAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new InternalConnectionAttributes(value.getInt("node1"), value.getInt("node2"));
        }

        @Override
        protected UdtValue outerToInner(InternalConnectionAttributes value) {
            return value == null ? null : getCqlType().newValue().setInt("node1", value.getNode1()).setInt("node2", value.getNode2());
        }
    }

    private static class MergedXnodeCodec extends MappingCodec<UdtValue, MergedXnodeAttributes> {

        public MergedXnodeCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(MergedXnodeAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected MergedXnodeAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new MergedXnodeAttributes(
                    value.getDouble("rdp"),
                    value.getDouble("xdp"),
                    value.getDouble("xnodeP1"),
                    value.getDouble("xnodeQ1"),
                    value.getDouble("xnodeP2"),
                    value.getDouble("xnodeQ2"),
                    value.getString("line1Name"),
                    value.getString("line2Name"),
                    value.getString("ucteXnodeCode"));
        }

        @Override
        protected UdtValue outerToInner(MergedXnodeAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setDouble("rdp", value.getRdp())
                    .setDouble("xdp", value.getXdp())
                    .setDouble("xnodeP1", value.getXnodeP1())
                    .setDouble("xnodeQ1", value.getXnodeQ1())
                    .setDouble("xnodeP2", value.getXnodeP2())
                    .setDouble("xnodeQ2", value.getXnodeQ2())
                    .setString("line1Name", value.getLine1Name())
                    .setString("line2Name", value.getLine2Name())
                    .setString("ucteXnodeCode", value.getCode());
        }
    }

    private static class VertexCodec extends MappingCodec<UdtValue, Vertex> {

        public VertexCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(Vertex.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected Vertex innerToOuter(UdtValue value) {
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

        @Override
        protected UdtValue outerToInner(Vertex value) {
            if (value == null) {
                return null;
            }

            UdtValue udtValue = getCqlType().newValue()
                    .setString("id", value.getId())
                    .setString("connectableType", value.getConnectableType().name())
                    .setString("side", value.getSide());

            if (value.getNode() != null) {
                udtValue = udtValue.setInt("node", value.getNode());
            }
            if (value.getBus() != null) {
                udtValue = udtValue.setString("bus", value.getBus());
            }
            return udtValue;
        }
    }

    private static class CalculatedBusCodec extends MappingCodec<UdtValue, CalculatedBusAttributes> {

        public CalculatedBusCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(CalculatedBusAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected CalculatedBusAttributes innerToOuter(UdtValue value) {
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

        @Override
        protected UdtValue outerToInner(CalculatedBusAttributes value) {
            if (value == null) {
                return null;
            }

            UdtValue udtValue = getCqlType().newValue()
                    .setSet("vertices", value.getVertices(), Vertex.class)
                    .setDouble("v", value.getV())
                    .setDouble("angle", value.getAngle());
            if (value.getConnectedComponentNumber() != null) {
                udtValue = udtValue.setInt("ccNum", value.getConnectedComponentNumber());
            }
            if (value.getSynchronousComponentNumber() != null) {
                udtValue = udtValue.setInt("scNum", value.getSynchronousComponentNumber());
            }
            return udtValue;
        }
    }

    private static class ActivePowerControlCodec extends MappingCodec<UdtValue, ActivePowerControlAttributes> {

        public ActivePowerControlCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(ActivePowerControlAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected ActivePowerControlAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new ActivePowerControlAttributes(
                    value.getBoolean("participate"),
                    value.getFloat("droop"));
        }

        @Override
        protected UdtValue outerToInner(ActivePowerControlAttributes value) {
            if (value == null) {
                return null;
            }
            return getCqlType().newValue()
                    .setBoolean("participate", value.isParticipate())
                    .setFloat("droop", value.getDroop());
        }
    }

    private static class EntsoeAreaCodec extends MappingCodec<UdtValue, EntsoeAreaAttributes> {

        public EntsoeAreaCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(EntsoeAreaAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected EntsoeAreaAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new EntsoeAreaAttributes(
                    value.getString("code"));
        }

        @Override
        protected UdtValue outerToInner(EntsoeAreaAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setString("code", value.getCode());
        }
    }

    private static class ShuntCompensatorLinearModelCodec extends MappingCodec<UdtValue, ShuntCompensatorLinearModelAttributes> {

        public ShuntCompensatorLinearModelCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(ShuntCompensatorLinearModelAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected ShuntCompensatorLinearModelAttributes innerToOuter(UdtValue value) {
            return value == null ? null : new ShuntCompensatorLinearModelAttributes(
                    value.getDouble("bPerSection"),
                    value.getDouble("gPerSection"),
                    value.getInt("maximumSectionCount"));
        }

        @Override
        protected UdtValue outerToInner(ShuntCompensatorLinearModelAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setDouble("bPerSection", value.getBPerSection())
                    .setDouble("gPerSection", value.getGPerSection())
                    .setInt("maximumSectionCount", value.getMaximumSectionCount());
        }
    }

    private static class ShuntCompensatorNonLinearSectionCodec extends MappingCodec<UdtValue, ShuntCompensatorNonLinearSectionAttributes> {

        public ShuntCompensatorNonLinearSectionCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(ShuntCompensatorNonLinearSectionAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected ShuntCompensatorNonLinearSectionAttributes innerToOuter(UdtValue value) {
            return value == null ? null : ShuntCompensatorNonLinearSectionAttributes.builder()
                    .b(value.getDouble("b"))
                    .g(value.getDouble("g"))
                    .build();
        }

        @Override
        protected UdtValue outerToInner(ShuntCompensatorNonLinearSectionAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setDouble("b", value.getB())
                    .setDouble("g", value.getG());
        }
    }

    private static class ShuntCompensatorNonLinearModelCodec extends MappingCodec<UdtValue, ShuntCompensatorNonLinearModelAttributes> {

        public ShuntCompensatorNonLinearModelCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(ShuntCompensatorNonLinearModelAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected ShuntCompensatorNonLinearModelAttributes innerToOuter(UdtValue value) {
            return value == null ? null : ShuntCompensatorNonLinearModelAttributes.builder()
                    .sections(value.getList("sections", ShuntCompensatorNonLinearSectionAttributes.class))
                    .build();
        }

        @Override
        protected UdtValue outerToInner(ShuntCompensatorNonLinearModelAttributes value) {
            return value == null ? null : getCqlType().newValue()
                    .setList("sections", value.getSections(), ShuntCompensatorNonLinearSectionAttributes.class);
        }

    }

    private static class CoordinatedReactiveControlCodec extends MappingCodec<UdtValue, CoordinatedReactiveControlAttributes> {

        public CoordinatedReactiveControlCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(CoordinatedReactiveControlAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected CoordinatedReactiveControlAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new CoordinatedReactiveControlAttributes(value.getDouble("qPercent"));
        }

        @Override
        protected UdtValue outerToInner(CoordinatedReactiveControlAttributes value) {
            if (value == null) {
                return null;
            }
            return getCqlType().newValue()
                    .setDouble("qPercent", value.getQPercent());
        }
    }

    private static class RemoteReactivePowerControlCodec extends MappingCodec<UdtValue, RemoteReactivePowerControlAttributes> {

        public RemoteReactivePowerControlCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(RemoteReactivePowerControlAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected RemoteReactivePowerControlAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new RemoteReactivePowerControlAttributes(value.getDouble("targetQ"), value.get("regulatingTerminal", TerminalRefAttributes.class), value.getBoolean("enabled"));
        }

        @Override
        protected UdtValue outerToInner(RemoteReactivePowerControlAttributes value) {
            if (value == null) {
                return null;
            }
            return getCqlType().newValue()
                    .setDouble("targetQ", value.getTargetQ())
                    .set("regulatingTerminal", value.getRegulatingTerminal(), TerminalRefAttributes.class)
                    .setBoolean("enabled", value.isEnabled());
        }
    }

    private static class VoltagePerReactivePowerControlCodec extends MappingCodec<UdtValue, VoltagePerReactivePowerControlAttributes> {

        public VoltagePerReactivePowerControlCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(VoltagePerReactivePowerControlAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected VoltagePerReactivePowerControlAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new VoltagePerReactivePowerControlAttributes(value.getDouble("slope"));
        }

        @Override
        protected UdtValue outerToInner(VoltagePerReactivePowerControlAttributes value) {
            if (value == null) {
                return null;
            }
            return getCqlType().newValue()
                    .setDouble("slope", value.getSlope());
        }
    }

    private static class DanglingLineGenerationCodec extends MappingCodec<UdtValue, DanglingLineGenerationAttributes> {

        public DanglingLineGenerationCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(DanglingLineGenerationAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected DanglingLineGenerationAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            DanglingLineGenerationAttributes attributes = new DanglingLineGenerationAttributes(
                    value.getDouble("minP"),
                    value.getDouble("maxP"),
                    value.getDouble("targetP"),
                    value.getDouble("targetQ"),
                    value.getDouble(TARGET_V),
                    value.getBoolean("voltageRegulatorOn"),
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

        @Override
        protected UdtValue outerToInner(DanglingLineGenerationAttributes value) {
            if (value == null) {
                return null;
            }

            UdtValue udtValue = getCqlType().newValue()
                    .setDouble("minP", value.getMinP())
                    .setDouble("maxP", value.getMaxP())
                    .setDouble("targetP", value.getTargetP())
                    .setDouble("targetQ", value.getTargetQ())
                    .setDouble(TARGET_V, value.getTargetV())
                    .setBoolean("voltageRegulatorOn", value.isVoltageRegulationOn());

            if (value.getReactiveLimits() != null) {
                if (value.getReactiveLimits().getKind() == ReactiveLimitsKind.MIN_MAX) {
                    udtValue = udtValue.set(MIN_MAX_REACTIVE_LIMITS, (MinMaxReactiveLimitsAttributes) value.getReactiveLimits(), MinMaxReactiveLimitsAttributes.class);
                } else {
                    udtValue = udtValue.set(REACTIVE_CAPABILITY_CURVE, (ReactiveCapabilityCurveAttributes) value.getReactiveLimits(), ReactiveCapabilityCurveAttributes.class);
                }
            }
            return udtValue;
        }
    }

    private static class LoadDetailCodec extends MappingCodec<UdtValue, LoadDetailAttributes> {

        public LoadDetailCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(LoadDetailAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected LoadDetailAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new LoadDetailAttributes(
                    value.getFloat("fixedActivePower"),
                    value.getFloat("fixedReactivePower"),
                    value.getFloat("variableActivePower"),
                    value.getFloat("variableReactivePower"));
        }

        @Override
        protected UdtValue outerToInner(LoadDetailAttributes value) {
            if (value == null) {
                return null;
            }

            return getCqlType().newValue()
                    .setFloat("fixedActivePower", value.getFixedActivePower())
                    .setFloat("fixedReactivePower", value.getFixedReactivePower())
                    .setFloat("variableActivePower", value.getVariableActivePower())
                    .setFloat("variableReactivePower", value.getVariableReactivePower());
        }
    }

    private static class CgmesSvMetadataCodec extends MappingCodec<UdtValue, CgmesSvMetadataAttributes> {

        public CgmesSvMetadataCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(CgmesSvMetadataAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected CgmesSvMetadataAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new CgmesSvMetadataAttributes(
                    value.getString("description"),
                    value.getInt("svVersion"),
                    value.getList("Dependencies", String.class),
                    value.getString("modelingAuthoritySet"));
        }

        @Override
        protected UdtValue outerToInner(CgmesSvMetadataAttributes value) {
            if (value == null) {
                return null;
            }

            return getCqlType().newValue()
                    .setString("description", value.getDescription())
                    .setInt("svVersion", value.getSvVersion())
                    .setList("Dependencies", value.getDependencies(), String.class)
                    .setString("modelingAuthoritySet", value.getModelingAuthoritySet());
        }

    }

    private static class CgmesSshMetadataCodec extends MappingCodec<UdtValue, CgmesSshMetadataAttributes> {

        public CgmesSshMetadataCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(CgmesSshMetadataAttributes.class));
        }

        @Override
        public GenericType<CgmesSshMetadataAttributes> getJavaType() {
            return GenericType.of(CgmesSshMetadataAttributes.class);
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected CgmesSshMetadataAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new CgmesSshMetadataAttributes(
                    value.getString("description"),
                    value.getInt("sshVersion"),
                    value.getList("dependencies", String.class),
                    value.getString("modelingAuthoritySet"));
        }

        @Override
        protected UdtValue outerToInner(CgmesSshMetadataAttributes value) {
            if (value == null) {
                return null;
            }

            return getCqlType().newValue()
                    .setString("description", value.getDescription())
                    .setInt("sshVersion", value.getSshVersion())
                    .setList("dependencies", value.getDependencies(), String.class)
                    .setString("modelingAuthoritySet", value.getModelingAuthoritySet());
        }
    }

    private static class CimCharacteristicsCodec extends MappingCodec<UdtValue, CimCharacteristicsAttributes> {

        public CimCharacteristicsCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(CimCharacteristicsAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected CimCharacteristicsAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new CimCharacteristicsAttributes(
                    CgmesTopologyKind.valueOf(value.getString("cgmesTopologyKind")),
                    value.getInt("cimVersion"));
        }

        @Override
        protected UdtValue outerToInner(CimCharacteristicsAttributes value) {
            if (value == null) {
                return null;
            }

            return getCqlType().newValue()
                    .setString("cgmesTopologyKind", value.getCgmesTopologyKind().toString())
                    .setInt("cimVersion", value.getCimVersion() == null ? -1 : value.getCimVersion());
        }
    }

    private static class ThreeWindingsTransformerPhaseAngleClockCodec extends MappingCodec<UdtValue, ThreeWindingsTransformerPhaseAngleClockAttributes> {

        public ThreeWindingsTransformerPhaseAngleClockCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(ThreeWindingsTransformerPhaseAngleClockAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected ThreeWindingsTransformerPhaseAngleClockAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new ThreeWindingsTransformerPhaseAngleClockAttributes(
                    value.getInt("phaseAngleClockLeg2"),
                    value.getInt("phaseAngleClockLeg3"));
        }

        @Override
        protected UdtValue outerToInner(ThreeWindingsTransformerPhaseAngleClockAttributes value) {
            if (value == null) {
                return null;
            }

            return getCqlType().newValue()
                    .setInt("phaseAngleClockLeg2", value.getPhaseAngleClockLeg2())
                    .setInt("phaseAngleClockLeg3", value.getPhaseAngleClockLeg3());
        }
    }

    private static class TwoWindingsTransformerPhaseAngleClockCodec extends MappingCodec<UdtValue, TwoWindingsTransformerPhaseAngleClockAttributes> {

        public TwoWindingsTransformerPhaseAngleClockCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(TwoWindingsTransformerPhaseAngleClockAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected TwoWindingsTransformerPhaseAngleClockAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new TwoWindingsTransformerPhaseAngleClockAttributes(
                    value.getInt("phaseAngleClock"));
        }

        @Override
        protected UdtValue outerToInner(TwoWindingsTransformerPhaseAngleClockAttributes value) {
            if (value == null) {
                return null;
            }

            return getCqlType().newValue()
                    .setInt("phaseAngleClock", value.getPhaseAngleClock());
        }
    }

    private static class CgmesIidmMappingCodec extends MappingCodec<UdtValue, CgmesIidmMappingAttributes> {

        public CgmesIidmMappingCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(CgmesIidmMappingAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected CgmesIidmMappingAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }

            GenericType<Map<String, Set<String>>> genericType = GenericType.mapOf(GenericType.STRING, GenericType.setOf(String.class));
            return new CgmesIidmMappingAttributes(
                  value.getMap("equipmentSideTopologicalNodeMap", TerminalRefAttributes.class, String.class),
                  value.get(1, genericType),
                  value.getSet("unmapped", String.class));
        }

        @Override
        protected UdtValue outerToInner(CgmesIidmMappingAttributes value) {
            if (value == null) {
                return null;
            }
            GenericType<Map<TerminalRefAttributes, String>> genericType1 = GenericType.mapOf(GenericType.of(TerminalRefAttributes.class), GenericType.STRING);
            GenericType<Map<String, Set<String>>> genericType2 = GenericType.mapOf(GenericType.STRING, GenericType.setOf(String.class));
            return getCqlType().newValue()
                    .set(0, value.getEquipmentSideTopologicalNodeMap(), genericType1)
                    .set(1, value.getBusTopologicalNodeMap(), genericType2)
                    .setSet("unmapped", value.getUnmapped(), String.class);
        }
    }

    private static class HvdcAngleDroopActivePowerControlCodec extends MappingCodec<UdtValue, HvdcAngleDroopActivePowerControlAttributes> {

        public HvdcAngleDroopActivePowerControlCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(HvdcAngleDroopActivePowerControlAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected HvdcAngleDroopActivePowerControlAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new HvdcAngleDroopActivePowerControlAttributes(
                    value.getFloat("p0"),
                    value.getFloat("droop"),
                    value.getBoolean("enabled"));
        }

        @Override
        protected UdtValue outerToInner(HvdcAngleDroopActivePowerControlAttributes value) {
            if (value == null) {
                return null;
            }

            return getCqlType().newValue()
                    .setFloat("p0", value.getP0())
                    .setFloat("droop", value.getDroop())
                    .setBoolean("enabled", value.isEnabled());
        }

    }

    private static class HvdcOperatorActivePowerRangeCodec extends MappingCodec<UdtValue, HvdcOperatorActivePowerRangeAttributes> {

        public HvdcOperatorActivePowerRangeCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(HvdcOperatorActivePowerRangeAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected HvdcOperatorActivePowerRangeAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new HvdcOperatorActivePowerRangeAttributes(
                    value.getFloat("oprFromCS1toCS2"),
                    value.getFloat("oprFromCS2toCS1"));
        }

        @Override
        protected UdtValue outerToInner(HvdcOperatorActivePowerRangeAttributes value) {
            if (value == null) {
                return null;
            }

            return getCqlType().newValue()
                    .setFloat("oprFromCS1toCS2", value.getOprFromCS1toCS2())
                    .setFloat("oprFromCS2toCS1", value.getOprFromCS2toCS1());
        }
    }

    private static class CgmesControlAreasCodec extends MappingCodec<UdtValue, CgmesControlAreasAttributes> {

        public CgmesControlAreasCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(CgmesControlAreasAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected CgmesControlAreasAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new CgmesControlAreasAttributes(value.getList("controlAreas", CgmesControlAreaAttributes.class));
        }

        @Override
        protected UdtValue outerToInner(CgmesControlAreasAttributes value) {
            if (value == null) {
                return null;
            }
            return getCqlType().newValue()
                    .setList("controlAreas", value.getControlAreas(), CgmesControlAreaAttributes.class);
        }
    }

    private static class CgmesControlAreaCodec extends MappingCodec<UdtValue, CgmesControlAreaAttributes> {

        public CgmesControlAreaCodec(TypeCodec<UdtValue> innerCodec) {
            super(innerCodec, GenericType.of(CgmesControlAreaAttributes.class));
        }

        @Override
        public UserDefinedType getCqlType() {
            return (UserDefinedType) super.getCqlType();
        }

        @Override
        protected CgmesControlAreaAttributes innerToOuter(UdtValue value) {
            if (value == null) {
                return null;
            }
            return new CgmesControlAreaAttributes(value.getString("id"),
                    value.getString("name"),
                    value.getString("energyIdentificationCodeEic"),
                    value.getList("terminals", TerminalRefAttributes.class),
                    value.getList("boundaries", TerminalRefAttributes.class),
                    value.getDouble("netInterchange"));
        }

        @Override
        protected UdtValue outerToInner(CgmesControlAreaAttributes value) {
            if (value == null) {
                return null;
            }
            return getCqlType().newValue()
                    .setString("id", value.getId())
                    .setString("name", value.getName())
                    .setString("energyIdentificationCodeEic", value.getEnergyIdentificationCodeEic())
                    .setList("terminals", value.getTerminals(), TerminalRefAttributes.class)
                    .setList("boundaries", value.getBoundaries(), TerminalRefAttributes.class)
                    .setDouble("netInterchange", value.getNetInterchange());
        }
    }
}
