/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class BoundaryLineTest {
    @Test
    void removeExtension() {
        Network network = BoundaryLineNetworkFactory.create();
        BoundaryLine boundaryLine = network.getBoundaryLine("BL");
        boundaryLine.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(boundaryLine.removeExtension(ConnectablePosition.class));
        assertNull(boundaryLine.getExtension(ConnectablePosition.class));
        assertFalse(boundaryLine.removeExtension(ConnectablePosition.class));
    }
}
