/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.network.store.model.*;
import com.powsybl.network.store.model.utils.Views;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class JsonViewTest {
    @Test
    void testBasicViewWithLines() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        LineAttributes lineAttributes = LineAttributes.builder()
                .name("line1")
                .selectedOperationalLimitsGroupId1("group1")
                .p1(1)
                .p2(2)
                .q2(4)
                .r(5)
                .x(6)
                .operationalLimitsGroups1(Map.of("group1", new OperationalLimitsGroupAttributes()))
                .build();
        String basicResultExpected = "{\"name\":\"line1\",\"fictitious\":false,\"extensionAttributes\":{},\"r\":5.0,\"x\":6.0," +
                "\"g1\":0.0,\"b1\":0.0,\"g2\":0.0,\"b2\":0.0,\"p1\":1.0,\"q1\":\"NaN\",\"p2\":2.0,\"q2\":4.0," +
                "\"selectedOperationalLimitsGroupId1\":\"group1\",\"regulatingEquipments\":[]}";
        String basicResult = mapper
                .writerWithView(Views.Basic.class)
                .writeValueAsString(lineAttributes);
        assertEquals(basicResultExpected, basicResult);

        String svResult = mapper
                .writerWithView(Views.SvView.class)
                .writeValueAsString(lineAttributes);
        assertEquals("{\"p1\":1.0,\"q1\":\"NaN\",\"p2\":2.0,\"q2\":4.0}", svResult);

        String expectedWithLimitsResult = "{\"name\":\"line1\",\"fictitious\":false,\"extensionAttributes\":{}," +
                "\"r\":5.0,\"x\":6.0,\"g1\":0.0,\"b1\":0.0,\"g2\":0.0,\"b2\":0.0,\"p1\":1.0," +
                "\"q1\":\"NaN\",\"p2\":2.0,\"q2\":4.0,\"operationalLimitsGroups1\":{\"group1\":{}}," +
                "\"selectedOperationalLimitsGroupId1\":\"group1\",\"operationalLimitsGroups2\":{},\"regulatingEquipments\":[]}";
        String withLimitsResult = mapper
                .writerWithView(Views.WithLimits.class)
                .writeValueAsString(lineAttributes);
        assertEquals(expectedWithLimitsResult, withLimitsResult);
    }

    @Test
    void testBasicViewWithTwt() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ActivePowerControlAttributes activePowerControlAttributes = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .minTargetP(0.0)
                .maxTargetP(10.0)
                .build();
        TwoWindingsTransformerAttributes twoWindingsTransformerAttributes = TwoWindingsTransformerAttributes.builder()
                .name("twt1")
                .p1(1)
                .p2(2)
                .q1(3)
                .q2(4)
                .r(5)
                .x(6)
                .selectedOperationalLimitsGroupId1("selectedGroupId1")
                .operationalLimitsGroups1(Map.of("group1", new OperationalLimitsGroupAttributes()))
                .regulatingEquipments(Set.of(new RegulatingEquipmentIdentifier("loadId", ResourceType.LOAD)))
                .properties(Map.of("key", "value"))
                .aliasByType(Map.of("typ1", "alias1"))
                .aliasesWithoutType(Set.of("alias2"))
                .extensionAttributes(Map.of("activePowerControl", activePowerControlAttributes))
                .build();
        String basicResultExpected = "{\"name\":\"twt1\",\"fictitious\":false,\"properties\":{\"key\":\"value\"}," +
                "\"aliasesWithoutType\":[\"alias2\"],\"aliasByType\":{\"typ1\":\"alias1\"}," +
                "\"extensionAttributes\":{\"activePowerControl\":{\"extensionName\":\"activePowerControl\",\"participate\":true," +
                "\"droop\":5.2,\"participationFactor\":0.5,\"minTargetP\":0.0,\"maxTargetP\":10.0}}," +
                "\"r\":5.0,\"x\":6.0,\"g\":0.0,\"b\":0.0,\"ratedU1\":0.0,\"ratedU2\":0.0,\"ratedS\":0.0,\"p1\":1.0," +
                "\"q1\":3.0,\"p2\":2.0,\"q2\":4.0,\"selectedOperationalLimitsGroupId1\":\"selectedGroupId1\"," +
                "\"regulatingEquipments\":[{\"equipmentId\":\"loadId\",\"resourceType\":\"LOAD\",\"regulatingTapChangerType\":\"NONE\"}]}";
        String basicResult = mapper
                .writerWithView(Views.Basic.class)
                .writeValueAsString(twoWindingsTransformerAttributes);
        assertEquals(basicResultExpected, basicResult);

        String svResult = mapper
                .writerWithView(Views.SvView.class)
                .writeValueAsString(twoWindingsTransformerAttributes);
        assertEquals("{\"p1\":1.0,\"q1\":3.0,\"p2\":2.0,\"q2\":4.0}", svResult);

        String expectedWithLimitsResult = "{\"name\":\"twt1\",\"fictitious\":false,\"properties\":{\"key\":\"value\"}," +
                "\"aliasesWithoutType\":[\"alias2\"],\"aliasByType\":{\"typ1\":\"alias1\"}," +
                "\"extensionAttributes\":{\"activePowerControl\":{\"extensionName\":\"activePowerControl\"," +
                "\"participate\":true,\"droop\":5.2,\"participationFactor\":0.5,\"minTargetP\":0.0,\"maxTargetP\":10.0}}," +
                "\"r\":5.0,\"x\":6.0,\"g\":0.0,\"b\":0.0,\"ratedU1\":0.0,\"ratedU2\":0.0,\"ratedS\":0.0,\"p1\":1.0,\"q1\":3.0," +
                "\"p2\":2.0,\"q2\":4.0,\"operationalLimitsGroups1\":{\"group1\":{}}," +
                "\"selectedOperationalLimitsGroupId1\":\"selectedGroupId1\",\"operationalLimitsGroups2\":{}," +
                "\"regulatingEquipments\":[{\"equipmentId\":\"loadId\",\"resourceType\":\"LOAD\",\"regulatingTapChangerType\":\"NONE\"}]}";
        String withLimitsResult = mapper
                .writerWithView(Views.WithLimits.class)
                .writeValueAsString(twoWindingsTransformerAttributes);
        assertEquals(expectedWithLimitsResult, withLimitsResult);
    }

    @Test
    void testAttributeFilter() {
        TwoWindingsTransformerAttributes twoWindingsTransformerAttributes = TwoWindingsTransformerAttributes.builder()
                .name("twt1")
                .p1(1)
                .p2(2)
                .q1(3)
                .q2(4)
                .r(5)
                .x(6)
                .selectedOperationalLimitsGroupId1("selectedGroupId1")
                .operationalLimitsGroups1(Map.of("group1", new OperationalLimitsGroupAttributes()))
                .build();

        Attributes svFilter = twoWindingsTransformerAttributes.filter(AttributeFilter.SV);
        assertEquals(new BranchSvAttributes(1, 3, 2, 4), svFilter);

        Attributes basicFilter = twoWindingsTransformerAttributes.filter(AttributeFilter.BASIC);
        assertEquals(twoWindingsTransformerAttributes, basicFilter);

        Attributes withLimitsFilter = twoWindingsTransformerAttributes.filter(AttributeFilter.WITH_LIMITS);
        assertEquals(twoWindingsTransformerAttributes, withLimitsFilter);
    }
}
