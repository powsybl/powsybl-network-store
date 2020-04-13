/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.github.nosan.embedded.cassandra.api.connection.CqlSessionCassandraConnection;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;
import com.github.nosan.embedded.cassandra.spring.test.EmbeddedCassandra;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.server.CassandraConfig;
import com.powsybl.network.store.server.EmbeddedCassandraFactoryConfig;
import com.powsybl.network.store.server.NetworkStoreApplication;
import com.powsybl.network.store.tools.NetworkStoreDeleteTool;
import com.powsybl.network.store.tools.NetworkStoreImportTool;
import com.powsybl.network.store.tools.NetworkStoreListTool;
import com.powsybl.network.store.tools.NetworkStoreScriptTool;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Tool;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {NetworkStoreApplication.class, CassandraConfig.class, NetworkStoreService.class,
        EmbeddedCassandraFactoryConfig.class, CqlCassandraConnectionTestFactory.class})
@EmbeddedCassandra(scripts = {"classpath:create_keyspace.cql", "classpath:iidm.cql"})
public class NetworkStoreToolsIT extends AbstractToolTest {

    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private CqlSessionCassandraConnection cqlSessionCassandraConnection;

    private String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/";
    }

    @Before
    public void setup() throws Exception {
        super.setUp();
        CqlDataSet.ofClasspaths("truncate.cql").forEachStatement(cqlSessionCassandraConnection::execute);
    }

    @Override
    protected Iterable<Tool> getTools() {
        Supplier<NetworkStoreService> networkStoreServiceSupplier = () -> new NetworkStoreService(getBaseUrl());
        return Arrays.asList(new NetworkStoreDeleteTool(networkStoreServiceSupplier),
                             new NetworkStoreImportTool(networkStoreServiceSupplier),
                             new NetworkStoreListTool(networkStoreServiceSupplier),
                             new NetworkStoreScriptTool(networkStoreServiceSupplier));
    }

    @Override
    public void assertCommand() {
        try {
            // import a xiidm file
            Files.copy(getClass().getResourceAsStream("/test.xiidm"), fileSystem.getPath("/work/test.xiidm"));
            assertCommand(new String[] {"network-store-import", "--input-file", "/work/test.xiidm"},
                          0,
                          "Importing file '/work/test.xiidm'...",
                          "");

            // get network UUID
            UUID networkUuid;
            try (NetworkStoreService networkStoreService = new NetworkStoreService(getBaseUrl())) {
                Map<UUID, String> networkIds = networkStoreService.getNetworkIds();
                assertEquals(1, networkIds.size());
                networkUuid = networkIds.entrySet().iterator().next().getKey();
            }

            // list networks
            assertCommand(new String[] {"network-store-list"},
                          0,
                          networkUuid + " : sim1",
                          "");

            // apply groovy script
            Files.copy(getClass().getResourceAsStream("/test.groovy"), fileSystem.getPath("/work/test.groovy"));
            assertCommand(new String[] {"network-store-script", "--network-uuid",  networkUuid.toString(), "--script-file", "/work/test.groovy"},
                          0,
                          "Applying '/work/test.groovy' on " + networkUuid + "..." + System.lineSeparator() + "id: sim1",
                          "");

            // delete network
            assertCommand(new String[] {"network-store-delete", "--network-uuid",  networkUuid.toString()},
                          0,
                          "Deleting " + networkUuid + "...",
                          "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
