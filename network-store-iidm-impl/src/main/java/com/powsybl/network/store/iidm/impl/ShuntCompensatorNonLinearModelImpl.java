/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ShuntCompensatorNonLinearModel;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.ShuntCompensatorNonLinearModelAttributes;
import com.powsybl.network.store.model.ShuntCompensatorNonLinearSectionAttributes;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ShuntCompensatorNonLinearModelImpl implements ShuntCompensatorNonLinearModel {

    class SectionImpl implements Section {

        private final ShuntCompensatorImpl shuntCompensator;

        private final int index;

        public SectionImpl(ShuntCompensatorImpl shuntCompensator, int index) {
            this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
            this.index = index;
        }

        private ShuntCompensatorNonLinearSectionAttributes getSectionAttributes() {
            return getAttributes().getSections().get(index);
        }

        @Override
        public double getB() {
            return getSectionAttributes().getB();
        }

        @Override
        public Section setB(double b) {
            ValidationUtil.checkB(shuntCompensator, b);
            double oldValue = getB();
            if (b != oldValue) {
                shuntCompensator.updateResource(res -> getSectionAttributes().setB(b),
                    "b", oldValue, b);
            }
            return this;
        }

        @Override
        public double getG() {
            return getSectionAttributes().getG();
        }

        @Override
        public Section setG(double g) {
            ValidationUtil.checkG(shuntCompensator, g);
            double oldValue = getG();
            if (g != oldValue) {
                shuntCompensator.updateResource(res -> getSectionAttributes().setG(g),
                    "g", oldValue, g);
            }
            return this;
        }

        @Override
        public boolean hasProperty() {
            Map<String, String> properties = getSectionAttributes().getProperties();
            return properties != null && !properties.isEmpty();
        }

        @Override
        public boolean hasProperty(String key) {
            Map<String, String> properties = getSectionAttributes().getProperties();
            return properties != null && properties.containsKey(key);
        }

        @Override
        public String getProperty(String key) {
            Map<String, String> properties = getSectionAttributes().getProperties();
            return properties != null ? properties.get(key) : null;
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            Map<String, String> properties = getSectionAttributes().getProperties();
            return properties != null ? properties.getOrDefault(key, defaultValue) : defaultValue;
        }

        @Override
        public String setProperty(String key, String value) {
            MutableObject<String> oldValue = new MutableObject<>();
            Map<String, String> properties = getSectionAttributes().getProperties();
            if (properties == null) {
                properties = new HashMap<>();
            }
            oldValue.setValue(properties.put(key, value));

            Map<String, String> finalProperties = properties;
            shuntCompensator.updateResourceWithoutNotification(r -> getSectionAttributes().setProperties(finalProperties));
            return oldValue.getValue();
        }

        @Override
        public boolean removeProperty(String key) {
            Map<String, String> properties = getSectionAttributes().getProperties();
            if (properties != null && properties.containsKey(key)) {
                shuntCompensator.updateResourceWithoutNotification(r -> getSectionAttributes().getProperties().remove(key));
                return true;
            }
            return false;
        }

        @Override
        public Set<String> getPropertyNames() {
            Map<String, String> properties = getSectionAttributes().getProperties();
            return properties != null ? properties.keySet() : Collections.emptySet();
        }
    }

    private final ShuntCompensatorImpl shuntCompensator;

    public ShuntCompensatorNonLinearModelImpl(ShuntCompensatorImpl shuntCompensator) {
        this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
    }

    private Resource<ShuntCompensatorAttributes> getResource() {
        return shuntCompensator.getResource();
    }

    private static ShuntCompensatorNonLinearModelAttributes getAttributes(Resource<ShuntCompensatorAttributes> resource) {
        return (ShuntCompensatorNonLinearModelAttributes) resource.getAttributes().getModel();
    }

    private ShuntCompensatorNonLinearModelAttributes getAttributes() {
        return getAttributes(getResource());
    }

    @Override
    public List<Section> getAllSections() {
        return IntStream.range(0, getAttributes().getSections().size())
                .boxed()
                .map((Function<Integer, Section>) i -> new SectionImpl(shuntCompensator, i))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasProperty() {
        Map<String, String> properties = getAttributes().getProperties();
        return properties != null && !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(String key) {
        Map<String, String> properties = getAttributes().getProperties();
        return properties != null && properties.containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        Map<String, String> properties = getAttributes().getProperties();
        return properties != null ? properties.get(key) : null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Map<String, String> properties = getAttributes().getProperties();
        return properties != null ? properties.getOrDefault(key, defaultValue) : defaultValue;
    }

    @Override
    public String setProperty(String key, String value) {
        MutableObject<String> oldValue = new MutableObject<>();
        Map<String, String> properties = getAttributes().getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        oldValue.setValue(properties.put(key, value));

        Map<String, String> finalProperties = properties;
        shuntCompensator.updateResourceWithoutNotification(r -> getAttributes().setProperties(finalProperties));
        return oldValue.getValue();
    }

    @Override
    public boolean removeProperty(String key) {
        Map<String, String> properties = getAttributes().getProperties();
        if (properties != null && properties.containsKey(key)) {
            shuntCompensator.updateResourceWithoutNotification(r -> getAttributes().getProperties().remove(key));
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        Map<String, String> properties = getAttributes().getProperties();
        return properties != null ? properties.keySet() : Collections.emptySet();
    }
}
