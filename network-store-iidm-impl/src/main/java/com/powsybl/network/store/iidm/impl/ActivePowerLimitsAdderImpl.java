/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ActivePowerLimits;
import com.powsybl.iidm.network.ActivePowerLimitsAdder;
import com.powsybl.network.store.model.LimitsAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ActivePowerLimitsAdderImpl<S, O extends LimitsOwner<S>>
        extends AbstractLoadingLimitsAdderImpl<S, O, ActivePowerLimits, ActivePowerLimitsAdder>
        implements ActivePowerLimitsAdder {

    ActivePowerLimitsAdderImpl(S side, O owner, String operationalGroupId) {
        super(side, owner, operationalGroupId);
    }

    @Override
    public TemporaryLimitAdder<ActivePowerLimitsAdder> beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl<>(this);
    }

    @Override
    protected ActivePowerLimitsImpl<S, O> createAndSetLimit(LimitsAttributes attributes) {
        owner.setActivePowerLimits(side, attributes, operationalGroupId);
        return new ActivePowerLimitsImpl<>(owner, side, operationalGroupId, attributes);
    }
}
