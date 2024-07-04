/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
class TwoWindingsTransformerTest {

    @Test
    void testTapChangerRemoval() {
        Network network = createNetwork();

        //test remove RatioTapChanger
        TwoWindingsTransformer twtWithRatioTapChanger = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543");
        twtWithRatioTapChanger.getRatioTapChanger().remove();
        assertNull(twtWithRatioTapChanger.getRatioTapChanger());

        //test remove PhaseTapChanger
        TwoWindingsTransformer twtWithPhaseTapChanger = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        twtWithPhaseTapChanger.getPhaseTapChanger().remove();
        assertNull(twtWithPhaseTapChanger.getPhaseTapChanger());
    }

    @Test
    void testTapChangerStepsReplacement() {
        Network network = createNetwork();

        // Test ratio tap changer steps replacement
        RatioTapChanger ratioTapChanger = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543").getRatioTapChanger();
        assertEquals(25, ratioTapChanger.getStepCount());
        RatioTapChangerStepsReplacer ratioStepsReplacer = ratioTapChanger.stepsReplacer()
            .beginStep()
            .setR(1.0)
            .setX(2.0)
            .setG(3.0)
            .setB(4.0)
            .setRho(5.0)
            .endStep()
            .beginStep()
            .setR(6.0)
            .setX(7.0)
            .setG(8.0)
            .setB(9.0)
            .setRho(10.0)
            .endStep();
        assertEquals("2 windings transformer 'b94318f6-6d24-4f56-96b9-df2531ad6543': incorrect tap position 10 [1, 2]",
            assertThrows(ValidationException.class, ratioStepsReplacer::replaceSteps).getMessage());
        ratioTapChanger.setTapPosition(1);
        ratioStepsReplacer.replaceSteps();
        assertEquals(2, ratioTapChanger.getStepCount());
        int ratioLowTapPosition = ratioTapChanger.getLowTapPosition();
        assertEquals(1.0, ratioTapChanger.getStep(ratioLowTapPosition).getR());
        assertEquals(2.0, ratioTapChanger.getStep(ratioLowTapPosition).getX());
        assertEquals(3.0, ratioTapChanger.getStep(ratioLowTapPosition).getG());
        assertEquals(4.0, ratioTapChanger.getStep(ratioLowTapPosition).getB());
        assertEquals(5.0, ratioTapChanger.getStep(ratioLowTapPosition).getRho());
        assertEquals(6.0, ratioTapChanger.getStep(ratioLowTapPosition + 1).getR());
        assertEquals(7.0, ratioTapChanger.getStep(ratioLowTapPosition + 1).getX());
        assertEquals(8.0, ratioTapChanger.getStep(ratioLowTapPosition + 1).getG());
        assertEquals(9.0, ratioTapChanger.getStep(ratioLowTapPosition + 1).getB());
        assertEquals(10.0, ratioTapChanger.getStep(ratioLowTapPosition + 1).getRho());

        // Test phase tap changer steps replacement
        PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertEquals(25, phaseTapChanger.getStepCount());
        PhaseTapChangerStepsReplacer phaseStepsReplacer = phaseTapChanger.stepsReplacer()
            .beginStep()
            .setR(6.0)
            .setX(5.0)
            .setG(4.0)
            .setB(3.0)
            .setAlpha(2.0)
            .setRho(1.0)
            .endStep();
        assertEquals("2 windings transformer 'a708c3bc-465d-4fe7-b6ef-6fa6408a62b0': incorrect tap position 10 [1, 1]",
            assertThrows(ValidationException.class, phaseStepsReplacer::replaceSteps).getMessage());
        phaseTapChanger.setTapPosition(1);
        phaseStepsReplacer.replaceSteps();
        assertEquals(1, phaseTapChanger.getStepCount());
        int phaseLowTapPosition = phaseTapChanger.getLowTapPosition();
        assertEquals(6.0, phaseTapChanger.getStep(phaseLowTapPosition).getR());
        assertEquals(5.0, phaseTapChanger.getStep(phaseLowTapPosition).getX());
        assertEquals(4.0, phaseTapChanger.getStep(phaseLowTapPosition).getG());
        assertEquals(3.0, phaseTapChanger.getStep(phaseLowTapPosition).getB());
        assertEquals(2.0, phaseTapChanger.getStep(phaseLowTapPosition).getAlpha());
        assertEquals(1.0, phaseTapChanger.getStep(phaseLowTapPosition).getRho());
    }

    @Test
    void testTapChangerNeutralPosition() {
        Network network = createNetwork();

        // Test ratio tap changer neutral position
        RatioTapChanger ratioTapChanger = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543").getRatioTapChanger();
        assertEquals(25, ratioTapChanger.getStepCount());
        assertEquals(13, ratioTapChanger.getNeutralPosition().orElseThrow());
        assertEquals(ratioTapChanger.getNeutralStep().orElseThrow().getR(), ratioTapChanger.getStep(13).getR());
        assertEquals(ratioTapChanger.getNeutralStep().orElseThrow().getB(), ratioTapChanger.getStep(13).getB());
        assertEquals(ratioTapChanger.getNeutralStep().orElseThrow().getG(), ratioTapChanger.getStep(13).getG());
        assertEquals(ratioTapChanger.getNeutralStep().orElseThrow().getX(), ratioTapChanger.getStep(13).getX());
        assertEquals(ratioTapChanger.getNeutralStep().orElseThrow().getRho(), ratioTapChanger.getStep(13).getRho());

        // Test phase tap changer neutral position
        PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertEquals(25, phaseTapChanger.getStepCount());
        assertEquals(13, phaseTapChanger.getNeutralPosition().orElseThrow());
        assertEquals(phaseTapChanger.getNeutralStep().orElseThrow().getR(), phaseTapChanger.getStep(13).getR());
        assertEquals(phaseTapChanger.getNeutralStep().orElseThrow().getB(), phaseTapChanger.getStep(13).getB());
        assertEquals(phaseTapChanger.getNeutralStep().orElseThrow().getG(), phaseTapChanger.getStep(13).getG());
        assertEquals(phaseTapChanger.getNeutralStep().orElseThrow().getX(), phaseTapChanger.getStep(13).getX());
        assertEquals(phaseTapChanger.getNeutralStep().orElseThrow().getRho(), phaseTapChanger.getStep(13).getRho());
        assertEquals(phaseTapChanger.getNeutralStep().orElseThrow().getAlpha(), phaseTapChanger.getStep(13).getAlpha());

        // Test with no phase tap changer neutral position
        PhaseTapChanger phaseTapChanger2 = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957")
                .newPhaseTapChanger()
                .beginStep()
                .setR(1)
                .setAlpha(12)
                .setRho(13)
                .endStep()
                .setTapPosition(0)
                .add();
        assertTrue(phaseTapChanger2.getNeutralPosition().isEmpty());
        assertTrue(phaseTapChanger2.getNeutralStep().isEmpty());

        // Test with no ratio tap changer neutral position
        RatioTapChanger ratioTapChanger2 = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957")
                .newRatioTapChanger()
                .beginStep()
                .setR(1)
                .setRho(13)
                .endStep()
                .setTapPosition(0)
                .add();
        assertTrue(ratioTapChanger2.getNeutralPosition().isEmpty());
        assertTrue(ratioTapChanger2.getNeutralStep().isEmpty());
    }

    private Network createNetwork() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        return Importer.find("CGMES")
            .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
    }
}
