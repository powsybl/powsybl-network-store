/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class NetworkCacheHandler {

    private final Map<UUID, NetworkCache> networkCaches = new HashMap<>();

    public Map<UUID, NetworkCache> getNetworkCaches() {
        return networkCaches;
    }

    public NetworkCache getNetworkCache(UUID networkUuid) {
        NetworkCache networkCache = networkCaches.computeIfAbsent(networkUuid, NetworkCache::new);
        return networkCache;
    }

    public void invalidateNetworkCache(UUID networkUuid) {
        networkCaches.remove(networkUuid);
    }
}
