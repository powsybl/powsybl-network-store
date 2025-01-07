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
    public double getTargetP() {
        return getResource().getAttributes().getTargetP();
    }

    @Override
    public Battery setTargetP(double targetP) {
        ValidationUtil.checkP0(this, targetP, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkActivePowerLimits(this, getMinP(), getMaxP());
        double oldValue = getResource().getAttributes().getTargetP();
        if (targetP != oldValue) {
            updateResource(res -> res.getAttributes().setTargetP(targetP));
            String variantId = getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "targetP", variantId, oldValue, targetP);
        }
        return this;
    }

    @Override
    public double getTargetQ() {
        return getResource().getAttributes().getTargetQ();
    }

    @Override
    public Battery setTargetQ(double targetQ) {
        ValidationUtil.checkQ0(this, targetQ, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getTargetQ();
        if (targetQ != oldValue) {
            updateResource(res -> res.getAttributes().setTargetQ(targetQ));
            String variantId = getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "targetQ", variantId, oldValue, targetQ);
        }
        return this;

    }

    @Override
    public double getMinP() {
        return getResource().getAttributes().getMinP();
    }

    @Override
    public Battery setMinP(double minP) {
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActivePowerLimits(this, minP, getMaxP());
        double oldValue = getResource().getAttributes().getMinP();
        if (minP != oldValue) {
            updateResource(res -> res.getAttributes().setMinP(minP));
            String variantId = getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "minP", variantId, oldValue, minP);
        }
        return this;

    }

    @Override
    public double getMaxP() {
        return getResource().getAttributes().getMaxP();
    }

    @Override
    public Battery setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, getMinP(), maxP);
        double oldValue = getResource().getAttributes().getMaxP();
        if (maxP != oldValue) {
            updateResource(res -> res.getAttributes().setMaxP(maxP));
            String variantId = getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "maxP", variantId, oldValue, maxP);
        }
        return this;

    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        var resource = getResource();
        ReactiveLimitsAttributes oldValue = resource.getAttributes().getReactiveLimits();
        resource.getAttributes().setReactiveLimits(reactiveLimits);
        updateResource(res -> res.getAttributes().setReactiveLimits(reactiveLimits));
        String variantId = getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "reactiveLimits", variantId, oldValue, reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        var resource = getResource();
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
        return new ReactiveCapabilityCurveAdderImpl<>(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl<>(this);
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        for (Terminal terminal : getTerminals()) {
            ((TerminalImpl<?>) terminal).removeAsRegulatingPoint();
            ((TerminalImpl<?>) terminal).getReferrerManager().notifyOfRemoval();
        }
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeBattery(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }
}
