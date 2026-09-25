/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Persisted attributes of the common voltage regulation API.
 *
 * <p>The regulating terminal is kept in this object for compatibility with
 * the resource format. Runtime implementations enforce the IIDM rule that
 * this terminal is not variant dependent.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Voltage regulation attributes")
public class NetworkVoltageRegulationAttributes extends AbstractAttributes implements Attributes {

    @Schema(description = "Regulation target value")
    @Builder.Default
    private double targetValue = Double.NaN;

    @Schema(description = "Regulation target deadband")
    @Builder.Default
    private double targetDeadband = Double.NaN;

    @Schema(description = "Regulation slope")
    @Builder.Default
    private double slope = Double.NaN;

    @Schema(description = "Regulation mode")
    private RegulationMode mode;

    @Schema(description = "Whether regulation is enabled")
    @Builder.Default
    private boolean regulating = true;

    @Schema(description = "Regulating terminal")
    private TerminalRefAttributes terminal;

    public VoltageRegulation.VoltageRegulationAttributes toAttributes() {
        return toAttributes(null);
    }

    public VoltageRegulation.VoltageRegulationAttributes toAttributes(Terminal terminal) {
        return new VoltageRegulation.VoltageRegulationAttributes(
                targetValue, targetDeadband, slope, mode, regulating,
                terminal);
    }
}
