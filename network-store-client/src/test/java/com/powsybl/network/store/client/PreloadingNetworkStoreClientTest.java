/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.model.*;
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
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestClient.class)
@ContextConfiguration(classes = RestClientImpl.class)
public class PreloadingNetworkStoreClientTest {

    @Autowired
    private RestClient restClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private RestNetworkStoreClient restStoreClient;
    private PreloadingNetworkStoreClient cachedClient;
    private UUID networkUuid;

    @Before
    public void setUp() throws IOException {
        restStoreClient = new RestNetworkStoreClient(restClient);
        cachedClient = new PreloadingNetworkStoreClient(new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool())), false, ForkJoinPool.commonPool());
        networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
    }

    @Test
    public void testSubstationCache() throws IOException {
        // Two successive substation retrievals, only the first should send a REST request, the second uses the cache
        Resource<SubstationAttributes> substation = Resource.substationBuilder()
                .id("sub1")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .tso("TSO_FR")
                        .name("SUB1")
                        .build())
                .build();

        String substationJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(substation)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/substations"))
                .andExpect(method(GET))
                .andRespond(withSuccess(substationJson, MediaType.APPLICATION_JSON));

        // First time substation retrieval by Id
        Resource<SubstationAttributes> substationAttributesResource = cachedClient.getSubstation(networkUuid, Resource.INITIAL_VARIANT_NUM, "sub1").orElse(null);
        assertNotNull(substationAttributesResource);
        assertEquals(Boolean.TRUE, substationAttributesResource.getAttributes().getName().equals("SUB1"));  // test substation name

        substationAttributesResource.getAttributes().setName("SUBSTATION1");  // change substation name

        // Second time substation retrieval by Id
        substationAttributesResource = cachedClient.getSubstation(networkUuid, Resource.INITIAL_VARIANT_NUM, "sub1").orElse(null);
        assertNotNull(substationAttributesResource);
        assertEquals(Boolean.TRUE, substationAttributesResource.getAttributes().getName().equals("SUBSTATION1"));  // test substation name

        // Remove component
        assertEquals(1, cachedClient.getSubstations(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeSubstations(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("sub1"));
        assertEquals(0, cachedClient.getSubstations(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        server.verify();
    }

    @Test
    public void testVoltageLevelCache() throws IOException {
        // Two successive voltage level retrievals, only the first should send a REST request, the second uses the cache
        Resource<VoltageLevelAttributes> vl = Resource.voltageLevelBuilder()
                .id("vl1")
                .attributes(VoltageLevelAttributes.builder()
                        .name("VL1")
                        .lowVoltageLimit(100)
                        .highVoltageLimit(200)
                        .build())
                .build();

        String voltageLevelJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(vl)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels"))
                .andExpect(method(GET))
                .andRespond(withSuccess(voltageLevelJson, MediaType.APPLICATION_JSON));

        // First time voltage level retrieval by Id
        Resource<VoltageLevelAttributes> voltageLevelAttributesResource = cachedClient.getVoltageLevel(networkUuid, Resource.INITIAL_VARIANT_NUM, "vl1").orElse(null);
        assertNotNull(voltageLevelAttributesResource);
        assertEquals(Boolean.TRUE, voltageLevelAttributesResource.getAttributes().getName().equals("VL1"));  // test voltage level name

        voltageLevelAttributesResource.getAttributes().setName("VOLTAGE_LEVEL_1");  // change substation name

        // Second time voltage level retrieval by Id
        voltageLevelAttributesResource = cachedClient.getVoltageLevel(networkUuid, Resource.INITIAL_VARIANT_NUM, "vl1").orElse(null);
        assertNotNull(voltageLevelAttributesResource);
        assertEquals(Boolean.TRUE, voltageLevelAttributesResource.getAttributes().getName().equals("VOLTAGE_LEVEL_1"));  // test voltage level name

        // Remove component
        assertEquals(1, cachedClient.getVoltageLevels(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeVoltageLevels(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("vl1"));
        assertEquals(0, cachedClient.getVoltageLevels(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        server.verify();
    }

    @Test
    public void testSwitchCache() throws IOException {
        // Two successive switch retrievals, only the first should send a REST request, the second uses the cache
        Resource<SwitchAttributes> breaker = Resource.switchBuilder()
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

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/switches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(breakersJson, MediaType.APPLICATION_JSON));

        // First time switch retrieval by Id
        Resource<SwitchAttributes> switchAttributesResource = cachedClient.getSwitch(networkUuid, Resource.INITIAL_VARIANT_NUM, "b1").orElse(null);
        assertNotNull(switchAttributesResource);
        assertEquals(Boolean.FALSE, switchAttributesResource.getAttributes().isOpen());  // test switch is closed

        switchAttributesResource.getAttributes().setOpen(true);  // change switch state

        // Second time switch retrieval by Id
        switchAttributesResource = cachedClient.getSwitch(networkUuid, Resource.INITIAL_VARIANT_NUM, "b1").orElse(null);
        assertNotNull(switchAttributesResource);
        assertEquals(Boolean.TRUE, switchAttributesResource.getAttributes().isOpen());  // test switch is open

        // Remove component
        assertEquals(1, cachedClient.getSwitches(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeSwitches(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("b1"));
        assertEquals(0, cachedClient.getSwitches(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        server.verify();
    }

    @Test
    public void testGeneratorCache() throws IOException {
        // Two successive generator retrievals, only the first should send a REST request, the second uses the cache
        Resource<GeneratorAttributes> generator = Resource.generatorBuilder()
                .id("g1")
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("g1")
                        .p(200)
                        .build())
                .build();

        String generatorsJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(generator)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/generators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(generatorsJson, MediaType.APPLICATION_JSON));

        // First time generator retrieval by Id
        Resource<GeneratorAttributes> generatorAttributesResource = cachedClient.getGenerator(networkUuid, Resource.INITIAL_VARIANT_NUM, "g1").orElse(null);
        assertNotNull(generatorAttributesResource);
        assertEquals(200., generatorAttributesResource.getAttributes().getP(), 0.001);

        generatorAttributesResource.getAttributes().setP(300.);

        // Second time generator retrieval by Id
        generatorAttributesResource = cachedClient.getGenerator(networkUuid, Resource.INITIAL_VARIANT_NUM, "g1").orElse(null);
        assertNotNull(generatorAttributesResource);
        assertEquals(300., generatorAttributesResource.getAttributes().getP(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("g1"));
        assertEquals(0, cachedClient.getGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testBatteryCache() throws IOException {
        // Two successive battery retrievals, only the first should send a REST request, the second uses the cache
        Resource<BatteryAttributes> battery = Resource.batteryBuilder()
                .id("b1")
                .attributes(BatteryAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("b1")
                        .p(250)
                        .q(120)
                        .build())
                .build();

        String batteriesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(battery)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/batteries"))
                .andExpect(method(GET))
                .andRespond(withSuccess(batteriesJson, MediaType.APPLICATION_JSON));

        // First time battery retrieval by Id
        Resource<BatteryAttributes> batteryAttributesResource = cachedClient.getBattery(networkUuid, Resource.INITIAL_VARIANT_NUM, "b1").orElse(null);
        assertNotNull(batteryAttributesResource);
        assertEquals(250., batteryAttributesResource.getAttributes().getP(), 0.001);
        assertEquals(120, batteryAttributesResource.getAttributes().getQ(), 0.001);

        batteryAttributesResource.getAttributes().setP(300.);
        batteryAttributesResource.getAttributes().setQ(150.);

        // Second time battery retrieval by Id
        batteryAttributesResource = cachedClient.getBattery(networkUuid, Resource.INITIAL_VARIANT_NUM, "b1").orElse(null);
        assertNotNull(batteryAttributesResource);
        assertEquals(300., batteryAttributesResource.getAttributes().getP(), 0.001);
        assertEquals(150., batteryAttributesResource.getAttributes().getQ(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getBatteries(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeBatteries(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("b1"));
        assertEquals(0, cachedClient.getBatteries(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testLoadCache() throws IOException {
        // Two successive load retrievals, only the first should send a REST request, the second uses the cache
        Resource<LoadAttributes> load = Resource.loadBuilder()
                .id("l1")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("l1")
                        .loadType(LoadType.AUXILIARY)
                        .p0(100)
                        .build())
                .build();

        String loadsJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(load)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/loads"))
                .andExpect(method(GET))
                .andRespond(withSuccess(loadsJson, MediaType.APPLICATION_JSON));

        // First time load retrieval by Id
        Resource<LoadAttributes> loadAttributesResource = cachedClient.getLoad(networkUuid, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null);
        assertNotNull(loadAttributesResource);
        assertEquals(LoadType.AUXILIARY, loadAttributesResource.getAttributes().getLoadType());
        assertEquals(100., loadAttributesResource.getAttributes().getP0(), 0.001);

        loadAttributesResource.getAttributes().setLoadType(LoadType.FICTITIOUS);
        loadAttributesResource.getAttributes().setP0(2000.);

        // Second time load retrieval by Id
        loadAttributesResource = cachedClient.getLoad(networkUuid, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null);
        assertNotNull(loadAttributesResource);
        assertEquals(LoadType.FICTITIOUS, loadAttributesResource.getAttributes().getLoadType());
        assertEquals(2000., loadAttributesResource.getAttributes().getP0(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getLoads(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeLoads(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("l1"));
        assertEquals(0, cachedClient.getLoads(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testShuntCompensatorCache() throws IOException {
        // Two successive shunt compensator retrievals, only the first should send a REST request, the second uses the cache
        Resource<ShuntCompensatorAttributes> shuntCompensator = Resource.shuntCompensatorBuilder()
                .id("sc1")
                .attributes(ShuntCompensatorAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("sc1")
                        .sectionCount(5)
                        .build())
                .build();

        String shuntCompenstorJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(shuntCompensator)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/shunt-compensators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(shuntCompenstorJson, MediaType.APPLICATION_JSON));

        // First time shunt compensator retrieval by Id
        Resource<ShuntCompensatorAttributes> shuntCompensatorAttributesResource = cachedClient.getShuntCompensator(networkUuid, Resource.INITIAL_VARIANT_NUM, "sc1").orElse(null);
        assertNotNull(shuntCompensatorAttributesResource);
        assertEquals(5, shuntCompensatorAttributesResource.getAttributes().getSectionCount());

        shuntCompensatorAttributesResource.getAttributes().setSectionCount(8);

        // Second time shunt compensator retrieval by Id
        shuntCompensatorAttributesResource = cachedClient.getShuntCompensator(networkUuid, Resource.INITIAL_VARIANT_NUM, "sc1").orElse(null);
        assertNotNull(shuntCompensatorAttributesResource);
        assertEquals(8, shuntCompensatorAttributesResource.getAttributes().getSectionCount());

        // Remove component
        assertEquals(1, cachedClient.getShuntCompensators(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeShuntCompensators(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("sc1"));
        assertEquals(0, cachedClient.getShuntCompensators(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testStaticVarCompensatorCache() throws IOException {
        // Two successive static var compensator retrievals, only the first should send a REST request, the second uses the cache
        Resource<StaticVarCompensatorAttributes> staticVarCompensator = Resource.staticVarCompensatorBuilder()
                .id("svc1")
                .attributes(StaticVarCompensatorAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("svc1")
                        .bmax(20)
                        .reactivePowerSetPoint(100)
                        .build())
                .build();

        String staticVarCompenstorJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(staticVarCompensator)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/static-var-compensators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(staticVarCompenstorJson, MediaType.APPLICATION_JSON));

        // First time static var compensator retrieval by Id
        Resource<StaticVarCompensatorAttributes> staticVarCompensatorAttributesResource = cachedClient.getStaticVarCompensator(networkUuid, Resource.INITIAL_VARIANT_NUM, "svc1").orElse(null);
        assertNotNull(staticVarCompensatorAttributesResource);
        assertEquals(20, staticVarCompensatorAttributesResource.getAttributes().getBmax(), 0.001);
        assertEquals(100, staticVarCompensatorAttributesResource.getAttributes().getReactivePowerSetPoint(), 0.001);

        staticVarCompensatorAttributesResource.getAttributes().setBmax(50.);
        staticVarCompensatorAttributesResource.getAttributes().setReactivePowerSetPoint(1500.);

        // Second time static var compensator retrieval by Id
        staticVarCompensatorAttributesResource = cachedClient.getStaticVarCompensator(networkUuid, Resource.INITIAL_VARIANT_NUM, "svc1").orElse(null);
        assertNotNull(staticVarCompensatorAttributesResource);
        assertEquals(50., staticVarCompensatorAttributesResource.getAttributes().getBmax(), 0.001);
        assertEquals(1500., staticVarCompensatorAttributesResource.getAttributes().getReactivePowerSetPoint(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getStaticVarCompensators(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeStaticVarCompensators(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("svc1"));
        assertEquals(0, cachedClient.getStaticVarCompensators(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testVscConverterStationCache() throws IOException {
        // Two successive vsc converter station retrievals, only the first should send a REST request, the second uses the cache
        Resource<VscConverterStationAttributes> vscConverterStation = Resource.vscConverterStationBuilder()
                .id("vsc1")
                .attributes(VscConverterStationAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("vsc1")
                        .lossFactor(0.6F)
                        .build())
                .build();

        String vscConverterStationJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(vscConverterStation)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/vsc-converter-stations"))
                .andExpect(method(GET))
                .andRespond(withSuccess(vscConverterStationJson, MediaType.APPLICATION_JSON));

        // First time vsc converter station retrieval by Id
        Resource<VscConverterStationAttributes> vscConverterStationAttributesResource = cachedClient.getVscConverterStation(networkUuid, Resource.INITIAL_VARIANT_NUM, "vsc1").orElse(null);
        assertNotNull(vscConverterStationAttributesResource);
        assertEquals(0.6F, vscConverterStationAttributesResource.getAttributes().getLossFactor(), 0.001);

        vscConverterStationAttributesResource.getAttributes().setLossFactor(0.8F);

        // Second time vsc converter station retrieval by Id
        vscConverterStationAttributesResource = cachedClient.getVscConverterStation(networkUuid, Resource.INITIAL_VARIANT_NUM, "vsc1").orElse(null);
        assertNotNull(vscConverterStationAttributesResource);
        assertEquals(0.8, vscConverterStationAttributesResource.getAttributes().getLossFactor(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getVscConverterStations(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeVscConverterStations(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("vsc1"));
        assertEquals(0, cachedClient.getVscConverterStations(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testLccConverterStationCache() throws IOException {
        // Two successive lcc converter station retrievals, only the first should send a REST request, the second uses the cache
        Resource<LccConverterStationAttributes> lccConverterStation = Resource.lccConverterStationBuilder()
                .id("lcc1")
                .attributes(LccConverterStationAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("lcc1")
                        .powerFactor(250)
                        .build())
                .build();

        String lccConverterStationJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(lccConverterStation)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lcc-converter-stations"))
                .andExpect(method(GET))
                .andRespond(withSuccess(lccConverterStationJson, MediaType.APPLICATION_JSON));

        // First time lcc converter station retrieval by Id
        Resource<LccConverterStationAttributes> lccConverterStationAttributesResource = cachedClient.getLccConverterStation(networkUuid, Resource.INITIAL_VARIANT_NUM, "lcc1").orElse(null);
        assertNotNull(lccConverterStationAttributesResource);
        assertEquals(250, lccConverterStationAttributesResource.getAttributes().getPowerFactor(), 0.001);

        lccConverterStationAttributesResource.getAttributes().setPowerFactor(400);

        // Second time lcc converter station retrieval by Id
        lccConverterStationAttributesResource = cachedClient.getLccConverterStation(networkUuid, Resource.INITIAL_VARIANT_NUM, "lcc1").orElse(null);
        assertNotNull(lccConverterStationAttributesResource);
        assertEquals(400, lccConverterStationAttributesResource.getAttributes().getPowerFactor(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getLccConverterStations(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeLccConverterStations(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("lcc1"));
        assertEquals(0, cachedClient.getLccConverterStations(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testGroundCache() throws IOException {
        // Two successive ground retrievals, only the first should send a REST request, the second uses the cache
        Resource<GroundAttributes> ground = Resource.groundBuilder()
                .id("groundId")
                .attributes(GroundAttributes.builder()
                        .voltageLevelId("vl2")
                        .p(1)
                        .q(2)
                        .bus("bus")
                        .build())
                .build();

        String groundJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(ground)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/grounds"))
                .andExpect(method(GET))
                .andRespond(withSuccess(groundJson, MediaType.APPLICATION_JSON));

        // First time ground retrieval by Id
        Resource<GroundAttributes> groundAttributesResource = cachedClient.getGround(networkUuid, Resource.INITIAL_VARIANT_NUM, "groundId").orElse(null);
        assertNotNull(groundAttributesResource);
        assertEquals("bus", groundAttributesResource.getAttributes().getBus());
        assertEquals(1, groundAttributesResource.getAttributes().getP(), 0.001);
        assertEquals(2, groundAttributesResource.getAttributes().getQ(), 0.001);
        assertEquals("vl2", groundAttributesResource.getAttributes().getVoltageLevelId());

        groundAttributesResource.getAttributes().setP(3);
        groundAttributesResource.getAttributes().setQ(4);

        // Second time ground retrieval by Id
        groundAttributesResource = cachedClient.getGround(networkUuid, Resource.INITIAL_VARIANT_NUM, "groundId").orElse(null);
        assertNotNull(groundAttributesResource);
        assertEquals(3, groundAttributesResource.getAttributes().getP(), 0.001);
        assertEquals(4, groundAttributesResource.getAttributes().getQ(), 0.001);

        // ground retrieval by voltage level
        List<Resource<GroundAttributes>> vl2Grounds = cachedClient.getVoltageLevelGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM, "vl2");
        assertEquals(1, vl2Grounds.size());
        assertEquals(3, vl2Grounds.get(0).getAttributes().getP(), 0.001);
        assertEquals(4, vl2Grounds.get(0).getAttributes().getQ(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("groundId"));
        assertEquals(0, cachedClient.getGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        // recreate ground
        cachedClient.createGrounds(networkUuid, List.of(ground));
        assertEquals(1, cachedClient.getGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        vl2Grounds = cachedClient.getVoltageLevelGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM, "vl2");
        assertEquals(1, vl2Grounds.size());
        assertEquals(1, vl2Grounds.get(0).getAttributes().getP(), 0.001);
        assertEquals(2, vl2Grounds.get(0).getAttributes().getQ(), 0.001);

        // Update grounds with new values in the resource
        vl2Grounds.get(0).getAttributes().setP(5);
        vl2Grounds.get(0).getAttributes().setQ(6);
        cachedClient.updateGrounds(networkUuid, vl2Grounds, null);
        // Retrieve grounds by container and ensure that vl2Grounds is up to date
        vl2Grounds = cachedClient.getVoltageLevelGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM, "vl2");
        assertEquals(1, vl2Grounds.size());
        assertEquals(5, vl2Grounds.get(0).getAttributes().getP(), 0.001);
        assertEquals(6, vl2Grounds.get(0).getAttributes().getQ(), 0.001);

        server.verify();
    }

    @Test
    public void testTwoWindingsTransformerCache() throws IOException {
        // Two successive two windings transformer retrievals, only the first should send a REST request, the second uses the cache
        Resource<TwoWindingsTransformerAttributes> twoWindingsTransformer = Resource.twoWindingsTransformerBuilder()
                .id("tw1")
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .r(2)
                        .x(3)
                        .build())
                .build();

        String twoWindingsTransformerJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(twoWindingsTransformer)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/2-windings-transformers"))
                .andExpect(method(GET))
                .andRespond(withSuccess(twoWindingsTransformerJson, MediaType.APPLICATION_JSON));

        // First time two windings transformer retrieval by Id
        Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerAttributesResource = cachedClient.getTwoWindingsTransformer(networkUuid, Resource.INITIAL_VARIANT_NUM, "tw1").orElse(null);
        assertNotNull(twoWindingsTransformerAttributesResource);
        assertEquals(2, twoWindingsTransformerAttributesResource.getAttributes().getR(), 0.001);
        assertEquals(3, twoWindingsTransformerAttributesResource.getAttributes().getX(), 0.001);

        twoWindingsTransformerAttributesResource.getAttributes().setR(5);
        twoWindingsTransformerAttributesResource.getAttributes().setX(9);

        // Second time two windings transformer retrieval by Id
        twoWindingsTransformerAttributesResource = cachedClient.getTwoWindingsTransformer(networkUuid, Resource.INITIAL_VARIANT_NUM, "tw1").orElse(null);
        assertNotNull(twoWindingsTransformerAttributesResource);
        assertEquals(5, twoWindingsTransformerAttributesResource.getAttributes().getR(), 0.001);
        assertEquals(9, twoWindingsTransformerAttributesResource.getAttributes().getX(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getTwoWindingsTransformers(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeTwoWindingsTransformers(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("tw1"));
        assertEquals(0, cachedClient.getTwoWindingsTransformers(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testThreeWindingsTransformerCache() throws IOException {
        // Two successive three windings transformer retrievals, only the first should send a REST request, the second uses the cache
        Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformer = Resource.threeWindingsTransformerBuilder()
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

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/3-windings-transformers"))
                .andExpect(method(GET))
                .andRespond(withSuccess(threeWindingsTransformerJson, MediaType.APPLICATION_JSON));

        // First time three windings transformer retrieval by Id
        Resource<ThreeWindingsTransformerAttributes> threeWindingsTransformerAttributesResource = cachedClient.getThreeWindingsTransformer(networkUuid, Resource.INITIAL_VARIANT_NUM, "tw1").orElse(null);
        assertNotNull(threeWindingsTransformerAttributesResource);
        assertEquals(50, threeWindingsTransformerAttributesResource.getAttributes().getP2(), 0.001);
        assertEquals(60, threeWindingsTransformerAttributesResource.getAttributes().getQ3(), 0.001);

        threeWindingsTransformerAttributesResource.getAttributes().setP2(200);
        threeWindingsTransformerAttributesResource.getAttributes().setQ3(550);

        // Second time three windings transformer retrieval by Id
        threeWindingsTransformerAttributesResource = cachedClient.getThreeWindingsTransformer(networkUuid, Resource.INITIAL_VARIANT_NUM, "tw1").orElse(null);
        assertNotNull(threeWindingsTransformerAttributesResource);
        assertEquals(200, threeWindingsTransformerAttributesResource.getAttributes().getP2(), 0.001);
        assertEquals(550, threeWindingsTransformerAttributesResource.getAttributes().getQ3(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getThreeWindingsTransformers(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeThreeWindingsTransformers(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("tw1"));
        assertEquals(0, cachedClient.getThreeWindingsTransformers(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testLineCache() throws IOException {
        // Two successive line retrievals, only the first should send a REST request, the second uses the cache
        Resource<LineAttributes> line = Resource.lineBuilder()
                .id("l1")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("l1")
                        .p1(50)
                        .build())
                .build();

        String linesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(line)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesJson, MediaType.APPLICATION_JSON));

        // First time line retrieval by Id
        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals(50., lineAttributesResource.getAttributes().getP1(), 0.001);

        lineAttributesResource.getAttributes().setP1(1000.);

        // Second time line retrieval by Id
        lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals(1000., lineAttributesResource.getAttributes().getP1(), 0.001);

        // Remove component
        assertEquals(1, cachedClient.getLines(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeLines(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("l1"));
        assertEquals(0, cachedClient.getLines(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testHvdcLineCache() throws IOException {
        // Two successive hvdc line retrievals, only the first should send a REST request, the second uses the cache
        Resource<HvdcLineAttributes> hvdcLine = Resource.hvdcLineBuilder()
                .id("hvdc1")
                .attributes(HvdcLineAttributes.builder()
                        .converterStationId1("c1")
                        .converterStationId2("c2")
                        .name("hvdc1")
                        .maxP(1000)
                        .build())
                .build();

        String hvdcLinesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(hvdcLine)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/hvdc-lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(hvdcLinesJson, MediaType.APPLICATION_JSON));

        // First time hvdc line retrieval by Id
        Resource<HvdcLineAttributes> hvdcLineAttributesResource = cachedClient.getHvdcLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "hvdc1").orElse(null);
        assertNotNull(hvdcLineAttributesResource);
        assertEquals(1000., hvdcLineAttributesResource.getAttributes().getMaxP(), 0.001);

        hvdcLineAttributesResource.getAttributes().setMaxP(3000.);

        // Second time hvdc line retrieval by Id
        hvdcLineAttributesResource = cachedClient.getHvdcLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "hvdc1").orElse(null);
        assertNotNull(hvdcLineAttributesResource);
        assertEquals(3000., hvdcLineAttributesResource.getAttributes().getMaxP(), 0.001);
        assertEquals(1, cachedClient.getHvdcLines(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeHvdcLines(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("hvdc1"));
        assertEquals(0, cachedClient.getHvdcLines(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        server.verify();
    }

    @Test
    public void testDanglingLineCache() throws IOException {
        // Two successive dangling line retrievals, only the first should send a REST request, the second uses the cache
        Resource<DanglingLineAttributes> danglingLine = Resource.danglingLineBuilder()
                .id("dl1")
                .attributes(DanglingLineAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("dl1")
                        .q0(10)
                        .build())
                .build();

        String danglingLinesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(danglingLine)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/dangling-lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(danglingLinesJson, MediaType.APPLICATION_JSON));

        // First time dangling line retrieval by Id
        Resource<DanglingLineAttributes> danglingLineAttributesResource = cachedClient.getDanglingLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "dl1").orElse(null);
        assertNotNull(danglingLineAttributesResource);
        assertEquals(10., danglingLineAttributesResource.getAttributes().getQ0(), 0.001);

        danglingLineAttributesResource.getAttributes().setQ0(60);

        // Second time dangling line retrieval by Id
        danglingLineAttributesResource = cachedClient.getDanglingLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "dl1").orElse(null);
        assertNotNull(danglingLineAttributesResource);
        assertEquals(60., danglingLineAttributesResource.getAttributes().getQ0(), 0.001);

        server.verify();
    }

    @Test
    public void testTieLineCache() throws IOException {
        // Two successive tie line retrievals, only the first should send a REST request, the second uses the cache
        Resource<TieLineAttributes> tieLine = Resource.tieLineBuilder()
                .id("tieLine1")
                .attributes(TieLineAttributes.builder()
                        .name("tieLine1")
                        .danglingLine1Id("dl1")
                        .danglingLine2Id("dl2")
                        .build())
                .build();

        String tieLineJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(tieLine)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/tie-lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(tieLineJson, MediaType.APPLICATION_JSON));

        // First time tie line retrieval by Id
        Resource<TieLineAttributes> tieLineAttributesResource = cachedClient.getTieLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "tieLine1").orElse(null);
        assertNotNull(tieLineAttributesResource);
        assertEquals("dl1", tieLineAttributesResource.getAttributes().getDanglingLine1Id());

        tieLineAttributesResource.getAttributes().setDanglingLine1Id("dll1");

        // Second time tie line retrieval by Id
        tieLineAttributesResource = cachedClient.getTieLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "tieLine1").orElse(null);
        assertNotNull(tieLineAttributesResource);
        assertEquals("dll1", tieLineAttributesResource.getAttributes().getDanglingLine1Id());

        // Remove component
        assertEquals(1, cachedClient.getTieLines(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeTieLines(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("tieLine1"));
        assertEquals(0, cachedClient.getTieLines(networkUuid, Resource.INITIAL_VARIANT_NUM).size());

        server.verify();
    }

    @Test
    public void testConfiguredBusCache() throws IOException {
        // Two successive configured bus retrievals, only the first should send a REST request, the second uses the cache
        Resource<ConfiguredBusAttributes> configuredBus = Resource.configuredBusBuilder()
                .id("cb1")
                .attributes(ConfiguredBusAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("cb1")
                        .angle(3)
                        .build())
                .build();

        String configuredBusJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(configuredBus)));

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/configured-buses"))
                .andExpect(method(GET))
                .andRespond(withSuccess(configuredBusJson, MediaType.APPLICATION_JSON));

        // First time configured bus retrieval by Id
        Resource<ConfiguredBusAttributes> configuredBusAttributesResource = cachedClient.getConfiguredBus(networkUuid, Resource.INITIAL_VARIANT_NUM, "cb1").orElse(null);
        assertNotNull(configuredBusAttributesResource);
        assertEquals(3., configuredBusAttributesResource.getAttributes().getAngle(), 0.001);

        configuredBusAttributesResource.getAttributes().setAngle(5);

        // Second time configured bus retrieval by Id
        configuredBusAttributesResource = cachedClient.getConfiguredBus(networkUuid, Resource.INITIAL_VARIANT_NUM, "cb1").orElse(null);
        assertNotNull(configuredBusAttributesResource);
        assertEquals(5., configuredBusAttributesResource.getAttributes().getAngle(), 0.001);

        assertEquals(1, cachedClient.getConfiguredBuses(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        cachedClient.removeConfiguredBuses(networkUuid, Resource.INITIAL_VARIANT_NUM, Collections.singletonList("cb1"));
        assertEquals(0, cachedClient.getConfiguredBuses(networkUuid, Resource.INITIAL_VARIANT_NUM).size());
        server.verify();
    }

    @Test
    public void testGetExtensionCache() throws IOException {
        String identifiableId1 = "GEN";
        String identifiableId2 = "GEN1";

        // Load the identifiables in the cache
        loadTwoIdentifiablesToCache(identifiableId1, identifiableId2);

        // Two successive ExtensionAttributes retrieval, only the first should send a REST request, the second uses the cache
        ActivePowerControlAttributes apc1 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .build();
        ActivePowerControlAttributes apc2 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(1)
                .build();

        String extensionAttributes = objectMapper.writerFor(new TypeReference<Map<String, ExtensionAttributes>>() {
        }).writeValueAsString(Map.of(identifiableId1, apc1, identifiableId2, apc2));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions/activepowercontrol"))
                .andExpect(method(GET))
                .andRespond(withSuccess(extensionAttributes, MediaType.APPLICATION_JSON));

        Optional<ExtensionAttributes> apc1Attributes = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1, "activepowercontrol");
        assertTrue(apc1Attributes.isPresent());
        ActivePowerControlAttributes activePowerControlAttributes = (ActivePowerControlAttributes) apc1Attributes.get();
        assertEquals(0.5, activePowerControlAttributes.getParticipationFactor(), 0);

        apc1Attributes = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1, "activepowercontrol");
        assertTrue(apc1Attributes.isPresent());
        activePowerControlAttributes = (ActivePowerControlAttributes) apc1Attributes.get();
        assertEquals(0.5, activePowerControlAttributes.getParticipationFactor(), 0);

        server.verify();
        server.reset();
    }

    @Test
    public void testGetExtensionEmptyExtensionAttributesCache() throws IOException {
        // Two successive ExtensionAttributes retrieval, only the first should send a REST request, the second uses the cache
        String identifiableId1 = "GEN";
        String extensionAttributes = objectMapper.writerFor(new TypeReference<Map<String, ExtensionAttributes>>() {
        }).writeValueAsString(Map.of());
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions/activepowercontrol"))
                .andExpect(method(GET))
                .andRespond(withSuccess(extensionAttributes, MediaType.APPLICATION_JSON));

        Optional<ExtensionAttributes> apc1Attributes = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1, "activepowercontrol");
        assertFalse(apc1Attributes.isPresent());

        apc1Attributes = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1, "activepowercontrol");
        assertFalse(apc1Attributes.isPresent());

        server.verify();
        server.reset();
    }

    @Test
    public void testGetExtensionsCache() throws IOException {
        String identifiableId1 = "GEN";
        String identifiableId2 = "GEN1";

        // Load the identifiables in the cache
        loadTwoIdentifiablesToCache(identifiableId1, identifiableId2);

        // Two successive ExtensionAttributes retrieval, only the first should send a REST request, the second uses the cache
        ActivePowerControlAttributes apc1 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .build();
        GeneratorStartupAttributes gs1 = GeneratorStartupAttributes.builder()
                .marginalCost(6.8)
                .forcedOutageRate(35)
                .plannedOutageRate(30)
                .startupCost(28)
                .plannedActivePowerSetpoint(5)
                .build();
        ActivePowerControlAttributes apc2 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(1)
                .build();

        String multipleExtensionAttributes = objectMapper.writerFor(new TypeReference<Map<String, Map<String, ExtensionAttributes>>>() {
        }).writeValueAsString(Map.of(identifiableId1, Map.of(ActivePowerControl.NAME, apc1, GeneratorStartup.NAME, gs1), identifiableId2, Map.of(ActivePowerControl.NAME, apc2)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(GET))
                .andRespond(withSuccess(multipleExtensionAttributes, MediaType.APPLICATION_JSON));

        Map<String, ExtensionAttributes> extensionAttributesMap = cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1);
        assertEquals(2, extensionAttributesMap.size());

        extensionAttributesMap = cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1);
        assertEquals(2, extensionAttributesMap.size());

        // Check that there is no new fetch when getting a single extension once all the extensions have been loaded in the identifiable
        cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1, "activepowercontrol");

        server.verify();
        server.reset();
    }

    @Test
    public void testGetExtensionsEmptyExtensionAttributesCache() throws IOException {
        String identifiableId1 = "GEN";
        String identifiableId2 = "GEN1";

        loadTwoIdentifiablesToCache(identifiableId1, identifiableId2);

        // Two successive ExtensionAttributes retrieval, only the first should send a REST request, the second uses the cache
        String multipleExtensionAttributes = objectMapper.writerFor(new TypeReference<Map<String, Map<String, ExtensionAttributes>>>() {
        }).writeValueAsString(Map.of());
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(GET))
                .andRespond(withSuccess(multipleExtensionAttributes, MediaType.APPLICATION_JSON));

        Map<String, ExtensionAttributes> extensionAttributesMap = cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1);
        assertEquals(0, extensionAttributesMap.size());

        extensionAttributesMap = cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId1);
        assertEquals(0, extensionAttributesMap.size());

        server.verify();
        server.reset();
    }

    private void loadTwoIdentifiablesToCache(String identifiableId1, String identifiableId2) throws JsonProcessingException {
        Resource<GeneratorAttributes> g1Resource = Resource.generatorBuilder()
                .id(identifiableId1)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("VL_1")
                        .build())
                .build();
        Resource<GeneratorAttributes> g2Resource = Resource.generatorBuilder()
                .id(identifiableId2)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("VL_1")
                        .build())
                .build();
        String generatorJson = objectMapper.writeValueAsString(TopLevelDocument.of(List.of(g1Resource, g2Resource)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/generators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(generatorJson, MediaType.APPLICATION_JSON));
        cachedClient.getGenerator(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableId1);
        server.verify();
        server.reset();
    }

    @Test
    public void testGetExtensionsCacheWithClonedNetwork() throws IOException {
        int targetVariantNum = 1;
        String targetVariantId = "new_variant";
        String identifiableId1 = "GEN";
        String identifiableId2 = "GEN1";

        // Load the identifiables in the cache
        loadTwoIdentifiablesToCache(identifiableId1, identifiableId2);

        // Two successive ExtensionAttributes retrieval, only the first should send a REST request, the second uses the cache
        ActivePowerControlAttributes apc1 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .build();
        GeneratorStartupAttributes gs1 = GeneratorStartupAttributes.builder()
                .marginalCost(6.8)
                .forcedOutageRate(35)
                .plannedOutageRate(30)
                .startupCost(28)
                .plannedActivePowerSetpoint(5)
                .build();
        ActivePowerControlAttributes apc2 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(1)
                .build();

        // Load extensions to cache on initial variant
        String multipleExtensionAttributes = objectMapper.writerFor(new TypeReference<Map<String, Map<String, ExtensionAttributes>>>() {
        }).writeValueAsString(Map.of(identifiableId1, Map.of(ActivePowerControl.NAME, apc1, GeneratorStartup.NAME, gs1), identifiableId2, Map.of(ActivePowerControl.NAME, apc2)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(GET))
                .andRespond(withSuccess(multipleExtensionAttributes, MediaType.APPLICATION_JSON));
        cachedClient.getAllExtensionsAttributesByResourceType(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR);
        server.verify();
        server.reset();

        // Clone network
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/to/" + targetVariantNum + "?targetVariantId=" + targetVariantId))
                .andExpect(method(PUT))
                .andRespond(withSuccess());
        cachedClient.cloneNetwork(networkUuid, Resource.INITIAL_VARIANT_NUM, targetVariantNum, targetVariantId);

        // Verify that the cache is copied and there is no new fetch
        cachedClient.getAllExtensionsAttributesByResourceType(networkUuid, targetVariantNum, ResourceType.GENERATOR);
        Map<String, ExtensionAttributes> extensionAttributesByExtensionNameMap = cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, targetVariantNum, ResourceType.GENERATOR, identifiableId1);
        assertEquals(2, extensionAttributesByExtensionNameMap.size());
        server.verify();
        server.reset();
    }
}
