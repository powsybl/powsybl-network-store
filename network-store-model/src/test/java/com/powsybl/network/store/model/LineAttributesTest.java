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

    @Test
    public void test() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        LineAttributes lineAttributes = LineAttributes.builder().build();
        String json = objectMapper.writeValueAsString(lineAttributes);
        assertEquals("{}", json);

        LineAttributes lineAttributes2 = objectMapper.readValue(json, LineAttributes.class);
        assertEquals(new LineAttributes(), lineAttributes2);
    }
}
