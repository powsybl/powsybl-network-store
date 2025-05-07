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
 * Handles basic resource caching operations
 */
public class ResourceCacheHandler<T extends IdentifiableAttributes> implements CacheHandler {
    /**
     * Resources indexed by id.
     */
    private final Map<String, Resource<T>> resources = new HashMap<>();

    /**
     * true if collection has been fully load, so if the cache is synchronized with the server, false otherwise.
     */
    private boolean fullyLoaded = false;

    /**
     * Resources indexed by container id. A container is either a substation or a voltage level.
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
     * A function to load one resource from the server.
     */
    private final TriFunction<UUID, Integer, String, Optional<Resource<T>>> oneLoaderFunction;

    /**
     * A function to load resources from a container
     */
    private final TriFunction<UUID, Integer, String, List<Resource<T>>> containerLoaderFunction;

    /**
     * A function to load all resources of the collection.
     */
    private final BiFunction<UUID, Integer, List<Resource<T>>> allLoaderFunction;

    public ResourceCacheHandler(TriFunction<UUID, Integer, String, Optional<Resource<T>>> oneLoaderFunction,
                                TriFunction<UUID, Integer, String, List<Resource<T>>> containerLoaderFunction,
                                BiFunction<UUID, Integer, List<Resource<T>>> allLoaderFunction) {
        this.oneLoaderFunction = Objects.requireNonNull(oneLoaderFunction);
        this.containerLoaderFunction = containerLoaderFunction;
        this.allLoaderFunction = Objects.requireNonNull(allLoaderFunction);
    }

    @Override
    public void init() {
        fullyLoaded = true;
    }

    public boolean isResourceLoaded(String id) {
        return resources.containsKey(id);
    }

    public List<Resource<T>> getCachedResources() {
        return new ArrayList<>(resources.values());
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public void initContainer(String containerId) {
        Objects.requireNonNull(containerId);
        containerFullyLoaded.add(containerId);
    }

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
                    // we already checked that the resource is not in the cache so we can directly put it in the cache
                    addOrReplaceResource(resource);
                }
            }
        }
        return Optional.ofNullable(resource);
    }

    public void loadAll(UUID networkUuid, int variantNum) {
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

    public List<Resource<T>> getResources(UUID networkUuid, int variantNum) {
        loadAll(networkUuid, variantNum);
        return new ArrayList<>(resources.values());
    }

    public Map<String, Resource<T>> getResourcesByContainerId(String containerId) {
        return resourcesByContainerId.computeIfAbsent(containerId, k -> new LinkedHashMap<>());
    }

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

    public void addOrReplaceResource(Resource<T> resource) {
        Objects.requireNonNull(resource);
        // full cache update
        resources.put(resource.getId(), resource);
        removedResources.remove(resource.getId());
        // by container cache update
        IdentifiableAttributes attributes = resource.getAttributes();
        if (attributes instanceof Contained) {
            Set<String> containerIds = ((Contained) attributes).getContainerIds();
            containerIds.forEach(containerId -> getResourcesByContainerId(containerId).put(resource.getId(), resource));
        }
    }

    public void createResource(Resource<T> resource) {
        String resourceId = resource.getId();
        if (resources.containsKey(resourceId)) {
            throw new PowsyblException("The collection cache already contains a " + resource.getType() + " with the id '" + resourceId + "'");
        }
        // we already checked that the resource is not in the cache so we can directly put it in the cache
        addOrReplaceResource(resource);
    }

    public void updateResource(Resource<T> resource) {
        addOrReplaceResource(resource);
    }

    public void removeResource(String id) {
        Objects.requireNonNull(id);
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

    public int getResourceCount(UUID networkUuid, int variantNum) {
        // the only reliable way to get count is to fully load the collection
        loadAll(networkUuid, variantNum);
        return resources.size();
    }

    public Resource<T> getResourceById(String id) {
        return resources.get(id);
    }

    public boolean isContainerFullyLoaded(String containerId) {
        return containerFullyLoaded.contains(containerId);
    }

    public boolean isResourceRemoved(String id) {
        return removedResources.contains(id);
    }

    public void cloneTo(ResourceCacheHandler<T> target, ObjectMapper objectMapper, int newVariantNum, Consumer<Resource<T>> resourcePostProcessor) {
        // use json serialization to clone the resources of source collection
        List<Resource<T>> clonedResources = Resource.cloneResourcesToVariant(resources.values(), newVariantNum, objectMapper, resourcePostProcessor);
        for (Resource<T> clonedResource : clonedResources) {
            target.resources.put(clonedResource.getId(), clonedResource);
        }
        for (Map.Entry<String, Map<String, Resource<T>>> e : resourcesByContainerId.entrySet()) {
            String containerId = e.getKey();
            Map<String, Resource<T>> containerResources = e.getValue();
            Map<String, Resource<T>> containerClonedResources = new HashMap<>(containerResources.size());
            target.resourcesByContainerId.put(containerId, containerClonedResources);
            for (String id : containerResources.keySet()) {
                containerClonedResources.put(id, target.resources.get(id));
            }
        }

        target.containerFullyLoaded.addAll(containerFullyLoaded);
        target.removedResources.addAll(removedResources);
        target.fullyLoaded = fullyLoaded;
    }
}
