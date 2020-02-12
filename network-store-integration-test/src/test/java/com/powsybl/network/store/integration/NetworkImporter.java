/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;

import java.nio.file.Paths;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public final class NetworkImporter {
    public static void main(String[] args) throws Exception {
        String baseUrl = "http://localhost:8080/";
        try (NetworkStoreService service = new NetworkStoreService(baseUrl, PreloadingStrategy.NONE)) {
            Network network = service.importNetwork(Paths.get("/tmp/test.zip"));
        }
    }

    private NetworkImporter() { }
}
