/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.network.store.iidm.impl.CreateNetworksUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class VoltageRegulationTest {

    @Test
    void batteryUsesNativeVoltageRegulation() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();
        Battery battery = network.getBattery("battery");

        assertNull(battery.getVoltageRegulation());

        battery.setLocalTargetQ(10.)
            .setLocalTargetV(225.)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .build();

        assertNotNull(battery.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE, battery.getVoltageRegulation().getMode());
        assertEquals(225., battery.getRegulatingTargetV());
        assertEquals(10., battery.getRegulatingTargetQ());
    }

    @Test
    void batteryCanRegulateRemotely() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();
        Battery battery = network.getBattery("battery");

        battery.setLocalTargetQ(10.)
            .newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .withTerminal(network.getStaticVarCompensator("SVC2").getTerminal())
            .withTargetValue(225.)
            .build();

        assertEquals(225., battery.getRegulatingTargetV());
        assertEquals("SVC2", battery.getRegulatingTerminal().getConnectable().getId());
    }
}
