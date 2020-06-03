/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.ConnectableType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Vertex")
public class Vertex {

    @ApiModelProperty("Connectable ID")
    private String id;

    @ApiModelProperty("Connectable type")
    private ConnectableType connectableType;

    @ApiModelProperty("Connection node")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer node;

    @ApiModelProperty("Connection bus")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bus;

    @ApiModelProperty("Connection side")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String side;
}
