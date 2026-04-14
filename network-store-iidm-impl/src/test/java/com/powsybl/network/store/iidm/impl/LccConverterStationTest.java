/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class LccConverterStationTest {
    @Test
    void removeExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        LccConverterStation lccConverterStation = network.getLccConverterStation("LCC1");
        lccConverterStation.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(lccConverterStation.removeExtension(ConnectablePosition.class));
        assertNull(lccConverterStation.getExtension(ConnectablePosition.class));
        assertFalse(lccConverterStation.removeExtension(ConnectablePosition.class));
    }

}
