/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.network.store.model.ReactiveCapabilityCurvePointAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PointAdderImpl implements ReactiveCapabilityCurveAdder.PointAdder {

    private double p;

    private double minQ;

    private double maxQ;

    private final ReactiveCapabilityCurveAdderImpl reactiveCapabilityCurveAdder;

    PointAdderImpl(ReactiveCapabilityCurveAdderImpl reactiveCapabilityCurveAdder) {
        this.reactiveCapabilityCurveAdder = reactiveCapabilityCurveAdder;
    }

    @Override
    public ReactiveCapabilityCurveAdder.PointAdder setP(double p) {
        this.p = p;
        return this;
    }

    @Override
    public ReactiveCapabilityCurveAdder.PointAdder setMinQ(double minQ) {
        this.minQ = minQ;
        return this;
    }

    @Override
    public ReactiveCapabilityCurveAdder.PointAdder setMaxQ(double maxQ) {
        this.maxQ = maxQ;
        return this;
    }

    @Override
    public ReactiveCapabilityCurveAdderImpl endPoint() {
        ReactiveCapabilityCurvePointAttributes attributes = ReactiveCapabilityCurvePointAttributes.builder()
                .p(p)
                .minQ(minQ)
                .maxQ(maxQ)
                .build();
        reactiveCapabilityCurveAdder.addPoint(attributes);
        return reactiveCapabilityCurveAdder;
    }
}
