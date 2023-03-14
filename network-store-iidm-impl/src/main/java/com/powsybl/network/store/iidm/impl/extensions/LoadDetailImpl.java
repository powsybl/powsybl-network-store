/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.network.store.iidm.impl.LoadImpl;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class LoadDetailImpl implements LoadDetail {

    private LoadImpl load;

    public LoadDetailImpl(LoadImpl load) {
        this.load = Objects.requireNonNull(load);
    }

    @Override
    public Load getExtendable() {
        return load;
    }

    @Override
    public void setExtendable(Load load) {
        this.load = (LoadImpl) load;
    }

    @Override
    public double getFixedActivePower() {
        return load.checkResource().getAttributes().getLoadDetail().getFixedActivePower();
    }

    @Override
    public LoadDetail setFixedActivePower(double fixedActivePower) {
        load.updateResource(res -> res.getAttributes().getLoadDetail().setFixedActivePower(checkPower(fixedActivePower, "Invalid fixedActivePower")));
        return this;
    }

    @Override
    public double getFixedReactivePower() {
        return load.checkResource().getAttributes().getLoadDetail().getFixedReactivePower();
    }

    @Override
    public LoadDetail setFixedReactivePower(double fixedReactivePower) {
        load.updateResource(res -> res.getAttributes().getLoadDetail().setFixedReactivePower(checkPower(fixedReactivePower, "Invalid fixedReactivePower")));
        return this;
    }

    @Override
    public double getVariableActivePower() {
        return load.checkResource().getAttributes().getLoadDetail().getVariableActivePower();
    }

    @Override
    public LoadDetail setVariableActivePower(double variableActivePower) {
        load.updateResource(res -> res.getAttributes().getLoadDetail().setVariableActivePower(checkPower(variableActivePower, "Invalid variableActivePower")));
        return this;
    }

    @Override
    public double getVariableReactivePower() {
        return load.checkResource().getAttributes().getLoadDetail().getVariableReactivePower();
    }

    @Override
    public LoadDetail setVariableReactivePower(double variableReactivePower) {
        load.updateResource(res -> res.getAttributes().getLoadDetail().setVariableReactivePower(checkPower(variableReactivePower, "Invalid variableReactivePower")));
        return this;
    }

    private static double checkPower(double power, String errorMessage) {
        if (Double.isNaN(power)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return power;
    }
}
