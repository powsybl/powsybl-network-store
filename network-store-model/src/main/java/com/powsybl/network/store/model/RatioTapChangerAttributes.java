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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Abdelsalem HEDHILI <abdelsalem.hedhili at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ApiModel("RatioTapChanger attributes")
public class RatioTapChangerAttributes extends TapChangerAttributes {

    @ApiModelProperty("steps")
    private List<RatioTapChangerStepAttributes> steps;

    @ApiModelProperty("loadTapChangingCapabilities")
    private boolean loadTapChangingCapabilities;

    @ApiModelProperty("targetV")
    private double targetV;

    @ApiModelProperty("targetDeadband")
    private double targetDeadband;

    @ApiModelProperty("regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;
}
