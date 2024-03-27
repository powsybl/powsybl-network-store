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
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.Set;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "ConfiguredBus attributes")
public class ConfiguredBusAttributes extends AbstractIdentifiableAttributes implements Contained {

    @Schema(description = "voltage level id")
    private String voltageLevelId;

    @Schema(description = "bus voltage magnitude in Kv")
    @Builder.Default
    private double v = Double.NaN;

    @Schema(description = "voltage angle of the bus in degree")
    @Builder.Default
    private double angle = Double.NaN;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(voltageLevelId);
    }
}
