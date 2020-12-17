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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Identifiable collection cache management.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionCache<T extends IdentifiableAttributes> {

    private class VariantCache {

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


        private Map<String, Resource<T>> getResourcesByContainerId(String containerId) {
            return resourcesByContainerId.computeIfAbsent(containerId, k -> new LinkedHashMap<>());
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
    }

    private final Map<Integer, VariantCache> cacheByVariant = new HashMap<>();

    /**
     * A function to load one resource from the server. An optional is returned because resource could not exist on
     * the server.
     */
    private final BiFunction<Integer, String, Optional<Resource<T>>> oneLoaderFunction;

    /**
     * A function to load resources from a container (so this is a just part of the full collection)
     */
    private final BiFunction<Integer, String, List<Resource<T>>> containerLoaderFunction;

    /**
     * A function to load all resources of the collection.
     */
    private final Function<Integer, List<Resource<T>>> allLoaderFunction;

    public CollectionCache(BiFunction<Integer, String, Optional<Resource<T>>> oneLoaderFunction,
                           BiFunction<Integer, String, List<Resource<T>>> containerLoaderFunction,
                           Function<Integer, List<Resource<T>>> allLoaderFunction) {
        this.oneLoaderFunction = Objects.requireNonNull(oneLoaderFunction);
        this.containerLoaderFunction = containerLoaderFunction;
        this.allLoaderFunction = Objects.requireNonNull(allLoaderFunction);
    }

    private VariantCache getVariantCache(int variantNum) {
        return cacheByVariant.computeIfAbsent(variantNum, k -> new VariantCache());
    }

    /**
     * Declare the collection as fully initialized. It means that the collection exists on client side but not yet on server
     * side and that even if empty the collection is fully loaded.
     */
    public void init(int variantNum) {
        getVariantCache(variantNum).fullyLoaded = true;
    }

    /**
     */
    public void initContainer(int variantNum, String containerId) {
        Objects.requireNonNull(containerId);
        VariantCache variantCache = getVariantCache(variantNum);
        variantCache.containerFullyLoaded.add(containerId);
    }

    /**
     * Get a resource from the collection by its id. If resource has not been found on cache, it is loaded
     * from the server.
     * @param id id of the resource
     * @return a resource from the collection
     */
    public Optional<Resource<T>> getResource(int variantNum, String id) {
        Objects.requireNonNull(id);

        VariantCache variantCache = getVariantCache(variantNum);

        Resource<T> resource = null;

        if (variantCache.resources.containsKey(id)) {
            // resource is in the cache
            resource = variantCache.resources.get(id);
        } else {
            // if resource has not been fully loaded (so in that case it means the resource does not exist)
            // of if the resource has not been removed we try to get it from the server
            if (!variantCache.fullyLoaded && !variantCache.removedResources.contains(id)) {
                resource = oneLoaderFunction.apply(variantNum, id).orElse(null);
                // if resource has been found on server side we add it to the cache
                if (resource != null) {
                    variantCache.addResource(resource);
                }
            }
        }

        return Optional.ofNullable(resource);
    }

    private Map<String, Resource<T>> loadAll(int variantNum) {
        VariantCache variantCache = getVariantCache(variantNum);

        if (!variantCache.fullyLoaded) {
            // if collection has not yet been fully loaded we load it from the server
            List<Resource<T>> resourcesToAdd = allLoaderFunction.apply(variantNum);

            // we update the full cache and set it as fully loaded
            // notice: it might overwrite already loaded resource (single or container)
            resourcesToAdd.forEach(resource -> variantCache.resources.put(resource.getId(), resource));
            variantCache.fullyLoaded = true;

            // we update by container cache
            for (Resource<T> resource : resourcesToAdd) {
                IdentifiableAttributes attributes = resource.getAttributes();
                if (attributes instanceof Contained) {
                    Set<String> containerIds = ((Contained) attributes).getContainerIds();
                    containerIds.forEach(containerId -> {
                        // we add container resources and update container fully loaded status
                        variantCache.getResourcesByContainerId(containerId).put(resource.getId(), resource);
                        variantCache.containerFullyLoaded.add(containerId);
                    });
                }

                // discard remove status of the resources
                variantCache.removedResources.remove(resource.getId());
            }
        }

        return variantCache.resources;
    }

    /**
     * Get all resources of the collection. If all resources have not already been fully loaded we load them from
     * the server.
     * @return all resources of the collection
     */
    public List<Resource<T>> getResources(int variantNum) {
        return new ArrayList<>(loadAll(variantNum).values());
    }

    /**
     * Get all resources of the collection that belongs to a container. If container resources have not yet been fully
     * loaded we load them from the server.
     * @param containerId the container id
     * @return all resources of the collection that belongs to the container
     */
    public List<Resource<T>> getContainerResources(int variantNum, String containerId) {
        Objects.requireNonNull(containerId);
        if (containerLoaderFunction == null) {
            throw new PowsyblException("it is not possible to load resources by container, if container resources loader has not been specified");
        }

        VariantCache variantCache = getVariantCache(variantNum);

        if (!variantCache.fullyLoaded && !variantCache.containerFullyLoaded.contains(containerId)) {
            List<Resource<T>> resourcesToAdd = containerLoaderFunction.apply(variantNum, containerId);

            // by container cache update
            variantCache.getResourcesByContainerId(containerId).putAll(resourcesToAdd.stream().collect(Collectors.toMap(Resource::getId, resource -> resource)));
            variantCache.containerFullyLoaded.add(containerId);

            // full cache update
            resourcesToAdd.forEach(resource -> {
                variantCache.resources.put(resource.getId(), resource);
                variantCache.removedResources.remove(resource.getId());
            });
        }
        return new ArrayList<>(variantCache.getResourcesByContainerId(containerId).values());
    }

    /**
     * Add new resources to the collection.
     *
     * @param resources newly created resources
     */
    public void createResources(int variantNum, List<Resource<T>> resources) {
        VariantCache variantCache = getVariantCache(variantNum);

        for (Resource<T> resource : resources) {
            variantCache.addResource(resource);
        }
    }

    /**
     * Update (replace) a resource of the collection.
     *
     * @param resource the resource to update
     */
    public void updateResource(int variantNum, Resource<T> resource) {
        VariantCache variantCache = getVariantCache(variantNum);

        variantCache.addResource(resource);
    }

    /**
     * Remove a resource from the collection.
     *
     * @param id the id of the resource to remove
     */
    public void removeResource(int variantNum, String id) {
        Objects.requireNonNull(id);

        VariantCache variantCache = getVariantCache(variantNum);

        // try to remove the resource from full cache
        Resource<T> resource = variantCache.resources.remove(id);
        variantCache.removedResources.add(id);

        // if resource has been found also remove it from container cache
        if (resource != null) {
            IdentifiableAttributes attributes = resource.getAttributes();
            if (attributes instanceof Contained) {
                Set<String> containerIds = ((Contained) attributes).getContainerIds();
                containerIds.forEach(containerId -> variantCache.getResourcesByContainerId(containerId).remove(resource.getId()));
            }
        }
    }

    /**
     * Get resource count.
     *
     * @return the resource count
     */
    public int getResourceCount(int variantNum) {
        // the only reliable way to get count is to fully load the collection
        return loadAll(variantNum).size();
    }
}
