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
class ReactiveCapabilityCurveAdderImpl<OWNER extends ReactiveLimitsOwner> implements ReactiveCapabilityCurveAdder {

    private final OWNER owner;

    private double minP;

    private double maxP;

    private TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points = new TreeMap<>();

    ReactiveCapabilityCurveAdderImpl(OWNER owner) {
        this.owner = owner;
    }

    @Override
    public PointAdder beginPoint() {
        return new PointAdderImpl(this);
    }

    public void addPoint(ReactiveCapabilityCurvePointAttributes pointAttributes) {
        points.put(pointAttributes.getP(), pointAttributes);
    }

    @Override
    public ReactiveCapabilityCurve add() {
        ReactiveCapabilityCurveAttributes attributes = ReactiveCapabilityCurveAttributes.builder()
                .points(points)
                .build();
        owner.setReactiveLimits(attributes);
        return new ReactiveCapabilityCurveImpl(attributes);
    }
}
