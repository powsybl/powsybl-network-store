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

        String equipmentId = "idLine";

        Resource<LineAttributes> resLine = Resource.lineBuilder()
                .id(equipmentId)
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("idLine")
                        .currentLimits1(LimitsAttributes.builder().permanentLimit(20.).build())
                        .build())
                .build();

        Resource<TemporaryLimitAttributes> resTemplimitSide1a = Resource.temporaryLimitBuilder()
                .id("idTemplimit1")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentId)
                        .equipmentType(LINE)
                        .side(1)
                        .acceptableDuration(100)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitOkSide2a = Resource.temporaryLimitBuilder()
                .id("idTemplimit2")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentId)
                        .equipmentType(LINE)
                        .side(2)
                        .acceptableDuration(100)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitOkSide2b = Resource.temporaryLimitBuilder()
                .id("idTemplimit3")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentId)
                        .equipmentType(LINE)
                        .side(2)
                        .acceptableDuration(200)
                        .build()
                ).build();

        // If there are multiple instance of a limit on the same side with the same acceptable duration, only one is kept.
        Resource<TemporaryLimitAttributes> resTemplimitOkSide2bSameAcceptableDuration = Resource.temporaryLimitBuilder()
                .id("idTemplimit4")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId(equipmentId)
                        .equipmentType(LINE)
                        .side(2)
                        .acceptableDuration(200)
                        .build()
                ).build();

        Resource<TemporaryLimitAttributes> resTemplimitWrongEquipmentId = Resource.temporaryLimitBuilder()
                .id("idTemplimit5")
                .attributes(TemporaryLimitAttributes.builder()
                        .equipmentId("not" + equipmentId)
                        .equipmentType(LINE)
                        .side(1)
                        .acceptableDuration(100)
                        .build()
                ).build();

        assertEquals(resLine.getId(), resTemplimitSide1a.getAttributes().getEquipmentId());
        assertEquals(resLine.getId(), resTemplimitOkSide2a.getAttributes().getEquipmentId());
        assertEquals(resLine.getId(), resTemplimitOkSide2b.getAttributes().getEquipmentId());

        List<Resource<LineAttributes>> lines = new ArrayList<>();
        lines.add(resLine);

        List<Resource<TemporaryLimitAttributes>> temporaryLimits = new ArrayList<>();
        temporaryLimits.add(resTemplimitSide1a);
        temporaryLimits.add(resTemplimitOkSide2a);
        temporaryLimits.add(resTemplimitOkSide2b);
        temporaryLimits.add(resTemplimitOkSide2bSameAcceptableDuration);
        temporaryLimits.add(resTemplimitWrongEquipmentId);

        assertNull(resLine.getAttributes().getTemporaryLimits1());
        assertNull(resLine.getAttributes().getTemporaryLimits2());

        networkStoreRepository.insertTemporaryLimitsInLines(lines, new ArrayList<>());

        assertNull(resLine.getAttributes().getTemporaryLimits1());
        assertNull(resLine.getAttributes().getTemporaryLimits2());

        networkStoreRepository.insertTemporaryLimitsInLines(lines, temporaryLimits);

        assertNotNull(resLine.getAttributes().getTemporaryLimits1());
        assertNotNull(resLine.getAttributes().getTemporaryLimits2());
        assertEquals(1, resLine.getAttributes().getTemporaryLimits1().size());
        assertEquals(2, resLine.getAttributes().getTemporaryLimits2().size());
    }

}
