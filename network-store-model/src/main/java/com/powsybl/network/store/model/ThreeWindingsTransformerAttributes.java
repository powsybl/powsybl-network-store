/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Three windings transformer attributes")
public class ThreeWindingsTransformerAttributes extends AbstractAttributes implements IdentifiableAttributes, Contained, TransformerAttributes, LimitHolder, TapChangerHolder {

    @Schema(description = "3 windings transformer name")
    private String name;

    @Schema(description = "fictitious")
    private boolean fictitious;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;

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

    @Schema(description = "Side 3 active power in MW")
    @Builder.Default
    private double p3 = Double.NaN;

    @Schema(description = "Side 3 reactive power in MVar")
    @Builder.Default
    private double q3 = Double.NaN;

    @Schema(description = "Side 1 leg")
    private LegAttributes leg1;

    @Schema(description = "Side 2 leg")
    private LegAttributes leg2;

    @Schema(description = "Side 3 leg")
    private LegAttributes leg3;

    @Schema(description = "Side 1 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position1;

    @Schema(description = "Side 2 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position2;

    @Schema(description = "Side 3 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position3;

    @Schema(description = "RatedU at the fictitious bus in kV")
    private double ratedU0;

    @Schema(description = "Phase angle clock for leg 2 and 3")
    private ThreeWindingsTransformerPhaseAngleClockAttributes phaseAngleClock;

    @Schema(description = "Branch status")
    private String branchStatus;

    @Schema(description = "CGMES tap changer attributes list")
    private List<CgmesTapChangerAttributes> cgmesTapChangerAttributesList;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return ImmutableSet.<String>builder()
                .add(leg1.getVoltageLevelId())
                .add(leg2.getVoltageLevelId())
                .add(leg3.getVoltageLevelId())
                .build();
    }

    @Override
    @JsonIgnore
    public List<Integer> getSideList() {
        return List.of(1, 2, 3);
    }

    @Override
    public LimitsAttributes getCurrentLimits(int side) {
        if (side == 1) {
            return leg1.getCurrentLimitsAttributes();
        }
        if (side == 2) {
            return leg2.getCurrentLimitsAttributes();
        }
        if (side == 3) {
            return leg3.getCurrentLimitsAttributes();
        }
        throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    public LimitsAttributes getApparentPowerLimits(int side) {
        if (side == 1) {
            return leg1.getApparentPowerLimitsAttributes();
        }
        if (side == 2) {
            return leg2.getApparentPowerLimitsAttributes();
        }
        if (side == 3) {
            return leg3.getApparentPowerLimitsAttributes();
        }
        throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    public LimitsAttributes getActivePowerLimits(int side) {
        if (side == 1) {
            return leg1.getActivePowerLimitsAttributes();
        }
        if (side == 2) {
            return leg2.getActivePowerLimitsAttributes();
        }
        if (side == 3) {
            return leg3.getActivePowerLimitsAttributes();
        }
        throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    public void setCurrentLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            leg1.setCurrentLimitsAttributes(limits);
        } else if (side == 2) {
            leg2.setCurrentLimitsAttributes(limits);
        } else if (side == 3) {
            leg3.setCurrentLimitsAttributes(limits);
        } else {
            throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    public void setApparentPowerLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            leg1.setApparentPowerLimitsAttributes(limits);
        } else if (side == 2) {
            leg2.setApparentPowerLimitsAttributes(limits);
        } else if (side == 3) {
            leg3.setApparentPowerLimitsAttributes(limits);
        } else {
            throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    public void setActivePowerLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            leg1.setActivePowerLimitsAttributes(limits);
        } else if (side == 2) {
            leg2.setActivePowerLimitsAttributes(limits);
        } else if (side == 3) {
            leg3.setActivePowerLimitsAttributes(limits);
        } else {
            throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @JsonIgnore
    public LegAttributes getLeg(int side) {
        if (side == 1) {
            return getLeg1();
        } else if (side == 2) {
            return getLeg2();
        } else if (side == 3) {
            return getLeg3();
        } else {
            throw new IllegalArgumentException(LimitHolder.EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    public RatioTapChangerAttributes getRatioTapChangerAttributes(int side) {
        if (side == 1) {
            return leg1.getRatioTapChangerAttributes();
        }
        if (side == 2) {
            return leg2.getRatioTapChangerAttributes();
        }
        if (side == 3) {
            return leg3.getRatioTapChangerAttributes();
        }
        throw new IllegalArgumentException(TapChangerHolder.EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    public PhaseTapChangerAttributes getPhaseTapChangerAttributes(int side) {
        if (side == 1) {
            return leg1.getPhaseTapChangerAttributes();
        }
        if (side == 2) {
            return leg2.getPhaseTapChangerAttributes();
        }
        if (side == 3) {
            return leg3.getPhaseTapChangerAttributes();
        }
        throw new IllegalArgumentException(TapChangerHolder.EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    public void setRatioTapChangerAttributes(int side, RatioTapChangerAttributes tapChanger) {
        if (side == 1) {
            leg1.setRatioTapChangerAttributes(tapChanger);
        } else if (side == 2) {
            leg2.setRatioTapChangerAttributes(tapChanger);
        } else if (side == 3) {
            leg3.setRatioTapChangerAttributes(tapChanger);
        } else {
            throw new IllegalArgumentException(TapChangerHolder.EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    public void setPhaseTapChangerAttributes(int side, PhaseTapChangerAttributes tapChanger) {
        if (side == 1) {
            leg1.setPhaseTapChangerAttributes(tapChanger);
        } else if (side == 2) {
            leg2.setPhaseTapChangerAttributes(tapChanger);
        } else if (side == 3) {
            leg3.setPhaseTapChangerAttributes(tapChanger);
        } else {
            throw new IllegalArgumentException(TapChangerHolder.EXCEPTION_UNKNOWN_SIDE);
        }
    }
}
