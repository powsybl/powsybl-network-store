/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

// TODO : to remove when hvdc line connection/disconnection will succeed

public class HvdcLineTest {
    @Test
    void testConnectDisconnect() {
        Network network = HvdcTestNetwork.createLcc();

        HvdcLine hvdcLine = network.getHvdcLine("L");

        // Check that the HVDC line is connected
        assertHvdcLineConnection(hvdcLine, true, true);

        // Connection fails since it's already connected
        Assertions.assertFalse(hvdcLine.connectConverterStations());

        // Disconnection fails if switches cannot be opened (here, only fictional switches could be opened)
        Assertions.assertFalse(hvdcLine.disconnectConverterStations(SwitchPredicates.IS_NONFICTIONAL.negate().and(SwitchPredicates.IS_OPEN.negate())));

        // Disconnection
        assertFalse(hvdcLine.disconnectConverterStations());
        assertHvdcLineConnection(hvdcLine, false, true);

        // Disconnection fails since it's already disconnected
        Assertions.assertFalse(hvdcLine.disconnectConverterStations());

        // Connection fails if switches cannot be opened (here, only fictional switches could be closed)
        Assertions.assertTrue(hvdcLine.connectConverterStations(SwitchPredicates.IS_NONFICTIONAL.negate()));

        // Connection
        assertFalse(hvdcLine.connectConverterStations());
        assertHvdcLineConnection(hvdcLine, true, true);

        // Disconnect one side
        assertTrue(hvdcLine.disconnectConverterStations(SwitchPredicates.IS_CLOSED_BREAKER, TwoSides.ONE));
        assertHvdcLineConnection(hvdcLine, false, true);

        // Connection on the other side fails since it's still connected
        Assertions.assertTrue(hvdcLine.connectConverterStations(SwitchPredicates.IS_NONFICTIONAL_BREAKER, TwoSides.TWO));
    }

    private void assertHvdcLineConnection(HvdcLine hvdcLine, boolean expectedConnectionOnSide1, boolean expectedConnectionOnSide2) {
        assertEquals(expectedConnectionOnSide1, hvdcLine.getConverterStation1().getTerminal().isConnected());
        assertEquals(expectedConnectionOnSide2, hvdcLine.getConverterStation2().getTerminal().isConnected());
    }
}
