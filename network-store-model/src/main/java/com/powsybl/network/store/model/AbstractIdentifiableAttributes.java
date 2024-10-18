/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractIdentifiableAttributes extends AbstractAttributes implements IdentifiableAttributes {

    @Schema(description = "Name")
    private String name;

    @Builder.Default
    @Schema(description = "Fictitious")
    private boolean fictitious = false;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;

    @Builder.Default
    @Schema(description = "Extension attributes")
    private Map<String, ExtensionAttributes> extensionAttributes = new HashMap<>();

    @Schema(description = "regulation info")
    private RegulationPointAttributes regulationPoint;

    @Builder.Default
    @Schema(description = "regulatingEquipments")
    private Map<String, ResourceType> regulatingEquipments = new HashMap<>();
}
