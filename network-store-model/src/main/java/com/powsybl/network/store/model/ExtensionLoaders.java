package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.util.ServiceLoaderCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

public final class ExtensionLoaders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionLoaders.class);

    private static final ServiceLoaderCache<ExtensionLoader> EXTENSION_LOADERS = new ServiceLoaderCache<>(ExtensionLoader.class);

    private ExtensionLoaders() {
    }

    public static <O extends Extendable<O>, E extends Extension<O>> ExtensionLoader findLoader(Class<? super E> type, String name) {
        return findLoader(s -> s.getName() != null
                && name.equals(s.getName())
                && type.isAssignableFrom(s.getType()), type.getSimpleName());
    }

    public static ExtensionLoader findLoader(String name) {
        return findLoader(s -> s.getName() != null && name.equals(s.getName()), name);
    }

    private static ExtensionLoader findLoader(
            Predicate<ExtensionLoader> typeFilter, String typeName) {

        List<ExtensionLoader> loaders = EXTENSION_LOADERS.getServices().stream()
                .filter(typeFilter)
                .toList();

        if (loaders.isEmpty()) {
            LOGGER.error(
                    "ExtensionLoader not found: {}",
                    typeName);
            throw new PowsyblException("ExtensionLoader not found");
        }

        if (loaders.size() > 1) {
            LOGGER.error(
                    "Multiple ExtensionLoader found for type {}: {}",
                    typeName, loaders);
            throw new PowsyblException(
                    "Multiple ExtensionLoaders configuration providers found");
        }
        return loaders.get(0);
    }
}
