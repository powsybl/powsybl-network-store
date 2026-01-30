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

    private static final PreloadingStrategy DEFAULT_PRELOADING_STRATEGY = PreloadingStrategy.NONE;

    private static final boolean DEFAULT_USE_CALCULATEDBUS_FICTITIOUSP0Q0 = true;

    private String baseUrl;

    private PreloadingStrategy preloadingStrategy = DEFAULT_PRELOADING_STRATEGY;

    // For now this one is
    // probably only temporary until we fix the underlying
    // performance issue that forces us to have it
    private boolean useCalculatedBusFictitiousP0Q0 = DEFAULT_USE_CALCULATEDBUS_FICTITIOUSP0Q0;

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
        PreloadingStrategy preloadingStrategy = moduleConfig.flatMap(mc -> mc.getOptionalEnumProperty("preloading-strategy", PreloadingStrategy.class))
                .orElse(DEFAULT_PRELOADING_STRATEGY);
        boolean useCalculatedBusFictitiousP0Q0 = moduleConfig.flatMap(mc -> mc.getOptionalBooleanProperty("use-calculatedbus-fictitiousP0Q0"))
                .orElse(DEFAULT_USE_CALCULATEDBUS_FICTITIOUSP0Q0);
        return new NetworkStoreConfig(baseUrl)
                .setPreloadingStrategy(preloadingStrategy)
                .setUseCalculatedBusFictitiousP0Q0(useCalculatedBusFictitiousP0Q0);
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

    public boolean isUseCalculatedBusFictitiousP0Q0() {
        return useCalculatedBusFictitiousP0Q0;
    }

    public NetworkStoreConfig setUseCalculatedBusFictitiousP0Q0(boolean useCalculatedBusFictitiousP0Q0) {
        this.useCalculatedBusFictitiousP0Q0 = useCalculatedBusFictitiousP0Q0;
        return this;
    }
}
