/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class StaticVarCompensatorTest {
    @Test
    void removeExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        StaticVarCompensator staticVarCompensator = network.getStaticVarCompensator("SVC");
        staticVarCompensator.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(staticVarCompensator.removeExtension(ConnectablePosition.class));
        assertNull(staticVarCompensator.getExtension(ConnectablePosition.class));
        assertFalse(staticVarCompensator.removeExtension(ConnectablePosition.class));
        staticVarCompensator.newExtension(StandbyAutomatonAdder.class).withB0(1.0)
                .withLowVoltageSetpoint(0.0)
                .withLowVoltageThreshold(0.5)
                .withHighVoltageSetpoint(1.0)
                .withHighVoltageThreshold(0.1)
                .add();
        assertTrue(staticVarCompensator.removeExtension(StandbyAutomaton.class));
        assertNull(staticVarCompensator.getExtension(StandbyAutomaton.class));
        assertFalse(staticVarCompensator.removeExtension(StandbyAutomaton.class));
    }

    @Test
    void updateWithInvalidVoltageSetpoint() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        svc.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(true)
                .build();
        assertEquals("Static var compensator 'SVC': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
                assertThrows(ValidationException.class, () -> svc.setLocalTargetV(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        svc.setLocalTargetV(Double.NaN);
    }

    @Test
    void updateWithInvalidReactivePowerSetpoint() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        svc.setLocalTargetQ(100.0);
        svc.newVoltageRegulation()
                .withMode(RegulationMode.REACTIVE_POWER)
                .withRegulating(true)
                .build();
        assertEquals("Static var compensator 'SVC': invalid value (NaN) for localTargetQ (voltageRegulation is set with REACTIVE_POWER mode and regulating true and the terminal is unset)",
                assertThrows(ValidationException.class, () -> svc.setLocalTargetQ(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        svc.setLocalTargetQ(Double.NaN);
    }

    @Test
    void undefinedRegulatingWithEquipmentValidationLevel() {
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

        StaticVarCompensator svc = voltageLevel.newStaticVarCompensator()
                .setId("SVC3")
                .setNode(0)
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setLocalTargetQ(1.0)
                .add();

        assertFalse(svc.isRegulating());
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
    }

    @Test
    void undefinedRegulating() {
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

        StaticVarCompensator svc3 = voltageLevel.newStaticVarCompensator()
                .setId("SVC3")
                .setNode(0)
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setLocalTargetQ(1.0)
                .add();

        assertFalse(svc3.isRegulating());
    }

    @Test
    void voltageSetpointWithoutVoltageRegulation() {
        Network network = createNetworkWithSvc();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        assertEquals(380.0, svc.getVoltageSetpoint());
        svc.setVoltageSetpoint(390.0);
        assertEquals(390.0, svc.getLocalTargetV());
        assertEquals(390.0, svc.getVoltageSetpoint());
    }

    @Test
    void reactivePowerSetpointWithoutVoltageRegulation() {
        Network network = createNetworkWithSvc();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        assertEquals(100.0, svc.getReactivePowerSetpoint());
        svc.setReactivePowerSetpoint(50.0);
        assertEquals(50.0, svc.getLocalTargetQ());
        assertEquals(50.0, svc.getReactivePowerSetpoint());
    }

    @Test
    void voltageSetpointWithRegulatingTerminal() {
        Network network = SvcTestCaseFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setVoltageSetpoint(392.0);
        assertEquals(392.0, svc.getLocalTargetV());
        assertEquals(392.0, svc.getVoltageSetpoint());
        Terminal loadTerminal = network.getLoad("L2").getTerminal();
        svc.setRegulatingTerminal(loadTerminal);
        assertEquals(loadTerminal, svc.getRegulatingTerminal());
        svc.setVoltageSetpoint(395.0);
        assertEquals(395.0, svc.getVoltageSetpoint());
        assertEquals(395.0, svc.getVoltageRegulation().getTargetValue());
        assertEquals(392.0, svc.getLocalTargetV());
        svc.setRegulationMode(RegulationMode.REACTIVE_POWER);
        svc.setVoltageSetpoint(385.0);
        assertEquals(385.0, svc.getLocalTargetV());
        assertEquals(385.0, svc.getVoltageSetpoint());
        assertEquals(395.0, svc.getVoltageRegulation().getTargetValue());
    }

    @Test
    void reactivePowerSetpointWithRegulatingTerminal() {
        Network network = SvcTestCaseFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setLocalTargetQ(50.0);
        svc.setRegulationMode(RegulationMode.REACTIVE_POWER);
        svc.setReactivePowerSetpoint(60.0);
        assertEquals(60.0, svc.getLocalTargetQ());
        assertEquals(60.0, svc.getReactivePowerSetpoint());
        Terminal loadTerminal = network.getLoad("L2").getTerminal();
        svc.setRegulatingTerminal(loadTerminal);
        assertEquals(loadTerminal, svc.getRegulatingTerminal());
        svc.setReactivePowerSetpoint(70.0);
        assertEquals(70.0, svc.getReactivePowerSetpoint());
        assertEquals(70.0, svc.getVoltageRegulation().getTargetValue());
        assertEquals(60.0, svc.getLocalTargetQ());
        svc.setRegulationMode(RegulationMode.VOLTAGE);
        svc.setReactivePowerSetpoint(80.0);
        assertEquals(80.0, svc.getLocalTargetQ());
        assertEquals(80.0, svc.getReactivePowerSetpoint());
        assertEquals(70.0, svc.getVoltageRegulation().getTargetValue());
    }

    @Test
    void regulationMode() {
        Network network = createNetworkWithSvc();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        assertNull(svc.getRegulationMode());
        svc.setRegulationMode(RegulationMode.VOLTAGE);
        assertEquals(RegulationMode.VOLTAGE, svc.getRegulationMode());
        svc.setRegulationMode(RegulationMode.REACTIVE_POWER);
        assertEquals(RegulationMode.REACTIVE_POWER, svc.getRegulationMode());
    }

    @Test
    void regulatingTerminalWithoutVoltageRegulation() {
        Network network = createNetworkWithSvc();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        Terminal loadTerminal = network.getLoad("L1").getTerminal();
        svc.setRegulatingTerminal(loadTerminal);
        assertEquals(loadTerminal, svc.getRegulatingTerminal());
        assertFalse(svc.isRegulating());
        assertEquals(100.0, svc.getVoltageRegulation().getTargetValue());
    }

    @Test
    void regulating() {
        Network network = createNetworkWithSvc();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        svc.setRegulating(false);
        assertNull(svc.getVoltageRegulation());
        svc.setRegulating(true);
        assertTrue(svc.isRegulating());
        assertEquals(RegulationMode.REACTIVE_POWER, svc.getRegulationMode());
        svc.setRegulating(false);
        assertFalse(svc.isRegulating());
    }

    @Test
    void getExtensions() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        assertTrue(svc.getExtensions().isEmpty());
        svc.newExtension(StandbyAutomatonAdder.class).withB0(1.0)
                .withLowVoltageSetpoint(0.0)
                .withLowVoltageThreshold(0.5)
                .withHighVoltageSetpoint(1.0)
                .withHighVoltageThreshold(0.1)
                .add();
        assertEquals(1, svc.getExtensions().size());
    }

    @Test
    void adderLocalTargetQGetter() {
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

        StaticVarCompensatorAdder adder = voltageLevel.newStaticVarCompensator()
                .setId("SVC3")
                .setNode(0)
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setLocalTargetQ(1.0);

        assertEquals(1.0, adder.getLocalTargetQ());
    }

    private static Network createNetworkWithSvc() {
        Network network = Network.create("test", "test");
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        voltageLevel.newLoad()
                .setId("L1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setP0(100.0)
                .setQ0(50.0)
                .add();
        voltageLevel.newStaticVarCompensator()
                .setId("SVC")
                .setConnectableBus("B1")
                .setBus("B1")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setLocalTargetV(380.0)
                .setLocalTargetQ(100.0)
                .add();
        return network;
    }
}
