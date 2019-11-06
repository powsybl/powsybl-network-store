/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PointAdderImpl implements ReactiveCapabilityCurveAdder.PointAdder {

    private final ReactiveCapabilityCurveAdderImpl reactiveCapabilityCurveAdder;

    PointAdderImpl(ReactiveCapabilityCurveAdderImpl reactiveCapabilityCurveAdder) {
        this.reactiveCapabilityCurveAdder = reactiveCapabilityCurveAdder;
    }

    @Override
    public ReactiveCapabilityCurveAdder.PointAdder setP(double p) {
        // TODO
        return this;
    }

    @Override
    public ReactiveCapabilityCurveAdder.PointAdder setMinQ(double minQ) {
        // TODO
        return this;
    }

    @Override
    public ReactiveCapabilityCurveAdder.PointAdder setMaxQ(double maxQ) {
        // TODO
        return this;
    }

    @Override
    public ReactiveCapabilityCurveAdderImpl endPoint() {
        return reactiveCapabilityCurveAdder;
    }
}
