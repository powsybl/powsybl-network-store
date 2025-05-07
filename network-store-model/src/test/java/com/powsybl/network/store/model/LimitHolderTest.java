/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public class LimitHolderTest {

    TreeMap<Integer, TemporaryLimitAttributes> tempLimitsA;
    TreeMap<Integer, TemporaryLimitAttributes> tempLimitsB;
    TreeMap<Integer, TemporaryLimitAttributes> tempLimitsC;
    LimitsAttributes limitsAttributesA;
    LimitsAttributes limitsAttributesB;
    LimitsAttributes limitsAttributesC;

    @Before
    public void setUp() {
        tempLimitsA = new TreeMap<>();
        tempLimitsA.put(50, TemporaryLimitAttributes.builder()
                .acceptableDuration(50)
                .value(500)
                .build());
        tempLimitsA.put(150, TemporaryLimitAttributes.builder()
                .acceptableDuration(150)
                .value(75)
                .build());

        limitsAttributesA = new LimitsAttributes();
        limitsAttributesA.setTemporaryLimits(tempLimitsA);

        tempLimitsB = new TreeMap<>();
        tempLimitsB.put(60, TemporaryLimitAttributes.builder()
                .acceptableDuration(60)
                .value(600)
                .build());

        limitsAttributesB = new LimitsAttributes();
        limitsAttributesB.setTemporaryLimits(tempLimitsB);

        tempLimitsC = new TreeMap<>();
        tempLimitsC.put(5000, TemporaryLimitAttributes.builder()
                .acceptableDuration(5000)
                .value(50000)
                .build());
        tempLimitsC.put(15000, TemporaryLimitAttributes.builder()
                .acceptableDuration(15000)
                .value(7500)
                .build());
        tempLimitsC.put(25000, TemporaryLimitAttributes.builder()
                .acceptableDuration(25000)
                .value(7)
                .build());

        limitsAttributesC = new LimitsAttributes();
        limitsAttributesC.setTemporaryLimits(tempLimitsC);
    }

    @Test
    public void branchLimitsTest() {
        LineAttributes line = new LineAttributes();
        assertNull(line.getCurrentLimits(1, "group1"));
        assertNull(line.getCurrentLimits(2, "group2"));
        try {
            line.getCurrentLimits(3, "group3");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        line.setCurrentLimits(1, limitsAttributesA, "group1");
        line.setCurrentLimits(2, limitsAttributesB, "group2");
        try {
            line.setCurrentLimits(3, limitsAttributesC, "group3");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        assertEquals(limitsAttributesA, line.getCurrentLimits(1, "group1"));
        assertEquals(limitsAttributesB, line.getCurrentLimits(2, "group2"));

        assertNull(line.getApparentPowerLimits(1, "group3"));
        assertNull(line.getApparentPowerLimits(2, "group3"));
        try {
            line.getApparentPowerLimits(3, "group3");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        line.setApparentPowerLimits(1, limitsAttributesA, "group1");
        line.setApparentPowerLimits(2, limitsAttributesB, "group2");
        try {
            line.setApparentPowerLimits(3, limitsAttributesC, "group3");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        assertEquals(limitsAttributesA, line.getApparentPowerLimits(1, "group1"));
        assertEquals(limitsAttributesB, line.getApparentPowerLimits(2, "group2"));

        assertNull(line.getActivePowerLimits(1, "group3"));
        assertNull(line.getActivePowerLimits(2, "group3"));
        try {
            line.getActivePowerLimits(3, "group3");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        line.setActivePowerLimits(1, limitsAttributesA, "group1");
        line.setActivePowerLimits(2, limitsAttributesB, "group2");
        try {
            line.setActivePowerLimits(3, limitsAttributesC, "group3");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        assertEquals(limitsAttributesA, line.getActivePowerLimits(1, "group1"));
        assertEquals(limitsAttributesB, line.getActivePowerLimits(2, "group2"));

    }

    @Test
    public void danglingLineLimitsTest() {
        DanglingLineAttributes line = new DanglingLineAttributes();
        assertNull(line.getCurrentLimits(1, "group1"));
        try {
            line.getCurrentLimits(2, "group2");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        line.setCurrentLimits(1, limitsAttributesA, "group1");
        try {
            line.setCurrentLimits(2, limitsAttributesB, "group2");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        assertEquals(limitsAttributesA, line.getCurrentLimits(1, "group1"));

        assertNull(line.getApparentPowerLimits(1, "group3"));
        try {
            line.getApparentPowerLimits(2, "group3");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        line.setApparentPowerLimits(1, limitsAttributesA, "group1");
        try {
            line.setApparentPowerLimits(2, limitsAttributesB, "group2");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        assertEquals(limitsAttributesA, line.getApparentPowerLimits(1, "group1"));

        assertNull(line.getActivePowerLimits(1, "group3"));
        try {
            line.getActivePowerLimits(2, "group3");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        line.setActivePowerLimits(1, limitsAttributesA, "group1");
        try {
            line.setActivePowerLimits(2, limitsAttributesB, "group2");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
        assertEquals(limitsAttributesA, line.getActivePowerLimits(1, "group1"));
    }

    @Test
    public void threeWindingsTransformerLimitsTest() {
        ThreeWindingsTransformerAttributes transformer = new ThreeWindingsTransformerAttributes();
        transformer.setLeg1(LegAttributes.builder().build());
        try {
            transformer.getCurrentLimits(4, "group4");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }

        transformer.setCurrentLimits(1, limitsAttributesA, "group1");
        assertEquals(limitsAttributesA, transformer.getCurrentLimits(1, "group1"));

        transformer.setActivePowerLimits(1, limitsAttributesA, "group1");
        assertEquals(limitsAttributesA, transformer.getActivePowerLimits(1, "group1"));

        transformer.setApparentPowerLimits(1, limitsAttributesA, "group1");
        assertEquals(limitsAttributesA, transformer.getApparentPowerLimits(1, "group1"));
    }

    @Test
    public void operationalLimitsGroupTest() {
        LineAttributes line = new LineAttributes();
        line.setCurrentLimits(1, limitsAttributesA, "group1");
        line.setCurrentLimits(2, limitsAttributesB, "group2");
        assertNotNull(line.getOperationalLimitsGroups(1));
        assertNotNull(line.getOperationalLimitsGroups(2));
        try {
            line.getOperationalLimitsGroups(3);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }

        DanglingLineAttributes danglingLine = new DanglingLineAttributes();
        danglingLine.setCurrentLimits(1, limitsAttributesA, "group1");
        assertNotNull(danglingLine.getOperationalLimitsGroups(1));
        try {
            danglingLine.getOperationalLimitsGroups(2);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }

        ThreeWindingsTransformerAttributes transformer = new ThreeWindingsTransformerAttributes();
        transformer.setLeg1(LegAttributes.builder().build());
        transformer.setCurrentLimits(1, limitsAttributesA, "group1");
        assertNotNull(transformer.getOperationalLimitsGroups(1));
        try {
            transformer.getOperationalLimitsGroups(4);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown side", e.getMessage());
        }
    }

    @Test
    public void getSideListTest() {
        assertEquals(2, new LineAttributes().getSideList().size());
        assertEquals(2, new TwoWindingsTransformerAttributes().getSideList().size());
        assertEquals(3, new ThreeWindingsTransformerAttributes().getSideList().size());
        assertEquals(1, new DanglingLineAttributes().getSideList().size());
    }

    @Test
    public void testAddTemporaryLimit() {
        assertEquals(2, limitsAttributesA.getTemporaryLimits().size());
        assertEquals(2, tempLimitsA.size());
        limitsAttributesA.addTemporaryLimit(TemporaryLimitAttributes.builder()
            .acceptableDuration(25)
            .value(1000)
            .build());
        assertEquals(3, limitsAttributesA.getTemporaryLimits().size());
        assertEquals(3, tempLimitsA.size());

        limitsAttributesB.addTemporaryLimit(null);
        assertEquals(1, limitsAttributesB.getTemporaryLimits().size());
        assertEquals(1, tempLimitsB.size());
    }
}
