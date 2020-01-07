/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VscConverterStationAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VscConverterStationImpl extends AbstractInjectionImpl<VscConverterStation, VscConverterStationAttributes> implements VscConverterStation, ReactiveLimitsOwner {

    public VscConverterStationImpl(NetworkObjectIndex index, Resource<VscConverterStationAttributes> resource) {
        super(index, resource);
    }

    static VscConverterStationImpl create(NetworkObjectIndex index, Resource<VscConverterStationAttributes> resource) {
        return new VscConverterStationImpl(index, resource);
    }

    @Override
    protected VscConverterStation getInjection() {
        return this;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.HVDC_CONVERTER_STATION;
    }

    @Override
    public HvdcLine getHvdcLine() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public HvdcType getHvdcType() {
        return HvdcType.VSC;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return resource.getAttributes().getVoltageRegulatorOn();
    }

    @Override
    public HvdcConverterStation setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        resource.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return resource.getAttributes().getVoltageSetPoint();
    }

    @Override
    public HvdcConverterStation setVoltageSetpoint(double voltageSetpoint) {
        resource.getAttributes().setVoltageSetPoint(voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return resource.getAttributes().getReactivePowerSetPoint();
    }

    @Override
    public HvdcConverterStation setReactivePowerSetpoint(double reactivePowerSetpoint) {
        resource.getAttributes().setReactivePowerSetPoint(reactivePowerSetpoint);
        return this;
    }

    @Override
    public float getLossFactor() {
        return resource.getAttributes().getLossFactor();
    }

    @Override
    public VscConverterStation setLossFactor(float lossFactor) {
        resource.getAttributes().setLossFactor(lossFactor);
        return this;
    }

    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        resource.getAttributes().setReactiveLimits(reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        return resource.getAttributes().getReactiveLimits();
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        ReactiveLimits reactiveLimits = resource.getAttributes().getReactiveLimits();
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(reactiveLimits)) {
            return type.cast(reactiveLimits);
        } else {
            throw new PowsyblException("incorrect reactive limits type "
                    + type.getName() + ", expected " + reactiveLimits.getClass());
        }
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }

}
