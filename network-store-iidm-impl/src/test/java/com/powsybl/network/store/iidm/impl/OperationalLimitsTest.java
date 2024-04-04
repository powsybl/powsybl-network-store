/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl.LegImpl;
/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */

class OperationalLimitsTest {

    @Test
    void lineOperationalLimits1Test() {
        Network network = CreateNetworksUtil.createBusBreakerNetwokWithMultipleEquipments();
        LineImpl l1 = (LineImpl) network.getLine("LINE1");
        l1.cancelSelectedOperationalLimitsGroup1();

        // Test OperationalLimitsGroup1
        // Test get limits
        assertEquals(Optional.empty(), l1.getOperationalLimitsGroup1("group1"));
        assertEquals(Optional.empty(), l1.getCurrentLimits1());
        assertEquals(Optional.empty(), l1.getActivePowerLimits1());
        assertEquals(Optional.empty(), l1.getApparentPowerLimits1());

        // Test limits creation
        l1.newOperationalLimitsGroup1("group1");
        l1.setSelectedOperationalLimitsGroup1("group");
        assertEquals("group", l1.getSelectedOperationalLimitsGroupId1().get());
        l1.cancelSelectedOperationalLimitsGroup1();
        assertEquals(Optional.empty(), l1.getSelectedOperationalLimitsGroupId1());
        l1.setSelectedOperationalLimitsGroup1("group1");
        assertNotNull(l1.getSelectedOperationalLimitsGroup1());
        l1.removeOperationalLimitsGroup1("group1");
        assertEquals(Optional.empty(), l1.getOperationalLimitsGroup1("group1"));
        try {
            l1.removeOperationalLimitsGroup1("group1");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Operational limits group 'group1' does not exist", e.getMessage());
        }
        l1.newCurrentLimits1().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, l1.getCurrentLimits1().get().getPermanentLimit());
        l1.newActivePowerLimits1().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, l1.getActivePowerLimits1().get().getPermanentLimit());
        l1.newApparentPowerLimits1().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, l1.getApparentPowerLimits1().get().getPermanentLimit());
        assertNotNull(l1.getSelectedOperationalLimitsGroup1());
    }

    @Test
        void lineOperationalLimits2Test() {
        Network network = CreateNetworksUtil.createBusBreakerNetwokWithMultipleEquipments();
        LineImpl l1 = (LineImpl) network.getLine("LINE1");
        assertEquals(Optional.empty(), l1.getOperationalLimitsGroup2("group2"));
        l1.cancelSelectedOperationalLimitsGroup2();

        // Test OperationalLimitsGroup2
        // test get limits
        assertEquals(Optional.empty(), l1.getCurrentLimits2());
        assertEquals(Optional.empty(), l1.getActivePowerLimits2());
        assertEquals(Optional.empty(), l1.getApparentPowerLimits2());

        // Test limits creation
        l1.newOperationalLimitsGroup2("group2");
        l1.setSelectedOperationalLimitsGroup2("group");
        assertEquals("group", l1.getSelectedOperationalLimitsGroupId2().get());
        l1.cancelSelectedOperationalLimitsGroup2();
        assertEquals(Optional.empty(), l1.getSelectedOperationalLimitsGroupId2());
        l1.setSelectedOperationalLimitsGroup1("group2");
        assertNotNull(l1.getSelectedOperationalLimitsGroup2());
        l1.removeOperationalLimitsGroup2("group2");
        assertEquals(Optional.empty(), l1.getOperationalLimitsGroup2("group2"));
        try {
            l1.removeOperationalLimitsGroup2("group2");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Operational limits group 'group2' does not exist", e.getMessage());
        }
        l1.newCurrentLimits2().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, l1.getCurrentLimits2().get().getPermanentLimit());
        l1.newActivePowerLimits2().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, l1.getActivePowerLimits2().get().getPermanentLimit());
        l1.newApparentPowerLimits2().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, l1.getApparentPowerLimits2().get().getPermanentLimit());
    }

    @Test
    void tieLineOperationalLimitsTest() {
        Network network = CreateNetworksUtil.createDummyNodeBreakerWithTieLineNetwork();
        TieLineImpl tl1 = (TieLineImpl) network.getTieLine("TL");

        // Test OperationalLimitsGroup1
        tl1.cancelSelectedOperationalLimitsGroup1();
        assertEquals(Optional.empty(), tl1.getOperationalLimitsGroup1("group1"));
        tl1.newOperationalLimitsGroup1("group1").newCurrentLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        tl1.setSelectedOperationalLimitsGroup1("group");
        assertEquals("group", tl1.getSelectedOperationalLimitsGroupId1().get());
        tl1.cancelSelectedOperationalLimitsGroup1();
        assertEquals(Optional.empty(), tl1.getSelectedOperationalLimitsGroupId1());
        tl1.setSelectedOperationalLimitsGroup1("group1");
        assertNotNull(tl1.getSelectedOperationalLimitsGroup1());
        tl1.removeOperationalLimitsGroup1("group1");
        assertEquals(Optional.empty(), tl1.getOperationalLimitsGroup1("group1"));
        try {
            tl1.removeOperationalLimitsGroup1("group1");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Operational limits group 'group1' does not exist", e.getMessage());
        }

        // Test OperationalLimitsGroup2
        tl1.cancelSelectedOperationalLimitsGroup2();
        assertEquals(Optional.empty(), tl1.getOperationalLimitsGroup2("group2"));
        tl1.newOperationalLimitsGroup2("group2").newCurrentLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        tl1.setSelectedOperationalLimitsGroup2("group");
        assertEquals("group", tl1.getSelectedOperationalLimitsGroupId2().get());
        tl1.cancelSelectedOperationalLimitsGroup2();
        assertEquals(Optional.empty(), tl1.getSelectedOperationalLimitsGroupId2());
        tl1.setSelectedOperationalLimitsGroup1("group2");
        assertNotNull(tl1.getSelectedOperationalLimitsGroup2());
        tl1.removeOperationalLimitsGroup2("group2");
        assertEquals(Optional.empty(), tl1.getOperationalLimitsGroup2("group2"));
        try {
            tl1.removeOperationalLimitsGroup2("group2");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Operational limits group 'group2' does not exist", e.getMessage());
        }
    }

    @Test
    void threeWindingsTransformersOperationalLimitsTest() {
        Network network = CreateNetworksUtil.createBusBreakerNetwokWithMultipleEquipments();
        ThreeWindingsTransformerImpl twt = (ThreeWindingsTransformerImpl) network.getThreeWindingsTransformer("TWT1");
        LegImpl leg1 = (LegImpl) twt.getLeg1();
        leg1.cancelSelectedOperationalLimitsGroup();

        // Test get limits
        assertEquals(Optional.empty(), leg1.getOperationalLimitsGroup("group1"));
        assertEquals(Optional.empty(), leg1.getCurrentLimits());
        assertEquals(Optional.empty(), leg1.getActivePowerLimits());
        assertEquals(Optional.empty(), leg1.getApparentPowerLimits());

        // Test limits creation
        leg1.newOperationalLimitsGroup("group1");
        leg1.setSelectedOperationalLimitsGroup("group");
        assertEquals("group", leg1.getSelectedOperationalLimitsGroupId().get());
        leg1.cancelSelectedOperationalLimitsGroup();
        assertEquals(Optional.empty(), leg1.getSelectedOperationalLimitsGroupId());
        leg1.setSelectedOperationalLimitsGroup("group1");
        leg1.removeOperationalLimitsGroup("group1");
        assertEquals(Optional.empty(), leg1.getOperationalLimitsGroup("group1"));
        try {
            leg1.removeOperationalLimitsGroup("group1");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Operational limits group 'group1' does not exist", e.getMessage());
        }
        leg1.newCurrentLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, leg1.getCurrentLimits().get().getPermanentLimit());
        leg1.newActivePowerLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, leg1.getActivePowerLimits().get().getPermanentLimit());
        leg1.newApparentPowerLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, leg1.getApparentPowerLimits().get().getPermanentLimit());
        assertNotNull(leg1.getSelectedOperationalLimitsGroup());
    }

    @Test
    void operationalLimitsGroupImplTest() {
        Network network = CreateNetworksUtil.createBusBreakerNetwokWithMultipleEquipments();
        LineImpl l1 = (LineImpl) network.getLine("LINE1");

        // test limits creation and removal
        OperationalLimitsGroup operationalLimitsGroup = l1.newOperationalLimitsGroup1("group1");
        assertTrue(operationalLimitsGroup.isEmpty());
        operationalLimitsGroup.newCurrentLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, operationalLimitsGroup.getCurrentLimits().get().getPermanentLimit());
        operationalLimitsGroup.newActivePowerLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, operationalLimitsGroup.getActivePowerLimits().get().getPermanentLimit());
        operationalLimitsGroup.newApparentPowerLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        assertEquals(9999, operationalLimitsGroup.getApparentPowerLimits().get().getPermanentLimit());
        operationalLimitsGroup.removeCurrentLimits();
        assertEquals(Optional.empty(), operationalLimitsGroup.getCurrentLimits());
        operationalLimitsGroup.removeActivePowerLimits();
        assertEquals(Optional.empty(), operationalLimitsGroup.getActivePowerLimits());
        operationalLimitsGroup.removeApparentPowerLimits();
        assertEquals(Optional.empty(), operationalLimitsGroup.getApparentPowerLimits());
        assertTrue(l1.getOperationalLimitsGroup1("group1").get().isEmpty());
        operationalLimitsGroup.removeCurrentLimits();
        operationalLimitsGroup.removeActivePowerLimits();
        operationalLimitsGroup.removeApparentPowerLimits();

        // test limits creation and removal via loadingLimits methods
        operationalLimitsGroup.newCurrentLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        operationalLimitsGroup.newActivePowerLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        operationalLimitsGroup.newApparentPowerLimits().setPermanentLimit(9999).beginTemporaryLimit()
                .setName("name1").setAcceptableDuration(9999).setValue(9999).endTemporaryLimit().add();
        operationalLimitsGroup.getCurrentLimits().get().remove();
        operationalLimitsGroup.getActivePowerLimits().get().remove();
        operationalLimitsGroup.getApparentPowerLimits().get().remove();
        assertTrue(l1.getOperationalLimitsGroup1("group1").get().isEmpty());
    }
}
