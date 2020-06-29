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
@ApiModel("Static var compensator attributes")
public class StaticVarCompensatorAttributes extends AbstractAttributes implements InjectionAttributes<StaticVarCompensatorAttributes> {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Static var compensator name")
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

    @ApiModelProperty("Minimum susceptance in S")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double bmin = Double.NaN;

    @ApiModelProperty("Maximum susceptance in S")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double bmax = Double.NaN;

    @ApiModelProperty("Voltage setpoint in Kv")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double voltageSetPoint = Double.NaN;

    @ApiModelProperty("Reactive power setpoint in MVAR")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double reactivePowerSetPoint = Double.NaN;

    @ApiModelProperty("Regulating mode")
    private StaticVarCompensator.RegulationMode regulationMode;

    @ApiModelProperty("Active power in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double p = Double.NaN;

    @ApiModelProperty("Reactive power in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double q = Double.NaN;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @ApiModelProperty("terminalRef")
    private TerminalRefAttributes regulatingTerminal;

    @Override
    public void initUpdatedAttributes(StaticVarCompensatorAttributes updatedAttributes) {
        updatedAttributes.setVoltageLevelId(voltageLevelId);
    }
}
