/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.network.store.model.CurrentLimitsAttributes;
import com.powsybl.network.store.model.TemporaryCurrentLimitAttributes;

import java.util.TreeMap;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CurrentLimitsAdderImpl<S, OWNER extends CurrentLimitsOwner<S>> implements CurrentLimitsAdder {

    private final OWNER owner;

    private final S side;

    private double permanentLimit;

    private TreeMap<Integer, TemporaryCurrentLimitAttributes> temporaryLimits = new TreeMap<>();

    CurrentLimitsAdderImpl(S side, OWNER owner) {
        this.owner = owner;
        this.side = side;
    }

    @Override
    public CurrentLimitsAdder setPermanentLimit(double permanentLimit) {
        this.permanentLimit = permanentLimit;
        return this;
    }

    @Override
    public TemporaryLimitAdder beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl(this);
    }

    public void addTemporaryLimit(TemporaryCurrentLimitAttributes temporaryLimitAttribute) {
        temporaryLimits.put(temporaryLimitAttribute.getAcceptableDuration(), temporaryLimitAttribute);
    }

    @Override
    public CurrentLimits add() {
        CurrentLimitsAttributes attributes = CurrentLimitsAttributes.builder()
                .permanentLimit(permanentLimit)
                .temporaryLimits(temporaryLimits)
                .build();
        owner.setCurrentLimits(side, attributes);
        return new CurrentLimitsImpl(attributes);
    }
}
