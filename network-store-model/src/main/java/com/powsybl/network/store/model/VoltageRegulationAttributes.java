/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Persisted attributes of a {@link com.powsybl.iidm.network.regulation.VoltageRegulation}.
 *
 * <p>The regulating terminal, regulation mode, regulating status and regulation references
 * are inherited from {@link RegulatingPointAttributes}. The regulation mode is stored as the
 * name of a {@link com.powsybl.iidm.network.regulation.RegulationMode} enum value and may be
 * null when no mode is defined for the current variant.
 *
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Voltage regulation attributes")
public class VoltageRegulationAttributes extends RegulatingPointAttributes {

    @Schema(description = "Target value of the regulation (in kV for voltage regulation, in MVar for reactive power regulation, load sign convention)")
    private double targetValue;

    @Schema(description = "Deadband in kV used to avoid excessive update of discrete control while regulating")
    private double targetDeadband;

    @Schema(description = "Slope of the regulation curve (lambda for voltage per reactive power regulation)")
    private double slope;
}
