/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class VscConverterStationTest {
    @Test
    void removeExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VscConverterStation vscConverterStation = network.getVscConverterStation("VSC1");
        vscConverterStation.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(vscConverterStation.removeExtension(ConnectablePosition.class));
        assertNull(vscConverterStation.getExtension(ConnectablePosition.class));
        assertFalse(vscConverterStation.removeExtension(ConnectablePosition.class));
    }

    @Test
    void updateWithInvalidLossFactor() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VscConverterStation vscConverterStation = network.getVscConverterStation("VSC1");
        assertEquals("VSC converter station 'VSC1': loss factor is invalid is undefined",
                assertThrows(ValidationException.class, () -> vscConverterStation.setLossFactor(Float.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        vscConverterStation.setLossFactor(Float.NaN);
    }

    @Test
    void updateWithInvalidVoltageSetpoint() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VscConverterStation vscConverterStation = network.getVscConverterStation("VSC1");
        vscConverterStation.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(true)
                .build();
        assertEquals("VSC converter station 'VSC1': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
                assertThrows(ValidationException.class, () -> vscConverterStation.setLocalTargetV(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        vscConverterStation.setLocalTargetV(Double.NaN);
    }

    @Test
    void updateWithInvalidReactivePowerSetpoint() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VscConverterStation vscConverterStation = network.getVscConverterStation("VSC1");
        vscConverterStation.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(false)
                .build();
        assertEquals("VSC converter station 'VSC1': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)",
                assertThrows(ValidationException.class, () -> vscConverterStation.setLocalTargetQ(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        vscConverterStation.setLocalTargetQ(Double.NaN);
    }

    @Test
    void testVscConverterStationAdderWithoutVoltageRegulatorOnWithEquipmentValidationLevel() {
        Network network = Network.create("test", "test");
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        VscConverterStation converterStation = voltageLevel.newVscConverterStation()
                .setId("C4")
                .setNode(0)
                .setLocalTargetQ(123)
                .setLossFactor(1.1f)
                .add();

        assertFalse(converterStation.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
    }

    @Test
    void testReactiveLimitsCreation() {
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

        VscConverterStation converterStation = voltageLevel.newVscConverterStation()
                .setId("C4")
                .setNode(0)
                .setLocalTargetQ(123)
                .setLossFactor(1.1f)
                .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(false)
                .add()
                .add();

        ReactiveLimits reactiveLimits = converterStation.getReactiveLimits();
        assertInstanceOf(MinMaxReactiveLimits.class, reactiveLimits);
        MinMaxReactiveLimits minMaxReactiveLimits = (MinMaxReactiveLimits) reactiveLimits;
        Assertions.assertEquals(Double.MAX_VALUE, minMaxReactiveLimits.getMaxQ());
        Assertions.assertEquals(-Double.MAX_VALUE, minMaxReactiveLimits.getMinQ());
    }

    @Test
    void legacyVoltageRegulatorOnSetterWithoutVoltageRegulation() {
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
        VscConverterStation converterStation = voltageLevel.newVscConverterStation()
                .setId("C4")
                .setNode(0)
                .setLocalTargetV(400.0)
                .setLocalTargetQ(123.0)
                .setLossFactor(1.1f)
                .add();
        assertFalse(converterStation.isVoltageRegulatorOn());
        converterStation.setVoltageRegulatorOn(true);
        assertTrue(converterStation.isVoltageRegulatorOn());
        converterStation.setVoltageRegulatorOn(false);
        assertFalse(converterStation.isVoltageRegulatorOn());
    }

    @Test
    void legacyVoltageSetpointSetter() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VscConverterStation converterStation = network.getVscConverterStation("VSC1");
        converterStation.setVoltageSetpoint(410.0);
        assertEquals(410.0, converterStation.getVoltageSetpoint());
        converterStation.setRegulatingTerminal(converterStation.getTerminal());
        converterStation.setVoltageSetpoint(420.0);
        assertEquals(420.0, converterStation.getVoltageSetpoint());
    }

    @Test
    void legacyReactivePowerSetpointSetter() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VscConverterStation converterStation = network.getVscConverterStation("VSC1");
        converterStation.setReactivePowerSetpoint(250.0);
        assertEquals(250.0, converterStation.getReactivePowerSetpoint());
    }

    @Test
    void legacySetRegulatingTerminalWithoutVoltageRegulation() {
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
        VscConverterStationAdder adder = voltageLevel.newVscConverterStation()
                .setId("C4")
                .setNode(0)
                .setLocalTargetV(400.0)
                .setLocalTargetQ(123.0)
                .setLossFactor(1.1f);
        assertEquals(123.0, adder.getLocalTargetQ());
        VscConverterStation converterStation = adder.add();
        converterStation.setRegulatingTerminal(converterStation.getTerminal());
        assertEquals(converterStation.getTerminal(), converterStation.getRegulatingTerminal());
        assertFalse(converterStation.isVoltageRegulatorOn());
    }
}
