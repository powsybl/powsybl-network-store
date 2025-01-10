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
    // For busbreaker topology,
    // set in busbreakerview when busview cache exists
    // here we use setV and in other tests we use setAngle to test
    // all combinations of topology and update order
    public void testBusBreakerSetVUpdateVoltageLevel() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");

        // Update voltage using BusView calculated bus, should also set the BusBreakerView configured bus immediately
        l1.getTerminal1().getBusView().getBus().setV(222);

        // Verify voltage update in BusBreakerView configured bus
        assertEquals("Voltage should match in BusBreakerView after update in BusView", 222, l1.getTerminal1().getBusBreakerView().getBus().getV(), 0.0);

        // Set voltage using BusBreakerView configured bus, should set the existing cache of the BusView calculated bus immediately
        // deterministic when the cache is existing
        l1.getTerminal1().getBusBreakerView().getBus().setV(400.0);

        // Verify voltage update in pre-existing cached BusView calculated bus
        assertEquals("Voltage should match in BusView after update in BusBreakerView", 400.0, l1.getTerminal1().getBusView().getBus().getV(), 0.0);
    }

    @Test
    // For nodebreaker topology
    // set in busbreakerview when busview cache doesn't exist,
    // and then set in busview when busbreakerview cache exists
    // here we use setV and in other tests we use setAngle to test
    // all combinations of topology and update order
    public void testNodeBreakerSetVUpdateVoltageLevel() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");

        // Update voltage using BusBreakerView calculated bus, with no cache for BusView calculated bus so no immediate update.
        // non deterministic in general but this test happens to have a one to one mapping between the busview and the busbreakerview
        l1.getTerminal1().getBusBreakerView().getBus().setV(222);

        // Verify the voltage update in BusView calculated bus, here it should getV from BusBreakerView calculated bus when creating the cache
        assertEquals("Voltage should match in BusBreakerView after second update in BusView", 222, l1.getTerminal1().getBusView().getBus().getV(), 0.0);

        // Set voltage using BusView calculated bus, should setV in the existing BusBreakerView calculated bus cache immediately
        l1.getTerminal1().getBusView().getBus().setV(400.0);

        // Verify voltage update in pre-existing cached BusBreakerView calculated bus
        assertEquals("Voltage should match in BusBreakerView after update in BusView", 400.0, l1.getTerminal1().getBusBreakerView().getBus().getV(), 0.0);
    }

    @Test
    // For busbreaker topology,
    // set in busbreakerview when busview cache doesn't exist
    // here we use setAngle and in other tests we use setV to test
    // all combinations of topology and update order
    public void testBusBreakerSetAngleUpdateVoltageLevel() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");

        // Update angle using BusBreakerView configured bus, with no cache for BusView calculated bus so no immediate update.
        // non deterministic, so we need to update all buses which are actually connected electrically together
        // In this test, all switches in vl1 are closed so update all the buses
        l1.getTerminal1().getVoltageLevel().getBusBreakerView().getBuses().forEach(bus -> bus.setAngle(111));

        // Verify the angle update in BusView calculated bus, here it should getAngle from BusBreakerView configured bus when creating the cache
        assertEquals("Angle should match in BusView after update in BusBreakerView", 111, l1.getTerminal1().getBusView().getBus().getAngle(), 0.0);

        // Set angle using BusView calculated bus, should setAngle in the BusBreakerView configured bus immediately
        l1.getTerminal1().getBusView().getBus().setAngle(400.0);

        // Verify Angle update in BusBreakerView configured bus
        assertEquals("Angle should match in BusView after update in BusBreakerView", 400.0, l1.getTerminal1().getBusView().getBus().getAngle(), 0.0);
    }

    @Test
    // For nodebreaker topology
    // set in busiew when busbreakervview cache doesn't exist,
    // and then set in busbreakerview when busview cache exists
    // here we use setAngle and in other tests we use setV to test
    // all combinations of topology and update order
    public void testNodeBreakerSetAngleUpdateVoltageLevel() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");

        // Update angle using BusView calculated bus, with no cache for BusBreakerView calculated bus so no immediate update.
        l1.getTerminal1().getBusView().getBus().setAngle(222);

        // Verify the angle update in BusBreakerView calculated bus, here it should getV from BusView calculated bus when creating the cache
        assertEquals("Angle should match in BusBreakerView after second update in BusView", 222, l1.getTerminal1().getBusBreakerView().getBus().getAngle(), 0.0);

        // Set angle using BusBreakerView calculated bus, should also setAngle in the existing BusView calculated bus cache immediately
        // deterministic when the cache is existing
        l1.getTerminal1().getBusBreakerView().getBus().setAngle(400.0);

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
