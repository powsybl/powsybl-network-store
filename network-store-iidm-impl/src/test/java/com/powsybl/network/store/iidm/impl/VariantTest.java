/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VariantTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        assertNotNull(network.getVariantManager());
        assertEquals(1, network.getVariantManager().getVariantIds().size());
        assertEquals(List.of(VariantManagerConstants.INITIAL_VARIANT_ID), network.getVariantManager().getVariantIds());
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, network.getVariantManager().getWorkingVariantId());
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "v");
        assertEquals(List.of(VariantManagerConstants.INITIAL_VARIANT_ID, "v"), network.getVariantManager().getVariantIds());
        network.getVariantManager().setWorkingVariant("v");
        assertEquals("v", network.getVariantManager().getWorkingVariantId());
    }
}
