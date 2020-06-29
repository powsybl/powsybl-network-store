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
public class VscConverterStationAttributes extends AbstractAttributes implements InjectionAttributes<VscConverterStationAttributes> {

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
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private float lossFactor = Float.NaN;

    @ApiModelProperty("Voltage regulator status")
    private Boolean voltageRegulatorOn;

    @ApiModelProperty("Reactive power set point in MVar")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double reactivePowerSetPoint = Double.NaN;

    @ApiModelProperty("Voltage set point in Kv")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double voltageSetPoint = Double.NaN;

    @ApiModelProperty("Reactive limits of the generator")
    private ReactiveLimitsAttributes reactiveLimits;

    @ApiModelProperty("Active power in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double p = Double.NaN;

    @ApiModelProperty("Reactive power in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double q = Double.NaN;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @Override
    public void initUpdatedAttributes(VscConverterStationAttributes updatedAttributes) {
        updatedAttributes.setVoltageLevelId(voltageLevelId);
    }
}
