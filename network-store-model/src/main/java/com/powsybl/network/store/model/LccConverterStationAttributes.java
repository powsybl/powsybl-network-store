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
import lombok.ToString;

import java.util.Map;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("LCC converter station attributes")
public class LccConverterStationAttributes extends AbstractAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("LCC converter station name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Connection node in node/breaker topology")
    private Integer node;

    @ApiModelProperty("Connection bus in bus/breaker topology")
    private String bus;

    @ApiModelProperty("Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Power factor")
    private float powerFactor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Loss factor")
    private float lossFactor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Active power in MW")
    private double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Reactive power in MW")
    private double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    public LccConverterStationAttributes(LccConverterStationAttributes other) {
        super(other);
        this.voltageLevelId = other.voltageLevelId;
        this.name = other.name;
        this.properties = other.properties;
        this.node = other.node;
        this.bus = other.bus;
        this.connectableBus = other.connectableBus;
        this.powerFactor = other.powerFactor;
        this.lossFactor = other.lossFactor;
        this.p = other.p;
        this.q = other.q;
        this.position = other.position;
    }
}
