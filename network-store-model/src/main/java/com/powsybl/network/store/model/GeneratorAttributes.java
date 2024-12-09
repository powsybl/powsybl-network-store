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
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Generator attributes")
public class GeneratorAttributes extends AbstractRegulatingEquipmentAttributes implements InjectionAttributes, ReactiveLimitHolder {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

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

    @Schema(description = "Coordinated reactive power control")
    private CoordinatedReactiveControlAttributes coordinatedReactiveControl;

    @Schema(description = "Remote reactive power control attributes")
    private RemoteReactivePowerControlAttributes remoteReactivePowerControl;

    @Schema(description = "Entsoe category attributes")
    private GeneratorEntsoeCategoryAttributes entsoeCategoryAttributes;

    @Schema(description = "Generator short circuit attributes")
    private GeneratorShortCircuitAttributes generatorShortCircuitAttributes;

    @Schema(description = "Condenser")
    private boolean condenser;

    @Builder.Default
    @Schema(description = "regulatingEquipments")
    private Map<String, ResourceType> regulatingEquipments = new HashMap<>();
}
