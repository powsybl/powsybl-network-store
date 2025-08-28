/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VariantTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        MockNetworkListener listener = new MockNetworkListener();
        network.addListener(listener);
        assertNotNull(network.getVariantManager());

        // check there is only initial variant
        assertEquals(1, network.getVariantManager().getVariantIds().size());
        assertEquals(Set.of(VariantManagerConstants.INITIAL_VARIANT_ID), network.getVariantManager().getVariantIds());
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, network.getVariantManager().getWorkingVariantId());

        // ensure gen target p is correct on initial variant
        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);
        assertEquals(607, gen.getTargetP(), 0);

        // create a new variant "v"
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "v");
        assertEquals(Set.of(VariantManagerConstants.INITIAL_VARIANT_ID, "v"), network.getVariantManager().getVariantIds());
        // check listeners are correctly notified
        assertEquals(1, listener.getNbCreatedVariant());

        // change gen target p on variant "v"
        network.getVariantManager().setWorkingVariant("v");
        assertEquals("v", network.getVariantManager().getWorkingVariantId());
        gen.setTargetP(608);
        assertEquals(608, gen.getTargetP(), 0);

        // check gen target p on initial variant has not been modified
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(607, gen.getTargetP(), 0);

        // check we can get back modified value for gen target p on "v" variant
        network.getVariantManager().setWorkingVariant("v");
        assertEquals(608, gen.getTargetP(), 0);

        // now we want a similar check but after "v" variant has been created
        Load load = network.getLoad("LOAD");
        assertNotNull(load);
        assertEquals(600, load.getP0(), 0);

        // change p0 value of load on initial variant
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(600, load.getP0(), 0);
        load.setP0(601);
        assertEquals(601, load.getP0(), 0);

        // check p0 value of load on "v" has not been modified
        network.getVariantManager().setWorkingVariant("v");
        assertEquals(600, load.getP0(), 0);

        // remove load in initial variant
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        load.remove();
        assertNull(network.getLoad("LOAD"));
        assertEquals(0, network.getLoadCount());
        PowsyblException e = assertThrows(PowsyblException.class, load::getP0);
        assertEquals("Object has been removed in current variant", e.getMessage());

        // check that load is still in variant "v"
        network.getVariantManager().setWorkingVariant("v");
        assertNotNull(network.getLoad("LOAD"));
        assertEquals(1, network.getLoadCount());
        assertSame(load, network.getLoad("LOAD"));
        assertEquals(600, load.getP0(), 0);
        assertNotNull(load.getTerminal().getVoltageLevel());

        // check that load is still removed in initial variant and ensure an exception is thrown when
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertNull(network.getLoad("LOAD"));
        e = assertThrows(PowsyblException.class, load::getP0);
        assertEquals("Object has been removed in current variant", e.getMessage());

        // check voltage level topology
        assertEquals(0, network.getVoltageLevel("VLLOAD").getLoadCount());
        network.getVoltageLevel("VLLOAD").visitEquipments(new DefaultTopologyVisitor() {
            @Override
            public void visitLoad(Load load) {
                fail();
            }
        });

        // Remove variant "v" while working on it
        // Should not fall back to initial variant
        network.getVariantManager().setWorkingVariant("v");
        network.getVariantManager().removeVariant("v");
        assertEquals(Set.of(VariantManagerConstants.INITIAL_VARIANT_ID), network.getVariantManager().getVariantIds());
        PowsyblException exception = assertThrows(PowsyblException.class, () -> network.getVariantManager().getWorkingVariantId());
        assertEquals("Variant index not set", exception.getMessage());
        // check listeners are correctly notified
        assertEquals(1, listener.getNbRemovedVariant());
    }

    @Test
    public void testCalculatedBus() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        Terminal gt = network.getGenerator("G").getTerminal();

        // create a new variant "v"
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "v");
        network.getVariantManager().setWorkingVariant("v");

        // recalculate buses on the new variant
        assertTrue(gt.isConnected());
        assertTrue(gt.disconnect());
        assertFalse(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());
        assertFalse(gt.isConnected());
        assertTrue(vl1.getNodeBreakerView().getSwitch("BR1").isOpen());
        assertEquals(3, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(3, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());

        // calculated buses on the initial variant not changed
        assertTrue(((VoltageLevelImpl) vl1).getResource().getAttributes().isCalculatedBusesValid());
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertTrue(gt.isConnected());
        assertFalse(vl1.getNodeBreakerView().getSwitch("BR1").isOpen());
        assertEquals(4, vl1.getBusBreakerView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(4, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
    }
}
