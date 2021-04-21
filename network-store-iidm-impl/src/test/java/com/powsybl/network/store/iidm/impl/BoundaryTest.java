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

    private static final double EPS = Math.pow(10, -1);

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
        assertEquals(106.1, boundary.getP(), EPS);
        assertEquals(-10.8, boundary.getQ(), EPS);
        assertEquals(222.9, boundary.getV(), EPS);
        assertEquals(9.3, boundary.getAngle(), EPS);
    }

    @Test
    public void test2() {
        Network network = Importers.getImporter("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), null);
        Line line = network.getLine("_e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc + _b18cd1aa-7808-49b9-a7cf-605eaf07b006");
        assertNotNull(line);
        assertTrue(line.isTieLine());
        TieLine tieLine = (TieLine) line;
        assertNotNull(tieLine.getHalf1().getBoundary());
        assertNotNull(tieLine.getHalf2().getBoundary());
    }
}
