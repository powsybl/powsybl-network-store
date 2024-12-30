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
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.CloneStrategy;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TopLevelDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestClient.class)
@ContextConfiguration(classes = RestClientImpl.class)
public class NetworkStoreServiceTest {

    @Autowired
    private RestClient restClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    @Test
    public void checkNetworkIsUpdatedWhenSettingCloneStrategy() throws JsonProcessingException {
        try (NetworkStoreService service = new NetworkStoreService(restClient, PreloadingStrategy.NONE)) {
            Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                    .id("n1")
                    .attributes(NetworkAttributes.builder()
                            .uuid(networkUuid)
                            .cloneStrategy(CloneStrategy.PARTIAL)
                            .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                            .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                            .build())
                    .build();

            server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM))
                    .andExpect(method(GET))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(TopLevelDocument.of(n1)), MediaType.APPLICATION_JSON));
            NetworkImpl network = (NetworkImpl) service.getNetwork(networkUuid);
            assertEquals("n1", network.getId());
            server.verify();
            server.reset();

            Resource<NetworkAttributes> n1UpdatedCloneStrategy = Resource.networkBuilder()
                    .id("n1")
                    .attributes(NetworkAttributes.builder()
                            .uuid(networkUuid)
                            .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                            .cloneStrategy(CloneStrategy.FULL)
                            .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                            .build())
                    .build();
            server.expect(requestTo("/networks/" + networkUuid))
                    .andExpect(method(PUT))
                    .andExpect(content().string(objectMapper.writeValueAsString(List.of(n1UpdatedCloneStrategy))))
                    .andRespond(withSuccess());
            service.setCloneStrategy(network, CloneStrategy.FULL);
            server.verify();
            server.reset();

            assertEquals(CloneStrategy.FULL, network.getResource().getAttributes().getCloneStrategy());
        }
    }
}
