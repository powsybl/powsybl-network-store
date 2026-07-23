/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class HvdcTest {
    @Test
    void removeExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        HvdcLine hvdc = network.getHvdcLine("HVDC1");
        hvdc.newExtension(HvdcAngleDroopActivePowerControlAdder.class).withDroop(1).withEnabled(true).add();
        assertTrue(hvdc.removeExtension(HvdcAngleDroopActivePowerControl.class));
        assertNull(hvdc.getExtension(HvdcAngleDroopActivePowerControl.class));
        Assertions.assertFalse(hvdc.removeExtension(HvdcAngleDroopActivePowerControl.class));
        hvdc.newExtension(HvdcOperatorActivePowerRangeAdder.class).withOprFromCS1toCS2(1.0f).withOprFromCS2toCS1(2.0f).add();
        assertTrue(hvdc.removeExtension(HvdcOperatorActivePowerRange.class));
        assertNull(hvdc.getExtension(HvdcOperatorActivePowerRange.class));
        Assertions.assertFalse(hvdc.removeExtension(HvdcOperatorActivePowerRange.class));
    }

    @Test
    void updateWithInvalidConvertersMode() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        HvdcLine hvdc = network.getHvdcLine("HVDC1");
        Assertions.assertEquals("HVDC line 'HVDC1': converter mode is invalid",
                Assertions.assertThrows(ValidationException.class, () -> hvdc.setConvertersMode(null)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        hvdc.setConvertersMode(null);
    }

    @Test
    void updateWithInvalidActivePowerSetpoint() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        HvdcLine hvdc = network.getHvdcLine("HVDC1");
        Assertions.assertEquals("HVDC line 'HVDC1': invalid value (NaN) for active power setpoint",
                Assertions.assertThrows(ValidationException.class, () -> hvdc.setActivePowerSetpoint(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        hvdc.setActivePowerSetpoint(Double.NaN);
    }
}
