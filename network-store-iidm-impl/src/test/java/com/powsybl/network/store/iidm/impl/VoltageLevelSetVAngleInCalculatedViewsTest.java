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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 *
 * complex specific exhaustive tests for setV and setAngle interactions with calculated views.
 */
public class VoltageLevelSetVAngleInCalculatedViewsTest {
    @Test
    public void testWithMultipleBusInBusBreakerAndBusView() {
        testSetMultipleBusAcrossViews(() -> {
            Network network = Network.create("test_mcc", "test");
            return createNodeBreaker(network);
        }, NBVL_BUSBREAKERVIEWBUS_TO_BUSVIEWBUS, NBVL_BUSVIEWBUS_TO_BUSBREAKERVIEWBUS, Bus::getV, Bus::setV);
        testSetMultipleBusAcrossViews(() -> {
            Network network = Network.create("test_mcc", "test");
            return createNodeBreaker(network);
        }, NBVL_BUSBREAKERVIEWBUS_TO_BUSVIEWBUS, NBVL_BUSVIEWBUS_TO_BUSBREAKERVIEWBUS, Bus::getAngle, Bus::setAngle);
        testSetMultipleBusAcrossViews(() -> {
            Network network = Network.create("test_mcc", "test");
            return createBusBreaker(network);
        }, BBVL_CONFIGUREDBUS_TO_BUSVIEWBUS, BBVL_BUSVIEWBUS_TO_CONFIGUREDBUS, Bus::getV, Bus::setV);
        testSetMultipleBusAcrossViews(() -> {
            Network network = Network.create("test_mcc", "test");
            return createBusBreaker(network);
        }, BBVL_CONFIGUREDBUS_TO_BUSVIEWBUS, BBVL_BUSVIEWBUS_TO_CONFIGUREDBUS, Bus::getAngle, Bus::setAngle);
    }

    //NOTE: id in both cases is the voltage level id suffixed by the smallest
    //node number of the nodebreaker definition connected to the bus.
    private static final Map<String, String> NBVL_BUSBREAKERVIEWBUS_TO_BUSVIEWBUS = new TreeMap<>(Map.ofEntries(
            Map.entry("VL1_0", ""),
            Map.entry("VL1_1", "VL1_1"),
            Map.entry("VL1_3", "VL1_1"),
            Map.entry("VL1_4", "VL1_4"),
            Map.entry("VL1_5", "VL1_5"),
            Map.entry("VL1_6", "VL1_5"),
            Map.entry("VL1_7", "VL1_5"),
            Map.entry("VL1_8", "VL1_5"),
            Map.entry("VL1_9", ""),
            Map.entry("VL1_17", ""),
            Map.entry("VL1_18", ""),
            Map.entry("VL1_19", "VL1_1")
    ));
    private static final Map<String, List<String>> NBVL_BUSVIEWBUS_TO_BUSBREAKERVIEWBUS = invertMap(NBVL_BUSBREAKERVIEWBUS_TO_BUSVIEWBUS);

