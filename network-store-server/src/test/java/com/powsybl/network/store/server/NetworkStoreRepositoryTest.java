package com.powsybl.network.store.server;

import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TemporaryLimitAttributes;
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

        TemporaryLimitAttributes resTemplimitAOkSide1a = new TemporaryLimitAttributes();
        resTemplimitAOkSide1a.setEquipmentId(equipmentIdA);
        resTemplimitAOkSide1a.setEquipmentType(LINE);
        resTemplimitAOkSide1a.setSide(1);
        resTemplimitAOkSide1a.setAcceptableDuration(100);

        TemporaryLimitAttributes resTemplimitAOkSide2a = new TemporaryLimitAttributes();
        resTemplimitAOkSide2a.setEquipmentId(equipmentIdA);
        resTemplimitAOkSide2a.setEquipmentType(LINE);
        resTemplimitAOkSide2a.setSide(2);
        resTemplimitAOkSide2a.setAcceptableDuration(100);

        TemporaryLimitAttributes resTemplimitAOkSide2b = new TemporaryLimitAttributes();
        resTemplimitAOkSide2b.setEquipmentId(equipmentIdA);
        resTemplimitAOkSide2b.setEquipmentType(LINE);
        resTemplimitAOkSide2b.setSide(2);
        resTemplimitAOkSide2b.setAcceptableDuration(200);

        // If there are multiple instance of a limit on the same side with the same acceptable duration, only one is kept.
        TemporaryLimitAttributes resTemplimitAOkSide2bSameAcceptableDuration = new TemporaryLimitAttributes();
        resTemplimitAOkSide2bSameAcceptableDuration.setEquipmentId(equipmentIdA);
        resTemplimitAOkSide2bSameAcceptableDuration.setEquipmentType(LINE);
        resTemplimitAOkSide2bSameAcceptableDuration.setSide(2);
        resTemplimitAOkSide2bSameAcceptableDuration.setAcceptableDuration(200);

        TemporaryLimitAttributes resTemplimitWrongEquipmentId = new TemporaryLimitAttributes();
        resTemplimitWrongEquipmentId.setEquipmentId("not" + equipmentIdA);
        resTemplimitWrongEquipmentId.setEquipmentType(LINE);
        resTemplimitWrongEquipmentId.setSide(1);
        resTemplimitWrongEquipmentId.setAcceptableDuration(100);

        TemporaryLimitAttributes resTemplimitBOkSide1a = new TemporaryLimitAttributes();
        resTemplimitBOkSide1a.setEquipmentId(equipmentIdB);
        resTemplimitBOkSide1a.setEquipmentType(LINE);
        resTemplimitBOkSide1a.setSide(1);
        resTemplimitBOkSide1a.setAcceptableDuration(100);

        TemporaryLimitAttributes resTemplimitBOkSide1b = new TemporaryLimitAttributes();
        resTemplimitBOkSide1b.setEquipmentId(equipmentIdB);
        resTemplimitBOkSide1b.setEquipmentType(LINE);
        resTemplimitBOkSide1b.setSide(1);
        resTemplimitBOkSide1b.setAcceptableDuration(200);

        TemporaryLimitAttributes resTemplimitBOkSide1c = new TemporaryLimitAttributes();
        resTemplimitBOkSide1c.setEquipmentId(equipmentIdB);
        resTemplimitBOkSide1c.setEquipmentType(LINE);
        resTemplimitBOkSide1c.setSide(1);
        resTemplimitBOkSide1c.setAcceptableDuration(300);

        assertEquals(resLineA.getId(), resTemplimitAOkSide1a.getEquipmentId());
        assertEquals(resLineA.getId(), resTemplimitAOkSide2a.getEquipmentId());
        assertEquals(resLineA.getId(), resTemplimitAOkSide2b.getEquipmentId());
        assertEquals(resLineA.getId(), resTemplimitAOkSide2bSameAcceptableDuration.getEquipmentId());
        assertNotEquals(resLineA.getId(), resTemplimitWrongEquipmentId.getEquipmentId());
        assertEquals(resLineB.getId(), resTemplimitBOkSide1a.getEquipmentId());
        assertEquals(resLineB.getId(), resTemplimitBOkSide1b.getEquipmentId());
        assertEquals(resLineB.getId(), resTemplimitBOkSide1c.getEquipmentId());

        List<Resource<LineAttributes>> lines = new ArrayList<>();
        lines.add(resLineA);
        lines.add(resLineB);

        List<TemporaryLimitAttributes> temporaryLimits = new ArrayList<>();
        temporaryLimits.add(resTemplimitAOkSide1a);
        temporaryLimits.add(resTemplimitBOkSide1a);
        temporaryLimits.add(resTemplimitAOkSide2a);
        temporaryLimits.add(resTemplimitAOkSide2b);
        temporaryLimits.add(resTemplimitAOkSide2bSameAcceptableDuration);
        temporaryLimits.add(resTemplimitBOkSide1b);
        temporaryLimits.add(resTemplimitWrongEquipmentId);
        temporaryLimits.add(resTemplimitBOkSide1c);

        assertNull(resLineA.getAttributes().getTemporaryLimits1());
        assertNull(resLineA.getAttributes().getTemporaryLimits2());
        assertNull(resLineB.getAttributes().getTemporaryLimits1());
        assertNull(resLineB.getAttributes().getTemporaryLimits2());

        networkStoreRepository.insertTemporaryLimitsInLines(lines, new ArrayList<>());

        assertNull(resLineA.getAttributes().getTemporaryLimits1());
        assertNull(resLineA.getAttributes().getTemporaryLimits2());
        assertNull(resLineB.getAttributes().getTemporaryLimits1());
        assertNull(resLineB.getAttributes().getTemporaryLimits2());

        networkStoreRepository.insertTemporaryLimitsInLines(lines, temporaryLimits);

        assertNotNull(resLineA.getAttributes().getTemporaryLimits1());
        assertNotNull(resLineA.getAttributes().getTemporaryLimits2());
        assertNotNull(resLineB.getAttributes().getTemporaryLimits1());
        assertNull(resLineB.getAttributes().getTemporaryLimits2());
        assertEquals(1, resLineA.getAttributes().getTemporaryLimits1().size());
        assertEquals(2, resLineA.getAttributes().getTemporaryLimits2().size());
        assertEquals(3, resLineB.getAttributes().getTemporaryLimits1().size());
    }

}
