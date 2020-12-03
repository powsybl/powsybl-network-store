/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionBuffer<T extends IdentifiableAttributes> {

    private final TriConsumer<UUID, Integer, List<Resource<T>>> createFct;

    private final TriConsumer<UUID, Integer, List<Resource<T>>> updateFct;

    private final TriConsumer<UUID, Integer, List<String>> removeFct;

    private final Map<Pair<String, Integer>, Resource<T>> createResources = new HashMap<>();

    private final Map<Pair<String, Integer>, Resource<T>> updateResources = new HashMap<>();

    private final Set<Pair<String, Integer>> removeResources = new HashSet<>();

    public CollectionBuffer(TriConsumer<UUID, Integer, List<Resource<T>>> createFct,
                            TriConsumer<UUID, Integer, List<Resource<T>>> updateFct,
                            TriConsumer<UUID, Integer, List<String>> removeFct) {
        this.createFct = Objects.requireNonNull(createFct);
        this.updateFct = updateFct;
        this.removeFct = removeFct;
    }

    void create(List<Resource<T>> resources, int variantNum) {
        for (Resource<T> resource : resources) {
            createResources.put(Pair.of(resource.getId(), variantNum), resource);
        }
    }

    void update(Resource<T> resource, int variantNum) {
        update(Collections.singletonList(resource), variantNum);
    }

    void update(List<Resource<T>> resources, int variantNum) {
        for (Resource<T> resource : resources) {
            // do not update the resource if a creation resource is already in the buffer
            // (so we don't need to generate an update as the resource has not yet been created
            // on server side and is still on client buffer)
            Pair<String, Integer> p = Pair.of(resource.getId(), variantNum);
            if (!createResources.containsKey(p)) {
                updateResources.put(p, resource);
            }
        }
    }

    void remove(String resourceId, int variantNum) {
        remove(Collections.singletonList(resourceId), variantNum);
    }

    void remove(List<String> resourceIds, int variantNum) {
        for (String resourceId : resourceIds) {
            // remove directly from the creation buffer if possible, otherwise remove from the server
            Pair<String, Integer> p = Pair.of(resourceId, variantNum);
            if (createResources.remove(p) == null) {
                removeResources.add(p);

                // no need to update the resource on server side if we remove it just after
                updateResources.remove(p);
            }
        }
    }

    private void flush(UUID networkUuid, Map<Pair<String, Integer>, Resource<T>> resourceMap, TriConsumer<UUID, Integer, List<Resource<T>>> fct) {
        Map<Integer, List<Map.Entry<Pair<String, Integer>, Resource<T>>>> byVariantNum = resourceMap.entrySet().stream().collect(Collectors.groupingBy(e -> e.getKey().getRight()));
        for (Map.Entry<Integer, List<Map.Entry<Pair<String, Integer>, Resource<T>>>> e : byVariantNum.entrySet()) {
            int versionNum = e.getKey();
            List<Resource<T>> resources = e.getValue().stream().map(Map.Entry::getValue).collect(Collectors.toList());
            fct.accept(networkUuid, versionNum, resources);
        }
    }

    void flush(UUID networkUuid) {
        if (removeFct != null && !removeResources.isEmpty()) {
            Map<Integer, List<Pair<String, Integer>>> byVariantNum = removeResources.stream().collect(Collectors.groupingBy(Pair::getRight));
            for (Map.Entry<Integer, List<Pair<String, Integer>>> e : byVariantNum.entrySet()) {
                int versionNum = e.getKey();
                List<String> resourceIds = e.getValue().stream().map(Pair::getLeft).collect(Collectors.toList());
                removeFct.accept(networkUuid, versionNum, resourceIds);
            }
        }
        if (!createResources.isEmpty()) {
            flush(networkUuid, createResources, createFct);
        }
        if (updateFct != null && !updateResources.isEmpty()) {
            flush(networkUuid, updateResources, updateFct);
        }
        createResources.clear();
        updateResources.clear();
        removeResources.clear();
    }
}
