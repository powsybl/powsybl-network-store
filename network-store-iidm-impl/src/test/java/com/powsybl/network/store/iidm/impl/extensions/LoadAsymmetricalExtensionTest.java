/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadAsymmetricalAdder;
import com.powsybl.iidm.network.extensions.LoadConnectionType;
import com.powsybl.network.store.iidm.impl.CreateNetworksUtil;
import com.powsybl.network.store.iidm.impl.LoadImpl;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class LoadAsymmetricalExtensionTest {

    @Getter
    static class DummyNetworkListener implements NetworkListener {

        private int nbUpdatedExtensions = 0;

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

        public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue,
                             Object newValue) {
        }

        @Override
        public void onVariantCreated(String sourceVariantId, String targetVariantId) {
            // Not tested here
        }

        @Override
        public void onVariantRemoved(String variantId) {
            // Not tested here
        }

        @Override
        public void onVariantOverwritten(String sourceVariantId, String targetVariantId) {
            // Not tested here
        }

        @Override
        public void onExtensionCreation(Extension<?> extension) {
        }

        @Override
        public void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        }

        @Override
        public void onExtensionBeforeRemoval(Extension<?> extension) {
        }

        @Override
        public void onExtensionUpdate(Extension<?> extendable, String attribute, String variantId, Object oldValue, Object newValue) {
            nbUpdatedExtensions++;
        }

        @Override
        public void onPropertyAdded(Identifiable<?> identifiable, String key, Object newValue) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onPropertyAdded'");
        }

        @Override
        public void onPropertyReplaced(Identifiable<?> identifiable, String key, Object oldValue, Object newValue) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onPropertyReplaced'");
        }

        @Override
        public void onPropertyRemoved(Identifiable<?> identifiable, String key, Object oldValue) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onPropertyRemoved'");
        }
    }

    @Test
    void testLoadAsymmetricalExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        // add dummy listener to check notification
        DummyNetworkListener listener = new DummyNetworkListener();
        network.addListener(listener);

        Load load = network.getLoad("L");
        assertNull(load.getExtension(LoadAsymmetrical.class));
        assertEquals(0, load.getExtensions().size());

        load.newExtension(LoadAsymmetricalAdder.class)
            .withConnectionType(LoadConnectionType.DELTA)
            .withDeltaPa(1)
            .withDeltaPb(2)
            .withDeltaPc(3)
            .withDeltaQa(4)
            .withDeltaQb(5)
            .withDeltaQc(6)
            .add();

        LoadAsymmetrical extension = load.getExtension(LoadAsymmetrical.class);
        assertNotNull(extension);
        assertEquals(1, extension.getDeltaPa());
        assertEquals(2, extension.getDeltaPb());
        assertEquals(3, extension.getDeltaPc());
        assertEquals(4, extension.getDeltaQa());
        assertEquals(5, extension.getDeltaQb());
        assertEquals(6, extension.getDeltaQc());

        // test update
        LoadImpl loadImpl = (LoadImpl) load;
        List<NetworkListener> listeners = loadImpl.getNetwork().getListeners();
        assertEquals(1, listeners.size());
        assertEquals(0, listener.getNbUpdatedExtensions());

        extension.setDeltaPa(8);
        assertEquals(8, extension.getDeltaPa());
        assertEquals(1, listener.getNbUpdatedExtensions());

        // setting the same value does not update
        extension.setDeltaPa(8);
        assertEquals(8, extension.getDeltaPa());
        assertEquals(1, listener.getNbUpdatedExtensions());

        extension.setConnectionType(LoadConnectionType.Y);
        assertEquals(LoadConnectionType.Y, extension.getConnectionType());
        assertEquals(2, listener.getNbUpdatedExtensions());
    }

    @Test
    void testLoadAsymmetricalGetExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Load load = network.getLoad("L");
        assertNotNull(load);

        assertNull(load.getExtension(Object.class));
        assertNull(load.getExtensionByName(""));
        assertEquals(0, load.getExtensions().size());

        load.newExtension(LoadAsymmetricalAdder.class)
            .withConnectionType(LoadConnectionType.DELTA)
            .withDeltaPa(1)
            .withDeltaPb(2)
            .withDeltaPc(3)
            .withDeltaQa(4)
            .withDeltaQb(5)
            .withDeltaQc(6)
            .add();

        assertNull(load.getExtension(Object.class));
        assertNull(load.getExtensionByName(""));
        assertNotNull(load.getExtension(LoadAsymmetrical.class));
        assertNotNull(load.getExtensionByName(LoadAsymmetrical.NAME));
        assertEquals(1, load.getExtensions().size());
    }
}
