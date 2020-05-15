/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.DanglingLineAttributes;
import com.powsybl.network.store.model.GeneratorAttributes;
import com.powsybl.network.store.model.HvdcLineAttributes;
import com.powsybl.network.store.model.LccConverterStationAttributes;
import com.powsybl.network.store.model.LegAttributes;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.StaticVarCompensatorAttributes;
import com.powsybl.network.store.model.SwitchAttributes;
import com.powsybl.network.store.model.ThreeWindingsTransformerAttributes;
import com.powsybl.network.store.model.TopLevelDocument;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;
import com.powsybl.network.store.model.VscConverterStationAttributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(PreloadingRestNetworkStoreClientTest.class)
@ContextConfiguration(classes = RestNetworkStoreClient.class)
public class PreloadingRestNetworkStoreClientTest {

    @Autowired
    private RestNetworkStoreClient restStoreClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws IOException {
    }

    @Test
    public void testSwitchCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive switch retrievals, only the first should send a REST request, the second uses the cache
        Resource<SwitchAttributes> breaker = Resource.switchBuilder(networkUuid, cachedClient)
                .id("b1")
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId("vl1")
                        .kind(SwitchKind.BREAKER)
                        .node1(1)
                        .node2(2)
                        .open(false)
                        .retained(false)
                        .fictitious(false)
                        .build())
                .build();

        String breakersJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(breaker)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/switches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(breakersJson, MediaType.APPLICATION_JSON));

        // First time switch retrieval by Id
        Resource<SwitchAttributes> switchAttributesResource = cachedClient.getSwitch(networkUuid, "b1").orElse(null);
        assertNotNull(switchAttributesResource);
        assertEquals(Boolean.FALSE, switchAttributesResource.getAttributes().isOpen());  // test switch is closed

        switchAttributesResource.getAttributes().setOpen(true);  // change switch state

        // Second time switch retrieval by Id
        switchAttributesResource = cachedClient.getSwitch(networkUuid, "b1").orElse(null);
        assertNotNull(switchAttributesResource);
        assertEquals(Boolean.TRUE, switchAttributesResource.getAttributes().isOpen());  // test switch is open

        server.verify();
    }

    @Test
    public void testGeneratorCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive generator retrievals, only the first should send a REST request, the second uses the cache
        Resource<GeneratorAttributes> generator = Resource.generatorBuilder(networkUuid, cachedClient)
                .id("g1")
                .attributes(GeneratorAttributes.builder()
                            .voltageLevelId("vl1")
                            .name("g1")
                            .p(200)
                        .build())
                .build();

        String generatorsJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(generator)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/generators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(generatorsJson, MediaType.APPLICATION_JSON));

        // First time generator retrieval by Id
        Resource<GeneratorAttributes> generatorAttributesResource = cachedClient.getGenerator(networkUuid, "g1").orElse(null);
        assertNotNull(generatorAttributesResource);
        assertEquals(200., generatorAttributesResource.getAttributes().getP(), 0.001);

        generatorAttributesResource.getAttributes().setP(300.);

        // Second time generator retrieval by Id
        generatorAttributesResource = cachedClient.getGenerator(networkUuid, "g1").orElse(null);
        assertNotNull(generatorAttributesResource);
        assertEquals(300., generatorAttributesResource.getAttributes().getP(), 0.001);

        server.verify();
    }

    @Test
    public void testLoadCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive load retrievals, only the first should send a REST request, the second uses the cache
        Resource<LoadAttributes> load = Resource.loadBuilder(networkUuid, cachedClient)
                .id("l1")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("l1")
                        .loadType(LoadType.AUXILIARY)
                        .p0(100)
                        .build())
                .build();

        String loadsJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(load)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/loads"))
                .andExpect(method(GET))
                .andRespond(withSuccess(loadsJson, MediaType.APPLICATION_JSON));

        // First time load retrieval by Id
        Resource<LoadAttributes> loadAttributesResource = cachedClient.getLoad(networkUuid, "l1").orElse(null);
        assertNotNull(loadAttributesResource);
        assertEquals(LoadType.AUXILIARY, loadAttributesResource.getAttributes().getLoadType());
        assertEquals(100., loadAttributesResource.getAttributes().getP0(), 0.001);

        loadAttributesResource.getAttributes().setLoadType(LoadType.FICTITIOUS);
        loadAttributesResource.getAttributes().setP0(2000.);

        // Second time load retrieval by Id
        loadAttributesResource = cachedClient.getLoad(networkUuid, "l1").orElse(null);
        assertNotNull(loadAttributesResource);
        assertEquals(LoadType.FICTITIOUS, loadAttributesResource.getAttributes().getLoadType());
        assertEquals(2000., loadAttributesResource.getAttributes().getP0(), 0.001);

        server.verify();
    }

    @Test
    public void testShuntCompensatorCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive shunt compensator retrievals, only the first should send a REST request, the second uses the cache
        Resource<ShuntCompensatorAttributes> shuntCompensator = Resource.shuntCompensatorBuilder(networkUuid, cachedClient)
                .id("sc1")
                .attributes(ShuntCompensatorAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("sc1")
                        .maximumSectionCount(5)
                        .build())
                .build();

        String shuntCompenstorJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(shuntCompensator)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/shunt-compensators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(shuntCompenstorJson, MediaType.APPLICATION_JSON));

        // First time shunt compensator retrieval by Id
        Resource<ShuntCompensatorAttributes> shuntCompensatorAttributesResource = cachedClient.getShuntCompensator(networkUuid, "sc1").orElse(null);
        assertNotNull(shuntCompensatorAttributesResource);
        assertEquals(5, shuntCompensatorAttributesResource.getAttributes().getMaximumSectionCount());

        shuntCompensatorAttributesResource.getAttributes().setMaximumSectionCount(8);

        // Second time shunt compensator retrieval by Id
        shuntCompensatorAttributesResource = cachedClient.getShuntCompensator(networkUuid, "sc1").orElse(null);
        assertNotNull(shuntCompensatorAttributesResource);
        assertEquals(8, shuntCompensatorAttributesResource.getAttributes().getMaximumSectionCount());

        server.verify();
    }

    @Test
    public void testStaticVarCompensatorCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive static var compensator retrievals, only the first should send a REST request, the second uses the cache
        Resource<StaticVarCompensatorAttributes> staticVarCompensator = Resource.staticVarCompensatorBuilder(networkUuid, cachedClient)
                .id("svc1")
                .attributes(StaticVarCompensatorAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("svc1")
                        .bmax(20)
                        .reactivePowerSetPoint(100)
                        .build())
                .build();

        String staticVarCompenstorJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(staticVarCompensator)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/static-var-compensators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(staticVarCompenstorJson, MediaType.APPLICATION_JSON));

        // First time static var compensator retrieval by Id
        Resource<StaticVarCompensatorAttributes> staticVarCompensatorAttributesResource = cachedClient.getStaticVarCompensator(networkUuid, "svc1").orElse(null);
        assertNotNull(staticVarCompensatorAttributesResource);
        assertEquals(20, staticVarCompensatorAttributesResource.getAttributes().getBmax(), 0.001);
        assertEquals(100, staticVarCompensatorAttributesResource.getAttributes().getReactivePowerSetPoint(), 0.001);

        staticVarCompensatorAttributesResource.getAttributes().setBmax(50.);
        staticVarCompensatorAttributesResource.getAttributes().setReactivePowerSetPoint(1500.);

        // Second time static var compensator retrieval by Id
        staticVarCompensatorAttributesResource = cachedClient.getStaticVarCompensator(networkUuid, "svc1").orElse(null);
        assertNotNull(staticVarCompensatorAttributesResource);
        assertEquals(50., staticVarCompensatorAttributesResource.getAttributes().getBmax(), 0.001);
        assertEquals(1500., staticVarCompensatorAttributesResource.getAttributes().getReactivePowerSetPoint(), 0.001);

        server.verify();
    }

    @Test
    public void testVscConverterStationCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive vsc converter station retrievals, only the first should send a REST request, the second uses the cache
        Resource<VscConverterStationAttributes> vscConverterStation = Resource.vscConverterStationBuilder(networkUuid, cachedClient)
                .id("vsc1")
                .attributes(VscConverterStationAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("vsc1")
                        .lossFactor(0.6F)
                        .build())
                .build();

        String vscConverterStationJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(vscConverterStation)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/vsc-converter-stations"))
                .andExpect(method(GET))
                .andRespond(withSuccess(vscConverterStationJson, MediaType.APPLICATION_JSON));

        // First time vsc converter station retrieval by Id
        Resource<VscConverterStationAttributes> vscConverterStationAttributesResource = cachedClient.getVscConverterStation(networkUuid, "vsc1").orElse(null);
        assertNotNull(vscConverterStationAttributesResource);
        assertEquals(0.6F, vscConverterStationAttributesResource.getAttributes().getLossFactor(), 0.001);

        vscConverterStationAttributesResource.getAttributes().setLossFactor(0.8F);

        // Second time vsc converter station retrieval by Id
        vscConverterStationAttributesResource = cachedClient.getVscConverterStation(networkUuid, "vsc1").orElse(null);
        assertNotNull(vscConverterStationAttributesResource);
        assertEquals(0.8, vscConverterStationAttributesResource.getAttributes().getLossFactor(), 0.001);

        server.verify();
    }

    @Test
    public void testLccConverterStationCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive lcc converter station retrievals, only the first should send a REST request, the second uses the cache
        Resource<LccConverterStationAttributes> lccConverterStation = Resource.lccConverterStationBuilder(networkUuid, cachedClient)
                .id("lcc1")
                .attributes(LccConverterStationAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("lcc1")
                        .powerFactor(250)
                        .build())
                .build();

        String lccConverterStationJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(lccConverterStation)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lcc-converter-stations"))
                .andExpect(method(GET))
                .andRespond(withSuccess(lccConverterStationJson, MediaType.APPLICATION_JSON));

        // First time lcc converter station retrieval by Id
        Resource<LccConverterStationAttributes> lccConverterStationAttributesResource = cachedClient.getLccConverterStation(networkUuid, "lcc1").orElse(null);
        assertNotNull(lccConverterStationAttributesResource);
        assertEquals(250, lccConverterStationAttributesResource.getAttributes().getPowerFactor(), 0.001);

        lccConverterStationAttributesResource.getAttributes().setPowerFactor(400);

        // Second time lcc converter station retrieval by Id
        lccConverterStationAttributesResource = cachedClient.getLccConverterStation(networkUuid, "lcc1").orElse(null);
        assertNotNull(lccConverterStationAttributesResource);
        assertEquals(400, lccConverterStationAttributesResource.getAttributes().getPowerFactor(), 0.001);

        server.verify();
    }

    @Test
    public void testTwoWindingsTransformerCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive two windings transformer retrievals, only the first should send a REST request, the second uses the cache
        Resource<TwoWindingsTransformerAttributes> twoWindingsTransformer = Resource.twoWindingsTransformerBuilder(networkUuid, cachedClient)
                .id("tw1")
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .r(2)
                        .x(3)
                        .build())
                .build();

        String twoWindingsTransformerJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(twoWindingsTransformer)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/2-windings-transformers"))
                .andExpect(method(GET))
                .andRespond(withSuccess(twoWindingsTransformerJson, MediaType.APPLICATION_JSON));

        // First time two windings transformer retrieval by Id
        Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerAttributesResource = cachedClient.getTwoWindingsTransformer(networkUuid, "tw1").orElse(null);
        assertNotNull(twoWindingsTransformerAttributesResource);
        assertEquals(2, twoWindingsTransformerAttributesResource.getAttributes().getR(), 0.001);
        assertEquals(3, twoWindingsTransformerAttributesResource.getAttributes().getX(), 0.001);

        twoWindingsTransformerAttributesResource.getAttributes().setR(5);
        twoWindingsTransformerAttributesResource.getAttributes().setX(9);

        // Second time two windings transformer retrieval by Id
        twoWindingsTransformerAttributesResource = cachedClient.getTwoWindingsTransformer(networkUuid, "tw1").orElse(null);
        assertNotNull(twoWindingsTransformerAttributesResource);
        assertEquals(5, twoWindingsTransformerAttributesResource.getAttributes().getR(), 0.001);
        assertEquals(9, twoWindingsTransformerAttributesResource.getAttributes().getX(), 0.001);

        server.verify();
    }

    @Test
    public void testThreeWindingsTransformerCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive three windings transformer retrievals, only the first should send a REST request, the second uses the cache
        Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformer = Resource.threeWindingsTransformerBuilder(networkUuid, cachedClient)
                .id("tw1")
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .leg1(LegAttributes.builder()
                                .legNumber(1)
                                .voltageLevelId("vl1")
                                .build())
                        .leg2(LegAttributes.builder()
                                .legNumber(2)
                                .voltageLevelId("vl2")
                                .build())
                        .p2(50)
                        .leg3(LegAttributes.builder()
                                .legNumber(3)
                                .voltageLevelId("vl3")
                                .build())
                        .q3(60)
                        .build())
                .build();

        String threeWindingsTransformerJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(threeWindingsTransformer)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/3-windings-transformers"))
                .andExpect(method(GET))
                .andRespond(withSuccess(threeWindingsTransformerJson, MediaType.APPLICATION_JSON));

        // First time three windings transformer retrieval by Id
        Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerAttributesResource = cachedClient.getThreeWindingsTransformer(networkUuid, "tw1").orElse(null);
        assertNotNull(threeWindingsTransformerAttributesResource);
        assertEquals(50, threeWindingsTransformerAttributesResource.getAttributes().getP2(), 0.001);
        assertEquals(60, threeWindingsTransformerAttributesResource.getAttributes().getQ3(), 0.001);

        threeWindingsTransformerAttributesResource.getAttributes().setP2(200);
        threeWindingsTransformerAttributesResource.getAttributes().setQ3(550);

        // Second time three windings transformer retrieval by Id
        threeWindingsTransformerAttributesResource = cachedClient.getThreeWindingsTransformer(networkUuid, "tw1").orElse(null);
        assertNotNull(threeWindingsTransformerAttributesResource);
        assertEquals(200, threeWindingsTransformerAttributesResource.getAttributes().getP2(), 0.001);
        assertEquals(550, threeWindingsTransformerAttributesResource.getAttributes().getQ3(), 0.001);

        server.verify();
    }

    @Test
    public void testLineCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive line retrievals, only the first should send a REST request, the second uses the cache
        Resource<LineAttributes> line = Resource.lineBuilder(networkUuid, cachedClient)
                .id("l1")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("l1")
                        .p1(50)
                        .build())
                .build();

        String linesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(line)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesJson, MediaType.APPLICATION_JSON));

        // First time line retrieval by Id
        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(networkUuid, "l1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals(50., lineAttributesResource.getAttributes().getP1(), 0.001);

        lineAttributesResource.getAttributes().setP1(1000.);

        // Second time line retrieval by Id
        lineAttributesResource = cachedClient.getLine(networkUuid, "l1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals(1000., lineAttributesResource.getAttributes().getP1(), 0.001);

        server.verify();
    }

    @Test
    public void testHvdcLineCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive hvdc line retrievals, only the first should send a REST request, the second uses the cache
        Resource<HvdcLineAttributes> hvdcLine = Resource.hvdcLineBuilder(networkUuid, cachedClient)
                .id("hvdc1")
                .attributes(HvdcLineAttributes.builder()
                        .converterStationId1("c1")
                        .converterStationId2("c2")
                        .name("hvdc1")
                        .maxP(1000)
                        .build())
                .build();

        String hvdcLinesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(hvdcLine)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/hvdc-lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(hvdcLinesJson, MediaType.APPLICATION_JSON));

        // First time hvdc line retrieval by Id
        Resource<HvdcLineAttributes> hvdcLineAttributesResource = cachedClient.getHvdcLine(networkUuid, "hvdc1").orElse(null);
        assertNotNull(hvdcLineAttributesResource);
        assertEquals(1000., hvdcLineAttributesResource.getAttributes().getMaxP(), 0.001);

        hvdcLineAttributesResource.getAttributes().setMaxP(3000.);

        // Second time hvdc line retrieval by Id
        hvdcLineAttributesResource = cachedClient.getHvdcLine(networkUuid, "hvdc1").orElse(null);
        assertNotNull(hvdcLineAttributesResource);
        assertEquals(3000., hvdcLineAttributesResource.getAttributes().getMaxP(), 0.001);

        server.verify();
    }

    @Test
    public void testDanglingLineCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive dangling line retrievals, only the first should send a REST request, the second uses the cache
        Resource<DanglingLineAttributes> danglingLine = Resource.danglingLineBuilder(networkUuid, cachedClient)
                .id("dl1")
                .attributes(DanglingLineAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("dl1")
                        .q0(10)
                        .build())
                .build();

        String danglingLinesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(danglingLine)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/dangling-lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(danglingLinesJson, MediaType.APPLICATION_JSON));

        // First time dangling line retrieval by Id
        Resource<DanglingLineAttributes> danglingLineAttributesResource = cachedClient.getDanglingLine(networkUuid, "dl1").orElse(null);
        assertNotNull(danglingLineAttributesResource);
        assertEquals(10., danglingLineAttributesResource.getAttributes().getQ0(), 0.001);

        danglingLineAttributesResource.getAttributes().setQ0(60);

        // Second time dangling line retrieval by Id
        danglingLineAttributesResource = cachedClient.getDanglingLine(networkUuid, "dl1").orElse(null);
        assertNotNull(danglingLineAttributesResource);
        assertEquals(60., danglingLineAttributesResource.getAttributes().getQ0(), 0.001);

        server.verify();
    }

    @Test
    public void testConfiguredBusCache() throws IOException {
        PreloadingRestNetworkStoreClient cachedClient = new PreloadingRestNetworkStoreClient(restStoreClient);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive configured bus retrievals, only the first should send a REST request, the second uses the cache
        Resource<ConfiguredBusAttributes> configuredBus = Resource.configuredBusBuilder(networkUuid, cachedClient)
                .id("cb1")
                .attributes(ConfiguredBusAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("cb1")
                        .angle(3)
                        .build())
                .build();

        String configuredBusJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(configuredBus)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/configured-buses"))
                .andExpect(method(GET))
                .andRespond(withSuccess(configuredBusJson, MediaType.APPLICATION_JSON));

        // First time configured bus retrieval by Id
        Resource<ConfiguredBusAttributes> configuredBusAttributesResource = cachedClient.getConfiguredBus(networkUuid, "cb1").orElse(null);
        assertNotNull(configuredBusAttributesResource);
        assertEquals(3., configuredBusAttributesResource.getAttributes().getAngle(), 0.001);

        configuredBusAttributesResource.getAttributes().setAngle(5);

        // Second time configured bus retrieval by Id
        configuredBusAttributesResource = cachedClient.getConfiguredBus(networkUuid, "cb1").orElse(null);
        assertNotNull(configuredBusAttributesResource);
        assertEquals(5., configuredBusAttributesResource.getAttributes().getAngle(), 0.001);

        server.verify();
    }
}
