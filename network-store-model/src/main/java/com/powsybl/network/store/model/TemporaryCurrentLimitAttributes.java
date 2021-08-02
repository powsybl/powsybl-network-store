/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Temporary current limit attributes")
public class TemporaryCurrentLimitAttributes {

    @Schema(description = "Temporary limit name")
    private String name;

    @Schema(description = "Temporary limit value")
    private double value;

    @Schema(description = "Temporary limit acceptable duration")
    private int acceptableDuration;

    @Schema(description = "Temporary limit is fictitious")
    private boolean fictitious;
}
