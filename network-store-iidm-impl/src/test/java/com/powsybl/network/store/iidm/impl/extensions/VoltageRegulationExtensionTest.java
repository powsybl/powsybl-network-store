/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.network.store.iidm.impl.CreateNetworksUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoltageRegulationExtensionTest {

    @Test
    void shouldCreateUpdateAndRemoveVoltageRegulation() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();
        Battery battery = network.getBattery("battery");
        Terminal remoteTerminal = network.getStaticVarCompensator("SVC2").getTerminal();

        assertNull(battery.getVoltageRegulation());
        VoltageRegulation voltageRegulation = battery.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTerminal(remoteTerminal)
                .withTargetValue(225.0)
                .build();

        assertNotNull(voltageRegulation);
        assertEquals(remoteTerminal, voltageRegulation.getTerminal());
        assertEquals(225.0, voltageRegulation.getTargetValue());
        assertTrue(battery.isRegulating());
        assertTrue(battery.isRemoteRegulating());

        voltageRegulation.setTargetValue(130.0);
        voltageRegulation.setRegulating(false);
        assertEquals(130.0, voltageRegulation.getTargetValue());
        assertFalse(battery.isRegulating());

        battery.removeVoltageRegulation();
        assertNull(battery.getVoltageRegulation());
        assertTrue(remoteTerminal.getReferrers().isEmpty());
    }

    @Test
    void shouldDeactivateWhenRemoteTerminalIsRemovedFromAnotherBus() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();
        Battery battery = network.getBattery("battery");
        Terminal remoteTerminal = network.getShuntCompensator("SHUNT1").getTerminal();

        VoltageRegulation voltageRegulation = battery.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTerminal(remoteTerminal)
                .withTargetValue(225.0)
                .build();
        assertEquals(remoteTerminal, voltageRegulation.getTerminal());

        remoteTerminal.getConnectable().remove();

        assertNull(voltageRegulation.getTerminal());
        assertFalse(voltageRegulation.isRegulating());
    }
}
