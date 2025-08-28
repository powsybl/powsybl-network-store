/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
@Getter
@SuperBuilder
public class LinePreloadingResource extends BasePreloadingResource implements PreloadingResource {

    @Builder.Default
    boolean loadSelectedOperationalLimits = false;

    @Builder.Default
    boolean loadOperationalLimits = false;

    @Override
    public CompletableFuture<Void> loadResource(PreloadingNetworkStoreClient client, UUID networkUuid, int variantNum) {
        return client.loadToCacheAsync(networkUuid, variantNum, getType())
            .thenRun(() -> {
                    Set<CompletableFuture<Void>> futures = getExtensionsFutures(client, networkUuid, variantNum);
                    if (loadSelectedOperationalLimits) {
                        futures.add(client.loadAllSelectedOperationalLimitsGroupAttributesByResourceTypeAsync(networkUuid, variantNum, getType()));
                    } else if (loadOperationalLimits) {
                        futures.add(client.loadAllOperationalLimitsGroupAttributesByResourceTypeAsync(networkUuid, variantNum, getType()));
                    }
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                }
            );
    }
}
