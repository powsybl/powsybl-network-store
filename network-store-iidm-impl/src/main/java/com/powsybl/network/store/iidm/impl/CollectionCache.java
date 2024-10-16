/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.iidm.impl.util.TriFunction;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Identifiable collection cache management.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionCache<T extends IdentifiableAttributes> {

    /**
     * Resources indexed by id. <br/>
     * We enforce a single resource per variant because they are referenced both in this map
     * and directly in any identifiable object created via the IIDM API. <br/>
     * Overwriting a resource creates a new reference, which breaks synchronization with
     * the IIDM object managed in the NetworkObjectIndex.
     */
    private final Map<String, Resource<T>> resources = new HashMap<>();

    /**
     * true if collection has been fully load, so if the cache is synchronized with the server, false otherwise.
     */
    private boolean fullyLoaded = false;

    /**
     * Resources indexed by container id. A container is either a substation or a voltage level. <br/>
     * We enforce a single resource per variant because they are referenced both in this map
     * and directly in any identifiable object created via the IIDM API. <br/>
     * Overwriting a resource creates a new reference, which breaks synchronization with
     * the IIDM object managed in the NetworkObjectIndex.
     */
    private final Map<String, Map<String, Resource<T>>> resourcesByContainerId = new HashMap<>();

    /**
     * Set of container ids fully loaded, so synchonized with the server.
     */
    private final Set<String> containerFullyLoaded = new HashSet<>();

    /**
     * Set of removed resource id.
     */
    private final Set<String> removedResources = new HashSet<>();

    /**
     * Indicates if the extension has been fully loaded and synchronized with the server.
     */
    private final Set<String> fullyLoadedExtensionsByExtensionName = new HashSet<>();

    /**
     * Indicates if all the extensions for a specific identifiable has been fully loaded and synchronized with the server.
     */
    private final Set<String> fullyLoadedExtensionsByIdentifiableIds = new HashSet<>();

    /**
     * Indicates if all the extensions for this collection have been fully loaded and synchronized with the server.
     */
    private boolean fullyLoadedExtensions = false;

    /**
     * Map storing sets of removed extension names associated with identifiable IDs.
     * The map is organized where:
     * - The keys are identifiable IDs.
     * - The values are sets of extension names that have been removed.
     */
    private final Map<String, Set<String>> removedExtensionAttributes = new HashMap<>();

    /**
     * A function to load one resource from the server. An optional is returned because resource could not exist on
     * the server.
     */
    private final TriFunction<UUID, Integer, String, Optional<Resource<T>>> oneLoaderFunction;

    /**
     * A function to load resources from a container (so this is a just part of the full collection)
     */
    private final TriFunction<UUID, Integer, String, List<Resource<T>>> containerLoaderFunction;

    /**
     * A function to load all resources of the collection.
     */
    private final BiFunction<UUID, Integer, List<Resource<T>>> allLoaderFunction;

    private final NetworkStoreClient delegate;

    public CollectionCache(TriFunction<UUID, Integer, String, Optional<Resource<T>>> oneLoaderFunction,
                           TriFunction<UUID, Integer, String, List<Resource<T>>> containerLoaderFunction,
                           BiFunction<UUID, Integer, List<Resource<T>>> allLoaderFunction, NetworkStoreClient delegate) {
        this.oneLoaderFunction = Objects.requireNonNull(oneLoaderFunction);
        this.containerLoaderFunction = containerLoaderFunction;
        this.allLoaderFunction = Objects.requireNonNull(allLoaderFunction);
        this.delegate = delegate;
    }

    public boolean isResourceLoaded(String id) {
        return resources.containsKey(id);
    }

    public List<Resource<T>> getCachedResources() {
        return new ArrayList<>(resources.values());
    }

    /**
     * Declare the collection as fully initialized. It means that the collection exists on client side but not yet on server
     * side and that even if empty the collection is fully loaded.
     */
    public void init() {
        fullyLoaded = true;
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    /**
     */
    public void initContainer(String containerId) {
        Objects.requireNonNull(containerId);

        containerFullyLoaded.add(containerId);
    }

    /**
     * Get a resource from the collection by its id. If resource has not been found on cache, it is loaded
     * from the server.
     * @param id id of the resource
     * @return a resource from the collection
     */
    public Optional<Resource<T>> getResource(UUID networkUuid, int variantNum, String id) {
        Objects.requireNonNull(id);

        Resource<T> resource = null;

        if (resources.containsKey(id)) {
            // resource is in the cache
            resource = resources.get(id);
        } else {
            // if resource has not been fully loaded (so in that case it means the resource does not exist)
            // of if the resource has not been removed we try to get it from the server
            if (!fullyLoaded && !removedResources.contains(id)) {
                resource = oneLoaderFunction.apply(networkUuid, variantNum, id).orElse(null);
                // if resource has been found on server side we add it to the cache
                if (resource != null) {
                    addResourceIfAbsent(resource);
                }
            }
        }

        return Optional.ofNullable(resource);
    }

    private void loadAll(UUID networkUuid, int variantNum) {
        if (!fullyLoaded) {
            // if collection has not yet been fully loaded we load it from the server
            List<Resource<T>> resourcesToAdd = allLoaderFunction.apply(networkUuid, variantNum);

            // we update the full cache and set it as fully loaded
            // notice: even if it adds some checks and reduces performance by a tiny bit, we avoid to overwrite already
            // loaded resource (single or container) because they are referenced in the resources or resourcesByContainerId map,
            // but also directly in any identifiable with the iidm api.
            resourcesToAdd.forEach(resource -> resources.putIfAbsent(resource.getId(), resource));
            fullyLoaded = true;

            // we update by container cache
            for (Resource<T> resource : resourcesToAdd) {
                IdentifiableAttributes attributes = resource.getAttributes();
                if (attributes instanceof Contained) {
                    Set<String> containerIds = ((Contained) attributes).getContainerIds();
                    containerIds.forEach(containerId -> {
                        // we add container resources and update container fully loaded status
                        // notice: even if it adds some checks and reduces performance by a tiny bit, we avoid to overwrite already
                        // loaded resource (single or container) because they are referenced in the resources or resourcesByContainerId map,
                        // but also directly in any identifiable with the iidm api.
                        getResourcesByContainerId(containerId).putIfAbsent(resource.getId(), resource);
                        containerFullyLoaded.add(containerId);
                    });
                }

                // discard remove status of the resources
                removedResources.remove(resource.getId());
            }
        }
    }

    /**
     * Get all resources of the collection. If all resources have not already been fully loaded we load them from
     * the server.
     * @return all resources of the collection
     */
    public List<Resource<T>> getResources(UUID networkUuid, int variantNum) {
        loadAll(networkUuid, variantNum);
        return new ArrayList<>(resources.values());
    }

    private Map<String, Resource<T>> getResourcesByContainerId(String containerId) {
        return resourcesByContainerId.computeIfAbsent(containerId, k -> new LinkedHashMap<>());
    }

    /**
     * Get all resources of the collection that belongs to a container. If container resources have not yet been fully
     * loaded we load them from the server.
     * @param containerId the container id
     * @return all resources of the collection that belongs to the container
     */
    public List<Resource<T>> getContainerResources(UUID networkUuid, int variantNum, String containerId) {
        Objects.requireNonNull(containerId);
        if (containerLoaderFunction == null) {
            throw new PowsyblException("it is not possible to load resources by container, if container resources loader has not been specified");
        }

        if (!fullyLoaded && !containerFullyLoaded.contains(containerId)) {
            List<Resource<T>> resourcesToAdd = containerLoaderFunction.apply(networkUuid, variantNum, containerId)
                .stream().filter(resource -> !removedResources.contains(resource.getId())).collect(Collectors.toList());

            resourcesToAdd.forEach(resource -> {
                String resourceId = resource.getId();
                // notice: even if it adds some checks and reduces performance by a tiny bit, we avoid to overwrite already
                // loaded resource (single or container) because they are referenced in the resources or resourcesByContainerId map,
                // but also directly in any identifiable with the iidm api.
                getResourcesByContainerId(containerId).putIfAbsent(resourceId, resource);
                resources.putIfAbsent(resourceId, resource);
                removedResources.remove(resourceId);
            });
            containerFullyLoaded.add(containerId);
        }
        return new ArrayList<>(getResourcesByContainerId(containerId).values());
    }

    public void addResourceIfAbsent(Resource<T> resource) {
        cacheResource(resource, false);
    }

    private void addOrReplaceResource(Resource<T> resource) {
        cacheResource(resource, true);
    }

    private void cacheResource(Resource<T> resource, boolean shouldOverwrite) {
        Objects.requireNonNull(resource);

        if (shouldOverwrite) {
            // notice: in case we already checked that the resource is not in the map, we can directly put it
            resources.put(resource.getId(), resource);
        } else {
            // notice: even if it adds some checks and reduces performance by a tiny bit, we avoid to overwrite already
            // loaded resource (single or container) because they are referenced in the resources or resourcesByContainerId map,
            // but also directly in any identifiable with the iidm api.
            resources.putIfAbsent(resource.getId(), resource);
        }
        removedResources.remove(resource.getId());

        // by container cache update
        IdentifiableAttributes attributes = resource.getAttributes();
        if (attributes instanceof Contained) {
            Set<String> containerIds = ((Contained) attributes).getContainerIds();
            containerIds.forEach(containerId -> {
                if (shouldOverwrite) {
                    // notice: in case we already checked that the resource is not in the map, we can directly put it
                    getResourcesByContainerId(containerId).put(resource.getId(), resource);
                } else {
                    // notice: even if it adds some checks and reduces performance by a tiny bit, we avoid to overwrite already
                    // loaded resource (single or container) because they are referenced in the resources or resourcesByContainerId map,
                    // but also directly in any identifiable with the iidm api.
                    getResourcesByContainerId(containerId).putIfAbsent(resource.getId(), resource);
                }
            });
        }
    }

    /**
     * Add a new resource to the collection.
     *
     * @param resource the newly created resources
     */
    public void createResource(Resource<T> resource) {
        String resourceId = resource.getId();
        if (resources.containsKey(resourceId)) {
            throw new PowsyblException("The collection cache already contains a " + resource.getType() + " with the id '" + resourceId + "'");
        }
        addOrReplaceResource(resource);
    }

    /**
     * Replace resource of the collection with {@code resource}.
     *
     * @param resource the resource to update
     */
    public void updateResource(Resource<T> resource) {
        addOrReplaceResource(resource);
    }

    /**
     * Remove a resource from the collection.
     *
     * @param id the id of the resource to remove
     */
    public void removeResource(String id) {
        Objects.requireNonNull(id);
        // keep track of removed extension attributes
        removeExtensionAttributesByIdentifiableId(id);
        // try to remove the resource from full cache
        Resource<T> resource = resources.remove(id);
        removedResources.add(id);

        // if resource has been found also remove it from container cache
        if (resource != null) {
            IdentifiableAttributes attributes = resource.getAttributes();
            if (attributes instanceof Contained) {
                Set<String> containerIds = ((Contained) attributes).getContainerIds();
                containerIds.forEach(containerId -> getResourcesByContainerId(containerId).remove(resource.getId()));
            }
        }
    }

    public void removeResources(List<String> ids) {
        Objects.requireNonNull(ids);
        ids.forEach(this::removeResource);
    }

    /**
     * Get resource count.
     *
     * @return the resource count
     */
    public int getResourceCount(UUID networkUuid, int variantNum) {
        // the only reliable way to get count is to fully load the collection
        loadAll(networkUuid, variantNum);
        return resources.size();
    }

    /**
     * Cache deep copy.
     *
     * @param objectMapper a object mapper to help cloning resources
     * @param newVariantNum new variant num for all resources of the cloned cache
     * @param resourcePostProcessor a resource post processor
     * @return the cache clone
     */
    public CollectionCache<T> clone(ObjectMapper objectMapper, int newVariantNum, Consumer<Resource<T>> resourcePostProcessor) {
        // use json serialization to clone the resources of source collection
        List<Resource<T>> clonedResources = Resource.cloneResourcesToVariant(resources.values(), newVariantNum, objectMapper, resourcePostProcessor);

        var clonedCache = new CollectionCache<>(oneLoaderFunction, containerLoaderFunction, allLoaderFunction, delegate);
        for (Resource<T> clonedResource : clonedResources) {
            clonedCache.resources.put(clonedResource.getId(), clonedResource);
        }
        for (Map.Entry<String, Map<String, Resource<T>>> e : resourcesByContainerId.entrySet()) {
            String containerId = e.getKey();
            Map<String, Resource<T>> containerResources = e.getValue();
            Map<String, Resource<T>> containerClonedResources = new HashMap<>(containerResources.size());
            clonedCache.resourcesByContainerId.put(containerId, containerClonedResources);
            for (String id : containerResources.keySet()) {
                containerClonedResources.put(id, clonedCache.resources.get(id));
            }
        }
        for (Map.Entry<String, Set<String>> entry : removedExtensionAttributes.entrySet()) {
            clonedCache.removedExtensionAttributes.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        clonedCache.fullyLoadedExtensionsByExtensionName.addAll(fullyLoadedExtensionsByExtensionName);
        clonedCache.fullyLoadedExtensionsByIdentifiableIds.addAll(fullyLoadedExtensionsByIdentifiableIds);
        clonedCache.fullyLoaded = fullyLoaded;
        clonedCache.fullyLoadedExtensions = fullyLoadedExtensions;
        clonedCache.containerFullyLoaded.addAll(containerFullyLoaded);
        clonedCache.removedResources.addAll(removedResources);
        return clonedCache;
    }

    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType type, String identifiableId, String extensionName) {
        Objects.requireNonNull(identifiableId);

        if (isExtensionAttributesCached(identifiableId, extensionName)) {
            return Optional.ofNullable(getCachedExtensionAttributes(identifiableId).get(extensionName));
        }

        if (!isFullyLoadedExtension(identifiableId, extensionName) && !isRemovedAttributes(identifiableId, extensionName)) {
            return delegate.getExtensionAttributes(networkUuid, variantNum, type, identifiableId, extensionName)
                    .map(attributes -> {
                        addExtensionAttributesToCache(identifiableId, extensionName, attributes);
                        return attributes;
                    });
        }
        return Optional.empty();
    }

    private Map<String, ExtensionAttributes> getCachedExtensionAttributes(String identifiableId) {
        Resource<T> resource = resources.get(identifiableId);
        if (resource != null) {
            return resource.getAttributes().getExtensionAttributes();
        } else {
            throw new PowsyblException("Cannot manipulate extensions for identifiable (" + identifiableId + ") as it has not been loaded into the cache.");
        }
    }

    private boolean isFullyLoadedExtension(String identifiableId, String extensionName) {
        return fullyLoadedExtensions || fullyLoadedExtensionsByIdentifiableIds.contains(identifiableId) || fullyLoadedExtensionsByExtensionName.contains(extensionName);
    }

    private boolean isFullyLoadedExtension(String extensionName) {
        return fullyLoadedExtensions || fullyLoadedExtensionsByExtensionName.contains(extensionName);
    }

    private boolean isRemovedAttributes(String id, String extensionName) {
        return removedResources.contains(id) || removedExtensionAttributes.containsKey(id) && removedExtensionAttributes.get(id).contains(extensionName);
    }

    private boolean isExtensionAttributesCached(String id, String extensionName) {
        return resources.containsKey(id) && getCachedExtensionAttributes(id).containsKey(extensionName);
    }

    /**
     * Add extension attributes in the cache for single extension attributes loading
     */
    private void addExtensionAttributesToCache(String identifiableId, String extensionName, ExtensionAttributes extensionAttributes) {
        Objects.requireNonNull(extensionAttributes);

        // notice: even if it adds some checks and reduces performance by a tiny bit, we avoid to overwrite already
        // loaded extension attributes because they are referenced in the resources or resourcesByContainerId map,
        // but also directly in any identifiable with the iidm api.
        getCachedExtensionAttributes(identifiableId).putIfAbsent(extensionName, extensionAttributes);
        Set<String> extensions = removedExtensionAttributes.get(identifiableId);
        if (extensions != null) {
            extensions.remove(extensionName);
            if (extensions.isEmpty()) {
                removedExtensionAttributes.remove(identifiableId);
            }
        }
    }

    /**
     * Get the extensions attributes with specified extension name for all the identifiables of the collection in the cache.
     */
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByResourceTypeAndExtensionName(UUID networkUuid, int variantNum, ResourceType type, String extensionName) {
        if (!isFullyLoadedExtension(extensionName)) {
            // if collection has not yet been fully loaded we load it from the server
            Map<String, ExtensionAttributes> extensionAttributesMap = delegate.getAllExtensionsAttributesByResourceTypeAndExtensionName(networkUuid, variantNum, type, extensionName);

            // we update the full cache and set it as fully loaded
            extensionAttributesMap.forEach((identifiableId, extensionAttributes) -> addExtensionAttributesToCache(identifiableId, extensionName, extensionAttributes));
            fullyLoadedExtensionsByExtensionName.add(extensionName);
        }
        //TODO This method is only used to load extension attributes in the collection cache when using preloading collection.
        // The return is never used by the client as the call to getAllExtensionsAttributesByResourceTypeAndExtensionName() is always followed
        // by a call to getExtensionAttributes(). The latter returns something meaningful for the client
        // and it's used in the identifiable.getExtension() method. The map extensionAttributesMap can't be stored in the cache to be returned
        // as we can't ensure synchronization with the resources map (if extensions or identifiables are updated/removed).
        // We should refactor this method to return void.
        return null;
    }

    /**
     * Get all extension attributes for one identifiable of the collection.
     */
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType type, String identifiableId) {
        Objects.requireNonNull(identifiableId);
        if (isExtensionAttributesCached(identifiableId)) {
            return getCachedExtensionAttributes(identifiableId);
        }

        if (!isFullyLoadedIdentifiable(identifiableId)) {
            Map<String, ExtensionAttributes> extensionAttributes = delegate.getAllExtensionsAttributesByIdentifiableId(networkUuid, variantNum, type, identifiableId);
            if (extensionAttributes != null) {
                addAllExtensionAttributesToCache(identifiableId, extensionAttributes);
                return extensionAttributes;
            }
        }
        return Map.of();
    }

    private boolean isFullyLoadedIdentifiable(String identifiableId) {
        return fullyLoadedExtensions || fullyLoadedExtensionsByIdentifiableIds.contains(identifiableId);
    }

    private boolean isExtensionAttributesCached(String identifiableId) {
        return (fullyLoadedExtensionsByIdentifiableIds.contains(identifiableId) || fullyLoadedExtensions) && resources.containsKey(identifiableId); // am i sure?
    }

    /**
     * Add extension attributes to the cache when loading all the extension attributes of an identifiable
     */
    private void addAllExtensionAttributesToCache(String id, Map<String, ExtensionAttributes> extensionAttributes) {
        Objects.requireNonNull(extensionAttributes);

        // notice: even if it adds some checks and reduces performance by a tiny bit, we avoid to overwrite already
        // loaded extension attributes because they are referenced in the resources or resourcesByContainerId map,
        // but also directly in any identifiable with the iidm api.
        extensionAttributes.forEach(getCachedExtensionAttributes(id)::putIfAbsent);
        fullyLoadedExtensionsByIdentifiableIds.add(id);
        removedExtensionAttributes.remove(id);
    }

    /**
     * Get all the extensions attributes for all the identifiables with specified resource type in the cache
     */
    public Map<String, Map<String, ExtensionAttributes>> getAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType type) {
        if (!fullyLoadedExtensions) {
            // if collection has not yet been fully loaded we load it from the server
            Map<String, Map<String, ExtensionAttributes>> extensionAttributesMap = delegate.getAllExtensionsAttributesByResourceType(networkUuid, variantNum, type);

            // we update the full cache and set it as fully loaded
            extensionAttributesMap.forEach(this::addAllExtensionAttributesToCache);
            fullyLoadedExtensions = true;
        }
        //TODO This method is only used to load extension attributes in the collection cache when using preloading collection.
        // The return is never used by the client as the call to getAllExtensionsAttributesByResourceType() is always followed
        // by a call to getAllExtensionsAttributesByIdentifiableId(). The latter returns something meaningful for the client
        // and it's used in the identifiable.getExtensions() method. The map extensionAttributesMap can't be stored in the cache to be returned
        // as we can't ensure synchronization with the resources map (if extensions or identifiables are updated/removed).
        // We should refactor this method to return void.
        return null;
    }

    public void removeExtensionAttributesByExtensionName(String identifiableId, String extensionName) {
        Objects.requireNonNull(identifiableId);
        Objects.requireNonNull(extensionName);
        if (resources.containsKey(identifiableId)) {
            getCachedExtensionAttributes(identifiableId).remove(extensionName);
            removedExtensionAttributes.computeIfAbsent(identifiableId, k -> new HashSet<>()).add(extensionName);
        }
    }

    public void removeExtensionAttributesByIdentifiableId(String identifiableId) {
        Objects.requireNonNull(identifiableId);
        if (resources.containsKey(identifiableId)) {
            Set<String> removedExtensionNames = getCachedExtensionAttributes(identifiableId).keySet();
            removedExtensionAttributes.computeIfAbsent(identifiableId, k -> new HashSet<>()).addAll(removedExtensionNames);
        }
    }
}
