/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

// TODO : to remove when Area will be implemented

class AreaTest {
    @Test
    void testUnimplementedAreas() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(IterableUtils.toList(network.getAreas()).isEmpty());
        assertTrue(network.getAreaStream().toList().isEmpty());
        assertTrue(IterableUtils.toList(network.getAreaTypes()).isEmpty());
        assertTrue(network.getAreaTypeStream().toList().isEmpty());
        assertEquals(0, network.getAreaCount());
        assertEquals(0, network.getAreaTypeCount());
        assertNull(network.getArea("area"));
        assertNull(network.newArea());

        VoltageLevel vl = network.getVoltageLevel("VLGEN");
        assertNotNull(vl);
        assertTrue(IterableUtils.toList(vl.getAreas()).isEmpty());
        assertTrue(vl.getAreasStream().toList().isEmpty());
        assertFalse(vl.getArea("area").isPresent());
    }
}
