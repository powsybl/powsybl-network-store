/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.network.store.model.TemporaryCurrentLimitAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TemporaryLimitAdderImpl implements CurrentLimitsAdder.TemporaryLimitAdder {

    private String name;

    private double value = Double.NaN;

    private int acceptableDuration;

    private boolean fictitious;

    private final CurrentLimitsAdderImpl currentLimitAdder;

    TemporaryLimitAdderImpl(CurrentLimitsAdderImpl currentLimitAdder) {
        this.currentLimitAdder = currentLimitAdder;
    }

    @Override
    public CurrentLimitsAdder.TemporaryLimitAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public CurrentLimitsAdder.TemporaryLimitAdder setValue(double value) {
        this.value = value;
        return this;
    }

    @Override
    public CurrentLimitsAdder.TemporaryLimitAdder setAcceptableDuration(int acceptableDuration) {
        this.acceptableDuration = acceptableDuration;
        return this;
    }

    @Override
    public CurrentLimitsAdder.TemporaryLimitAdder setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    @Override
    public CurrentLimitsAdder endTemporaryLimit() {
        TemporaryCurrentLimitAttributes attributes = TemporaryCurrentLimitAttributes.builder()
                .name(name)
                .value(value)
                .acceptableDuration(acceptableDuration)
                .fictitious(fictitious)
                .build();
        currentLimitAdder.addTemporaryLimit(attributes);
        return currentLimitAdder;
    }
}
