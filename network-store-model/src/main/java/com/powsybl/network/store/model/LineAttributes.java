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
import lombok.*;

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Line attributes")
public class LineAttributes extends AbstractAttributes implements BranchAttributes<LineAttributes> {

    @ApiModelProperty("Side 1 voltage level ID")
    private String voltageLevelId1;

    @ApiModelProperty("Side 2 voltage level ID")
    private String voltageLevelId2;

    @ApiModelProperty("Line name")
    private String name;

    @ApiModelProperty("fictitious")
    private Boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Side 1 connection node in node/breaker topology")
    private Integer node1;

    @ApiModelProperty("Side 2 connection node in node/breaker topology")
    private Integer node2;

    @ApiModelProperty("Side 1 connection bus in bus/breaker topology")
    private String bus1;

    @ApiModelProperty("Side 2 connection bus in bus/breaker topology")
    private String bus2;

    @ApiModelProperty("Side 1 possible connection bus in bus/breaker topology")
    private String connectableBus1;

    @ApiModelProperty("Side 2 possible connection bus in bus/breaker topology")
    private String connectableBus2;

    @ApiModelProperty("Resistance in Ohm")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double r = Double.NaN;

    @ApiModelProperty("Reactance in Ohm")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double x = Double.NaN;

    @ApiModelProperty("Side 1 half conductance in Siemens")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double g1 = Double.NaN;

    @ApiModelProperty("Side 1 half susceptance in Siemens")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double b1 = Double.NaN;

    @ApiModelProperty("Side 2 half conductance in Siemens")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double g2 = Double.NaN;

    @ApiModelProperty("Side 2 half susceptance in Siemens")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double b2 = Double.NaN;

    @ApiModelProperty("Side 1 active power in MW")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double p1 = Double.NaN;

    @ApiModelProperty("Side 1 reactive power in MVar")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double q1 = Double.NaN;

    @ApiModelProperty("Side 2 active power in MW")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double p2 = Double.NaN;

    @ApiModelProperty("Side 2 reactive power in MVar")
    @Builder.Default
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double q2 = Double.NaN;

    @ApiModelProperty("Side 1 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position1;

    @ApiModelProperty("Side 2 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position2;

    @ApiModelProperty("mergedXnode extension for tie lines")
    private MergedXnodeAttributes mergedXnode;

    @ApiModelProperty("Current limits side 1")
    private CurrentLimitsAttributes currentLimits1;

    @ApiModelProperty("Current limits side 2")
    private CurrentLimitsAttributes currentLimits2;
}
