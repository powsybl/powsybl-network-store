/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BatteryImpl extends AbstractInjectionImpl<Battery, BatteryAttributes> implements Battery, ReactiveLimitsOwner {

    public BatteryImpl(NetworkObjectIndex index, Resource<BatteryAttributes> resource) {
        super(index, resource);
    }

    static BatteryImpl create(NetworkObjectIndex index, Resource<BatteryAttributes> resource) {
        return new BatteryImpl(index, resource);
    }

    @Override
    protected Battery getInjection() {
        return this;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.BATTERY;
    }

    @Override
    public double getP0() {
        return resource.getAttributes().getP0();
    }

    @Override
    public Battery setP0(double p0) {
        ValidationUtil.checkP0(this, p0);
        ValidationUtil.checkActivePowerLimits(this, getMinP(), getMaxP(), p0);
        double oldValue = resource.getAttributes().getP0();
        resource.getAttributes().setP0(p0);
        String variantId = getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "p0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return resource.getAttributes().getQ0();
    }

    @Override
    public Battery setQ0(double q0) {
        ValidationUtil.checkQ0(this, q0);
        double oldValue = resource.getAttributes().getQ0();
        resource.getAttributes().setQ0(q0);
        String variantId = getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "q0", variantId, oldValue, q0);
        return this;

    }

    @Override
    public double getMinP() {
        return resource.getAttributes().getMinP();
    }

    @Override
    public Battery setMinP(double minP) {
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActivePowerLimits(this, minP, getMaxP(), getP0());
        double oldValue = resource.getAttributes().getMinP();
        resource.getAttributes().setMinP(minP);
        index.notifyUpdate(this, "minP", oldValue, minP);
        return this;

    }

    @Override
    public double getMaxP() {
        return resource.getAttributes().getMaxP();
    }

    @Override
    public Battery setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, getMinP(), maxP, getP0());
        double oldValue = resource.getAttributes().getMaxP();
        resource.getAttributes().setMaxP(maxP);
        index.notifyUpdate(this, "maxP", oldValue, maxP);
        return this;

    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        resource.getAttributes().setReactiveLimits(reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        ReactiveLimitsAttributes reactiveLimitsAttributes = resource.getAttributes().getReactiveLimits();
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

    @Override
    public void remove() {
        index.removeBattery(resource.getId());
        index.notifyRemoval(this);
    }

    @Override
    protected String getTypeDescription() {
        return "Battery";
    }
}
