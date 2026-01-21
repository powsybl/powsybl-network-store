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
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest
public class CachedNetworkStoreClientTest {

    // Necessary with empty @RestClientTest for this
    // lib which doesn't have a @SpringBootApplication in
    // its main sources.
    @SpringBootConfiguration
    public static class EmptyConfig {

    }

    // Don't use the component scanned RestClient in this test
    // to avoid the /v1 prefix because the tests were written
    // without it (could be considered more legible... but not
    // terribly important. Feel free to change if needed)
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RestClient testClient(RestTemplateBuilder restTemplateBuilder) {
            return new RestClientImpl(restTemplateBuilder);
        }
    }

    @Autowired
    private RestClient restClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private RestNetworkStoreClient restStoreClient;

    @Before
    public void setUp() throws IOException {
        restStoreClient = new RestNetworkStoreClient(restClient);
    }

    @Test
    public void testSingleLineCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive line retrievals, only the first should send a REST request, the second uses the cache
        Resource<LineAttributes> l1 = Resource.lineBuilder()
                .id("LINE_1")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_2")
                        .name("LINE_1")
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

        Resource<LineAttributes> l2 = Resource.lineBuilder()
                .id("LINE_2")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_3")
                        .build())
                .build();

        String line1Json = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(l1)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines/LINE_1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(line1Json, MediaType.APPLICATION_JSON));

        // First time line retrieval by Id
        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);

        // Second time line retrieval by Id
        lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);

        server.verify();

        server.reset();

        // First, we retrieve all lines of the network, the second time we retrieve only one line. For this second retrieval, no REST request is sent (cache is used)

        //cachedClient.deleteNetwork(networkUuid);

        // We expect all lines retrieval REST request to be executed just once
        String linesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(l1, l2)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesJson, MediaType.APPLICATION_JSON));

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines/LINE_1"));

        // First time retrieval of all lines of the network
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getLines(networkUuid, Resource.INITIAL_VARIANT_NUM);
        assertNotNull(lineAttributesResources);
        assertEquals(2, lineAttributesResources.size());

        // Second time retrieval of all lines of the network
        lineAttributesResources = cachedClient.getLines(networkUuid, Resource.INITIAL_VARIANT_NUM);
        assertNotNull(lineAttributesResources);
        assertEquals(2, lineAttributesResources.size());

        // Retrieval of a single line of the network
        lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

        server.reset();

        lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);

        assertEquals(0., lineAttributesResource.getAttributes().getP1(), 0.);  // test P1 value

        lineAttributesResource.getAttributes().setP1(100.);  // set P1 value

        lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals(100., lineAttributesResource.getAttributes().getP1(), 0.);  // test P1 value

        server.verify();
    }

    @Test
    public void testVoltageLevelLineCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive lines retrievals by voltage level, only the first should send a REST request, the second uses the cache

        Resource<LineAttributes> l1 = Resource.lineBuilder()
                .id("LINE_1")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_2")
                        .build())
                .build();

        Resource<LineAttributes> l2 = Resource.lineBuilder()
                .id("LINE_2")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_3")
                        .build())
                .build();

        String linesV1Json = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(l1, l2)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/VL_1/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesV1Json, MediaType.APPLICATION_JSON));

        // First time lines retrieval by voltage level
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_1");
        assertEquals(2, lineAttributesResources.size());

        // Second time lines retrieval by voltage level
        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_1");
        assertEquals(2, lineAttributesResources.size());

        server.verify();

        server.reset();

        // Single line retrieval by Id

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines/LINE_1"));

        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

        server.reset();

        // Getting all lines of the network

        // We expect all lines retrieval REST request will be executed only once
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesV1Json, MediaType.APPLICATION_JSON));

        // First time all network lines retrieval
        List<Resource<LineAttributes>> allLineAttributesResources = cachedClient.getLines(networkUuid, Resource.INITIAL_VARIANT_NUM);
        assertNotNull(allLineAttributesResources);
        assertEquals(2, allLineAttributesResources.size());

        // Second time all network lines retrieval
        allLineAttributesResources = cachedClient.getLines(networkUuid, Resource.INITIAL_VARIANT_NUM);
        assertNotNull(allLineAttributesResources);
        assertEquals(2, allLineAttributesResources.size());

        server.verify();
    }

    @Test
    public void testAllLinesCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive lines retrievals by voltage level, only the first should send a REST request, the second uses the cache

        Resource<LineAttributes> l1 = Resource.lineBuilder()
                .id("LINE_1")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_2")
                        .build())
                .build();

        Resource<LineAttributes> l2 = Resource.lineBuilder()
                .id("LINE_2")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_3")
                        .build())
                .build();

        Resource<LineAttributes> l3 = Resource.lineBuilder()
                .id("LINE_3")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_4")
                        .build())
                .build();

        String alllinesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(l1, l2, l3)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(alllinesJson, MediaType.APPLICATION_JSON));

        // First time all lines retrieval
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getLines(networkUuid, Resource.INITIAL_VARIANT_NUM);
        assertEquals(3, lineAttributesResources.size());

        // Second time lines retrieval by voltage level
        lineAttributesResources = cachedClient.getLines(networkUuid, Resource.INITIAL_VARIANT_NUM);
        assertEquals(3, lineAttributesResources.size());

        server.verify();

        server.reset();

        // Single line retrieval by Id

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines/LINE_1"));

        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

        server.reset();

        // Getting all lines of a specified voltage level

        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/VL_1/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/VL_2/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/VL_3/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/VL_4/lines"));

        // Lines retrieval by voltage level (should use cache)
        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_1");
        assertEquals(3, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_2");
        assertEquals(1, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_3");
        assertEquals(1, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_4");
        assertEquals(1, lineAttributesResources.size());

        server.verify();
    }

    @Test
    public void testAllGroundsCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive Ground retrievals by voltage level, only the first should send a REST request, the second uses the cache

        Resource<GroundAttributes> g1 = Resource.groundBuilder()
                .id("groundId1")
                .attributes(GroundAttributes.builder()
                        .voltageLevelId("VL_1")
                        .p(1)
                        .q(2)
                        .build())
                .build();

        Resource<GroundAttributes> g2 = Resource.groundBuilder()
                .id("groundId2")
                .attributes(GroundAttributes.builder()
                        .voltageLevelId("VL_1")
                        .p(3)
                        .q(4)
                        .build())
                .build();

        Resource<GroundAttributes> g3 = Resource.groundBuilder()
                .id("groundId3")
                .attributes(GroundAttributes.builder()
                        .voltageLevelId("VL_1")
                        .p(5)
                        .q(6)
                        .build())
                .build();

        String allGroundsJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(g1, g2, g3)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/grounds"))
                .andExpect(method(GET))
                .andRespond(withSuccess(allGroundsJson, MediaType.APPLICATION_JSON));

        // First time all grounds retrieval
        List<Resource<GroundAttributes>> groundAttributesResources = cachedClient.getGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM);
        assertEquals(3, groundAttributesResources.size());

        // Second time grounds retrieval by voltage level
        groundAttributesResources = cachedClient.getGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM);
        assertEquals(3, groundAttributesResources.size());

        server.verify();

        server.reset();

        // Single ground retrieval by Id

        // We expect single ground retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/grounds/groundId1"));

        Resource<GroundAttributes> groundAttributesResource = cachedClient.getGround(networkUuid, Resource.INITIAL_VARIANT_NUM, "groundId1").orElse(null);
        assertNotNull(groundAttributesResource);
        assertEquals("groundId1", groundAttributesResource.getId());

        server.verify();

        server.reset();

        // Getting all grounds of a specified voltage level

        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/VL_1/grounds"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/VL_2/grounds"));

        // ground retrieval by voltage level (should use cache)
        groundAttributesResources = cachedClient.getVoltageLevelGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_1");
        assertEquals(3, groundAttributesResources.size());

        groundAttributesResources = cachedClient.getVoltageLevelGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_2");
        assertEquals(0, groundAttributesResources.size());

        server.verify();
    }

    @Test
    public void testSwitchCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

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

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/switches/b1"))
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

        server.verify();
        server.reset();

        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/switches"))
                .andExpect(method(PUT))
                .andRespond(withSuccess());

        cachedClient.flush(networkUuid);
    }

    @Test
    public void testGetIdentifiable() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        int variantNum = 0;

        for (int i = 0; i <= 11; i++) {
            String gId = "g" + i;

            Resource<GeneratorAttributes> g1Resource = Resource.generatorBuilder()
                    .id(gId)
                    .attributes(GeneratorAttributes.builder()
                            .voltageLevelId("VL_1")
                            .build())
                    .build();

            if (i <= 10) {
                String g1Json = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(g1Resource)));
                server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + gId))
                        .andExpect(method(GET))
                        .andRespond(withSuccess(g1Json, MediaType.APPLICATION_JSON));
            } else {
                String json = objectMapper.writeValueAsString(IntStream.range(0, 11).mapToObj(value -> "g" + value).collect(Collectors.toList()));
                server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables-ids"))
                        .andExpect(method(GET))
                        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
            }

            cachedClient.getIdentifiable(networkUuid, variantNum, gId);

            server.verify();
            server.reset();
        }
    }

    @Test
    public void testGetExtensionCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "GEN";

        // Load the identifiable in the cache
        loadGeneratorToCache(identifiableId, networkUuid, cachedClient);

        // Two successive ExtensionAttributes retrieval, only the first should send a REST request, the second uses the cache
        ActivePowerControlAttributes apc1 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .build();
        getExtensionAttributes(apc1, networkUuid, identifiableId, cachedClient, ActivePowerControl.NAME);

        // Not found extension
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + identifiableId + "/extensions/" + ConnectablePosition.NAME))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<ExtensionAttributes> notFoundExtensionAttributes = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ConnectablePosition.NAME);
        assertFalse(notFoundExtensionAttributes.isPresent());
        server.verify();
        server.reset();

        removeExtensionAttributes(networkUuid, identifiableId, cachedClient, ActivePowerControl.NAME);

        // When removing the generator, the extension attributes should be removed from the cache as well
        GeneratorStartupAttributes gs1 = GeneratorStartupAttributes.builder()
                .marginalCost(6.8)
                .forcedOutageRate(35)
                .plannedOutageRate(30)
                .startupCost(28)
                .plannedActivePowerSetpoint(5)
                .build();
        String oneExtensionAttributes = objectMapper.writeValueAsString(ExtensionAttributesTopLevelDocument.of(gs1));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + identifiableId + "/extensions/" + GeneratorStartup.NAME))
                .andExpect(method(GET))
                .andRespond(withSuccess(oneExtensionAttributes, MediaType.APPLICATION_JSON));

        Optional<ExtensionAttributes> gs1Attributes = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, GeneratorStartup.NAME);
        assertTrue(gs1Attributes.isPresent());

        cachedClient.removeGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, List.of(identifiableId));
        gs1Attributes = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, GeneratorStartup.NAME);
        assertFalse(gs1Attributes.isPresent());
    }

    private void removeExtensionAttributes(UUID networkUuid, String identifiableId, CachedNetworkStoreClient cachedClient, String extensionName) {
        // When calling removeExtensionAttributes, the attributes should be removed from the cache and no new request should be done
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"));
        cachedClient.removeExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, Map.of(identifiableId, Set.of(extensionName)));
        Optional<ExtensionAttributes> extensionAttributes = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, extensionName);
        assertFalse(extensionAttributes.isPresent());
        server.verify();
        server.reset();
    }

    private void getExtensionAttributes(ExtensionAttributes extensionAttributes, UUID networkUuid, String identifiableId, CachedNetworkStoreClient cachedClient, String extensionName) throws JsonProcessingException {
        String oneExtensionAttributes = objectMapper.writeValueAsString(ExtensionAttributesTopLevelDocument.of(List.of(extensionAttributes)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + identifiableId + "/extensions/" + extensionName))
                .andExpect(method(GET))
                .andRespond(withSuccess(oneExtensionAttributes, MediaType.APPLICATION_JSON));

        Optional<ExtensionAttributes> extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, extensionName);
        assertTrue(extensionAttributesResult.isPresent());

        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, extensionName);
        assertTrue(extensionAttributesResult.isPresent());

        server.verify();
        server.reset();
    }

    @Test
    public void testGetExtensionsCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "GEN";

        // Load the identifiable in the cache
        loadGeneratorToCache(identifiableId, networkUuid, cachedClient);

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

        String multipleExtensionAttributes = objectMapper.writerFor(new TypeReference<Map<String, ExtensionAttributes>>() {
        }).writeValueAsString(Map.of(ActivePowerControl.NAME, apc1, GeneratorStartup.NAME, gs1));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + identifiableId + "/extensions"))
                .andExpect(method(GET))
                .andRespond(withSuccess(multipleExtensionAttributes, MediaType.APPLICATION_JSON));

        Map<String, ExtensionAttributes> extensionAttributesMap = cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId);
        assertEquals(2, extensionAttributesMap.size());

        extensionAttributesMap = cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId);
        assertEquals(2, extensionAttributesMap.size());

        server.verify();
        server.reset();

        // When calling removeExtensionAttributes, the attributes should be removed from the cache and no new request should be done
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"));
        cachedClient.removeExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, Map.of(identifiableId, Set.of(ActivePowerControl.NAME)));

        extensionAttributesMap = cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId);
        assertEquals(1, extensionAttributesMap.size());
        assertNull(extensionAttributesMap.get(ActivePowerControl.NAME));
        server.verify();
        server.reset();
    }

    @Test
    public void testGetExtensionCacheWithClonedNetwork() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        int targetVariantNum = 1;
        String targetVariantId = "new_variant";
        String identifiableId = "GEN";

        loadGeneratorToCache(identifiableId, networkUuid, cachedClient);
        ActivePowerControlAttributes apc1 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .build();
        getExtensionAttributes(apc1, networkUuid, identifiableId, cachedClient, ActivePowerControl.NAME);
        OperatingStatusAttributes os1 = OperatingStatusAttributes.builder()
                .operatingStatus("foo")
                .build();
        getExtensionAttributes(os1, networkUuid, identifiableId, cachedClient, OperatingStatus.NAME);

        // Remove extension attributes to check that the removed cache is cloned
        removeExtensionAttributes(networkUuid, identifiableId, cachedClient, OperatingStatus.NAME);
        // When cloning the network, the cached attributes should remained cached
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/to/" + targetVariantNum + "?targetVariantId=" + targetVariantId))
                .andExpect(method(PUT))
                .andRespond(withSuccess());
        // Clone network and verify that there is the expected extension in the cloned cache
        cachedClient.cloneNetwork(networkUuid, Resource.INITIAL_VARIANT_NUM, targetVariantNum, targetVariantId);
        Optional<ExtensionAttributes> apc1Attributes = cachedClient.getExtensionAttributes(networkUuid, targetVariantNum, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(apc1Attributes.isPresent());
        Optional<ExtensionAttributes> os1Attributes = cachedClient.getExtensionAttributes(networkUuid, targetVariantNum, ResourceType.GENERATOR, identifiableId, OperatingStatus.NAME);
        assertFalse(os1Attributes.isPresent());
        server.verify();
        server.reset();
    }

    @Test
    /*
    * Following sequence should not overwrite resource in the cache
    * getGenerator()
    * getVoltageLevelGenerator()
    * getGenerators()
     */
    public void testGetExtensionOverwriteCache1() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "GEN";

        // Load the identifiable in the cache
        loadGeneratorToCache(identifiableId, networkUuid, cachedClient);

        // Load extension attributes in the cache
        loadExtensionAttributesToCache(networkUuid, identifiableId, cachedClient);
        Optional<ExtensionAttributes> extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load generators for voltage level VL_1 (by container), this should not overwrite existing resource in the cache
        loadVoltageLevelGenerators(identifiableId, networkUuid, cachedClient);
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load all generators, this should not overwrite existing resource in the cache
        loadGeneratorCollection(identifiableId, networkUuid, cachedClient);
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());
    }

    @Test
    /*
     * Following sequence should not overwrite resource in the cache
     * getGenerator()
     * getGenerators()
     * getVoltageLevelGenerator()
     */
    public void testGetExtensionOverwriteCache2() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "GEN";

        // Load the identifiable in the cache
        loadGeneratorToCache(identifiableId, networkUuid, cachedClient);

        // Load extension attributes in the cache
        loadExtensionAttributesToCache(networkUuid, identifiableId, cachedClient);
        Optional<ExtensionAttributes> extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load all generators, this should not overwrite existing resource in the cache
        loadGeneratorCollection(identifiableId, networkUuid, cachedClient);
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load generators for voltage level VL_1 (by container), this should not overwrite existing resource in the cache
        cachedClient.getVoltageLevelGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_1");
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());
    }

    @Test
    /*
     * Following sequence should not overwrite resource in the cache
     * getIdentifiable()
     * getVoltageLevelGenerator()
     * getGenerators()
     */
    public void testGetExtensionOverwriteCache3() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "GEN";

        // Load the identifiable in the cache
        loadIdentifiableToCache(identifiableId, networkUuid, cachedClient);

        // Load extension attributes in the cache
        loadExtensionAttributesToCache(networkUuid, identifiableId, cachedClient);
        Optional<ExtensionAttributes> extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load generators for voltage level VL_1 (by container), this should not overwrite existing resource in the cache
        loadVoltageLevelGenerators(identifiableId, networkUuid, cachedClient);
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load all generators, this should not overwrite existing resource in the cache
        loadGeneratorCollection(identifiableId, networkUuid, cachedClient);
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());
    }

    @Test
    /*
     * Following sequence should not overwrite resource in the cache
     * getIdentifiable()
     * getGenerators()
     * getVoltageLevelGenerator()
     */
    public void testGetExtensionOverwriteCache4() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "GEN";

        // Load the identifiable in the cache
        loadIdentifiableToCache(identifiableId, networkUuid, cachedClient);

        // Load extension attributes in the cache
        loadExtensionAttributesToCache(networkUuid, identifiableId, cachedClient);
        Optional<ExtensionAttributes> extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load all generators, this should not overwrite existing resource in the cache
        loadGeneratorCollection(identifiableId, networkUuid, cachedClient);
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load generators for voltage level VL_1 (by container), this should not overwrite existing resource in the cache
        cachedClient.getVoltageLevelGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_1");
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());
    }

    @Test
    /*
     * Following sequence should not overwrite resource in the cache
     * getGenerators()
     * getVoltageLevelGenerator()
     * getGenerator()
     */
    public void testGetExtensionOverwriteCache5() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "GEN";

        // Load all generators, this should not overwrite existing resource in the cache
        loadGeneratorCollection(identifiableId, networkUuid, cachedClient);

        // Load extension attributes in the cache
        loadExtensionAttributesToCache(networkUuid, identifiableId, cachedClient);
        Optional<ExtensionAttributes> extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load generators for voltage level VL_1 (by container), this should not overwrite existing resource in the cache
        cachedClient.getVoltageLevelGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, "VL_1");
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());

        // Load the identifiable in the cache, this should not overwrite existing resource in the cache
        cachedClient.getGenerator(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableId);
        extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());
    }

    @Test
    public void testRemovedIdentifiableInCachePresentOnServer() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "GEN";

        // Remove generator
        cachedClient.removeGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, List.of(identifiableId));

        // Load extension from removed identifiable (should not fetch from the server)
        assertTrue(cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME).isEmpty());
        server.verify();
        server.reset();

        // Load all active power control extensions for all generators (should not throw)
        ActivePowerControlAttributes apc1 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .build();
        String extensionAttributes = objectMapper.writerFor(new TypeReference<Map<String, ExtensionAttributes>>() {
        }).writeValueAsString(Map.of(identifiableId, apc1));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions/" + ActivePowerControl.NAME))
                .andExpect(method(GET))
                .andRespond(withSuccess(extensionAttributes, MediaType.APPLICATION_JSON));
        cachedClient.getAllExtensionsAttributesByResourceTypeAndExtensionName(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, ActivePowerControl.NAME);
        assertTrue(cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME).isEmpty());
        server.verify();
        server.reset();

        // Load all extensions of removed identifiable  (should not fetch from the server)
        assertTrue(cachedClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId).isEmpty());

        // Load all extensions for all generators (should not throw)
        extensionAttributes = objectMapper.writerFor(new TypeReference<Map<String, Map<String, ExtensionAttributes>>>() {
        }).writeValueAsString(Map.of(identifiableId, Map.of(ActivePowerControl.NAME, apc1)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(GET))
                .andRespond(withSuccess(extensionAttributes, MediaType.APPLICATION_JSON));
        cachedClient.getAllExtensionsAttributesByResourceType(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR);
        assertTrue(cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME).isEmpty());
        server.verify();
        server.reset();
    }

    private void loadGeneratorToCache(String identifiableId, UUID networkUuid, CachedNetworkStoreClient cachedClient) throws JsonProcessingException {
        Resource<GeneratorAttributes> g1Resource = Resource.generatorBuilder()
                .id(identifiableId)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("VL_1")
                        .build())
                .build();
        String generatorJson = objectMapper.writeValueAsString(TopLevelDocument.of(List.of(g1Resource)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/generators/" + identifiableId))
                .andExpect(method(GET))
                .andRespond(withSuccess(generatorJson, MediaType.APPLICATION_JSON));
        cachedClient.getGenerator(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableId);
        server.verify();
        server.reset();
    }

    private void loadLineToCache(String identifiableId, UUID networkUuid, CachedNetworkStoreClient cachedClient) throws JsonProcessingException {
        Resource<LineAttributes> g1Resource = Resource.lineBuilder()
            .id(identifiableId)
            .attributes(LineAttributes.builder()
                .voltageLevelId1("VL_1")
                .voltageLevelId2("VL_2")
                .build())
            .build();
        String lineJson = objectMapper.writeValueAsString(TopLevelDocument.of(List.of(g1Resource)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines/" + identifiableId))
            .andExpect(method(GET))
            .andRespond(withSuccess(lineJson, MediaType.APPLICATION_JSON));
        cachedClient.getLine(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableId);
        server.verify();
        server.reset();
    }

    private void loadIdentifiableToCache(String identifiableId, UUID networkUuid, CachedNetworkStoreClient cachedClient) throws JsonProcessingException {
        Resource<GeneratorAttributes> g1Resource = Resource.generatorBuilder()
                .id(identifiableId)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("VL_1")
                        .build())
                .build();
        String generatorJson = objectMapper.writeValueAsString(TopLevelDocument.of(List.of(g1Resource)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + identifiableId))
                .andExpect(method(GET))
                .andRespond(withSuccess(generatorJson, MediaType.APPLICATION_JSON));
        cachedClient.getIdentifiable(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableId);
        server.verify();
        server.reset();
    }

    private void loadGeneratorCollection(String identifiableId, UUID networkUuid, CachedNetworkStoreClient cachedClient) throws JsonProcessingException {
        String voltageLevelId = "VL_1";
        Resource<GeneratorAttributes> g1Resource = Resource.generatorBuilder()
                .id(identifiableId)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId(voltageLevelId)
                        .build())
                .build();
        String generatorJson = objectMapper.writeValueAsString(TopLevelDocument.of(List.of(g1Resource)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/generators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(generatorJson, MediaType.APPLICATION_JSON));
        cachedClient.getGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM);
        server.verify();
        server.reset();
    }

    private void loadVoltageLevelGenerators(String identifiableId, UUID networkUuid, CachedNetworkStoreClient cachedClient) throws JsonProcessingException {
        String voltageLevelId = "VL_1";
        Resource<GeneratorAttributes> g1Resource = Resource.generatorBuilder()
                .id(identifiableId)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId(voltageLevelId)
                        .build())
                .build();
        String generatorJson = objectMapper.writeValueAsString(TopLevelDocument.of(List.of(g1Resource)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/voltage-levels/" + voltageLevelId + "/generators"))
                .andExpect(method(GET))
                .andRespond(withSuccess(generatorJson, MediaType.APPLICATION_JSON));
        cachedClient.getVoltageLevelGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, voltageLevelId);
        server.verify();
        server.reset();
    }

    private void loadExtensionAttributesToCache(UUID networkUuid, String identifiableId, CachedNetworkStoreClient cachedClient) throws JsonProcessingException {
        ActivePowerControlAttributes apc1 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .build();
        String oneExtensionAttributes = objectMapper.writeValueAsString(ExtensionAttributesTopLevelDocument.of(List.of(apc1)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + identifiableId + "/extensions/" + ActivePowerControl.NAME))
                .andExpect(method(GET))
                .andRespond(withSuccess(oneExtensionAttributes, MediaType.APPLICATION_JSON));
        Optional<ExtensionAttributes> extensionAttributesResult = cachedClient.getExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, identifiableId, ActivePowerControl.NAME);
        assertTrue(extensionAttributesResult.isPresent());
        server.verify();
        server.reset();
    }

    @Test
    public void testGetOperationalLimitsGroupCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "LINE";
        String operationalLimitsGroupId = "default";

        // Load the line in the cache
        loadLineToCache(identifiableId, networkUuid, cachedClient);

        // network is not loaded before
        // Two successive OperationalLimitsGroup retrieval, only the first should send a REST request, the second uses the cache
        OperationalLimitsGroupAttributes olg1 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId);
        getOperationalLimitsGroup(olg1, networkUuid, identifiableId, cachedClient, operationalLimitsGroupId, 1);

        // Not found Operational Limits Group
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + "otherId" + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + "randomOLGid" + "/side/" + "1"))
            .andExpect(method(GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<OperationalLimitsGroupAttributes> notFoundOperationalLimitsGroup = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, "otherId", "randomOLGid", 1);
        assertFalse(notFoundOperationalLimitsGroup.isPresent());
        server.verify();
        server.reset();

        // if the line is removed, it returns an empty operational limits group, and it does not call the api
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/side/1/operationalLimitsGroup"))
            .andExpect(method(GET));
        cachedClient.removeLines(networkUuid, Resource.INITIAL_VARIANT_NUM, List.of(identifiableId));
        Optional<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributes = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, identifiableId, operationalLimitsGroupId, 1);
        assertTrue(operationalLimitsGroupAttributes.isEmpty());
        server.verify();
        server.reset();
    }

    @Test
    public void testLoadingAllSelectedOperationalLimitsGroups() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "LINE";
        String operationalLimitsGroupId = "selected";
        String operationalLimitsGroupId2 = "otherGroup";

        // Load the line in the cache
        loadLineToCache(identifiableId, networkUuid, cachedClient);

        // network is not loaded before
        // Two successive OperationalLimitsGroup retrieval, only the first should send a REST request, the second uses the cache
        OperationalLimitsGroupAttributes olg1 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId);
        OperationalLimitsGroupAttributes olg2 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId2);
        String selectedOperationalLimitsGroup = objectMapper.writeValueAsString(Map.of(identifiableId, Map.of(1, Map.of(operationalLimitsGroupId, olg1))));
        String otherOperationalLimitsGroup = objectMapper.writeValueAsString(OperationalLimitsGroupAttributesTopLevelDocument.of(olg2));
        // load all selected operational limits group
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup/selected"))
            .andExpect(method(GET))
            .andRespond(withSuccess(selectedOperationalLimitsGroup, MediaType.APPLICATION_JSON));
        cachedClient.loadAllSelectedOperationalLimitsGroupAttributesByResourceType(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE);
        server.verify();
        server.reset();

        // a selected group is asked it should not call the url as it is loaded to the cache
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId + "/side/" + "1"))
            .andExpect(method(GET));
        Optional<OperationalLimitsGroupAttributes> olg1Attributes = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, identifiableId, operationalLimitsGroupId, 1);
        assertTrue(olg1Attributes.isPresent());
        server.verify();
        server.reset();

        // another group is asked and will be loaded from the back and loaded to the cache
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId2 + "/side/" + "1"))
            .andExpect(method(GET))
            .andRespond(withSuccess(otherOperationalLimitsGroup, MediaType.APPLICATION_JSON));
        Optional<OperationalLimitsGroupAttributes> olg2Attributes = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, identifiableId, operationalLimitsGroupId2, 1);
        assertTrue(olg2Attributes.isPresent());
        server.verify();
        server.reset();
    }

    private OperationalLimitsGroupAttributes createOperationalLimitsGroupAttributes(String operationalLimitsGroupId) {
        TreeMap<Integer, TemporaryLimitAttributes> temporaryLimits = new TreeMap<>();
        temporaryLimits.put(10, TemporaryLimitAttributes.builder()
            .value(12)
            .name("temporarylimit1")
            .acceptableDuration(10)
            .fictitious(false)
            .build());
        temporaryLimits.put(15, TemporaryLimitAttributes.builder()
            .value(9)
            .name("temporarylimit2")
            .acceptableDuration(15)
            .fictitious(false)
            .build());
        return OperationalLimitsGroupAttributes.builder()
            .id(operationalLimitsGroupId)
            .currentLimits(LimitsAttributes.builder()
                .permanentLimit(1)
                .temporaryLimits(temporaryLimits)
                .build())
            .build();
    }

    private void getOperationalLimitsGroup(OperationalLimitsGroupAttributes operationalLimitsGroupAttributes, UUID networkUuid, String identifiableId, CachedNetworkStoreClient cachedClient, String operationalLimitsGroupId, int side) throws JsonProcessingException {
        // called twice the first call the rest api the second uses cache
        String oneOperationaLimitsGroupAttributes = objectMapper.writeValueAsString(OperationalLimitsGroupAttributesTopLevelDocument.of(List.of(operationalLimitsGroupAttributes)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId + "/side/" + side))
            .andExpect(method(GET))
            .andRespond(withSuccess(oneOperationaLimitsGroupAttributes, MediaType.APPLICATION_JSON));

        Optional<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributesResult = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, identifiableId, operationalLimitsGroupId, side);
        assertTrue(operationalLimitsGroupAttributesResult.isPresent());

        operationalLimitsGroupAttributesResult = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, identifiableId, operationalLimitsGroupId, side);
        assertTrue(operationalLimitsGroupAttributesResult.isPresent());

        server.verify();
        server.reset();
    }

    @Test
    public void testClone() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.randomUUID();
        int targetVariantNum1 = 1;
        String targetVariantId1 = "variant1";
        int targetVariantNum2 = 2;
        String targetVariantId2 = "variant2";
        // Load network to cache
        Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM))
                .andExpect(method(GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(TopLevelDocument.of(n1)), MediaType.APPLICATION_JSON));
        cachedClient.getNetwork(networkUuid, Resource.INITIAL_VARIANT_NUM);
        server.verify();
        server.reset();
        // Partial clone 0 -> 1
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/to/" + targetVariantNum1 + "?targetVariantId=" + targetVariantId1))
                .andExpect(method(PUT))
                .andRespond(withSuccess());
        cachedClient.cloneNetwork(networkUuid, Resource.INITIAL_VARIANT_NUM, targetVariantNum1, targetVariantId1);
        Optional<Resource<NetworkAttributes>> networkOpt = cachedClient.getNetwork(networkUuid, targetVariantNum1);
        assertTrue(networkOpt.isPresent());
        NetworkAttributes networkAttributes = networkOpt.get().getAttributes();
        assertEquals(0, networkAttributes.getFullVariantNum());
        server.verify();
        server.reset();
        // Partial clone 1 -> 2
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + targetVariantNum1 + "/to/" + targetVariantNum2 + "?targetVariantId=" + targetVariantId2))
                .andExpect(method(PUT))
                .andRespond(withSuccess());
        cachedClient.cloneNetwork(networkUuid, targetVariantNum1, targetVariantNum2, targetVariantId2);
        networkOpt = cachedClient.getNetwork(networkUuid, targetVariantNum2);
        assertTrue(networkOpt.isPresent());
        networkAttributes = networkOpt.get().getAttributes();
        assertEquals(0, networkAttributes.getFullVariantNum());
        server.verify();
        server.reset();
    }

    @Test
    public void testLoadingAllOperationalLimitsGroupsForBranch() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "LINE";
        String identifiableId2 = "LINE_2";
        String operationalLimitsGroupId = "selected";
        String operationalLimitsGroupId2 = "otherGroup";
        String operationalLimitsGroupId3 = "groupOnLine2";
        int side = 1;

        // Load the line in the cache
        loadLineToCache(identifiableId, networkUuid, cachedClient);
        loadLineToCache(identifiableId2, networkUuid, cachedClient);

        // network is not loaded before
        OperationalLimitsGroupAttributes olg1 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId);
        OperationalLimitsGroupAttributes olg2 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId2);
        OperationalLimitsGroupAttributes olg3 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId3);
        String allOperationalLimitsGroupOnBranchLine = objectMapper.writeValueAsString(List.of(olg1, olg2));
        // load all operational limits group for the branch LINE
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/side/" + side + "/operationalLimitsGroup"))
            .andExpect(method(GET))
            .andRespond(withSuccess(allOperationalLimitsGroupOnBranchLine, MediaType.APPLICATION_JSON));
        cachedClient.getOperationalLimitsGroupAttributesForBranchSide(networkUuid, Resource.INITIAL_VARIANT_NUM,
            ResourceType.LINE, identifiableId, side);
        server.verify();
        server.reset();

        // getting any operational limits group on LINE does not call the rest api
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId + "/side/" + side))
            .andExpect(method(GET));
        Optional<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributes1 = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM,
            ResourceType.LINE, identifiableId, operationalLimitsGroupId, side);
        server.verify();
        server.reset();
        assertTrue(operationalLimitsGroupAttributes1.isPresent());
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId2 + "/side/" + side))
            .andExpect(method(GET));
        Optional<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributes2 = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM,
            ResourceType.LINE, identifiableId, operationalLimitsGroupId2, side);
        server.verify();
        server.reset();
        assertTrue(operationalLimitsGroupAttributes2.isPresent());

        // getting a olg from another branch will trigger rest api
        String olgFromAnotherBranch = objectMapper.writeValueAsString(OperationalLimitsGroupAttributesTopLevelDocument.of(List.of(olg3)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + identifiableId2 + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId3 + "/side/" + side))
            .andExpect(method(GET))
            .andRespond(withSuccess(olgFromAnotherBranch, MediaType.APPLICATION_JSON));
        Optional<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributes3 = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM,
            ResourceType.LINE, identifiableId2, operationalLimitsGroupId3, side);
        server.verify();
        server.reset();
        assertTrue(operationalLimitsGroupAttributes3.isPresent());

    }

    @Test
    public void testLoadingAllOperationalLimitsGroups() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String identifiableId = "LINE_1";
        String identifiableId2 = "LINE_2";
        String operationalLimitsGroupId = "selected";
        String operationalLimitsGroupId2 = "selected";
        String operationalLimitsGroupId3 = "otherGroup";

        // Load the lines in the cache
        loadLineToCache(identifiableId, networkUuid, cachedClient);
        loadLineToCache(identifiableId2, networkUuid, cachedClient);

        // network is not loaded before
        OperationalLimitsGroupAttributes olg1 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId);
        OperationalLimitsGroupAttributes olg2 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId2);
        OperationalLimitsGroupAttributes olg3 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId3);
        String allOperationalLimitsGroups = objectMapper.writerFor(new TypeReference<Map<String, Map<Integer, Map<String, OperationalLimitsGroupAttributes>>>>() {
        }).writeValueAsString(Map.of(identifiableId, Map.of(1, Map.of(operationalLimitsGroupId, olg1)),
            identifiableId2, Map.of(2, Map.of(operationalLimitsGroupId2, olg2, operationalLimitsGroupId3, olg3))));

        // load all operational limits groups to cache
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup"))
            .andExpect(method(GET))
            .andRespond(withSuccess(allOperationalLimitsGroups, MediaType.APPLICATION_JSON));
        cachedClient.loadAllOperationalLimitsGroupAttributesByResourceType(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE);
        server.verify();
        server.reset();

        // a group is asked it should not call the url as it is loaded to the cache
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId + "/side/" + "1"))
            .andExpect(method(GET));
        Optional<OperationalLimitsGroupAttributes> olg1Attributes = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, identifiableId, operationalLimitsGroupId, 1);
        assertTrue(olg1Attributes.isPresent());
        server.verify();
        server.reset();

        // loading all selected operational limits groups won't load anything as everything is already loaded
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup/selected"))
            .andExpect(method(GET));
        cachedClient.loadAllSelectedOperationalLimitsGroupAttributesByResourceType(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE);
        server.verify();
        server.reset();

        // loading all operational limits groups of a branch won't load anything as everything is already loaded
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/side/1/operationalLimitsGroup"))
            .andExpect(method(GET));
        cachedClient.getOperationalLimitsGroupAttributesForBranchSide(networkUuid, Resource.INITIAL_VARIANT_NUM,
            ResourceType.LINE, identifiableId, 1);
        server.verify();
        server.reset();
    }

    @Test
    public void testUpdatingLineWithOperationalLimitsGroup() {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // create a line with an operationalLimitsGroup
        String identifiableId = "LINE_1";
        String operationalLimitsGroupId = "id";
        OperationalLimitsGroupAttributes olg1 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId);
        Resource<LineAttributes> lineResource = Resource.lineBuilder()
            .id(identifiableId)
            .attributes(LineAttributes.builder()
                .voltageLevelId1("VL_1")
                .voltageLevelId2("VL_2").operationalLimitsGroups1(Map.of(operationalLimitsGroupId, olg1))
                .build())
            .build();
        cachedClient.createLines(networkUuid, List.of(lineResource));

        // checking that the olg is in the cache and do not call the rest api
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                + "/branch/" + identifiableId + "/types/" + ResourceType.LINE + "/side/1/operationalLimitsGroup"))
            .andExpect(method(GET));
        Optional<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributes = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, 0, ResourceType.LINE, identifiableId, operationalLimitsGroupId, 1);
        server.verify();
        server.reset();
        assertTrue(operationalLimitsGroupAttributes.isPresent());
    }

    @Test
    public void testRemoveOperationalLimitsGroupCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String branchId = "LINE";
        String operationalLimitsGroupId = "toRemove";

        // Load the line in the cache
        loadLineToCache(branchId, networkUuid, cachedClient);

        // network is not loaded before
        OperationalLimitsGroupAttributes olg1 = createOperationalLimitsGroupAttributes(operationalLimitsGroupId);
        // getting the olg will load it to cache
        getOperationalLimitsGroup(olg1, networkUuid, branchId, cachedClient, operationalLimitsGroupId, 1);

        // remove the operational limits group will not call the api as the olg was loaded just before
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup"))
            .andExpect(method(DELETE));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + branchId + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId + "/side/1"))
                .andExpect(method(GET));
        cachedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId))));
        server.verify();
        server.reset();

        // trying to get it return empty and does not call the REST api
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/" + branchId + "/types/" + ResourceType.LINE + "/operationalLimitsGroup/" + operationalLimitsGroupId + "/side/1"))
                .andExpect(method(GET));
        Optional<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributes = cachedClient.getOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, branchId, operationalLimitsGroupId, 1);
        assertTrue(operationalLimitsGroupAttributes.isEmpty());
        server.verify();
        server.reset();
    }

    @Test
    public void testGetNetworkAfterCreateAvoidServerCall() {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool()));
        UUID networkUuid1 = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        UUID networkUuid2 = UUID.fromString("9028181c-7977-4592-ba19-88027e4254e4");

        Resource<NetworkAttributes> network1 = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid1)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        Resource<NetworkAttributes> network2 = Resource.networkBuilder()
                .id("n2")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid2)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        cachedClient.createNetworks(List.of(network1, network2));

        cachedClient.getNetwork(networkUuid1, 0);
        cachedClient.getNetwork(networkUuid2, 0);

        server.verify();
        server.reset();
    }
}
