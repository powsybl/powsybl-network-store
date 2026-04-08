/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.network.store.model.AttributeFilter;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionBuffer<T extends IdentifiableAttributes> {

    private final BiConsumer<UUID, List<Resource<T>>> createFct;

    private final TriConsumer<UUID, List<Resource<T>>, AttributeFilter> updateFct;

    private final TriConsumer<UUID, Integer, List<String>> removeFct;

    private final Map<String, Resource<T>> createResources = new LinkedHashMap<>();

    private static final class ResourceAndFilter<T extends IdentifiableAttributes> {

        private final Resource<T> resource;

        private AttributeFilter attributeFilter;

        private ResourceAndFilter(Resource<T> resource, AttributeFilter attributeFilter) {
            this.resource = Objects.requireNonNull(resource);
            this.attributeFilter = attributeFilter;
        }

        private Resource<T> getResource() {
            return resource;
        }

        private AttributeFilter getAttributeFilter() {
            return attributeFilter;
        }

        private void setAttributeFilter(AttributeFilter attributeFilter) {
            this.attributeFilter = attributeFilter;
        }
    }

    private final Map<String, ResourceAndFilter<T>> updateResources = new LinkedHashMap<>();

    private final Set<String> removeResourcesIds = new HashSet<>();

    public CollectionBuffer(BiConsumer<UUID, List<Resource<T>>> createFct,
                            TriConsumer<UUID, List<Resource<T>>, AttributeFilter> updateFct,
                            TriConsumer<UUID, Integer, List<String>> removeFct) {
        this.createFct = Objects.requireNonNull(createFct);
        this.updateFct = updateFct;
        this.removeFct = removeFct;
    }

    void create(Resource<T> resource) {
        createResources.put(resource.getId(), resource);
    }

    void update(Resource<T> resource) {
        update(resource, AttributeFilter.PRIMARY_AS_NULL);
    }

    void update(Resource<T> resource, AttributeFilter attributeFilter) {
        // do not update the resource if a creation resource is already in the buffer
        // (so we don't need to generate an update as the resource has not yet been created
        // on server side and is still on client buffer)
        if (!createResources.containsKey(resource.getId())) {
            ResourceAndFilter<T> resourceAndFilter = updateResources.get(resource.getId());
            if (resourceAndFilter == null) {
                updateResources.put(resource.getId(), new ResourceAndFilter<>(resource, attributeFilter));
            } else {
                // Keep the covering (broader) filter.
                resourceAndFilter.setAttributeFilter(AttributeFilter.covering(resourceAndFilter.getAttributeFilter(), attributeFilter));
            }
        }
    }

    void remove(String resourceId) {
        remove(Collections.singletonList(resourceId));
    }

    void remove(List<String> resourceIds) {
        for (String resourceId : resourceIds) {
            // remove directly from the creation buffer if possible, otherwise remove from the server"
            if (createResources.remove(resourceId) == null) {
                removeResourcesIds.add(resourceId);

                // no need to update the resource on server side if we remove it just after
                updateResources.remove(resourceId);
            }
        }
    }

    void flush(UUID networkUuid, int variantNum) {
        if (removeFct != null && !removeResourcesIds.isEmpty()) {
            removeFct.accept(networkUuid, variantNum, new ArrayList<>(removeResourcesIds));
        }
        if (!createResources.isEmpty()) {
            createFct.accept(networkUuid, new ArrayList<>(createResources.values()));
        }
        if (updateFct != null && !updateResources.isEmpty()) {
            List<Resource<T>> primaryResources = new ArrayList<>();
            Map<AttributeFilter, List<Resource<T>>> filteredResources = new EnumMap<>(AttributeFilter.class);
            for (ResourceAndFilter<T> resource : updateResources.values()) {
                if (resource.getAttributeFilter() == AttributeFilter.PRIMARY_AS_NULL) {
                    primaryResources.add(resource.getResource());
                } else {
                    filteredResources.computeIfAbsent(resource.getAttributeFilter(), k -> new ArrayList<>())
                            .add(resource.getResource());
                }
            }
            // NOTE: here we use different batches for the different filters.
            // This has the effect of controlling both the serialization
            // (include/exclude fields with json views), and also to split
            // accross multiple server requests. Controlling the serialization
            // always has an effect, but doing separate requests has an effect
            // just for AttributeFilter.SV, not for AttributeFilter.LIMITS.
            // For the cases like LIMITS where separate requests have no effect, we could
            // rewrite the code to keep the separate serialization views, but still
            // send the resources in a single request if needed.
            // NOTE: the difference between AttributeFilter.SV and AttributeFilter.LIMITS
            // comes from whether or not the excluded fields are a allowed to
            // be removed/cleared by updates or need explicit remove calls. Concrete examples:
            // - for a line resitance R (excluded from SV), we chose that the only way to unset it is to update()
            //   => the server needs to know whether the absence of this field means either 'don't write' or 'unset'.
            // - for a line operational limit group (excluded from PRIMARY), we chose that the only way to delete it
            //   is to call remove(olg_id) and that update() never removes absent data.
            //   => the server doesn't need to know why data is absent, it's always 'don't write'
            updateFct.accept(networkUuid, primaryResources, AttributeFilter.PRIMARY_AS_NULL);
            for (var e : filteredResources.entrySet()) {
                updateFct.accept(networkUuid, new ArrayList<>(e.getValue()), e.getKey());
            }
        }
        createResources.clear();
        updateResources.clear();
        removeResourcesIds.clear();
    }

    /**
     * Buffer deep copy.
     *
     * @param objectMapper a object mapper to help cloning resources
     * @param newVariantNum new variant num for all resources of the cloned buffer
     * @param resourcePostProcessor a resource post processor
     * @return the buffer clone
     */
    public CollectionBuffer<T> clone(ObjectMapper objectMapper, int newVariantNum, Consumer<Resource<T>> resourcePostProcessor) {
        List<Resource<T>> clonedCreateResources = Resource.cloneResourcesToVariant(createResources.values(), newVariantNum, objectMapper, resourcePostProcessor);
        List<Resource<T>> clonedUpdateResources = Resource.cloneResourcesToVariant(updateResources.values().stream().map(ResourceAndFilter::getResource).collect(Collectors.toList()), newVariantNum, objectMapper, resourcePostProcessor);

        var clonedBuffer = new CollectionBuffer<>(createFct, updateFct, removeFct);
        for (Resource<T> clonedResource : clonedCreateResources) {
            clonedBuffer.createResources.put(clonedResource.getId(), clonedResource);
        }
        for (Resource<T> clonedResource : clonedUpdateResources) {
            // TODO Why are we not preserving the ResourceAndFilter here ? It forces us to send everything.
            clonedBuffer.updateResources.put(clonedResource.getId(), new ResourceAndFilter<>(clonedResource, AttributeFilter.FULL));
        }
        clonedBuffer.removeResourcesIds.addAll(removeResourcesIds);

        return clonedBuffer;
    }

    public Set<String> getCreateResourcesIds() {
        return createResources.keySet();
    }

    public Set<String> getRemoveResourcesIds() {
        return removeResourcesIds;
    }
}
