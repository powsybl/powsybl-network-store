/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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
    public void testDuplicatedLoader() {
        PowsyblException exception = assertThrows(PowsyblException.class, () -> ExtensionLoaders.findLoaderByName("loader"));
        assertEquals("Multiple ExtensionLoaders configuration providers found", exception.getMessage());
    }
}
