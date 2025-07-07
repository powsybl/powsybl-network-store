/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { PreloadingStrategyConfiguration.class })
@ActiveProfiles("collection")
public class PreloadingStrategyConfigurationCollectionTest {

    @Autowired
    private PreloadingStrategyConfiguration preloadingStrategyConfiguration;

    @Test
    public void testPreloadingStrategyConfiguration() {
        assertNotNull(preloadingStrategyConfiguration.preloadingStrategy());
        assertTrue(preloadingStrategyConfiguration.preloadingStrategy().isCollection());
    }
}
