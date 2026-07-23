/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.network.store.model.LimitsAttributes;
import lombok.EqualsAndHashCode;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
// EqualsAndHashCode is needed for tck tests
@EqualsAndHashCode
public class CurrentLimitsImpl<S, O extends LimitsOwner<S>> extends AbstractLoadingLimits<S, O, CurrentLimitsImpl<S, O>> implements CurrentLimits {

    public CurrentLimitsImpl(O owner, S side, String operationalGroupId, LimitsAttributes attributes) {
        super(owner, side, operationalGroupId, attributes);
    }

    @Override
    public LimitType getLimitType() {
        return LimitType.CURRENT;
    }

    @Override
    public void remove() {
        owner.setCurrentLimits(side, null, operationalGroupId);
    }
}
