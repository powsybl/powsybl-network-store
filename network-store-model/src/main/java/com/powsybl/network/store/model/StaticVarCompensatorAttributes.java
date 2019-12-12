/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.StaticVarCompensator;
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
@ApiModel("Static var compensator attributes")
public class StaticVarCompensatorAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Static var compensator name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Connection node in node/breaker topology")
    private int node;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Minimum susceptance in S")
    private double bmin;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Maximum susceptance in S")
    private double bmax;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Voltage setpoint in Kv")
    private double voltageSetPoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Reactive power setpoint in MVAR")
    private double reactivePowerSetPoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Regulating mode")
    private StaticVarCompensator.RegulationMode regulationMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Active power in MW")
    private double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Reactive power in MW")
    private double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;
}
