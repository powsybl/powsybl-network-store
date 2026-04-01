/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class BatteryTest {
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

}
