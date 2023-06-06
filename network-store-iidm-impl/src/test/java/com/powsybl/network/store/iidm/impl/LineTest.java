/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryLimitAttributes;

import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class LineTest {

    //TODO: there is a similar test in the TCK tests. A CurrentLimitsTest extends AbstractCurrentLimitsTest should be created and this test can be deleted.
    // The TCK test doesn't pass yet. As is, the network-store implementation of setV(v) on buses is not consistent. We have problems with the views we are working on (BusBreakerView or BusView).
    @Test
    public void isOverloadedTest() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        LineImpl l1 = (LineImpl) network.getLine("L1");
        l1.getTerminal1().setP(10);
        l1.getTerminal1().setQ(0);
        l1.getTerminal1().getBusView().getBus().setV(400.0);
        assertFalse(l1.isOverloaded());

        l1.getTerminal1().setP(400);
        l1.setCurrentLimits(Branch.Side.ONE, new LimitsAttributes(40, null));
        assertTrue(l1.getNullableCurrentLimits1().getTemporaryLimits().isEmpty());
        assertTrue(l1.isOverloaded());

        TreeMap<Integer, TemporaryLimitAttributes> temporaryLimits = new TreeMap<>();
        temporaryLimits.put(5, TemporaryLimitAttributes.builder().name("TempLimit5").value(1000).acceptableDuration(5).fictitious(false).build());
        l1.setCurrentLimits(Branch.Side.ONE, new LimitsAttributes(40, temporaryLimits));
        l1.setCurrentLimits(Branch.Side.TWO, new LimitsAttributes(40, temporaryLimits));
        assertEquals(5, l1.getOverloadDuration());

        assertTrue(l1.checkPermanentLimit(Branch.Side.ONE, LimitType.CURRENT));
        assertTrue(l1.checkPermanentLimit1(LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit(Branch.Side.TWO, LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit2(LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit(Branch.Side.ONE, LimitType.APPARENT_POWER));
        assertFalse(l1.checkPermanentLimit(Branch.Side.TWO, LimitType.ACTIVE_POWER));
        assertThrows(UnsupportedOperationException.class, () -> l1.checkPermanentLimit(Branch.Side.TWO, LimitType.VOLTAGE));

        Branch.Overload overload = l1.checkTemporaryLimits(Branch.Side.ONE, LimitType.CURRENT);
        assertEquals("TempLimit5", overload.getTemporaryLimit().getName());
        assertEquals(40.0, overload.getPreviousLimit(), 0);
        assertEquals(5, overload.getTemporaryLimit().getAcceptableDuration());
        assertNull(l1.checkTemporaryLimits(Branch.Side.TWO, LimitType.CURRENT));

        temporaryLimits.put(5, TemporaryLimitAttributes.builder().name("TempLimit5").value(20).acceptableDuration(5).fictitious(false).build());
        assertEquals(Integer.MAX_VALUE, l1.getOverloadDuration());

        temporaryLimits.put(10, TemporaryLimitAttributes.builder().name("TempLimit10").value(8).acceptableDuration(10).fictitious(false).build());
        // check duration sorting order: first entry has the highest duration
        assertEquals(10., l1.getNullableCurrentLimits1().getTemporaryLimits().iterator().next().getAcceptableDuration(), 0);
    }

    @Test
    public void testAddConnectablePositionExtensionToLine() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        Line l1 = network.getLine("L1");

        l1.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                    .withName("cpa1")
                    .withOrder(0)
                    .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .newFeeder2()
                    .withName("cpa2")
                    .withOrder(0)
                    .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        Line l2 = network.newLine()
                .setId("L2")
                .setVoltageLevel1("VL1")
                .setNode1(3)
                .setVoltageLevel2("VL2")
                .setNode2(3)
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        l2.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                    .withName("cpa1")
                    .withOrder(0)
                    .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        Line l3 = network.newLine()
                .setId("L3")
                .setVoltageLevel1("VL1")
                .setNode1(3)
                .setVoltageLevel2("VL2")
                .setNode2(3)
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        l3.newExtension(ConnectablePositionAdder.class).newFeeder2().withDirection(ConnectablePosition.Direction.TOP).withOrder(0).withName("cpa3").add().add();

        assertEquals("cpa1", l1.getExtension(ConnectablePosition.class).getFeeder1().getName().orElseThrow());
        assertEquals(ConnectablePosition.Direction.TOP, l1.getExtension(ConnectablePosition.class).getFeeder1().getDirection());
        assertEquals(0, (int) l1.getExtension(ConnectablePosition.class).getFeeder1().getOrder().orElseThrow());
        assertEquals("cpa2", l1.getExtension(ConnectablePosition.class).getFeeder2().getName().orElseThrow());
        assertEquals(ConnectablePosition.Direction.TOP, l1.getExtension(ConnectablePosition.class).getFeeder2().getDirection());
        assertEquals(0, (int) l1.getExtension(ConnectablePosition.class).getFeeder2().getOrder().orElseThrow());

        assertEquals("cpa1", l2.getExtension(ConnectablePosition.class).getFeeder1().getName().orElseThrow());
        assertEquals(ConnectablePosition.Direction.TOP, l2.getExtension(ConnectablePosition.class).getFeeder1().getDirection());
        assertEquals(0, (int) l2.getExtension(ConnectablePosition.class).getFeeder1().getOrder().orElseThrow());
        assertNull(l2.getExtension(ConnectablePosition.class).getFeeder2());

        assertEquals("cpa3", l3.getExtension(ConnectablePosition.class).getFeeder2().getName().orElseThrow());
        assertEquals(ConnectablePosition.Direction.TOP, l3.getExtension(ConnectablePosition.class).getFeeder2().getDirection());
        assertEquals(0, (int) l3.getExtension(ConnectablePosition.class).getFeeder2().getOrder().orElseThrow());
        assertNull(l3.getExtension(ConnectablePosition.class).getFeeder1());
    }
}
