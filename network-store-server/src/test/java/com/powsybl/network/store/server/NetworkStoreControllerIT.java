/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SubstationAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;
import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static com.powsybl.network.store.model.NetworkStoreApi.VERSION;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(NetworkStoreController.class)
@ContextConfiguration(classes = {NetworkStoreApplication.class, CassandraConfig.class, NetworkStoreRepository.class})
@TestExecutionListeners(listeners = CustomCassandraUnitTestExecutionListener.class,
                        mergeMode = MERGE_WITH_DEFAULTS)
@CassandraDataSet(value = "iidm.cql", keyspace = CassandraConstants.KEYSPACE_IIDM)
@EmbeddedCassandra(timeout = 30000L)
public class NetworkStoreControllerIT {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    // This method is provided to avoid timeout when dropping tables
    @Before
    public void initialize() {
        EmbeddedCassandraServerHelper.getCluster().getConfiguration().getSocketOptions().setReadTimeoutMillis(60000);
    }

    @Test
    public void test() throws Exception {
        mvc.perform(get("/" + VERSION + "/networks")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("{data: []}"));

        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        mvc.perform(get("/" + VERSION + "/networks/" + networkUuid)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Resource<NetworkAttributes> foo = Resource.networkBuilder()
                .id("foo")
                .attributes(NetworkAttributes.builder()
                                             .uuid(networkUuid)
                                             .caseDate(DateTime.parse("2015-01-01T00:00:00.000Z"))
                                             .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(foo))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + networkUuid)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").value("foo"));

        mvc.perform(get("/" + VERSION + "/networks/" + networkUuid + "/substations")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("{data: []}"));

        Resource<SubstationAttributes> bar = Resource.substationBuilder().id("bar")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .tso("RTE")
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + networkUuid + "/substations")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(bar))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + networkUuid + "/substations/bar")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].id").value("bar"))
                .andExpect(jsonPath("data[0].attributes.country").value("FR"))
                .andExpect(jsonPath("data[0].attributes.tso").value("RTE"));

        Resource<SubstationAttributes> bar2 = Resource.substationBuilder()
                .id("bar2")
                .attributes(SubstationAttributes.builder()
                        .country(Country.BE)
                        .tso("ELIA")
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + networkUuid + "/substations")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(bar2))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + networkUuid + "/substations")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("meta.totalCount").value("2"))
                .andExpect(jsonPath("data", hasSize(2)))
                .andExpect(jsonPath("data[0].id").value("bar"))
                .andExpect(jsonPath("data[0].attributes.country").value("FR"))
                .andExpect(jsonPath("data[0].attributes.tso").value("RTE"))
                .andExpect(jsonPath("data[1].id").value("bar2"))
                .andExpect(jsonPath("data[1].attributes.country").value("BE"))
                .andExpect(jsonPath("data[1].attributes.tso").value("ELIA"));

        mvc.perform(get("/" + VERSION + "/networks/" + networkUuid + "/substations?limit=1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("meta.totalCount").value("2"))
                .andExpect(jsonPath("data", hasSize(1)));

        Resource<VoltageLevelAttributes> baz = Resource.voltageLevelBuilder()
                .id("baz")
                .attributes(VoltageLevelAttributes.builder()
                        .substationId("bar")
                        .nominalV(380)
                        .lowVoltageLimit(360)
                        .highVoltageLimit(400)
                        .topologyKind(TopologyKind.NODE_BREAKER)
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + networkUuid + "/voltage-levels")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(baz))))
                .andExpect(status().isCreated());
        Resource<VoltageLevelAttributes> baz2 = Resource.voltageLevelBuilder()
                .id("baz2")
                .attributes(VoltageLevelAttributes.builder()
                        .substationId("bar2")
                        .nominalV(382)
                        .lowVoltageLimit(362)
                        .highVoltageLimit(402)
                        .topologyKind(TopologyKind.NODE_BREAKER)
                        .build())
                .build();
        mvc.perform(post("/" + VERSION + "/networks/" + networkUuid + "/voltage-levels")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singleton(baz2))))
                .andExpect(status().isCreated());

        mvc.perform(get("/" + VERSION + "/networks/" + networkUuid + "/substations/bar/voltage-levels")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("meta.totalCount").value("1"))
                .andExpect(jsonPath("data", hasSize(1)));

        mvc.perform(get("/" + VERSION + "/networks/" + networkUuid + "/voltage-levels")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("data", hasSize(2)));
    }
}
