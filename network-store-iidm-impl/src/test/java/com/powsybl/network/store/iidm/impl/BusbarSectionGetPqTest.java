/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusbarSectionGetPqTest {

    @Test
    void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        BusbarSection bbs = network.getBusbarSection("S1VL1_BBS");
        Terminal terminal = bbs.getTerminal();
        assertEquals(0, terminal.getP());
        assertEquals(0, terminal.getQ());
        PowsyblException e = assertThrows(PowsyblException.class, () -> terminal.setP(1));
        assertEquals("Terminal of connectable 'S1VL1_BBS':  cannot set active power on a busbar section", e.getMessage());
        e = assertThrows(PowsyblException.class, () -> terminal.setQ(1));
        assertEquals("Terminal of connectable 'S1VL1_BBS':  cannot set reactive power on a busbar section", e.getMessage());
    }
}
