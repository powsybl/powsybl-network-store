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
@ApiModel("Shunt compensator attributes")
public class ShuntCompensatorAttributes extends AbstractAttributes implements InjectionAttributes<ShuntCompensatorAttributes> {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Shunt compensator name")
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

    @ApiModelProperty("Susceptance per section in S")
    private double bPerSection;

    @ApiModelProperty("Maximum number of section")
    private int maximumSectionCount;

    @ApiModelProperty("Current number of section")
    private int currentSectionCount;

    @ApiModelProperty("Active power in MW")
    private double p;

    @ApiModelProperty("Reactive power in MW")
    private double q;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @ApiModelProperty("regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;
}
