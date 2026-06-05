/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
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
        staticVarCompensator.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(1.0)
                .add();
        assertTrue(staticVarCompensator.removeExtension(VoltagePerReactivePowerControl.class));
        assertNull(staticVarCompensator.getExtension(VoltagePerReactivePowerControl.class));
        assertFalse(staticVarCompensator.removeExtension(VoltagePerReactivePowerControl.class));
    }

    @Test
    void updateWithInvalidVoltageSetpoint() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        svc.setRegulating(true);
        svc.setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE);
        assertEquals("Static var compensator 'SVC': invalid value (NaN) for voltage setpoint",
                assertThrows(ValidationException.class, () -> svc.setVoltageSetpoint(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        svc.setVoltageSetpoint(Double.NaN);
    }

    @Test
    void updateWithInvalidReactivePowerSetpoint() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC");
        svc.setRegulating(true);
        svc.setReactivePowerSetpoint(100.0);
        svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
        assertEquals("Static var compensator 'SVC': invalid value (NaN) for reactive power setpoint",
                assertThrows(ValidationException.class, () -> svc.setReactivePowerSetpoint(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        svc.setReactivePowerSetpoint(Double.NaN);
    }
}
