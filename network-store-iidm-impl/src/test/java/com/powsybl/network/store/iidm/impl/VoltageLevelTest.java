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
        assertEquals("Voltage should match in BusView after update in BusBreakerView", 222, l1.getTerminal1().getBusBreakerView().getBus().getV(), 0.0);

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
}
