/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("CalculatedBus")
public class CalculatedBusAttributes {

    @ApiModelProperty("Set of connected node/bus")
    private Set<Vertex> vertices;

    @ApiModelProperty("Connected component number")
    private Integer connectedComponentNumber;

    @ApiModelProperty("Synchronous component number")
    private Integer synchronousComponentNumber;

    @ApiModelProperty("Voltage magnitude in Kv")
    private double v;

    @ApiModelProperty("Voltage angle in °")
    private double angle;
}
