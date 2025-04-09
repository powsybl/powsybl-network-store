/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.network.store.iidm.impl.AbstractInjectionImpl;
import com.powsybl.network.store.iidm.impl.BatteryImpl;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.ActivePowerControlAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ActivePowerControlAdderImpl<I extends Injection<I>> extends AbstractIidmExtensionAdder<I, ActivePowerControl<I>> implements ActivePowerControlAdder<I> {

    private boolean participate;

    private double droop;

    private double participationFactor;

    private double minTargetP = Double.NaN;

    private double maxTargetP = Double.NaN;

    public ActivePowerControlAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected ActivePowerControl<I> createExtension(I injection) {
        if (injection instanceof GeneratorImpl || injection instanceof BatteryImpl) {
            ActivePowerControlAttributes attributes = ActivePowerControlAttributes.builder()
                    .droop(droop)
                    .participate(participate)
                    .participationFactor(participationFactor)
                    .minTargetP(minTargetP)
                    .maxTargetP(maxTargetP)
                    .build();
            ((AbstractInjectionImpl<?, ?>) injection).updateResourceWithoutNotification(res -> res.getAttributes().getExtensionAttributes().put(ActivePowerControl.NAME, attributes));
            return new ActivePowerControlImpl<>(injection);
        } else {
            throw new UnsupportedOperationException("Cannot set ActivePowerControl on this kind of component");
        }
    }

    @Override
    public ActivePowerControlAdder<I> withParticipate(boolean participate) {
        this.participate = participate;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withDroop(double droop) {
        this.droop = droop;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withParticipationFactor(double participationFactor) {
        this.participationFactor = participationFactor;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withMinTargetP(double minTargetP) {
        this.minTargetP = minTargetP;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withMaxTargetP(double maxTargetP) {
        this.maxTargetP = maxTargetP;
        return this;
    }
}
