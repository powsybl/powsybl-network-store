/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class BusBreakerTerminalTest {

    @Test
    public void testBusView() {
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

        assertTrue(gt.disconnect());
        assertFalse(gt.isConnected());
        assertNull(gt.getBusView().getBus());
        assertNotNull(gt.getBusView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), gt.getBusView().getConnectableBus());

        assertTrue(l1t.disconnect());
        assertFalse(l1t.isConnected());
        assertEquals(0, vl1.getBusView().getBusStream().count()); // Because no line in the VL
        assertNull(l1t.getBusView().getBus());
        assertNull(l1t.getBusView().getConnectableBus()); //
        assertEquals(vl1.getBusView().getBus("VL1_0"), l1t.getBusView().getConnectableBus());
        assertTrue(l1t.connect());
        assertTrue(l1t.isConnected());

        assertTrue(gt.connect());
        assertTrue(gt.isConnected());
        assertEquals(vl1.getBusView().getBus("VL1_0"), gt.getBusView().getConnectableBus());
    }

    @Test
    public void testBusBreakerView() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Terminal gt = network.getGenerator("G").getTerminal();
        Terminal l1t = network.getLine("L1").getTerminal1();

        assertTrue(gt.isConnected());
        assertTrue(l1t.isConnected());

        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBusStream().filter(b -> b instanceof CalculatedBus).count());
        assertEquals(gt.getBusBreakerView().getBus(), gt.getBusBreakerView().getConnectableBus());
        assertEquals(l1t.getBusBreakerView().getBus(), l1t.getBusBreakerView().getConnectableBus());

        Bus configuredBus = gt.getBusBreakerView().getBus();
        assertNotNull(configuredBus);
        assertTrue(gt.disconnect());
        assertFalse(gt.isConnected());
        assertNull(gt.getBusBreakerView().getBus());
        assertNotNull(gt.getBusBreakerView().getConnectableBus());
        assertEquals(configuredBus, gt.getBusBreakerView().getConnectableBus());
        assertTrue(gt.connect());
        assertTrue(gt.isConnected());
        assertEquals(gt.getBusBreakerView().getBus(), gt.getBusBreakerView().getConnectableBus());

        assertTrue(l1t.disconnect());
        assertFalse(l1t.isConnected());
        assertNull(l1t.getBusBreakerView().getBus());
        assertNotNull(l1t.getBusBreakerView().getConnectableBus());
        assertTrue(l1t.connect());
        assertTrue(l1t.isConnected());
    }

    @Test
    public void testBusBreakerViewSetConnectable() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Terminal gt = network.getGenerator("G").getTerminal();
        assertTrue(gt.isConnected());
        Terminal l1t = network.getLine("L1").getTerminal1();
        Terminal l2t = network.getLine("L1").getTerminal2();

        Terminal.BusBreakerView gtbbv = gt.getBusBreakerView();
        assertTrue(assertThrows(AssertionError.class, () -> {
            gtbbv.setConnectableBus("FOO");
        }).getMessage().contains("FOO not found"));
        assertTrue(assertThrows(AssertionError.class, () -> {
            gtbbv.setConnectableBus("B2");
        }).getMessage().contains("G is connected"));

        gt.getBusBreakerView().setConnectableBus("B1");
        assertTrue(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());

        assertTrue(gt.disconnect());
        assertFalse(gt.isConnected());
        gt.getBusBreakerView().setConnectableBus("B2");
        assertFalse(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());
        assertEquals("B2", gt.getBusBreakerView().getConnectableBus().getId());

        assertTrue(gt.connect());
        assertTrue(gt.isConnected());
        assertEquals(gt.getBusBreakerView().getBus(), gt.getBusBreakerView().getConnectableBus());

        assertTrue(l1t.disconnect());
        assertFalse(l1t.isConnected());
        l1t.getBusBreakerView().setConnectableBus("B2");
        assertFalse(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());
        assertEquals("B2", l1t.getBusBreakerView().getConnectableBus().getId());

        assertTrue(l2t.disconnect());
        assertFalse(l2t.isConnected());
        l2t.getBusBreakerView().setConnectableBus("B21");
        assertFalse(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());
        assertEquals("B21", l2t.getBusBreakerView().getConnectableBus().getId());
    }
}
