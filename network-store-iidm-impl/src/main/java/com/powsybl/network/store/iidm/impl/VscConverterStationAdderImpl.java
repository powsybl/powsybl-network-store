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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VscConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<VscConverterStationAdderImpl> implements VscConverterStationAdder {

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes;

    private double localTargetV = Double.NaN;
    private double localTargetQ = Double.NaN;

    private boolean localTargetQExplicit;

    @Override
    public VoltageRegulationAdder<VscConverterStationAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(VscConverterStation.class, this, getNetwork(), this, attributes -> voltageRegulationAttributes = attributes);
    }

    @Override
    public VscConverterStationAdder setLocalTargetV(double targetV) {
        this.localTargetV = targetV;
        return this;
    }

    @Override
    public VscConverterStationAdder setLocalTargetQ(double targetQ) {
        this.localTargetQ = targetQ;
        this.localTargetQExplicit = true;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return localTargetQ;
    }

    private Boolean voltageRegulatorOn;

    private double reactivePowerSetPoint = Double.NaN;

    private double voltageSetPoint = Double.NaN;

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
    public VscConverterStation add() {
        NetworkImpl network = getNetwork();
        if (voltageRegulationAttributes == null && voltageRegulatorOn != null) {
            VoltageRegulationCompatibility.createVscRegulation(this, voltageSetPoint, localTargetV, reactivePowerSetPoint,
                voltageRegulatorOn, regulatingTerminal);
        } else if (voltageRegulationAttributes != null) {
            if (Double.isNaN(localTargetV) && !Double.isNaN(voltageSetPoint)) {
                localTargetV = voltageSetPoint;
            }
            if (Double.isNaN(localTargetQ) && !Double.isNaN(reactivePowerSetPoint)) {
                localTargetQ = reactivePowerSetPoint;
            }
        }
        if (!localTargetQExplicit && Double.isNaN(localTargetQ) && !Double.isNaN(reactivePowerSetPoint)) {
            localTargetQ = reactivePowerSetPoint;
        }
        if (voltageRegulationAttributes == null && Double.isNaN(localTargetV) && !Double.isNaN(voltageSetPoint)
            && regulatingTerminal == null) {
            localTargetV = voltageSetPoint;
        }
        if (voltageRegulationAttributes == null && Double.isNaN(localTargetQ)
            && Double.isNaN(reactivePowerSetPoint) && voltageRegulatorOn == null) {
            localTargetQ = 0;
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
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes(id, ResourceType.VSC_CONVERTER_STATION, RegulatingTapChangerType.NONE,
            new TerminalRefAttributes(id, null), terminalRefAttributes, null, ResourceType.VSC_CONVERTER_STATION, voltageRegulatorOn);

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
                          .localTargetQ(localTargetQ)
                         .localTargetV(localTargetV)
                        .voltageRegulation(NetworkVoltageRegulationAttributesMapper.map(voltageRegulationAttributes))
                        .regulatingPoint(regulatingPointAttributes)
                        .reactiveLimits(minMaxAttributes)
                        .build())
                .build();
        VscConverterStationImpl station = getIndex().createVscConverterStation(resource);
        station.getVoltageRegulation();
        station.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return station;
    }

    @Override
    protected void validate() {
        super.validate();
        VoltageRegulationValidation.check(this, voltageRegulationAttributes, VscConverterStation.class, localTargetV, localTargetQ, getNetwork());
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.VSC_CONVERTER_STATION.getDescription();
    }
}
