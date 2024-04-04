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
public class TwoWindingsTransformerTest {

    @Test
    public void testTapChangerRemoval() {
        Properties properties = new Properties();
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), properties);
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
    public void testTapChangerStepsReplacement() {
        Network network = Importer.find("CGMES")
            .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), null);

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
}
