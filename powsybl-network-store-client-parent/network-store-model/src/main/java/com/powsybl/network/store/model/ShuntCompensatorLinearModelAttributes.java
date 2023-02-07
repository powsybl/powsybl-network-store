/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.ShuntCompensatorModelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Shunt compensator linear model attributes")
public class ShuntCompensatorLinearModelAttributes implements ShuntCompensatorModelAttributes {

    @Schema(description = "Type of shunt compensator model")
    private final ShuntCompensatorModelType type = ShuntCompensatorModelType.LINEAR;

    @Schema(description = "Susceptance per section in S")
    private double bPerSection;

    @Schema(description = "Conductance per section in S")
    private double gPerSection;

    @Schema(description = "Maximum number of section")
    private int maximumSectionCount;

    @Override
    public int getMaximumSectionCount() {
        return maximumSectionCount;
    }

    @Override
    public double getB(int sectionCount) {
        return bPerSection * sectionCount;
    }

    @Override
    public double getG(int sectionCount) {
        return Double.isNaN(gPerSection) ? 0 : gPerSection * sectionCount;
    }
}
