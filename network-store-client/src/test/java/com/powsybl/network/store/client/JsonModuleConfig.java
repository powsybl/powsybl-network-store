/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */

import com.fasterxml.jackson.databind.Module;
import com.powsybl.network.store.model.TerminalRefAttributesJsonModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class JsonModuleConfig {

    @Bean
    public Module createTerminalRefJsonModule() {
        return new TerminalRefAttributesJsonModule();
    }
}
