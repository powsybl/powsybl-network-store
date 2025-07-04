package com.powsybl.network.store.client;

import com.powsybl.network.store.model.ResourceType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PreloadingResource {
    ResourceType getType();

    CompletableFuture<Void> loadResource(PreloadingNetworkStoreClient client, UUID networkUuid, int variantNum, Set<ResourceType> loadedResourceTypes);
}
