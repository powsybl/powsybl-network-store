/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.network.store.model.*;
import jakarta.annotation.PostConstruct;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest
public class BufferedNetworkStoreClientTest {

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
        public RestClient testClient(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
            return new RestClientImpl(restTemplateBuilder, objectMapper);
        }
    }

    @PostConstruct
    public void configureObjectMapper() {
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
    }

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
        UUID networkUuid = UUID.randomUUID();
        int targetVariantNum1 = 1;
        String targetVariantId1 = "variant1";
        int targetVariantNum2 = 2;
        String targetVariantId2 = "variant2";
        // Update network n1
        Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        bufferedClient.updateNetworks(List.of(n1), null);
        // Partial clone 0 -> 1
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
        // Update again network n1 after clones, it should only update this network, not the clones
        n1.getAttributes().setCaseDate(ZonedDateTime.parse("2018-01-01T00:00:00.000Z"));
        // Flush and check that all the networks are also updated with correct targetVariantId, fullVariantNum
        Resource<NetworkAttributes> n1UpdatedAfterClone = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2018-01-01T00:00:00.000Z"))
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
                        .fullVariantNum(0)
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
                        .fullVariantNum(0)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(n1Clone1to2))))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }

    @Test
    public void testUpdateLine() throws JsonProcessingException {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.randomUUID();
        LineAttributes lineAttributes = LineAttributes.builder()
                .p1(1)
                .p2(2)
                .q1(3)
                .q2(4)
                .r(5)
                .x(6)
                .operationalLimitsGroups1(Map.of("group1", new OperationalLimitsGroupAttributes()))
                .build();
        Resource<LineAttributes> l1 = Resource.lineBuilder()
                .id("LINE_1")
                .attributes(lineAttributes)
                .build();
        Resource<LineAttributes> l1Copy = Resource.lineBuilder()
                .id("LINE_1")
                .attributes(lineAttributes)
                .build();
        l1Copy.setFilter(AttributeFilter.WITH_LIMITS);
        // test only sv filter
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines/sv"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"LINE\",\"id\":\"LINE_1\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}}]"))
                .andRespond(withSuccess());
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.SV);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test sv then with limits filter -> should apply with limits
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(l1Copy))))
                .andRespond(withSuccess());
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.SV);
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.WITH_LIMITS);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test basic then sv filter -> should apply null (without limits)
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                // no operational limits group in request
                .andExpect(content().string("[{\"type\":\"LINE\",\"id\":\"LINE_1\",\"variantNum\":0,\"filter\":\"BASIC\",\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"r\":5.0,\"x\":6.0,\"g1\":0.0,\"b1\":0.0,\"g2\":0.0,\"b2\":0.0,\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0,\"regulatingEquipments\":[]}}]"))
                .andRespond(withSuccess());
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.SV);
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.BASIC);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test sv then basic then with limits filter -> should apply with limits filter
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(l1Copy))))
                .andRespond(withSuccess());
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.SV);
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.BASIC);
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.WITH_LIMITS);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }

    @Test
    public void testViewWithUpdateLines() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.randomUUID();
        LineAttributes lineAttributes = LineAttributes.builder()
                .p1(1)
                .p2(2)
                .q1(3)
                .q2(4)
                .r(5)
                .x(6)
                .operationalLimitsGroups1(Map.of("group1", new OperationalLimitsGroupAttributes()))
                .build();
        Resource<LineAttributes> l1 = Resource.lineBuilder()
                .id("LINE_1")
                .attributes(lineAttributes)
                .build();
        Resource<LineAttributes> l2 = Resource.lineBuilder()
                .id("LINE_2")
                .attributes(lineAttributes)
                .build();
        // test only sv filter
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines/sv"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"LINE\",\"id\":\"LINE_1\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}},{\"type\":\"LINE\",\"id\":\"LINE_2\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}}]"))
                .andRespond(withSuccess());
        bufferedClient.updateLines(networkUuid, List.of(l1, l2), AttributeFilter.SV);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }

    @Test
    public void testViewWithUpdateTwoWindingsTransformer() throws JsonProcessingException {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.randomUUID();
        TwoWindingsTransformerAttributes twoWindingsTransformerAttributes = TwoWindingsTransformerAttributes.builder()
                .p1(1)
                .p2(2)
                .q1(3)
                .q2(4)
                .r(5)
                .x(6)
                .selectedOperationalLimitsGroupId1("selectedGroupId1")
                .operationalLimitsGroups1(Map.of("group1", new OperationalLimitsGroupAttributes()))
                .regulatingEquipments(Set.of(new RegulatingEquipmentIdentifier("loadId", ResourceType.LOAD)))
                .build();
        Resource<TwoWindingsTransformerAttributes> twt1 = Resource.twoWindingsTransformerBuilder()
                .id("TWT_1")
                .attributes(twoWindingsTransformerAttributes)
                .build();
        Resource<TwoWindingsTransformerAttributes> twt2 = Resource.twoWindingsTransformerBuilder()
                .id("TWT_2")
                .attributes(twoWindingsTransformerAttributes)
                .build();
        // test sv filter
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/2-windings-transformers/sv"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"id\":\"TWT_1\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}},{\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"id\":\"TWT_2\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}}]"))
                .andRespond(withSuccess());
        bufferedClient.updateTwoWindingsTransformers(networkUuid, List.of(twt1, twt2), AttributeFilter.SV);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test basic filter
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/2-windings-transformers"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"id\":\"TWT_1\",\"variantNum\":0,\"filter\":\"BASIC\",\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"r\":5.0,\"x\":6.0,\"g\":0.0,\"b\":0.0,\"ratedU1\":0.0,\"ratedU2\":0.0,\"ratedS\":0.0,\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0,\"selectedOperationalLimitsGroupId1\":\"selectedGroupId1\",\"regulatingEquipments\":[{\"equipmentId\":\"loadId\",\"resourceType\":\"LOAD\",\"regulatingTapChangerType\":\"NONE\"}]}},{\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"id\":\"TWT_2\",\"variantNum\":0,\"filter\":\"BASIC\",\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"r\":5.0,\"x\":6.0,\"g\":0.0,\"b\":0.0,\"ratedU1\":0.0,\"ratedU2\":0.0,\"ratedS\":0.0,\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0,\"selectedOperationalLimitsGroupId1\":\"selectedGroupId1\",\"regulatingEquipments\":[{\"equipmentId\":\"loadId\",\"resourceType\":\"LOAD\",\"regulatingTapChangerType\":\"NONE\"}]}}]"))
                .andRespond(withSuccess());
        bufferedClient.updateTwoWindingsTransformers(networkUuid, List.of(twt1, twt2), AttributeFilter.BASIC);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test with_limits filter
        twt1.setFilter(AttributeFilter.WITH_LIMITS);
        twt2.setFilter(AttributeFilter.WITH_LIMITS);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/2-windings-transformers"))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(twt1, twt2))))
                .andRespond(withSuccess());
        bufferedClient.updateTwoWindingsTransformers(networkUuid, List.of(twt1, twt2), AttributeFilter.WITH_LIMITS);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }
}
