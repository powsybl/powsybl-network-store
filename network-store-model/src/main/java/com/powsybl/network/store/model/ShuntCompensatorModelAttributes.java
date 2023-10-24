/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.powsybl.iidm.network.ShuntCompensatorModelType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ShuntCompensatorLinearModelAttributes.class, name = "LinearModel"),
    @JsonSubTypes.Type(value = ShuntCompensatorNonLinearModelAttributes.class, name = "NonLinearModel")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ShuntCompensatorModelAttributes {

    ShuntCompensatorModelType getType();

    int getMaximumSectionCount();

    double getB(int sectionCount);

    double getG(int sectionCount);
}
