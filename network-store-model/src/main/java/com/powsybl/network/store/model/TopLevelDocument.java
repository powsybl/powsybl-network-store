/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Schema(description = "Top level document compliant with Json API spec")
public class TopLevelDocument<T extends IdentifiableAttributes> extends AbstractTopLevelDocument<Resource<T>> {

    @JsonCreator
    public TopLevelDocument(@JsonProperty("data") List<Resource<T>> data, @JsonProperty("meta") Map<String, String> meta) {
        super(data, meta);
    }

    public static <T extends IdentifiableAttributes> TopLevelDocument<T> empty() {
        return new TopLevelDocument<>(List.of(), new HashMap<>());
    }

    public static <T extends IdentifiableAttributes> TopLevelDocument<T> of(Resource<T> data) {
        return new TopLevelDocument<>(List.of(data), new HashMap<>());
    }

    public static <T extends IdentifiableAttributes> TopLevelDocument<T> of(List<Resource<T>> data) {
        return new TopLevelDocument<>(data, new HashMap<>());
    }
}