    private static VoltageLevelImpl createNodeBreaker(Network network) {
        // nodebreaker topology voltage level
        VoltageLevel vl1 = network.newVoltageLevel()
            .setId("VL1")
            .setNominalV(225.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        // This bbs is not even in the same graph as the other stuff and has no equipment connected
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS_ALONE")
            .setNode(0)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS1")
            .setNode(1)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW1")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(false)
            .setNode1(1)
            .setNode2(2)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS2")
            .setNode(2)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW2")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(false)
            .setNode1(2)
            .setNode2(3)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS3")
            .setNode(3)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW3")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(true)
            .setNode1(3)
            .setNode2(4)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS4")
            .setNode(4)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW4")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(true)
            .setNode1(4)
            .setNode2(5)
            .add();

        // add busbar sections in star coupling to have a bus in the middle without any equipment. It will
        // exist in the busbreaker view but not in the busview. For this one use closed switches so that it
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS5")
            .setNode(5)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS6")
            .setNode(6)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS7")
            .setNode(7)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW_STAR1")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(false)
            .setNode1(5)
            .setNode2(8)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW_STAR2")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(false)
            .setNode1(6)
            .setNode2(8)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW_STAR3")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(false)
            .setNode1(7)
            .setNode2(8)
            .add();
        // same star coupling as before but with open switches so that
        // the central bus is not mapped to anything in the bus view
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW_OPENSTAR1")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(true)
            .setNode1(5)
            .setNode2(9)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW_OPENSTAR2")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(true)
            .setNode1(6)
            .setNode2(9)
            .add();
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1SW_OPENSTAR3")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(true)
            .setNode1(7)
            .setNode2(9)
            .add();

        // add loads so that buses are not pruned
        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(1)
            .setNode2(10)
            .add();
        vl1.newLoad().setId("VL1L1").setNode(10).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(2)
            .setNode2(11)
            .add();
        vl1.newLoad().setId("VL1L2").setNode(11).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(3)
            .setNode2(12)
            .add();
        vl1.newLoad().setId("VL1L3").setNode(12).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(4)
            .setNode2(13)
            .add();
        vl1.newLoad().setId("VL1L4").setNode(13).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(5)
            .setNode2(14)
            .add();
        vl1.newLoad().setId("VL1L5").setNode(14).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(6)
            .setNode2(15)
            .add();
        vl1.newLoad().setId("VL1L6").setNode(15).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        vl1.getNodeBreakerView().newInternalConnection()
            .setNode1(7)
            .setNode2(16)
            .add();
        vl1.newLoad().setId("VL1L7").setNode(16).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        //add a load on bbs1 but not connected, it will be in a separate floating bus in the busbreakerview and pruned in the busview
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1_SW_L8")
            .setKind(SwitchKind.BREAKER)
            .setRetained(false)
            .setOpen(true)
            .setNode1(1)
            .setNode2(17)
            .add();
        vl1.newLoad().setId("VL1L8").setNode(17).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        //add a load on bbs1 but not connected but with a retained switch, it will be in a separate bus in the busbreakerview and pruned in the busview
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1_SW_L9")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(true)
            .setNode1(1)
            .setNode2(18)
            .add();
        vl1.newLoad().setId("VL1L9").setNode(18).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        //add a load on bbs1 but with retained switch, it will be in a separate bus in the busbreakerview but connected in the busview
        vl1.getNodeBreakerView().newSwitch()
            .setId("VL1_SW_L10")
            .setKind(SwitchKind.BREAKER)
            .setRetained(true)
            .setOpen(false)
            .setNode1(1)
            .setNode2(19)
            .add();
        vl1.newLoad().setId("VL1L10").setNode(19).setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        return (VoltageLevelImpl) vl1;
    }

    //NOTE: id for the busbreakerview is the configured bus;
    // id for the busview is order of traversal during busview computation
    private static final Map<String, String> BBVL_CONFIGUREDBUS_TO_BUSVIEWBUS = new TreeMap<>(Map.of(
            "BUS_ALONE", "",
            "BUS1", "VL2_0",
            "BUS2", "VL2_0",
            "BUS3", "",
            "BUS4", ""
    ));

    private static final Map<String, List<String>> BBVL_BUSVIEWBUS_TO_CONFIGUREDBUS = invertMap(BBVL_CONFIGUREDBUS_TO_BUSVIEWBUS);

    private static VoltageLevelImpl createBusBreaker(Network network) {
        // busbreaker topology voltage level
        VoltageLevel vl2 = network.newVoltageLevel()
            .setId("VL2")
            .setNominalV(225.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        // This bus is not even in the same graph as the other stuff and has no equipment connected
        vl2.getBusBreakerView().newBus()
            .setId("BUS_ALONE")
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
        vl2.getBusBreakerView().newSwitch()
            .setId("VL2SW3")
            .setOpen(true)
            .setBus1("BUS3")
            .setBus2("BUS4")
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("BUS4")
            .add();

        // loads to avoid pruning
        vl2.newLoad().setId("VL2L1").setBus("BUS1").setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);
        //no load on BUS2 but it is connected to bus1
        //no load on BUS3 so that it is prunned, disconnected from everything
        vl2.newLoad().setId("VL2L4").setConnectableBus("BUS4").setP0(1).setQ0(1).add().getTerminal().setP(0.001).setQ(1);

        return (VoltageLevelImpl) vl2;
    }

    private void testSetMultipleBusAcrossViews(Supplier<VoltageLevel> networkVoltageLevelSupplier, Map<String, String> busBreakerViewBusToBusViewBus, Map<String, List<String>> busViewBusToBusBreakerViewBus,
            Function<Bus, Double> getter, BiConsumer<Bus, Double> setter) {

        VoltageLevel vl;
        // tests for busbreakerview set

        // test with views that are initialized but unset, new network each time
        // currently no need to be deterministic for this to work, the last set
        // in the busbreakerview is applied to the busview
        for (Map.Entry<String, String > entry : busBreakerViewBusToBusViewBus.entrySet()) {
            String busbreakerviewbusid = entry.getKey();
            String busviewbusid = entry.getValue();
            vl = networkVoltageLevelSupplier.get();
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
                // this triggers view calculation only for nodebreakertopology
                vl.getBusBreakerView().getBuses();
            }
            vl.getBusView().getBuses();
            setAndCheckBusBreakerBus(vl, busbreakerviewbusid, busviewbusid, getter, setter);
        }

        // test with views that are initialized and everything set to nan, reuse network
        // currently no need to be deterministic for this to work, the last set
        // in the busbreakerview is applied to the busview
        vl = networkVoltageLevelSupplier.get();
        for (Map.Entry<String, String > entry : busBreakerViewBusToBusViewBus.entrySet()) {
            String busbreakerviewbusid = entry.getKey();
            String busviewbusid = entry.getValue();
            setAllBusBreakerViewBusAndBusViewBus(vl, Double.NaN, setter);
            setAndCheckBusBreakerBus(vl, busbreakerviewbusid, busviewbusid, getter, setter);
        }

        // test with both views invalid, new network each time,
        // in this case we need to set all bbvbs to get a deterministic behavior
        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();
            vl = networkVoltageLevelSupplier.get();
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
                // this triggers view calculation only for nodebreakertopology
                vl.getBusBreakerView().getBuses();
            }
            vl.getBusView().getBuses();
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            setAndCheckBusBreakerBusDeterministic(vl, busbreakerviewbusids, busviewbusid, getter, setter);
        }

        // test with busview invalid, busbreakerview unset, new network each time
        // in this case we need to set all bbvbs to get a deterministic behavior
        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();
            vl = networkVoltageLevelSupplier.get();
            vl.getBusView().getBuses();
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            setAndCheckBusBreakerBusDeterministic(vl, busbreakerviewbusids, busviewbusid, getter, setter);
        }

        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            // this test of initiliazed busbreakeriewbus only makes sense for nodebreaker topology,
            // for busbreakertopology it's the same as all views uninitialized

            // test with busbreakerview invalid, busview unset
            // in this case we need to set all bbvbs to get a deterministic behavior
            for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
                String busviewbusid = entry.getKey();
                List<String> busbreakerviewbusids = entry.getValue();
                vl = networkVoltageLevelSupplier.get();
                vl.getBusBreakerView().getBuses();
                ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
                setAndCheckBusBreakerBusDeterministic(vl, busbreakerviewbusids, busviewbusid, getter, setter);
            }
        }

        // test with views that are not initialized at all, new network each time
        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();
            vl = networkVoltageLevelSupplier.get();
            setAndCheckBusBreakerBusDeterministic(vl, busbreakerviewbusids, busviewbusid, getter, setter);
        }

        // test with views that are initialized and everything set to a real value, reuse network.
        // With invalid views it should not propagate to the other view
        // TODO currently we have this behavior between the 2 views to
        // mimic the behavior when only the same view is set (values are not carried over
        // when the new view is computed after invalidating it). But if we change this behavior,
        // we may change the behavior when there are two views to mimic it, and so we should invert this test.
        // for the busbreakertopology, it's as if the busbreakerview is always valid as it's directly
        // the configured buses, so it keeps the values. It doesn't make much sense but that's how it is.
        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            // test one same view
            vl = networkVoltageLevelSupplier.get();
            setAllBusBreakerViewBus(vl, 1.0, setter);
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
                assertTrue("case nodebreakertopology " + bbvb.getId() + " (busbreakerviewbus) is set, should not have been carried over from invalid previous view",
                        Double.isNaN(getter.apply(bbvb)));
            }

            // test one other view
            vl = networkVoltageLevelSupplier.get();
            setAllBusViewBus(vl, 1.0, setter);
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
                assertTrue("case nodebreakertopology " + bbvb.getId() + " (busbreakerviewbus) is set, should not have been carried over from invalid previous view",
                        Double.isNaN(getter.apply(bbvb)));
            }
        } else { // BUS_BREAKER
            // No need to test the busbreakerview in busbreakertopology,
            // it's not a view  it's directly the configured buses.
            // so test only with busview view
            // NOTE: for busbreakertopology we keep the previous values.
            vl = networkVoltageLevelSupplier.get();
            setAllBusViewBus(vl, 1.0, setter);
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
                if (!busBreakerViewBusToBusViewBus.get(bbvb.getId()).isEmpty()) {
                    //only test non pruned bbvb because we set in the busview, so pruned bbvb are unset
                    assertEquals("case busbreakertopology not pruned " + bbvb.getId() + " (busbreakerviewbus) is unset, but should have been set when setting on the busview",
                            1, getter.apply(bbvb), 0.0);
                } else {
                    // the pruned ones should be unset
                    assertTrue("case busbreakertopology pruned " + bbvb.getId() + " (busbreakerviewbus) is set, should not have been set because it is pruned from the busview",
                            Double.isNaN(getter.apply(bbvb)));
                }
            }
        }

        // tests for busbreakerview set

        // test with views that are initialized but unset, new network each time
        // currently no need to be deterministic for this to work, the last set
        // in the busbreakerview is applied to the busview
        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();
            vl = networkVoltageLevelSupplier.get();
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
                // this triggers view calculation only for nodebreakertopology
                vl.getBusBreakerView().getBuses();
            }
            setAndCheckBusViewBus(vl, busviewbusid, busbreakerviewbusids, getter, setter);
        }

        // test with views that is initialized and everything set to nan, reuse network
        vl = networkVoltageLevelSupplier.get();
        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();
            setAllBusBreakerViewBusAndBusViewBus(vl, Double.NaN, setter);
            setAndCheckBusViewBus(vl, busviewbusid, busbreakerviewbusids, getter, setter);
        }

        // test with both views invalid, new network each time
        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();
            vl = networkVoltageLevelSupplier.get();
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
                // this triggers view calculation only for nodebreakertopology
                vl.getBusBreakerView().getBuses();
            }
            vl.getBusView().getBuses();
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            setAndCheckBusViewBus(vl, busviewbusid, busbreakerviewbusids, getter, setter);
        }

        // test with busview invalid, busbreakerview unset, new network each time
        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();
            vl = networkVoltageLevelSupplier.get();
            vl.getBusView().getBuses();
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            setAndCheckBusViewBus(vl, busviewbusid, busbreakerviewbusids, getter, setter);
        }

        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            // this test of initiliazed busbreakeriewbus only makes sense for nodebreaker topology,
            // for busbreakertopology it's the same as all views uninitialized

            // test with busbreakerview invalid, busview unset
            for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
                String busviewbusid = entry.getKey();
                List<String> busbreakerviewbusids = entry.getValue();
                vl = networkVoltageLevelSupplier.get();
                vl.getBusBreakerView().getBuses();
                ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
                setAndCheckBusViewBus(vl, busviewbusid, busbreakerviewbusids, getter, setter);
            }
        }

        // test with views that are not initialized at all, new network each time
        for (Map.Entry<String, List<String> > entry : busViewBusToBusBreakerViewBus.entrySet()) {
            String busviewbusid = entry.getKey();
            List<String> busbreakerviewbusids = entry.getValue();
            vl = networkVoltageLevelSupplier.get();
            setAndCheckBusViewBus(vl, busviewbusid, busbreakerviewbusids, getter, setter);
        }

        // test with views that are initialized and everything set to a real value, reuse network.
        // With invalid views it should not propagate to the other view
        // TODO currently we have this behavior between the 2 views to
        // mimic the behavior when only the same view is set (values are not carried over
        // (when the new view is computed after invalidating it). But if we change this behavior,
        // we may change the behavior when there are two views to mimic it, and so we should invert this test.
        // test one same view, nodebreakertopology and busbreakertopology behave the same here
        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            vl = networkVoltageLevelSupplier.get();
            setAllBusViewBus(vl, 1.0, setter);
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            for (Bus bvb : vl.getBusView().getBuses()) {
                assertTrue("case " + bvb.getId() + " (busbreakerviewbus) is set, should not have been carried over from invalid previous view",
                        Double.isNaN(getter.apply(bvb)));
            }

            // test one other view
            vl = networkVoltageLevelSupplier.get();
            setAllBusViewBus(vl, 1.0, setter);
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            for (Bus bvb : vl.getBusView().getBuses()) {
                assertTrue("case " + bvb.getId() + " (busbreakerviewbus) is set, should not have been carried over from invalid previous view",
                        Double.isNaN(getter.apply(bvb)));
            }
        } else { // BUS_BREAKER
            // No need to test the busbreakerview in busbreakertopology,
            // it's not a view  it's directly the configured buses.
            // so test only with busview view
            // NOTE: for busbreakertopology we keep the previous values.
            vl = networkVoltageLevelSupplier.get();
            setAllBusViewBus(vl, 1.0, setter);
            ((VoltageLevelImpl) vl).invalidateCalculatedBuses();
            for (Bus bvb : vl.getBusView().getBuses()) {
                assertEquals("case busbreakertopology " + bvb.getId() + " (busbreakerviewbus) is not set but should be copied from configured buses",
                        1, getter.apply(bvb), 0.0);
            }
        }
    }

    private void setAllBusBreakerViewBusAndBusViewBus(VoltageLevel vl, double value, BiConsumer<Bus, Double> setter) {
        // order doesn't matter when we set everything, some things
        // are set twice but it doesn't matter.
        setAllBusBreakerViewBus(vl, value, setter);
        setAllBusViewBus(vl, value, setter);
    }

    private void setAllBusViewBus(VoltageLevel vl, double value, BiConsumer<Bus, Double> setter) {
        for (Bus bvb : vl.getBusView().getBuses()) {
            setter.accept(bvb, value);
        }
    }

    private void setAllBusBreakerViewBus(VoltageLevel vl, double value, BiConsumer<Bus, Double> setter) {
        for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
            setter.accept(bbvb, value);
        }
    }

    private void setAndCheckBusViewBus(VoltageLevel vl, String busviewbusid, List<String> busbreakerviewbusids, Function<Bus, Double> getter, BiConsumer<Bus, Double> setter) {
        setter.accept(vl.getBusView().getBus(busviewbusid), 1.);
        for (String busbreakerviewbusid : busbreakerviewbusids) {
            assertEquals("case " + busviewbusid + " (busviewbus) is set, " + busbreakerviewbusid + " (busbreakerviewbus) should have been set",
                    1, getter.apply(vl.getBusBreakerView().getBus(busbreakerviewbusid)), 0);
        }
        for (Bus bvb : vl.getBusView().getBuses()) {
            if (!busviewbusid.equals(bvb.getId())) {
                assertTrue("case " + busviewbusid + " (busviewbus) is set, " + bvb.getId() + " (busviewbus) should not have been set",
                        Double.isNaN(getter.apply(bvb)));
            }
        }
        for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
            if (!busbreakerviewbusids.contains(bbvb.getId())) {
                assertTrue("case " + busviewbusid + " (busviewbus) is set, " + bbvb.getId() + " (busbreakerviewbus) should not have been set",
                        Double.isNaN(getter.apply(bbvb)));
            }
        }
    }

    private void setAndCheckBusBreakerBus(VoltageLevel vl, String busbreakerviewbusid, String busviewbusid, Function<Bus, Double> getter, BiConsumer<Bus, Double> setter) {
        setter.accept(vl.getBusBreakerView().getBus(busbreakerviewbusid), 1.);
        if (!busviewbusid.isEmpty()) {
            assertEquals("case " + busbreakerviewbusid + " (busbreakerviewbus) is set, " + busviewbusid + " (busviewbus) should have been set",
                    1, getter.apply(vl.getBusView().getBus(busviewbusid)), 0);
        }
        for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
            if (!busbreakerviewbusid.equals(bbvb.getId())) {
                assertTrue("case " + busbreakerviewbusid + " (busbreakerviewbus) is set, " + bbvb.getId() + " (busbreakerviewbus) should not have been set",
                        Double.isNaN(getter.apply(bbvb)));
            }
        }
        for (Bus bvb : vl.getBusView().getBuses()) {
            if (!busviewbusid.equals(bvb.getId())) {
                assertTrue("case " + busbreakerviewbusid + "(busbreakerviewbus) is set, " + bvb.getId() + " (busviewbus) should not have been set",
                        Double.isNaN(getter.apply(bvb)));
            }
        }
    }

    // Setting all busbreakerviewbus connected in the busview to get deterministic behavior
    private void setAndCheckBusBreakerBusDeterministic(VoltageLevel vl, List<String> busbreakerviewbusids, String busviewbusid, Function<Bus, Double> getter, BiConsumer<Bus, Double> setter) {
        for (String busbreakerviewbusid : busbreakerviewbusids) {
            setter.accept(vl.getBusBreakerView().getBus(busbreakerviewbusid), 1.);
        }
        if (!busviewbusid.isEmpty()) {
            assertEquals("case " + busbreakerviewbusids + " (busbreakerviewbus) is set, " + busviewbusid + " (busviewbus) should have been set",
                    1, getter.apply(vl.getBusView().getBus(busviewbusid)), 0);
        }
        for (Bus bbvb : vl.getBusBreakerView().getBuses()) {
            if (!busbreakerviewbusids.contains(bbvb.getId())) {
                assertTrue("case " + busbreakerviewbusids + " (busbreakerviewbus) is set, " + bbvb.getId() + " (busbreakerviewbus) should not have been set",
                        Double.isNaN(getter.apply(bbvb)));
            }
        }
        for (Bus bvb : vl.getBusView().getBuses()) {
            if (!busviewbusid.equals(bvb.getId())) {
                assertTrue("case " + busbreakerviewbusids + "(busbreakerviewbus) is set, " + bvb.getId() + " (busviewbus) should not have been set",
                        Double.isNaN(getter.apply(bvb)));
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
