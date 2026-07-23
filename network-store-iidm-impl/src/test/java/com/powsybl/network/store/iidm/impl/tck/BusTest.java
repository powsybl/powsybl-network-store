/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractBusBreakerTest;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusTest extends AbstractBusBreakerTest {
    @Test
    public void testFictitiousNodeBreakerView() {
        Network network = NetworkTest1Factory.create();
        VoltageLevel voltageLevel1 = network.getVoltageLevel("voltageLevel1");
        assertNotNull(voltageLevel1);
        assertSame(TopologyKind.NODE_BREAKER, voltageLevel1.getTopologyKind());
        VoltageLevel.NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        assertEquals(0.0, topology1.getFictitiousP0(0), 0.0);
        assertEquals(0.0, topology1.getFictitiousQ0(0), 0.0);
        topology1.setFictitiousP0(0, 1.0).setFictitiousQ0(0, 2.0);
        assertEquals(1.0, topology1.getFictitiousP0(0), 0.0);
        assertEquals(2.0, topology1.getFictitiousQ0(0), 0.0);

        // update a calculated bus
        Bus vl1CalculatedBus = voltageLevel1.getBusView().getBus("voltageLevel1_0");
        assertNotNull(vl1CalculatedBus);
        assertEquals(1.0, vl1CalculatedBus.getFictitiousP0(), 0.0);
        assertEquals(2.0, vl1CalculatedBus.getFictitiousQ0(), 0.0);
        vl1CalculatedBus.setFictitiousP0(3.0).setFictitiousQ0(5.0);
        assertEquals(3.0, vl1CalculatedBus.getFictitiousP0(), 0.0);
        assertEquals(5.0, vl1CalculatedBus.getFictitiousQ0(), 0.0);

        assertEquals(3.0, topology1.getFictitiousP0(0), 0.0);
        assertEquals(5.0, topology1.getFictitiousQ0(0), 0.0);
    }
}
