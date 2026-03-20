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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.util.ReactiveCapabilityCurveUtil;
import org.apache.commons.lang3.mutable.MutableObject;

import static com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes.COMPARATOR;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

    static class PointImpl implements Point {

        private final ReactiveCapabilityCurvePointAttributes attributes;
        private final AbstractInjectionImpl<?, ?> owner;

        public PointImpl(ReactiveCapabilityCurvePointAttributes attributes, AbstractInjectionImpl<?, ?> injection) {
            this.attributes = attributes;
            this.owner = injection;
        }

        static PointImpl create(ReactiveCapabilityCurvePointAttributes attributes, AbstractInjectionImpl<?, ?> injection) {
            return new PointImpl(attributes, injection);
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

        @Override
        public boolean hasProperty() {
            Map<String, String> properties = attributes.getProperties();
            return properties != null && !properties.isEmpty();
        }

        @Override
        public boolean hasProperty(String key) {
            Map<String, String> properties = attributes.getProperties();
            return properties != null && properties.containsKey(key);
        }

        @Override
        public String getProperty(String key) {
            Map<String, String> properties = attributes.getProperties();
            return properties != null ? properties.get(key) : null;
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            Map<String, String> properties = attributes.getProperties();
            return properties != null ? properties.getOrDefault(key, defaultValue) : defaultValue;
        }

        @Override
        public String setProperty(String key, String value) {
            MutableObject<String> oldValue = new MutableObject<>();
            Map<String, String> properties = attributes.getProperties();
            if (properties == null) {
                properties = new HashMap<>();
            }
            oldValue.setValue(properties.put(key, value));

            Map<String, String> finalProperties = properties;
            owner.updateResourceWithoutNotification(r -> attributes.setProperties(finalProperties));
            return oldValue.getValue();
        }

        @Override
        public boolean removeProperty(String key) {
            Map<String, String> properties = attributes.getProperties();
            if (properties != null && properties.containsKey(key)) {
                owner.updateResourceWithoutNotification(r -> attributes.getProperties().remove(key));
                return true;
            }
            return false;
        }

        @Override
        public Set<String> getPropertyNames() {
            Map<String, String> properties = attributes.getProperties();
            return properties != null ? properties.keySet() : Collections.emptySet();
        }
    }

    private final ReactiveCapabilityCurveAttributes attributes;
    private final AbstractInjectionImpl<?, ?> owner;

    public ReactiveCapabilityCurveImpl(ReactiveCapabilityCurveAttributes attributes, AbstractInjectionImpl<?, ?> injection) {
        this.attributes = attributes;
        this.owner = injection;
    }

    @Override
    public Collection<Point> getPoints() {
        return Collections.unmodifiableCollection(attributes.getPoints().values().stream().map(attributes -> PointImpl.create(attributes, owner)).collect(Collectors.toList()));
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
            TreeMap<Double, ReactiveCapabilityCurve.Point> pointsMap = new TreeMap<>(COMPARATOR);
            attributes.getPoints().forEach((k, point) -> pointsMap.put(k, PointImpl.create(point, owner)));

            PointImpl extrapolatedPoint = (PointImpl) ReactiveCapabilityCurveUtil.extrapolateReactiveLimitsSlope(p,
                pointsMap, (localP, minQ, maxQ) -> PointImpl.create(ReactiveCapabilityCurvePointAttributes.builder().p(localP).minQ(minQ).maxQ(maxQ).build(), owner),
                attributes.getOwnerDescription());
            return getMinOrMaxQ.applyAsDouble(extrapolatedPoint.getAttributes());
        }
        if (p < this.getMinP()) { // p < minP
            ReactiveCapabilityCurvePointAttributes pMin = attributes.getPoints().firstEntry().getValue();
            return getMinOrMaxQ.applyAsDouble(pMin);
        } else { // p > maxP
            ReactiveCapabilityCurvePointAttributes pMax = attributes.getPoints().lastEntry().getValue();
            return getMinOrMaxQ.applyAsDouble(pMax);
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

    @Override
    public boolean hasProperty() {
        Map<String, String> properties = attributes.getProperties();
        return properties != null && !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(String key) {
        Map<String, String> properties = attributes.getProperties();
        return properties != null && properties.containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        Map<String, String> properties = attributes.getProperties();
        return properties != null ? properties.get(key) : null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Map<String, String> properties = attributes.getProperties();
        return properties != null ? properties.getOrDefault(key, defaultValue) : defaultValue;
    }

    @Override
    public String setProperty(String key, String value) {
        MutableObject<String> oldValue = new MutableObject<>();
        Map<String, String> properties = attributes.getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        oldValue.setValue(properties.put(key, value));

        Map<String, String> finalProperties = properties;
        owner.updateResourceWithoutNotification(r -> attributes.setProperties(finalProperties));
        return oldValue.getValue();
    }

    @Override
    public boolean removeProperty(String key) {
        Map<String, String> properties = attributes.getProperties();
        if (properties != null && properties.containsKey(key)) {
            owner.updateResourceWithoutNotification(r -> attributes.getProperties().remove(key));
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        Map<String, String> properties = attributes.getProperties();
        return properties != null ? properties.keySet() : Collections.emptySet();
    }
}
