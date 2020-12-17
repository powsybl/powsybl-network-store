/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
//public class TerminalTest extends AbstractNodeBreakerTest {
public class TerminalTest {

    @Test
    public void testBusBreakerTopology() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Terminal gt = network.getGenerator("G").getTerminal();
        Terminal l1t = network.getLine("L1").getTerminal1();

        assertTrue(gt.isConnected());
        assertTrue(l1t.isConnected());

        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(0, vl1.getBusView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(gt.getBusView().getBus(), gt.getBusView().getConnectableBus());
        assertEquals(l1t.getBusView().getBus(), l1t.getBusView().getConnectableBus());

        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBusStream().filter(b -> b instanceof CalculatedBus).count());
        assertEquals(gt.getBusBreakerView().getBus(), gt.getBusBreakerView().getConnectableBus());
        assertEquals(l1t.getBusBreakerView().getBus(), l1t.getBusBreakerView().getConnectableBus());

        Bus configuredBus = gt.getBusBreakerView().getBus();
        assertNotNull(configuredBus);
        assertTrue(gt.disconnect());
        assertFalse(gt.isConnected());
        assertNull(gt.getBusView().getBus());
        assertNotNull(gt.getBusView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), gt.getBusView().getConnectableBus());
        assertNull(gt.getBusBreakerView().getBus());
        assertNotNull(gt.getBusBreakerView().getConnectableBus());
        assertEquals(configuredBus, gt.getBusBreakerView().getConnectableBus());

        assertTrue(l1t.disconnect());
        assertFalse(l1t.isConnected());
        assertEquals(0, vl1.getBusView().getBusStream().count()); // Because no line in the VL
        assertNull(l1t.getBusView().getBus());
        assertNull(l1t.getBusView().getConnectableBus()); //
        assertEquals(vl1.getBusView().getBus("VL1_0"), l1t.getBusView().getConnectableBus());
        assertNull(l1t.getBusBreakerView().getBus());
        assertNotNull(l1t.getBusBreakerView().getConnectableBus());

        assertTrue(l1t.connect());
        assertTrue(l1t.isConnected());
        assertTrue(gt.connect());
        assertTrue(gt.isConnected());

        assertTrue(assertThrows(AssertionError.class, () -> {
            gt.getBusBreakerView().setConnectableBus("TOTO");
        }).getMessage().contains("TOTO not found"));

        assertTrue(assertThrows(AssertionError.class, () -> {
            gt.getBusBreakerView().setConnectableBus("B2");
        }).getMessage().contains("G is connected"));

        gt.getBusBreakerView().setConnectableBus("B1");
        assertTrue(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());

        assertTrue(gt.disconnect());

        gt.getBusBreakerView().setConnectableBus("B2");
        assertFalse(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());
        assertEquals("B2", gt.getBusBreakerView().getConnectableBus().getId());

        assertFalse(gt.isConnected());

        assertTrue(gt.connect());
        assertEquals(gt.getBusBreakerView().getBus(), gt.getBusBreakerView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), gt.getBusView().getConnectableBus());
    }

    @Test
    public void testNodeBreakerTopology() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Terminal lt = network.getLoad("L").getTerminal();
        Terminal gt = network.getGenerator("G").getTerminal();
        Terminal ldt1 = network.getLine("L1").getTerminal1();
        Terminal ldt2 = network.getLine("L1").getTerminal2();
        Terminal bbs1t = network.getBusbarSection("BBS1").getTerminal();

        assertTrue(lt.isConnected());
        assertTrue(gt.isConnected());
        assertTrue(ldt1.isConnected());
        assertTrue(ldt2.isConnected());
        assertTrue(bbs1t.isConnected());

        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(0, vl1.getBusView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(lt.getBusView().getBus(), lt.getBusView().getConnectableBus());
        assertEquals(gt.getBusView().getBus(), gt.getBusView().getConnectableBus());
        assertEquals(ldt1.getBusView().getBus(), ldt1.getBusView().getConnectableBus());
        assertEquals(ldt1.getBusView().getBus(), ldt1.getBusView().getConnectableBus());
        assertEquals(bbs1t.getBusView().getBus(), bbs1t.getBusView().getConnectableBus());

        assertEquals(1, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(lt.getBusBreakerView().getBus(), lt.getBusBreakerView().getConnectableBus());
        assertEquals(gt.getBusBreakerView().getBus(), gt.getBusBreakerView().getConnectableBus());
        assertEquals(ldt1.getBusBreakerView().getBus(), ldt1.getBusBreakerView().getConnectableBus());
        assertEquals(ldt1.getBusBreakerView().getBus(), ldt1.getBusBreakerView().getConnectableBus());
        assertEquals(bbs1t.getBusBreakerView().getBus(), bbs1t.getBusBreakerView().getConnectableBus());

        assertFalse(lt.disconnect());
        assertTrue(lt.isConnected()); // because of D1 which is not openable

        assertFalse(bbs1t.disconnect());
        assertTrue(bbs1t.isConnected()); // because busbar section is not deconnectable

        assertTrue(gt.disconnect());
        assertFalse(gt.isConnected());
        assertNull(gt.getBusView().getBus());

        assertNotNull(gt.getBusView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), gt.getBusView().getConnectableBus());

        assertEquals(2, vl1.getBusBreakerView().getBusStream().count());
        assertNotNull(gt.getBusBreakerView().getConnectableBus());
        assertEquals(vl1.getBusBreakerView().getBus("VL1_0"), gt.getBusBreakerView().getConnectableBus());

        assertTrue(gt.connect());
        assertTrue(gt.isConnected());

        assertTrue(ldt1.disconnect());
        assertFalse(ldt1.isConnected());
        assertNull(ldt1.getBusView().getBus());

        assertNotNull(ldt1.getBusView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), ldt1.getBusView().getConnectableBus());

        assertEquals(2, vl1.getBusBreakerView().getBusStream().count());
        assertNotNull(ldt1.getBusBreakerView().getConnectableBus());
        assertEquals(vl1.getBusBreakerView().getBus("VL1_1"), ldt1.getBusBreakerView().getConnectableBus());

        assertTrue(assertThrows(PowsyblException.class, () -> {
            gt.getBusBreakerView().setConnectableBus("TOTO");
        }).getMessage().contains("Not supported in a node breaker topology"));
    }

}
