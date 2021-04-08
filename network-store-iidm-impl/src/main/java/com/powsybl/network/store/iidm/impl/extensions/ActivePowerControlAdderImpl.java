/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ActivePowerControlAdderImpl<I extends Injection<I>> extends AbstractExtensionAdder<I, ActivePowerControl<I>> implements ActivePowerControlAdder<I> {

    private boolean participate;

    private float droop;

    public ActivePowerControlAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected ActivePowerControl<I> createExtension(I injection) {
        return new ActivePowerControlImpl(injection, participate, droop);
    }

    @Override
    public ActivePowerControlAdder<I> withParticipate(boolean participate) {
        this.participate = participate;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withDroop(float droop) {
        this.droop = droop;
        return this;
    }
}
