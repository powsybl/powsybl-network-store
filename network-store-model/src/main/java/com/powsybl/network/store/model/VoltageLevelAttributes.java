/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class VoltageLevelAttributes extends AbstractAttributes implements IdentifiableAttributes<VoltageLevelAttributes>, Contained {

    @ApiModelProperty("Substation ID")
    private String substationId;

    @ApiModelProperty("Voltage level name")
    private String name;

    @ApiModelProperty("fictitious")
    private Boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Nominal voltage in kV")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double nominalV = Double.NaN;

    @ApiModelProperty("Low voltage limit in kV")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double lowVoltageLimit = Double.NaN;

    @ApiModelProperty("High voltage limit in kV")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NanFilter.class)
    private double highVoltageLimit = Double.NaN;

    @ApiModelProperty("Topology kind")
    private TopologyKind topologyKind;

    @ApiModelProperty("Internal connection of the voltage level")
    private List<InternalConnectionAttributes> internalConnections;

    @ApiModelProperty("Calculated buses")
    private List<CalculatedBusAttributes> calculatedBuses;

    @ApiModelProperty("Node to calculated bus")
    private Map<Integer, Integer> nodeToCalculatedBus;

    @ApiModelProperty("Bus to calculated bus")
    private Map<String, Integer> busToCalculatedBus;

    @Builder.Default
    @ApiModelProperty("Calculated bus validity")
    private Boolean calculatedBusesValid = Boolean.FALSE;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(substationId);
    }

    @Override
    public void initUpdatedAttributes(VoltageLevelAttributes updatedAttributes) {
        updatedAttributes.setSubstationId(substationId);
    }
}
