/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
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
        assertFalse(generator.isVoltageRegulatorOn());
    }

    @Test
    void testIdBeforeRemovalMultipleVariants() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "variant1");
        variantManager.setWorkingVariant("variant1");

        // Remove generator in variant1
        Generator generator = network.getGenerator("GH3");
        assertNotNull(generator);
        generator.remove();
        assertNull(network.getGenerator("GH3"));
        assertEquals("GH3", generator.getId());

        // Clone variant1 and switch to variant2 with removed generator
        variantManager.cloneVariant("variant1", "variant2");
        variantManager.setWorkingVariant("variant2");
        assertNull(network.getGenerator("GH3"));
        assertEquals("GH3", generator.getId());

        // Switch to initial variant with existing generator
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertNotNull(network.getGenerator("GH3"));
        assertEquals("GH3", generator.getId());

        // Switch again to variant1 with removed generator
        variantManager.setWorkingVariant("variant1");
        assertNull(network.getGenerator("GH3"));
        assertEquals("GH3", generator.getId());
    }

    @Test
    void removeExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Generator generator = network.getGenerator("GH3");
        generator.newExtension(CoordinatedReactiveControlAdder.class).withQPercent(10.0).add();
        assertTrue(generator.removeExtension(CoordinatedReactiveControl.class));
        assertNull(generator.getExtension(CoordinatedReactiveControl.class));
        assertFalse(generator.removeExtension(CoordinatedReactiveControl.class));
        generator.newExtension(GeneratorShortCircuitAdder.class).withDirectSubtransX(1).add();
        assertTrue(generator.removeExtension(GeneratorShortCircuit.class));
        assertNull(generator.getExtension(GeneratorShortCircuit.class));
        assertFalse(generator.removeExtension(GeneratorShortCircuit.class));
        generator.newExtension(GeneratorEntsoeCategoryAdder.class).withCode(1).add();
        assertTrue(generator.removeExtension(GeneratorEntsoeCategory.class));
        assertNull(generator.getExtension(GeneratorEntsoeCategory.class));
        assertFalse(generator.removeExtension(GeneratorEntsoeCategory.class));
        generator.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(generator.removeExtension(ConnectablePosition.class));
        assertNull(generator.getExtension(ConnectablePosition.class));
        assertFalse(generator.removeExtension(ConnectablePosition.class));
    }

    @Test
    void updateWithInvalidTargetP() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Generator generator = network.getGenerator("GH3");
        assertEquals("Generator 'GH3': invalid value (NaN) for active power setpoint",
                assertThrows(ValidationException.class, () -> generator.setTargetP(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        generator.setTargetP(Double.NaN);
    }

    @Test
    void updateWithInvalidTargetV() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Generator generator = network.getGenerator("GH3");
        assertEquals("Generator 'GH3': invalid value (NaN) for voltage setpoint (voltage regulator is on)",
                assertThrows(ValidationException.class, () -> generator.setTargetV(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        generator.setTargetV(Double.NaN);
    }

    @Test
    void updateWithInvalidTargetQ() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Generator generator = network.getGenerator("GH3");
        generator.setVoltageRegulatorOn(false);
        assertEquals("Generator 'GH3': invalid value (NaN) for reactive power setpoint (voltage regulator is off)",
                assertThrows(ValidationException.class, () -> generator.setTargetQ(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        generator.setTargetQ(Double.NaN);
    }
}
