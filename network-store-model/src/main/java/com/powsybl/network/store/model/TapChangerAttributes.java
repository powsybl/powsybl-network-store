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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TapChangerAttributes {

    @Schema(description = "lowTapPosition")
    private int lowTapPosition;

    @Schema(description = "tapPosition")
    private int tapPosition;

    @Schema(description = "regulating")
    private boolean regulating;

    @Schema(description = "targetDeadband")
    private double targetDeadband;

    @Schema(description = "regulatingTerminal")
    private TerminalRefAttributes regulatingTerminal;
}
