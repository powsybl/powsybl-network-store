/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.StaticVarCompensator;
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
@ApiModel("Static var compensator attributes")
public class StaticVarCompensatorAttributes extends AbstractAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Static var compensator name")
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

    @ApiModelProperty("Minimum susceptance in S")
    private double bmin;

    @ApiModelProperty("Maximum susceptance in S")
    private double bmax;

    @ApiModelProperty("Voltage setpoint in Kv")
    private double voltageSetPoint;

    @ApiModelProperty("Reactive power setpoint in MVAR")
    private double reactivePowerSetPoint;

    @ApiModelProperty("Regulating mode")
    private StaticVarCompensator.RegulationMode regulationMode;

    @ApiModelProperty("Active power in MW")
    private double p;

    @ApiModelProperty("Reactive power in MW")
    private double q;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @ApiModelProperty("terminalRef")
    private TerminalRefAttributes regulatingTerminal;

    @ApiModelProperty("Voltage per reactive control")
    private VoltagePerReactivePowerControlAttributes voltagePerReactiveControl;

    public StaticVarCompensatorAttributes(StaticVarCompensatorAttributes other) {
        super(other);
        this.voltageLevelId = other.voltageLevelId;
        this.name = other.name;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
        this.node = other.node;
        this.bus = other.bus;
        this.connectableBus = other.connectableBus;
        this.bmin = other.bmin;
        this.bmax = other.bmax;
        this.voltageSetPoint = other.voltageSetPoint;
        this.reactivePowerSetPoint = other.reactivePowerSetPoint;
        this.regulationMode = other.regulationMode;
        this.p = other.p;
        this.q = other.q;
        this.position = other.position;
        this.regulatingTerminal = other.regulatingTerminal;
        voltagePerReactiveControl = other.voltagePerReactiveControl;
    }
}
