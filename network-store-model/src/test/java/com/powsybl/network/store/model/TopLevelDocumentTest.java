/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.EnergySource;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.powsybl.network.store.model.ResourceType.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TopLevelDocumentTest {

    @Test
    public void test() throws IOException {
        Resource<SubstationAttributes> resource = Resource.substationBuilder()
                .id("S")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .build())
                .build();
        TopLevelDocument document = TopLevelDocument.of(resource);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"country\":\"FR\"}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(resource, document2.getData().get(0));
    }

    @Test
    public void testMultiResources() throws IOException {
        Resource<SubstationAttributes> resource = Resource.substationBuilder()
                .id("S")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .build())
                .build();
        TopLevelDocument document = TopLevelDocument.of(ImmutableList.of(resource, resource));
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"country\":\"FR\"}},{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"country\":\"FR\"}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(2, document2.getData().size());
        assertEquals(resource, document2.getData().get(0));
        assertEquals(resource, document2.getData().get(1));
    }

    @Test
    public void testEmpty() throws IOException {
        TopLevelDocument document = TopLevelDocument.empty();
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[],\"meta\":{}}";
        assertEquals(jsonRef, json);
    }

    @Test
    public void testMeta() throws IOException {
        Resource<SubstationAttributes> resource = Resource.substationBuilder()
                .id("S")
                .attributes(SubstationAttributes.builder()
                        .country(Country.FR)
                        .build())
                .build();
        TopLevelDocument document = TopLevelDocument.of(resource);
        document.addMeta("test", "test123");
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"country\":\"FR\"}}],\"meta\":{\"test\":\"test123\"}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(resource, document2.getData().get(0));
        assertEquals("test123", document2.getMeta().get("test"));
    }

    @Test
    public void testGenerator() throws IOException {
        TerminalRefAttributes regulatedTerminalAttributes =
            TerminalRefAttributes.builder().side("ONE").connectableId("idEq").build();
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes("gen1", GENERATOR, null,
            new TerminalRefAttributes("gen1", null), regulatedTerminalAttributes, null, GENERATOR, true);
        List<RegulatingEquipmentIdentifier> regEquipments = new ArrayList<>();
        regEquipments.add(new RegulatingEquipmentIdentifier("gen1", GENERATOR));
        regEquipments.add(new RegulatingEquipmentIdentifier("gen2", GENERATOR));
        GeneratorAttributes generatorAttributes = GeneratorAttributes
                .builder()
                .voltageLevelId("vl1")
                .name("name")
                .bus("bus1")
                .energySource(EnergySource.HYDRO)
                .maxP(1)
                .minP(2)
                .fictitious(false)
                .node(1)
                .targetP(3)
                .targetV(4)
                .regulatingPoint(regulatingPointAttributes)
                .regulatingEquipments(regEquipments)
                .build();

        Resource<GeneratorAttributes> resourceGenerator = Resource.generatorBuilder()
                .id("gen1")
                .attributes(generatorAttributes)
                .build();

        TopLevelDocument document = TopLevelDocument.of(resourceGenerator);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"GENERATOR\",\"id\":\"gen1\",\"variantNum\":0,\"attributes\":{\"name\":\"name\",\"fictitious\":false,\"extensionAttributes\":{},\"regulatingPoint\":{\"regulatingEquipmentId\":\"gen1\",\"regulatingResourceType\":\"GENERATOR\",\"regulatingTapChangerType\":null,\"localTerminal\":{\"connectableId\":\"gen1\"},\"regulatingTerminal\":{\"connectableId\":\"idEq\",\"side\":\"ONE\"},\"regulationMode\":null,\"regulatedResourceType\":\"GENERATOR\",\"regulating\":true},\"voltageLevelId\":\"vl1\",\"node\":1,\"bus\":\"bus1\",\"energySource\":\"HYDRO\",\"minP\":2.0,\"maxP\":1.0,\"targetP\":3.0,\"targetQ\":0.0,\"targetV\":4.0,\"ratedS\":0.0,\"p\":NaN,\"q\":NaN,\"condenser\":false,\"regulatingEquipments\":[{\"equipmentId\":\"gen1\",\"resourceType\":\"GENERATOR\",\"regulatingTapChangerType\":\"NO_TAP_CHANGER\"},{\"equipmentId\":\"gen2\",\"resourceType\":\"GENERATOR\",\"regulatingTapChangerType\":\"NO_TAP_CHANGER\"}]}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(resourceGenerator, document2.getData().get(0));
    }

    @Test
    public void testEmptyExtensionAttributes() throws IOException {
        ExtensionAttributesTopLevelDocument document = ExtensionAttributesTopLevelDocument.empty();
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[],\"meta\":{}}";
        assertEquals(jsonRef, json);
    }
}
