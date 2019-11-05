/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SubstationAttributes;
import com.powsybl.network.store.model.TopLevelDocument;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestNetworkStoreClient.class)
@ContextConfiguration(classes = RestNetworkStoreClient.class)
public class RestNetworkStoreClientTest {

    @Autowired
    private RestNetworkStoreClient restStoreClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws IOException {
        Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                                              .caseDate(DateTime.parse("2015-01-01T00:00:00.000Z"))
                                              .build())
                .build();

        server.expect(requestTo("/networks"))
                .andExpect(method(GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(n1))), MediaType.APPLICATION_JSON));

        server.expect(requestTo("/networks/n1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(TopLevelDocument.of(n1)), MediaType.APPLICATION_JSON));

        Resource<SubstationAttributes> s1 = Resource.substationBuilder()
                .id("s1")
                .attributes(SubstationAttributes.builder()
                                                    .country(Country.FR)
                                                    .tso("RTE")
                                                    .build())
                .build();
        String substationsJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(s1)));

        server.expect(requestTo("/networks/n1/substations"))
                .andExpect(method(GET))
                .andRespond(withSuccess(substationsJson, MediaType.APPLICATION_JSON));

        server.expect(requestTo("/networks/n1/substations"))
                .andExpect(method(POST))
                .andRespond(withSuccess());
    }

    @Test
    public void test() {
        try (NetworkStoreService service = new NetworkStoreService(restStoreClient, PreloadingStrategy.NONE)) {
            assertEquals(Collections.singletonList("n1"), service.getNetworkIds());
            Network network = service.getNetwork("n1");
            assertEquals("n1", network.getId());
            List<Substation> substations = network.getSubstationStream().collect(Collectors.toList());
            assertEquals(1, substations.size());
            assertEquals("s1", substations.get(0).getId());
        }
    }
}
