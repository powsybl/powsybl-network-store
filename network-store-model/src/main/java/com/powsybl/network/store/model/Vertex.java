/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.ConnectableType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Vertex")
public class Vertex {

    @Schema(description = "Connectable ID")
    private String id;

    @Schema(description = "Connectable type")
    private ConnectableType connectableType;

    @Schema(description = "Connection node")
    private Integer node;

    @Schema(description = "Connection bus")
    private String bus;

    @Schema(description = "Connection side")
    private String side;
}
