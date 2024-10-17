/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.OfflineNetworkStoreClient;
import com.powsybl.network.store.model.ResourceType;
import org.junit.Test;

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
}
