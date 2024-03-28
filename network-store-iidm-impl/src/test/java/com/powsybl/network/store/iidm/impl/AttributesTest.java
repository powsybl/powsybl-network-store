/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.GeneratorStartupAttributes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class AttributesTest {

    @Test
    public void testSerialization() throws JsonProcessingException {
        GeneratorStartupAttributes generatorStartupAttributes = new GeneratorStartupAttributes(0.5, 10, 5, 3, 5);
        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(generatorStartupAttributes);
        assertEquals("{\"@type\":\"startup\",\"plannedActivePowerSetpoint\":0.5,\"startupCost\":10.0,\"marginalCost\":5.0,\"plannedOutageRate\":3.0,\"forcedOutageRate\":5.0}", serialized);
    }

    @Test
    public void testSerDeserialization() throws JsonProcessingException {
        GeneratorStartupAttributes generatorStartupAttributes = new GeneratorStartupAttributes(0.5, 10, 5, 3, 5);
        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(generatorStartupAttributes);
        GeneratorStartupAttributes deserialized = (GeneratorStartupAttributes) mapper.readValue(serialized, ExtensionAttributes.class);
        assertEquals(0.5, deserialized.getPlannedActivePowerSetpoint(), 0.1);
        assertEquals(10.0, deserialized.getStartupCost(), 0.1);
        assertEquals(5.0, deserialized.getMarginalCost(), 0.1);
        assertEquals(3.0, deserialized.getPlannedOutageRate(), 0.1);
        assertEquals(5.0, deserialized.getForcedOutageRate(), 0.1);
    }

}
