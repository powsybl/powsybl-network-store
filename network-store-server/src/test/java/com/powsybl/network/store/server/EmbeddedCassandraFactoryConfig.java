/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.artifact.DefaultArtifact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class EmbeddedCassandraFactoryConfig {

    @Bean
    @Scope("singleton")
    CassandraFactory embeddedCassandraFactory() throws UnknownHostException {
        EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
        Version version = Version.of("4.0-alpha3");
        Path directory = Paths.get(System.getProperty("user.home") + "/apache-cassandra-4.0-alpha3");
        cassandraFactory.setArtifact(new DefaultArtifact(version, directory));
        cassandraFactory.setPort(9142);
        cassandraFactory.setJmxLocalPort(0);
        cassandraFactory.setRpcPort(0);
        cassandraFactory.setStoragePort(16432);
        cassandraFactory.setAddress(InetAddress.getByName("localhost"));
        return cassandraFactory;
    }
}
