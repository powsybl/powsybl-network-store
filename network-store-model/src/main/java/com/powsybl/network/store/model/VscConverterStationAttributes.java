/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "VSC converter station attributes")
public class VscConverterStationAttributes extends AbstractIdentifiableAttributes implements InjectionAttributes, ReactiveLimitHolder {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Connection node in node/breaker topology")
    private Integer node;

    @Schema(description = "Connection bus in bus/breaker topology")
    private String bus;

    @Schema(description = "Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @Schema(description = "Loss factor")
    @Builder.Default
    private float lossFactor = Float.NaN;

    @Schema(description = "Voltage regulator status")
    private Boolean voltageRegulatorOn;

    @Schema(description = "Reactive power set point in MVar")
    private double reactivePowerSetPoint;

    @Schema(description = "Voltage set point in Kv")
    private double voltageSetPoint;

    @Schema(description = "Reactive limits of the generator")
    private ReactiveLimitsAttributes reactiveLimits;

    @Schema(description = "Active power in MW")
    @Builder.Default
    private double p = Double.NaN;

    @Schema(description = "Reactive power in MW")
    @Builder.Default
    private double q = Double.NaN;

    @Schema(description = "Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @Schema(description = "Regulating terminal")
    private TerminalRefAttributes regulatingTerminal;
}
