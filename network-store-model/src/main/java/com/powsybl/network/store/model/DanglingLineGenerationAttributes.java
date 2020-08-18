/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("Dangling line generation attributes")
public class DanglingLineGenerationAttributes {

    @ApiModelProperty("minP")
    private double minP;

    @ApiModelProperty("maxP")
    private double maxP;

    @ApiModelProperty("targetP")
    private double targetP;

    @ApiModelProperty("targetQ")
    private double targetQ;

    @ApiModelProperty("targetV")
    private double targetV;

    @ApiModelProperty("Voltage regulation status")
    private boolean voltageRegulationOn;

    @ApiModelProperty("reactiveLimits")
    private ReactiveLimitsAttributes reactiveLimits;
}
