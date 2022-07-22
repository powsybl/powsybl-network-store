/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Line attributes")
public class LineAttributes extends AbstractAttributes implements BranchAttributes {

    @Schema(description = "Side 1 voltage level ID")
    private String voltageLevelId1;

    @Schema(description = "Side 2 voltage level ID")
    private String voltageLevelId2;

    @Schema(description = "Line name")
    private String name;

    @Schema(description = "fictitious")
    private boolean fictitious;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;

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

    @Schema(description = "Current permanent limit side 1")
    private double currentPermanentLimit1;

    @Schema(description = "Current permanent limit side 2")
    private double currentPermanentLimit2;

    @Schema(description = "Apparent power permanent limit side 1")
    private double apparentPowerPermanentLimit1;

    @Schema(description = "Apparent power permanent limit side 2")
    private double apparentPowerPermanentLimit2;

    @Schema(description = "Active power permanent limit side 1")
    private double activePowerPermanentLimit1;

    @Schema(description = "Active power permanent limit side 2")
    private double activePowerPermanentLimit2;

    @Schema(description = "Branch status")
    private String branchStatus;
}
