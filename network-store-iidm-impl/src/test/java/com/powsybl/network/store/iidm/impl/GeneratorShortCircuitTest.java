/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer@rte-france.com>}
 */
class GeneratorShortCircuitTest {
    @Test
    void test2() {
        Network network = EurostagTutorialExample1Factory.create();
        Generator gen = network.getGenerator("GEN");
        assertNotNull(gen);
        GeneratorShortCircuit generatorShortCircuit = gen.newExtension(GeneratorShortCircuitAdder.class)
                .withDirectTransX(20)
                .withDirectSubtransX(20)
                .withStepUpTransformerX(20)
                .add();
        assertEquals(20, generatorShortCircuit.getDirectTransX(), 0);
        assertEquals(20, generatorShortCircuit.getDirectSubtransX(), 0);
        assertEquals(20, generatorShortCircuit.getStepUpTransformerX(), 0);
        generatorShortCircuit.setDirectTransX(10);
        assertEquals(10, generatorShortCircuit.getDirectTransX(), 0);
        generatorShortCircuit.setDirectSubtransX(30);
        assertEquals(30, generatorShortCircuit.getDirectSubtransX(), 0);
        generatorShortCircuit.setStepUpTransformerX(10);
        assertEquals(10, generatorShortCircuit.getStepUpTransformerX(), 0);
    }
}
