/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;

import java.io.IOException;

import static com.powsybl.network.store.model.OperationalLimitsGroupIdentifier.KEY_MAPPER;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class OperationalLimitsGroupIdentifierSerializer extends StdKeySerializers.StringKeySerializer {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeFieldName(KEY_MAPPER.writeValueAsString(value));
    }
}
