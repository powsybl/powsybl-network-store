/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class ComponentTest {

    @Test
    public void testBusBreakerComponent() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        assertEquals(1, vl1.getBusView().getBusStream().collect(Collectors.toList()).size());
        assertEquals(1, vl2.getBusView().getBusStream().collect(Collectors.toList()).size());

        assertEquals(5, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl2.getBusView().getBus("VL2_0").getConnectedTerminalCount());

        testBusComponent(vl1.getBusView().getMergedBus("B1"), ComponentConstants.MAIN_NUM, 2);
        testBusComponent(vl1.getBusView().getMergedBus("B2"), ComponentConstants.MAIN_NUM, 2);
        testBusComponent(vl1.getBusView().getMergedBus("B3"), ComponentConstants.MAIN_NUM, 2);

        testBusComponent(vl2.getBusView().getMergedBus("B21"), ComponentConstants.MAIN_NUM, 2);

        Bus mergedBus1 = vl1.getBusView().getBus("VL1_0");
        testBusComponent(mergedBus1, ComponentConstants.MAIN_NUM, 2);

        Bus mergedBus2 = vl2.getBusView().getBus("VL2_0");
        testBusComponent(mergedBus2, ComponentConstants.MAIN_NUM, 2);

        Line line = network.getLine("L1");

        line.getTerminal1().disconnect();
        testBusComponent(vl2.getBusView().getMergedBus("B21"), ComponentConstants.MAIN_NUM, 1);
        testBusComponent(mergedBus2, ComponentConstants.MAIN_NUM, 1);

        assertEquals(ComponentConstants.MAIN_NUM, line.getTerminal2().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("G2").getTerminal().getBusView().getBus().getConnectedComponent().getNum());

        line.getTerminal1().connect();
        line.getTerminal2().disconnect();
        testBusComponent(vl1.getBusView().getMergedBus("B1"), ComponentConstants.MAIN_NUM, 1);
        testBusComponent(vl1.getBusView().getMergedBus("B2"), ComponentConstants.MAIN_NUM, 1);
        testBusComponent(vl1.getBusView().getMergedBus("B3"), ComponentConstants.MAIN_NUM, 1);
        testBusComponent(mergedBus1, ComponentConstants.MAIN_NUM, 1);

        assertEquals(ComponentConstants.MAIN_NUM, line.getTerminal1().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("G").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("LD1").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("LD2").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("LD3").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
    }

    @Test
    public void testConfiguredBusComponent() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        assertEquals(3, vl1.getBusBreakerView().getBusStream().collect(Collectors.toList()).size());
        assertEquals(1, vl2.getBusBreakerView().getBusStream().collect(Collectors.toList()).size());

        assertEquals(3, vl1.getBusBreakerView().getBus("B1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B2").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B3").getConnectedTerminalCount());
        assertEquals(2, vl2.getBusBreakerView().getBus("B21").getConnectedTerminalCount());

        testBusComponent(vl1.getBusBreakerView().getBus("B1"), ComponentConstants.MAIN_NUM, 2);
        testBusComponent(vl1.getBusBreakerView().getBus("B2"), ComponentConstants.MAIN_NUM, 2);
        testBusComponent(vl1.getBusBreakerView().getBus("B3"), ComponentConstants.MAIN_NUM, 2);
        testBusComponent(vl2.getBusBreakerView().getBus("B21"), ComponentConstants.MAIN_NUM, 2);

        Line line = network.getLine("L1");

        line.getTerminal1().disconnect();
        testBusComponent(vl2.getBusBreakerView().getBus("B21"), ComponentConstants.MAIN_NUM, 1);

        assertEquals(ComponentConstants.MAIN_NUM, line.getTerminal2().getBusBreakerView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("G2").getTerminal().getBusBreakerView().getBus().getConnectedComponent().getNum());

        line.getTerminal1().connect();
        line.getTerminal2().disconnect();
        testBusComponent(vl1.getBusBreakerView().getBus("B1"), ComponentConstants.MAIN_NUM, 1);
        testBusComponent(vl1.getBusBreakerView().getBus("B2"), ComponentConstants.MAIN_NUM, 1);
        testBusComponent(vl1.getBusBreakerView().getBus("B3"), ComponentConstants.MAIN_NUM, 1);

        assertEquals(ComponentConstants.MAIN_NUM, line.getTerminal1().getBusBreakerView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("G").getTerminal().getBusBreakerView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("LD1").getTerminal().getBusBreakerView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("LD2").getTerminal().getBusBreakerView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("LD3").getTerminal().getBusBreakerView().getBus().getConnectedComponent().getNum());
    }

    @Test
    public void testNodeBreakerComponent() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        assertEquals(1, vl1.getBusView().getBusStream().collect(Collectors.toList()).size());
        assertEquals(1, vl2.getBusView().getBusStream().collect(Collectors.toList()).size());

        assertEquals(4, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(3, vl2.getBusView().getBus("VL2_0").getConnectedTerminalCount());

        testBusComponent(vl1.getBusView().getBus("VL1_0"), ComponentConstants.MAIN_NUM, 2);
        testBusComponent(vl2.getBusView().getBus("VL2_0"), ComponentConstants.MAIN_NUM, 2);

        Line line = network.getLine("L1");
        line.getTerminal1().disconnect();
        testBusComponent(vl1.getBusView().getBus("VL1_0"), ComponentConstants.MAIN_NUM, 1);
        testBusComponent(vl2.getBusView().getBus("VL2_0"), 1, 1);

        assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("G").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("L").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getBusbarSection("BBS1").getTerminal().getBusView().getBus().getConnectedComponent().getNum());

        assertEquals(1, line.getTerminal2().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(1, network.getLoad("LD").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(1, network.getBusbarSection("BBS2").getTerminal().getBusView().getBus().getConnectedComponent().getNum());

        line.getTerminal1().connect();
        line.getTerminal2().disconnect();
        testBusComponent(vl1.getBusView().getBus("VL1_0"), ComponentConstants.MAIN_NUM, 1);
        testBusComponent(vl2.getBusView().getBus("VL2_0"), 1, 1);

        assertEquals(ComponentConstants.MAIN_NUM, line.getTerminal1().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("G").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("L").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(ComponentConstants.MAIN_NUM, network.getBusbarSection("BBS1").getTerminal().getBusView().getBus().getConnectedComponent().getNum());

        assertEquals(1, network.getLoad("LD").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
        assertEquals(1, network.getBusbarSection("BBS2").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
    }

    private void testBusComponent(Bus bus, int componentNum, int componentSize) {
        if (ComponentConstants.MAIN_NUM == componentNum) {
            assertTrue(bus.isInMainConnectedComponent());
            assertTrue(bus.isInMainSynchronousComponent());
        } else {
            assertFalse(bus.isInMainConnectedComponent());
            assertFalse(bus.isInMainSynchronousComponent());
        }

        Component connectedComponent = bus.getConnectedComponent();
        assertEquals(componentSize, connectedComponent.getBusStream().count());
        assertEquals(componentSize, connectedComponent.getSize());
        assertEquals(componentNum, connectedComponent.getNum());

        Component synchronousComponent = bus.getSynchronousComponent();
        assertEquals(componentSize, synchronousComponent.getBusStream().count());
        assertEquals(componentSize, synchronousComponent.getSize());
        assertEquals(componentNum, synchronousComponent.getNum());
    }
}
