/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class NetworkCollectionIndex<C> {

    private final Map<Pair<UUID, Integer>, C> collections = new LinkedHashMap<>();

    private final BiFunction<UUID, Integer, C> factory;

    public NetworkCollectionIndex(BiFunction<UUID, Integer, C> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public List<C> getCollections() {
        return new ArrayList<>(collections.values());
    }

    public List<C> getCollections(UUID networkUuid) {
        return collections.entrySet().stream()
                .filter(e -> e.getKey().getLeft().equals(networkUuid))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public List<C> getCollections(int variantNum) {
        return collections.entrySet().stream()
                .filter(e -> e.getKey().getRight() == variantNum)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public C getCollection(UUID networkUuid, int variantNum) {
        Objects.requireNonNull(networkUuid);
        return collections.computeIfAbsent(Pair.of(networkUuid, variantNum), p -> factory.apply(p.getLeft(), variantNum));
    }

    public void removeCollection(UUID networkUuid, int variantNum) {
        Objects.requireNonNull(networkUuid);
        collections.remove(Pair.of(networkUuid, variantNum));
    }

    public void removeCollection(UUID networkUuid) {
        collections.keySet().removeIf(p -> p.getLeft().equals(networkUuid));
    }

    public void applyToCollection(BiConsumer<Pair<UUID, Integer>, C> fct) {
        for (Map.Entry<Pair<UUID, Integer>, C> e : collections.entrySet()) {
            Pair<UUID, Integer> p = e.getKey();
            C collection = e.getValue();
            fct.accept(p, collection);
        }
    }
}
