/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Ground;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments;
import static org.junit.Assert.assertNull;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */
public class GroundTest {

    @Test
    public void assertGroundRemoval() {
        Network network = createNodeBreakerNetwokWithMultipleEquipments();
        Ground ground = network.getGround("ground");
        ground.remove();
        assertNull(network.getGround("ground"));
    }
}
