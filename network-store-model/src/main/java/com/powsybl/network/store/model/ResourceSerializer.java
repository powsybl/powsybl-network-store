/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ResourceSerializer extends StdSerializer<Resource> {

    public ResourceSerializer() {
        super(Resource.class);
    }

    @Override
    public void serialize(Resource resource, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", resource.getType().name());
        jsonGenerator.writeStringField("id", resource.getId());
        if (resource.getAttributes() != null) {
            IdentifiableAttributes update = null;
            if (resource.isSerializeUpdate()) {
                update = AttributesSpyer.getUpdate(resource.getAttributes());
            }
            if (update != null) {
                jsonGenerator.writeObjectField("attributes", update);
            } else {
                jsonGenerator.writeObjectField("attributes", resource.getAttributes());
            }
        }
        jsonGenerator.writeEndObject();
    }
}
