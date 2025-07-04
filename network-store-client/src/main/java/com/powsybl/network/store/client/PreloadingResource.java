/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.ResourceType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
public interface PreloadingResource {
    ResourceType getType();

    CompletableFuture<Void> loadResource(PreloadingNetworkStoreClient client, UUID networkUuid, int variantNum, Set<ResourceType> loadedResourceTypes);
}
