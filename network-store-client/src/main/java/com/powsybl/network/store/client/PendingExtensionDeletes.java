/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.AttributeFilter;
import com.powsybl.network.store.model.ResourceType;

import java.util.*;

/**
 * Buffer for explicit extension attribute delete commands.
 *
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class PendingExtensionDeletes {

    private final RestNetworkStoreClient delegate;

    private final Map<ResourceType, Map<String, Set<String>>> extensionsToDelete = new EnumMap<>(ResourceType.class);

    public PendingExtensionDeletes(RestNetworkStoreClient delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public void delete(ResourceType resourceType, Map<String, Set<String>> extensionsByIdentifiableId) {
        Map<String, Set<String>> deletesByResource = extensionsToDelete.computeIfAbsent(resourceType, ignored -> new HashMap<>());
        extensionsByIdentifiableId.forEach((identifiableId, extensionNames) ->
                deletesByResource.computeIfAbsent(identifiableId, ignored -> new HashSet<>()).addAll(extensionNames));
    }

    public void cancelForResourceUpdate(ResourceType resourceType, String identifiableId, Set<String> extensionNames, AttributeFilter attributeFilter) {
        if (writesExtensions(attributeFilter)) {
            cancel(resourceType, identifiableId, extensionNames);
        }
    }

    public void cancelForResourceCreate(ResourceType resourceType, String identifiableId, Set<String> extensionNames) {
        cancel(resourceType, identifiableId, extensionNames);
    }

    public void cancelForRemovedResources(ResourceType resourceType, Collection<String> identifiableIds) {
        Map<String, Set<String>> deletesByResource = extensionsToDelete.get(resourceType);
        if (deletesByResource == null) {
            return;
        }
        identifiableIds.forEach(deletesByResource::remove);
        if (deletesByResource.isEmpty()) {
            extensionsToDelete.remove(resourceType);
        }
    }

    public PendingExtensionDeletes copy() {
        PendingExtensionDeletes copy = new PendingExtensionDeletes(delegate);
        extensionsToDelete.forEach((resourceType, deletesByResource) -> {
            Map<String, Set<String>> copiedDeletesByResource = new HashMap<>();
            deletesByResource.forEach((identifiableId, extensionNames) ->
                    copiedDeletesByResource.put(identifiableId, new HashSet<>(extensionNames)));
            copy.extensionsToDelete.put(resourceType, copiedDeletesByResource);
        });
        return copy;
    }

    public void flush(UUID networkUuid, int variantNum) {
        extensionsToDelete.forEach((resourceType, deletesByResource) ->
                delegate.removeExtensionAttributes(networkUuid, variantNum, resourceType, deletesByResource));
        extensionsToDelete.clear();
    }

    private void cancel(ResourceType resourceType, String identifiableId, Set<String> extensionNames) {
        Map<String, Set<String>> deletesByResource = extensionsToDelete.get(resourceType);
        if (deletesByResource == null) {
            return;
        }
        Set<String> deletedExtensionNames = deletesByResource.get(identifiableId);
        if (deletedExtensionNames != null) {
            deletedExtensionNames.removeAll(extensionNames);
            if (deletedExtensionNames.isEmpty()) {
                deletesByResource.remove(identifiableId);
            }
        }
        if (deletesByResource.isEmpty()) {
            extensionsToDelete.remove(resourceType);
        }
    }

    private static boolean writesExtensions(AttributeFilter attributeFilter) {
        return attributeFilter != AttributeFilter.SV;
    }
}
