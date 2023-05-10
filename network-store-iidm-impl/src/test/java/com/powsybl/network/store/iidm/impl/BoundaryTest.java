/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BoundaryTest {

    private static final double EPS1 = Math.pow(10, -1);
    private static final double EPS2 = Math.pow(10, -2);

    @Test
    public void danglingLineTest() {
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), new NetworkFactoryImpl(), null);
        DanglingLine dl = network.getDanglingLine("a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4");
        assertNotNull(dl);
        dl.getTerminal().setP(-102.2);
        dl.getTerminal().setQ(25.1);
        dl.getTerminal().getBusView().getBus().setV(226.3);
        dl.getTerminal().getBusView().getBus().setAngle(1.1);
        Boundary boundary = dl.getBoundary();
        assertNotNull(boundary);
        assertEquals(26.8, boundary.getP(), EPS1);
        assertEquals(-1.48, boundary.getQ(), EPS1);
        assertEquals(226.39, boundary.getV(), EPS1);
        assertEquals(3.05, boundary.getAngle(), EPS2);
        //assertSame(dl, boundary.getConnectable());
        //assertNull(boundary.getSide());
    }

    @Test
    public void tieLineTest() {
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), null);
        TieLine tieLine = network.getTieLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc");
        tieLine.getDanglingLine1().getTerminal().setP(-26);
        tieLine.getDanglingLine1().getTerminal().setQ(193);
        tieLine.getDanglingLine1().getTerminal().getBusView().getBus().setV(411.3);
        tieLine.getDanglingLine1().getTerminal().getBusView().getBus().setAngle(0);
        tieLine.getDanglingLine2().getTerminal().setP(40);
        tieLine.getDanglingLine2().getTerminal().setQ(-214);
        tieLine.getDanglingLine2().getTerminal().getBusView().getBus().setV(427.1);
        tieLine.getDanglingLine2().getTerminal().getBusView().getBus().setAngle(0.2);
        Boundary boundary1 = tieLine.getDanglingLine1().getBoundary();
        Boundary boundary2 = tieLine.getDanglingLine2().getBoundary();
        assertNotNull(boundary1);
        assertNotNull(boundary2);
        assertEquals(33.15, boundary1.getP(), EPS1);
        assertEquals(-202.57, boundary1.getQ(), EPS1);
        assertEquals(408.3, boundary1.getV(), EPS1);
        assertEquals(0.09, boundary1.getAngle(), EPS2);
        assertEquals(-33.46, boundary2.getP(), EPS1);
        assertEquals(203.62, boundary2.getQ(), EPS1);
        assertEquals(430.1, boundary2.getV(), EPS1);
        assertEquals(0.1, boundary2.getAngle(), EPS2);
        //assertSame(tieLine.getDanglingLine2(), boundary2.getConnectable());
        //assertEquals(Branch.Side.TWO, boundary2.getSide());
    }
}
