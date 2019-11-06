/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CurrentLimitsAdderImpl implements CurrentLimitsAdder {

    private final NetworkObjectIndex index;

    CurrentLimitsAdderImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    @Override
    public CurrentLimitsAdder setPermanentLimit(double limit) {
        // TODO
        return this;
    }

    @Override
    public TemporaryLimitAdder beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl(this);
    }

    @Override
    public CurrentLimits add() {
        return CurrentLimitsImpl.create(index);
    }
}
