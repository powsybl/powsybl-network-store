package com.powsybl.network.store.server;

import com.fasterxml.jackson.databind.Module;
import com.powsybl.network.store.model.TerminalRefAttributesJsonModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class NetworkStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetworkStoreApplication.class, args);
    }

    @Bean
    public Module createTerminalRefJsonModule() {
        return new TerminalRefAttributesJsonModule();
    }
}
