/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModel;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class ShuntCompensatorTest {
    @Test
    void removeExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");
        shuntCompensator.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(shuntCompensator.removeExtension(ConnectablePosition.class));
        assertNull(shuntCompensator.getExtension(ConnectablePosition.class));
        assertFalse(shuntCompensator.removeExtension(ConnectablePosition.class));
    }

    @Test
    void testLinearModelProperties() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ShuntCompensator shunt = network.getShuntCompensator("SHUNT");

        ShuntCompensatorLinearModel linearModel = (ShuntCompensatorLinearModel) shunt.getModel();

        assertFalse(linearModel.hasProperty());
        assertTrue(linearModel.getPropertyNames().isEmpty());

        linearModel.setProperty("shuntProp1", "shuntValue1");
        linearModel.setProperty("shuntProp2", "shuntValue2");

        assertTrue(linearModel.hasProperty());
        assertTrue(linearModel.hasProperty("shuntProp1"));
        assertTrue(linearModel.hasProperty("shuntProp2"));
        assertFalse(linearModel.hasProperty("nonExistentShuntProp"));

        assertEquals("shuntValue1", linearModel.getProperty("shuntProp1"));
        assertEquals("shuntValue2", linearModel.getProperty("shuntProp2"));
        assertNull(linearModel.getProperty("nonExistentShuntProp"));

        assertEquals("shuntValue1", linearModel.getProperty("shuntProp1", "defaultShuntValue"));
        assertEquals("defaultShuntValue", linearModel.getProperty("nonExistentShuntProp", "defaultShuntValue"));

        String oldShuntValue = linearModel.setProperty("shuntProp3", "shuntValue3");
        assertNull(oldShuntValue);
        assertEquals("shuntValue3", linearModel.getProperty("shuntProp3"));

        String replacedShuntValue = linearModel.setProperty("shuntProp1", "newShuntValue1");
        assertEquals("shuntValue1", replacedShuntValue);
        assertEquals("newShuntValue1", linearModel.getProperty("shuntProp1"));

        Set<String> propertyNames = linearModel.getPropertyNames();
        assertEquals(3, propertyNames.size());
        assertTrue(propertyNames.contains("shuntProp1"));
        assertTrue(propertyNames.contains("shuntProp2"));
        assertTrue(propertyNames.contains("shuntProp3"));

        assertTrue(linearModel.removeProperty("shuntProp3"));
        assertFalse(linearModel.hasProperty("shuntProp3"));
        assertFalse(linearModel.removeProperty("nonExistentShuntProp"));

        ShuntCompensatorLinearModel retrievedModel = (ShuntCompensatorLinearModel) network.getShuntCompensator("SHUNT").getModel();
        assertEquals("newShuntValue1", retrievedModel.getProperty("shuntProp1"));
        assertEquals("shuntValue2", retrievedModel.getProperty("shuntProp2"));
        assertFalse(retrievedModel.hasProperty("shuntProp3"));
    }

    @Test
    void testNonLinearModelProperties() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageLevel voltageLevel = network.getVoltageLevel("S1VL1");
        ShuntCompensator shunt = voltageLevel.newShuntCompensator()
                .setId("SHUNT_NON_LINEAR")
                .setNode(10)
                .setSectionCount(1)
                .newNonLinearModel()
                .beginSection()
                .setB(1e-5)
                .setG(1e-6)
                .endSection()
                .beginSection()
                .setB(2e-5)
                .setG(2e-6)
                .endSection()
                .add()
                .add();

        ShuntCompensatorNonLinearModel nonLinearModel = (ShuntCompensatorNonLinearModel) shunt.getModel();

        assertFalse(nonLinearModel.hasProperty());
        assertTrue(nonLinearModel.getPropertyNames().isEmpty());

        nonLinearModel.setProperty("modelProp1", "modelValue1");
        nonLinearModel.setProperty("modelProp2", "modelValue2");

        assertTrue(nonLinearModel.hasProperty());
        assertTrue(nonLinearModel.hasProperty("modelProp1"));
        assertTrue(nonLinearModel.hasProperty("modelProp2"));
        assertFalse(nonLinearModel.hasProperty("nonExistentModelProp"));

        assertEquals("modelValue1", nonLinearModel.getProperty("modelProp1"));
        assertEquals("modelValue2", nonLinearModel.getProperty("modelProp2"));
        assertNull(nonLinearModel.getProperty("nonExistentModelProp"));

        assertEquals("modelValue1", nonLinearModel.getProperty("modelProp1", "defaultModelValue"));
        assertEquals("defaultModelValue", nonLinearModel.getProperty("nonExistentModelProp", "defaultModelValue"));

        String oldModelValue = nonLinearModel.setProperty("modelProp3", "modelValue3");
        assertNull(oldModelValue);
        assertEquals("modelValue3", nonLinearModel.getProperty("modelProp3"));

        String replacedModelValue = nonLinearModel.setProperty("modelProp1", "newModelValue1");
        assertEquals("modelValue1", replacedModelValue);
        assertEquals("newModelValue1", nonLinearModel.getProperty("modelProp1"));

        Set<String> modelPropertyNames = nonLinearModel.getPropertyNames();
        assertEquals(3, modelPropertyNames.size());
        assertTrue(modelPropertyNames.contains("modelProp1"));
        assertTrue(modelPropertyNames.contains("modelProp2"));
        assertTrue(modelPropertyNames.contains("modelProp3"));

        assertTrue(nonLinearModel.removeProperty("modelProp3"));
        assertFalse(nonLinearModel.hasProperty("modelProp3"));
        assertFalse(nonLinearModel.removeProperty("nonExistentModelProp"));

        ShuntCompensatorNonLinearModel.Section section = nonLinearModel.getAllSections().get(0);

        assertFalse(section.hasProperty());
        assertTrue(section.getPropertyNames().isEmpty());

        section.setProperty("sectionProp1", "sectionValue1");
        section.setProperty("sectionProp2", "sectionValue2");

        assertTrue(section.hasProperty());
        assertTrue(section.hasProperty("sectionProp1"));
        assertTrue(section.hasProperty("sectionProp2"));
        assertFalse(section.hasProperty("nonExistentSectionProp"));

        assertEquals("sectionValue1", section.getProperty("sectionProp1"));
        assertEquals("sectionValue2", section.getProperty("sectionProp2"));
        assertNull(section.getProperty("nonExistentSectionProp"));

        assertEquals("sectionValue1", section.getProperty("sectionProp1", "defaultSectionValue"));
        assertEquals("defaultSectionValue", section.getProperty("nonExistentSectionProp", "defaultSectionValue"));

        String oldSectionValue = section.setProperty("sectionProp3", "sectionValue3");
        assertNull(oldSectionValue);
        assertEquals("sectionValue3", section.getProperty("sectionProp3"));

        String replacedSectionValue = section.setProperty("sectionProp1", "newSectionValue1");
        assertEquals("sectionValue1", replacedSectionValue);
        assertEquals("newSectionValue1", section.getProperty("sectionProp1"));

        Set<String> sectionPropertyNames = section.getPropertyNames();
        assertEquals(3, sectionPropertyNames.size());
        assertTrue(sectionPropertyNames.contains("sectionProp1"));
        assertTrue(sectionPropertyNames.contains("sectionProp2"));
        assertTrue(sectionPropertyNames.contains("sectionProp3"));

        assertTrue(section.removeProperty("sectionProp3"));
        assertFalse(section.hasProperty("sectionProp3"));
        assertFalse(section.removeProperty("nonExistentSectionProp"));

        ShuntCompensatorNonLinearModel retrievedModel = (ShuntCompensatorNonLinearModel) network.getShuntCompensator("SHUNT_NON_LINEAR").getModel();
        assertEquals("newModelValue1", retrievedModel.getProperty("modelProp1"));
        assertEquals("modelValue2", retrievedModel.getProperty("modelProp2"));
        assertFalse(retrievedModel.hasProperty("modelProp3"));

        ShuntCompensatorNonLinearModel.Section retrievedSection = retrievedModel.getAllSections().get(0);
        assertEquals("newSectionValue1", retrievedSection.getProperty("sectionProp1"));
        assertEquals("sectionValue2", retrievedSection.getProperty("sectionProp2"));
        assertFalse(retrievedSection.hasProperty("sectionProp3"));
    }

    @Test
    void updateWithInvalidTargetV() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");
        shuntCompensator.setTargetV(100.0);
        shuntCompensator.setTargetDeadband(0.5);
        shuntCompensator.setVoltageRegulatorOn(true);
        assertEquals("Shunt compensator 'SHUNT': invalid value (NaN) for voltage setpoint (voltage regulator is on)",
                assertThrows(ValidationException.class, () -> shuntCompensator.setTargetV(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        shuntCompensator.setTargetV(Double.NaN);
    }

    @Test
    void updateWithInvalidTargetDeadband() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");
        shuntCompensator.setTargetV(100.0);
        shuntCompensator.setTargetDeadband(0.5);
        shuntCompensator.setVoltageRegulatorOn(true);
        assertEquals("Shunt compensator 'SHUNT': Undefined value for target deadband of regulating shunt compensator",
                assertThrows(ValidationException.class, () -> shuntCompensator.setTargetDeadband(Double.NaN)).getMessage());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        shuntCompensator.setTargetDeadband(Double.NaN);
    }
}
