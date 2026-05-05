/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.PropertiesHolder;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractPropertiesHolder implements PropertiesHolder {
    protected abstract Map<String, String> getProperties();

    protected abstract void setProperties(Map<String, String> properties);

    protected abstract void persistProperties(Map<String, String> properties);

    @Override
    public boolean hasProperty() {
        Map<String, String> properties = getProperties();
        return properties != null && !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(String key) {
        Map<String, String> properties = getProperties();
        return properties != null && properties.containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        Map<String, String> properties = getProperties();
        return properties != null ? properties.get(key) : null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Map<String, String> properties = getProperties();
        return properties != null ? properties.getOrDefault(key, defaultValue) : defaultValue;
    }

    @Override
    public String setProperty(String key, String value) {
        MutableObject<String> oldValue = new MutableObject<>();
        Map<String, String> properties = getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        oldValue.setValue(properties.put(key, value));

        persistProperties(properties);
        return oldValue.getValue();
    }

    @Override
    public boolean removeProperty(String key) {
        Map<String, String> properties = getProperties();
        if (properties != null && properties.containsKey(key)) {
            properties.remove(key);
            persistProperties(properties);
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        Map<String, String> properties = getProperties();
        return properties != null ? properties.keySet() : Collections.emptySet();
    }
}
