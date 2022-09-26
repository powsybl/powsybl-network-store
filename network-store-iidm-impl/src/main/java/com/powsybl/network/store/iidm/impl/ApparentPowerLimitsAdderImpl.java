/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ApparentPowerLimits;
import com.powsybl.iidm.network.ApparentPowerLimitsAdder;
import com.powsybl.network.store.model.LimitsAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ApparentPowerLimitsAdderImpl<S, O extends LimitsOwner<S>>
        extends AbstractLoadingLimitsAdderImpl<S, O, ApparentPowerLimits, ApparentPowerLimitsAdder>
        implements ApparentPowerLimitsAdder {

    ApparentPowerLimitsAdderImpl(S side, O owner) {
        super(side, owner);
    }

    @Override
    public TemporaryLimitAdder<ApparentPowerLimitsAdder> beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl<>(this);
    }

    @Override
    protected ApparentPowerLimitsImpl createAndSetLimit(LimitsAttributes attributes) {
        owner.setApparentPowerLimits(side, attributes);
        return new ApparentPowerLimitsImpl(owner, attributes);
    }
}
