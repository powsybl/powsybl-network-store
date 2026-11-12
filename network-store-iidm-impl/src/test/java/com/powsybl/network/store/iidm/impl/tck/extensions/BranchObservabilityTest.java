/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractBranchObservabilityTest;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BranchObservabilityTest extends AbstractBranchObservabilityTest {

    //FIXME: to be removed when tck test does not check object instance are equals, but fields are equals
    @Test
    public void test() {
        Network network = BatteryNetworkFactory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        assertNotNull(line);
        line.newExtension(BranchObservabilityAdder.class)
                .withObservable(true)
                .withStandardDeviationP1(0.02d)
                .withStandardDeviationP2(0.04d)
                .withRedundantP1(true)
                .withRedundantP2(false)
                .withStandardDeviationQ1(0.5d)
                .withStandardDeviationQ2(1.0d)
                .withRedundantQ1(true)
                .withRedundantQ2(false)
                .add();
        BranchObservability<Line> branchObservability = line.getExtension(BranchObservability.class);
        assertEquals("branchObservability", branchObservability.getName());
        assertEquals("NHV1_NHV2_1", branchObservability.getExtendable().getId());

        assertTrue(branchObservability.isObservable());
        branchObservability.setObservable(false);
        assertFalse(branchObservability.isObservable());

        // P
        assertTrue(branchObservability.getQualityP1().isPresent());
        assertTrue(branchObservability.getQualityP2().isPresent());
        assertEquals(0.02d, branchObservability.getNullableQualityP1().getStandardDeviation(), 0d);
        branchObservability.getNullableQualityP1().setStandardDeviation(0.03d);
        assertEquals(0.03d, branchObservability.getNullableQualityP1().getStandardDeviation(), 0d);
        assertEquals(0.04d, branchObservability.getNullableQualityP2().getStandardDeviation(), 0d);
        branchObservability.getNullableQualityP2().setStandardDeviation(0.08d);
        assertEquals(0.08d, branchObservability.getNullableQualityP2().getStandardDeviation(), 0d);

        assertTrue(branchObservability.getNullableQualityP1().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityP1().isRedundant().get());
        branchObservability.getNullableQualityP1().setRedundant(false);
        assertTrue(branchObservability.getNullableQualityP1().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityP1().isRedundant().get());
        assertTrue(branchObservability.getNullableQualityP2().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityP2().isRedundant().get());
        branchObservability.getNullableQualityP2().setRedundant(true);
        assertTrue(branchObservability.getNullableQualityP2().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityP2().isRedundant().get());

        // Q
        assertTrue(branchObservability.getQualityQ1().isPresent());
        assertTrue(branchObservability.getQualityQ2().isPresent());
        assertEquals(0.5d, branchObservability.getNullableQualityQ1().getStandardDeviation(), 0d);
        branchObservability.getNullableQualityQ1().setStandardDeviation(0.6d);
        assertEquals(0.6d, branchObservability.getNullableQualityQ1().getStandardDeviation(), 0d);
        assertEquals(1.0d, branchObservability.getNullableQualityQ2().getStandardDeviation(), 0d);
        branchObservability.getNullableQualityQ2().setStandardDeviation(1.01d);
        assertEquals(1.01d, branchObservability.getNullableQualityQ2().getStandardDeviation(), 0d);

        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().get());
        branchObservability.getNullableQualityQ1().setRedundant(false);
        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityQ1().isRedundant().get());
        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityQ1().isRedundant().get());
        assertTrue(branchObservability.getNullableQualityQ2().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityQ2().isRedundant().get());
        branchObservability.getNullableQualityQ2().setRedundant(true);
        assertTrue(branchObservability.getNullableQualityQ2().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityQ2().isRedundant().get());
    }
}
