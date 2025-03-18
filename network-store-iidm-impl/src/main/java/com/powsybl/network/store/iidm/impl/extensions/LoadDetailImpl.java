/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.network.store.iidm.impl.LoadImpl;
import com.powsybl.network.store.model.LoadDetailAttributes;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class LoadDetailImpl extends AbstractExtension<Load> implements LoadDetail {

    public LoadDetailImpl(LoadImpl load) {
        super(Objects.requireNonNull(load));
    }

    private LoadImpl getLoad() {
        return (LoadImpl) getExtendable();
    }

    private LoadDetailAttributes getLoadDetailAttributes() {
        return getLoad().getResource().getAttributes().getLoadDetail();
    }

    @Override
    public double getFixedActivePower() {
        return getLoadDetailAttributes().getFixedActivePower();
    }

    @Override
    public LoadDetail setFixedActivePower(double fixedActivePower) {
        double oldValue = getFixedActivePower();
        if (oldValue != fixedActivePower) {
            getLoad().updateResourceExtension(this, res -> res.getAttributes().getLoadDetail().setFixedActivePower(checkPower(fixedActivePower, "Invalid fixedActivePower")), "fixedActivePower", oldValue, fixedActivePower);
        }
        return this;
    }

    @Override
    public double getFixedReactivePower() {
        return getLoadDetailAttributes().getFixedReactivePower();
    }

    @Override
    public LoadDetail setFixedReactivePower(double fixedReactivePower) {
        double oldValue = getFixedReactivePower();
        if (oldValue != fixedReactivePower) {
            getLoad().updateResourceExtension(this, res -> res.getAttributes().getLoadDetail().setFixedReactivePower(checkPower(fixedReactivePower, "Invalid fixedReactivePower")), "fixedReactivePower", oldValue, fixedReactivePower);
        }
        return this;
    }

    @Override
    public double getVariableActivePower() {
        return getLoadDetailAttributes().getVariableActivePower();
    }

    @Override
    public LoadDetail setVariableActivePower(double variableActivePower) {
        double oldValue = getVariableActivePower();
        if (oldValue != variableActivePower) {
            getLoad().updateResourceExtension(this, res -> res.getAttributes().getLoadDetail().setVariableActivePower(checkPower(variableActivePower, "Invalid variableActivePower")), "variableActivePower", oldValue, variableActivePower);
        }
        return this;
    }

    @Override
    public double getVariableReactivePower() {
        return getLoadDetailAttributes().getVariableReactivePower();
    }

    @Override
    public LoadDetail setVariableReactivePower(double variableReactivePower) {
        double oldValue = getVariableReactivePower();
        if (oldValue != variableReactivePower) {
            getLoad().updateResourceExtension(this, res -> res.getAttributes().getLoadDetail().setVariableReactivePower(checkPower(variableReactivePower, "Invalid variableReactivePower")), "variableReactivePower", oldValue, variableReactivePower);
        }
        return this;
    }

    private static double checkPower(double power, String errorMessage) {
        if (Double.isNaN(power)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return power;
    }
}
