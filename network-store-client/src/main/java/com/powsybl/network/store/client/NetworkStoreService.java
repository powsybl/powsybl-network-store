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
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
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
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Service
public class NetworkStoreService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkStoreService.class);

    private final RestClient restClient;

    private final PreloadingStrategy defaultPreloadingStrategy;

    private final BiFunction<RestClient, PreloadingStrategy, NetworkStoreClient> decorator;

    public NetworkStoreService(String baseUri, RestTemplateBuilder restTemplateBuilder) {
        this(baseUri, PreloadingStrategy.NONE, restTemplateBuilder);
    }

    @Autowired
    public NetworkStoreService(@Value("${network-store-server.base-uri:http://network-store-server/}") String baseUri,
                               @Value("${network-store-server.preloading-strategy:NONE}") PreloadingStrategy defaultPreloadingStrategy,
                               RestTemplateBuilder restTemplateBuilder) {
        this(new RestClient(createRestTemplateBuilder(baseUri, restTemplateBuilder)), defaultPreloadingStrategy);
    }

    NetworkStoreService(RestClient restClient, PreloadingStrategy defaultPreloadingStrategy) {
        this(restClient, defaultPreloadingStrategy, NetworkStoreService::createStoreClient);
    }

    NetworkStoreService(RestClient restClient, PreloadingStrategy defaultPreloadingStrategy,
                        BiFunction<RestClient, PreloadingStrategy, NetworkStoreClient> decorator) {
        this.restClient = Objects.requireNonNull(restClient);
        this.defaultPreloadingStrategy = Objects.requireNonNull(defaultPreloadingStrategy);
        this.decorator = Objects.requireNonNull(decorator);
    }

    public NetworkStoreService(String baseUri, PreloadingStrategy defaultPreloadingStrategy,
                               BiFunction<RestClient, PreloadingStrategy, NetworkStoreClient> decorator, RestTemplateBuilder restTemplateBuilder) {
        this(new RestClient(createRestTemplateBuilder(baseUri, restTemplateBuilder)), defaultPreloadingStrategy, decorator);
    }

    public static NetworkStoreService create(NetworkStoreConfig config, RestTemplateBuilder restTemplateBuilder) {
        Objects.requireNonNull(config);
        return new NetworkStoreService(config.getBaseUrl(), config.getPreloadingStrategy(), restTemplateBuilder);
    }

    public static RestTemplateBuilder createRestTemplateBuilder(String baseUri, RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory(UriComponentsBuilder.fromUriString(baseUri)
                        .path(NetworkStoreApi.VERSION)));
    }

    private PreloadingStrategy getNonNullPreloadingStrategy(PreloadingStrategy preloadingStrategy) {
        return preloadingStrategy != null ? preloadingStrategy : defaultPreloadingStrategy;
    }

    private static NetworkStoreClient createStoreClient(RestClient restClient, PreloadingStrategy preloadingStrategy) {
        Objects.requireNonNull(preloadingStrategy);
        LOGGER.info("Preloading strategy: {}", preloadingStrategy);
        switch (preloadingStrategy) {
            case NONE:
                return new CachedNetworkStoreClient(new BufferedNetworkStoreClient(new RestNetworkStoreClient(restClient)));
            case COLLECTION:
                return new PreloadingNetworkStoreClient(new BufferedNetworkStoreClient(new RestNetworkStoreClient(restClient)));
            default:
                throw new IllegalStateException("Unknown preloading strategy: " + preloadingStrategy);
        }
    }

    public NetworkFactory getNetworkFactory() {
        return getNetworkFactory(null);
    }

    public NetworkFactory getNetworkFactory(PreloadingStrategy preloadingStrategy) {
        return new NetworkFactoryImpl(() -> decorator.apply(restClient, getNonNullPreloadingStrategy(preloadingStrategy)));
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

    public Network importNetwork(Path file, Properties parameters) {
        return importNetwork(file, null, parameters);
    }

    public Network importNetwork(Path file, PreloadingStrategy preloadingStrategy, Properties parameters) {
        DataSource dataSource = Importers.createDataSource(file);
        return importNetwork(dataSource, preloadingStrategy, LocalComputationManager.getDefault(), parameters);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource) {
        return importNetwork(dataSource, null, LocalComputationManager.getDefault(), null);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy) {
        return importNetwork(dataSource, preloadingStrategy, LocalComputationManager.getDefault(), null);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy,
                                 ComputationManager computationManager, Properties parameters) {
        Importer importer = Importers.findImporter(dataSource, computationManager);
        if (importer == null) {
            throw new PowsyblException("No importer found");
        }
        Network network = importer.importData(dataSource, getNetworkFactory(preloadingStrategy), parameters);
        flush(network);
        return network;
    }

    public Map<UUID, String> getNetworkIds() {
        return new RestNetworkStoreClient(restClient).getNetworks().stream()
                .collect(Collectors.toMap(resource -> resource.getAttributes().getUuid(),
                                          Resource::getId));
    }

    public Network getNetwork(UUID uuid) {
        return getNetwork(uuid, null);
    }

    public Network getNetwork(UUID uuid, PreloadingStrategy preloadingStrategy) {
        Objects.requireNonNull(uuid);
        NetworkStoreClient storeClient = decorator.apply(restClient, getNonNullPreloadingStrategy(preloadingStrategy));
        return NetworkImpl.create(storeClient, storeClient.getNetwork(uuid)
                .orElseThrow(() -> new PowsyblException("Network '" + uuid + "' not found")));
    }

    public void deleteNetwork(UUID uuid) {
        new RestNetworkStoreClient(restClient).deleteNetwork(uuid);
    }

    public void deleteAllNetworks() {
        RestNetworkStoreClient restStoreClient = new RestNetworkStoreClient(restClient);
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
