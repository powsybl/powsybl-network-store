/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Static var compensator attributes")
public class StaticVarCompensatorAttributes extends AbstractRegulatingEquipmentAttributes implements InjectionAttributes, VoltageRegulationReactiveTargetAttributes {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Connection node in node/breaker topology")
    private Integer node;

    @Schema(description = "Connection bus in bus/breaker topology")
    private String bus;

    @Schema(description = "Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @Schema(description = "Minimum susceptance in S")
    private double bmin;

    @Schema(description = "Maximum susceptance in S")
    private double bmax;

    @Schema(description = "Voltage setpoint in Kv")
    private double voltageSetPoint;

    @Builder.Default
    @Schema(description = "Local voltage target in kV")
    private double localTargetV = Double.NaN;

    @Schema(description = "Reactive power setpoint in MVAR")
    private double reactivePowerSetPoint;

    @Builder.Default
    @Schema(description = "Local reactive power target in MVar")
    private double localTargetQ = Double.NaN;

    @JsonView(AttributeFilter.JsonViews.OnlySv.class)
    @Schema(description = "Active power in MW")
    @Builder.Default
    private double p = Double.NaN;

    @JsonView(AttributeFilter.JsonViews.OnlySv.class)
    @Schema(description = "Reactive power in MW")
    @Builder.Default
    private double q = Double.NaN;

    @Schema(description = "Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @Schema(description = "Voltage per reactive control")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private VoltagePerReactivePowerControlAttributes voltagePerReactiveControl;

    @Schema(description = "Standby automaton")
    private StandbyAutomatonAttributes standbyAutomaton;

    @Builder.Default
    @Schema(description = "regulatingEquipments")
    private Set<RegulatingEquipmentIdentifier> regulatingEquipments = new HashSet<>();
}
