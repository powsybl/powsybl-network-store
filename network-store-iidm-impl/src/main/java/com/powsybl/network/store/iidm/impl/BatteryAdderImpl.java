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

    private double targetP = Double.NaN;

    private double targetQ = Double.NaN;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private double localTargetV = Double.NaN;

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes;

    BatteryAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public VoltageRegulationAdder<BatteryAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(Battery.class, this, null, getIndex(), this,
                attributes -> {
                    voltageRegulationAttributes = attributes;
                    return null;
                });
    }

    @Override
    public double getLocalTargetQ() {
        return targetQ;
    }

    @Override
    public BatteryAdder setLocalTargetQ(double localTargetQ) {
        this.targetQ = localTargetQ;
        return this;
    }

    @Override
    public BatteryAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
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
        ValidationUtil.checkLocalTargetQandV(this,
                Battery.class,
                localTargetV,
                targetQ,
                voltageRegulationAttributes,
                getNetwork().getMinValidationLevel(),
                getNetwork().getReportNodeContext().getReportNode());

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
                        .localTargetV(localTargetV)
                        .voltageRegulation(voltageRegulationAttributes == null ? null : NetworkVoltageRegulationAttributes.builder()
                                .targetValue(voltageRegulationAttributes.targetValue())
                                .targetDeadband(voltageRegulationAttributes.targetDeadband())
                                .slope(voltageRegulationAttributes.slope())
                                .mode(voltageRegulationAttributes.mode())
                                .regulating(voltageRegulationAttributes.isRegulating())
                                .terminal(TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal()))
                                .build())
                        .reactiveLimits(minMaxAttributes)
                        .build())
                .build();
        BatteryImpl battery = getIndex().createBattery(resource);
        battery.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return battery;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.BATTERY.getDescription();
    }
}
