/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Area attributes")
public class AreaAttributes extends AbstractIdentifiableAttributes {
    @Schema(description = "Area Type")
    private String areaType;

    @Schema(description = "Voltage level ids")
    private Set<String> voltageLevelIds;

    @Schema(description = "area boundaries")
    private List<AreaBoundaryAttributes> areaBoundaries;

    @Schema(description = "interchange target")
    private Double interchangeTarget;

}
