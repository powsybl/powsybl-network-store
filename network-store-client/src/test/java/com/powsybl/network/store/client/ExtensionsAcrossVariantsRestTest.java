/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * A network with two variants, each holding a different {@link ActivePowerControl} on the same battery, is served
 * through mocked REST responses. The extension attributes must follow the working variant, with the production
 * store client (cached) as well as with a plain REST one (not cached).
 *
 * @author Frederic Didier
 */
@RestClientTest
@ContextConfiguration(classes = ResourceAttributesRestClientImpl.class)
class ExtensionsAcrossVariantsRestTest {

    private static final String BASE_URI = "http://network-store-server/" + NetworkStoreApi.VERSION;

    private static final String VARIANT1 = "variant1";

    private static final int VARIANT1_NUM = 1;

    private static final String BATTERY_ID = "BAT";

    @Autowired
    private ResourceAttributesRestClient restClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, String> responsesByUri = new HashMap<>();

    private final UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    @BeforeEach
    void setUp() throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        // requests can come in any order and any number of times
        server.expect(manyTimes(), request -> { })
                .andRespond(request -> {
                    String body = responsesByUri.get(request.getURI().toString());
                    return body != null
                            ? withSuccess(body, MediaType.APPLICATION_JSON).createResponse(request)
                            : withStatus(HttpStatus.NOT_FOUND).createResponse(request);
                });

        expectGet("/networks/" + networkUuid, json(List.of(
                new VariantInfos(VariantManagerConstants.INITIAL_VARIANT_ID, Resource.INITIAL_VARIANT_NUM),
                new VariantInfos(VARIANT1, VARIANT1_NUM))));
        stubVariant(Resource.INITIAL_VARIANT_NUM, VariantManagerConstants.INITIAL_VARIANT_ID,
                ActivePowerControlAttributes.builder().droop(4.0).participate(true).participationFactor(1.2).build());
        stubVariant(VARIANT1_NUM, VARIANT1,
                ActivePowerControlAttributes.builder().droop(6.0).participate(false).participationFactor(2.4).build());
    }

    private void stubVariant(int variantNum, String variantId, ActivePowerControlAttributes extension) throws JsonProcessingException {
        Resource<NetworkAttributes> network = Resource.networkBuilder()
                .id("n1")
                .variantNum(variantNum)
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(variantId)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        Resource<BatteryAttributes> battery = Resource.batteryBuilder()
                .id(BATTERY_ID)
                .variantNum(variantNum)
                .attributes(BatteryAttributes.builder().voltageLevelId("VL").build())
                .build();
        String variantUrl = "/networks/" + networkUuid + "/" + variantNum;
        expectGet(variantUrl, json(TopLevelDocument.of(network)));
        expectGet(variantUrl + "/batteries/" + BATTERY_ID, json(TopLevelDocument.of(battery)));
        expectGet(variantUrl + "/identifiables/" + BATTERY_ID + "/extensions/" + ActivePowerControl.NAME,
                json(ExtensionAttributesTopLevelDocument.of(extension)));
    }

    private void expectGet(String path, String body) {
        responsesByUri.put(BASE_URI + path, body);
    }

    private String json(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private void checkExtensionFollowsWorkingVariant(NetworkStoreService service) {
        Network network = service.getNetwork(networkUuid);
        Battery battery = network.getBattery(BATTERY_ID);

        ActivePowerControl<Battery> extension = battery.getExtension(ActivePowerControl.class);
        assertEquals(4.0, extension.getDroop());
        assertTrue(extension.isParticipate());

        network.getVariantManager().setWorkingVariant(VARIANT1);

        // both the extension created before the switch and a new one must read the second variant
        assertEquals(6.0, extension.getDroop());
        assertFalse(extension.isParticipate());
        ActivePowerControl<Battery> extensionOfVariant1 = battery.getExtension(ActivePowerControl.class);
        assertEquals(6.0, extensionOfVariant1.getDroop());
        assertEquals(2.4, extensionOfVariant1.getParticipationFactor());

        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(4.0, extension.getDroop());
        assertEquals(1.2, extension.getParticipationFactor());
    }

    @Test
    void extensionsFollowVariantSwitchWithCachedClient() {
        try (NetworkStoreService service = new NetworkStoreService(restClient, PreloadingStrategy.NONE)) {
            checkExtensionFollowsWorkingVariant(service);
        }
    }

    @Test
    void extensionsFollowVariantSwitchWithPlainRestClient() {
        try (NetworkStoreService service = new NetworkStoreService(restClient, PreloadingStrategy.NONE,
                (client, strategy, executor) -> (NetworkStoreClient) new RestNetworkStoreClient(client))) {
            checkExtensionFollowsWorkingVariant(service);
        }
    }
}
