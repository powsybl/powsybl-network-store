/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Map;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Dangling line attributes")
public class DanglingLineAttributes extends AbstractAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Dangling line name")
    private String name;

    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Connection node in node/breaker topology")
    private Integer node;

    @ApiModelProperty("Constant active power in MW")
    private double p0;

    @ApiModelProperty("Constant reactive power in MW")
    private double q0;

    @ApiModelProperty("Series resistance")
    private double r;

    @ApiModelProperty("Series reactance")
    private double x;

    @ApiModelProperty("Shunt conductance in S")
    private double g;

    @ApiModelProperty("Shunt susceptance in S")
    private double b;

    @ApiModelProperty("Generation")
    private DanglingLineGenerationAttributes generation;

    @ApiModelProperty("UCTE XNode code")
    private String ucteXnodeCode;

    @ApiModelProperty("Current limits")
    private CurrentLimitsAttributes currentLimits;

    @ApiModelProperty("Active power in MW")
    @Builder.Default
    private double p = Double.NaN;

    @ApiModelProperty("Reactive power in MW")
    @Builder.Default
    private double q = Double.NaN;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @ApiModelProperty("Connection bus in bus/breaker topology")
    private String bus;

    @ApiModelProperty("Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @ApiModelProperty("activePowerControl")
    private ActivePowerControlAttributes activePowerControl;

    public DanglingLineAttributes(DanglingLineAttributes other) {
        super(other);
        this.voltageLevelId = other.voltageLevelId;
        this.name = other.name;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
        this.node = other.node;
        this.p0 = other.p0;
        this.q0 = other.q0;
        this.r = other.r;
        this.x = other.x;
        this.g = other.g;
        this.b = other.b;
        this.generation = other.generation;
        this.ucteXnodeCode = other.ucteXnodeCode;
        this.currentLimits = other.currentLimits;
        this.p = other.p;
        this.q = other.q;
        this.position = other.position;
        this.bus = other.bus;
        this.connectableBus = other.connectableBus;
        this.activePowerControl = other.activePowerControl;
    }
}
