package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.Module;
import com.powsybl.network.store.model.TerminalRefAttributesJsonModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class JsonModuleConfig {

    @Bean
    public Module createTerminalRefJsonModule() {
        return new TerminalRefAttributesJsonModule();
    }
}
