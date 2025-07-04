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

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TapChangerAttributes extends AbstractRegulatingEquipmentAttributes {

    @Schema(description = "lowTapPosition")
    private int lowTapPosition;

    @Schema(description = "tapPosition")
    private int tapPosition;

    @Schema(description = "solved tap position")
    private Integer solvedTapPosition;

    @Schema(description = "targetDeadband")
    private double targetDeadband;

    @Schema(description = "loadTapChangingCapabilities")
    private boolean loadTapChangingCapabilities;

    @Schema(description = "steps")
    private List<TapChangerStepAttributes> steps;
}
