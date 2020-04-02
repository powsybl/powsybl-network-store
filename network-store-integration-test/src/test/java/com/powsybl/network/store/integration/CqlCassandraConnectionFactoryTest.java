/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.github.nosan.embedded.cassandra.api.connection.CqlSessionCassandraConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@Configuration
public class CqlCassandraConnectionFactoryTest {

    @Bean
    CqlSessionCassandraConnectionFactory cqlCassandraConnectionFactory() {
        return new CqlSessionCassandraConnectionFactory();
    }
}
