/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.iidm.network.extensions.Coordinate;

import java.io.IOException;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
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
