/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.network.store.model.ReactiveCapabilityCurvePointAttributes;
import com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    @Override
    public Collection<Point> getPoints() {
        return Collections.unmodifiableCollection(attributes.getPoints().values().stream().map(PointImpl::create).collect(Collectors.toList()));
    }

    @Override
    public int getPointCount() {
        return attributes.getPoints().size();
    }

    @Override
    public double getMinP() {
        return  attributes.getPoints().firstKey();
    }

    @Override
    public double getMaxP() {
        return attributes.getPoints().lastKey();
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.CURVE;
    }

    @Override
    public double getMinQ(double p) {

        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points = attributes.getPoints();
        assert points.size() >= 2;

        ReactiveCapabilityCurvePointAttributes pt = points.get(p);
        if  (pt != null) {
            return pt.getMinQ();
        } else {
            Map.Entry<Double, ReactiveCapabilityCurvePointAttributes> e1 = points.floorEntry(p);
            Map.Entry<Double, ReactiveCapabilityCurvePointAttributes> e2 = points.ceilingEntry(p);
            if (e1 == null && e2 != null) {
                return e2.getValue().getMinQ();
            } else if (e1 != null && e2 == null) {
                return e1.getValue().getMinQ();
            } else if (e1 != null && e2 != null) {
                ReactiveCapabilityCurvePointAttributes p1 = e1.getValue();
                ReactiveCapabilityCurvePointAttributes p2 = e2.getValue();
                return p1.getMinQ() + (p2.getMinQ() - p1.getMinQ()) / (p2.getP() - p1.getP()) * (p - p1.getP());
            } else {
                throw new AssertionError();
            }
        }
    }

    @Override
    public double getMaxQ(double p) {
        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points = attributes.getPoints();
        assert points.size() >= 2;

        ReactiveCapabilityCurvePointAttributes pt = points.get(p);
        if  (pt != null) {
            return pt.getMaxQ();
        } else {
            Map.Entry<Double, ReactiveCapabilityCurvePointAttributes> e1 = points.floorEntry(p);
            Map.Entry<Double, ReactiveCapabilityCurvePointAttributes> e2 = points.ceilingEntry(p);
            if (e1 == null && e2 != null) {
                return e2.getValue().getMaxQ();
            } else if (e1 != null && e2 == null) {
                return e1.getValue().getMaxQ();
            } else if (e1 != null && e2 != null) {
                ReactiveCapabilityCurvePointAttributes p1 = e1.getValue();
                ReactiveCapabilityCurvePointAttributes p2 = e2.getValue();
                return p1.getMaxQ() + (p2.getMaxQ() - p1.getMaxQ()) / (p2.getP() - p1.getP()) * (p - p1.getP());
            } else {
                throw new AssertionError();
            }
        }

    }
}
