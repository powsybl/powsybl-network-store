/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class NetworkCollectionIndex<C> {

    private final Map<UUID, C> collections = new HashMap<>();

    private final Function<UUID, C> factory;

    public NetworkCollectionIndex(Function<UUID, C> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public C getCollection(UUID networkUuid) {
        Objects.requireNonNull(networkUuid);
        return collections.computeIfAbsent(networkUuid, factory);
    }

    public void removeCollection(UUID networkUuid) {
        Objects.requireNonNull(networkUuid);
        collections.remove(networkUuid);
    }

    public void applyToCollection(BiConsumer<UUID, C> fct) {
        for (Map.Entry<UUID, C> e : collections.entrySet()) {
            UUID networkUuid = e.getKey();
            C collection = e.getValue();
            fct.accept(networkUuid, collection);
        }
    }
}
