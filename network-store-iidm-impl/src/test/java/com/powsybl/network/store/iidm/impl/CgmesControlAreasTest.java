/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CgmesControlAreasTest {

    @Test
    void test() {
        Network network = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource());
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        CgmesControlArea cgmesControlArea = cgmesControlAreas.getCgmesControlArea("50487bb8-be6d-42a8-9358-cc0bbfe6cfa7");
        assertEquals(236.9798, cgmesControlArea.getNetInterchange());
        cgmesControlArea.setNetInterchange(100);
        assertEquals(100, cgmesControlArea.getNetInterchange());
        assertEquals(10, cgmesControlArea.getPTolerance());
        cgmesControlArea.setPTolerance(11);
        assertEquals(11, cgmesControlArea.getPTolerance());
    }
}
