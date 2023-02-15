/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.ShuntCompensatorLinearModelAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ShuntCompensatorLinearModelImpl implements ShuntCompensatorLinearModel {

    private final ShuntCompensatorImpl shuntCompensator;

    private final ShuntCompensatorLinearModelAttributes attributes;

    public ShuntCompensatorLinearModelImpl(ShuntCompensatorImpl shuntCompensator, ShuntCompensatorLinearModelAttributes attributes) {
        this.shuntCompensator = shuntCompensator;
        this.attributes = attributes;
    }

    @Override
    public double getBPerSection() {
        return attributes.getBPerSection();
    }

    @Override
    public ShuntCompensatorLinearModel setBPerSection(double bPerSection) {
        ValidationUtil.checkBPerSection(shuntCompensator, bPerSection);
        double oldValue = attributes.getBPerSection();
        attributes.setBPerSection(bPerSection);
        shuntCompensator.updateResource();
        shuntCompensator.notifyUpdate("bPerSection", oldValue, bPerSection);
        return this;
    }

    @Override
    public double getGPerSection() {
        return attributes.getGPerSection();
    }

    @Override
    public ShuntCompensatorLinearModel setGPerSection(double gPerSection) {
        double oldValue = attributes.getGPerSection();
        attributes.setGPerSection(gPerSection);
        shuntCompensator.updateResource();
        shuntCompensator.notifyUpdate("gPerSection", oldValue, gPerSection);
        return this;
    }

    @Override
    public ShuntCompensatorLinearModel setMaximumSectionCount(int maximumSectionCount) {
        ValidationUtil.checkSections(shuntCompensator, shuntCompensator.getSectionCount(), maximumSectionCount, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        int oldValue = attributes.getMaximumSectionCount();
        attributes.setMaximumSectionCount(maximumSectionCount);
        shuntCompensator.updateResource();
        shuntCompensator.notifyUpdate("maximumSectionCount", oldValue, maximumSectionCount);
        return this;
    }
}
