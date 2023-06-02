/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Battery attributes")
public class BatteryAttributes extends AbstractAttributes implements InjectionAttributes, ReactiveLimitHolder {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Battery name")
    private String name;

    @Builder.Default
    @Schema(description = "Battery fictitious")
    private boolean fictitious = false;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;

    @Schema(description = "Connection node in node/breaker topology")
    private Integer node;

    @Schema(description = "Connection bus in bus/breaker topology")
    private String bus;

    @Schema(description = "Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @Schema(description = "Constant active power target in MW")
    private double targetP;

    @Schema(description = "Constant reactive power target in MVar")
    private double targetQ;

    @Schema(description = "Minimum active power in MW")
    private double minP;

    @Schema(description = "Maximum active power in MW")
    private double maxP;

    @Schema(description = "Active power in MW")
    @Builder.Default
    private double p = Double.NaN;

    @Schema(description = "Reactive power in MW")
    @Builder.Default
    private double q = Double.NaN;

    @Schema(description = "Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @Schema(description = "reactiveLimits")
    private ReactiveLimitsAttributes reactiveLimits;

    @Schema(description = "activePowerControl")
    private ActivePowerControlAttributes activePowerControl;

}
