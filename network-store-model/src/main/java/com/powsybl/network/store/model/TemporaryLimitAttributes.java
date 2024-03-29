/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.LimitType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Temporary limit attributes")
public class TemporaryLimitAttributes implements Comparable<TemporaryLimitAttributes> {

    @JsonIgnore
    @Schema(description = "Temporary limit side", required = true)
    private Integer side;

    @JsonIgnore
    @Schema(description = "Temporary limit type", required = true)
    private LimitType limitType;

    @JsonIgnore
    @Schema(description = "Temporary limit group Id")
    private String operationalLimitsGroupId;

    @Schema(description = "Temporary limit name")
    private String name;

    @Schema(description = "Temporary limit value")
    private double value;

    @Schema(description = "Temporary limit acceptable duration", required = true)
    private Integer acceptableDuration;

    @Schema(description = "Temporary limit is fictitious")
    private boolean fictitious;

    @Override
    // descending order on acceptableDuration
    public int compareTo(TemporaryLimitAttributes other) {
        return other.getAcceptableDuration() - this.getAcceptableDuration();
    }
}
