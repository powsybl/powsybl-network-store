/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class SvAttributesTest {

    @Test
    public void testBindAttributesForBranch() {
        BranchSvAttributes attributes = BranchSvAttributes.builder()
                .p1(100.0)
                .q1(50.0)
                .p2(-100.0)
                .q2(-50.0)
                .build();
        List<Object> values = new ArrayList<>();
        BranchSvAttributes.bindAttributes(attributes, values);

        assertEquals(4, values.size());
        assertEquals(100.0, values.get(0));
        assertEquals(50.0, values.get(1));
        assertEquals(-100.0, values.get(2));
        assertEquals(-50.0, values.get(3));
    }

    @Test
    public void testUpdateAttributesForBranch() {
        BranchAttributes existingAttributes = LineAttributes.builder()
                .p1(0.0)
                .q1(0.0)
                .p2(0.0)
                .q2(0.0)
                .build();
        BranchSvAttributes newAttributes = BranchSvAttributes.builder()
                .p1(120.0)
                .q1(60.0)
                .p2(-120.0)
                .q2(-60.0)
                .build();

        BranchSvAttributes.updateAttributes(existingAttributes, newAttributes);

        assertEquals(120.0, existingAttributes.getP1(), 0.1);
        assertEquals(60.0, existingAttributes.getQ1(), 0.1);
        assertEquals(-120.0, existingAttributes.getP2(), 0.1);
        assertEquals(-60.0, existingAttributes.getQ2(), 0.1);
    }

    @Test
    public void testBindAttributesForInjection() {
        InjectionSvAttributes attributes = InjectionSvAttributes.builder()
                .p(150.0)
                .q(75.0)
                .build();
        List<Object> values = new ArrayList<>();
        InjectionSvAttributes.bindAttributes(attributes, values);

        assertEquals(2, values.size());
        assertEquals(150.0, values.get(0));
        assertEquals(75.0, values.get(1));
    }

    @Test
    public void testUpdateAttributesForInjection() {
        InjectionAttributes existingAttributes = LoadAttributes.builder()
                .p(0.0)
                .q(0.0)
                .build();
        InjectionSvAttributes newAttributes = InjectionSvAttributes.builder()
                .p(180.0)
                .q(90.0)
                .build();

        InjectionSvAttributes.updateAttributes(existingAttributes, newAttributes);

        assertEquals(180.0, existingAttributes.getP(), 0.1);
        assertEquals(90.0, existingAttributes.getQ(), 0.1);
    }

    @Test
    public void testBindAttributesForThreeWindingsTransformer() {
        ThreeWindingsTransformerSvAttributes attributes = ThreeWindingsTransformerSvAttributes.builder()
                .p1(10.0)
                .p2(20.0)
                .p3(30.0)
                .q1(5.0)
                .q2(10.0)
                .q3(15.0)
                .build();
        List<Object> values = new ArrayList<>();
        ThreeWindingsTransformerSvAttributes.bindAttributes(attributes, values);

        assertEquals(6, values.size());
        assertEquals(10.0, values.get(0));
        assertEquals(5.0, values.get(1));
        assertEquals(20.0, values.get(2));
        assertEquals(10.0, values.get(3));
        assertEquals(30.0, values.get(4));
        assertEquals(15.0, values.get(5));
    }

    @Test
    public void testUpdateAttributesForThreeWindingsTransformer() {
        ThreeWindingsTransformerAttributes existingAttributes = ThreeWindingsTransformerAttributes.builder()
                .p1(0.0)
                .p2(0.0)
                .p3(0.0)
                .q1(0.0)
                .q2(0.0)
                .q3(0.0)
                .build();
        ThreeWindingsTransformerSvAttributes newAttributes = ThreeWindingsTransformerSvAttributes.builder()
                .p1(10.0)
                .p2(20.0)
                .p3(30.0)
                .q1(5.0)
                .q2(10.0)
                .q3(15.0)
                .build();

        ThreeWindingsTransformerSvAttributes.updateAttributes(existingAttributes, newAttributes);

        assertEquals(10.0, existingAttributes.getP1(), 0.1);
        assertEquals(5.0, existingAttributes.getQ1(), 0.1);
        assertEquals(20.0, existingAttributes.getP2(), 0.1);
        assertEquals(10.0, existingAttributes.getQ2(), 0.1);
        assertEquals(30.0, existingAttributes.getP3(), 0.1);
        assertEquals(15.0, existingAttributes.getQ3(), 0.1);
    }

    @Test
    public void testBindAttributesForVoltageLevel() {
        List<CalculatedBusAttributes> calculatedBusAttributesBv = List.of(CalculatedBusAttributes.builder().v(8.0).angle(6.9).build(), CalculatedBusAttributes.builder().v(9.0).angle(7.9).build());
        List<CalculatedBusAttributes> calculatedBusAttributesBbv = List.of(CalculatedBusAttributes.builder().v(10.0).angle(3.9).build(), CalculatedBusAttributes.builder().v(6.0).angle(1.9).build());
        VoltageLevelSvAttributes attributes = VoltageLevelSvAttributes.builder()
                .calculatedBusesForBusView(calculatedBusAttributesBv)
                .calculatedBusesForBusBreakerView(calculatedBusAttributesBbv)
                .build();
        List<Object> values = new ArrayList<>();
        VoltageLevelSvAttributes.bindAttributes(attributes, values);

        assertEquals(2, values.size());
        assertEquals(calculatedBusAttributesBv, values.get(0));
        assertEquals(calculatedBusAttributesBbv, values.get(1));
    }

    @Test
    public void testUpdateAttributesForVoltageLevel() {
        List<CalculatedBusAttributes> calculatedBusAttributesBv = List.of(CalculatedBusAttributes.builder().v(8.0).angle(6.9).build(), CalculatedBusAttributes.builder().v(9.0).angle(7.9).build());
        List<CalculatedBusAttributes> calculatedBusAttributesBbv = List.of(CalculatedBusAttributes.builder().v(10.0).angle(3.9).build(), CalculatedBusAttributes.builder().v(6.0).angle(1.9).build());
        VoltageLevelAttributes existingAttributes = VoltageLevelAttributes.builder()
                .calculatedBusesForBusView(new ArrayList<>())
                .calculatedBusesForBusBreakerView(new ArrayList<>())
                .build();
        VoltageLevelSvAttributes newAttributes = VoltageLevelSvAttributes.builder()
                .calculatedBusesForBusView(calculatedBusAttributesBv)
                .calculatedBusesForBusBreakerView(calculatedBusAttributesBbv)
                .build();

        VoltageLevelSvAttributes.updateAttributes(existingAttributes, newAttributes);

        assertEquals(calculatedBusAttributesBv, existingAttributes.getCalculatedBusesForBusView());
        assertEquals(calculatedBusAttributesBbv, existingAttributes.getCalculatedBusesForBusBreakerView());
    }
}
