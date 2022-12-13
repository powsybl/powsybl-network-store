/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.LccConverterStationAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class LccConverterStationImpl extends AbstractHvdcConverterStationImpl<LccConverterStation, LccConverterStationAttributes> implements LccConverterStation {

    public LccConverterStationImpl(NetworkObjectIndex index, Resource<LccConverterStationAttributes> resource) {
        super(index, resource);
    }

    static LccConverterStationImpl create(NetworkObjectIndex index, Resource<LccConverterStationAttributes> resource) {
        return new LccConverterStationImpl(index, resource);
    }

    @Override
    protected LccConverterStation getInjection() {
        return this;
    }

    @Override
    public HvdcType getHvdcType() {
        return HvdcType.LCC;
    }

    @Override
    public float getPowerFactor() {
        return checkResource().getAttributes().getPowerFactor();
    }

    @Override
    public LccConverterStation setPowerFactor(float powerFactor) {
        var resource = checkResource();
        ValidationUtil.checkPowerFactor(this, powerFactor);
        float oldValue = resource.getAttributes().getPowerFactor();
        resource.getAttributes().setPowerFactor(powerFactor);
        updateResource();
        index.notifyUpdate(this, "powerFactor", oldValue, powerFactor);
        return this;
    }

    @Override
    public float getLossFactor() {
        return checkResource().getAttributes().getLossFactor();
    }

    @Override
    public LccConverterStation setLossFactor(float lossFactor) {
        var resource = checkResource();
        ValidationUtil.checkLossFactor(this, lossFactor);
        float oldValue = resource.getAttributes().getLossFactor();
        resource.getAttributes().setLossFactor(lossFactor);
        updateResource();
        index.notifyUpdate(this, "lossFactor", oldValue, lossFactor);
        return this;
    }

    @Override
    public void remove(boolean removeDanglingSwitches) {
        var resource = checkResource();
        HvdcLine hvdcLine = getHvdcLine(); // For optimization
        if (hvdcLine != null) {
            throw new ValidationException(this, "Impossible to remove this converter station (still attached to '" + hvdcLine.getId() + "')");
        }
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeLccConverterStation(resource.getId());
        index.notifyAfterRemoval(resource.getId());
        if (removeDanglingSwitches) {
            getTerminal().removeDanglingSwitches();
        }
    }
}
