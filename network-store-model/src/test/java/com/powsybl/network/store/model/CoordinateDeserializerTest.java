/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class CoordinateDeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String json = "{\"latitude\": 45.3, \"longitude\": 42}";

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Coordinate.class, new CoordinateDeserializer());
        mapper.registerModule(module);

        Coordinate coordinate = mapper.readValue(json, Coordinate.class);

        assertEquals(45.3, coordinate.getLatitude(), 0.1);
        assertEquals(42, coordinate.getLongitude(), 0.1);
    }

    @Test
    public void testDeserializeMissingFields() throws JsonProcessingException {
        String json = "{\"latitude\": 45.3}";

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Coordinate.class, new CoordinateDeserializer());
        mapper.registerModule(module);

        Coordinate coordinate = mapper.readValue(json, Coordinate.class);

        assertEquals(45.3, coordinate.getLatitude(), 0.1);
        assertEquals(0.0, coordinate.getLongitude(), 0.1);
    }

    @Test
    public void testDeserializeUnexpectedField() {
        String json = "{\"lat\": 45.3, \"longitude\": 42}";

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Coordinate.class, new CoordinateDeserializer());
        mapper.registerModule(module);

        assertThrows(IllegalStateException.class, () -> {
            mapper.readValue(json, Coordinate.class);
        });
    }

    @Test
    public void testDeserializeWithAdditionalFields() {
        String json = "{ \"latitude\": 45.3, \"longitude\": 42, \"field1\": 12}";

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Coordinate.class, new CoordinateDeserializer());
        mapper.registerModule(module);

        assertThrows(IllegalStateException.class, () -> {
            mapper.readValue(json, Coordinate.class);
        });
    }
}
