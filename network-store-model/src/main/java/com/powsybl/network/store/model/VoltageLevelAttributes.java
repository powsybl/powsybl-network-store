/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.TopologyKind;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Voltage level attributes")
public class VoltageLevelAttributes implements IdentifiableAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Substation ID")
    private String substationId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Voltage level name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Nominal voltage in kV")
    private double nominalV;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Low voltage limit in kV")
    private double lowVoltageLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("High voltage limit in kV")
    private double highVoltageLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Topology kind")
    private TopologyKind topologyKind;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Nodes count")
    private int nodeCount;

    @ApiModelProperty("Internal connection of the voltage level")
    @Builder.Default
    private List<InternalConnectionAttributes> internalConnections = new ArrayList<>();
}
