/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer@rte-france.com>}
 */
class BatteryShortCircuitTest {
    @Test
    void test2() {
        Network network = BatteryNetworkFactory.create();
        Battery gen = network.getBattery("BAT");
        Assertions.assertNotNull(gen);
        BatteryShortCircuit batteryShortCircuit = gen.newExtension(BatteryShortCircuitAdder.class)
                .withDirectTransX(20)
                .withDirectSubtransX(20)
                .withStepUpTransformerX(20)
                .add();
        assertEquals(20, batteryShortCircuit.getDirectTransX(), 0);
        assertEquals(20, batteryShortCircuit.getDirectSubtransX(), 0);
        assertEquals(20, batteryShortCircuit.getStepUpTransformerX(), 0);
        batteryShortCircuit.setDirectTransX(10);
        assertEquals(10, batteryShortCircuit.getDirectTransX(), 0);
        batteryShortCircuit.setDirectSubtransX(30);
        assertEquals(30, batteryShortCircuit.getDirectSubtransX(), 0);
        batteryShortCircuit.setStepUpTransformerX(10);
        assertEquals(10, batteryShortCircuit.getStepUpTransformerX(), 0);
    }
}
