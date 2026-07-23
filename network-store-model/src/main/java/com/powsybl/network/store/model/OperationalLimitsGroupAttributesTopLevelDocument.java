/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Schema(description = "Top level document compliant with Json API spec")
public class OperationalLimitsGroupAttributesTopLevelDocument extends AbstractTopLevelDocument<OperationalLimitsGroupAttributes> {

    @JsonCreator
    public OperationalLimitsGroupAttributesTopLevelDocument(@JsonProperty("data") List<OperationalLimitsGroupAttributes> data, @JsonProperty("meta") Map<String, String> meta) {
        super(data, meta);
    }

    public static OperationalLimitsGroupAttributesTopLevelDocument empty() {
        return new OperationalLimitsGroupAttributesTopLevelDocument(List.of(), new HashMap<>());
    }

    public static OperationalLimitsGroupAttributesTopLevelDocument of(OperationalLimitsGroupAttributes data) {
        return new OperationalLimitsGroupAttributesTopLevelDocument(List.of(data), new HashMap<>());
    }

    public static OperationalLimitsGroupAttributesTopLevelDocument of(List<OperationalLimitsGroupAttributes> data) {
        return new OperationalLimitsGroupAttributesTopLevelDocument(data, new HashMap<>());
    }
}
