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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class ThreeWindingsTransformerFortescueExtensionTest {

    @Test
    void testTwtFortescueExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        // add dummy listener to check notification
        DummyNetworkListener listener = new DummyNetworkListener();
        network.addListener(listener);

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("TWT1");
        assertNull(twt.getExtension(ThreeWindingsTransformerFortescue.class));
        assertEquals(0, twt.getExtensions().size());

        twt.newExtension(ThreeWindingsTransformerFortescueAdder.class)
            .leg1()
            .withRz(1)
            .withXz(2)
            .withConnectionType(WindingConnectionType.DELTA)
            .withFreeFluxes(true)
            .withGroundingR(3)
            .withGroundingX(4)
            .leg2()
            .withRz(5)
            .withXz(6)
            .withConnectionType(WindingConnectionType.Y)
            .withFreeFluxes(false)
            .withGroundingR(7)
            .withGroundingX(8)
            .add();

        ThreeWindingsTransformerFortescue extension = twt.getExtension(ThreeWindingsTransformerFortescue.class);
        assertNotNull(extension);
        assertNotNull(extension.getLeg1());
        ThreeWindingsTransformerFortescue.LegFortescue leg1 = extension.getLeg1();
        assertEquals(1, leg1.getRz());
        assertEquals(2, leg1.getXz());
        assertTrue(leg1.isFreeFluxes());
        assertEquals(WindingConnectionType.DELTA, leg1.getConnectionType());
        assertEquals(3, leg1.getGroundingR());
        assertEquals(4, leg1.getGroundingX());

        assertNotNull(extension.getLeg2());
        ThreeWindingsTransformerFortescue.LegFortescue leg2 = extension.getLeg2();
        assertEquals(5, leg2.getRz());
        assertEquals(6, leg2.getXz());
        assertFalse(leg2.isFreeFluxes());
        assertEquals(WindingConnectionType.Y, leg2.getConnectionType());
        assertEquals(7, leg2.getGroundingR());
        assertEquals(8, leg2.getGroundingX());

        assertNotNull(extension.getLeg3());

        // test update
        ThreeWindingsTransformerImpl twtImpl = (ThreeWindingsTransformerImpl) twt;
        List<NetworkListener> listeners = twtImpl.getNetwork().getListeners();
        assertEquals(1, listeners.size());
        assertEquals(0, listener.getNbUpdatedExtensions());

        // update does not work because LegFortescue is a class and not an interface. wecould extends LegFortescue to be debated
        //        extension.getLeg1().setRz(8);
        //        assertEquals(8, extension.getLeg1().getRz());
        //        assertEquals(1, listener.getNbUpdatedExtensions());
        //
        //        extension.getLeg2().setConnectionType(WindingConnectionType.Y_GROUNDED);
        //        assertEquals(WindingConnectionType.Y_GROUNDED, extension.getLeg2().getConnectionType());
        //        assertEquals(2, listener.getNbUpdatedExtensions());
    }

    @Test
    void testTwtFortescueGetExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("TWT1");
        assertNotNull(twt);

        assertNull(twt.getExtension(Object.class));
        assertNull(twt.getExtensionByName(""));
        assertEquals(0, twt.getExtensions().size());

        twt.newExtension(ThreeWindingsTransformerFortescueAdder.class)
            .leg1()
            .withRz(1)
            .withXz(2)
            .withConnectionType(WindingConnectionType.DELTA)
            .withFreeFluxes(true)
            .withGroundingR(3)
            .withGroundingX(4)
            .leg2()
            .withRz(5)
            .withXz(6)
            .withConnectionType(WindingConnectionType.Y)
            .withFreeFluxes(false)
            .withGroundingR(7)
            .withGroundingX(8)
            .add();

        assertNull(twt.getExtension(Object.class));
        assertNull(twt.getExtensionByName(""));
        assertNotNull(twt.getExtension(ThreeWindingsTransformerFortescue.class));
        assertNotNull(twt.getExtensionByName(ThreeWindingsTransformerFortescue.NAME));
        assertEquals(1, twt.getExtensions().size());
    }
}
