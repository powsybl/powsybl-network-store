/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.tck.AbstractLineTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LineTest extends AbstractLineTest {

    @Override
    @Test
    public void testChangesNotification() {
        // This is an adaptation of the same test method in powsybl-core
        // to be removed when changing the R value will notify the update with the variant id provided (instead of null) in powsybl-core

        Network network = NoEquipmentNetworkFactory.create();

        // Changes listener
        NetworkListener exceptionListener = mock(DefaultNetworkListener.class);
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onUpdate(any(), anyString(), anyString(), any(), any());

        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(exceptionListener);
        network.addListener(mockedListener);

        // Tested instance
        Line acLine = network.newLine()
            .setId("line")
            .setName("lineName")
            .setR(1.0)
            .setX(2.0)
            .setG1(3.0)
            .setG2(3.5)
            .setB1(4.0)
            .setB2(4.5)
            .setVoltageLevel1("vl1")
            .setVoltageLevel2("vl2")
            .setBus1("busA")
            .setBus2("busB")
            .setConnectableBus1("busA")
            .setConnectableBus2("busB")
            .add();
        verify(mockedListener, times(1)).onCreation(acLine);
        // Get initial values
        double p0OldValue = acLine.getTerminal1().getP();
        double q0OldValue = acLine.getTerminal1().getQ();
        // Change values P1 & Q1
        acLine.getTerminal1().setP(1.0);
        acLine.getTerminal1().setQ(Math.sqrt(2.0));

        // Check update notification
        verify(mockedListener, times(1)).onUpdate(acLine, "p1", INITIAL_VARIANT_ID, p0OldValue, 1.0);
        verify(mockedListener, times(1)).onUpdate(acLine, "q1", INITIAL_VARIANT_ID, q0OldValue, Math.sqrt(2.0));

        // Change values that not depend on the variant
        acLine.setR(1.5);

        // Here is the change from the powsybl-core version
        //verify(mockedListener, times(1)).onUpdate(acLine, "r", null, 1.0, 1.5);
        verify(mockedListener, times(1)).onUpdate(acLine, "r", INITIAL_VARIANT_ID, 1.0, 1.5);

        // Simulate exception for onUpdate calls
        doThrow(new PowsyblException())
            .when(mockedListener)
            .onUpdate(any(Line.class), anyString(), anyString(), anyDouble(), anyDouble());
        // Change P1 value
        try {
            acLine.getTerminal1().setP(1.1);
            verify(mockedListener, times(1)).onUpdate(acLine, "p1", INITIAL_VARIANT_ID, 1.0, 1.1);
        } catch (PowsyblException notExpected) {
            fail();
        }

        // At this point
        // no more changes is taking into account

        // Case when same values P1 & Q1 are set
        acLine.getTerminal1().setP(1.1);
        acLine.getTerminal1().setQ(Math.sqrt(2.0));
        // Case when no listener is registered
        network.removeListener(mockedListener);
        acLine.getTerminal1().setP(2.0);
        acLine.getTerminal1().setQ(1.0);

        // Check no notification
        verifyNoMoreInteractions(mockedListener);
    }
}
