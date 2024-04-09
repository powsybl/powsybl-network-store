/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public final class ExtensionLoaders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionLoaders.class);

    private static final ServiceLoaderCache<ExtensionLoader> EXTENSION_LOADERS = new ServiceLoaderCache<>(ExtensionLoader.class);

    private ExtensionLoaders() {
    }

    public static ExtensionLoader findLoader(String name) {
        return findLoader(s -> s.getName() != null && name.equals(s.getName()), name);
    }

    public static <K extends ExtensionAttributes> ExtensionLoader findLoaderByAttributes(Class<? super K> type) {
        return findLoader(s -> type.isAssignableFrom(s.getAttributesType()), type.getSimpleName());
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
