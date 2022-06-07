/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
@Schema(description = "Top level document compliant with Json API spec representing errors")
public class TopLevelError {

    public static final String META_STATUS = "status";
    public static final String META_MESSAGE = "message";

    @Schema(description = "Errors", required = true)
    private final List<ErrorObject> errors;

    @Schema(description = "Metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> meta;

    @JsonCreator
    public TopLevelError(@JsonProperty("errors") List<ErrorObject> errors, @JsonProperty("meta") Map<String, String> meta) {
        this.errors = Objects.requireNonNull(errors);
        this.meta = meta;
    }

    public static TopLevelError of(ErrorObject error) {
        return new TopLevelError(List.of(error), null);
    }

    public static TopLevelError of(List<ErrorObject> errors) {
        return new TopLevelError(errors, null);
    }

    public static TopLevelError ofStatus(ErrorObject error, String status) {
        return new TopLevelError(List.of(error), Map.of(META_STATUS, status));
    }

    public static TopLevelError ofMessage(ErrorObject error, String message) {
        return new TopLevelError(List.of(error), Map.of(META_MESSAGE, message));
    }

    public static TopLevelError of(ErrorObject error, String status, String message) {
        return new TopLevelError(List.of(error), Map.of(META_STATUS, status, META_MESSAGE, message));
    }

    public List<ErrorObject> getErrors() {
        return errors;
    }

    public Map<String, String> getMeta() {
        return meta;
    }
}
