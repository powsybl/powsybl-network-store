/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.network.store.model.CloneStrategy;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestClient.class)
@ContextConfiguration(classes = RestClientImpl.class)
public class BufferedNetworkStoreClientTest {

    @Autowired
    private RestClient restClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private RestNetworkStoreClient restStoreClient;

    @Before
    public void setUp() {
        restStoreClient = new RestNetworkStoreClient(restClient);
    }

    @Test
    public void testClone() throws IOException {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        int targetVariantNum1 = 1;
        String targetVariantId1 = "variant1";
        int targetVariantNum2 = 2;
        String targetVariantId2 = "variant2";
        int targetVariantNum3 = 3;
        String targetVariantId3 = "variant3";
        // Update network n1
        Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .cloneStrategy(CloneStrategy.FULL)
                        .build())
                .build();
        bufferedClient.updateNetworks(List.of(n1), null);
        // Full clone 0 -> 1
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/to/" + targetVariantNum1 + "?targetVariantId=" + targetVariantId1))
                .andExpect(method(PUT))
                .andRespond(withSuccess());
        bufferedClient.cloneNetwork(networkUuid, Resource.INITIAL_VARIANT_NUM, targetVariantNum1, targetVariantId1);
        server.verify();
        server.reset();
        // Partial clone 1 -> 2
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + targetVariantNum1 + "/to/" + targetVariantNum2 + "?targetVariantId=" + targetVariantId2))
                .andExpect(method(PUT))
                .andRespond(withSuccess());
        bufferedClient.cloneNetwork(networkUuid, targetVariantNum1, targetVariantNum2, targetVariantId2);
        server.verify();
        server.reset();
        // Partial clone 2 -> 3
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + targetVariantNum2 + "/to/" + targetVariantNum3 + "?targetVariantId=" + targetVariantId3))
                .andExpect(method(PUT))
                .andRespond(withSuccess());
        bufferedClient.cloneNetwork(networkUuid, targetVariantNum2, targetVariantNum3, targetVariantId3);
        server.verify();
        server.reset();
        // Update again network n1 after clones, it should only update this network, not the clones
        n1.getAttributes().setCaseDate(ZonedDateTime.parse("2018-01-01T00:00:00.000Z"));
        // Flush and check that all the networks are also updated with correct targetVariantId, fullVariantNum and cloneStrategy
        Resource<NetworkAttributes> n1UpdatedAfterClone = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2018-01-01T00:00:00.000Z"))
                        .cloneStrategy(CloneStrategy.FULL)
                        .build())
                .build();
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(n1UpdatedAfterClone))))
                .andRespond(withSuccess());
        Resource<NetworkAttributes> n1Clone0to1 = Resource.networkBuilder()
                .id("n1")
                .variantNum(targetVariantNum1)
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(targetVariantId1)
                        .cloneStrategy(CloneStrategy.PARTIAL)
                        .fullVariantNum(-1)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(n1Clone0to1))))
                .andRespond(withSuccess());
        Resource<NetworkAttributes> n1Clone1to2 = Resource.networkBuilder()
                .id("n1")
                .variantNum(targetVariantNum2)
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(targetVariantId2)
                        .cloneStrategy(CloneStrategy.PARTIAL)
                        .fullVariantNum(1)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(n1Clone1to2))))
                .andRespond(withSuccess());
        Resource<NetworkAttributes> n1Clone2to3 = Resource.networkBuilder()
                .id("n1")
                .variantNum(targetVariantNum3)
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(targetVariantId3)
                        .cloneStrategy(CloneStrategy.PARTIAL)
                        .fullVariantNum(1)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(n1Clone2to3))))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }
}
