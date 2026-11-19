/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.voltage.regulation;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.voltage.regulation.AbstractVoltageRegulationOnGeneratorTest;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.network.store.iidm.impl.tck.ExcludeTestsExtension;
import com.powsybl.network.store.iidm.impl.tck.VoltageRegulationTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExcludeTestsExtension.class)
class VoltageRegulationOnGeneratorTest extends AbstractVoltageRegulationOnGeneratorTest {

    //FIXME: Override of testNotifyUpdate(). To be removed when testNotifyUpdate() asserts on values, not reference
    @Test
    void testNotifyUpdateByValues() {
        Network network = BatteryNetworkFactory.create();
        Generator generator = network.getVoltageLevel("VLGEN").getGeneratorStream().toList().getFirst();
        VoltageRegulationTestUtils.assertVoltageRegulationRemovalEventByValue(network, generator, generator.getId());
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
