/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ActivePowerLimitsAdder;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.TemporaryCurrentLimitAttributes;

import java.util.Collection;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
class TemporaryLimitActiveLimitAdderImpl implements ActivePowerLimitsAdder.TemporaryLimitAdder {

    private String name;

    private double value = Double.NaN;

    private Integer acceptableDuration;

    private boolean fictitious;

    private final ActivePowerLimitsAdderImpl activePowerLimitsAdder;

    private boolean ensureNameUnicity = false;

    TemporaryLimitActiveLimitAdderImpl(ActivePowerLimitsAdderImpl activePowerLimitsAdder) {
        this.activePowerLimitsAdder = activePowerLimitsAdder;
    }

    @Override
    public ActivePowerLimitsAdder.TemporaryLimitAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ActivePowerLimitsAdder.TemporaryLimitAdder setValue(double value) {
        this.value = value;
        return this;
    }

    @Override
    public ActivePowerLimitsAdder.TemporaryLimitAdder setAcceptableDuration(int acceptableDuration) {
        this.acceptableDuration = acceptableDuration;
        return this;
    }

    @Override
    public ActivePowerLimitsAdder.TemporaryLimitAdder setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    @Override
    public ActivePowerLimitsAdder.TemporaryLimitAdder ensureNameUnicity() {
        this.ensureNameUnicity = true;
        return this;
    }

    @Override
    public ActivePowerLimitsAdder endTemporaryLimit() {
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

        TemporaryCurrentLimitAttributes attributes = TemporaryCurrentLimitAttributes.builder()
                .name(name)
                .value(value)
                .acceptableDuration(acceptableDuration)
                .fictitious(fictitious)
                .build();
        activePowerLimitsAdder.addTemporaryLimit(attributes);
        return activePowerLimitsAdder;
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
        Collection<TemporaryCurrentLimitAttributes> values = activePowerLimitsAdder.getTemporaryLimits().values();
        return values.stream().anyMatch(t -> t.getName().equals(name));
    }
}
