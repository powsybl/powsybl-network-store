/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.EnergySource;
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
@ApiModel("Generator attributes")
public class GeneratorAttributes extends AbstractAttributes implements InjectionAttributes<GeneratorAttributes> {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Generator name")
    private String name;

    @ApiModelProperty("Generator fictitious")
    private Boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Connection node in node/breaker topology")
    private Integer node;

    @ApiModelProperty("Connection bus in bus/breaker topology")
    private String bus;

    @ApiModelProperty("Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @ApiModelProperty("Energy source")
    private EnergySource energySource;

    @ApiModelProperty("Minimum active power in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double minP = Double.NaN;

    @ApiModelProperty("Maximum active power in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double maxP = Double.NaN;

    @ApiModelProperty("Voltage regulation status")
    private Boolean voltageRegulatorOn;

    @ApiModelProperty("Active power target in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double targetP = Double.NaN;

    @ApiModelProperty("Reactive power target in MVar")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double targetQ = Double.NaN;

    @ApiModelProperty("Voltage target in kV")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double targetV = Double.NaN;

    @ApiModelProperty("Rated apparent power in MVA")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double ratedS = Double.NaN;

    @ApiModelProperty("Active power in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double p = Double.NaN;

    @ApiModelProperty("Reactive power in MW")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double q = Double.NaN;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @ApiModelProperty("reactiveLimits")
    private ReactiveLimitsAttributes reactiveLimits;

    @ApiModelProperty("Active power control")
    private ActivePowerControlAttributes activePowerControl;

    @ApiModelProperty("regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;

    @Override
    public void initUpdatedAttributes(GeneratorAttributes updatedAttributes) {
        updatedAttributes.setVoltageLevelId(voltageLevelId);
    }
}
