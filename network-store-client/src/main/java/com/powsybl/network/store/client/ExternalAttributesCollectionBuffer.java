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
import java.util.Map;
import java.util.UUID;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class ExternalAttributesCollectionBuffer<T> {
    private final QuadriConsumer<UUID, Integer, ResourceType, Map<String, T>> removeFct;

    private final Map<ResourceType, Map<String, T>> removeResourcesIds = new HashMap<>();

    public ExternalAttributesCollectionBuffer(QuadriConsumer<UUID, Integer, ResourceType, Map<String, T>> removeFct) {
        this.removeFct = removeFct;
    }

    void remove(Map<String, T> resourceIds, ResourceType resourceType) {
        removeResourcesIds.computeIfAbsent(resourceType, s -> new HashMap<>()).putAll(resourceIds);
    }

    void flush(UUID networkUuid, int variantNum) {
        if (removeFct != null && !removeResourcesIds.isEmpty()) {
            removeResourcesIds.forEach((resourceType, resourceIds) -> {
                removeFct.accept(networkUuid, variantNum, resourceType, removeResourcesIds.get(resourceType));
            });
        }
        removeResourcesIds.clear();
    }

    public ExternalAttributesCollectionBuffer<T> clone() {
        var clonedBuffer = new ExternalAttributesCollectionBuffer<>(removeFct);
        clonedBuffer.removeResourcesIds.putAll(removeResourcesIds);
        return clonedBuffer;
    }
}
