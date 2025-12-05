/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VscConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<VscConverterStationAdderImpl> implements VscConverterStationAdder {

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
        String id = checkAndGetUniqueId();
        checkNodeBus();
        validate();

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
                        .regulatingPoint(regulatingPointAttributes)
                        .build())
                .build();
        VscConverterStationImpl station = getIndex().createVscConverterStation(resource);
        station.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        station.setRegulatingTerminal(regulatingTerminal);
        return station;
    }

    @Override
    protected void validate() {
        super.validate();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, voltageSetPoint, reactivePowerSetPoint, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.VSC_CONVERTER_STATION.getDescription();
    }
}
