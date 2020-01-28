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
 * @author Abdelsalem HEDHILI <abdelsalem.hedhili at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("PhaseTapChangerStep attributes")
public class PhaseTapChangerStepAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("position")
    private int position;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("rho")
    private double rho;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("r")
    private double r;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("x")
    private double x;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("g")
    private double g;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("b")
    private double b;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("alpha")
    private double alpha;
}
