/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.regulation.RegulationMode;
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
@Schema(description = "Voltage regulation attributes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class NetworkVoltageRegulationAttributes {
    @Builder.Default
    private double targetValue = Double.NaN;

    @Builder.Default
    private double targetDeadband = Double.NaN;

    @Builder.Default
    private double slope = Double.NaN;

    private RegulationMode mode;

    @Builder.Default
    private boolean regulating = false;

    private TerminalRefAttributes terminal;
}
