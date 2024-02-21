/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.Iterables;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryLimitAttributes;

import org.junit.Test;

import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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
        l1.setCurrentLimits(TwoSides.ONE, new LimitsAttributes(40, null), "DEFAULT");
        assertTrue(l1.getNullableCurrentLimits1().getTemporaryLimits().isEmpty());
        assertTrue(l1.isOverloaded());

        TreeMap<Integer, TemporaryLimitAttributes> temporaryLimits = new TreeMap<>();
        temporaryLimits.put(5, TemporaryLimitAttributes.builder().name("TempLimit5").value(1000).acceptableDuration(5).fictitious(false).build());
        l1.setCurrentLimits(TwoSides.ONE, new LimitsAttributes(40, temporaryLimits), "DEFAULT");
        l1.setCurrentLimits(TwoSides.TWO, new LimitsAttributes(40, temporaryLimits), "DEFAULT");
        assertEquals(5, l1.getOverloadDuration());

        assertTrue(l1.checkPermanentLimit(TwoSides.ONE, LimitType.CURRENT));
        assertTrue(l1.checkPermanentLimit1(LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit(TwoSides.TWO, LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit2(LimitType.CURRENT));
        assertFalse(l1.checkPermanentLimit(TwoSides.ONE, LimitType.APPARENT_POWER));
        assertFalse(l1.checkPermanentLimit(TwoSides.TWO, LimitType.ACTIVE_POWER));
        assertThrows(UnsupportedOperationException.class, () -> l1.checkPermanentLimit(TwoSides.TWO, LimitType.VOLTAGE));

        Overload overload = l1.checkTemporaryLimits(TwoSides.ONE, LimitType.CURRENT);
        assertEquals("TempLimit5", overload.getTemporaryLimit().getName());
        assertEquals(40.0, overload.getPreviousLimit(), 0);
        assertEquals(5, overload.getTemporaryLimit().getAcceptableDuration());
        assertNull(l1.checkTemporaryLimits(TwoSides.TWO, LimitType.CURRENT));

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

    @Test
    public void testTieLine() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
        TieLine tieLine = network.getTieLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc");
        assertEquals("TN_Border_GY11", tieLine.getPairingKey());
        DanglingLine dl1 = tieLine.getDanglingLine1();
        DanglingLine dl2 = tieLine.getDanglingLine2();
        assertNotNull(dl1);
        assertNotNull(dl2);
        assertEquals(dl1.getId(), tieLine.getDanglingLine(TwoSides.ONE).getId());
        assertEquals(dl2.getId(), tieLine.getDanglingLine(TwoSides.TWO).getId());
        assertEquals(dl1.getId(), tieLine.getDanglingLine(dl1.getTerminal().getVoltageLevel().getId()).getId());
        assertEquals(dl2.getId(), tieLine.getDanglingLine(dl2.getTerminal().getVoltageLevel().getId()).getId());
        assertNull(tieLine.getDanglingLine("null"));

        assertEquals(0.84, tieLine.getR(), 1e-3);
        assertEquals(12.6, tieLine.getX(), 1e-3);
        assertEquals(0., tieLine.getG1(), 1e-3);
        assertEquals(0, tieLine.getG2(), 1e-3);
        assertEquals(0, tieLine.getB1(), 1e-3);
        assertEquals(0, tieLine.getB2(), 1e-3);

        tieLine.remove();

        assertNull(network.getTieLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc"));
    }

    @Test
    public void testDanglingLines() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
        TieLine tieLine = network.getTieLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc");
        assertEquals("TN_Border_GY11", tieLine.getPairingKey());

        assertEquals(10, network.getDanglingLineCount());
        VoltageLevel vl1 = network.getVoltageLevel("469df5f7-058f-4451-a998-57a48e8a56fe");
        assertEquals(3, vl1.getDanglingLineCount());

        List<DanglingLine> dls = vl1.getDanglingLineStream().collect(Collectors.toList());
        assertEquals(3, dls.size());
        assertEquals(3, (int) vl1.getDanglingLineStream(DanglingLineFilter.ALL).count());
        assertEquals(3, (int) vl1.getDanglingLineStream(DanglingLineFilter.PAIRED).count());

        VoltageLevel vl2 = network.getVoltageLevel("c1d5bfde8f8011e08e4d00247eb1f55e");
        assertEquals(3, vl2.getDanglingLineCount());

        List<DanglingLine> dls2 = vl2.getDanglingLineStream().collect(Collectors.toList());
        assertEquals(3, dls2.size());
        assertEquals(3, Iterables.size(vl2.getDanglingLines(DanglingLineFilter.ALL)));
        assertEquals(3, (int) vl2.getDanglingLineStream(DanglingLineFilter.ALL).count());
        assertEquals(3, (int) vl2.getDanglingLineStream(DanglingLineFilter.PAIRED).count());

        Bus configuredBus = network.getBusBreakerView().getBus("795a117d-7caf-4fc2-a8d9-dc8f4cf2344a");
        assertEquals(2, (int) configuredBus.getDanglingLineStream().count());
        assertEquals(2, Iterables.size(configuredBus.getDanglingLines(DanglingLineFilter.ALL)));
        assertEquals(2, (int) configuredBus.getDanglingLineStream(DanglingLineFilter.ALL).count());
        assertEquals(2, (int) configuredBus.getDanglingLineStream(DanglingLineFilter.PAIRED).count());

        Bus calculatedBus = network.getBusView().getBus("c1d5bfea8f8011e08e4d00247eb1f55e_0");
        assertEquals(2, (int) calculatedBus.getDanglingLineStream().count());
        assertEquals(2, Iterables.size(calculatedBus.getDanglingLines(DanglingLineFilter.ALL)));
        assertEquals(2, (int) calculatedBus.getDanglingLineStream(DanglingLineFilter.ALL).count());
        assertEquals(2, (int) calculatedBus.getDanglingLineStream(DanglingLineFilter.PAIRED).count());
    }

    @Test
    public void testTieLineTerminals() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
        TieLine tieLine = network.getTieLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc");
        assertEquals("TN_Border_GY11", tieLine.getPairingKey());
        DanglingLine dl1 = tieLine.getDanglingLine1();
        DanglingLine dl2 = tieLine.getDanglingLine2();

        assertNotNull(tieLine.getTerminal1());
        assertSame(tieLine.getTerminal1(), dl1.getTerminal());

        assertNotNull(tieLine.getTerminal2());
        assertSame(tieLine.getTerminal2(), dl2.getTerminal());

        assertSame(tieLine.getTerminal(TwoSides.ONE), dl1.getTerminal());
        assertSame(tieLine.getTerminal(TwoSides.TWO), dl2.getTerminal());

        assertSame(tieLine.getTerminal(dl1.getTerminal().getVoltageLevel().getId()), dl1.getTerminal());
        assertSame(tieLine.getTerminal(dl2.getTerminal().getVoltageLevel().getId()), dl2.getTerminal());

        assertEquals(TwoSides.ONE, tieLine.getSide(dl1.getTerminal()));
        assertEquals(TwoSides.TWO, tieLine.getSide(dl2.getTerminal()));

        String vlId1 = dl1.getTerminal().getVoltageLevel().getId();
        Terminal t1 = dl1.getTerminal();
        assertThrows(PowsyblException.class,
            () -> TieLineImpl.getTerminal(vlId1, t1, t1));

        assertThrows(PowsyblException.class, () -> tieLine.getTerminal("null"));

    }

    @Test
    public void testTieLineLimits() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
        TieLine tieLine = network.getTieLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc");

        DanglingLine dl1 = tieLine.getDanglingLine1();
        DanglingLine dl2 = tieLine.getDanglingLine2();

        assertNotNull(tieLine.getNullableCurrentLimits1());
        assertNotNull(tieLine.getNullableCurrentLimits2());
        assertNull(tieLine.getNullableApparentPowerLimits1());
        assertNull(tieLine.getNullableApparentPowerLimits2());
        assertNotNull(tieLine.getActivePowerLimits1());
        assertNotNull(tieLine.getActivePowerLimits2());
        assertEquals(tieLine.getCurrentLimits1().isPresent(), dl1.getCurrentLimits().isPresent());
        assertEquals(tieLine.getActivePowerLimits1().isPresent(), dl1.getActivePowerLimits().isPresent());
        assertEquals(tieLine.getApparentPowerLimits1().isPresent(), dl1.getApparentPowerLimits().isPresent());
        assertEquals(tieLine.getCurrentLimits2().isPresent(), dl2.getCurrentLimits().isPresent());
        assertEquals(tieLine.getActivePowerLimits2().isPresent(), dl2.getActivePowerLimits().isPresent());
        assertEquals(tieLine.getApparentPowerLimits2().isPresent(), dl2.getApparentPowerLimits().isPresent());
        assertEquals(tieLine.getCurrentLimits(TwoSides.ONE).isPresent(), dl1.getCurrentLimits().isPresent());
        assertEquals(tieLine.getActivePowerLimits(TwoSides.ONE).isPresent(), dl1.getActivePowerLimits().isPresent());
        assertEquals(tieLine.getApparentPowerLimits(TwoSides.ONE).isPresent(), dl1.getApparentPowerLimits().isPresent());
        assertEquals(tieLine.getCurrentLimits(TwoSides.TWO).isPresent(), dl2.getCurrentLimits().isPresent());
        assertEquals(tieLine.getActivePowerLimits(TwoSides.TWO).isPresent(), dl2.getActivePowerLimits().isPresent());
        assertEquals(tieLine.getApparentPowerLimits(TwoSides.TWO).isPresent(), dl2.getApparentPowerLimits().isPresent());
        assertEquals(tieLine.getOperationalLimitsGroups1().size(), dl1.getOperationalLimitsGroups().size());
        assertEquals(tieLine.getOperationalLimitsGroups2().size(), dl2.getOperationalLimitsGroups().size());

        assertFalse(tieLine.isOverloaded());
        assertFalse(tieLine.isOverloaded(1.0f));

        assertEquals(Integer.MAX_VALUE, tieLine.getOverloadDuration());

    }

    @Test
    public void testTieLineLimitsCreation() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
        TieLine tieLine = network.getTieLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc");

        //Test current limit overriding
        assertEquals(2, tieLine.getCurrentLimits1().get().getTemporaryLimits().size());
        CurrentLimits currentlimits1 = tieLine.newCurrentLimits1()
                .setPermanentLimit(10.0)
                .beginTemporaryLimit()
                .setName("dummy")
                .setValue(10.0)
                .setAcceptableDuration(1)
                .endTemporaryLimit()
                .add();
        assertNotNull(currentlimits1);
        assertEquals(1, tieLine.getCurrentLimits1().get().getTemporaryLimits().size());

        assertEquals(2, tieLine.getCurrentLimits2().get().getTemporaryLimits().size());
        CurrentLimits currentlimit2 = tieLine.newCurrentLimits2()
                .setPermanentLimit(10.0)
                .beginTemporaryLimit()
                .setName("dummy2")
                .setValue(10.0)
                .setAcceptableDuration(1)
                .endTemporaryLimit()
                .add();
        assertNotNull(currentlimit2);
        assertEquals(1, tieLine.getCurrentLimits2().get().getTemporaryLimits().size());

        //Test active power limit overriding
        assertNull(tieLine.getNullableActivePowerLimits1());
        assertTrue(tieLine.getActivePowerLimits1().isEmpty());
        ActivePowerLimits activepowerlimits1 = tieLine.newActivePowerLimits1()
                .setPermanentLimit(10.0)
                .beginTemporaryLimit()
                .setName("dummy")
                .setValue(10.0)
                .setAcceptableDuration(1)
                .endTemporaryLimit()
                .add();
        assertNotNull(activepowerlimits1);
        assertEquals(1, tieLine.getActivePowerLimits1().get().getTemporaryLimits().size());

        assertNull(tieLine.getNullableActivePowerLimits2());
        assertTrue(tieLine.getActivePowerLimits2().isEmpty());
        ActivePowerLimits activepowerlimits2 = tieLine.newActivePowerLimits2()
                .setPermanentLimit(10.0)
                .beginTemporaryLimit()
                .setName("dummy")
                .setValue(10.0)
                .setAcceptableDuration(1)
                .endTemporaryLimit()
                .add();
        assertNotNull(activepowerlimits2);
        assertEquals(1, tieLine.getActivePowerLimits2().get().getTemporaryLimits().size());

        //Test apparent power limit overriding

        assertTrue(tieLine.getApparentPowerLimits1().isEmpty());
        ApparentPowerLimits apparentpowerlimits1 = tieLine.newApparentPowerLimits1()
                .setPermanentLimit(10.0)
                .beginTemporaryLimit()
                .setName("dummy")
                .setValue(10.0)
                .setAcceptableDuration(1)
                .endTemporaryLimit()
                .add();
        assertNotNull(apparentpowerlimits1);
        assertEquals(1, tieLine.getApparentPowerLimits1().get().getTemporaryLimits().size());

        assertTrue(tieLine.getApparentPowerLimits2().isEmpty());
        ApparentPowerLimits apparentpowerlimits2 = tieLine.newApparentPowerLimits2()
                .setPermanentLimit(10.0)
                .beginTemporaryLimit()
                .setName("dummy")
                .setValue(10.0)
                .setAcceptableDuration(1)
                .endTemporaryLimit()
                .add();
        assertNotNull(apparentpowerlimits2);
        assertEquals(1, tieLine.getApparentPowerLimits2().get().getTemporaryLimits().size());
    }

    @Test
    public void testTieLineLimitsCheck() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
        TieLine tieLine = network.getTieLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc");
        tieLine.getCurrentLimits1().get().getPermanentLimit();

        assertFalse(tieLine.checkPermanentLimit(TwoSides.ONE, 2.0f, LimitType.CURRENT));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.TWO, 2.0f, LimitType.CURRENT));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.ONE, LimitType.CURRENT));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.TWO, LimitType.CURRENT));

        tieLine.newActivePowerLimits1().setPermanentLimit(10.0).add();
        tieLine.newActivePowerLimits2().setPermanentLimit(10.0).add();

        assertFalse(tieLine.checkPermanentLimit(TwoSides.ONE, 2.0f, LimitType.ACTIVE_POWER));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.TWO, 2.0f, LimitType.ACTIVE_POWER));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.ONE, LimitType.ACTIVE_POWER));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.TWO, LimitType.ACTIVE_POWER));

        tieLine.newApparentPowerLimits1().setPermanentLimit(10.0).add();
        tieLine.newApparentPowerLimits2().setPermanentLimit(10.0).add();

        assertFalse(tieLine.checkPermanentLimit(TwoSides.ONE, 2.0f, LimitType.APPARENT_POWER));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.TWO, 2.0f, LimitType.APPARENT_POWER));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.ONE, LimitType.APPARENT_POWER));
        assertFalse(tieLine.checkPermanentLimit(TwoSides.TWO, LimitType.APPARENT_POWER));
    }
}
