/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryCurrentLimitAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ActivePowerLimitsImpl implements ActivePowerLimits {

    static class TemporaryLimitImpl implements LoadingLimits.TemporaryLimit {
        TemporaryCurrentLimitAttributes attributes;

        TemporaryLimitImpl(TemporaryCurrentLimitAttributes attributes) {
            this.attributes = attributes;
        }

        static ActivePowerLimitsImpl.TemporaryLimitImpl create(TemporaryCurrentLimitAttributes attributes) {
            return new ActivePowerLimitsImpl.TemporaryLimitImpl(attributes);
        }

        @Override
        public String getName() {
            return attributes.getName();
        }

        @Override
        public double getValue() {
            return attributes.getValue();
        }

        @Override
        public int getAcceptableDuration() {
            return attributes.getAcceptableDuration();
        }

        @Override
        public boolean isFictitious() {
            return attributes.isFictitious();
        }
    }

    private final LimitsOwner<?> owner;

    LimitsAttributes attributes;

    public ActivePowerLimitsImpl(LimitsOwner<?> owner, LimitsAttributes attributes) {
        this.owner = Objects.requireNonNull(owner);
        this.attributes = attributes;
    }

    @Override
    public double getPermanentLimit() {
        return attributes.getPermanentLimit();
    }

    @Override
    public ActivePowerLimits setPermanentLimit(double permanentLimit) {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit);
        attributes.setPermanentLimit(permanentLimit);
        return this;
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return Collections.unmodifiableCollection(attributes.getTemporaryLimits().values().stream().map(ActivePowerLimitsImpl.TemporaryLimitImpl::create).collect(Collectors.toList()));
    }

    @Override
    public LoadingLimits.TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        TemporaryCurrentLimitAttributes temporaryLimitAttributes = attributes.getTemporaryLimits().get(acceptableDuration);
        return new ActivePowerLimitsImpl.TemporaryLimitImpl(temporaryLimitAttributes);
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        LoadingLimits.TemporaryLimit tl = getTemporaryLimit(acceptableDuration);
        return tl != null ? tl.getValue() : Double.NaN;
    }
}

