/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.ResourceUpdaterImpl;
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
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestNetworkStoreClient.class)
@ContextConfiguration(classes = RestNetworkStoreClient.class)
public class CachedNetworkStoreClientTest {

    private static final UUID NETWORK_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    @Autowired
    private RestNetworkStoreClient restStoreClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private ResourceUpdater resourceUpdater;

    @Before
    public void setUp() throws IOException {
        resourceUpdater = new ResourceUpdaterImpl(NETWORK_UUID, restStoreClient);
    }

    @Test
    public void testSingleLineCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient));

        // Two successive line retrievals, only the first should send a REST request, the second uses the cache
        Resource<LineAttributes> l1 = Resource.lineBuilder(NETWORK_UUID, resourceUpdater)
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

        Resource<LineAttributes> l2 = Resource.lineBuilder(NETWORK_UUID, resourceUpdater)
                .id("LINE_2")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_3")
                        .build())
                .build();

        String line1Json = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(l1)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + NETWORK_UUID + "/lines/LINE_1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(line1Json, MediaType.APPLICATION_JSON));

        // First time line retrieval by Id
        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(NETWORK_UUID, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);

        // Second time line retrieval by Id
        lineAttributesResource = cachedClient.getLine(NETWORK_UUID, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);

        server.verify();

        server.reset();

        // First, we retrieve all lines of the network, the second time we retrieve only one line. For this second retrieval, no REST request is sent (cache is used)

        //cachedClient.deleteNetwork(networkUuid);

        // We expect all lines retrieval REST request to be executed just once
        String linesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(l1, l2)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + NETWORK_UUID + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesJson, MediaType.APPLICATION_JSON));

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + NETWORK_UUID + "/lines/LINE_1"));

        // First time retrieval of all lines of the network
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getLines(NETWORK_UUID);
        assertNotNull(lineAttributesResources);
        assertEquals(2, lineAttributesResources.size());

        // Second time retrieval of all lines of the network
        lineAttributesResources = cachedClient.getLines(NETWORK_UUID);
        assertNotNull(lineAttributesResources);
        assertEquals(2, lineAttributesResources.size());

        // Retrieval of a single line of the network
        lineAttributesResource = cachedClient.getLine(NETWORK_UUID, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

        server.reset();

        lineAttributesResource = cachedClient.getLine(NETWORK_UUID, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);

        assertEquals(0., lineAttributesResource.getAttributes().getP1(), 0.);  // test P1 value

        lineAttributesResource.getAttributes().setP1(100.);  // set P1 value

        lineAttributesResource = cachedClient.getLine(NETWORK_UUID, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals(100., lineAttributesResource.getAttributes().getP1(), 0.);  // test P1 value

        server.verify();
    }

    @Test
    public void testVoltageLevelLineCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient));

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
        server.expect(ExpectedCount.once(), requestTo("/networks/" + NETWORK_UUID + "/voltage-levels/VL_1/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesV1Json, MediaType.APPLICATION_JSON));

        // First time lines retrieval by voltage level
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getVoltageLevelLines(NETWORK_UUID, "VL_1");
        assertEquals(2, lineAttributesResources.size());

        // Second time lines retrieval by voltage level
        lineAttributesResources = cachedClient.getVoltageLevelLines(NETWORK_UUID, "VL_1");
        assertEquals(2, lineAttributesResources.size());

        server.verify();

        server.reset();

        // Single line retrieval by Id

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + NETWORK_UUID + "/lines/LINE_1"));

        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(NETWORK_UUID, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

        server.reset();

        // Getting all lines of the network

        // We expect all lines retrieval REST request will be executed only once
        server.expect(ExpectedCount.once(), requestTo("/networks/" + NETWORK_UUID + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesV1Json, MediaType.APPLICATION_JSON));

        // First time all network lines retrieval
        List<Resource<LineAttributes>> allLineAttributesResources = cachedClient.getLines(NETWORK_UUID);
        assertNotNull(allLineAttributesResources);
        assertEquals(2, allLineAttributesResources.size());

        // Second time all network lines retrieval
        allLineAttributesResources = cachedClient.getLines(NETWORK_UUID);
        assertNotNull(allLineAttributesResources);
        assertEquals(2, allLineAttributesResources.size());

        server.verify();
    }

    @Test
    public void testAllLinesCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient));

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
        server.expect(ExpectedCount.once(), requestTo("/networks/" + NETWORK_UUID + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(alllinesJson, MediaType.APPLICATION_JSON));

        // First time all lines retrieval
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getLines(NETWORK_UUID);
        assertEquals(3, lineAttributesResources.size());

        // Second time lines retrieval by voltage level
        lineAttributesResources = cachedClient.getLines(NETWORK_UUID);
        assertEquals(3, lineAttributesResources.size());

        server.verify();

        server.reset();

        // Single line retrieval by Id

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + NETWORK_UUID + "/lines/LINE_1"));

        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(NETWORK_UUID, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

        server.reset();

        // Getting all lines of a specified voltage level

        server.expect(ExpectedCount.never(), requestTo("/networks/" + NETWORK_UUID + "/voltage-levels/VL_1/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + NETWORK_UUID + "/voltage-levels/VL_2/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + NETWORK_UUID + "/voltage-levels/VL_3/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + NETWORK_UUID + "/voltage-levels/VL_4/lines"));

        // Lines retrieval by voltage level (should use cache)
        lineAttributesResources = cachedClient.getVoltageLevelLines(NETWORK_UUID, "VL_1");
        assertEquals(3, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(NETWORK_UUID, "VL_2");
        assertEquals(1, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(NETWORK_UUID, "VL_3");
        assertEquals(1, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(NETWORK_UUID, "VL_4");
        assertEquals(1, lineAttributesResources.size());

        server.verify();
    }

    @Test
    public void testSwitchCache() throws IOException {
        CachedNetworkStoreClient cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(restStoreClient));

        // Two successive switch retrievals, only the first should send a REST request, the second uses the cache
        Resource<SwitchAttributes> breaker = Resource.switchBuilder(NETWORK_UUID, resourceUpdater)
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

        server.expect(ExpectedCount.once(), requestTo("/networks/" + NETWORK_UUID + "/switches/b1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(breakersJson, MediaType.APPLICATION_JSON));

        // First time switch retrieval by Id
        Resource<SwitchAttributes> switchAttributesResource = cachedClient.getSwitch(NETWORK_UUID, "b1").orElse(null);
        assertNotNull(switchAttributesResource);
        assertEquals(Boolean.FALSE, switchAttributesResource.getAttributes().isOpen());  // test switch is closed

        switchAttributesResource.getAttributes().setOpen(true);  // change switch state

        // Second time switch retrieval by Id
        switchAttributesResource = cachedClient.getSwitch(NETWORK_UUID, "b1").orElse(null);
        assertNotNull(switchAttributesResource);
        assertEquals(Boolean.TRUE, switchAttributesResource.getAttributes().isOpen());  // test switch is open

        server.verify();
        server.reset();

        server.expect(ExpectedCount.once(), requestTo("/networks/" + NETWORK_UUID + "/switches"))
                .andExpect(method(PUT))
                .andRespond(withSuccess());

        cachedClient.flush();
    }
}
