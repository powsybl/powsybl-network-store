/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.iidm.impl.extensions.ActivePowerControlImpl;
import com.powsybl.network.store.model.*;

import java.util.Collection;

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
        return checkResource().getAttributes().getTargetP();
    }

    @Override
    public Battery setTargetP(double targetP) {
        var resource = checkResource();
        ValidationUtil.checkP0(this, targetP, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        ValidationUtil.checkActivePowerLimits(this, getMinP(), getMaxP());
        double oldValue = resource.getAttributes().getTargetP();
        resource.getAttributes().setTargetP(targetP);
        updateResource();
        String variantId = getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "targetP", variantId, oldValue, targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return checkResource().getAttributes().getTargetQ();
    }

    @Override
    public Battery setTargetQ(double targetQ) {
        var resource = checkResource();
        ValidationUtil.checkQ0(this, targetQ, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = resource.getAttributes().getTargetQ();
        resource.getAttributes().setTargetQ(targetQ);
        updateResource();
        String variantId = getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "targetQ", variantId, oldValue, targetQ);
        return this;

    }

    @Override
    public double getMinP() {
        return checkResource().getAttributes().getMinP();
    }

    @Override
    public Battery setMinP(double minP) {
        var resource = checkResource();
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActivePowerLimits(this, minP, getMaxP());
        double oldValue = resource.getAttributes().getMinP();
        resource.getAttributes().setMinP(minP);
        updateResource();
        index.notifyUpdate(this, "minP", oldValue, minP);
        return this;

    }

    @Override
    public double getMaxP() {
        return checkResource().getAttributes().getMaxP();
    }

    @Override
    public Battery setMaxP(double maxP) {
        var resource = checkResource();
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, getMinP(), maxP, getP0());
        double oldValue = resource.getAttributes().getMaxP();
        resource.getAttributes().setMaxP(maxP);
        updateResource();
        index.notifyUpdate(this, "maxP", oldValue, maxP);
        return this;

    }

    private <E extends Extension<Battery>> E createActivePowerControlExtension() {
        E extension = null;
        var resource = checkResource();
        ActivePowerControlAttributes attributes = resource.getAttributes().getActivePowerControl();
        if (attributes != null) {
            extension = (E) new ActivePowerControlImpl<>(getInjection(), attributes.isParticipate(), attributes.getDroop());
        }
        return extension;
    }

    @Override
    public <E extends Extension<Battery>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        var resource = checkResource();
        if (type == ActivePowerControl.class) {
            ActivePowerControl<Battery> activePowerControl = (ActivePowerControl) extension;
            resource.getAttributes().setActivePowerControl(ActivePowerControlAttributes.builder()
                    .participate(activePowerControl.isParticipate())
                    .droop(activePowerControl.getDroop())
                    .build());
        }
    }

    @Override
    public <E extends Extension<Battery>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (type == ActivePowerControl.class) {
            extension = createActivePowerControlExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<Battery>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if (name.equals("activePowerControl")) {
            extension = createActivePowerControlExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<Battery>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createActivePowerControlExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        var resource = checkResource();
        ReactiveLimitsAttributes oldValue = resource.getAttributes().getReactiveLimits();
        resource.getAttributes().setReactiveLimits(reactiveLimits);
        updateResource();
        index.notifyUpdate(this, "reactiveLimits", oldValue, reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        var resource = checkResource();
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
    public void remove(boolean removeDanglingSwitches) {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        index.removeBattery(resource.getId());
        invalidateCalculatedBuses(getTerminals());
        index.notifyAfterRemoval(resource.getId());
        if (removeDanglingSwitches) {
            getTerminal().removeDanglingSwitches();
        }
    }
}
