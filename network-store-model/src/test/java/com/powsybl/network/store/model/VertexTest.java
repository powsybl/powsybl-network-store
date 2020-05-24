/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.ConnectableType;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VertexTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testNodeBreaker() throws IOException {
        Vertex v = new Vertex("l", ConnectableType.LINE, 3, null, "ONE");
        String json = objectMapper.writeValueAsString(v);

        Vertex v2 = objectMapper.readValue(json, Vertex.class);
        assertEquals("l", v2.getId());
        assertEquals(ConnectableType.LINE, v2.getConnectableType());
        assertEquals(3, (int) v2.getNode());
        assertNull(v2.getBus());
        assertEquals("ONE", v2.getSide());
    }
}
