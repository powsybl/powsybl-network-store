/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.LoadingLimitsAdder.TemporaryLimitAdder;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.TemporaryCurrentLimitAttributes;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TemporaryLimitCurrentLimitAdderImpl implements TemporaryLimitAdder {

    private String name;

    private double value = Double.NaN;

    private Integer acceptableDuration;

    private boolean fictitious;

    private final CurrentLimitsAdderImpl currentLimitAdder;

    private boolean ensureNameUnicity = false;

    TemporaryLimitCurrentLimitAdderImpl(CurrentLimitsAdderImpl currentLimitAdder) {
        this.currentLimitAdder = currentLimitAdder;
    }

    @Override
    public TemporaryLimitAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TemporaryLimitAdder setValue(double value) {
        this.value = value;
        return this;
    }

    @Override
    public TemporaryLimitAdder setAcceptableDuration(int acceptableDuration) {
        this.acceptableDuration = acceptableDuration;
        return this;
    }

    @Override
    public TemporaryLimitAdder setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    @Override
    public TemporaryLimitAdder ensureNameUnicity() {
        this.ensureNameUnicity = true;
        return this;
    }

    @Override
    public CurrentLimitsAdder endTemporaryLimit() {
        if (Double.isNaN(value)) {
            throw new ValidationException(currentLimitAdder.getOwner(), "temporary limit value is not set");
        }
        if (value <= 0) {
            throw new ValidationException(currentLimitAdder.getOwner(), "temporary limit value must be > 0");
        }
        if (acceptableDuration == null) {
            throw new ValidationException(currentLimitAdder.getOwner(), "acceptable duration is not set");
        }
        if (acceptableDuration < 0) {
            throw new ValidationException(currentLimitAdder.getOwner(), "acceptable duration must be >= 0");
        }
        checkAndGetUniqueName();

        TemporaryCurrentLimitAttributes attributes = TemporaryCurrentLimitAttributes.builder()
                .name(name)
                .value(value)
                .acceptableDuration(acceptableDuration)
                .fictitious(fictitious)
                .build();
        currentLimitAdder.addTemporaryLimit(attributes);
        return currentLimitAdder;
    }

    private void checkAndGetUniqueName() {
        if (name == null) {
            throw new ValidationException(currentLimitAdder.getOwner(), "name is not set");
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
        Collection<TemporaryCurrentLimitAttributes> values = currentLimitAdder.getTemporaryLimits().values();
        return values.stream().anyMatch(t -> t.getName().equals(name));
    }
}
