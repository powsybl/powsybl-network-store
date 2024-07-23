/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractConnectableTest;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ConnectableTest extends AbstractConnectableTest {

    @Override
    @Test
    public void nominallyConnectedTest() {
        // Network creation
        Network network = createNetwork();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line1 = network.getLine("L1");
        Switch switch1 = network.getSwitch("B_L1_1");
        Switch switch2 = network.getSwitch("B_L1_2");

        // Line1 is fully connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        checkLineConnection(line1, true);
        assertFalse(switch1.isOpen());
        assertFalse(switch2.isOpen());

        // Failing disconnection
        assertFalse(line1.disconnect(SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER));
        assertFalse(switch1.isOpen());
        assertFalse(switch2.isOpen());

        // Check that line1 is still fully connected
        line1.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));

        // disconnect the line 1
        assertTrue(line1.disconnect());

        // check line 1 is disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        checkLineConnection(line1, false);
        assertTrue(switch1.isOpen());
        assertTrue(switch2.isOpen());

        // disconnect the already fully disconnected line 1
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        network.getReportNodeContext().pushReportNode(reportNode);
        assertFalse(line1.disconnect());
        network.getReportNodeContext().popReportNode();
        assertEquals("alreadyDisconnectedTerminal", reportNode.getChildren().get(0).getMessageKey());
        assertTrue(switch1.isOpen());
        assertTrue(switch2.isOpen());

        // Failing reconnection of the line 1
        assertFalse(line1.connect(SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER));

        // Check that line1 is still fully disconnected
        line1.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));

        // Reconnect the line 1
        assertTrue(line1.connect());

        // check line 1 is connected
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        checkLineConnection(line1, true);

        // One of the two switches has been closed to connect the line
        assertFalse(switch1.isOpen() && switch2.isOpen());
        assertTrue(switch1.isOpen() || switch2.isOpen());
    }

    private void checkLineConnection(Line line, boolean shouldBeConnected) {
        if (shouldBeConnected) {
            line.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        } else {
            line.getTerminals().forEach(terminal -> assertNull(terminal.getBusView().getBus()));
        }
        line.getTerminals().forEach(terminal -> assertEquals(shouldBeConnected, terminal.isConnected()));
    }

    @Override
    @Test
    public void partiallyConnectedTest() {
        // Network creation
        Network network = createNetwork();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line2 = network.getLine("L2");
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("twt");

        // Line1 and twt are fully connected
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTrue(topo.getOptionalTerminal(6).isPresent());
        assertNotNull(line2.getTerminals().get(1).getBusView().getBus());
        assertNull(twt.getTerminals().get(0).getBusView().getBus());
        assertNotNull(twt.getTerminals().get(1).getBusView().getBus());
        assertNotNull(twt.getTerminals().get(2).getBusView().getBus());
        assertFalse(line2.getTerminals().get(0).isConnected());
        assertTrue(line2.getTerminals().get(1).isConnected());
        assertFalse(twt.getTerminals().get(0).isConnected());
        assertTrue(twt.getTerminals().get(1).isConnected());
        assertTrue(twt.getTerminals().get(2).isConnected());

        // Failing connection
        assertFalse(line2.connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER));

        // connect the line 2
        assertTrue(line2.connect(SwitchPredicates.IS_BREAKER_OR_DISCONNECTOR));

        // check line 2 is connected
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        line2.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line2.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));

        // connect the already fully connected line 2
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        network.getReportNodeContext().pushReportNode(reportNode);
        assertFalse(line2.connect());
        network.getReportNodeContext().popReportNode();
        assertEquals("alreadyConnectedTerminal", reportNode.getChildren().get(0).getMessageKey());

        // Disconnect the twt
        assertTrue(twt.disconnect(SwitchPredicates.IS_CLOSED_BREAKER));

        // check twt is disconnected
        assertTrue(topo.getOptionalTerminal(6).isPresent());
        twt.getTerminals().forEach(terminal -> assertNull(terminal.getBusView().getBus()));
        twt.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));
    }
}
