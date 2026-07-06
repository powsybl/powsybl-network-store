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
 * Buffer for explicit operational limits group delete commands.
 *
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class PendingOperationalLimitsGroupDeletes {

    private final RestNetworkStoreClient delegate;

    private final Map<ResourceType, Map<String, Map<Integer, Set<String>>>> groupsToDelete = new EnumMap<>(ResourceType.class);

    public PendingOperationalLimitsGroupDeletes(RestNetworkStoreClient delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public void delete(ResourceType resourceType, Map<String, Map<Integer, Set<String>>> operationalLimitsGroupsToDelete) {
        Map<String, Map<Integer, Set<String>>> deletesByResource = groupsToDelete.computeIfAbsent(resourceType, ignored -> new HashMap<>());
        merge(deletesByResource, operationalLimitsGroupsToDelete);
    }

    public void cancelForResourceCreate(ResourceType resourceType, String branchId, int side, Set<String> groupIds) {
        cancel(resourceType, branchId, side, groupIds);
    }

    public void cancelForResourceUpdate(ResourceType resourceType, String branchId, int side, Set<String> groupIds, AttributeFilter attributeFilter) {
        if (writesOperationalLimits(attributeFilter)) {
            cancel(resourceType, branchId, side, groupIds);
        }
    }

    public void cancelForRemovedResources(ResourceType resourceType, Collection<String> branchIds) {
        Map<String, Map<Integer, Set<String>>> deletesByResource = groupsToDelete.get(resourceType);
        if (deletesByResource == null) {
            return;
        }
        branchIds.forEach(deletesByResource::remove);
        if (deletesByResource.isEmpty()) {
            groupsToDelete.remove(resourceType);
        }
    }

    public PendingOperationalLimitsGroupDeletes copy() {
        PendingOperationalLimitsGroupDeletes copy = new PendingOperationalLimitsGroupDeletes(delegate);
        groupsToDelete.forEach((resourceType, deletesByResource) -> {
            Map<String, Map<Integer, Set<String>>> copiedDeletesByResource = new HashMap<>();
            deletesByResource.forEach((branchId, deletesBySide) -> {
                Map<Integer, Set<String>> copiedDeletesBySide = new HashMap<>();
                deletesBySide.forEach((side, groupIds) -> copiedDeletesBySide.put(side, new HashSet<>(groupIds)));
                copiedDeletesByResource.put(branchId, copiedDeletesBySide);
            });
            copy.groupsToDelete.put(resourceType, copiedDeletesByResource);
        });
        return copy;
    }

    public void flush(UUID networkUuid, int variantNum) {
        groupsToDelete.forEach((resourceType, deletesByResource) ->
                delegate.removeOperationalLimitsGroupAttributes(networkUuid, variantNum, resourceType, deletesByResource));
        groupsToDelete.clear();
    }

    private void cancel(ResourceType resourceType, String branchId, int side, Set<String> groupIds) {
        Map<String, Map<Integer, Set<String>>> deletesByResource = groupsToDelete.get(resourceType);
        if (deletesByResource == null) {
            return;
        }
        Map<Integer, Set<String>> deletesBySide = deletesByResource.get(branchId);
        if (deletesBySide != null) {
            Set<String> deletedGroupIds = deletesBySide.get(side);
            if (deletedGroupIds != null) {
                deletedGroupIds.removeAll(groupIds);
                if (deletedGroupIds.isEmpty()) {
                    deletesBySide.remove(side);
                }
            }
            if (deletesBySide.isEmpty()) {
                deletesByResource.remove(branchId);
            }
        }
        if (deletesByResource.isEmpty()) {
            groupsToDelete.remove(resourceType);
        }
    }

    private static void merge(Map<String, Map<Integer, Set<String>>> target, Map<String, Map<Integer, Set<String>>> source) {
        source.forEach((branchId, sourceDeletesBySide) ->
                sourceDeletesBySide.forEach((side, groupIds) ->
                        target.computeIfAbsent(branchId, ignored -> new HashMap<>())
                                .computeIfAbsent(side, ignored -> new HashSet<>())
                                .addAll(groupIds)));
    }

    private static boolean writesOperationalLimits(AttributeFilter attributeFilter) {
        return attributeFilter == AttributeFilter.LIMITS || attributeFilter == AttributeFilter.FULL;
    }
}
