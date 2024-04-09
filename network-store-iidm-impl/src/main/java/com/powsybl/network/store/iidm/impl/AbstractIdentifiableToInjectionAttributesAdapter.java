package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Map;
import java.util.Set;

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
