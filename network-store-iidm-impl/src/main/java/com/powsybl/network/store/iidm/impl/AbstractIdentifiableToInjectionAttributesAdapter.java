/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public abstract class AbstractIdentifiableToInjectionAttributesAdapter<T extends IdentifiableAttributes> implements InjectionAttributes {
    protected final T attributes;

    protected AbstractIdentifiableToInjectionAttributesAdapter(T attributes) {
        this.attributes = attributes;
    }

    @Override
    public Resource getResource() {
        return attributes.getResource();
    }

    @Override
    public void setResource(Resource resource) {
        attributes.setResource(resource);
    }

    @Override
    public String getName() {
        return attributes.getName();
    }

    @Override
    public void setName(String name) {
        attributes.setName(name);
    }

    @Override
    public Map<String, String> getProperties() {
        return attributes.getProperties();
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        attributes.setProperties(properties);
    }

    @Override
    public Set<String> getAliasesWithoutType() {
        return attributes.getAliasesWithoutType();
    }

    @Override
    public void setAliasesWithoutType(Set<String> aliasesWithoutType) {
        attributes.setAliasesWithoutType(aliasesWithoutType);
    }

    @Override
    public Map<String, String> getAliasByType() {
        return attributes.getAliasByType();
    }

    @Override
    public void setAliasByType(Map<String, String> aliasByType) {
        attributes.setAliasByType(aliasByType);
    }

    @Override
    public boolean isFictitious() {
        return attributes.isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        attributes.setFictitious(fictitious);
    }

    @Override
    public Map<String, ExtensionAttributes> getExtensionAttributes() {
        return attributes.getExtensionAttributes();
    }

    @Override
    public void setExtensionAttributes(Map<String, ExtensionAttributes> extensionAttributes) {
        attributes.setExtensionAttributes(extensionAttributes);
    }
}
