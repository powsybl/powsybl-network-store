/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.tools;

import com.google.auto.service.AutoService;
import com.powsybl.network.store.client.NetworkStoreConfig;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class NetworkStoreDeleteTool implements Tool {

    private static final String NETWORK_UUID = "network-uuid";

    private final Supplier<NetworkStoreService> networkStoreServiceSupplier;

    public NetworkStoreDeleteTool() {
        this(() -> NetworkStoreService.create(NetworkStoreConfig.load()));
    }

    public NetworkStoreDeleteTool(Supplier<NetworkStoreService> networkStoreServiceSupplier) {
        this.networkStoreServiceSupplier = Objects.requireNonNull(networkStoreServiceSupplier);
    }

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "network-store-delete";
            }

            @Override
            public String getTheme() {
                return "Network";
            }

            @Override
            public String getDescription() {
                return "delete a network in the store";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder()
                        .longOpt(NETWORK_UUID)
                        .desc("Network UUID in the store")
                        .hasArg()
                        .argName("UUID")
                        .required()
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) {
        ToolOptions toolOptions = new ToolOptions(line, context);
        UUID networkUuid = toolOptions.getValue(NETWORK_UUID).map(UUID::fromString).orElseThrow(() -> new IllegalArgumentException("Network ID is missing"));

        try (NetworkStoreService service = networkStoreServiceSupplier.get()) {
            context.getOutputStream().println("Deleting " + networkUuid + "...");

            service.deleteNetwork(networkUuid);
        }
    }
}
