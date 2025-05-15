/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class GeneratorTest {

    @Test
    void testRegulationWhenRegulatedElementIsRemoved() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        // initialization
        Generator generator = network.getGenerator("GH3");
        Load load = network.getLoad("LD1");
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        assertEquals(400, generator.getTargetV());

        // set the generator's regulation on the load terminal (both equipments are not on the same voltage level/bus)
        generator.setRegulatingTerminal(load.getTerminal());
        generator.setTargetV(225);
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(load.getTerminal(), generator.getRegulatingTerminal());
        assertEquals(225, generator.getTargetV());

        // remove the load
        network.getLoad("LD1").remove();
        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        assertFalse(generator.isVoltageRegulatorOn());
    }

    @Test
    void testRegulationWhenRegulatedElementOnSameBusIsRemoved() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        // initialization
        Generator generator = network.getGenerator("GH3");
        Load load = network.getLoad("LD2");
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());

        // set the generator's regulation on the load terminal (both equipments are on the same voltage level/bus)
        generator.setRegulatingTerminal(load.getTerminal());
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(load.getTerminal(), generator.getRegulatingTerminal());

        // remove the load
        network.getLoad("LD2").remove();
        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        assertTrue(generator.isVoltageRegulatorOn());
    }

    @Test
    void testIdBeforeRemovalMultipleVariants() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        Generator generator = network.getGenerator("GH3");
        assertNotNull(generator);
        generator.remove();
        assertEquals("GH3", generator.getId());

        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "variant1");
        variantManager.setWorkingVariant("variant1");
        assertEquals("GH3", generator.getId());
    }
}
