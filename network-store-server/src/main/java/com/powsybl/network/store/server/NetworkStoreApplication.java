package com.powsybl.network.store.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.powsybl.commons.json.JsonUtil;

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
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        objectMapper.registerModule(new JodaModule());
        return objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
