/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ApiModel(value = "Top level document", description = "Top level document compliant with Json API spec")
public class TopLevelDocument<T extends IdentifiableAttributes> {

    @ApiModelProperty(value = "data", required = true)
    private final List<Resource<T>> data;

    @ApiModelProperty(value = "Metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> meta;

    @JsonCreator
    public TopLevelDocument(@JsonProperty("data") List<Resource<T>> data, @JsonProperty("meta") Map<String, String> meta) {
        this.data = Objects.requireNonNull(data);
        this.meta = meta;
    }

    public static <T extends IdentifiableAttributes> TopLevelDocument<T> empty() {
        return new TopLevelDocument<>(ImmutableList.of(), new HashMap<>());
    }

    public static <T extends IdentifiableAttributes> TopLevelDocument<T> of(Resource<T> data) {
        return new TopLevelDocument<>(ImmutableList.of(data), new HashMap<>());
    }

    public static <T extends IdentifiableAttributes> TopLevelDocument<T> of(List<Resource<T>> data) {
        return new TopLevelDocument<>(data, new HashMap<>());
    }

    public List<Resource<T>> getData() {
        return data;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public TopLevelDocument<T> addMeta(String name, String value) {
        meta.put(name, value);
        return this;
    }
}
