/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.LoadingLimitsAdder;
import com.powsybl.iidm.network.LoadingLimitsAdder.TemporaryLimitAdder;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.TemporaryLimitAttributes;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
class TemporaryLimitAdderImpl<S,
                              OWNER extends LimitsOwner<S>,
                              L extends LoadingLimits,
                              A extends LoadingLimitsAdder<L, A>,
                              B extends LoadingLimitsAdderExt<S, OWNER, L, A>>
        implements TemporaryLimitAdder<A> {

    private final B activePowerLimitsAdder;

    private String name;

    private double value = Double.NaN;

    private Integer acceptableDuration;

    private boolean fictitious;

    private boolean ensureNameUnicity = false;

    TemporaryLimitAdderImpl(B activePowerLimitsAdder) {
        this.activePowerLimitsAdder = Objects.requireNonNull(activePowerLimitsAdder);
    }

    @Override
    public TemporaryLimitAdder<A> setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TemporaryLimitAdder<A> setValue(double value) {
        this.value = value;
        return this;
    }

    @Override
    public TemporaryLimitAdder<A> setAcceptableDuration(int acceptableDuration) {
        this.acceptableDuration = acceptableDuration;
        return this;
    }

    @Override
    public TemporaryLimitAdder<A> setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    @Override
    public TemporaryLimitAdder<A> ensureNameUnicity() {
        this.ensureNameUnicity = true;
        return this;
    }

    @Override
    public A endTemporaryLimit() {
        if (Double.isNaN(value)) {
            throw new ValidationException(activePowerLimitsAdder.getOwner(), "temporary limit value is not set");
        }
        if (value <= 0) {
            throw new ValidationException(activePowerLimitsAdder.getOwner(), "temporary limit value must be > 0");
        }
        if (acceptableDuration == null) {
            throw new ValidationException(activePowerLimitsAdder.getOwner(), "acceptable duration is not set");
        }
        if (acceptableDuration < 0) {
            throw new ValidationException(activePowerLimitsAdder.getOwner(), "acceptable duration must be >= 0");
        }
        checkAndGetUniqueName();

        TemporaryLimitAttributes attributes = TemporaryLimitAttributes.builder()
                .name(name)
                .value(value)
                .acceptableDuration(acceptableDuration)
                .fictitious(fictitious)
                .build();
        activePowerLimitsAdder.addTemporaryLimit(attributes);
        return (A) activePowerLimitsAdder;
    }

    private void checkAndGetUniqueName() {
        if (name == null) {
            throw new ValidationException(activePowerLimitsAdder.getOwner(), "name is not set");
        }
        if (ensureNameUnicity) {
            int i = 0;
            String uniqueName = name;
            while (i < Integer.MAX_VALUE && nameExists(uniqueName)) {
                uniqueName = name + "#" + i;
                i++;
            }
            name = uniqueName;
        }
    }

    private boolean nameExists(String name) {
        Collection<TemporaryLimitAttributes> values = activePowerLimitsAdder.getTemporaryLimits().values();
        return values.stream().anyMatch(t -> t.getName().equals(name));
    }
}
