/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.tools;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreConfig;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class NetworkStoreScriptTool implements Tool {

    private static final String SCRIPT_FILE = "script-file";
    private static final String NETWORK_UUID = "network-uuid";

    private final Supplier<NetworkStoreService> networkStoreServiceSupplier;

    public NetworkStoreScriptTool() {
        this(() -> NetworkStoreService.create(NetworkStoreConfig.load()));
    }

    public NetworkStoreScriptTool(Supplier<NetworkStoreService> networkStoreServiceSupplier) {
        this.networkStoreServiceSupplier = Objects.requireNonNull(networkStoreServiceSupplier);
    }

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "network-store-script";
            }

            @Override
            public String getTheme() {
                return "Network";
            }

            @Override
            public String getDescription() {
                return "apply a script on a network in the store";
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
                options.addOption(Option.builder()
                        .longOpt(SCRIPT_FILE)
                        .desc("Script file to apply to the network")
                        .hasArg()
                        .argName("FILE")
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

    private void runGroovyScript(Network network, Path scriptFile, PrintStream out) throws IOException {
        try (Reader reader = Files.newBufferedReader(scriptFile, StandardCharsets.UTF_8)) {
            CompilerConfiguration conf = new CompilerConfiguration();

            Binding binding = new Binding();
            binding.setVariable("network", network);
            binding.setVariable("out", out);

            GroovyShell shell = new GroovyShell(binding, conf);
            shell.evaluate(reader);
        }
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        ToolOptions toolOptions = new ToolOptions(line, context);
        UUID networkUuid = toolOptions.getValue(NETWORK_UUID).map(UUID::fromString).orElseThrow(() -> new IllegalArgumentException("Network UUID is missing"));
        Path scriptFile = toolOptions.getPath(SCRIPT_FILE).orElseThrow(() -> new IllegalArgumentException("Script file is missing"));

        try (NetworkStoreService service = networkStoreServiceSupplier.get()) {
            Network network = service.getNetwork(networkUuid);
            if (scriptFile.toString().endsWith(".groovy")) {
                context.getOutputStream().println("Applying '" + scriptFile + "' on " + networkUuid + "...");

                runGroovyScript(network, scriptFile, context.getOutputStream());
            }
        }
    }
}
