/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.SwitchKind;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ResourceTest {

    @Test
    public void networkTest() throws IOException {
        Resource<NetworkAttributes> resource = Resource.networkBuilder().id("foo")
                .attributes(NetworkAttributes.builder()
                        .uuid(UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4"))
                        .caseDate(DateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        String json = objectMapper.writeValueAsString(resource);
        assertEquals("{\"type\":\"NETWORK\",\"id\":\"foo\",\"attributes\":{\"uuid\":\"7928181c-7977-4592-ba19-88027e4254e4\",\"fictitious\":false,\"caseDate\":1420070400000,\"forecastDistance\":0,\"connectedComponentsValid\":false,\"synchronousComponentsValid\":false}}", json);
        Resource<NetworkAttributes> resource2 = objectMapper.readValue(json, new TypeReference<Resource<NetworkAttributes>>() { });
        assertNotNull(resource2);
        assertEquals("foo", resource2.getId());
        assertEquals(DateTime.parse("2015-01-01T00:00:00.000Z"), resource2.getAttributes().getCaseDate());
        assertEquals(0, resource2.getAttributes().getForecastDistance());
        assertNull(resource2.getAttributes().getSourceFormat());
    }

    @Test
    public void substationTest() throws IOException {
        Resource<SubstationAttributes> resource = Resource.substationBuilder()
                .id("S")
                .attributes(SubstationAttributes.builder()
                        .name("SS")
                        .country(Country.FR)
                        .tso("RTE")
                        .build())
                .build();

        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writeValueAsString(resource);

        String jsonRef = "{\"type\":\"SUBSTATION\",\"id\":\"S\",\"attributes\":{\"name\":\"SS\",\"fictitious\":false,\"country\":\"FR\",\"tso\":\"RTE\"}}";
        assertEquals(jsonRef, json);

        Resource<SubstationAttributes> resource2 = objectMapper.readValue(json, new TypeReference<Resource<SubstationAttributes>>() { });
        assertEquals(ResourceType.SUBSTATION, resource2.getType());
        assertEquals("S", resource2.getId());
        assertEquals("SS", resource2.getAttributes().getName());
        assertEquals(Country.FR, resource2.getAttributes().getCountry());
        assertEquals("RTE", resource2.getAttributes().getTso());
    }

    @Test
    public void switchTest() {
        UUID testNetworkId = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        boolean[] dirty = new boolean[1];
        ResourceUpdater updateR = (networkUuid, resource) -> {
            dirty[0] = true;
        };

        Resource<SwitchAttributes> resourceBreaker = Resource.switchBuilder(testNetworkId, updateR)
                .id("idBreaker")
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId("vl1")
                        .name("b1")
                        .bus1("bus1")
                        .bus2("bus2")
                        .kind(SwitchKind.BREAKER)
                        .open(false)
                        .fictitious(false)
                        .build())
                .build();

        assertEquals(Boolean.FALSE, dirty[0]);
        assertEquals(Boolean.FALSE, resourceBreaker.getAttributes().isOpen());
        resourceBreaker.getAttributes().setOpen(true);  // opening the breaker switch
        assertEquals(Boolean.TRUE, dirty[0]);
        assertEquals(Boolean.TRUE, resourceBreaker.getAttributes().isOpen());

        dirty[0] = false;
        Resource<SwitchAttributes> resourceDisconnector = Resource.switchBuilder(testNetworkId, updateR)
                .id("idDisconnector")
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId("vl2")
                        .name("d1")
                        .bus1("bus3")
                        .bus2("bus4")
                        .kind(SwitchKind.DISCONNECTOR)
                        .open(true)
                        .fictitious(false)
                        .build())
                .build();

        assertEquals(Boolean.FALSE, dirty[0]);
        assertEquals(Boolean.TRUE, resourceDisconnector.getAttributes().isOpen());
        resourceDisconnector.getAttributes().setOpen(false);  // closing the disconnector switch
        assertEquals(Boolean.TRUE, dirty[0]);
        assertEquals(Boolean.FALSE, resourceDisconnector.getAttributes().isOpen());
    }

    @Test
    public void lineTest() throws IOException {
        UUID testNetworkId = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        boolean[] dirty = new boolean[1];
        ResourceUpdater updateR = (networkUuid, resource) -> {
            dirty[0] = true;
        };

        Resource<LineAttributes> resourceLine = Resource.lineBuilder(testNetworkId, updateR)
                .id("idLine")
                .attributes(LineAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("idLine")
                        .node1(1)
                        .node2(1)
                        .bus1("bus1")
                        .bus2("bus2")
                        .r(1)
                        .x(1)
                        .g1(1)
                        .b1(1)
                        .g2(1)
                        .b2(1)
                        .p1(0)
                        .q1(0)
                        .p2(0)
                        .q2(0)
                        .build())
                .build();

        assertEquals(Boolean.FALSE, dirty[0]);
        assertEquals(0., resourceLine.getAttributes().getP1(), 0);
        resourceLine.getAttributes().setP1(100.0);
        assertEquals(Boolean.TRUE, dirty[0]);
        assertEquals(100.0, resourceLine.getAttributes().getP1(), 0);
    }

    @Test
    public void twoWindingsTransormer() {
        UUID testNetworkId = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        boolean[] dirty = new boolean[1];
        ResourceUpdater updateR = (networkUuid, resource) -> {
            dirty[0] = true;
        };

        Resource<TwoWindingsTransformerAttributes> resourceTransformer = Resource.twoWindingsTransformerBuilder(testNetworkId, updateR)
                .id("id2WT")
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("id2WT")
                        .node1(1)
                        .node2(1)
                        .bus1("bus1")
                        .bus2("bus2")
                        .r(1)
                        .x(1)
                        .b(1)
                        .g(1)
                        .ratedU1(1.)
                        .ratedU2(1.)
                        .p1(0)
                        .p2(0)
                        .q1(0)
                        .q2(0)
                        .build())
                .build();

        assertEquals(Boolean.FALSE, dirty[0]);
        assertEquals(0., resourceTransformer.getAttributes().getP1(), 0);
        resourceTransformer.getAttributes().setP1(100.0);
        assertEquals(Boolean.TRUE, dirty[0]);
        assertEquals(100.0, resourceTransformer.getAttributes().getP1(), 0);
    }

    @Test
    public void threeWindingsTransormer() throws IOException {
        UUID testNetworkId = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

        boolean[] dirty = new boolean[1];
        ResourceUpdater updateR = (networkUuid, resource) -> {
            dirty[0] = true;
        };

        Resource<ThreeWindingsTransformerAttributes> resourceTransformer = Resource.threeWindingsTransformerBuilder(testNetworkId, updateR)
                .id("id3WT")
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name("id3WT")
                        .ratedU0(1)
                        .p1(0)
                        .p2(0)
                        .p3(0)
                        .q1(0)
                        .q2(0)
                        .q3(0)
                        .build())
                .build();

        assertEquals(Boolean.FALSE, dirty[0]);
        assertEquals(0., resourceTransformer.getAttributes().getP1(), 0);
        assertEquals(0., resourceTransformer.getAttributes().getQ2(), 0);
        assertEquals(0., resourceTransformer.getAttributes().getP3(), 0);

        resourceTransformer.getAttributes().setP1(200.);
        resourceTransformer.getAttributes().setQ2(500.);
        resourceTransformer.getAttributes().setP3(700.);

        assertEquals(Boolean.TRUE, dirty[0]);
        assertEquals(200., resourceTransformer.getAttributes().getP1(), 0);
        assertEquals(500., resourceTransformer.getAttributes().getQ2(), 0);
        assertEquals(700., resourceTransformer.getAttributes().getP3(), 0);
    }
}
