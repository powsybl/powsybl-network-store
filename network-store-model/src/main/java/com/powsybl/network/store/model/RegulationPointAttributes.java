/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Regulation Point attributes")
public class RegulationPointAttributes extends AbstractAttributes implements Attributes {

    @Schema(description = "Regulated Equipment Id")
    private String regulatedEquipmentId;

    @Schema(description = "Resource type")
    private ResourceType resourceType;

    @Schema(description = "Local terminal")
    private TerminalRefAttributes localTerminal;

    @Schema(description = "Regulating terminal")
    private TerminalRefAttributes regulatingTerminal;

    @Schema(description = "Regulation mode ordinal")
    private String regulationMode;

    @Schema(description = "Regulated equipment resource type")
    private ResourceType regulatingResourceType;
}
