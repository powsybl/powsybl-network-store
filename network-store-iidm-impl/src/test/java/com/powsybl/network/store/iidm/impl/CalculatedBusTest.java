/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.tck.AbstractCalculatedTopologyTest;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class CalculatedBusTest extends AbstractCalculatedTopologyTest {

    private void addBusBarSection(VoltageLevel vl) {
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS12")
                .setNode(10)
                .add();
        vl.newGenerator()
                .setId("G12")
                .setNode(11)
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BRS12")
                .setNode1(0)
                .setNode2(10)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR12")
                .setNode1(10)
                .setNode2(11)
                .setOpen(false)
                .add();

    }

    @Test
    public void testCalculatedBusesNodeBreaker() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        assertEquals(1, vl1.getBusView().getBusStream().count());
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(1, vl1.getBusBreakerView().getBusStream().count());

        addBusBarSection(vl1);

        // BusView
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(5, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());

        // BusBreakerView
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(1, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(5, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());

        vl1.getNodeBreakerView().getSwitch("BRS12").setRetained(true);

        // BusView
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(0, vl1.getBusView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(5, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        Bus b1 = vl1.getBusView().getMergedBus("BBS1");
        Bus b2 = vl1.getBusView().getMergedBus("BBS12");
        assertEquals(b1, b2);
        assertEquals(5, b1.getConnectedTerminalCount());

        // BusBreakerView
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(2, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(3, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getSwitchCount());

        vl1.getNodeBreakerView().getSwitch("BRS12").setRetained(false);

        // BusView
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(5, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        b1 = vl1.getBusView().getMergedBus("BBS1");
        b2 = vl1.getBusView().getMergedBus("BBS12");
        assertEquals(b1, b2);
        assertEquals(5, b1.getConnectedTerminalCount());

        // BusBreakerView
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(1, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(5, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());

        vl1.getNodeBreakerView().getSwitch("BRS12").setOpen(true);

        // BusView
        assertEquals(2, vl1.getBusView().getBusStream().count());
        assertEquals(3, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusView().getBus("VL1_1").getConnectedTerminalCount());
        b1 = vl1.getBusView().getMergedBus("BBS1");
        b2 = vl1.getBusView().getMergedBus("BBS12");
        assertNotEquals(b1, b2);
        assertEquals(3, b1.getConnectedTerminalCount());
        assertEquals(2, b2.getConnectedTerminalCount());

        // BusBreakerView
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(2, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_1").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());

        vl1.getNodeBreakerView().getSwitch("BR1").setOpen(true);

        // BusView
        assertEquals(2, vl1.getBusView().getBusStream().count());
        assertEquals(2, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusView().getBus("VL1_1").getConnectedTerminalCount());
        b1 = vl1.getBusView().getMergedBus("BBS1");
        b2 = vl1.getBusView().getMergedBus("BBS12");
        assertNotEquals(b1, b2);
        assertEquals(2, b1.getConnectedTerminalCount());
        assertEquals(2, b2.getConnectedTerminalCount());

        // BusBreakerView
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_1").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_2").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());

        vl1.getNodeBreakerView().getSwitch("BR2").setOpen(true);

        // BusView
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(2, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        b1 = vl1.getBusView().getMergedBus("BBS12");
        assertEquals(2, b1.getConnectedTerminalCount());

        // BusBreakerView
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(4, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(2, vl1.getBusBreakerView().getBus("VL1_1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_2").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_3").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());

        vl1.getNodeBreakerView().getSwitch("BR12").setOpen(true);

        // BusView
        assertEquals(0, vl1.getBusView().getBusStream().count());

        // BusBreakerView
        ((VoltageLevelImpl) vl1).invalidateCalculatedBuses();                        // TODO is it the normal behaviour ?
        assertEquals(5, vl1.getBusBreakerView().getBusStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_2").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_3").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("VL1_4").getConnectedTerminalCount());
        assertEquals(0, vl1.getBusBreakerView().getSwitchCount());

        assertThrows(AssertionError.class, () -> {
            vl1.getBusView().getMergedBus("TOTO");
        });
    }

    @Test
    public void testCalculatedBusesBusBreaker() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithMultiBuses();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        List<Bus> calculatedBuses = vl1.getBusView().getBusStream().collect(Collectors.toList());
        List<Bus> configurededBuses = vl1.getBusBreakerView().getBusStream().collect(Collectors.toList());
        assertEquals(0, calculatedBuses.size());
        assertEquals(3, configurededBuses.size());

        assertEquals(0, configurededBuses.stream().filter(b -> b instanceof CalculatedBus).count());

        assertNull(vl1.getBusView().getMergedBus("B1"));
        assertNull(vl1.getBusView().getMergedBus("B2"));
        assertNull(vl1.getBusView().getMergedBus("B3"));
        assertNull(vl1.getBusView().getMergedBus("TOTO"));
    }

    @Test
    public void testCalculatedBusesBusBreakerWithLine() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        List<Bus> calculatedBuses = vl1.getBusView().getBusStream().collect(Collectors.toList());
        List<Bus> configurededBuses = vl1.getBusBreakerView().getBusStream().collect(Collectors.toList());
        assertEquals(1, calculatedBuses.size());
        assertEquals(3, configurededBuses.size());

        assertEquals(0, calculatedBuses.stream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(0, configurededBuses.stream().filter(b -> b instanceof CalculatedBus).count());

        assertNotNull(vl1.getBusView().getMergedBus("B1"));
        assertNotNull(vl1.getBusView().getMergedBus("B2"));
        assertNotNull(vl1.getBusView().getMergedBus("B3"));
        assertNull(vl1.getBusView().getMergedBus("TOTO"));
        assertEquals(2, vl1.getBusBreakerView().getSwitchCount());

        assertThrows(ValidationException.class, () -> vl1.getBusBreakerView().getSwitch("BR1").setRetained(true));

        vl1.getBusBreakerView().getSwitch("BR1").setOpen(true);
        vl1.getBusBreakerView().getSwitch("BR2").setOpen(false);
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());
        assertNotNull(vl1.getBusView().getMergedBus("B1"));
        assertNull(vl1.getBusView().getMergedBus("B2"));
        assertNull(vl1.getBusView().getMergedBus("B3"));

        vl1.getBusBreakerView().getSwitch("BR1").setOpen(false);
        vl1.getBusBreakerView().getSwitch("BR2").setOpen(true);
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());
        assertNotNull(vl1.getBusView().getMergedBus("B1"));
        assertNotNull(vl1.getBusView().getMergedBus("B2"));
        assertNull(vl1.getBusView().getMergedBus("B3"));

        vl1.getBusBreakerView().getSwitch("BR1").setOpen(true);
        vl1.getBusBreakerView().getSwitch("BR2").setOpen(true);
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());
        assertNotNull(vl1.getBusView().getMergedBus("B1"));
        assertNull(vl1.getBusView().getMergedBus("B2"));
        assertNull(vl1.getBusView().getMergedBus("B3"));

        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        calculatedBuses = vl2.getBusView().getBusStream().collect(Collectors.toList());
        configurededBuses = vl2.getBusBreakerView().getBusStream().collect(Collectors.toList());
        assertEquals(1, calculatedBuses.size());
        assertEquals(1, configurededBuses.size());
        assertNotNull(vl2.getBusView().getMergedBus("B21"));
        assertNull(vl2.getBusView().getMergedBus("TOTO"));
        assertEquals(0, vl2.getBusBreakerView().getSwitchCount());
    }
}
