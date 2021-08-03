/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dangling line generation attributes")
public class DanglingLineGenerationAttributes {

    @Schema(description = "minP")
    private double minP;

    @Schema(description = "maxP")
    private double maxP;

    @Schema(description = "targetP")
    private double targetP;

    @Schema(description = "targetQ")
    private double targetQ;

    @Schema(description = "targetV")
    private double targetV;

    @Schema(description = "Voltage regulation status")
    private boolean voltageRegulationOn;

    @Schema(description = "reactiveLimits")
    private ReactiveLimitsAttributes reactiveLimits;
}
