package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.iidm.network.extensions.Coordinate;

import java.io.IOException;

public class CoordinateDeserializer extends StdDeserializer<Coordinate> {

    protected CoordinateDeserializer() {
        super(Coordinate.class);
    }

    @Override
    public Coordinate deserialize(JsonParser parser, DeserializationContext deserializationContext)
            throws IOException {
        double latitude = 0.0;
        double longitude = 0.0;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "latitude" -> {
                    parser.nextToken();
                    latitude = parser.getDoubleValue();
                }
                case "longitude" -> {
                    parser.nextToken();
                    longitude = parser.getDoubleValue();
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new Coordinate(latitude, longitude);
    }
}
