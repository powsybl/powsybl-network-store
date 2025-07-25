/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.extensions.WindingConnectionType;
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
@Schema(description = "TwoWindingsTransformer Fortescue Attributes")
public class TwoWindingsTransformerFortescueAttributes implements ExtensionAttributes {

    @Schema(description = "rz")
    private double rz;

    @Schema(description = "xz")
    private double xz;

    @Schema(description = "free fluxes")
    private boolean freeFluxes;

    @Schema(description = "connection type on side 2")
    private WindingConnectionType connectionType1;

    @Schema(description = "connection type on side 2")
    private WindingConnectionType connectionType2;

    @Schema(description = "grounding r on side 1")
    private double groundingR1;

    @Schema(description = "grounding x on side 1")
    private double groundingX1;

    @Schema(description = "grounding r on side 2")
    private double groundingR2;

    @Schema(description = "grounding x on side 2")
    private double groundingX2;
}
