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
public class TwoWindingsTransformerAttributes extends AbstractAttributes implements BranchAttributes, TapChangerHolder, TapChangerParentAttributes, TransformerAttributes {

    @Schema(description = "Side 1 voltage level ID")
    private String voltageLevelId1;

    @Schema(description = "Side 2 voltage level ID")
    private String voltageLevelId2;

    @Schema(description = "2 windings transformer name")
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

    @Schema(description = "Current limits side 1")
    private LimitsAttributes currentLimits1;

    @Schema(description = "Current limits side 2")
    private LimitsAttributes currentLimits2;

    @Schema(description = "Phase angle clock")
    private TwoWindingsTransformerPhaseAngleClockAttributes phaseAngleClockAttributes;

    @Schema(description = "Apparent power limit side 1")
    private LimitsAttributes apparentPowerLimits1;

    @Schema(description = "Apparent power limit side 2")
    private LimitsAttributes apparentPowerLimits2;

    @Schema(description = "Active power limit side 1")
    private LimitsAttributes activePowerLimits1;

    @Schema(description = "Active power limit side 2")
    private LimitsAttributes activePowerLimits2;

    @Schema(description = "Branch status")
    private String branchStatus;

    @Schema(description = "CGMES tap changer attributes list")
    private List<CgmesTapChangerAttributes> cgmesTapChangerAttributesList;

    @Override
    public RatioTapChangerAttributes getRatioTapChangerAttributes(int side) {
        if (side != 0) {
            throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
        }
        return getRatioTapChangerAttributes();
    }

    @Override
    public PhaseTapChangerAttributes getPhaseTapChangerAttributes(int side) {
        if (side != 0) {
            throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
        }
        return getPhaseTapChangerAttributes();
    }

    @Override
    public void setRatioTapChangerAttributes(int side, RatioTapChangerAttributes tapChanger) {
        if (side != 0) {
            throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
        }
        setRatioTapChangerAttributes(tapChanger);
    }

    @Override
    public void setPhaseTapChangerAttributes(int side, PhaseTapChangerAttributes tapChanger) {
        if (side != 0) {
            throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
        }
        setPhaseTapChangerAttributes(tapChanger);
    }
}
