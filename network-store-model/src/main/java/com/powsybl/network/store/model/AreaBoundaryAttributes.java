/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Area boundaries attributes")
public class AreaBoundaryAttributes extends AbstractAttributes implements Attributes {

    @Schema(description = "Terminal")
    private TerminalRefAttributes terminal;

    @Schema(description = "ac")
    private Boolean ac;

    @Schema(description = "Area Id")
    private String areaId;

    @Schema(description = "Boundary Dangling Line Id")
    private String boundaryDanglingLineId;
}
