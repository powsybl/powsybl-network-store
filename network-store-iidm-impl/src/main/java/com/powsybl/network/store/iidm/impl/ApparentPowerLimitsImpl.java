/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ApparentPowerLimits;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.network.store.model.LimitsAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ApparentPowerLimitsImpl<S, O extends LimitsOwner<S>> extends AbstractLoadingLimits<S, O, ApparentPowerLimitsImpl<S, O>> implements ApparentPowerLimits {

    public ApparentPowerLimitsImpl(O owner, S side, String operationalGroupId, LimitsAttributes attributes) {
        super(owner, side, operationalGroupId, attributes);
    }

    @Override
    public LimitType getLimitType() {
        return LimitType.APPARENT_POWER;
    }

    @Override
    public void remove() {
        owner.setApparentPowerLimits(side, null, operationalGroupId);
    }
}

