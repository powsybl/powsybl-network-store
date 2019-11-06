/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.CurrentLimits;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsImpl implements CurrentLimits {

    public CurrentLimitsImpl(NetworkObjectIndex index) {
    }

    static CurrentLimitsImpl create(NetworkObjectIndex index) {
        return new CurrentLimitsImpl(index);
    }

    @Override
    public double getPermanentLimit() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CurrentLimits setPermanentLimit(double permanentLimit) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        throw new UnsupportedOperationException("TODO");
    }
}
