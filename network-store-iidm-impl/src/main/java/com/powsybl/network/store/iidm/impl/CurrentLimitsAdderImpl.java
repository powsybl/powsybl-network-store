/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.network.store.model.LimitsAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsAdderImpl<S, O extends LimitsOwner<S>>
        extends AbstractLoadingLimitsAdderImpl<S, O, CurrentLimits, CurrentLimitsAdder> implements CurrentLimitsAdder {

    public CurrentLimitsAdderImpl(S side, O owner, String operationalGroupId) {
        super(side, owner, operationalGroupId);
    }

    @Override
    public TemporaryLimitAdder<CurrentLimitsAdder> beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl<>(this);
    }

    @Override
    protected CurrentLimitsImpl<S, O> createAndSetLimit(LimitsAttributes attributes) {
        owner.setCurrentLimits(side, attributes, operationalGroupId);
        return new CurrentLimitsImpl<>(owner, side, operationalGroupId, attributes);
    }
}
