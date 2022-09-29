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

        RatioTapChangerStepAttributes ratioStepA1 = RatioTapChangerStepAttributes.builder()
                .rho(1.)
                .r(1.)
                .g(1.)
                .b(1.)
                .x(1.)
                .side(0)
                .type(TapChangerType.RATIO)
                .build();

        RatioTapChangerStepAttributes ratioStepA2 = RatioTapChangerStepAttributes.builder()
                .rho(2.)
                .r(2.)
                .g(2.)
                .b(2.)
                .x(2.)
                .side(0)
                .type(TapChangerType.RATIO)
                .build();

        RatioTapChangerStepAttributes ratioStepA3 = RatioTapChangerStepAttributes.builder()
                .rho(3.)
                .r(3.)
                .g(3.)
                .b(3.)
                .x(3.)
                .side(1)
                .type(TapChangerType.RATIO)
                .build();

        PhaseTapChangerStepAttributes phaseStepB1 = PhaseTapChangerStepAttributes.builder()
                .rho(10.)
                .r(10.)
                .g(10.)
                .b(10.)
                .x(10.)
                .alpha(10.)
                .side(0)
                .type(TapChangerType.PHASE)
                .build();

        PhaseTapChangerStepAttributes phaseStepB2 = PhaseTapChangerStepAttributes.builder()
                .rho(20.)
                .r(20.)
                .g(20.)
                .b(20.)
                .x(20.)
                .alpha(20.)
                .side(0)
                .type(TapChangerType.PHASE)
                .build();

        PhaseTapChangerStepAttributes phaseStepB3 = PhaseTapChangerStepAttributes.builder()
                .rho(30.)
                .r(30.)
                .g(30.)
                .b(30.)
                .x(30.)
                .alpha(30.)
                .side(0)
                .type(TapChangerType.PHASE)
                .build();

        PhaseTapChangerStepAttributes phaseStepB4 = PhaseTapChangerStepAttributes.builder()
                .rho(40.)
                .r(40.)
                .g(40.)
                .b(40.)
                .x(40.)
                .alpha(40.)
                .side(1)
                .type(TapChangerType.PHASE)
                .build();

        List<Resource<TwoWindingsTransformerAttributes>> twoWTransformers = new ArrayList<>();
        twoWTransformers.add(res2WTransformerA);
        twoWTransformers.add(res2WTransformerB);

        List<AbstractTapChangerStepAttributes> tapChangerStepsA = new ArrayList<>();
        tapChangerStepsA.add(ratioStepA1);
        tapChangerStepsA.add(ratioStepA2);
        tapChangerStepsA.add(ratioStepA3);

        List<AbstractTapChangerStepAttributes> tapChangerStepsB = new ArrayList<>();
        tapChangerStepsB.add(phaseStepB1);
        tapChangerStepsB.add(phaseStepB2);
        tapChangerStepsB.add(phaseStepB3);
        tapChangerStepsB.add(phaseStepB4);

        Map<OwnerInfo, List<AbstractTapChangerStepAttributes>> mapA = new HashMap<>();
        Map<OwnerInfo, List<AbstractTapChangerStepAttributes>> mapB = new HashMap<>();

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
                        .leg1(LegAttributes.builder()
                                .ratioTapChangerAttributes(RatioTapChangerAttributes.builder()
                                .lowTapPosition(20)
                                .build())
                        .build()
                        )
                        .build())
                .build();

        Resource<ThreeWindingsTransformerAttributes> res3WTransformerB = Resource.threeWindingsTransformerBuilder()
                .id(equipmentIdB)
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name("id3WTransformerB")
                        .leg2(LegAttributes.builder()
                                .phaseTapChangerAttributes(PhaseTapChangerAttributes.builder()
                                .lowTapPosition(20)
                                .build())
                        .build()
                        )
                        .build())
                .build();

        assertEquals(res3WTransformerA.getId(), info3WTransformerA.getEquipmentId());
        assertEquals(res3WTransformerB.getId(), info3WTransformerB.getEquipmentId());
        assertNotEquals(res3WTransformerA.getId(), info3WTransformerX.getEquipmentId());

        RatioTapChangerStepAttributes ratioStepA1 = RatioTapChangerStepAttributes.builder()
                .rho(1.)
                .r(1.)
                .g(1.)
                .b(1.)
                .x(1.)
                .side(1)
                .type(TapChangerType.RATIO)
                .build();

        RatioTapChangerStepAttributes ratioStepA2 = RatioTapChangerStepAttributes.builder()
                .rho(2.)
                .r(2.)
                .g(2.)
                .b(2.)
                .x(2.)
                .side(1)
                .type(TapChangerType.RATIO)
                .build();

        RatioTapChangerStepAttributes ratioStepA3Bad = RatioTapChangerStepAttributes.builder()
                .rho(3.)
                .r(3.)
                .g(3.)
                .b(3.)
                .x(3.)
                .side(4)
                .type(TapChangerType.RATIO)
                .build();

        PhaseTapChangerStepAttributes phaseStepB1 = PhaseTapChangerStepAttributes.builder()
                .rho(10.)
                .r(10.)
                .g(10.)
                .b(10.)
                .x(10.)
                .alpha(10.)
                .side(2)
                .type(TapChangerType.PHASE)
                .build();

        PhaseTapChangerStepAttributes phaseStepB2 = PhaseTapChangerStepAttributes.builder()
                .rho(20.)
                .r(20.)
                .g(20.)
                .b(20.)
                .x(20.)
                .alpha(20.)
                .side(2)
                .type(TapChangerType.PHASE)
                .build();

        PhaseTapChangerStepAttributes phaseStepB3 = PhaseTapChangerStepAttributes.builder()
                .rho(30.)
                .r(30.)
                .g(30.)
                .b(30.)
                .x(30.)
                .alpha(30.)
                .side(2)
                .type(TapChangerType.PHASE)
                .build();

        PhaseTapChangerStepAttributes phaseStepB4Bad = PhaseTapChangerStepAttributes.builder()
                .rho(40.)
                .r(40.)
                .g(40.)
                .b(40.)
                .x(40.)
                .alpha(40.)
                .side(4)
                .type(TapChangerType.PHASE)
                .build();

        List<Resource<ThreeWindingsTransformerAttributes>> threeWTransformers = new ArrayList<>();
        threeWTransformers.add(res3WTransformerA);
        threeWTransformers.add(res3WTransformerB);

        List<AbstractTapChangerStepAttributes> tapChangerStepsA = new ArrayList<>();
        tapChangerStepsA.add(ratioStepA1);
        tapChangerStepsA.add(ratioStepA2);
        tapChangerStepsA.add(ratioStepA3Bad);

        List<AbstractTapChangerStepAttributes> tapChangerStepsB = new ArrayList<>();
        tapChangerStepsB.add(phaseStepB1);
        tapChangerStepsB.add(phaseStepB2);
        tapChangerStepsB.add(phaseStepB3);
        tapChangerStepsB.add(phaseStepB4Bad);

        Map<OwnerInfo, List<AbstractTapChangerStepAttributes>> mapA = new HashMap<>();
        Map<OwnerInfo, List<AbstractTapChangerStepAttributes>> mapB = new HashMap<>();

        mapA.put(info3WTransformerA, tapChangerStepsA);
        mapB.put(info3WTransformerB, tapChangerStepsB);

        assertNull(res3WTransformerA.getAttributes().getRatioTapChangerAttributes(1).getSteps());
        assertNull(res3WTransformerA.getAttributes().getPhaseTapChangerAttributes(1));
        assertNull(res3WTransformerB.getAttributes().getRatioTapChangerAttributes(2));
        assertNull(res3WTransformerB.getAttributes().getPhaseTapChangerAttributes(2).getSteps());

        networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, threeWTransformers, new HashMap<>());

        assertNull(res3WTransformerA.getAttributes().getRatioTapChangerAttributes(1).getSteps());
        assertNull(res3WTransformerA.getAttributes().getPhaseTapChangerAttributes(1));
        assertNull(res3WTransformerB.getAttributes().getRatioTapChangerAttributes(2));
        assertNull(res3WTransformerB.getAttributes().getPhaseTapChangerAttributes(2).getSteps());

        assertThrows(IllegalArgumentException.class, () -> {
            networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, threeWTransformers, mapA);
        });
        assertNotNull(res3WTransformerA.getAttributes().getRatioTapChangerAttributes(1).getSteps());
        assertNull(res3WTransformerA.getAttributes().getPhaseTapChangerAttributes(1));
        assertEquals(2, res3WTransformerA.getAttributes().getRatioTapChangerAttributes(1).getSteps().size());

        assertThrows(IllegalArgumentException.class, () -> {
            networkStoreRepository.insertTapChangerStepsInEquipments(NETWORK_UUID, threeWTransformers, mapB);
        });
        assertNotNull(res3WTransformerB.getAttributes().getPhaseTapChangerAttributes(2).getSteps());
        assertNull(res3WTransformerB.getAttributes().getRatioTapChangerAttributes(2));
        assertEquals(3, res3WTransformerB.getAttributes().getPhaseTapChangerAttributes(2).getSteps().size());

    }

}
