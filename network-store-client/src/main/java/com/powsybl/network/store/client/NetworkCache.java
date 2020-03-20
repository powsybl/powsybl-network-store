/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.google.common.collect.ImmutableList;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.RelatedVoltageLevelsAttributes;
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

        private final Map<String, Set<Resource<T>>> resourcesByVoltageLevel = new HashMap<>();

        private boolean isFullyLoaded = false;

        private final Map<String, Boolean> isByVoltageLevelFullyLoaded = new HashMap<>();

        public boolean isFullyLoaded(String voltageLevelId) {
            return isByVoltageLevelFullyLoaded.getOrDefault(voltageLevelId, false);
        }

        public boolean isFullyLoaded() {
            return isFullyLoaded;
        }

        public Resource<T> get(String id) {
            return resources.get(id);
        }

        public boolean contains(String id) {
            return resources.containsKey(id);
        }

        public List<Resource<T>> getAll() {
            return new ArrayList<>(resources.values());
        }

        public void add(String id, Resource<T> resource) {
            resources.put(id, resource);
            if (resource != null) {
                IdentifiableAttributes attributes = resource.getAttributes();
                if (attributes instanceof RelatedVoltageLevelsAttributes) {
                    (((RelatedVoltageLevelsAttributes) attributes).getVoltageLevels()).forEach(voltageLevelId -> resourcesByVoltageLevel.computeIfAbsent(voltageLevelId, k -> new HashSet<>()).add(resource));
                }
            }
        }

        public void fill(List<Resource<T>> resourcesToAdd) {
            resourcesToAdd.forEach(resource -> resources.put(resource.getId(), resource));
            isFullyLoaded = true;

            // Update of cache by voltage level
            Map<String, Set<Resource<T>>> resourcesByVoltageLevelToAdd = new HashMap<>();
            for (Resource<T> resource : resourcesToAdd) {
                IdentifiableAttributes attributes = resource.getAttributes();
                if (attributes instanceof RelatedVoltageLevelsAttributes) {
                    (((RelatedVoltageLevelsAttributes) attributes).getVoltageLevels()).forEach(voltageLevelId -> resourcesByVoltageLevelToAdd.computeIfAbsent(voltageLevelId, k -> new HashSet<>()).add(resource));
                }
            }
            for (Map.Entry<String, Set<Resource<T>>> resourcesToAddByVoltageLevel : resourcesByVoltageLevelToAdd.entrySet()) {
                resourcesByVoltageLevel.computeIfAbsent(resourcesToAddByVoltageLevel.getKey(), k -> new HashSet<>()).addAll(resourcesToAddByVoltageLevel.getValue());
                isByVoltageLevelFullyLoaded.put(resourcesToAddByVoltageLevel.getKey(), true);
            }
        }

        public List<Resource<T>> getAll(String voltageLevelId) {
            return ImmutableList.<Resource<T>>builder().addAll(resourcesByVoltageLevel.computeIfAbsent(voltageLevelId, k -> new HashSet<>())).build();
        }

        public void fillByVoltageLevel(String voltageLevelId, List<Resource<T>> resourcesToAdd) {
            resourcesByVoltageLevel.put(voltageLevelId, new HashSet<>(resourcesToAdd));
            isByVoltageLevelFullyLoaded.put(voltageLevelId, true);

            // Update of full cache
            resourcesToAdd.forEach(resource -> resources.put(resource.getId(), resource));
        }
    }

    private final Map<ResourceType, ResourceCache> resourcesCaches = new EnumMap<>(ResourceType.class);

    public NetworkCache(UUID networkId) {
        this.networkId = networkId;
    }

    public <T extends IdentifiableAttributes> Optional<Resource<T>> getResource(ResourceType resourceType, String resourceId, Function<String, Optional<Resource<T>>> loaderFunction) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        Resource<T> resource;
        if (resourceCache.contains(resourceId)) {
            resource = resourceCache.get(resourceId);
        } else {
            resource = loaderFunction.apply(resourceId).orElse(null);
            resourceCache.add(resourceId, resource);
        }

        return Optional.ofNullable(resource);
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getAllResources(ResourceType resourceType, Supplier<List<Resource<T>>> loaderFunction) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        List<Resource<T>> resources;
        if (resourceCache.isFullyLoaded()) {
            resources = resourceCache.getAll();
        } else {
            resources = loaderFunction.get();
            resourceCache.fill(resources);
        }

        return resources;
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getResourcesByVoltageId(ResourceType resourceType, String voltageLevelId,  Function<String, List<Resource<T>>> loaderFunction) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        List<Resource<T>> resources;
        if (resourceCache.isFullyLoaded(voltageLevelId)) {
            resources = resourceCache.getAll(voltageLevelId);
        } else {
            resources = loaderFunction.apply(voltageLevelId);
            resourceCache.fillByVoltageLevel(voltageLevelId, resources);
        }

        return resources;
    }

    public <T extends IdentifiableAttributes> void addResources(ResourceType resourceType, List<Resource<T>> resources) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        for (Resource<T> resource : resources) {
            resourceCache.add(resource.getId(), resource);
        }
    }

    public void invalidate() {
        resourcesCaches.clear();
    }

}
