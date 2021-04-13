/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.iidm.impl.*;
import com.powsybl.network.store.model.ActivePowerControlAttributes;
import com.powsybl.network.store.model.InjectionAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ActivePowerControlImpl<I extends Injection<I>> implements ActivePowerControl<I> {

    private I injection;

    public ActivePowerControlImpl(I injection) {
        this.injection = injection;
    }

    public ActivePowerControlImpl(I injection, boolean isParticipate, float droop) {
        this(injection);
        if (injection instanceof GeneratorImpl || injection instanceof BatteryImpl) {
            getInjectionResources().setActivePowerControl(ActivePowerControlAttributes.builder()
                    .droop(droop)
                    .participate(isParticipate)
                    .build());
        } else {
            throw new PowsyblException("Cannot set ActivePowerControl on this kind of component");
        }
    }

    @Override
    public boolean isParticipate() {
        return getInjectionResources().getActivePowerControl().isParticipate();
    }

    @Override
    public void setParticipate(boolean isParticipate) {
        getInjectionResources().getActivePowerControl().setParticipate(isParticipate);
    }

    @Override
    public float getDroop() {
        return getInjectionResources().getActivePowerControl().getDroop();
    }

    @Override
    public void setDroop(float droop) {
        getInjectionResources().getActivePowerControl().setDroop(droop);
    }

    @Override
    public I getExtendable() {
        return injection;
    }

    @Override
    public void setExtendable(I i) {
        this.injection = i;
    }

    private InjectionAttributes getInjectionResources() {
        if (injection instanceof GeneratorImpl) {
            return ((GeneratorImpl) injection).getResource().getAttributes();
        } else if (injection instanceof BatteryImpl) {
            return ((BatteryImpl) injection).getResource().getAttributes();
        } else {
            throw new PowsyblException("Cannot set ActivePowerControl on this kind of component");
        }
    }
}
