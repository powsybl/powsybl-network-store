/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetworkWithLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class BusBarSectionTest {

    @Test
    void testTerminals() {
        Network network = createNodeBreakerNetworkWithLine();
        BusbarSection bbs = network.getBusbarSection("BBS1");
        assertNotNull(bbs);
        assertInstanceOf(BusbarSectionImpl.class, bbs);

        Terminal terminal = bbs.getTerminal();
        assertEquals(List.of(terminal), ((BusbarSectionImpl) bbs).getTerminals(null));
        assertEquals(List.of(terminal), ((BusbarSectionImpl) bbs).getTerminals(ThreeSides.ONE));
        assertEquals(Collections.emptyList(), ((BusbarSectionImpl) bbs).getTerminals(ThreeSides.TWO));
        assertEquals(Collections.emptyList(), ((BusbarSectionImpl) bbs).getTerminals(ThreeSides.THREE));
    }
}
