/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "operational limit group attributes")
public class OperationalLimitsGroupAttributes {

    @Schema(description = "Id")
    private String id;

    @Schema(description = "Current limits")
    private LimitsAttributes currentLimits;

    @Schema(description = "apparent power limits")
    private LimitsAttributes apparentPowerLimits;

    @Schema(description = "Active power limits")
    private LimitsAttributes activePowerLimits;

    @JsonIgnore
    @Schema(description = "group side")
    private Integer side;
}
