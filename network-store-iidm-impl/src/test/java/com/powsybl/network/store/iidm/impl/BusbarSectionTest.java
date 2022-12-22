/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class BusbarSectionTest {

    @Test
    public void testVAngle() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        BusbarSection bbs = network.getBusbarSection("BBS1");
        assertTrue(Double.isNaN(bbs.getV()));
        assertTrue(Double.isNaN(bbs.getAngle()));
        var bus = bbs.getTerminal().getBusBreakerView().getBus();
        bus.setV(1.0d);
        bus.setAngle(2.0d);
        assertEquals(1.0d, bbs.getV(), 0.0);
        assertEquals(2.0d, bbs.getAngle(), 0.0);
    }

}
