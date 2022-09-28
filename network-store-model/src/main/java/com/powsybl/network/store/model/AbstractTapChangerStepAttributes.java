/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sylvain Bouzols <sylvain.bouzols at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractTapChangerStepAttributes {

    @Schema(description = "rho")
    private double rho;

    @Schema(description = "r")
    private double r;

    @Schema(description = "x")
    private double x;

    @Schema(description = "g")
    private double g;

    @Schema(description = "b")
    private double b;

    // TODO MOVE THOSE side and type somewhere else (TapChangerAttributes ?)
    @JsonIgnore
    private Integer index;

    @JsonIgnore
    private Integer side;

    @JsonIgnore
    private TapChangerType type;
}
