/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.NetworkFactoryService;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(NetworkFactoryService.class)
public class NetworkFactoryServiceImpl implements NetworkFactoryService {

    @Override
    public String getName() {
        return "NetworkStore";
    }

    @Override
    public NetworkFactory createNetworkFactory() {
        return new NetworkFactoryImpl();
    }
}
