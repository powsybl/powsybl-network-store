/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerDisconnectionDiamondPathBugTest {

    /**
     * <pre>
     *     L
     *     |
     *  ---1---
     *  |     |
     * BR1   BR2
     *  |     |
     *  ---0--- BBS1</pre>
     */
    private Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.newLoad()
                .setId("L")
                .setNode(1)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR1")
                .setNode1(1)
                .setNode2(0)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR2")
                .setNode1(1)
                .setNode2(0)
                .setOpen(false)
                .add();
        return network;
    }

    /**
     * <pre>
     *     L
     *     |
     *   2 |-------
     *     |      |
     *  -------   |
     *  |     |   D1
     * BR1   D2   |                     LA
     *  |     |   |                      |
     *  ---1---   |                      4
     *     |      |                      |
     *    BR2     |                     BR4
     *     |      |                      |
     *  ---0-------   -----BR3-----   ---3---
     *    BBS1                         BBS2</pre>
     */
    private Network createNetwork2() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(3)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR3")
                .setNode1(0)
                .setNode2(3)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR4")
                .setNode1(3)
                .setNode2(4)
                .setOpen(false)
                .add();
        vl.newLoad()
                .setId("LA")
                .setNode(4)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.newLoad()
                .setId("L")
                .setNode(2)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR1")
                .setNode1(1)
                .setNode2(2)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D2")
                .setNode1(1)
                .setNode2(2)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR2")
                .setNode1(1)
                .setNode2(0)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D1")
                .setNode1(0)
                .setNode2(2)
                .setOpen(false)
                .add();
        return network;
    }

    /**
     * <pre>
     *    L1                   L2
     *     |                    |
     *     1                    3
     *     |                    |
     *    D1                   D2
     *     |                    |
     *  ---0---  ----BR----  ---2---
     *    BBS1                BBS2</pre>
     */
    private Network createNetwork3() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(2)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR")
                .setNode1(0)
                .setNode2(2)
                .setOpen(false)
                .add();
        vl.newLoad()
                .setId("L1")
                .setNode(1)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.newLoad()
                .setId("L2")
                .setNode(3)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D1")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D2")
                .setNode1(2)
                .setNode2(3)
                .setOpen(false)
                .add();
        return network;
    }

    @Test
    public void testDisconnect() {
        Network network = createNetwork();
        Load l = network.getLoad("L");
        assertTrue(l.getTerminal().isConnected());
        assertTrue(l.getTerminal().disconnect());
        assertFalse(l.getTerminal().isConnected());
    }

    @Test
    public void testDisconnect2() {
        Network network = createNetwork2();
        Load l = network.getLoad("L");
        assertTrue(l.getTerminal().isConnected());
        assertFalse(l.getTerminal().disconnect());
        assertTrue(l.getTerminal().isConnected()); // because of D1 which is not openable
    }

    @Test
    public void testDisconnect3() {
        Network network = createNetwork3();
        Switch s = network.getSwitch("BR");
        assertFalse(s.isOpen());
        Load l1 = network.getLoad("L1");
        assertTrue(l1.getTerminal().isConnected());
        assertFalse(l1.getTerminal().disconnect());
        assertFalse(s.isOpen());
        assertTrue(l1.getTerminal().isConnected()); // because of D1 which is not openable
    }
}
