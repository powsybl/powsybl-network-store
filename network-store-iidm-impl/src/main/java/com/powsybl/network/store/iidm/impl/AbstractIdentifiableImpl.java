/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.base.Strings;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.commons.extensions.ExtensionAdderProviders;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.network.store.model.AttributeFilter;
import com.powsybl.network.store.model.ExtensionLoaders;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.powsybl.network.store.model.ExtensionLoaders.loaderExists;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractIdentifiableImpl<I extends Identifiable<I>, D extends IdentifiableAttributes>
        implements Identifiable<I>, Validable {

    protected final NetworkObjectIndex index;

    private Resource<D> resource;

    protected AbstractIdentifiableImpl(NetworkObjectIndex index, Resource<D> resource) {
        this.index = index;
        this.resource = resource;
    }

    public void updateResource(Consumer<Resource<D>> modifier) {
        updateResource(modifier, null);
    }

    public void updateResource(Consumer<Resource<D>> modifier, AttributeFilter attributeFilter) {
        modifier.accept(resource);
        index.updateResource(resource, attributeFilter);
    }

    public Resource<D> getNullableResource() {
        return resource;
    }

    public void setResource(Resource<D> resource) {
        this.resource = resource;
    }

    public Resource<D> getResource() {
        if (resource == null) {
            throw new PowsyblException("Object has been removed in current variant");
        }
        if (index.getWorkingVariantNum() == -1) {
            throw new PowsyblException("Variant index not set");
        }
        return resource;
    }

    protected Optional<Resource<D>> getOptionalResource() {
        return Optional.ofNullable(resource);
    }

    public String getId() {
        return getResource().getId();
    }

    @Deprecated
    public String getName() {
        return getNameOrId();
    }

    @Override
    public String getNameOrId() {
        Resource<D> r = getResource();
        return r.getAttributes().getName() != null ? r.getAttributes().getName() : r.getId();
    }

    @Override
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(getResource().getAttributes().getName());
    }

    @Override
    public I setName(String name) {
        String oldName = getResource().getAttributes().getName();
        if (!Objects.equals(oldName, name)) {
            updateResource(r -> r.getAttributes().setName(name));
            index.notifyUpdate(this, "name", getNetwork().getVariantManager().getWorkingVariantId(), oldName, name);
        }
        return (I) this;
    }

    @Override
    public Set<String> getAliases() {
        Set<String> aliases = new HashSet<>();
        var attributes = getResource().getAttributes();
        if (attributes.getAliasesWithoutType() != null) {
            aliases.addAll(attributes.getAliasesWithoutType());
        }
        if (attributes.getAliasByType() != null) {
            aliases.addAll(attributes.getAliasByType().values());
        }
        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public Optional<String> getAliasType(String alias) {
        Objects.requireNonNull(alias);
        var attributes = getResource().getAttributes();
        if (attributes.getAliasByType() != null) {
            return attributes.getAliasByType().entrySet().stream().filter(entry -> entry.getValue().equals(alias)).map(Map.Entry::getKey).findFirst();
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getAliasFromType(String aliasType) {
        if (Strings.isNullOrEmpty(aliasType)) {
            throw new PowsyblException("Alias type must not be null or empty");
        }
        var attributes = getResource().getAttributes();
        if (attributes.getAliasByType() != null) {
            return Optional.ofNullable(attributes.getAliasByType().get(aliasType));
        }
        return Optional.empty();
    }

    @Override
    public void addAlias(String alias) {
        addAlias(alias, false);
    }

    @Override
    public void addAlias(String alias, boolean ensureAliasUnicity) {
        addAlias(alias, null, ensureAliasUnicity);
    }

    @Override
    public void addAlias(String alias, String aliasType) {
        addAlias(alias, aliasType, false);
    }

    @Override
    public void addAlias(String alias, String aliasType, boolean ensureAliasUnicity) {
        Objects.requireNonNull(alias);
        String uniqueAlias = ensureAliasUnicity ? Identifiables.getUniqueId(alias, getNetwork().getIndex()::contains) : alias;
        if (!getNetwork().checkAliasUnicity(this, uniqueAlias)) {
            return;
        }

        updateResource(r -> {
            var attributes = r.getAttributes();
            var aliasByType = attributes.getAliasByType();
            if (aliasType != null && aliasByType != null && aliasByType.containsKey(aliasType)) {
                throw new PowsyblException(this.getId() + " already has an alias of type " + aliasType);
            }

            if (aliasType != null && !aliasType.isEmpty()) {
                if (aliasByType == null) {
                    aliasByType = new LinkedHashMap<>();
                    attributes.setAliasByType(aliasByType);
                }
                aliasByType.put(aliasType, uniqueAlias);
            } else {
                var aliasesWithoutType = attributes.getAliasesWithoutType();
                if (aliasesWithoutType == null) {
                    aliasesWithoutType = new LinkedHashSet<>();
                    attributes.setAliasesWithoutType(aliasesWithoutType);
                }
                aliasesWithoutType.add(uniqueAlias);
            }
        });
        getNetwork().addAlias(uniqueAlias, this.getId());
    }

    @Override
    public void removeAlias(String alias) {
        Objects.requireNonNull(alias);
        updateResource(r -> {
            var attributes = r.getAttributes();
            String type = getAliasType(alias).orElse(null);
            if (type != null && !type.isEmpty()) {
                var aliasByType = attributes.getAliasByType();
                if (aliasByType != null) {
                    aliasByType.remove(type);
                }
            } else {
                var aliasesWithoutType = attributes.getAliasesWithoutType();
                if (aliasesWithoutType == null || !aliasesWithoutType.contains(alias)) {
                    throw new PowsyblException(String.format("No alias '%s' found in the network", alias));
                }
                aliasesWithoutType.remove(alias);
            }
        });
        getNetwork().removeAlias(alias);
    }

    @Override
    public boolean hasAliases() {
        var attributes = getResource().getAttributes();
        var aliasByType = attributes.getAliasByType();
        return aliasByType != null && !aliasByType.isEmpty();
    }

    @Deprecated
    public Properties getProperties() {
        Resource<D> r = getResource();
        Properties properties = new Properties();
        if (r.getAttributes().getProperties() != null) {
            properties.putAll(r.getAttributes().getProperties());
        }
        return properties;
    }

    public String getProperty(String key) {
        Map<String, String> properties = getResource().getAttributes().getProperties();
        return properties != null ? properties.get(key) : null;
    }

    public String getProperty(String key, String defaultValue) {
        Map<String, String> properties = getResource().getAttributes().getProperties();
        return properties != null ? properties.getOrDefault(key, defaultValue) : defaultValue;
    }

    public Set<String> getPropertyNames() {
        Map<String, String> properties = getResource().getAttributes().getProperties();
        return properties != null ? properties.keySet() : Collections.emptySet();
    }

    public String setProperty(String key, String value) {
        MutableObject<String> oldValue = new MutableObject<>();

        updateResource(r -> {
            Map<String, String> properties = r.getAttributes().getProperties();
            if (properties == null) {
                properties = new HashMap<>();
            }
            r.getAttributes().setProperties(properties);
            oldValue.setValue(properties.put(key, value));
        });

        if (Objects.isNull(oldValue.getValue())) {
            index.notifyPropertyAdded(this, () -> "properties[" + key + "]", value);
        } else {
            index.notifyPropertyReplaced(this, () -> "properties[" + key + "]", oldValue.getValue(), value);
        }
        return oldValue.getValue();
    }

    public boolean hasProperty() {
        Map<String, String> properties = getResource().getAttributes().getProperties();
        return properties != null && !properties.isEmpty();
    }

    public boolean hasProperty(String key) {
        Map<String, String> properties = getResource().getAttributes().getProperties();
        return properties != null && properties.containsKey(key);
    }

    @Override
    public boolean removeProperty(String key) {
        Map<String, String> properties = getResource().getAttributes().getProperties();
        if (properties != null && properties.containsKey(key)) {
            String oldValue = properties.get(key);
            updateResource(r -> r.getAttributes().getProperties().remove(key));
            index.notifyPropertyRemoved(this, () -> "properties[" + key + "]", oldValue);
            return true;
        }
        return false;
    }

    public NetworkImpl getNetwork() {
        getResource();
        return index.getNetwork();
    }

    protected void invalidateCalculatedBuses(List<? extends Terminal> terminals) {
        terminals.stream().map(Terminal::getVoltageLevel).filter(Objects::nonNull).map(VoltageLevel::getId)
            .forEach(id -> index.getVoltageLevel(id).ifPresent(VoltageLevelImpl::invalidateCalculatedBuses));
    }

    public <E extends Extension<I>> void addExtension(Class<? super E> type, E extension) {
        // not extension support by default
    }

    public <E extends Extension<I>> E getExtension(Class<? super E> type) {
        if (!loaderExists(type)) {
            return null;
        }
        return getExtensionByName(ExtensionLoaders.findLoader(type).getName());
    }

    public <E extends Extension<I>> E getExtensionByName(String name) {
        if (!loaderExists(name)) {
            return null;
        }
        index.loadExtensionAttributes(resource.getType(), resource.getId(), name);
        if (resource.getAttributes().getExtensionAttributes().containsKey(name)) {
            return (E) ExtensionLoaders.findLoaderByName(name).load(this);
        }
        return null;
    }

    public <E extends Extension<I>> boolean removeExtension(Class<E> type) {
        E extension = getExtension(type);
        if (extension == null) {
            return false;
        }
        extension.cleanup();
        index.notifyExtensionBeforeRemoval(extension);
        index.removeExtensionAttributes(resource.getType(), resource.getId(), extension.getName());
        index.notifyExtensionAfterRemoval(this, extension.getName());
        return true;
    }

    public <E extends Extension<I>> Collection<E> getExtensions() {
        index.loadAllExtensionsAttributesByIdentifiableId(resource.getType(), resource.getId());
        return resource.getAttributes().getExtensionAttributes().keySet().stream()
                .filter(ExtensionLoaders::loaderExists)
                .map(name -> (E) ExtensionLoaders.findLoaderByName(name).load(this))
                .collect(Collectors.toList());
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

    @Override
    public boolean isFictitious() {
        return getResource().getAttributes().isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        boolean oldValue = getResource().getAttributes().isFictitious();
        if (fictitious != oldValue) {
            updateResource(r -> r.getAttributes().setFictitious(fictitious));
            index.notifyUpdate(this, "fictitious", getNetwork().getVariantManager().getWorkingVariantId(), oldValue, fictitious);
        }
    }

    @Override
    public String getMessageHeader() {
        return resource.getMessageHeader();
    }

    public NetworkObjectIndex getIndex() {
        return index;
    }
}
