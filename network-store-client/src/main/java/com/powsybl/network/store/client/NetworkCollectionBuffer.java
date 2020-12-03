/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkCollectionBuffer {

    private final Map<Integer, Resource<NetworkAttributes>> networkResourcesToFlush = new HashMap<>();

    private final Map<Integer, Resource<NetworkAttributes>> updateNetworkResourcesToFlush = new HashMap<>();

    public void create(Resource<NetworkAttributes> networkResource) {
        networkResourcesToFlush.put(networkResource.getAttributes().getVariantNum(), networkResource);
    }

    public void update(Resource<NetworkAttributes> networkResource) {
        if (!networkResourcesToFlush.containsKey(networkResource.getAttributes().getVariantNum())) {
            updateNetworkResourcesToFlush.put(networkResource.getAttributes().getVariantNum(), networkResource);
        }
    }

    public void delete(UUID networkUuid) {
        // only delete network on server if not in the creation buffer
        if (networkResourcesToFlush.remove(networkUuid) == null) {
            updateNetworkResourcesToFlush.remove(networkUuid);

            delegate.deleteNetwork(networkUuid);
        }
    }
}
