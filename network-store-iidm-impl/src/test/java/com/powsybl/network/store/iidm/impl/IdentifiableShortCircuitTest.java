/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer@rte-france.com>}
 */
class IdentifiableShortCircuitTest {
    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("VLLOAD");
        assertNotNull(voltageLevel);
        voltageLevel.newExtension(IdentifiableShortCircuitAdder.class)
                .withIpMin(1000)
                .withIpMax(2000)
                .add();
        IdentifiableShortCircuit identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);
        assertEquals(1000, identifiableShortCircuit.getIpMin(), 0);
        assertEquals(2000, identifiableShortCircuit.getIpMax(), 0);
        identifiableShortCircuit.setIpMax(1500);
        identifiableShortCircuit.setIpMin(900);
        assertEquals(900, identifiableShortCircuit.getIpMin(), 0);
        assertEquals(1500, identifiableShortCircuit.getIpMax(), 0);

        // set same values, for condition coverage
        identifiableShortCircuit.setIpMax(1500);
        identifiableShortCircuit.setIpMin(900);
        assertEquals(900, identifiableShortCircuit.getIpMin(), 0);
        assertEquals(1500, identifiableShortCircuit.getIpMax(), 0);
    }
}
