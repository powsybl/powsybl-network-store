/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.network.store.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NetworkStoreRepositoryTest {

    private static final UUID NETWORK_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    @Autowired
    protected NetworkStoreRepository networkStoreRepository;

    @Test
    public void insertTemporaryLimitsInLinesTest() {

        String equipmentIdA = "idLineA";
        String equipmentIdB = "idLineB";

        OwnerInfo infoLineA = new OwnerInfo(
                equipmentIdA,
                ResourceType.LINE,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo infoLineB = new OwnerInfo(
                equipmentIdB,
                ResourceType.LINE,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo infoLineX = new OwnerInfo(
                "badID",
                ResourceType.LINE,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );

        Resource<LineAttributes> resLineA = Resource.lineBuilder()
                .id(equipmentIdA)
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("idLineA")
                        .currentLimits1(LimitsAttributes.builder().permanentLimit(20.).build())
                        .build())
                .build();

        Resource<LineAttributes> resLineB = Resource.lineBuilder()
                .id(equipmentIdB)
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("idLineB")
                        .currentLimits1(LimitsAttributes.builder().permanentLimit(20.).build())
                        .build())
                .build();

        assertEquals(resLineA.getId(), infoLineA.getEquipmentId());
        assertEquals(resLineB.getId(), infoLineB.getEquipmentId());
        assertNotEquals(resLineA.getId(), infoLineX.getEquipmentId());

        TemporaryLimitAttributes templimitAOkSide1a = TemporaryLimitAttributes.builder()
                .side(1)
                .acceptableDuration(100)
                .limitType(LimitType.CURRENT)
                .build();

        TemporaryLimitAttributes templimitAOkSide2a = TemporaryLimitAttributes.builder()
                .side(2)
                .acceptableDuration(100)
                .limitType(LimitType.CURRENT)
                .build();

        TemporaryLimitAttributes templimitAOkSide2b = TemporaryLimitAttributes.builder()
                .side(2)
                .acceptableDuration(200)
                .limitType(LimitType.CURRENT)
                .build();

        // If there are multiple instance of a limit on the same side with the same acceptable duration, only one is kept.
        TemporaryLimitAttributes templimitAOkSide2bSameAcceptableDuration = TemporaryLimitAttributes.builder()
                .side(2)
                .acceptableDuration(200)
                .limitType(LimitType.CURRENT)
                .build();

        TemporaryLimitAttributes templimitWrongEquipmentId = TemporaryLimitAttributes.builder()
                .side(1)
                .acceptableDuration(100)
                .limitType(LimitType.CURRENT)
                .build();

        TemporaryLimitAttributes templimitBOkSide1a = TemporaryLimitAttributes.builder()
                .side(1)
                .acceptableDuration(100)
                .limitType(LimitType.CURRENT)
                .build();

        TemporaryLimitAttributes templimitBOkSide1b = TemporaryLimitAttributes.builder()
                .side(1)
                .acceptableDuration(200)
                .limitType(LimitType.CURRENT)
                .build();

        TemporaryLimitAttributes templimitBOkSide1c = TemporaryLimitAttributes.builder()
                .side(1)
                .acceptableDuration(300)
                .limitType(LimitType.CURRENT)
                .build();

        List<Resource<LineAttributes>> lines = new ArrayList<>();
        lines.add(resLineA);
        lines.add(resLineB);

        List<TemporaryLimitAttributes> temporaryLimitsA = new ArrayList<>();
        temporaryLimitsA.add(templimitAOkSide1a);
        temporaryLimitsA.add(templimitAOkSide2a);
        temporaryLimitsA.add(templimitAOkSide2b);
        temporaryLimitsA.add(templimitAOkSide2bSameAcceptableDuration);

        List<TemporaryLimitAttributes> temporaryLimitsB = new ArrayList<>();
        temporaryLimitsB.add(templimitBOkSide1a);
        temporaryLimitsB.add(templimitBOkSide1b);
        temporaryLimitsB.add(templimitBOkSide1c);

        List<TemporaryLimitAttributes> temporaryLimitsX = new ArrayList<>();
        temporaryLimitsX.add(templimitWrongEquipmentId);

        Map<OwnerInfo, List<TemporaryLimitAttributes>> map = new HashMap<>();

        map.put(infoLineA, temporaryLimitsA);
        map.put(infoLineB, temporaryLimitsB);
        map.put(infoLineX, temporaryLimitsX);

        assertNull(resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineA.getAttributes().getCurrentLimits2());
        assertNull(resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineB.getAttributes().getCurrentLimits2());

        networkStoreRepository.insertTemporaryLimitsInEquipments(NETWORK_UUID, lines, new HashMap<>());

        assertNull(resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineA.getAttributes().getCurrentLimits2());
        assertNull(resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineB.getAttributes().getCurrentLimits2());

        networkStoreRepository.insertTemporaryLimitsInEquipments(NETWORK_UUID, lines, map);
        assertNotNull(resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNotNull(resLineA.getAttributes().getCurrentLimits2().getTemporaryLimits());
        assertEquals(1, resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits().size());
        assertEquals(2, resLineA.getAttributes().getCurrentLimits2().getTemporaryLimits().size());
        assertNotNull(resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineB.getAttributes().getCurrentLimits2());
        assertEquals(3, resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits().size());
    }

    @Test
    public void insertReactiveCapabilityCurvesInGeneratorsTest() {

        String equipmentIdA = "idGeneratorA";
        String equipmentIdB = "idGeneratorB";
        String equipmentIdMinMax = "idGeneratorMinMax";

        OwnerInfo infoGeneratorA = new OwnerInfo(
                equipmentIdA,
                ResourceType.GENERATOR,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo infoGeneratorB = new OwnerInfo(
                equipmentIdB,
                ResourceType.GENERATOR,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo infoGeneratorMinMax = new OwnerInfo(
                equipmentIdMinMax,
                ResourceType.GENERATOR,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo infoGeneratorX = new OwnerInfo(
                "badID",
                ResourceType.GENERATOR,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );

        Resource<GeneratorAttributes> resGeneratorA = Resource.generatorBuilder()
                .id(equipmentIdA)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("idGeneratorA")
                        .build()) // In this case, the reactivelimits are not initialized
                .build();

        Resource<GeneratorAttributes> resGeneratorB = Resource.generatorBuilder()
                .id(equipmentIdB)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("vl2")
                        .name("idGeneratorB")
                        .reactiveLimits(ReactiveCapabilityCurveAttributes.builder().build()) // In this case, the reactivelimits are already initialized as ReactiveCapabilityCurveAttributes
                        .build())
                .build();

        Resource<GeneratorAttributes> resGeneratorMinMax = Resource.generatorBuilder()
                .id(equipmentIdMinMax)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId("vl3")
                        .name("idGeneratorMinMax")
                        .reactiveLimits(MinMaxReactiveLimitsAttributes.builder() // In this case, the reactivelimits are already initialized as MinMaxReactiveLimitsAttributes
                                .maxQ(50.)
                                .minQ(20.)
                                .build())
                        .build())
                .build();

        assertEquals(resGeneratorA.getId(), infoGeneratorA.getEquipmentId());
        assertEquals(resGeneratorB.getId(), infoGeneratorB.getEquipmentId());
        assertEquals(resGeneratorMinMax.getId(), infoGeneratorMinMax.getEquipmentId());
        assertNotEquals(resGeneratorA.getId(), infoGeneratorX.getEquipmentId());
        assertNotEquals(resGeneratorB.getId(), infoGeneratorX.getEquipmentId());
        assertNotEquals(resGeneratorMinMax.getId(), infoGeneratorX.getEquipmentId());

        ReactiveCapabilityCurvePointAttributes curvePointOka = ReactiveCapabilityCurvePointAttributes.builder()
                .minQ(-100.)
                .maxQ(100.)
                .p(0.)
                .build();

        ReactiveCapabilityCurvePointAttributes curvePointOkb = ReactiveCapabilityCurvePointAttributes.builder()
                .minQ(10.)
                .maxQ(30.)
                .p(20.)
                .build();

        ReactiveCapabilityCurvePointAttributes curvePointOkc = ReactiveCapabilityCurvePointAttributes.builder()
                .minQ(5.)
                .maxQ(25.)
                .p(15.)
                .build();

        // If there are multiple instance of a curve point with the same value P, only one is kept.
        ReactiveCapabilityCurvePointAttributes curvePointSameValueP = ReactiveCapabilityCurvePointAttributes.builder()
                .minQ(10.)
                .maxQ(30.)
                .p(20.)
                .build();

        ReactiveCapabilityCurvePointAttributes curvePointWrongEquipmentId = ReactiveCapabilityCurvePointAttributes.builder()
                .minQ(10.)
                .maxQ(30.)
                .p(20.)
                .build();

        List<Resource<GeneratorAttributes>> generators = new ArrayList<>();
        generators.add(resGeneratorA);
        generators.add(resGeneratorB);
        generators.add(resGeneratorMinMax);

        List<ReactiveCapabilityCurvePointAttributes> curvePointsForGeneratorA = new ArrayList<>();
        curvePointsForGeneratorA.add(curvePointOka);
        curvePointsForGeneratorA.add(curvePointOkb);
        curvePointsForGeneratorA.add(curvePointOkc);
        curvePointsForGeneratorA.add(curvePointSameValueP);

        List<ReactiveCapabilityCurvePointAttributes> curvePointsForGeneratorB = new ArrayList<>();
        curvePointsForGeneratorB.add(curvePointOka);
        curvePointsForGeneratorB.add(curvePointOkb);

        List<ReactiveCapabilityCurvePointAttributes> curvePointsX = new ArrayList<>();
        curvePointsX.add(curvePointWrongEquipmentId);

        Map<OwnerInfo, List<ReactiveCapabilityCurvePointAttributes>> map = new HashMap<>();

        map.put(infoGeneratorA, curvePointsForGeneratorA);
        map.put(infoGeneratorB, curvePointsForGeneratorB);
        map.put(infoGeneratorX, curvePointsX);

        assertNull(resGeneratorA.getAttributes().getReactiveLimits());
        assertTrue(resGeneratorB.getAttributes().getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes);
        assertNull(((ReactiveCapabilityCurveAttributes) resGeneratorB.getAttributes().getReactiveLimits()).getPoints());
        assertTrue(resGeneratorMinMax.getAttributes().getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes);

        networkStoreRepository.insertReactiveCapabilityCurvePointsInEquipments(NETWORK_UUID, generators, new HashMap<>());

        assertNull(resGeneratorA.getAttributes().getReactiveLimits());
        assertTrue(resGeneratorB.getAttributes().getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes);
        assertNull(((ReactiveCapabilityCurveAttributes) resGeneratorB.getAttributes().getReactiveLimits()).getPoints());
        assertTrue(resGeneratorMinMax.getAttributes().getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes);

        networkStoreRepository.insertReactiveCapabilityCurvePointsInEquipments(NETWORK_UUID, generators, map);

        assertTrue(resGeneratorA.getAttributes().getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes);
        assertNotNull(((ReactiveCapabilityCurveAttributes) resGeneratorA.getAttributes().getReactiveLimits()).getPoints());
        assertEquals(3, ((ReactiveCapabilityCurveAttributes) resGeneratorA.getAttributes().getReactiveLimits()).getPoints().size());

        assertTrue(resGeneratorB.getAttributes().getReactiveLimits() instanceof ReactiveCapabilityCurveAttributes);
        assertNotNull(((ReactiveCapabilityCurveAttributes) resGeneratorB.getAttributes().getReactiveLimits()).getPoints());
        assertEquals(2, ((ReactiveCapabilityCurveAttributes) resGeneratorB.getAttributes().getReactiveLimits()).getPoints().size());

        assertTrue(resGeneratorMinMax.getAttributes().getReactiveLimits() instanceof MinMaxReactiveLimitsAttributes);
    }
}
