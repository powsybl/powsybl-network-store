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
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SwitchAttributes;
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
}
