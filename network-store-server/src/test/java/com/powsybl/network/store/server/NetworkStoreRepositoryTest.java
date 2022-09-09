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

import static com.powsybl.network.store.server.QueryCatalog.LINE;
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

        TemporaryCurrentLimitAttributes templimitAOkSide1a = TemporaryCurrentLimitAttributes.builder()
                .equipmentId(equipmentIdA)
                .equipmentType(LINE)
                .side(1)
                .acceptableDuration(100)
                .limitType(TemporaryLimitType.CURRENT_LIMIT)
                .build();

        TemporaryCurrentLimitAttributes templimitAOkSide2a = TemporaryCurrentLimitAttributes.builder()
                .equipmentId(equipmentIdA)
                .equipmentType(LINE)
                .side(2)
                .acceptableDuration(100)
                .limitType(TemporaryLimitType.CURRENT_LIMIT)
                .build();

        TemporaryCurrentLimitAttributes templimitAOkSide2b = TemporaryCurrentLimitAttributes.builder()
                .equipmentId(equipmentIdA)
                .equipmentType(LINE)
                .side(2)
                .acceptableDuration(200)
                .limitType(TemporaryLimitType.CURRENT_LIMIT)
                .build();

        // If there are multiple instance of a limit on the same side with the same acceptable duration, only one is kept.
        TemporaryCurrentLimitAttributes templimitAOkSide2bSameAcceptableDuration = TemporaryCurrentLimitAttributes.builder()
                .equipmentId(equipmentIdA)
                .equipmentType(LINE)
                .side(2)
                .acceptableDuration(200)
                .limitType(TemporaryLimitType.CURRENT_LIMIT)
                .build();

        TemporaryCurrentLimitAttributes templimitWrongEquipmentId = TemporaryCurrentLimitAttributes.builder()
                .equipmentId("not" + equipmentIdA)
                .equipmentType(LINE)
                .side(1)
                .acceptableDuration(100)
                .limitType(TemporaryLimitType.CURRENT_LIMIT)
                .build();

        TemporaryCurrentLimitAttributes templimitBOkSide1a = TemporaryCurrentLimitAttributes.builder()
                .equipmentId(equipmentIdB)
                .equipmentType(LINE)
                .side(1)
                .acceptableDuration(100)
                .limitType(TemporaryLimitType.CURRENT_LIMIT)
                .build();

        TemporaryCurrentLimitAttributes templimitBOkSide1b = TemporaryCurrentLimitAttributes.builder()
                .equipmentId(equipmentIdB)
                .equipmentType(LINE)
                .side(1)
                .acceptableDuration(200)
                .limitType(TemporaryLimitType.CURRENT_LIMIT)
                .build();

        TemporaryCurrentLimitAttributes templimitBOkSide1c = TemporaryCurrentLimitAttributes.builder()
                .equipmentId(equipmentIdB)
                .equipmentType(LINE)
                .side(1)
                .acceptableDuration(300)
                .limitType(TemporaryLimitType.CURRENT_LIMIT)
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

        List<TemporaryCurrentLimitAttributes> temporaryLimits = new ArrayList<>();
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
