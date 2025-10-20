/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.network.store.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
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

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestClient.class)
@ContextConfiguration(classes = RestClientImpl.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

    @BeforeEach
    public void beforeEach() {
        server.reset();
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
    public void testRemoveOperationalLimitsGroupWithBuffer() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String branchId = "LINE";
        String operationalLimitsGroupId = "toRemove1";
        String operationalLimitsGroupId2 = "toRemove2";
        String operationalLimitsGroupId3 = "toRemove3";

        // remove three operational limits group without the cache will call only the server once
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                        + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup"))
                .andExpect(method(DELETE))
                .andExpect(content().string("{\"LINE\":{\"1\":[\"toRemove2\",\"toRemove1\"],\"2\":[\"toRemove3\"]}}"))
                .andRespond(withSuccess());
        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId))));
        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId2))));
        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(2, Set.of(operationalLimitsGroupId3))));
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }

    @Test
    public void testRemoveOLGAndUpdatingLine() {
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

        // remove operational limits group and then recreate it in line update will not
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                .andExpect(content().json("[{\"type\":\"LINE\",\"id\":\"LINE\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"voltageLevelId1\":\"VL_1\",\"voltageLevelId2\":\"VL_2\",\"r\":0.0,\"x\":0.0,\"g1\":0.0,\"b1\":0.0,\"g2\":0.0,\"b2\":0.0,\"p1\":\"NaN\",\"q1\":\"NaN\",\"p2\":\"NaN\",\"q2\":\"NaN\",\"operationalLimitsGroups1\":{\"toRemove2\":{\"id\":\"toRemove2\"},\"toKeep\":{\"id\":\"toKeep\"}},\"operationalLimitsGroups2\":{},\"regulatingEquipments\":[]}}]"))
                .andRespond(withStatus(HttpStatus.OK));
        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId2))));
        bufferedClient.updateLines(networkUuid, List.of(line1), null);
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }

    @Test
    public void testUpdatingLineAndRemoveOLG() {
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

        // updating lines and then removing olg
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM
                        + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup"))
                .andExpect(method(DELETE))
                .andExpect(content().json("{\"LINE\":{\"1\":[\"toRemove2\"]}}"))
                .andRespond(withSuccess());
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/lines"))
                .andExpect(method(PUT))
                .andExpect(content().json("[{\"type\":\"LINE\",\"id\":\"LINE\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"voltageLevelId1\":\"VL_1\",\"voltageLevelId2\":\"VL_2\",\"r\":0.0,\"x\":0.0,\"g1\":0.0,\"b1\":0.0,\"g2\":0.0,\"b2\":0.0,\"p1\":\"NaN\",\"q1\":\"NaN\",\"p2\":\"NaN\",\"q2\":\"NaN\",\"operationalLimitsGroups1\":{\"toRemove2\":{\"id\":\"toRemove2\"},\"toKeep\":{\"id\":\"toKeep\"}},\"operationalLimitsGroups2\":{},\"regulatingEquipments\":[]}}]"))
                .andRespond(withStatus(HttpStatus.OK));
        bufferedClient.updateLines(networkUuid, List.of(line1), null);
        bufferedClient.removeOperationalLimitsGroupAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.LINE, Map.of(branchId, Map.of(1, Set.of(operationalLimitsGroupId2))));
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }

    @Test
    public void testRemoveExtensionWithBuffer() {
        BufferedNetworkStoreClient bufferedClient = new BufferedNetworkStoreClient(restStoreClient, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        String generator1 = "GEN1";
        String generator2 = "GEN2";
        // remove three operational limits group without the cache will call only the server once
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(DELETE))
                .andExpect(content().json("{\"coordinatedReactiveControl\":[\"GEN1\"],\"activePowerControl\":[\"GEN1\",\"GEN2\"]}"))
                .andRespond(withSuccess());
        bufferedClient.removeExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, Map.of(ActivePowerControl.NAME, Set.of(generator1)));
        bufferedClient.removeExtensionAttributes(networkUuid, Resource.INITIAL_VARIANT_NUM, ResourceType.GENERATOR, Map.of(ActivePowerControl.NAME, Set.of(generator2), CoordinatedReactiveControl.NAME, Set.of(generator1)));
        bufferedClient.flush(networkUuid);
        server.verify();
        server.reset();
    }
}
