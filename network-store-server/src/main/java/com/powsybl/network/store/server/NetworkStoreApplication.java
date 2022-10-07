package com.powsybl.network.store.server;

import com.powsybl.ws.commons.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.logging.core.LogServiceFactory;
import liquibase.license.LicenseServiceFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.nativex.hint.TypeHint;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@TypeHint(types = {SimpleDriverDataSource.class, LiquibaseConfiguration.class, LogServiceFactory.class,
        LicenseServiceFactory.class})
@SpringBootApplication
public class NetworkStoreApplication {

    public static void main(String[] args) {
        Utils.initProperties();
        SpringApplication.run(NetworkStoreApplication.class, args);
    }
}
