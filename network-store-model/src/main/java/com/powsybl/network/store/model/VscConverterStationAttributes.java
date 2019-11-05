/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("VSC converter station attributes")
public class VscConverterStationAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("VSC converter station name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Connection node in node/breaker topology")
    private int node;

    @ApiModelProperty("Loss factor")
    private float lossFactor;

    @ApiModelProperty("Voltage regulator status")
    private Boolean voltageRegulatorOn;

    @ApiModelProperty("Reactive power setpoint in MVar")
    private double reactivePowerSetpoint;

    @ApiModelProperty("Voltage setpoint in Kv")
    private double voltageSetpoint;

    @ApiModelProperty("Active power in MW")
    private double p;

    @ApiModelProperty("Reactive power in MW")
    private double q;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;
}
