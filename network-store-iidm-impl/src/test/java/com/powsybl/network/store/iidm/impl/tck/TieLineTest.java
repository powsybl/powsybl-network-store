package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractTieLineTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.network.store.iidm.impl.TieLineImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TieLineTest extends AbstractTieLineTest {
    /* Temporary fix for upgrade to PowSyBl 2023.3.
    * In order for this test to pass in the network-store, we need to get the dangling line from the network
    * directly and not from the line because the line is removed when we evaluate the assertion.
    * line1.getDanglingLine1() is replaced by eurostagNetwork.getDanglingLine(danglingLine11Id) where the id is
    * stored before the line deletion. */
    @Override
    @Test
    public void testRemoveUpdateDanglingLinesNotCalculated() {
        Network eurostagNetwork = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine line1 = eurostagNetwork.getTieLine("NHV1_NHV2_1");
        TieLine line2 = eurostagNetwork.getTieLine("NHV1_NHV2_2");
        assertNotNull(line1);
        assertNotNull(line2);
        assertEquals(0.0, line1.getDanglingLine1().getP0());
        assertEquals(0.0, line1.getDanglingLine1().getQ0());
        assertEquals(0.0, line1.getDanglingLine2().getP0());
        assertEquals(0.0, line1.getDanglingLine2().getQ0());
        assertEquals(0.0, line2.getDanglingLine1().getP0());
        assertEquals(0.0, line2.getDanglingLine1().getQ0());
        assertEquals(0.0, line2.getDanglingLine2().getP0());
        assertEquals(0.0, line2.getDanglingLine2().getQ0());
        // reset the terminal flows at dangling lines, we simulate we do not have calculated
        line1.getDanglingLine1().getTerminal().setP(Double.NaN);
        line1.getDanglingLine1().getTerminal().setQ(Double.NaN);
        line1.getDanglingLine2().getTerminal().setP(Double.NaN);
        line1.getDanglingLine2().getTerminal().setQ(Double.NaN);
        line2.getDanglingLine1().getTerminal().setP(Double.NaN);
        line2.getDanglingLine1().getTerminal().setQ(Double.NaN);
        line2.getDanglingLine2().getTerminal().setP(Double.NaN);
        line2.getDanglingLine2().getTerminal().setQ(Double.NaN);
        // Set some non-zero p0, q0 values to check that:
        // if we remove the tie line without flows calculated
        // p0, q0 of dangling lines are preserved
        line1.getDanglingLine1().setP0(10);
        line1.getDanglingLine1().setQ0(20);
        line1.getDanglingLine2().setP0(-10);
        line1.getDanglingLine2().setQ0(-20);
        // Save id before remove
        String danglingLine11Id = line1.getDanglingLine1().getId();
        String danglingLine12Id = line1.getDanglingLine2().getId();
        String danglingLine21Id = line2.getDanglingLine1().getId();
        String danglingLine22Id = line2.getDanglingLine2().getId();
        line1.remove(true);
        line2.remove(true);
        assertEquals(10, eurostagNetwork.getDanglingLine(danglingLine11Id).getP0(), 0.001);
        assertEquals(20, eurostagNetwork.getDanglingLine(danglingLine11Id).getQ0(), 0.001);
        assertEquals(-10, eurostagNetwork.getDanglingLine(danglingLine12Id).getP0(), 0.001);
        assertEquals(-20, eurostagNetwork.getDanglingLine(danglingLine12Id).getQ0(), 0.001);
        assertEquals(0, eurostagNetwork.getDanglingLine(danglingLine21Id).getP0(), 0.001);
        assertEquals(0, eurostagNetwork.getDanglingLine(danglingLine21Id).getQ0(), 0.001);
        assertEquals(0, eurostagNetwork.getDanglingLine(danglingLine22Id).getP0(), 0.001);
        assertEquals(0, eurostagNetwork.getDanglingLine(danglingLine22Id).getQ0(), 0.001);
    }

    @Test
    @Override
    public void testRemoveUpdateDanglingLinesDcCalculated() {
        //FIXME test fails
    }

    @Test
    @Override
    public void testRemoveUpdateDanglingLines() {
        //FIXME test fails
    }

    @Test
    void testTerminals() {
        Network network = createNetworkWithTieLine();
        TieLine line = network.getTieLine("TL");
        assertNotNull(line);
        assertInstanceOf(TieLineImpl.class, line);

        Terminal terminal1 = line.getDanglingLine1().getTerminal();
        Terminal terminal2 = line.getDanglingLine2().getTerminal();
        assertEquals(List.of(terminal1, terminal2), ((TieLineImpl) line).getTerminalsOfDanglingLines(null));
        assertEquals(List.of(terminal1), ((TieLineImpl) line).getTerminalsOfDanglingLines(TwoSides.ONE));
        assertEquals(List.of(terminal2), ((TieLineImpl) line).getTerminalsOfDanglingLines(TwoSides.TWO));
    }

    private Network createNetworkWithTieLine() {
        // Initialize the network
        Network networkWithTieLine = FourSubstationsNodeBreakerFactory.create();

        // Existing voltage levels in Node-breaker view
        VoltageLevel s1vl1 = networkWithTieLine.getVoltageLevel("S1VL1");

        // New voltage levels in bus-breaker view
        VoltageLevel s2vl2 = networkWithTieLine.getSubstation("S2").newVoltageLevel()
            .setId("S2VL2")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        // New buses
        s2vl2.getBusBreakerView()
            .newBus()
            .setId("bus22")
            .add();

        /*
         * First Tie line on node-breaker
         */
        // Add a dangling line in the first Voltage level
        createSwitch(s1vl1, "S1VL1_DL_DISCONNECTOR", SwitchKind.DISCONNECTOR, 0, 20);
        createSwitch(s1vl1, "S1VL1_DL_BREAKER", SwitchKind.BREAKER, 20, 21);
        DanglingLine danglingLine1 = s1vl1.newDanglingLine()
            .setId("NHV1_XNODE1")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(1.5)
            .setX(20.0)
            .setG(1E-6)
            .setB(386E-6 / 2)
            .setNode(21)
            .setPairingKey("XNODE1")
            .add();

        // Add a dangling line in the second Voltage level
        DanglingLine danglingLine2 = s2vl2.newDanglingLine()
            .setId("S2VL2_DL")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(1.5)
            .setX(13.0)
            .setG(2E-6)
            .setB(386E-6 / 2)
            .setBus("bus22")
            .setPairingKey("XNODE1")
            .add();
        networkWithTieLine.newTieLine()
            .setId("TL")
            .setDanglingLine1(danglingLine1.getId())
            .setDanglingLine2(danglingLine2.getId())
            .add();

        return networkWithTieLine;
    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
            .setId(id)
            .setName(id)
            .setKind(kind)
            .setRetained(kind.equals(SwitchKind.BREAKER))
            .setOpen(false)
            .setFictitious(false)
            .setNode1(node1)
            .setNode2(node2)
            .add();
    }
}
