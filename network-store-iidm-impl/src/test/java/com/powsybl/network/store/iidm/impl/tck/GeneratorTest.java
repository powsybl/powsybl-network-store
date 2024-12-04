/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractGeneratorTest;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorTest extends AbstractGeneratorTest {

    @Override
    public void testRemove() {
        // FIXME remove this test when exception msg are homogenized with the powsybl-core
    }

    @Test
    void testRegulationWhenRegulatedElementIsRemoved() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        // initialization
        Generator generator = network.getGenerator("GH3");
        Load load = network.getLoad("LD1");
        Assertions.assertTrue(generator.isVoltageRegulatorOn());
        Assertions.assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        Assertions.assertEquals(400, generator.getTargetV());

        // set the generator's regulation on the load terminal (both equipments are not on the same voltage level/bus)
        generator.setRegulatingTerminal(load.getTerminal());
        generator.setTargetV(225);
        Assertions.assertTrue(generator.isVoltageRegulatorOn());
        Assertions.assertEquals(load.getTerminal(), generator.getRegulatingTerminal());
        Assertions.assertEquals(225, generator.getTargetV());

        // remove the load
        network.getLoad("LD1").remove();
        Assertions.assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        Assertions.assertFalse(generator.isVoltageRegulatorOn());
    }

    @Test
    void testRegulationWhenRegulatedElementOnSameBusIsRemoved() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        // initialization
        Generator generator = network.getGenerator("GH3");
        Load load = network.getLoad("LD2");
        Assertions.assertTrue(generator.isVoltageRegulatorOn());
        Assertions.assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());

        // set the generator's regulation on the load terminal (both equipments are on the same voltage level/bus)
        generator.setRegulatingTerminal(load.getTerminal());
        Assertions.assertTrue(generator.isVoltageRegulatorOn());
        Assertions.assertEquals(load.getTerminal(), generator.getRegulatingTerminal());

        // remove the load
        network.getLoad("LD2").remove();
        Assertions.assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        Assertions.assertTrue(generator.isVoltageRegulatorOn());
    }
}
