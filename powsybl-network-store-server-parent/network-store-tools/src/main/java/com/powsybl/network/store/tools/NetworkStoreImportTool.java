/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Importers;
import com.powsybl.network.store.client.NetworkStoreConfig;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

import static com.powsybl.iidm.network.tools.ConversionToolUtils.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class NetworkStoreImportTool implements Tool {

    private static final String INPUT_FILE = "input-file";

    private final Supplier<NetworkStoreService> networkStoreServiceSupplier;

    public NetworkStoreImportTool() {
        this(() -> NetworkStoreService.create(NetworkStoreConfig.load()));
    }

    public NetworkStoreImportTool(Supplier<NetworkStoreService> networkStoreServiceSupplier) {
        this.networkStoreServiceSupplier = Objects.requireNonNull(networkStoreServiceSupplier);
    }

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "network-store-import";
            }

            @Override
            public String getTheme() {
                return "Network";
            }

            @Override
            public String getDescription() {
                return "import a network in the store";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(INPUT_FILE)
                        .desc("the input file")
                        .hasArg()
                        .argName("INPUT_FILE")
                        .required()
                        .build());
                options.addOption(createImportParametersFileOption());
                options.addOption(createImportParameterOption());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        ToolOptions toolOptions = new ToolOptions(line, context);
        Path inputFile = toolOptions.getPath(INPUT_FILE).orElseThrow(() -> new IllegalArgumentException("Input file is missing"));

        Properties inputParams = readProperties(line, OptionType.IMPORT, context);

        DataSource dataSource = Importers.createDataSource(inputFile);

        try (NetworkStoreService service = networkStoreServiceSupplier.get()) {
            context.getOutputStream().println("Importing file '" + inputFile + "'...");

            service.importNetwork(dataSource, null, context.getShortTimeExecutionComputationManager(), inputParams);
        }
    }
}
