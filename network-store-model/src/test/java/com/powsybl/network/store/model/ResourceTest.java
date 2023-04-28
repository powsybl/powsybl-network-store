/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
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
                        .caseDate(DateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        String json = objectMapper.writeValueAsString(resource);
        assertEquals("{\"type\":\"NETWORK\",\"id\":\"foo\",\"variantNum\":0,\"attributes\":{\"uuid\":\"7928181c-7977-4592-ba19-88027e4254e4\",\"fictitious\":false,\"caseDate\":1420070400000,\"forecastDistance\":0,\"connectedComponentsValid\":false,\"synchronousComponentsValid\":false}}", json);
        Resource<NetworkAttributes> resource2 = objectMapper.readValue(json, new TypeReference<Resource<NetworkAttributes>>() { });
        assertNotNull(resource2);
        assertEquals("foo", resource2.getId());
        assertEquals(DateTime.parse("2015-01-01T00:00:00.000Z"), resource2.getAttributes().getCaseDate());
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

        String jsonRef = "{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"name\":\"SS\",\"fictitious\":false,\"country\":\"FR\",\"tso\":\"RTE\"}}";
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
                        .branchStatus("IN_OPERATION")
                        .build())
                .build();

        assertFalse(resourceLine.getAttributes().isFictitious());

        assertTrue(Double.isNaN(resourceLine.getAttributes().getP1()));
        assertTrue(Double.isNaN(resourceLine.getAttributes().getQ1()));
        assertTrue(Double.isNaN(resourceLine.getAttributes().getP2()));
        assertTrue(Double.isNaN(resourceLine.getAttributes().getQ2()));

        resourceLine.getAttributes().setP1(100.0);
        assertEquals(100.0, resourceLine.getAttributes().getP1(), 0);

        assertEquals("IN_OPERATION", resourceLine.getAttributes().getBranchStatus());
    }

    @Test
    public void twoWindingsTransormer() {
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
                        .branchStatus("IN_OPERATION")
                        .build())
                .build();

        assertFalse(resourceTransformer.getAttributes().isFictitious());

        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getP1()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getQ1()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getP2()));
        assertTrue(Double.isNaN(resourceTransformer.getAttributes().getQ2()));

        resourceTransformer.getAttributes().setP1(100.0);
        assertEquals(100.0, resourceTransformer.getAttributes().getP1(), 0);

        assertEquals("IN_OPERATION", resourceTransformer.getAttributes().getBranchStatus());
    }

    @Test
    public void threeWindingsTransormer() throws IOException {
        Resource<ThreeWindingsTransformerAttributes> resourceTransformer = Resource.threeWindingsTransformerBuilder()
                .id("id3WT")
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name("id3WT")
                        .ratedU0(1)
                        .branchStatus("IN_OPERATION")
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

        assertEquals("IN_OPERATION", resourceTransformer.getAttributes().getBranchStatus());
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
                .build();

        Resource<LoadAttributes> resourceLoad = Resource.loadBuilder()
                .id("load1")
                .attributes(loadAttributes)
                .build();

        assertFalse(resourceLoad.getAttributes().isFictitious());
        assertEquals(1, resourceLoad.getAttributes().getNode(), 0);

        assertTrue(Double.isNaN(resourceLoad.getAttributes().getP()));
        assertTrue(Double.isNaN(resourceLoad.getAttributes().getQ()));
    }

    @Test
    public void generator() {
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
                .regulatingTerminal(TerminalRefAttributes.builder().side("ONE").connectableId("idEq").build())
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

        assertEquals("idEq", resourceGenerator.getAttributes().getRegulatingTerminal().getConnectableId());
        assertEquals("ONE", resourceGenerator.getAttributes().getRegulatingTerminal().getSide());

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

        ShuntCompensatorAttributes shuntCompensatorAttributes = ShuntCompensatorAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("name")
                .bus("bus1")
                .p(100)
                .q(200)
                .model(linearModelAttributes)
                .sectionCount(2)
                .regulatingTerminal(TerminalRefAttributes.builder().side("ONE").connectableId("idEq").build())
                .build();

        Resource<ShuntCompensatorAttributes> resourceShunt = Resource.shuntCompensatorBuilder()
                .id("shunt1")
                .attributes(shuntCompensatorAttributes)
                .build();

        assertFalse(resourceShunt.getAttributes().isFictitious());
        assertEquals("idEq", resourceShunt.getAttributes().getRegulatingTerminal().getConnectableId());
        assertEquals("ONE", resourceShunt.getAttributes().getRegulatingTerminal().getSide());
        assertEquals(100., resourceShunt.getAttributes().getP(), 0.001);
        assertEquals(200, resourceShunt.getAttributes().getQ(), 0.001);
        assertEquals(2, resourceShunt.getAttributes().getSectionCount());
        assertEquals(ShuntCompensatorModelType.LINEAR, resourceShunt.getAttributes().getModel().getType());
        assertEquals(1, ((ShuntCompensatorLinearModelAttributes) resourceShunt.getAttributes().getModel()).getBPerSection(), 0.001);
        assertEquals(2, ((ShuntCompensatorLinearModelAttributes) resourceShunt.getAttributes().getModel()).getGPerSection(), 0.001);
        assertEquals(3, resourceShunt.getAttributes().getModel().getMaximumSectionCount(), 0.001);
    }

    @Test
    public void danglingLine() {
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
                .ucteXnodeCode("XN1")
                .bus("bus1")
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
        assertEquals("XN1", resourceDanglingLine.getAttributes().getUcteXnodeCode());
        assertEquals("bus1", resourceDanglingLine.getAttributes().getBus());

        assertTrue(Double.isNaN(resourceDanglingLine.getAttributes().getP()));
        assertTrue(Double.isNaN(resourceDanglingLine.getAttributes().getQ()));
    }

    @Test
    public void tieLine() throws JsonProcessingException {
        TieLineAttributes tieLineAttributes = TieLineAttributes
                .builder()
                .name("tieLine1")
                .fictitious(false)
                .half1Id("half1")
                .half2Id("half2")
                .build();

        Resource<TieLineAttributes> resourceTieLine = Resource.tieLineBuilder()
                .id("dl1")
                .attributes(tieLineAttributes)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
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
}
