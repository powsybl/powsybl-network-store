/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Shunt compensator non linear model attributes")
public class ShuntCompensatorNonLinearModelAttributes implements ShuntCompensatorModelAttributes {

    @Schema(description = "Type of shunt compensator model")
    private final ShuntCompensatorModelType type = ShuntCompensatorModelType.NON_LINEAR;

    @Schema(description = "Sections")
    private List<ShuntCompensatorNonLinearSectionAttributes> sections;

    @JsonIgnore
    @Override
    public int getMaximumSectionCount() {
        return sections.size();
    }

    @JsonIgnore
    @Override
    public double getB(int sectionCount) {
        return sectionCount == 0 ? 0 : sections.get(sectionCount - 1).getB();
    }

    @JsonIgnore
    @Override
    public double getG(int sectionCount) {
        return sectionCount == 0 ? 0 : sections.get(sectionCount - 1).getG();
    }
}
