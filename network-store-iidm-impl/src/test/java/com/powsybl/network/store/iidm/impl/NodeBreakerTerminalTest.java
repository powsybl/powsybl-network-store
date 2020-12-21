/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractNodeBreakerTest;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class NodeBreakerTerminalTest extends AbstractNodeBreakerTest {

    @Test
    public void connectDisconnectRemove() {
        Network network;
        try {
            Method createNetwork = AbstractNodeBreakerTest.class.getDeclaredMethod("createNetwork");
            createNetwork.setAccessible(true);
            network = (Network) createNetwork.invoke(this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new PowsyblException(e);
        }

        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL").getNodeBreakerView();
        Load l = network.getLoad("L");
        Generator g = network.getGenerator("G");

        // generator is connected, load is disconnected
        assertTrue(topo.getOptionalTerminal(2).isPresent());
        assertTrue(topo.getOptionalTerminal(3).isPresent());
        assertNotNull(g.getTerminal().getBusView().getBus());
        assertNull(l.getTerminal().getBusView().getBus());
        assertTrue(g.getTerminal().isConnected());
        assertFalse(l.getTerminal().isConnected());

        // connect the load
        assertTrue(l.getTerminal().connect());

        // check load is connected
        assertTrue(topo.getOptionalTerminal(2).isPresent());
        assertNotNull(l.getTerminal().getBusView().getBus());
        assertTrue(l.getTerminal().isConnected());

        // disconnect the generator
        g.getTerminal().disconnect();

        // check generator is disconnected
        assertTrue(topo.getOptionalTerminal(3).isPresent());
        assertNull(g.getTerminal().getBusView().getBus());
        assertFalse(g.getTerminal().isConnected());
    }

    @Test
    public void testBusView() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Terminal lt = network.getLoad("L").getTerminal();
        Terminal gt = network.getGenerator("G").getTerminal();
        Terminal ldt1 = network.getLine("L1").getTerminal1();
        Terminal bbs1t = network.getBusbarSection("BBS1").getTerminal();

        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(0, vl1.getBusView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(lt.getBusView().getBus(), lt.getBusView().getConnectableBus());
        assertEquals(gt.getBusView().getBus(), gt.getBusView().getConnectableBus());
        assertEquals(ldt1.getBusView().getBus(), ldt1.getBusView().getConnectableBus());
        assertEquals(ldt1.getBusView().getBus(), ldt1.getBusView().getConnectableBus());
        assertEquals(bbs1t.getBusView().getBus(), bbs1t.getBusView().getConnectableBus());

        assertFalse(lt.disconnect());
        assertTrue(lt.isConnected()); // because of D1 which is not openable

        assertFalse(bbs1t.disconnect());
        assertTrue(bbs1t.isConnected()); // because busbar section is not deconnectable

        assertTrue(gt.disconnect());
        assertFalse(gt.isConnected());
        assertNull(gt.getBusView().getBus());
        assertNotNull(gt.getBusView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), gt.getBusView().getConnectableBus());
        assertTrue(gt.connect());
        assertTrue(gt.isConnected());

        assertTrue(ldt1.disconnect());
        assertFalse(ldt1.isConnected());
        assertNull(ldt1.getBusView().getBus());
        assertNotNull(ldt1.getBusView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), ldt1.getBusView().getConnectableBus());
    }

    @Test
    public void testBusBreakerView() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Terminal lt = network.getLoad("L").getTerminal();
        Terminal gt = network.getGenerator("G").getTerminal();
        Terminal ldt1 = network.getLine("L1").getTerminal1();
        Terminal bbs1t = network.getBusbarSection("BBS1").getTerminal();

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
        assertEquals(2, vl1.getBusBreakerView().getBusStream().count());
        assertNotNull(gt.getBusBreakerView().getConnectableBus());
        assertEquals(vl1.getBusBreakerView().getBus("VL1_0"), gt.getBusBreakerView().getConnectableBus());
        assertTrue(gt.connect());
        assertTrue(gt.isConnected());

        assertTrue(ldt1.disconnect());
        assertFalse(ldt1.isConnected());
        assertEquals(2, vl1.getBusBreakerView().getBusStream().count());
        assertNotNull(ldt1.getBusBreakerView().getConnectableBus());
        assertEquals(vl1.getBusBreakerView().getBus("VL1_1"), ldt1.getBusBreakerView().getConnectableBus());

        Terminal.BusBreakerView gtbbv = gt.getBusBreakerView();
        assertTrue(assertThrows(PowsyblException.class, () -> {
            gtbbv.setConnectableBus("FOO");
        }).getMessage().contains("Not supported in a node breaker topology"));
        Terminal.BusBreakerView bbs1tbbv = bbs1t.getBusBreakerView();
        assertTrue(assertThrows(PowsyblException.class, () -> {
            bbs1tbbv.setConnectableBus("FOO");
        }).getMessage().contains("Not supported in a node breaker topology"));
    }
}
