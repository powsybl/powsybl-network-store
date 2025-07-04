/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.ResourceType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
@Getter
@SuperBuilder
public class BasePreloadingResource implements PreloadingResource {

    protected ResourceType type;

    @Builder.Default
    protected List<String> extensions = Collections.emptyList();

    @Override
    public CompletableFuture<Void> loadResource(PreloadingNetworkStoreClient client, UUID networkUuid, int variantNum, Set<ResourceType> loadedResourceTypes) {
        return client.loadToCacheAsync(networkUuid, variantNum, getType())
            .thenRun(() -> {
                    loadedResourceTypes.add(getType());
                    CompletableFuture.allOf(getExtensionsFutures(client, networkUuid, variantNum).toArray(new CompletableFuture[0])).join();
                }
            );
    }

    protected Set<CompletableFuture<Void>> getExtensionsFutures(PreloadingNetworkStoreClient client, UUID networkUuid, int variantNum) {
        return getExtensions()
            .stream()
            .map(extension -> client.loadAllExtensionsAttributesByResourceTypeAndExtensionNameAsync(networkUuid, variantNum, getType(), extension))
            .collect(Collectors.toSet());
    }
}
