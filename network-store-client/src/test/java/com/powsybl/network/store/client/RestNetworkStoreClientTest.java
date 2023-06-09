/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import com.powsybl.network.store.model.ErrorObject;

import org.hamcrest.Matchers;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RunWith(SpringRunner.class)
@RestClientTest(RestClient.class)
@ContextConfiguration(classes = RestClientImpl.class)
public class RestNetworkStoreClientTest {

    private static final String VARIANT1 = "variant1";

    @Autowired
    private RestClient restClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    @Before
    public void setUp() throws IOException {
        Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                                             .uuid(networkUuid)
                                             .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                                             .caseDate(DateTime.parse("2015-01-01T00:00:00.000Z"))
                                             .build())
                .build();

        UUID clonedNetworkUuid = UUID.fromString("2c28af2e-286c-4cb2-a5fc-a82cd4d40631");
        Resource<NetworkAttributes> n2 = Resource.networkBuilder()
                .id("n2")
                .attributes(NetworkAttributes.builder()
                        .uuid(clonedNetworkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(DateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();

        server.expect(requestTo("/networks"))
                .andExpect(method(GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(List.of(new NetworkInfos(networkUuid, "n1"))), MediaType.APPLICATION_JSON));

        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM))
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

        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/substations"))
                .andExpect(method(GET))
                .andRespond(withSuccess(substationsJson, MediaType.APPLICATION_JSON));

        // voltage level
        List<InternalConnectionAttributes> ics = new ArrayList<>();
        ics.add(InternalConnectionAttributes.builder()
                .node1(10)
                .node2(20)
                .build());

        Resource<VoltageLevelAttributes> vl = Resource.voltageLevelBuilder()
                .id("vl1")
                .attributes(VoltageLevelAttributes.builder()
                        .substationId("s1")
                        .nominalV(380)
                        .lowVoltageLimit(360)
                        .highVoltageLimit(400)
                        .topologyKind(TopologyKind.NODE_BREAKER)
                        .internalConnections(ics)
                        .build())
                .build();

        String voltageLevelsJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(vl)));

        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/substations/s1/voltage-levels"))
                .andExpect(method(GET))
                .andRespond(withSuccess(voltageLevelsJson, MediaType.APPLICATION_JSON));

        // switch
        Resource<SwitchAttributes> breaker = Resource.switchBuilder()
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

        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/switches"))
                .andExpect(method(GET))
                .andRespond(withSuccess(breakersJson, MediaType.APPLICATION_JSON));

        server.expect(requestTo("/networks/" + networkUuid))
                .andExpect(method(GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(List.of(new VariantInfos(VariantManagerConstants.INITIAL_VARIANT_ID, Resource.INITIAL_VARIANT_NUM))), MediaType.APPLICATION_JSON));

        // line
        Resource<LineAttributes> line = Resource.lineBuilder()
                .id("idLine")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("idLine")
                        .node1(1)
                        .node2(1)
                        .bus1("bus1")
                        .bus2("bus2")
                        .r(1)
                        .x(1)
                        .g1(1)
                        .b1(1)
                        .g2(1)
                        .b2(1)
                        .p1(0)
                        .q1(0)
                        .p2(0)
                        .q2(0)
                        .build())
                .build();

        String linesJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(line)));

        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(linesJson, MediaType.APPLICATION_JSON));

        server.expect(requestTo("/networks/" + networkUuid + "/" + VariantManagerConstants.INITIAL_VARIANT_ID + "/toId/" + VARIANT1 + "?mayOverwrite=false"))
                .andExpect(method(PUT))
                .andRespond(withSuccess());

        String errorExistingJson = objectMapper
                .writeValueAsString(TopLevelError.of(ErrorObject.cloneOverExisting(VARIANT1)));
        server.expect(requestTo("/networks/" + networkUuid + "/" + VariantManagerConstants.INITIAL_VARIANT_ID + "/toId/" + VARIANT1 + "?mayOverwrite=false"))
                .andExpect(method(PUT))
                .andRespond(withBadRequest().body(errorExistingJson).contentType(MediaType.APPLICATION_JSON));

        server.expect(requestTo("/networks/" + networkUuid + "/" + VariantManagerConstants.INITIAL_VARIANT_ID + "/toId/" + VARIANT1 + "?mayOverwrite=true"))
                .andExpect(method(PUT))
                .andRespond(withSuccess());

        String errorInitialJson = objectMapper.writeValueAsString(TopLevelError.of(ErrorObject.cloneOverInitialForbidden()));
        server.expect(requestTo("/networks/" + networkUuid + "/" + VARIANT1 + "/toId/" + VariantManagerConstants.INITIAL_VARIANT_ID + "?mayOverwrite=true"))
                .andExpect(method(PUT))
                .andRespond(withBadRequest().body(errorInitialJson).contentType(MediaType.APPLICATION_JSON));

        server.expect(requestTo(Matchers.matchesPattern("/networks/.*\\?duplicateFrom=7928181c-7977-4592-ba19-88027e4254e4&targetVariantIds=" + VariantManagerConstants.INITIAL_VARIANT_ID)))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        server.expect(requestTo(Matchers.matchesPattern("/networks/.*/" + Resource.INITIAL_VARIANT_NUM)))
                .andExpect(method(GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(TopLevelDocument.of(n2)), MediaType.APPLICATION_JSON));

        //Tie line
        Resource<TieLineAttributes> tieLine = Resource.tieLineBuilder()
                .id("tieLine1")
                .attributes(TieLineAttributes.builder()
                        .name("tieLine1")
                        .danglingLine1Id("dl1")
                        .danglingLine2Id("dl2")
                        .build())
                .build();

        String tieLineJson = objectMapper.writeValueAsString(TopLevelDocument.of(ImmutableList.of(tieLine)));

        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/tie-lines"))
                .andExpect(method(GET))
                .andRespond(withSuccess(tieLineJson, MediaType.APPLICATION_JSON));

        server.expect(requestTo("/networks/" + networkUuid + "/" + VariantManagerConstants.INITIAL_VARIANT_ID + "/toId/" + VARIANT1 + "?mayOverwrite=false"))
                .andExpect(method(PUT))
                .andRespond(withSuccess());
    }

    @Test
    public void test() {
        try (NetworkStoreService service = new NetworkStoreService(restClient, PreloadingStrategy.NONE)) {
            assertEquals(Collections.singletonMap(networkUuid, "n1"), service.getNetworkIds());
            Network network = service.getNetwork(networkUuid);
            assertEquals("n1", network.getId());
            assertEquals(UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4"), service.getNetworkUuid(network));
            List<Substation> substations = network.getSubstationStream().collect(Collectors.toList());
            assertEquals(1, substations.size());
            assertEquals("s1", substations.get(0).getId());

            // voltage level
            List<VoltageLevel> voltageLevels = substations.get(0).getVoltageLevelStream().collect(Collectors.toList());
            assertEquals(1, voltageLevels.size());
            assertEquals("vl1", voltageLevels.get(0).getId());

            // switch
            List<Switch> switches = network.getSwitchStream().collect(Collectors.toList());
            assertEquals(1, switches.size());
            assertEquals("b1", switches.get(0).getId());
            assertEquals(Boolean.FALSE, switches.get(0).isOpen());

            switches.get(0).setOpen(true);  // opening the switch

            switches = network.getSwitchStream().collect(Collectors.toList());
            assertEquals(1, switches.size());
            assertEquals("b1", switches.get(0).getId());
            assertEquals(Boolean.TRUE, switches.get(0).isOpen());

            // line
            List<Line> lines = network.getLineStream().collect(Collectors.toList());
            assertEquals(1, lines.size());
            assertEquals("idLine", lines.get(0).getId());
            assertEquals(0., lines.get(0).getTerminal1().getP(), 0.);

            lines.get(0).getTerminal1().setP(100.);  // set terminal1 P value

            lines = network.getLineStream().collect(Collectors.toList());
            assertEquals(1, lines.size());
            assertEquals("idLine", lines.get(0).getId());
            assertEquals(100., lines.get(0).getTerminal1().getP(), 0.);

            service.cloneVariant(networkUuid, VariantManagerConstants.INITIAL_VARIANT_ID, VARIANT1);
            PowsyblException e1 = assertThrows(PowsyblException.class, () -> service.cloneVariant(networkUuid, VariantManagerConstants.INITIAL_VARIANT_ID, VARIANT1));
            assertTrue(e1.getMessage().contains("already exists"));
            service.cloneVariant(networkUuid, VariantManagerConstants.INITIAL_VARIANT_ID, VARIANT1, true);
            PowsyblException e2 = assertThrows(PowsyblException.class, () -> service.cloneVariant(networkUuid, VARIANT1, VariantManagerConstants.INITIAL_VARIANT_ID, true));
            assertTrue(e2.getMessage().contains("forbidden"));

            //duplicate network
            Network clonedNetwork = service.cloneNetwork(UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4"), List.of(VariantManagerConstants.INITIAL_VARIANT_ID));
            UUID clonedNetworkUuid = service.getNetworkUuid(clonedNetwork);

            assertNotNull(clonedNetworkUuid);

            //Tie lines
            List<TieLine> tieLines = network.getTieLineStream().collect(Collectors.toList());
            assertEquals(1, tieLines.size());

            tieLines.get(0).setName("tieLine2");

            tieLines = network.getTieLineStream().collect(Collectors.toList());
            assertEquals(1, tieLines.size());
            assertEquals("tieLine2", tieLines.get(0).getNameOrId());
        }
    }
}
