/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.ReactiveCapabilityCurvePointAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PointAdderImpl implements ReactiveCapabilityCurveAdder.PointAdder, Validable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PointAdderImpl.class);

    private double p = Double.NaN;

    private double minQ = Double.NaN;

    private double maxQ = Double.NaN;

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
        if (Double.isNaN(p)) {
            throw new ValidationException(this, "P is not set");
        }
        if (Double.isNaN(minQ)) {
            throw new ValidationException(this, "min Q is not set");
        }
        if (Double.isNaN(maxQ)) {
            throw new ValidationException(this, "max Q is not set");
        }
        ReactiveCapabilityCurvePointAttributes point = reactiveCapabilityCurveAdder.getPoint(p);
        if (point != null) {
            if (point.getMinQ() != minQ || point.getMaxQ() != maxQ) {
                throw new ValidationException(this,
                        "a point already exists for active power " + p  + " with a different reactive power range: [" +
                                minQ  + ", " + maxQ + "] != " + "[" + point.getMinQ() + ", " + point.getMaxQ() + "]");
            } else {
                LOGGER.warn("{}duplicate point for active power {}", getMessageHeader(), p);
            }
        }

        ReactiveCapabilityCurvePointAttributes attributes = ReactiveCapabilityCurvePointAttributes.builder()
                .p(p)
                .minQ(minQ)
                .maxQ(maxQ)
                .build();
        reactiveCapabilityCurveAdder.addPoint(attributes);
        return reactiveCapabilityCurveAdder;
    }

    @Override
    public String getMessageHeader() {
        return "reactiveCapabilityCurvePoint '" + toString() + "': ";
    }
}
