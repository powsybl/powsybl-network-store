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
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VariantManagerConstants;
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

    @Test
    public void testSubnetwork() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        network.createSubnetwork("subNetwork1", "subNetwork1", "");
        network.createSubnetwork("subNetwork2", "subNetwork2", "");
        Network subNetwork1 = network.getSubnetwork("subNetwork1");
        Substation s3 = subNetwork1.newSubstation()
                .setId("S3")
                .setCountry(Country.FR)
                .add();
        Network subNetwork2 = network.getSubnetwork("subNetwork2");
        Substation s4 = subNetwork2.newSubstation()
                .setId("S4")
                .setCountry(Country.FR)
                .add();
        assertEquals(4, network.getSubstationCount());
        assertEquals(1, subNetwork1.getSubstationCount());
        assertNotNull(subNetwork1.getSubstation("S3"));
        assertEquals(1, subNetwork2.getSubstationCount());
        assertNotNull(subNetwork2.getSubstation("S4"));
    }

    @Test
    public void simpleMergeTest() {

        Network n1 = CreateNetworksUtil.createBusBreakerNetworkWithLine("_n1");
        Network n2 = CreateNetworksUtil.createBusBreakerNetworkWithLine("_n2");
        String n1Id = n1.getId();
        String n2Id = n2.getId();
        assertEquals(1, n1.getLineCount());
        assertEquals(1, n2.getLineCount());

        Network mergedNetwork = Network.merge(n1, n2);
        assertEquals(2, mergedNetwork.getLineCount());
        assertEquals(2, mergedNetwork.getSubnetworks().size());

        Network subN1 = mergedNetwork.getSubnetwork(n1Id);
        Network subN2 = mergedNetwork.getSubnetwork(n2Id);
        assertEquals(1, subN1.getLineCount());
        assertEquals(1, subN2.getLineCount());

        Network detachedN2 = subN2.detach();
        assertEquals(1, detachedN2.getLineCount());
        assertEquals(1, mergedNetwork.getSubnetworks().size());
    }

    @Test
    public void subnetworkWithVariant() {
        Network n1 = CreateNetworksUtil.createBusBreakerNetworkWithLine("_n1");
        assertEquals(2, n1.getGeneratorCount());
        n1.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "v");
        n1.getVariantManager().setWorkingVariant("v");
        Network subnetwork = n1.createSubnetwork("subNetwork1", "subNetwork1", "");
        assertEquals(1, n1.getSubnetworks().size());
        n1.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(0, n1.getSubnetworks().size());
    }
}
