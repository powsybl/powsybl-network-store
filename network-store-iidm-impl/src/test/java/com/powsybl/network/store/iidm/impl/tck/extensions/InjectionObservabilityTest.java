/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractInjectionObservabilityTest;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class InjectionObservabilityTest extends AbstractInjectionObservabilityTest {
    // TODO : add this test method later in AbstractInjectionObservabilityTest
    @Test
    void testOnBusbarSections() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        BusbarSection bbs = network.getBusbarSection("S1VL1_BBS");
        assertNotNull(bbs);
        bbs.newExtension(InjectionObservabilityAdder.class)
            .withObservable(true)
            .withStandardDeviationP(0.02d)
            .withRedundantP(true)
            .withStandardDeviationQ(0.5d)
            .withRedundantQ(true)
            .withStandardDeviationV(0.0d)
            .withRedundantV(true)
            .add();
        InjectionObservability<BusbarSection> injectionObservability = bbs.getExtension(InjectionObservability.class);
        assertEquals("injectionObservability", injectionObservability.getName());
        assertEquals("S1VL1_BBS", injectionObservability.getExtendable().getId());

        assertTrue(injectionObservability.isObservable());
        injectionObservability.setObservable(false);
        assertFalse(injectionObservability.isObservable());

        assertEquals(0.02d, injectionObservability.getQualityP().orElseThrow().getStandardDeviation(), 0d);
        injectionObservability.getQualityP().orElseThrow().setStandardDeviation(0.03d);
        assertEquals(0.03d, injectionObservability.getQualityP().orElseThrow().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getQualityP().orElseThrow().isRedundant().isPresent());
        assertTrue(injectionObservability.getQualityP().orElseThrow().isRedundant().get());
        injectionObservability.getQualityP().orElseThrow().setRedundant(false);
        assertTrue(injectionObservability.getQualityP().orElseThrow().isRedundant().isPresent());
        assertFalse(injectionObservability.getQualityP().orElseThrow().isRedundant().get());

        assertEquals(0.5d, injectionObservability.getQualityQ().orElseThrow().getStandardDeviation(), 0d);
        injectionObservability.getQualityQ().orElseThrow().setStandardDeviation(0.6d);
        assertEquals(0.6d, injectionObservability.getQualityQ().orElseThrow().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getQualityQ().orElseThrow().isRedundant().isPresent());
        assertTrue(injectionObservability.getQualityQ().orElseThrow().isRedundant().get());
        injectionObservability.getQualityQ().orElseThrow().setRedundant(false);
        assertTrue(injectionObservability.getQualityQ().orElseThrow().isRedundant().isPresent());
        assertFalse(injectionObservability.getQualityQ().orElseThrow().isRedundant().get());

        assertEquals(0.0d, injectionObservability.getQualityV().orElseThrow().getStandardDeviation(), 0d);
        injectionObservability.getQualityV().orElseThrow().setStandardDeviation(0.01d);
        assertEquals(0.01d, injectionObservability.getQualityV().orElseThrow().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getQualityV().orElseThrow().isRedundant().isPresent());
        assertTrue(injectionObservability.getQualityV().orElseThrow().isRedundant().get());
        injectionObservability.getQualityV().orElseThrow().setRedundant(false);
        assertTrue(injectionObservability.getQualityV().orElseThrow().isRedundant().isPresent());
        assertFalse(injectionObservability.getQualityV().orElseThrow().isRedundant().get());
    }
}
