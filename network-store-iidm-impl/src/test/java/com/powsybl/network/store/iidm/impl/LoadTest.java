/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
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

    @Test
    void removeExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Load load = network.getLoad("LD1");
        load.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(load.removeExtension(ConnectablePosition.class));
        assertNull(load.getExtension(ConnectablePosition.class));
        assertFalse(load.removeExtension(ConnectablePosition.class));
        load.newExtension(LoadDetailAdder.class).withFixedActivePower(1).withVariableActivePower(10).withFixedReactivePower(2).add();
        assertTrue(load.removeExtension(LoadDetail.class));
        assertNull(load.getExtension(LoadDetail.class));
        assertFalse(load.removeExtension(LoadDetail.class));
    }

    @Test
    void updateWithInvalidP0() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Load load = network.getLoad("LD1");
        assertEquals("Load 'LD1': p0 is invalid",
                assertThrows(ValidationException.class, () -> load.setP0(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        load.setP0(Double.NaN);
    }

    @Test
    void updateWithInvalidQ0() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Load load = network.getLoad("LD1");
        assertEquals("Load 'LD1': q0 is invalid",
                assertThrows(ValidationException.class, () -> load.setQ0(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        load.setQ0(Double.NaN);
    }
}
