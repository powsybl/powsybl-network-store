/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class NetworkStoreConfig {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080/";

    private static final PreloadingStrategy DEFAULT_PRELOADING_STRATEGY = null;

    private String baseUrl;

    private PreloadingStrategy preloadingStrategy = DEFAULT_PRELOADING_STRATEGY;

    public NetworkStoreConfig(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl);
    }

    public NetworkStoreConfig() {
        this(DEFAULT_BASE_URL);
    }

    public static NetworkStoreConfig load() {
        Optional<ModuleConfig> moduleConfig = PlatformConfig.defaultConfig()
                .getOptionalModuleConfig("network-store");
        String baseUrl = moduleConfig.flatMap(mc -> mc.getOptionalStringProperty("base-url"))
                .orElse(DEFAULT_BASE_URL);
        PreloadingStrategy preloadingStrategy = moduleConfig
            .flatMap(mc -> mc.getOptionalStringProperty("preloading-strategy"))
            .map(PreloadingStrategy::fromString)
            .orElse(DEFAULT_PRELOADING_STRATEGY);
        return new NetworkStoreConfig(baseUrl)
                .setPreloadingStrategy(preloadingStrategy);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public NetworkStoreConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public PreloadingStrategy getPreloadingStrategy() {
        return preloadingStrategy;
    }

    public NetworkStoreConfig setPreloadingStrategy(PreloadingStrategy preloadingStrategy) {
        this.preloadingStrategy = Objects.requireNonNull(preloadingStrategy);
        return this;
    }
}
