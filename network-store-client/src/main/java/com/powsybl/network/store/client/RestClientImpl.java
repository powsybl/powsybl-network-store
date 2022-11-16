/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        return new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(UriComponentsBuilder.fromUriString(baseUri)
                        .path(NetworkStoreApi.VERSION)));
    }

    private <T extends IdentifiableAttributes> ResponseEntity<TopLevelDocument<T>> getDocument(String url, Object... uriVariables) {
        return restTemplate.exchange(url,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                new ParameterizedTypeReference<TopLevelDocument<T>>() {
                },
                uriVariables);
    }

    private static <T extends IdentifiableAttributes> TopLevelDocument<T> getBody(ResponseEntity<TopLevelDocument<T>> response) {
        if (response.getBody() == null) {
            throw new PowsyblException("Body is null");
        }
        return response.getBody();
    }

    @Override
    public <T extends IdentifiableAttributes> void create(String url, List<Resource<T>> resources, Object... uriVariables) {
        restTemplate.postForObject(url, resources, Void.class, uriVariables);
    }

    @Override
    public <T extends IdentifiableAttributes> Optional<Resource<T>> getOne(String target, String url, Object... uriVariables) {
        ResponseEntity<TopLevelDocument<T>> response = getDocument(url, uriVariables);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.of(getBody(response).getData().get(0));
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Optional.empty();
        } else {
            throw new PowsyblException("Fail to get " + target + ", status: " + response.getStatusCode());
        }
    }

    @Override
    public <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        ResponseEntity<TopLevelDocument<T>> response = getDocument(url, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new PowsyblException("Fail to get " + target + " list, status: " + response.getStatusCode());
        }
        return getBody(response).getData();
    }

    @Override
    public <T extends IdentifiableAttributes> void update(String url, Resource<T> resource, Object... uriVariables) {
        restTemplate.put(url, resource, uriVariables);
    }

    @Override
    public <T extends Attributes> void updateAll(String url, List<Resource<T>> resources, Object... uriVariables) {
        HttpEntity<?> entity = new HttpEntity<>(resources);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new PowsyblException("Fail to put at " + url + ", status: " + response.getStatusCode());
        }
    }

    @Override
    public <E> E get(String uri, ParameterizedTypeReference<E> responseType, Object... uriVariables) {
        return restTemplate.exchange(uri, HttpMethod.GET, null, responseType, uriVariables)
            .getBody();
    }

    @Override
    public void put(String uri, Object... uriVariables) {
        restTemplate.put(uri, null, uriVariables);
    }

    @Override
    public void post(String uri, Object... uriVariables) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(uri, new HttpEntity<>(headers), Void.class, uriVariables);
    }

    @Override
    public void delete(String url, Object... uriVariables) {
        restTemplate.delete(url, uriVariables);
    }
}
