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
import java.util.HashMap;
import java.util.Map;

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
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes("gen1", GENERATOR,
            new TerminalRefAttributes("gen1", null), regulatedTerminalAttributes, null, GENERATOR);
        Map<String, ResourceType> regEquipments = new HashMap<>();
        regEquipments.put("gen1", GENERATOR);
        regEquipments.put("gen2", GENERATOR);
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
        String jsonRef = "{\"data\":[{\"type\":\"GENERATOR\",\"id\":\"gen1\",\"variantNum\":0,\"attributes\":{\"name\":\"name\",\"fictitious\":false,\"extensionAttributes\":{},\"regulatingPoint\":{\"regulatedEquipmentId\":\"gen1\",\"resourceType\":\"GENERATOR\",\"localTerminal\":{\"connectableId\":\"gen1\"},\"regulatingTerminal\":{\"connectableId\":\"idEq\",\"side\":\"ONE\"},\"regulationMode\":null,\"regulatingResourceType\":\"GENERATOR\"},\"voltageLevelId\":\"vl1\",\"node\":1,\"bus\":\"bus1\",\"energySource\":\"HYDRO\",\"minP\":2.0,\"maxP\":1.0,\"voltageRegulatorOn\":false,\"targetP\":3.0,\"targetQ\":0.0,\"targetV\":4.0,\"ratedS\":0.0,\"p\":NaN,\"q\":NaN,\"condenser\":false,\"regulatingEquipments\":{\"gen1\":\"GENERATOR\",\"gen2\":\"GENERATOR\"}}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(resourceGenerator, document2.getData().get(0));
    }

    @Test
    public void testShuntCompensator() throws IOException {
        TerminalRefAttributes regulatedTerminalAttributes =
            TerminalRefAttributes.builder().side("ONE").connectableId("idEq").build();
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes("gen1", SHUNT_COMPENSATOR,
            new TerminalRefAttributes("gen1", null), regulatedTerminalAttributes, null, SHUNT_COMPENSATOR);
        Map<String, ResourceType> regEquipments = new HashMap<>();
        regEquipments.put("gen1", GENERATOR);
        regEquipments.put("shunt2", SHUNT_COMPENSATOR);
        ShuntCompensatorAttributes shuntCompensatorAttributes = ShuntCompensatorAttributes
            .builder()
            .voltageLevelId("vl1")
            .name("name")
            .bus("bus1")
            .fictitious(false)
            .targetV(10.0)
            .regulatingPoint(regulatingPointAttributes)
            .regulatingEquipments(regEquipments)
            .build();

        Resource<ShuntCompensatorAttributes> resourceShuntCompensator = Resource.shuntCompensatorBuilder()
            .id("shunt1")
            .attributes(shuntCompensatorAttributes)
            .build();

        TopLevelDocument document = TopLevelDocument.of(resourceShuntCompensator);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"SHUNT_COMPENSATOR\",\"id\":\"shunt1\",\"variantNum\":0,\"attributes\":{\"name\":\"name\",\"fictitious\":false,\"extensionAttributes\":{},\"regulatingPoint\":{\"regulatedEquipmentId\":\"gen1\",\"resourceType\":\"SHUNT_COMPENSATOR\",\"localTerminal\":{\"connectableId\":\"gen1\"},\"regulatingTerminal\":{\"connectableId\":\"idEq\",\"side\":\"ONE\"},\"regulationMode\":null,\"regulatingResourceType\":\"SHUNT_COMPENSATOR\"},\"voltageLevelId\":\"vl1\",\"bus\":\"bus1\",\"sectionCount\":0,\"p\":NaN,\"q\":NaN,\"voltageRegulatorOn\":false,\"targetV\":10.0,\"targetDeadband\":0.0,\"regulatingEquipments\":{\"gen1\":\"GENERATOR\",\"shunt2\":\"SHUNT_COMPENSATOR\"}}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(resourceShuntCompensator, document2.getData().get(0));
    }

    @Test
    public void testStaticVarCompensator() throws IOException {
        TerminalRefAttributes regulatedTerminalAttributes = TerminalRefAttributes.builder().side("ONE").connectableId("idEq").build();
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes("gen1", STATIC_VAR_COMPENSATOR,
            new TerminalRefAttributes("gen1", null), regulatedTerminalAttributes, null, STATIC_VAR_COMPENSATOR);
        Map<String, ResourceType> regEquipments = new HashMap<>();
        regEquipments.put("gen2", GENERATOR);
        regEquipments.put("shunt3", SHUNT_COMPENSATOR);
        StaticVarCompensatorAttributes staticVarCompensatorAttributes = StaticVarCompensatorAttributes
            .builder()
            .voltageLevelId("vl1")
            .name("name")
            .bus("bus1")
            .fictitious(false)
            .regulatingPoint(regulatingPointAttributes)
            .regulatingEquipments(regEquipments)
            .build();

        Resource<StaticVarCompensatorAttributes> resourceShuntCompensator = Resource.staticVarCompensatorBuilder()
            .id("svc1")
            .attributes(staticVarCompensatorAttributes)
            .build();

        TopLevelDocument document = TopLevelDocument.of(resourceShuntCompensator);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"STATIC_VAR_COMPENSATOR\",\"id\":\"svc1\",\"variantNum\":0,\"attributes\":{\"name\":\"name\",\"fictitious\":false,\"extensionAttributes\":{},\"regulatingPoint\":{\"regulatedEquipmentId\":\"gen1\",\"resourceType\":\"STATIC_VAR_COMPENSATOR\",\"localTerminal\":{\"connectableId\":\"gen1\"},\"regulatingTerminal\":{\"connectableId\":\"idEq\",\"side\":\"ONE\"},\"regulationMode\":null,\"regulatingResourceType\":\"STATIC_VAR_COMPENSATOR\"},\"voltageLevelId\":\"vl1\",\"bus\":\"bus1\",\"bmin\":0.0,\"bmax\":0.0,\"voltageSetPoint\":0.0,\"reactivePowerSetPoint\":0.0,\"p\":NaN,\"q\":NaN,\"regulatingEquipments\":{\"shunt3\":\"SHUNT_COMPENSATOR\",\"gen2\":\"GENERATOR\"}}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument document2 = objectMapper.readValue(json, TopLevelDocument.class);
        assertEquals(resourceShuntCompensator, document2.getData().get(0));
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
