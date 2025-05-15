/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes;
import com.powsybl.network.store.model.ReactiveCapabilityCurvePointAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol <nicolas.rol at rte-france.com>
 */
class ReactiveCapabilityCurveImplTest {
    private ReactiveCapabilityCurveImpl createCurve(ReactiveCapabilityCurvePointAttributes... points) {
        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> map = new TreeMap<>();
        for (ReactiveCapabilityCurvePointAttributes pt : points) {
            map.put(pt.getP(), pt);
        }

        ReactiveCapabilityCurveAttributes reactiveCapabilityCurveAttributes = ReactiveCapabilityCurveAttributes.builder()
            .points(map)
            .ownerDescription("ReactiveCapabilityCurve owner")
            .build();
        return new ReactiveCapabilityCurveImpl(reactiveCapabilityCurveAttributes);
    }

    @Test
    void testReactiveCapabilityCurve() {
        ReactiveCapabilityCurvePointAttributes.ReactiveCapabilityCurvePointAttributesBuilder builder1 = ReactiveCapabilityCurvePointAttributes.builder();
        builder1.p(100.0).minQ(200.0).maxQ(300.0);
        ReactiveCapabilityCurvePointAttributes.ReactiveCapabilityCurvePointAttributesBuilder builder2 = ReactiveCapabilityCurvePointAttributes.builder();
        builder2.p(200.0).minQ(300.0).maxQ(400.0);
        ReactiveCapabilityCurveImpl curve = createCurve(builder1.build(), builder2.build());
        // bounds test
        assertEquals(200.0, curve.getMinQ(100.0), 0.0);
        assertEquals(300.0, curve.getMaxQ(100.0), 0.0);
        assertEquals(300.0, curve.getMinQ(200.0), 0.0);
        assertEquals(400.0, curve.getMaxQ(200.0), 0.0);

        // interpolation test
        assertEquals(250.0, curve.getMinQ(150.0), 0.0);
        assertEquals(350.0, curve.getMaxQ(150.0), 0.0);
        assertEquals(210.0, curve.getMinQ(110.0), 0.0);
        assertEquals(310.0, curve.getMaxQ(110.0), 0.0);

        // out of bounds test
        assertEquals(200.0, curve.getMinQ(0.0), 0.0);
        assertEquals(300.0, curve.getMaxQ(0.0), 0.0);
        assertEquals(300.0, curve.getMinQ(1000.0), 0.0);
        assertEquals(400.0, curve.getMaxQ(1000.0), 0.0);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testReactiveCapabilityCurveWithReactiveLimitsExtrapolation(boolean extrapolate) {
        ReactiveCapabilityCurvePointAttributes.ReactiveCapabilityCurvePointAttributesBuilder builder1 = ReactiveCapabilityCurvePointAttributes.builder();
        builder1.p(100.0).minQ(200.0).maxQ(300.0);
        ReactiveCapabilityCurvePointAttributes.ReactiveCapabilityCurvePointAttributesBuilder builder2 = ReactiveCapabilityCurvePointAttributes.builder();
        builder2.p(200.0).minQ(300.0).maxQ(400.0);
        ReactiveCapabilityCurvePointAttributes.ReactiveCapabilityCurvePointAttributesBuilder builder3 = ReactiveCapabilityCurvePointAttributes.builder();
        builder3.p(300.0).minQ(300.0).maxQ(400.0);
        ReactiveCapabilityCurvePointAttributes.ReactiveCapabilityCurvePointAttributesBuilder builder4 = ReactiveCapabilityCurvePointAttributes.builder();
        builder4.p(400.0).minQ(310.0).maxQ(390.0);
        ReactiveCapabilityCurveImpl curve = createCurve(builder1.build(), builder2.build(), builder3.build(), builder4.build());
        // bounds test
        assertEquals(200.0, curve.getMinQ(100.0, extrapolate), 0.0);
        assertEquals(300.0, curve.getMaxQ(100.0, extrapolate), 0.0);
        assertEquals(300.0, curve.getMinQ(200.0, extrapolate), 0.0);
        assertEquals(400.0, curve.getMaxQ(200.0, extrapolate), 0.0);

        // interpolation test
        assertEquals(250.0, curve.getMinQ(150.0, extrapolate), 0.0);
        assertEquals(350.0, curve.getMaxQ(150.0, extrapolate), 0.0);
        assertEquals(210.0, curve.getMinQ(110.0, extrapolate), 0.0);
        assertEquals(310.0, curve.getMaxQ(110.0, extrapolate), 0.0);

        // out of bounds test
        assertEquals(extrapolate ? 100.0 : 200.0, curve.getMinQ(0.0, extrapolate), 0.0);
        assertEquals(extrapolate ? 200.0 : 300.0, curve.getMaxQ(0.0, extrapolate), 0.0);
        assertEquals(extrapolate ? 320.0 : 310.0, curve.getMinQ(500.0, extrapolate), 0.0);
        assertEquals(extrapolate ? 380.0 : 390.0, curve.getMaxQ(500.0, extrapolate), 0.0);

        // intersecting reactive limits test
        assertEquals(extrapolate ? 350.0 : 310.0, curve.getMinQ(1500.0, extrapolate), 0.0);
        assertEquals(extrapolate ? 350.0 : 390.0, curve.getMaxQ(1500.0, extrapolate), 0.0);
    }

    @Test
    void testWithNegativeZeroValue() {
        ReactiveCapabilityCurvePointAttributes.ReactiveCapabilityCurvePointAttributesBuilder builder1 = ReactiveCapabilityCurvePointAttributes.builder();
        builder1.p(100.0).minQ(200.0).maxQ(300.0);
        ReactiveCapabilityCurvePointAttributes.ReactiveCapabilityCurvePointAttributesBuilder builder2 = ReactiveCapabilityCurvePointAttributes.builder();
        builder2.p(200.0).minQ(300.0).maxQ(400.0);
        ReactiveCapabilityCurveImpl curve = createCurve(builder1.build(), builder2.build());
        // "-0.0 == 0.0" (JLS), but "Double.compareTo(-0.0, 0.0) = -1"
        // This test asserts that -0.0 is considered as equal to 0.0 by the reactive capability curve.
        assertEquals(200.0, curve.getMinQ(-0.0), 0.0);
    }
}
