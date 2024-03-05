/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import org.junit.Before;
import org.junit.Test;

import com.powsybl.iidm.network.LimitType;

import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public class LimitHolderTest {

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
        line.setLimits(LimitType.CURRENT, 1, LimitsAttributes.builder().permanentLimit(100).build(), "group1");
        line.setLimits(LimitType.APPARENT_POWER, 1, LimitsAttributes.builder().permanentLimit(200).build(), "group1");
        line.setLimits(LimitType.ACTIVE_POWER, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build(), "group1");
        line.setLimits(LimitType.CURRENT, 2, LimitsAttributes.builder().permanentLimit(1000).build(), "group1");
        line.setLimits(LimitType.APPARENT_POWER, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build(), "group1");
        line.setLimits(LimitType.ACTIVE_POWER, 2, LimitsAttributes.builder().permanentLimit(3000).build(), "group1");

        ThreeWindingsTransformerAttributes threeWindingTransformer = ThreeWindingsTransformerAttributes.builder()
                .leg1(new LegAttributes())
                .leg2(new LegAttributes())
                .leg3(new LegAttributes()).build();
        threeWindingTransformer.setLimits(LimitType.CURRENT, 1, LimitsAttributes.builder().permanentLimit(100).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 1, LimitsAttributes.builder().permanentLimit(200).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.CURRENT, 2, LimitsAttributes.builder().permanentLimit(1000).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 2, LimitsAttributes.builder().permanentLimit(3000).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.CURRENT, 3, LimitsAttributes.builder().permanentLimit(100000).temporaryLimits(tempLimitsC).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 3, LimitsAttributes.builder().permanentLimit(200000).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 3, LimitsAttributes.builder().permanentLimit(300000).build(), "group1");

        DanglingLineAttributes danglingLine = new DanglingLineAttributes();

        LimitsInfos lineLimitsInfos = line.getAllLimitsInfos();
        assertEquals(3, lineLimitsInfos.getTemporaryLimits().size());
        LimitsInfos threeWindingTransformerLimitsInfos = threeWindingTransformer.getAllLimitsInfos();
        assertEquals(6, threeWindingTransformerLimitsInfos.getTemporaryLimits().size());
        LimitsInfos danglingLineLimitsInfos = danglingLine.getAllLimitsInfos();
        assertEquals(0, danglingLineLimitsInfos.getTemporaryLimits().size());
    }

    @Test
    public void getTemporaryLimitsByTypeAndSideTest() {
        LineAttributes line = new LineAttributes();
        line.setLimits(LimitType.CURRENT, 1, LimitsAttributes.builder().permanentLimit(100).build(), "group1");
        line.setLimits(LimitType.APPARENT_POWER, 1, LimitsAttributes.builder().permanentLimit(200).build(), "group1");
        line.setLimits(LimitType.ACTIVE_POWER, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build(), "group1");
        line.setLimits(LimitType.CURRENT, 2, LimitsAttributes.builder().permanentLimit(1000).build(), "group1");
        line.setLimits(LimitType.APPARENT_POWER, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build(), "group1");
        line.setLimits(LimitType.ACTIVE_POWER, 2, LimitsAttributes.builder().permanentLimit(3000).build(), "group1");

        ThreeWindingsTransformerAttributes threeWindingTransformer = ThreeWindingsTransformerAttributes.builder()
                .leg1(new LegAttributes())
                .leg2(new LegAttributes())
                .leg3(new LegAttributes()).build();
        threeWindingTransformer.setLimits(LimitType.CURRENT, 1, LimitsAttributes.builder().permanentLimit(100).temporaryLimits(tempLimitsA).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 1, LimitsAttributes.builder().permanentLimit(200).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.CURRENT, 2, LimitsAttributes.builder().permanentLimit(1000).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 2, LimitsAttributes.builder().permanentLimit(3000).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.CURRENT, 3, LimitsAttributes.builder().permanentLimit(100000).temporaryLimits(tempLimitsC).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 3, LimitsAttributes.builder().permanentLimit(200000).temporaryLimits(tempLimitsB).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 3, LimitsAttributes.builder().permanentLimit(300000).temporaryLimits(tempLimitsA).build(), "group1");

        DanglingLineAttributes danglingLine = new DanglingLineAttributes();

        LimitsInfos lineLimitsInfos1 = new LimitsInfos();
        line.fillLimitsInfosByTypeAndSide(lineLimitsInfos1, LimitType.CURRENT, 1);
        assertEquals(0, lineLimitsInfos1.getTemporaryLimits().size());
        line.fillLimitsInfosByTypeAndSide(lineLimitsInfos1, LimitType.APPARENT_POWER, 1);
        assertEquals(0, lineLimitsInfos1.getTemporaryLimits().size());
        line.fillLimitsInfosByTypeAndSide(lineLimitsInfos1, LimitType.ACTIVE_POWER, 1);
        assertEquals(2, lineLimitsInfos1.getTemporaryLimits().size());
        line.fillLimitsInfosByTypeAndSide(lineLimitsInfos1, LimitType.CURRENT, 2);
        assertEquals(2, lineLimitsInfos1.getTemporaryLimits().size());
        line.fillLimitsInfosByTypeAndSide(lineLimitsInfos1, LimitType.APPARENT_POWER, 2);
        assertEquals(3, lineLimitsInfos1.getTemporaryLimits().size());
        line.fillLimitsInfosByTypeAndSide(lineLimitsInfos1, LimitType.ACTIVE_POWER, 2);
        assertEquals(3, lineLimitsInfos1.getTemporaryLimits().size());

        LimitsInfos threeWindingTransformerLimitsInfos1 = new LimitsInfos();
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.CURRENT, 1);
        assertEquals(2, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.APPARENT_POWER, 1);
        assertEquals(2, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.ACTIVE_POWER, 1);
        assertEquals(4, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.CURRENT, 2);
        assertEquals(4, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.APPARENT_POWER, 2);
        assertEquals(5, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.ACTIVE_POWER, 2);
        assertEquals(5, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.CURRENT, 3);
        assertEquals(8, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.APPARENT_POWER, 3);
        assertEquals(9, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos1, LimitType.ACTIVE_POWER, 3);
        assertEquals(11, threeWindingTransformerLimitsInfos1.getTemporaryLimits().size());

        LimitsInfos danglingLineLimitsInfos1 = new LimitsInfos();
        danglingLine.fillLimitsInfosByTypeAndSide(danglingLineLimitsInfos1, LimitType.CURRENT, 1);
        assertEquals(0, danglingLineLimitsInfos1.getTemporaryLimits().size());

        LimitsInfos lineLimitsInfos2 = new LimitsInfos();
        line.fillLimitsInfosByTypeAndSide(lineLimitsInfos2, LimitType.ACTIVE_POWER, 1);
        assertEquals(LimitType.ACTIVE_POWER, lineLimitsInfos2.getTemporaryLimits().get(0).getLimitType());
        assertEquals(1, lineLimitsInfos2.getTemporaryLimits().get(0).getSide().intValue());

        LimitsInfos threeWindingTransformerLimitsInfos2 = new LimitsInfos();
        threeWindingTransformer.fillLimitsInfosByTypeAndSide(threeWindingTransformerLimitsInfos2, LimitType.CURRENT, 3);
        assertEquals(LimitType.CURRENT, threeWindingTransformerLimitsInfos2.getTemporaryLimits().get(2).getLimitType());
        assertEquals(3, threeWindingTransformerLimitsInfos2.getTemporaryLimits().get(2).getSide().intValue());
    }

    @Test
    public void getterSetterLimitsDanglingLineTest() {

        DanglingLineAttributes danglingLine = new DanglingLineAttributes();
        danglingLine.setLimits(LimitType.CURRENT, 1, LimitsAttributes.builder().permanentLimit(100).build(), "group1");
        danglingLine.setLimits(LimitType.APPARENT_POWER, 1, LimitsAttributes.builder().permanentLimit(200).build(), "group1");
        danglingLine.setLimits(LimitType.ACTIVE_POWER, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build(), "group1");

        assertEquals(100, danglingLine.getLimits(LimitType.CURRENT, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(200, danglingLine.getLimits(LimitType.APPARENT_POWER, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(300, danglingLine.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(2, danglingLine.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getTemporaryLimits().size());
        assertEquals(75, danglingLine.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getTemporaryLimits().get(150).getValue(), 0.001);
        try {
            danglingLine.getLimits(LimitType.CURRENT, 2, "group1");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            danglingLine.setLimits(LimitType.APPARENT_POWER, 2, null, "group1");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getterSetterLimitsLineTest() {

        LineAttributes line = new LineAttributes();
        line.setLimits(LimitType.CURRENT, 1, LimitsAttributes.builder().permanentLimit(100).build(), "group1");
        line.setLimits(LimitType.APPARENT_POWER, 1, LimitsAttributes.builder().permanentLimit(200).build(), "group1");
        line.setLimits(LimitType.ACTIVE_POWER, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build(), "group1");
        line.setLimits(LimitType.CURRENT, 2, LimitsAttributes.builder().permanentLimit(1000).build(), "group1");
        line.setLimits(LimitType.APPARENT_POWER, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build(), "group1");
        line.setLimits(LimitType.ACTIVE_POWER, 2, LimitsAttributes.builder().permanentLimit(3000).build(), "group1");

        assertEquals(100, line.getLimits(LimitType.CURRENT, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(200, line.getLimits(LimitType.APPARENT_POWER, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(300, line.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(2, line.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getTemporaryLimits().size());
        assertEquals(75, line.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getTemporaryLimits().get(150).getValue(), 0.001);

        assertEquals(1000, line.getLimits(LimitType.CURRENT, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(2000, line.getLimits(LimitType.APPARENT_POWER, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(3000, line.getLimits(LimitType.ACTIVE_POWER, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(1, line.getLimits(LimitType.APPARENT_POWER, 2, "group1").getTemporaryLimits().size());
        assertEquals(600, line.getLimits(LimitType.APPARENT_POWER, 2, "group1").getTemporaryLimits().get(60).getValue(), 0.001);
        try {
            line.getLimits(LimitType.CURRENT, 3, "group1");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            line.setLimits(LimitType.APPARENT_POWER, 3, null, "group1");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getterSetterLimitsTwoWindingsTransformerTest() {

        TwoWindingsTransformerAttributes twoWindingTransformer = new TwoWindingsTransformerAttributes();
        twoWindingTransformer.setLimits(LimitType.CURRENT, 1, LimitsAttributes.builder().permanentLimit(100).build(), "group1");
        twoWindingTransformer.setLimits(LimitType.APPARENT_POWER, 1, LimitsAttributes.builder().permanentLimit(200).build(), "group1");
        twoWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build(), "group1");
        twoWindingTransformer.setLimits(LimitType.CURRENT, 2, LimitsAttributes.builder().permanentLimit(1000).build(), "group1");
        twoWindingTransformer.setLimits(LimitType.APPARENT_POWER, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build(), "group1");
        twoWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 2, LimitsAttributes.builder().permanentLimit(3000).build(), "group1");

        assertEquals(100, twoWindingTransformer.getLimits(LimitType.CURRENT, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(200, twoWindingTransformer.getLimits(LimitType.APPARENT_POWER, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(300, twoWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(2, twoWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getTemporaryLimits().size());
        assertEquals(75, twoWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getTemporaryLimits().get(150).getValue(), 0.001);

        assertEquals(1000, twoWindingTransformer.getLimits(LimitType.CURRENT, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(2000, twoWindingTransformer.getLimits(LimitType.APPARENT_POWER, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(3000, twoWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(1, twoWindingTransformer.getLimits(LimitType.APPARENT_POWER, 2, "group1").getTemporaryLimits().size());
        assertEquals(600, twoWindingTransformer.getLimits(LimitType.APPARENT_POWER, 2, "group1").getTemporaryLimits().get(60).getValue(), 0.001);
        try {
            twoWindingTransformer.getLimits(LimitType.CURRENT, 3, "group1");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            twoWindingTransformer.setLimits(LimitType.APPARENT_POWER, 3, null, "group1");
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
        threeWindingTransformer.setLimits(LimitType.CURRENT, 1, LimitsAttributes.builder().permanentLimit(100).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 1, LimitsAttributes.builder().permanentLimit(200).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 1, LimitsAttributes.builder().permanentLimit(300).temporaryLimits(tempLimitsA).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.CURRENT, 2, LimitsAttributes.builder().permanentLimit(1000).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 2, LimitsAttributes.builder().permanentLimit(2000).temporaryLimits(tempLimitsB).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 2, LimitsAttributes.builder().permanentLimit(3000).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.CURRENT, 3, LimitsAttributes.builder().permanentLimit(100000).temporaryLimits(tempLimitsC).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 3, LimitsAttributes.builder().permanentLimit(200000).build(), "group1");
        threeWindingTransformer.setLimits(LimitType.ACTIVE_POWER, 3, LimitsAttributes.builder().permanentLimit(300000).build(), "group1");

        assertEquals(100, threeWindingTransformer.getLimits(LimitType.CURRENT, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(200, threeWindingTransformer.getLimits(LimitType.APPARENT_POWER, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(300, threeWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getPermanentLimit(), 0.001);
        assertEquals(2, threeWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getTemporaryLimits().size());
        assertEquals(75, threeWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 1, "group1").getTemporaryLimits().get(150).getValue(), 0.001);

        assertEquals(1000, threeWindingTransformer.getLimits(LimitType.CURRENT, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(2000, threeWindingTransformer.getLimits(LimitType.APPARENT_POWER, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(3000, threeWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 2, "group1").getPermanentLimit(), 0.001);
        assertEquals(1, threeWindingTransformer.getLimits(LimitType.APPARENT_POWER, 2, "group1").getTemporaryLimits().size());
        assertEquals(600, threeWindingTransformer.getLimits(LimitType.APPARENT_POWER, 2, "group1").getTemporaryLimits().get(60).getValue(), 0.001);

        assertEquals(100000, threeWindingTransformer.getLimits(LimitType.CURRENT, 3, "group1").getPermanentLimit(), 0.001);
        assertEquals(200000, threeWindingTransformer.getLimits(LimitType.APPARENT_POWER, 3, "group1").getPermanentLimit(), 0.001);
        assertEquals(300000, threeWindingTransformer.getLimits(LimitType.ACTIVE_POWER, 3, "group1").getPermanentLimit(), 0.001);
        assertEquals(3, threeWindingTransformer.getLimits(LimitType.CURRENT, 3, "group1").getTemporaryLimits().size());
        assertEquals(7, threeWindingTransformer.getLimits(LimitType.CURRENT, 3, "group1").getTemporaryLimits().get(25000).getValue(), 0.001);
        try {
            threeWindingTransformer.getLimits(LimitType.CURRENT, 4, "group1");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            threeWindingTransformer.setLimits(LimitType.APPARENT_POWER, 4, null, "group1");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getSideListTest() {
        assertEquals(2, new LineAttributes().getSideList().size());
        assertEquals(2, new TwoWindingsTransformerAttributes().getSideList().size());
        assertEquals(3, new ThreeWindingsTransformerAttributes().getSideList().size());
        assertEquals(1, new DanglingLineAttributes().getSideList().size());
    }
}
