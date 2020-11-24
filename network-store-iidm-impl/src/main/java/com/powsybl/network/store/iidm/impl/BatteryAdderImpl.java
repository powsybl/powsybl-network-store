/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.BatteryAttributes;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BatteryAdderImpl extends AbstractInjectionAdder<BatteryAdderImpl> implements BatteryAdder {

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    BatteryAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public BatteryAdder setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public BatteryAdder setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public BatteryAdder setMinP(double minP) {
        this.minP = minP;
        return this;
    }

    @Override
    public BatteryAdder setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public Battery add() {
        String id = checkAndGetUniqueId();
        ValidationUtil.checkP0(this, p0);
        ValidationUtil.checkQ0(this, q0);
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP, p0);

        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        Resource<BatteryAttributes> resource = Resource.batteryBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(BatteryAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus())
                        .maxP(maxP)
                        .minP(minP)
                        .p0(p0)
                        .q0(q0)
                        .reactiveLimits(minMaxAttributes)
                        .build())
                .build();
        return getIndex().createBattery(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "Battery";
    }
}
