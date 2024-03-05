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
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class VscConverterStationImpl extends AbstractHvdcConverterStationImpl<VscConverterStation, VscConverterStationAttributes> implements VscConverterStation, ReactiveLimitsOwner {

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
    public HvdcType getHvdcType() {
        return HvdcType.VSC;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return getResource().getAttributes().getVoltageRegulatorOn();
    }

    @Override
    public VscConverterStationImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, getVoltageSetpoint(), getReactivePowerSetpoint(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        boolean oldValue = getResource().getAttributes().getVoltageRegulatorOn();
        if (voltageRegulatorOn != oldValue) {
            updateResource(res -> res.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        }
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return getResource().getAttributes().getVoltageSetPoint();
    }

    @Override
    public VscConverterStationImpl setVoltageSetpoint(double voltageSetpoint) {
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), voltageSetpoint, getReactivePowerSetpoint(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getResource().getAttributes().getVoltageSetPoint();
        if (Double.compare(voltageSetpoint, oldValue) != 0) {
            updateResource(res -> res.getAttributes().setVoltageSetPoint(voltageSetpoint));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "voltageSetpoint", variantId, oldValue, voltageSetpoint);
        }
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return getResource().getAttributes().getReactivePowerSetPoint();
    }

    @Override
    public VscConverterStationImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), getVoltageSetpoint(), reactivePowerSetpoint, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getResource().getAttributes().getReactivePowerSetPoint();
        if (Double.compare(reactivePowerSetpoint, oldValue) != 0) {
            updateResource(res -> res.getAttributes().setReactivePowerSetPoint(reactivePowerSetpoint));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "reactivePowerSetpoint", variantId, oldValue, reactivePowerSetpoint);
        }
        return this;
    }

    @Override
    public float getLossFactor() {
        return getResource().getAttributes().getLossFactor();
    }

    @Override
    public VscConverterStation setLossFactor(float lossFactor) {
        ValidationUtil.checkLossFactor(this, lossFactor);
        float oldValue = getResource().getAttributes().getLossFactor();
        if (lossFactor != oldValue) {
            updateResource(res -> res.getAttributes().setLossFactor(lossFactor));
            index.notifyUpdate(this, "lossFactor", oldValue, lossFactor);
        }
        return this;
    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        ReactiveLimitsAttributes oldValue = getResource().getAttributes().getReactiveLimits();
        updateResource(res -> res.getAttributes().setReactiveLimits(reactiveLimits));
        index.notifyUpdate(this, "reactiveLimits", oldValue, reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        ReactiveLimitsAttributes reactiveLimitsAttributes = getResource().getAttributes().getReactiveLimits();
        if (reactiveLimitsAttributes.getKind() == ReactiveLimitsKind.CURVE) {
            return new ReactiveCapabilityCurveImpl((ReactiveCapabilityCurveAttributes) reactiveLimitsAttributes);
        } else {
            return new MinMaxReactiveLimitsImpl((MinMaxReactiveLimitsAttributes) reactiveLimitsAttributes);
        }
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        ReactiveLimits reactiveLimits = getReactiveLimits();
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(reactiveLimits)) {
            return type.cast(reactiveLimits);
        } else {
            throw new ValidationException(this, "incorrect reactive limits type "
                    + type.getName() + ", expected " + reactiveLimits.getClass());
        }
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl<>(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl<>(this);
    }

    @Override
    public void remove() {
        var resource = getResource();
        HvdcLine hvdcLine = getHvdcLine(); // For optimization
        if (hvdcLine != null) {
            throw new ValidationException(this, "Impossible to remove this converter station (still attached to '" + hvdcLine.getId() + "')");
        }
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeVscConverterStation(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }
}
