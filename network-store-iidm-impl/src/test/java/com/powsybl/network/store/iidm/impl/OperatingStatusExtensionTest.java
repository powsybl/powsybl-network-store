/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.extensions.OperatingStatusAdder;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        OperatingStatusAdder operatingStatusAdder = line.newExtension(OperatingStatusAdder.class);
        assertThrows(NullPointerException.class, () -> operatingStatusAdder.withStatus(null));
        operatingStatusAdder.withStatus(OperatingStatus.Status.PLANNED_OUTAGE).add();

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

        OperatingStatusAdder operatingStatusAdder = twt.newExtension(OperatingStatusAdder.class);
        assertThrows(NullPointerException.class, () -> operatingStatusAdder.withStatus(null));
        operatingStatusAdder.withStatus(OperatingStatus.Status.FORCED_OUTAGE).add();

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

        OperatingStatusAdder operatingStatusAdder = twt.newExtension(OperatingStatusAdder.class);
        assertThrows(NullPointerException.class, () -> operatingStatusAdder.withStatus(null));
        operatingStatusAdder.withStatus(OperatingStatus.Status.IN_OPERATION).add();

        OperatingStatus brs = twt.getExtension(OperatingStatus.class);
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.IN_OPERATION, brs.getStatus());
        brs = twt.getExtensionByName("operatingStatus");
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.IN_OPERATION, brs.getStatus());

        assertEquals(1, twt.getExtensions().size());
    }

    @Test
    public void testDanglingLineOperatingStatusExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        DanglingLine dl = network.getDanglingLine("DL1");
        assertNotNull(dl);

        assertNull(dl.getExtension(OperatingStatus.class));
        assertEquals(0, dl.getExtensions().size());

        OperatingStatusAdder operatingStatusAdder = dl.newExtension(OperatingStatusAdder.class);
        assertThrows(NullPointerException.class, () -> operatingStatusAdder.withStatus(null));
        operatingStatusAdder.withStatus(OperatingStatus.Status.FORCED_OUTAGE).add();

        OperatingStatus brs = dl.getExtension(OperatingStatus.class);
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.FORCED_OUTAGE, brs.getStatus());
        brs = dl.getExtensionByName("operatingStatus");
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.FORCED_OUTAGE, brs.getStatus());

        assertEquals(1, dl.getExtensions().size());
    }

    @Test
    public void testTieLineOperatingStatusExtension() {
        Network network = CreateNetworksUtil.createDummyNodeBreakerWithTieLineNetwork();

        TieLine tl = network.getTieLine("TL");
        assertNotNull(tl);

        assertNull(tl.getExtension(OperatingStatus.class));
        assertEquals(0, tl.getExtensions().size());

        OperatingStatusAdder operatingStatusAdder = tl.newExtension(OperatingStatusAdder.class);
        assertThrows(NullPointerException.class, () -> operatingStatusAdder.withStatus(null));
        operatingStatusAdder.withStatus(OperatingStatus.Status.FORCED_OUTAGE).add();

        OperatingStatus brs = tl.getExtension(OperatingStatus.class);
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.FORCED_OUTAGE, brs.getStatus());
        brs = tl.getExtensionByName("operatingStatus");
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.FORCED_OUTAGE, brs.getStatus());

        assertEquals(1, tl.getExtensions().size());
    }

    @Test
    public void testHvdcLineOperatingStatusExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetwokWithMultipleEquipments();

        HvdcLine hvdcLine = network.getHvdcLine("HVDC1");
        assertNotNull(hvdcLine);

        assertNull(hvdcLine.getExtension(OperatingStatus.class));
        assertEquals(0, hvdcLine.getExtensions().size());

        OperatingStatusAdder operatingStatusAdder = hvdcLine.newExtension(OperatingStatusAdder.class);
        assertThrows(NullPointerException.class, () -> operatingStatusAdder.withStatus(null));
        operatingStatusAdder.withStatus(OperatingStatus.Status.FORCED_OUTAGE).add();

        OperatingStatus brs = hvdcLine.getExtension(OperatingStatus.class);
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.FORCED_OUTAGE, brs.getStatus());
        brs = hvdcLine.getExtensionByName("operatingStatus");
        assertNotNull(brs);
        assertEquals(OperatingStatus.Status.FORCED_OUTAGE, brs.getStatus());

        assertEquals(1, hvdcLine.getExtensions().size());
    }

    @Test
    public void testRemoveExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        DummyNetworkListener listener = new DummyNetworkListener();
        network.addListener(listener);

        Line l1 = network.getLine("L1");
        l1.newExtension(OperatingStatusAdder.class)
                .withStatus(OperatingStatus.Status.PLANNED_OUTAGE)
                .add();

        assertEquals(OperatingStatus.Status.PLANNED_OUTAGE, l1.getExtension(OperatingStatus.class).getStatus());

        l1.removeExtension(OperatingStatus.class);
        assertNull(l1.getExtension(OperatingStatus.class));

    }

    private class DummyNetworkListener implements NetworkListener {

        @Override
        public void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        }

        @Override
        public void onExtensionBeforeRemoval(Extension<?> extension) {
        }

        @Override
        public void onCreation(Identifiable identifiable) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        @Override
        public void beforeRemoval(Identifiable identifiable) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        @Override
        public void afterRemoval(String id) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        @Override
        public void onUpdate(Identifiable identifiable, String s, Object o, Object o1) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        @Override
        public void onVariantCreated(String sourceVariantId, String targetVariantId) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        @Override
        public void onVariantRemoved(String variantId) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        public int getNbCreatedVariant() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        public int getNbRemovedVariant() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        @Override
        public void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue,
                             Object newValue) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
        }

        @Override
        public void onExtensionCreation(Extension<?> extension) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onExtensionCreation'");
        }

        @Override
        public void onExtensionUpdate(Extension<?> extendable, String attribute, Object oldValue, Object newValue) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onExtensionUpdate'");
        }
    }
}
