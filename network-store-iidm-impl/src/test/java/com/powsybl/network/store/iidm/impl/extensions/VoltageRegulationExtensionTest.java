/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.network.store.iidm.impl.BatteryImpl;
import com.powsybl.network.store.iidm.impl.CreateNetworksUtil;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class VoltageRegulationExtensionTest {

    @Test
    public void testBatteryVoltageRegulationExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        // add dummy listener to check notification
        MockNetworkListener listener = new MockNetworkListener();
        network.addListener(listener);

        Battery battery = network.getBattery("battery");
        assertNotNull(battery);

        assertNull(battery.getExtension(VoltageRegulation.class));
        assertEquals(0, battery.getExtensions().size());

        battery.newExtension(VoltageRegulationAdder.class)
            .withRegulatingTerminal(battery.getTerminal())
            .withVoltageRegulatorOn(true)
            .withTargetV(225.0)
            .add();

        VoltageRegulation vr = battery.getExtension(VoltageRegulation.class);
        assertNotNull(vr);
        assertEquals(1, battery.getExtensions().size());
        assertTrue(vr.isVoltageRegulatorOn());
        assertEquals(225.0, vr.getTargetV(), 0.0);

        // test update of target v and check notification
        BatteryImpl batteryImpl = (BatteryImpl) battery;
        List<NetworkListener> listeners = batteryImpl.getNetwork().getListeners();
        assertEquals(1, listeners.size());
        assertEquals(0, listener.getNbUpdatedExtensions());

        vr.setTargetV(130.0);
        assertEquals(130.0, vr.getTargetV(), 0.0);
        assertEquals(1, listener.getNbUpdatedExtensions());

        // test update of voltage regulator and check notification
        vr.setVoltageRegulatorOn(false);
        assertFalse(vr.isVoltageRegulatorOn());
        assertEquals(2, listener.getNbUpdatedExtensions());

        // test update of target v and voltage regulator with same old value and check notification
        vr.setTargetV(130.0);
        assertEquals(2, listener.getNbUpdatedExtensions());
        vr.setVoltageRegulatorOn(false);
        assertEquals(2, listener.getNbUpdatedExtensions());
        vr.setRegulatingTerminal(network.getStaticVarCompensator("SVC2").getTerminal());
        assertEquals(3, listener.getNbUpdatedExtensions());
        // resetting voltage terminal wont change getNbUpdatedExtensions
        vr.setRegulatingTerminal(network.getStaticVarCompensator("SVC2").getTerminal());
        assertEquals(3, listener.getNbUpdatedExtensions());

        // test setting null to regulatingTerminal
        vr.setRegulatingTerminal(null);
        assertEquals(4, listener.getNbUpdatedExtensions());
        assertEquals(battery.getTerminal(), vr.getRegulatingTerminal());
    }

    @Test
    public void testVoltageRegulationGetExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        Battery battery = network.getBattery("battery");
        assertNotNull(battery);

        assertNull(battery.getExtension(Object.class));
        assertNull(battery.getExtensionByName(""));
        assertEquals(0, battery.getExtensions().size());

        battery.newExtension(VoltageRegulationAdder.class)
            .withRegulatingTerminal(battery.getTerminal())
            .withVoltageRegulatorOn(true)
            .withTargetV(225.0)
            .add();

        assertNull(battery.getExtension(Object.class));
        assertNull(battery.getExtensionByName(""));
        assertEquals(1, battery.getExtensions().size());
    }

    @Test
    public void testRegulatingTerminal() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        Battery battery = network.getBattery("battery");
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT1");
        battery.newExtension(VoltageRegulationAdder.class)
            .withRegulatingTerminal(shuntCompensator.getTerminal())
            .withVoltageRegulatorOn(true)
            .withTargetV(225.0)
            .add();
        VoltageRegulation voltageRegulation = battery.getExtension(VoltageRegulation.class);
        assertEquals(shuntCompensator.getTerminal(), voltageRegulation.getRegulatingTerminal());
        shuntCompensator.remove();

        // regulating terminal deleted must relocate to local
        assertEquals(battery.getTerminal(), voltageRegulation.getRegulatingTerminal());
    }
}
