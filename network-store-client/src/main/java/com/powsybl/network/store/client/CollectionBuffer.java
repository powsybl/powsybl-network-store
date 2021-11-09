/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionBuffer<T extends IdentifiableAttributes> {

    private final BiConsumer<UUID, List<Resource<T>>> createFct;

    private final BiConsumer<UUID, List<Resource<T>>> updateFct;

    private final TriConsumer<UUID, Integer, List<String>> removeFct;

    private final Map<String, Resource<T>> createResources = new LinkedHashMap<>();

    private final Map<String, Resource<T>> updateResources = new LinkedHashMap<>();

    private final Set<String> removeResources = new HashSet<>();

    public CollectionBuffer(BiConsumer<UUID, List<Resource<T>>> createFct,
                            BiConsumer<UUID, List<Resource<T>>> updateFct,
                            TriConsumer<UUID, Integer, List<String>> removeFct) {
        this.createFct = Objects.requireNonNull(createFct);
        this.updateFct = updateFct;
        this.removeFct = removeFct;
    }

    void create(Resource<T> resource) {
        createResources.put(resource.getId(), resource);
    }

    void update(Resource<T> resource) {
        // do not update the resource if a creation resource is already in the buffer
        // (so we don't need to generate an update as the resource has not yet been created
        // on server side and is still on client buffer)
        if (!createResources.containsKey(resource.getId())) {
            updateResources.put(resource.getId(), resource);
        }
    }

    void remove(String resourceId) {
        remove(Collections.singletonList(resourceId));
    }

    void remove(List<String> resourceIds) {
        for (String resourceId : resourceIds) {
            // remove directly from the creation buffer if possible, otherwise remove from the server"
            if (createResources.remove(resourceId) == null) {
                removeResources.add(resourceId);

                // no need to update the resource on server side if we remove it just after
                updateResources.remove(resourceId);
            }
        }
    }

    void flush(UUID networkUuid, int variantNum) {
        if (removeFct != null && !removeResources.isEmpty()) {
            removeFct.accept(networkUuid, variantNum, new ArrayList<>(removeResources));
        }
        if (!createResources.isEmpty()) {
            createFct.accept(networkUuid, new ArrayList<>(createResources.values()));
        }
        if (updateFct != null && !updateResources.isEmpty()) {
            updateFct.accept(networkUuid, new ArrayList<>(updateResources.values()));
        }
        createResources.clear();
        updateResources.clear();
        removeResources.clear();
    }
}
