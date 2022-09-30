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
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.BranchStatusAdder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BranchStatusExtensionTest {

    @Test
    public void testLineBranchStatusExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        Line line = network.getLine("LINE1");
        assertNotNull(line);

        assertNull(line.getExtension(BranchStatus.class));
        assertNull(line.getExtensionByName("branchStatus"));
        assertEquals(0, line.getExtensions().size());

        assertThrows(NullPointerException.class, () -> line.newExtension(BranchStatusAdder.class).withStatus(null).add());
        line.newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.PLANNED_OUTAGE).add();

        BranchStatus brs = line.getExtension(BranchStatus.class);
        assertNotNull(brs);
        assertEquals(BranchStatus.Status.PLANNED_OUTAGE, brs.getStatus());

        brs = line.getExtensionByName("branchStatus");
        assertNotNull(brs);
        assertEquals(BranchStatus.Status.PLANNED_OUTAGE, brs.getStatus());

        assertEquals(1, line.getExtensions().size());
    }

    @Test
    public void testTwoWindingsTransformerBranchStatusExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TwoWT1");
        assertNotNull(twt);

        assertNull(twt.getExtension(BranchStatus.class));
        assertEquals(0, twt.getExtensions().size());

        assertThrows(NullPointerException.class, () -> twt.newExtension(BranchStatusAdder.class).withStatus(null).add());
        twt.newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.FORCED_OUTAGE).add();

        BranchStatus brs = twt.getExtension(BranchStatus.class);
        assertNotNull(brs);
        assertEquals(BranchStatus.Status.FORCED_OUTAGE, brs.getStatus());
        brs = twt.getExtensionByName("branchStatus");
        assertNotNull(brs);
        assertEquals(BranchStatus.Status.FORCED_OUTAGE, brs.getStatus());

        assertEquals(1, twt.getExtensions().size());
    }

    @Test
    public void testThreeWindingsTransformerBranchStatusExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("TWT1");
        assertNotNull(twt);

        assertNull(twt.getExtension(BranchStatus.class));
        assertEquals(0, twt.getExtensions().size());

        assertThrows(NullPointerException.class, () -> twt.newExtension(BranchStatusAdder.class).withStatus(null).add());
        twt.newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.IN_OPERATION).add();

        BranchStatus brs = twt.getExtension(BranchStatus.class);
        assertNotNull(brs);
        assertEquals(BranchStatus.Status.IN_OPERATION, brs.getStatus());
        brs = twt.getExtensionByName("branchStatus");
        assertNotNull(brs);
        assertEquals(BranchStatus.Status.IN_OPERATION, brs.getStatus());

        assertEquals(1, twt.getExtensions().size());
    }
}
