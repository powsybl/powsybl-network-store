/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class EmptyCalculatedBusBugTest {

    @Test
    public void testCalculatedBuses1() {
        Network network = CreateNetworksUtil.createEmptyNodeBreakerNetwork();
        VoltageLevel vl = network.getVoltageLevel("VL");

        assertEquals(0, vl.getBusView().getBusStream().count());

        assertEquals(2, vl.getBusBreakerView().getBusStream().count());
    }

    @Test
    public void testCalculatedBuses2() {
        Network network = CreateNetworksUtil.createEmptyNodeBreakerNetwork();
        VoltageLevel vl = network.getVoltageLevel("VL");
        vl.getNodeBreakerView()
                .newInternalConnection()
                .setNode1(1)
                .setNode2(2)
                .add();

        Load l1 = vl.newLoad()
                .setId("L1")
                .setNode(0)
                .setP0(100.0)
                .setQ0(50.0)
                .add();

        assertEquals(0, vl.getBusView().getBusStream().count());
        assertNull(l1.getTerminal().getBusView().getBus());

        assertEquals(2, vl.getBusBreakerView().getBusStream().count());
        assertNotNull(l1.getTerminal().getBusBreakerView().getBus());
    }

}
