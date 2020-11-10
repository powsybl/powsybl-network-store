/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.commons.extensions.ExtensionAdderProviders;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractIdentifiableImpl<I extends Identifiable<I>, D extends IdentifiableAttributes>
        implements Identifiable<I> {

    protected final NetworkObjectIndex index;

    protected final Resource<D> resource;

    protected AbstractIdentifiableImpl(NetworkObjectIndex index, Resource<D> resource) {
        this.index = index;
        this.resource = resource;
    }

    public Resource<D> getResource() {
        return resource;
    }

    public String getId() {
        return resource.getId();
    }

    public String getName() {
        return getNameOrId();
    }

    @Override
    public String getNameOrId() {
        return resource.getAttributes().getName() != null ? resource.getAttributes().getName() : resource.getId();
    }

    @Override
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(resource.getAttributes().getName());
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        if (resource.getAttributes().getProperties() != null) {
            properties.putAll(resource.getAttributes().getProperties());
        }
        return properties;
    }

    public String getProperty(String key) {
        Map<String, String> properties = resource.getAttributes().getProperties();
        return properties != null ? properties.get(key) : null;
    }

    public String getProperty(String key, String defaultValue) {
        Map<String, String> properties = resource.getAttributes().getProperties();
        return properties != null ? properties.getOrDefault(key, defaultValue) : null;
    }

    public Set<String> getPropertyNames() {
        Map<String, String> properties = resource.getAttributes().getProperties();
        return properties != null ? properties.keySet() : Collections.emptySet();
    }

    public String setProperty(String key, String value) {
        Map<String, String> properties = resource.getAttributes().getProperties();
        if (properties == null) {
            properties = new HashMap<>();
            resource.getAttributes().setProperties(properties);
        }

        String oldValue = properties.put(key, value);
        if (Objects.isNull(oldValue)) {
            index.notifyElementAdded(this, () -> "properties[" + key + "]", value);
        } else {
            index.notifyElementReplaced(this, () -> "properties[" + key + "]", oldValue, value);
        }
        return oldValue;
    }

    public boolean hasProperty() {
        Map<String, String> properties = resource.getAttributes().getProperties();
        return properties != null && !properties.isEmpty();
    }

    public boolean hasProperty(String key) {
        Map<String, String> properties = resource.getAttributes().getProperties();
        return properties != null && properties.containsKey(key);
    }

    public NetworkImpl getNetwork() {
        return index.getNetwork();
    }

    public <E extends Extension<I>> void addExtension(Class<? super E> type, E extension) {
        // not extension support by default
    }

    public <E extends Extension<I>> E getExtension(Class<? super E> type) {
        return null;
    }

    public <E extends Extension<I>> E getExtensionByName(String name) {
        return null;
    }

    public <E extends Extension<I>> boolean removeExtension(Class<E> type) {
        throw new UnsupportedOperationException("TODO");
    }

    public <E extends Extension<I>> Collection<E> getExtensions() {
        return new ArrayList<>();
    }

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public <E extends Extension<I>, B extends ExtensionAdder<I, E>> B newExtension(Class<B> type) {
        ExtensionAdderProvider provider = ExtensionAdderProviders.findCachedProvider(getImplementationName(), type);
        return (B) provider.newAdder(this);
    }

    public boolean isFictitious() {
        return resource.getAttributes().isFictitious();
    }

    public void setFictitious(boolean fictitious) {
        boolean oldValue = resource.getAttributes().isFictitious();
        resource.getAttributes().setFictitious(fictitious);
        index.notifyUpdate(this, "fictitious", oldValue, fictitious);
    }

    protected abstract String getTypeDescription();

    public String getMessageHeader() {
        return getTypeDescription() + " '" + getId() + "': ";
    }
}
