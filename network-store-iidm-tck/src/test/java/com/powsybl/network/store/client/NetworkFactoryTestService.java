/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.NetworkFactoryService;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(NetworkFactoryService.class)
public class NetworkFactoryTestService implements NetworkFactoryService {

    private static final String NAME = "NetworkStoreTest";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public NetworkFactory createNetworkFactory() {
        String networkStoreBaseUrl = PlatformConfig.defaultConfig().getModuleConfig("network-store")
                .getStringProperty("base-url");
        RestClient restClient = new RestClientImpl(networkStoreBaseUrl);
        return new NetworkFactoryImpl(() -> new RestNetworkStoreClient(restClient));
    }
}
