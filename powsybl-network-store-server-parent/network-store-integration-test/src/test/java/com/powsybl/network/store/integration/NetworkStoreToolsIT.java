/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.server.NetworkStoreApplication;
import com.powsybl.network.store.tools.NetworkStoreDeleteTool;
import com.powsybl.network.store.tools.NetworkStoreImportTool;
import com.powsybl.network.store.tools.NetworkStoreListTool;
import com.powsybl.network.store.tools.NetworkStoreScriptTool;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Tool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
abstract class AbstractNetworkStoreToolsIT extends AbstractToolTest {
}

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextHierarchy({
    @ContextConfiguration(classes = {NetworkStoreApplication.class, NetworkStoreService.class})
    })
/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class NetworkStoreToolsIT extends AbstractNetworkStoreToolsIT {

    @LocalServerPort
    private int randomServerPort;

    private NetworkStoreDeleteTool deleteTool;

    private NetworkStoreImportTool importTool;

    private NetworkStoreListTool listTool;

    private NetworkStoreScriptTool scriptTool;

    private String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/";
    }

    @Before
    public void setup() throws Exception {
        Supplier<NetworkStoreService> networkStoreServiceSupplier = () -> new NetworkStoreService(getBaseUrl());
        deleteTool = new NetworkStoreDeleteTool(networkStoreServiceSupplier);
        importTool = new NetworkStoreImportTool(networkStoreServiceSupplier);
        listTool = new NetworkStoreListTool(networkStoreServiceSupplier);
        scriptTool = new NetworkStoreScriptTool(networkStoreServiceSupplier);

        super.setUp();
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Arrays.asList(deleteTool, importTool, listTool, scriptTool);
    }

    @Override
    public void assertCommand() {
        assertCommand(deleteTool.getCommand(), "network-store-delete", 1, 1);
        assertOption(deleteTool.getCommand().getOptions(), "network-uuid", true, true);
        assertCommand(importTool.getCommand(), "network-store-import", 3, 1);
        assertOption(importTool.getCommand().getOptions(), "input-file", true, true);
        assertCommand(listTool.getCommand(), "network-store-list", 0, 0);
        assertCommand(scriptTool.getCommand(), "network-store-script", 2, 2);
        assertOption(scriptTool.getCommand().getOptions(), "network-uuid", true, true);
        assertOption(scriptTool.getCommand().getOptions(), "script-file", true, true);
    }

    @Test
    public void test() throws IOException {
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
        assertCommand(new String[] {"network-store-script", "--network-uuid", networkUuid.toString(), "--script-file", "/work/test.groovy"},
                      0,
                      "Applying '/work/test.groovy' on " + networkUuid + "..." + System.lineSeparator() + "id: sim1",
                      "");

        // delete network
        assertCommand(new String[] {"network-store-delete", "--network-uuid", networkUuid.toString()},
                      0,
                      "Deleting " + networkUuid + "...",
                      "");
    }
}
