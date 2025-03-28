/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.tck.AbstractConnectionNotificationTest;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ConnectionNotificationTest extends AbstractConnectionNotificationTest {
    @Override
    @Test
    // Test overrided from powsybl-core, to add the notification on 'calculatedBusesValid' attribute change
    public void nodeBreakerTest() {
        var network = FourSubstationsNodeBreakerFactory.create();
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        var l1 = network.getLine("LINE_S2S3");

        assertTrue(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                new UpdateNetworkEvent("LINE_S2S3", "beginDisconnect", "InitialState", false, null),
                new UpdateNetworkEvent("S2VL1_LINES2S3_BREAKER", "open", "InitialState", false, true),
                new UpdateNetworkEvent("S2VL1", "calculatedBusesValid", "InitialState", true, false), // in addition to the test in powsybl-core
                new UpdateNetworkEvent("LINE_S2S3", "endDisconnect", "InitialState", null, true)),
            eventRecorder.getEvents());

        eventRecorder.reset();
        assertFalse(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                new UpdateNetworkEvent("LINE_S2S3", "beginDisconnect", "InitialState", true, null),
                new UpdateNetworkEvent("LINE_S2S3", "endDisconnect", "InitialState", null, true)),
            eventRecorder.getEvents());

        eventRecorder.reset();
        assertTrue(l1.getTerminal1().connect());
        assertEquals(List.of(
                new UpdateNetworkEvent("LINE_S2S3", "beginConnect", "InitialState", false, null),
                new UpdateNetworkEvent("S2VL1_LINES2S3_BREAKER", "open", "InitialState", true, false),
                new UpdateNetworkEvent("S2VL1", "calculatedBusesValid", "InitialState", true, false), // in addition to the test in powsybl-core
                new UpdateNetworkEvent("LINE_S2S3", "endConnect", "InitialState", null, true)),
            eventRecorder.getEvents());

        eventRecorder.reset();
        assertFalse(l1.getTerminal1().connect());
        assertEquals(List.of(
                new UpdateNetworkEvent("LINE_S2S3", "beginConnect", "InitialState", true, null),
                new UpdateNetworkEvent("LINE_S2S3", "endConnect", "InitialState", null, true)),
            eventRecorder.getEvents());
    }
}
