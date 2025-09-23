/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RestClientImpl implements RestClient {

    private final RestTemplate restTemplate;

    public RestClientImpl(String baseUri) {
        this(createRestTemplateBuilder(baseUri));
    }

    @Autowired
    public RestClientImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = Objects.requireNonNull(restTemplateBuilder).errorHandler(new RestTemplateResponseErrorHandler()).build();
    }

    public static RestTemplateBuilder createRestTemplateBuilder(String baseUri) {
        return new RestTemplateBuilder(restTemplate1 -> restTemplate1.setMessageConverters(List.of(createMapping()))).uriTemplateHandler(new DefaultUriBuilderFactory(UriComponentsBuilder.fromUriString(baseUri)
                        .path(NetworkStoreApi.VERSION)));
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        return objectMapper;
    }

    private static MappingJackson2HttpMessageConverter createMapping() {
        var converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(createObjectMapper());
        return converter;
    }

    private <T, D extends AbstractTopLevelDocument<T>> ResponseEntity<D> getDocument(String url, ParameterizedTypeReference<D> parameterizedTypeReference, Object... uriVariables) {
        return restTemplate.exchange(url,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                parameterizedTypeReference,
                uriVariables);
    }

    private static <T, D extends AbstractTopLevelDocument<T>> D getBody(ResponseEntity<D> response) {
        D body = response.getBody();
        if (body == null) {
            throw new PowsyblException("Body is null");
        }
        return body;
    }

    private static PowsyblException createHttpException(String url, String method, HttpStatusCode httpStatusCode) {
        return new PowsyblException("Fail to " + method + " at " + url + ", status: " + httpStatusCode);
    }

    @Override
    public <T extends IdentifiableAttributes> void createAll(String url, List<Resource<T>> resources, Object... uriVariables) {
        HttpEntity<?> entity = new HttpEntity<>(resources);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class, uriVariables);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw createHttpException(url, "post", response.getStatusCode());
        }
    }

    @Override
    public <T extends IdentifiableAttributes> Optional<Resource<T>> getOne(String target, String url, Object... uriVariables) {
        return getOneDocument(url, new ParameterizedTypeReference<TopLevelDocument<T>>() {
        }, uriVariables);
    }

    @Override
    public Optional<ExtensionAttributes> getOneExtensionAttributes(String url, Object... uriVariables) {
        return getOneDocument(url, new ParameterizedTypeReference<ExtensionAttributesTopLevelDocument>() {
        }, uriVariables);
    }

    @Override
    public Optional<OperationalLimitsGroupAttributes> getOneOperationalLimitsGroupAttributes(String url, Object... uriVariables) {
        return getOneDocument(url, new ParameterizedTypeReference<OperationalLimitsGroupAttributesTopLevelDocument>() {
        }, uriVariables);
    }

    private <T, D extends AbstractTopLevelDocument<T>> Optional<T> getOneDocument(String url, ParameterizedTypeReference<D> parameterizedTypeReference, Object... uriVariables) {
        ResponseEntity<D> response = getDocument(url, parameterizedTypeReference, uriVariables);
        if (response.getStatusCode() == HttpStatus.OK) {
            AbstractTopLevelDocument<T> body = getBody(response);
            return Optional.of(body.getData().get(0));
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Optional.empty();
        } else {
            throw createHttpException(url, "get", response.getStatusCode());
        }
    }

    @Override
    public <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        ResponseEntity<TopLevelDocument<T>> response = getDocument(url, new ParameterizedTypeReference<>() {
        }, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "get", response.getStatusCode());
        }
        TopLevelDocument<T> body = getBody(response);
        return body.getData();
    }

    @Override
    public <T extends Attributes> void updateAll(String url, List<Resource<T>> resources, Object... uriVariables) {
        HttpEntity<?> entity = new HttpEntity<>(resources);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "put", response.getStatusCode());
        }
    }

    @Override
    public <E> E get(String url, ParameterizedTypeReference<E> responseType, Object... uriVariables) {
        ResponseEntity<E> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "get", response.getStatusCode());
        }
        return response.getBody();
    }

    @Override
    public void put(String url, Object... uriVariables) {
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, null, Void.class, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "put", response.getStatusCode());
        }
    }

    @Override
    public void post(String url, Object... uriVariables) {
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, null, Void.class, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "post", response.getStatusCode());
        }
    }

    @Override
    public void delete(String url, Object... uriVariables) {
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "delete", response.getStatusCode());
        }
    }

    @Override
    public void deleteAll(String url, List<String> body, Object... uriVariables) {
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(body);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "delete", response.getStatusCode());
        }
    }

    @Override
    public void deleteAllOperationalLimitsGroups(String url, Map<String, Map<Integer, Set<String>>> body, Object... uriVariables) {
        HttpEntity<Map<String, Map<Integer, Set<String>>>> requestEntity = new HttpEntity<>(body);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "delete", response.getStatusCode());
        }
    }
}
