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
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.network.store.client.util.ExecutorUtil;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.iidm.impl.util.TriFunction;
import com.powsybl.network.store.model.*;
import com.powsybl.tools.Version;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Service
public class NetworkStoreService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkStoreService.class);

    private final RestClient restClient;

    private final PreloadingStrategy defaultPreloadingStrategy;

    private final TriFunction<RestClient, PreloadingStrategy, ExecutorService, NetworkStoreClient> decorator;

    private final ExecutorService executorService = Executors.newFixedThreadPool(ResourceType.values().length);

    public NetworkStoreService(String baseUri) {
        this(baseUri, PreloadingStrategy.NONE);
    }

    @Autowired
    public NetworkStoreService(@Value("${powsybl.services.network-store-server.base-uri:http://network-store-server/}") String baseUri,
                               @Value("${powsybl.services.network-store-server.preloading-strategy:NONE}") PreloadingStrategy defaultPreloadingStrategy) {
        this(new RestClientImpl(baseUri), defaultPreloadingStrategy);
    }

    public NetworkStoreService(RestClient restClient, PreloadingStrategy defaultPreloadingStrategy) {
        this(restClient, defaultPreloadingStrategy, NetworkStoreService::createStoreClient);
    }

    NetworkStoreService(RestClient restClient, PreloadingStrategy defaultPreloadingStrategy,
                        TriFunction<RestClient, PreloadingStrategy, ExecutorService, NetworkStoreClient> decorator) {
        this.restClient = Objects.requireNonNull(restClient);
        this.defaultPreloadingStrategy = Objects.requireNonNull(defaultPreloadingStrategy);
        this.decorator = Objects.requireNonNull(decorator);
    }

    public NetworkStoreService(String baseUri, PreloadingStrategy defaultPreloadingStrategy,
                               TriFunction<RestClient, PreloadingStrategy, ExecutorService, NetworkStoreClient> decorator) {
        this(new RestClientImpl(baseUri), defaultPreloadingStrategy, decorator);
    }

    public static NetworkStoreService create(NetworkStoreConfig config) {
        Objects.requireNonNull(config);
        return new NetworkStoreService(config.getBaseUrl(), config.getPreloadingStrategy());
    }

    private PreloadingStrategy getNonNullPreloadingStrategy(PreloadingStrategy preloadingStrategy) {
        return preloadingStrategy != null ? preloadingStrategy : defaultPreloadingStrategy;
    }

    private static NetworkStoreClient createStoreClient(RestClient restClient, PreloadingStrategy preloadingStrategy,
                                                        ExecutorService executorService) {
        Objects.requireNonNull(preloadingStrategy);
        LOGGER.info("Preloading strategy: {}", preloadingStrategy);
        var cachedClient = new CachedNetworkStoreClient(new BufferedNetworkStoreClient(new RestNetworkStoreClient(restClient), executorService));
        return switch (preloadingStrategy) {
            case NONE -> cachedClient;
            case COLLECTION -> new PreloadingNetworkStoreClient(cachedClient, false, executorService);
            case ALL_COLLECTIONS_NEEDED_FOR_BUS_VIEW ->
                new PreloadingNetworkStoreClient(cachedClient, true, executorService);
        };
    }

    public NetworkFactory getNetworkFactory() {
        return getNetworkFactory(null);
    }

    public NetworkFactory getNetworkFactory(PreloadingStrategy preloadingStrategy) {
        return new NetworkFactoryImpl(() -> decorator.apply(restClient, getNonNullPreloadingStrategy(preloadingStrategy), executorService));
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

    public Network importNetwork(Path file, ReportNode report) {
        return importNetwork(DataSource.fromPath(file), null, LocalComputationManager.getDefault(), null, report);
    }

    public Network importNetwork(Path file, Properties parameters) {
        return importNetwork(file, null, parameters);
    }

    public Network importNetwork(Path file, PreloadingStrategy preloadingStrategy, Properties parameters) {
        DataSource dataSource = DataSource.fromPath(file);
        return importNetwork(dataSource, preloadingStrategy, LocalComputationManager.getDefault(), parameters);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource) {
        return importNetwork(dataSource, null, LocalComputationManager.getDefault(), null);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, ReportNode reporter) {
        return importNetwork(dataSource, reporter, true);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, ReportNode reporter, boolean flush) {
        return importNetwork(dataSource, null, LocalComputationManager.getDefault(), null, reporter, flush);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, ReportNode reporter, Properties parameters, boolean flush) {
        return importNetwork(dataSource, null, LocalComputationManager.getDefault(), parameters, reporter, flush);
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
        Importer importer = Importer.find(dataSource, computationManager);
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
                                 ComputationManager computationManager, Properties parameters, ReportNode reporter) {
        return importNetwork(dataSource, preloadingStrategy, computationManager, parameters, reporter, true);
    }

    public Network importNetwork(ReadOnlyDataSource dataSource, PreloadingStrategy preloadingStrategy,
                                 ComputationManager computationManager, Properties parameters, ReportNode reporter, boolean flush) {
        Importer importer = Importer.find(dataSource, computationManager);
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
        NetworkStoreClient storeClient = decorator.apply(restClient, getNonNullPreloadingStrategy(preloadingStrategy), executorService);
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

    public List<VariantInfos> getVariantsInfos(UUID networkId) {
        return new RestNetworkStoreClient(restClient).getVariantsInfos(networkId);
    }

    public Network cloneNetwork(UUID sourceNetworkId, List<String> targetVariantIds) {
        RestNetworkStoreClient restStoreClient = new RestNetworkStoreClient(restClient);
        UUID targetNetworkUuid = UUID.randomUUID();
        restStoreClient.cloneNetwork(targetNetworkUuid, sourceNetworkId, targetVariantIds);
        return getNetwork(targetNetworkUuid);
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
        NetworkImpl networkImpl = getNetworkImpl(network);
        networkImpl.getIndex().getStoreClient().flush(networkImpl.getUuid());
    }

    public void setCloneStrategy(Network network, CloneStrategy cloneStrategy) {
        NetworkImpl networkImpl = getNetworkImpl(network);
        networkImpl.getResource().getAttributes().setCloneStrategy(cloneStrategy);
        NetworkStoreClient client = new RestNetworkStoreClient(restClient);
        client.updateNetworks(List.of(networkImpl.getResource()), null);
    }

    @PostConstruct
    public void start() {
        LOGGER.info(Version.getTableString());
    }

    @Override
    @PreDestroy
    public void close() {
        ExecutorUtil.shutdownAndAwaitTermination(executorService);
    }

    public void cloneVariant(UUID networkUuid, String sourceVariantId, String targetVariantId, CloneStrategy cloneStrategy) {
        cloneVariant(networkUuid, sourceVariantId, targetVariantId, false, cloneStrategy);
    }

    public void cloneVariant(UUID networkUuid, String sourceVariantId, String targetVariantId, boolean mayOverwrite, CloneStrategy cloneStrategy) {
        NetworkStoreClient client = new RestNetworkStoreClient(restClient);
        client.cloneNetwork(networkUuid, sourceVariantId, targetVariantId, mayOverwrite, cloneStrategy);
    }
}
