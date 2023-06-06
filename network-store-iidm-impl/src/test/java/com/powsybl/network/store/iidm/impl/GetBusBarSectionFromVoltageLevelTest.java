/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GetBusBarSectionFromVoltageLevelTest {

    @Test
    public void testGetBusBarSectionFromWrongVoltageLevel() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithTwoVoltageLevelsAndBusBarSections();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        BusbarSection bbs1 = vl1.getNodeBreakerView().getBusbarSection("BBS1");
        assertNotNull(bbs1);

        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        BusbarSection bbs2 = vl2.getNodeBreakerView().getBusbarSection("BBS2");
        assertNotNull(bbs2);

        BusbarSection notExistingBbs = vl1.getNodeBreakerView().getBusbarSection("unknownBBS");

        assertNull(notExistingBbs);

        BusbarSection bbs2FromVl1 = vl1.getNodeBreakerView().getBusbarSection("BBS2");
        BusbarSection bbs1FromVl2 = vl2.getNodeBreakerView().getBusbarSection("BBS1");

        assertNull(bbs2FromVl1);
        assertNull(bbs1FromVl2);
    }
}
