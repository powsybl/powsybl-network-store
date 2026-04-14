/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import java.util.Optional;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.NetworkFactoryService;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(NetworkFactoryService.class)
public class NetworkFactoryServiceImpl implements NetworkFactoryService {

    public static final boolean DEFAULT_USE_CALCULATEDBUS_FICTITIOUSP0Q0 = true;

    @Override
    public String getName() {
        return "NetworkStore";
    }

    @Override
    public NetworkFactory createNetworkFactory() {
        boolean useCalculatedBusFictitiousP0Q0 = loadConfigUseCalculatedBusFictitiousP0Q0();
        return new NetworkFactoryImpl(useCalculatedBusFictitiousP0Q0);
    }

    // if we start to have more things like this, we should
    // group them in a separate class of parameters independant
    // of the server. For now this one is
    // probably only temporary until we fix the underlying
    // performance issue that forces us to have it
    public static boolean loadConfigUseCalculatedBusFictitiousP0Q0() {
        return loadConfigUseCalculatedBusFictitiousP0Q0(PlatformConfig.defaultConfig());
    }

    public static boolean loadConfigUseCalculatedBusFictitiousP0Q0(PlatformConfig platformConfig) {
        Optional<ModuleConfig> moduleConfig = platformConfig
                .getOptionalModuleConfig("network-store-iidm");
        boolean useCalculatedBusFictitiousP0Q0 = moduleConfig.flatMap(mc -> mc.getOptionalBooleanProperty("use-calculatedbus-fictitiousP0Q0"))
                .orElse(DEFAULT_USE_CALCULATEDBUS_FICTITIOUSP0Q0);
        return useCalculatedBusFictitiousP0Q0;
    }
}
