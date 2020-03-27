/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TopLevelDocument;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestNetworkStoreClient.class)
@ContextConfiguration(classes = RestNetworkStoreClient.class)
public class CachedRestNetworkStoreClientTest {

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
    public void testSingleLineCache() throws IOException {
        CachedRestNetworkStoreClient cachedClient = new CachedRestNetworkStoreClient(new BufferedRestNetworkStoreClient(restStoreClient));
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        // Two successive line retrievals, only the first should send a REST request, the second uses the cache

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

        String line1Json = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(l1)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines/LINE_1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(line1Json, MediaType.APPLICATION_JSON));

        // First time line retrieval by Id
        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(networkUuid, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);

        // Second time line retrieval by Id
        lineAttributesResource = cachedClient.getLine(networkUuid, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);

        server.verify();

        server.reset();

        // First, we retrieve all lines of the network, the second time we retrieve only one line. For this second retrieval, no REST request is sent (cache is used)

        //cachedClient.deleteNetwork(networkUuid);

        // We expect all lines retrieval REST request to be executed just once
        String linesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(l1, l2)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesJson, MediaType.APPLICATION_JSON));

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/lines/LINE_1"));

        // First time retrieval of all lines of the network
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getLines(networkUuid);
        assertNotNull(lineAttributesResources);
        assertEquals(2, lineAttributesResources.size());

        // Second time retrieval of all lines of the network
        lineAttributesResources = cachedClient.getLines(networkUuid);
        assertNotNull(lineAttributesResources);
        assertEquals(2, lineAttributesResources.size());

        // Retrieval of a single line of the network
        lineAttributesResource = cachedClient.getLine(networkUuid, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

    }

    @Test
    public void testVoltageLevelLineCache() throws IOException {
        CachedRestNetworkStoreClient cachedClient = new CachedRestNetworkStoreClient(new BufferedRestNetworkStoreClient(restStoreClient));
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
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/voltage-levels/VL_1/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesV1Json, MediaType.APPLICATION_JSON));

        // First time lines retrieval by voltage level
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, "VL_1");
        assertEquals(2, lineAttributesResources.size());

        // Second time lines retrieval by voltage level
        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, "VL_1");
        assertEquals(2, lineAttributesResources.size());

        server.verify();

        server.reset();

        // Single line retrieval by Id

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/lines/LINE_1"));

        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(networkUuid, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

        server.reset();

        // Getting all lines of the network

        // We expect all lines retrieval REST request will be executed only once
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesV1Json, MediaType.APPLICATION_JSON));

        // First time all network lines retrieval
        List<Resource<LineAttributes>> allLineAttributesResources = cachedClient.getLines(networkUuid);
        assertNotNull(allLineAttributesResources);
        assertEquals(2, allLineAttributesResources.size());

        // Second time all network lines retrieval
        allLineAttributesResources = cachedClient.getLines(networkUuid);
        assertNotNull(allLineAttributesResources);
        assertEquals(2, allLineAttributesResources.size());

        server.verify();
    }

    @Test
    public void testAllLinesCache() throws IOException {
        CachedRestNetworkStoreClient cachedClient = new CachedRestNetworkStoreClient(new BufferedRestNetworkStoreClient(restStoreClient));
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
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(alllinesJson, MediaType.APPLICATION_JSON));

        // First time all lines retrieval
        List<Resource<LineAttributes>> lineAttributesResources = cachedClient.getLines(networkUuid);
        assertEquals(3, lineAttributesResources.size());

        // Second time lines retrieval by voltage level
        lineAttributesResources = cachedClient.getLines(networkUuid);
        assertEquals(3, lineAttributesResources.size());

        server.verify();

        server.reset();

        // Single line retrieval by Id

        // We expect single line retrieval by id REST request will never be executed (cache will be used)
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/lines/LINE_1"));

        Resource<LineAttributes> lineAttributesResource = cachedClient.getLine(networkUuid, "LINE_1").orElse(null);
        assertNotNull(lineAttributesResource);
        assertEquals("LINE_1", lineAttributesResource.getId());

        server.verify();

        server.reset();

        // Getting all lines of a specified voltage level

        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/voltage-levels/VL_1/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/voltage-levels/VL_2/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/voltage-levels/VL_3/lines"));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/voltage-levels/VL_4/lines"));

        // Lines retrieval by voltage level (should use cache)
        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, "VL_1");
        assertEquals(3, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, "VL_2");
        assertEquals(1, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, "VL_3");
        assertEquals(1, lineAttributesResources.size());

        lineAttributesResources = cachedClient.getVoltageLevelLines(networkUuid, "VL_4");
        assertEquals(1, lineAttributesResources.size());

        server.verify();
    }
}
