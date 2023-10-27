package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.tck.AbstractTieLineTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TieLineTest extends AbstractTieLineTest {
    /* Temporary fix for upgrade to PowSyBl 2023.3.
    * In order for this test to pass in the network-store, we need to get the dangling line from the network
    * directly and not from the line because the line is removed when we evaluate the assertion.
    * line1.getDanglingLine1() is replaced by eurostagNetwork.getDanglingLine(danglingLine11Id) where the id is
    * stored before the line deletion. */
    @Override
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

    /* Temporary fix for upgrade to PowSyBl 2023.3.
     * In order for this test to pass in the network-store, we need to get the dangling line from the network
     * directly and not from the line because the line is removed when we evaluate the assertion.
     * line1.getDanglingLine1() is replaced by eurostagNetwork.getDanglingLine(danglingLine11Id) where the id is
     * stored before the line deletion. */
    @Override
    public void testRemoveUpdateDanglingLinesDcCalculated() {
        Network eurostagNetwork = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine line1 = eurostagNetwork.getTieLine("NHV1_NHV2_1");
        TieLine line2 = eurostagNetwork.getTieLine("NHV1_NHV2_2");
        Assertions.assertNotNull(line1);
        Assertions.assertNotNull(line2);
        Assertions.assertEquals(0.0, line1.getDanglingLine1().getP0());
        Assertions.assertEquals(0.0, line1.getDanglingLine1().getQ0());
        Assertions.assertEquals(0.0, line1.getDanglingLine2().getP0());
        Assertions.assertEquals(0.0, line1.getDanglingLine2().getQ0());
        Assertions.assertEquals(0.0, line2.getDanglingLine1().getP0());
        Assertions.assertEquals(0.0, line2.getDanglingLine1().getQ0());
        Assertions.assertEquals(0.0, line2.getDanglingLine2().getP0());
        Assertions.assertEquals(0.0, line2.getDanglingLine2().getQ0());
        line1.getDanglingLine1().getTerminal().setQ(Double.NaN);
        line1.getDanglingLine2().getTerminal().setQ(Double.NaN);
        line2.getDanglingLine1().getTerminal().setQ(Double.NaN);
        line2.getDanglingLine2().getTerminal().setQ(Double.NaN);
        line1.getDanglingLine1().setP0(10.0);
        line1.getDanglingLine1().setQ0(20.0);
        line1.getDanglingLine2().setP0(-10.0);
        line1.getDanglingLine2().setQ0(-20.0);
        // Save id before remove
        String danglingLine11Id = line1.getDanglingLine1().getId();
        String danglingLine12Id = line1.getDanglingLine2().getId();
        String danglingLine21Id = line2.getDanglingLine1().getId();
        String danglingLine22Id = line2.getDanglingLine2().getId();
        line1.remove(true);
        line2.remove(true);
        Assertions.assertEquals(302.444, eurostagNetwork.getDanglingLine(danglingLine11Id).getP0(), 0.001);
        Assertions.assertEquals(20.0, eurostagNetwork.getDanglingLine(danglingLine11Id).getQ0(), 0.001);
        Assertions.assertEquals(-300.434, eurostagNetwork.getDanglingLine(danglingLine12Id).getP0(), 0.001);
        Assertions.assertEquals(-20.0, eurostagNetwork.getDanglingLine(danglingLine12Id).getQ0(), 0.001);
        Assertions.assertEquals(302.444, eurostagNetwork.getDanglingLine(danglingLine21Id).getP0(), 0.001);
        Assertions.assertEquals(0.0, eurostagNetwork.getDanglingLine(danglingLine21Id).getQ0(), 0.001);
        Assertions.assertEquals(-300.434, eurostagNetwork.getDanglingLine(danglingLine22Id).getP0(), 0.001);
        Assertions.assertEquals(0.0, eurostagNetwork.getDanglingLine(danglingLine22Id).getQ0(), 0.001);
    }

    /* Temporary fix for upgrade to PowSyBl 2023.3.
     * In order for this test to pass in the network-store, we need to get the dangling line from the network
     * directly and not from the line because the line is removed when we evaluate the assertion.
     * line1.getDanglingLine1() is replaced by eurostagNetwork.getDanglingLine(danglingLine11Id) where the id is
     * stored before the line deletion. */
    @Override
    public void testRemoveUpdateDanglingLines() {
        Network eurostagNetwork = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine line1 = eurostagNetwork.getTieLine("NHV1_NHV2_1");
        TieLine line2 = eurostagNetwork.getTieLine("NHV1_NHV2_2");
        Assertions.assertNotNull(line1);
        Assertions.assertNotNull(line2);
        Assertions.assertEquals(0.0, line1.getDanglingLine1().getP0());
        Assertions.assertEquals(0.0, line1.getDanglingLine1().getQ0());
        Assertions.assertEquals(0.0, line1.getDanglingLine2().getP0());
        Assertions.assertEquals(0.0, line1.getDanglingLine2().getQ0());
        Assertions.assertEquals(0.0, line2.getDanglingLine1().getP0());
        Assertions.assertEquals(0.0, line2.getDanglingLine1().getQ0());
        Assertions.assertEquals(0.0, line2.getDanglingLine2().getP0());
        Assertions.assertEquals(0.0, line2.getDanglingLine2().getQ0());
        // Save id before remove
        String danglingLine11Id = line1.getDanglingLine1().getId();
        String danglingLine12Id = line1.getDanglingLine2().getId();
        String danglingLine21Id = line2.getDanglingLine1().getId();
        String danglingLine22Id = line2.getDanglingLine2().getId();
        line1.remove(true);
        line2.remove(true);
        Assertions.assertEquals(301.316, eurostagNetwork.getDanglingLine(danglingLine11Id).getP0(), 0.001);
        Assertions.assertEquals(116.525, eurostagNetwork.getDanglingLine(danglingLine11Id).getQ0(), 0.001);
        Assertions.assertEquals(-301.782, eurostagNetwork.getDanglingLine(danglingLine12Id).getP0(), 0.001);
        Assertions.assertEquals(-116.442, eurostagNetwork.getDanglingLine(danglingLine12Id).getQ0(), 0.001);
        Assertions.assertEquals(301.316, eurostagNetwork.getDanglingLine(danglingLine21Id).getP0(), 0.001);
        Assertions.assertEquals(116.525, eurostagNetwork.getDanglingLine(danglingLine21Id).getQ0(), 0.001);
        Assertions.assertEquals(-301.782, eurostagNetwork.getDanglingLine(danglingLine22Id).getP0(), 0.001);
        Assertions.assertEquals(-116.442, eurostagNetwork.getDanglingLine(danglingLine22Id).getQ0(), 0.001);
    }
}
