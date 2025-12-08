/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.network.store.iidm.impl.extensions.TwoWindingsTransformerFortescueAdderImplProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class TwoWindingsTransformerFortescueExtensionTest {

    @Test
    void testImplementationName() {
        assertEquals("NetworkStore", new TwoWindingsTransformerFortescueAdderImplProvider().getImplementationName());
    }

    @Test
    void testTwtFortescueExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        // add dummy listener to check notification
        DummyNetworkListener listener = new DummyNetworkListener();
        network.addListener(listener);

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TwoWT1");
        assertNull(twt.getExtension(TwoWindingsTransformerFortescue.class));
        assertEquals(0, twt.getExtensions().size());

        twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
            .withFreeFluxes(true)
            .withRz(1)
            .withXz(2)
            .withConnectionType1(WindingConnectionType.DELTA)
            .withConnectionType2(WindingConnectionType.Y)
            .withGroundingR1(3)
            .withGroundingX1(4)
            .withGroundingR2(5)
            .withGroundingX2(6)
            .add();

        TwoWindingsTransformerFortescue extension = twt.getExtension(TwoWindingsTransformerFortescue.class);
        assertNotNull(extension);
        assertEquals(1, extension.getRz());
        assertEquals(2, extension.getXz());
        assertTrue(extension.isFreeFluxes());
        assertEquals(WindingConnectionType.DELTA, extension.getConnectionType1());
        assertEquals(WindingConnectionType.Y, extension.getConnectionType2());
        assertEquals(3, extension.getGroundingR1());
        assertEquals(4, extension.getGroundingX1());
        assertEquals(5, extension.getGroundingR2());
        assertEquals(6, extension.getGroundingX2());

        // test update
        assertSetters(extension, (TwoWindingsTransformerImpl) twt, listener);
    }

    private static void assertSetters(TwoWindingsTransformerFortescue extension,
                                      TwoWindingsTransformerImpl twtImpl,
                                      DummyNetworkListener listener) {
        List<NetworkListener> listeners = twtImpl.getNetwork().getListeners();
        assertEquals(1, listeners.size());
        assertEquals(0, listener.getNbUpdatedExtensions());

        extension.setRz(8);
        assertEquals(8, extension.getRz());
        assertEquals(1, listener.getNbUpdatedExtensions());

        extension.setConnectionType1(WindingConnectionType.Y_GROUNDED);
        assertEquals(WindingConnectionType.Y_GROUNDED, extension.getConnectionType1());
        assertEquals(2, listener.getNbUpdatedExtensions());

        extension.setXz(9);
        assertEquals(9, extension.getXz());
        assertEquals(3, listener.getNbUpdatedExtensions());

        extension.setConnectionType2(WindingConnectionType.DELTA);
        assertEquals(WindingConnectionType.DELTA, extension.getConnectionType2());
        assertEquals(4, listener.getNbUpdatedExtensions());

        extension.setFreeFluxes(false);
        assertFalse(extension.isFreeFluxes());
        assertEquals(5, listener.getNbUpdatedExtensions());

        extension.setGroundingR1(10);
        assertEquals(10, extension.getGroundingR1());
        assertEquals(6, listener.getNbUpdatedExtensions());

        extension.setGroundingX1(11);
        assertEquals(11, extension.getGroundingX1());
        assertEquals(7, listener.getNbUpdatedExtensions());

        extension.setGroundingR2(12);
        assertEquals(12, extension.getGroundingR2());
        assertEquals(8, listener.getNbUpdatedExtensions());

        extension.setGroundingX2(13);
        assertEquals(13, extension.getGroundingX2());
        assertEquals(9, listener.getNbUpdatedExtensions());

    }

    @Test
    void testTwtFortescueGetExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TwoWT1");
        assertNotNull(twt);

        assertNull(twt.getExtension(Object.class));
        assertNull(twt.getExtensionByName(""));
        assertEquals(0, twt.getExtensions().size());

        twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
            .withFreeFluxes(true)
            .withRz(1)
            .withXz(2)
            .withConnectionType1(WindingConnectionType.DELTA)
            .withConnectionType2(WindingConnectionType.Y)
            .withGroundingR1(3)
            .withGroundingX1(4)
            .withGroundingR2(5)
            .withGroundingX2(6)
            .add();

        assertNull(twt.getExtension(Object.class));
        assertNull(twt.getExtensionByName(""));
        assertNotNull(twt.getExtension(TwoWindingsTransformerFortescue.class));
        assertNotNull(twt.getExtensionByName(TwoWindingsTransformerFortescue.NAME));
        assertEquals(1, twt.getExtensions().size());
    }
}
