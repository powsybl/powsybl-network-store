/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class LoaderTest {
    @Test
    public void testLoaderNotFound() {
        PowsyblException exception = assertThrows(PowsyblException.class, () -> ExtensionLoaders.findLoaderByName("unknown"));
        assertEquals("ExtensionLoader not found", exception.getMessage());
    }

    @Test
    public void testLoaderExists() {
        assertTrue(ExtensionLoaders.loaderExists("loader"));
        assertFalse(ExtensionLoaders.loaderExists("unknown"));
        assertTrue(ExtensionLoaders.loaderExists(Extension.class));
        assertFalse(ExtensionLoaders.loaderExists(Object.class));
    }

    @Test
    public void testDuplicatedLoader() {
        PowsyblException exception = assertThrows(PowsyblException.class, () -> ExtensionLoaders.findLoaderByName("loader"));
        assertEquals("Multiple ExtensionLoaders configuration providers found", exception.getMessage());
    }
}
