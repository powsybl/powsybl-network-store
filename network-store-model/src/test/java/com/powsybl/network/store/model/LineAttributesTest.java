/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LineAttributesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void test() throws IOException {
        LineAttributes lineAttributes = LineAttributes.builder().build();
        String json = objectMapper.writeValueAsString(lineAttributes);
        assertEquals("{}", json);

        LineAttributes lineAttributes2 = objectMapper.readValue(json, LineAttributes.class);
        assertEquals(new LineAttributes(), lineAttributes2);
    }

    @Test
    public void updateTest() throws IOException {
        LineAttributes lineAttributes = LineAttributes.builder()
                .name("test")
                .fictitious(false)
                .build();

        LineAttributes lineAttributes2 = AttributesSpyer.spy(lineAttributes, ResourceType.LINE);
        lineAttributes2.setP1(10);
        String json = objectMapper.writeValueAsString(lineAttributes2);
        assertEquals("{\"name\":\"test\",\"fictitious\":false,\"p1\":10.0}", json);
        LineAttributes update = AttributesSpyer.getUpdate(lineAttributes2);
        String updateJson = objectMapper.writeValueAsString(update);
        assertEquals("{\"p1\":10.0}", updateJson);

        Resource<LineAttributes> resource = Resource.lineBuilder()
                .id("id")
                .attributes(lineAttributes2)
                .build();
        resource.setSerializeUpdate(true);
        assertEquals("{\"type\":\"LINE\",\"id\":\"id\",\"attributes\":{\"p1\":10.0}}", objectMapper.writeValueAsString(resource));

        Resource<LineAttributes> resource2 = Resource.lineBuilder()
                .id("id")
                .attributes(lineAttributes)
                .build();
        resource2.setSerializeUpdate(true);
        assertEquals("{\"type\":\"LINE\",\"id\":\"id\",\"attributes\":{\"name\":\"test\",\"fictitious\":false}}", objectMapper.writeValueAsString(resource2));
    }
}
