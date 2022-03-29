/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Load detail attributes")
public class LoadDetailAttributes {

    @Schema(description = "Fixed active power in MW")
    private double fixedActivePower;

    @Schema(description = "Fixed reactive power in MW")
    private double fixedReactivePower;

    @Schema(description = "Variable active power in MW")
    private double variableActivePower;

    @Schema(description = "Variable reactive power in MW")
    private double variableReactivePower;
}
