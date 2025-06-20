/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

import static com.powsybl.network.store.model.OperationalLimitsGroupIdentifier.KEY_MAPPER;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class OperationalLimitsGroupIdentifierDeserializer extends KeyDeserializer {

    @Override
    public OperationalLimitsGroupIdentifier deserializeKey(String s, DeserializationContext context) throws IOException {
        return KEY_MAPPER.readValue(s, OperationalLimitsGroupIdentifier.class);
    }
}
