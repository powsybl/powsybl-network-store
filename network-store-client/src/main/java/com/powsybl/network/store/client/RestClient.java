/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TopLevelDocument;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RestClient {

    private final RestTemplate restTemplate;

    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = Objects.requireNonNull(restTemplate);
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

    public <T extends IdentifiableAttributes> void create(String url, List<Resource<T>> resources, Object... uriVariables) {
        restTemplate.postForObject(url, resources, Void.class, uriVariables);
    }

    public void delete(String url, Object... uriVariables) {
        restTemplate.delete(url, uriVariables);
    }

    public <T extends IdentifiableAttributes> Optional<Resource<T>> get(String target, String url, Object... uriVariables) {
        ResponseEntity<TopLevelDocument<T>> response = getDocument(url, uriVariables);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.of(getBody(response).getData().get(0));
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Optional.empty();
        } else {
            throw new PowsyblException("Fail to get " + target + ", status: " + response.getStatusCode());
        }
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        ResponseEntity<TopLevelDocument<T>> response = getDocument(url, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new PowsyblException("Fail to get " + target + " list, status: " + response.getStatusCode());
        }
        return getBody(response).getData();
    }

    public <T extends IdentifiableAttributes> int getTotalCount(String target, String url, Object... uriVariables) {
        ResponseEntity<TopLevelDocument<T>> response = getDocument(url, uriVariables);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new PowsyblException("Fail to get " + target + " empty list, status: " + response.getStatusCode());
        }
        return Integer.parseInt(getBody(response).getMeta().get("totalCount"));
    }

    public <T extends IdentifiableAttributes> void update(String url, Resource<T> resource, Object... uriVariables) {
        restTemplate.put(url, resource, uriVariables);
    }

    public <T extends IdentifiableAttributes> void updateAll(String url, List<Resource<T>> resources, Object... uriVariables) {
        restTemplate.put(url, resources, uriVariables);
    }
}
