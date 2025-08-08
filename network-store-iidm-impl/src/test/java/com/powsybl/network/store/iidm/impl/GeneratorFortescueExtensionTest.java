/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class GeneratorFortescueExtensionTest {

    @Test
    void testGeneratorFortescueExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        // add dummy listener to check notification
        DummyNetworkListener listener = new DummyNetworkListener();
        network.addListener(listener);

        Generator generator = network.getGenerator("G");
        assertNull(generator.getExtension(GeneratorFortescue.class));
        assertEquals(0, generator.getExtensions().size());

        generator.newExtension(GeneratorFortescueAdder.class)
            .withGrounded(true)
            .withRz(1)
            .withXz(2)
            .withRn(3)
            .withXn(4)
            .withGroundingR(5)
            .withGroundingX(6)
            .add();

        GeneratorFortescue extension = generator.getExtension(GeneratorFortescue.class);
        assertNotNull(extension);
        assertEquals(1, extension.getRz());
        assertEquals(2, extension.getXz());
        assertEquals(3, extension.getRn());
        assertEquals(4, extension.getXn());
        assertTrue(extension.isGrounded());
        assertEquals(6, extension.getGroundingX());

        // test update
        GeneratorImpl generatorImpl = (GeneratorImpl) generator;
        List<NetworkListener> listeners = generatorImpl.getNetwork().getListeners();
        assertEquals(1, listeners.size());
        assertEquals(0, listener.getNbUpdatedExtensions());

        extension.setRz(8);
        assertEquals(8, extension.getRz());
        assertEquals(1, listener.getNbUpdatedExtensions());

        extension.setGrounded(false);
        assertFalse(extension.isGrounded());
        assertEquals(2, listener.getNbUpdatedExtensions());
    }

    @Test
    void testGeneratorFortescueGetExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Generator generator = network.getGenerator("G");
        assertNotNull(generator);

        assertNull(generator.getExtension(Object.class));
        assertNull(generator.getExtensionByName(""));
        assertEquals(0, generator.getExtensions().size());

        generator.newExtension(GeneratorFortescueAdder.class)
            .withGrounded(true)
            .withRz(1)
            .withXz(2)
            .withRn(3)
            .withXn(4)
            .withGroundingR(5)
            .withGroundingX(6)
            .add();

        assertNull(generator.getExtension(Object.class));
        assertNull(generator.getExtensionByName(""));
        assertNotNull(generator.getExtension(GeneratorFortescue.class));
        assertNotNull(generator.getExtensionByName(GeneratorFortescue.NAME));
        assertEquals(1, generator.getExtensions().size());
    }
}
