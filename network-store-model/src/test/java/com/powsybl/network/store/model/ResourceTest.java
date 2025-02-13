/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ResourceTest {

    @Test
    public void networkTest() throws IOException {
        Resource<NetworkAttributes> resource = Resource.networkBuilder().id("foo")
                .attributes(NetworkAttributes.builder()
                        .uuid(UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4"))
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        String json = objectMapper.writeValueAsString(resource);
        assertEquals("{\"type\":\"NETWORK\",\"id\":\"foo\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"uuid\":\"7928181c-7977-4592-ba19-88027e4254e4\",\"fullVariantNum\":-1,\"caseDate\":1420070400000,\"forecastDistance\":0,\"connectedComponentsValid\":false,\"synchronousComponentsValid\":false}}", json);
        Resource<NetworkAttributes> resource2 = objectMapper.readValue(json, new TypeReference<Resource<NetworkAttributes>>() { });
        assertNotNull(resource2);
        assertEquals("foo", resource2.getId());
        assertEquals(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"), resource2.getAttributes().getCaseDate());
        assertEquals(0, resource2.getAttributes().getForecastDistance());
        assertNull(resource2.getAttributes().getSourceFormat());
    }

    @Test
    public void substationTest() throws IOException {
        Resource<SubstationAttributes> resource = Resource.substationBuilder()
                .id("S")
                .attributes(SubstationAttributes.builder()
                        .name("SS")
                        .country(Country.FR)
                        .tso("RTE")
                        .build())
                .build();

        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(resource);

        String jsonRef = "{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"name\":\"SS\",\"fictitious\":false,\"extensionAttributes\":{},\"country\":\"FR\",\"tso\":\"RTE\"}}";
        assertEquals(jsonRef, json);

        Resource<SubstationAttributes> resource2 = objectMapper.readValue(json, new TypeReference<Resource<SubstationAttributes>>() { });
        assertEquals(ResourceType.SUBSTATION, resource2.getType());
        assertEquals("S", resource2.getId());
        assertEquals("SS", resource2.getAttributes().getName());
        assertEquals(Country.FR, resource2.getAttributes().getCountry());
        assertEquals("RTE", resource2.getAttributes().getTso());
    }

    @Test
    public void configuredBus() {
        ConfiguredBusAttributes configuredBusAttributes = ConfiguredBusAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("bus1")
                .fictitious(false)
                .build();

        Resource<ConfiguredBusAttributes> resourceConfiguredBus = Resource.configuredBusBuilder()
                .id("load1")
                .attributes(configuredBusAttributes)
                .build();

        assertFalse(resourceConfiguredBus.getAttributes().isFictitious());

        assertTrue(Double.isNaN(resourceConfiguredBus.getAttributes().getV()));
        assertTrue(Double.isNaN(resourceConfiguredBus.getAttributes().getAngle()));
    }

    @Test
    public void switchTest() {
        Resource<SwitchAttributes> resourceBreaker = Resource.switchBuilder()
                .id("idBreaker")
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("b1")
                        .bus1("bus1")
                        .bus2("bus2")
                        .kind(SwitchKind.BREAKER)
                        .open(false)
                        .fictitious(false)
                        .build())
                .build();

        assertFalse(resourceBreaker.getAttributes().isFictitious());

        assertEquals(Boolean.FALSE, resourceBreaker.getAttributes().isOpen());
        resourceBreaker.getAttributes().setOpen(true);  // opening the breaker switch
        assertEquals(Boolean.TRUE, resourceBreaker.getAttributes().isOpen());

        Resource<SwitchAttributes> resourceDisconnector = Resource.switchBuilder()
                .id("idDisconnector")
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId("vl2")
                        .name("d1")
                        .bus1("bus3")
                        .bus2("bus4")
                        .kind(SwitchKind.DISCONNECTOR)
                        .open(true)
                        .fictitious(false)
                        .build())
                .build();

        assertEquals(Boolean.TRUE, resourceDisconnector.getAttributes().isOpen());
        resourceDisconnector.getAttributes().setOpen(false);  // closing the disconnector switch
        assertEquals(Boolean.FALSE, resourceDisconnector.getAttributes().isOpen());
    }

    @Test
    public void lineTest() {
        Resource<LineAttributes> resourceLine = Resource.lineBuilder()
                .id("idLine")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("idLine")
                        .node1(1)
                        .node2(1)
                        .bus1("bus1")
                        .bus2("bus2")
                        .r(1)
                        .x(1)
                        .g1(1)
                        .b1(1)
                        .g2(1)
                        .b2(1)
                        .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                        .build())
                .build();

        assertFalse(resourceLine.getAttributes().isFictitious());

        assertTrue(Double.isNaN(resourceLine.getAttributes().getP1()));
        assertTrue(Double.isNaN(resourceLine.getAttributes().getQ1()));
        assertTrue(Double.isNaN(resourceLine.getAttributes().getP2()));
        assertTrue(Double.isNaN(resourceLine.getAttributes().getQ2()));

        resourceLine.getAttributes().setP1(100.0);
        assertEquals(100.0, resourceLine.getAttributes().getP1(), 0);
        assertFalse(resourceLine.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceLine.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void twoWindingsTransformer() {
        Resource<TwoWindingsTransformerAttributes> resourceTransformer = Resource.twoWindingsTransformerBuilder()
                .id("id2WT")
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("id2WT")
                        .node1(1)
                        .node2(1)
                        .bus1("bus1")
                        .bus2("bus2")
                        .r(1)
                        .x(1)
                        .b(1)
                        .g(1)
                        .ratedU1(1.)
                        .ratedU2(1.)
                        .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                        .build())
                .build();

        assertFalse(resourceTransformer.getAttributes().isFictitious());

        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getP1()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getQ1()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getP2()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getQ2()));

        resourceTransformer.getAttributes().setP1(100.0);
        assertEquals(100.0, resourceTransformer.getAttributes().getP1(), 0);
        assertFalse(resourceTransformer.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceTransformer.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void threeWindingsTransformer() {
        Resource<ThreeWindingsTransformerAttributes> resourceTransformer = Resource.threeWindingsTransformerBuilder()
                .id("id3WT")
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name("id3WT")
                        .ratedU0(1)
                    .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                        .build())
                .build();

        assertFalse(resourceTransformer.getAttributes().isFictitious());

        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getP1()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getQ1()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getP2()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getQ2()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getP3()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getQ3()));

        resourceTransformer.getAttributes().setP1(200.);
        resourceTransformer.getAttributes().setQ2(500.);
        resourceTransformer.getAttributes().setP3(700.);

        assertEquals(200., resourceTransformer.getAttributes().getP1(), 0);
        assertEquals(500., resourceTransformer.getAttributes().getQ2(), 0);
        assertEquals(700., resourceTransformer.getAttributes().getP3(), 0);
        assertFalse(resourceTransformer.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceTransformer.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void load() {
        LoadAttributes loadAttributes = LoadAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("name")
                .bus("bus1")
                .fictitious(false)
                .node(1)
                .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                .build();

        Resource<LoadAttributes> resourceLoad = Resource.loadBuilder()
                .id("load1")
                .attributes(loadAttributes)
                .build();

        assertFalse(resourceLoad.getAttributes().isFictitious());
        assertEquals(1, resourceLoad.getAttributes().getNode(), 0);

        assertTrue(Double.isNaN(resourceLoad.getAttributes().getP()));
        assertTrue(Double.isNaN(resourceLoad.getAttributes().getQ()));
        assertFalse(resourceLoad.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceLoad.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void generator() {
        TerminalRefAttributes regulatingTerminal = TerminalRefAttributes.builder().side("ONE").connectableId("idEq").build();
        RegulatingPointAttributes regulatingPointAttributes = RegulatingPointAttributes.builder()
            .regulatingEquipmentId("gen")
            .regulatingResourceType(ResourceType.GENERATOR)
            .regulatingTerminal(regulatingTerminal)
            .localTerminal(new TerminalRefAttributes("gen", null))
            .build();
        GeneratorAttributes generatorAttributes = GeneratorAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("name")
                .bus("bus1")
                .energySource(EnergySource.HYDRO)
                .maxP(1)
                .minP(2)
                .fictitious(false)
                .node(1)
                .targetP(3)
                .targetV(4)
                .regulatingPoint(regulatingPointAttributes)
                .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                .build();

        Resource<GeneratorAttributes> resourceGenerator = Resource.generatorBuilder()
                .id("gen1")
                .attributes(generatorAttributes)
                .build();

        assertFalse(resourceGenerator.getAttributes().isFictitious());
        assertEquals(1, resourceGenerator.getAttributes().getMaxP(), 0);
        assertEquals(2, resourceGenerator.getAttributes().getMinP(), 0);
        assertEquals(3, resourceGenerator.getAttributes().getTargetP(), 0);
        assertEquals(4, resourceGenerator.getAttributes().getTargetV(), 0);
        assertEquals(1, resourceGenerator.getAttributes().getNode(), 0);

        assertTrue(Double.isNaN(resourceGenerator.getAttributes().getP()));
        assertTrue(Double.isNaN(resourceGenerator.getAttributes().getQ()));

        assertEquals("idEq", resourceGenerator.getAttributes().getRegulatingPoint().getRegulatingTerminal().getConnectableId());
        assertEquals("ONE", resourceGenerator.getAttributes().getRegulatingPoint().getRegulatingTerminal().getSide());
        assertEquals("gen", resourceGenerator.getAttributes().getRegulatingPoint().getLocalTerminal().getConnectableId());
        assertNull(resourceGenerator.getAttributes().getRegulatingPoint().getLocalTerminal().getSide());
        assertFalse(resourceGenerator.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceGenerator.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void battery() {
        BatteryAttributes batteryAttributes = BatteryAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("name")
                .bus("bus1")
                .maxP(300)
                .minP(200)
                .targetP(250)
                .targetQ(100)
                .fictitious(false)
                .node(1)
                .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                .build();

        Resource<BatteryAttributes> resourceBattery = Resource.batteryBuilder()
                .id("battery1")
                .attributes(batteryAttributes)
                .build();

        assertEquals(Boolean.FALSE, resourceBattery.getAttributes().isFictitious());
        assertEquals(300, resourceBattery.getAttributes().getMaxP(), 0);
        assertEquals(200, resourceBattery.getAttributes().getMinP(), 0);
        assertEquals(250, resourceBattery.getAttributes().getTargetP(), 0);
        assertEquals(100, resourceBattery.getAttributes().getTargetQ(), 0);
        assertEquals(1, resourceBattery.getAttributes().getNode(), 0);

        assertTrue(Double.isNaN(resourceBattery.getAttributes().getP()));
        assertTrue(Double.isNaN(resourceBattery.getAttributes().getQ()));
        assertFalse(resourceBattery.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceBattery.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void ground() {
        GroundAttributes groundAttributes = GroundAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("name")
                .bus("bus1")
                .p(250)
                .q(100)
                .fictitious(false)
                .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                .node(1)
                .build();

        Resource<GroundAttributes> resourceGround = Resource.groundBuilder()
                .id("ground1")
                .attributes(groundAttributes)
                .build();

        assertEquals(Boolean.FALSE, resourceGround.getAttributes().isFictitious());
        assertEquals(250, resourceGround.getAttributes().getP(), 0);
        assertEquals(100, resourceGround.getAttributes().getQ(), 0);
        assertEquals(1, resourceGround.getAttributes().getNode(), 0);
        assertFalse(resourceGround.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceGround.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void shuntCompensator() {
        ShuntCompensatorLinearModelAttributes linearModelAttributes = ShuntCompensatorLinearModelAttributes.builder()
                        .bPerSection(1)
                        .gPerSection(2)
                        .maximumSectionCount(3)
                        .build();
        assertEquals(3, linearModelAttributes.getMaximumSectionCount());
        assertEquals(2, linearModelAttributes.getB(2), 0.1);
        assertEquals(4, linearModelAttributes.getG(2), 0.1);

        ShuntCompensatorNonLinearModelAttributes nonLinearModelAttributes = ShuntCompensatorNonLinearModelAttributes.builder()
                .sections(Arrays.asList(ShuntCompensatorNonLinearSectionAttributes.builder().b(1).g(2).build(),
                        ShuntCompensatorNonLinearSectionAttributes.builder().b(5).g(6).build()))
                .build();
        assertEquals(2, nonLinearModelAttributes.getMaximumSectionCount());
        assertEquals(5, nonLinearModelAttributes.getB(2), 0.1);
        assertEquals(2, nonLinearModelAttributes.getG(1), 0.1);

        TerminalRefAttributes regulatingTerminal = TerminalRefAttributes.builder().side("ONE").connectableId("idEq").build();
        RegulatingPointAttributes regulatingPointAttributes = RegulatingPointAttributes.builder()
            .regulatingEquipmentId("shunt")
            .regulatingResourceType(ResourceType.SHUNT_COMPENSATOR)
            .regulatingTerminal(regulatingTerminal)
            .localTerminal(new TerminalRefAttributes("shunt", null))
            .build();
        ShuntCompensatorAttributes shuntCompensatorAttributes = ShuntCompensatorAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("name")
                .bus("bus1")
                .p(100)
                .q(200)
                .model(linearModelAttributes)
                .sectionCount(2)
                .regulatingPoint(regulatingPointAttributes)
                .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                .build();

        Resource<ShuntCompensatorAttributes> resourceShunt = Resource.shuntCompensatorBuilder()
                .id("shunt1")
                .attributes(shuntCompensatorAttributes)
                .build();

        assertFalse(resourceShunt.getAttributes().isFictitious());
        assertEquals("idEq", resourceShunt.getAttributes().getRegulatingPoint().getRegulatingTerminal().getConnectableId());
        assertEquals("ONE", resourceShunt.getAttributes().getRegulatingPoint().getRegulatingTerminal().getSide());
        assertEquals("shunt", resourceShunt.getAttributes().getRegulatingPoint().getLocalTerminal().getConnectableId());
        assertNull(resourceShunt.getAttributes().getRegulatingPoint().getLocalTerminal().getSide());
        assertEquals(100., resourceShunt.getAttributes().getP(), 0.001);
        assertEquals(200, resourceShunt.getAttributes().getQ(), 0.001);
        assertEquals(2, resourceShunt.getAttributes().getSectionCount());
        assertEquals(ShuntCompensatorModelType.LINEAR, resourceShunt.getAttributes().getModel().getType());
        assertEquals(1, ((ShuntCompensatorLinearModelAttributes) resourceShunt.getAttributes().getModel()).getBPerSection(), 0.001);
        assertEquals(2, ((ShuntCompensatorLinearModelAttributes) resourceShunt.getAttributes().getModel()).getGPerSection(), 0.001);
        assertEquals(3, resourceShunt.getAttributes().getModel().getMaximumSectionCount(), 0.001);
        assertFalse(resourceShunt.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceShunt.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void danglingLine() throws JsonProcessingException {
        DanglingLineGenerationAttributes danglingLineGenerationAttributes = DanglingLineGenerationAttributes
                .builder()
                .minP(100)
                .maxP(200)
                .targetP(300)
                .targetQ(400)
                .targetV(500)
                .voltageRegulationOn(true)
                .reactiveLimits(MinMaxReactiveLimitsAttributes.builder().minQ(10).maxQ(20).build())
                .build();

        DanglingLineAttributes danglingLineAttributes = DanglingLineAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("dl1")
                .fictitious(false)
                .node(1)
                .p0(1000)
                .q0(2000)
                .r(1)
                .x(2)
                .g(3)
                .b(4)
                .generation(danglingLineGenerationAttributes)
                .pairingKey("XN1")
                .bus("bus1")
                .tieLineId("idTieLineParent")
                .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                .build();

        Resource<DanglingLineAttributes> resourceDanglingLine = Resource.danglingLineBuilder()
                .id("dl1")
                .attributes(danglingLineAttributes)
                .build();

        assertEquals("vl1", resourceDanglingLine.getAttributes().getVoltageLevelId());
        assertEquals("dl1", resourceDanglingLine.getAttributes().getName());
        assertFalse(resourceDanglingLine.getAttributes().isFictitious());
        assertEquals(1, resourceDanglingLine.getAttributes().getNode(), 0);
        assertEquals(1000, resourceDanglingLine.getAttributes().getP0(), 0);
        assertEquals(2000, resourceDanglingLine.getAttributes().getQ0(), 0);
        assertEquals(1, resourceDanglingLine.getAttributes().getR(), 0);
        assertEquals(2, resourceDanglingLine.getAttributes().getX(), 0);
        assertEquals(3, resourceDanglingLine.getAttributes().getG(), 0);
        assertEquals(4, resourceDanglingLine.getAttributes().getB(), 0);
        assertEquals(100, resourceDanglingLine.getAttributes().getGeneration().getMinP(), 0);
        assertEquals(200, resourceDanglingLine.getAttributes().getGeneration().getMaxP(), 0);
        assertEquals(300, resourceDanglingLine.getAttributes().getGeneration().getTargetP(), 0);
        assertEquals(400, resourceDanglingLine.getAttributes().getGeneration().getTargetQ(), 0);
        assertEquals(500, resourceDanglingLine.getAttributes().getGeneration().getTargetV(), 0);
        assertTrue(resourceDanglingLine.getAttributes().getGeneration().isVoltageRegulationOn());
        assertEquals(ReactiveLimitsKind.MIN_MAX, resourceDanglingLine.getAttributes().getGeneration().getReactiveLimits().getKind());
        assertEquals(10, ((MinMaxReactiveLimitsAttributes) resourceDanglingLine.getAttributes().getGeneration().getReactiveLimits()).getMinQ(), 0);
        assertEquals(20, ((MinMaxReactiveLimitsAttributes) resourceDanglingLine.getAttributes().getGeneration().getReactiveLimits()).getMaxQ(), 0);
        assertEquals("XN1", resourceDanglingLine.getAttributes().getPairingKey());
        assertEquals("bus1", resourceDanglingLine.getAttributes().getBus());
        assertEquals("idTieLineParent", resourceDanglingLine.getAttributes().getTieLineId());

        assertTrue(Double.isNaN(resourceDanglingLine.getAttributes().getP()));
        assertTrue(Double.isNaN(resourceDanglingLine.getAttributes().getQ()));
        assertFalse(resourceDanglingLine.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceDanglingLine.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(resourceDanglingLine);

        Resource<DanglingLineAttributes> resource2 = objectMapper.readValue(json, new TypeReference<Resource<DanglingLineAttributes>>() { });
        assertNotNull(resource2);
    }

    @Test
    public void tieLine() throws JsonProcessingException {
        TieLineAttributes tieLineAttributes = TieLineAttributes
                .builder()
                .name("tieLine1")
                .fictitious(false)
                .danglingLine1Id("half1")
                .danglingLine2Id("half2")
                .build();

        Resource<TieLineAttributes> resourceTieLine = Resource.tieLineBuilder()
                .id("dl1")
                .attributes(tieLineAttributes)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(resourceTieLine);

        Resource<TieLineAttributes> resource2 = objectMapper.readValue(json, new TypeReference<Resource<TieLineAttributes>>() { });
        assertNotNull(resource2);
    }

    @Test
    public void toSvTest() {
        LoadAttributes attributes = LoadAttributes.builder()
                .voltageLevelId("vl1")
                .name("name")
                .bus("bus1")
                .fictitious(false)
                .node(1)
                .p(10d)
                .q(20.4)
                .build();

        Resource<LoadAttributes> resource = Resource.loadBuilder()
                .id("load1")
                .attributes(attributes)
                .build();
        assertNull(resource.getFilter());

        Resource<Attributes> svResource = resource.filterAttributes(AttributeFilter.SV);
        assertEquals(ResourceType.LOAD, svResource.getType());
        assertEquals("load1", svResource.getId());
        assertEquals(0, svResource.getVariantNum());
        assertSame(AttributeFilter.SV, svResource.getFilter());
        assertTrue(svResource.getAttributes() instanceof InjectionSvAttributes);
        assertEquals(10d, ((InjectionSvAttributes) svResource.getAttributes()).getP(), 0);
        assertEquals(20.4d, ((InjectionSvAttributes) svResource.getAttributes()).getQ(), 0);
    }

    @Test
    public void busBarSection() {
        Resource<BusbarSectionAttributes> resourceTransformer = Resource.busbarSectionBuilder()
            .id("idBbs")
            .attributes(BusbarSectionAttributes.builder()
                .voltageLevelId("vl1")
                .name("bbs")
                .regulatingEquipments(Collections.singleton(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)))
                .build())
            .build();

        assertEquals("idBbs", resourceTransformer.getId());
        assertEquals("vl1", resourceTransformer.getAttributes().getVoltageLevelId());
        assertEquals("bbs", resourceTransformer.getAttributes().getName());
        assertFalse(resourceTransformer.getAttributes().getRegulatingEquipments().isEmpty());
        assertTrue(resourceTransformer.getAttributes().getRegulatingEquipments().contains(new RegulatingEquipmentIdentifier("gen1", ResourceType.GENERATOR)));
    }

    @Test
    public void resourceTypeTest() {
        assertEquals(ResourceType.NETWORK, ResourceType.convert(IdentifiableType.NETWORK));
        assertEquals(ResourceType.SUBSTATION, ResourceType.convert(IdentifiableType.SUBSTATION));
        assertEquals(ResourceType.VOLTAGE_LEVEL, ResourceType.convert(IdentifiableType.VOLTAGE_LEVEL));
        assertThrows(PowsyblException.class, () -> ResourceType.convert(IdentifiableType.AREA));
        assertEquals(ResourceType.HVDC_LINE, ResourceType.convert(IdentifiableType.HVDC_LINE));
        assertThrows(PowsyblException.class, () -> ResourceType.convert(IdentifiableType.BUS));
        assertEquals(ResourceType.SWITCH, ResourceType.convert(IdentifiableType.SWITCH));
        assertEquals(ResourceType.BUSBAR_SECTION, ResourceType.convert(IdentifiableType.BUSBAR_SECTION));
        assertEquals(ResourceType.LINE, ResourceType.convert(IdentifiableType.LINE));
        assertEquals(ResourceType.TIE_LINE, ResourceType.convert(IdentifiableType.TIE_LINE));
        assertEquals(ResourceType.TWO_WINDINGS_TRANSFORMER, ResourceType.convert(IdentifiableType.TWO_WINDINGS_TRANSFORMER));
        assertEquals(ResourceType.THREE_WINDINGS_TRANSFORMER, ResourceType.convert(IdentifiableType.THREE_WINDINGS_TRANSFORMER));
        assertEquals(ResourceType.GENERATOR, ResourceType.convert(IdentifiableType.GENERATOR));
        assertEquals(ResourceType.BATTERY, ResourceType.convert(IdentifiableType.BATTERY));
        assertEquals(ResourceType.LOAD, ResourceType.convert(IdentifiableType.LOAD));
        assertEquals(ResourceType.SHUNT_COMPENSATOR, ResourceType.convert(IdentifiableType.SHUNT_COMPENSATOR));
        assertEquals(ResourceType.DANGLING_LINE, ResourceType.convert(IdentifiableType.DANGLING_LINE));
        assertEquals(ResourceType.STATIC_VAR_COMPENSATOR, ResourceType.convert(IdentifiableType.STATIC_VAR_COMPENSATOR));
        assertEquals(ResourceType.VSC_CONVERTER_STATION, ResourceType.convert(IdentifiableType.HVDC_CONVERTER_STATION));
        assertThrows(PowsyblException.class, () -> ResourceType.convert(IdentifiableType.OVERLOAD_MANAGEMENT_SYSTEM));
        assertEquals(ResourceType.GROUND, ResourceType.convert(IdentifiableType.GROUND));
    }
}
