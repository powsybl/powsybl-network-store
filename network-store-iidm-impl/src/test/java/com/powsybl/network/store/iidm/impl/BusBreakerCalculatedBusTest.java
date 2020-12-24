/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractCalculatedTopologyTest;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class BusBreakerCalculatedBusTest extends AbstractCalculatedTopologyTest {

    @Test
    public void equalsContract() {
        NetworkObjectIndex index1 = new NetworkObjectIndex(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()));
        NetworkObjectIndex index2 = new NetworkObjectIndex(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()));
        EqualsVerifier.simple().forClass(CalculatedBus.class).withIgnoredFields("connectedComponent", "synchronousComponent")
                .withPrefabValues(NetworkObjectIndex.class, index1, index2)
                .withPrefabValues(ComponentImpl.class, new ComponentImpl(null, null), new ComponentImpl(null, null))
                .verify();
    }

    @Test
    public void testCalculatedBusesWithMultiBuses() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithMultiBuses();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        List<Bus> calculatedBuses = vl1.getBusView().getBusStream().collect(Collectors.toList());
        List<Bus> configurededBuses = vl1.getBusBreakerView().getBusStream().collect(Collectors.toList());
        assertEquals(0, calculatedBuses.size());
        assertEquals(3, configurededBuses.size());

        assertEquals(0, configurededBuses.stream().filter(b -> b instanceof CalculatedBus).count());

        assertNull(vl1.getBusView().getMergedBus("B1"));
        assertNull(vl1.getBusView().getMergedBus("B2"));
        assertNull(vl1.getBusView().getMergedBus("B3"));
        assertNull(vl1.getBusView().getMergedBus("FOO"));
    }

    @Test
    public void testCalculatedBuses() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        List<Bus> calculatedBuses = vl1.getBusView().getBusStream().collect(Collectors.toList());
        List<Bus> configurededBuses = vl1.getBusBreakerView().getBusStream().collect(Collectors.toList());
        assertEquals(1, calculatedBuses.size());
        assertEquals(3, configurededBuses.size());

        assertEquals(0, calculatedBuses.stream().filter(b -> b instanceof ConfiguredBusImpl).count());
        assertEquals(0, configurededBuses.stream().filter(b -> b instanceof CalculatedBus).count());

        assertEquals(3, vl1.getBusBreakerView().getBus("B1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B2").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B3").getConnectedTerminalCount());
        assertEquals(5, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());

        assertNotNull(vl1.getBusView().getMergedBus("B1"));
        assertNotNull(vl1.getBusView().getMergedBus("B2"));
        assertNotNull(vl1.getBusView().getMergedBus("B3"));
        assertNull(vl1.getBusView().getMergedBus("FOO"));
        assertEquals(2, vl1.getBusBreakerView().getSwitchCount());

        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        calculatedBuses = vl2.getBusView().getBusStream().collect(Collectors.toList());
        configurededBuses = vl2.getBusBreakerView().getBusStream().collect(Collectors.toList());
        assertEquals(1, calculatedBuses.size());
        assertEquals(1, configurededBuses.size());
        assertNotNull(vl2.getBusView().getMergedBus("B21"));
        assertNull(vl2.getBusView().getMergedBus("FOO"));
        assertEquals(0, vl2.getBusBreakerView().getSwitchCount());
    }

    @Test
    public void testCalculatedBusesSwitchRetain() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Switch s = vl1.getBusBreakerView().getSwitch("BR1");

        assertThrows(ValidationException.class, () -> s.setRetained(true));
    }

    @Test
    public void testCalculatedBusesSwitch1() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        vl1.getBusBreakerView().getSwitch("BR1").setOpen(true);
        vl1.getBusBreakerView().getSwitch("BR2").setOpen(false);

        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());

        assertEquals(3, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(3, vl1.getBusBreakerView().getBus("B1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B2").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B3").getConnectedTerminalCount());

        assertNotNull(vl1.getBusView().getMergedBus("B1"));
        assertNull(vl1.getBusView().getMergedBus("B2"));
        assertNull(vl1.getBusView().getMergedBus("B3"));
    }

    @Test
    public void testCalculatedBusesSwitch2() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        vl1.getBusBreakerView().getSwitch("BR1").setOpen(false);
        vl1.getBusBreakerView().getSwitch("BR2").setOpen(true);

        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());

        assertEquals(4, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(3, vl1.getBusBreakerView().getBus("B1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B2").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B3").getConnectedTerminalCount());

        assertNotNull(vl1.getBusView().getMergedBus("B1"));
        assertNotNull(vl1.getBusView().getMergedBus("B2"));
        assertNull(vl1.getBusView().getMergedBus("B3"));
    }

    @Test
    public void testCalculatedBusesSwitch3() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        vl1.getBusBreakerView().getSwitch("BR1").setOpen(true);
        vl1.getBusBreakerView().getSwitch("BR2").setOpen(true);

        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBusStream().count());

        assertEquals(3, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(3, vl1.getBusBreakerView().getBus("B1").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B2").getConnectedTerminalCount());
        assertEquals(1, vl1.getBusBreakerView().getBus("B3").getConnectedTerminalCount());

        assertNotNull(vl1.getBusView().getMergedBus("B1"));
        assertNull(vl1.getBusView().getMergedBus("B2"));
        assertNull(vl1.getBusView().getMergedBus("B3"));
    }

    @Test
    public void testBusViewTerminals() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        assertTrue(((CalculatedBus) vl1.getBusView().getBus("VL1_0")).isBusView());
        assertEquals(5, ((BaseBus) vl1.getBusView().getBus("VL1_0")).getAllTerminalsStream().count());
        assertEquals(5, vl1.getBusView().getBus("VL1_0").getConnectedTerminalStream().count());

        assertEquals(1, vl1.getBusView().getBus("VL1_0").getLineStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getThreeWindingsTransformerStream().count());
        assertEquals(1, vl1.getBusView().getBus("VL1_0").getGeneratorStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getBatteryStream().count());
        assertEquals(3, vl1.getBusView().getBus("VL1_0").getLoadStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getShuntCompensatorStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getDanglingLineStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getStaticVarCompensatorStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getLccConverterStationStream().count());
        assertEquals(0, vl1.getBusView().getBus("VL1_0").getVscConverterStationStream().count());

        CreateNetworksUtil.disconnectAllTerminalsVoltageLevel(vl1);
        assertEquals(0, vl1.getBusView().getBusStream().count());

        assertTrue(((CalculatedBus) vl2.getBusView().getBus("VL2_0")).isBusView());
        assertEquals(2, ((BaseBus) vl2.getBusView().getBus("VL2_0")).getAllTerminalsStream().count());
        assertEquals(2, vl2.getBusView().getBus("VL2_0").getConnectedTerminalStream().count());

        assertEquals(1, vl2.getBusView().getBus("VL2_0").getLineStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getThreeWindingsTransformerStream().count());
        assertEquals(1, vl2.getBusView().getBus("VL2_0").getGeneratorStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getBatteryStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getLoadStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getShuntCompensatorStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getDanglingLineStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getStaticVarCompensatorStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getLccConverterStationStream().count());
        assertEquals(0, vl2.getBusView().getBus("VL2_0").getVscConverterStationStream().count());
    }

    @Test
    public void testBusBreakerViewTerminals() {
        Network network = CreateNetworksUtil.createBusBreakerNetworkWithLine();

        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        assertEquals(3, ((BaseBus) vl1.getBusBreakerView().getBus("B1")).getAllTerminalsStream().count());
        assertEquals(3, vl1.getBusBreakerView().getBus("B1").getConnectedTerminalStream().count());

        assertEquals(1, vl1.getBusBreakerView().getBus("B1").getLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getThreeWindingsTransformerStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("B1").getGeneratorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getBatteryStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("B1").getLoadStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getShuntCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getDanglingLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getStaticVarCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getLccConverterStationStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getVscConverterStationStream().count());

        assertEquals(1, ((BaseBus) vl1.getBusBreakerView().getBus("B2")).getAllTerminalsStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("B2").getConnectedTerminalStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("B2").getLoadStream().count());

        assertEquals(1, ((BaseBus) vl1.getBusBreakerView().getBus("B3")).getAllTerminalsStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("B3").getConnectedTerminalStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("B3").getLoadStream().count());

        CreateNetworksUtil.disconnectAllTerminalsVoltageLevel(vl1);
        assertEquals(0, vl1.getBusBreakerView().getBus("B1").getConnectedTerminalStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B2").getConnectedTerminalStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("B3").getConnectedTerminalStream().count());
    }

    @Test
    public void testBusBreakerViewTerminalsVl1() {
        Network network = CreateNetworksUtil.createBusBreakerNetwokWithMultipleEquipments();

        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        assertEquals(6, ((BaseBus) vl1.getBusBreakerView().getBus("BUS1")).getAllTerminalsStream().count());

        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getConnectedTerminalStream().count());

        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getThreeWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getGeneratorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getBatteryStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getLoadStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getShuntCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getDanglingLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getStaticVarCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getLccConverterStationStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getVscConverterStationStream().count());

        CreateNetworksUtil.connectAllTerminalsVoltageLevel(vl1);

        assertEquals(6, vl1.getBusBreakerView().getBus("BUS1").getConnectedTerminalStream().count());

        assertEquals(1, vl1.getBusBreakerView().getBus("BUS1").getLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getThreeWindingsTransformerStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getGeneratorStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("BUS1").getBatteryStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getLoadStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("BUS1").getShuntCompensatorStream().count());
        assertEquals(2, vl1.getBusBreakerView().getBus("BUS1").getDanglingLineStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getStaticVarCompensatorStream().count());
        assertEquals(0, vl1.getBusBreakerView().getBus("BUS1").getLccConverterStationStream().count());
        assertEquals(1, vl1.getBusBreakerView().getBus("BUS1").getVscConverterStationStream().count());
    }

    @Test
    public void testBusBreakerViewTerminalsVl2() {
        Network network = CreateNetworksUtil.createBusBreakerNetwokWithMultipleEquipments();

        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        assertEquals(6, ((BaseBus) vl2.getBusBreakerView().getBus("BUS2")).getAllTerminalsStream().count());

        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getConnectedTerminalStream().count());

        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getLineStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getTwoWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getThreeWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getGeneratorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getBatteryStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getLoadStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getShuntCompensatorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getDanglingLineStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getStaticVarCompensatorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getLccConverterStationStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getVscConverterStationStream().count());

        CreateNetworksUtil.connectAllTerminalsVoltageLevel(vl2);

        assertEquals(6, vl2.getBusBreakerView().getBus("BUS2").getConnectedTerminalStream().count());

        assertEquals(1, vl2.getBusBreakerView().getBus("BUS2").getLineStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getTwoWindingsTransformerStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("BUS2").getThreeWindingsTransformerStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getGeneratorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getBatteryStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getLoadStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("BUS2").getShuntCompensatorStream().count());
        assertEquals(0, vl2.getBusBreakerView().getBus("BUS2").getDanglingLineStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("BUS2").getStaticVarCompensatorStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("BUS2").getLccConverterStationStream().count());
        assertEquals(1, vl2.getBusBreakerView().getBus("BUS2").getVscConverterStationStream().count());
    }

    @Test
    public void testBusBreakerViewTerminalsOtherVls() {
        Network network = CreateNetworksUtil.createBusBreakerNetwokWithMultipleEquipments();

        VoltageLevel vl3 = network.getVoltageLevel("VL3");
        VoltageLevel vl4 = network.getVoltageLevel("VL4");
        VoltageLevel vl5 = network.getVoltageLevel("VL5");
        VoltageLevel vl6 = network.getVoltageLevel("VL6");

        assertEquals(1, ((BaseBus) vl3.getBusBreakerView().getBus("BUS3")).getAllTerminalsStream().count());
        assertEquals(0, vl3.getBusBreakerView().getBus("BUS3").getConnectedTerminalStream().count());
        CreateNetworksUtil.connectAllTerminalsVoltageLevel(vl3);
        assertEquals(1, vl3.getBusBreakerView().getBus("BUS3").getConnectedTerminalStream().count());
        assertEquals(1, vl3.getBusBreakerView().getBus("BUS3").getThreeWindingsTransformerStream().count());

        assertEquals(1, ((BaseBus) vl4.getBusBreakerView().getBus("BUS4")).getAllTerminalsStream().count());
        assertEquals(0, vl4.getBusBreakerView().getBus("BUS4").getConnectedTerminalStream().count());
        CreateNetworksUtil.connectAllTerminalsVoltageLevel(vl4);
        assertEquals(1, vl4.getBusBreakerView().getBus("BUS4").getConnectedTerminalStream().count());
        assertEquals(1, vl4.getBusBreakerView().getBus("BUS4").getThreeWindingsTransformerStream().count());

        assertEquals(1, ((BaseBus) vl5.getBusBreakerView().getBus("BUS5")).getAllTerminalsStream().count());
        assertEquals(0, vl5.getBusBreakerView().getBus("BUS5").getConnectedTerminalStream().count());
        CreateNetworksUtil.connectAllTerminalsVoltageLevel(vl5);
        assertEquals(1, vl5.getBusBreakerView().getBus("BUS5").getConnectedTerminalStream().count());
        assertEquals(1, vl5.getBusBreakerView().getBus("BUS5").getTwoWindingsTransformerStream().count());

        assertEquals(1, ((BaseBus) vl6.getBusBreakerView().getBus("BUS6")).getAllTerminalsStream().count());
        assertEquals(0, vl6.getBusBreakerView().getBus("BUS6").getConnectedTerminalStream().count());
        CreateNetworksUtil.connectAllTerminalsVoltageLevel(vl6);
        assertEquals(1, vl6.getBusBreakerView().getBus("BUS6").getConnectedTerminalStream().count());
        assertEquals(1, vl6.getBusBreakerView().getBus("BUS6").getTwoWindingsTransformerStream().count());
    }
}
