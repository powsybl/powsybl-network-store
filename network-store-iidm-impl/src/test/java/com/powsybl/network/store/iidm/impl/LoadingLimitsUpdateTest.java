/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoadingLimitsUpdateTest {

    @Test
    void testSetPermanentLimitAndTemporaryLimitValueNotify() {
        Network network = CreateNetworksUtil.createBusBreakerNetwokWithMultipleEquipments();
        NetworkListener mockedListener = mock(NetworkListener.class);

        LineImpl line = (LineImpl) network.getLine("LINE1");
        line.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
            .setPermanentLimit(100)
            .beginTemporaryLimit().setName("tl").setAcceptableDuration(60).setValue(200).endTemporaryLimit()
            .add();

        network.addListener(mockedListener);

        CurrentLimits limits = line.getCurrentLimits1().orElseThrow();
        limits.setPermanentLimit(150);
        assertEquals(150, line.getCurrentLimits1().orElseThrow().getPermanentLimit(), 0.0);
        verify(mockedListener).onUpdate(line, "permanentLimit", "InitialState", 100.0, 150.0);

        clearInvocations(mockedListener);

        limits.setTemporaryLimitValue(60, 300);
        assertEquals(300, line.getCurrentLimits1().orElseThrow().getTemporaryLimitValue(60), 0.0);
        verify(mockedListener).onUpdate(line, "temporaryLimitValue[60]", "InitialState", 200.0, 300.0);
    }
}
