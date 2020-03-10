/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class NetworkCache {

    private final UUID networkId;

    private class ResourceCache<T extends IdentifiableAttributes> {

        private final Map<String, Resource<T>> resources = new HashMap<>();

        private boolean isFullyLoaded = false;

        public ResourceCache() {
        }

        public boolean isFullyLoaded() {
            return isFullyLoaded;
        }

        public Resource<T> get(String id) {
            return resources.get(id);
        }

        public List<Resource<T>> getAll() {
            return new ArrayList<Resource<T>>(resources.values());
        }

        public void put(String id, Resource<T> resource) {
            resources.put(id, resource);
        }

        public void addAll(List<Resource<T>> resourcesToAdd) {
            resourcesToAdd.stream().map(resource -> resources.put(resource.getId(), resource));
            isFullyLoaded = true;
        }
    }

    private class ResourceByVoltageLevelCache<T extends IdentifiableAttributes> {

        private final Map<String, Set<Resource<T>>> resourcesByVoltageLevel = new HashMap<>();

        Map<String, Boolean> isFullyLoaded = new HashMap<>();

        public ResourceByVoltageLevelCache() {
        }

        public boolean isFullyLoaded(String voltageLevelId) {
            return isFullyLoaded.containsKey(voltageLevelId) ? isFullyLoaded.get(voltageLevelId) : false;
        }

        public List<Resource<T>> getAll(String voltageLevelId) {
            return ImmutableList.<Resource<T>>builder().addAll(resourcesByVoltageLevel.computeIfAbsent(voltageLevelId, k -> new HashSet<>())).build();
        }

        public void addAll(String voltageLevelId, List<Resource<T>> resourcesToAdd) {
            resourcesByVoltageLevel.put(voltageLevelId, ImmutableSet.<Resource<T>>builder().addAll(resourcesToAdd).build());
            isFullyLoaded.put(voltageLevelId, true);
        }
    }

    private final Map<ResourceType, ResourceCache> resourcesCaches = new HashMap<>();

    private final Map<ResourceType, ResourceByVoltageLevelCache> resourcesByVoltagelevelCaches = new HashMap<>();

    public NetworkCache(UUID networkId) {
        this.networkId = networkId;
    }

    public <T extends IdentifiableAttributes> Optional<Resource<T>> getResource(ResourceType resourceType, String resourceId, Function<String, Optional<Resource<T>>> loaderFunction) {
        Resource<T> resource = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache()).get(resourceId);
        if (resource == null) {
            resource = loaderFunction.apply(resourceId).orElse(null);
            resourcesCaches.get(resourceType).put(resourceId, resource);
        }

        return Optional.ofNullable(resource);
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getAllResources(ResourceType resourceType, Supplier<List<Resource<T>>> loaderFunction) {
        List<Resource<T>> resources;
        if (resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache()).isFullyLoaded()) {
            resources = resourcesCaches.get(resourceType).getAll();
        } else {
            resources = loaderFunction.get();
            resourcesCaches.get(resourceType).addAll(resources);
        }

        return resources;
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getResourcesByVoltageId(ResourceType resourceType, String voltageLevelId,  Function<String, List<Resource<T>>> loaderFunction) {
        List<Resource<T>> resources;
        if (resourcesByVoltagelevelCaches.computeIfAbsent(resourceType, k -> new ResourceByVoltageLevelCache()).isFullyLoaded(voltageLevelId)) {
            resources = resourcesByVoltagelevelCaches.get(resourceType).getAll(voltageLevelId);
        } else {
            resources = loaderFunction.apply(voltageLevelId);
            resourcesByVoltagelevelCaches.get(resourceType).addAll(voltageLevelId, resources);
        }

        return resources;
    }

}
