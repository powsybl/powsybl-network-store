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

import static com.powsybl.network.store.model.TemporaryLimitType.*;
import static org.junit.Assert.*;

/**
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public class LimitSelectorTest {

    TreeMap<Integer, TemporaryLimitAttributes> tempLimitsA;
    TreeMap<Integer, TemporaryLimitAttributes> tempLimitsB;
    TreeMap<Integer, TemporaryLimitAttributes> tempLimitsC;

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

        tempLimitsB = new TreeMap<>();
        tempLimitsB.put(60, TemporaryLimitAttributes.builder()
                .acceptableDuration(60)
                .value(600)
                .build());

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
    }

    @Test
    public void getAllTemporaryLimitsTest() {
        LineAttributes line = new LineAttributes();
        line.setLimits(CURRENT_LIMIT, 1, LimitsAttributes.builder().permanentLimit(100).build());
        line.setLimits(APPARENT_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(200).build());
        line.setLimits(ACTIVE_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build());
        line.setLimits(CURRENT_LIMIT, 2, LimitsAttributes.builder().permanentLimit(1000).build());
        line.setLimits(APPARENT_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build());
        line.setLimits(ACTIVE_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(3000).build());

        ThreeWindingsTransformerAttributes threeWindingTransformer = ThreeWindingsTransformerAttributes.builder()
                .leg1(new LegAttributes())
                .leg2(new LegAttributes())
                .leg3(new LegAttributes()).build();
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 1, LimitsAttributes.builder().permanentLimit(100).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(200).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build());
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 2, LimitsAttributes.builder().permanentLimit(1000).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(3000).build());
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 3, LimitsAttributes.builder().permanentLimit(100000).temporaryLimits(tempLimitsC).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 3, LimitsAttributes.builder().permanentLimit(200000).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 3, LimitsAttributes.builder().permanentLimit(300000).build());

        DanglingLineAttributes danglingLine = new DanglingLineAttributes();

        assertEquals(3, line.getAllTemporaryLimits().size());
        assertEquals(6, threeWindingTransformer.getAllTemporaryLimits().size());
        assertEquals(0, danglingLine.getAllTemporaryLimits().size());
    }

    @Test
    public void getTemporaryLimitsByTypeAndSideTest() {
        LineAttributes line = new LineAttributes();
        line.setLimits(CURRENT_LIMIT, 1, LimitsAttributes.builder().permanentLimit(100).build());
        line.setLimits(APPARENT_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(200).build());
        line.setLimits(ACTIVE_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build());
        line.setLimits(CURRENT_LIMIT, 2, LimitsAttributes.builder().permanentLimit(1000).build());
        line.setLimits(APPARENT_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build());
        line.setLimits(ACTIVE_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(3000).build());

        ThreeWindingsTransformerAttributes threeWindingTransformer = ThreeWindingsTransformerAttributes.builder()
                .leg1(new LegAttributes())
                .leg2(new LegAttributes())
                .leg3(new LegAttributes()).build();
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 1, LimitsAttributes.builder().permanentLimit(100).temporaryLimits(tempLimitsA).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(200).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build());
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 2, LimitsAttributes.builder().permanentLimit(1000).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(3000).build());
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 3, LimitsAttributes.builder().permanentLimit(100000).temporaryLimits(tempLimitsC).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 3, LimitsAttributes.builder().permanentLimit(200000).temporaryLimits(tempLimitsB).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 3, LimitsAttributes.builder().permanentLimit(300000).temporaryLimits(tempLimitsA).build());

        DanglingLineAttributes danglingLine = new DanglingLineAttributes();

        assertEquals(0, line.getTemporaryLimitsByTypeAndSide(CURRENT_LIMIT, 1).size());
        assertEquals(0, line.getTemporaryLimitsByTypeAndSide(APPARENT_POWER_LIMIT, 1).size());
        assertEquals(2, line.getTemporaryLimitsByTypeAndSide(ACTIVE_POWER_LIMIT, 1).size());
        assertEquals(0, line.getTemporaryLimitsByTypeAndSide(CURRENT_LIMIT, 2).size());
        assertEquals(1, line.getTemporaryLimitsByTypeAndSide(APPARENT_POWER_LIMIT, 2).size());
        assertEquals(0, line.getTemporaryLimitsByTypeAndSide(ACTIVE_POWER_LIMIT, 2).size());

        assertEquals(2, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(CURRENT_LIMIT, 1).size());
        assertEquals(0, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(APPARENT_POWER_LIMIT, 1).size());
        assertEquals(2, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(ACTIVE_POWER_LIMIT, 1).size());
        assertEquals(0, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(CURRENT_LIMIT, 2).size());
        assertEquals(1, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(APPARENT_POWER_LIMIT, 2).size());
        assertEquals(0, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(ACTIVE_POWER_LIMIT, 2).size());
        assertEquals(3, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(CURRENT_LIMIT, 3).size());
        assertEquals(1, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(APPARENT_POWER_LIMIT, 3).size());
        assertEquals(2, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(ACTIVE_POWER_LIMIT, 3).size());

        assertEquals(0, danglingLine.getTemporaryLimitsByTypeAndSide(ACTIVE_POWER_LIMIT, 1).size());

        assertEquals(ACTIVE_POWER_LIMIT, line.getTemporaryLimitsByTypeAndSide(ACTIVE_POWER_LIMIT, 1).get(0).getLimitType());
        assertEquals(1, line.getTemporaryLimitsByTypeAndSide(ACTIVE_POWER_LIMIT, 1).get(0).getSide().intValue());

        assertEquals(CURRENT_LIMIT, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(CURRENT_LIMIT, 3).get(2).getLimitType());
        assertEquals(3, threeWindingTransformer.getTemporaryLimitsByTypeAndSide(CURRENT_LIMIT, 3).get(2).getSide().intValue());
    }

    @Test
    public void getterSetterLimitsDanglingLineTest() {

        DanglingLineAttributes danglingLine = new DanglingLineAttributes();
        danglingLine.setLimits(CURRENT_LIMIT, 1, LimitsAttributes.builder().permanentLimit(100).build());
        danglingLine.setLimits(APPARENT_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(200).build());
        danglingLine.setLimits(ACTIVE_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build());

        assertEquals(100, danglingLine.getLimits(CURRENT_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(200, danglingLine.getLimits(APPARENT_POWER_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(300, danglingLine.getLimits(ACTIVE_POWER_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(2, danglingLine.getLimits(ACTIVE_POWER_LIMIT, 1).getTemporaryLimits().size());
        assertEquals(75, danglingLine.getLimits(ACTIVE_POWER_LIMIT, 1).getTemporaryLimits().get(150).getValue(), 0.001);
        try {
            danglingLine.getLimits(CURRENT_LIMIT, 2);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            danglingLine.setLimits(APPARENT_POWER_LIMIT, 2, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getterSetterLimitsLineTest() {

        LineAttributes line = new LineAttributes();
        line.setLimits(CURRENT_LIMIT, 1, LimitsAttributes.builder().permanentLimit(100).build());
        line.setLimits(APPARENT_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(200).build());
        line.setLimits(ACTIVE_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build());
        line.setLimits(CURRENT_LIMIT, 2, LimitsAttributes.builder().permanentLimit(1000).build());
        line.setLimits(APPARENT_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build());
        line.setLimits(ACTIVE_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(3000).build());

        assertEquals(100, line.getLimits(CURRENT_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(200, line.getLimits(APPARENT_POWER_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(300, line.getLimits(ACTIVE_POWER_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(2, line.getLimits(ACTIVE_POWER_LIMIT, 1).getTemporaryLimits().size());
        assertEquals(75, line.getLimits(ACTIVE_POWER_LIMIT, 1).getTemporaryLimits().get(150).getValue(), 0.001);

        assertEquals(1000, line.getLimits(CURRENT_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(2000, line.getLimits(APPARENT_POWER_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(3000, line.getLimits(ACTIVE_POWER_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(1, line.getLimits(APPARENT_POWER_LIMIT, 2).getTemporaryLimits().size());
        assertEquals(600, line.getLimits(APPARENT_POWER_LIMIT, 2).getTemporaryLimits().get(60).getValue(), 0.001);
        try {
            line.getLimits(CURRENT_LIMIT, 3);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            line.setLimits(APPARENT_POWER_LIMIT, 3, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getterSetterLimitsTwoWindingsTransformerTest() {

        TwoWindingsTransformerAttributes twoWindingTransformer = new TwoWindingsTransformerAttributes();
        twoWindingTransformer.setLimits(CURRENT_LIMIT, 1, LimitsAttributes.builder().permanentLimit(100).build());
        twoWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(200).build());
        twoWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build());
        twoWindingTransformer.setLimits(CURRENT_LIMIT, 2, LimitsAttributes.builder().permanentLimit(1000).build());
        twoWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build());
        twoWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(3000).build());

        assertEquals(100, twoWindingTransformer.getLimits(CURRENT_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(200, twoWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(300, twoWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(2, twoWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 1).getTemporaryLimits().size());
        assertEquals(75, twoWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 1).getTemporaryLimits().get(150).getValue(), 0.001);

        assertEquals(1000, twoWindingTransformer.getLimits(CURRENT_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(2000, twoWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(3000, twoWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(1, twoWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 2).getTemporaryLimits().size());
        assertEquals(600, twoWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 2).getTemporaryLimits().get(60).getValue(), 0.001);
        try {
            twoWindingTransformer.getLimits(CURRENT_LIMIT, 3);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            twoWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 3, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getterSetterLimitsThreeWindingsTransformerTest() {

        ThreeWindingsTransformerAttributes threeWindingTransformer = ThreeWindingsTransformerAttributes.builder()
                .leg1(new LegAttributes())
                .leg2(new LegAttributes())
                .leg3(new LegAttributes()).build();
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 1, LimitsAttributes.builder().permanentLimit(100).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(200).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build());
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 2, LimitsAttributes.builder().permanentLimit(1000).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 2, LimitsAttributes.builder().permanentLimit(3000).build());
        threeWindingTransformer.setLimits(CURRENT_LIMIT, 3, LimitsAttributes.builder().permanentLimit(100000).temporaryLimits(tempLimitsC).build());
        threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 3, LimitsAttributes.builder().permanentLimit(200000).build());
        threeWindingTransformer.setLimits(ACTIVE_POWER_LIMIT, 3, LimitsAttributes.builder().permanentLimit(300000).build());

        assertEquals(100, threeWindingTransformer.getLimits(CURRENT_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(200, threeWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(300, threeWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 1).getPermanentLimit(), 0.001);
        assertEquals(2, threeWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 1).getTemporaryLimits().size());
        assertEquals(75, threeWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 1).getTemporaryLimits().get(150).getValue(), 0.001);

        assertEquals(1000, threeWindingTransformer.getLimits(CURRENT_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(2000, threeWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(3000, threeWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 2).getPermanentLimit(), 0.001);
        assertEquals(1, threeWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 2).getTemporaryLimits().size());
        assertEquals(600, threeWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 2).getTemporaryLimits().get(60).getValue(), 0.001);

        assertEquals(100000, threeWindingTransformer.getLimits(CURRENT_LIMIT, 3).getPermanentLimit(), 0.001);
        assertEquals(200000, threeWindingTransformer.getLimits(APPARENT_POWER_LIMIT, 3).getPermanentLimit(), 0.001);
        assertEquals(300000, threeWindingTransformer.getLimits(ACTIVE_POWER_LIMIT, 3).getPermanentLimit(), 0.001);
        assertEquals(3, threeWindingTransformer.getLimits(CURRENT_LIMIT, 3).getTemporaryLimits().size());
        assertEquals(7, threeWindingTransformer.getLimits(CURRENT_LIMIT, 3).getTemporaryLimits().get(25000).getValue(), 0.001);
        try {
            threeWindingTransformer.getLimits(CURRENT_LIMIT, 4);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            threeWindingTransformer.setLimits(APPARENT_POWER_LIMIT, 4, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getEquipmentTypeTest() {
        assertEquals(ResourceType.LINE, new LineAttributes().getEquipmentType());
        assertEquals(ResourceType.TWO_WINDINGS_TRANSFORMER, new TwoWindingsTransformerAttributes().getEquipmentType());
        assertEquals(ResourceType.THREE_WINDINGS_TRANSFORMER, new ThreeWindingsTransformerAttributes().getEquipmentType());
        assertEquals(ResourceType.DANGLING_LINE, new DanglingLineAttributes().getEquipmentType());

        // Tests needed to prevent the database data from being desynchronized if ResourceType evolves.
        assertEquals("LINE", ResourceType.LINE.toString());
        assertEquals("TWO_WINDINGS_TRANSFORMER", ResourceType.TWO_WINDINGS_TRANSFORMER.toString());
        assertEquals("THREE_WINDINGS_TRANSFORMER", ResourceType.THREE_WINDINGS_TRANSFORMER.toString());
        assertEquals("DANGLING_LINE", ResourceType.DANGLING_LINE.toString());
    }

    @Test
    public void getSideListTest() {
        assertEquals(2, new LineAttributes().getSideList().size());
        assertEquals(2, new TwoWindingsTransformerAttributes().getSideList().size());
        assertEquals(3, new ThreeWindingsTransformerAttributes().getSideList().size());
        assertEquals(1, new DanglingLineAttributes().getSideList().size());
    }
}
