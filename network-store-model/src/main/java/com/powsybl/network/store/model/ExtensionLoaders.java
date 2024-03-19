package com.powsybl.network.store.model;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;

public final class ExtensionLoaders {
    private static final ServiceLoaderCache<ExtensionLoader> EXTENSION_LOADER = new ServiceLoaderCache<>(ExtensionLoader.class);

    private ExtensionLoaders() {
    }

    public static List<ExtensionLoader> getExtensionLoaders() {
        return EXTENSION_LOADER.getServices();
    }
}
