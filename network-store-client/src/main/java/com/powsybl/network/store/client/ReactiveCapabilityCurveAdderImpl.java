/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.network.store.model.ReactiveCapabilityCurvePointAttributes;
import com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes;

import java.util.TreeMap;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ReactiveCapabilityCurveAdderImpl implements ReactiveCapabilityCurveAdder {

    private double minP;

    private double maxP;

    private double minQ;

    private double maxQ;

    private TreeMap<Double, ReactiveCapabilityCurve.Point> points = new TreeMap<>();

    @Override
    public PointAdder beginPoint() {
        return new PointAdderImpl(this);
    }

    public void addPoint(ReactiveCapabilityCurvePointAttributes pointAttributes) {
        points.put(pointAttributes.getP(), new ReactiveCapabilityCurveImpl.PointImpl(pointAttributes));
    }

    @Override
    public ReactiveCapabilityCurve add() {
        ReactiveCapabilityCurveAttributes attributes = ReactiveCapabilityCurveAttributes.builder()
                .minP(minP)
                .maxP(maxP)
                .minQ(minQ)
                .maxQ(maxQ)
                .points(points)
                .build();
        return ReactiveCapabilityCurveImpl.create(attributes);
    }
}
