/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.CurrentLimitsAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TemporaryLimitAdderImpl implements CurrentLimitsAdder.TemporaryLimitAdder {

    private final CurrentLimitsAdderImpl adder;

    TemporaryLimitAdderImpl(CurrentLimitsAdderImpl adder) {
        this.adder = adder;
    }

    @Override
    public CurrentLimitsAdder.TemporaryLimitAdder setName(String name) {
        // TODO
        return this;
    }

    @Override
    public CurrentLimitsAdder.TemporaryLimitAdder setValue(double value) {
        // TODO
        return this;
    }

    @Override
    public CurrentLimitsAdder.TemporaryLimitAdder setAcceptableDuration(int duration) {
        // TODO
        return this;
    }

    @Override
    public CurrentLimitsAdder.TemporaryLimitAdder setFictitious(boolean fictitious) {
        // TODO
        return this;
    }

    @Override
    public CurrentLimitsAdder endTemporaryLimit() {
        return adder;
    }
}
