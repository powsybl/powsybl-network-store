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
import com.powsybl.network.store.model.ResourceType;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class LccConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<LccConverterStationAdderImpl> implements LccConverterStationAdder {

    private float powerFactor = Float.NaN;

    LccConverterStationAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index, String parentNetwork) {
        super(voltageLevelResource, index, parentNetwork);
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

        Resource<LccConverterStationAttributes> resource = Resource.lccConverterStationBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .parentNetwork(getParentNetwork())
                .attributes(LccConverterStationAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .lossFactor(getLossFactor())
                        .powerFactor(powerFactor)
                        .build())
                .build();
        LccConverterStationImpl station = getIndex().createLccConverterStation(resource);
        station.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return station;
    }

    @Override
    protected void validate() {
        super.validate();
        ValidationUtil.checkPowerFactor(this, powerFactor);
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.LCC_CONVERTER_STATION.getDescription();
    }
}
