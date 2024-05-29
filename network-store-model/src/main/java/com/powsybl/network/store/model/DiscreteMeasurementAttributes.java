/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Discrete Measurement attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscreteMeasurementAttributes {

    @Schema(description = "The ID of the discrete measurement if it exists")
    private String id;

    @Schema(description = "Specify what is measured")
    private DiscreteMeasurement.Type type;

    @Schema(description = "If it is the modelization of a tap position, can explicit which tap changer of the transformer it is applied on.")
    private DiscreteMeasurement.TapChanger tapChanger;

    @Schema(description = "What type of discrete value is used for this measurement (boolean, int or string)")
    private DiscreteMeasurement.ValueType valueType;

    @Schema(description = "List of properties (names associated with their values)")
    private Map<String, String> properties = new HashMap<>();

    @Schema(description = "Discrete Measurement value")
    private Object value;

    @Schema(description = "The validity status of the measurement")
    private boolean valid;

}
