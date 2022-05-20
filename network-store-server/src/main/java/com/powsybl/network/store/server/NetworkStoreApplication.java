package com.powsybl.network.store.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class NetworkStoreApplication {

    private static final String ALLOW_ENCODED_IN_PATH_PROPERTY = "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH";

    public static void main(String[] args) {
        System.setProperty(ALLOW_ENCODED_IN_PATH_PROPERTY, "true");
        SpringApplication.run(NetworkStoreApplication.class, args);
    }
}
