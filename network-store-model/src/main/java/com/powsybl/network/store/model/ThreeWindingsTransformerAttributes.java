/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Map;
import java.util.Set;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Three windings transformer attributes")
public class ThreeWindingsTransformerAttributes extends AbstractAttributes implements IdentifiableAttributes, Contained {

    @ApiModelProperty("3 windings transformer name")
    private String name;

    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Aliases without type")
    private Set<String> aliasesWithoutType;

    @ApiModelProperty("Alias by type")
    private Map<String, String> aliasByType;

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

    @ApiModelProperty("Side 3 active power in MW")
    @Builder.Default
    private double p3 = Double.NaN;

    @ApiModelProperty("Side 3 reactive power in MVar")
    @Builder.Default
    private double q3 = Double.NaN;

    @ApiModelProperty("Side 1 leg")
    private LegAttributes leg1;

    @ApiModelProperty("Side 2 leg")
    private LegAttributes leg2;

    @ApiModelProperty("Side 3 leg")
    private LegAttributes leg3;

    @ApiModelProperty("Side 1 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position1;

    @ApiModelProperty("Side 2 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position2;

    @ApiModelProperty("Side 3 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position3;

    @ApiModelProperty("RatedU at the fictitious bus in kV")
    private double ratedU0;

    @ApiModelProperty("Phase angle clock for leg 2 and 3")
    private ThreeWindingsTransformerPhaseAngleClockAttributes phaseAngleClock;

    @ApiModelProperty("Branch status")
    private String branchStatus;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return ImmutableSet.<String>builder()
                .add(leg1.getVoltageLevelId())
                .add(leg2.getVoltageLevelId())
                .add(leg3.getVoltageLevelId())
                .build();
    }
}
