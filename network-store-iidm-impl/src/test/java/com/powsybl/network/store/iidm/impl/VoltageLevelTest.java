/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class VoltageLevelTest {

    @Test
    public void testBusBreakerSetVUpdateVoltageLevel() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");

        // Update voltage using BusView
        l1.getTerminal1().getBusView().getBus().setV(222);

        // Verify the voltage update in BusBreakerView
        assertEquals("Voltage should match in BusBreakerView after update in BusView", 222, l1.getTerminal1().getBusBreakerView().getBus().getV(), 0.0);

        // Set voltage using BusBreakerView
        l1.getTerminal1().getBusBreakerView().getBus().setV(400.0);

        // Verify voltage update in BusView
        assertEquals("Voltage should match in BusView after update in BusBreakerView", 400.0, l1.getTerminal1().getBusView().getBus().getV(), 0.0);
    }

    @Test
    public void testNodeBreakerSetVUpdateVoltageLevel() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");

        // Update voltage using BusBreakerView
        l1.getTerminal1().getBusBreakerView().getBus().setV(222);

        // Verify the voltage update in BusView
        assertEquals("Voltage should match in BusBreakerView after second update in BusView", 222, l1.getTerminal1().getBusView().getBus().getV(), 0.0);

        // Set voltage using BusView
        l1.getTerminal1().getBusView().getBus().setV(400.0);

        // Verify voltage update in BusBreakerView
        assertEquals("Voltage should match in BusBreakerView after update in BusView", 400.0, l1.getTerminal1().getBusBreakerView().getBus().getV(), 0.0);
    }

    @Test
    public void testBusBreakerSetAngleUpdateVoltageLevel() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");

        // Update angle using BusView
        l1.getTerminal1().getBusView().getBus().setAngle(111);

        // Verify the angle update in BusBreakerView
        assertEquals("Angle should match in BusView after update in BusBreakerView", 111, l1.getTerminal1().getBusBreakerView().getBus().getAngle(), 0.0);

        // Set angle using BusBreakerView
        l1.getTerminal1().getBusBreakerView().getBus().setAngle(400.0);

        // Verify Angle update in BusView
        assertEquals("Angle should match in BusView after update in BusBreakerView", 400.0, l1.getTerminal1().getBusView().getBus().getAngle(), 0.0);
    }

    @Test
    public void testNodeBreakerSetAngleUpdateVoltageLevel() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");

        // Update angle using BusBreakerView
        l1.getTerminal1().getBusBreakerView().getBus().setAngle(222);

        // Verify the angle update in BusView
        assertEquals("Angle should match in BusBreakerView after second update in BusView", 222, l1.getTerminal1().getBusView().getBus().getAngle(), 0.0);

        // Set angle using BusView
        l1.getTerminal1().getBusView().getBus().setAngle(400.0);

        // Verify angle update in BusBreakerView
        assertEquals("Angle should match in BusBreakerView after update in BusView", 400.0, l1.getTerminal1().getBusBreakerView().getBus().getAngle(), 0.0);
    }

    @Test
    public void testBusBreakerConnectables() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();

        assertEquals(network.getLine("L1"), network.getVoltageLevel("VL1").getConnectable("L1", Line.class));
        assertEquals(network.getGenerator("G"), network.getVoltageLevel("VL1").getConnectable("G", Generator.class));
        assertEquals(network.getLoad("LD1"), network.getVoltageLevel("VL1").getConnectable("LD1", Load.class));
        assertEquals(network.getLoad("LD2"), network.getVoltageLevel("VL1").getConnectable("LD2", Load.class));
        assertEquals(network.getLoad("LD3"), network.getVoltageLevel("VL1").getConnectable("LD3", Load.class));
    }

    @Test
    public void testNodeBreakerConnectables() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        assertEquals(network.getLine("L1"), network.getVoltageLevel("VL1").getConnectable("L1", Line.class));
        assertEquals(network.getGenerator("G"), network.getVoltageLevel("VL1").getConnectable("G", Generator.class));
        assertEquals(network.getLoad("L"), network.getVoltageLevel("VL1").getConnectable("L", Load.class));
        assertEquals(network.getBusbarSection("BBS1"), network.getVoltageLevel("VL1").getConnectable("BBS1", BusbarSection.class));
    }

    @Test
    public void testBusbarSectionPositions() {
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

        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection()
            .setId("idBBS")
            .setName("nameBBS")
            .setNode(0)
            .add();
        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(0).withSectionIndex(0).add();
        assertNotNull(network.getVoltageLevel("VL"));
        assertNotNull(network.getVoltageLevel("VL").getNodeBreakerView().getBusbarSection("idBBS"));

        BusbarSectionPositionAdder busbarSectionPositionAdder = bbs.newExtension(BusbarSectionPositionAdder.class);
        assertEquals("Busbar section 'idBBS': Busbar index has to be greater or equals to zero",
            assertThrows(ValidationException.class, busbarSectionPositionAdder::add).getMessage());

        busbarSectionPositionAdder = bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(0);
        assertEquals("Busbar section 'idBBS': Section index has to be greater or equals to zero",
            assertThrows(ValidationException.class, busbarSectionPositionAdder::add).getMessage());

        busbarSectionPositionAdder = bbs.newExtension(BusbarSectionPositionAdder.class).withSectionIndex(0);
        assertEquals("Busbar section 'idBBS': Busbar index has to be greater or equals to zero",
            assertThrows(ValidationException.class, busbarSectionPositionAdder::add).getMessage());

        busbarSectionPositionAdder = bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(-1).withSectionIndex(0);
        assertEquals("Busbar section 'idBBS': Busbar index has to be greater or equals to zero",
            assertThrows(ValidationException.class, busbarSectionPositionAdder::add).getMessage());

        busbarSectionPositionAdder = bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(0).withSectionIndex(-1);
        assertEquals("Busbar section 'idBBS': Section index has to be greater or equals to zero",
            assertThrows(ValidationException.class, busbarSectionPositionAdder::add).getMessage());

        busbarSectionPositionAdder = bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(-1).withSectionIndex(-1);
        assertEquals("Busbar section 'idBBS': Busbar index has to be greater or equals to zero",
            assertThrows(ValidationException.class, busbarSectionPositionAdder::add).getMessage());
    }

    @Test
    public void testWithMultipleBusInBusBreakerAndBusView() {
        Network network = Network.create("test_mcc", "test");
        Substation s1 = network.newSubstation()
            .setId("A")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("B")
            .setNominalV(225.0)
            .setLowVoltageLimit(0.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("C")
            .setNode(0)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("D")
            .setKind(SwitchKind.DISCONNECTOR)
            .setRetained(false)
            .setOpen(false)
            .setNode1(0)
            .setNode2(1)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("E")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(false)
            .setNode1(1)
            .setNode2(2)
            .add();

        Substation s2 = network.newSubstation()
            .setId("F")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
            .setId("G")
            .setNominalV(225.0)
            .setLowVoltageLimit(0.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl2.getNodeBreakerView().newBusbarSection()
            .setId("H")
            .setNode(0)
            .add();
        vl2.getNodeBreakerView().newBusbarSection()
            .setId("I")
            .setNode(1)
            .add();
        vl2.getNodeBreakerView().newSwitch()
            .setId("J")
            .setKind(SwitchKind.DISCONNECTOR)
            .setRetained(true)
            .setOpen(false)
            .setNode1(0)
            .setNode2(2)
            .add();
        vl2.getNodeBreakerView().newSwitch()
            .setId("K")
            .setKind(SwitchKind.DISCONNECTOR)
            .setRetained(true)
            .setOpen(false)
            .setNode1(1)
            .setNode2(3)
            .add();
        vl2.getNodeBreakerView().newSwitch()
            .setId("L")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(false)
            .setNode1(2)
            .setNode2(3)
            .add();
        vl2.getNodeBreakerView().newSwitch()
            .setId("M")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(false)
            .setNode1(0)
            .setNode2(4)
            .add();
        vl2.getNodeBreakerView().newBusbarSection()
            .setId("newBbs1")
            .setNode(5)
            .add();
        vl2.getNodeBreakerView().newSwitch()
            .setId("newDisconnector1")
            .setKind(SwitchKind.DISCONNECTOR)
            .setRetained(true)
            .setOpen(false)
            .setNode1(5)
            .setNode2(6)
            .add();
        vl2.getNodeBreakerView().newSwitch()
            .setId("newBreaker1")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(false)
            .setNode1(6)
            .setNode2(7)
            .add();
        vl2.newGenerator()
            .setId("newGen1")
            .setNode(7)
            .setMaxP(100.0)
            .setMinP(50.0)
            .setTargetP(100.0)
            .setTargetV(400.0)
            .setVoltageRegulatorOn(true)
            .add();
        network.newLine()
            .setId("N")
            .setR(0.001)
            .setX(0.1)
            .setG1(0.0)
            .setB1(0.0)
            .setG2(0.0)
            .setB2(0.0)
            .setVoltageLevel1("B")
            .setNode1(2)
            .setVoltageLevel2("G")
            .setNode2(4)
            .add();

        // set 2 different V values for the 2 buses in the bus view of voltage level vl2
        assertNotNull(vl2.getBusView().getBus("G_0"));
        vl2.getBusView().getBus("G_0").setV(230.);
        assertNotNull(vl2.getBusView().getBus("G_5"));
        vl2.getBusView().getBus("G_5").setV(250.);

        // 2 buses in the bus breaker view of voltage level vl2 have V value = 230
        assertNotNull(vl2.getBusBreakerView().getBus("G_0"));
        assertEquals(230., vl2.getBusBreakerView().getBus("G_0").getV(), 0.0);
        assertNotNull(vl2.getBusBreakerView().getBus("G_1"));
        assertEquals(230., vl2.getBusBreakerView().getBus("G_1").getV(), 0.0);

        // the 4 other buses in the bus breaker view of voltage level vl2 have V value = 250
        assertNotNull(vl2.getBusBreakerView().getBus("G_2"));
        assertEquals(250., vl2.getBusBreakerView().getBus("G_2").getV(), 0.0);
        assertNotNull(vl2.getBusBreakerView().getBus("G_3"));
        assertEquals(250., vl2.getBusBreakerView().getBus("G_3").getV(), 0.0);
        assertNotNull(vl2.getBusBreakerView().getBus("G_5"));
        assertEquals(250., vl2.getBusBreakerView().getBus("G_5").getV(), 0.0);
        assertNotNull(vl2.getBusBreakerView().getBus("G_6"));
        assertEquals(250., vl2.getBusBreakerView().getBus("G_6").getV(), 0.0);

        // set 2 different V values for 2 buses in the bus breaker view of voltage level vl2, which correspond to 2
        // different buses in the bus view
        vl2.getBusBreakerView().getBus("G_1").setV(270.);
        vl2.getBusBreakerView().getBus("G_5").setV(290.);

        assertEquals(270., vl2.getBusView().getBus("G_0").getV(), 0.0);
        assertEquals(290., vl2.getBusView().getBus("G_5").getV(), 0.0);
    }
}
