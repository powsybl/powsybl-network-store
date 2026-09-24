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
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.network.store.model.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BatteryAdderImpl extends AbstractInjectionAdder<BatteryAdderImpl> implements BatteryAdder {

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes;

    @Override
    public VoltageRegulationAdder<BatteryAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(Battery.class, this, getNetwork(), this, attributes -> voltageRegulationAttributes = attributes);
    }

    private double targetP = Double.NaN;

    private double targetQ = Double.NaN;

    private double localTargetV = Double.NaN;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    BatteryAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public BatteryAdder setTargetP(double targetP) {
        this.targetP = targetP;
        return this;
    }

    @Override
    public BatteryAdder setTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public BatteryAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    @Override
    public BatteryAdder setLocalTargetQ(double localTargetQ) {
        this.targetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return targetQ;
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
        checkNodeBus();
        ValidationUtil.checkP0(this, targetP, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        VoltageRegulationValidation.check(this, voltageRegulationAttributes, Battery.class, localTargetV, targetQ, getNetwork());

        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        Resource<BatteryAttributes> resource = Resource.batteryBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(BatteryAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus())
                        .maxP(maxP)
                        .minP(minP)
                        .targetP(targetP)
                        .targetQ(targetQ)
                        .localTargetQ(targetQ)
                        .localTargetV(localTargetV)
                        .voltageRegulation(NetworkVoltageRegulationAttributesMapper.map(voltageRegulationAttributes))
                        .reactiveLimits(minMaxAttributes)
                        .build())
                .build();
        BatteryImpl battery = getIndex().createBattery(resource);
        battery.getVoltageRegulation();
        battery.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return battery;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.BATTERY.getDescription();
    }
}
