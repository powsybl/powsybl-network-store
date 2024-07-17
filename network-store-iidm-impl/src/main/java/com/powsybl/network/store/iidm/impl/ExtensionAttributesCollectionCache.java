/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.iidm.impl.util.PentaFunction;
import com.powsybl.network.store.iidm.impl.util.QuadriFunction;
import com.powsybl.network.store.iidm.impl.util.TriFunction;
import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.ResourceType;

import java.util.*;

/**
 * Extension attributes collection cache management.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class ExtensionAttributesCollectionCache {

    /**
     * Cache storing extension attributes.
     *
     * The cache is organized as a nested map where:
     * - The outer map uses identifiable IDs as keys.
     * - The inner map uses extension names as keys, mapping to corresponding extension attributes.
     */
    private final Map<String, Map<String, ExtensionAttributes>> extensionAttributesCache = new HashMap<>();

    /**
     * Indicates if the extension for a specific resource type has been fully loaded and synchronized with the server.
     *
     * The map is organized where:
     * - The keys are resource types.
     * - The values are sets of extension names that have been fully loaded.
     */
    private final Map<ResourceType, Set<String>> fullyLoadedExtensions = new EnumMap<>(ResourceType.class);

    /**
     * Indicates if all the extensions for a specific resource type has been fully loaded and synchronized with the server.
     */
    private final EnumSet<ResourceType> fullyLoadedTypes = EnumSet.noneOf(ResourceType.class);

    /**
     * Indicates if all the extensions for a specific identifiable has been fully loaded and synchronized with the server.
     */
    private final Set<String> fullyLoadedIdentifiables = new HashSet<>();

    /**
     * Map storing sets of removed extension names associated with identifiable IDs.
     *
     * The map is organized where:
     * - The keys are identifiable IDs.
     * - The values are sets of extension names that have been removed.
     */
    private final Map<String, Set<String>> removedExtensionAttributes = new HashMap<>();

    /**
     * A function to load one extension attributes from the server. An optional is returned because extension attributes could not exist on
     * the server.
     */
    private final PentaFunction<UUID, Integer, ResourceType, String, String, Optional<ExtensionAttributes>> extensionAttributeLoader;

    /**
     * A function to load the extension attributes with a specific extension name for the collection with specified resource type.
     */
    private final QuadriFunction<UUID, Integer, ResourceType, String, Map<String, ExtensionAttributes>> extensionAttributesLoaderByResourceTypeAndName;

    /**
     * A function to load all extension attributes for one identifiable from the server.
     */
    private final QuadriFunction<UUID, Integer, ResourceType, String, Map<String, ExtensionAttributes>> extensionAttributesLoaderById;

    /**
     * A function to load all extension attributes for the collection with specified resource type.
     */
    private final TriFunction<UUID, Integer, ResourceType, Map<String, Map<String, ExtensionAttributes>>> extensionAttributesLoaderByResourceType;

    public ExtensionAttributesCollectionCache(PentaFunction<UUID, Integer, ResourceType, String, String, Optional<ExtensionAttributes>> extensionAttributeLoader,
                                              QuadriFunction<UUID, Integer, ResourceType, String, Map<String, ExtensionAttributes>> extensionAttributesLoaderByResourceTypeAndName,
                                              QuadriFunction<UUID, Integer, ResourceType, String, Map<String, ExtensionAttributes>> extensionAttributesLoaderById,
                                              TriFunction<UUID, Integer, ResourceType, Map<String, Map<String, ExtensionAttributes>>> extensionAttributesLoaderByResourceType) {
        this.extensionAttributeLoader = Objects.requireNonNull(extensionAttributeLoader);
        this.extensionAttributesLoaderByResourceTypeAndName = Objects.requireNonNull(extensionAttributesLoaderByResourceTypeAndName);
        this.extensionAttributesLoaderById = Objects.requireNonNull(extensionAttributesLoaderById);
        this.extensionAttributesLoaderByResourceType = Objects.requireNonNull(extensionAttributesLoaderByResourceType);
    }

    public ExtensionAttributesCollectionCache(ExtensionAttributesCollectionCache sourceCollection) {
        extensionAttributeLoader = sourceCollection.extensionAttributeLoader;
        extensionAttributesLoaderByResourceTypeAndName = sourceCollection.extensionAttributesLoaderByResourceTypeAndName;
        extensionAttributesLoaderById = sourceCollection.extensionAttributesLoaderById;
        extensionAttributesLoaderByResourceType = sourceCollection.extensionAttributesLoaderByResourceType;

        for (Map.Entry<String, Map<String, ExtensionAttributes>> entry : sourceCollection.extensionAttributesCache.entrySet()) {
            extensionAttributesCache.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        for (Map.Entry<ResourceType, Set<String>> entry : sourceCollection.fullyLoadedExtensions.entrySet()) {
            fullyLoadedExtensions.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        for (Map.Entry<String, Set<String>> entry : sourceCollection.removedExtensionAttributes.entrySet()) {
            removedExtensionAttributes.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        fullyLoadedTypes.addAll(sourceCollection.fullyLoadedTypes);
        fullyLoadedIdentifiables.addAll(sourceCollection.fullyLoadedIdentifiables);
    }

    /**
     * Get an extension attributes from the collection by its id and extension name. If resource has not been found on cache, it is loaded
     * from the server.
     */
    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType type, String identifiableId, String extensionName) {
        Objects.requireNonNull(identifiableId);

        if (isExtensionAttributesCached(identifiableId, extensionName)) {
            return Optional.ofNullable(extensionAttributesCache.get(identifiableId).get(extensionName));
        }

        if (!isFullyLoadedExtension(type, identifiableId, extensionName) && !isRemovedAttributes(identifiableId, extensionName)) {
            Optional<ExtensionAttributes> extensionAttributes = extensionAttributeLoader.apply(networkUuid, variantNum, type, identifiableId, extensionName);
            // if extension attributes has been found on server side we add it to the cache
            extensionAttributes.ifPresent(attributes -> addExtensionAttributes(identifiableId, extensionName, attributes));
            return extensionAttributes;
        }
        return Optional.empty();
    }

    public boolean isFullyLoadedExtension(ResourceType type, String identifiableId, String extensionName) {
        return fullyLoadedTypes.contains(type) || fullyLoadedIdentifiables.contains(identifiableId) || fullyLoadedExtensions.containsKey(type) && fullyLoadedExtensions.get(type).contains(extensionName);
    }

    public boolean isFullyLoadedExtension(ResourceType type, String extensionName) {
        return fullyLoadedTypes.contains(type) || fullyLoadedExtensions.containsKey(type) && fullyLoadedExtensions.get(type).contains(extensionName);
    }

    public boolean isRemovedAttributes(String id, String extensionName) {
        return removedExtensionAttributes.containsKey(id) && removedExtensionAttributes.get(id).contains(extensionName);
    }

    public boolean isExtensionAttributesCached(String id, String extensionName) {
        return extensionAttributesCache.containsKey(id) && extensionAttributesCache.get(id).containsKey(extensionName);
    }

    /**
     * Add extension attributes in the cache for single extension attributes loading
     */
    private void addExtensionAttributes(String identifiableId, String extensionName, ExtensionAttributes extensionAttributes) {
        Objects.requireNonNull(extensionAttributes);

        extensionAttributesCache.computeIfAbsent(identifiableId, key -> new HashMap<>())
                .put(extensionName, extensionAttributes);
        Set<String> extensions = removedExtensionAttributes.get(identifiableId);
        if (extensions != null) {
            extensions.remove(extensionName);
            if (extensions.isEmpty()) {
                removedExtensionAttributes.remove(identifiableId);
            }
        }
    }

    /**
     * Load the extensions attributes with specified extension name for all the identifiables with specified resource type in the cache.
     */
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByResourceTypeAndExtensionName(UUID networkUuid, int variantNum, ResourceType type, String extensionName) {
        if (!isFullyLoadedExtension(type, extensionName)) {
            // if collection has not yet been fully loaded we load it from the server
            Map<String, ExtensionAttributes> extensionAttributesMap = extensionAttributesLoaderByResourceTypeAndName.apply(networkUuid, variantNum, type, extensionName);

            // we update the full cache and set it as fully loaded
            extensionAttributesMap.forEach((identifiableId, extensionAttributes) -> extensionAttributesCache.computeIfAbsent(identifiableId, key -> new HashMap<>())
                    .put(extensionName, extensionAttributes));
            fullyLoadedExtensions.computeIfAbsent(type, k -> new HashSet<>()).add(extensionName);

            // discard remove status of the resources
            extensionAttributesMap.forEach((k, v) -> {
                if (removedExtensionAttributes.containsKey(k)) {
                    removedExtensionAttributes.get(k).remove(extensionName);
                }
            });
        }
        // The return of this method is meaningless as it's not used in the client but only to load extension attributes
        // in the cache with collection preloading strategy.
        return Map.of();
    }

    /**
     * Get all extension attributes for one identifiable of the collection.
     */
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType type, String identifiableId) {
        Objects.requireNonNull(identifiableId);
        if (isExtensionAttributesCached(type, identifiableId)) {
            return extensionAttributesCache.get(identifiableId);
        }

        if (!isFullyLoadedIdentifiable(type, identifiableId)) {
            Map<String, ExtensionAttributes> extensionAttributes = extensionAttributesLoaderById.apply(networkUuid, variantNum, type, identifiableId);
            if (extensionAttributes != null) {
                addExtensionAttributes(identifiableId, extensionAttributes);
                return extensionAttributes;
            }
        }
        return Map.of();
    }

    private boolean isFullyLoadedIdentifiable(ResourceType type, String identifiableId) {
        return fullyLoadedTypes.contains(type) || fullyLoadedIdentifiables.contains(identifiableId);
    }

    private boolean isExtensionAttributesCached(ResourceType type, String identifiableId) {
        return (fullyLoadedIdentifiables.contains(identifiableId) || fullyLoadedTypes.contains(type)) && extensionAttributesCache.containsKey(identifiableId);
    }

    /**
     * Add extension attributes to the cache when loading all the extension attributes of an identifiable
     */
    private void addExtensionAttributes(String id, Map<String, ExtensionAttributes> extensionAttributes) {
        Objects.requireNonNull(extensionAttributes);

        extensionAttributesCache.put(id, extensionAttributes);
        fullyLoadedIdentifiables.add(id);
        removedExtensionAttributes.remove(id);
    }

    /**
     * Load all the extensions attributes for all the identifiables with specified resource type in the cache
     */
    public Map<String, Map<String, ExtensionAttributes>> getAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType type) {
        if (!fullyLoadedTypes.contains(type)) {
            // if collection has not yet been fully loaded we load it from the server
            Map<String, Map<String, ExtensionAttributes>> extensionAttributesMap = extensionAttributesLoaderByResourceType.apply(networkUuid, variantNum, type);

            // we update the full cache and set it as fully loaded
            extensionAttributesCache.putAll(extensionAttributesMap);
            fullyLoadedTypes.add(type);

            // discard remove status of the resources
            extensionAttributesMap.forEach((k, v) -> removedExtensionAttributes.remove(k));
        }
        // The return of this method is meaningless as it's not used in the client but only to load extension attributes
        // in the cache with collection preloading strategy.
        return Map.of();
    }

    public void removeExtensionAttributesByExtensionName(String identifiableId, String extensionName) {
        Objects.requireNonNull(identifiableId);
        Objects.requireNonNull(extensionName);
        if (isExtensionAttributesCached(identifiableId, extensionName)) {
            extensionAttributesCache.get(identifiableId).remove(extensionName);
            removedExtensionAttributes.computeIfAbsent(identifiableId, k -> new HashSet<>()).add(extensionName);
        }
    }

    public void removeExtensionAttributesByIdentifiableId(String identifiableId) {
        Objects.requireNonNull(identifiableId);
        if (extensionAttributesCache.containsKey(identifiableId)) {
            Set<String> removedExtensionNames = extensionAttributesCache.get(identifiableId).keySet();
            extensionAttributesCache.remove(identifiableId);
            removedExtensionAttributes.computeIfAbsent(identifiableId, k -> new HashSet<>()).addAll(removedExtensionNames);
        }
    }

    public void removeExtensionAttributesByIdentifiableIds(List<String> identifiableIds) {
        Objects.requireNonNull(identifiableIds);
        identifiableIds.forEach(this::removeExtensionAttributesByIdentifiableId);
    }
}
