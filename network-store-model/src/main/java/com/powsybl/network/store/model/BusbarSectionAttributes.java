/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Busbar section attributes")
public class BusbarSectionAttributes extends AbstractIdentifiableAttributes implements Contained {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Connection node in node/breaker topology")
    private int node;

    @Schema(description = "Busbar section position (for substation diagram)")
    private BusbarSectionPositionAttributes position;

    @Schema(description = "Reference priorities")
    private ReferencePrioritiesAttributes referencePriorities;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(voltageLevelId);
    }
}
