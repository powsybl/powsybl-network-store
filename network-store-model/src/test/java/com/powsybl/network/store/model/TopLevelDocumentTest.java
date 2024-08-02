/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.EnergySource;
import org.junit.Test;

import java.io.IOException;

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
        TopLevelDocument<Resource<SubstationAttributes>> document = TopLevelDocument.of(resource);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"country\":\"FR\"}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument<Resource<SubstationAttributes>> document2 = objectMapper.readValue(json, new TypeReference<>() { });
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
        TopLevelDocument<Resource<SubstationAttributes>> document = TopLevelDocument.of(ImmutableList.of(resource, resource));
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"country\":\"FR\"}},{\"type\":\"SUBSTATION\",\"id\":\"S\",\"variantNum\":0,\"attributes\":{\"fictitious\":false,\"extensionAttributes\":{},\"country\":\"FR\"}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument<Resource<SubstationAttributes>> document2 = objectMapper.readValue(json, new TypeReference<>() { });
        assertEquals(2, document2.getData().size());
        assertEquals(resource, document2.getData().get(0));
        assertEquals(resource, document2.getData().get(1));
    }

    @Test
    public void testGenerator() throws IOException {

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
                .regulatingTerminal(TerminalRefAttributes.builder().side("ONE").connectableId("idEq").build())
                .build();

        Resource<GeneratorAttributes> resourceGenerator = Resource.generatorBuilder()
                .id("gen1")
                .attributes(generatorAttributes)
                .build();

        TopLevelDocument<Resource<GeneratorAttributes>> document = TopLevelDocument.of(resourceGenerator);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(document);
        String jsonRef = "{\"data\":[{\"type\":\"GENERATOR\",\"id\":\"gen1\",\"variantNum\":0,\"attributes\":{\"name\":\"name\",\"fictitious\":false,\"extensionAttributes\":{},\"voltageLevelId\":\"vl1\",\"node\":1,\"bus\":\"bus1\",\"energySource\":\"HYDRO\",\"minP\":2.0,\"maxP\":1.0,\"voltageRegulatorOn\":false,\"targetP\":3.0,\"targetQ\":0.0,\"targetV\":4.0,\"ratedS\":0.0,\"p\":NaN,\"q\":NaN,\"regulatingTerminal\":{\"connectableId\":\"idEq\",\"side\":\"ONE\"},\"condenser\":false}}],\"meta\":{}}";
        assertEquals(jsonRef, json);
        TopLevelDocument<Resource<GeneratorAttributes>> document2 = objectMapper.readValue(json, new TypeReference<>() { });
        assertEquals(resourceGenerator, document2.getData().get(0));
    }
}
