/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryLimitAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractLoadingLimits<T extends LoadingLimits> implements LoadingLimits {

    private static final class TemporaryLimitImpl implements LoadingLimits.TemporaryLimit {

        private final TemporaryLimitAttributes attributes;

        private TemporaryLimitImpl(TemporaryLimitAttributes attributes) {
            this.attributes = attributes;
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

    private final LimitsAttributes attributes;

    protected AbstractLoadingLimits(LimitsOwner<?> owner, LimitsAttributes attributes) {
        this.owner = Objects.requireNonNull(owner);
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    public double getPermanentLimit() {
        return attributes.getPermanentLimit();
    }

    @Override
    public T setPermanentLimit(double permanentLimit) {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit);
        attributes.setPermanentLimit(permanentLimit);
        return (T) this;
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return attributes.getTemporaryLimits() == null ? Collections.emptyList()
            : attributes.getTemporaryLimits().values().stream().sorted().map(TemporaryLimitImpl::new).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public LoadingLimits.TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        if (attributes.getTemporaryLimits() == null) {
            return null;
        }
        TemporaryLimitAttributes temporaryLimitAttributes = attributes.getTemporaryLimits().get(acceptableDuration);
        return temporaryLimitAttributes == null ? null : new TemporaryLimitImpl(temporaryLimitAttributes);
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        LoadingLimits.TemporaryLimit tl = getTemporaryLimit(acceptableDuration);
        return tl != null ? tl.getValue() : Double.NaN;
    }
}
