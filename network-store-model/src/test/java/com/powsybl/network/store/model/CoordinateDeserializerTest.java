package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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
