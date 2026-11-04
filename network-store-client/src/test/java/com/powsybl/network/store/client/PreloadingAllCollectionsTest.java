/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.OfflineNetworkStoreClient;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    private static OfflineNetworkStoreClient createStoreClientWithLoad() {
        var storeClient = spy(new OfflineNetworkStoreClient());
        Resource<LoadAttributes> load = Resource.loadBuilder()
                .id("LOAD")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("VL")
                        .build())
                .build();
        doReturn(Optional.of(load)).when(storeClient).getIdentifiable(any(), anyInt(), eq("LOAD"));
        return storeClient;
    }

    private static PreloadingNetworkStoreClient createClientWithLoad(OfflineNetworkStoreClient storeClient,
                                                                     boolean allCollectionsNeededForBusView,
                                                                     IdentifiablePreloadingMode identifiablePreloadingMode) {
        return new PreloadingNetworkStoreClient(new CachedNetworkStoreClient(storeClient), allCollectionsNeededForBusView, identifiablePreloadingMode, ForkJoinPool.commonPool());
    }

    private static void assertCachedResourceTypes(PreloadingNetworkStoreClient client, Set<ResourceType> expectedResourceTypes) {
        for (ResourceType resourceType : ResourceType.values()) {
            assertEquals(expectedResourceTypes.contains(resourceType), client.isResourceTypeCached(NETWORK_UUID, 0, resourceType));
        }
    }

    @Test
    void getIdentifiablePreloadInNoneMode() {
        OfflineNetworkStoreClient storeClient = createStoreClientWithLoad();
        PreloadingNetworkStoreClient client = createClientWithLoad(storeClient, false, IdentifiablePreloadingMode.NONE);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        verify(storeClient, times(1)).getIdentifiable(NETWORK_UUID, 0, "LOAD");
        verify(storeClient, never()).getLoads(NETWORK_UUID, 0);
        verify(storeClient, never()).getGenerators(NETWORK_UUID, 0);
        assertCachedResourceTypes(client, Set.of());
    }

    @Test
    void getIdentifiablePreloadInNoneModeWithBusView() {
        OfflineNetworkStoreClient storeClient = createStoreClientWithLoad();
        PreloadingNetworkStoreClient client = createClientWithLoad(storeClient, true, IdentifiablePreloadingMode.NONE);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        verify(storeClient, times(1)).getIdentifiable(NETWORK_UUID, 0, "LOAD");
        verify(storeClient, never()).getLoads(NETWORK_UUID, 0);
        verify(storeClient, never()).getGenerators(NETWORK_UUID, 0);
        assertCachedResourceTypes(client, Set.of());

        // the bus view flag still applies as soon as a collection is requested
        client.getSubstations(NETWORK_UUID, 0);
        verify(storeClient, times(1)).getLoads(NETWORK_UUID, 0);
        verify(storeClient, times(1)).getGenerators(NETWORK_UUID, 0);
        assertCachedResourceTypes(client, PreloadingNetworkStoreClient.RESOURCE_TYPES_NEEDED_FOR_BUS_VIEW);
    }

    @Test
    void getIdentifiablePreloadCollectionOfTypeInAllMode() {
        OfflineNetworkStoreClient storeClient = createStoreClientWithLoad();
        PreloadingNetworkStoreClient client = createClientWithLoad(storeClient, false, IdentifiablePreloadingMode.COLLECTION);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        // second call: the collection is already cached
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        verify(storeClient, times(1)).getIdentifiable(NETWORK_UUID, 0, "LOAD");
        verify(storeClient, times(1)).getLoads(NETWORK_UUID, 0);
        verify(storeClient, never()).getGenerators(NETWORK_UUID, 0);
        assertCachedResourceTypes(client, Set.of(ResourceType.LOAD));
    }

    @Test
    void getIdentifiablePreloadAllCollectionsNeededForBusViewInAllMode() {
        OfflineNetworkStoreClient storeClient = createStoreClientWithLoad();
        PreloadingNetworkStoreClient client = createClientWithLoad(storeClient, true, IdentifiablePreloadingMode.COLLECTION);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "LOAD").isPresent());
        verify(storeClient, times(1)).getIdentifiable(NETWORK_UUID, 0, "LOAD");
        verify(storeClient, times(1)).getLoads(NETWORK_UUID, 0);
        verify(storeClient, times(1)).getGenerators(NETWORK_UUID, 0);
        assertCachedResourceTypes(client, PreloadingNetworkStoreClient.RESOURCE_TYPES_NEEDED_FOR_BUS_VIEW);
    }

    @Test
    void getUnknownIdentifiableNoPreloadInAllMode() {
        OfflineNetworkStoreClient storeClient = createStoreClientWithLoad();
        PreloadingNetworkStoreClient client = createClientWithLoad(storeClient, false, IdentifiablePreloadingMode.COLLECTION);
        assertTrue(client.getIdentifiable(NETWORK_UUID, 0, "unknown").isEmpty());
        verify(storeClient, times(1)).getIdentifiable(NETWORK_UUID, 0, "unknown");
        verify(storeClient, never()).getLoads(NETWORK_UUID, 0);
        verify(storeClient, never()).getGenerators(NETWORK_UUID, 0);
        assertCachedResourceTypes(client, Set.of());
    }
}
