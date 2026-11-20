/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.network.store.iidm.impl.CachedNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.CreateNetworksUtil;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.iidm.impl.OfflineNetworkStoreClient;
import org.junit.jupiter.api.Test;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetworkWithLine;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */

class ConnectablePositionExtensionTest {
    @Test
    void testModifyConnectablePositionExtensionOfBranch() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        Line l1 = network.getLine("L1");

        l1.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("l1")
                .withOrder(1)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .newFeeder2()
                .withName("l2")
                .withOrder(2)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        assertNotNull(l1.getExtension(ConnectablePosition.class).getFeeder1());
        assertNotNull(l1.getExtension(ConnectablePosition.class).getFeeder2());
        l1.getExtension(ConnectablePosition.class).getFeeder1().setName("l1Modified").setOrder(10).setDirection(ConnectablePosition.Direction.BOTTOM);
        l1.getExtension(ConnectablePosition.class).getFeeder2().setName("l2Modified").setOrder(20).setDirection(ConnectablePosition.Direction.BOTTOM);
        assertEquals("l1Modified", l1.getExtension(ConnectablePosition.class).getFeeder1().getName().orElseThrow());
        assertEquals("l2Modified", l1.getExtension(ConnectablePosition.class).getFeeder2().getName().orElseThrow());
        assertEquals(10, (int) l1.getExtension(ConnectablePosition.class).getFeeder1().getOrder().orElseThrow());
        assertEquals(20, (int) l1.getExtension(ConnectablePosition.class).getFeeder2().getOrder().orElseThrow());
        assertEquals(ConnectablePosition.Direction.BOTTOM, l1.getExtension(ConnectablePosition.class).getFeeder1().getDirection());
        assertEquals(ConnectablePosition.Direction.BOTTOM, l1.getExtension(ConnectablePosition.class).getFeeder2().getDirection());
    }

    @Test
    void testModifyConnectablePositionExtensionOfInjection() {
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
        load.getExtension(ConnectablePosition.class).getFeeder().setName("ldModfied").setOrder(200)
                .setDirection(ConnectablePosition.Direction.BOTTOM);
        assertEquals("ldModfied", load.getExtension(ConnectablePosition.class).getFeeder().getName().orElseThrow());
        assertEquals(200, load.getExtension(ConnectablePosition.class).getFeeder().getOrder().orElseThrow());
        assertEquals(ConnectablePosition.Direction.BOTTOM, load.getExtension(ConnectablePosition.class).getFeeder().getDirection());
    }

    @Test
    void testModifyConnectablePositionExtensionOfThreeWindingsTransformer() {
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
        twt1.getExtension(ConnectablePosition.class).getFeeder1().setName("twt1Modfied").setOrder(100).setDirection(ConnectablePosition.Direction.BOTTOM);
        twt1.getExtension(ConnectablePosition.class).getFeeder2().setName("twt2Modfied").setOrder(200).setDirection(ConnectablePosition.Direction.TOP);
        twt1.getExtension(ConnectablePosition.class).getFeeder3().setName("twt3Modfied").setOrder(300).setDirection(ConnectablePosition.Direction.TOP);
        assertEquals("twt1Modfied", twt1.getExtension(ConnectablePosition.class).getFeeder1().getName().orElseThrow());
        assertEquals("twt2Modfied", twt1.getExtension(ConnectablePosition.class).getFeeder2().getName().orElseThrow());
        assertEquals("twt3Modfied", twt1.getExtension(ConnectablePosition.class).getFeeder3().getName().orElseThrow());
        assertEquals(100, twt1.getExtension(ConnectablePosition.class).getFeeder1().getOrder().orElseThrow());
        assertEquals(200, twt1.getExtension(ConnectablePosition.class).getFeeder2().getOrder().orElseThrow());
        assertEquals(300, twt1.getExtension(ConnectablePosition.class).getFeeder3().getOrder().orElseThrow());
        assertEquals(ConnectablePosition.Direction.BOTTOM, twt1.getExtension(ConnectablePosition.class).getFeeder1().getDirection());
        assertEquals(ConnectablePosition.Direction.TOP, twt1.getExtension(ConnectablePosition.class).getFeeder2().getDirection());
        assertEquals(ConnectablePosition.Direction.TOP, twt1.getExtension(ConnectablePosition.class).getFeeder3().getDirection());
    }

    @Test
    void testRemoveOrderOfBranch() {
        NetworkStoreClient client = spy(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()));
        Network network = FourSubstationsNodeBreakerFactory.create(new NetworkFactoryImpl(() -> client));
        Line l1 = network.getLine("LINE_S2S3");
        l1.newExtension(ConnectablePositionAdder.class)
                .newFeeder1().withName("l1").withOrder(1).withDirection(ConnectablePosition.Direction.TOP).add()
                .newFeeder2().withName("l2").withOrder(2).withDirection(ConnectablePosition.Direction.TOP).add()
                .add();
        ConnectablePosition.Feeder feeder1 = l1.getExtension(ConnectablePosition.class).getFeeder1();
        assertTrue(feeder1.getOrder().isPresent());
        clearInvocations(client);
        feeder1.removeOrder();
        assertTrue(feeder1.getOrder().isEmpty());
        // check network store client is actually updating resource when removing order
        verify(client).updateLines(any(), anyList(), any());
    }

    @Test
    void testRemoveOrderOfInjection() {
        NetworkStoreClient client = spy(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()));
        Network network = FourSubstationsNodeBreakerFactory.create(new NetworkFactoryImpl(() -> client));
        Load load = network.getLoad("LD1");
        load.newExtension(ConnectablePositionAdder.class)
                .newFeeder().withName("ld").withOrder(20).withDirection(ConnectablePosition.Direction.TOP).add()
                .add();
        ConnectablePosition.Feeder feeder = load.getExtension(ConnectablePosition.class).getFeeder();
        clearInvocations(client);
        feeder.removeOrder();
        assertTrue(feeder.getOrder().isEmpty());
        // check network store client is actually updating resource when removing order
        verify(client).updateLoads(any(), anyList(), any());
    }

    @Test
    void testRemoveOrderOfThreeWindingsTransformer() {
        NetworkStoreClient client = spy(new CachedNetworkStoreClient(new OfflineNetworkStoreClient()));
        Network network = ThreeWindingsTransformerNetworkFactory.create(new NetworkFactoryImpl(() -> client));
        ThreeWindingsTransformer twt1 = network.getThreeWindingsTransformer("3WT");
        twt1.newExtension(ConnectablePositionAdder.class)
                .newFeeder1().withName("twt1").withOrder(10).withDirection(ConnectablePosition.Direction.TOP).add()
                .newFeeder2().withName("twt2").withOrder(20).withDirection(ConnectablePosition.Direction.BOTTOM).add()
                .newFeeder3().withName("twt3").withOrder(30).withDirection(ConnectablePosition.Direction.BOTTOM).add()
                .add();
        ConnectablePosition.Feeder feeder1 = twt1.getExtension(ConnectablePosition.class).getFeeder1();
        clearInvocations(client);
        feeder1.removeOrder();
        assertTrue(feeder1.getOrder().isEmpty());
        // check network store client is actually updating resource when removing order
        verify(client).updateThreeWindingsTransformers(any(), anyList(), any());
    }
}
