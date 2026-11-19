/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractBatteryTest;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ExtendWith(ExcludeTestsExtension.class)
class BatteryTest extends AbstractBatteryTest {

    //FIXME: Override of testNewVoltageRegulationInMultiVariants(). To be removed when testNewVoltageRegulationInMultiVariants() asserts on values, not reference
    @Test
    void testNewVoltageRegulationInMultiVariantsByValues() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getVoltageLevel("VLBAT").newBattery()
                .setId("testMultiVariant")
                .setTargetP(11)
                .setLocalTargetQ(12)
                .setMinP(10)
                .setMaxP(20)
                .setBus("NBAT")
                .add();

        VoltageRegulationTestUtils.assertNewVoltageRegulationInMultiVariants(network, battery);
    }
}
