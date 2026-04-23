/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        vscConverterStation.setVoltageRegulatorOn(true);
        assertEquals("VSC converter station 'VSC1': invalid value (NaN) for voltage setpoint (voltage regulator is on)",
                assertThrows(ValidationException.class, () -> vscConverterStation.setVoltageSetpoint(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        vscConverterStation.setVoltageSetpoint(Double.NaN);
    }

    @Test
    void updateWithInvalidReactivePowerSetpoint() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VscConverterStation vscConverterStation = network.getVscConverterStation("VSC1");
        vscConverterStation.setVoltageRegulatorOn(false);
        assertEquals("VSC converter station 'VSC1': invalid value (NaN) for reactive power setpoint (voltage regulator is off)",
                assertThrows(ValidationException.class, () -> vscConverterStation.setReactivePowerSetpoint(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        vscConverterStation.setReactivePowerSetpoint(Double.NaN);
    }
}
