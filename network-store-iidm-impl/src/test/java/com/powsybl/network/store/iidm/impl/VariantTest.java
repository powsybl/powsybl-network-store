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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VariantTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        assertNotNull(network.getVariantManager());

        // check there is only initial variant
        assertEquals(1, network.getVariantManager().getVariantIds().size());
        assertEquals(List.of(VariantManagerConstants.INITIAL_VARIANT_ID), network.getVariantManager().getVariantIds());
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, network.getVariantManager().getWorkingVariantId());

        // ensure gen target p is correct on initial variant
        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);
        assertEquals(607, gen.getTargetP(), 0);

        // create a new variant "v"
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "v");
        assertEquals(List.of(VariantManagerConstants.INITIAL_VARIANT_ID, "v"), network.getVariantManager().getVariantIds());

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
    }

    @Test
    public void multiThreadTest() throws Exception {
        Network network = EurostagTutorialExample1Factory.create();
        assertFalse(network.getVariantManager().isVariantMultiThreadAccessAllowed());
        network.getVariantManager().allowVariantMultiThreadAccess(true);
        assertTrue(network.getVariantManager().isVariantMultiThreadAccessAllowed());
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, network.getVariantManager().getWorkingVariantId());
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "v");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            network.getVariantManager().setWorkingVariant("v");
            assertEquals("v", network.getVariantManager().getWorkingVariantId());
            var load = network.getLoad("LOAD");
            load.setP0(601);
            assertEquals(601, load.getP0(), 0);
        });
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, network.getVariantManager().getWorkingVariantId());
        var load = network.getLoad("LOAD");
        assertEquals(600, load.getP0(), 0);
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }
}
