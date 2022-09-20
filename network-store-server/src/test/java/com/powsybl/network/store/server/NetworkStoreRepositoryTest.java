/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.network.store.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.network.store.server.Mappings.LINE_TABLE;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NetworkStoreRepositoryTest {

    @Autowired
    protected NetworkStoreRepository networkStoreRepository;

    @Test
    public void insertTemporaryLimitsInLinesTest() {

        String equipmentIdA = "idLineA";
        String equipmentIdB = "idLineB";

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

        LinkableToEquipmentLinker templimitAOkSide1a = LinkableToEquipmentLinker.builder()
                .equipmentId(equipmentIdA)
                .equipmentType(LINE_TABLE)
                .element(TemporaryLimitAttributes.builder()
                        .acceptableDuration(100)
                        .side(1)
                        .limitType(TemporaryLimitType.CURRENT_LIMIT)
                        .build())
                .build();

        LinkableToEquipmentLinker templimitAOkSide2a = LinkableToEquipmentLinker.builder()
                .equipmentId(equipmentIdA)
                .equipmentType(LINE_TABLE)
                .element(TemporaryLimitAttributes.builder()
                        .acceptableDuration(100)
                        .side(2)
                        .limitType(TemporaryLimitType.CURRENT_LIMIT)
                        .build())
                .build();

        LinkableToEquipmentLinker templimitAOkSide2b = LinkableToEquipmentLinker.builder()
                .equipmentId(equipmentIdA)
                .equipmentType(LINE_TABLE)
                .element(TemporaryLimitAttributes.builder()
                        .acceptableDuration(200)
                        .side(2)
                        .limitType(TemporaryLimitType.CURRENT_LIMIT)
                        .build())
                .build();

        // If there are multiple instance of a limit on the same side with the same acceptable duration, only one is kept.
        LinkableToEquipmentLinker templimitAOkSide2bSameAcceptableDuration = LinkableToEquipmentLinker.builder()
                .equipmentId(equipmentIdA)
                .equipmentType(LINE_TABLE)
                .element(TemporaryLimitAttributes.builder()
                        .acceptableDuration(200)
                        .side(2)
                        .limitType(TemporaryLimitType.CURRENT_LIMIT)
                        .build())
                .build();

        LinkableToEquipmentLinker templimitWrongEquipmentId = LinkableToEquipmentLinker.builder()
                .equipmentId("not" + equipmentIdA)
                .equipmentType(LINE_TABLE)
                .element(TemporaryLimitAttributes.builder()
                        .acceptableDuration(100)
                        .side(1)
                        .limitType(TemporaryLimitType.CURRENT_LIMIT)
                        .build())
                .build();

        LinkableToEquipmentLinker templimitBOkSide1a = LinkableToEquipmentLinker.builder()
                .equipmentId(equipmentIdB)
                .equipmentType(LINE_TABLE)
                .element(TemporaryLimitAttributes.builder()
                        .acceptableDuration(100)
                        .side(1)
                        .limitType(TemporaryLimitType.CURRENT_LIMIT)
                        .build())
                .build();

        LinkableToEquipmentLinker templimitBOkSide1b = LinkableToEquipmentLinker.builder()
                .equipmentId(equipmentIdB)
                .equipmentType(LINE_TABLE)
                .element(TemporaryLimitAttributes.builder()
                        .acceptableDuration(200)
                        .side(1)
                        .limitType(TemporaryLimitType.CURRENT_LIMIT)
                        .build())
                .build();

        LinkableToEquipmentLinker templimitBOkSide1c = LinkableToEquipmentLinker.builder()
                .equipmentId(equipmentIdB)
                .equipmentType(LINE_TABLE)
                .element(TemporaryLimitAttributes.builder()
                        .acceptableDuration(300)
                        .side(1)
                        .limitType(TemporaryLimitType.CURRENT_LIMIT)
                        .build())
                .build();

        assertEquals(resLineA.getId(), templimitAOkSide1a.getEquipmentId());
        assertEquals(resLineA.getId(), templimitAOkSide2a.getEquipmentId());
        assertEquals(resLineA.getId(), templimitAOkSide2b.getEquipmentId());
        assertEquals(resLineA.getId(), templimitAOkSide2bSameAcceptableDuration.getEquipmentId());
        assertNotEquals(resLineA.getId(), templimitWrongEquipmentId.getEquipmentId());
        assertEquals(resLineB.getId(), templimitBOkSide1a.getEquipmentId());
        assertEquals(resLineB.getId(), templimitBOkSide1b.getEquipmentId());
        assertEquals(resLineB.getId(), templimitBOkSide1c.getEquipmentId());

        List<Resource<LineAttributes>> lines = new ArrayList<>();
        lines.add(resLineA);
        lines.add(resLineB);

        List<LinkableToEquipmentLinker> temporaryLimits = new ArrayList<>();
        temporaryLimits.add(templimitAOkSide1a);
        temporaryLimits.add(templimitBOkSide1a);
        temporaryLimits.add(templimitAOkSide2a);
        temporaryLimits.add(templimitAOkSide2b);
        temporaryLimits.add(templimitAOkSide2bSameAcceptableDuration);
        temporaryLimits.add(templimitBOkSide1b);
        temporaryLimits.add(templimitWrongEquipmentId);
        temporaryLimits.add(templimitBOkSide1c);

        assertNull(resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineA.getAttributes().getCurrentLimits2());
        assertNull(resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineB.getAttributes().getCurrentLimits2());

        networkStoreRepository.insertTemporaryLimitsInEquipments(lines, new ArrayList<>());

        assertNull(resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineA.getAttributes().getCurrentLimits2());
        assertNull(resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineB.getAttributes().getCurrentLimits2());

        networkStoreRepository.insertTemporaryLimitsInEquipments(lines, temporaryLimits);

        assertNotNull(resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNotNull(resLineA.getAttributes().getCurrentLimits2().getTemporaryLimits());
        assertNotNull(resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineB.getAttributes().getCurrentLimits2());
        assertEquals(1, resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits().size());
        assertEquals(2, resLineA.getAttributes().getCurrentLimits2().getTemporaryLimits().size());
        assertEquals(3, resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits().size());
    }

}
