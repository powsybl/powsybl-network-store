package com.powsybl.network.store.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
