package com.powsybl.network.store.server;

import com.powsybl.ws.commons.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class NetworkStoreApplication {

    public static void main(String[] args) {
        Utils.initProperties();
        SpringApplication.run(NetworkStoreApplication.class, args);
    }
}
