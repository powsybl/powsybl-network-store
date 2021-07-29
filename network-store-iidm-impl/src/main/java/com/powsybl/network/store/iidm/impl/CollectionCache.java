/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Contained;
import com.powsybl.network.store.model.Resource;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Identifiable collection cache management.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionCache<T extends IdentifiableAttributes> {

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
     * A function to load one resource from the server. An optional is returned because resource could not exist on
     * the server.
     */
    private final Function<String, Optional<Resource<T>>> oneLoaderFunction;

    /**
     * A function to load resources from a container (so this is a just part of the full collection)
     */
    private final Function<String, List<Resource<T>>> containerLoaderFunction;

    /**
     * A function to load all resources of the collection.
     */
    private final Supplier<List<Resource<T>>> allLoaderFunction;

    public CollectionCache(Function<String, Optional<Resource<T>>> oneLoaderFunction,
                           Function<String, List<Resource<T>>> containerLoaderFunction,
                           Supplier<List<Resource<T>>> allLoaderFunction) {
        this.oneLoaderFunction = Objects.requireNonNull(oneLoaderFunction);
        this.containerLoaderFunction = containerLoaderFunction;
        this.allLoaderFunction = Objects.requireNonNull(allLoaderFunction);
    }

    /**
     * Declare the collection as fully initialized. It means that the collection exists on client side but not yet on server
     * side and that even if empty the collection is fully loaded.
     */
    public void init() {
        fullyLoaded = true;
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
    public Optional<Resource<T>> getResource(String id) {
        Objects.requireNonNull(id);

        Resource<T> resource = null;

        if (resources.containsKey(id)) {
            // resource is in the cache
            resource = resources.get(id);
        } else {
            // if resource has not been fully loaded (so in that case it means the resource does not exist)
            // of if the resource has not been removed we try to get it from the server
            if (!fullyLoaded && !removedResources.contains(id)) {
                resource = oneLoaderFunction.apply(id).orElse(null);
                // if resource has been found on server side we add it to the cache
                if (resource != null) {
                    addResource(resource);
                }
            }
        }

        return Optional.ofNullable(resource);
    }

    private void loadAll() {
        if (!fullyLoaded) {
            // if collection has not yet been fully loaded we load it from the server
            List<Resource<T>> resourcesToAdd = allLoaderFunction.get();

            // we update the full cache and set it as fully loaded
            // notice: it might overwrite already loaded resource (single or container)
            resourcesToAdd.forEach(resource -> resources.put(resource.getId(), resource));
            fullyLoaded = true;

            // we update by container cache
            for (Resource<T> resource : resourcesToAdd) {
                IdentifiableAttributes attributes = resource.getAttributes();
                if (attributes instanceof Contained) {
                    Set<String> containerIds = ((Contained) attributes).getContainerIds();
                    containerIds.forEach(containerId -> {
                        // we add container resources and update container fully loaded status
                        getResourcesByContainerId(containerId).put(resource.getId(), resource);
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
    public List<Resource<T>> getResources() {
        loadAll();
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
    public List<Resource<T>> getContainerResources(String containerId) {
        Objects.requireNonNull(containerId);
        if (containerLoaderFunction == null) {
            throw new PowsyblException("it is not possible to load resources by container, if container resources loader has not been specified");
        }

        if (!fullyLoaded && !containerFullyLoaded.contains(containerId)) {
            List<Resource<T>> resourcesToAdd = containerLoaderFunction.apply(containerId);

            // by container cache update
            getResourcesByContainerId(containerId).putAll(resourcesToAdd.stream().collect(Collectors.toMap(Resource::getId, resource -> resource)));
            containerFullyLoaded.add(containerId);

            // full cache update
            resourcesToAdd.forEach(resource -> {
                resources.put(resource.getId(), resource);
                removedResources.remove(resource.getId());
            });
        }
        return new ArrayList<>(getResourcesByContainerId(containerId).values());
    }

    private void addResource(Resource<T> resource) {
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

    /**
     * Add a new resource to the collection.
     *
     * @param resource the newly created resources
     */
    public void createResource(Resource<T> resource) {
        addResource(resource);
    }

    /**
     * Update (replace) a resource of the collection.
     *
     * @param resource the resources to update
     */
    public void updateResource(Resource<T> resource) {
        addResource(resource);
    }

    /**
     * Remove a resource from the collection.
     *
     * @param id the id of the resource to remove
     */
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

    /**
     * Get resource count.
     *
     * @return the resource count
     */
    public int getResourceCount() {
        // the only reliable way to get count is to fully load the collection
        loadAll();
        return resources.size();
    }
}
