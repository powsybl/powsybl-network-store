/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Line attributes")
public class LineAttributes extends AbstractIdentifiableAttributes implements BranchAttributes {

    @Schema(description = "Side 1 voltage level ID")
    private String voltageLevelId1;

    @Schema(description = "Side 2 voltage level ID")
    private String voltageLevelId2;

    @Schema(description = "Side 1 connection node in node/breaker topology")
    private Integer node1;

    @Schema(description = "Side 2 connection node in node/breaker topology")
    private Integer node2;

    @Schema(description = "Side 1 connection bus in bus/breaker topology")
    private String bus1;

    @Schema(description = "Side 2 connection bus in bus/breaker topology")
    private String bus2;

    @Schema(description = "Side 1 possible connection bus in bus/breaker topology")
    private String connectableBus1;

    @Schema(description = "Side 2 possible connection bus in bus/breaker topology")
    private String connectableBus2;

    @Schema(description = "Resistance in Ohm")
    private double r;

    @Schema(description = "Reactance in Ohm")
    private double x;

    @Schema(description = "Side 1 half conductance in Siemens")
    private double g1;

    @Schema(description = "Side 1 half susceptance in Siemens")
    private double b1;

    @Schema(description = "Side 2 half conductance in Siemens")
    private double g2;

    @Schema(description = "Side 2 half susceptance in Siemens")
    private double b2;

    @Schema(description = "Side 1 active power in MW")
    @Builder.Default
    private double p1 = Double.NaN;

    @Schema(description = "Side 1 reactive power in MVar")
    @Builder.Default
    private double q1 = Double.NaN;

    @Schema(description = "Side 2 active power in MW")
    @Builder.Default
    private double p2 = Double.NaN;

    @Schema(description = "Side 2 reactive power in MVar")
    @Builder.Default
    private double q2 = Double.NaN;

    @Schema(description = "Side 1 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position1;

    @Schema(description = "Side 2 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position2;

    @Schema(description = "mergedXnode extension for tie lines")
    private MergedXnodeAttributes mergedXnode;

    @Schema(description = "OperationalLimitsGroup1")
    @Builder.Default
    private Map<String, OperationalLimitsGroupAttributes> operationalLimitsGroups1 = new HashMap<>();

    @Schema(description = "selected OperationalLimitsGroupId1")
    private String selectedOperationalLimitsGroupId1;

    @Schema(description = "OperationalLimitsGroup2")
    @Builder.Default
    private Map<String, OperationalLimitsGroupAttributes> operationalLimitsGroups2 = new HashMap<>();

    @Schema(description = "selected OperationalLimitsGroupId2")
    private String selectedOperationalLimitsGroupId2;

    @Schema(description = "Reference priorities")
    private ReferencePrioritiesAttributes referencePriorities;
}
