/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server.exceptions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.powsybl.network.store.model.ErrorObject;
import com.powsybl.network.store.model.TopLevelError;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    private static HttpStatus computeHttpStatus(TopLevelError topLevelError) {
        List<String> statusesString;
        Map<String, String> meta = topLevelError.getMeta();
        if (meta != null && meta.get(TopLevelError.META_STATUS) != null) {
            statusesString = List.of(meta.get(TopLevelError.META_STATUS));
        } else {
            statusesString = topLevelError.getErrors().stream()
                .map(ErrorObject::getStatus).collect(Collectors.toList());
        }
        List<HttpStatus> statuses = statusesString.stream()
            .map(Integer::parseInt)
            .distinct()
            .map(HttpStatus::valueOf)
            .collect(Collectors.toList());
        if (statuses.size() == 1) {
            return statuses.get(0);
        }

        List<Series> series = statuses.stream().map(HttpStatus::series).collect(Collectors.toList());
        if (series.contains(HttpStatus.Series.CLIENT_ERROR)) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @ResponseBody
    @ExceptionHandler(JsonApiErrorResponseException.class)
    public ResponseEntity<TopLevelError> handleControllerException(HttpServletRequest request, JsonApiErrorResponseException ex) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(ex.getMessage(), ex);
        }
        TopLevelError topLevelError = ex.getTopLevelError();
        return new ResponseEntity<>(topLevelError, computeHttpStatus(topLevelError));
    }
}
