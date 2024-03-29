/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class NetworkTest {

    @Test
    public void testBusBreakerComponent() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();

        assertEquals(1, network.getSubstationStream().count());
        assertEquals(2, network.getVoltageLevelStream().count());
        assertEquals(2, network.getSwitchCount());
        assertEquals(0, network.getBusbarSectionCount());
        assertEquals(1, network.getLineCount());
        assertEquals(1, network.getCountryCount());

        assertEquals(1, Iterables.size(network.getSubstations(Country.FR, "TSO1", "region1")));

        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(1, network.getBusView().getSynchronousComponents().size());
        assertNotNull(network.getBusView().getBus("VL1_0"));
        assertNotNull(network.getBusView().getBus("VL2_0"));
    }

    @Test
    public void testNodeBreakerComponent() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        assertEquals(2, network.getSubstationStream().count());
        assertEquals(2, network.getVoltageLevelStream().count());
        assertEquals(7, network.getSwitchCount());
        assertEquals(2, network.getBusbarSectionCount());
        assertEquals(1, network.getLineCount());
        assertEquals(1, network.getCountryCount());

        assertEquals(1, Iterables.size(network.getSubstations(Country.FR, "TSO1", "region11")));
        assertEquals(1, Iterables.size(network.getSubstations(Country.FR, "TSO1", "region12")));
        assertEquals(1, Iterables.size(network.getSubstations(Country.FR, "TSO2", "region2")));

        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(1, network.getBusView().getSynchronousComponents().size());
        assertNotNull(network.getBusView().getBus("VL1_0"));
        assertNotNull(network.getBusView().getBus("VL2_0"));
    }
}
