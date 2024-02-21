/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import java.util.Collection;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryLimitAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CurrentLimitsAdderImpl<S, O extends LimitsOwner<S>>
        extends AbstractLoadingLimitsAdderImpl<S, O, CurrentLimits, CurrentLimitsAdder> implements CurrentLimitsAdder {

    CurrentLimitsAdderImpl(S side, O owner) {
        super(side, owner);
    }

    @Override
    public TemporaryLimitAdder<CurrentLimitsAdder> beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl<>(this);
    }

    @Override
    protected CurrentLimitsImpl createAndSetLimit(LimitsAttributes attributes) {
        owner.setCurrentLimits(side, attributes, null);
        return new CurrentLimitsImpl(owner, attributes);
    }

    @Override
    public double getTemporaryLimitValue(String name) {
        TemporaryLimitAttributes tl = getTemporaryLimits().values().stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElse(null);
        return tl != null ? tl.getValue() : Double.NaN;
    }

    @Override
    public int getTemporaryLimitAcceptableDuration(String name) {
        TemporaryLimitAttributes tl = getTemporaryLimits().values().stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElse(null);
        return tl != null ? tl.getAcceptableDuration() : 0;
    }

    @Override
    public double getLowestTemporaryLimitValue() {
        return getTemporaryLimits().values().stream()
                .mapToDouble(TemporaryLimitAttributes::getValue)
                .min()
                .orElse(Double.NaN);
    }

    @Override
    public Collection<String> getTemporaryLimitNames() {
        return getTemporaryLimits().values().stream()
                .map(TemporaryLimitAttributes::getName)
                .toList();
    }

    @Override
    public void removeTemporaryLimit(String name) {
        getTemporaryLimits().values().removeIf(t -> t.getName().equals(name));
    }

    @Override
    public String getOwnerId() {
        return owner.getIdentifiable().getId();
    }
}
