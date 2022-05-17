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
import com.powsybl.commons.reporter.Reporter;
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
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.NetworkInfos;
import com.powsybl.network.store.model.Resource;
import com.powsybl.tools.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;
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

    public NetworkStoreService(String baseUri) {
        this(baseUri, PreloadingStrategy.NONE);
    }

    @Autowired
    public NetworkStoreService(@Value("${network-store-server.base-uri:http://network-store-server/}") String baseUri,
                               @Value("${network-store-server.preloading-strategy:NONE}") PreloadingStrategy defaultPreloadingStrategy) {
        this(new RestClientImpl(baseUri), defaultPreloadingStrategy);
    }

    public NetworkStoreService(RestClient restClient, PreloadingStrategy defaultPreloadingStrategy) {
        this(restClient, defaultPreloadingStrategy, NetworkStoreService::createStoreClient);
    }

    NetworkStoreService(RestClient restClient, PreloadingStrategy defaultPreloadingStrategy,
                        BiFunction<RestClient, PreloadingStrategy, NetworkStoreClient> decorator) {
        this.restClient = Objects.requireNonNull(restClient);
        this.defaultPreloadingStrategy = Objects.requireNonNull(defaultPreloadingStrategy);
        this.decorator = Objects.requireNonNull(decorator);
    }

    public NetworkStoreService(String baseUri, PreloadingStrategy defaultPreloadingStrategy,
                               BiFunction<RestClient, PreloadingStrategy, NetworkStoreClient> decorator) {
        this(new RestClientImpl(baseUri), defaultPreloadingStrategy, decorator);
    }

    public static NetworkStoreService create(NetworkStoreConfig config) {
        Objects.requireNonNull(config);
        return new NetworkStoreService(config.getBaseUrl(), config.getPreloadingStrategy());
    }

    private PreloadingStrategy getNonNullPreloadingStrategy(PreloadingStrategy preloadingStrategy) {
        return preloadingStrategy != null ? preloadingStrategy : defaultPreloadingStrategy;
    }

    private static NetworkStoreClient createStoreClient(RestClient restClient, PreloadingStrategy preloadingStrategy) {
        Objects.requireNonNull(preloadingStrategy);
        LOGGER.info("Preloading strategy: {}", preloadingStrategy);
        var cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(new RestNetworkStoreClient(restClient)));
        switch (preloadingStrategy) {
            case NONE:
                return cachedClient;
            case COLLECTION:
                return new PreloadingNetworkStoreClient(cachedClient);
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
        return importNetwork(file, (Properties) null);
    }

    public Network importNetwork(Path file, Reporter report) {
        return importNetwork(Importers.createDataSource(file), null, LocalComputationManager.getDefault(), null, report);
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

    public Network importNetwork(ReadOnlyDataSource dataSource, Reporter reporter) {
        return importNetwork(dataSource, reporter, true);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, Reporter reporter, boolean flush) {
        return importNetwork(dataSource, null, LocalComputationManager.getDefault(), null, reporter, flush);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy) {
        return importNetwork(dataSource, preloadingStrategy, LocalComputationManager.getDefault(), null);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy,
                                 ComputationManager computationManager, Properties parameters) {
        return importNetwork(dataSource, preloadingStrategy, computationManager, parameters, true);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy,
                                 ComputationManager computationManager, Properties parameters, boolean flush) {
        Importer importer = Importers.findImporter(dataSource, computationManager);
        if (importer == null) {
            throw new PowsyblException("No importer found");
        }
        Network network = importer.importData(dataSource, getNetworkFactory(preloadingStrategy), parameters);
        if (flush) {
            flush(network);
        }
        return network;
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy,
                                 ComputationManager computationManager, Properties parameters, Reporter reporter) {
        return importNetwork(dataSource, preloadingStrategy, computationManager, parameters, reporter, true);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy,
                                 ComputationManager computationManager, Properties parameters, Reporter reporter, boolean flush) {
        Importer importer = Importers.findImporter(dataSource, computationManager);
        if (importer == null) {
            throw new PowsyblException("No importer found");
        }
        Network network = importer.importData(dataSource, getNetworkFactory(preloadingStrategy), parameters, reporter);
        if (flush) {
            flush(network);
        }
        return network;
    }

    public Map<UUID, String> getNetworkIds() {
        return new RestNetworkStoreClient(restClient).getNetworksInfos().stream()
                .collect(Collectors.toMap(NetworkInfos::getUuid, NetworkInfos::getId));
    }

    public Network getNetwork(UUID uuid) {
        return getNetwork(uuid, null);
    }

    public Network getNetwork(UUID uuid, PreloadingStrategy preloadingStrategy) {
        Objects.requireNonNull(uuid);
        NetworkStoreClient storeClient = decorator.apply(restClient, getNonNullPreloadingStrategy(preloadingStrategy));
        return NetworkImpl.create(storeClient, storeClient.getNetwork(uuid, Resource.INITIAL_VARIANT_NUM)
                .orElseThrow(() -> new PowsyblException("Network '" + uuid + "' not found")));
    }

    public void deleteNetwork(UUID uuid) {
        new RestNetworkStoreClient(restClient).deleteNetwork(uuid);
    }

    public void deleteAllNetworks() {
        RestNetworkStoreClient restStoreClient = new RestNetworkStoreClient(restClient);
        getNetworkIds().forEach((key, value) -> restStoreClient.deleteNetwork(key));
    }

    public void createNetwork(UUID networkId, UUID parentNetworkId, int targetVariantNum) {
        RestNetworkStoreClient restStoreClient = new RestNetworkStoreClient(restClient);
        List<Resource<NetworkAttributes>> parentNetworkAttributes = new ArrayList<>();
        IntStream.range(0, targetVariantNum).forEach(i -> {
            Resource<NetworkAttributes> parentNetworkAttribute = restStoreClient.getNetwork(parentNetworkId, i).orElse(null);
            if (parentNetworkAttribute != null) {
                parentNetworkAttributes.add(parentNetworkAttribute);
            } else {
                throw new PowsyblException("Cannot retrieve parent network attributes : " + parentNetworkId);
            }
        });
        restStoreClient.cloneNetwork(networkId, parentNetworkAttributes);
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

    public void cloneVariant(UUID networkUuid, String sourceVariantId, String targetVariantId) {
        cloneVariant(networkUuid, sourceVariantId, targetVariantId, false);
    }

    public void cloneVariant(UUID networkUuid, String sourceVariantId, String targetVariantId, boolean mayOverwrite) {
        NetworkStoreClient client = new RestNetworkStoreClient(restClient);
        client.cloneNetwork(networkUuid, sourceVariantId, targetVariantId, mayOverwrite);
    }
}
