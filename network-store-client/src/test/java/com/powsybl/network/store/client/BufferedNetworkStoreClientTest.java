/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
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
        bufferedClient.updateNetworks(List.of(n1), AttributeFilter.PRIMARY_AS_NULL);
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
        // test only sv filter
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.SV);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines/sv"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"LINE\",\"id\":\"LINE_1\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}}]"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test sv then with limits filter -> should apply with limits
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.SV);
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.LIMITS);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(l1Copy))))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test primary then sv filter -> should apply primary (without limits)
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.SV);
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.PRIMARY_AS_NULL);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                // no operational limits group in request, no filter field (PRIMARY)
                .andExpect(content().string("[{\"type\":\"LINE\",\"id\":\"LINE_1\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"r\":5.0,\"x\":6.0,\"g1\":0.0,\"b1\":0.0,\"g2\":0.0,\"b2\":0.0,\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0,\"regulatingEquipments\":[]}}]"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test sv then primary then with limits filter -> should apply with limits filter
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.SV);
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.PRIMARY_AS_NULL);
        bufferedClient.updateLines(networkUuid, List.of(l1), AttributeFilter.LIMITS);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(l1Copy))))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
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
        bufferedClient.updateLines(networkUuid, List.of(l1, l2), AttributeFilter.SV);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines/sv"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"LINE\",\"id\":\"LINE_1\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}},{\"type\":\"LINE\",\"id\":\"LINE_2\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}}]"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
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
        bufferedClient.updateTwoWindingsTransformers(networkUuid, List.of(twt1, twt2), AttributeFilter.SV);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/2-windings-transformers/sv"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"id\":\"TWT_1\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}},{\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"id\":\"TWT_2\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}}]"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test primary filter
        bufferedClient.updateTwoWindingsTransformers(networkUuid, List.of(twt1, twt2), AttributeFilter.PRIMARY_AS_NULL);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/2-windings-transformers"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"id\":\"TWT_1\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"r\":5.0,\"x\":6.0,\"g\":0.0,\"b\":0.0,\"ratedU1\":0.0,\"ratedU2\":0.0,\"ratedS\":0.0,\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0,\"selectedOperationalLimitsGroupId1\":\"selectedGroupId1\",\"regulatingEquipments\":[{\"equipmentId\":\"loadId\",\"resourceType\":\"LOAD\",\"regulatingTapChangerType\":\"NONE\"}]}},{\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"id\":\"TWT_2\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"r\":5.0,\"x\":6.0,\"g\":0.0,\"b\":0.0,\"ratedU1\":0.0,\"ratedU2\":0.0,\"ratedS\":0.0,\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0,\"selectedOperationalLimitsGroupId1\":\"selectedGroupId1\",\"regulatingEquipments\":[{\"equipmentId\":\"loadId\",\"resourceType\":\"LOAD\",\"regulatingTapChangerType\":\"NONE\"}]}}]"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();

        // test with_limits filter
        bufferedClient.updateTwoWindingsTransformers(networkUuid, List.of(twt1, twt2), AttributeFilter.LIMITS);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/2-windings-transformers"))
                .andExpect(method(PUT))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(twt1, twt2))))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
    }

    @Test
    public void testUpdateAllWithAttributeFilter() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.randomUUID();
        LoadAttributes loadAttributes = new LoadAttributes();
        loadAttributes.setP(200);
        loadAttributes.setQ(-200);
        Resource<LoadAttributes> loadResource = Resource.create(ResourceType.LOAD, "loadId", 0, loadAttributes);
        List<Resource<LoadAttributes>> loadResources = List.of(loadResource);
        bufferedClient.updateLoads(networkUuid, loadResources, AttributeFilter.SV);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/loads/sv"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"LOAD\",\"id\":\"loadId\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p\":200.0,\"q\":-200.0}}]"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        assertNull(loadResources.getFirst().getFilter());
        server.reset();

        // the current production code does not depend on this behavior but test it to know if it changes
        // to avoid risks if the production starts depending on it
        loadResource = new Resource<>(ResourceType.LOAD, "loadId", 0, AttributeFilter.SV, loadAttributes);
        loadResources = List.of(loadResource);
        bufferedClient.updateLoads(networkUuid, loadResources, AttributeFilter.SV);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/loads/sv"))
                .andExpect(method(PUT))
                .andExpect(content().string("[{\"type\":\"LOAD\",\"id\":\"loadId\",\"variantNum\":0,\"filter\":\"SV\",\"attributes\":{\"p\":200.0,\"q\":-200.0}}]"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
        assertEquals(AttributeFilter.SV, loadResources.getFirst().getFilter());
    }

    @Test
    public void testRemoveOperationalLimitsGroupWithBuffer() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String branchId = "LINE";
        String operationalLimitsGroupId = "toRemove1";
        String operationalLimitsGroupId2 = "toRemove2";
        String operationalLimitsGroupId3 = "toRemove3";

        // remove three operational limits group without the cache will call only the server once
        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId))));
        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId2))));
        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(2, Set.of(operationalLimitsGroupId3))));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                        + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup"))
                .andExpect(method(DELETE))
                .andExpect(content().string("{\"LINE\":{\"1\":[\"toRemove2\",\"toRemove1\"],\"2\":[\"toRemove3\"]}}"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
    }

    @Test
    public void testRemoveOLGThenUpdatingLine() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String branchId = "LINE";
        String operationalLimitsGroupId = "toKeep";
        String operationalLimitsGroupId2 = "toRemove2";
        OperationalLimitsGroupAttributes operationalLimitsGroupAttributes1 = OperationalLimitsGroupAttributes.builder()
                .id(operationalLimitsGroupId)
                .build();
        OperationalLimitsGroupAttributes operationalLimitsGroupAttributes2 = OperationalLimitsGroupAttributes.builder()
                .id(operationalLimitsGroupId2)
                .build();
        Resource<LineAttributes> line1 = Resource.lineBuilder()
                .id(branchId)
                .variantNum(0)
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("VL_1")
                        .voltageLevelId2("VL_2")
                        .operationalLimitsGroups1(Map.of(operationalLimitsGroupId, operationalLimitsGroupAttributes1, operationalLimitsGroupId2, operationalLimitsGroupAttributes2))
                        .build())
                .build();

        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId2))));
        bufferedClient.updateLines(networkUuid, List.of(line1), AttributeFilter.LIMITS);
        // remove operational limits group and then recreate it in line update will not
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                .andExpect(content().json("[{\"type\":\"LINE\",\"id\":\"LINE\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"voltageLevelId1\":\"VL_1\",\"voltageLevelId2\":\"VL_2\",\"r\":0.0,\"x\":0.0,\"g1\":0.0,\"b1\":0.0,\"g2\":0.0,\"b2\":0.0,\"p1\":\"NaN\",\"q1\":\"NaN\",\"p2\":\"NaN\",\"q2\":\"NaN\",\"operationalLimitsGroups1\":{\"toRemove2\":{\"id\":\"toRemove2\"},\"toKeep\":{\"id\":\"toKeep\"}},\"operationalLimitsGroups2\":{},\"regulatingEquipments\":[]}}]"))
                .andRespond(withStatus(HttpStatus.OK));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                        + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup"))
                .andExpect(method(DELETE));
        bufferedClient.flush(networkUuid);
        server.verify();
    }

    @Test
    public void testRemoveExtensionThenUpdatingGenerator() throws JsonProcessingException {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String generatorId = "GEN";
        Resource<GeneratorAttributes> generator = Resource.generatorBuilder()
                .id(generatorId)
                .variantNum(0)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("VL_1")
                        .extensionAttributes(Map.of(ActivePowerControl.NAME, ActivePowerControlAttributes.builder().build()))
                        .build())
                .build();
        // updating lines and then removing olg
        bufferedClient.removeExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, Map.of(generatorId, Set.of(ActivePowerControl.NAME)));
        bufferedClient.updateGenerators(networkUuid, List.of(generator), null);
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/generators"))
                .andExpect(method(PUT))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(generator))))
                .andRespond(withStatus(HttpStatus.OK));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(DELETE));
        bufferedClient.flush(networkUuid);
        server.verify();
    }

    @Test
    public void testRemoveExtensionWithBuffer() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String generator1 = "GEN1";
        String generator2 = "GEN2";
        // remove three operational limits group without the cache will call only the server once
        bufferedClient.removeExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, Map.of(generator1, Set.of(ActivePowerControl.NAME)));
        bufferedClient.removeExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, Map.of(generator2, Set.of(ActivePowerControl.NAME),
                generator1, Set.of(CoordinatedReactiveControl.NAME)));
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(DELETE))
                .andExpect(content().json("{\"GEN1\":[\"coordinatedReactiveControl\", \"activePowerControl\"],\"GEN2\":[\"activePowerControl\"]}"))
                .andRespond(withSuccess());
        bufferedClient.flush(networkUuid);
        server.verify();
    }

    @Test
    public void testRemoveExtensionThenGeneratorContainingIt() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String generator1 = "GEN1";
        bufferedClient.removeExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, Map.of(generator1, Set.of(ActivePowerControl.NAME)));
        bufferedClient.removeGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, List.of(generator1));
        // remove three operational limits group without the cache will call only the server once
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/generators"))
                .andExpect(method(DELETE))
                .andRespond(withStatus(HttpStatus.OK));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(DELETE));
        bufferedClient.flush(networkUuid);
        server.verify();
    }

    @Test
    public void testRemoveOlgThenLineContaining() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String branchId = "LINE";
        String operationalLimitsGroupId2 = "olg2";

        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId2))));
        bufferedClient.removeLines(networkUuid, Resource.INITIAL_VARIANT_NUM, List.of(branchId));

        // removing olg and then removing the line will only remove the line (because it contains the olg)
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines"))
                .andExpect(method(DELETE))
                .andRespond(withStatus(HttpStatus.OK));
        server.expect(ExpectedCount.never(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                        + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup"))
                .andExpect(method(DELETE));
        bufferedClient.flush(networkUuid);
        server.verify();
    }
}
