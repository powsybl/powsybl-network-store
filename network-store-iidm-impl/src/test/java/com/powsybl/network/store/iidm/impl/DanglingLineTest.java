/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class DanglingLineTest {
    @Test
    void testOperationalLimitsGroup() {
        Network network = Network.create("test", "test");
        Substation substation = network.newSubstation().setId("sub").setCountry(Country.FR).setTso("RTE").add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
            .setId("vl")
            .setName("vl")
            .setNominalV(440.0F)
            .setHighVoltageLimit(400.0F)
            .setLowVoltageLimit(200.0F)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        voltageLevel.getBusBreakerView()
            .newBus()
            .setId("bus_vl")
            .setName("bus_vl")
            .add();
        voltageLevel.newDanglingLine()
            .setId("danglingId")
            .setName("DanglingName")
            .setR(10.0F)
            .setX(20.0F)
            .setP0(30.0F)
            .setQ0(40.0F)
            .setPairingKey("code")
            .setBus("bus_vl")
            .add();
        DanglingLine danglingLine = network.getDanglingLine("danglingId");
        OperationalLimitsGroup defaultOperationalGroup = danglingLine.getOrCreateSelectedOperationalLimitsGroup();
        Assertions.assertEquals("DEFAULT", defaultOperationalGroup.getId());
        Assertions.assertTrue(defaultOperationalGroup.getCurrentLimits().isEmpty());
        Assertions.assertTrue(defaultOperationalGroup.getActivePowerLimits().isEmpty());
        Assertions.assertTrue(defaultOperationalGroup.getApparentPowerLimits().isEmpty());

        danglingLine.newOperationalLimitsGroup("test");
        danglingLine.setSelectedOperationalLimitsGroup("test");
        OperationalLimitsGroup testOperationalGroup = danglingLine.getOrCreateSelectedOperationalLimitsGroup();
        Assertions.assertEquals("test", testOperationalGroup.getId());
    }
}
