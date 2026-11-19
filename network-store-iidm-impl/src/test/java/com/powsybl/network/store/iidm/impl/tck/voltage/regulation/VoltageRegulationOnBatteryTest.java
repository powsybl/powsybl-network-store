/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.voltage.regulation;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.tck.voltage.regulation.AbstractVoltageRegulationOnBatteryTest;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.network.store.iidm.impl.tck.ExcludeTestsExtension;
import com.powsybl.network.store.iidm.impl.tck.VoltageRegulationTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExcludeTestsExtension.class)
class VoltageRegulationOnBatteryTest extends AbstractVoltageRegulationOnBatteryTest {

    //FIXME: Override of testNotifyUpdate(). To be removed when testNotifyUpdate() asserts on values, not reference
    @Test
    void testNotifyUpdateByValues() {
        Network network = BatteryNetworkFactory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("VLGEN");
        Terminal remoteTerminal = network.getBattery("BAT").getTerminal();
        Battery battery = voltageLevel.newBattery()
                .setId("battery")
                .setBus("NGEN")
                .setConnectableBus("NGEN")
                .setMinP(12)
                .setMaxP(120)
                .setTargetP(100)
                .setLocalTargetV(220)
                .setLocalTargetQ(10)
                .newVoltageRegulation()
                    .withRegulating(true)
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(220)
                    .withTerminal(remoteTerminal)
                    .add()
                .add();
        VoltageRegulationTestUtils.assertVoltageRegulationRemovalEventByValue(network, battery, battery.getId());
    }

    @Override
    public void testRemoveTerminalOnMultiVariantThrowException() {
        // network store iidm implementation supports structure modification in variants. Should not throw exception.
    }

    @Override
    public void testMergeWithTerminalInMultiVariant() {
        // network store iidm implementation supports structure modification in variants. Should not throw exception.
    }

    //TODO in this PR: add multi variant tests
}
