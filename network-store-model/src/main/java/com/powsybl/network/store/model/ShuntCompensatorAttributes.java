/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("Shunt compensator attributes")
public class ShuntCompensatorAttributes extends AbstractAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Shunt compensator name")
    private String name;

    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Aliases without type")
    private Set<String> aliasesWithoutType;

    @ApiModelProperty("Alias by type")
    private Map<String, String> aliasByType;

    @ApiModelProperty("Connection node in node/breaker topology")
    private Integer node;

    @ApiModelProperty("Connection bus in bus/breaker topology")
    private String bus;

    @ApiModelProperty("Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @ApiModelProperty("Model")
    private ShuntCompensatorModelAttributes model;

    @ApiModelProperty("Count of sections in service")
    private int sectionCount;

    @ApiModelProperty("Active power in MW")
    private double p;

    @ApiModelProperty("Reactive power in MW")
    private double q;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @ApiModelProperty("regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;

    @ApiModelProperty("Voltage regulation status")
    private boolean voltageRegulatorOn;

    @ApiModelProperty("targetV")
    private double targetV;

    @ApiModelProperty("targetDeadband")
    private double targetDeadband;

    @ApiModelProperty("activePowerControl")
    private ActivePowerControlAttributes activePowerControl;

    public ShuntCompensatorAttributes(ShuntCompensatorAttributes other) {
        super(other);
        this.voltageLevelId = other.voltageLevelId;
        this.name = other.name;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
        this.aliasesWithoutType = other.aliasesWithoutType;
        this.aliasByType = other.aliasByType;
        this.node = other.node;
        this.bus = other.bus;
        this.connectableBus = other.connectableBus;
        this.model = other.model;
        this.sectionCount = other.sectionCount;
        this.p = other.p;
        this.q = other.q;
        this.position = other.position;
        this.regulatingTerminal = other.regulatingTerminal;
        this.voltageRegulatorOn = other.voltageRegulatorOn;
        this.targetV = other.targetV;
        this.targetDeadband = other.targetDeadband;
        this.activePowerControl = other.activePowerControl;
    }
}
