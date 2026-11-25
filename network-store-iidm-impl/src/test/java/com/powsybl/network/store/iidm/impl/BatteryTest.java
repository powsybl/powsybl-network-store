/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class BatteryTest {
    @Test
    void removeExtension() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        battery.newExtension(BatteryShortCircuitAdder.class).withDirectSubtransX(1).add();
        assertTrue(battery.removeExtension(BatteryShortCircuit.class));
        assertNull(battery.getExtension(BatteryShortCircuit.class));
        assertFalse(battery.removeExtension(BatteryShortCircuit.class));
        battery.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(battery.removeExtension(ConnectablePosition.class));
        assertNull(battery.getExtension(ConnectablePosition.class));
        assertFalse(battery.removeExtension(ConnectablePosition.class));
    }

    @Test
    void updateWithInvalidTargetP() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        assertEquals("Battery 'BAT': p0 is invalid",
                assertThrows(ValidationException.class, () -> battery.setTargetP(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        battery.setTargetP(Double.NaN);
    }

    @Test
    void updateWithInvalidTargetQ() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        assertEquals("Battery 'BAT': invalid value (NaN) for localTargetQ (voltageRegulation is not set)",
                assertThrows(ValidationException.class, () -> battery.setLocalTargetQ(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        battery.setLocalTargetQ(Double.NaN);
    }

    @Test
    void batteryAdderGetLocalTargetQ() {
        Network network = Network.create("test", "test");
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BatteryAdder adder = voltageLevel.newBattery()
                .setId("BAT1")
                .setNode(0)
                .setMinP(-20.0)
                .setMaxP(20.0)
                .setTargetP(10.0)
                .setLocalTargetQ(5.0);
        assertEquals(5.0, adder.getLocalTargetQ());
        Battery battery = adder.add();
        assertEquals(5.0, battery.getLocalTargetQ());
    }

    @Test
    void regulatingPointWithoutVoltageRegulation() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        AbstractRegulatingPoint regulatingPoint = ((BatteryImpl) battery).getRegulatingPoint();
        assertNull(regulatingPoint.getAttributes());
        assertNull(regulatingPoint.getRegulatingTerminal());
        assertNull(regulatingPoint.getLocalTerminal());
        assertNull(regulatingPoint.getExplicitRegulatingTerminal());
        regulatingPoint.removeRegulation(ReportNode.NO_OP);
        assertNull(regulatingPoint.getRegulatingTerminal());
    }

    @Test
    void removeRegulationWithoutRegulatingTerminal() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        battery.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(false)
                .withTargetValue(400.0)
                .build();
        AbstractRegulatingPoint regulatingPoint = ((BatteryImpl) battery).getRegulatingPoint();
        regulatingPoint.removeRegulation(ReportNode.NO_OP);
        assertEquals(battery.getTerminal(), regulatingPoint.getRegulatingTerminal());
        assertFalse(regulatingPoint.isRegulating());
    }
}
