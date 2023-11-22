/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ActivePowerControlExtensionTest {

    private class DummyNetworkListener implements NetworkListener {

        private int nbUpdatedEquipments = 0;

        @Override
        public void onCreation(Identifiable identifiable) {
            // Not tested here
        }

        @Override
        public void beforeRemoval(Identifiable identifiable) {
            // Not tested here
        }

        @Override
        public void afterRemoval(String id) {
            // Not tested here
        }

        @Override
        public void onUpdate(Identifiable identifiable, String s, Object o, Object o1) {
            nbUpdatedEquipments++;
        }

        public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue,
                             Object newValue) {
            nbUpdatedEquipments++;
        }

        @Override
        public void onVariantCreated(String sourceVariantId, String targetVariantId) {
            // Not tested here
        }

        @Override
        public void onVariantRemoved(String variantId) {
            // Not tested here
        }

        public int getNbUpdatedEquipments() {
            return nbUpdatedEquipments;
        }
    }

    @Test
    public void testBatteryActivePowerControlExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        // add dummy listener to check notification
        DummyNetworkListener listener = new DummyNetworkListener();
        network.addListener(listener);

        Battery battery = network.getBattery("battery");
        assertNotNull(battery);

        assertNull(battery.getExtension(ActivePowerControl.class));
        assertEquals(0, battery.getExtensions().size());

        battery.newExtension(ActivePowerControlAdder.class)
                .withParticipate(true)
                .withDroop(0.1)
                .add();

        ActivePowerControl apc = battery.getExtension(ActivePowerControl.class);
        assertNotNull(apc);
        assertEquals(1, battery.getExtensions().size());
        assertEquals(true, apc.isParticipate());
        assertEquals(0.1, apc.getDroop(), 0.0);

        // test update of droop and check notification
        BatteryImpl batteryImpl = (BatteryImpl) battery;
        List<NetworkListener> listeners = batteryImpl.getNetwork().getListeners();
        assertEquals(1, listeners.size());
        assertEquals(0, listener.getNbUpdatedEquipments());

        apc.setDroop(0.2);
        assertEquals(0.2, apc.getDroop(), 0.0);
        assertEquals(1, listener.getNbUpdatedEquipments());

        // test update of participate and check notification
        apc.setParticipate(false);
        assertEquals(false, apc.isParticipate());
        assertEquals(2, listener.getNbUpdatedEquipments());
    }
}
