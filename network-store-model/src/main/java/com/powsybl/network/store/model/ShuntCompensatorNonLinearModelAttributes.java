/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("Shunt compensator non linear model attributes")
public class ShuntCompensatorNonLinearModelAttributes implements ShuntCompensatorModelAttributes {

    @ApiModelProperty("Type of shunt compensator model")
    private final ShuntCompensatorModelType type = ShuntCompensatorModelType.NON_LINEAR;

    @ApiModelProperty("Sections")
    private List<ShuntCompensatorNonLinearSectionAttributes> sections;

    @Override
    public int getMaximumSectionCount() {
        return sections.size();
    }

    @Override
    public double getB(int sectionCount) {
        if (sectionCount < 0 || sectionCount > sections.size()) {
            throw new PowsyblException("the given count of sections (" + sectionCount + ") is invalid (negative or strictly greater than the number of sections");
        }
        return sectionCount == 0 ? 0 : sections.get(sectionCount - 1).getB();
    }

    @Override
    public double getG(int sectionCount) {
        if (sectionCount < 0 || sectionCount > sections.size()) {
            throw new PowsyblException("the given count of sections (" + sectionCount + ") is invalid (negative or strictly greater than the number of sections");
        }
        return sectionCount == 0 ? 0 : sections.get(sectionCount - 1).getG();
    }
}
