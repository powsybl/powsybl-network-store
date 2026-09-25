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

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VscConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<VscConverterStationAdderImpl> implements VscConverterStationAdder {

    private Boolean voltageRegulatorOn;

    private double reactivePowerSetPoint = Double.NaN;

    private double voltageSetPoint = Double.NaN;

    private Terminal regulatingTerminal;

    private double localTargetV = Double.NaN;

    private double localTargetQ = Double.NaN;

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes;

    private boolean voltageRegulationConfigured;

    VscConverterStationAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public VoltageRegulationAdder<VscConverterStationAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(VscConverterStation.class, this, null, getIndex(), this,
                attributes -> {
                    voltageRegulationAttributes = attributes;
                    voltageRegulationConfigured = true;
                    return null;
                });
    }

    @Override
    public double getLocalTargetQ() {
        return localTargetQ;
    }

    @Override
    public VscConverterStationAdder setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public VscConverterStationAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
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
    public VscConverterStation add() {
        NetworkImpl network = getNetwork();

        if (voltageRegulationAttributes == null && voltageRegulatorOn != null) {
            createVoltageRegulationBackwardCompatibility(this, voltageSetPoint, reactivePowerSetPoint, voltageRegulatorOn, regulatingTerminal);
        } else if (voltageRegulationConfigured) {
            if (!Double.isNaN(voltageSetPoint) && Double.isNaN(localTargetV)) {
                localTargetV = voltageSetPoint;
            }
            if (!Double.isNaN(reactivePowerSetPoint) && Double.isNaN(localTargetQ)) {
                localTargetQ = reactivePowerSetPoint;
            }
        }
        String id = checkAndGetUniqueId();
        checkNodeBus();
        validate();

        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
        TerminalRefAttributes voltageRegulationTerminalRef = voltageRegulationAttributes == null
                ? null
                : TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal());
        Boolean regulatingPointStatus = voltageRegulationAttributes == null
                ? voltageRegulatorOn
                : Boolean.valueOf(voltageRegulationAttributes.isRegulating());
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes(id, ResourceType.VSC_CONVERTER_STATION, RegulatingTapChangerType.NONE,
            new TerminalRefAttributes(id, null), voltageRegulationTerminalRef != null ? voltageRegulationTerminalRef : terminalRefAttributes,
            null, ResourceType.VSC_CONVERTER_STATION,
            regulatingPointStatus);

        Resource<VscConverterStationAttributes> resource = Resource.vscConverterStationBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(VscConverterStationAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .lossFactor(getLossFactor())
                        .voltageSetPoint(voltageSetPoint)
                        .reactivePowerSetPoint(reactivePowerSetPoint)
                        .localTargetV(localTargetV)
                        .localTargetQ(localTargetQ)
                        .regulatingPoint(regulatingPointAttributes)
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
        VscConverterStationImpl station = getIndex().createVscConverterStation(resource);
        station.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        if (!voltageRegulationConfigured && regulatingTerminal != null) {
            station.setRegulatingTerminal(regulatingTerminal);
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
