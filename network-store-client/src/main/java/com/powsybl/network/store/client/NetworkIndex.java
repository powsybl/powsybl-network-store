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
public class NetworkIndex<C> {

    private final Map<UUID, C> networks = new HashMap<>();

    private final Function<UUID, C> factory;

    public NetworkIndex(Function<UUID, C> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public Collection<C> getNetworks() {
        return networks.values();
    }

    public C getNetwork(UUID networkUuid) {
        Objects.requireNonNull(networkUuid);
        return networks.computeIfAbsent(networkUuid, factory);
    }

    public void removeNetwork(UUID networkUuid) {
        Objects.requireNonNull(networkUuid);
        networks.remove(networkUuid);
    }

    public <U> void apply(BiConsumer<UUID, C> fct) {
        for (Map.Entry<UUID, C> e : networks.entrySet()) {
            UUID networkUuid = e.getKey();
            C collection = e.getValue();
            fct.accept(networkUuid, collection);
        }
    }
}
