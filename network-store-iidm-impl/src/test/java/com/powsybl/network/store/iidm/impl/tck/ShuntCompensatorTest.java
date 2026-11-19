/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.tck.AbstractShuntCompensatorTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ExtendWith(ExcludeTestsExtension.class)
class ShuntCompensatorTest extends AbstractShuntCompensatorTest {
    @Override
    public void baseLinearShuntTest() {
        // FIXME remove this test when we use the release containing this PR : https://github.com/powsybl/powsybl-core/pull/2847
    }

    //FIXME: Override of testNewVoltageRegulationInMultiVariants(). To be removed when testNewVoltageRegulationInMultiVariants() asserts on values, not reference
    @Test
    void testNewVoltageRegulationInMultiVariantsByValues() {
        Network network = NoEquipmentNetworkFactory.create();
        network.getVoltageLevel("vl2").newLoad()
                .setId("load")
                .setBus("busB")
                .setConnectableBus("busB")
                .setP0(0)
                .setQ0(0)
                .add();
        Terminal terminal = network.getLoad("load").getTerminal();
        VoltageLevel voltageLevel = network.getVoltageLevel("vl1");
        ShuntCompensator shunt = voltageLevel.newShuntCompensator()
                .setId("testMultiVariant")
                .setName("shuntName")
                .setConnectableBus("busA")
                .setSectionCount(6)
                .setLocalTargetV(200)
                .newVoltageRegulation()
                    .withTerminal(terminal)
                    .withRegulating(true)
                    .withTargetValue(200)
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetDeadband(10)
                    .add()
                .newLinearModel()
                    .setBPerSection(5)
                    .setGPerSection(4)
                    .setMaximumSectionCount(10)
                    .add()
                .add();

        VoltageRegulationTestUtils.assertNewVoltageRegulationInMultiVariants(network, shunt);
    }
}
