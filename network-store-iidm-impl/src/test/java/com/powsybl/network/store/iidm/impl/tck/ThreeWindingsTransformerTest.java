/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractThreeWindingsTransformerTest;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerTest extends AbstractThreeWindingsTransformerTest {

    @Test
    void testTerminals() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        assertNotNull(twt);
        assertInstanceOf(ThreeWindingsTransformerImpl.class, twt);

        Terminal terminal1 = twt.getLeg1().getTerminal();
        Terminal terminal2 = twt.getLeg2().getTerminal();
        Terminal terminal3 = twt.getLeg3().getTerminal();
        assertEquals(List.of(terminal1, terminal2, terminal3), ((ThreeWindingsTransformerImpl) twt).getTerminals(null));
        assertEquals(List.of(terminal1), ((ThreeWindingsTransformerImpl) twt).getTerminals(ThreeSides.ONE));
        assertEquals(List.of(terminal2), ((ThreeWindingsTransformerImpl) twt).getTerminals(ThreeSides.TWO));
        assertEquals(List.of(terminal3), ((ThreeWindingsTransformerImpl) twt).getTerminals(ThreeSides.THREE));
    }

    @Test
    void testTapChangerEqualsAndHashCode() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        Network network2 = FourSubstationsNodeBreakerFactory.create();
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        TwoWindingsTransformer twt2 = network2.getTwoWindingsTransformer("TWT");
        Set<RatioTapChanger> ratioTapChangers = new HashSet<>();
        ratioTapChangers.add(twt.getLeg2().getRatioTapChanger());
        ratioTapChangers.add(twt.getLeg2().getRatioTapChanger());
        ratioTapChangers.remove(twt.getLeg3().getRatioTapChanger());

        assertEquals(1, ratioTapChangers.size());

        // use the equals and the hashcode of RatioTapChangerImpl
        ratioTapChangers.remove(twt.getLeg2().getRatioTapChanger());

        assertEquals(0, ratioTapChangers.size());
        assertEquals(twt.getLeg2().getRatioTapChanger(), twt.getLeg2().getRatioTapChanger());
        assertNull(twt.getLeg1().getRatioTapChanger());
        assertNotNull(twt.getLeg2().getRatioTapChanger());
        assertNotEquals(twt.getLeg2().getRatioTapChanger(), twt.getLeg3().getRatioTapChanger());
        assertNotEquals(twt2.getRatioTapChanger(), twt.getLeg2().getRatioTapChanger());
        assertNotEquals(twt.getLeg2().getRatioTapChanger(), twt2.getRatioTapChanger());

        createPhaseTapChangers(twt);
        //phase tap changer
        assertEquals(twt.getLeg1().getPhaseTapChanger(), twt.getLeg1().getPhaseTapChanger());
        assertNull(twt.getLeg3().getPhaseTapChanger());
        assertNotEquals(twt.getLeg2().getPhaseTapChanger(), twt.getLeg1().getPhaseTapChanger());
        assertNotEquals(twt2.getPhaseTapChanger(), twt.getLeg1().getPhaseTapChanger());
    }

    private void createPhaseTapChangers(ThreeWindingsTransformer twt) {
        twt.getLeg1().newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(false)
            .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
            .setRegulationValue(25)
            .setRegulationTerminal(twt.getTerminal(ThreeSides.ONE))
            .setTargetDeadband(22)
            .beginStep()
            .setAlpha(-10)
            .setRho(0.99)
            .setR(1.)
            .setX(4.)
            .setG(0.5)
            .setB(1.5)
            .endStep()
            .add();

        twt.getLeg2().newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(false)
            .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
            .setRegulationValue(25)
            .setRegulationTerminal(twt.getTerminal(ThreeSides.ONE))
            .setTargetDeadband(22)
            .beginStep()
            .setAlpha(-10)
            .setRho(0.99)
            .setR(1.)
            .setX(4.)
            .setG(0.5)
            .setB(1.5)
            .endStep()
            .add();
    }

    @Test
    public void testPhaseTapChangerInAllLegs() {
        // Problems with equality between PhaseTapChangerImpl
    }

    @Test
    public void testRatioTapChangerAndCurrentLimitsInLeg2() {

    }

    @Test
    public void testRatioTapChangerAndCurrentLimitsInLeg3() {

    }

    @Test
    public void testPowerLimitsInLeg1() {

    }

    @Test
    public void testCurrentLimitsInLeg1() {

    }
}
