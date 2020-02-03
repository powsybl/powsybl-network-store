/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MinMaxReactiveLimitsImpl implements MinMaxReactiveLimits {

    private final MinMaxReactiveLimitsAttributes attributes;

    MinMaxReactiveLimitsImpl(MinMaxReactiveLimitsAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public double getMinQ() {
        return attributes.getMinQ();
    }

    @Override
    public double getMaxQ() {
        return attributes.getMaxQ();
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.MIN_MAX;
    }

    @Override
    public double getMinQ(double p) {
        return attributes.getMinQ();
    }

    @Override
    public double getMaxQ(double p) {
        return attributes.getMaxQ();
    }
}
