/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Ground;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.tck.AbstractGroundTest;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
class GroundTest extends AbstractGroundTest {

    @Test
    public void testOnSubnetwork() {
        // FIXME remove this test when subnetworks are implemented
    }

    @Test
    void assertGroundRemoval() {
        Network network = createNodeBreakerNetwokWithMultipleEquipments();
        Ground ground = network.getGround("ground");
        PowsyblException exception = assertThrows(PowsyblException.class, ground::connect);
        assertEquals("Invalid vertex -4", exception.getMessage());
        ground.remove();
        assertNull(network.getGround("ground"));
    }
}
