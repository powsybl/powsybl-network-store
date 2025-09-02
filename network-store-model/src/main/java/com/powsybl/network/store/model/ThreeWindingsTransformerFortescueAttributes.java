/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ThreeWindingsTransformer Fortescue Attributes")
public class ThreeWindingsTransformerFortescueAttributes implements ExtensionAttributes {

    @Schema(description = "leg1")
    private LegFortescueAttributes leg1;

    @Schema(description = "leg2")
    private LegFortescueAttributes leg2;

    @Schema(description = "leg3")
    private LegFortescueAttributes leg3;
}
