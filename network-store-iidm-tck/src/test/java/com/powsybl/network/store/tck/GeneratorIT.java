/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.tck;

import com.github.nosan.embedded.cassandra.api.connection.ClusterCassandraConnection;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;
import com.powsybl.iidm.network.tck.AbstractGeneratorTest;
import com.powsybl.network.store.server.CassandraConfig;
import com.powsybl.network.store.server.NetworkStoreApplication;
import com.powsybl.network.store.test.EmbeddedCassandraFactoryConfig;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.ContextHierarchy;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextHierarchy({
    @ContextConfiguration(classes = {EmbeddedCassandraFactoryConfig.class, CassandraConfig.class}),
    @ContextConfiguration(classes = {NetworkStoreApplication.class})
    })
@TestPropertySource(properties = { "spring.config.location=classpath:application.yaml" })
public class GeneratorIT extends AbstractGeneratorTest {

    @Autowired
    private ClusterCassandraConnection clusterCassandraConnection;

    @Before
    public void setup() {
        CqlDataSet.ofClasspaths("truncate.cql").forEachStatement(clusterCassandraConnection::execute);
    }

}
