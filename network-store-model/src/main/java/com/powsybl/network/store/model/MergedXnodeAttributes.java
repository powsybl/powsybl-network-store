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
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("MergedXnode extension attributes")
public class MergedXnodeAttributes {

    @ApiModelProperty("r divider position 1 -> 2")
    private Float rdp;

    @ApiModelProperty("x divider position 1 -> 2")
    private Float xdp;

    @ApiModelProperty("Xnode active power consumption in MW of side 1")
    private Double xnodeP1;

    @ApiModelProperty("Xnode reactive power consumption in MW of side 1")
    private Double xnodeQ1;

    @ApiModelProperty("Xnode active power consumption in MW of side 2")
    private Double xnodeP2;

    @ApiModelProperty("Xnode reactive power consumption in MW of side 2")
    private Double xnodeQ2;

    @ApiModelProperty("line id of side 1")
    private String line1Id;

    @ApiModelProperty("line name of side 1")
    private String line1Name;

    @ApiModelProperty("line 1 fictitious")
    private boolean line1Fictitious;

    @ApiModelProperty("half line 1 susceptance in Siemens of side 1")
    private double line1B1;

    @ApiModelProperty("half line 1 susceptance in Siemens of side 2")
    private double line1B2;

    @ApiModelProperty("half line 1 conductance in Siemens of side 1")
    private double line1G1;

    @ApiModelProperty("half line 1 conductance in Siemens of side 2")
    private double line1G2;

    @ApiModelProperty("line id of side 2")
    private String line2Id;

    @ApiModelProperty("line name of side 2")
    private String line2Name;

    @ApiModelProperty("line 2 fictitious")
    private boolean line2Fictitious;

    @ApiModelProperty("half line 2 susceptance in Siemens of side 1")
    private double line2B1;

    @ApiModelProperty("half line 2 susceptance in Siemens of side 2")
    private double line2B2;

    @ApiModelProperty("half line 2 conductance in Siemens of side 1")
    private double line2G1;

    @ApiModelProperty("half line 2 conductance in Siemens of side 2")
    private double line2G2;

    @ApiModelProperty("UCTE Xnode code corresponding to this line")
    private String code;
}
