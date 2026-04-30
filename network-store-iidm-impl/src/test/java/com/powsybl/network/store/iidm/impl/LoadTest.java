/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetworkWithLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadTest {

    @Test
    void testAddConnectablePositionExtension() {
        Network network = createNodeBreakerNetworkWithLine();
        Load load = network.getLoad("LD");

        load.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("cpa")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        assertEquals("cpa", load.getExtension(ConnectablePosition.class).getFeeder().getName().orElseThrow());
        assertNull(load.getExtension(ConnectablePosition.class).getFeeder1());
        assertNull(load.getExtension(ConnectablePosition.class).getFeeder2());
        assertNull(load.getExtension(ConnectablePosition.class).getFeeder3());
    }

    @Test
    void testConnectDisconnect() {
        Network network = createNodeBreakerNetworkWithLine();
        Load load = network.getLoad("LD");

        // The load starts connected
        load.getTerminals().forEach(terminal -> Assertions.assertTrue(terminal.isConnected()));

        // Disconnect the load
        Assertions.assertTrue(load.disconnect());
        load.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));

        // Reconnect the load
        assertTrue(load.connect());
        load.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));
    }
}
