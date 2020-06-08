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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
public class GeneratorAttributes extends AbstractAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Generator name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Generator fictitious")
    private boolean fictitious;

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
    private double minP;

    @ApiModelProperty("Maximum active power in MW")
    private double maxP;

    @ApiModelProperty("Voltage regulation status")
    private boolean voltageRegulatorOn;

    @ApiModelProperty("Active power target in MW")
    private double targetP;

    @ApiModelProperty("Reactive power target in MVar")
    private double targetQ;

    @ApiModelProperty("Voltage target in kV")
    private double targetV;

    @ApiModelProperty("Rated apparent power in MVA")
    private double ratedS;

    @ApiModelProperty("Active power in MW")
    private double p;

    @ApiModelProperty("Reactive power in MW")
    private double q;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @ApiModelProperty("reactiveLimits")
    private ReactiveLimitsAttributes reactiveLimits;

    @ApiModelProperty("regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;

    public GeneratorAttributes(GeneratorAttributes other) {
        super(other);
        this.voltageLevelId = other.voltageLevelId;
        this.name = other.name;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
        this.node = other.node;
        this.bus = other.bus;
        this.connectableBus = other.connectableBus;
        this.energySource = other.energySource;
        this.minP = other.minP;
        this.maxP = other.maxP;
        this.voltageRegulatorOn = other.voltageRegulatorOn;
        this.targetP = other.targetP;
        this.targetQ = other.targetQ;
        this.targetV = other.targetV;
        this.ratedS = other.ratedS;
        this.p = other.p;
        this.q = other.q;
        this.position = other.position;
        this.reactiveLimits = other.reactiveLimits;
        this.regulatingTerminal = other.regulatingTerminal;
    }
}
