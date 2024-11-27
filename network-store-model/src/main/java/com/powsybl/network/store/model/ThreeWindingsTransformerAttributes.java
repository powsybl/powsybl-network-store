/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.PowsyblException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Three windings transformer attributes")
public class ThreeWindingsTransformerAttributes extends AbstractIdentifiableAttributes implements Contained, TransformerAttributes, LimitHolder, RegulatedEquipmentAttributes {

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

    @Schema(description = "CGMES tap changer attributes list")
    private List<CgmesTapChangerAttributes> cgmesTapChangerAttributesList;

    @Builder.Default
    @Schema(description = "regulatingEquipments")
    private List<RegulatingEquipmentIdentifier> regulatingEquipments = new ArrayList<>();

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
    public LimitsAttributes getCurrentLimits(int side, String groupId) {
        return getLeg(side).getOrCreateOperationalLimitsGroup(groupId).getCurrentLimits();
    }

    @Override
    public LimitsAttributes getApparentPowerLimits(int side, String groupId) {
        return getLeg(side).getOrCreateOperationalLimitsGroup(groupId).getApparentPowerLimits();
    }

    @Override
    public LimitsAttributes getActivePowerLimits(int side, String groupId) {
        return getLeg(side).getOrCreateOperationalLimitsGroup(groupId).getActivePowerLimits();
    }

    @Override
    public void setCurrentLimits(int side, LimitsAttributes limits, String groupId) {
        getLeg(side).getOrCreateOperationalLimitsGroup(groupId).setCurrentLimits(limits);
    }

    @Override
    public void setApparentPowerLimits(int side, LimitsAttributes limits, String groupId) {
        getLeg(side).getOrCreateOperationalLimitsGroup(groupId).setApparentPowerLimits(limits);
    }

    @Override
    public void setActivePowerLimits(int side, LimitsAttributes limits, String groupId) {
        getLeg(side).getOrCreateOperationalLimitsGroup(groupId).setActivePowerLimits(limits);
    }

    @Override
    public Map<String, OperationalLimitsGroupAttributes> getOperationalLimitsGroups(int side) {
        return getLeg(side).getOperationalLimitsGroups();
    }

    @JsonIgnore
    @Override
    public Attributes filter(AttributeFilter filter) {
        if (filter != AttributeFilter.SV) {
            throw new PowsyblException("Unsupported attribute filter: " + filter);
        }
        return new ThreeWindingsTransformerSvAttributes(getP1(), getQ1(), getP2(), getQ2(), getP3(), getQ3());
    }
}
