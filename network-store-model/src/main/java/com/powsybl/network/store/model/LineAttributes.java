/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Line attributes")
public class LineAttributes implements BranchAttributes {

    @ApiModelProperty("Side 1 voltage level ID")
    private String voltageLevelId1;

    @ApiModelProperty("Side 2 voltage level ID")
    private String voltageLevelId2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Line name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Side 1 connection node in node/breaker topology")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer node1;

    @ApiModelProperty("Side 2 connection node in node/breaker topology")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer node2;

    @ApiModelProperty("Side 1 connection bus in bus/breaker topology")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bus1;

    @ApiModelProperty("Side 2 connection bus in bus/breaker topology")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bus2;

    @ApiModelProperty("Side 1 possible connection bus in bus/breaker topology")
    private String connectableBus1;

    @ApiModelProperty("Side 2 possible connection bus in bus/breaker topology")
    private String connectableBus2;

    @ApiModelProperty("Resistance in Ohm")
    private double r;

    @ApiModelProperty("Reactance in Ohm")
    private double x;

    @ApiModelProperty("Side 1 half conductance in Siemens")
    private double g1;

    @ApiModelProperty("Side 1 half susceptance in Siemens")
    private double b1;

    @ApiModelProperty("Side 2 half conductance in Siemens")
    private double g2;

    @ApiModelProperty("Side 2 half susceptance in Siemens")
    private double b2;

    @ApiModelProperty("Side 1 active power in MW")
    private double p1;

    @ApiModelProperty("Side 1 reactive power in MVar")
    private double q1;

    @ApiModelProperty("Side 2 active power in MW")
    private double p2;

    @ApiModelProperty("Side 2 reactive power in MVar")
    private double q2;

    @ApiModelProperty("Side 1 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position1;

    @ApiModelProperty("Side 2 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position2;

    @ApiModelProperty("mergedXnode extension for tie lines")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MergedXnodeAttributes mergedXnode;
}
