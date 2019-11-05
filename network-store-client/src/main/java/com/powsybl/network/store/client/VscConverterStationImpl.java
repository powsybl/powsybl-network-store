/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VscConverterStationAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VscConverterStationImpl extends AbstractInjectionImpl<VscConverterStation, VscConverterStationAttributes> implements VscConverterStation {

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
        return resource.getAttributes().getVoltageSetpoint();
    }

    @Override
    public HvdcConverterStation setVoltageSetpoint(double voltageSetpoint) {
        resource.getAttributes().setVoltageSetpoint(voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return resource.getAttributes().getReactivePowerSetpoint();
    }

    @Override
    public HvdcConverterStation setReactivePowerSetpoint(double reactivePowerSetpoint) {
        resource.getAttributes().setReactivePowerSetpoint(reactivePowerSetpoint);
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
    public ReactiveLimits getReactiveLimits() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl();
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        throw new UnsupportedOperationException("TODO");
    }
}
