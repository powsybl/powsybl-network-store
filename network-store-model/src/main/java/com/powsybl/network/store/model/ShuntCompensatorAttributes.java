/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
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
@Schema(description = "Shunt compensator attributes")
public class ShuntCompensatorAttributes extends AbstractAttributes implements InjectionAttributes {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Shunt compensator name")
    private String name;

    @Schema(description = "fictitious")
    private boolean fictitious;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    @Builder.Default
    private Set<String> aliasesWithoutType = new HashSet<>();

    @Schema(description = "Alias by type")
    @Builder.Default
    private Map<String, String> aliasByType = new HashMap<>();

    @Schema(description = "Connection node in node/breaker topology")
    private Integer node;

    @Schema(description = "Connection bus in bus/breaker topology")
    private String bus;

    @Schema(description = "Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @Schema(description = "Model")
    private ShuntCompensatorModelAttributes model;

    @Schema(description = "Count of sections in service")
    private int sectionCount;

    @Schema(description = "Active power in MW")
    private double p;

    @Schema(description = "Reactive power in MW")
    private double q;

    @Schema(description = "Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @Schema(description = "regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;

    @Schema(description = "Voltage regulation status")
    private boolean voltageRegulatorOn;

    @Schema(description = "targetV")
    private double targetV;

    @Schema(description = "targetDeadband")
    private double targetDeadband;
}
