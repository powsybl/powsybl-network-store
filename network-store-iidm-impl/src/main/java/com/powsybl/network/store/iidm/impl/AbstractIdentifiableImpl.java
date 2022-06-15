/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

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
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.*;

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

    public void updateResource() {
        index.updateResource(resource);
    }

    public Resource<D> getResource() {
        return resource;
    }

    public void setResource(Resource<D> resource) {
        this.resource = resource;
    }

    protected Resource<D> checkResource() {
        if (resource == null) {
            throw new PowsyblException("Object has been removed in current variant");
        }
        return resource;
    }

    protected Optional<Resource<D>> optResource() {
        return Optional.ofNullable(resource);
    }

    public String getId() {
        return checkResource().getId();
    }

    @Deprecated
    public String getName() {
        return getNameOrId();
    }

    @Override
    public String getNameOrId() {
        Resource<D> r = checkResource();
        return r.getAttributes().getName() != null ? r.getAttributes().getName() : r.getId();
    }

    @Override
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(checkResource().getAttributes().getName());
    }

    @Override
    public Set<String> getAliases() {
        Set<String> aliases = new HashSet<>();
        var attributes = checkResource().getAttributes();
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
        var attributes = checkResource().getAttributes();
        if (attributes.getAliasByType() != null) {
            return attributes.getAliasByType().entrySet().stream().filter(entry -> entry.getValue().equals(alias)).map(Map.Entry::getKey).findFirst();
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getAliasFromType(String aliasType) {
        Objects.requireNonNull(aliasType);
        var attributes = checkResource().getAttributes();
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
        var attributes = checkResource().getAttributes();
        String uniqueAlias = alias;
        if (ensureAliasUnicity) {
            uniqueAlias = Identifiables.getUniqueId(alias, getNetwork().getIndex()::contains);
        }
        if (!getNetwork().checkAliasUnicity(this, uniqueAlias)) {
            return;
        }

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
        getNetwork().addAlias(uniqueAlias, this.getId());
        updateResource();
    }

    @Override
    public void removeAlias(String alias) {
        Objects.requireNonNull(alias);
        var attributes = checkResource().getAttributes();
        String type = getAliasType(alias).orElse(null);
        if (type != null && !type.isEmpty()) {
            var aliasByType = attributes.getAliasByType();
            if (aliasByType != null) {
                aliasByType.remove(type);
            }
        } else {
            var aliasesWithoutType = attributes.getAliasesWithoutType();
            if (aliasesWithoutType != null) {
                if (!aliasesWithoutType.contains(alias)) {
                    throw new PowsyblException(String.format("No alias '%s' found in the network", alias));
                }
                aliasesWithoutType.remove(alias);
            }
        }
        getNetwork().removeAlias(alias);
        updateResource();
    }

    @Override
    public boolean hasAliases() {
        var attributes = checkResource().getAttributes();
        var aliasByType = attributes.getAliasByType();
        return aliasByType != null && !aliasByType.isEmpty();
    }

    @Deprecated
    public Properties getProperties() {
        Resource<D> r = checkResource();
        Properties properties = new Properties();
        if (r.getAttributes().getProperties() != null) {
            properties.putAll(r.getAttributes().getProperties());
        }
        return properties;
    }

    public String getProperty(String key) {
        Map<String, String> properties = checkResource().getAttributes().getProperties();
        return properties != null ? properties.get(key) : null;
    }

    public String getProperty(String key, String defaultValue) {
        Map<String, String> properties = checkResource().getAttributes().getProperties();
        return properties != null ? properties.getOrDefault(key, defaultValue) : null;
    }

    public Set<String> getPropertyNames() {
        Map<String, String> properties = checkResource().getAttributes().getProperties();
        return properties != null ? properties.keySet() : Collections.emptySet();
    }

    public String setProperty(String key, String value) {
        Resource<D> r = checkResource();
        Map<String, String> properties = r.getAttributes().getProperties();
        if (properties == null) {
            properties = new HashMap<>();
            r.getAttributes().setProperties(properties);
            updateResource();
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
        Map<String, String> properties = checkResource().getAttributes().getProperties();
        return properties != null && !properties.isEmpty();
    }

    public boolean hasProperty(String key) {
        Map<String, String> properties = checkResource().getAttributes().getProperties();
        return properties != null && properties.containsKey(key);
    }

    @Override
    public boolean removeProperty(String key) {
        Resource<D> r = checkResource();
        Map<String, String> properties = r.getAttributes().getProperties();
        if (properties != null) {
            return properties.remove(key) != null;
        }
        return false;
    }

    public NetworkImpl getNetwork() {
        if (resource == null) {
            return null;
        }
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
        return checkResource().getAttributes().isFictitious();
    }

    public void setFictitious(boolean fictitious) {
        Resource<D> r = checkResource();
        boolean oldValue = r.getAttributes().isFictitious();
        r.getAttributes().setFictitious(fictitious);
        updateResource();
        index.notifyUpdate(this, "fictitious", oldValue, fictitious);
    }

    @Override
    public String getMessageHeader() {
        return resource.getMessageHeader();
    }
}
