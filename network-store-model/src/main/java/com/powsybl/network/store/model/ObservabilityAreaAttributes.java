/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Observability area attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObservabilityAreaAttributes implements ExtensionAttributes {

    @Schema(description = "observable areas for buses of the current topology")
    @Builder.Default
    private Map<String, CharacteristicsAttributes> observabilityAreaByBusViewBus = new HashMap<>();

    @Schema(description = "observable areas for buses of the bus breaker topology")
    @Builder.Default
    private Map<String, CharacteristicsAttributes> observabilityAreaByBusBreakerViewBus = new HashMap<>();

    @Schema(description = "observable areas for nodes of the node breaker topology")
    @Builder.Default
    private Map<Integer, CharacteristicsAttributes> observabilityAreaByNodes = new HashMap<>();

    @Schema(description = "nodes by bus")
    @Builder.Default
    private List<Set<Integer>> nodesByBus = new ArrayList<>();

    @Schema(description = "bus breaker view buses by bus view bus")
    @Builder.Default
    private List<Set<String>> busBreakerViewBusesByBusViewBus = new ArrayList<>();
}
