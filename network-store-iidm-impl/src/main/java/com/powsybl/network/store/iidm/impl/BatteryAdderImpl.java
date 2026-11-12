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

    private double localTargetQ = Double.NaN;

    private double localTargetV = Double.NaN;

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes = null;

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
        return this.setLocalTargetQ(targetQ);
    }

    @Override
    public BatteryAdder setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    @Override
    public BatteryAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    @Override
    public VoltageRegulationAdder<BatteryAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(Battery.class, this, this, getNetwork(), this::setVoltageRegulationAttributes);
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
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
            localTargetQ,
            voltageRegulationAttributes,
            getNetwork().getMinValidationLevel(),
            getNetwork().getReportNodeContext().getReportNode());

        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        VoltageRegulationAttributes persistedVoltageRegulation = voltageRegulationAttributes != null
            ? VoltageRegulationAttributes.builder()
                .regulatingEquipmentId(id)
                .regulatingResourceType(ResourceType.BATTERY)
                .regulatingTapChangerType(RegulatingTapChangerType.NONE)
                .localTerminal(new TerminalRefAttributes(id, null))
                .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal()))
                .regulatedResourceType(voltageRegulationAttributes.terminal() != null
                    ? ((AbstractIdentifiableImpl<?, ?>) voltageRegulationAttributes.terminal().getConnectable()).getResource().getType()
                    : ResourceType.BATTERY)
                .regulationMode(voltageRegulationAttributes.mode() != null ? voltageRegulationAttributes.mode().name() : null)
                .regulating(voltageRegulationAttributes.isRegulating())
                .targetValue(voltageRegulationAttributes.targetValue())
                .targetDeadband(voltageRegulationAttributes.targetDeadband())
                .slope(voltageRegulationAttributes.slope())
                .build()
            : null;

        Resource<BatteryAttributes> resource = Resource.batteryBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(BatteryAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .equivalent(isEquivalent())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus())
                        .maxP(maxP)
                        .minP(minP)
                        .targetP(targetP)
                        .localTargetQ(localTargetQ)
                        .localTargetV(localTargetV)
                        .voltageRegulation(persistedVoltageRegulation)
                        .reactiveLimits(minMaxAttributes)
                        .build())
                .build();
        BatteryImpl battery = getIndex().createBattery(resource);
        battery.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        if (voltageRegulationAttributes != null && voltageRegulationAttributes.terminal() != null) {
            ((TerminalImpl<?>) voltageRegulationAttributes.terminal()).setAsRegulatingPoint(battery.getRegulatingPoint());
            ((VoltageRegulationImpl) battery.getVoltageRegulation()).registerOnTerminal();
        }
        return battery;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.BATTERY.getDescription();
    }
}
