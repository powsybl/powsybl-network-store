/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
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
        assertTrue(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        assertEquals(400, generator.getRegulatingTargetV());

        // set the generator's regulation on the load terminal (both equipments are not on the same voltage level/bus)
        generator.getVoltageRegulation().setTerminal(load.getTerminal(), 225);
        assertTrue(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertEquals(load.getTerminal(), generator.getRegulatingTerminal());
        assertEquals(225, generator.getRegulatingTargetV());

        // remove the load
        network.getLoad("LD1").remove();
        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        assertFalse(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
    }

    @Test
    void testRegulationWhenRegulatedElementOnSameBusIsRemoved() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        // initialization
        Generator generator = network.getGenerator("GH3");
        Load load = network.getLoad("LD2");
        assertTrue(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());

        // set the generator's regulation on the load terminal (both equipments are on the same voltage level/bus)
        generator.getVoltageRegulation().setTerminal(load.getTerminal(), generator.getRegulatingTargetV());
        assertTrue(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertEquals(load.getTerminal(), generator.getRegulatingTerminal());

        // remove the load
        network.getLoad("LD2").remove();
        // since powsybl-core 7.4.0, when the regulated equipment is on the same bus, the regulation
        // is relocated on the local terminal and stays active
        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        assertTrue(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
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
        assertEquals("Generator 'GH3': invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)",
                assertThrows(ValidationException.class, () -> generator.setLocalTargetV(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        generator.setLocalTargetV(Double.NaN);
    }

    @Test
    void updateWithInvalidTargetQ() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Generator generator = network.getGenerator("GH3");
        generator.getVoltageRegulation().setRegulating(false);
        assertEquals("Generator 'GH3': invalid value (NaN) for localTargetQ (voltageRegulation is set with regulating false)",
                assertThrows(ValidationException.class, () -> generator.setLocalTargetQ(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        generator.setLocalTargetQ(Double.NaN);
    }

    @Test
    void undefinedVoltageRegulatorOnWithEquipmentValidationLevel() {
        Network network = Network.create("test", "test");
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        Generator generator = voltageLevel.newGenerator()
                .setId("GEN")
                .setMaxP(Double.MAX_VALUE)
                .setMinP(-Double.MAX_VALUE)
                .setTargetP(30.0)
                .setLocalTargetQ(40.0)
                .setNode(0)
                .add();

        assertFalse(generator.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
    }

    //TODO: to remove when deprecated voltage regulation methods are removed.
    @Test
    void legacyVoltageRegulatorOnSetter() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Generator generator = network.getGenerator("GH3");
        generator.setVoltageRegulatorOn(false);
        assertFalse(generator.isVoltageRegulatorOn());
        generator.setVoltageRegulatorOn(true);
        assertTrue(generator.isVoltageRegulatorOn());
    }

    //TODO: to remove when deprecated voltage regulation methods are removed.
    @Test
    void legacyVoltageRegulatorOnSetterWithoutVoltageRegulation() {
        Network network = Network.create("test", "test");
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        GeneratorAdder adder = voltageLevel.newGenerator()
                .setId("GEN")
                .setMaxP(Double.MAX_VALUE)
                .setMinP(-Double.MAX_VALUE)
                .setTargetP(30.0)
                .setLocalTargetV(400.0)
                .setLocalTargetQ(40.0)
                .setNode(0);
        assertEquals(40.0, adder.getLocalTargetQ());
        Generator generator = adder.add();
        assertFalse(generator.isVoltageRegulatorOn());
        generator.setVoltageRegulatorOn(true);
        assertTrue(generator.isVoltageRegulatorOn());
        generator.setVoltageRegulatorOn(false);
        assertFalse(generator.isVoltageRegulatorOn());
    }

    //TODO: to remove when deprecated voltage regulation methods are removed.
    @Test
    void legacySetRegulatingTerminalWithoutVoltageRegulation() {
        Network network = Network.create("test", "test");
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        Generator generator = voltageLevel.newGenerator()
                .setId("GEN")
                .setMaxP(Double.MAX_VALUE)
                .setMinP(-Double.MAX_VALUE)
                .setTargetP(30.0)
                .setLocalTargetV(400.0)
                .setLocalTargetQ(40.0)
                .setNode(0)
                .add();
        voltageLevel.newLoad()
                .setId("LD1")
                .setP0(10.0)
                .setQ0(5.0)
                .setNode(1)
                .add();
        Load load = network.getLoad("LD1");
        generator.setRegulatingTerminal(load.getTerminal());
        assertEquals(load.getTerminal(), generator.getRegulatingTerminal());
        assertFalse(generator.isVoltageRegulatorOn());
    }

    //TODO: to remove when deprecated voltage regulation methods are removed.
    @Test
    void legacySetTargetV() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Generator generator = network.getGenerator("GH3");
        assertEquals(400, generator.getTargetV());
        generator.setRegulatingTerminal(generator.getTerminal());
        generator.setTargetV(405);
        assertEquals(405, generator.getTargetV());
    }

    //TODO: to remove when deprecated voltage regulation methods are removed.
    @Test
    void legacySetTargetVWithEquivalentLocalTargetV() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Generator generator = network.getGenerator("GH3");
        generator.setRegulatingTerminal(generator.getTerminal());
        generator.setTargetV(405, 404);
        assertEquals(405, generator.getTargetV());
        assertEquals(404, generator.getLocalTargetV());
        assertEquals(404, generator.getEquivalentLocalTargetV());
    }

    //TODO: to remove when deprecated voltage regulation methods are removed.
    @Test
    void legacySetTargetVWithEquivalentLocalTargetVWithoutRegulatingTerminal() {
        Network network = Network.create("test", "test");
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        Generator generator = voltageLevel.newGenerator()
                .setId("GEN")
                .setMaxP(Double.MAX_VALUE)
                .setMinP(-Double.MAX_VALUE)
                .setTargetP(30.0)
                .setLocalTargetV(400.0)
                .setLocalTargetQ(40.0)
                .setNode(0)
                .add();
        generator.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(false)
                .build();
        generator.setTargetV(401, 402);
        assertEquals(401, generator.getLocalTargetV());
        assertEquals(401, generator.getTargetV());
    }

    //TODO: to remove when deprecated voltage regulation methods are removed.
    @Test
    void legacySetTargetVWithEquivalentLocalTargetVWithoutVoltageRegulation() {
        Network network = Network.create("test", "test");
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        Generator generator = voltageLevel.newGenerator()
                .setId("GEN")
                .setMaxP(Double.MAX_VALUE)
                .setMinP(-Double.MAX_VALUE)
                .setTargetP(30.0)
                .setLocalTargetV(400.0)
                .setLocalTargetQ(40.0)
                .setNode(0)
                .add();
        generator.setTargetV(401, 402);
        assertEquals(401, generator.getVoltageRegulation().getTargetValue());
        assertEquals(402, generator.getLocalTargetV());
        assertFalse(generator.isVoltageRegulatorOn());
    }

}
