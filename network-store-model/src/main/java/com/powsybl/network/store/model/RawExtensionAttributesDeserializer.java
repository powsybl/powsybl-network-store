/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.util.Map;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class RawExtensionAttributesDeserializer extends JsonDeserializer<RawExtensionAttributes> {

    @Override
    public RawExtensionAttributes deserialize(JsonParser p, DeserializationContext context) {
        Map<String, Object> attributes;
        try {
            attributes = p.readValueAs(Map.class);
        } catch (Exception e) {
            attributes = Map.of();
        }
        return new RawExtensionAttributes(attributes);
    }
}
