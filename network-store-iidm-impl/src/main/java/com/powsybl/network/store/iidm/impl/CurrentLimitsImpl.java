/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.CurrentLimitsAttributes;
import com.powsybl.network.store.model.TemporaryCurrentLimitAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsImpl implements CurrentLimits {

    static class TemporaryLimitImpl implements TemporaryLimit {

        private TemporaryCurrentLimitAttributes attributes;

        TemporaryLimitImpl(TemporaryCurrentLimitAttributes attributes) {
            this.attributes = attributes;
        }

        static TemporaryLimitImpl create(TemporaryCurrentLimitAttributes attributes) {
            return new TemporaryLimitImpl(attributes);
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

    private final CurrentLimitsOwner<?> owner;

    private final CurrentLimitsAttributes attributes;

    public CurrentLimitsImpl(CurrentLimitsOwner<?> owner, CurrentLimitsAttributes attributes) {
        this.owner = Objects.requireNonNull(owner);
        this.attributes = attributes;
    }

    @Override
    public double getPermanentLimit() {
        return attributes.getPermanentLimit();
    }

    @Override
    public CurrentLimits setPermanentLimit(double permanentLimit) {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit);
        attributes.setPermanentLimit(permanentLimit);
        owner.getObject().updateResource();
        return this;
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return Collections.unmodifiableCollection(attributes.getTemporaryLimits().values().stream().map(TemporaryLimitImpl::create).collect(Collectors.toList()));
    }

    @Override
    public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        TemporaryCurrentLimitAttributes temporaryLimitAttributes = attributes.getTemporaryLimits().get(acceptableDuration);
        return new TemporaryLimitImpl(temporaryLimitAttributes);
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        TemporaryLimit tl = getTemporaryLimit(acceptableDuration);
        return tl != null ? tl.getValue() : Double.NaN;
    }
}
