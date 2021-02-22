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
@ApiModel("2 windings transformer attributes")
public class TwoWindingsTransformerAttributes extends AbstractAttributes implements BranchAttributes, TapChangerParentAttributes {

    @ApiModelProperty("Side 1 voltage level ID")
    private String voltageLevelId1;

    @ApiModelProperty("Side 2 voltage level ID")
    private String voltageLevelId2;

    @ApiModelProperty("2 windings transformer name")
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

    @ApiModelProperty("Conductance in Siemens")
    private double g;

    @ApiModelProperty("Susceptance in Siemens")
    private double b;

    @ApiModelProperty("Side 1 rated voltage in Kv")
    private double ratedU1;

    @ApiModelProperty("Side 2 rated voltage in Kv")
    private double ratedU2;

    @ApiModelProperty("Rated conductance in Siemens")
    private double ratedS;

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

    @ApiModelProperty("Phase tap changer")
    private PhaseTapChangerAttributes phaseTapChangerAttributes;

    @ApiModelProperty("Ratio tap changer")
    private RatioTapChangerAttributes ratioTapChangerAttributes;

    @ApiModelProperty("Current limits side 1")
    private CurrentLimitsAttributes currentLimits1;

    @ApiModelProperty("Current limits side 2")
    private CurrentLimitsAttributes currentLimits2;

    @ApiModelProperty("Phase angle clock")
    private TwoWindingsTransformerPhaseAngleClockAttributes phaseAngleClockAttributes;

    public TwoWindingsTransformerAttributes(TwoWindingsTransformerAttributes other) {
        super(other);
        this.voltageLevelId1 = other.voltageLevelId1;
        this.voltageLevelId2 = other.voltageLevelId2;
        this.fictitious = other.fictitious;
        this.name = other.name;
        this.properties = other.properties;
        this.aliasesWithoutType = other.aliasesWithoutType;
        this.aliasByType = other.aliasByType;
        this.node1 = other.node1;
        this.node2 = other.node2;
        this.bus1 = other.bus1;
        this.bus2 = other.bus2;
        this.connectableBus1 = other.connectableBus1;
        this.connectableBus2 = other.connectableBus2;
        this.r = other.r;
        this.x = other.x;
        this.g = other.g;
        this.b = other.b;
        this.ratedU1 = other.ratedU1;
        this.ratedU2 = other.ratedU2;
        this.ratedS = other.ratedS;
        this.p1 = other.p1;
        this.q1 = other.q1;
        this.p2 = other.p2;
        this.q2 = other.q2;
        this.position1 = other.position1;
        this.position2 = other.position2;
        this.phaseTapChangerAttributes = other.phaseTapChangerAttributes;
        this.ratioTapChangerAttributes = other.ratioTapChangerAttributes;
        this.currentLimits1 = other.currentLimits1;
        this.currentLimits2 = other.currentLimits2;
        this.phaseAngleClockAttributes = other.phaseAngleClockAttributes;
    }
}
