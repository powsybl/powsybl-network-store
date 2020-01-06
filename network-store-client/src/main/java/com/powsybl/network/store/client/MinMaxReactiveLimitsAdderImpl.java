/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.MinMaxReactiveLimitsAdder;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MinMaxReactiveLimitsAdderImpl implements MinMaxReactiveLimitsAdder {

    private double minQ = Double.NaN;

    private double maxQ = Double.NaN;

    @Override
    public MinMaxReactiveLimitsAdder setMinQ(double minQ) {
        this.minQ = minQ;
        return this;
    }

    @Override
    public MinMaxReactiveLimitsAdder setMaxQ(double maxQ) {
        this.maxQ = maxQ;
        return this;
    }

    @Override
    public MinMaxReactiveLimits add() {
        if (Double.isNaN(minQ)) {
            throw new PowsyblException("minimum reactive power is not set");
        }
        if (Double.isNaN(maxQ)) {
            throw new PowsyblException("maximum reactive power is not set");
        }
        if (maxQ < minQ) {
            throw new PowsyblException("maximum reactive power is expected to be greater than or equal to minimum reactive power");
        }
        MinMaxReactiveLimitsAttributes attributes = MinMaxReactiveLimitsAttributes.builder()
                .minQ(minQ)
                .maxQ(maxQ)
                .build();
        return new MinMaxReactiveLimitsImpl(attributes);
    }
}
