/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;
import com.powsybl.network.store.model.VscConverterStationAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VscConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<VscConverterStationAdderImpl> implements VscConverterStationAdder {

    private Boolean voltageRegulatorOn;

    private double reactivePowerSetPoint = Double.NaN;

    private double voltageSetPoint = Double.NaN;

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
    public VscConverterStation add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        validate();

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
                        .voltageRegulatorOn(voltageRegulatorOn)
                        .voltageSetPoint(voltageSetPoint)
                        .reactivePowerSetPoint(reactivePowerSetPoint)
                        .build())
                .build();
        VscConverterStationImpl station = getIndex().createVscConverterStation(resource);
        station.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return station;
    }

    @Override
    protected void validate() {
        super.validate();

        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, voltageSetPoint, reactivePowerSetPoint);
    }

    @Override
    protected String getTypeDescription() {
        return "vscConverterStation";
    }
}
