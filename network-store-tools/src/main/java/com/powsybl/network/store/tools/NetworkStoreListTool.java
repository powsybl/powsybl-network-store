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
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class NetworkStoreListTool implements Tool {

    private final Supplier<NetworkStoreService> networkStoreServiceSupplier;

    public NetworkStoreListTool() {
        this(() -> NetworkStoreService.create(NetworkStoreConfig.load()));
    }

    public NetworkStoreListTool(Supplier<NetworkStoreService> networkStoreServiceSupplier) {
        this.networkStoreServiceSupplier = Objects.requireNonNull(networkStoreServiceSupplier);
    }

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "network-store-list";
            }

            @Override
            public String getTheme() {
                return "Network";
            }

            @Override
            public String getDescription() {
                return "list networks in the store";
            }

            @Override
            public Options getOptions() {
                return new Options();
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) {
        try (NetworkStoreService service = networkStoreServiceSupplier.get()) {
            service.getNetworkIds().forEach((key, value) -> context.getOutputStream().println(key + " : " + value));
        }
    }
}
