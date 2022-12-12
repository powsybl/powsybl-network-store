/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractHvdcConverterStationAdder<T extends AbstractHvdcConverterStationAdder<T>> extends AbstractInjectionAdder<T> {

    private float lossFactor = Float.NaN;

    AbstractHvdcConverterStationAdder(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    public float getLossFactor() {
        return lossFactor;
    }

    public T setLossFactor(float lossFactor) {
        this.lossFactor = lossFactor;
        return (T) this;
    }

    protected void validate() {
        ValidationUtil.checkLossFactor(this, lossFactor);
    }
}
