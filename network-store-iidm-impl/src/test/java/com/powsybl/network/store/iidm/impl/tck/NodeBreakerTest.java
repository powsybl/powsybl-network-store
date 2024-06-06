/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractNodeBreakerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerTest extends AbstractNodeBreakerTest {

    @Test
    public void testCalculatedBus() {
        Network network = createIsolatedLoadNetwork();

        Bus busL0 = network.getLoad("L0").getTerminal().getBusBreakerView().getBus();
        assertNotNull(busL0);
        assertEquals("VL", busL0.getVoltageLevel().getId());
        assertEquals("VL_0", busL0.getId());

        assertNull(network.getBusBreakerView().getBus("unknownBus"));

        network.getVoltageLevel("VL").getNodeBreakerView().newBusbarSection().setId("VL_0").setNode(10).add();
        busL0 = network.getLoad("L0").getTerminal().getBusBreakerView().getBus();
        assertEquals("VL_0", busL0.getId());
    }

    private static Network createIsolatedLoadNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL").setNominalV(1f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel vl2 = s1.newVoltageLevel().setId("VL2").setNominalV(1f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl1.getNodeBreakerView()
                .newBusbarSection()
                .setId("B0")
                .setNode(0)
                .add();
        vl1.getNodeBreakerView()
                .newBusbarSection()
                .setId("B1")
                .setNode(1)
                .add();
        vl1.getNodeBreakerView()
                .newBusbarSection()
                .setId("B2")
                .setNode(2)
                .add();

        vl1.newLoad()
                .setId("L0")
                .setNode(6)
                .setP0(0)
                .setQ0(0)
                .add();
        vl1.newLoad()
                .setId("L1")
                .setNode(3)
                .setP0(0)
                .setQ0(0)
                .add();
        vl1.newLoad()
                .setId("L2")
                .setNode(4)
                .setP0(0)
                .setQ0(0)
                .add();
        vl1.newLoad()
                .setId("L3")
                .setNode(5)
                .setP0(0)
                .setQ0(0)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("L0-node")
                .setOpen(false)
                .setNode1(0)
                .setNode2(6)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("L1-node")
                .setOpen(true)
                .setNode1(4)
                .setNode2(10)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("L0-B0")
                .setOpen(false)
                .setNode1(3)
                .setNode2(1)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("B0-node")
                .setOpen(true)
                .setNode1(1)
                .setNode2(10)
                .setRetained(true)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("node-B1")
                .setOpen(false)
                .setNode1(10)
                .setNode2(2)
                .setRetained(true)
                .add();

        vl2.newLoad()
                .setId("L4")
                .setNode(0)
                .setP0(0)
                .setQ0(0)
                .add();
        return network;
    }
}
