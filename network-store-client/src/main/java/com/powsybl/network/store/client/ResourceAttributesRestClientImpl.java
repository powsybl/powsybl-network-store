/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Component
public class ResourceAttributesRestClientImpl implements ResourceAttributesRestClient {

    private final RestClient restClient;

    // Used for standalone (non-Spring) usage, e.g. from integration tests or external tools
    public ResourceAttributesRestClientImpl(String baseUri) {
        this(createRestClientBuilder(baseUri));
    }

    // Used in unit tests with a mock-backed RestClient.Builder
    public ResourceAttributesRestClientImpl(RestClient.Builder restClientBuilder) {
        this.restClient = Objects.requireNonNull(restClientBuilder)
            .defaultStatusHandler(new RestClientResponseErrorHandler())
            .messageConverters(ResourceAttributesRestClientImpl::enableDefaultViewInclusion)
            .build();
    }

    @Autowired
    public ResourceAttributesRestClientImpl(RestClient.Builder restClientBuilder,
                          @Value("${powsybl.services.network-store-server.base-uri:http://network-store-server/}") String baseUri) {
        this.restClient = Objects.requireNonNull(restClientBuilder)
            .defaultStatusHandler(new RestClientResponseErrorHandler())
            .baseUrl(baseUri + NetworkStoreApi.VERSION)
            .messageConverters(ResourceAttributesRestClientImpl::enableDefaultViewInclusion)
            .build();
    }

    public static RestClient.Builder createRestClientBuilder(String baseUri) {
        return RestClient.builder()
            .baseUrl(baseUri + NetworkStoreApi.VERSION)
            .messageConverters(converters -> {
                converters.clear();
                converters.add(createMapping());
            });
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

    /**
     * Replace the MappingJackson2HttpMessageConverter in the RestClient's
     * converter list with a new instance whose ObjectMapper has
     * DEFAULT_VIEW_INCLUSION enabled.
     *
     * We must replace (not mutate) the converter because Spring Boot 3.x shares the
     * same MappingJackson2HttpMessageConverter instance across all RestClients built
     * from the auto-configured RestClient.Builder. Mutating it would enable
     * DEFAULT_VIEW_INCLUSION for every RestClient in the application.
     *
     * The replacement converter should be configured exactly like the Spring
     * Boot one except for the ObjectMapper. There are no official Spring Boot APIs
     * to guarantee the converter equivalence, so this may need to be revisited
     * if future versions change which converters are created by springboot and
     * how they get the autoconfiguration.
     * The ObjectMapper is configured exactly like the Spring Boot one except
     * for DEFAULT_VIEW_INCLUSION (using the jackson copy() API). This is guaranteed
     * but will also need to be updated when upgrade from jackson2 to jackson3 and
     * the replacement will depend on springboot's integrations of jackson3's features
     * (rebuild() vs copy())
     */
    private static void enableDefaultViewInclusion(List<HttpMessageConverter<?>> converters) {
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof MappingJackson2HttpMessageConverter c) {
                converters.set(i, new MappingJackson2HttpMessageConverter(
                    c.getObjectMapper().copy().enable(MapperFeature.DEFAULT_VIEW_INCLUSION)));
                return;
            }
        }
    }

    private <T, D extends AbstractTopLevelDocument<T>> ResponseEntity<D> getDocument(String url, ParameterizedTypeReference<D> parameterizedTypeReference, Object... uriVariables) {
        return restClient.get()
            .uri(url, uriVariables)
            .retrieve()
            .toEntity(parameterizedTypeReference);
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
        ResponseEntity<Void> response = restClient.post()
            .uri(url, uriVariables)
            .body(resources)
            .retrieve()
            .toEntity(Void.class);
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
    public <T extends Attributes> void updateAll(String url, List<Resource<T>> resources, Class<?> viewClass, Object... uriVariables) {
        MappingJacksonValue body = wrapViewAware(viewClass, resources);
        ResponseEntity<Void> response = restClient.put()
            .uri(url, uriVariables)
            .body(body)
            .retrieve()
            .toEntity(Void.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "put", response.getStatusCode());
        }
    }

    private <T extends Attributes> MappingJacksonValue wrapViewAware(Class<?> viewClass, List<Resource<T>> resources) {
        MappingJacksonValue jacksonValue = new MappingJacksonValue(resources);
        jacksonValue.setSerializationView(viewClass);
        return jacksonValue;
    }

    @Override
    public <E> E get(String url, ParameterizedTypeReference<E> responseType, Object... uriVariables) {
        ResponseEntity<E> response = restClient.get()
            .uri(url, uriVariables)
            .retrieve()
            .toEntity(responseType);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "get", response.getStatusCode());
        }
        return response.getBody();
    }

    @Override
    public void put(String url, Object... uriVariables) {
        ResponseEntity<Void> response = restClient.put()
            .uri(url, uriVariables)
            .retrieve()
            .toEntity(Void.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "put", response.getStatusCode());
        }
    }

    @Override
    public void post(String url, Object... uriVariables) {
        ResponseEntity<Void> response = restClient.post()
            .uri(url, uriVariables)
            .retrieve()
            .toEntity(Void.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "post", response.getStatusCode());
        }
    }

    @Override
    public void delete(String url, Object... uriVariables) {
        ResponseEntity<Void> response = restClient.delete()
            .uri(url, uriVariables)
            .retrieve()
            .toEntity(Void.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "delete", response.getStatusCode());
        }
    }

    @Override
    public <T> void deleteAll(String url, T body, Object... uriVariables) {
        ResponseEntity<Void> response = restClient.method(HttpMethod.DELETE)
            .uri(url, uriVariables)
            .body(body)
            .retrieve()
            .toEntity(Void.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw createHttpException(url, "delete", response.getStatusCode());
        }
    }
}
