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
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.ShuntCompensatorLinearModelAttributes;

import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ShuntCompensatorLinearModelImpl implements ShuntCompensatorLinearModel {

    private final ShuntCompensatorImpl shuntCompensator;

    public ShuntCompensatorLinearModelImpl(ShuntCompensatorImpl shuntCompensator) {
        this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
    }

    private Resource<ShuntCompensatorAttributes> getResource() {
        return shuntCompensator.getResource();
    }

    private static ShuntCompensatorLinearModelAttributes getAttributes(Resource<ShuntCompensatorAttributes> resource) {
        return (ShuntCompensatorLinearModelAttributes) resource.getAttributes().getModel();
    }

    private ShuntCompensatorLinearModelAttributes getAttributes() {
        return getAttributes(getResource());
    }

    @Override
    public double getBPerSection() {
        return getAttributes().getBPerSection();
    }

    @Override
    public ShuntCompensatorLinearModel setBPerSection(double bPerSection) {
        ValidationUtil.checkBPerSection(shuntCompensator, bPerSection);
        double oldValue = getAttributes().getBPerSection();
        if (bPerSection != oldValue) {
            shuntCompensator.updateResource(res -> getAttributes(res).setBPerSection(bPerSection));
            shuntCompensator.notifyUpdate("bPerSection", oldValue, bPerSection);
        }
        return this;
    }

    @Override
    public double getGPerSection() {
        return getAttributes().getGPerSection();
    }

    @Override
    public ShuntCompensatorLinearModel setGPerSection(double gPerSection) {
        double oldValue = getAttributes().getGPerSection();
        if (gPerSection != oldValue) {
            shuntCompensator.updateResource(res -> getAttributes(res).setGPerSection(gPerSection));
            shuntCompensator.notifyUpdate("gPerSection", oldValue, gPerSection);
        }
        return this;
    }

    @Override
    public ShuntCompensatorLinearModel setMaximumSectionCount(int maximumSectionCount) {
        ValidationUtil.checkSections(shuntCompensator, shuntCompensator.getSectionCount(), maximumSectionCount, ValidationLevel.STEADY_STATE_HYPOTHESIS, shuntCompensator.getNetwork().getReportNodeContext().getReportNode());
        int oldValue = getAttributes().getMaximumSectionCount();
        if (maximumSectionCount != oldValue) {
            shuntCompensator.updateResource(res -> getAttributes(res).setMaximumSectionCount(maximumSectionCount));
            shuntCompensator.notifyUpdate("maximumSectionCount", oldValue, maximumSectionCount);
        }
        return this;
    }
}
