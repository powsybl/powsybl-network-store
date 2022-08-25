/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Temporary limit attributes")
public class TemporaryLimitAttributes extends AbstractAttributes implements IdentifiableAttributes {

    // TODO Maybe use an auto increment ID ?

    @Schema(description = "Equipment ID corresponding to this Temporary limit", required = true)
    private String equipmentId;

    @Schema(description = "Equipment type corresponding to this Temporary limit")
    private String equipmentType;

    @Schema(description = "Temporary limit name")
    private String name;

    @Schema(description = "Temporary limit is fictitious")
    private boolean fictitious;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;

    @Schema(description = "Temporary limit side", required = true)
    //private Branch.Side side; // TODO CHARLY [Maybe?] utiliser Branch.Side à la place d'un integer
    private Integer side;

    @Schema(description = "Temporary limit type")
    private TemporaryLimitType limitType;

    @Schema(description = "Temporary limit value")
    private double value;

    @Schema(description = "Temporary limit acceptable duration", required = true)
    private Integer acceptableDuration;
}
