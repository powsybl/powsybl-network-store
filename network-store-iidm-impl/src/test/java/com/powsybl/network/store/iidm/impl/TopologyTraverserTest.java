/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractTopologyTraverserTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TopologyTraverserTest extends AbstractTopologyTraverserTest {

    private Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl1.newLoad()
                .setId("LD1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setP0(1.0)
                .setQ0(1.0)
                .add();
        vl1.newGenerator()
                .setId("G")
                .setBus("B1")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        return network;
    }

    private Network createNetworkWithTwoBuses() {
        Network network = createNetwork();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        network.getVoltageLevel("VL1").getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR1")
                .setBus1("B2")
                .setBus2("B1")
                .setOpen(true)
                .add();
        vl1.newLoad()
                .setId("LD2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        return network;
    }

    private Network createNetworkWithMultiBuses() {
        Network network = createNetworkWithTwoBuses();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        network.getVoltageLevel("VL1").getBusBreakerView().newBus()
                .setId("B3")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR2")
                .setBus1("B3")
                .setBus2("B2")
                .setOpen(true)
                .add();
        vl1.newLoad()
                .setId("LD3")
                .setConnectableBus("B3")
                .setBus("B3")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        return network;
    }

    private Network createNetworkWithLine() {
        Network network = createNetworkWithMultiBuses();
        Substation s = network.getSubstation("S");
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B21")
                .add();
        vl2.newGenerator()
                .setId("G2")
                .setBus("B21")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        network.newLine()
                .setId("L1")
                .setVoltageLevel1("VL1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setBus2("B21")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        return network;
    }

    @Test
    public void testWithOneBus() {
        Network network = createNetwork();
        Terminal start = network.getLoad("LD1").getTerminal();
        List<String> traversed = recordTraversed(start, aSwitch -> aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER);
        assertEquals(Arrays.asList("LD1", "G"), traversed);
    }

    @Test
    public void testTwoBuses() {
        Network network = createNetworkWithTwoBuses();
        Predicate<Switch> switchPredicate = aSwitch -> aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        List<String> traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G", "LD2"), traversed);

        traversed = recordTraversed(network.getGenerator("G").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("G", "LD1", "LD2"), traversed);

        traversed = recordTraversed(network.getLoad("LD2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD2", "G", "LD1"), traversed);

        Switch s = network.getSwitch("BR1");
        s.setOpen(false);

        traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G"), traversed);

        traversed = recordTraversed(network.getGenerator("G").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("G", "LD1"), traversed);

        traversed = recordTraversed(network.getLoad("LD2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD2"), traversed);
    }

    @Test
    public void testMultiBuses() {
        Network network = createNetworkWithMultiBuses();
        Predicate<Switch> switchPredicate = aSwitch -> aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        List<String> traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G", "LD2", "LD3"), traversed);

        traversed = recordTraversed(network.getLoad("LD2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD2", "G", "LD1", "LD3"), traversed);

        traversed = recordTraversed(network.getLoad("LD3").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD3", "LD2", "G", "LD1"), traversed);

        Switch s = network.getSwitch("BR1");
        s.setOpen(false);

        traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G"), traversed);

        traversed = recordTraversed(network.getLoad("LD2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD2", "LD3"), traversed);

        traversed = recordTraversed(network.getLoad("LD3").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD3", "LD2"), traversed);

        s = network.getSwitch("BR2");
        s.setOpen(false);

        traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G"), traversed);

        traversed = recordTraversed(network.getLoad("LD2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD2"), traversed);

        traversed = recordTraversed(network.getLoad("LD3").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD3"), traversed);
    }

    @Test
    public void testWithLine() {
        Network network = createNetworkWithLine();
        Predicate<Switch> switchPredicate = aSwitch -> aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        List<String> traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G", "L1", "L1", "G2", "LD2", "LD3"), traversed);

        traversed = recordTraversed(network.getGenerator("G2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("G2", "L1", "L1", "G", "LD1", "LD2", "LD3"), traversed);
    }

    private List<String> recordTraversed(Terminal start, Predicate<Switch> switchPredicate) {
        List<String> traversed = new ArrayList<>();
        start.traverse(new VoltageLevel.TopologyTraverser() {
            @Override
            public boolean traverse(Terminal terminal, boolean connected) {
                traversed.add(terminal.getConnectable().getId());
                return true;
            }

            @Override
            public boolean traverse(Switch aSwitch) {
                return switchPredicate.test(aSwitch);
            }
        });
        return traversed;
    }

}
