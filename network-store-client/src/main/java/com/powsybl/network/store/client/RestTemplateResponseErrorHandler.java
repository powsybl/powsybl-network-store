/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.DuplicateVariantNumException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.network.store.model.TopLevelError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        byte[] body = response.getBody().readAllBytes();
        String strBody = new String(body);
        if (strBody.contains("network_pkey")) {
            throw new DuplicateVariantNumException(strBody);
        }
        if (response.getStatusCode().is5xxServerError()) {
            throw new HttpServerErrorException(response.getStatusCode(), response.getStatusText(), body, StandardCharsets.UTF_8);
        } else if (response.getStatusCode().is4xxClientError()) {
            if (response.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new HttpClientErrorException(response.getStatusCode(), response.getStatusText(), body, StandardCharsets.UTF_8);
            }
        }
    }

    public static Optional<TopLevelError> parseJsonApiError(String body, ObjectMapper mapper) {
        TopLevelError error = null;
        if (!body.isBlank()) {
            try {
                error = mapper.readValue(body, TopLevelError.class);
            } catch (JsonProcessingException ex) {
                // nothing to do, not json after all
            }
        }
        return Optional.ofNullable(error);
    }
}
