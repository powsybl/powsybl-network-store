/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class DanglingLineTest {
    @Test
    void removeExtension() {
        Network network = DanglingLineNetworkFactory.create();
        DanglingLine danglingLine = network.getDanglingLine("DL");
        danglingLine.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(danglingLine.removeExtension(ConnectablePosition.class));
        assertNull(danglingLine.getExtension(ConnectablePosition.class));
        assertFalse(danglingLine.removeExtension(ConnectablePosition.class));
    }

    @Test
    void updateWithInvalidP0() {
        Network network = DanglingLineNetworkFactory.create();
        DanglingLine danglingLine = network.getDanglingLine("DL");
        assertEquals("Dangling line 'DL': p0 is invalid",
                assertThrows(ValidationException.class, () -> danglingLine.setP0(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        danglingLine.setP0(Double.NaN);
    }

    @Test
    void updateWithInvalidQ0() {
        Network network = DanglingLineNetworkFactory.create();
        DanglingLine danglingLine = network.getDanglingLine("DL");
        assertEquals("Dangling line 'DL': q0 is invalid",
                assertThrows(ValidationException.class, () -> danglingLine.setQ0(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        danglingLine.setQ0(Double.NaN);
    }
}
