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
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("LCC converter station attributes")
public class LccConverterStationAttributes extends AbstractAttributes implements InjectionAttributes<LccConverterStationAttributes> {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("LCC converter station name")
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

    @ApiModelProperty("Power factor")
    private float powerFactor;

    @ApiModelProperty("Loss factor")
    private float lossFactor;

    @ApiModelProperty("Active power in MW")
    private double p;

    @ApiModelProperty("Reactive power in MW")
    private double q;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @Override
    public void initUpdatedAttributes(LccConverterStationAttributes updatedAttributes) {
        updatedAttributes.setVoltageLevelId(voltageLevelId);
    }
}
