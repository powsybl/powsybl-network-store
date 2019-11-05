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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Busbar section attributes")
public class BusbarSectionAttributes implements IdentifiableAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Busbar section name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Connection node in node/breaker topology")
    private int node;

    @ApiModelProperty("Busbar section position (for substation diagram)")
    private BusbarSectionPositionAttributes position;
}
