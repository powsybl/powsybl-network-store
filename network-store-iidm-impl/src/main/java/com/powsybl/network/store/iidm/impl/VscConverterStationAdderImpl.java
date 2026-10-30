/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.network.store.model.*;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibilityForGeneratorConvention;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VscConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<VscConverterStationAdderImpl> implements VscConverterStationAdder {

    private Boolean voltageRegulatorOn;

    private double reactivePowerSetPoint = Double.NaN;

    private double voltageSetPoint = Double.NaN;

    private double localTargetQ = Double.NaN;

    private double localTargetV = Double.NaN;

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes = null;

    private Terminal regulatingTerminal;

    VscConverterStationAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public VscConverterStationAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VscConverterStationAdder setVoltageSetpoint(double voltageSetPoint) {
        this.voltageSetPoint = voltageSetPoint;
        return this;
    }

    @Override
    public VscConverterStationAdder setReactivePowerSetpoint(double reactivePowerSetPoint) {
        this.reactivePowerSetPoint = reactivePowerSetPoint;
        return this;
    }

    @Override
    public VscConverterStationAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public VscConverterStationAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    @Override
    public VscConverterStationAdder setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    @Override
    public VoltageRegulationAdder<VscConverterStationAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(VscConverterStation.class, this, this, getNetwork(), this::setVoltageRegulationAttributes);
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
    }

    @Override
    public VscConverterStation add() {
        NetworkImpl network = getNetwork();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && voltageRegulatorOn == null) {
            voltageRegulatorOn = false;
        }
        String id = checkAndGetUniqueId();
        checkNodeBus();

        // Backward compatibility
        if (voltageRegulationAttributes == null && voltageRegulatorOn != null) {
            // If a vsc converter station with old setters is added and voltageRegulation does not exist,
            // the new voltageRegulation will be created from the old attributes.
            createVoltageRegulationBackwardCompatibilityForGeneratorConvention(this, voltageSetPoint, reactivePowerSetPoint, voltageRegulatorOn, regulatingTerminal);
        } else {
            // In the case of a vsc converter station with old setters and newVoltageRegulation method used
            // the old local attributes will be set without overriding the local attributes if already set
            if (!Double.isNaN(voltageSetPoint) && Double.isNaN(localTargetV)) {
                localTargetV = voltageSetPoint;
            }
            if (!Double.isNaN(reactivePowerSetPoint) && Double.isNaN(localTargetQ)) {
                localTargetQ = reactivePowerSetPoint;
            }
        }

        validate();
        ValidationUtil.checkLocalTargetQandV(this,
            VscConverterStation.class,
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
                .regulatingResourceType(ResourceType.VSC_CONVERTER_STATION)
                .regulatingTapChangerType(RegulatingTapChangerType.NONE)
                .localTerminal(new TerminalRefAttributes(id, null))
                .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal()))
                .regulatedResourceType(voltageRegulationAttributes.terminal() != null
                    ? ((AbstractIdentifiableImpl<?, ?>) voltageRegulationAttributes.terminal().getConnectable()).getResource().getType()
                    : ResourceType.VSC_CONVERTER_STATION)
                .regulationMode(voltageRegulationAttributes.mode() != null ? voltageRegulationAttributes.mode().name() : null)
                .regulating(voltageRegulationAttributes.isRegulating())
                .targetValue(voltageRegulationAttributes.targetValue())
                .targetDeadband(voltageRegulationAttributes.targetDeadband())
                .slope(voltageRegulationAttributes.slope())
                .build()
            : null;

        Resource<VscConverterStationAttributes> resource = Resource.vscConverterStationBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(VscConverterStationAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .equivalent(isEquivalent())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .lossFactor(getLossFactor())
                        .localTargetV(localTargetV)
                        .localTargetQ(localTargetQ)
                        .voltageRegulation(persistedVoltageRegulation)
                        .reactiveLimits(minMaxAttributes)
                        .build())
                .build();
        VscConverterStationImpl station = getIndex().createVscConverterStation(resource);
        station.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        if (voltageRegulationAttributes != null && voltageRegulationAttributes.terminal() != null) {
            ((TerminalImpl<?>) voltageRegulationAttributes.terminal()).setAsRegulatingPoint(station.getRegulatingPoint());
            ((VoltageRegulationImpl) station.getVoltageRegulation()).registerOnTerminal();
        }
        return station;
    }

    @Override
    protected void validate() {
        super.validate();
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.VSC_CONVERTER_STATION.getDescription();
    }
}
