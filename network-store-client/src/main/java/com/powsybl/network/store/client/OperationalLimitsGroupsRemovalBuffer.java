/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.client.util.QuadriConsumer;
import com.powsybl.network.store.model.ResourceType;

import java.util.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class OperationalLimitsGroupsRemovalBuffer {

    private final Map<ResourceType, Map<String, Map<Integer, Set<String>>>> removedOperationalLimitsIds = new EnumMap<>(ResourceType.class);
    private final QuadriConsumer<UUID, Integer, ResourceType, Map<String, Map<Integer, Set<String>>>> removeFct;

    public OperationalLimitsGroupsRemovalBuffer(QuadriConsumer<UUID, Integer, ResourceType, Map<String, Map<Integer, Set<String>>>> removeFct) {
        this.removeFct = removeFct;
    }

    public OperationalLimitsGroupsRemovalBuffer cloneBuffer() {
        var clonedBuffer = new OperationalLimitsGroupsRemovalBuffer(removeFct);
        removedOperationalLimitsIds.forEach((resourceType, operationalLimitsGroupIdsMap) ->
                operationalLimitsGroupIdsMap.forEach((branchId, operationalLimitsGroupIdsBySide) ->
                        operationalLimitsGroupIdsBySide.forEach((side, limitsGroupIds) ->
                                clonedBuffer.removedOperationalLimitsIds.computeIfAbsent(resourceType, s -> new HashMap<>())
                                        .computeIfAbsent(branchId, s -> new HashMap<>())
                                        .computeIfAbsent(side, s -> new HashSet<>())
                                        .addAll(limitsGroupIds))));
        return clonedBuffer;
    }

    void remove(Map<String, Map<Integer, Set<String>>> operationalLimitsGroupIds, ResourceType resourceType) {
        removedOperationalLimitsIds.computeIfAbsent(resourceType, s -> new HashMap<>());
        mergeOperationalLimitsGroups(removedOperationalLimitsIds.get(resourceType), operationalLimitsGroupIds);
    }

    void clearPendingRemovalsForResources(List<String> resourceIds, ResourceType resourceType) {
        Map<String, Map<Integer, Set<String>>> removeExternalAttributesIdsByResource = removedOperationalLimitsIds.get(resourceType);
        if (removeExternalAttributesIdsByResource != null) {
            removeExternalAttributesIdsByResource.entrySet().removeIf(entry -> resourceIds.contains(entry.getKey()));
            if (removeExternalAttributesIdsByResource.isEmpty()) {
                removedOperationalLimitsIds.remove(resourceType);
            }
        }
    }

    void flush(UUID networkUuid, int variantNum) {
        if (!removedOperationalLimitsIds.isEmpty()) {
            removedOperationalLimitsIds.forEach((resourceType, resourceIds) ->
                    removeFct.accept(networkUuid, variantNum, resourceType, removedOperationalLimitsIds.get(resourceType)));
        }
        removedOperationalLimitsIds.clear();
    }

    private static void mergeOperationalLimitsGroups(Map<String, Map<Integer, Set<String>>> savedOperationalLimitsGroupsToRemove,
                                                     Map<String, Map<Integer, Set<String>>> newOperationalLimitsGroupsToRemove) {
        newOperationalLimitsGroupsToRemove.forEach((branchId, operationalLimitsGroupIdsBySide) ->
                operationalLimitsGroupIdsBySide.forEach((side, limitIdSet) ->
                        savedOperationalLimitsGroupsToRemove.computeIfAbsent(branchId, s -> new HashMap<>())
                                .computeIfAbsent(side, s -> new HashSet<>())
                                .addAll(limitIdSet)));
    }

}
