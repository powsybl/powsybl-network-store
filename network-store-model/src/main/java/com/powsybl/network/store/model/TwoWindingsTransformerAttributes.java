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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("2 windings transformer attributes")
public class TwoWindingsTransformerAttributes extends AbstractAttributes implements BranchAttributes<TwoWindingsTransformerAttributes>, TapChangerParentAttributes {

    @ApiModelProperty("Side 1 voltage level ID")
    private String voltageLevelId1;

    @ApiModelProperty("Side 2 voltage level ID")
    private String voltageLevelId2;

    @ApiModelProperty("2 windings transformer name")
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
    private double r;

    @ApiModelProperty("Reactance in Ohm")
    private double x;

    @ApiModelProperty("Conductance in Siemens")
    private double g;

    @ApiModelProperty("Susceptance in Siemens")
    private double b;

    @ApiModelProperty("Side 1 rated voltage in Kv")
    private double ratedU1;

    @ApiModelProperty("Side 2 rated voltage in Kv")
    private double ratedU2;

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

    @ApiModelProperty("Phase tap changer")
    private PhaseTapChangerAttributes phaseTapChangerAttributes;

    @ApiModelProperty("Ratio tap changer")
    private RatioTapChangerAttributes ratioTapChangerAttributes;

    @ApiModelProperty("Current limits side 1")
    private CurrentLimitsAttributes currentLimits1;

    @ApiModelProperty("Current limits side 2")
    private CurrentLimitsAttributes currentLimits2;
}
