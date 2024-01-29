/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.OperatingStatusAdder;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class OperatingStatusExtensionTest {

    @Test
    public void testLineOperatingStatusExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        Line line = network.getLine("LINE1");
        assertNotNull(line);

        assertNull(line.getExtension(OperatingStatus.class));
        assertNull(line.getExtensionByName("operatingStatus"));
        assertEquals(0, line.getExtensions().size());

        assertThrows(NullPointerException.class, () -> line.newExtension(OperatingStatusAdder.class).withStatus(null).add());
        line.newExtension(OperatingStatusAdder.class).withStatus(OperatingStatus.Status.PLANNED_OUTAGE).add();

        OperatingStatus brs = line.getExtension(OperatingStatus.class);
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.PLANNED_OUTAGE, brs.getStatus());

        brs = line.getExtensionByName("operatingStatus");
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.PLANNED_OUTAGE, brs.getStatus());

        assertEquals(1, line.getExtensions().size());
    }

    @Test
    public void testTwoWindingsTransformerOperatingStatusExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TwoWT1");
        assertNotNull(twt);

        assertNull(twt.getExtension(OperatingStatus.class));
        assertEquals(0, twt.getExtensions().size());

        assertThrows(NullPointerException.class, () -> twt.newExtension(OperatingStatusAdder.class).withStatus(null).add());
        twt.newExtension(OperatingStatusAdder.class).withStatus(OperatingStatus.Status.FORCED_OUTAGE).add();

        OperatingStatus brs = twt.getExtension(OperatingStatus.class);
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.FORCED_OUTAGE, brs.getStatus());
        brs = twt.getExtensionByName("operatingStatus");
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.FORCED_OUTAGE, brs.getStatus());

        assertEquals(1, twt.getExtensions().size());
    }

    @Test
    public void testThreeWindingsTransformerOperatingStatusExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("TWT1");
        assertNotNull(twt);

        assertNull(twt.getExtension(OperatingStatus.class));
        assertEquals(0, twt.getExtensions().size());

        assertThrows(NullPointerException.class, () -> twt.newExtension(OperatingStatusAdder.class).withStatus(null).add());
        twt.newExtension(OperatingStatusAdder.class).withStatus(OperatingStatus.Status.IN_OPERATION).add();

        OperatingStatus brs = twt.getExtension(OperatingStatus.class);
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.IN_OPERATION, brs.getStatus());
        brs = twt.getExtensionByName("operatingStatus");
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.IN_OPERATION, brs.getStatus());

        assertEquals(1, twt.getExtensions().size());
    }
}
