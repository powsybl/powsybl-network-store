/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.LccConverterStationAdder;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.LccConverterStationAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class LccConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<LccConverterStationAdderImpl> implements LccConverterStationAdder {

    private float powerFactor = Float.NaN;

    LccConverterStationAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public LccConverterStationAdder setPowerFactor(float powerFactor) {
        this.powerFactor = powerFactor;
        return this;
    }

    @Override
    public LccConverterStation add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        validate();

        Resource<LccConverterStationAttributes> resource = Resource.lccConverterStationBuilder(index.getResourceUpdater())
                .id(id)
                .attributes(LccConverterStationAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus())
                        .lossFactor(getLossFactor())
                        .powerFactor(powerFactor)
                        .build())
                .build();
        return getIndex().createLccConverterStation(resource);
    }

    @Override
    protected void validate() {
        super.validate();
        ValidationUtil.checkPowerFactor(this, powerFactor);
    }

    @Override
    protected String getTypeDescription() {
        return "lccConverterStation";
    }
}
