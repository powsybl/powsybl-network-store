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
    public void getSideListTest() {
        assertEquals(2, new LineAttributes().getSideList().size());
        assertEquals(2, new TwoWindingsTransformerAttributes().getSideList().size());
        assertEquals(3, new ThreeWindingsTransformerAttributes().getSideList().size());
        assertEquals(1, new DanglingLineAttributes().getSideList().size());
    }
}
