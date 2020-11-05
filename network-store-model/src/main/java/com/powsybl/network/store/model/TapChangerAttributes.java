/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TapChangerAttributes {

    @ApiModelProperty("lowTapPosition")
    private int lowTapPosition;

    @ApiModelProperty("tapPosition")
    private int tapPosition;

    @ApiModelProperty("regulating")
    private boolean regulating;

    @ApiModelProperty("targetDeadband")
    private double targetDeadband;

    @ApiModelProperty("regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;
}
