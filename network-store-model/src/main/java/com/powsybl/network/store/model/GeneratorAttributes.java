/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.EnergySource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generator attributes")
public class GeneratorAttributes extends AbstractAttributes implements InjectionAttributes, ReactiveLimitHolder {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Generator name")
    private String name;

    @Schema(description = "Generator fictitious")
    private boolean fictitious;

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

    @Schema(description = "Energy source")
    private EnergySource energySource;

    @Schema(description = "Minimum active power in MW")
    private double minP;

    @Schema(description = "Maximum active power in MW")
    private double maxP;

    @Schema(description = "Voltage regulation status")
    private boolean voltageRegulatorOn;

    @Schema(description = "Active power target in MW")
    private double targetP;

    @Schema(description = "Reactive power target in MVar")
    private double targetQ;

    @Schema(description = "Voltage target in kV")
    private double targetV;

    @Schema(description = "Rated apparent power in MVA")
    private double ratedS;

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

    @Schema(description = "Active power control")
    private ActivePowerControlAttributes activePowerControl;

    @Schema(description = "regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;

    @Schema(description = "Coordinated reactive power control")
    private CoordinatedReactiveControlAttributes coordinatedReactiveControl;

    @Schema(description = "Remote reactive power control attributes")
    private RemoteReactivePowerControlAttributes remoteReactivePowerControl;

    @Schema(description = "Entsoe category attributes")
    private GeneratorEntsoeCategoryAttributes entsoeCategoryAttributes;

    @Schema(description = "Generator Startup attributes attributes")
    private GeneratorStartupAttributes generatorStartupAttributes;

    @Schema(description = "Generator short circuit attributes")
    private GeneratorShortCircuitAttributes generatorShortCircuitAttributes;
}
