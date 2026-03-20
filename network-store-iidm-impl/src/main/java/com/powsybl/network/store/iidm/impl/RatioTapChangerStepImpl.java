/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TapChangerStepAttributes;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@EqualsAndHashCode
public class RatioTapChangerStepImpl implements RatioTapChangerStep {

    private final RatioTapChangerImpl ratioTapChanger;

    private final int tapPositionIndex;

    RatioTapChangerStepImpl(RatioTapChangerImpl ratioTapChanger, int tapPositionIndex) {
        this.ratioTapChanger = Objects.requireNonNull(ratioTapChanger);
        this.tapPositionIndex = tapPositionIndex;
    }

    private AbstractIdentifiableImpl<?, ?> getTransformer() {
        return ratioTapChanger.getTransformer();
    }

    private TapChangerStepAttributes getTapChangerStepAttributes(Resource<?> res) {
        return ratioTapChanger.getAttributes(res).getSteps().get(tapPositionIndex);
    }

    private TapChangerStepAttributes getTapChangerStepAttributes() {
        return ratioTapChanger.getAttributes().getSteps().get(tapPositionIndex);
    }

    @Override
    public double getRho() {
        return getTapChangerStepAttributes().getRho();
    }

    @Override
    public RatioTapChangerStepImpl setRho(double rho) {
        double oldValue = getTapChangerStepAttributes().getRho();
        if (rho != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setRho(rho),
                "rho", oldValue, rho);
        }
        return this;
    }

    @Override
    public double getR() {
        return getTapChangerStepAttributes(ratioTapChanger.getResource()).getR();
    }

    @Override
    public RatioTapChangerStepImpl setR(double r) {
        double oldValue = getTapChangerStepAttributes().getR();
        if (r != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setR(r),
                "r", oldValue, r);
        }
        return this;
    }

    @Override
    public double getX() {
        return getTapChangerStepAttributes().getX();
    }

    @Override
    public RatioTapChangerStepImpl setX(double x) {
        double oldValue = getTapChangerStepAttributes().getX();
        if (x != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setX(x),
                "x", oldValue, x);
        }
        return this;
    }

    @Override
    public double getG() {
        return getTapChangerStepAttributes().getG();
    }

    @Override
    public RatioTapChangerStepImpl setG(double g) {
        double oldValue = getTapChangerStepAttributes().getG();
        if (g != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setG(g),
                "g", oldValue, g);
        }
        return this;
    }

    @Override
    public double getB() {
        return getTapChangerStepAttributes().getB();
    }

    @Override
    public RatioTapChangerStepImpl setB(double b) {
        double oldValue = getTapChangerStepAttributes().getB();
        if (b != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setB(b),
                "b", oldValue, b);
        }
        return this;
    }

    @Override
    public boolean hasProperty() {
        Map<String, String> properties = getTapChangerStepAttributes().getProperties();
        return properties != null && !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(String key) {
        Map<String, String> properties = getTapChangerStepAttributes().getProperties();
        return properties != null && properties.containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        Map<String, String> properties = getTapChangerStepAttributes().getProperties();
        return properties != null ? properties.get(key) : null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Map<String, String> properties = getTapChangerStepAttributes().getProperties();
        return properties != null ? properties.getOrDefault(key, defaultValue) : defaultValue;
    }

    @Override
    public String setProperty(String key, String value) {
        MutableObject<String> oldValue = new MutableObject<>();
        Map<String, String> properties = getTapChangerStepAttributes().getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        oldValue.setValue(properties.put(key, value));

        Map<String, String> finalProperties = properties;
        ratioTapChanger.getTransformer().updateResourceWithoutNotification(r -> getTapChangerStepAttributes().setProperties(finalProperties));
        return oldValue.getValue();
    }

    @Override
    public boolean removeProperty(String key) {
        Map<String, String> properties = getTapChangerStepAttributes().getProperties();
        if (properties != null && properties.containsKey(key)) {
            ratioTapChanger.getTransformer().updateResourceWithoutNotification(r -> getTapChangerStepAttributes().getProperties().remove(key));
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        Map<String, String> properties = getTapChangerStepAttributes().getProperties();
        return properties != null ? properties.keySet() : Collections.emptySet();
    }
}
