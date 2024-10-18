/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.OfflineNetworkStoreClient;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PreloadingAllCollectionsTest {

    @Test
    public void testWithAllCollections() {
        var client = new PreloadingNetworkStoreClient(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()), true, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        assertTrue(client.shouldLoadAllCollectionsNeededForBusView(ResourceType.SUBSTATION));
        client.getSubstations(networkUuid, 0);
        assertFalse(client.shouldLoadAllCollectionsNeededForBusView(ResourceType.SUBSTATION));
    }

    @Test
    public void testWithAllCollectionsDeleteNetwork() {
        var client = new PreloadingNetworkStoreClient(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()), true, ForkJoinPool.commonPool());
        UUID networkUuid = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
        Resource<NetworkAttributes> n1 = Resource.networkBuilder()
                .id("n1")
                .attributes(NetworkAttributes.builder()
                        .uuid(networkUuid)
                        .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                        .caseDate(ZonedDateTime.parse("2015-01-01T00:00:00.000Z"))
                        .build())
                .build();
        client.createNetworks(List.of(n1));
        assertTrue(client.shouldLoadAllCollectionsNeededForBusView(ResourceType.SUBSTATION));
        client.getSubstations(networkUuid, 0);
        assertFalse(client.shouldLoadAllCollectionsNeededForBusView(ResourceType.SUBSTATION));
        // When we delete the network, we should load again all collection needed for bus view on next get
        client.deleteNetwork(networkUuid);
        assertTrue(client.shouldLoadAllCollectionsNeededForBusView(ResourceType.SUBSTATION));
        client.createNetworks(List.of(n1));
        assertTrue(client.shouldLoadAllCollectionsNeededForBusView(ResourceType.SUBSTATION));
        client.getSubstations(networkUuid, 0);
        assertFalse(client.shouldLoadAllCollectionsNeededForBusView(ResourceType.SUBSTATION));
        // When we delete the network variant, we should load again all collection needed for bus view on next get
        client.deleteNetwork(networkUuid, 0);
        assertTrue(client.shouldLoadAllCollectionsNeededForBusView(ResourceType.SUBSTATION));
    }
}
