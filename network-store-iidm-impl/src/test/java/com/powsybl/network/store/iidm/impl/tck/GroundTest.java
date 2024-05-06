/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractGroundTest;
import com.powsybl.iidm.network.test.TwoVoltageLevelNetworkFactory;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
class GroundTest extends AbstractGroundTest {
    @Test
    void test() {
        // FIXME
    }

    @Test
    void testOnSubnetwork() {
        // FIXME
    }

    @Test
    void groundRemoval() {
        Network network = TwoVoltageLevelNetworkFactory.createWithGrounds();
        network.getGround("GroundNB").remove();
        assertNull(network.getGround("GroundNB"));
    }
}
