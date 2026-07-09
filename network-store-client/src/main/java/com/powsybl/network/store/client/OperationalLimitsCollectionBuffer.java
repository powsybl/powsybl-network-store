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
public class OperationalLimitsCollectionBuffer<T extends NetworkStoreClient> {

    private final Map<ResourceType, Map<String, Map<Integer, Set<String>>>> removedOperationalLimitsIds = new EnumMap<>(ResourceType.class);
    private final T delegate;

    public OperationalLimitsCollectionBuffer(T delegate) {
        this.delegate = delegate;
    }

    public OperationalLimitsCollectionBuffer<T> cloneBuffer() {
        var clonedBuffer = new OperationalLimitsCollectionBuffer<>(delegate);
        removedOperationalLimitsIds.forEach((resourceType, operationalLimitsGroupIds) ->
                operationalLimitsGroupIds.forEach((resourceId, operationalLimitsIds) ->
                        operationalLimitsIds.forEach((side, limitIdSet) ->
                                clonedBuffer.removedOperationalLimitsIds.computeIfAbsent(resourceType, s -> new HashMap<>())
                                        .computeIfAbsent(resourceId, s -> new HashMap<>())
                                        .computeIfAbsent(side, s -> new HashSet<>())
                                        .addAll(limitIdSet))));
        return clonedBuffer;
    }

    void remove(Map<String, Map<Integer, Set<String>>> operationalLimitsGroupIds, ResourceType resourceType) {
        removedOperationalLimitsIds.computeIfAbsent(resourceType, s -> new HashMap<>());
        mergeOperationalLimitsGroups(removedOperationalLimitsIds.get(resourceType), operationalLimitsGroupIds);
    }

    void restoreRemoveByResourcesIds(List<String> resourceIds, ResourceType resourceType) {
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
                    delegate.removeOperationalLimitsGroupAttributes(networkUuid, variantNum, resourceType, removedOperationalLimitsIds.get(resourceType)));
        }
        removedOperationalLimitsIds.clear();
    }

    private static void mergeOperationalLimitsGroups(Map<String, Map<Integer, Set<String>>> globalMap,
                                                     Map<String, Map<Integer, Set<String>>> mapToAdd) {
        mapToAdd.forEach((branchId, limitSetBySide) ->
                limitSetBySide.forEach((side, limitIdSet) ->
                        globalMap.computeIfAbsent(branchId, s -> new HashMap<>())
                                .computeIfAbsent(side, s -> new HashSet<>())
                                .addAll(limitIdSet)));
    }

}
