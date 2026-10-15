/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.powsybl.network.store.model.AttributeFilter;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ResourceAttributesRestClientImplTest {

    private static final String BASE_URI = "http://localhost:8080/";
    private static final ZonedDateTime CASE_DATE = ZonedDateTime.parse("2015-01-01T00:00:00Z");

    @Test
    void testSpringBuilderPreservesConvertersAndMapperConfiguration() {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        var converter = new MappingJackson2HttpMessageConverter(mapper);
        var builder = RestClient.builder()
            .messageConverters(converters -> {
                converters.clear();
                converters.add(new StringHttpMessageConverter());
                converters.add(converter);
            });
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResourceAttributesRestClient restClient = new ResourceAttributesRestClientImpl(builder, BASE_URI);

        server.expect(requestTo(BASE_URI + "v1/networks"))
            .andExpect(method(PUT))
            .andExpect(jsonPath("$[0].id").value("n1"))
            .andExpect(jsonPath("$[0].attributes.caseDate").value("2015-01-01T00:00:00Z"))
            .andRespond(withSuccess());
        server.expect(requestTo(BASE_URI + "v1/text"))
            .andRespond(withSuccess("custom converter", MediaType.TEXT_PLAIN));

        restClient.updateAll("/networks", List.of(network()), AttributeFilter.JsonViews.Primary.class);
        assertFalse(mapper.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION));
        assertSame(mapper, converter.getObjectMapper());

        assertEquals("custom converter", restClient.get("/text", new ParameterizedTypeReference<String>() { }));
        server.verify();
    }

    @Test
    void testStandaloneBuilderPreservesTimestampSerialization() {
        var builder = ResourceAttributesRestClientImpl.createRestClientBuilder(BASE_URI);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResourceAttributesRestClient restClient = new ResourceAttributesRestClientImpl(builder);

        server.expect(requestTo(BASE_URI + "v1/networks"))
            .andExpect(method(PUT))
            .andExpect(jsonPath("$[0].id").value("n1"))
            .andExpect(jsonPath("$[0].attributes.caseDate").value(CASE_DATE.toInstant().toEpochMilli()))
            .andRespond(withSuccess());
        restClient.updateAll("/networks", List.of(network()), AttributeFilter.JsonViews.Primary.class);
        server.verify();
    }

    private static Resource<NetworkAttributes> network() {
        return Resource.networkBuilder()
            .id("n1")
            .attributes(NetworkAttributes.builder().caseDate(CASE_DATE).build())
            .build();
    }
}
