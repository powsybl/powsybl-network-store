/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class ShuntCompensatorTest {
    @Test
    void removeExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");
        shuntCompensator.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(shuntCompensator.removeExtension(ConnectablePosition.class));
        assertNull(shuntCompensator.getExtension(ConnectablePosition.class));
        assertFalse(shuntCompensator.removeExtension(ConnectablePosition.class));
    }

    @Test
    void updateWithInvalidTargetV() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");
        shuntCompensator.setTargetV(100.0);
        shuntCompensator.setTargetDeadband(0.5);
        shuntCompensator.setVoltageRegulatorOn(true);
        assertEquals("Shunt compensator 'SHUNT': invalid value (NaN) for voltage setpoint (voltage regulator is on)",
                assertThrows(ValidationException.class, () -> shuntCompensator.setTargetV(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        shuntCompensator.setTargetV(Double.NaN);
    }

    @Test
    void updateWithInvalidTargetDeadband() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");
        shuntCompensator.setTargetV(100.0);
        shuntCompensator.setTargetDeadband(0.5);
        shuntCompensator.setVoltageRegulatorOn(true);
        assertEquals("Shunt compensator 'SHUNT': Undefined value for target deadband of regulating shunt compensator",
                assertThrows(ValidationException.class, () -> shuntCompensator.setTargetDeadband(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        shuntCompensator.setTargetDeadband(Double.NaN);
    }
}
