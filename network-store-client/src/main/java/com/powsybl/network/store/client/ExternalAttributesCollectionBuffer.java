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
import java.util.function.BiConsumer;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class ExternalAttributesCollectionBuffer<T> {
    private final QuadriConsumer<UUID, Integer, ResourceType, Map<String, T>> removeFct;

    private final Map<ResourceType, Map<String, T>> removeResourcesIds = new HashMap<>();
    private final BiConsumer<Map<String, T>, Map<String, T>> addFct;

    public ExternalAttributesCollectionBuffer(QuadriConsumer<UUID, Integer, ResourceType, Map<String, T>> removeFct,
                                              BiConsumer<Map<String, T>, Map<String, T>> addFct) {
        this.removeFct = removeFct;
        this.addFct = addFct;
    }

    public ExternalAttributesCollectionBuffer(ExternalAttributesCollectionBuffer<T> toCopy) {
        this(toCopy.removeFct, toCopy.addFct);
        this.removeResourcesIds.putAll(toCopy.removeResourcesIds);
    }

    void remove(Map<String, T> resourceIds, ResourceType resourceType) {
        removeResourcesIds.computeIfAbsent(resourceType, s -> new HashMap<>());
        addFct.accept(removeResourcesIds.get(resourceType), resourceIds);
    }

    void flush(UUID networkUuid, int variantNum) {
        if (removeFct != null && !removeResourcesIds.isEmpty()) {
            removeResourcesIds.forEach((resourceType, resourceIds) ->
                    removeFct.accept(networkUuid, variantNum, resourceType, removeResourcesIds.get(resourceType)));
        }
        removeResourcesIds.clear();
    }
}
