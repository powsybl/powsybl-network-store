/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractStandbyAutomatonTest;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StandbyAutomatonTest extends AbstractStandbyAutomatonTest {

    @Test
    /*
        this test needs to be overwritten because the field b0 of StandbyAutomaton is common
        for all variants in the powsybl-core implementation and not in this implementation
    */
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";
        String variant3 = "variant3";

        Network network = SvcTestCaseFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);
        svc.newExtension(StandbyAutomatonAdder.class)
                .withB0(0.0001f)
                .withStandbyStatus(true)
                .withLowVoltageSetpoint(390f)
                .withHighVoltageSetpoint(400f)
                .withLowVoltageThreshold(385f)
                .withHighVoltageThreshold(405f)
                .add();
        StandbyAutomaton standbyAutomaton = svc.getExtension(StandbyAutomaton.class);
        assertNotNull(standbyAutomaton);

        // Testing variant cloning
        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertTrue(standbyAutomaton.isStandby());
        assertEquals(0.0001f, standbyAutomaton.getB0(), 0f);
        assertEquals(390f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0f);

        // Testing setting different values in the cloned variant and going back to the initial one
        standbyAutomaton.setB0(0.0004f)
                .setStandby(false)
                .setLowVoltageSetpoint(392f)
                .setHighVoltageSetpoint(403f)
                .setLowVoltageThreshold(390f)
                .setHighVoltageThreshold(410f);

        assertFalse(standbyAutomaton.isStandby());
        assertEquals(0.0004f, standbyAutomaton.getB0(), 0f);
        assertEquals(392f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(403f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(390f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(410f, standbyAutomaton.getHighVoltageThreshold(), 0f);

        // set the same values, for condition coverage
        standbyAutomaton.setB0(0.0004f)
            .setStandby(false)
            .setLowVoltageSetpoint(392f)
            .setHighVoltageSetpoint(403f)
            .setLowVoltageThreshold(390f)
            .setHighVoltageThreshold(410f);

        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertTrue(standbyAutomaton.isStandby());
        assertEquals(0.0001f, standbyAutomaton.getB0(), 0f); // not modify by variant change
        assertEquals(390f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0f);

        // Removes a variant then adds another variant to test variant recycling (hence calling allocateVariantArrayElement)
        variantManager.removeVariant(variant1);
        List<String> targetVariantIds = Arrays.asList(variant1, variant3);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant1);
        assertTrue(standbyAutomaton.isStandby());
        assertEquals(0.0001f, standbyAutomaton.getB0(), 0f);
        assertEquals(390f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0f);
        variantManager.setWorkingVariant(variant3);
        assertTrue(standbyAutomaton.isStandby());
        assertEquals(0.0001f, standbyAutomaton.getB0(), 0f);
        assertEquals(390f, standbyAutomaton.getLowVoltageSetpoint(), 0f);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0f);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0f);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0f);

        // Test removing current variant
        variantManager.removeVariant(variant3);
        try {
            standbyAutomaton.getLowVoltageSetpoint();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Variant index not set", e.getMessage());
        }
    }

}
