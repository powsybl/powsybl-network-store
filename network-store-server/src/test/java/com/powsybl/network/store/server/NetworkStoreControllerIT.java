/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.powsybl.network.store.model.NetworkStoreApi.VERSION;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class NetworkStoreControllerIT {

    private static final UUID NETWORK_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Test
    public void test() throws Exception {
        mvc.perform(get("/" + VERSION + "/networks")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[]"));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Resource<NetworkAttributes> foo = Resource.networkBuilder()
                .id("foo")
                .attributes(NetworkAttributes.builder()
                                             .uuid(NETWORK_UUID)
                                             .variantId("v")
                                             .caseDate(DateTime.parse("2015-01-01T00:00:00.000Z"))
                                             .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(foo))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"uuid\":\"" + NETWORK_UUID + "\",\"id\":\"foo\"}]"));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":\"v\",\"num\":0}]"));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").value("foo"));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/substations")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("{data: []}"));

        Resource<SubstationAttributes> bar = Resource.substationBuilder().id("bar")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .tso("RTE")
                        .entsoeArea(EntsoeAreaAttributes.builder().code("D7").build())
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/substations")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(bar))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/substations/bar")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").value("bar"))
                .andExpect(jsonPath("data[0].attributes.country").value("FR"))
                .andExpect(jsonPath("data[0].attributes.tso").value("RTE"))
                .andExpect(jsonPath("data[0].attributes.entsoeArea.code").value("D7"));

        Resource<SubstationAttributes> bar2 = Resource.substationBuilder()
                .id("bar2")
                .attributes(SubstationAttributes.builder()
                        .country(Country.BE)
                        .tso("ELIA")
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/substations")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(bar2))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/substations")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("meta.totalCount").value("2"))
                .andExpect(jsonPath("data", hasSize(2)))
                .andExpect(jsonPath("data[0].id").value("bar"))
                .andExpect(jsonPath("data[0].attributes.country").value("FR"))
                .andExpect(jsonPath("data[0].attributes.tso").value("RTE"))
                .andExpect(jsonPath("data[1].id").value("bar2"))
                .andExpect(jsonPath("data[1].attributes.country").value("BE"))
                .andExpect(jsonPath("data[1].attributes.tso").value("ELIA"));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/substations?limit=1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("meta.totalCount").value("2"))
                .andExpect(jsonPath("data", hasSize(1)));

        List<InternalConnectionAttributes> ics1 = new ArrayList<>();
        ics1.add(InternalConnectionAttributes.builder()
                .node1(10)
                .node2(20)
                .build());

        Resource<VoltageLevelAttributes> baz = Resource.voltageLevelBuilder()
                .id("baz")
                .attributes(VoltageLevelAttributes.builder()
                        .substationId("bar")
                        .nominalV(380)
                        .lowVoltageLimit(360)
                        .highVoltageLimit(400)
                        .topologyKind(TopologyKind.NODE_BREAKER)
                        .internalConnections(ics1)
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/voltage-levels")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(baz))))
                .andExpect(status().isCreated());

        List<InternalConnectionAttributes> ics2 = new ArrayList<>();
        ics2.add(InternalConnectionAttributes.builder()
                .node1(12)
                .node2(22)
                .build());

        Resource<VoltageLevelAttributes> baz2 = Resource.voltageLevelBuilder()
                .id("baz2")
                .attributes(VoltageLevelAttributes.builder()
                        .substationId("bar2")
                        .nominalV(382)
                        .lowVoltageLimit(362)
                        .highVoltageLimit(402)
                        .topologyKind(TopologyKind.NODE_BREAKER)
                        .internalConnections(ics2)
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/voltage-levels")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(baz2))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/substations/bar/voltage-levels")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("meta.totalCount").value("1"))
                .andExpect(jsonPath("data", hasSize(1)));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data", hasSize(2)));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/baz")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data", hasSize(1)));

        mvc.perform(delete("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/switches/b1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // switch creation and update
        Resource<SwitchAttributes> resBreaker = Resource.switchBuilder()
                .id("b1")
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId("baz")
                        .kind(SwitchKind.BREAKER)
                        .node1(1)
                        .node2(2)
                        .open(false)
                        .retained(false)
                        .fictitious(false)
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/switches")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(resBreaker))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/switches/b1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.open").value("false"));

        resBreaker.getAttributes().setOpen(true);  // opening the breaker switch
        mvc.perform(put("/" + VERSION + "/networks/" + NETWORK_UUID + "/switches")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(resBreaker))))
                .andExpect(status().isOk());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/switches/b1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.open").value("true"));

        // line creation and update
        Resource<LineAttributes> resLine = Resource.lineBuilder()
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
                        .p1(0)
                        .q1(0)
                        .p2(0)
                        .q2(0)
                        .build())
                .build();

        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/lines")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(resLine))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/lines/idLine")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.p1").value(0.));

        resLine.getAttributes().setP1(100.);  // changing p1 value
        mvc.perform(put("/" + VERSION + "/networks/" + NETWORK_UUID + "/lines")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(resLine))))
                .andExpect(status().isOk());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/lines/idLine")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.p1").value(100.));

        // generator creation and update
        Resource<GeneratorAttributes> generator = Resource.generatorBuilder()
                .id("id")
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("gen1")
                        .energySource(EnergySource.HYDRO)
                        .reactiveLimits(MinMaxReactiveLimitsAttributes.builder().maxQ(10).minQ(10).build())
                        .regulatingTerminal(TerminalRefAttributes.builder()
                                .connectableId("idEq")
                                .side("ONE")
                                .build())
                        .build())
                .build();

        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/generators")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(generator))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/generators")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.regulatingTerminal.connectableId").value("idEq"))
                .andExpect(jsonPath("data[0].attributes.regulatingTerminal.side").value("ONE"));

        generator.getAttributes().getRegulatingTerminal().setConnectableId("idEq2");  // changing p1 value
        generator.getAttributes().getRegulatingTerminal().setSide("TWO");  // changing p1 value
        mvc.perform(put("/" + VERSION + "/networks/" + NETWORK_UUID + "/generators")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(generator))))
                .andExpect(status().isOk());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/generators")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.regulatingTerminal.connectableId").value("idEq2"))
                .andExpect(jsonPath("data[0].attributes.regulatingTerminal.side").value("TWO"));

        // battery creation and update
        Resource<BatteryAttributes> battery = Resource.batteryBuilder()
                .id("batteryId")
                .attributes(BatteryAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("battery1")
                        .p0(250)
                        .q0(100)
                        .maxP(500)
                        .minP(100)
                        .reactiveLimits(MinMaxReactiveLimitsAttributes.builder().maxQ(10).minQ(10).build())
                        .build())
                .build();

        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/batteries")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(battery))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/batteries")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.p0").value("250.0"))
                .andExpect(jsonPath("data[0].attributes.q0").value("100.0"))
                .andExpect(jsonPath("data[0].attributes.maxP").value("500.0"))
                .andExpect(jsonPath("data[0].attributes.minP").value("100.0"));

        battery.getAttributes().setP(310);
        battery.getAttributes().setQ(120);
        mvc.perform(put("/" + VERSION + "/networks/" + NETWORK_UUID + "/batteries")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(battery))))
                .andExpect(status().isOk());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/batteries/" + battery.getId())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.p0").value("250.0"))
                .andExpect(jsonPath("data[0].attributes.q0").value("100.0"))
                .andExpect(jsonPath("data[0].attributes.maxP").value("500.0"))
                .andExpect(jsonPath("data[0].attributes.minP").value("100.0"))
                .andExpect(jsonPath("data[0].attributes.p").value("310.0"))
                .andExpect(jsonPath("data[0].attributes.q").value("120.0"));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/vl1/batteries")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.p0").value("250.0"))
                .andExpect(jsonPath("data[0].attributes.q0").value("100.0"))
                .andExpect(jsonPath("data[0].attributes.maxP").value("500.0"))
                .andExpect(jsonPath("data[0].attributes.minP").value("100.0"))
                .andExpect(jsonPath("data[0].attributes.p").value("310.0"))
                .andExpect(jsonPath("data[0].attributes.q").value("120.0"));

        mvc.perform(delete("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/batteries/battery1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // shunt compensator creation and update
        Resource<ShuntCompensatorAttributes> shuntCompensator = Resource.shuntCompensatorBuilder()
                .id("idShunt")
                .attributes(ShuntCompensatorAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("shunt1")
                        .model(ShuntCompensatorLinearModelAttributes.builder().bPerSection(1).gPerSection(2).maximumSectionCount(3).build())
                        .p(100.)
                        .build())
                .build();

        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/shunt-compensators")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(shuntCompensator))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/shunt-compensators")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.model.bperSection").value(1))
                .andExpect(jsonPath("data[0].attributes.model.gperSection").value(2))
                .andExpect(jsonPath("data[0].attributes.model.maximumSectionCount").value(3))
                .andExpect(jsonPath("data[0].attributes.p").value(100.));

        ((ShuntCompensatorLinearModelAttributes) shuntCompensator.getAttributes().getModel()).setBPerSection(15); // changing bPerSection value
        ((ShuntCompensatorLinearModelAttributes) shuntCompensator.getAttributes().getModel()).setGPerSection(22); // changing gPerSection value
        shuntCompensator.getAttributes().setP(200.);  // changing p value

        mvc.perform(put("/" + VERSION + "/networks/" + NETWORK_UUID + "/shunt-compensators")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(shuntCompensator))))
                .andExpect(status().isOk());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/shunt-compensators")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.model.bperSection").value(15))
                .andExpect(jsonPath("data[0].attributes.model.gperSection").value(22))
                .andExpect(jsonPath("data[0].attributes.p").value(200.));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/shunt-compensators/idShunt")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.model.bperSection").value(15))
                .andExpect(jsonPath("data[0].attributes.model.gperSection").value(22))
                .andExpect(jsonPath("data[0].attributes.p").value(200.));
        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/vl1/shunt-compensators")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.model.bperSection").value(15))
                .andExpect(jsonPath("data[0].attributes.model.gperSection").value(22))
                .andExpect(jsonPath("data[0].attributes.p").value(200.));

        // dangling line creation and update
        Resource<DanglingLineAttributes> danglingLine = Resource.danglingLineBuilder()
                .id("idDanglingLine")
                .attributes(DanglingLineAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("dl1")
                        .fictitious(true)
                        .node(5)
                        .p0(10)
                        .q0(20)
                        .r(6)
                        .x(7)
                        .g(8)
                        .b(9)
                        .generation(DanglingLineGenerationAttributes.builder()
                                .minP(1)
                                .maxP(2)
                                .targetP(3)
                                .targetQ(4)
                                .targetV(5)
                                .voltageRegulationOn(false)
                                .reactiveLimits(MinMaxReactiveLimitsAttributes.builder().minQ(20).maxQ(30).build())
                                .build())
                        .ucteXnodeCode("XN1")
                        .currentLimits(LimitsAttributes.builder().permanentLimit(5).build())
                        .p(100.)
                        .q(200)
                        .build())
                .build();

        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/dangling-lines")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(danglingLine))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/dangling-lines")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.p0").value(10))
                .andExpect(jsonPath("data[0].attributes.g").value(8))
                .andExpect(jsonPath("data[0].attributes.generation.maxP").value(2))
                .andExpect(jsonPath("data[0].attributes.generation.targetV").value(5))
                .andExpect(jsonPath("data[0].attributes.generation.voltageRegulationOn").value(false))
                .andExpect(jsonPath("data[0].attributes.generation.reactiveLimits.maxQ").value(30));

        danglingLine.getAttributes().getGeneration().setMaxP(33);
        danglingLine.getAttributes().getGeneration().setVoltageRegulationOn(true);
        danglingLine.getAttributes().getGeneration().setTargetQ(54);

        mvc.perform(put("/" + VERSION + "/networks/" + NETWORK_UUID + "/dangling-lines")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(danglingLine))))
                .andExpect(status().isOk());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/dangling-lines")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.generation.maxP").value(33))
                .andExpect(jsonPath("data[0].attributes.generation.targetQ").value(54))
                .andExpect(jsonPath("data[0].attributes.generation.voltageRegulationOn").value(true));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/dangling-lines/idDanglingLine")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.generation.maxP").value(33))
                .andExpect(jsonPath("data[0].attributes.generation.targetQ").value(54))
                .andExpect(jsonPath("data[0].attributes.generation.voltageRegulationOn").value(true));

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/vl1/dangling-lines")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data[0].attributes.generation.maxP").value(33))
                .andExpect(jsonPath("data[0].attributes.generation.targetQ").value(54))
                .andExpect(jsonPath("data[0].attributes.generation.voltageRegulationOn").value(true));

        // Test removals
        mvc.perform(delete("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/switches/b1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(delete("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/baz")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(delete("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + Resource.INITIAL_VARIANT_NUM + "/substations/bar")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void networkCloneTest() throws Exception {
        // create a simple network with just one substation
        Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                .id("n1")
                .variantNum(0)
                .attributes(NetworkAttributes.builder()
                        .uuid(NETWORK_UUID)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(DateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singleton(n1))))
                .andExpect(status().isCreated());

        Resource<SubstationAttributes> s1 = Resource.substationBuilder()
                .id("s1")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .tso("RTE")
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + NETWORK_UUID + "/substations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singleton(s1))))
                .andExpect(status().isCreated());

        // clone the initial variant
        mvc.perform(put("/" + VERSION + "/networks/" + NETWORK_UUID + "/" + 0 + "/to/" + 1 + "?targetVariantId=v"))
                .andExpect(status().isOk());

        mvc.perform(get("/" + VERSION + "/networks/" + NETWORK_UUID)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[{\"id\":\"InitialState\",\"num\":0},{\"id\":\"v\",\"num\":1}]"));
    }
}
