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

import java.util.*;

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

    @Test
    public void insertTapChangerStepsInTwoWindingsTranformerTest() {

        String equipmentIdA = "id2WTransformerA";
        String equipmentIdB = "id2WTransformerB";

        OwnerInfo info2WTransformerA = new OwnerInfo(
                equipmentIdA,
                ResourceType.TWO_WINDINGS_TRANSFORMER,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo info2WTransformerB = new OwnerInfo(
                equipmentIdB,
                ResourceType.TWO_WINDINGS_TRANSFORMER,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo info2WTransformerX = new OwnerInfo(
                "badID",
                ResourceType.TWO_WINDINGS_TRANSFORMER,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );

        Resource<TwoWindingsTransformerAttributes> res2WTransformerA = Resource.twoWindingsTransformerBuilder()
                .id(equipmentIdA)
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("id2WTransformerA")
                        .ratioTapChangerAttributes(RatioTapChangerAttributes.builder()
                                .lowTapPosition(20)
                                .build())
                        .build())
                .build();

        Resource<TwoWindingsTransformerAttributes> res2WTransformerB = Resource.twoWindingsTransformerBuilder()
                .id(equipmentIdB)
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("id2WTransformerB")
                        .phaseTapChangerAttributes(PhaseTapChangerAttributes.builder()
                                .lowTapPosition(30)
                                .build())
                        .build())
                .build();

        assertEquals(res2WTransformerA.getId(), info2WTransformerA.getEquipmentId());
        assertEquals(res2WTransformerB.getId(), info2WTransformerB.getEquipmentId());
        assertNotEquals(res2WTransformerA.getId(), info2WTransformerX.getEquipmentId());

        TapChangerStepAttributes ratioStepA1 = TapChangerStepAttributes.builder()
                .rho(1.)
                .r(1.)
                .g(1.)
                .b(1.)
                .x(1.)
                .side(0)
                .index(0)
                .type(TapChangerType.RATIO)
                .build();

        TapChangerStepAttributes ratioStepA2 = TapChangerStepAttributes.builder()
                .rho(2.)
                .r(2.)
                .g(2.)
                .b(2.)
                .x(2.)
                .side(0)
                .index(1)
                .type(TapChangerType.RATIO)
                .build();

        TapChangerStepAttributes ratioStepA3 = TapChangerStepAttributes.builder()
                .rho(3.)
                .r(3.)
                .g(3.)
                .b(3.)
                .x(3.)
                .side(0)
                .index(2)
                .type(TapChangerType.RATIO)
                .build();

        TapChangerStepAttributes phaseStepB1 = TapChangerStepAttributes.builder()
                .rho(10.)
                .r(10.)
                .g(10.)
                .b(10.)
                .x(10.)
                .alpha(10.)
                .side(0)
                .index(0)
                .type(TapChangerType.PHASE)
                .build();

        TapChangerStepAttributes phaseStepB2 = TapChangerStepAttributes.builder()
                .rho(20.)
                .r(20.)
                .g(20.)
                .b(20.)
                .x(20.)
                .alpha(20.)
                .side(0)
                .index(1)
                .type(TapChangerType.PHASE)
                .build();

        TapChangerStepAttributes phaseStepB3 = TapChangerStepAttributes.builder()
                .rho(30.)
                .r(30.)
                .g(30.)
                .b(30.)
                .x(30.)
                .alpha(30.)
                .side(0)
                .index(2)
                .type(TapChangerType.PHASE)
                .build();

        TapChangerStepAttributes phaseStepB4 = TapChangerStepAttributes.builder()
                .rho(40.)
                .r(40.)
                .g(40.)
                .b(40.)
                .x(40.)
                .alpha(40.)
                .side(0)
                .index(3)
                .type(TapChangerType.PHASE)
                .build();

        assertEquals(TapChangerType.RATIO, ratioStepA1.getType());
        assertEquals(TapChangerType.PHASE, phaseStepB1.getType());

        List<Resource<TwoWindingsTransformerAttributes>> twoWTransformers = new ArrayList<>();
        twoWTransformers.add(res2WTransformerA);
        twoWTransformers.add(res2WTransformerB);

        List<TapChangerStepAttributes> tapChangerStepsA = new ArrayList<>();
        tapChangerStepsA.add(ratioStepA1);
        tapChangerStepsA.add(ratioStepA2);
        tapChangerStepsA.add(ratioStepA3);

        List<TapChangerStepAttributes> tapChangerStepsB = new ArrayList<>();
        tapChangerStepsB.add(phaseStepB1);
        tapChangerStepsB.add(phaseStepB2);
        tapChangerStepsB.add(phaseStepB3);
        tapChangerStepsB.add(phaseStepB4);

        Map<OwnerInfo, List<TapChangerStepAttributes>> mapA = new HashMap<>();
        Map<OwnerInfo, List<TapChangerStepAttributes>> mapB = new HashMap<>();

        mapA.put(info2WTransformerA, tapChangerStepsA);
        mapB.put(info2WTransformerB, tapChangerStepsB);

        assertNull(res2WTransformerA.getAttributes().getRatioTapChangerAttributes().getSteps());
        assertNull(res2WTransformerA.getAttributes().getPhaseTapChangerAttributes());
        assertNull(res2WTransformerB.getAttributes().getRatioTapChangerAttributes());
        assertNull(res2WTransformerB.getAttributes().getPhaseTapChangerAttributes().getSteps());

        networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, twoWTransformers, new HashMap<>());

        assertNull(res2WTransformerA.getAttributes().getRatioTapChangerAttributes().getSteps());
        assertNull(res2WTransformerA.getAttributes().getPhaseTapChangerAttributes());
        assertNull(res2WTransformerB.getAttributes().getRatioTapChangerAttributes());
        assertNull(res2WTransformerB.getAttributes().getPhaseTapChangerAttributes().getSteps());

        networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, twoWTransformers, mapA);
        assertNotNull(res2WTransformerA.getAttributes().getRatioTapChangerAttributes().getSteps());
        assertNull(res2WTransformerA.getAttributes().getPhaseTapChangerAttributes());
        assertEquals(3, res2WTransformerA.getAttributes().getRatioTapChangerAttributes().getSteps().size());

        networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, twoWTransformers, mapB);
        assertNotNull(res2WTransformerB.getAttributes().getPhaseTapChangerAttributes().getSteps());
        assertNull(res2WTransformerB.getAttributes().getRatioTapChangerAttributes());
        assertEquals(4, res2WTransformerB.getAttributes().getPhaseTapChangerAttributes().getSteps().size());

    }

    public int getTapChangerStepsNumber(UUID networkUuid, int variantNum, ResourceType type, List<OwnerInfo> infos) {
        return infos.stream()
                .map(x -> {
                    Map<OwnerInfo, List<TapChangerStepAttributes>> steps = networkStoreRepository.getTapChangerSteps(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "equipmentType", type.toString());
                    if (steps.get(x) != null) {
                        return steps.get(x).size();
                    }
                    return 0;
                })
                .reduce(0, Integer::sum);
    }

    @Test
    public void insertTapChangerStepsInThreeWindingsTranformerTest() {

        String equipmentIdA = "id3WTransformerA";
        String equipmentIdB = "id3WTransformerB";

        OwnerInfo info3WTransformerA = new OwnerInfo(
                equipmentIdA,
                ResourceType.THREE_WINDINGS_TRANSFORMER,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo info3WTransformerB = new OwnerInfo(
                equipmentIdB,
                ResourceType.THREE_WINDINGS_TRANSFORMER,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );
        OwnerInfo info3WTransformerX = new OwnerInfo(
                "badID",
                ResourceType.TWO_WINDINGS_TRANSFORMER,
                NETWORK_UUID,
                Resource.INITIAL_VARIANT_NUM
        );

        Resource<ThreeWindingsTransformerAttributes> res3WTransformerA = Resource.threeWindingsTransformerBuilder()
                .id(equipmentIdA)
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name("id3WTransformerA")
                        .ratedU0(1)
                        .branchStatus("IN_OPERATION")
                        .leg1(LegAttributes.builder()
                                .ratioTapChangerAttributes(RatioTapChangerAttributes.builder()
                                .lowTapPosition(20)
                                .build())
                        .build()
                        )
                        .leg2(new LegAttributes())
                        .leg3(new LegAttributes())
                        .build())
                .build();

        Resource<ThreeWindingsTransformerAttributes> res3WTransformerB = Resource.threeWindingsTransformerBuilder()
                .id(equipmentIdB)
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name("id3WTransformerB")
                        .ratedU0(1)
                        .branchStatus("IN_OPERATION")
                        .leg1(new LegAttributes())
                        .leg2(LegAttributes.builder()
                                .phaseTapChangerAttributes(PhaseTapChangerAttributes.builder()
                                .lowTapPosition(20)
                                .build())
                        .build()
                        )
                        .leg3(new LegAttributes())
                        .build())
                .build();

        assertEquals(res3WTransformerA.getId(), info3WTransformerA.getEquipmentId());
        assertEquals(res3WTransformerB.getId(), info3WTransformerB.getEquipmentId());
        assertNotEquals(res3WTransformerA.getId(), info3WTransformerX.getEquipmentId());

        TapChangerStepAttributes ratioStepAS11 = TapChangerStepAttributes.builder()
                .rho(1.)
                .r(1.)
                .g(1.)
                .b(1.)
                .x(1.)
                .side(1)
                .index(0)
                .type(TapChangerType.RATIO)
                .build();

        TapChangerStepAttributes ratioStepAS12 = TapChangerStepAttributes.builder()
                .rho(2.)
                .r(2.)
                .g(2.)
                .b(2.)
                .x(2.)
                .side(1)
                .index(1)
                .type(TapChangerType.RATIO)
                .build();

        TapChangerStepAttributes ratioStepAS21 = TapChangerStepAttributes.builder()
                .rho(1.)
                .r(1.)
                .g(1.)
                .b(1.)
                .x(1.)
                .side(2)
                .index(0)
                .type(TapChangerType.RATIO)
                .build();

        TapChangerStepAttributes ratioStepAS22 = TapChangerStepAttributes.builder()
                .rho(2.)
                .r(2.)
                .g(2.)
                .b(2.)
                .x(2.)
                .side(2)
                .index(1)
                .type(TapChangerType.RATIO)
                .build();

        TapChangerStepAttributes ratioStepABad = TapChangerStepAttributes.builder()
                .rho(3.)
                .r(3.)
                .g(3.)
                .b(3.)
                .x(3.)
                .side(4) // this side doesn't exists
                .index(2)
                .type(TapChangerType.RATIO)
                .build();

        TapChangerStepAttributes phaseStepBS21 = TapChangerStepAttributes.builder()
                .rho(10.)
                .r(10.)
                .g(10.)
                .b(10.)
                .x(10.)
                .alpha(10.)
                .side(2)
                .index(0)
                .type(TapChangerType.PHASE)
                .build();

        TapChangerStepAttributes phaseStepBS22 = TapChangerStepAttributes.builder()
                .rho(20.)
                .r(20.)
                .g(20.)
                .b(20.)
                .x(20.)
                .alpha(20.)
                .side(2)
                .index(1)
                .type(TapChangerType.PHASE)
                .build();

        TapChangerStepAttributes phaseStepBS31 = TapChangerStepAttributes.builder()
                .rho(30.)
                .r(30.)
                .g(30.)
                .b(30.)
                .x(30.)
                .alpha(30.)
                .side(3)
                .index(0)
                .type(TapChangerType.PHASE)
                .build();

        TapChangerStepAttributes phaseStepBBad = TapChangerStepAttributes.builder()
                .rho(40.)
                .r(40.)
                .g(40.)
                .b(40.)
                .x(40.)
                .alpha(40.)
                .side(4) // this side doesn't exists
                .index(0)
                .type(TapChangerType.PHASE)
                .build();

        List<Resource<ThreeWindingsTransformerAttributes>> threeWTransformers = new ArrayList<>();
        threeWTransformers.add(res3WTransformerA);
        threeWTransformers.add(res3WTransformerB);

        List<TapChangerStepAttributes> tapChangerStepsA = new ArrayList<>();
        tapChangerStepsA.add(ratioStepAS11);
        tapChangerStepsA.add(ratioStepAS12);
        tapChangerStepsA.add(ratioStepAS21);
        tapChangerStepsA.add(ratioStepAS22);
        tapChangerStepsA.add(ratioStepABad);

        List<TapChangerStepAttributes> tapChangerStepsB = new ArrayList<>();
        tapChangerStepsB.add(phaseStepBS21);
        tapChangerStepsB.add(phaseStepBS22);
        tapChangerStepsB.add(phaseStepBS31);
        tapChangerStepsB.add(phaseStepBBad);

        Map<OwnerInfo, List<TapChangerStepAttributes>> mapA = new HashMap<>();
        Map<OwnerInfo, List<TapChangerStepAttributes>> mapB = new HashMap<>();

        mapA.put(info3WTransformerA, tapChangerStepsA);
        mapB.put(info3WTransformerB, tapChangerStepsB);

        assertNull(res3WTransformerA.getAttributes().getLeg(1).getRatioTapChangerAttributes().getSteps());
        assertNull(res3WTransformerA.getAttributes().getLeg(1).getPhaseTapChangerAttributes());
        assertNull(res3WTransformerA.getAttributes().getLeg(2).getRatioTapChangerAttributes());
        assertNull(res3WTransformerA.getAttributes().getLeg(2).getPhaseTapChangerAttributes());
        assertNull(res3WTransformerA.getAttributes().getLeg(3).getRatioTapChangerAttributes());
        assertNull(res3WTransformerA.getAttributes().getLeg(3).getPhaseTapChangerAttributes());

        assertNull(res3WTransformerB.getAttributes().getLeg(1).getRatioTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(1).getPhaseTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(2).getRatioTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(2).getPhaseTapChangerAttributes().getSteps());
        assertNull(res3WTransformerB.getAttributes().getLeg(3).getRatioTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(3).getPhaseTapChangerAttributes());

        networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, threeWTransformers, new HashMap<>());

        assertNull(res3WTransformerA.getAttributes().getLeg(1).getRatioTapChangerAttributes().getSteps());
        assertNull(res3WTransformerA.getAttributes().getLeg(1).getPhaseTapChangerAttributes());
        assertNull(res3WTransformerA.getAttributes().getLeg(2).getRatioTapChangerAttributes());
        assertNull(res3WTransformerA.getAttributes().getLeg(2).getPhaseTapChangerAttributes());
        assertNull(res3WTransformerA.getAttributes().getLeg(3).getRatioTapChangerAttributes());
        assertNull(res3WTransformerA.getAttributes().getLeg(3).getPhaseTapChangerAttributes());

        assertNull(res3WTransformerB.getAttributes().getLeg(1).getRatioTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(1).getPhaseTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(2).getRatioTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(2).getPhaseTapChangerAttributes().getSteps());
        assertNull(res3WTransformerB.getAttributes().getLeg(3).getRatioTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(3).getPhaseTapChangerAttributes());

        // in A
        assertThrows(IllegalArgumentException.class, () -> {
            networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, threeWTransformers, mapA);
        });
        assertNotNull(res3WTransformerA.getAttributes().getLeg(1).getRatioTapChangerAttributes().getSteps());
        assertNull(res3WTransformerA.getAttributes().getLeg(1).getPhaseTapChangerAttributes());
        assertEquals(2, res3WTransformerA.getAttributes().getLeg(1).getRatioTapChangerAttributes().getSteps().size());

        assertNotNull(res3WTransformerA.getAttributes().getLeg(2).getRatioTapChangerAttributes().getSteps());
        assertNull(res3WTransformerA.getAttributes().getLeg(2).getPhaseTapChangerAttributes());
        assertEquals(2, res3WTransformerA.getAttributes().getLeg(2).getRatioTapChangerAttributes().getSteps().size());

        // in B
        assertThrows(IllegalArgumentException.class, () -> {
            networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, threeWTransformers, mapB);
        });
        assertNull(res3WTransformerB.getAttributes().getLeg(1).getPhaseTapChangerAttributes());
        assertNull(res3WTransformerB.getAttributes().getLeg(1).getRatioTapChangerAttributes());

        assertNotNull(res3WTransformerB.getAttributes().getLeg(2).getPhaseTapChangerAttributes().getSteps());
        assertNull(res3WTransformerB.getAttributes().getLeg(2).getRatioTapChangerAttributes());
        assertEquals(2, res3WTransformerB.getAttributes().getLeg(2).getPhaseTapChangerAttributes().getSteps().size());

        assertNotNull(res3WTransformerB.getAttributes().getLeg(3).getPhaseTapChangerAttributes().getSteps());
        assertNull(res3WTransformerB.getAttributes().getLeg(3).getRatioTapChangerAttributes());
        assertEquals(1, res3WTransformerB.getAttributes().getLeg(3).getPhaseTapChangerAttributes().getSteps().size());
    }
}
