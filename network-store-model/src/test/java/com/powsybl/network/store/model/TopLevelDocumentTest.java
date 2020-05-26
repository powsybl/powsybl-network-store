/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Country;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TopLevelDocumentTest {

    @Test
    public void test() throws IOException {
        Resource<SubstationAttributes> resource = Resource.substationBuilder()
                .id("S")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .build())
                .build();
        TopLevelDocument document = TopLevelDocument.of(resource);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"SUBSTATION\",\"id\":\"S\",\"attributes\":{\"fictitious\":false,\"country\":\"FR\"}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(resource, document2.getData().get(0));
    }

    @Test
    public void testMultiResources() throws IOException {
        Resource<SubstationAttributes> resource = Resource.substationBuilder()
                .id("S")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .build())
                .build();
        TopLevelDocument document = TopLevelDocument.of(ImmutableList.of(resource, resource));
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"SUBSTATION\",\"id\":\"S\",\"attributes\":{\"fictitious\":false,\"country\":\"FR\"}},{\"type\":\"SUBSTATION\",\"id\":\"S\",\"attributes\":{\"fictitious\":false,\"country\":\"FR\"}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(2, document2.getData().size());
        assertEquals(resource, document2.getData().get(0));
        assertEquals(resource, document2.getData().get(1));
    }
}
