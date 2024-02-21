/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "2 windings transformer attributes")
public class TwoWindingsTransformerAttributes extends AbstractAttributes implements BranchAttributes, TapChangerParentAttributes, TransformerAttributes {

    @Schema(description = "Side 1 voltage level ID")
    private String voltageLevelId1;

    @Schema(description = "Side 2 voltage level ID")
    private String voltageLevelId2;

    @Schema(description = "2 windings transformer name")
    private String name;

    @Builder.Default
    @Schema(description = "fictitious")
    private boolean fictitious = false;

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

    @Schema(description = "Conductance in Siemens")
    private double g;

    @Schema(description = "Susceptance in Siemens")
    private double b;

    @Schema(description = "Side 1 rated voltage in Kv")
    private double ratedU1;

    @Schema(description = "Side 2 rated voltage in Kv")
    private double ratedU2;

    @Schema(description = "Rated conductance in Siemens")
    private double ratedS;

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

    @Schema(description = "Phase tap changer")
    private PhaseTapChangerAttributes phaseTapChangerAttributes;

    @Schema(description = "Ratio tap changer")
    private RatioTapChangerAttributes ratioTapChangerAttributes;

    @Schema(description = "Phase angle clock")
    private TwoWindingsTransformerPhaseAngleClockAttributes phaseAngleClockAttributes;

    @Schema(description = "OperationalLimitGroup1")
    private List<OperationalLimitGroupAttributes> operationalLimitsGroups1;

    @Schema(description = "selected OperationalLimitGroupId1")
    private String selectedOperationalLimitsGroupId1;

    @Schema(description = "OperationalLimitGroup2")
    private List<OperationalLimitGroupAttributes> operationalLimitsGroups2;

    @Schema(description = "selected OperationalLimitGroupId2")
    private String selectedOperationalLimitsGroupId2;

    @Schema(description = "Operating status")
    private String operatingStatus;

    @Schema(description = "CGMES tap changer attributes list")
    private List<CgmesTapChangerAttributes> cgmesTapChangerAttributesList;
}
