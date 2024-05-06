/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Dangling line attributes")
public class DanglingLineAttributes extends AbstractIdentifiableAttributes implements FlowsLimitsAttributes, InjectionAttributes, LimitHolder {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Connection node in node/breaker topology")
    private Integer node;

    @Schema(description = "Constant active power in MW")
    private double p0;

    @Schema(description = "Constant reactive power in MW")
    private double q0;

    @Schema(description = "Series resistance")
    private double r;

    @Schema(description = "Series reactance")
    private double x;

    @Schema(description = "Shunt conductance in S")
    private double g;

    @Schema(description = "Shunt susceptance in S")
    private double b;

    @Schema(description = "Generation")
    private DanglingLineGenerationAttributes generation;

    @Schema(description = "Pairing key")
    private String pairingKey;

    @Schema(description = "OperationalLimitGroup")
    @Builder.Default
    private Map<String, OperationalLimitsGroupAttributes> operationalLimitsGroups = new HashMap<>();

    @Schema(description = "selected OperationalLimitsGroupId")
    private String selectedOperationalLimitsGroupId;

    @Schema(description = "Active power in MW")
    @Builder.Default
    private double p = Double.NaN;

    @Schema(description = "Reactive power in MW")
    @Builder.Default
    private double q = Double.NaN;

    @Schema(description = "Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @Schema(description = "Connection bus in bus/breaker topology")
    private String bus;

    @Schema(description = "Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @Schema(description = "Tie line ID in case of a paired dangling line")
    private String tieLineId;

    @Schema(description = "Reference priorities")
    private ReferencePrioritiesAttributes referencePriorities;

    @Override
    @JsonIgnore
    public List<Integer> getSideList() {
        return List.of(1);
    }

    @Override
    public LimitsAttributes getCurrentLimits(int side, String groupId) {
        if (side == 1) {
            return getOrCreateOperationalLimitsGroup(groupId).getCurrentLimits();
        }
        throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    public LimitsAttributes getApparentPowerLimits(int side, String groupId) {
        if (side == 1) {
            return getOrCreateOperationalLimitsGroup(groupId).getApparentPowerLimits();
        }
        throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    public LimitsAttributes getActivePowerLimits(int side, String groupId) {
        if (side == 1) {
            return getOrCreateOperationalLimitsGroup(groupId).getActivePowerLimits();
        }
        throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    public void setCurrentLimits(int side, LimitsAttributes limits, String groupId) {
        if (side == 1) {
            getOrCreateOperationalLimitsGroup(groupId).setCurrentLimits(limits);
        } else {
            throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    public void setApparentPowerLimits(int side, LimitsAttributes limits, String groupId) {
        if (side == 1) {
            getOrCreateOperationalLimitsGroup(groupId).setApparentPowerLimits(limits);
        } else {
            throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    public void setActivePowerLimits(int side, LimitsAttributes limits, String groupId) {
        if (side == 1) {
            getOrCreateOperationalLimitsGroup(groupId).setActivePowerLimits(limits);
        } else {
            throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    public Map<String, OperationalLimitsGroupAttributes> getOperationalLimitsGroups(int side) {
        if (side == 1) {
            return operationalLimitsGroups;
        } else {
            throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
        }
    }
}
