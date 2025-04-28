/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes;
import com.powsybl.network.store.model.ReactiveCapabilityCurvePointAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.util.ReactiveCapabilityCurveUtil;

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

        protected ReactiveCapabilityCurvePointAttributes getAttributes() {
            return attributes;
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
        return attributes.getPoints().firstKey();
    }

    @Override
    public double getMaxP() {
        return attributes.getPoints().lastKey();
    }

    @Override
    public double getMinQ(double p) {
        return getMinQ(p, false);
    }

    @Override
    public double getMinQ(double p, boolean extrapolateReactiveLimitSlope) {
        return getReactiveLimit(p, extrapolateReactiveLimitSlope, ReactiveCapabilityCurvePointAttributes::getMinQ);
    }

    @Override
    public double getMaxQ(double p, boolean extrapolateReactiveLimitSlope) {
        return getReactiveLimit(p, extrapolateReactiveLimitSlope, ReactiveCapabilityCurvePointAttributes::getMaxQ);
    }

    @Override
    public double getMaxQ(double p) {
        return getMaxQ(p, false);
    }

    private double getReactiveLimit(double p, boolean extrapolateReactiveLimitSlope, ToDoubleFunction<ReactiveCapabilityCurvePointAttributes> getMinOrMaxQ) {
        checkPointsSize(attributes.getPoints());

        // First case : searched point is one of the points defining the curve
        ReactiveCapabilityCurvePointAttributes pt = attributes.getPoints().get(p);
        if (pt != null) {
            return getMinOrMaxQ.applyAsDouble(pt);
        }

        // Second case : searched point is between minP and maxP
        if (p >= this.getMinP() && p <= this.getMaxP()) {
            ReactiveCapabilityCurvePointAttributes p1 = attributes.getPoints().floorEntry(p).getValue();
            ReactiveCapabilityCurvePointAttributes p2 = attributes.getPoints().ceilingEntry(p).getValue();
            return getMinOrMaxQ.applyAsDouble(p1) + (getMinOrMaxQ.applyAsDouble(p2) - getMinOrMaxQ.applyAsDouble(p1)) / (p2.getP() - p1.getP()) * (p - p1.getP());
        }

        // Third case : searched point is outside minP and maxP
        if (extrapolateReactiveLimitSlope) {
            // Points map
            TreeMap<Double, ReactiveCapabilityCurve.Point> pointsMap = new TreeMap<>();
            attributes.getPoints().forEach((k, point) -> pointsMap.put(k, PointImpl.create(point)));

            PointImpl extrapolatedPoint = (PointImpl) ReactiveCapabilityCurveUtil.extrapolateReactiveLimitsSlope(p,
                pointsMap, (localP, minQ, maxQ) -> PointImpl.create(new ReactiveCapabilityCurvePointAttributes(localP, minQ, maxQ)),
                attributes.getOwnerDescription());
            return getMinOrMaxQ.applyAsDouble(extrapolatedPoint.getAttributes());
        } else {
            if (p < this.getMinP()) { // p < minP
                ReactiveCapabilityCurvePointAttributes pMin = attributes.getPoints().firstEntry().getValue();
                return getMinOrMaxQ.applyAsDouble(pMin);
            } else { // p > maxP
                ReactiveCapabilityCurvePointAttributes pMax = attributes.getPoints().lastEntry().getValue();
                return getMinOrMaxQ.applyAsDouble(pMax);
            }
        }
    }

    private static void checkPointsSize(TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points) {
        if (points.size() < 2) {
            throw new IllegalStateException("a reactive capability curve should have at least two points");
        }
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.CURVE;
    }
}
