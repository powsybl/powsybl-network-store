/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.iidm.impl.AbstractInjectionImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ActivePowerControlImpl<I extends Injection<I>> extends AbstractExtension<I> implements ActivePowerControl<I> {

    public ActivePowerControlImpl(I injection) {
        super(injection);
    }

    @Override
    public boolean isParticipate() {
        return getInjection().getResource().getAttributes().getActivePowerControl().isParticipate();
    }

    @Override
    public void setParticipate(boolean participate) {
        boolean oldValue = getInjection().getResource().getAttributes().getActivePowerControl().isParticipate();
        if (oldValue != participate) {
            getInjection().updateResource(res -> res.getAttributes().getActivePowerControl().setParticipate(participate));
            String variantId = getInjection().getNetwork().getVariantManager().getWorkingVariantId();
            getInjection().getNetwork().getIndex().notifyUpdate(getInjection(), "participate", variantId, oldValue, participate);
        }
    }

    @Override
    public double getDroop() {
        return getInjection().getResource().getAttributes().getActivePowerControl().getDroop();
    }

    @Override
    public void setDroop(double droop) {
        double oldValue = getInjection().getResource().getAttributes().getActivePowerControl().getDroop();
        if (oldValue != droop) {
            getInjection().updateResource(res -> res.getAttributes().getActivePowerControl().setDroop(droop));
            String variantId = getInjection().getNetwork().getVariantManager().getWorkingVariantId();
            getInjection().getNetwork().getIndex().notifyUpdate(getInjection(), "droop", variantId, oldValue, droop);
        }
    }

    @Override
    public double getParticipationFactor() {
        return getInjection().getResource().getAttributes().getActivePowerControl().getParticipationFactor();
    }

    @Override
    public void setParticipationFactor(double participationFactor) {
        getInjection().updateResource(res -> res.getAttributes().getActivePowerControl().setParticipationFactor(participationFactor));
    }

    private AbstractInjectionImpl<?, ?> getInjection() {
        return (AbstractInjectionImpl<?, ?>) getExtendable();
    }

}
