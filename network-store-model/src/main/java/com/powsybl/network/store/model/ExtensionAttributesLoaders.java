package com.powsybl.network.store.model;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;

public final class ExtensionAttributesLoaders {
    private static final ServiceLoaderCache<ExtensionAttributesLoader> EXTENSION_ATTRIBUTES_LOADER = new ServiceLoaderCache<>(ExtensionAttributesLoader.class);

    private ExtensionAttributesLoaders() {
    }

    public static List<ExtensionAttributesLoader> getExtensionAttributesLoaders() {
        return EXTENSION_ATTRIBUTES_LOADER.getServices();
    }

}
