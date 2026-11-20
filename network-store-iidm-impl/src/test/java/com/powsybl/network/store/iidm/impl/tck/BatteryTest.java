/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.tck.AbstractBatteryTest;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ExtendWith(ExcludeTestsExtension.class)
class BatteryTest extends AbstractBatteryTest {

    @Test
    void testRemoveVoltageRegulationPerVariant() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        battery.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .build();

        VoltageRegulationTestUtils.assertRemoveVoltageRegulationPerVariant(network, battery);
    }
}
