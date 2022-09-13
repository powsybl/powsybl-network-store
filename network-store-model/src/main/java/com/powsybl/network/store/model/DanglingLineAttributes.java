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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dangling line attributes")
public class DanglingLineAttributes extends AbstractAttributes implements InjectionAttributes, LimitSelector {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Dangling line name")
    private String name;

    @Schema(description = "fictitious")
    private boolean fictitious;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;

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

    @Schema(description = "UCTE XNode code")
    private String ucteXnodeCode;

    @Schema(description = "Current limits")
    private LimitsAttributes currentLimits;

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

    @Schema(description = "apparent power limits")
    private LimitsAttributes apparentPowerLimits;

    @Schema(description = "Active power limits")
    private LimitsAttributes activePowerLimits;

    @Override
    @JsonIgnore
    public LimitsAttributes getLimits(TemporaryLimitType type, int side) {
        if (side != 1) {
            throw new IllegalArgumentException("Unknown side for danglingline");
        }
        switch (type) {
            case CURRENT_LIMIT:
                return currentLimits;

            case APPARENT_POWER_LIMIT:
                return apparentPowerLimits;

            case ACTIVE_POWER_LIMIT:
                return activePowerLimits;

            default:
                throw new IllegalArgumentException("Unknown temporary limit type for danglingline");
        }
    }

    @Override
    @JsonIgnore
    public void setLimits(TemporaryLimitType type, int side, LimitsAttributes limits) {
        if (side != 1) {
            throw new IllegalArgumentException("Unknown side for danglingline");
        }
        switch (type) {
            case CURRENT_LIMIT:
                setCurrentLimits(limits);
                break;

            case APPARENT_POWER_LIMIT:
                setApparentPowerLimits(limits);
                break;

            case ACTIVE_POWER_LIMIT:
                setActivePowerLimits(limits);
                break;

            default:
                throw new IllegalArgumentException("Unknown temporary limit type for danglingline");
        }
    }

    @Override
    @JsonIgnore
    public String getEquipmentType() {
        return "danglingLine";
    }

    @Override
    @JsonIgnore
    public List<Integer> getSideList() {
        return List.of(1);
    }
}
