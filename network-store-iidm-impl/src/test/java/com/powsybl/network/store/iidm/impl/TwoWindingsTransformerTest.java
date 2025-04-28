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
import com.powsybl.iidm.network.events.RemovalNetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.network.store.model.ResourceType;
import com.powsybl.network.store.model.TerminalRefAttributes;
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
    void testTapChangerRegulation() {
        String loadId = "69add5b4-70bd-4360-8a93-286256c0d38b";
        String twtId1 = "b94318f6-6d24-4f56-96b9-df2531ad6543";
        String twtId2 = "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0";
        Network network = createNetwork();
        RatioTapChanger ratioTapChanger = network.getTwoWindingsTransformer(twtId1).getRatioTapChanger();
        assertEquals(twtId1, ratioTapChanger.getRegulationTerminal().getConnectable().getId());
        Load load = network.getLoad(loadId);
        ratioTapChanger.setRegulationTerminal(load.getTerminal());
        assertEquals(loadId, ratioTapChanger.getRegulationTerminal().getConnectable().getId());
        PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer(twtId2).getPhaseTapChanger();
        phaseTapChanger.setRegulationTerminal(load.getTerminal());
        assertEquals(loadId, phaseTapChanger.getRegulationTerminal().getConnectable().getId());
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
        load.remove();
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, phaseTapChanger.getRegulationMode());
        assertNull(phaseTapChanger.getRegulationTerminal());
        assertNull(ratioTapChanger.getRegulationMode());
        assertNull(ratioTapChanger.getRegulationTerminal());
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

    @Test
    void createWithVoltageRegulationTest() {
        Network network = createNetwork();
        network.getSubstation("37e14a0f-5e34-4647-a062-8bfd9305fa9d")
            .newTwoWindingsTransformer()
            .setId("test")
            .setVoltageLevel1("b10b171b-3bc5-4849-bb1f-61ed9ea1ec7c")
            .setConnectableBus1("99b219f3-4593-428b-a4da-124a54630178")
            .setVoltageLevel2("469df5f7-058f-4451-a998-57a48e8a56fe")
            .setConnectableBus2("e44141af-f1dc-44d3-bfa4-b674e5c953d7")
            .setR(250)
            .setX(100)
            .setG(52)
            .setB(12)
            .setRatedU1(225)
            .setRatedU2(380)
            .add();
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("test");
        twt.newRatioTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(false)
            .setRegulationTerminal(network.getLoad("69add5b4-70bd-4360-8a93-286256c0d38b").getTerminal())
            .setTargetDeadband(22)
            .setTargetV(220)
            .beginStep()
            .setRho(0.99)
            .setR(1.)
            .setX(4.)
            .setG(0.5)
            .setB(1.5)
            .endStep()
            .beginStep()
            .setRho(1)
            .setR(1.1)
            .setX(4.1)
            .setG(0.6)
            .setB(1.6)
            .endStep()
            .beginStep()
            .setRho(1.01)
            .setR(1.2)
            .setX(4.2)
            .setG(0.7)
            .setB(1.7)
            .endStep()
            .add();
        assertEquals(network.getLoad("69add5b4-70bd-4360-8a93-286256c0d38b").getTerminal(), twt.getRatioTapChanger().getRegulationTerminal());
    }

    @Test
    void testTwoWindingsTransformerRemovalEvents() {
        Network network = createNetwork();
        String twoWindingsTransformerId = "b94318f6-6d24-4f56-96b9-df2531ad6543";
        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer(twoWindingsTransformerId);
        assertNotNull(twoWindingsTransformer);
        String generatorId = "550ebe0d-f2b2-48c1-991f-cebea43a21aa";
        Generator generator = network.getGenerator(generatorId);
        assertNotNull(generator);

        // Add regulation to the generator
        generator.setRegulatingTerminal(twoWindingsTransformer.getTerminal(TwoSides.ONE));

        // Add event listener on 2WT removal
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        twoWindingsTransformer.remove();
        assertNull(network.getTwoWindingsTransformer(twoWindingsTransformerId));
        List<? extends Record> expectedEvents = List.of(
                new RemovalNetworkEvent(twoWindingsTransformerId, false),
                new UpdateNetworkEvent(twoWindingsTransformerId, "regulatingTerminal", VariantManagerConstants.INITIAL_VARIANT_ID, TerminalRefAttributes.builder().connectableId(twoWindingsTransformerId).side(TwoSides.TWO.name()).build(), null),
                new UpdateNetworkEvent(twoWindingsTransformerId, "regulationMode", VariantManagerConstants.INITIAL_VARIANT_ID, RatioTapChanger.RegulationMode.VOLTAGE.name(), null),
                new UpdateNetworkEvent(generatorId, "regulatingTerminal", VariantManagerConstants.INITIAL_VARIANT_ID, TerminalRefAttributes.builder().connectableId(twoWindingsTransformerId).side(TwoSides.ONE.name()).build(), TerminalRefAttributes.builder().connectableId(generatorId).build()),
                new UpdateNetworkEvent(generatorId, "regulatedResourceType", VariantManagerConstants.INITIAL_VARIANT_ID, ResourceType.TWO_WINDINGS_TRANSFORMER, ResourceType.GENERATOR),
                new UpdateNetworkEvent(generatorId, "regulating", VariantManagerConstants.INITIAL_VARIANT_ID, true, false),
                new RemovalNetworkEvent(twoWindingsTransformerId, true));
        // Order is not guaranteed with regulation events as we use Set for regulating equipments
        assertTrue(eventRecorder.getEvents().containsAll(expectedEvents));
        assertEquals(expectedEvents.size(), eventRecorder.getEvents().size());
    }

    @Test
    void testChecks() {
        Network network = createNetwork();
        TwoWindingsTransformer twtWithPhaseTapChanger = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        assertNotNull(twtWithPhaseTapChanger.getPhaseTapChanger());
        PhaseTapChanger phaseTapChanger = twtWithPhaseTapChanger.getPhaseTapChanger();
        assertEquals(25, phaseTapChanger.getStepCount());
        String message = assertThrows(ValidationException.class, () -> phaseTapChanger.getStep(26)).getMessage();
        assertEquals("2 windings transformer 'a708c3bc-465d-4fe7-b6ef-6fa6408a62b0': incorrect tap position 26 [1, 25]", message);

        assertEquals(10, phaseTapChanger.getTapPosition());
        phaseTapChanger.setLowTapPosition(2);
        assertEquals(2, phaseTapChanger.getLowTapPosition());
        assertEquals(11, phaseTapChanger.getTapPosition());
    }
}
