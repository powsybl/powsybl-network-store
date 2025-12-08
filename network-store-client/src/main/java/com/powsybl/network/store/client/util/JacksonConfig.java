/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client.util;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Configuration
public class JacksonConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void configureObjectMapper() {
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
    }

}
