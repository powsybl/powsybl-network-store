/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.ResourceType;

import java.util.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class ExtensionsCollectionBuffer<T extends NetworkStoreClient> {

    private final Map<ResourceType, Map<String, Set<String>>> removedExtensionIds = new EnumMap<>(ResourceType.class);
    private final T delegate;

    public ExtensionsCollectionBuffer(T delegate) {
        this.delegate = delegate;
    }

    public ExtensionsCollectionBuffer<T> cloneBuffer() {
        var clonedBuffer = new ExtensionsCollectionBuffer<>(delegate);
        removedExtensionIds.forEach((resourceType, extensionsIds) ->
                extensionsIds.forEach((resourceId, limitIdSet) ->
                        clonedBuffer.removedExtensionIds.computeIfAbsent(resourceType, s -> new HashMap<>())
                                .computeIfAbsent(resourceId, s -> new HashSet<>()).addAll(limitIdSet)));
        return clonedBuffer;
    }

    void remove(Map<String, Set<String>> extensionsIds, ResourceType resourceType) {
        removedExtensionIds.computeIfAbsent(resourceType, s -> new HashMap<>());
        mergeExtensions(removedExtensionIds.get(resourceType), extensionsIds);
    }

    void restoreRemoveByResourcesIds(List<String> resourceIds, ResourceType resourceType) {
        Map<String, Set<String>> removeExternalAttributesIdsByResource = removedExtensionIds.get(resourceType);
        if (removeExternalAttributesIdsByResource != null) {
            removeExternalAttributesIdsByResource.entrySet().removeIf(entry -> resourceIds.contains(entry.getKey()));
            if (removeExternalAttributesIdsByResource.isEmpty()) {
                removedExtensionIds.remove(resourceType);
            }
        }
    }

    void flush(UUID networkUuid, int variantNum) {
        if (!removedExtensionIds.isEmpty()) {
            removedExtensionIds.forEach((resourceType, resourceIds) ->
                    delegate.removeExtensionsAttributes(networkUuid, variantNum, resourceType, removedExtensionIds.get(resourceType)));
        }
        removedExtensionIds.clear();
    }

    private static void mergeExtensions(Map<String, Set<String>> globalMap, Map<String, Set<String>> mapToAdd) {
        mapToAdd.forEach((identifiableId, extensionsByIdentifiable) ->
                globalMap.computeIfAbsent(identifiableId, s -> new HashSet<>())
                        .addAll(extensionsByIdentifiable));
    }

}
