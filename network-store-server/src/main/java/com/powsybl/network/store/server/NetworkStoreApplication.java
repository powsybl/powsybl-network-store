package com.powsybl.network.store.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class NetworkStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetworkStoreApplication.class, args);
    }
}
