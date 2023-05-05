/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.tck.AbstractLoadTest;
import org.junit.Test;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetworkWithLine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadTest extends AbstractLoadTest {

    @Test
    public void testSetterGetterInMultiVariants() {
        // FIXME variant difference with core
    }

    @Test
    public void testAddConnectablePositionExtension() {
        Network network = createNodeBreakerNetworkWithLine();
        Load load = network.getLoad("LD");

        load.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("cpa")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        assertEquals("cpa", load.getExtension(ConnectablePosition.class).getFeeder().getName().orElseThrow());
        assertNull(load.getExtension(ConnectablePosition.class).getFeeder1());
        assertNull(load.getExtension(ConnectablePosition.class).getFeeder2());
        assertNull(load.getExtension(ConnectablePosition.class).getFeeder3());
    }
}
