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

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class LoadDetailImpl implements LoadDetail {

    private LoadImpl load;

    public LoadDetailImpl(LoadImpl load) {
        this.load = load;
    }

    public LoadDetailImpl(LoadImpl load, float fixedActivePower, float fixedReactivePower,
                          float variableActivePower, float variableReactivePower) {
        this(load.initLoadDetailAttributes(fixedActivePower, fixedReactivePower, variableActivePower, variableReactivePower));
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
    public float getFixedActivePower() {
        return load.getResource().getAttributes().getLoadDetail().getFixedActivePower();
    }

    @Override
    public LoadDetail setFixedActivePower(float fixedActivePower) {
        load.getResource().getAttributes().getLoadDetail().setFixedActivePower(checkPower(fixedActivePower, "Invalid fixedActivePower"));
        return this;
    }

    @Override
    public float getFixedReactivePower() {
        return load.getResource().getAttributes().getLoadDetail().getFixedReactivePower();
    }

    @Override
    public LoadDetail setFixedReactivePower(float fixedReactivePower) {
        load.getResource().getAttributes().getLoadDetail().setFixedReactivePower(checkPower(fixedReactivePower, "Invalid fixedReactivePower"));
        return this;
    }

    @Override
    public float getVariableActivePower() {
        return load.getResource().getAttributes().getLoadDetail().getVariableActivePower();
    }

    @Override
    public LoadDetail setVariableActivePower(float variableActivePower) {
        load.getResource().getAttributes().getLoadDetail().setVariableActivePower(checkPower(variableActivePower, "Invalid variableActivePower"));
        return this;
    }

    @Override
    public float getVariableReactivePower() {
        return load.getResource().getAttributes().getLoadDetail().getVariableReactivePower();
    }

    @Override
    public LoadDetail setVariableReactivePower(float variableReactivePower) {
        load.getResource().getAttributes().getLoadDetail().setVariableReactivePower(checkPower(variableReactivePower, "Invalid variableReactivePower"));
        return this;
    }

    private static float checkPower(float power, String errorMessage) {
        if (Float.isNaN(power)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return power;
    }
}
