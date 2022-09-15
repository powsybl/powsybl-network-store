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
public class ThreeWindingsTransformerAttributes extends AbstractAttributes implements IdentifiableAttributes, Contained, TransformerAttributes, LimitSelector {

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
    public ResourceType getEquipmentType() {
        return ResourceType.THREE_WINDINGS_TRANSFORMER;
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
        return leg3.getCurrentLimitsAttributes();
    }

    @Override
    public LimitsAttributes getApparentPowerLimits(int side) {
        if (side == 1) {
            return leg1.getApparentPowerLimitsAttributes();
        }
        if (side == 2) {
            return leg2.getApparentPowerLimitsAttributes();
        }
        return leg3.getApparentPowerLimitsAttributes();
    }

    @Override
    public LimitsAttributes getActivePowerLimits(int side) {
        if (side == 1) {
            return leg1.getActivePowerLimitsAttributes();
        }
        if (side == 2) {
            return leg2.getActivePowerLimitsAttributes();
        }
        return leg3.getActivePowerLimitsAttributes();
    }

    @Override
    public void setCurrentLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            leg1.setCurrentLimitsAttributes(limits);
        } else if (side == 2) {
            leg2.setCurrentLimitsAttributes(limits);
        } else {
            leg3.setCurrentLimitsAttributes(limits);
        }
    }

    @Override
    public void setApparentPowerLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            leg1.setApparentPowerLimitsAttributes(limits);
        } else if (side == 2) {
            leg2.setApparentPowerLimitsAttributes(limits);
        } else {
            leg3.setApparentPowerLimitsAttributes(limits);
        }
    }

    @Override
    public void setActivePowerLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            leg1.setActivePowerLimitsAttributes(limits);
        } else if (side == 2) {
            leg2.setActivePowerLimitsAttributes(limits);
        } else {
            leg3.setActivePowerLimitsAttributes(limits);
        }
    }
}
