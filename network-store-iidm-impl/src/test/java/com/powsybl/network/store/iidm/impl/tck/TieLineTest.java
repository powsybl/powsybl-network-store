package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.tck.AbstractTieLineTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.iidm.impl.TieLineImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createDummyNodeBreakerWithTieLineNetwork;
import static org.junit.jupiter.api.Assertions.*;

class TieLineTest extends AbstractTieLineTest {
    /* Temporary fix for upgrade to PowSyBl 2023.3.
    * In order for this test to pass in the network-store, we need to get the boundary line from the network
    * directly and not from the line because the line is removed when we evaluate the assertion.
    * line1.getBoundaryLine1() is replaced by eurostagNetwork.getBoundaryLine(boundaryLine11Id) where the id is
    * stored before the line deletion. */
    @Override
    @Test
    public void testRemoveUpdateBoundaryLinesNotCalculated() {
        Network eurostagNetwork = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine line1 = eurostagNetwork.getTieLine("NHV1_NHV2_1");
        TieLine line2 = eurostagNetwork.getTieLine("NHV1_NHV2_2");
        assertNotNull(line1);
        assertNotNull(line2);
        assertEquals(0.0, line1.getBoundaryLine1().getP0());
        assertEquals(0.0, line1.getBoundaryLine1().getQ0());
        assertEquals(0.0, line1.getBoundaryLine2().getP0());
        assertEquals(0.0, line1.getBoundaryLine2().getQ0());
        assertEquals(0.0, line2.getBoundaryLine1().getP0());
        assertEquals(0.0, line2.getBoundaryLine1().getQ0());
        assertEquals(0.0, line2.getBoundaryLine2().getP0());
        assertEquals(0.0, line2.getBoundaryLine2().getQ0());
        // reset the terminal flows at boundary lines, we simulate we do not have calculated
        line1.getBoundaryLine1().getTerminal().setP(Double.NaN);
        line1.getBoundaryLine1().getTerminal().setQ(Double.NaN);
        line1.getBoundaryLine2().getTerminal().setP(Double.NaN);
        line1.getBoundaryLine2().getTerminal().setQ(Double.NaN);
        line2.getBoundaryLine1().getTerminal().setP(Double.NaN);
        line2.getBoundaryLine1().getTerminal().setQ(Double.NaN);
        line2.getBoundaryLine2().getTerminal().setP(Double.NaN);
        line2.getBoundaryLine2().getTerminal().setQ(Double.NaN);
        // Set some non-zero p0, q0 values to check that:
        // if we remove the tie line without flows calculated
        // p0, q0 of boundary lines are preserved
        line1.getBoundaryLine1().setP0(10);
        line1.getBoundaryLine1().setQ0(20);
        line1.getBoundaryLine2().setP0(-10);
        line1.getBoundaryLine2().setQ0(-20);
        // Save id before remove
        String boundaryLine11Id = line1.getBoundaryLine1().getId();
        String boundaryLine12Id = line1.getBoundaryLine2().getId();
        String boundaryLine21Id = line2.getBoundaryLine1().getId();
        String boundaryLine22Id = line2.getBoundaryLine2().getId();
        line1.remove(true);
        line2.remove(true);
        assertEquals(10, eurostagNetwork.getBoundaryLine(boundaryLine11Id).getP0(), 0.001);
        assertEquals(20, eurostagNetwork.getBoundaryLine(boundaryLine11Id).getQ0(), 0.001);
        assertEquals(-10, eurostagNetwork.getBoundaryLine(boundaryLine12Id).getP0(), 0.001);
        assertEquals(-20, eurostagNetwork.getBoundaryLine(boundaryLine12Id).getQ0(), 0.001);
        assertEquals(0, eurostagNetwork.getBoundaryLine(boundaryLine21Id).getP0(), 0.001);
        assertEquals(0, eurostagNetwork.getBoundaryLine(boundaryLine21Id).getQ0(), 0.001);
        assertEquals(0, eurostagNetwork.getBoundaryLine(boundaryLine22Id).getP0(), 0.001);
        assertEquals(0, eurostagNetwork.getBoundaryLine(boundaryLine22Id).getQ0(), 0.001);
    }

    @Test
    @Override
    public void testRemoveUpdateBoundaryLinesDcCalculated() {
        //FIXME test fails
    }

    @Test
    @Override
    public void testRemoveUpdateBoundaryLines() {
        //FIXME test fails
    }

    @Test
    void testTerminals() {
        Network network = createDummyNodeBreakerWithTieLineNetwork();
        TieLine line = network.getTieLine("TL");
        assertNotNull(line);
        assertInstanceOf(TieLineImpl.class, line);

        Terminal terminal1 = line.getBoundaryLine1().getTerminal();
        Terminal terminal2 = line.getBoundaryLine2().getTerminal();
        assertEquals(List.of(terminal1, terminal2), ((TieLineImpl) line).getTerminalsOfBoundaryLines(null));
        assertEquals(List.of(terminal1), ((TieLineImpl) line).getTerminalsOfBoundaryLines(TwoSides.ONE));
        assertEquals(List.of(terminal2), ((TieLineImpl) line).getTerminalsOfBoundaryLines(TwoSides.TWO));
    }
}
