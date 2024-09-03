/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.tck.AbstractHvdcLineTest;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.network.store.iidm.impl.HvdcLineImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class HvdcLineTest extends AbstractHvdcLineTest {

    @Test
    void testTerminals() {
        Network network = HvdcTestNetwork.createVsc();
        HvdcLine line = network.getHvdcLine("L");
        assertNotNull(line);
        assertInstanceOf(HvdcLineImpl.class, line);

        Terminal terminal1 = line.getConverterStation1().getTerminal();
        Terminal terminal2 = line.getConverterStation2().getTerminal();
        assertEquals(List.of(terminal1, terminal2), ((HvdcLineImpl) line).getTerminalsOfConverterStations(null));
        assertEquals(List.of(terminal1), ((HvdcLineImpl) line).getTerminalsOfConverterStations(TwoSides.ONE));
        assertEquals(List.of(terminal2), ((HvdcLineImpl) line).getTerminalsOfConverterStations(TwoSides.TWO));
    }
}
