/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.tck.AbstractTopologyTraverserTest;
import com.powsybl.math.graph.TraverseResult;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TerminalTopologyTraverserTest extends AbstractTopologyTraverserTest {

    @Test
    public void testWithOneBus() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithOneBus();
        Terminal start = network.getLoad("LD1").getTerminal();
        List<String> traversed = recordTraversed(start, aSwitch -> aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER);
        assertEquals(Arrays.asList("LD1", "G"), traversed);
    }

    @Test
    public void testTwoBuses() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithTwoBuses();
        Predicate<Switch> switchPredicate = aSwitch -> aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        List<String> traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G"), traversed);

        traversed = recordTraversed(network.getGenerator("G").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("G", "LD1"), traversed);

        traversed = recordTraversed(network.getLoad("LD2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD2"), traversed);

        Switch s = network.getSwitch("BR1");
        s.setOpen(true);

        traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G", "LD2"), traversed);

        traversed = recordTraversed(network.getGenerator("G").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("G", "LD1", "LD2"), traversed);

        traversed = recordTraversed(network.getLoad("LD2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD2", "G", "LD1"), traversed);
    }

    @Test
    public void testMultiBuses() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithMultiBuses();
        Predicate<Switch> switchPredicate = aSwitch -> aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        Switch s = network.getSwitch("BR1");
        s.setOpen(true);

        s = network.getSwitch("BR2");
        s.setOpen(true);

        List<String> traversed = recordTraversed(network.getLoad("LD1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD1", "G", "LD2", "LD3"), traversed);

        traversed = recordTraversed(network.getLoad("LD2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD2", "G", "LD1", "LD3"), traversed);

        traversed = recordTraversed(network.getLoad("LD3").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD3", "LD2", "G", "LD1"), traversed);

        s = network.getSwitch("BR1");
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
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();

        List<String> traversed = recordTraversed(network.getLoad("LD1").getTerminal(), s -> true);
        assertEquals(Arrays.asList("LD1", "G", "L1", "L1", "G2", "LD2", "LD3"), traversed);

        traversed = recordTraversed(network.getGenerator("G").getTerminal(), s -> true);
        assertEquals(Arrays.asList("G", "LD1", "L1", "L1", "G2", "LD2", "LD3"), traversed);

        traversed = recordTraversed(network.getGenerator("G2").getTerminal(), s -> true);
        assertEquals(Arrays.asList("G2", "L1", "L1", "G", "LD1", "LD2", "LD3"), traversed);

        traversed = recordTraversed(network.getLine("L1").getTerminal1(), s -> true);
        assertEquals(Arrays.asList("L1", "G", "LD1", "LD2", "LD3", "L1", "G2"), traversed);

        traversed = recordTraversed(network.getLine("L1").getTerminal2(), s -> true);
        assertEquals(Arrays.asList("L1", "G2", "L1", "G", "LD1", "LD2", "LD3"), traversed);

        Predicate<Switch> switchPredicate = aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        network.getSwitch("BR2").setOpen(true);
        traversed = recordTraversed(network.getLine("L1").getTerminal1(), switchPredicate);
        assertEquals(Arrays.asList("L1", "G", "LD1", "LD2", "L1", "G2"), traversed);

        network.getSwitch("BR1").setOpen(true);
        traversed = recordTraversed(network.getLine("L1").getTerminal1(), switchPredicate);
        assertEquals(Arrays.asList("L1", "G", "LD1", "L1", "G2"), traversed);
    }

    @Test
    public void testWithNodeBreaker() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Terminal start = network.getVoltageLevel("VL1").getNodeBreakerView().getBusbarSection("BBS1").getTerminal();
        List<String> traversed = recordTraversed(start, s -> true);
        assertEquals(Arrays.asList("BBS1", "L", "G", "L1", "L1", "BBS2", "LD"), traversed);

        traversed = recordTraversed(network.getGenerator("G").getTerminal(), s -> true);
        assertEquals(Arrays.asList("G", "BBS1", "L", "L1", "L1", "BBS2", "LD"), traversed);

        traversed = recordTraversed(network.getLine("L1").getTerminal1(), s -> true);
        assertEquals(Arrays.asList("L1", "BBS1", "L", "G", "L1", "BBS2", "LD"), traversed);

        traversed = recordTraversed(network.getLine("L1").getTerminal2(), s -> true);
        assertEquals(Arrays.asList("L1", "BBS2", "LD", "L1", "BBS1", "L", "G"), traversed);

        traversed = recordTraversed(network.getLoad("LD").getTerminal(), s -> true);
        assertEquals(Arrays.asList("LD", "BBS2", "L1", "L1", "BBS1", "L", "G"), traversed);

        network.getSwitch("D1").setOpen(true);
        traversed = recordTraversed(network.getLoad("L").getTerminal(), s -> true);
        assertEquals(Arrays.asList("L", "BBS1", "G", "L1", "L1", "BBS2", "LD"), traversed);

        network.getSwitch("D2").setOpen(true);
        traversed = recordTraversed(network.getLoad("L").getTerminal(), s -> true);
        assertEquals(Arrays.asList("L", "BBS1", "G", "L1", "L1", "BBS2", "LD"), traversed);

        network.getSwitch("D21").setOpen(true);
        traversed = recordTraversed(network.getLoad("LD").getTerminal(), s -> true);
        assertEquals(Arrays.asList("LD", "BBS2", "L1", "L1", "BBS1", "L", "G"), traversed);

        network.getSwitch("BR1").setOpen(true);
        traversed = recordTraversed(network.getLoad("L").getTerminal(), s -> true);
        assertEquals(Arrays.asList("L", "BBS1", "G", "L1", "L1", "BBS2", "LD"), traversed);

        network.getSwitch("BR2").setOpen(true);
        traversed = recordTraversed(network.getLoad("L").getTerminal(), s -> true);
        assertEquals(Arrays.asList("L", "BBS1", "G", "L1", "L1", "BBS2", "LD"), traversed);

        network.getSwitch("BR3").setOpen(true);
        traversed = recordTraversed(network.getLoad("LD").getTerminal(), s -> true);
        assertEquals(Arrays.asList("LD", "BBS2", "L1", "L1", "BBS1", "L", "G"), traversed);

        network.getSwitch("BR4").setOpen(true);
        traversed = recordTraversed(network.getLoad("LD").getTerminal(), s -> true);
        assertEquals(Arrays.asList("LD", "BBS2", "L1", "L1", "BBS1", "L", "G"), traversed);
    }

    @Test
    public void testWithNodeBreakerWithSwitch() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Predicate<Switch> switchPredicate = aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        List<String> traversed = recordTraversed(network.getLoad("L").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("L"), traversed);

        traversed = recordTraversed(network.getGenerator("G").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("G", "BBS1"), traversed);

        traversed = recordTraversed(network.getBusbarSection("BBS1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("BBS1", "G"), traversed);

        traversed = recordTraversed(network.getLine("L1").getTerminal1(), switchPredicate);
        assertEquals(Arrays.asList("L1", "L1"), traversed);

        traversed = recordTraversed(network.getLine("L1").getTerminal2(), switchPredicate);
        assertEquals(Arrays.asList("L1", "L1"), traversed);

        traversed = recordTraversed(network.getBusbarSection("BBS2").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("BBS2", "LD"), traversed);

        traversed = recordTraversed(network.getLoad("LD").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD", "BBS2"), traversed);
    }

    @Test
    public void testWithNodeBreakerWithSwitch1() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Predicate<Switch> switchPredicate = aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        network.getSwitch("BR1").setOpen(true);

        List<String> traversed = traversed = recordTraversed(network.getLoad("L").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("L"), traversed);

        traversed = recordTraversed(network.getGenerator("G").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("G"), traversed);

        traversed = recordTraversed(network.getBusbarSection("BBS1").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("BBS1"), traversed);
    }

    @Test
    public void testWithNodeBreakerWithSwitch2() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Predicate<Switch> switchPredicate = aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        network.getSwitch("BR1").setOpen(true);
        network.getSwitch("BR2").setOpen(true);

        List<String> traversed = traversed = recordTraversed(network.getLine("L1").getTerminal1(), switchPredicate);
        assertEquals(Arrays.asList("L1", "L1"), traversed);

        traversed = recordTraversed(network.getLine("L1").getTerminal2(), switchPredicate);
        assertEquals(Arrays.asList("L1", "L1"), traversed);
    }

    @Test
    public void testWithNodeBreakerWithSwitch3() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Predicate<Switch> switchPredicate = aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        network.getSwitch("BR1").setOpen(true);
        network.getSwitch("BR2").setOpen(true);
        network.getSwitch("BR4").setOpen(true);

        List<String> traversed = traversed = recordTraversed(network.getLine("L1").getTerminal1(), switchPredicate);
        assertEquals(Arrays.asList("L1", "L1"), traversed);

        traversed = recordTraversed(network.getLine("L1").getTerminal2(), switchPredicate);
        assertEquals(Arrays.asList("L1", "L1"), traversed);
    }

    @Test
    public void testWithNodeBreakerWithSwitch4() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Predicate<Switch> switchPredicate = aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() == SwitchKind.BREAKER;

        network.getSwitch("BR1").setOpen(true);
        network.getSwitch("BR2").setOpen(true);
        network.getSwitch("BR4").setOpen(true);
        network.getSwitch("BR3").setOpen(true);

        List<String> traversed = traversed = recordTraversed(network.getLoad("LD").getTerminal(), switchPredicate);
        assertEquals(Arrays.asList("LD"), traversed);
    }

    private List<String> recordTraversed(Terminal start, Predicate<Switch> switchPredicate) {
        List<String> traversed = new ArrayList<>();
        start.traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                traversed.add(terminal.getConnectable().getId());
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                return switchPredicate.test(aSwitch) ? TraverseResult.CONTINUE : TraverseResult.TERMINATE_PATH;
            }
        });
        return traversed;
    }

}
