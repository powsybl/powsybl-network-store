/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.OfflineNetworkStoreClient;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PreloadingAllCollectionsTest {

    @Test
    void test() {
        var client = new PreloadingNetworkStoreClient(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()), false, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        client.getSubstations(networkUuid, 0);
        assertTrue(client.isResourceTypeCached(networkUuid, 0, ResourceType.SUBSTATION));
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType != ResourceType.SUBSTATION) {
                assertFalse(client.isResourceTypeCached(networkUuid, 0, ResourceType.GENERATOR));
            }
        }
    }

    @Test
    void testWithAllCollections() {
        var client = new PreloadingNetworkStoreClient(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()), true, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        client.getSubstations(networkUuid, 0);
        for (ResourceType resourceType : ResourceType.values()) {
            if (PreloadingNetworkStoreClient.RESOURCE_TYPES_NEEDED_FOR_BUS_VIEW.contains(resourceType)) {
                assertTrue(client.isResourceTypeCached(networkUuid, 0, resourceType));
            } else {
                assertFalse(client.isResourceTypeCached(networkUuid, 0, resourceType));
            }
        }
    }

    private static PreloadingNetworkStoreClient createClientWithLoad(PreloadingMode preloadingMode) {
        return createClientWithLoad(false, preloadingMode);
    }

    private static PreloadingNetworkStoreClient createClientWithLoad(boolean allCollectionsNeededForBusView, PreloadingMode preloadingMode) {
        var storeClient = new OfflineNetworkStoreClient() {
            @Override
            public Optional<Resource<IdentifiableAttributes>> getIdentifiable(UUID networkUuid, int variantNum, String id) {
                if ("LOAD".equals(id)) {
                    Resource<LoadAttributes> load = Resource.loadBuilder()
                            .id("LOAD")
                            .attributes(LoadAttributes.builder()
                                    .voltageLevelId("VL")
                                    .build())
                            .build();
                    return Optional.of((Resource<IdentifiableAttributes>) (Resource<?>) load);
                }
                return Optional.empty();
            }
        };
        return new PreloadingNetworkStoreClient(new CachedNetworkStoreClient(storeClient), allCollectionsNeededForBusView, preloadingMode, ForkJoinPool.commonPool());
    }

    @Test
    void getIdentifiablePreloadInNoneMode() {
        var client = createClientWithLoad(PreloadingMode.NONE);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        assertTrue(client.getIdentifiable(networkUuid, 0, "LOAD").isPresent());
        for (ResourceType resourceType : ResourceType.values()) {
            assertFalse(client.isResourceTypeCached(networkUuid, 0, resourceType));
        }
    }

    @Test
    void getIdentifiablePreloadCollectionOfTypeInAllMode() {
        var client = createClientWithLoad(PreloadingMode.ALL);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        assertTrue(client.getIdentifiable(networkUuid, 0, "LOAD").isPresent());
        // second call: the collection is already cached
        assertTrue(client.getIdentifiable(networkUuid, 0, "LOAD").isPresent());
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType == ResourceType.LOAD) {
                assertTrue(client.isResourceTypeCached(networkUuid, 0, resourceType));
            } else {
                assertFalse(client.isResourceTypeCached(networkUuid, 0, resourceType));
            }
        }
    }

    @Test
    void getUnknownIdentifiableNoPreloadInAllMode() {
        var client = createClientWithLoad(PreloadingMode.ALL);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        assertTrue(client.getIdentifiable(networkUuid, 0, "unknown").isEmpty());
        for (ResourceType resourceType : ResourceType.values()) {
            assertFalse(client.isResourceTypeCached(networkUuid, 0, resourceType));
        }
    }

    @Test
    void getIdentifiablePreloadOnlyCollectionOfTypeInBusViewMode() {
        var client = createClientWithLoad(true, PreloadingMode.ALL);
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        assertTrue(client.getIdentifiable(networkUuid, 0, "LOAD").isPresent());
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType == ResourceType.LOAD) {
                assertTrue(client.isResourceTypeCached(networkUuid, 0, resourceType));
            } else {
                assertFalse(client.isResourceTypeCached(networkUuid, 0, resourceType));
            }
        }
    }
}
