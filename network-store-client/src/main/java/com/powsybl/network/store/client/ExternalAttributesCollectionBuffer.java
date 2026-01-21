/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.client.util.QuadriConsumer;
import com.powsybl.network.store.model.ResourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class ExternalAttributesCollectionBuffer<T> {
    private final QuadriConsumer<UUID, Integer, ResourceType, Map<String, T>> removeFct;

    private final Map<ResourceType, Map<String, T>> removeExternalAttributesIds = new HashMap<>();
    private final BiConsumer<Map<String, T>, Map<String, T>> mergeFct;
    private final BiConsumer<Map<String, T>, Map<String, T>> updateRemoveAttributesFct;

    public ExternalAttributesCollectionBuffer(QuadriConsumer<UUID, Integer, ResourceType, Map<String, T>> removeFct,
                                              BiConsumer<Map<String, T>, Map<String, T>> addFct,
                                              BiConsumer<Map<String, T>, Map<String, T>> updateRemoveAttributesFct) {
        this.removeFct = removeFct;
        this.mergeFct = addFct;
        this.updateRemoveAttributesFct = updateRemoveAttributesFct;
    }

    public ExternalAttributesCollectionBuffer<T> clone() {
        var clonedBuffer = new ExternalAttributesCollectionBuffer<>(removeFct, mergeFct, updateRemoveAttributesFct);
        clonedBuffer.removeExternalAttributesIds.putAll(removeExternalAttributesIds);
        return clonedBuffer;
    }

    void remove(Map<String, T> resourceIds, ResourceType resourceType) {
        removeExternalAttributesIds.computeIfAbsent(resourceType, s -> new HashMap<>());
        mergeFct.accept(removeExternalAttributesIds.get(resourceType), resourceIds);
    }

    void restoreRemoveExternalAttributes(Map<String, T> resourceIds, ResourceType resourceType) {
        updateRemoveAttributesFct.accept(removeExternalAttributesIds.get(resourceType), resourceIds);
    }

    void restoreRemoveByResourcesIds(List<String> resourceIds, ResourceType resourceType) {
        Map<String, T> removeExternalAttributesIdsByResource = removeExternalAttributesIds.get(resourceType);
        if (removeExternalAttributesIdsByResource != null) {
            removeExternalAttributesIdsByResource.entrySet().removeIf(entry -> resourceIds.contains(entry.getKey()));
            if (removeExternalAttributesIdsByResource.isEmpty()) {
                removeExternalAttributesIds.remove(resourceType);
            }
        }
    }

    void flush(UUID networkUuid, int variantNum) {
        if (removeFct != null && !removeExternalAttributesIds.isEmpty()) {
            removeExternalAttributesIds.forEach((resourceType, resourceIds) ->
                    removeFct.accept(networkUuid, variantNum, resourceType, removeExternalAttributesIds.get(resourceType)));
        }
        removeExternalAttributesIds.clear();
    }
}
