/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.tck.AbstractCalculatedTopologyTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class NodeBreakerCalculatedBusTest extends AbstractCalculatedTopologyTest {

    @Test
    public void testCalculatedBuses() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBusStream().count());

        CreateNetworksUtil.addBusBarSection(vl1);

        // BusView
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(6, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());

        // BusBreakerView
        assertEquals(1, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(6, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());

        VoltageLevel.BusView bv = vl1.getBusView();
        assertThrows(AssertionError.class, () -> {
            bv.getMergedBus("FOO");
        });
    }

    @Test
    public void testCalculatedBusesSwitchRetain() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        CreateNetworksUtil.addBusBarSection(vl1);

        SwitchImpl s = (SwitchImpl) vl1.getNodeBreakerView().getSwitch("BRS12");
        assertEquals(0, s.getNode1());
        assertEquals(10, s.getNode2());
        assertFalse(s.isRetained());

        s.setRetained(true);
        assertTrue(s.isRetained());

        // BusView
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(0, vl1.getBusView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(6, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        Bus b1 = vl1.getBusView().getMergedBus("BBS1");
        Bus b2 = vl1.getBusView().getMergedBus("BBS12");
        assertEquals(b1, b2);
        assertEquals(b1, vl1.getBusView().getBus("VL1_0"));
        assertEquals(b2, vl1.getBusView().getBus("VL1_0"));

        // BusBreakerView
        assertEquals(2, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(4, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_10").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getSwitchCount());

        s.setRetained(false);

        // BusView
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(6, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        b1 = vl1.getBusView().getMergedBus("BBS1");
        b2 = vl1.getBusView().getMergedBus("BBS12");
        assertEquals(b1, b2);
        assertEquals(b1, vl1.getBusView().getBus("VL1_0"));
        assertEquals(b2, vl1.getBusView().getBus("VL1_0"));

        // BusBreakerView
        assertEquals(1, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(6, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());

        s.setRetained(false);
        assertTrue(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());
    }

    @Test
    public void testCalculatedBusesSwitch1() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        CreateNetworksUtil.addBusBarSection(vl1);

        vl1.getNodeBreakerView().getSwitch("BRS12").setOpen(true);

        // BusView
        assertEquals(2, vl1.getBusView().getBusStream().count());
        assertEquals(4, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusView().getBus("VL1_10").getConnectedTerminalCount());
        Bus b1 = vl1.getBusView().getMergedBus("BBS1");
        Bus b2 = vl1.getBusView().getMergedBus("BBS12");
        assertNotEquals(b1, b2);
        assertEquals(b1, vl1.getBusView().getBus("VL1_0"));
        assertEquals(b2, vl1.getBusView().getBus("VL1_10"));

        // BusBreakerView
        assertEquals(2, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(4, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_10").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());
    }

    @Test
    public void testCalculatedBusesSwitch2() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        CreateNetworksUtil.addBusBarSection(vl1);

        vl1.getNodeBreakerView().getSwitch("BRS12").setOpen(true);
        vl1.getNodeBreakerView().getSwitch("BR1").setOpen(true);

        // BusView
        assertEquals(2, vl1.getBusView().getBusStream().count());
        assertEquals(3, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusView().getBus("VL1_10").getConnectedTerminalCount());
        Bus b1 = vl1.getBusView().getMergedBus("BBS1");
        Bus b2 = vl1.getBusView().getMergedBus("BBS12");
        assertNotEquals(b1, b2);
        assertEquals(b1, vl1.getBusView().getBus("VL1_0"));
        assertEquals(b2, vl1.getBusView().getBus("VL1_10"));

        // BusBreakerView
        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_10").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_1").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());
    }

    @Test
    public void testCalculatedBusesSwitch3() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        CreateNetworksUtil.addBusBarSection(vl1);

        vl1.getNodeBreakerView().getSwitch("BRS12").setOpen(true);
        vl1.getNodeBreakerView().getSwitch("BR1").setOpen(true);
        vl1.getNodeBreakerView().getSwitch("BR2").setOpen(true);

        // BusView
        assertEquals(2, vl1.getBusView().getBusStream().count());
        assertEquals(2, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusView().getBus("VL1_10").getConnectedTerminalCount());
        Bus b1 = vl1.getBusView().getMergedBus("BBS1");
        Bus b2 = vl1.getBusView().getMergedBus("BBS12");
        assertNotEquals(b1, b2);
        assertEquals(b1, vl1.getBusView().getBus("VL1_0"));
        assertEquals(b2, vl1.getBusView().getBus("VL1_10"));

        // BusBreakerView
        assertEquals(4, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_1").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_10").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_3").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());
    }

    @Test
    public void testCalculatedBusesSwitch4() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        CreateNetworksUtil.addBusBarSection(vl1);

        vl1.getNodeBreakerView().getSwitch("BRS12").setOpen(true);
        vl1.getNodeBreakerView().getSwitch("BR1").setOpen(true);
        vl1.getNodeBreakerView().getSwitch("BR2").setOpen(true);
        vl1.getNodeBreakerView().getSwitch("BR12").setOpen(true);

        // BusView
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(2, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        Bus b = vl1.getBusView().getMergedBus("BBS1");
        assertEquals(b, vl1.getBusView().getBus("VL1_0"));

        // BusBreakerView
        assertEquals(5, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_10").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_11").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_3").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());
    }

    @Test
    public void testBusViewTerminals() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        assertTrue(((CalculatedBus) vl1.getBusView().getBus("VL1_0")).isBusView());
        assertEquals(4, ((BaseBus) vl1.getBusView().getBus("VL1_0")).getAllTerminalsStream().count());
        assertEquals(4, vl1.getBusView().getBus("VL1_0").getConnectedTerminalStream().count());

        assertEquals(1, vl1.getBusView().getBus("VL1_0").getLineStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getThreeWindingsTransformerStream().count());
        assertEquals(1, vl1.getBusView().getBus("VL1_0").getGeneratorStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getBatteryStream().count());
        assertEquals(1, vl1.getBusView().getBus("VL1_0").getLoadStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getShuntCompensatorStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getDanglingLineStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getStaticVarCompensatorStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getLccConverterStationStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getVscConverterStationStream().count());
        // BUSBAR ?

        assertTrue(((CalculatedBus) vl2.getBusView().getBus("VL2_0")).isBusView());
        assertEquals(3, ((BaseBus) vl2.getBusView().getBus("VL2_0")).getAllTerminalsStream().count());
        assertEquals(3, vl2.getBusView().getBus("VL2_0").getConnectedTerminalStream().count());

        assertEquals(1, vl2.getBusView().getBus("VL2_0").getLineStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getThreeWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getGeneratorStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getBatteryStream().count());
        assertEquals(1, vl2.getBusView().getBus("VL2_0").getLoadStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getShuntCompensatorStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getDanglingLineStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getStaticVarCompensatorStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getLccConverterStationStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getVscConverterStationStream().count());
        // BUSBAR ?

        CreateNetworksUtil.disconnectAllTerminalsVoltageLevel(vl1);

        assertEquals(2, vl1.getBusView().getBus("VL1_0").getConnectedTerminalStream().count());
        assertEquals(1, vl1.getBusView().getBus("VL1_0").getLoadStream().count());
        // BUSBAR ?

        CreateNetworksUtil.disconnectAllTerminalsVoltageLevel(vl2);

        assertEquals(0, vl2.getBusView().getBusStream().count());
    }

    @Test
    public void testBusBreakerViewTerminals() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        assertFalse(((CalculatedBus) vl1.getBusBreakerView().getBus("VL1_0")).isBusView());

        assertEquals(4, ((BaseBus) vl1.getBusBreakerView().getBus("VL1_0")).getAllTerminalsStream().count());

        assertEquals(4, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalStream().count());

        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_0").getLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getThreeWindingsTransformerStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_0").getGeneratorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getBatteryStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_0").getLoadStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getShuntCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getDanglingLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getStaticVarCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getLccConverterStationStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getVscConverterStationStream().count());
        // BUSBAR ?

        assertFalse(((CalculatedBus) vl2.getBusBreakerView().getBus("VL2_0")).isBusView());

        assertEquals(3, ((BaseBus) vl2.getBusBreakerView().getBus("VL2_0")).getAllTerminalsStream().count());

        assertEquals(3, vl2.getBusBreakerView().getBus("VL2_0").getConnectedTerminalStream().count());

        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_0").getLineStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getThreeWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getGeneratorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getBatteryStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_0").getLoadStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getShuntCompensatorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getDanglingLineStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getStaticVarCompensatorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getLccConverterStationStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getVscConverterStationStream().count());
        // BUSBAR ?

        CreateNetworksUtil.disconnectAllTerminalsVoltageLevel(vl1);
        assertEquals(2, ((BaseBus) vl1.getBusBreakerView().getBus("VL1_0")).getAllTerminalsStream().count());
        assertEquals(1, ((BaseBus) vl1.getBusBreakerView().getBus("VL1_1")).getAllTerminalsStream().count());
        assertEquals(1, ((BaseBus) vl1.getBusBreakerView().getBus("VL1_3")).getAllTerminalsStream().count());

        CreateNetworksUtil.disconnectAllTerminalsVoltageLevel(vl2);
        assertEquals(1, ((BaseBus) vl2.getBusBreakerView().getBus("VL2_0")).getAllTerminalsStream().count());
        assertEquals(1, ((BaseBus) vl2.getBusBreakerView().getBus("VL2_1")).getAllTerminalsStream().count());
        assertEquals(1, ((BaseBus) vl2.getBusBreakerView().getBus("VL2_3")).getAllTerminalsStream().count());
    }

    @Test
    public void testBusBreakerViewTerminalsVl1() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        assertFalse(((CalculatedBus) vl1.getBusBreakerView().getBus("VL1_0")).isBusView());

        assertEquals(1, ((BaseBus) vl1.getBusBreakerView().getBus("VL1_0")).getAllTerminalsStream().count());

        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalStream().count());

        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getThreeWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getGeneratorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getBatteryStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getLoadStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_0").getShuntCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getDanglingLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getStaticVarCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getLccConverterStationStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("VL1_0").getVscConverterStationStream().count());

        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_1").getVscConverterStationStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_2").getDanglingLineStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_3").getDanglingLineStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_4").getBatteryStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_5").getLineStream().count());
    }

    @Test
    public void testBusBreakerViewTerminalsVl2() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        assertFalse(((CalculatedBus) vl2.getBusBreakerView().getBus("VL2_0")).isBusView());

        assertEquals(1, ((BaseBus) vl2.getBusBreakerView().getBus("VL2_0")).getAllTerminalsStream().count());

        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_0").getConnectedTerminalStream().count());

        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getLineStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getThreeWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getGeneratorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getBatteryStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getLoadStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getShuntCompensatorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getDanglingLineStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_0").getStaticVarCompensatorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getLccConverterStationStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("VL2_0").getVscConverterStationStream().count());

        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_0").getStaticVarCompensatorStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_1").getLccConverterStationStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_2").getVscConverterStationStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_3").getShuntCompensatorStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("VL2_4").getLineStream().count());
    }

    @Test
    public void testBusBreakerViewTerminalsOtherVls() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();
        VoltageLevel vl3 = network.getVoltageLevel("VL3");
        VoltageLevel vl4 = network.getVoltageLevel("VL4");
        VoltageLevel vl5 = network.getVoltageLevel("VL5");
        VoltageLevel vl6 = network.getVoltageLevel("VL6");

        assertFalse(((CalculatedBus) vl3.getBusBreakerView().getBus("VL3_2")).isBusView());
        assertEquals(1, ((BaseBus) vl3.getBusBreakerView().getBus("VL3_2")).getAllTerminalsStream().count());
        assertEquals(1, vl3.getBusBreakerView().getBus("VL3_2").getConnectedTerminalStream().count());

        assertFalse(((CalculatedBus) vl3.getBusBreakerView().getBus("VL3_2")).isBusView());
        assertEquals(1, ((BaseBus) vl3.getBusBreakerView().getBus("VL3_2")).getAllTerminalsStream().count());
        assertEquals(1, vl3.getBusBreakerView().getBus("VL3_2").getConnectedTerminalStream().count());

        assertFalse(((CalculatedBus) vl4.getBusBreakerView().getBus("VL4_3")).isBusView());
        assertEquals(1, ((BaseBus) vl4.getBusBreakerView().getBus("VL4_3")).getAllTerminalsStream().count());
        assertEquals(1, vl4.getBusBreakerView().getBus("VL4_3").getConnectedTerminalStream().count());

        assertFalse(((CalculatedBus) vl5.getBusBreakerView().getBus("VL5_1")).isBusView());
        assertEquals(1, ((BaseBus) vl5.getBusBreakerView().getBus("VL5_1")).getAllTerminalsStream().count());
        assertEquals(1, vl5.getBusBreakerView().getBus("VL5_1").getConnectedTerminalStream().count());

        assertFalse(((CalculatedBus) vl6.getBusBreakerView().getBus("VL6_2")).isBusView());
        assertEquals(1, ((BaseBus) vl6.getBusBreakerView().getBus("VL6_2")).getAllTerminalsStream().count());
        assertEquals(1, vl6.getBusBreakerView().getBus("VL6_2").getConnectedTerminalStream().count());
    }
}
