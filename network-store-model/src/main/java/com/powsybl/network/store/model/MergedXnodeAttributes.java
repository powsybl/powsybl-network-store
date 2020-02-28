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
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("MergedXnode extension attributes")
public class MergedXnodeAttributes {

    @ApiModelProperty("r divider position 1 -> 2")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float rdp;

    @ApiModelProperty("x divider position 1 -> 2")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float xdp;

    @ApiModelProperty("Xnode active power consumption in MW of side 1")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double xnodeP1;

    @ApiModelProperty("Xnode reactive power consumption in MW of side 1")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double xnodeQ1;

    @ApiModelProperty("Xnode active power consumption in MW of side 2")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double xnodeP2;

    @ApiModelProperty("Xnode reactive power consumption in MW of side 2")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double xnodeQ2;

    @ApiModelProperty("line name of side 1")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String line1Name;

    @ApiModelProperty("line name of side 2")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String line2Name;

    @ApiModelProperty("UCTE Xnode code corresponding to this line")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;
}
