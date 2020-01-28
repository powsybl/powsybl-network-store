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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Abdelsalem HEDHILI <abdelsalem.hedhili at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatioTapChangerAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("lowTapPosition")
    private int lowTapPosition = 0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("tapPosition")
    private Integer tapPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("steps")
    private final List<RatioTapChangerStepAttributes> steps = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("loadTapChangingCapabilities")
    private boolean loadTapChangingCapabilities = false;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("regulating")
    private boolean regulating = false;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("targetV")
    private double targetV = Double.NaN;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("targetDeadband")
    private double targetDeadband = Double.NaN;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("ratioTapChangerStepAttributes")
    private List<RatioTapChangerStepAttributes> ratioTapChangerStepAttributes;
}
