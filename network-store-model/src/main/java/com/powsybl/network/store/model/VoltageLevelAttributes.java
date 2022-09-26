/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.TopologyKind;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Voltage level attributes")
public class VoltageLevelAttributes extends AbstractAttributes implements IdentifiableAttributes, Contained {

    @Schema(description = "Substation ID")
    private String substationId;

    @Schema(description = "Voltage level name")
    private String name;

    @Schema(description = "fictitious")
    private boolean fictitious;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;

    @Schema(description = "Nominal voltage in kV")
    private double nominalV;

    @Schema(description = "Low voltage limit in kV")
    private double lowVoltageLimit;

    @Schema(description = "High voltage limit in kV")
    private double highVoltageLimit;

    @Schema(description = "Topology kind")
    private TopologyKind topologyKind;

    @Schema(description = "Internal connection of the voltage level")
    @Builder.Default
    private List<InternalConnectionAttributes> internalConnections = new ArrayList<>();

    @Schema(description = "Calculated buses for bus view")
    private List<CalculatedBusAttributes> calculatedBusesForBusView;

    @Schema(description = "Node to calculated bus for bus view")
    private Map<Integer, Integer> nodeToCalculatedBusForBusView;

    @Schema(description = "Bus to calculated bus for bus view")
    private Map<String, Integer> busToCalculatedBusForBusView;

    @Schema(description = "Calculated buses for bus breaker view")
    private List<CalculatedBusAttributes> calculatedBusesForBusBreakerView;

    @Schema(description = "Node to calculated bus for bus breaker view")
    private Map<Integer, Integer> nodeToCalculatedBusForBusBreakerView;

    @Schema(description = "Bus to calculated bus for bus breaker view")
    private Map<String, Integer> busToCalculatedBusForBusBreakerView;

    @Schema(description = "Slack terminal")
    private TerminalRefAttributes slackTerminal;

    @Schema(description = "Identifiable short circuit attributes")
    private IdentifiableShortCircuitAttributes identifiableShortCircuitAttributes;

    @Builder.Default
    @Schema(description = "Calculated buses validity")
    private boolean calculatedBusesValid = false;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(substationId);
    }
}
