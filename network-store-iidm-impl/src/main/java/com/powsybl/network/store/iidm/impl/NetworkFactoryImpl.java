/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkFactoryImpl implements NetworkFactory {

    private final Supplier<NetworkStoreClient> storeClientSupplier;

    public NetworkFactoryImpl() {
        this(() -> new CachedNetworkStoreClient(new OfflineNetworkStoreClient()));
    }

    public NetworkFactoryImpl(Supplier<NetworkStoreClient> storeClientSupplier) {
        this.storeClientSupplier = Objects.requireNonNull(storeClientSupplier);
    }

    @Override
    public Network createNetwork(String id, String sourceFormat) {
        UUID networkUuid = UUID.randomUUID();
        NetworkStoreClient storeClient = storeClientSupplier.get();
        Resource<NetworkAttributes> resource = Resource.networkBuilder()
                .id(id)
                .variantNum(Resource.INITIAL_VARIANT_NUM)
                .attributes(NetworkAttributes.builder()
                                             .uuid(networkUuid)
                                             .variantId(VariantManagerConstants.INITIAL_VARIANT_ID)
                                             .caseDate(ZonedDateTime.now())
                                             .forecastDistance(0)
                                             .sourceFormat(sourceFormat)
                                             .build())
                .build();
        storeClient.createNetworks(Collections.singletonList(resource));
        return NetworkImpl.create(storeClient, resource);
    }

    @Override
    public Network merge(String id, Network... networks) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Network merge(Network... networks) {
        throw new UnsupportedOperationException("TODO");
    }
}
