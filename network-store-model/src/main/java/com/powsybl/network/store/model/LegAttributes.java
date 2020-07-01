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
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("Three windings transformer leg attributes")
public class LegAttributes implements TapChangerParentAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Connection node in node/breaker topology")
    private Integer node;

    @ApiModelProperty("Connection bus in bus/breaker topology")
    private String bus;

    @ApiModelProperty("Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @ApiModelProperty("Nominal series resistance specified in ohm at the voltage of the leg")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double r = Double.NaN;

    @ApiModelProperty("Nominal series reactance specified in ohm at the voltage of the leg")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double x = Double.NaN;

    @ApiModelProperty("Nominal magnetizing conductance specified in S at the voltage of the leg")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double g = Double.NaN;

    @ApiModelProperty("Nominal nominal magnetizing susceptance specified in S  at the voltage of the leg")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double b = Double.NaN;

    @ApiModelProperty("Rated voltage in kV")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double ratedU = Double.NaN;

    @ApiModelProperty("Leg number")
    private Integer legNumber;

    @ApiModelProperty("PhaseTapChangerAttributes")
    private PhaseTapChangerAttributes phaseTapChangerAttributes;

    @ApiModelProperty("RatioTapChangerAttributes")
    private RatioTapChangerAttributes ratioTapChangerAttributes;

    @ApiModelProperty("currentLimitsAttributes")
    private CurrentLimitsAttributes currentLimitsAttributes;
}
