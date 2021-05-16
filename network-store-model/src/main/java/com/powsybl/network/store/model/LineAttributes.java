/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Line attributes")
public class LineAttributes extends AbstractAttributes implements BranchAttributes {

    @ApiModelProperty("Side 1 voltage level ID")
    private String voltageLevelId1;

    @ApiModelProperty("Side 2 voltage level ID")
    private String voltageLevelId2;

    @ApiModelProperty("Line name")
    private String name;

    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Aliases without type")
    private Set<String> aliasesWithoutType;

    @ApiModelProperty("Alias by type")
    private Map<String, String> aliasByType;

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
    @Builder.Default
    private double p1 = Double.NaN;

    @ApiModelProperty("Side 1 reactive power in MVar")
    @Builder.Default
    private double q1 = Double.NaN;

    @ApiModelProperty("Side 2 active power in MW")
    @Builder.Default
    private double p2 = Double.NaN;

    @ApiModelProperty("Side 2 reactive power in MVar")
    @Builder.Default
    private double q2 = Double.NaN;

    @ApiModelProperty("Side 1 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position1;

    @ApiModelProperty("Side 2 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position2;

    @ApiModelProperty("mergedXnode extension for tie lines")
    private MergedXnodeAttributes mergedXnode;

    private LimitsAttributes currentLimits1;

    @ApiModelProperty("Current limits side 2")
    private LimitsAttributes currentLimits2;

    @ApiModelProperty("Apparent power limit side 1")
    private LimitsAttributes apparentPowerLimits1;

    @ApiModelProperty("Apparent power limit side 2")
    private LimitsAttributes apparentPowerLimits2;

    @ApiModelProperty("Active power limit side 1")
    private LimitsAttributes activePowerLimits1;

    @ApiModelProperty("Active power limit side 2")
    private LimitsAttributes activePowerLimits2;

    @ApiModelProperty("Branch status")
    private String branchStatus;
}
