/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.network.store.model.ShuntCompensatorLinearModelAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ShuntCompensatorLinearModelImpl implements ShuntCompensatorLinearModel {

    private final ShuntCompensatorLinearModelAttributes attributes;

    public ShuntCompensatorLinearModelImpl(ShuntCompensatorLinearModelAttributes attributes) {
        this.attributes = attributes;
    }

    static ShuntCompensatorLinearModelImpl create(ShuntCompensatorLinearModelAttributes attributes) {
        return new ShuntCompensatorLinearModelImpl(attributes);
    }

    @Override
    public double getBPerSection() {
        return attributes.getBPerSection();
    }

    @Override
    public ShuntCompensatorLinearModel setBPerSection(double bPerSection) {
        attributes.setBPerSection(bPerSection);
        return this;
    }

    @Override
    public double getGPerSection() {
        return attributes.getGPerSection();
    }

    @Override
    public ShuntCompensatorLinearModel setGPerSection(double gPerSection) {
        attributes.setGPerSection(gPerSection);
        return this;
    }

    @Override
    public ShuntCompensatorLinearModel setMaximumSectionCount(int maximumSectionCount) {
        attributes.setMaximumSectionCount(maximumSectionCount);
        return this;
    }
}
