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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
    void testTapChangerEqualsAndHashCode() {
        Network network = createNetwork();
        TwoWindingsTransformer twtWithPhaseTapChanger = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        TwoWindingsTransformer twtWithRatioTapChanger = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543");
        Set<PhaseTapChanger> phaseTapChangers = new HashSet<>();
        phaseTapChangers.add(twtWithPhaseTapChanger.getPhaseTapChanger());

        // use the equals and the hashcode of PhaseTapChangerImpl
        phaseTapChangers.remove(twtWithPhaseTapChanger.getPhaseTapChanger());

        assertEquals(0, phaseTapChangers.size());
        PhaseTapChanger phaseTapChanger = twtWithPhaseTapChanger.getPhaseTapChanger();
        RatioTapChanger ratioTapChanger = twtWithRatioTapChanger.getRatioTapChanger();
        assertEquals(phaseTapChanger, phaseTapChanger);
        assertEquals(ratioTapChanger, ratioTapChanger);
        assertNotNull(phaseTapChanger);
        assertNotNull(ratioTapChanger);
        assertNotEquals(phaseTapChanger, ratioTapChanger);
        assertNotEquals(ratioTapChanger, phaseTapChanger);
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

        phaseTapChanger.getStep(phaseLowTapPosition).setRho(1.5);
        phaseTapChanger.getStep(phaseLowTapPosition).setR(6.5);
        phaseTapChanger.getStep(phaseLowTapPosition).setX(5.5);
        phaseTapChanger.getStep(phaseLowTapPosition).setB(3.5);
        phaseTapChanger.getStep(phaseLowTapPosition).setG(4.5);
        phaseTapChanger.getStep(phaseLowTapPosition).setAlpha(2.5);
        assertEquals(6.5, phaseTapChanger.getStep(phaseLowTapPosition).getR());
        assertEquals(5.5, phaseTapChanger.getStep(phaseLowTapPosition).getX());
        assertEquals(4.5, phaseTapChanger.getStep(phaseLowTapPosition).getG());
        assertEquals(3.5, phaseTapChanger.getStep(phaseLowTapPosition).getB());
        assertEquals(2.5, phaseTapChanger.getStep(phaseLowTapPosition).getAlpha());
        assertEquals(1.5, phaseTapChanger.getStep(phaseLowTapPosition).getRho());
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

        // Test with RatioTapChanger stepReplacer
        ratioTapChanger2.stepsReplacer()
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRho(6.0)
                .endStep()
                .beginStep()
                .setR(5.0)
                .setX(6.0)
                .setG(7.0)
                .setB(8.0)
                .setRho(7.0)
                .endStep()
                .beginStep()
                .setR(9.0)
                .setX(10.0)
                .setG(11.0)
                .setB(12.0)
                .setRho(1.0)
                .endStep()
                .replaceSteps();

        assertEquals(3, ratioTapChanger2.getStepCount());
        assertEquals(3, ratioTapChanger2.getAllSteps().size());
        assertEquals(0, ratioTapChanger2.getLowTapPosition());
        assertEquals(2, ratioTapChanger2.getHighTapPosition());
        assertEquals(2, ratioTapChanger2.getNeutralPosition().orElseThrow());

        //check neutral step attributes
        var ratioTapChangerNeutralStep = ratioTapChanger2.getNeutralStep().orElseThrow();
        assertEquals(1, ratioTapChangerNeutralStep.getRho());
        assertEquals(9, ratioTapChangerNeutralStep.getR());
        assertEquals(10, ratioTapChangerNeutralStep.getX());
        assertEquals(11, ratioTapChangerNeutralStep.getG());
        assertEquals(12, ratioTapChangerNeutralStep.getB());

        //test with PhaseTapChanger stepsReplacer
        phaseTapChanger2.stepsReplacer()
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setAlpha(5.0)
                .setRho(6.0)
                .endStep()
                .beginStep()
                .setR(5.0)
                .setX(6.0)
                .setG(7.0)
                .setB(8.0)
                .setAlpha(6.0)
                .setRho(7.0)
                .endStep()
                .beginStep()
                .setR(9.0)
                .setX(10.0)
                .setG(11.0)
                .setB(12.0)
                .setAlpha(0.0)
                .setRho(1.0)
                .endStep()
                .replaceSteps();

        assertEquals(3, phaseTapChanger2.getStepCount());
        assertEquals(3, phaseTapChanger2.getAllSteps().size());
        assertEquals(0, phaseTapChanger2.getLowTapPosition());
        assertEquals(2, phaseTapChanger2.getHighTapPosition());
        assertEquals(2, phaseTapChanger2.getNeutralPosition().orElseThrow());

        //check neutral step attributes
        var phaseTapChangerNeutralStep = phaseTapChanger2.getNeutralStep().orElseThrow();
        assertEquals(0, phaseTapChangerNeutralStep.getAlpha());
        assertEquals(1, phaseTapChangerNeutralStep.getRho());
        assertEquals(9, phaseTapChangerNeutralStep.getR());
        assertEquals(10, phaseTapChangerNeutralStep.getX());
        assertEquals(11, phaseTapChangerNeutralStep.getG());
        assertEquals(12, phaseTapChangerNeutralStep.getB());
    }

    private Network createNetwork() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        return Importer.find("CGMES")
            .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
    }

    @Test
    void testTerminals() {
        Network network = createNetwork();
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957");
        assertNotNull(twt);
        assertInstanceOf(TwoWindingsTransformerImpl.class, twt);

        Terminal terminal1 = twt.getTerminal1();
        Terminal terminal2 = twt.getTerminal2();
        assertEquals(List.of(terminal1, terminal2), ((TwoWindingsTransformerImpl) twt).getTerminals(null));
        assertEquals(List.of(terminal1), ((TwoWindingsTransformerImpl) twt).getTerminals(ThreeSides.ONE));
        assertEquals(List.of(terminal2), ((TwoWindingsTransformerImpl) twt).getTerminals(ThreeSides.TWO));
        assertEquals(Collections.emptyList(), ((TwoWindingsTransformerImpl) twt).getTerminals(ThreeSides.THREE));
    }

    @Test
    void settersTest() {
        Network network = createNetwork();
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957");

        twt.setR(4.);
        twt.setX(8.);
        twt.setG(2.);
        twt.setB(7.);
        twt.setRatedU1(12.);
        twt.setRatedU2(14.);
        twt.setRatedS(16.);

        assertEquals(4., twt.getR());
        assertEquals(8., twt.getX());
        assertEquals(2., twt.getG());
        assertEquals(7., twt.getB());
        assertEquals(12., twt.getRatedU1());
        assertEquals(14., twt.getRatedU2());
        assertEquals(16., twt.getRatedS());
    }
}
