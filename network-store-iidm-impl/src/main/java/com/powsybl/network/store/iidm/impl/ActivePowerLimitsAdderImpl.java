/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import java.util.Collection;

import com.powsybl.iidm.network.ActivePowerLimits;
import com.powsybl.iidm.network.ActivePowerLimitsAdder;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryLimitAttributes;

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
    protected ActivePowerLimitsImpl createAndSetLimit(LimitsAttributes attributes) {
        owner.setActivePowerLimits(side, attributes, operationalGroupId);
        return new ActivePowerLimitsImpl(owner, attributes);
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
        return tl != null ? tl.getAcceptableDuration() : Integer.MAX_VALUE;
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
        return getOwner().getIdentifiable().getId();
    }
}
