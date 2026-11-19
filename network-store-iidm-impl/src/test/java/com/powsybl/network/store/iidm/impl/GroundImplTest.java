/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GroundImplTest {

    @Test
    void testSetFictitiousNotifiesUpdate() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();
        NetworkListener mockedListener = mock(NetworkListener.class);
        network.addListener(mockedListener);

        GroundImpl ground = (GroundImpl) network.getGround("ground");
        assertFalse(ground.isFictitious());

        // no change: no notification
        ground.setFictitious(false);

        // set fictitious to true to test notification update
        ground.getResource().getAttributes().setFictitious(true);
        ground.setFictitious(false);
        verify(mockedListener).onUpdate(ground, "fictitious", "InitialState", true, false);

        assertFalse(ground.isFictitious());
    }
}
