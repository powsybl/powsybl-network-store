/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
@Configuration
public class PreloadingStrategyConfiguration {

    @Value("${powsybl.services.network-store-server.preloading-strategy:NONE}")
    private String strategy;

    @Bean
    public PreloadingStrategy preloadingStrategy() {
        return PreloadingStrategy.fromString(strategy);
    }
}
