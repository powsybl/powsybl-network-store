/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Jon harper <jon.harper at rte-france.com>
 *
 * Verify that our production code that will be using component scan
 * in the server using this library uses a resttemplate with a prefix set.
 * We test only getNetworkIds and assume that NetworkStoreService correctly
 * uses the same restclient in all its usages (pass it to the network,
 * other methods in networkstoreservice, etc)
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestClient.class)
@ContextConfiguration(classes = RestClientImpl.class)
public class NetworkStoreClientPrefixTest {

    @Autowired
    private RestClient restClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DEFAULT_PREFIX = "http://network-store-server/v1";

    @Test
    public void testGetIds() throws JsonProcessingException {
        try (NetworkStoreService service = new NetworkStoreService(restClient, PreloadingStrategy.NONE)) {
            // Is there a way to avoid stubbing the response (we only want to test the request?)
            server.expect(requestTo(DEFAULT_PREFIX + "/networks"))
                .andExpect(method(GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(List.of()), MediaType.APPLICATION_JSON));
            service.getNetworkIds();
            server.verify();
        }
    }
}
