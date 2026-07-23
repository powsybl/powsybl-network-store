/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class ThreeWindingTransformerTest {

    private Network createNetwork() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        PhaseTapChanger phaseTapChanger = network.getThreeWindingsTransformer("3WT").getLeg2()
            .newPhaseTapChanger()
            .setTargetDeadband(2)
            .setRegulating(false)
            .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
            .setTapPosition(0)
            .beginStep()
            .setR(1)
            .setAlpha(12)
            .setRho(13)
            .endStep()
            .setTapPosition(0)
            .add();
        phaseTapChanger.setProperty("prop1", "value1");
        phaseTapChanger.setProperty("prop2", "value2");
        PhaseTapChangerStep phaseTapChangerStep = network.getThreeWindingsTransformer("3WT").getLeg2()
            .getPhaseTapChanger()
            .getStep(0);
        phaseTapChangerStep.setProperty("stepProp1", "stepValue1");
        phaseTapChangerStep.setProperty("stepProp2", "stepValue2");
        return network;
    }

    @Test
    void testRatioTapChangerRegulation() {
        String twtId = "3WT";
        String load1Id = "LOAD_33";
        String load2Id = "LOAD_11";
        Network network = createNetwork();

        RatioTapChanger ratioTapChanger = network.getThreeWindingsTransformer(twtId).getLeg2().getRatioTapChanger();
        assertEquals(load1Id, ratioTapChanger.getRegulationTerminal().getConnectable().getId());
        Load load = network.getLoad(load2Id);
        ratioTapChanger.setRegulationTerminal(load.getTerminal());
        assertEquals(load2Id, ratioTapChanger.getRegulationTerminal().getConnectable().getId());
        assertTrue(ratioTapChanger.isRegulating());
        assertEquals(RatioTapChanger.RegulationMode.VOLTAGE, ratioTapChanger.getRegulationMode());

        load.remove();

        assertFalse(ratioTapChanger.isRegulating());
        assertEquals(RatioTapChanger.RegulationMode.VOLTAGE, ratioTapChanger.getRegulationMode());
        assertNull(ratioTapChanger.getRegulationTerminal());
    }

    @Test
    void testPhaseTapChangerRegulation() {
        String twtId = "3WT";
        String load2Id = "LOAD_11";
        Network network = createNetwork();
        RatioTapChanger ratioTapChanger = network.getThreeWindingsTransformer(twtId).getLeg2().getRatioTapChanger();
        ratioTapChanger.setRegulating(false);

        Load load = network.getLoad(load2Id);

        PhaseTapChanger phaseTapChanger = network.getThreeWindingsTransformer(twtId).getLeg2().getPhaseTapChanger();
        phaseTapChanger.setRegulationTerminal(load.getTerminal());
        assertEquals(load2Id, phaseTapChanger.getRegulationTerminal().getConnectable().getId());
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
        phaseTapChanger.setRegulationValue(225);
        phaseTapChanger.setLoadTapChangingCapabilities(true);
        phaseTapChanger.setRegulating(true);

        load.remove();

        assertEquals(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, phaseTapChanger.getRegulationMode());
        assertNull(phaseTapChanger.getRegulationTerminal());
        assertFalse(phaseTapChanger.isRegulating());
    }

    @Test
    void removeExtension() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer threeWindingsTransformer = network.getThreeWindingsTransformer("3WT");
        testRemoveWithOneFeeder(threeWindingsTransformer, threeWindingsTransformer.newExtension(ConnectablePositionAdder.class).newFeeder1());
        testRemoveWithOneFeeder(threeWindingsTransformer, threeWindingsTransformer.newExtension(ConnectablePositionAdder.class).newFeeder2());
        testRemoveWithOneFeeder(threeWindingsTransformer, threeWindingsTransformer.newExtension(ConnectablePositionAdder.class).newFeeder3());
        threeWindingsTransformer.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClockLeg3(1).withPhaseAngleClockLeg2(1).add();
        assertTrue(threeWindingsTransformer.removeExtension(ThreeWindingsTransformerPhaseAngleClock.class));
        assertNull(threeWindingsTransformer.getExtension(ThreeWindingsTransformerPhaseAngleClock.class));
        assertFalse(threeWindingsTransformer.removeExtension(ThreeWindingsTransformerPhaseAngleClock.class));
    }

    private void testRemoveWithOneFeeder(ThreeWindingsTransformer threeWindingsTransformer, ConnectablePositionAdder.FeederAdder feederAdder) {
        feederAdder.withOrder(1).add().add();
        assertTrue(threeWindingsTransformer.removeExtension(ConnectablePosition.class));
        assertNull(threeWindingsTransformer.getExtension(ConnectablePosition.class));
        assertFalse(threeWindingsTransformer.removeExtension(ConnectablePosition.class));
    }

    @Test
    void testPhaseTapChangerProperties() {
        String twtId = "3WT";
        Network network = createNetwork();

        PhaseTapChanger phaseTapChanger = network.getThreeWindingsTransformer("3WT").getLeg2().getPhaseTapChanger();

        assertTrue(phaseTapChanger.hasProperty());
        assertTrue(phaseTapChanger.hasProperty("prop1"));
        assertTrue(phaseTapChanger.hasProperty("prop2"));
        assertFalse(phaseTapChanger.hasProperty("nonExistentProp"));

        assertEquals("value1", phaseTapChanger.getProperty("prop1"));
        assertEquals("value2", phaseTapChanger.getProperty("prop2"));
        assertNull(phaseTapChanger.getProperty("nonExistentProp"));

        String oldValue = phaseTapChanger.setProperty("prop3", "value3");
        assertNull(oldValue);
        assertEquals("value3", phaseTapChanger.getProperty("prop3"));

        String replacedValue = phaseTapChanger.setProperty("prop1", "newValue1");
        assertEquals("value1", replacedValue);
        assertEquals("newValue1", phaseTapChanger.getProperty("prop1"));

        Set<String> propertyNames = phaseTapChanger.getPropertyNames();
        assertEquals(3, propertyNames.size());
        assertTrue(propertyNames.contains("prop1"));
        assertTrue(propertyNames.contains("prop2"));
        assertTrue(propertyNames.contains("prop3"));

        assertTrue(phaseTapChanger.removeProperty("prop3"));
        assertFalse(phaseTapChanger.hasProperty("prop3"));
        assertFalse(phaseTapChanger.removeProperty("nonExistentProp"));

        PhaseTapChangerStep step = phaseTapChanger.getStep(0);
        assertTrue(step.hasProperty());
        assertTrue(step.hasProperty("stepProp1"));
        assertTrue(step.hasProperty("stepProp2"));
        assertFalse(step.hasProperty("nonExistentStepProp"));

        assertEquals("stepValue1", step.getProperty("stepProp1"));
        assertEquals("stepValue2", step.getProperty("stepProp2"));
        assertNull(step.getProperty("nonExistentStepProp"));

        assertEquals("stepValue1", step.getProperty("stepProp1", "defaultStepValue"));
        assertEquals("defaultStepValue", step.getProperty("nonExistentStepProp", "defaultStepValue"));

        String oldStepValue = step.setProperty("stepProp3", "stepValue3");
        assertNull(oldStepValue);
        assertEquals("stepValue3", step.getProperty("stepProp3"));

        String replacedStepValue = step.setProperty("stepProp1", "newStepValue1");
        assertEquals("stepValue1", replacedStepValue);
        assertEquals("newStepValue1", step.getProperty("stepProp1"));

        Set<String> stepPropertyNames = step.getPropertyNames();
        assertEquals(3, stepPropertyNames.size());
        assertTrue(stepPropertyNames.contains("stepProp1"));
        assertTrue(stepPropertyNames.contains("stepProp2"));
        assertTrue(stepPropertyNames.contains("stepProp3"));

        assertTrue(step.removeProperty("stepProp3"));
        assertFalse(step.hasProperty("stepProp3"));
        assertFalse(step.removeProperty("nonExistentStepProp"));

        PhaseTapChanger retrievedPhaseTapChanger = network.getThreeWindingsTransformer(twtId).getLeg2().getPhaseTapChanger();
        assertEquals("newValue1", retrievedPhaseTapChanger.getProperty("prop1"));
        assertEquals("value2", retrievedPhaseTapChanger.getProperty("prop2"));
        assertFalse(retrievedPhaseTapChanger.hasProperty("prop3"));

        PhaseTapChangerStep retrievedStep = retrievedPhaseTapChanger.getStep(0);
        assertEquals("newStepValue1", retrievedStep.getProperty("stepProp1"));
        assertEquals("stepValue2", retrievedStep.getProperty("stepProp2"));
        assertFalse(retrievedStep.hasProperty("stepProp3"));
    }

    @Test
    void testRatioTapChangerProperties() {
        String twtId = "3WT";
        Network network = ThreeWindingsTransformerNetworkFactory.create();

        RatioTapChanger ratioTapChanger = network.getThreeWindingsTransformer(twtId).getLeg2().getRatioTapChanger();

        ratioTapChanger.setProperty("prop1", "propValue1");
        ratioTapChanger.setProperty("prop2", "propValue2");

        RatioTapChangerStep step = ratioTapChanger.getCurrentStep();
        step.setProperty("propStep1", "propStepValue1");
        step.setProperty("propStep2", "propStepValue2");

        assertTrue(ratioTapChanger.hasProperty());
        assertTrue(ratioTapChanger.hasProperty("prop1"));
        assertTrue(ratioTapChanger.hasProperty("prop2"));
        assertFalse(ratioTapChanger.hasProperty("nonExistentProp"));

        assertEquals("propValue1", ratioTapChanger.getProperty("prop1"));
        assertEquals("propValue2", ratioTapChanger.getProperty("prop2"));
        assertNull(ratioTapChanger.getProperty("nonExistentProp"));

        assertEquals("propValue1", ratioTapChanger.getProperty("prop1", "defaultPropValue"));
        assertEquals("defaultPropValue", ratioTapChanger.getProperty("nonExistentProp", "defaultPropValue"));

        String oldPropValue = ratioTapChanger.setProperty("prop3", "propValue3");
        assertNull(oldPropValue);
        assertEquals("propValue3", ratioTapChanger.getProperty("prop3"));

        String replacedPropValue = ratioTapChanger.setProperty("prop1", "newPropValue1");
        assertEquals("propValue1", replacedPropValue);
        assertEquals("newPropValue1", ratioTapChanger.getProperty("prop1"));

        Set<String> propertyNames = ratioTapChanger.getPropertyNames();
        assertEquals(3, propertyNames.size());
        assertTrue(propertyNames.contains("prop1"));
        assertTrue(propertyNames.contains("prop2"));
        assertTrue(propertyNames.contains("prop3"));

        assertTrue(ratioTapChanger.removeProperty("prop3"));
        assertFalse(ratioTapChanger.hasProperty("prop3"));
        assertFalse(ratioTapChanger.removeProperty("nonExistentProp"));

        assertTrue(step.hasProperty());
        assertTrue(step.hasProperty("propStep1"));
        assertTrue(step.hasProperty("propStep2"));
        assertFalse(step.hasProperty("nonExistentPropStep"));

        assertEquals("propStepValue1", step.getProperty("propStep1"));
        assertEquals("propStepValue2", step.getProperty("propStep2"));
        assertNull(step.getProperty("nonExistentPropStep"));

        assertEquals("propStepValue1", step.getProperty("propStep1", "defaultPropStepValue"));
        assertEquals("defaultPropStepValue", step.getProperty("nonExistentPropStep", "defaultPropStepValue"));

        String oldPropStepValue = step.setProperty("propStep3", "propStepValue3");
        assertNull(oldPropStepValue);
        assertEquals("propStepValue3", step.getProperty("propStep3"));

        String replacedPropStepValue = step.setProperty("propStep1", "newPropStepValue1");
        assertEquals("propStepValue1", replacedPropStepValue);
        assertEquals("newPropStepValue1", step.getProperty("propStep1"));

        Set<String> stepPropertyNames = step.getPropertyNames();
        assertEquals(3, stepPropertyNames.size());
        assertTrue(stepPropertyNames.contains("propStep1"));
        assertTrue(stepPropertyNames.contains("propStep2"));
        assertTrue(stepPropertyNames.contains("propStep3"));

        assertTrue(step.removeProperty("propStep3"));
        assertFalse(step.hasProperty("propStep3"));
        assertFalse(step.removeProperty("nonExistentPropStep"));

        RatioTapChanger retrievedRatioTapChanger = network.getThreeWindingsTransformer(twtId).getLeg2().getRatioTapChanger();
        assertEquals("newPropValue1", retrievedRatioTapChanger.getProperty("prop1"));
        assertEquals("propValue2", retrievedRatioTapChanger.getProperty("prop2"));
        assertFalse(retrievedRatioTapChanger.hasProperty("prop3"));

        RatioTapChangerStep retrievedStep = retrievedRatioTapChanger.getCurrentStep();
        assertEquals("newPropStepValue1", retrievedStep.getProperty("propStep1"));
        assertEquals("propStepValue2", retrievedStep.getProperty("propStep2"));
        assertFalse(retrievedStep.hasProperty("propStep3"));
    }
}
