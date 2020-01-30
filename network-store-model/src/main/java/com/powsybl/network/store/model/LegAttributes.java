/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("Three windings transformer leg attributes")
public class LegAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Connection node in node/breaker topology")
    private Integer node;

    @ApiModelProperty("Nominal series resistance specified in ohm at the voltage of the leg")
    private double r;

    @ApiModelProperty("Nominal series reactance specified in ohm at the voltage of the leg")
    private double x;

    @ApiModelProperty("Nominal magnetizing conductance specified in S at the voltage of the leg")
    private double g;

    @ApiModelProperty("Nominal nominal magnetizing susceptance specified in S  at the voltage of the leg")
    private double b;

    @ApiModelProperty("Rated voltage in kV")
    private double ratedU;

    @ApiModelProperty("Leg number")
    private int legNumber;

}
