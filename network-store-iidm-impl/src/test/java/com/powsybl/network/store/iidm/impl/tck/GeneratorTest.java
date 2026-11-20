/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.tck.AbstractGeneratorTest;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ExtendWith(ExcludeTestsExtension.class)
class GeneratorTest extends AbstractGeneratorTest {

    @Test
    void testRemoveVoltageRegulationPerVariant() {
        Network network = FictitiousSwitchFactory.create();
        Generator generator = network.getGenerator("CB");
        generator.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .build();

        VoltageRegulationTestUtils.assertRemoveVoltageRegulationPerVariant(network, generator);
    }
}
