/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
public class PreloadingStrategyTest {

    @Test
    public void testFromStringCreationWithCollection() {
        var preloadingStrategy = PreloadingStrategy.fromString("COLLECTION");
        assertNotNull(preloadingStrategy);
        assertTrue(preloadingStrategy.isCollection());
    }

    @Test
    public void testFromStringCreationWithNone() {
        var preloadingStrategy = PreloadingStrategy.fromString("NONE");
        assertNull(preloadingStrategy);
    }

    @Test
    public void testFromStringCreationWithAllCollectionsNeededForBusView() {
        var preloadingStrategy = PreloadingStrategy.fromString("ALL_COLLECTIONS_NEEDED_FOR_BUS_VIEW");
        assertNotNull(preloadingStrategy);
        assertFalse(preloadingStrategy.isCollection());
        assertEquals(
            PreloadingStrategy.allCollectionsNeededForBusView().getResourceTypes(),
            preloadingStrategy.getResourceTypes()
        );
    }

    @Test
    public void testFromStringCreationFailsWithUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> PreloadingStrategy.fromString("FAILS"));
    }
}
