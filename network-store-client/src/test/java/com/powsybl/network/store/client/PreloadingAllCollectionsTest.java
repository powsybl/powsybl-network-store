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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PreloadingAllCollectionsTest {

    private static final UUID NETWORK_UUID = UUID.randomUUID();

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

    private static final class StoreClientWithLoad extends OfflineNetworkStoreClient {
        private int getIdentifiableCallCount = 0;
        private int getLoadsCallCount = 0;

        @Override
        public Optional<Resource<IdentifiableAttributes>> getIdentifiable(UUID networkUuid, int variantNum, String id) {
            getIdentifiableCallCount++;
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

        @Override
        public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
            getLoadsCallCount++;
            return super.getLoads(networkUuid, variantNum);
        }
    }

    private static PreloadingNetworkStoreClient createClientWithLoad(StoreClientWithLoad storeClient, PreloadingMode preloadingMode) {
        return createClientWithLoad(storeClient, false, preloadingMode);
    }

    private static PreloadingNetworkStoreClient createClientWithLoad(StoreClientWithLoad storeClient, boolean allCollectionsNeededForBusView, PreloadingMode preloadingMode) {
        return new PreloadingNetworkStoreClient(new CachedNetworkStoreClient(storeClient), allCollectionsNeededForBusView, preloadingMode, ForkJoinPool.commonPool());
    }

    @Test
    void getIdentifiablePreloadInNoneMode() {
        var storeClient = new StoreClientWithLoad();
        var client = createClientWithLoad(storeClient, PreloadingMode.NONE);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        assertEquals(1, storeClient.getIdentifiableCallCount);
        assertEquals(0, storeClient.getLoadsCallCount);
        for (ResourceType resourceType : ResourceType.values()) {
            assertFalse(client.isResourceTypeCached(NETWORK_UUID, 0, resourceType));
        }
    }

    @Test
    void getIdentifiablePreloadCollectionOfTypeInAllMode() {
        var storeClient = new StoreClientWithLoad();
        var client = createClientWithLoad(storeClient, PreloadingMode.ALL);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        // second call: the collection is already cached
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        assertEquals(1, storeClient.getIdentifiableCallCount);
        assertEquals(1, storeClient.getLoadsCallCount);
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType == ResourceType.LOAD) {
                assertTrue(client.isResourceTypeCached(NETWORK_UUID, 0, resourceType));
            } else {
                assertFalse(client.isResourceTypeCached(NETWORK_UUID, 0, resourceType));
            }
        }
    }

    @Test
    void getUnknownIdentifiableNoPreloadInAllMode() {
        var storeClient = new StoreClientWithLoad();
        var client = createClientWithLoad(storeClient, PreloadingMode.ALL);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "unknown").isEmpty());
        assertEquals(1, storeClient.getIdentifiableCallCount);
        assertEquals(0, storeClient.getLoadsCallCount);
        for (ResourceType resourceType : ResourceType.values()) {
            assertFalse(client.isResourceTypeCached(NETWORK_UUID, 0, resourceType));
        }
    }

    @Test
    void getIdentifiablePreloadOnlyCollectionOfTypeInBusViewMode() {
        var storeClient = new StoreClientWithLoad();
        var client = createClientWithLoad(storeClient, true, PreloadingMode.ALL);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        assertEquals(1, storeClient.getIdentifiableCallCount);
        assertEquals(1, storeClient.getLoadsCallCount);
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType == ResourceType.LOAD) {
                assertTrue(client.isResourceTypeCached(NETWORK_UUID, 0, resourceType));
            } else {
                assertFalse(client.isResourceTypeCached(NETWORK_UUID, 0, resourceType));
            }
        }
    }
}
