/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.iidm.import_.Importers;
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
    public void test() {
        Network network = Importers.getImporter("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), new NetworkFactoryImpl(), null);
        DanglingLine dl = network.getDanglingLine("_a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4");
        assertNotNull(dl);
        dl.getTerminal().setP(-102.2);
        dl.getTerminal().setQ(25.1);
        dl.getTerminal().getBusView().getBus().setV(226.3);
        dl.getTerminal().getBusView().getBus().setAngle(1.1);
        Boundary boundary = dl.getBoundary();
        assertNotNull(boundary);
        assertEquals(106.1, boundary.getP(), EPS1);
        assertEquals(-10.8, boundary.getQ(), EPS1);
        assertEquals(222.9, boundary.getV(), EPS1);
        assertEquals(9.38, boundary.getAngle(), EPS2);
    }

    @Test
    public void test2() {
        Network network = Importers.getImporter("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), null);
        Line line = network.getLine("_e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc + _b18cd1aa-7808-49b9-a7cf-605eaf07b006");
        assertNotNull(line);
        assertTrue(line.isTieLine());
        TieLine tieLine = (TieLine) line;
        tieLine.getTerminal1().setP(-26);
        tieLine.getTerminal1().setQ(193);
        tieLine.getTerminal1().getBusView().getBus().setV(411.3);
        tieLine.getTerminal1().getBusView().getBus().setAngle(0);
        tieLine.getTerminal2().setP(40);
        tieLine.getTerminal2().setQ(-214);
        tieLine.getTerminal2().getBusView().getBus().setV(427.1);
        tieLine.getTerminal2().getBusView().getBus().setAngle(0.2);
        Boundary boundary1 = tieLine.getHalf1().getBoundary();
        Boundary boundary2 = tieLine.getHalf2().getBoundary();
        assertNotNull(boundary1);
        assertNotNull(boundary2);
        assertEquals(31.9, boundary1.getP(), EPS1);
        assertEquals(-202.4, boundary1.getQ(), EPS1);
        assertEquals(408.3, boundary1.getV(), EPS1);
        assertEquals(0.09, boundary1.getAngle(), EPS2);
        assertEquals(-32.2, boundary2.getP(), EPS1);
        assertEquals(203.4, boundary2.getQ(), EPS1);
        assertEquals(430.1, boundary2.getV(), EPS1);
        assertEquals(0.1, boundary2.getAngle(), EPS2);
    }
}
