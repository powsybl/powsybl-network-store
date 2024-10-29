/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractThreeWindingsTransformerTest;
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
    @Override
    public void baseTests() {
        // FIXME : remove this test when we use the release containing this PR : https://github.com/powsybl/powsybl-core/pull/3019
    }

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
    void testPhaseTapChangerEqualsAndHashCode() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        Set<RatioTapChanger> ratioTapChangers = new HashSet<>();
        ratioTapChangers.add(twt.getLeg2().getRatioTapChanger());
        ratioTapChangers.add(twt.getLeg2().getRatioTapChanger());
        ratioTapChangers.remove(twt.getLeg3().getRatioTapChanger());

        assertEquals(1, ratioTapChangers.size());

        // use the equals and the hashcode of PhaseTapChangerImpl
        ratioTapChangers.remove(twt.getLeg2().getRatioTapChanger());

        assertEquals(0, ratioTapChangers.size());
        assertTrue(twt.getLeg2().getRatioTapChanger().equals(twt.getLeg2().getRatioTapChanger()));
        assertFalse(twt.getLeg2().getRatioTapChanger().equals(null));
        assertFalse(twt.getLeg2().getRatioTapChanger().equals(twt.getLeg3().getRatioTapChanger()));
    }
}
