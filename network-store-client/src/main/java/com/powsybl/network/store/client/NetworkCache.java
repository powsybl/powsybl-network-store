/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

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

        private boolean fullyLoaded = false;

        private final Map<String, Boolean> containerFullyLoaded = new HashMap<>();

        public boolean isFullyLoaded(String containerId) {
            return containerFullyLoaded.getOrDefault(containerId, false);
        }

        public boolean isFullyLoaded() {
            return fullyLoaded;
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

        private Set<Resource<T>> getResourcesByContainerId(String containerId) {
            return resourcesByContainerId.computeIfAbsent(containerId, k -> new HashSet<>());
        }

        public void add(String id, Resource<T> resource) {
            resources.put(id, resource);
            if (resource != null) {
                IdentifiableAttributes attributes = resource.getAttributes();
                if (attributes instanceof RelatedVoltageLevelsAttributes) {
                    (((RelatedVoltageLevelsAttributes) attributes).getVoltageLevels()).forEach(voltageLevelId -> getResourcesByContainerId(voltageLevelId).add(resource));
                } else if (attributes instanceof VoltageLevelAttributes) {
                    getResourcesByContainerId(((VoltageLevelAttributes) attributes).getSubstationId()).add(resource);
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
                        (((RelatedVoltageLevelsAttributes) attributes).getVoltageLevels()).forEach(voltageLevelId -> getResourcesByContainerId(voltageLevelId).remove(resource));
                    } else if (attributes instanceof VoltageLevelAttributes) {
                        getResourcesByContainerId(((VoltageLevelAttributes) attributes).getSubstationId()).remove(resource);
                    }
                }
            }
        }

        public void fillAll(List<Resource<T>> resourcesToAdd) {
            resourcesToAdd.forEach(resource -> resources.put(resource.getId(), resource));
            fullyLoaded = true;

            // Update of cache by container id
            Map<String, Set<Resource<T>>> resourcesByContainerIdToAdd = new HashMap<>();
            for (Resource<T> resource : resourcesToAdd) {
                IdentifiableAttributes attributes = resource.getAttributes();
                if (attributes instanceof RelatedVoltageLevelsAttributes) {
                    (((RelatedVoltageLevelsAttributes) attributes).getVoltageLevels())
                            .forEach(voltageLevelId -> resourcesByContainerIdToAdd.computeIfAbsent(voltageLevelId, k -> new HashSet<>()).add(resource));
                } else if (attributes instanceof VoltageLevelAttributes) {
                    resourcesByContainerIdToAdd.computeIfAbsent(((VoltageLevelAttributes) attributes).getSubstationId(), k -> new HashSet<>()).add(resource);
                }
            }
            for (Map.Entry<String, Set<Resource<T>>> resourcesToAddByVoltageLevel : resourcesByContainerIdToAdd.entrySet()) {
                getResourcesByContainerId(resourcesToAddByVoltageLevel.getKey())
                        .addAll(resourcesToAddByVoltageLevel.getValue());
                containerFullyLoaded.put(resourcesToAddByVoltageLevel.getKey(), true);
            }
        }

        public List<Resource<T>> getContainer(String containerId) {
            return new ArrayList<>(getResourcesByContainerId(containerId));
        }

        public int getResourceCount() {
            return resources.size();
        }

        public void fillContainer(String containerId, List<Resource<T>> resourcesToAdd) {
            getResourcesByContainerId(containerId).addAll(resourcesToAdd);
            containerFullyLoaded.put(containerId, true);

            // Update of full cache
            resourcesToAdd.forEach(resource -> resources.put(resource.getId(), resource));
        }
    }

    private final Map<ResourceType, ResourceCache> resourcesCaches = new EnumMap<>(ResourceType.class);

    public NetworkCache(UUID networkId) {
        this.networkId = networkId;
    }

    private static final Set<ResourceType> NETWORK_CONTAINERS = EnumSet.of(
            ResourceType.SUBSTATION,
            ResourceType.VOLTAGE_LEVEL,
            ResourceType.LOAD,
            ResourceType.GENERATOR,
            ResourceType.SHUNT_COMPENSATOR,
            ResourceType.VSC_CONVERTER_STATION,
            ResourceType.LCC_CONVERTER_STATION,
            ResourceType.STATIC_VAR_COMPENSATOR,
            ResourceType.BUSBAR_SECTION,
            ResourceType.SWITCH,
            ResourceType.TWO_WINDINGS_TRANSFORMER,
            ResourceType.THREE_WINDINGS_TRANSFORMER,
            ResourceType.LINE,
            ResourceType.HVDC_LINE,
            ResourceType.DANGLING_LINE,
            ResourceType.CONFIGURED_BUS);

    private static final Set<ResourceType> VOLTAGE_LEVEL_CONTAINERS = EnumSet.of(
            ResourceType.LOAD,
            ResourceType.GENERATOR,
            ResourceType.SHUNT_COMPENSATOR,
            ResourceType.VSC_CONVERTER_STATION,
            ResourceType.LCC_CONVERTER_STATION,
            ResourceType.STATIC_VAR_COMPENSATOR,
            ResourceType.BUSBAR_SECTION,
            ResourceType.SWITCH,
            ResourceType.TWO_WINDINGS_TRANSFORMER,
            ResourceType.THREE_WINDINGS_TRANSFORMER,
            ResourceType.LINE,
            ResourceType.HVDC_LINE,
            ResourceType.DANGLING_LINE,
            ResourceType.CONFIGURED_BUS);

    private <T extends IdentifiableAttributes> void initContainers(ResourceType resourceType, String resourceId) {
        // init resource cache to empty (and up to date) for which this resource is a container
        if (resourceType == ResourceType.NETWORK) {
            for (ResourceType otherResourceType : NETWORK_CONTAINERS) {
                ResourceCache<T> otherResourceCache = getResourceCache(otherResourceType);
                otherResourceCache.fillAll(Collections.emptyList());
            }
        } else if (resourceType == ResourceType.SUBSTATION) {
            ResourceCache<T> otherResourceCache = getResourceCache(ResourceType.VOLTAGE_LEVEL);
            otherResourceCache.fillContainer(resourceId, Collections.emptyList());
        } else {
            for (ResourceType otherResourceType : VOLTAGE_LEVEL_CONTAINERS) {
                ResourceCache<T> otherResourceCache = getResourceCache(otherResourceType);
                otherResourceCache.fillContainer(resourceId, Collections.emptyList());
            }
        }
    }

    public void createNetworkResource(Resource<NetworkAttributes> networkResource) {
        this.networkResource = networkResource;
        initContainers(ResourceType.NETWORK, networkResource.getId());
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

    private <T extends IdentifiableAttributes> ResourceCache<T> getResourceCache(ResourceType resourceType) {
        return resourcesCaches.computeIfAbsent(resourceType, k -> new ResourceCache<T>());
    }

    public <T extends IdentifiableAttributes> Optional<Resource<T>> getResource(ResourceType resourceType, String resourceId, Function<String, Optional<Resource<T>>> loaderFunction) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);

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
        ResourceCache<T> resourceCache = getResourceCache(resourceType);

        Resource<T> resource = null;
        if (resourceCache.contains(resourceId)) {
            resource = resourceCache.get(resourceId);
        }

        return Optional.ofNullable(resource);
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getAllResources(ResourceType resourceType, Supplier<List<Resource<T>>> loaderFunction) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);

        List<Resource<T>> resources;
        if (resourceCache.isFullyLoaded()) {
            resources = resourceCache.getAll();
        } else {
            List<Resource<T>> resourcesToAdd = loaderFunction.get();
            resourceCache.fillAll(resourcesToAdd);
            resources = resourceCache.getAll();
        }

        return resources;
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getAllResources(ResourceType resourceType) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);

        return resourceCache.getAll();
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getResourcesByContainerId(ResourceType resourceType, String containerId,  Function<String, List<Resource<T>>> loaderFunction) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);

        List<Resource<T>> resources;
        if (resourceCache.isFullyLoaded(containerId)) {
            resources = resourceCache.getContainer(containerId);
        } else {
            List<Resource<T>> resourcesToAdd = loaderFunction.apply(containerId);
            resourceCache.fillContainer(containerId, resourcesToAdd);
            resources = resourceCache.getContainer(containerId);
        }

        return resources;
    }

    public <T extends IdentifiableAttributes> List<Resource<T>> getResourcesByContainerId(ResourceType resourceType, String containerId) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);

        return resourceCache.getContainer(containerId);
    }

    public <T extends IdentifiableAttributes> void createResources(ResourceType resourceType, List<Resource<T>> resources) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);
        for (Resource<T> resource : resources) {
            resourceCache.add(resource.getId(), resource);
            initContainers(resourceType, resource.getId());
        }
    }

    public <T extends IdentifiableAttributes> void addResources(ResourceType resourceType, List<Resource<T>> resources) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);

        for (Resource<T> resource : resources) {
            resourceCache.add(resource.getId(), resource);
        }
    }

    public <T extends IdentifiableAttributes> void addResource(ResourceType resourceType, Resource<T> resource) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);
        resourceCache.add(resource.getId(), resource);
    }

    public <T extends IdentifiableAttributes> void removeResource(ResourceType resourceType, String resourceId) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);
        resourceCache.removeResource(resourceId);
    }

    public <T extends IdentifiableAttributes> int getResourceCount(ResourceType resourceType) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);
        return resourceCache.getResourceCount();
    }

    public <T extends IdentifiableAttributes> void fillAllResources(ResourceType resourceType, List<Resource<T>> resources) {
        ResourceCache<T> resourceCache = getResourceCache(resourceType);
        resourceCache.fillAll(resources);
    }
}
