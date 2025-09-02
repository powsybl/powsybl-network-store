/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class LineFortescueExtensionTest {

    @Test
    void testLineFortescueExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        // add dummy listener to check notification
        DummyNetworkListener listener = new DummyNetworkListener();
        network.addListener(listener);

        Line line = network.getLine("L1");
        assertNull(line.getExtension(LineFortescue.class));
        assertEquals(0, line.getExtensions().size());

        line.newExtension(LineFortescueAdder.class)
            .withRz(1)
            .withXz(2)
            .withOpenPhaseA(true)
            .withOpenPhaseB(false)
            .withOpenPhaseC(true)
            .add();

        LineFortescue extension = line.getExtension(LineFortescue.class);
        assertNotNull(extension);
        assertEquals(1, extension.getRz());
        assertEquals(2, extension.getXz());
        assertTrue(extension.isOpenPhaseA());
        assertFalse(extension.isOpenPhaseB());
        assertTrue(extension.isOpenPhaseC());

        // test update
        LineImpl lineImpl = (LineImpl) line;
        List<NetworkListener> listeners = lineImpl.getNetwork().getListeners();
        assertEquals(1, listeners.size());
        assertEquals(0, listener.getNbUpdatedExtensions());

        extension.setRz(8);
        assertEquals(8, extension.getRz());
        assertEquals(1, listener.getNbUpdatedExtensions());

        extension.setOpenPhaseA(false);
        assertFalse(extension.isOpenPhaseA());
        assertEquals(2, listener.getNbUpdatedExtensions());
    }

    @Test
    void testLineFortescueGetExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Line line = network.getLine("L1");
        assertNotNull(line);

        assertNull(line.getExtension(Object.class));
        assertNull(line.getExtensionByName(""));
        assertEquals(0, line.getExtensions().size());

        line.newExtension(LineFortescueAdder.class)
            .withRz(1)
            .withXz(2)
            .withOpenPhaseA(true)
            .withOpenPhaseB(false)
            .withOpenPhaseC(true)
            .add();

        assertNull(line.getExtension(Object.class));
        assertNull(line.getExtensionByName(""));
        assertNotNull(line.getExtension(LineFortescue.class));
        assertNotNull(line.getExtensionByName(LineFortescue.NAME));
        assertEquals(1, line.getExtensions().size());
    }
}
