/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.google.common.collect.ImmutableList;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class NetworkCache {

    private final UUID networkId;

    private Resource<NetworkAttributes> networkResource;

    private class ResourceCache<T extends IdentifiableAttributes> {

        private final Map<String, Resource<T>> resources = new HashMap<>();

        private final Map<String, Set<Resource<T>>> resourcesByContainerId = new HashMap<>();

        private boolean isFullyLoaded = false;

        private final Map<String, Boolean> isByContainerIdFullyLoaded = new HashMap<>();

        public boolean isFullyLoaded(String containerId) {
            return isByContainerIdFullyLoaded.getOrDefault(containerId, false);
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
                    (((RelatedVoltageLevelsAttributes) attributes).getVoltageLevels()).forEach(voltageLevelId -> resourcesByContainerId.computeIfAbsent(voltageLevelId, k -> new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()))).add(resource));
                } else if (attributes instanceof VoltageLevelAttributes) {
                    resourcesByContainerId.computeIfAbsent(((VoltageLevelAttributes) attributes).getSubstationId(), k -> new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()))).add(resource);
                }
            }
        }

        public void removeResource(String id) {
            if (resources.containsKey(id)) {
                Resource<T> resource = resources.get(id);
                resources.remove(id);
                if (resource != null) {
                    IdentifiableAttributes attributes = resource.getAttributes();
                    if (attributes instanceof RelatedVoltageLevelsAttributes) {
                        (((RelatedVoltageLevelsAttributes) attributes).getVoltageLevels()).forEach(voltageLevelId -> resourcesByContainerId.computeIfAbsent(voltageLevelId, k -> new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()))).remove(resource));
                    } else if (attributes instanceof VoltageLevelAttributes) {
                        resourcesByContainerId.computeIfAbsent(((VoltageLevelAttributes) attributes).getSubstationId(), k -> new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()))).remove(resource);
                    }
                }
            }
        }

        public void fill(List<Resource<T>> resourcesToAdd) {
            resourcesToAdd.forEach(resource -> resources.put(resource.getId(), resource));
            isFullyLoaded = true;

            // Update of cache by container id
            Map<String, Set<Resource<T>>> resourcesByContainerIdToAdd = new HashMap<>();
            for (Resource<T> resource : resourcesToAdd) {
                IdentifiableAttributes attributes = resource.getAttributes();
                if (attributes instanceof RelatedVoltageLevelsAttributes) {
                    (((RelatedVoltageLevelsAttributes) attributes).getVoltageLevels()).forEach(voltageLevelId -> resourcesByContainerIdToAdd.computeIfAbsent(voltageLevelId, k -> new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()))).add(resource));
                } else if (attributes instanceof VoltageLevelAttributes) {
                    resourcesByContainerIdToAdd.computeIfAbsent(((VoltageLevelAttributes) attributes).getSubstationId(), k -> new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()))).add(resource);
                }
            }
            for (Map.Entry<String, Set<Resource<T>>> resourcesToAddByVoltageLevel : resourcesByContainerIdToAdd.entrySet()) {
                resourcesByContainerId.computeIfAbsent(resourcesToAddByVoltageLevel.getKey(), k -> new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()))).addAll(resourcesToAddByVoltageLevel.getValue());
                isByContainerIdFullyLoaded.put(resourcesToAddByVoltageLevel.getKey(), true);
            }
        }

        public List<Resource<T>> getAll(String voltageLevelId) {
            return ImmutableList.<Resource<T>>builder().addAll(resourcesByContainerId.computeIfAbsent(voltageLevelId, k -> new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId())))).build();
        }

        public int getResourceCount() {
            return resources.size();
        }

        public void fillByVoltageLevel(String voltageLevelId, List<Resource<T>> resourcesToAdd) {
            Set<Resource<T>> resourcesSetToAdd = new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()));
            resourcesSetToAdd.addAll(resourcesToAdd);
            resourcesByContainerId.put(voltageLevelId, resourcesSetToAdd);
            isByContainerIdFullyLoaded.put(voltageLevelId, true);

            // Update of full cache
            resourcesToAdd.forEach(resource -> resources.put(resource.getId(), resource));
        }
    }

    private final Map<ResourceType, ResourceCache> resourcesCaches = new EnumMap<>(ResourceType.class);

    public NetworkCache(UUID networkId) {
        this.networkId = networkId;
    }

    public void setNetworkResource(Resource<NetworkAttributes> networkResource) {
        this.networkResource = networkResource;
    }

    public Resource<NetworkAttributes> getNetworkResource() {
        return networkResource;
    }

    public Optional<Resource<NetworkAttributes>> getNetworkResource(Supplier<Optional<Resource<NetworkAttributes>>> loaderFunction) {
        Resource<NetworkAttributes> resource;
        if (networkResource != null) {
            resource = networkResource;
        } else {
            resource = loaderFunction.get().orElse(null);
            networkResource = resource;
        }

        return Optional.ofNullable(resource);
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

    public <T extends IdentifiableAttributes> Optional<Resource<T>> getResource(ResourceType resourceType, String resourceId) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        Resource<T> resource = null;
        if (resourceCache.contains(resourceId)) {
            resource = resourceCache.get(resourceId);
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

    public <T extends IdentifiableAttributes> List<Resource<T>> getAllResources(ResourceType resourceType) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        List<Resource<T>> resources = resourceCache.getAll();

        return resources;
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getResourcesByContainerId(ResourceType resourceType, String containerId,  Function<String, List<Resource<T>>> loaderFunction) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        List<Resource<T>> resources;
        if (resourceCache.isFullyLoaded(containerId)) {
            resources = resourceCache.getAll(containerId);
        } else {
            resources = loaderFunction.apply(containerId);
            resourceCache.fillByVoltageLevel(containerId, resources);
        }

        return resources;
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getResourcesByContainerId(ResourceType resourceType, String containerId) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        List<Resource<T>> resources = resourceCache.getAll(containerId);

        return resources;
    }

    public <T extends IdentifiableAttributes> void addResources(ResourceType resourceType, List<Resource<T>> resources) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());

        for (Resource<T> resource : resources) {
            resourceCache.add(resource.getId(), resource);
        }
    }

    public <T extends IdentifiableAttributes> void addResource(ResourceType resourceType, Resource<T> resource) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());
        resourceCache.add(resource.getId(), resource);
    }

    public <T extends IdentifiableAttributes> void removeResource(ResourceType resourceType, String resourceId) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());
        resourceCache.removeResource(resourceId);
    }

    public <T extends IdentifiableAttributes> int getResourceCount(ResourceType resourceType) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());
        return resourceCache.getResourceCount();
    }

    public <T extends IdentifiableAttributes> void fillResources(ResourceType resourceType, List<Resource<T>> resources) {
        ResourceCache<T> resourceCache = resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());
        resourceCache.fill(resources);
    }

    public void invalidate() {
        resourcesCaches.clear();
    }

}
