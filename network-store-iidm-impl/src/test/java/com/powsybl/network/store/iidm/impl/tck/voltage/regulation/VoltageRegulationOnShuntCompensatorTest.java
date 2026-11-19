/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.voltage.regulation;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.tck.voltage.regulation.AbstractVoltageRegulationOnShuntCompensatorTest;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.network.store.iidm.impl.tck.ExcludeTestsExtension;
import com.powsybl.network.store.iidm.impl.tck.VoltageRegulationTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExcludeTestsExtension.class)
class VoltageRegulationOnShuntCompensatorTest extends AbstractVoltageRegulationOnShuntCompensatorTest {
    //FIXME: Override of testNotifyUpdate(). To be removed when testNotifyUpdate() asserts on values, not reference
    @Test
    void testNotifyUpdateByValues() {
        Network network = BatteryNetworkFactory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("VLGEN");
        Terminal remoteTerminal = network.getBattery("BAT").getTerminal();
        ShuntCompensator shuntCompensator = voltageLevel.newShuntCompensator()
                .setId("shuntCompensator")
                .setBus("NGEN")
                .setConnectableBus("NGEN")
                .setSectionCount(1)
                .setLocalTargetV(220)
                .newVoltageRegulation()
                    .withRegulating(true)
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(220)
                    .withTerminal(remoteTerminal)
                    .withTargetDeadband(10)
                    .add()
                .newLinearModel()
                    .setBPerSection(1)
                    .setGPerSection(0)
                    .setMaximumSectionCount(1)
                    .add()
                .add();
        VoltageRegulationTestUtils.assertVoltageRegulationRemovalEventByValue(network, shuntCompensator, shuntCompensator.getId());
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
