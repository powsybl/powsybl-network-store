/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractLineTest;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class LineTest extends AbstractLineTest {

    @Test
    public void testTieLineAdder() {
        // FIXME TO FIX LATER
    }

    @Test
    public void baseAcLineTests() {
        // FIXME TO FIX LATER
    }

    private static Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        Line l = network.newLine()
                .setId("L")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .add();
        l.newCurrentLimits1()
                .setPermanentLimit(1000.0)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1600.0)
                .endTemporaryLimit()
                .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        Line l = network.getLine("L");
        assertFalse(l.isOverloaded());
        l.getTerminal1().getBusView().getBus().setV(390.0);
        l.getTerminal1().setP(100.0).setQ(50.0); // i = 165.51212
        assertFalse(Double.isNaN(l.getTerminal1().getI()));
        assertFalse(l.isOverloaded());
        assertFalse(l.checkPermanentLimit1(LimitType.CURRENT));
        assertNull(l.checkTemporaryLimits1(LimitType.CURRENT));

        l.getTerminal1().setP(800.0).setQ(400.0); // i = 1324.0969
        assertTrue(l.isOverloaded());
        assertEquals(5 * 60L, l.getOverloadDuration());
        assertTrue(l.checkPermanentLimit1(LimitType.CURRENT));
        assertNotNull(l.checkTemporaryLimits1(LimitType.CURRENT));
        assertEquals(5 * 60L, l.checkTemporaryLimits1(LimitType.CURRENT).getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0, l.checkTemporaryLimits1(LimitType.CURRENT).getPreviousLimit(), 0.0);

        l.getTerminal1().setP(900.0).setQ(500.0); // i = 1524.1499
        assertEquals(60, l.getOverloadDuration());
        assertNotNull(l.checkTemporaryLimits1(LimitType.CURRENT));
        assertEquals(60, l.checkTemporaryLimits1(LimitType.CURRENT).getTemporaryLimit().getAcceptableDuration());
        assertEquals(1400.0, l.checkTemporaryLimits1(LimitType.CURRENT).getPreviousLimit(), 0.0);
    }
}
