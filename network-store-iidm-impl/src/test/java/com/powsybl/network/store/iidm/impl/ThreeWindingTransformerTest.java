/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class ThreeWindingTransformerTest {
    @Test
    void testTapChangerRegulation() {
        String twtId = "3WT";
        String load1Id = "LOAD_33";
        String load2Id = "LOAD_11";
        Network network = createNetwork();

        RatioTapChanger ratioTapChanger = network.getThreeWindingsTransformer(twtId).getLeg2().getRatioTapChanger();
        assertEquals(load1Id, ratioTapChanger.getRegulationTerminal().getConnectable().getId());
        Load load = network.getLoad(load2Id);
        ratioTapChanger.setRegulationTerminal(load.getTerminal());
        assertEquals(load2Id, ratioTapChanger.getRegulationTerminal().getConnectable().getId());

        PhaseTapChanger phaseTapChanger = network.getThreeWindingsTransformer(twtId).getLeg2().getPhaseTapChanger();
        phaseTapChanger.setRegulationTerminal(load.getTerminal());
        assertEquals(load2Id, phaseTapChanger.getRegulationTerminal().getConnectable().getId());
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
        phaseTapChanger.setRegulationValue(225);
        load.remove();
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, phaseTapChanger.getRegulationMode());
        assertEquals(twtId, phaseTapChanger.getRegulationTerminal().getConnectable().getId());
        assertNull(ratioTapChanger.getRegulationMode());
        assertEquals(twtId, ratioTapChanger.getRegulationTerminal().getConnectable().getId());
    }

    private Network createNetwork() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.getThreeWindingsTransformer("3WT").getLeg2()
            .newPhaseTapChanger()
            .setTargetDeadband(2)
            .setRegulating(false)
            .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
            .setTapPosition(0)
            .beginStep()
            .setR(1)
            .setAlpha(12)
            .setRho(13)
            .endStep()
            .setTapPosition(0)
            .add();
        return network;
    }
}
