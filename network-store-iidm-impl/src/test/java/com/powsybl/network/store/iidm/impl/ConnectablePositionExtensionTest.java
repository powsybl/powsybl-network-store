/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import org.junit.Test;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetworkWithLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */

public class ConnectablePositionExtensionTest {
    @Test
    public void testModifyConnectablePositionExtensionOfBranch() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        Line l1 = network.getLine("L1");

        l1.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("l1")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .newFeeder2()
                .withName("l2")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        assertNotNull(l1.getExtension(ConnectablePosition.class).getFeeder1());
        assertNotNull(l1.getExtension(ConnectablePosition.class).getFeeder2());
        l1.getExtension(ConnectablePosition.class).getFeeder1().setName("l1Modified").setOrder(10);
        l1.getExtension(ConnectablePosition.class).getFeeder2().setName("l2Modified").setDirection(ConnectablePosition.Direction.BOTTOM);
        assertEquals("l1Modified", l1.getExtension(ConnectablePosition.class).getFeeder1().getName().orElseThrow());
        assertEquals("l2Modified", l1.getExtension(ConnectablePosition.class).getFeeder2().getName().orElseThrow());
        assertEquals(10, (int) l1.getExtension(ConnectablePosition.class).getFeeder1().getOrder().orElseThrow());
        assertEquals(ConnectablePosition.Direction.BOTTOM, l1.getExtension(ConnectablePosition.class).getFeeder2().getDirection());
    }

    @Test
    public void testModifyConnectablePositionExtensionOfInjection() {
        Network network = createNodeBreakerNetworkWithLine();
        Load load = network.getLoad("LD");

        load.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("ld")
                .withOrder(20)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        assertNull(load.getExtension(ConnectablePosition.class).getFeeder1());
        assertNull(load.getExtension(ConnectablePosition.class).getFeeder2());
        assertNull(load.getExtension(ConnectablePosition.class).getFeeder3());
        load.getExtension(ConnectablePosition.class).getFeeder().setName("ldModfied").setOrder(200);
        assertEquals("ldModfied", load.getExtension(ConnectablePosition.class).getFeeder().getName().orElseThrow());
        assertEquals(200, load.getExtension(ConnectablePosition.class).getFeeder().getOrder().orElseThrow());
    }

    @Test
    public void testModifyConnectablePositionExtensionOfThreeWindingsTransformer() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        ThreeWindingsTransformer twt1 = network.getThreeWindingsTransformer("TWT1");
        assertNotNull(twt1);

        twt1.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("twt1")
                .withOrder(10)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .newFeeder2()
                .withName("twt2")
                .withOrder(20)
                .withDirection(ConnectablePosition.Direction.BOTTOM)
                .add()
                .newFeeder3()
                .withName("twt3")
                .withOrder(30)
                .withDirection(ConnectablePosition.Direction.BOTTOM)
                .add()
                .add();

        assertNotNull(twt1.getExtension(ConnectablePosition.class).getFeeder1());
        assertNotNull(twt1.getExtension(ConnectablePosition.class).getFeeder2());
        assertNotNull(twt1.getExtension(ConnectablePosition.class).getFeeder3());
        twt1.getExtension(ConnectablePosition.class).getFeeder1().setName("twt1Modfied");
        twt1.getExtension(ConnectablePosition.class).getFeeder2().setDirection(ConnectablePosition.Direction.TOP);
        twt1.getExtension(ConnectablePosition.class).getFeeder3().setOrder(200);
        assertEquals("twt1Modfied", twt1.getExtension(ConnectablePosition.class).getFeeder1().getName().orElseThrow());
        assertEquals("twt2", twt1.getExtension(ConnectablePosition.class).getFeeder2().getName().orElseThrow());
        assertEquals("twt3", twt1.getExtension(ConnectablePosition.class).getFeeder3().getName().orElseThrow());
        assertEquals(10, twt1.getExtension(ConnectablePosition.class).getFeeder1().getOrder().orElseThrow());
        assertEquals(20, twt1.getExtension(ConnectablePosition.class).getFeeder2().getOrder().orElseThrow());
        assertEquals(200, twt1.getExtension(ConnectablePosition.class).getFeeder3().getOrder().orElseThrow());
        assertEquals(ConnectablePosition.Direction.TOP, twt1.getExtension(ConnectablePosition.class).getFeeder1().getDirection());
        assertEquals(ConnectablePosition.Direction.TOP, twt1.getExtension(ConnectablePosition.class).getFeeder2().getDirection());
        assertEquals(ConnectablePosition.Direction.BOTTOM, twt1.getExtension(ConnectablePosition.class).getFeeder3().getDirection());
    }
}
