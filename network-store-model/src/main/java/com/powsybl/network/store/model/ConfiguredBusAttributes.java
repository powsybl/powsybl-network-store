/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ConfiguredBus attributes")
public class ConfiguredBusAttributes extends AbstractAttributes implements IdentifiableAttributes, Contained {

    @Schema(description = "Bus name")
    private String name;

    @Schema(description = "Bus fictitious")
    private boolean fictitious;

    @Schema(description = "Aliases without type")
    @Builder.Default
    private Set<String> aliasesWithoutType = new HashSet<>();

    @Schema(description = "Alias by type")
    @Builder.Default
    private Map<String, String> aliasByType = new HashMap<>();

    @Schema(description = "voltage level id")
    private String voltageLevelId;

    @Schema(description = "bus voltage magnitude in Kv")
    @Builder.Default
    private double v = Double.NaN;

    @Schema(description = "voltage angle of the bus in degree")
    @Builder.Default
    private double angle = Double.NaN;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(voltageLevelId);
    }
}
