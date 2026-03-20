/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MinMaxReactiveLimitsImpl implements MinMaxReactiveLimits {

    private final MinMaxReactiveLimitsAttributes attributes;
    private final AbstractInjectionImpl<?, ?> owner;

    MinMaxReactiveLimitsImpl(MinMaxReactiveLimitsAttributes attributes, AbstractInjectionImpl<?, ?> injection) {
        this.attributes = attributes;
        this.owner = injection;
    }

    @Override
    public double getMinQ() {
        return attributes.getMinQ();
    }

    @Override
    public double getMaxQ() {
        return attributes.getMaxQ();
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.MIN_MAX;
    }

    @Override
    public double getMinQ(double p) {
        return attributes.getMinQ();
    }

    @Override
    public double getMaxQ(double p) {
        return attributes.getMaxQ();
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
