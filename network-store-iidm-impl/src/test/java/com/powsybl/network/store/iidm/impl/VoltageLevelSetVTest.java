/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class VoltageLevelSetVTest {
    @Test
    public void testWithMultipleBusInBusBreakerAndBusView() {
        Network network = Network.create("test_mcc", "test");

        VoltageLevelImpl vl1 = createNodeBreaker(network);
        VoltageLevelImpl vl2 = createBusBreaker(network);

        testSetVMultipleBusAcrossViews(vl1, NBVL_BUSBREAKERVIEWBUS_TO_BUSVIEWBUS, NBVL_BUSVIEWBUS_TO_BUSBREAKERVIEWBUS);
        testSetVMultipleBusAcrossViews(vl2, BBVL_CONFIGUREDBUS_TO_BUSVIEWBUS, BBVL_BUSVIEWBUS_TO_CONFIGUREDBUS);
    }

    private static final Map<String, String> NBVL_BUSBREAKERVIEWBUS_TO_BUSVIEWBUS = new TreeMap<>(Map.of(
            "VL1_0", "VL1_0",
            "VL1_2", "VL1_0",
            "VL1_3", "VL1_3",
            "VL1_4", "VL1_4",
            "VL1_10", "",
            "VL1_11", "VL1_0",
            "VL1_5", ""
    ));
    private static final Map<String, List<String>> NBVL_BUSVIEWBUS_TO_BUSBREAKERVIEWBUS = invertMap(NBVL_BUSBREAKERVIEWBUS_TO_BUSVIEWBUS);

    private static VoltageLevelImpl createNodeBreaker(Network network) {
        // nodebreaker topology voltage level
        VoltageLevel vl1 = network.newVoltageLevel()
            .setId("VL1")
            .setNominalV(225.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW1")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(false)
            .setNode1(0)
            .setNode2(1)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS2")
            .setNode(1)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW2")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(false)
            .setNode1(1)
            .setNode2(2)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS3")
            .setNode(2)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW3")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(true)
            .setNode1(2)
            .setNode2(3)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS4")
            .setNode(3)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW4")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(true)
            .setNode1(3)
            .setNode2(4)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS5")
            .setNode(4)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW5")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(true)
            .setNode1(4)
            .setNode2(5)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS6")
            .setNode(5)
            .add();

        // add loads so that buses are not pruned
        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(0)
            .setNode2(6)
            .add();
        vl1.newLoad().setId("VL1L1").setNode(6).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(2)
            .setNode2(7)
            .add();
        vl1.newLoad().setId("VL1L2").setNode(7).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(3)
            .setNode2(8)
            .add();
        vl1.newLoad().setId("VL1L3").setNode(8).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(4)
            .setNode2(9)
            .add();
        vl1.newLoad().setId("VL1L4").setNode(9).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        //no load on BBS6, it will be pruned

        //add a load on bbs1 but not connected, it will be in a separate bus in the busbreakerview and pruned in the busview
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW6")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(true)
            .setNode1(0)
            .setNode2(10)
            .add();
        vl1.newLoad().setId("VL1L5").setNode(10).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        //add a load on bbs1 but with retained switch, it will be in a separate bus in the busbreakerview but connected in the busview
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW7")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(false)
            .setNode1(0)
            .setNode2(11)
            .add();
        vl1.newLoad().setId("VL1L6").setNode(11).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        return (VoltageLevelImpl) vl1;
    }

    private static final Map<String, String> BBVL_CONFIGUREDBUS_TO_BUSVIEWBUS = new TreeMap<>(Map.of(
            "BUS1", "VL2_0",
            "BUS2", "VL2_0",
            "BUS3", "VL2_1",
            "BUS4", "VL2_2",
            "BUS5", ""
    ));

    private static final Map<String, List<String>> BBVL_BUSVIEWBUS_TO_CONFIGUREDBUS = invertMap(BBVL_CONFIGUREDBUS_TO_BUSVIEWBUS);

    private static VoltageLevelImpl createBusBreaker(Network network) {
        // busbreaker topology voltage level
        VoltageLevel vl2 = network.newVoltageLevel()
            .setId("VL2")
            .setNominalV(225.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("BUS1")
            .add();
        vl2.getBusBreakerView().newSwitch()
            .setId("VL2SW1")
            .setOpen(false)
            .setBus1("BUS1")
            .setBus2("BUS2")
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("BUS2")
            .add();
        vl2.getBusBreakerView().newSwitch()
            .setId("VL2SW2")
            .setOpen(true)
            .setBus1("BUS2")
            .setBus2("BUS3")
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("BUS3")
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("BUS4")
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("BUS5")
            .add();

        // loads to avoid pruning
        vl2.newLoad().setId("VL2L1").setBus("BUS1").setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);
        vl2.newLoad().setId("VL2L2").setBus("BUS3").setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);
        vl2.newLoad().setId("VL2L3").setBus("BUS4").setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);
        vl2.newLoad().setId("VL2L4").setConnectableBus("BUS5").setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);
        // BUS2 has no load it will be pruned

        return (VoltageLevelImpl) vl2;
    }

    private void testSetVMultipleBusAcrossViews(VoltageLevelImpl vl, Map<String, String> busBreakerViewBusToBusViewBus, Map<String, List<String>> busViewBusToBusBreakerViewBus) {
        for (Map.Entry<String, String > entry : busBreakerViewBusToBusViewBus.entrySet()) {
            String busbreakerviewbusid = entry.getKey();
            String busviewbusid = entry.getValue();

            // should we replace with new network for each test ??
            vl.invalidateCalculatedBuses(); // but this keeps previous v values
            // TODO this forces a computation of all busviews
            // need to test when views are not initialized
            for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
                bbvb.setV(Double.NaN);
            }
            for (Bus bvb : vl.getBusView().getBuses()) {
                bvb.setV(Double.NaN);
            }

            vl.getBusBreakerView().getBus(busbreakerviewbusid).setV(1.);
            if (!busviewbusid.isEmpty()) {
                assertEquals("case " + busbreakerviewbusid + " (busbreakerviewbus) is set, " + busviewbusid + " (busviewbus) should have been set",
                        1, vl.getBusView().getBus(busviewbusid).getV(), 0);
            }
            for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
                if (!busbreakerviewbusid.equals(bbvb.getId())) {
                    assertTrue("case " + busbreakerviewbusid + " (busbreakerviewbus) is set, " + bbvb.getId() + " (busbreakerviewbus) should not have been set",
                            Double.isNaN(bbvb.getV()));
                }
            }
            for (Bus bvb : vl.getBusView().getBuses()) {
                if (!busviewbusid.equals(bvb.getId())) {
                    assertTrue("case " + busbreakerviewbusid + "(busbreakerviewbus) is set, " + bvb.getId() + " (busviewbus) should not have been set",
                            Double.isNaN(bvb.getV()));
                }
            }
        }

        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();

            // should we replace with new network for each test ??
            vl.invalidateCalculatedBuses(); // but this keeps previous v values
            // TODO this forces a computation of all busviews
            // need to test when views are not initialized
            for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
                bbvb.setV(Double.NaN);
            }
            for (Bus bvb : vl.getBusView().getBuses()) {
                bvb.setV(Double.NaN);
            }

            vl.getBusView().getBus(busviewbusid).setV(1.);
            for (String busbreakerviewbusid : busbreakerviewbusids) {
                assertEquals("case " + busviewbusid + " (busviewbus) is set, " + busbreakerviewbusid + " (busbreakerviewbus) should have been set",
                        1, vl.getBusBreakerView().getBus(busbreakerviewbusid).getV(), 0);
            }
            for (Bus bvb : vl.getBusView().getBuses()) {
                if (!busviewbusid.equals(bvb.getId())) {
                    assertTrue("case " + busviewbusid + " (busviewbus) is set, " + bvb.getId() + " (busviewbus) should not have been set",
                            Double.isNaN(bvb.getV()));
                }
            }
            for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
                if (!busbreakerviewbusids.contains(bbvb.getId())) {
                    assertTrue("case " + busviewbusid + " (busviewbus) is set, " + bbvb.getId() + " (busbreakerviewbus) should not have been set",
                            Double.isNaN(bbvb.getV()));
                }
            }
        }
    }

    private static Map<String, List<String>> invertMap(Map<String, String> map) {
        return map
                 .entrySet().stream()
                 .filter(x -> !x.getValue().isEmpty())
                 .collect(Collectors.groupingBy(Map.Entry::getValue, TreeMap::new, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }
}
