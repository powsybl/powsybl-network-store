/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.TopologyKind;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Voltage level attributes")
public class VoltageLevelAttributes extends AbstractAttributes implements IdentifiableAttributes, Contained {

    @ApiModelProperty("Substation ID")
    private String substationId;

    @ApiModelProperty("Voltage level name")
    private String name;

    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Nominal voltage in kV")
    private double nominalV;

    @ApiModelProperty("Low voltage limit in kV")
    private double lowVoltageLimit;

    @ApiModelProperty("High voltage limit in kV")
    private double highVoltageLimit;

    @ApiModelProperty("Topology kind")
    private TopologyKind topologyKind;

    @ApiModelProperty("Internal connection of the voltage level")
    @Builder.Default
    private List<InternalConnectionAttributes> internalConnections = new ArrayList<>();

    @ApiModelProperty("Calculated buses for bus view")
    private List<CalculatedBusAttributes> calculatedBusesForBusView;

    @ApiModelProperty("Node to calculated bus for bus view")
    private Map<Integer, Integer> nodeToCalculatedBusForBusView;

    @ApiModelProperty("Bus to calculated bus for bus view")
    private Map<String, Integer> busToCalculatedBusForBusView;

    @ApiModelProperty("Calculated buses for bus breaker view")
    private List<CalculatedBusAttributes> calculatedBusesForBusBreakerView;

    @ApiModelProperty("Node to calculated bus for bus breaker view")
    private Map<Integer, Integer> nodeToCalculatedBusForBusBreakerView;

    @ApiModelProperty("Bus to calculated bus for bus breaker view")
    private Map<String, Integer> busToCalculatedBusForBusBreakerView;

    @ApiModelProperty("Slack terminal")
    private TerminalRefAttributes slackTerminal;

    @Builder.Default
    @ApiModelProperty("Calculated buses validity")
    private boolean calculatedBusesValid = false;

    public VoltageLevelAttributes(VoltageLevelAttributes other) {
        super(other);
        this.substationId = other.substationId;
        this.name = other.name;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
        this.nominalV = other.nominalV;
        this.lowVoltageLimit = other.lowVoltageLimit;
        this.highVoltageLimit = other.highVoltageLimit;
        this.topologyKind = other.topologyKind;
        this.internalConnections = other.internalConnections;
        this.calculatedBusesForBusView = other.calculatedBusesForBusView;
        this.nodeToCalculatedBusForBusView = other.nodeToCalculatedBusForBusView;
        this.busToCalculatedBusForBusView = other.busToCalculatedBusForBusView;
        this.calculatedBusesForBusBreakerView = other.calculatedBusesForBusBreakerView;
        this.nodeToCalculatedBusForBusBreakerView = other.nodeToCalculatedBusForBusBreakerView;
        this.busToCalculatedBusForBusBreakerView = other.busToCalculatedBusForBusBreakerView;
        this.calculatedBusesValid = other.calculatedBusesValid;
        this.calculatedBusesForBusView = other.calculatedBusesForBusView;
        this.slackTerminal = other.slackTerminal;
    }

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(substationId);
    }
}
