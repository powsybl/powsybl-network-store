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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("VSC converter station attributes")
public class VscConverterStationAttributes extends AbstractAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("VSC converter station name")
    private String name;

    @ApiModelProperty("fictitious")
    private Boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Connection node in node/breaker topology")
    private Integer node;

    @ApiModelProperty("Connection bus in bus/breaker topology")
    private String bus;

    @ApiModelProperty("Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @ApiModelProperty("Loss factor")
    private float lossFactor;

    @ApiModelProperty("Voltage regulator status")
    private Boolean voltageRegulatorOn;

    @ApiModelProperty("Reactive power set point in MVar")
    private double reactivePowerSetPoint;

    @ApiModelProperty("Voltage set point in Kv")
    private double voltageSetPoint;

    @ApiModelProperty("Reactive limits of the generator")
    private ReactiveLimitsAttributes reactiveLimits;

    @ApiModelProperty("Active power in MW")
    private double p;

    @ApiModelProperty("Reactive power in MW")
    private double q;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    public VscConverterStationAttributes(VscConverterStationAttributes other) {
        super(other);
        this.voltageLevelId = other.voltageLevelId;
        this.name = other.name;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
        this.node = other.node;
        this.bus = other.bus;
        this.connectableBus = other.connectableBus;
        this.lossFactor = other.lossFactor;
        this.voltageRegulatorOn = other.voltageRegulatorOn;
        this.reactivePowerSetPoint = other.reactivePowerSetPoint;
        this.voltageSetPoint = other.voltageSetPoint;
        this.reactiveLimits = other.reactiveLimits;
        this.p = other.p;
        this.q = other.q;
        this.position = other.position;
    }
}
