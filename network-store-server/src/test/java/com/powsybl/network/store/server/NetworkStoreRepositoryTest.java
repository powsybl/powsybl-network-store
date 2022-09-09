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

        TemporaryCurrentLimitAttributes templimitAOkSide1a = new TemporaryCurrentLimitAttributes();
        templimitAOkSide1a.setEquipmentId(equipmentIdA);
        templimitAOkSide1a.setEquipmentType(LINE);
        templimitAOkSide1a.setSide(1);
        templimitAOkSide1a.setAcceptableDuration(100);
        templimitAOkSide1a.setLimitType(TemporaryLimitType.CURRENT_LIMIT);

        TemporaryCurrentLimitAttributes templimitAOkSide2a = new TemporaryCurrentLimitAttributes();
        templimitAOkSide2a.setEquipmentId(equipmentIdA);
        templimitAOkSide2a.setEquipmentType(LINE);
        templimitAOkSide2a.setSide(2);
        templimitAOkSide2a.setAcceptableDuration(100);
        templimitAOkSide2a.setLimitType(TemporaryLimitType.CURRENT_LIMIT);

        TemporaryCurrentLimitAttributes templimitAOkSide2b = new TemporaryCurrentLimitAttributes();
        templimitAOkSide2b.setEquipmentId(equipmentIdA);
        templimitAOkSide2b.setEquipmentType(LINE);
        templimitAOkSide2b.setSide(2);
        templimitAOkSide2b.setAcceptableDuration(200);
        templimitAOkSide2b.setLimitType(TemporaryLimitType.CURRENT_LIMIT);

        // If there are multiple instance of a limit on the same side with the same acceptable duration, only one is kept.
        TemporaryCurrentLimitAttributes templimitAOkSide2bSameAcceptableDuration = new TemporaryCurrentLimitAttributes();
        templimitAOkSide2bSameAcceptableDuration.setEquipmentId(equipmentIdA);
        templimitAOkSide2bSameAcceptableDuration.setEquipmentType(LINE);
        templimitAOkSide2bSameAcceptableDuration.setSide(2);
        templimitAOkSide2bSameAcceptableDuration.setAcceptableDuration(200);
        templimitAOkSide2bSameAcceptableDuration.setLimitType(TemporaryLimitType.CURRENT_LIMIT);

        TemporaryCurrentLimitAttributes templimitWrongEquipmentId = new TemporaryCurrentLimitAttributes();
        templimitWrongEquipmentId.setEquipmentId("not" + equipmentIdA);
        templimitWrongEquipmentId.setEquipmentType(LINE);
        templimitWrongEquipmentId.setSide(1);
        templimitWrongEquipmentId.setAcceptableDuration(100);
        templimitWrongEquipmentId.setLimitType(TemporaryLimitType.CURRENT_LIMIT);

        TemporaryCurrentLimitAttributes templimitBOkSide1a = new TemporaryCurrentLimitAttributes();
        templimitBOkSide1a.setEquipmentId(equipmentIdB);
        templimitBOkSide1a.setEquipmentType(LINE);
        templimitBOkSide1a.setSide(1);
        templimitBOkSide1a.setAcceptableDuration(100);
        templimitBOkSide1a.setLimitType(TemporaryLimitType.CURRENT_LIMIT);

        TemporaryCurrentLimitAttributes templimitBOkSide1b = new TemporaryCurrentLimitAttributes();
        templimitBOkSide1b.setEquipmentId(equipmentIdB);
        templimitBOkSide1b.setEquipmentType(LINE);
        templimitBOkSide1b.setSide(1);
        templimitBOkSide1b.setAcceptableDuration(200);
        templimitBOkSide1b.setLimitType(TemporaryLimitType.CURRENT_LIMIT);

        TemporaryCurrentLimitAttributes templimitBOkSide1c = new TemporaryCurrentLimitAttributes();
        templimitBOkSide1c.setEquipmentId(equipmentIdB);
        templimitBOkSide1c.setEquipmentType(LINE);
        templimitBOkSide1c.setSide(1);
        templimitBOkSide1c.setAcceptableDuration(300);
        templimitBOkSide1c.setLimitType(TemporaryLimitType.CURRENT_LIMIT);

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

        networkStoreRepository.insertTemporaryLimitsInLines(lines, new ArrayList<>());

        assertNull(resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineA.getAttributes().getCurrentLimits2());
        assertNull(resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineB.getAttributes().getCurrentLimits2());

        networkStoreRepository.insertTemporaryLimitsInLines(lines, temporaryLimits);

        assertNotNull(resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNotNull(resLineA.getAttributes().getCurrentLimits2().getTemporaryLimits());
        assertNotNull(resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits());
        assertNull(resLineB.getAttributes().getCurrentLimits2());
        assertEquals(1, resLineA.getAttributes().getCurrentLimits1().getTemporaryLimits().size());
        assertEquals(2, resLineA.getAttributes().getCurrentLimits2().getTemporaryLimits().size());
        assertEquals(3, resLineB.getAttributes().getCurrentLimits1().getTemporaryLimits().size());
    }

}
