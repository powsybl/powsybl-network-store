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

        Resource<TemporaryLimitAttributes> resTemplimitAOkSide1a = Resource.temporaryLimitBuilder()
                .id("idTemplimit1")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentIdA)
                        .equipmentType(LINE)
                        .side(1)
                        .acceptableDuration(100)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitAOkSide2a = Resource.temporaryLimitBuilder()
                .id("idTemplimit2")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentIdA)
                        .equipmentType(LINE)
                        .side(2)
                        .acceptableDuration(100)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitAOkSide2b = Resource.temporaryLimitBuilder()
                .id("idTemplimit3")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentIdA)
                        .equipmentType(LINE)
                        .side(2)
                        .acceptableDuration(200)
                        .build()
                ).build();

        // If there are multiple instance of a limit on the same side with the same acceptable duration, only one is kept.
        Resource<TemporaryLimitAttributes> resTemplimitAOkSide2bSameAcceptableDuration = Resource.temporaryLimitBuilder()
                .id("idTemplimit4")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentIdA)
                        .equipmentType(LINE)
                        .side(2)
                        .acceptableDuration(200)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitWrongEquipmentId = Resource.temporaryLimitBuilder()
                .id("idTemplimit5")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId("not" + equipmentIdA)
                        .equipmentType(LINE)
                        .side(1)
                        .acceptableDuration(100)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitBOkSide1a = Resource.temporaryLimitBuilder()
                .id("idTemplimit6")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentIdB)
                        .equipmentType(LINE)
                        .side(1)
                        .acceptableDuration(100)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitBOkSide1b = Resource.temporaryLimitBuilder()
                .id("idTemplimit7")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentIdB)
                        .equipmentType(LINE)
                        .side(1)
                        .acceptableDuration(200)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitBOkSide1c = Resource.temporaryLimitBuilder()
                .id("idTemplimit8")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentIdB)
                        .equipmentType(LINE)
                        .side(1)
                        .acceptableDuration(300)
                        .build()
                ).build();

        assertEquals(resLineA.getId(), resTemplimitAOkSide1a.getAttributes().getEquipmentId());
        assertEquals(resLineA.getId(), resTemplimitAOkSide2a.getAttributes().getEquipmentId());
        assertEquals(resLineA.getId(), resTemplimitAOkSide2b.getAttributes().getEquipmentId());
        assertEquals(resLineA.getId(), resTemplimitAOkSide2bSameAcceptableDuration.getAttributes().getEquipmentId());
        assertNotEquals(resLineA.getId(), resTemplimitWrongEquipmentId.getAttributes().getEquipmentId());
        assertEquals(resLineB.getId(), resTemplimitBOkSide1a.getAttributes().getEquipmentId());
        assertEquals(resLineB.getId(), resTemplimitBOkSide1b.getAttributes().getEquipmentId());
        assertEquals(resLineB.getId(), resTemplimitBOkSide1c.getAttributes().getEquipmentId());

        List<Resource<LineAttributes>> lines = new ArrayList<>();
        lines.add(resLineA);
        lines.add(resLineB);

        List<Resource<TemporaryLimitAttributes>> temporaryLimits = new ArrayList<>();
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
