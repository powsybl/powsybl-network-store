/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.model.*;
import org.hamcrest.Matchers;
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
import org.springframework.web.client.ResourceAccessException;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

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

    private final UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    @Before
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule())
            .registerModule(new SimpleModule().addKeyDeserializer(OperationalLimitsGroupIdentifier.class,
                new OperationalLimitsGroupIdentifierDeserializer()))
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    private void setupNetworkStubs() throws JsonProcessingException {
        Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();

        UUID clonedNetworkUuid = UUID.fromString("2c28af2e-286c-4cb2-a5fc-a82cd4d40631");
        Resource<NetworkAttributes> n2 = Resource.networkBuilder()
                .id("n2")
                .attributes(NetworkAttributes.builder()
                        .uuid(clonedNetworkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
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
    }

    @Test
    public void test() throws JsonProcessingException {
        setupNetworkStubs();
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

    @Test
    public void testRemoveAll() {
        RestNetworkStoreClient restNetworkStoreClient = new RestNetworkStoreClient(restClient, objectMapper);

        List<String> ids = List.of("id1", "id2", "id3");

        testDeleteAllByType(ids, "substations", (List<String> identifiableIds) -> restNetworkStoreClient.removeSubstations(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "voltage-levels", (List<String> identifiableIds) -> restNetworkStoreClient.removeVoltageLevels(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "generators", (List<String> identifiableIds) -> restNetworkStoreClient.removeGenerators(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "loads", (List<String> identifiableIds) -> restNetworkStoreClient.removeLoads(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "batteries", (List<String> identifiableIds) -> restNetworkStoreClient.removeBatteries(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "busbar-sections", (List<String> identifiableIds) -> restNetworkStoreClient.removeBusBarSections(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "configured-buses", (List<String> identifiableIds) -> restNetworkStoreClient.removeConfiguredBuses(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "dangling-lines", (List<String> identifiableIds) -> restNetworkStoreClient.removeDanglingLines(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "vsc-converter-stations", (List<String> identifiableIds) -> restNetworkStoreClient.removeVscConverterStations(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "lcc-converter-stations", (List<String> identifiableIds) -> restNetworkStoreClient.removeLccConverterStations(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "lines", (List<String> identifiableIds) -> restNetworkStoreClient.removeLines(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "shunt-compensators", (List<String> identifiableIds) -> restNetworkStoreClient.removeShuntCompensators(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "hvdc-lines", (List<String> identifiableIds) -> restNetworkStoreClient.removeHvdcLines(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "switches", (List<String> identifiableIds) -> restNetworkStoreClient.removeSwitches(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "static-var-compensators", (List<String> identifiableIds) -> restNetworkStoreClient.removeStaticVarCompensators(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "2-windings-transformers", (List<String> identifiableIds) -> restNetworkStoreClient.removeTwoWindingsTransformers(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "3-windings-transformers", (List<String> identifiableIds) -> restNetworkStoreClient.removeThreeWindingsTransformers(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "tie-lines", (List<String> identifiableIds) -> restNetworkStoreClient.removeTieLines(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));
        testDeleteAllByType(ids, "grounds", (List<String> identifiableIds) -> restNetworkStoreClient.removeGrounds(networkUuid, Resource.INITIAL_VARIANT_NUM, identifiableIds));

        server.verify();
    }

    @Test
    public void testRemoveError() {
        RestNetworkStoreClient restNetworkStoreClient = new RestNetworkStoreClient(restClient, objectMapper);
        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/substations"))
                .andExpect(method(DELETE))
                .andExpect(content().string("[\"wrongId\"]"))
                .andRespond(withResourceNotFound());
        List<String> wrongId = List.of("wrongId");
        PowsyblException powsyblException = assertThrows(PowsyblException.class, () -> restNetworkStoreClient.removeSubstations(networkUuid, Resource.INITIAL_VARIANT_NUM, wrongId));
        assertEquals("Fail to delete at /networks/{networkUuid}/{variantNum}/substations, status: 404 NOT_FOUND", powsyblException.getMessage());
        server.verify();
    }

    @Test
    public void testRemoveWithResourceAccessException() {
        RestNetworkStoreClient restNetworkStoreClient = new RestNetworkStoreClient(restClient, objectMapper);
        server.expect(ExpectedCount.times(2), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/substations"))
                .andExpect(method(DELETE))
                .andExpect(content().string("[\"wrongId2\"]"))
                .andRespond(request -> {
                    throw new ResourceAccessException("ResourceAccessException error");
                });
        List<String> wrongId2 = List.of("wrongId2");
        ResourceAccessException httpClientErrorException = assertThrows(ResourceAccessException.class, () -> restNetworkStoreClient.removeSubstations(networkUuid, Resource.INITIAL_VARIANT_NUM, wrongId2));
        assertEquals("ResourceAccessException error", httpClientErrorException.getMessage());
        server.verify();
    }

    private void testDeleteAllByType(List<String> ids, String type, Consumer<List<String>> deleteFunction) {
        String idsStr = String.join("\",\"", ids);
        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/" + type))
                .andExpect(method(DELETE))
                .andExpect(content().string("[\"" + idsStr + "\"]"))
                .andRespond(withSuccess());
        assertDoesNotThrow(() -> deleteFunction.accept(ids));
        server.reset();
    }

    @Test
    public void testRawExtensionAttributes() {
        String identifiableId = "identifiableId";
        String extensionName = "extensionName1";
        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + identifiableId + "/extensions/" + extensionName))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"data\":[{\"extensionName\":\"unknownExtension\",\"attribute1\":5.0}],\"meta\":{}}", MediaType.APPLICATION_JSON));
        RestNetworkStoreClient restNetworkStoreClient = new RestNetworkStoreClient(restClient, objectMapper);
        Optional<ExtensionAttributes> result = restNetworkStoreClient.getExtensionAttributes(networkUuid, 0, ResourceType.NETWORK, identifiableId, extensionName);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testRawExtensionAttributesByIdentifiableId() {
        String identifiableId = "identifiableId";
        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/" + identifiableId + "/extensions"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"activePowerControl\":{\"extensionName\":\"activePowerControl\",\"participate\":true,\"droop\":5.2,\"participationFactor\":0.5,\"minTargetP\":0.0,\"maxTargetP\":0.0},\"unknownExtension\":{\"extensionName\":\"unknownExtension\",\"attribute1\":5.0}}", MediaType.APPLICATION_JSON));

        RestNetworkStoreClient restNetworkStoreClient = new RestNetworkStoreClient(restClient, objectMapper);
        Map<String, ExtensionAttributes> result = restNetworkStoreClient.getAllExtensionsAttributesByIdentifiableId(networkUuid, 0, ResourceType.GENERATOR, identifiableId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("activePowerControl"));
    }

    @Test
    public void testRawExtensionAttributesByResourceTypeAndExtensionName() {
        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions/" + ActivePowerControl.NAME))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"identifiableId1\":{\"extensionName\":\"unknownExtension\",\"attribute1\":true}}", MediaType.APPLICATION_JSON));

        RestNetworkStoreClient restNetworkStoreClient = new RestNetworkStoreClient(restClient, objectMapper);
        Map<String, ExtensionAttributes> result = restNetworkStoreClient.getAllExtensionsAttributesByResourceTypeAndExtensionName(networkUuid, 0, ResourceType.GENERATOR, ActivePowerControl.NAME);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testRawExtensionAttributesByResourceType() {
        server.expect(requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/identifiables/types/" + ResourceType.GENERATOR + "/extensions"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"identifiableId2\":{\"unknownExtension\":{\"extensionName\":\"unknownExtension\",\"attribute1\":5.0}},\"identifiableId1\":{\"unknownExtension\":{\"extensionName\":\"unknownExtension\",\"attribute1\":5.0},\"activePowerControl\":{\"extensionName\":\"activePowerControl\",\"participate\":true,\"droop\":5.2,\"participationFactor\":0.5,\"minTargetP\":0.0,\"maxTargetP\":0.0}}}", MediaType.APPLICATION_JSON));

        RestNetworkStoreClient restNetworkStoreClient = new RestNetworkStoreClient(restClient, objectMapper);
        Map<String, Map<String, ExtensionAttributes>> result = restNetworkStoreClient.getAllExtensionsAttributesByResourceType(networkUuid, 0, ResourceType.GENERATOR);
        assertNotNull(result);
        // Identifiables with empty maps are filtered (like identifiableId2)
        assertEquals(1, result.size());
        Map<String, ExtensionAttributes> resultIdentifiable1 = result.get("identifiableId1");
        assertEquals(1, resultIdentifiable1.size());
        assertTrue(resultIdentifiable1.containsKey("activePowerControl"));
    }

    @Test
    public void testRawOperationalLimitsGroupAttributesByResourceType() {
        server.expect(ExpectedCount.once(), requestTo("/networks/" + networkUuid + "/" + Resource.INITIAL_VARIANT_NUM + "/branch/types/" + ResourceType.LINE + "/operationalLimitsGroup/selected/"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{\"{\\\"branchId\\\":\\\"branchId\\\",\\\"operationalLimitsGroupId\\\":\\\"selected\\\",\\\"side\\\":1}\":{\"currentLimits\":{\"permanentLimit\":1.0,\"temporaryLimits\":{\"10\":{\"name\":\"temporarylimit1\",\"value\":12.0,\"acceptableDuration\":10,\"fictitious\":false},\"15\":{\"name\":\"temporarylimit2\",\"value\":9.0,\"acceptableDuration\":15,\"fictitious\":false}}}}}", MediaType.APPLICATION_JSON));

        RestNetworkStoreClient restNetworkStoreClient = new RestNetworkStoreClient(restClient, objectMapper);
        Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes> result = restNetworkStoreClient.getAllSelectedOperationalLimitsGroupAttributesByResourceType(networkUuid, 0, ResourceType.LINE);
        assertNotNull(result);
        // Identifiables with empty maps are filtered (like identifiableId2)
        assertEquals(1, result.size());
        OperationalLimitsGroupAttributes resultIdentifiable1 = result.get(new OperationalLimitsGroupIdentifier("branchId", "selected", 1));
        assertEquals(2, resultIdentifiable1.getCurrentLimits().getTemporaryLimits().size());
    }
}
