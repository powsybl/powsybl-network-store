/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Schema(description = "Top level document compliant with Json API spec")
@Getter
public abstract class AbstractTopLevelDocument<T> {

    @Schema(description = "data", required = true)
    private final List<T> data;

    @Schema(description = "Metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> meta;

    @JsonCreator
    protected AbstractTopLevelDocument(@JsonProperty("data") List<T> data, @JsonProperty("meta") Map<String, String> meta) {
        this.data = Objects.requireNonNull(data);
        this.meta = meta;
    }

    public AbstractTopLevelDocument<T> addMeta(String name, String value) {
        meta.put(name, value);
        return this;
    }
}
