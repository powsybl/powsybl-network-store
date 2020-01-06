/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.network.store.model.ReactiveCapabilityCurvePointAttributes;
import com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

    static class PointImpl implements Point {

        private final ReactiveCapabilityCurvePointAttributes attributes;

        public PointImpl(ReactiveCapabilityCurvePointAttributes attributes) {
            this.attributes = attributes;
        }

        static PointImpl create(ReactiveCapabilityCurvePointAttributes attributes) {
            return new PointImpl(attributes);
        }

        @Override
        public double getP() {
            return attributes.getP();
        }

        @Override
        public double getMinQ() {
            return attributes.getMinQ();
        }

        @Override
        public double getMaxQ() {
            return attributes.getMaxQ();
        }

    }

    private final ReactiveCapabilityCurveAttributes attributes;

    public ReactiveCapabilityCurveImpl(ReactiveCapabilityCurveAttributes attributes) {
        this.attributes = attributes;
    }

    static ReactiveCapabilityCurveImpl create(ReactiveCapabilityCurveAttributes attributes) {
        return new ReactiveCapabilityCurveImpl(attributes);
    }

    @Override
    public Collection<Point> getPoints() {
        return Collections.unmodifiableCollection(attributes.getPoints().values());
    }

    @Override
    public int getPointCount() {
        return attributes.getPointCount();
    }

    @Override
    public double getMinP() {
        return  attributes.getMinP();
    }

    @Override
    public double getMaxP() {
        return attributes.getMaxP();
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.CURVE;
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
