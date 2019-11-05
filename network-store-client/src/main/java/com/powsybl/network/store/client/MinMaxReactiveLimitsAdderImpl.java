/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.MinMaxReactiveLimitsAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MinMaxReactiveLimitsAdderImpl implements MinMaxReactiveLimitsAdder {

    @Override
    public MinMaxReactiveLimitsAdder setMinQ(double minQ) {
        return this;
    }

    @Override
    public MinMaxReactiveLimitsAdder setMaxQ(double maxQ) {
        return this;
    }

    @Override
    public MinMaxReactiveLimits add() {
        return new MinMaxReactiveLimitsImpl();
    }
}
