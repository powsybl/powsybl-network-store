/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractLineTest;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryCurrentLimitAttributes;
import org.junit.Test;

import java.util.TreeMap;

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

    //TODO: there is a similar test in the TCK tests. A CurrentLimitsTest extends AbstractCurrentLimitsTest should be created and this test can be deleted.
    // The TCK test doesn't pass yet. As is, the network-store implementation of setV(v) on buses is not consistent. We have problems with the views we are working on (BusBreakerView or BusView).
    @Test
    public void isOverloadedTest() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");
        l1.getTerminal1().setP(10);
        l1.getTerminal1().setQ(0);
        l1.getTerminal1().getBusView().getBus().setV(400.0);
        assertFalse(l1.isOverloaded());

        l1.getTerminal1().setP(400);
        l1.setCurrentLimits(Branch.Side.ONE, new LimitsAttributes(40, null));
        assertTrue(l1.isOverloaded());

        TreeMap<Integer, TemporaryCurrentLimitAttributes> temporaryLimits = new TreeMap<>();
        temporaryLimits.put(0, new TemporaryCurrentLimitAttributes("TempLimit1", 1000, 5, false));
        l1.setCurrentLimits(Branch.Side.ONE, new LimitsAttributes(40, temporaryLimits));
        l1.setCurrentLimits(Branch.Side.TWO, new LimitsAttributes(40, temporaryLimits));
        assertEquals(5, l1.getOverloadDuration());

        assertTrue(l1.checkPermanentLimit(Branch.Side.ONE, LimitType.CURRENT));
        assertTrue(l1.checkPermanentLimit1(LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit(Branch.Side.TWO, LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit2(LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit(Branch.Side.ONE, LimitType.APPARENT_POWER));
        assertFalse(l1.checkPermanentLimit(Branch.Side.TWO, LimitType.ACTIVE_POWER));
        assertThrows(UnsupportedOperationException.class, () -> l1.checkPermanentLimit(Branch.Side.TWO, LimitType.VOLTAGE));

        Branch.Overload overload = l1.checkTemporaryLimits(Branch.Side.ONE, LimitType.CURRENT);
        assertEquals("TempLimit1", overload.getTemporaryLimit().getName());
        assertEquals(40.0, overload.getPreviousLimit(), 0);
        assertEquals(5, overload.getTemporaryLimit().getAcceptableDuration());
        assertNull(l1.checkTemporaryLimits(Branch.Side.TWO, LimitType.CURRENT));

        temporaryLimits.put(0, new TemporaryCurrentLimitAttributes("TempLimit1", 20, 5, false));
        assertEquals(Integer.MAX_VALUE, l1.getOverloadDuration());
    }
}
