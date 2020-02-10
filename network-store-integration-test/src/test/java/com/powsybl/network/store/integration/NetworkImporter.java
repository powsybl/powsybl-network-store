package com.powsybl.network.store.integration;

import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;

import java.nio.file.Paths;

public final class NetworkImporter {
    public static void main(String[] args) throws Exception {
        String baseUrl = "http://localhost:8080/";
        try (NetworkStoreService service = new NetworkStoreService(baseUrl, PreloadingStrategy.NONE)) {
            Network network = service.importNetwork(Paths.get("/tmp/test.zip"));
        }
    }

    private NetworkImporter() { }
}
