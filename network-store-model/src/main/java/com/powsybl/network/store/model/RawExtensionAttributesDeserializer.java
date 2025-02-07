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

import java.io.IOException;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class RawExtensionAttributesDeserializer extends JsonDeserializer<RawExtensionAttributes> {

    @Override
    public RawExtensionAttributes deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String rawJson = p.readValueAsTree().toString();
        return new RawExtensionAttributes(rawJson);
    }
}
