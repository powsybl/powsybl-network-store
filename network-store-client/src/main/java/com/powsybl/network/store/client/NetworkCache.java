/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class NetworkCache {

    private final UUID networkId;

    private class ResourceCache<T extends IdentifiableAttributes> {

        private final Map<String, Resource<T>> resources = new HashMap<>();

        boolean isComplete = false;

        public ResourceCache() {
        }

        public Resource<T> get(String id) {
            return resources.get(id);
        }

        public void put(String id, Resource<T> resource) {
            resources.put(id, resource);
        }
    }

    private final Map<ResourceType, ResourceCache> resourcesCaches = new HashMap<>();

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

}
