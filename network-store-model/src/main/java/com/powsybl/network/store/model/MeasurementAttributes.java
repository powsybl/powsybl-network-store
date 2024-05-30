/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.extensions.Measurement;
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
@Schema(description = "Measurement attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeasurementAttributes {

    @Schema(description = "The ID of the measurement")
    private String id;

    @Schema(description = "the type of measurement")
    private Measurement.Type type;

    @Schema(description = "List of properties (names associated with their values)")
    private Map<String, String> properties = new HashMap<>();

    @Schema(description = "The side the measurement is applied on")
    private Integer side;

    @Schema(description = "Measurement value")
    private double value;

    @Schema(description = "The standard deviation")
    private double standardDeviation;

    @Schema(description = "The validity status of the measurement")
    private boolean valid;

}
