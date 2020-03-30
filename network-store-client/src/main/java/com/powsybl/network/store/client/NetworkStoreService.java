/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.network.store.model.NetworkStoreApi;
import com.powsybl.network.store.model.Resource;
import com.powsybl.tools.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Service
public class NetworkStoreService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkStoreService.class);

    private final RestNetworkStoreClient restStoreClient;

    private final PreloadingStrategy defaultPreloadingStrategy;

    private final BiFunction<RestNetworkStoreClient, PreloadingStrategy, NetworkStoreClient> decorator;

    public NetworkStoreService(String baseUri) {
        this(baseUri, PreloadingStrategy.NONE);
    }

    @Autowired
    public NetworkStoreService(@Value("${network-store-server.base-uri:http://network-store-server/}") String baseUri,
                               @Value("${network-store-server.preloading-strategy:NONE}") PreloadingStrategy defaultPreloadingStrategy) {
        this(new RestNetworkStoreClient(createRestTemplateBuilder(baseUri)), defaultPreloadingStrategy);
    }

    NetworkStoreService(RestNetworkStoreClient restStoreClient, PreloadingStrategy defaultPreloadingStrategy) {
        this(restStoreClient, defaultPreloadingStrategy, NetworkStoreService::createStoreClient);
    }

    NetworkStoreService(RestNetworkStoreClient restStoreClient, PreloadingStrategy defaultPreloadingStrategy,
                        BiFunction<RestNetworkStoreClient, PreloadingStrategy, NetworkStoreClient> decorator) {
        this.restStoreClient = Objects.requireNonNull(restStoreClient);
        this.defaultPreloadingStrategy = Objects.requireNonNull(defaultPreloadingStrategy);
        this.decorator = Objects.requireNonNull(decorator);
    }

    public NetworkStoreService(String baseUri, PreloadingStrategy defaultPreloadingStrategy,
                               BiFunction<RestNetworkStoreClient, PreloadingStrategy, NetworkStoreClient> decorator) {
        this(new RestNetworkStoreClient(createRestTemplateBuilder(baseUri)), defaultPreloadingStrategy, decorator);
    }

    public static NetworkStoreService create(NetworkStoreConfig config) {
        Objects.requireNonNull(config);
        return new NetworkStoreService(config.getBaseUrl(), config.getPreloadingStrategy());
    }

    private static RestTemplateBuilder createRestTemplateBuilder(String baseUri) {
        return new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(UriComponentsBuilder.fromUriString(baseUri)
                        .path(NetworkStoreApi.VERSION)));
    }

    private PreloadingStrategy getNonNullPreloadingStrategy(PreloadingStrategy preloadingStrategy) {
        return preloadingStrategy != null ? preloadingStrategy : defaultPreloadingStrategy;
    }

    private static NetworkStoreClient createStoreClient(RestNetworkStoreClient restStoreClient, PreloadingStrategy preloadingStrategy) {
        Objects.requireNonNull(preloadingStrategy);
        LOGGER.info("Preloading strategy: {}", preloadingStrategy);
        switch (preloadingStrategy) {
            case NONE:
                return new CachedRestNetworkStoreClient(new BufferedRestNetworkStoreClient(restStoreClient));
            case COLLECTION:
                return new PreloadingRestNetworkStoreClient(restStoreClient);
            default:
                throw new IllegalStateException("Unknown preloading strategy: " + preloadingStrategy);
        }
    }

    public NetworkFactory getNetworkFactory() {
        return getNetworkFactory(null);
    }

    public NetworkFactory getNetworkFactory(PreloadingStrategy preloadingStrategy) {
        return new NetworkFactoryImpl(() -> decorator.apply(restStoreClient, getNonNullPreloadingStrategy(preloadingStrategy)));
    }

    public Network createNetwork(String id, String sourceFormat) {
        return createNetwork(id, sourceFormat, null);
    }

    public Network createNetwork(String id, String sourceFormat, PreloadingStrategy preloadingStrategy) {
        return getNetworkFactory(preloadingStrategy).createNetwork(id, sourceFormat);
    }

    public Network importNetwork(Path file) {
        return importNetwork(file, null);
    }

    public Network importNetwork(Path file, PreloadingStrategy preloadingStrategy) {
        DataSource dataSource = Importers.createDataSource(file);
        return importNetwork(dataSource, preloadingStrategy);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource) {
        return importNetwork(dataSource, null);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy) {
        Importer importer = Importers.findImporter(dataSource, LocalComputationManager.getDefault());
        if (importer == null) {
            throw new PowsyblException("No importer found");
        }
        Network network = importer.importData(dataSource, getNetworkFactory(preloadingStrategy), null);
        flush(network);
        return network;
    }

    public Map<UUID, String> getNetworkIds() {
        return restStoreClient.getNetworks().stream()
                .collect(Collectors.toMap(resource -> resource.getAttributes().getUuid(),
                                          Resource::getId));
    }

    public Network getNetwork(UUID uuid) {
        return getNetwork(uuid, null);
    }

    public Network getNetwork(UUID uuid, PreloadingStrategy preloadingStrategy) {
        Objects.requireNonNull(uuid);
        NetworkStoreClient storeClient = decorator.apply(restStoreClient, getNonNullPreloadingStrategy(preloadingStrategy));
        return NetworkImpl.create(storeClient, storeClient.getNetwork(uuid)
                .orElseThrow(() -> new PowsyblException("Network '" + uuid + "' not found")));
    }

    public void deleteNetwork(UUID uuid) {
        restStoreClient.deleteNetwork(uuid);
    }

    public void deleteAllNetworks() {
        getNetworkIds().forEach((key, value) -> restStoreClient.deleteNetwork(key));
    }

    private NetworkImpl getNetworkImpl(Network network) {
        Objects.requireNonNull(network);
        if (!(network instanceof NetworkImpl)) {
            throw new PowsyblException("Cannot flush this network implementation: " + network.getClass().getName());
        }
        return (NetworkImpl) network;
    }

    public UUID getNetworkUuid(Network network) {
        return getNetworkImpl(network).getUuid();
    }

    public void flush(Network network) {
        getNetworkImpl(network).getIndex().getStoreClient().flush();
    }

    @PostConstruct
    public void start() {
        LOGGER.info(Version.getTableString());
    }

    @Override
    @PreDestroy
    public void close() {
    }
}
