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
public class LoadDetailImplNetworkStore implements LoadDetail {

    private LoadImpl load;

    public LoadDetailImplNetworkStore(LoadImpl load) {
        this.load = load;
    }

    public LoadDetailImplNetworkStore(LoadImpl load, float fixedActivePower, float fixedReactivePower,
                          float variableActivePower, float variableReactivePower) {
        this(load);
        load.initLoadDetailAttributes(fixedActivePower, fixedReactivePower, variableActivePower, variableReactivePower);
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
        return load.getFixedActivePower();
    }

    @Override
    public LoadDetail setFixedActivePower(float fixedActivePower) {
        load.setFixedActivePower(checkPower(fixedActivePower, "Invalid fixedActivePower"));
        return this;
    }

    @Override
    public float getFixedReactivePower() {
        return load.getFixedReactivePower();
    }

    @Override
    public LoadDetail setFixedReactivePower(float fixedReactivePower) {
        load.setFixedReactivePower(checkPower(fixedReactivePower, "Invalid fixedReactivePower"));
        return this;
    }

    @Override
    public float getVariableActivePower() {
        return load.getVariableActivePower();
    }

    @Override
    public LoadDetail setVariableActivePower(float variableActivePower) {
        load.setVariableActivePower(checkPower(variableActivePower, "Invalid variableActivePower"));
        return this;
    }

    @Override
    public float getVariableReactivePower() {
        return load.getVariableReactivePower();
    }

    @Override
    public LoadDetail setVariableReactivePower(float variableReactivePower) {
        load.setVariableReactivePower(checkPower(variableReactivePower, "Invalid variableReactivePower"));
        return this;
    }

    private static float checkPower(float power, String errorMessage) {
        if (Float.isNaN(power)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return power;
    }
}
