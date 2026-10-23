/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyVoltageRegulationAttributesMapperTest {

    private final ObjectMapper objectMapper = JsonUtil.createObjectMapper();

    @Test
    void migrateGeneratorRemoteReactivePowerControl() throws IOException {
        String json = "{" +
            "\"type\":\"GENERATOR\",\"id\":\"G\",\"variantNum\":0,\"attributes\":{" +
            "\"targetQ\":10.0,\"targetV\":220.0,\"localTargetQ\":10.0,\"localTargetV\":110.0," +
            "\"remoteReactivePowerControl\":{" +
            "\"targetQ\":50.0,\"enabled\":true," +
            "\"regulatingTerminal\":{\"connectableId\":\"LOAD\"}}}}";

        Resource<GeneratorAttributes> resource = objectMapper.readValue(json,
            new TypeReference<Resource<GeneratorAttributes>>() { });

        NetworkVoltageRegulationAttributes regulation = resource.getAttributes().getVoltageRegulation();
        assertEquals(RegulationMode.REACTIVE_POWER, regulation.getMode());
        assertEquals(50.0, regulation.getTargetValue());
        assertTrue(regulation.isRegulating());
        assertEquals("LOAD", regulation.getTerminal().getConnectableId());
        assertEquals(10.0, resource.getAttributes().getLocalTargetQ());
        assertEquals(110.0, resource.getAttributes().getLocalTargetV());
        assertNull(resource.getAttributes().getRemoteReactivePowerControl());

        String serialized = objectMapper.writeValueAsString(resource);
        assertTrue(serialized.contains("voltageRegulation"));
        assertFalse(serialized.contains("remoteReactivePowerControl"));
    }

    @Test
    void migrateStaticVarCompensatorVoltagePerReactivePowerControl() throws IOException {
        String json = "{" +
            "\"type\":\"STATIC_VAR_COMPENSATOR\",\"id\":\"SVC\",\"variantNum\":0," +
            "\"attributes\":{" +
            "\"voltageSetPoint\":220.0,\"reactivePowerSetPoint\":3.0," +
            "\"regulatingPoint\":{" +
            "\"regulatingTerminal\":{\"connectableId\":\"LOAD\"},\"regulating\":true}," +
            "\"voltagePerReactiveControl\":{\"slope\":2.0}}}";

        Resource<StaticVarCompensatorAttributes> resource = objectMapper.readValue(json,
            new TypeReference<Resource<StaticVarCompensatorAttributes>>() { });

        NetworkVoltageRegulationAttributes regulation = resource.getAttributes().getVoltageRegulation();
        assertEquals(RegulationMode.VOLTAGE_PER_REACTIVE_POWER, regulation.getMode());
        assertEquals(220.0, regulation.getTargetValue());
        assertEquals(2.0, regulation.getSlope());
        assertTrue(regulation.isRegulating());
        assertEquals("LOAD", regulation.getTerminal().getConnectableId());
        assertTrue(Double.isNaN(resource.getAttributes().getLocalTargetV()));
        assertEquals(3.0, resource.getAttributes().getLocalTargetQ());
        assertNull(resource.getAttributes().getVoltagePerReactiveControl());
    }

    @Test
    void migrateBatteryVoltageRegulationExtension() throws IOException {
        String json = "{" +
            "\"type\":\"BATTERY\",\"id\":\"BAT\",\"variantNum\":0," +
            "\"attributes\":{" +
            "\"extensionAttributes\":{" +
            "\"voltageRegulation\":{" +
            "\"extensionName\":\"voltageRegulation\",\"voltageRegulatorOn\":true,\"targetV\":225.0}}}}";

        Resource<BatteryAttributes> resource = objectMapper.readValue(json,
            new TypeReference<Resource<BatteryAttributes>>() { });

        NetworkVoltageRegulationAttributes regulation = resource.getAttributes().getVoltageRegulation();
        assertEquals(RegulationMode.VOLTAGE, regulation.getMode());
        assertTrue(Double.isNaN(regulation.getTargetValue()));
        assertTrue(regulation.isRegulating());
        assertEquals(225.0, resource.getAttributes().getLocalTargetV());
        assertFalse(resource.getAttributes().getExtensionAttributes().containsKey("voltageRegulation"));
    }
}
